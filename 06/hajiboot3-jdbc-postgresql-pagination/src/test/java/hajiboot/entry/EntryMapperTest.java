package hajiboot.entry;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import hajiboot.pagination.CursorPage;
import hajiboot.pagination.CursorPageRequest;
import hajiboot.pagination.CursorPageRequest.Navigation;
import hajiboot.pagination.OffsetPage;
import hajiboot.pagination.OffsetPageRequest;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest(properties = {
		"logging.level.sql=DEBUG",
		"logging.level.org.springframework.jdbc.support.JdbcTransactionManager=DEBUG",
		"spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
		"spring.datasource.url=jdbc:tc:postgresql:14-alpine:///databasename"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(EntryMapper.class)
class EntryMapperTest {
	@Autowired
	EntryMapper entryMapper;

	Entry fixture(Integer entryId) {
		return new Entry(entryId, "test title", "test content",
				Set.of(new Tag("a"), new Tag("b"), new Tag("c")),
				new UpdateMeta("test", Instant.now()),
				new UpdateMeta("test", Instant.now()));
	}

	@Test
	void insertAndFindOne() {
		Integer entryId = this.entryMapper.nextEntryId();
		Entry entry = fixture(entryId);
		int inserted = this.entryMapper.insert(entry);
		assertThat(inserted).isEqualTo(1);
		Optional<Entry> found = this.entryMapper.findOne(entryId);
		assertThat(found.isPresent()).isTrue();
		assertThat(found.get()).isEqualTo(entry);
	}

	@Test
	void findOne_empty() {
		Optional<Entry> found = this.entryMapper.findOne(9999);
		assertThat(found.isEmpty()).isTrue();
	}

	@Test
	void insertAndUpdateAndFind() {
		Integer entryId = this.entryMapper.nextEntryId();
		Entry entry = fixture(entryId);
		this.entryMapper.insert(entry);
		Entry modified = entry.withContent("update")
				.withLastModified(entry.lastModified().withDate(Instant.now()));
		int updated = this.entryMapper.update(modified);
		assertThat(updated).isEqualTo(1);
		Optional<Entry> found = this.entryMapper.findOne(entryId);
		assertThat(found.isPresent()).isTrue();
		assertThat(found.get()).isEqualTo(modified);
	}

	@Test
	void insertAndDeleteAndFind() {
		Integer entryId = this.entryMapper.nextEntryId();
		Entry entry = fixture(entryId);
		this.entryMapper.insert(entry);
		int deleted = this.entryMapper.delete(entryId);
		assertThat(deleted).isEqualTo(1);
		Optional<Entry> found = this.entryMapper.findOne(entryId);
		assertThat(found.isEmpty()).isTrue();
	}


	@Test
	void insertAllAndFindAll() {
		Entry entry1 = fixture(this.entryMapper.nextEntryId());
		Entry entry2 = fixture(this.entryMapper.nextEntryId());
		Entry entry3 = fixture(this.entryMapper.nextEntryId());
		int inserted = this.entryMapper.insertAll(List.of(entry1, entry2, entry3));
		assertThat(inserted).isEqualTo(3);
		List<Entry> entries = this.entryMapper.findAll();
		assertThat(entries).containsExactly(entry3.withContent(""), entry2.withContent(""), entry1.withContent(""));
	}

	@Test
	void findByTags() {
		Entry entry1 = fixture(this.entryMapper.nextEntryId())
				.withTags(Set.of(new Tag("a"), new Tag("b")));
		Entry entry2 = fixture(this.entryMapper.nextEntryId())
				.withTags(Set.of(new Tag("b"), new Tag("c")));
		Entry entry3 = fixture(this.entryMapper.nextEntryId())
				.withTags(Set.of(new Tag("c"), new Tag("a")));
		this.entryMapper.insertAll(List.of(entry1, entry2, entry3));

		assertThat(this.entryMapper.findByTag(new Tag("a")))
				.containsExactly(entry3.withContent(""), entry1.withContent(""));
		assertThat(this.entryMapper.findByTag(new Tag("b")))
				.containsExactly(entry2.withContent(""), entry1.withContent(""));
		assertThat(this.entryMapper.findByTag(new Tag("c")))
				.containsExactly(entry3.withContent(""), entry2.withContent(""));
	}

	@Test
	void count() {
		assertThat(this.entryMapper.count()).isEqualTo(0);

		Entry entry1 = fixture(this.entryMapper.nextEntryId())
				.withTags(Set.of(new Tag("a"), new Tag("b")));
		Entry entry2 = fixture(this.entryMapper.nextEntryId())
				.withTags(Set.of(new Tag("b"), new Tag("c")));
		Entry entry3 = fixture(this.entryMapper.nextEntryId())
				.withTags(Set.of(new Tag("c"), new Tag("a")));
		this.entryMapper.insertAll(List.of(entry1, entry2, entry3));

		assertThat(this.entryMapper.count()).isEqualTo(3);
	}

	@Test
	void findAllOffset_empty() {
		OffsetPage<Entry> page = this.entryMapper.findAll(new OffsetPageRequest(0, 10));
		assertThat(page.content()).isEmpty();
		assertThat(page.number()).isEqualTo(0);
		assertThat(page.size()).isEqualTo(10);
		assertThat(page.hasNext()).isFalse();
		assertThat(page.hasPrevious()).isFalse();
		assertThat(page.totalPages()).isEqualTo(0);
		assertThat(page.totalElements()).isEqualTo(0);
	}

	@Test
	void findAllOffset() {
		Entry entry1 = fixture(this.entryMapper.nextEntryId());
		Entry entry2 = fixture(this.entryMapper.nextEntryId());
		Entry entry3 = fixture(this.entryMapper.nextEntryId());
		this.entryMapper.insertAll(List.of(entry1, entry2, entry3));

		OffsetPage<Entry> page0 = this.entryMapper.findAll(new OffsetPageRequest(0, 2));
		assertThat(page0.content()).containsExactly(entry3.withContent(""), entry2.withContent(""));
		assertThat(page0.number()).isEqualTo(0);
		assertThat(page0.size()).isEqualTo(2);
		assertThat(page0.hasNext()).isTrue();
		assertThat(page0.hasPrevious()).isFalse();
		assertThat(page0.totalPages()).isEqualTo(2);
		assertThat(page0.totalElements()).isEqualTo(3);

		OffsetPage<Entry> page1 = this.entryMapper.findAll(new OffsetPageRequest(1, 2));
		assertThat(page1.content()).containsExactly(entry1.withContent(""));
		assertThat(page1.number()).isEqualTo(1);
		assertThat(page1.size()).isEqualTo(2);
		assertThat(page1.hasNext()).isFalse();
		assertThat(page1.hasPrevious()).isTrue();
		assertThat(page1.totalPages()).isEqualTo(2);
		assertThat(page1.totalElements()).isEqualTo(3);

		OffsetPage<Entry> page2 = this.entryMapper.findAll(new OffsetPageRequest(2, 2));
		assertThat(page2.content()).isEmpty();
		assertThat(page2.number()).isEqualTo(2);
		assertThat(page2.size()).isEqualTo(2);
		assertThat(page2.hasNext()).isFalse();
		assertThat(page2.hasPrevious()).isTrue();
		assertThat(page2.totalPages()).isEqualTo(2);
		assertThat(page2.totalElements()).isEqualTo(3);
	}

	@Test
	void findAllCursorNext_empty() {
		CursorPage<Entry, Instant> page = this.entryMapper.findAll(new CursorPageRequest<>(null, 10, Navigation.NEXT));
		assertThat(page.content()).isEmpty();
		assertThat(page.size()).isEqualTo(10);
		assertThat(page.hasNext()).isFalse();
		assertThat(page.hasPrevious()).isFalse();
	}

	@Test
	void findAllCursorNext() {
		Entry entry1 = fixture(this.entryMapper.nextEntryId());
		Entry entry2 = fixture(this.entryMapper.nextEntryId());
		Entry entry3 = fixture(this.entryMapper.nextEntryId());
		Entry entry4 = fixture(this.entryMapper.nextEntryId());
		Entry entry5 = fixture(this.entryMapper.nextEntryId());
		this.entryMapper.insertAll(List.of(entry1, entry2, entry3, entry4, entry5));

		CursorPage<Entry, Instant> page0 = this.entryMapper.findAll(new CursorPageRequest<>(null, 2, Navigation.NEXT));
		assertThat(page0.content()).containsExactly(entry5.withContent(""), entry4.withContent(""));
		assertThat(page0.size()).isEqualTo(2);
		assertThat(page0.hasNext()).isTrue();
		assertThat(page0.hasPrevious()).isFalse();
		assertThat(page0.tailCursor()).isEqualTo(entry5.lastModified().date());
		assertThat(page0.headCursor()).isEqualTo(entry4.lastModified().date());

		CursorPage<Entry, Instant> page1 = this.entryMapper.findAll(new CursorPageRequest<>(page0.headCursor(), 2, Navigation.NEXT));
		assertThat(page1.content()).containsExactly(entry3.withContent(""), entry2.withContent(""));
		assertThat(page1.size()).isEqualTo(2);
		assertThat(page1.hasNext()).isTrue();
		assertThat(page1.hasPrevious()).isTrue();
		assertThat(page1.tailCursor()).isEqualTo(entry3.lastModified().date());
		assertThat(page1.headCursor()).isEqualTo(entry2.lastModified().date());

		CursorPage<Entry, Instant> page2 = this.entryMapper.findAll(new CursorPageRequest<>(page1.headCursor(), 2, Navigation.NEXT));
		assertThat(page2.content()).containsExactly(entry1.withContent(""));
		assertThat(page2.size()).isEqualTo(2);
		assertThat(page2.hasNext()).isFalse();
		assertThat(page2.hasPrevious()).isTrue();
		assertThat(page2.tailCursor()).isEqualTo(entry1.lastModified().date());
		assertThat(page2.headCursor()).isEqualTo(entry1.lastModified().date());
	}

	@Test
	void findAllCursorNext_exact2Page() {
		Entry entry1 = fixture(this.entryMapper.nextEntryId());
		Entry entry2 = fixture(this.entryMapper.nextEntryId());
		Entry entry3 = fixture(this.entryMapper.nextEntryId());
		Entry entry4 = fixture(this.entryMapper.nextEntryId());
		Entry entry5 = fixture(this.entryMapper.nextEntryId());
		this.entryMapper.insertAll(List.of(entry2, entry3, entry4, entry5));

		CursorPage<Entry, Instant> page0 = this.entryMapper.findAll(new CursorPageRequest<>(null, 2, Navigation.NEXT));
		assertThat(page0.content()).containsExactly(entry5.withContent(""), entry4.withContent(""));
		assertThat(page0.size()).isEqualTo(2);
		assertThat(page0.hasNext()).isTrue();
		assertThat(page0.hasPrevious()).isFalse();
		assertThat(page0.tailCursor()).isEqualTo(entry5.lastModified().date());
		assertThat(page0.headCursor()).isEqualTo(entry4.lastModified().date());

		CursorPage<Entry, Instant> page1 = this.entryMapper.findAll(new CursorPageRequest<>(page0.headCursor(), 2, Navigation.NEXT));
		assertThat(page1.content()).containsExactly(entry3.withContent(""), entry2.withContent(""));
		assertThat(page1.size()).isEqualTo(2);
		assertThat(page1.hasNext()).isFalse();
		assertThat(page1.hasPrevious()).isTrue();
		assertThat(page1.tailCursor()).isEqualTo(entry3.lastModified().date());
		assertThat(page1.headCursor()).isEqualTo(entry2.lastModified().date());
	}

	@Test
	void findAllCursorPrevious_empty() {
		CursorPage<Entry, Instant> page = this.entryMapper.findAll(new CursorPageRequest<>(null, 10, Navigation.PREVIOUS));
		assertThat(page.content()).isEmpty();
		assertThat(page.size()).isEqualTo(10);
		assertThat(page.hasNext()).isFalse();
		assertThat(page.hasPrevious()).isFalse();
	}

	@Test
	void findAllCursorPrevious() {
		Entry entry1 = fixture(this.entryMapper.nextEntryId());
		Entry entry2 = fixture(this.entryMapper.nextEntryId());
		Entry entry3 = fixture(this.entryMapper.nextEntryId());
		Entry entry4 = fixture(this.entryMapper.nextEntryId());
		Entry entry5 = fixture(this.entryMapper.nextEntryId());
		this.entryMapper.insertAll(List.of(entry1, entry2, entry3, entry4, entry5));

		CursorPage<Entry, Instant> page0 = this.entryMapper.findAll(new CursorPageRequest<>(null, 2, Navigation.PREVIOUS));
		assertThat(page0.content()).containsExactly(entry2.withContent(""), entry1.withContent(""));
		assertThat(page0.size()).isEqualTo(2);
		assertThat(page0.hasNext()).isFalse();
		assertThat(page0.hasPrevious()).isTrue();
		assertThat(page0.tailCursor()).isEqualTo(entry2.lastModified().date());
		assertThat(page0.headCursor()).isEqualTo(entry1.lastModified().date());

		CursorPage<Entry, Instant> page1 = this.entryMapper.findAll(new CursorPageRequest<>(page0.tailCursor(), 2, Navigation.PREVIOUS));
		assertThat(page1.content()).containsExactly(entry4.withContent(""), entry3.withContent(""));
		assertThat(page1.size()).isEqualTo(2);
		assertThat(page1.hasNext()).isTrue();
		assertThat(page1.hasPrevious()).isTrue();
		assertThat(page1.tailCursor()).isEqualTo(entry4.lastModified().date());
		assertThat(page1.headCursor()).isEqualTo(entry3.lastModified().date());

		CursorPage<Entry, Instant> page2 = this.entryMapper.findAll(new CursorPageRequest<>(page1.tailCursor(), 2, Navigation.PREVIOUS));
		assertThat(page2.content()).containsExactly(entry5.withContent(""));
		assertThat(page2.size()).isEqualTo(2);
		assertThat(page2.hasNext()).isTrue();
		assertThat(page2.hasPrevious()).isFalse();
		assertThat(page2.tailCursor()).isEqualTo(entry5.lastModified().date());
		assertThat(page2.headCursor()).isEqualTo(entry5.lastModified().date());
	}

	@Test
	void findAllCursorPrevious_exact2Page() {
		Entry entry1 = fixture(this.entryMapper.nextEntryId());
		Entry entry2 = fixture(this.entryMapper.nextEntryId());
		Entry entry3 = fixture(this.entryMapper.nextEntryId());
		Entry entry4 = fixture(this.entryMapper.nextEntryId());
		Entry entry5 = fixture(this.entryMapper.nextEntryId());
		this.entryMapper.insertAll(List.of(entry1, entry2, entry3, entry4));

		CursorPage<Entry, Instant> page0 = this.entryMapper.findAll(new CursorPageRequest<>(null, 2, Navigation.PREVIOUS));
		assertThat(page0.content()).containsExactly(entry2.withContent(""), entry1.withContent(""));
		assertThat(page0.size()).isEqualTo(2);
		assertThat(page0.hasNext()).isFalse();
		assertThat(page0.hasPrevious()).isTrue();
		assertThat(page0.tailCursor()).isEqualTo(entry2.lastModified().date());
		assertThat(page0.headCursor()).isEqualTo(entry1.lastModified().date());

		CursorPage<Entry, Instant> page1 = this.entryMapper.findAll(new CursorPageRequest<>(page0.tailCursor(), 2, Navigation.PREVIOUS));
		assertThat(page1.content()).containsExactly(entry4.withContent(""), entry3.withContent(""));
		assertThat(page1.size()).isEqualTo(2);
		assertThat(page1.hasNext()).isTrue();
		assertThat(page1.hasPrevious()).isFalse();
		assertThat(page1.tailCursor()).isEqualTo(entry4.lastModified().date());
		assertThat(page1.headCursor()).isEqualTo(entry3.lastModified().date());
	}
}
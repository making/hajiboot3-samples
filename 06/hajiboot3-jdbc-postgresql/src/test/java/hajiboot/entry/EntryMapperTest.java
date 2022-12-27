package hajiboot.entry;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
}
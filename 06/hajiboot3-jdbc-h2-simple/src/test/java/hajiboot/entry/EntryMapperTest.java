package hajiboot.entry;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest(properties = { "logging.level.sql=DEBUG", "logging.level.org.springframework.jdbc.support.JdbcTransactionManager=DEBUG" })
@Import(EntryMapper.class)
class EntryMapperTest {
	@Autowired
	EntryMapper entryMapper;

	@Test
	void insertAndFindOne() {
		Integer entryId = this.entryMapper.nextEntryId();
		Entry entry = new Entry(entryId, "test title", "test content");
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
}
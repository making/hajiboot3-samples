package hajiboot.entry;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import hajiboot.util.FileLoader;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest(properties = {
		"logging.level.org.springframework.jdbc.support.JdbcTransactionManager=DEBUG",
		"spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
		"spring.datasource.url=jdbc:tc:postgresql:14-alpine:///databasename"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(EntryMapper.class)
class EntryMapperExplainTest {
	@Autowired
	EntryMapper entryMapper;

	@Autowired
	JdbcTemplate jdbcTemplate;

	Entry fixture(Integer entryId) {
		return new Entry(entryId, "test title", "test content",
				Set.of(new Tag("a"), new Tag("b"), new Tag("c")),
				new UpdateMeta("test", Instant.now()),
				new UpdateMeta("test", Instant.now()));
	}

	@Test
	void explain() {
		List<Entry> entries = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			entries.add(fixture(this.entryMapper.nextEntryId()).withTags(Set.of(new Tag("a"), new Tag("b"))));
			entries.add(fixture(this.entryMapper.nextEntryId()).withTags(Set.of(new Tag("a"), new Tag("b"))));
			entries.add(fixture(this.entryMapper.nextEntryId()).withTags(Set.of(new Tag("a"), new Tag("b"))));
			entries.add(fixture(this.entryMapper.nextEntryId()).withTags(Set.of(new Tag("b"), new Tag("c"), new Tag("d"))));
			entries.add(fixture(this.entryMapper.nextEntryId()).withTags(Set.of(new Tag("c"), new Tag("a"), new Tag("d"))));
		}
		this.entryMapper.insertAll(entries);

		String explain = String.join("\n", this.jdbcTemplate.queryForList("EXPLAIN ANALYZE " + FileLoader.loadAsString("hajiboot/entry/EntryMapper/findByTag.sql"), String.class, "a"));
		System.out.println("====");
		System.out.println(explain);
		System.out.println("====");
	}
}
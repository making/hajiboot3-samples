package hajiboot.entry;

import java.util.Optional;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class EntryMapper {
	private final JdbcTemplate jdbcTemplate;

	public EntryMapper(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Optional<Entry> findOne(Integer entryId) {
		String sql = """
				SELECT entry_id,
				       title,
				       content
				FROM entry
				WHERE entry_id = ?
				""";
		return Optional.ofNullable(DataAccessUtils.uniqueResult(this.jdbcTemplate.query(sql, new DataClassRowMapper<>(Entry.class), entryId)));
	}

	public Integer nextEntryId() {
		String sql = "SELECT nextval('entry_id_seq')";
		return this.jdbcTemplate.queryForObject(sql, Integer.class);
	}

	@Transactional
	public int insert(Entry entry) {
		String sql = "INSERT INTO entry(entry_id, title, content) VALUES(?, ?, ?)";
		return this.jdbcTemplate.update(sql, entry.entryId(), entry.title(), entry.content());
	}
}
package hajiboot.entry;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class EntryMapper {
	private final JdbcTemplate jdbcTemplate;

	public EntryMapper(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<Entry> rowMapper = (rs, rowNum) -> {
		int entryId = rs.getInt("entry_id");
		String title = rs.getString("title");
		String content = rs.getString("content");
		Set<Tag> tags = Arrays.stream((Object[]) rs.getArray("tags").getArray())
				.map(String.class::cast).map(Tag::new)
				.collect(Collectors.toUnmodifiableSet());
		UpdateMeta created = new UpdateMeta(rs.getString("created_by"),
				rs.getTimestamp("created_date").toInstant());
		UpdateMeta lastModified = new UpdateMeta(rs.getString("last_modified_by"),
				rs.getTimestamp("last_modified_date").toInstant());
		return new Entry(entryId, title, content, tags, created,
				lastModified);
	};

	public Optional<Entry> findOne(Integer entryId) {
		String sql = """
				SELECT entry_id,
				       title,
				       content,
				       tags,
				       created_by,
				       created_date,
				       last_modified_by,
				       last_modified_date
				FROM entry
				WHERE entry_id = ?
				""";
		return Optional.ofNullable(DataAccessUtils.uniqueResult(this.jdbcTemplate.query(sql, this.rowMapper, entryId)));
	}

	public List<Entry> findAll() {
		String sql = """
				SELECT entry_id,
				       title,
				       '' AS content,
				       tags,
				       created_by,
				       created_date,
				       last_modified_by,
				       last_modified_date
				FROM entry
				ORDER BY last_modified_date DESC
				""";
		return this.jdbcTemplate.query(sql, this.rowMapper);
	}

	public List<Entry> findByTag(Tag tag) {
		String sql = """
				SELECT entry_id,
				       title,
				       '' AS content,
				       tags,
				       created_by,
				       created_date,
				       last_modified_by,
				       last_modified_date
				FROM entry
				WHERE ARRAY_CONTAINS(tags, ?)
				ORDER BY last_modified_date DESC
				""";
		return this.jdbcTemplate.query(sql, this.rowMapper, tag.name());
	}

	public Integer nextEntryId() {
		String sql = "SELECT nextval('entry_id_seq')";
		return this.jdbcTemplate.queryForObject(sql, Integer.class);
	}

	@Transactional
	public int insert(Entry entry) {
		String sql = "INSERT INTO entry(entry_id, title, content, tags, created_by, created_date, last_modified_by, last_modified_date) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
		return this.jdbcTemplate.update(sql, entry.entryId(), entry.title(), entry.content(),
				entry.tags().stream().map(Tag::name).toArray(),
				entry.created().name(), entry.created().date(),
				entry.lastModified().name(), entry.lastModified().date());
	}

	@Transactional
	public int update(Entry entry) {
		String sql = "UPDATE entry SET title = ?, content = ?, tags = ?, created_by = ?, created_date = ?, last_modified_by = ?, last_modified_date = ? WHERE entry_id = ?";
		return this.jdbcTemplate.update(sql, entry.title(), entry.content(),
				entry.tags().stream().map(Tag::name).toArray(), entry.created().name(), entry.created().date(),
				entry.lastModified().name(), entry.lastModified().date(), entry.entryId());
	}

	@Transactional
	public int delete(Integer entryId) {
		return this.jdbcTemplate.update("DELETE FROM entry WHERE  entry_id = ?", entryId);
	}

	@Transactional
	public int insertAll(List<Entry> entries) {
		String sql = "INSERT INTO entry(entry_id, title, content, tags, created_by, created_date, last_modified_by, last_modified_date) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
		final int[] updated = this.jdbcTemplate.batchUpdate(sql, entries.stream()
				.map(entry -> new Object[] { entry.entryId(), entry.title(), entry.content(),
						entry.tags().stream().map(Tag::name).toArray(), entry.created().name(), entry.created().date(),
						entry.lastModified().name(), entry.lastModified().date() })
				.toList());
		return Arrays.stream(updated).sum();
	}
}
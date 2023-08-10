package hajiboot.entry;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import hajiboot.pagination.CursorPage;
import hajiboot.pagination.CursorPageRequest;
import hajiboot.pagination.CursorPageRequest.Navigation;
import hajiboot.pagination.OffsetPage;
import hajiboot.pagination.OffsetPageRequest;
import hajiboot.util.FileLoader;

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
		String sql = FileLoader.loadSqlAsString("hajiboot/entry/EntryMapper/findOne.sql");
		return Optional.ofNullable(DataAccessUtils.uniqueResult(this.jdbcTemplate.query(sql, this.rowMapper, entryId)));
	}

	public List<Entry> findAll() {
		String sql = FileLoader.loadSqlAsString("hajiboot/entry/EntryMapper/findAll.sql");
		return this.jdbcTemplate.query(sql, this.rowMapper);
	}

	public long count() {
		String sql = FileLoader.loadSqlAsString("hajiboot/entry/EntryMapper/count.sql");
		return Objects.requireNonNull(this.jdbcTemplate.queryForObject(sql, Long.class));
	}

	public OffsetPage<Entry> findAll(OffsetPageRequest pageRequest) {
		String sql = FileLoader.loadSqlAsString("hajiboot/entry/EntryMapper/findAllOffset.sql")
				.formatted(pageRequest.offset(), pageRequest.pageSize());
		List<Entry> content = this.jdbcTemplate.query(sql, this.rowMapper);
		long totalElements = this.count();
		return new OffsetPage<>(content, pageRequest.pageSize(), pageRequest.pageNumber(), totalElements);
	}

	public CursorPage<Entry, Instant> findAll(CursorPageRequest<Instant> pageRequest) {
		Optional<Instant> cursor = pageRequest.cursorOptional();
		Navigation navigation = Objects.requireNonNull(pageRequest.navigation());
		int pageSizePlus1 = pageRequest.pageSize() + 1;
		String sql = FileLoader.loadSqlAsString(navigation.isNext() ? "hajiboot/entry/EntryMapper/findAllCursorNext.sql" : "hajiboot/entry/EntryMapper/findAllCursorPrevious.sql")
				.formatted(pageSizePlus1);
		List<Entry> contentPlus1 = this.jdbcTemplate.query(sql, this.rowMapper, cursor.map(Timestamp::from).orElse(null));
		boolean hasPrevious;
		boolean hasNext;
		List<Entry> content;
		if (navigation.isNext()) {
			hasPrevious = cursor.isPresent();
			hasNext = contentPlus1.size() == pageSizePlus1;
			content = hasNext ? contentPlus1.subList(0, pageRequest.pageSize()) : contentPlus1;
		}
		else {
			hasPrevious = contentPlus1.size() == pageSizePlus1;
			hasNext = cursor.isPresent();
			content = hasPrevious ? contentPlus1.subList(1, pageSizePlus1) : contentPlus1;
		}
		return new CursorPage<>(content, pageRequest.pageSize(), e -> e.lastModified().date(), hasPrevious, hasNext);

	}

	public List<Entry> findByTag(Tag tag) {
		String sql = FileLoader.loadSqlAsString("hajiboot/entry/EntryMapper/findByTag.sql");
		return this.jdbcTemplate.query(sql, this.rowMapper, tag.name());
	}

	public Integer nextEntryId() {
		String sql = FileLoader.loadSqlAsString("hajiboot/entry/EntryMapper/nextEntryId.sql");
		return this.jdbcTemplate.queryForObject(sql, Integer.class);
	}

	@Transactional
	public int insert(Entry entry) {
		String sql = FileLoader.loadSqlAsString("hajiboot/entry/EntryMapper/insert.sql");
		return this.jdbcTemplate.update(sql, entry.entryId(), entry.title(), entry.content(),
				entry.tags().stream().map(Tag::name).collect(Collectors.joining(",")),
				entry.created().name(), entry.created().toTimestamp(),
				entry.lastModified().name(), entry.lastModified().toTimestamp());
	}

	@Transactional
	public int update(Entry entry) {
		String sql = FileLoader.loadSqlAsString("hajiboot/entry/EntryMapper/update.sql");
		return this.jdbcTemplate.update(sql, entry.title(), entry.content(),
				entry.tags().stream().map(Tag::name).collect(Collectors.joining(",")), entry.created().name(), entry.created().toTimestamp(),
				entry.lastModified().name(), entry.lastModified().toTimestamp(), entry.entryId());
	}

	@Transactional
	public int delete(Integer entryId) {
		String sql = FileLoader.loadSqlAsString("hajiboot/entry/EntryMapper/delete.sql");
		return this.jdbcTemplate.update(sql, entryId);
	}

	@Transactional
	public int insertAll(List<Entry> entries) {
		String sql = FileLoader.loadSqlAsString("hajiboot/entry/EntryMapper/insert.sql");
		final int[] updated = this.jdbcTemplate.batchUpdate(sql, entries.stream()
				.map(entry -> new Object[] { entry.entryId(), entry.title(), entry.content(),
						entry.tags().stream().map(Tag::name).collect(Collectors.joining(",")), entry.created().name(), entry.created().toTimestamp(),
						entry.lastModified().name(), entry.lastModified().toTimestamp() })
				.toList());
		return Arrays.stream(updated).sum();
	}
}
package hajiboot.entry;

import java.util.Set;

public record Entry(Integer entryId, String title, String content, Set<Tag> tags,
					UpdateMeta created, UpdateMeta lastModified) {
	public Entry withContent(String content) {
		return new Entry(this.entryId, this.title, content, this.tags, this.created, this.lastModified);
	}

	public Entry withLastModified(UpdateMeta lastModified) {
		return new Entry(this.entryId, this.title, this.content, this.tags, this.created, lastModified);
	}
}
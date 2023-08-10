package hajiboot.entry;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(example = """
		{
		  "entryId": 100,
		   "title":"Hello World!",
		   "content": "This is an example entry!",
		   "tags": [
		     {
		       "name": "Tag 1"
		     },
		     {
		       "name": "Tag 2"
		     }
		   ],
		   "created": {
		     "name": "system",
		     "date": "2023-02-18T00:00:00Z"
		   },
		   "lastModified": {
		     "name": "system",
		     "date": "2023-02-19T00:00:00Z"
		   }
		}
		""")
public record Entry(Integer entryId, String title, String content, Set<Tag> tags,
					UpdateMeta created, UpdateMeta lastModified) {
	public Entry withTitle(String title) {
		return new Entry(this.entryId, title, this.content, this.tags, this.created, this.lastModified);
	}

	public Entry withContent(String content) {
		return new Entry(this.entryId, this.title, content, this.tags, this.created, this.lastModified);
	}

	public Entry withTags(Set<Tag> tags) {
		return new Entry(this.entryId, this.title, this.content, tags, this.created, this.lastModified);
	}

	public Entry withLastModified(UpdateMeta lastModified) {
		return new Entry(this.entryId, this.title, this.content, this.tags, this.created, lastModified);
	}
}
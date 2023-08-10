package hajiboot.entry.web;

import java.time.Clock;
import java.util.Set;

import hajiboot.entry.Entry;
import hajiboot.entry.Tag;
import hajiboot.entry.UpdateMeta;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(example = """
		{
		   "title":"Hello World!",
		   "content": "This is an example entry!",
		   "tags": [
		     {
		       "name": "Tag 1"
		     },
		     {
		       "name": "Tag 2"
		     }
		   ]
		}
		""")
public record EntryCreateRequest(String title, String content, Set<Tag> tags) {

	public Entry toEntry(Integer entryId, String username, Clock clock) {
		final UpdateMeta created = new UpdateMeta(username, clock.instant());
		return new Entry(entryId, title, content, tags, created, created);
	}
}

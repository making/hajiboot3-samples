package hajiboot.entry.web;

import java.time.Clock;
import java.util.Set;

import hajiboot.entry.Entry;
import hajiboot.entry.Tag;
import hajiboot.entry.UpdateMeta;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
public record EntryUpdateRequest(String title, String content, Set<Tag> tags) {

	public Entry updateEntry(Entry entry, String username, Clock clock) {
		boolean updated = false;
		Entry toReturn = entry;
		if (StringUtils.hasText(title)) {
			toReturn = entry.withTitle(title);
			updated = true;
		}
		if (StringUtils.hasText(content)) {
			toReturn = entry.withContent(content);
			updated = true;
		}
		if (!CollectionUtils.isEmpty(tags)) {
			toReturn = entry.withTags(tags);
			updated = true;
		}
		if (updated) {
			toReturn = toReturn.withLastModified(new UpdateMeta(username, clock.instant()));
		}
		return toReturn;
	}
}

package hajiboot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import hajiboot.entry.Entry;
import hajiboot.entry.EntryMapper;
import hajiboot.entry.Tag;
import hajiboot.entry.UpdateMeta;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InitialDataImporter implements ApplicationRunner {
	private final EntryMapper entryMapper;

	private final Environment environment;

	public InitialDataImporter(EntryMapper entryMapper, Environment environment) {
		this.entryMapper = entryMapper;
		this.environment = environment;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) throws Exception {
		final CloudPlatform cloudPlatform = CloudPlatform.getActive(this.environment);
		if (cloudPlatform != null && cloudPlatform != CloudPlatform.NONE) {
			return;
		}
		final long count = this.entryMapper.count();
		if (count == 0) {
			final List<Entry> entries = new ArrayList<>();
			for (int i = 1; i <= 30; i++) {
				final Integer entryId = this.entryMapper.nextEntryId();
				final UpdateMeta updateMeta = new UpdateMeta("admin", Instant.now());
				entries.add(new Entry(entryId, "Title " + entryId, "Content " + entryId,
						Set.of(new Tag("a " + entryId), new Tag("b " + entryId)),
						updateMeta, updateMeta));
			}
			this.entryMapper.insertAll(entries);
		}
	}
}

package hajiboot.entry;

import java.sql.Timestamp;
import java.time.Instant;

public record UpdateMeta(String name, Instant date) {
	public UpdateMeta withDate(Instant date) {
		return new UpdateMeta(this.name, date);
	}

	public Timestamp toTimestamp() {
		return Timestamp.from(this.date);
	}
}

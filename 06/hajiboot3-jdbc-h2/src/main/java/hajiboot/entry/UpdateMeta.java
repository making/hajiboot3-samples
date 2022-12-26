package hajiboot.entry;

import java.time.Instant;

public record UpdateMeta(String name, Instant date) {
	public UpdateMeta withDate(Instant date) {
		return new UpdateMeta(this.name, date);
	}
}

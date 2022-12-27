package hajiboot.pagination;

import java.util.Optional;

public record CursorPageRequest<C>(C cursor, int pageSize,
								   Direction direction) {

	public Optional<C> cursorOptional() {
		return Optional.ofNullable(this.cursor);
	}

	public enum Direction {
		DESC, ASC
	}
}

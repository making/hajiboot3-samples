package hajiboot.entry.web;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;

import hajiboot.entry.Entry;
import hajiboot.entry.EntryMapper;
import hajiboot.pagination.OffsetPage;
import hajiboot.pagination.OffsetPageRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@Tag(name = "entry")
public class EntryController {
	private final EntryMapper entryMapper;

	private final Clock clock;

	public EntryController(EntryMapper entryMapper, Clock clock) {
		this.entryMapper = entryMapper;
		this.clock = clock;
	}

	@GetMapping(path = "/entries/{entryId}")
	@ApiResponses({
			@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = Entry.class), mediaType = MediaType.APPLICATION_JSON_VALUE),
					headers = {
							@Header(name = HttpHeaders.LAST_MODIFIED, required = true, schema = @Schema(type = "string")),
							@Header(name = HttpHeaders.CACHE_CONTROL, required = true, schema = @Schema(type = "string"))
					}),
			@ApiResponse(responseCode = "304",
					content = @Content(schema = @Schema(hidden = true)),
					headers = {
							@Header(name = HttpHeaders.LAST_MODIFIED, required = true, schema = @Schema(type = "string")),
							@Header(name = HttpHeaders.CACHE_CONTROL, required = true, schema = @Schema(type = "string"))
					}),
			@ApiResponse(responseCode = "404",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
	})
	@Parameters({ @Parameter(name = HttpHeaders.IF_MODIFIED_SINCE, in = ParameterIn.HEADER, schema = @Schema(type = "string")) })
	public ResponseEntity<Entry> getEntry(@PathVariable Integer entryId, NativeWebRequest webRequest) {
		final Entry entry = this.entryMapper.findOne(entryId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"The requested entry is not found. (entry Id = %d)".formatted(entryId)));
		final long lastModifiedTimestamp = entry.lastModified().date().toEpochMilli();
		if (webRequest.checkNotModified(lastModifiedTimestamp)) {
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
					.cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)))
					.build();
		}
		return ResponseEntity
				.ok()
				.cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)))
				.body(entry);
	}

	@GetMapping(path = "/entries", produces = MediaType.APPLICATION_JSON_VALUE)
	@Parameters({
			@Parameter(name = "page", schema = @Schema(implementation = Integer.class, defaultValue = "0", requiredMode = RequiredMode.NOT_REQUIRED)),
			@Parameter(name = "size", schema = @Schema(implementation = Integer.class, defaultValue = "20", requiredMode = RequiredMode.NOT_REQUIRED))
	})
	public OffsetPage<Entry> getEntries(@Parameter(hidden = true) OffsetPageRequest pageRequest) {
		return this.entryMapper.findAll(pageRequest);
	}

	@PostMapping(path = "/entries", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses({
			@ApiResponse(responseCode = "201",
					content = @Content(schema = @Schema(implementation = Entry.class), mediaType = MediaType.APPLICATION_JSON_VALUE),
					headers = {
							@Header(name = HttpHeaders.LOCATION, required = true, schema = @Schema(type = "string", format = "uri"))
					})
	})
	public ResponseEntity<Entry> postEntries(@RequestBody EntryCreateRequest request, UriComponentsBuilder uriComponentsBuilder) {
		final Integer entryId = this.entryMapper.nextEntryId();
		final Entry entry = request.toEntry(entryId, "system", this.clock);
		this.entryMapper.insert(entry);
		final URI uri = uriComponentsBuilder.path("/entries/{entryId}").build(entryId);
		return ResponseEntity.created(uri).body(entry);
	}

	@PatchMapping(path = "/entries/{entryId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	@ApiResponses({
			@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = Entry.class), mediaType = MediaType.APPLICATION_JSON_VALUE),
					headers = {
							@Header(name = HttpHeaders.LAST_MODIFIED, required = true, schema = @Schema(type = "string")),
							@Header(name = HttpHeaders.CACHE_CONTROL, required = true, schema = @Schema(type = "string"))
					}),
			@ApiResponse(responseCode = "404",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
	})
	public Entry patchEntry(@PathVariable Integer entryId, @RequestBody EntryUpdateRequest request) {
		final Entry entry = this.entryMapper.findOne(entryId)
				.map(e -> request.updateEntry(e, "system", this.clock))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"The requested entry is not found. (entry Id = %d)".formatted(entryId)));
		this.entryMapper.update(entry);
		return entry;
	}

	@DeleteMapping(path = "/entries/{entryId}")
	@ApiResponses({
			@ApiResponse(responseCode = "204", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseEntity<Void> deleteEntry(@PathVariable Integer entryId) {
		this.entryMapper.delete(entryId);
		return ResponseEntity.noContent()
				.build();
	}
}

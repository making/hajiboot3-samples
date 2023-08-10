package hajiboot.entry.web;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;

import hajiboot.entry.Entry;
import hajiboot.entry.EntryMapper;
import hajiboot.entry.Tag;
import hajiboot.entry.UpdateMeta;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EntryController.class)
class EntryControllerTest {
	@MockBean
	EntryMapper entryMapper;

	@Autowired
	MockMvc mockMvc;

	Entry fixture(Integer entryId) {
		return new Entry(entryId, "test title", "test content",
				Set.of(new Tag("a"), new Tag("b"), new Tag("c")),
				new UpdateMeta("test", Instant.parse("2023-02-10T07:00:00Z")),
				new UpdateMeta("test", Instant.parse("2023-02-10T08:00:00Z")));
	}

	@Test
	void getEntry_200() throws Exception {
		given(this.entryMapper.findOne(1)).willReturn(Optional.of(fixture(1)));
		this.mockMvc.perform(get("/entries/1"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.LAST_MODIFIED, "Fri, 10 Feb 2023 08:00:00 GMT"));
	}

	@Test
	void getEntry_200_modified() throws Exception {
		given(this.entryMapper.findOne(1)).willReturn(Optional.of(fixture(1)));
		this.mockMvc.perform(get("/entries/1")
						.header(HttpHeaders.IF_MODIFIED_SINCE, "Fri, 10 Feb 2023 07:00:00 GMT"))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	void getEntry_304() throws Exception {
		given(this.entryMapper.findOne(1)).willReturn(Optional.of(fixture(1)));
		this.mockMvc.perform(get("/entries/1")
						.header(HttpHeaders.IF_MODIFIED_SINCE, "Fri, 10 Feb 2023 08:00:00 GMT"))
				.andDo(print())
				.andExpect(status().isNotModified());
	}

	@Test
	void headEntry_304() throws Exception {
		given(this.entryMapper.findOne(1)).willReturn(Optional.of(fixture(1)));
		this.mockMvc.perform(MockMvcRequestBuilders.head("/entries/1")
						.header(HttpHeaders.IF_MODIFIED_SINCE, "Fri, 10 Feb 2023 08:00:00 GMT"))
				.andDo(print())
				.andExpect(status().isNotModified());
	}

	@Test
	@Disabled
	void getEntry_404() throws Exception {
		given(this.entryMapper.findOne(1)).willReturn(Optional.empty());
		this.mockMvc.perform(get("/entries/1"))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(mvcResult -> assertThat(mvcResult.getResolvedException()).isInstanceOf(ResponseStatusException.class))
				.andExpect(mvcResult -> assertThat(mvcResult.getResponse().getErrorMessage()).isEqualTo("The requested entry is not found. (entry Id = 1)"));
	}

	@Test
	void postEntries_201() throws Exception {
		given(this.entryMapper.nextEntryId()).willReturn(10);
		given(this.entryMapper.insert(any())).willReturn(1);
		this.mockMvc.perform(post("/entries")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": "Hello World!", "content":  "This is my first blog post!", "tags": [{"name":  "test"}, {"name":  "blog"}]}
								"""))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/entries/10"))
				.andExpect(jsonPath("$.entryId").value(10))
				.andExpect(jsonPath("$.title").value("Hello World!"))
				.andExpect(jsonPath("$.content").value("This is my first blog post!"))
				.andExpect(jsonPath("$.tags.length()").value(2))
				.andExpect(jsonPath("$.tags[0].name").value("test"))
				.andExpect(jsonPath("$.tags[1].name").value("blog"))
				.andExpect(jsonPath("$.created.name").value("system"))
				.andExpect(jsonPath("$.created.date").value("2023-02-10T09:00:00Z"))
				.andExpect(jsonPath("$.lastModified.name").value("system"))
				.andExpect(jsonPath("$.lastModified.date").value("2023-02-10T09:00:00Z"));
		;
	}

	@Test
	void deleteEntry_204() throws Exception {
		given(this.entryMapper.delete(1)).willReturn(1);
		this.mockMvc.perform(delete("/entries/1"))
				.andDo(print())
				.andExpect(status().isNoContent());
	}

	@TestConfiguration
	static class Config {
		@Bean
		public Clock testClock() {
			return Clock.fixed(Instant.parse("2023-02-10T09:00:00Z"), ZoneId.of("UTC"));
		}
	}
}
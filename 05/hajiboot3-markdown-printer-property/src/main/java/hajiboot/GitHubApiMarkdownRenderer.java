package hajiboot;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GitHubApiMarkdownRenderer implements MarkdownRenderer {

	private final RestClient restClient;

	public GitHubApiMarkdownRenderer(GitHubProperties props /* (1) */, RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder
			//.defaultHeaders(httpHeaders -> httpHeaders.setBearerAuth(props.accessToken()))
			.defaultHeader("Authorization", "Bearer " + props.accessToken())
			.build();
	}

	@Override
	public String render(String markdown) {
		return this.restClient.post()
			.uri("https://api.github.com/markdown/raw")
			.body(markdown)
			.retrieve()
			.body(String.class);
	}

}

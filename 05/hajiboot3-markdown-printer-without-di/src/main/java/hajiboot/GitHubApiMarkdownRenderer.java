package hajiboot;

import org.springframework.web.client.RestClient;

public class GitHubApiMarkdownRenderer implements MarkdownRenderer {

	private final RestClient restClient;

	public GitHubApiMarkdownRenderer(RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder.build();
	}

	@Override
	public String render(String markdown) {
		return this.restClient.post() // (1)
				.uri("https://api.github.com/markdown/raw")
				.body(markdown)
				.retrieve()
				.body(String.class);
	}

}

package hajiboot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@ConditionalOnProperty(name = "hajiboot3.markdown.type", havingValue = "github")
public class GitHubApiMarkdownRenderer implements MarkdownRenderer {
	private final RestTemplate restTemplate;

	public GitHubApiMarkdownRenderer(GitHubProperties props /* (1) */, RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder
				.defaultHeader("Authorization", "token " + props.accessToken())
				.build();
	}

	@Override
	public String render(String markdown) {
		return this.restTemplate.postForObject("https://api.github.com/markdown/raw",
				markdown, String.class);
	}
}

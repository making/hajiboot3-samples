package hajiboot;

import org.springframework.web.client.RestClient;

public class Main {

	public static void main(String[] args) {
		RestClient.Builder restClientBuilder = RestClient.builder();
		MarkdownRenderer markdownRenderer = new GitHubApiMarkdownRenderer(restClientBuilder);
		MarkdownPrinter markdownPrinter = new MarkdownPrinter(markdownRenderer);
		markdownPrinter.print(System.in);
	}

}

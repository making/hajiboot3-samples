package hajiboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GitHubProperties.class)
public class Hajiboot3MarkdownPrinterApplication {

	public static void main(String[] args) {
		SpringApplication.run(Hajiboot3MarkdownPrinterApplication.class, args);
	}
}
package hajiboot;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class Hajiboot3MarkdownPrinterApplicationTests {

	@Test
	void contextLoads(CapturedOutput capture) {
		String token = Optional.ofNullable(System.getenv("GITHUB_API_TOKEN")).orElse("YOUR-TOKEN"); // (1)
		System.setIn(new ByteArrayInputStream("Hello **Spring Boot**".getBytes(StandardCharsets.UTF_8)));
		SpringApplication.run(Hajiboot3MarkdownPrinterApplication.class,
				"--github.access-token=" + token /* (2) */);
		assertThat(capture.toString()).contains("<p>Hello <strong>Spring Boot</strong></p>");
	}

}
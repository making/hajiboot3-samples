package hajiboot;

import jakarta.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "github") // (1)
@Validated
public record GitHubProperties(@NotEmpty String accessToken) { // (2)
}
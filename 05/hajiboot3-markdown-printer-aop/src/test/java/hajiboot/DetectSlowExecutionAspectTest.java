package hajiboot;

import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({ OutputCaptureExtension.class, SpringExtension.class }) // (1)
public class DetectSlowExecutionAspectTest {

	@Autowired // (2)
	private Sleep sleep;

	@Test
	void detectSlow(CapturedOutput capture) {
		sleep.apply(1000L); // (3)
		String log = capture.toString();
		assertThat(log).contains("Detect slow execution elapsed="); // (4)
		assertThat(log).contains(
				", method=public java.lang.String " + Sleep.class.getName() + ".apply(java.lang.Long), args=[1000]");
	}

	@Test
	void notDetected(CapturedOutput capture) {
		sleep.apply(100L);
		String log = capture.toString();
		assertThat(log).doesNotContain("Detect slow execution elapsed="); // (5)
	}

	@TestConfiguration // (6)
	@EnableAspectJAutoProxy(proxyTargetClass = true) // (7)
	public static class Config {

		@Bean
		public DetectSlowExecutionAspect detectSlowExecutionAspectForTest() {
			return new DetectSlowExecutionAspect();
		}

		@Bean
		public Sleep sleep() {
			return new Sleep();
		}

	}

	public static class Sleep implements Function<Long, String> {

		// (8)
		@DetectSlowExecution(threshold = 800)
		@Override
		public String apply(Long sleep) {
			try {
				Thread.sleep(sleep);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return "Slept " + sleep + " [msec]";
		}

	}

}

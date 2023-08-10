package hajiboot.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.ClassPathResource;

public class FileLoader {
	private static final Map<String, String> cache = new ConcurrentHashMap<>();

	public static String loadAsString(String file) {
		return cache.computeIfAbsent(file, f -> {
			try {
				return Files.readString(new ClassPathResource(file).getFile().toPath());
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	public static String loadSqlAsString(String file) {
		return "/* %s */ %s".formatted(file, loadAsString(file));
	}
}
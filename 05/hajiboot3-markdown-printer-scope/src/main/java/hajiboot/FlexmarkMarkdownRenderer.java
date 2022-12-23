package hajiboot;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "hajiboot3.markdown.type", havingValue = "flexmark", matchIfMissing = true) // (1)
public class FlexmarkMarkdownRenderer implements MarkdownRenderer {
	private final Parser parser = Parser.builder().build();

	private final HtmlRenderer renderer = HtmlRenderer.builder().build();

	@Override
	public String render(String markdown) {
		Document document = this.parser.parse(markdown);
		return this.renderer.render(document);
	}
}
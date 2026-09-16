package nl.hva.cmi.lessen.discombobulator.steps.detect;

import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;
import nl.hva.cmi.lessen.discombobulator.model.Span;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexSpanDetector implements SpanDetector {

    private final String label;
    private final Pattern pattern;

    public RegexSpanDetector(String label, Pattern pattern) {
        this.label = label;
        this.pattern = pattern;
    }

    @Override
    public List<Span> detect(String text, ChatMessage message) {
        if (text == null || text.isEmpty()) return Collections.emptyList();

        List<Span> spans = new ArrayList<>();
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            spans.add(new Span(m, label));
        }
        return spans;
    }
}

package nl.hva.cmi.lessen.discombobulator.steps.detect;


import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;
import nl.hva.cmi.lessen.discombobulator.model.Span;

import java.util.ArrayList;
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
    public List<Span> detect(ChatMessage message) {
        if (message.content == null || message.content.isEmpty()) return java.util.Collections.emptyList();

        List<Span> spans = new ArrayList<>();
        Matcher m = pattern.matcher(message.content);
        while (m.find()) {
            spans.add(new Span(m, label));
        }
        return spans;
    }

}

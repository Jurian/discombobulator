package nl.hva.cmi.lessen.discombobulator.rule;


import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;
import nl.hva.cmi.lessen.discombobulator.model.Span;

import java.util.List;
import java.util.regex.Pattern;

public class RegexRule extends SpanApplyingRule {
    private final String label;
    private final Pattern pattern;

    public RegexRule(String label, Pattern pattern) {this(label, pattern, -1);}

    public RegexRule(String label, Pattern pattern, int maxSpanDistance) {
        super(maxSpanDistance);
        this.label = label;
        this.pattern = pattern;
    }

    @Override
    public List<Span> detect(ChatMessage message) {
        if (message.content == null || message.content.isEmpty()) return java.util.Collections.emptyList();
        return Span.find(pattern, message.content, label);
    }
}

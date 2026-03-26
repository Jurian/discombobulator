package nl.hva.cmi.lessen.discombobulator.rule;

import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;
import nl.hva.cmi.lessen.discombobulator.model.Span;

import java.util.List;
import java.util.regex.Pattern;

public final class GreetingRule extends SpanApplyingRule {
    public static Pattern defaultPattern() {
        return Pattern.compile(
                "(^|[«>\"“”]\\s*)" +
                        "(hé|hee|hi|hello|hey|hoi|hoihoi|geachte|hallo|dag|goedendag|goedemorgen|goedemiddag|goedenavond|beste|dear)" +
                        "\\s+" +
                        "([A-Z][\\p{L}'-]{2,})\\b" +
                        "([.,!]+)?",
                Pattern.CASE_INSENSITIVE
        );
    }

    private final Pattern pattern;
    private final String labelTo;
    private final String labelFrom;

    public GreetingRule(String labelTo, String labelFrom) {
        this(labelTo, labelFrom, defaultPattern());
    }

    private GreetingRule(String labelTo, String labelFrom, Pattern pattern) {
        super(-1);
        this.pattern = pattern;
        this.labelTo = labelTo;
        this.labelFrom = labelFrom;
    }

    @Override
    public List<Span> detect(ChatMessage message) {
        if (message.content == null || message.content.isEmpty()) return java.util.Collections.emptyList();

        var m = pattern.matcher(message.content);
        var spans = new java.util.ArrayList<Span>();

        while (m.find()) {
            String prefix = (m.group(1) == null) ? "" : m.group(1);
            String greeting = m.group(2);
            String candidateName = m.group(3);

            // If there's no name captured, there is nothing to label as a span.
            if (candidateName == null) continue;

            // Preserve your original "don't replace if name == greeting" rule
            if (candidateName.equalsIgnoreCase(greeting)) continue;

            boolean isReplyContext = !prefix.isEmpty();
            ChatMessage.CHAT_TYPE effectiveType = isReplyContext
                    ? (message.type == ChatMessage.CHAT_TYPE.TO ? ChatMessage.CHAT_TYPE.FROM : ChatMessage.CHAT_TYPE.TO)
                    : message.type;

            String label = (effectiveType == ChatMessage.CHAT_TYPE.TO) ? labelTo : labelFrom;

            // Span boundaries for group(3)
            int start = m.start(3);
            int endEx = m.end(3);

            spans.add(new Span(start, endEx, label));
        }

        return spans;
    }

}

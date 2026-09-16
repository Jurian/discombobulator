package nl.hva.cmi.lessen.discombobulator.steps.detect;

import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;
import nl.hva.cmi.lessen.discombobulator.model.Span;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class GreetingSpanDetector implements SpanDetector {

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

    public GreetingSpanDetector(String labelTo, String labelFrom, Pattern pattern) {
        this.pattern = pattern;
        this.labelTo = labelTo;
        this.labelFrom = labelFrom;
    }

    @Override
    public List<Span> detect(String text, ChatMessage message) {
        if (text == null || text.isEmpty()) return Collections.emptyList();
        if (message.type == null) return Collections.emptyList();

        var m = pattern.matcher(text);
        var spans = new ArrayList<Span>();

        while (m.find()) {
            String prefix = (m.group(1) == null) ? "" : m.group(1);
            String greeting = m.group(2);
            String candidateName = m.group(3);

            if (candidateName == null) continue;
            if (candidateName.equalsIgnoreCase(greeting)) continue;

            boolean isReplyContext = !prefix.isEmpty();
            ChatMessage.CHAT_TYPE effectiveType = isReplyContext
                    ? (message.type == ChatMessage.CHAT_TYPE.TO ? ChatMessage.CHAT_TYPE.FROM : ChatMessage.CHAT_TYPE.TO)
                    : message.type;

            String label = (effectiveType == ChatMessage.CHAT_TYPE.TO) ? labelTo : labelFrom;

            spans.add(new Span(m.start(3), m.end(3), label));
        }

        return spans;
    }
}

package nl.hva.cmi.lessen.discombobulator.steps.detect;

import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;
import nl.hva.cmi.lessen.discombobulator.model.Span;

import java.util.List;

public interface SpanDetector {
    /**
     * Detects PII spans in {@code text}.
     *
     * @param text    the current working text (may differ from {@code message.content}
     *                if a previous replace step has already run)
     * @param message the original message, used for metadata such as direction
     */
    List<Span> detect(String text, ChatMessage message);
}

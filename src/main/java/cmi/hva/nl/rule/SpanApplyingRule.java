package cmi.hva.nl.rule;

import cmi.hva.nl.parse.ChatMessage;

import java.util.List;

public abstract class SpanApplyingRule implements TextAnonymizationRule, SpanDetector {

    private final int maxSpanDistance;

    public SpanApplyingRule(int maxSpanDistance) {
        this.maxSpanDistance = maxSpanDistance;
    }

    @Override
    public String apply(ChatMessage message) {
        if (message.content == null) return null;
        if (message.content.isEmpty()) return message.content;

        List<Span> spans = detect(message);

        if (maxSpanDistance >= 0 && spans.size() > 1) {
            Span.consolidateNearbySpans(spans, maxSpanDistance); // in-place in your codebase
        }

        return applySpans(message.content, spans, Span::getLabel);
    }

    protected String applySpans(String text, List<Span> spans, java.util.function.Function<Span, String> labelResolver) {
        StringBuilder out = new StringBuilder(text.length());
        int last = 0;

        for (Span span : spans) {
            if (span.getStart() < last) continue;

            out.append(text, last, span.getStart());
            out.append(labelResolver.apply(span));
            last = span.getEndExclusive();
        }

        out.append(text, last, text.length());
        return out.toString();
    }
}
package nl.hva.cmi.lessen.discombobulator.steps.replace;

import nl.hva.cmi.lessen.discombobulator.pipeline.PipelineStep;
import nl.hva.cmi.lessen.discombobulator.model.Span;

import java.util.List;

public abstract class SpanApplyingRule implements PipelineStep {

    private final int maxSpanDistance;

    public SpanApplyingRule(int maxSpanDistance) {
        this.maxSpanDistance = maxSpanDistance;
    }

    @Override
    public void apply() {

        if (maxSpanDistance >= 0 && spans.size() > 1) {
            Span.consolidateNearbySpans(spans, maxSpanDistance);
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
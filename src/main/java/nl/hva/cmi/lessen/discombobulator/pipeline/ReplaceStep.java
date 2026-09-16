package nl.hva.cmi.lessen.discombobulator.pipeline;

import nl.hva.cmi.lessen.discombobulator.model.Span;

import java.util.Comparator;
import java.util.List;

public class ReplaceStep implements PipelineStep {

    @Override
    public void apply(PipelineContext ctx) {
        ctx.spans.sort(Comparator.comparingInt(Span::getStart));
        ctx.text = applySpans(ctx.text, ctx.spans);
        ctx.spans.clear();
    }

    private static String applySpans(String text, List<Span> spans) {
        StringBuilder out = new StringBuilder(text.length());
        int last = 0;
        for (Span span : spans) {
            if (span.getStart() < last) continue; // skip overlapping spans
            out.append(text, last, span.getStart());
            out.append(span.getLabel());
            last = span.getEndExclusive();
        }
        out.append(text, last, text.length());
        return out.toString();
    }
}

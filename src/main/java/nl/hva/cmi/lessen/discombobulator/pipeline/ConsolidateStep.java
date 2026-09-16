package nl.hva.cmi.lessen.discombobulator.pipeline;

import nl.hva.cmi.lessen.discombobulator.model.Span;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

public class ConsolidateStep implements PipelineStep {

    private final String label;
    private final int maxSpanDistance;

    public ConsolidateStep(String label, int maxSpanDistance) {
        this.label = label;
        this.maxSpanDistance = maxSpanDistance;
    }

    @Override
    public void apply(PipelineContext ctx) {
        consolidate(ctx.spans, maxSpanDistance, label);
    }

    private static void consolidate(List<Span> spans, int maxSpanDistance, String label) {
        spans.sort(Comparator.comparingInt(Span::getStart));

        boolean merged;
        do {
            merged = false;
            for (int i = 0; i < spans.size(); i++) {
                Span a = spans.get(i);
                for (ListIterator<Span> it = spans.listIterator(i + 1); it.hasNext(); ) {
                    Span b = it.next();
                    if (b.getStart() - a.getEndExclusive() > maxSpanDistance) break;
                    if (a.isNearby(b, maxSpanDistance)) {
                        a.merge(b, label);
                        it.remove();
                        merged = true;
                        break;
                    }
                }
                if (merged) break;
            }
        } while (merged);

        spans.forEach(s -> s.setLabel(label));
    }
}

package nl.hva.cmi.lessen.discombobulator.steps.detect;

import nl.hva.cmi.lessen.discombobulator.pipeline.PipelineStep;
import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;
import nl.hva.cmi.lessen.discombobulator.model.Span;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

public class SpanConsolidator implements PipelineStep {

    private final int maxSpanDistance;

    public SpanConsolidator(int maxSpanDistance) {
        this.maxSpanDistance = maxSpanDistance;
    }

    private List<Span> consolidateNearbySpans(List<Span> spans) {

        spans.sort(Comparator.comparingInt(Span::getStart));

        boolean merged;
        do {
            merged = false;

            for (int i = 0; i < spans.size(); i++) {
                Span a = spans.get(i);

                // iterate b's with an iterator, starting at i+1
                for (ListIterator<Span> it = spans.listIterator(i + 1); it.hasNext(); ) {
                    Span b = it.next();

                    if (b.getStart() - a.getEndExclusive() > maxSpanDistance) {
                        break; // sorted => no more candidates for a
                    }

                    if (a.isNearby(b, maxSpanDistance)) {
                        a.merge(b);
                        it.remove();      // safe removal, no skipping risk
                        merged = true;
                        break;            // restart scanning (like your break outer)
                    }
                }

                if (merged) break; // restart outer scan to catch chain merges
            }

        } while (merged);

        return spans;
    }

    @Override
    public List<Span> detect(ChatMessage message) {
        return List.of();
    }
}

package nl.hva.cmi.lessen.discombobulator.steps.detect;

import nl.hva.cmi.lessen.discombobulator.pipeline.PipelineStep;
import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;
import nl.hva.cmi.lessen.discombobulator.model.Span;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TrieSpanDetector implements SpanDetector, PipelineStep {

    private final Trie trie;
    private final String label;

    private final Pattern doNotMatchPattern; // if matches anchor -> skip

    public TrieSpanDetector(Trie trie, String label, int maxSpanDistance, Pattern doNotMatchPattern) {
        this.trie = trie;
        this.label = label;
        this.doNotMatchPattern = doNotMatchPattern;
    }

    @Override
    public List<Span> detect(ChatMessage message) {
        if (message.content == null || message.content.isEmpty()) return java.util.Collections.emptyList();

        if(doNotMatchPattern == null) {
            List<Span> spans = new ArrayList<>();
            for (Emit e : trie.parseText(message.content)) {
                spans.add(new Span(e, label));
            }
            return spans;
        }

        List<Span> spans = new ArrayList<>();
        for (Emit e : trie.parseText(message.content)) {
            String spanText = message.content.substring(e.getStart(), e.getEnd() + 1);
            if (doNotMatchPattern.matcher(spanText).find()) continue; // exclude
            spans.add(new Span(e, label));
        }

        return postProcess(message, spans);
    }

    /**
     * Hook for subclasses to filter/modify trie matches in-place.
     * Default: no-op.
     */
    protected List<Span> postProcess(ChatMessage message, List<Span> candidates) {
        return candidates;
    }

    @Override
    public void applyStep(PipelineStep previousStep) {

    }
}


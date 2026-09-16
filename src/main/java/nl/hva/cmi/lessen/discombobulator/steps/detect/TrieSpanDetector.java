package nl.hva.cmi.lessen.discombobulator.steps.detect;

import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;
import nl.hva.cmi.lessen.discombobulator.model.Span;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class TrieSpanDetector implements SpanDetector {

    private final Trie trie;
    private final String label;
    private final Pattern doNotMatchPattern;

    public TrieSpanDetector(Trie trie, String label, Pattern doNotMatchPattern) {
        this.trie = trie;
        this.label = label;
        this.doNotMatchPattern = doNotMatchPattern;
    }

    @Override
    public List<Span> detect(String text, ChatMessage message) {
        if (text == null || text.isEmpty()) return Collections.emptyList();

        List<Span> spans = new ArrayList<>();
        for (Emit e : trie.parseText(text)) {
            if (doNotMatchPattern != null) {
                String spanText = text.substring(e.getStart(), e.getEnd() + 1);
                if (doNotMatchPattern.matcher(spanText).find()) continue;
            }
            spans.add(new Span(e, label));
        }
        return postProcess(text, message, spans);
    }

    /** Hook for subclasses to filter or modify trie matches. Default: no-op. */
    protected List<Span> postProcess(String text, ChatMessage message, List<Span> candidates) {
        return candidates;
    }
}

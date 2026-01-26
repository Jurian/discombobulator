package cmi.hva.nl.rule;

import cmi.hva.nl.parse.ChatMessage;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TrieRule extends SpanApplyingRule {

    private final Trie trie;
    private final String label;

    private final Pattern doNotMatchPattern; // if matches anchor -> skip

    public TrieRule(Trie trie, String label) {
        this(trie, label, -1,null);
    }

    public TrieRule(Trie trie, String label, int maxSpanDistance) {
        this(trie, label, maxSpanDistance, null);
    }

    public TrieRule(Trie trie, String label,  int maxSpanDistance, Pattern doNotMatchPattern) {
        super(maxSpanDistance);
        this.trie = trie;
        this.label = label;
        this.doNotMatchPattern = doNotMatchPattern;
    }

    @Override
    public List<Span> detect(ChatMessage message) {
        if (message.content == null || message.content.isEmpty()) return java.util.Collections.emptyList();

        if(doNotMatchPattern == null) return Span.find(trie, message.content, label);

        List<Span> spans = new ArrayList<>();
        for (Emit e : trie.parseText(message.content)) {

            String spanText = message.content.substring(e.getStart(), e.getEnd() + 1);
            if (doNotMatchPattern.matcher(spanText).find()) {
                continue; // exclude
            }
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
}


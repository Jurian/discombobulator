package cmi.hva.nl.rule;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code Span} represents a labeled region within a text by character offsets.
 *
 * <p>
 * Spans are used by {@link TextAnonymizationRule} implementations to describe
 * detected text segments (e.g., identifiers such as names, postal codes, or URLs)
 * that should be replaced by a placeholder label.
 * </p>
 *
 * <p>
 * A span is defined by a start index (inclusive) and an end index (exclusive),
 * following the common Java substring convention: {@code text.substring(start, endExclusive)}.
 * </p>
 *
 * <p>
 * Spans can be merged to form clusters of nearby or overlapping detections.
 * The {@code size} field counts how many original spans were merged into the
 * current span, which is useful for downstream decisions (for example,
 * emitting {@code [ADDRESS]} when multiple address components are nearby).
 * </p>
 *
 * <p>
 * This class is mutable. Callers should treat instances as internal working
 * objects and avoid sharing them across threads without external synchronization.
 * </p>
 */
public class Span {

    /**
     * Number of original spans represented by this span. Starts at {@code 1}
     * and increases when spans are merged.
     */
    private int size = 1;
    /** Start offset (inclusive) in the source text. */
    private int start;
    /** End offset (exclusive) in the source text. */
    private int endExclusive;
    /**
     * Replacement label associated with this span (e.g., {@code [EMAIL]}).
     * May be {@code null} for intermediate spans depending on calling logic.
     */
    private String label;

    /**
     * Returns the label associated with this span.
     *
     * @return the label (may be {@code null})
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Returns how many original spans have been merged into this span.
     *
     * @return the number of merged spans
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Returns the start offset (inclusive).
     *
     * @return the start index
     */
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Returns the end offset (exclusive).
     *
     * @return the end-exclusive index
     */
    public int getEndExclusive() {
        return endExclusive;
    }

    public void setEndExclusive(int endExclusive) {
        this.endExclusive = endExclusive;
    }

    public int length() {
        return this.endExclusive - this.start;
    }

    /**
     * Creates a new span.
     *
     * @param start        start offset (inclusive)
     * @param endExclusive end offset (exclusive)
     * @param label        replacement label to associate with this span (may be {@code null})
     */
    public Span(int start, int endExclusive, String label) {
        this.start = start;
        this.endExclusive = endExclusive;
        this.label = label;
    }

    /**
     * Creates a new {@code Span} from an {@link Emit} produced by an Aho–Corasick
     * trie match.
     *
     * <p>
     * The start offset is taken directly from {@link Emit#getStart()}, and the end
     * offset is derived from {@link Emit#getEnd()} by converting it to an
     * end-exclusive index ({@code end + 1}), consistent with Java substring
     * conventions.
     * </p>
     *
     * @param e     the emitted match from a trie parse operation
     * @param label the replacement label to associate with this span
     */
    public Span(Emit e, String label) {
        this(e.getStart(), e.getEnd() + 1, label);
    }

    /**
     * Creates a new {@code Span} from the current match of a {@link Matcher}.
     *
     * <p>
     * The start and end offsets are taken directly from
     * {@link Matcher#start()} and {@link Matcher#end()}, which already follow
     * the start-inclusive, end-exclusive convention used by
     * {@link String#substring(int, int)}.
     * </p>
     *
     * <p>
     * This constructor is typically used by regex-based detection logic to
     * convert matches into span representations suitable for consolidation
     * and replacement.
     * </p>
     *
     * @param m     the matcher positioned at a successful match
     * @param label the replacement label to associate with this span
     */
    public Span(Matcher m, String label) {
        this(m.start(), m.end(), label);
    }

    /**
     * Merges the given span into this span, expanding boundaries to cover both.
     *
     * <p>
     * The resulting label remains this span's current label.
     * The {@code size} counter is increased by the other span's size.
     * </p>
     *
     * @param span the span to merge into this span
     */
    public void merge(Span span) {
        merge(span, this.label);
    }

    /**
     * Merges the given span into this span, expanding boundaries to cover both,
     * and sets the resulting label explicitly.
     *
     * <p>
     * The start becomes {@code min(start)}, the end becomes {@code max(endExclusive)}.
     * The {@code size} counter is increased by the other span's size.
     * </p>
     *
     * @param span  the span to merge into this span
     * @param label the label to assign to the merged span
     */
    public void merge(Span span, String label) {
        this.start = Math.min(start, span.start);
        this.endExclusive = Math.max(endExclusive, span.endExclusive);
        this.label = label;
        this.size += span.size;
    }

    /**
     * Consolidates (merges) spans that overlap or occur within {@code maxSpanDistance}
     * characters of each other.
     *
     * <p>
     * The input list is sorted in-place by {@code start} and then repeatedly scanned
     * until no more merges are possible (to correctly handle chain merges).
     * </p>
     *
     * <p>
     * When two spans are merged, the resulting span keeps the label of the first span
     * encountered (i.e., the earlier span after sorting), and its {@code size} becomes
     * the sum of merged sizes.
     * </p>
     *
     * @param spans           spans to consolidate (modified in-place)
     * @param maxSpanDistance maximum allowed gap (in characters) between spans to merge
     * @return the same list instance, containing consolidated spans
     */
    public static List<Span> consolidateNearbySpans(List<Span> spans, int maxSpanDistance) {

        spans.sort(Comparator.comparingInt(c -> c.start));

        boolean merged;
        do {
            merged = false;

            for (int i = 0; i < spans.size(); i++) {
                Span a = spans.get(i);

                // iterate b's with an iterator, starting at i+1
                for (ListIterator<Span> it = spans.listIterator(i + 1); it.hasNext(); ) {
                    Span b = it.next();

                    if (b.start - a.endExclusive > maxSpanDistance) {
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

    /**
     * Flattens multiple span lists and consolidates nearby/overlapping spans.
     *
     * @param spanLists        iterable of span lists to merge and consolidate
     * @param maxSpanDistance  maximum allowed gap (in characters) between spans to merge
     * @return a consolidated list of spans
     */
    public static List<Span> consolidateNearbySpans(Iterable<List<Span>> spanLists, int maxSpanDistance) {
        List<Span> merged = new ArrayList<>();
        for(List<Span> spanList : spanLists) {
            merged.addAll(spanList);
        }
        return consolidateNearbySpans(merged, maxSpanDistance);
    }

    /**
     * Returns {@code true} if this span overlaps {@code other} or if the distance
     * between them is at most {@code maxSpanDistance}.
     *
     * <p>
     * Distance is measured in characters between the end of the earlier span and
     * the start of the later span.
     * </p>
     *
     * @param other           the other span
     * @param maxSpanDistance maximum allowed gap (in characters) to consider "nearby"
     * @return {@code true} if spans overlap or are within the threshold distance
     */
    public boolean isNearby(Span other, int maxSpanDistance) {
        if (endExclusive <= other.start) { // b after a
            return other.start - endExclusive <= maxSpanDistance;
        } else if (other.endExclusive <= start) { // a after b
            return start- other.endExclusive  <= maxSpanDistance;
        } else { // overlap
            return true;
        }
    }

    /**
     * Returns {@code true} if {@code b} ends before (or exactly at) this span's start
     * and the gap between {@code b} and this span is at most {@code maxSpanDistance}.
     *
     * <p>
     * Note: this method checks whether {@code b} is immediately before {@code this}
     * (i.e., {@code b -> this}), not the other way around.
     * </p>
     *
     * @param b               the candidate span that may be before this span
     * @param maxSpanDistance maximum allowed gap (in characters)
     * @return {@code true} if {@code b} is nearby before this span
     */
    public boolean isNearbyBefore(Span b, int maxSpanDistance) {
        return b.endExclusive <= start && start - b.endExclusive <= maxSpanDistance;
    }

    /**
     * Finds all occurrences of keywords from the given Aho–Corasick {@link Trie}
     * in the provided text and converts them into {@link Span} instances.
     *
     * <p>
     * Each emitted match from the trie is translated into a span using the
     * {@link Span#Span(Emit, String)} constructor. The resulting spans use
     * start-inclusive and end-exclusive character offsets.
     * </p>
     *
     * <p>
     * This is a convenience method intended to centralize trie-based span creation
     * logic so that matching behavior remains consistent across callers.
     * </p>
     *
     * @param trie  the trie used to detect keyword occurrences
     * @param text  the input text to scan
     * @param label the label to associate with each detected span
     * @return a list of spans representing all detected matches
     */
    public static List<Span> find(Trie trie, String text, String label) {
        List<Span> spans = new ArrayList<>();
        for (Emit e : trie.parseText(text)) {
            spans.add(new Span(e, label));
        }
        return spans;
    }

    /**
     * Finds all matches of the given regular expression {@link Pattern}
     * in the provided text and converts them into {@link Span} instances.
     *
     * <p>
     * Each regex match is translated into a span using the
     * {@link Span#Span(Matcher, String)} constructor. The resulting spans use
     * start-inclusive and end-exclusive character offsets.
     * </p>
     *
     * <p>
     * This method centralizes regex-based span creation and ensures consistent
     * handling of match boundaries across different rules.
     * </p>
     *
     * @param pattern the regular expression pattern to apply
     * @param text    the input text to scan
     * @param label   the label to associate with each detected span
     * @return a list of spans representing all detected matches
     */
    public static List<Span> find(Pattern pattern, String text, String label) {
        List<Span> spans = new ArrayList<>();
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            spans.add(new Span(m, label));
        }
        return spans;
    }
}

package nl.hva.cmi.lessen.discombobulator.model;

import org.ahocorasick.trie.Emit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * {@code Span} represents a labeled region within a text by character offsets.
 *
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

    public void setLabel(String label) {
        this.label = label;
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
}

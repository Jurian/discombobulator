package cmi.hva.nl.rule;

import cmi.hva.nl.parse.ChatMessage;
import org.ahocorasick.trie.Trie;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AnchoredTrieRule extends TrieRule {

    public enum AnchorSide {
        BEFORE,
        AFTER,
        BOTH
    }

    private final Pattern anchorPattern;
    private final int anchorMaxDistance;
    private final AnchorSide anchorSide;

    private final Pattern alwaysMatchPattern;

    public AnchoredTrieRule(
            Trie trie,
            String label,
            Pattern anchorPattern,
            int anchorMaxDistance
    ) {
        this(trie, label, -1, null, anchorPattern, anchorMaxDistance, AnchorSide.BEFORE, null);
    }

    public AnchoredTrieRule(
            Trie trie,
            String label,
            Pattern anchorPattern,
            int anchorMaxDistance,
            AnchorSide anchorSide
    ) {
        this(trie, label, -1, null, anchorPattern, anchorMaxDistance, anchorSide, null);
    }

    public AnchoredTrieRule(
            Trie trie,
            String label,
            Pattern anchorPattern,
            int anchorMaxDistance,
            AnchorSide anchorSide,
            Pattern acceptSpanPattern
    ) {
        this(trie, label, -1, null, anchorPattern, anchorMaxDistance, anchorSide, acceptSpanPattern);
    }

    public AnchoredTrieRule(
            Trie trie,
            String label,
            int maxSpanDistance,
            Pattern anchorPattern,
            int anchorMaxDistance
    ) {
        this(trie, label, maxSpanDistance, null, anchorPattern, anchorMaxDistance, AnchorSide.BEFORE, null);
    }

    public AnchoredTrieRule(
            Trie trie,
            String label,
            int maxSpanDistance,
            Pattern anchorPattern,
            int anchorMaxDistance,
            AnchorSide anchorSide
    ) {
        this(trie, label, maxSpanDistance, null, anchorPattern, anchorMaxDistance, anchorSide, null);
    }

    public AnchoredTrieRule(
            Trie trie,
            String label,
            int maxSpanDistance,
            Pattern anchorPattern,
            int anchorMaxDistance,
            AnchorSide anchorSide,
            Pattern acceptSpanPattern
    ) {
        this(trie, label, maxSpanDistance, null, anchorPattern, anchorMaxDistance, anchorSide, acceptSpanPattern);
    }

    public AnchoredTrieRule(
            Trie trie,
            String label,
            int maxSpanDistance,
            Pattern doNotMatchPattern,
            Pattern anchorPattern,
            int anchorMaxDistance,
            AnchorSide anchorSide,
            Pattern alwaysMatchPattern
    ) {
        super(trie, label, maxSpanDistance, doNotMatchPattern);
        this.anchorPattern = anchorPattern;
        this.anchorMaxDistance = anchorMaxDistance;
        this.anchorSide = (anchorSide == null) ? AnchorSide.BEFORE : anchorSide;
        this.alwaysMatchPattern = alwaysMatchPattern; // nullable
    }

    @Override
    protected List<Span> postProcess(ChatMessage message, List<Span> candidates) {
        String text = message.content;
        if (candidates.isEmpty()) return candidates;

        AnchorIndex anchors = AnchorIndex.build(anchorPattern, text);

        for (var it = candidates.iterator(); it.hasNext(); ) {
            Span span = it.next();

            int start = span.getStart();
            int endEx = span.getEndExclusive();

            if (alwaysMatchPattern != null) {
                String spanText = text.substring(start, endEx);
                if (alwaysMatchPattern.matcher(spanText).matches()) {
                    continue; // keep without needing an anchor
                }
            }

            boolean okBefore = false;
            boolean okAfter = false;

            if (anchorSide == AnchorSide.BEFORE || anchorSide == AnchorSide.BOTH) {
                okBefore = isAnchoredBefore(text, anchors, start);
            }
            if (anchorSide == AnchorSide.AFTER || anchorSide == AnchorSide.BOTH) {
                okAfter = isAnchoredAfter(text, anchors, endEx);
            }

            boolean keep = (anchorSide == AnchorSide.BOTH) ? (okBefore || okAfter)
                    : (anchorSide == AnchorSide.BEFORE) ? okBefore
                    : okAfter;

            if (!keep) it.remove();
        }

        return candidates;
    }

    // ---------------------------
    // BEFORE anchoring
    // ---------------------------

    private boolean isAnchoredBefore(String text, AnchorIndex anchors, int spanStart) {
        // immediate: anchor ends right before spanStart (ignoring whitespace)
        int anchorEnd = endBeforeWhitespace(text, spanStart);
        if (anchors.hasAnchorEndingAt(anchorEnd)) return true;

        // otherwise: last anchor whose end <= spanStart and within max distance
        int idx = anchors.lastAnchorEndingAtOrBefore(spanStart);
        if (idx < 0) return false;

        int lastAnchorEnd = anchors.endByEndIndex(idx);
        int distance = spanStart - lastAnchorEnd;
        return distance >= 0 && within(distance, anchorMaxDistance);
    }

    private static int endBeforeWhitespace(String text, int pos) {
        int i = pos - 1;
        while (i >= 0 && Character.isWhitespace(text.charAt(i))) i--;
        return i + 1;
    }

    // ---------------------------
    // AFTER anchoring
    // ---------------------------

    private boolean isAnchoredAfter(String text, AnchorIndex anchors, int spanEndExclusive) {
        // immediate: anchor starts right after spanEndExclusive (ignoring whitespace)
        int anchorStart = startAfterWhitespace(text, spanEndExclusive);
        if (anchors.hasAnchorStartingAt(anchorStart)) return true;

        // otherwise: first anchor whose start >= spanEndExclusive and within max distance
        int idx = anchors.firstAnchorStartingAtOrAfter(spanEndExclusive);
        if (idx < 0) return false;

        int nextAnchorStart = anchors.startByStartIndex(idx);
        int distance = nextAnchorStart - spanEndExclusive;
        return distance >= 0 && within(distance, anchorMaxDistance);
    }

    private static int startAfterWhitespace(String text, int pos) {
        int i = pos;
        while (i < text.length() && Character.isWhitespace(text.charAt(i))) i++;
        return i;
    }

    private static boolean within(int distance, int maxDistance) {
        // If you want "-1 means unlimited", keep this.
        // If you want "-1 means disabled", change it.
        return maxDistance < 0 || distance <= maxDistance;
    }

    // ---------------------------
    // Cached anchor index
    // ---------------------------

    /**
     * Anchor matches cached once, then queried via binary search.
     *
     * We keep two sorted views:
     * - by END for BEFORE queries
     * - by START for AFTER queries
     */
    private static final class AnchorIndex {

        // Sorted by start (scan order)
        private final int[] startsSorted;
        private final int[] endsAlignedToStarts;

        // Sorted by end
        private final int[] endsSorted;
        private final int[] startsAlignedToEnds;

        private AnchorIndex(
                int[] startsSorted,
                int[] endsAlignedToStarts,
                int[] endsSorted,
                int[] startsAlignedToEnds
        ) {
            this.startsSorted = startsSorted;
            this.endsAlignedToStarts = endsAlignedToStarts;
            this.endsSorted = endsSorted;
            this.startsAlignedToEnds = startsAlignedToEnds;
        }

        int size() { return startsSorted.length; }

        // ---- queries by END (BEFORE) ----

        boolean hasAnchorEndingAt(int end) {
            return binarySearchAny(endsSorted, end) >= 0;
        }

        int lastAnchorEndingAtOrBefore(int position) {
            int idx = upperBound(endsSorted, position) - 1;
            return idx >= 0 ? idx : -1;
        }

        int endByEndIndex(int idx) { return endsSorted[idx]; }
        int startAlignedToEndIndex(int idx) { return startsAlignedToEnds[idx]; }

        // ---- queries by START (AFTER) ----

        boolean hasAnchorStartingAt(int start) {
            return binarySearchAny(startsSorted, start) >= 0;
        }

        int firstAnchorStartingAtOrAfter(int position) {
            int idx = lowerBound(startsSorted, position);
            return (idx >= 0 && idx < startsSorted.length) ? idx : -1;
        }

        int startByStartIndex(int idx) { return startsSorted[idx]; }
        int endAlignedToStartIndex(int idx) { return endsAlignedToStarts[idx]; }

        static AnchorIndex build(Pattern anchorPattern, String text) {
            Matcher m = anchorPattern.matcher(text);

            List<int[]> matches = new ArrayList<>();
            while (m.find()) {
                int s = m.start();
                int e = m.end();
                if (s == e) continue; // ignore zero-length anchors
                matches.add(new int[]{s, e});
            }

            if (matches.isEmpty()) {
                return new AnchorIndex(new int[0], new int[0], new int[0], new int[0]);
            }

            // startsSorted is naturally sorted by scan order
            int n = matches.size();
            int[] startsSorted = new int[n];
            int[] endsAlignedToStarts = new int[n];
            for (int i = 0; i < n; i++) {
                startsSorted[i] = matches.get(i)[0];
                endsAlignedToStarts[i] = matches.get(i)[1];
            }

            // build endsSorted + startsAlignedToEnds by sorting indices by end
            Integer[] order = new Integer[n];
            for (int i = 0; i < n; i++) order[i] = i;
            java.util.Arrays.sort(order, (a, b) -> Integer.compare(endsAlignedToStarts[a], endsAlignedToStarts[b]));

            int[] endsSorted = new int[n];
            int[] startsAlignedToEnds = new int[n];
            for (int i = 0; i < n; i++) {
                int j = order[i];
                endsSorted[i] = endsAlignedToStarts[j];
                startsAlignedToEnds[i] = startsSorted[j];
            }

            return new AnchorIndex(startsSorted, endsAlignedToStarts, endsSorted, startsAlignedToEnds);
        }

        // ---- binary search helpers ----

        private static int binarySearchAny(int[] a, int target) {
            int lo = 0, hi = a.length - 1;
            while (lo <= hi) {
                int mid = (lo + hi) >>> 1;
                int v = a[mid];
                if (v < target) lo = mid + 1;
                else if (v > target) hi = mid - 1;
                else return mid;
            }
            return -1;
        }

        // first index i where a[i] > value
        private static int upperBound(int[] a, int value) {
            int lo = 0, hi = a.length;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (a[mid] <= value) lo = mid + 1;
                else hi = mid;
            }
            return lo;
        }

        // first index i where a[i] >= value
        private static int lowerBound(int[] a, int value) {
            int lo = 0, hi = a.length;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (a[mid] < value) lo = mid + 1;
                else hi = mid;
            }
            return lo;
        }
    }
}

package nl.hva.cmi.lessen.discombobulator.steps.detect;

import nl.hva.cmi.lessen.discombobulator.model.ChatMessage;
import nl.hva.cmi.lessen.discombobulator.model.Span;
import org.ahocorasick.trie.Trie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AnchoredTrieSpanDetector extends TrieSpanDetector {

    public enum AnchorSide {
        BEFORE,
        AFTER,
        BOTH
    }

    private final Pattern anchorPattern;
    private final int anchorMaxDistance;
    private final AnchorSide anchorSide;
    private final Pattern alwaysMatchPattern;

    public AnchoredTrieSpanDetector(
            Trie trie,
            String label,
            Pattern doNotMatchPattern,
            Pattern anchorPattern,
            int anchorMaxDistance,
            AnchorSide anchorSide,
            Pattern alwaysMatchPattern) {
        super(trie, label, doNotMatchPattern);
        this.anchorPattern = anchorPattern;
        this.anchorMaxDistance = anchorMaxDistance;
        this.anchorSide = (anchorSide == null) ? AnchorSide.BEFORE : anchorSide;
        this.alwaysMatchPattern = alwaysMatchPattern;
    }

    @Override
    protected List<Span> postProcess(String text, ChatMessage message, List<Span> candidates) {
        if (candidates.isEmpty()) return candidates;

        AnchorIndex anchors = AnchorIndex.build(anchorPattern, text);

        for (var it = candidates.iterator(); it.hasNext(); ) {
            Span span = it.next();

            int start = span.getStart();
            int endEx = span.getEndExclusive();

            if (alwaysMatchPattern != null) {
                String spanText = text.substring(start, endEx);
                if (alwaysMatchPattern.matcher(spanText).matches()) {
                    continue;
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

            boolean keep = switch (anchorSide) {
                case BOTH -> okBefore || okAfter;
                case BEFORE -> okBefore;
                case AFTER -> okAfter;
            };

            if (!keep) it.remove();
        }

        return candidates;
    }

    private boolean isAnchoredBefore(String text, AnchorIndex anchors, int spanStart) {
        int anchorEnd = endBeforeWhitespace(text, spanStart);
        if (anchors.hasAnchorEndingAt(anchorEnd)) return true;

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

    private boolean isAnchoredAfter(String text, AnchorIndex anchors, int spanEndExclusive) {
        int anchorStart = startAfterWhitespace(text, spanEndExclusive);
        if (anchors.hasAnchorStartingAt(anchorStart)) return true;

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
        return maxDistance < 0 || distance <= maxDistance;
    }

    private record AnchorIndex(int[] startsSorted, int[] endsAlignedToStarts,
                               int[] endsSorted, int[] startsAlignedToEnds) {

        boolean hasAnchorEndingAt(int end) {
            return binarySearchAny(endsSorted, end) >= 0;
        }

        int lastAnchorEndingAtOrBefore(int position) {
            int idx = upperBound(endsSorted, position) - 1;
            return idx >= 0 ? idx : -1;
        }

        int endByEndIndex(int idx) {
            return endsSorted[idx];
        }

        boolean hasAnchorStartingAt(int start) {
            return binarySearchAny(startsSorted, start) >= 0;
        }

        int firstAnchorStartingAtOrAfter(int position) {
            int idx = lowerBound(startsSorted, position);
            return (idx >= 0 && idx < startsSorted.length) ? idx : -1;
        }

        int startByStartIndex(int idx) {
            return startsSorted[idx];
        }

        static AnchorIndex build(Pattern anchorPattern, String text) {
            Matcher m = anchorPattern.matcher(text);
            List<int[]> matches = new ArrayList<>();
            while (m.find()) {
                int s = m.start(), e = m.end();
                if (s == e) continue;
                matches.add(new int[]{s, e});
            }

            if (matches.isEmpty()) {
                return new AnchorIndex(new int[0], new int[0], new int[0], new int[0]);
            }

            int n = matches.size();
            int[] startsSorted = new int[n];
            int[] endsAlignedToStarts = new int[n];
            for (int i = 0; i < n; i++) {
                startsSorted[i] = matches.get(i)[0];
                endsAlignedToStarts[i] = matches.get(i)[1];
            }

            Integer[] order = new Integer[n];
            for (int i = 0; i < n; i++) order[i] = i;
            Arrays.sort(order, (a, b) -> Integer.compare(endsAlignedToStarts[a], endsAlignedToStarts[b]));

            int[] endsSorted = new int[n];
            int[] startsAlignedToEnds = new int[n];
            for (int i = 0; i < n; i++) {
                int j = order[i];
                endsSorted[i] = endsAlignedToStarts[j];
                startsAlignedToEnds[i] = startsSorted[j];
            }

            return new AnchorIndex(startsSorted, endsAlignedToStarts, endsSorted, startsAlignedToEnds);
        }

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

        private static int upperBound(int[] a, int value) {
            int lo = 0, hi = a.length;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (a[mid] <= value) lo = mid + 1;
                else hi = mid;
            }
            return lo;
        }

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

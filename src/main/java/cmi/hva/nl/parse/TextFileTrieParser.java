package cmi.hva.nl.parse;

import org.ahocorasick.trie.Trie;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

/**
 * {@code TextFileTrieParser} builds an Aho–Corasick {@link Trie} from one or more
 * text files.
 *
 * <p>
 * Each non-empty line in the provided files is treated as a keyword and added
 * to a trie structure that can be used for efficient multi-pattern matching.
 * The parser supports several configurable options that influence matching
 * behavior, such as case sensitivity, whole-word matching, and overlap handling.
 * </p>
 *
 * <p>
 * This class is part of the parsing and preprocessing layer. It is typically
 * used to load linguistic resources (e.g. name lists, place names, brand names)
 * that are later consumed by anonymization rules. It does not process
 * user-generated text directly and does not perform anonymization itself.
 * </p>
 *
 * <p>
 * An optional ignore list can be supplied to exclude specific keywords while
 * building the trie. This is useful for filtering out terms that would otherwise
 * cause false positives.
 * </p>
 */
public class TextFileTrieParser implements Parser<Trie> {
    /**
     * Optional set of keywords that should be ignored when building the trie.
     * May be {@code null} if no ignore list is used.
     */
    private final Set<String> ignored;

    /** Whether keyword matching should be case-insensitive. */
    private boolean ignoreCase;

    /** Whether only whole-word matches should be emitted. */
    private boolean wholeWords;

    /** Whether overlapping matches should be ignored by the trie. */
    private boolean ignoreOverlaps;

    /**
     * Returns whether case-insensitive matching is enabled.
     *
     * @return {@code true} if case-insensitive matching is enabled
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    /**
     * Enables or disables case-insensitive keyword matching.
     *
     * @param ignoreCase {@code true} to ignore character case
     */
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    /**
     * Returns whether whole-word matching is enabled.
     *
     * @return {@code true} if only whole words are matched
     */
    public boolean isWholeWords() {
        return wholeWords;
    }

    /**
     * Enables or disables whole-word matching.
     *
     * @param wholeWords {@code true} to match only whole words
     */
    public void setWholeWords(boolean wholeWords) {
        this.wholeWords = wholeWords;
    }

    /**
     * Returns whether overlapping matches are ignored.
     *
     * @return {@code true} if overlapping matches are ignored
     */
    public boolean isIgnoreOverlaps() {
        return ignoreOverlaps;
    }

    /**
     * Enables or disables ignoring of overlapping matches.
     *
     * @param ignoreOverlaps {@code true} to ignore overlapping matches
     */
    public void setIgnoreOverlaps(boolean ignoreOverlaps) {
        this.ignoreOverlaps = ignoreOverlaps;
    }

    /**
     * Creates a new {@code TextFileTrieParser} without an ignore list.
     */
    public TextFileTrieParser() {
        this.ignored = null;
    }

    /**
     * Creates a new {@code TextFileTrieParser} with a set of ignored keywords.
     *
     * @param ignored keywords that should not be added to the trie
     */
    public TextFileTrieParser(Set<String> ignored) {
        this.ignored = ignored;
    }


    /**
     * Parses a single text file and builds a {@link Trie}.
     *
     * @param fileName the file to parse
     * @return a trie containing all parsed keywords
     * @throws IOException if the file cannot be read
     */
    @Override
    public Trie parse(String fileName) throws IOException {
        return parse(java.util.List.of(fileName));
    }

    /**
     * Parses multiple text files and builds a {@link Trie}.
     *
     * <p>
     * Files are processed in the order provided. Matching behavior is controlled
     * by the configured flags ({@link #setIgnoreCase(boolean)},
     * {@link #setWholeWords(boolean)}, {@link #setIgnoreOverlaps(boolean)}).
     * </p>
     *
     * @param fileNames iterable of file names to parse
     * @return a trie containing all parsed keywords
     * @throws IOException if any file cannot be read
     */
    public Trie parse(Iterable<String> fileNames) throws IOException {

        if(this.ignored != null) return parseIgnored(fileNames);

        Trie.TrieBuilder builder = Trie.builder();
        if(ignoreCase) builder = builder.ignoreCase();
        if(ignoreOverlaps) builder = builder.ignoreOverlaps();
        if(wholeWords) builder = builder.onlyWholeWords();

        for (String fileName : fileNames) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        builder.addKeyword(line);
                    }
                }
            }
        }

        return builder.build();
    }

    /**
     * Parses text files and builds a {@link Trie}, excluding ignored keywords.
     *
     * <p>
     * Any keyword present in the {@code ignored} set is skipped during trie
     * construction.
     * </p>
     *
     * @param fileNames iterable of file names to parse
     * @return a trie containing all parsed keywords except ignored ones
     * @throws IOException if any file cannot be read
     */
    private Trie parseIgnored(Iterable<String> fileNames) throws IOException {

        Trie.TrieBuilder builder = Trie.builder();
        if(ignoreCase) builder = builder.ignoreCase();
        if(ignoreOverlaps) builder = builder.ignoreOverlaps();
        if(wholeWords) builder = builder.onlyWholeWords();

        for (String fileName : fileNames) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !ignored.contains(line)) {
                        builder.addKeyword(line);
                    }
                }
            }
        }

        return builder.build();
    }
}
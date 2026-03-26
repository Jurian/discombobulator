package nl.hva.cmi.lessen.discombobulator.parse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * {@code TextFileParser} is a generic utility for parsing one or more text files
 * line by line and collecting mapped values into a set.
 *
 * <p>
 * Each non-empty line is trimmed and passed through a user-supplied
 * {@link Function} that maps the raw line to a value of type {@code T}.
 * Lines for which the mapping function returns {@code null} are ignored.
 * </p>
 *
 * <p>
 * This class is part of the parsing and preprocessing layer. It is commonly
 * used to load linguistic resources or configuration data (e.g. name lists,
 * ignore lists) rather than user-generated content. It does not perform any
 * anonymization or privacy-related processing itself.
 * </p>
 *
 * <p>
 * The resulting set preserves insertion order and is returned as an
 * unmodifiable collection.
 * </p>
 *
 * @param <T> the type of values produced from each parsed line
 */
public class TextFileParser<T> implements Parser<Set<T>> {

    private final Function<String, T> lineMapper;

    public TextFileParser(Function<String, T> lineMapper) {
        this.lineMapper = lineMapper;
    }

    public Set<T> parse(String fileName) throws IOException {
        return parse(java.util.List.of(fileName));
    }

    /**
     * Parses multiple text files and aggregates their contents.
     *
     * <p>
     * Files are processed in the order provided. Duplicate values (as defined
     * by {@link Object#equals(Object)}) are ignored.
     * </p>
     *
     * @param fileNames iterable of file names to parse
     * @return an unmodifiable set of mapped values
     * @throws IOException if any file cannot be read
     */
    public Set<T> parse(Iterable<String> fileNames) throws IOException {
        Set<T> results = new LinkedHashSet<>();

        for (String fileName : fileNames) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        T value = lineMapper.apply(line);
                        if (value != null) {
                            results.add(value);
                        }
                    }
                }
            }
        }

        return java.util.Collections.unmodifiableSet(results);
    }
}
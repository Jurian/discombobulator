package cmi.hva.nl.parse;

import java.io.IOException;

/**
 * Generic interface for parsing input resources into structured representations.
 *
 * <p>
 * Implementations of {@code Parser} read data from an external source—typically
 * a file—and transform it into a structured in-memory representation of type
 * {@code R}. This interface is intentionally minimal to support a variety of
 * parsing strategies and target data structures.
 * </p>
 *
 * <p>
 * In the context of this codebase, parsers are primarily used for loading
 * configuration data and linguistic resources (e.g. name lists, ignore lists)
 * rather than user-generated content. As such, this interface does not define
 * any privacy or anonymization behavior by itself.
 * </p>
 *
 * @param <R> the type of the parsed result
 */
public interface Parser<R> {

    /**
     * Parses the given file and produces a result of type {@code R}.
     *
     * @param fileName the name or path of the file to parse
     * @return the parsed representation
     * @throws IOException if the file cannot be read or parsed
     */
    R parse(String fileName) throws IOException;
}
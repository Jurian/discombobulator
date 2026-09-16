package nl.hva.cmi.lessen.discombobulator.config;

import nl.hva.cmi.lessen.discombobulator.parse.TextFileParser;
import nl.hva.cmi.lessen.discombobulator.parse.TextFileTrieParser;
import nl.hva.cmi.lessen.discombobulator.pipeline.*;
import nl.hva.cmi.lessen.discombobulator.steps.detect.*;
import org.ahocorasick.trie.Trie;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class PipelineFactory {

    public static Pipeline build(AppConfig config) throws IOException {
        Map<String, SpanDetector> detectors = buildDetectors(config.detectors);

        List<PipelineStep> steps = new ArrayList<>();
        for (StepConfig stepConfig : config.pipeline) {
            steps.add(buildStep(stepConfig, detectors));
        }
        return new Pipeline(steps);
    }

    private static Map<String, SpanDetector> buildDetectors(
            Map<String, DetectorConfig> configs) throws IOException {

        Map<String, SpanDetector> detectors = new LinkedHashMap<>();
        for (var entry : configs.entrySet()) {
            detectors.put(entry.getKey(), buildDetector(entry.getValue()));
        }
        return detectors;
    }

    private static SpanDetector buildDetector(DetectorConfig config) throws IOException {
        // AnchoredTrieDetectorConfig must be checked before TrieDetectorConfig (it extends it)
        if (config instanceof AnchoredTrieDetectorConfig a) return buildAnchoredTrie(a);
        if (config instanceof TrieTailRegexDetectorConfig t) return buildTrieTailRegex(t);
        if (config instanceof TrieDetectorConfig t)         return buildTrie(t);
        if (config instanceof RegexDetectorConfig r)        return buildRegex(r);
        if (config instanceof GreetingDetectorConfig g)     return buildGreeting(g);
        throw new IllegalArgumentException("Unknown detector type: " + config.getClass().getSimpleName());
    }

    private static PipelineStep buildStep(
            StepConfig config,
            Map<String, SpanDetector> detectors) {

        if (config instanceof DetectStepConfig d) {
            List<SpanDetector> stepDetectors = d.detectors.stream()
                    .map(name -> {
                        SpanDetector det = detectors.get(name);
                        if (det == null) throw new IllegalArgumentException("Unknown detector: " + name);
                        return det;
                    })
                    .toList();
            return new DetectStep(stepDetectors);
        }
        if (config instanceof ConsolidateStepConfig c) return new ConsolidateStep(c.label, c.maxSpanDistance);
        if (config instanceof ReplaceStepConfig)       return new ReplaceStep();
        throw new IllegalArgumentException("Unknown step type: " + config.getClass().getSimpleName());
    }

    // -----------------------------------------------------------------------
    // Detector builders
    // -----------------------------------------------------------------------

    private static RegexSpanDetector buildRegex(RegexDetectorConfig cfg) {
        int flags = 0;
        if (cfg.options.ignoreCase)            flags |= Pattern.CASE_INSENSITIVE;
        if (cfg.options.multiline)             flags |= Pattern.MULTILINE;
        if (cfg.options.dotAll)                flags |= Pattern.DOTALL;
        if (cfg.options.unicodeCharacterClass) flags |= Pattern.UNICODE_CHARACTER_CLASS;
        if (cfg.options.comments)              flags |= Pattern.COMMENTS;
        return new RegexSpanDetector(cfg.label, Pattern.compile(cfg.pattern, flags));
    }

    private static TrieSpanDetector buildTrie(TrieDetectorConfig cfg) throws IOException {
        Trie trie = buildTrieFromConfig(cfg);
        return new TrieSpanDetector(trie, cfg.label, null);
    }

    private static AnchoredTrieSpanDetector buildAnchoredTrie(AnchoredTrieDetectorConfig cfg) throws IOException {
        Trie trie = buildTrieFromConfig(cfg);
        Pattern anchorPattern = Pattern.compile(Pattern.quote(cfg.anchorPattern));
        return new AnchoredTrieSpanDetector(
                trie, cfg.label, null,
                anchorPattern, cfg.anchorMaxDistance, cfg.anchorSide, null);
    }

    private static GreetingSpanDetector buildGreeting(GreetingDetectorConfig cfg) {
        Pattern pattern = cfg.pattern != null
                ? Pattern.compile(cfg.pattern, cfg.ignoreCase ? Pattern.CASE_INSENSITIVE : 0)
                : GreetingSpanDetector.defaultPattern();
        return new GreetingSpanDetector(cfg.labelTo, cfg.labelFrom, pattern);
    }

    private static SpanDetector buildTrieTailRegex(TrieTailRegexDetectorConfig cfg) {
        throw new UnsupportedOperationException("trieTailRegex detector is not yet implemented");
    }

    private static Trie buildTrieFromConfig(TrieDetectorConfig cfg) throws IOException {
        Set<String> ignored = loadIgnoreFiles(cfg.options.ignoreFiles);

        TextFileTrieParser parser = new TextFileTrieParser(ignored.isEmpty() ? null : ignored);
        parser.setIgnoreCase(cfg.options.ignoreCase);
        parser.setIgnoreOverlaps(cfg.options.ignoreOverlaps);
        parser.setWholeWords(cfg.options.wholeWords);
        return parser.parse(cfg.file);
    }

    private static Set<String> loadIgnoreFiles(List<String> ignoreFiles) throws IOException {
        if (ignoreFiles == null || ignoreFiles.isEmpty()) return Set.of();
        TextFileParser<String> parser = new TextFileParser<>(
                line -> line.startsWith("#") ? null : line);
        return parser.parse(ignoreFiles);
    }
}

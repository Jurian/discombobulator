package nl.hva.cmi.lessen.discombobulator.rule;

public record TrieOptions(
        boolean ignoreOverlaps,
        boolean wholeWords,
        boolean ignoreCase,
        java.util.List<String> ignoreFile,
        int maxSpanDistance) { }

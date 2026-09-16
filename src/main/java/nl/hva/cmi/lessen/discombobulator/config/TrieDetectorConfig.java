package nl.hva.cmi.lessen.discombobulator.config;

import java.util.List;

public class TrieDetectorConfig extends DetectorConfig {
    public String label;
    public String file;
    public Options options = new Options();

    public static class Options {
        public boolean ignoreOverlaps = true;
        public boolean wholeWords = false;
        public boolean ignoreCase = false;
        public List<String> ignoreFiles = List.of();
    }
}

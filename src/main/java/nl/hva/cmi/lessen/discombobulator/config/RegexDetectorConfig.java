package nl.hva.cmi.lessen.discombobulator.config;

public class RegexDetectorConfig extends DetectorConfig {
    public String label;
    public String pattern;
    public Options options = new Options();

    public static class Options {
        public boolean ignoreCase = false;
        public boolean multiline = false;
        public boolean dotAll = false;
        public boolean unicodeCharacterClass = false;
        public boolean comments = false;
    }
}

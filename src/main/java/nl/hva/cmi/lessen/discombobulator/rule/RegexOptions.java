package nl.hva.cmi.lessen.discombobulator.rule;

public record RegexOptions(
        boolean ignoreCase,
        boolean unicodeCase,
        boolean multiline,
        boolean dotAll,
        boolean unicodeCharacterClass,
        boolean comments,
        int maxSpanDistance) {

    public int toPatternFlags() {
        int flags = 0;
        if (ignoreCase) flags |= java.util.regex.Pattern.CASE_INSENSITIVE;
        if (unicodeCase) flags |= java.util.regex.Pattern.UNICODE_CASE;
        if (multiline) flags |= java.util.regex.Pattern.MULTILINE;
        if (dotAll) flags |= java.util.regex.Pattern.DOTALL;
        if (unicodeCharacterClass) flags |= java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;
        if (comments) flags |= java.util.regex.Pattern.COMMENTS;
        return flags;
    }
}

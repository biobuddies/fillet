package es.biobuddi.fillet;

import java.util.regex.Pattern;

/** Shell and curl style globs: `*` any run, `?` any one, `[!a-z]` sets, `{a,b}` alternatives. */
final class Glob {

    private Glob() {}

    static Pattern compile(String glob) {
        var regex = new StringBuilder();
        var depth = 0;
        for (var index = 0; index < glob.length(); index++) {
            var character = glob.charAt(index);
            var end = character == '[' ? glob.indexOf(']', index + 2) : -1;
            if (character == '\\' && index + 1 < glob.length()) {
                regex.append(Pattern.quote(String.valueOf(glob.charAt(++index))));
            } else if (end > 0) {
                var set = glob.substring(index + 1, end);
                regex
                    .append(
                        set.matches("[!^].+") ? "[^" + escape(set.substring(1)) : "[" + escape(set)
                    )
                    .append(']');
                index = end;
            } else if (character == '{') {
                depth++;
                regex.append("(?:");
            } else if (character == '}' && depth > 0) {
                depth--;
                regex.append(')');
            } else {
                regex.append(
                    character == '*'
                        ? ".*"
                        : character == '?'
                          ? "."
                          : character == ',' && depth > 0
                            ? "|"
                            : Pattern.quote(String.valueOf(character))
                );
            }
        }
        if (depth > 0) throw new IllegalArgumentException("Unclosed { in " + glob);
        return Pattern.compile(regex.toString(), Pattern.DOTALL);
    }

    /** Neutralize Java character class syntax that globs lack, like `[a&&b]` and `[[a]]`. */
    private static String escape(String set) {
        return set.replaceAll("[\\\\\\[\\]&]", "\\\\$0");
    }

    static boolean matches(String glob, String target) {
        return compile(glob).matcher(target).matches();
    }
}

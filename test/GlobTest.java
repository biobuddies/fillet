package es.biobuddi.fillet;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class GlobTest {

    public static void main(String[] arguments) {
        var targets = List.of(
            "android-app://com.google.android.apps.maps",
            "android-app://com.paris.velib",
            "android-app://com.walmart.android",
            "android-app://gov.nps.mobileapp",
            "http://acparis.org",
            "https://acparis.org",
            "https://www.acparis.org",
            "https://www.nps.gov",
            "https://www.walmart.com"
        );
        Map.ofEntries(
            Map.entry("*", targets),
            Map.entry("https://acparis.org", List.of("https://acparis.org")),
            Map.entry("android-app://com.paris.velib", List.of("android-app://com.paris.velib")),
            Map.entry(
                "{android-app://com.paris.velib,https://acparis.org}",
                List.of("android-app://com.paris.velib", "https://acparis.org")
            ),
            Map.entry(
                "https://*acparis.org",
                List.of("https://acparis.org", "https://www.acparis.org")
            ),
            Map.entry("http?://acparis.org", List.of("https://acparis.org")),
            Map.entry(
                "http{,s}://acparis.org",
                List.of("http://acparis.org", "https://acparis.org")
            ),
            Map.entry(
                "https://www.[nw]*",
                List.of("https://www.nps.gov", "https://www.walmart.com")
            ),
            Map.entry(
                "android-app://com.[!p]*",
                List.of(
                    "android-app://com.google.android.apps.maps",
                    "android-app://com.walmart.android"
                )
            ),
            Map.entry("android-app://*", targets.subList(0, 4))
        ).forEach((glob, expected) -> {
            var actual = targets
                .stream()
                .filter(target -> Glob.matches(glob, target))
                .collect(Collectors.toList());
            assert actual.equals(expected) : glob + " matched " + actual;
        });
        Stream.of("a.b", "a+b", "a(b)", "a|b", "a$b", "a^b", "a\\b").forEach(literal -> {
            var actual = Stream.of(literal, "ab", "axb")
                .filter(target -> Glob.matches(literal.replace("\\", "\\\\"), target))
                .collect(Collectors.toList());
            assert actual.equals(List.of(literal)) : literal + " matched " + actual;
        });
        assert Glob.matches("\\*", "*") && !Glob.matches("\\*", "x");
        assert Glob.matches("[[&]]", "&]");
        assert Glob.matches("}", "}");
        try {
            Glob.compile("{a,b");
            assert false : "unclosed { compiled";
        } catch (IllegalArgumentException expected) {}
    }
}

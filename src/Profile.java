package es.biobuddi.fillet;

import java.util.List;
import java.util.Map;

/**
 * Values to fill, offered where any association glob matches the target: `https://example.org`
 * for web pages or `android-app://org.example` for apps.
 */
record Profile(String label, List<String> associations, Map<Field, String> values) {
    static final List<Profile> ALL = List.of(
        new Profile(
            "The American Church in Paris",
            List.of("*"),
            Map.of(
                Field.COUNTRY,
                "France",
                Field.LOCALITY,
                "Paris",
                Field.ORGANIZATION,
                "The American Church in Paris",
                Field.PHONE,
                "+33 1 40 62 05 00",
                Field.POSTAL_CODE,
                "75007",
                Field.STREET,
                "65 Quai d'Orsay",
                Field.URL,
                "https://acparis.org"
            )
        )
    );

    static String target(String packageName, String webScheme, String webDomain) {
        return webDomain == null
            ? "android-app://" + packageName
            : (webScheme == null ? "https" : webScheme) + "://" + webDomain;
    }

    boolean associates(String target) {
        return associations.stream().anyMatch(glob -> Glob.matches(glob, target));
    }
}

package es.biobuddi.fillet;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Form fields, recognized from autofill hints, HTML autocomplete/name/id, resource ids, and
 * placeholders. Declaration order is precedence; SKIP claims decoys such as address-line2.
 */
enum Field {
    SKIP(
        "addressline2",
        "addressline3",
        "address2",
        "address3",
        "addresslevel1",
        "addresslevel3",
        "email",
        "familyname",
        "firstname",
        "givenname",
        "lastname",
        "password",
        "province",
        "region",
        "state",
        "username"
    ),
    POSTAL_CODE("codepostal", "postalcode", "postcode", "zip", "zipcode"),
    COUNTRY("country", "pays"),
    LOCALITY("addresslevel2", "city", "locality", "town", "ville"),
    PHONE("phone", "tel", "telephone"),
    STREET("address", "addressline1", "address1", "postaladdress", "street", "streetaddress"),
    ORGANIZATION("company", "name", "org", "organization", "organization"),
    URL("homepage", "url", "website");

    private final List<String> keywords;

    Field(String... keywords) {
        this.keywords = List.of(keywords);
    }

    /** Short keywords match whole candidates only, so `tel` skips `hotel`. */
    boolean matches(String candidate) {
        return keywords
            .stream()
            .anyMatch(
                keyword ->
                    candidate.equals(keyword) ||
                    (keyword.length() >= 4 && candidate.contains(keyword))
            );
    }

    static Optional<Field> classify(List<String> candidates) {
        return candidates
            .stream()
            .map(candidate -> candidate.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", ""))
            .flatMap(candidate ->
                Arrays.stream(values())
                    .filter(field -> field.matches(candidate))
                    .limit(1)
            )
            .findFirst()
            .filter(field -> field != SKIP);
    }
}

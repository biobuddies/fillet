package es.biobuddi.fillet;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class FieldTest {

    public static void main(String[] arguments) {
        Map.ofEntries(
            Map.entry(List.of("address-line1"), Optional.of(Field.STREET)),
            Map.entry(List.of("street-address"), Optional.of(Field.STREET)),
            Map.entry(List.of("address-level2"), Optional.of(Field.LOCALITY)),
            Map.entry(List.of("postal-code"), Optional.of(Field.POSTAL_CODE)),
            Map.entry(List.of("country-name"), Optional.of(Field.COUNTRY)),
            Map.entry(List.of("tel"), Optional.of(Field.PHONE)),
            Map.entry(List.of("organization"), Optional.of(Field.ORGANIZATION)),
            Map.entry(List.of("url"), Optional.of(Field.URL)),
            Map.entry(List.of("address-line2", "address"), Optional.<Field>empty()),
            Map.entry(List.of("address-level1"), Optional.<Field>empty()),
            Map.entry(List.of("username"), Optional.<Field>empty()),
            Map.entry(List.of("hotel"), Optional.<Field>empty()),
            Map.entry(List.of("postal_address_postal_code"), Optional.of(Field.POSTAL_CODE)),
            Map.entry(List.of("q7", "billingCity"), Optional.of(Field.LOCALITY)),
            Map.entry(List.of("", "Telephone number"), Optional.of(Field.PHONE)),
            Map.entry(List.<String>of(), Optional.<Field>empty())
        ).forEach((candidates, expected) -> {
            var actual = Field.classify(candidates);
            assert actual.equals(expected) : candidates + " classified " + actual;
        });
    }
}

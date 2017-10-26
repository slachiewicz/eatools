package no.eatools.diagramgen;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * @author ohs
 */
public class EaAttributeSet {
    public static final String ATTRIBUTE_DELIMITER = ";";
    public static final String KEYVALUE_SEPARATOR = "=";
    Map<EaAttributeName, String> theAttributes = new HashMap<>();
    public static final EaAttributeSet EMPTY = new EaAttributeSet("");

    public EaAttributeSet(final String attributeString) {
        Arrays.stream(trimToEmpty(attributeString).split(ATTRIBUTE_DELIMITER))
              .forEach(this::add);
    }

    private void add(final String attribute) {
        final String[] keyValue = attribute.split(KEYVALUE_SEPARATOR);
        if (keyValue.length < 1) {
            return;
        }
        final EaAttributeName key = EaAttributeName.fromString(keyValue[0]);
        theAttributes.put(key, keyValue.length > 1 ? keyValue[1] : "");
    }

    public String add(final EaAttributeName key, final String value) {
        return theAttributes.put(key, value);
    }

    public String get(final EaAttributeName key) {
        return theAttributes.get(key);
    }

    public String toString() {
        return theAttributes.entrySet()
                            .stream()
                            .map(e -> e.getKey() + KEYVALUE_SEPARATOR + e.getValue())
                            .collect(Collectors.joining(ATTRIBUTE_DELIMITER));
    }
}

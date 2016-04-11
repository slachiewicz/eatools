package no.eatools.diagramgen;

import no.bouvet.ohs.jops.Camel;
import no.bouvet.ohs.jops.Enums;

/**
 * The set of EA Meta types (should correspond to UML meta types).
 *
 * @author AB22273
 * @date 23.okt.2008
 * @since 23.okt.2008 14:12:40
 */
public enum EaMetaType {
    NULL,
    CLASS,
    COMPONENT,
    NODE,
    OBJECT,
    TAGGED_VALUE,
    PACKAGE,
    RELATIONSHIP,
    ASSOCIATION,
    DEPENDENCY,
    GENERALIZATION,
    REALIZATION,
    LINK,
    DIAGRAM,
    INTERFACE,
    NOTE,
    TEXT,
    ACTOR,
    DATA_STORE,
    QUEUE,
    PROVIDED_INTERFACE;
    // etc.
    // todo complete the set


    /**
     * Simple factory for getting safe EAmetType.
     *
     * @param metaType
     * @return never null
     */
    public static EaMetaType fromString(final String metaType) {
        return Enums.valueOf(EaMetaType.class, Camel.toConstantString(metaType), NULL);
    }

    public String toString() {
        return Camel.toCamelCaseString(super.toString());
    }

    public boolean equals(final String stringType) {
        return toString().equals(stringType);
    }
}

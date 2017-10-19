package no.eatools.diagramgen;

import no.bouvet.ohs.jops.Camel;
import no.bouvet.ohs.jops.Enums;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The set of EA Meta types (should correspond to UML meta types).
 *
 * @author AB22273
 * @date 23.okt.2008
 * @since 23.okt.2008 14:12:40
 */
public enum EaMetaType {
    ACTOR,
    ASSOCIATION,
    CLASS,
    COMPONENT,
    DATA_STORE,
    DEPENDENCY,
    DIAGRAM,
    GENERALIZATION,
    INTERFACE,
    LINK,
    NESTING,
    NODE,
    NOTE,
    OBJECT,
    PACKAGE,
    PROCESS,
    PROVIDED_INTERFACE,
    QUEUE,
    REALIZATION,
    REALISATION,
    RELATIONSHIP,
    TAGGED_VALUE,
    TEXT,
    SEQUENCE,
    WEB_PAGE,
    CLIENT_PAGE,
    NULL;
    // etc.
    // todo complete the set

    private static final transient Logger LOG = LoggerFactory.getLogger(EaMetaType.class);


    /**
     * Simple factory for getting safe EaMetaType.
     *
     * @param metaType
     * @return never null
     */
    public static EaMetaType fromString(final String metaType) {
        LOG.debug("Looking up metatype for [{}]", metaType);
        final EaMetaType result = Enums.valueOf(EaMetaType.class, Camel.toConstantString(metaType), NULL);
        if(result == NULL) {
            LOG.warn("Unknown metaType for {}", metaType);
        }
        return result;
    }

    public String toString() {
        return Camel.toCamelCaseString(super.toString());
    }

    public boolean equals(final String stringType) {
        return toString().equals(stringType);
    }


    public String toEaString() {
        return StringUtils.capitalize(toString().toLowerCase());
    }
}

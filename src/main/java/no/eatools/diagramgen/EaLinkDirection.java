package no.eatools.diagramgen;

import no.bouvet.ohs.jops.Camel;
import no.bouvet.ohs.jops.Default;
import no.bouvet.ohs.jops.Enums;

/**
 * @author AB22273
 * @date 28.nov.2008
 * @since 28.nov.2008 12:01:46
 */
public enum EaLinkDirection {
    @Default
    UNSPECIFIED,

    /**
     * "Source -> Destination"
     */
    SOURCE_DESTINATION {
        public String toString() {
            return super.toString().replace(Camel.HYPHEN, SPACED_RIGHT_ARROW);
        }
    },

    /**
     * "Destination -> Source"
     */
    DESTINATION_SOURCE {
        public String toString() {
            return super.toString().replace(Camel.HYPHEN, SPACED_RIGHT_ARROW);
        }
    },

    /**
     * "Bi-Directional"
     */
    BI_DIRECTIONAL
    ;

    public static final String SPACED_RIGHT_ARROW = " -> ";

    /**
     * Simple factory for getting safe EA metaType.
     *
     * @param metaType
     * @return never null
     */
    public static EaLinkDirection fromString(String metaType) {
        return Enums.valueOf(EaLinkDirection.class, Camel.toConstantString(metaType), UNSPECIFIED);
    }

    public String toString() {
        return Camel.toHyphenatedCamelCaseString(super.toString());
    }
}

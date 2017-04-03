package no.eatools.diagramgen;

import org.sparx.ObjectType;

/**
 * @author ohs
 */
public class EaObjectType {
    public static String toString(ObjectType type) {
        return type.toString().replaceFirst("ot", "");
    }
}

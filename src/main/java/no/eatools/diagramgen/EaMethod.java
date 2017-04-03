package no.eatools.diagramgen;

import org.sparx.Method;

/**
 * @author ohs
 */
public class EaMethod {
    private final Method theMethod;


    public EaMethod(EaElement owner, String methodName) {
        theMethod = owner.addMethod(methodName);

    }
}

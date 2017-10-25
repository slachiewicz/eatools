package no.eatools.diagramgen;

import java.util.Collections;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Element;
import org.sparx.Method;

import static org.junit.Assert.*;

/**
 * @author ohs
 */
public class EaElementTest extends AbstractEaTestCase {
    private static final transient Logger LOG = LoggerFactory.getLogger(EaElementTest.class);

    @Test
    public void testAddMethod() throws Exception {
        final EaPackage pkg = eaRepo.findPackageByName("Objekter", true);
        final Element service = eaRepo.findOrCreateComponentInPackage(pkg, "AService");
        final EaElement theService = new EaElement(service, eaRepo);
        final int id = theService.getId();
        final String methodName = "operation1";
        final String returnType = "int";
        final EaMethod theMethod = theService.addMethod(methodName, returnType, service.GetMethods(), Collections.emptyList());
        theMethod.addParameter("aParameter", "TypicalType");

        eaRepo.clearPackageCache();

        boolean found = false;
        final Element refoundElement = eaRepo.findElementByID(id);
        for (final Method method : refoundElement.GetMethods()) {
            LOG.info("Found method [{}] params [{}]", method.GetName(), method.GetParameters());
            if(method.GetName().equals(methodName) && method.GetReturnType().equals(returnType)) {
                found = true;
            }
        }
        assertTrue(found);

        // clean up
        assertTrue(new EaElement(refoundElement, eaRepo).removeMethod(methodName, returnType));
    }
}

package no.eatools.diagramgen;

import org.junit.Test;
import org.sparx.Element;

/**
 * @author ohs
 */
public class EaElementTest extends AbstractEaTestCase {

    @Test
    public void testAddMethod() throws Exception {
        EaPackage pkg = eaRepo.findPackageByName("Klasser", true);
        Element service = eaRepo.findOrCreateComponentInPackage(pkg.unwrap(), "AService");
        EaElement theService = new EaElement(service, eaRepo);
        theService.addMethod("operation1");
    }
}

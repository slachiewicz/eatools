package no.eatools.documenter;

import java.util.Date;

import no.eatools.diagramgen.AbstractEaTestCase;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author ohs
 */
public class EaXsdTypeTest extends AbstractEaTestCase {

    @Test
    @Category(no.eatools.documenter.EaXsdTypeTest.class)
    public void testSetNewDocumentation() throws Exception {
        new EaXsdType(eaRepo).setNewDocumentation("Halla Balla " + new Date().toString());
        eaRepo.close();
    }
}

package no.eatools.diagramgen;

import java.io.File;

import no.eatools.util.EaApplicationProperties;

import junit.framework.TestCase;

/**
 * Helper class that takes care of standard JUnit things
 */
public abstract class AbtractEaTestCase extends TestCase {
    protected EaRepo eaRepo;

    protected void setUp() throws Exception {
        super.setUp();

//        EaApplicationProperties.init("test.ea.application.properties", new PropertyMap(EaApplicationProperties.class));
        File modelFile = new File(EaApplicationProperties.EA_PROJECT.value());
        eaRepo = new EaRepo(modelFile);
        eaRepo.open();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        eaRepo.close();
    }
}

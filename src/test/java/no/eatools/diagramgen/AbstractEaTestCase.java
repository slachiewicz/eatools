package no.eatools.diagramgen;

import java.io.File;

import org.junit.After;
import org.junit.Before;

import static no.eatools.util.EaApplicationProperties.EA_ROOTPKG;

/**
 * Helper class that takes care of standard JUnit things
 */
public abstract class AbstractEaTestCase {
    protected EaRepo eaRepo;

    @Before
    public void setUp() throws Exception {
//        super.setUp();

//        EaApplicationProperties.init("test.ea.application.properties", new PropertyMap(EaApplicationProperties.class));
//        File modelFile = new File(EaApplicationProperties.EA_PROJECT.value());
        EA_ROOTPKG.setValue("Model");
        File modelFile = new File("test.eap");
        eaRepo = new EaRepo(modelFile);
        eaRepo.open();
    }

    @After
    public void tearDown() throws Exception {
//        super.tearDown();
        eaRepo.close();
    }
}

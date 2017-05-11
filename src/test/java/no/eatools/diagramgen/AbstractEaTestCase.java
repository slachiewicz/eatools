package no.eatools.diagramgen;

import java.io.File;

import no.bouvet.ohs.jops.Prop;
import no.eatools.util.EaApplicationProperties;

import org.junit.After;
import org.junit.Before;

import static no.eatools.util.EaApplicationProperties.*;

/**
 * Helper class that takes care of standard JUnit things
 */
public abstract class AbstractEaTestCase {
    protected EaRepo eaRepo;

    @Before
    public void setUp() throws Exception {
        // Remove properties from previous runs
        Prop.getInstance().clear();
        EaApplicationProperties.reset();
//        super.setUp();

//        EaApplicationProperties.init("test.ea.application.properties", new PropertyMap(EaApplicationProperties.class));
//        File modelFile = new File(EaApplicationProperties.EA_PROJECT.value());
        EA_ROOTPKG.setValue("Model");
        EA_PROJECT.setValue("test.eap");
        EA_DOC_ROOT_DIR.setValue("./tmp/");

        EA_DIAGRAM_URL_FILE.setValue("diagram_url.txt");
        final File modelFile = new File(EA_PROJECT.value());
        eaRepo = new EaRepo(modelFile);
        eaRepo.open();
        String testPackageName = "Klasser";
        // Purge from previous tests
        eaRepo.deletePackage(eaRepo.findPackageByName(testPackageName, true), true );
        eaRepo.findOrCreatePackage(eaRepo.findPackageByName("Domain Model", true).unwrap(),
                                   testPackageName);
    }

    @After
    public void tearDown() throws Exception {
//        super.tearDown();
        eaRepo.close();
    }
}


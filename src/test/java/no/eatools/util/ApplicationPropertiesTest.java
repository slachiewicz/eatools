package no.eatools.util;

/**
 * @author AB22273
 * @date 05.nov.2008
 */

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class ApplicationPropertiesTest extends TestCase {
    private static final transient Logger log = LoggerFactory.getLogger(ApplicationPropertiesTest.class);

    public void testLoadProperties() throws Exception {
//        EaApplicationProperties.init(null, new PropertyMap(EaApplicationProperties.class));
        String rootPackageName = EaApplicationProperties.EA_ROOTPKG.value();
        assertNotNull(rootPackageName);
        assertEquals("Model", rootPackageName);
    }

    public void testModelFileExistsAndIsReadable() throws Exception {
//        EaApplicationProperties.init(null, new PropertyMap(EaApplicationProperties.class));
        File modelFile = new File(EaApplicationProperties.EA_PROJECT.value());
        log.debug("Model file is " + modelFile.getAbsolutePath());
        assertTrue(modelFile.canRead());
    }
 }

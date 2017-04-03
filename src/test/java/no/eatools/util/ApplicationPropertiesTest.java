package no.eatools.util;

import java.io.File;

import no.eatools.diagramgen.AbstractEaTestCase;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * @author AB22273
 * @since  05.nov.2008
 */
public class ApplicationPropertiesTest extends AbstractEaTestCase {
    private static final transient Logger log = LoggerFactory.getLogger(ApplicationPropertiesTest.class);

    @Test
    public void testLoadProperties() throws Exception {
        String rootPackageName = EaApplicationProperties.EA_ROOTPKG.value();
        assertNotNull(rootPackageName);
        assertEquals("Model", rootPackageName);
    }

    @Test
    public void testModelFileExistsAndIsReadable() throws Exception {
        File modelFile = new File(EaApplicationProperties.EA_PROJECT.value());
        log.debug("Model file is " + modelFile.getAbsolutePath());
        assertTrue(modelFile.canRead());
    }
 }

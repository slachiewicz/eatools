package no.eatools.diagramgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

/**
 * Unit tests for the EaDiagram class. These tests may be run in a non-Windows environment where SSJavaCOM.dll is not available.
 */
public class EaDiagramBasicTest extends TestCase {
    private static final transient Logger log = LoggerFactory.getLogger(EaDiagramBasicTest.class);


    public void testGetAbsolutePathName() throws Exception {
        EaDiagram subject = new EaDiagram(null, null, "HeiHopp");
        assertNotNull(subject);

//        log.debug(subject.createAbsoluteFileName("a.b"));

    }
}

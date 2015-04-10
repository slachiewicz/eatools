package no.eatools.diagramgen;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * Unit tests for the EaDiagram class. These tests may be run in a non-Windows environment where SSJavaCOM.dll is not available.
 */
public class EaDiagramBasicTest extends TestCase {
    private static final Log log = LogFactory.getLog(EaDiagramBasicTest.class);


    public void testGetAbsolutePathName() throws Exception {
        EaDiagram subject = new EaDiagram(null, null, "HeiHopp");

        log.debug(subject.getAbsolutePathName());

    }
}

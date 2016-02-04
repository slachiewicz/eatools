package no.eatools.diagramgen;

import org.junit.Test;

/**
 * @author ohs
 */
public class EaDiagramGeneratorTest {

    @Test
    public void testFileUrlFromNodePath() throws Exception {
        EaDiagramGenerator.main(new String[]{"test.ea.application.properties", "-np Elhub Architecture.Information Architecture.EIM.Logical.Common.EIM Address"});

    }
}

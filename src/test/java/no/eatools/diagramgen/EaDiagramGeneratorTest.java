package no.eatools.diagramgen;

import java.util.List;

import no.bouvet.ohs.jops.Prop;
import no.eatools.util.EaApplicationProperties;

import org.junit.Test;

import static junit.framework.TestCase.*;

/**
 * @author ohs
 */
public class EaDiagramGeneratorTest extends AbstractEaTestCase {

    @Test
    public void testFileUrlFromNodePath() throws Exception {
        EaDiagramGenerator.main(new String[]{"test.ea.application.properties", "-np Elhub Architecture.Information Architecture.EIM.Logical.Common.EIM Address"});

    }

    @Test
    public void testPropertyArgs() throws Exception {
        final String[] args = {"test.ea.application.properties", "-p", "ea.package.filter=ABC"};
        EaDiagramGenerator.main(args);
        assertNull(Prop.getInstance().get("ea.package.filter"));
        assertNull(Prop.getInstance().get("EA_PACKAGE_FILTER"));
        assertNull(Prop.getInstance().get(EaApplicationProperties.EA_PACKAGE_FILTER));
        assertEquals("ABC", EaApplicationProperties.EA_PACKAGE_FILTER.value());
        EaApplicationProperties.reset();
        assertNull(EaApplicationProperties.EA_PACKAGE_FILTER.value());
    }

    @Test
    public void testToListOfPackages() throws Exception {
        String packageList = null;
        final EaDiagramGenerator subject = new EaDiagramGenerator();
        List<String> result = subject.toListOfPackages(packageList);
        System.out.printf(result.toString());
        assertTrue(result.isEmpty());

        packageList = "";
        result = subject.toListOfPackages(packageList);
        assertTrue(result.isEmpty());

        packageList = " ";
        result = subject.toListOfPackages(packageList);
        assertTrue(result.isEmpty());

        packageList = ",";
        result = subject.toListOfPackages(packageList);
        assertTrue(result.isEmpty());

        packageList = " , ";
        result = subject.toListOfPackages(packageList);
        assertTrue(result.isEmpty());

        packageList = "  , b->c";
        result = subject.toListOfPackages(packageList);
        assertEquals(1, result.size());
        assertEquals("b->c", result.get(0));

        packageList = " a , ";
        result = subject.toListOfPackages(packageList);
        assertEquals(1, result.size());
        assertEquals("a", result.get(0));

        packageList = " a , b->c";
        result = subject.toListOfPackages(packageList);
        assertEquals(2, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b->c", result.get(1));

        packageList = " a , b c ,";
        result = subject.toListOfPackages(packageList);
        assertEquals(2, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b c", result.get(1));

    }
}

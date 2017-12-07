package no.eatools.diagramgen;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author ohs
 */
public class EaPackageTest {

    @Test
    public void testHierarchyToList() throws Exception {
        String hier = "";

        assertEquals(1, EaPackage.hierarchyToList(hier).size());
        assertTrue(EaPackage.hierarchyToList(null).isEmpty());

        assertEquals(2, EaPackage.hierarchyToList("a->b").size());
        assertEquals(1, EaPackage.hierarchyToList("a->").size());
    }
}

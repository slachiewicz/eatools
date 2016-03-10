package no.eatools.diagramgen;

import java.util.List;

import no.eatools.util.EaApplicationProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Package;

/**
 * Unit tests for the EaDiagram class. Note that these tests rely on the model defined in the
 */
public class EaDiagramTest extends AbtractEaTestCase {
    private static final transient Logger log = LoggerFactory.getLogger(EaDiagramTest.class);

    public void testFindDiagramsInPackage() throws Exception {
        Package rootPkg = eaRepo.getRootPackage();
        assertNotNull(rootPkg);
        Package thePkg = eaRepo.findPackageByName("Domain Model", rootPkg, EaRepo.RECURSIVE);
        assertNotNull(thePkg);

        List<EaDiagram> diagrams = eaRepo.findDiagramsInPackage(thePkg);
        assertNotNull(diagrams);
    }

    public void testLogicalPathName() throws Exception {
        EaDiagram diagram = eaRepo.findDiagram("Domain Model");
        assertNotNull(diagram);
        String filename = diagram.getPathname();
        assertEquals("\\Model\\Domain Model", filename);
    }

    public void testGenerateDiagram() {
        //EaDiagram diagram = EaDiagram.findDiagram(eaRepo, "Domain Model");
        String diagramName = EaApplicationProperties.EA_DIAGRAM_TO_GENERATE.value();
        if (diagramName.equals("")) diagramName = "Domain Model";
        EaDiagram diagram = eaRepo.findDiagram(diagramName);
        if (diagram != null) {
            boolean didCreate = diagram.writeImageToFile(false);
            assertTrue(didCreate);
        } else {
            fail();
        }
    }

    public void testGenerateAllDiagramsInPackage() throws Exception {
        Package pkg = eaRepo.findPackageByName("Klasser", EaRepo.RECURSIVE);

        for (ImageFileFormat imageFileFormat : ImageFileFormat.values()) {
            List<EaDiagram> diagrams = eaRepo.findDiagramsInPackage(pkg);
            for (EaDiagram d : diagrams) {
                boolean didCreate = d.writeImageToFile(imageFileFormat, false);
                assertTrue(didCreate);
            }
        }
    }

    public void testGenerateAllDiagramsInProject() throws Exception {
        int count = eaRepo.generateAllDiagramsFromRoot();
        log.debug("Generated " + count + " diagrams");
        assertTrue(count > 0);
    }
}

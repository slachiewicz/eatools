package no.eatools.diagramgen;

import java.util.List;

import no.bouvet.ohs.futil.ImageFileFormat;
import no.eatools.util.EaApplicationProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Unit tests for the EaDiagram class. Note that these tests rely on the model defined in the
 */
public class EaDiagramTest extends AbstractEaTestCase {
    private static final transient Logger log = LoggerFactory.getLogger(EaDiagramTest.class);

    public void testFindDiagramsInPackage() throws Exception {
        final EaPackage rootPkg = eaRepo.getRootPackage();
        assertNotNull(rootPkg);
        final EaPackage thePkg = eaRepo.findPackageByName("Domain Model", rootPkg, EaRepo.RECURSIVE);
        assertNotNull(thePkg);

        final List<EaDiagram> diagrams = eaRepo.findDiagramsInPackage(thePkg);
        assertNotNull(diagrams);
    }

    public void testLogicalPathName() throws Exception {
        final EaDiagram diagram = eaRepo.findDiagramByName("Domain Model");
        assertNotNull(diagram);
        final String filename = diagram.getPathname();
        assertEquals("\\Model\\Domain Model", filename);
    }

    public void testGenerateDiagram() {
        //EaDiagram diagram = EaDiagram.findDiagram(eaRepo, "Domain Model");
        String diagramName = EaApplicationProperties.EA_DIAGRAM_TO_GENERATE.value();
        if (diagramName.equals("")) diagramName = "Domain Model";
        final EaDiagram diagram = eaRepo.findDiagramByName(diagramName);
        if (diagram != null) {
            final String diagramUrl = diagram.writeImageToFile(false);
            assertTrue(! diagramUrl.isEmpty());
        } else {
            fail();
        }
    }

    public void testGenerateAllDiagramsInPackage() throws Exception {
        final EaPackage pkg = eaRepo.findPackageByName("Klasser", EaRepo.RECURSIVE);

        for (final ImageFileFormat imageFileFormat : ImageFileFormat.values()) {
            final List<EaDiagram> diagrams = eaRepo.findDiagramsInPackage(pkg);
            for (final EaDiagram d : diagrams) {
                final String diagramUrl = d.writeImageToFile(imageFileFormat, false);
                assertTrue(! diagramUrl.isEmpty());
            }
        }
    }

    public void testGenerateAllDiagramsInProject() throws Exception {
        final int count = eaRepo.generateAllDiagramsFromRoot();
        log.debug("Generated " + count + " diagrams");
        assertTrue(count > 0);
    }
}

package no.eatools.util;

import java.io.File;

import no.eatools.diagramgen.ImageFileFormat;

import org.junit.Before;
import org.junit.Test;

import static no.bouvet.ohs.jops.SystemPropertySet.*;
import static no.eatools.util.NameNormalizer.*;
import static org.junit.Assert.*;

/**
 * @author ohs
 */
public class NameNormalizerTest {

    private boolean isWindows;

    @Before
    public void setUp() throws Exception {
        isWindows = OS_NAME.value()
                           .startsWith("Win");
        if (isWindows) {
            FILE_SEPARATOR.setValue(WINDOWS_SEPARATOR);
        } else {
            FILE_SEPARATOR.setValue(UNIX_SEPARATOR);
        }
    }

    @Test
    public void testNodePathToUrl() throws Exception {

        final String nodePath = "Elhub Architecture.Information Architecture.EIM.Logical.Common.EIM Address";

        String result = nodePathToUrl(nodePath);
        assertEquals("/elhub_architecture/information_architecture/eim/logical/common/eim_address.png", result);
        System.out.println(result);

        FILE_SEPARATOR.setValue("/");
        result = nodePathToUrl(nodePath);
        assertEquals("/elhub_architecture/information_architecture/eim/logical/common/eim_address.png", result);

        FILE_SEPARATOR.setValue("\\");
        result = nodePathToUrl(nodePath);
        assertEquals("/elhub_architecture/information_architecture/eim/logical/common/eim_address.png", result);
    }

    @Test
    public void testMakeFriendlyWebName() throws Exception {
        if (isWindows) {
            final String inputFile = "Elhub Architecture//Information Architecture.æåå.{Logical.Common.EIM} \\\\Address.png";

            assertEquals("\\", FILE_SEPARATOR.value());
            String result = makeWebFriendlyFilename(inputFile, true);
            assertEquals("elhub_architecture--information_architecture.aeaaaa.--logical.common.eim--_\\address.png", result);
            System.out.println(result);

            FILE_SEPARATOR.setValue("/");
            result = makeWebFriendlyFilename(inputFile, true);
            assertEquals("elhub_architecture/information_architecture.aeaaaa.--logical.common.eim--_--address.png", result);

            FILE_SEPARATOR.setValue("\\");
//        EnumProperty.syncProperties(SystemPropertySet.SystemPropertySet.getThePropertyMap());
            result = makeWebFriendlyFilename(inputFile, true);
            assertEquals("elhub_architecture--information_architecture.aeaaaa.--logical.common.eim--_\\address.png", result);

        } else {
            final String inputFile = "Elhub Architecture//Information Architecture.æåå.{Logical.Common.EIM} \\\\Address.png";

            assertEquals("/", FILE_SEPARATOR.value());
            String result = makeWebFriendlyFilename(inputFile, true);
            assertEquals("elhub_architecture/information_architecture.aeaaaa.--logical.common.eim--_--address.png", result);
            System.out.println(result);

            FILE_SEPARATOR.setValue("/");
            result = makeWebFriendlyFilename(inputFile, true);
            assertEquals("elhub_architecture/information_architecture.aeaaaa.--logical.common.eim--_--address.png", result);

            FILE_SEPARATOR.setValue("\\");
//        EnumProperty.syncProperties(SystemPropertySet.SystemPropertySet.getThePropertyMap());
            result = makeWebFriendlyFilename(inputFile, true);
            assertEquals("elhub_architecture--information_architecture.aeaaaa.--logical.common.eim--_\\address.png", result);
        }
    }

    @Test
    public void testCreateUrlForFileAbsolute() throws Exception {

        File file;
        String diagramUrlPart;
        if (isWindows) {
            file = new File("C:\\x" + FILE_SEPARATOR.value() + "y" + FILE_SEPARATOR.value() + "z.png");
            System.out.println(file.getAbsolutePath());
            diagramUrlPart = createUrlPartForFile(file, "C:\\");
        } else {
            file = new File("/x" + FILE_SEPARATOR.value() + "y" + FILE_SEPARATOR.value() + "z.png");
            System.out.println(file.getAbsolutePath());
            diagramUrlPart = createUrlPartForFile(file, "/");
        }
        System.out.println(diagramUrlPart);
        assertEquals("x/y/z.png", diagramUrlPart);
    }

    @Test
    public void testCreateUrlForFileRelative() throws Exception {

        String fileExtension = ImageFileFormat.PNG.getFileExtension();
        String rootDir;
        String rootDirRel;
        String logicalPathName = "Level.too dåp";
        String diagramName = "The Diagram";
        String diagramGUID = "{CAFE-BABE-B16B-00B5}";
        String diagramVersion = "1.0";
        DiagramNameMode diagramNameMode;

        File file;
        if (isWindows) {
            rootDirRel = "down\\deeper\\";
        } else {
            rootDirRel = "down/deeper/";
        }
        diagramNameMode = DiagramNameMode.FULL_PATH;

        file = createFile(rootDirRel, logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);

        String diagramUrlPart = createUrlPartForFile(file, rootDirRel);

        System.out.println(diagramUrlPart);
        assertEquals("down/deeper/level.too_daap/the_diagram.png", diagramUrlPart);
    }

    @Test
    public void testIsAbsoluteFileName() throws Exception {
        if (isWindows) {
            assertTrue(isAbsoluteFileName("C:\\tmp\\here"));
            assertTrue(isAbsoluteFileName("C:\\tmp\\here\\"));
            assertFalse(isAbsoluteFileName("/Volumes"));
            assertFalse(isAbsoluteFileName("/Volumes/"));
            assertFalse(isAbsoluteFileName("Volumes/"));
            assertFalse(isAbsoluteFileName("/XYZ"));
            assertTrue(isAbsoluteFileName("C:\\"));
        } else {
            assertTrue(isAbsoluteFileName("/"));
            assertTrue(isAbsoluteFileName("/Volumes"));
            assertTrue(isAbsoluteFileName("/Volumes/"));
            assertFalse(isAbsoluteFileName("Volumes/"));
            assertTrue(isAbsoluteFileName("/XYZ"));
            assertFalse(isAbsoluteFileName("C:\\"));
        }
    }

    @Test
    public void testCreateFile() throws Exception {
        String fileExtension = ImageFileFormat.PNG.getFileExtension();
        String rootDir;
        String rootDirRel;
        String logicalPathName = "Level.too dåp";
        String diagramName = "The Diagram";
        String diagramGUID = "{CAFE-BABE-B16B-00B5}";
        String diagramVersion = "1.0";
        DiagramNameMode diagramNameMode;

        File result;
        if (isWindows) {
            rootDir = "C:\\";
            rootDirRel = "down\\deeper\\";
        } else {
            rootDir = "/";
            rootDirRel = "down/deeper/";
        }
        diagramNameMode = DiagramNameMode.FULL_PATH;

        result = createFile(rootDir, logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);
        System.out.println(result.getAbsolutePath());
        if (isWindows) {
            assertEquals("C:\\level.too_daap\\the_diagram.png", result.getAbsolutePath());
        } else {
            assertEquals("/level.too_daap/the_diagram.png", result.getAbsolutePath());
        }

        result = createFile(rootDirRel, logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);
        System.out.println(result.getAbsolutePath());
    }

    @Test
    public void testCreateUrlPart() throws Exception {
        String fileExtension = ImageFileFormat.PNG.getFileExtension();
        String logicalPathName = "Level.too dåp/hær";
        String diagramName = "The Diagram";
        String diagramGUID = "{CAFE-BABE-B16B-00B5}";
        String diagramVersion = "1.0";
        DiagramNameMode diagramNameMode;

        String result;
        diagramNameMode = DiagramNameMode.FULL_PATH;

        result = createUrlPart(logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);
        System.out.println(result);
        assertEquals("level.too_daap/haer/the_diagram.png", result);
    }
}

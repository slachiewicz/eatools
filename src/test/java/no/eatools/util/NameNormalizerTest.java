package no.eatools.util;

import java.io.File;

import no.bouvet.ohs.futil.ImageFileFormat;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static no.bouvet.ohs.jops.SystemPropertySet.FILE_SEPARATOR;
import static no.bouvet.ohs.jops.SystemPropertySet.OS_NAME;
import static no.eatools.util.NameNormalizer.*;
import static org.junit.Assert.*;

/**
 * @author ohs
 */
public class NameNormalizerTest {
    private static final transient Logger LOG = LoggerFactory.getLogger(NameNormalizerTest.class);

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
    public void testDiagramNameWithLongDash() {
        final String diagramName = "Project Management – Building Block cross reference";

        String result = makeWebFriendlyName(diagramName, true, false);
        assertEquals("project_management___building_block_cross_reference", result);
    }

    @Test
    public void testMakeFriendlyWebName() throws Exception {
        if (isWindows) {
            final String inputFile = "Elhub Architecture//Information Architecture.æåå.{Logical.Common.EIM} \\\\Address.png";

            assertEquals("\\", FILE_SEPARATOR.value());
            String result = makeWebFriendlyName(inputFile, true, false);
            assertEquals("elhub_architecture--information_architecture.aeaaaa.--logical.common.eim--_\\address.png", result);
            System.out.println(result);

            FILE_SEPARATOR.setValue("/");
            result = makeWebFriendlyName(inputFile, true, false);
            assertEquals("elhub_architecture/information_architecture.aeaaaa.--logical.common.eim--_--address.png", result);

            FILE_SEPARATOR.setValue("\\");
//        EnumProperty.syncProperties(SystemPropertySet.SystemPropertySet.getThePropertyMap());
            result = makeWebFriendlyName(inputFile, true, false);
            assertEquals("elhub_architecture--information_architecture.aeaaaa.--logical.common.eim--_\\address.png", result);

        } else {
            final String inputFile = "Elhub Architecture//Information Architecture.æåå.{Logical.Common.EIM} \\\\Address.png";

            assertEquals("/", FILE_SEPARATOR.value());
            String result = makeWebFriendlyName(inputFile, true, false);
            assertEquals("elhub_architecture/information_architecture.aeaaaa.--logical.common.eim--_--address.png", result);
            System.out.println(result);

            FILE_SEPARATOR.setValue("/");
            result = makeWebFriendlyName(inputFile, true, true);
            assertEquals("elhub_architecture/information_architecture.aeaaaa.--logical.common.eim--_--address.png", result);

            FILE_SEPARATOR.setValue("\\");
//        EnumProperty.syncProperties(SystemPropertySet.SystemPropertySet.getThePropertyMap());
            result = makeWebFriendlyName(inputFile, true, false);
            assertEquals("elhub_architecture--information_architecture.aeaaaa.--logical.common.eim--_\\address.png", result);
        }
    }

//    @Test
//    public void testCreateUrlForFileAbsolute() throws Exception {
//
//        File file;
//        String diagramUrlPart;
//        if (isWindows) {
//            file = new File("C:\\x" + FILE_SEPARATOR.value() + "y" + FILE_SEPARATOR.value() + "z.png");
//            System.out.println(file.getAbsolutePath());
//            diagramUrlPart = createUrlPartForFile(file, "C:\\");
//        } else {
//            file = new File("/x" + FILE_SEPARATOR.value() + "y" + FILE_SEPARATOR.value() + "z.png");
//            System.out.println(file.getAbsolutePath());
//            diagramUrlPart = createUrlPartForFile(file, "/");
//        }
//        System.out.println(diagramUrlPart);
//        assertEquals("x/y/z.png", diagramUrlPart);
//    }
//
//    @Test
//    public void testCreateUrlForFileRelative() throws Exception {
//
//        String fileExtension = ImageFileFormat.PNG.getFileExtension();
//        String rootDir;
//        String rootDirRel;
//        String logicalPathName = "Level.too dåp";
//        String diagramName = "The Diagram";
//        String diagramGUID = "{CAFE-BABE-B16B-00B5}";
//        String diagramVersion = "1.0";
//        DiagramNameMode diagramNameMode;
//
//        File file;
//        if (isWindows) {
//            rootDirRel = "down\\deeper\\";
//        } else {
//            rootDirRel = "down/deeper/";
//        }
//        diagramNameMode = DiagramNameMode.FULL_PATH;
//
//        file = createFile(rootDirRel, logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);
//
//        String diagramUrlPart = createUrlPartForFile(file, rootDirRel);
//
//        System.out.println(diagramUrlPart);
//        assertEquals("down/deeper/level.too_daap/the_diagram.png", diagramUrlPart);
//    }

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
        final String fileExtension = ImageFileFormat.PNG.getFileExtension();
        final String rootDir;
        final String rootDirRel;
        String logicalPathName = "/Level.too dåp";
        final String diagramName = "The Diagram";
        final String diagramGUID = "{CAFE-BABE-B16B-00B5}";
        final String diagramVersion = "1.0";
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

        result = createFile(1, rootDir, logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);
        System.out.println(result.getAbsolutePath());
        if (isWindows) {
            assertEquals("C:\\level.too_daap\\the_diagram.png", result.getAbsolutePath());
        } else {
            assertEquals("/level.too_daap/the_diagram.png", result.getAbsolutePath());
        }

        result = createFile(1, rootDirRel, logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);
        System.out.println(result.getAbsolutePath());

        diagramNameMode = DiagramNameMode.GUID_AT_START;
        logicalPathName = "/Level.too dåp/two/three";

        result = createFile(0, rootDir, logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);
        System.out.println(result.getAbsolutePath());
        if (isWindows) {
            assertEquals("C:\\--cafe-babe-b16b-00b5--the_diagram.png", result.getAbsolutePath());
        } else {
            assertEquals("/--cafe-babe-b16b-00b5--the_diagram.png", result.getAbsolutePath());
        }

        result = createFile(1, rootDir, logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);
        System.out.println(result.getAbsolutePath());
        if (isWindows) {
            assertEquals("C:\\level.too_daap\\--cafe-babe-b16b-00b5--the_diagram.png", result.getAbsolutePath());
        } else {
            assertEquals("/level.too_daap/--cafe-babe-b16b-00b5--the_diagram.png", result.getAbsolutePath());
        }

        result = createFile(2, rootDir, logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);
        System.out.println(result.getAbsolutePath());
        if (isWindows) {
            assertEquals("C:\\level.too_daap\\two\\--cafe-babe-b16b-00b5--the_diagram.png", result.getAbsolutePath());
        } else {
            assertEquals("/level.too_daap/two/--cafe-babe-b16b-00b5--the_diagram.png", result.getAbsolutePath());
        }

    }

    @Test
    public void testCreateUrlPart() throws Exception {
        final String fileExtension = ImageFileFormat.PNG.getFileExtension();
        final String logicalPathName = "/Level.too dåp/hær";
        final String diagramName = "The Diagram";
        final String diagramGUID = "{CAFE-BABE-B16B-00B5}";
        final String diagramVersion = "1.0";
        DiagramNameMode diagramNameMode;

        String result;
        diagramNameMode = DiagramNameMode.FULL_PATH;

        result = createUrlPart(0, logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);
        System.out.println(result);
        assertEquals("level.too_daap/haer/the_diagram.png", result);

        result = createUrlPart(1, logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);
        System.out.println(result);
        assertEquals("level.too_daap/haer/the_diagram.png", result);

        diagramNameMode = DiagramNameMode.GUID_AT_END;

        result = createUrlPart(0, logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);
        System.out.println(result);
        assertEquals("the_diagram--cafe-babe-b16b-00b5--.png", result);

        result = createUrlPart(1, logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);
        System.out.println(result);
        assertEquals("level.too_daap/the_diagram--cafe-babe-b16b-00b5--.png", result);

        result = createUrlPart(2, logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);
        System.out.println(result);
        assertEquals("level.too_daap/haer/the_diagram--cafe-babe-b16b-00b5--.png", result);

        diagramNameMode = DiagramNameMode.GUID_AT_START;

        result = createUrlPart(0, logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);
        System.out.println(result);
        assertEquals("--cafe-babe-b16b-00b5--the_diagram.png", result);

        result = createUrlPart(1, logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);
        System.out.println(result);
        assertEquals("level.too_daap/--cafe-babe-b16b-00b5--the_diagram.png", result);

        result = createUrlPart(2, logicalPathName, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);
        System.out.println(result);
        assertEquals("level.too_daap/haer/--cafe-babe-b16b-00b5--the_diagram.png", result);
    }

    @Test
    public void testCreatePath() throws Exception {
        final String logicalPathName = "/a/b/c";

        assertEquals("", createPath(logicalPathName, -1));

        assertEquals("", createPath(logicalPathName, 0));
        assertEquals("/a", createPath(logicalPathName, 1));
        assertEquals("/a/b", createPath(logicalPathName, 2));
        assertEquals("/a/b/c", createPath(logicalPathName, 3));
        assertEquals("/a/b/c", createPath(logicalPathName, 4));
    }

    @Test
    public void testIsCygPath() throws Exception {
        final String cyppath = "/cygdrive/c/test";

        assertEquals("Not a proper windows path", "C:/test", cygPathToWindowsPath(cyppath));
    }
}

package no.eatools.diagramgen;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import no.eatools.util.ImageMetadata;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Diagram;
import org.sparx.DiagramLink;

import static no.bouvet.ohs.jops.SystemPropertySet.*;
import static no.eatools.util.EaApplicationProperties.*;
import static no.eatools.util.NameNormalizer.*;

/**
 * A Wrapper class to facilitate  Diagram generation and manipulation.
 *
 * @author Per Spilling (per.spilling@objectware.no)
 */
public class EaDiagram {
// ------------------------------ FIELDS ------------------------------

    public static final ImageFileFormat defaultImageFormat = ImageFileFormat.PNG;
    private static final transient Logger LOG = LoggerFactory.getLogger(EaDiagram.class);
    private final Diagram eaDiagram;
    private final EaRepo eaRepo;
    private final String logicalPathname;

// --------------------------- CONSTRUCTORS ---------------------------

    public EaDiagram(final EaRepo repository, final Diagram diagram, final String pathName) {
        eaDiagram = diagram;
        eaRepo = repository;
        logicalPathname = pathName;
        System.out.println("Found diagram :" + pathName + ":" + diagram.GetName() + " " + diagram.GetDiagramGUID());
    }

// -------------------------- STATIC METHODS --------------------------

    public static EaDiagram findEaDiagram(final EaRepo eaRepo, final String diagramName) {
        final EaDiagram diagram;
        if (StringUtils.isNumeric(diagramName)) {
            final int diagramId = Integer.parseInt(diagramName);
            diagram = eaRepo.findDiagramById(diagramId);
        } else {
            diagram = eaRepo.findDiagram(diagramName);
        }
        return diagram;
    }

// -------------------------- OTHER METHODS --------------------------

    public String getPathname() {
        return logicalPathname;
    }

    /**
     * Experimental
     *
     * @param style
     */
    public void setAllConnectorsToStyle(final int style) {
//        for (DiagramObject diagramObject : eaDiagram.GetDiagramObjects()) {
//            diagramObject.GetBottom();
//
//            Element element = eaRepo.findElementByID(diagramObject.GetElementID());
//            for (Connector connector : element.GetConnectors()) {
//                System.out.println("ConnectorStyle" +  connector.GetName() + ":" + connector.GetRouteStyle());
//            }
//        }
        System.out.println("Modifying diagram " + eaDiagram.GetName());
        for (final DiagramLink diagramLink : eaDiagram.GetDiagramLinks()) {
            String styleString = diagramLink.GetStyle();
            LOG.debug("Old style: {}", styleString);
            if(! styleString.contains("TREE")) {
                styleString += "TREE=OS;";
                diagramLink.SetStyle(styleString);
                LOG.debug("New style: {}", styleString);
                diagramLink.Update();
            }
        }
        eaDiagram.Update();
    }

//    public EaDiagram(final EaRepo repository, final Diagram diagram, final String pathName, final ImageFileFormat imageFormat) {
//        eaDiagram = diagram;
//        eaRepo = repository;
//        logicalPathname = pathName;
//        defaultImageFormat = imageFormat;
//    }

    public boolean writeImageToFile(final boolean urlForFileOnly) {
        try {
            return writeImageToFile(defaultImageFormat, urlForFileOnly);
        } catch (final Exception e) {
            LOG.error("Unable to write to file", e);
            return false;
        }
    }

    public boolean writeImageToFile(final ImageFileFormat imageFileFormat, final boolean urlForFileOnly) {
        // make sure the directory exists
        final File f = new File(getAbsolutePathName(logicalPathname));
        f.mkdirs();
        final String diagramFileName = getAbsoluteFilename();
        if (diagramFileName == null) {
            // Something went wrong
            LOG.error("Unable to create filename from " + logicalPathname);
            return false;
        }
        final File file = new File(diagramFileName);
        String diagramGUID = eaDiagram.GetDiagramGUID();
        LOG.debug(diagramGUID + ": " + eaDiagram.GetDiagramID() + ": " + file.getAbsolutePath());

        final String urlBase = createUrlForFile(file);
        updateDiagramUrlFile(urlBase);
        if (urlForFileOnly) {
            return true;
        }

        LOG.info("project {}, diagramguid {}, diagramfilename {}, imagefileformat {}", eaRepo.getProject(), diagramGUID, diagramFileName, imageFileFormat);
        if (eaRepo.getProject().PutDiagramImageToFile(diagramGUID, diagramFileName, imageFileFormat.isRaster())) {
            LOG.info("Diagram generated at: " + diagramFileName);
            if (!file.canRead()) {
                LOG.error("Unable to read file " + file.getAbsolutePath());
                return false;
            }
            LOG.info("Adding metadata");
            new ImageMetadata().writeCustomMetaData(file, "EA_GUID", diagramGUID);
            return true;
        } else {
            LOG.error("Unable to create diagram:" + diagramFileName);
            return false;
        }
    }

    public String getAbsolutePathName(String logicalPathname) {
        String dirRootString = EA_DOC_ROOT_DIR.value();
        final File dirRoot = new File(dirRootString);
        if (!dirRoot.isAbsolute()) {
            final File cwd = new File(USER_DIR.value());
            LOG.info(dirRootString + " is not a root directory. Using " + cwd);
            dirRootString = cwd + FILE_SEPARATOR.value() + dirRootString;
        }
        return dirRootString + makeWebFriendlyFilename(logicalPathname);
    }

    public String getAbsoluteFilename() {
        return getAbsolutePathName(logicalPathname) + FILE_SEPARATOR.value() + getFilename();
    }

    public String getFilename() {
        final String version = EA_ADD_VERSION.exists() ? eaDiagram.GetVersion() : StringUtils.EMPTY;
        return makeWebFriendlyFilename(eaDiagram.GetName() + version + defaultImageFormat.getFileExtension());
    }

    private String createUrlForFile(final File file) {
        String urlBase = "";
        try {
            final URL diagramUrl = file.toURI().toURL();
            LOG.info("Diagram url " + diagramUrl);
            urlBase = diagramUrl.toString().replace(diagramUrl.getProtocol(), "").replace(EA_DOC_ROOT_DIR.value(), "")
                    .replace(":", "");
            LOG.info("-> URLBase: " + urlBase);
        } catch (final MalformedURLException e) {
            LOG.error("Unable to create url from file " + urlBase);
        }
        return urlBase;
    }

    public static void updateDiagramUrlFile(final String urlBase) {
        File urlFile = null;
        try {
            urlFile = new File(EA_DIAGRAM_URL_FILE.value());
            FileUtils.write(urlFile, urlBase);
        } catch (final IOException e) {
            LOG.error("Unable to write url to file " + urlFile.getAbsolutePath());
        }
    }
}

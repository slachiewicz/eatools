package no.eatools.diagramgen;

import java.io.File;
import java.io.IOException;

import no.bouvet.ohs.futil.ImageFileFormat;
import no.bouvet.ohs.futil.ImageMetadata;
import no.eatools.util.DiagramNameMode;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Diagram;
import org.sparx.DiagramLink;

import static no.eatools.util.EaApplicationProperties.*;
import static no.eatools.util.NameNormalizer.*;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * A Wrapper class to facilitate  Diagram generation and manipulation.
 *
 * @author Per Spilling (per.spilling@objectware.no)
 */
public class EaDiagram {
// ------------------------------ FIELDS ------------------------------

    public static final ImageFileFormat defaultImageFormat = ImageFileFormat.PNG;
    private static final transient Logger LOG = LoggerFactory.getLogger(EaDiagram.class);
    private static final transient Logger DIAGRAM_LOG = LoggerFactory.getLogger("diagramLogger");
    private final Diagram eaDiagram;
    private final EaRepo eaRepo;
    private final String logicalPathname;
    private final int diagramID;
    private final DiagramNameMode diagramNameMode;

// --------------------------- CONSTRUCTORS ---------------------------

    public EaDiagram(final EaRepo repository, final Diagram diagram, final String pathName) {
        eaDiagram = diagram;
        eaRepo = repository;
        logicalPathname = pathName;
        diagramID = eaDiagram != null ? eaDiagram.GetDiagramID() : 0;
        diagramNameMode = (DiagramNameMode) EA_DIAGRAM_NAME_MODE.valueAsEnum();
        final String msg = "Found diagram :" + pathName + ":" + (diagram != null ? diagram.GetName() + " " + diagram.GetDiagramGUID() : " No " +
                "diagram ");
        System.out.println(msg);
        LOG.info(msg);
    }

// -------------------------- STATIC METHODS --------------------------

    public static EaDiagram findEaDiagram(final EaRepo eaRepo, final String diagramName) {
        final EaDiagram diagram;
        if (isNumeric(diagramName)) {
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
            if (!styleString.contains("TREE")) {
                styleString += "TREE=OS;";
                diagramLink.SetStyle(styleString);
                LOG.debug("New style: {}", styleString);
                diagramLink.Update();
            }
        }
        eaDiagram.Update();
    }

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
        final String diagramGUID = eaDiagram.GetDiagramGUID();

        final int level = NumberUtils.toInt(EA_DIAGRAM_NAME_LEVEL.value("0"));
        final String urlPart =  createUrlPart(level, logicalPathname, eaDiagram.GetName(), eaDiagram.GetDiagramGUID(), eaDiagram
                .GetVersion(), diagramNameMode, imageFileFormat.getFileExtension());

        final File file = createFile(level, EA_DOC_ROOT_DIR.value(), logicalPathname, eaDiagram.GetName(), eaDiagram.GetDiagramGUID(), eaDiagram
                .GetVersion(), diagramNameMode, imageFileFormat.getFileExtension());
        file.getParentFile()
            .mkdirs();

//        final String diagramUrlPart = createUrlPartForFile(file, EA_DOC_ROOT_DIR.value());
        DIAGRAM_LOG.info("{}, {}, {}, {}, {}, {}", eaDiagram.GetName(), logicalPathname, diagramGUID, eaDiagram.GetVersion(), file.getAbsolutePath
                (), EA_URL_BASE.value() + urlPart);

        updateDiagramUrlFile(urlPart);
        if (urlForFileOnly) {
            return true;
        }

        LOG.info("diagramguid {}, diagramfilename {}, imagefileformat {}", diagramGUID, file.getAbsolutePath(), imageFileFormat);
        if (eaRepo.getProject()
                  .PutDiagramImageToFile(diagramGUID, file.getAbsolutePath(), imageFileFormat.isRaster())) {
            LOG.info("Diagram generated at: " + file.getAbsolutePath());
            if (!file.canRead()) {
                LOG.error("Unable to read file [{}] Make sure the drive [{}] is properly mounted ", file.getAbsolutePath(), EA_DOC_ROOT_DIR.value());
                return false;
            }
            new ImageMetadata().writeCustomMetaData(file, "EA_GUID", diagramGUID);
            LOG.info("Adding metadata [{}] to [{}]",diagramGUID, file.getAbsolutePath());
            return true;
        } else {
            LOG.error("Unable to create diagram:" + file.getAbsolutePath());
            return false;
        }
    }

//    public String createAbsoluteFileName(final String logicalPathname) {
//        final File file = createFile(1, EA_DOC_ROOT_DIR.value(), logicalPathname, eaDiagram.GetName(), eaDiagram.GetDiagramGUID(), eaDiagram
//                .GetVersion(), diagramNameMode, defaultImageFormat.getFileExtension());
//        return file.getAbsolutePath();
//    }

    /**
     * Write the urlBase to a specified file at current directory.
     *
     * @param urlBase
     */
    public static void updateDiagramUrlFile(final String urlBase) {
        File urlFile = null;
        try {
            urlFile = new File(EA_DIAGRAM_URL_FILE.value());
            FileUtils.write(urlFile, urlBase);
        } catch (final IOException e) {
            LOG.error("Unable to write url to file " + EA_DIAGRAM_URL_FILE.value(), e);
        }
    }

    public int getDiagramID() {
        return diagramID;
    }

    public String getName() {
        return eaDiagram.GetName();
    }
}

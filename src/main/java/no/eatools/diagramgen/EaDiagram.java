package no.eatools.diagramgen;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import no.bouvet.ohs.futil.ImageFileFormat;
import no.bouvet.ohs.futil.ImageMetadata;
import no.eatools.util.DiagramNameMode;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Collection;
import org.sparx.Diagram;
import org.sparx.DiagramLink;
import org.sparx.DiagramObject;
import org.sparx.Project;

import static no.eatools.diagramgen.EaType.*;
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
    // Element types that shall not appear on auto-diagrams
    public static final EnumSet<EaType> bannedElementTypes = EnumSet.of(Note, Sequence, Text);

    private final Diagram eaDiagram;
    private final EaRepo eaRepo;
    private final String logicalPathname;
    private final int diagramID;
    private final DiagramNameMode diagramNameMode;
    private final Map<Integer, EaElement> diagramElements = new HashMap<>();
    private Status status = Status.UNKNOWN;

// --------------------------- CONSTRUCTORS ---------------------------

    public EaDiagram(final EaRepo repository, final Diagram diagram, final String pathName) {
        eaDiagram = diagram;
        eaRepo = repository;
        logicalPathname = pathName;
        diagramID = eaDiagram != null ? eaDiagram.GetDiagramID() : 0;
        diagramNameMode = (DiagramNameMode) EA_DIAGRAM_NAME_MODE.valueAsEnum();
    }

// -------------------------- STATIC METHODS --------------------------

    public static EaDiagram findEaDiagram(final EaRepo eaRepo, final String diagramName) {
        final EaDiagram diagram;
        if (isNumeric(diagramName)) {
            final int diagramId = Integer.parseInt(diagramName);
            diagram = eaRepo.findDiagramById(diagramId);
        } else {
            diagram = eaRepo.findDiagramByName(diagramName);
        }
        if(diagram != null) {
            final String msg = "Found diagram :" + diagram.getPathname() + ":" + diagram.getName() + " " + diagram.getGuid();
            System.out.println(msg);
            LOG.info(msg);
        } else {
            LOG.warn("No diagram with name [{}] found", diagramName);
        }
        return diagram;
    }

    public String getGuid() {
        return eaDiagram.GetDiagramGUID();
    }

    public Status getStatus() {
        return status;
    }

    public enum Status {
        NEW,
        UPDATED,
        DELETED,
        UNKNOWN
    }

// -------------------------- OTHER METHODS --------------------------

    public String getPathname() {
        return logicalPathname;
    }

    /**
     * Experimental
     * todo fix it
     *
     * @param style
     */
    public void setAllConnectorsToStyle(final EaConnectorStyle style) {
//        for (DiagramObject diagramObject : eaDiagram.GetDiagramObjects()) {
//            diagramObject.GetBottom();
//
//            Element element = eaRepo.findElementByID(diagramObject.GetElementID());
//            for (Connector connector : element.GetConnectors()) {
//                System.out.println("ConnectorStyle" +  connector.GetName() + ":" + connector.GetRouteStyle());
//            }
//        }
        try {
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
        } catch (final Exception e) {
            LOG.error("Unable to set connector styles on [{}] bacause [{}]", this, e);
        }
    }

    public String writeImageToFile(final boolean urlForFileOnly) {
        try {
            return writeImageToFile(defaultImageFormat, urlForFileOnly);
        } catch (final Exception e) {
            LOG.error("Unable to write to file", e);
            return EMPTY;
        }
    }

    public String writeImageToFile(final ImageFileFormat imageFileFormat, final boolean urlForFileOnly) {
        if(eaDiagram == null) {
            LOG.info("No diagram to generate");
            return "";
        }
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
        final String completeUrl = EA_URL_BASE.value() + urlPart;
        DIAGRAM_LOG.info("{}, {}, {}, {}, {}, {}", eaDiagram.GetName(), logicalPathname, diagramGUID, eaDiagram.GetVersion(), file.getAbsolutePath
                (), completeUrl);

        updateDiagramUrlFile(urlPart);
        if (urlForFileOnly) {
            return completeUrl;
        }

        LOG.info("diagramguid [{}], diagramfilename [{}], imagefileformat [{}]", diagramGUID, file.getAbsolutePath(), imageFileFormat);
        if (eaRepo.getProject()
                  .PutDiagramImageToFile(diagramGUID, file.getAbsolutePath(), imageFileFormat.isRaster())) {
            LOG.info("Diagram generated at: " + file.getAbsolutePath());
            if (!file.canRead()) {
                LOG.error("Unable to read file [{}] Make sure the drive [{}] is properly mounted ", file.getAbsolutePath(), EA_DOC_ROOT_DIR.value());
                return EMPTY;
            }
            new ImageMetadata().writeCustomMetaData(file, "EA_GUID", diagramGUID);
            LOG.info("Adding metadata [{}] to [{}]",diagramGUID, file.getAbsolutePath());
            return completeUrl;
        } else {
            LOG.error("Unable to create diagram: [{}]", file.getAbsolutePath());
            return EMPTY;
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

    public static String createStandardDiagramName(final EaElement centralElement) {
        return centralElement.getName() + "_AUTO";
    }

    public void removeAllElements() {
        final Collection<DiagramObject> diagramObjects = eaDiagram.GetDiagramObjects();
        for (short i = 0; i < diagramObjects.GetCount(); i++) {
            diagramObjects.Delete(i);
//            diagramObjects.Refresh();
        }
        eaDiagram.Update();
        diagramObjects.Refresh();
    }

    public DiagramObject add(final EaElement element) {
        final Collection<DiagramObject> diagramObjects = eaDiagram.GetDiagramObjects();
        final DiagramObject diagramObject = diagramObjects
                .AddNew(element.getName(), element.getMetaType().toString());
        diagramObject.SetElementID(element.getId());
        diagramObject.Update();
        eaDiagram.Update();
        diagramObjects.Refresh();
        LOG.info("Added ({}, {}, {}) [{}] to diagram [{}]", element.getMetaType(), element.getType(), element.getEaMetaType(),  element.getName(), getName());

        diagramElements.put(diagramObject.GetInstanceID(), element);
        return diagramObject;
    }

    public void setParentId(final int id) {
        eaDiagram.SetParentID(id);
        eaDiagram.Update();
    }

    public void hideDetails() {
        eaDiagram.SetShowDetails(0);
        eaDiagram.Update();
    }

    public boolean adjustElementAppearances() {
        try {
            final Collection<DiagramObject> diagramObjects = eaDiagram.GetDiagramObjects();
            diagramObjects.Refresh();
            for (final DiagramObject diagramObject : diagramObjects) {
                final EaElement eaElement = diagramElements.get(diagramObject.GetInstanceID());
                if (eaElement.getMetaType() == EaMetaType.INTERFACE) {
                    final String oldStyle = diagramObject.GetStyle();
                    final String newStyle = "Lollipop=1;" + oldStyle.replaceAll("Lollipop=.;", "");
                    diagramObject.SetStyle(newStyle);
                    final int left = diagramObject.GetLeft();
                    final int top = diagramObject.GetTop();
                    LOG.info("Style [{}] Left {} Top {}", oldStyle, left, top);
                    diagramObject.SetRight(left + EA_AUTO_DIAGRAM_INTERFACE_SIZE.toInt());
                    diagramObject.SetBottom(top - EA_AUTO_DIAGRAM_INTERFACE_SIZE.toInt());
                    diagramObject.Update();
                }
                diagramObjects.Refresh();
            }
            eaDiagram.Update();
        } catch (final Exception e) {
            LOG.error("Unable to adjust appearances for [{}] because [{}]", this, e);
            return false;
        }
        return true;
    }

    public void update() {
        eaDiagram.Update();
    }

    public void setStatus(final Status status) {
        this.status = status == null? Status.UNKNOWN : status;
    }

    public boolean layoutAndSaveDiagram(final EaElement centralElement) {
        hideDetails();

        add(centralElement);

        centralElement.findConnectedElements()
                      .stream()
                      .filter(e -> !bannedElementTypes.contains(e.getType()))
                      .forEach(this::add);

        final Project project = eaRepo.getProject();
        // todo, find out which one actually does the job:
        if(doLayout(project.GUIDtoXML(getGuid()),
                 EA_AUTO_DIAGRAM_OPTIONS.toInt(),
                 EA_AUTO_DIAGRAM_ITERATIONS.toInt(),
                 EA_AUTO_DIAGRAM_LAYER_SPACING.toInt(),
                 EA_AUTO_DIAGRAM_COLUMN_SPACING.toInt())) {
            if (adjustElementAppearances()) {
                return eaRepo.saveDiagram(this);
            }
        }
        return false;
    }

    private boolean doLayout(final String guid, final int options, final int iterations, final int layerSpacing, final
    int columnSpacing) {
        try {
            final boolean result = eaRepo.getProject()
                                         .LayoutDiagramEx(guid,
                                                          options,
                                                          iterations,
                                                          layerSpacing,
                                                          columnSpacing,
                                                          false);

            update();
            LOG.info("Applied autolayout to [{}] [{}] result: [{}]. Ran with options [{}], iterations {} layerSpacing {} columnSpacing {}",
                     getName(), guid, result, String.format("%#010x", options), iterations, layerSpacing, columnSpacing);
        } catch (final Exception e) {
            LOG.error("Unable to lay out [{}] because [{}]", this, e);
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "EaDiagram{" +
                "name=" + getName() +
                ", logicalPathname='" + logicalPathname + '\'' +
                ", diagramID=" + diagramID +
                ", diagramNameMode=" + diagramNameMode +
                ", status=" + status +
                '}';
    }
}

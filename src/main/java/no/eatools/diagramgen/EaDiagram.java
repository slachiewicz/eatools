package no.eatools.diagramgen;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.eatools.util.IntCounter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Collection;
import org.sparx.Diagram;
import org.sparx.DiagramLink;
import org.sparx.Element;
import org.sparx.Package;

import static no.bouvet.ohs.jops.SystemPropertySet.*;
import static no.eatools.util.EaApplicationProperties.*;
import static no.eatools.util.NameNormalizer.*;

/**
 * A Wrapper class to facilitate  Diagram generation and manipulation.
 *
 * @author Per Spilling (per.spilling@objectware.no)
 */
public class EaDiagram {
    private static final transient Logger log = LoggerFactory.getLogger(EaDiagram.class);
    private final Diagram eaDiagram;
    public static final ImageFileFormat defaultImageFormat = ImageFileFormat.PNG;
    private final EaRepo eaRepo;
    private final String logicalPathname;

    /**
     * Generate all diagrams from the model into the directory path.
     * The package structure of the model is retained as directory structure.
     * All existing diagrams are overwritten.
     */
    public static int generateAll(final EaRepo eaRepo) {
        final IntCounter count = new IntCounter();
        generateAllDiagrams(eaRepo, eaRepo.getRootPackage(), count);
        return count.count;
    }

    /**
     * Recursive method that finds all diagrams in a package and writes them to file.
     *
     * @param repo
     * @param pkg
     * @param diagramCount
     */
    private static void generateAllDiagrams(final EaRepo repo, final Package pkg, final IntCounter diagramCount) {
        for (final Package p : pkg.GetPackages()) {
            generateAllDiagrams(repo, p, diagramCount);
        }
        if (!repo.packageMatch(pkg)) {
            log.info("--- Skipping package " + pkg.GetName());
            return;
        }

        final List<EaDiagram> diagrams = findDiagramsInPackage(repo, pkg);
        if (diagrams.size() > 0) {
            log.debug("Generating diagrams in package: " + pkg.GetName());
            diagramCount.count = diagramCount.count + diagrams.size();
            for (final EaDiagram d : diagrams) {
                log.debug("Generating diagrams: " + d.getFilename());
                d.writeImageToFile(false);
            }
        }
    }

    public static EaDiagram findEaDiagram(final EaRepo eaRepo, final String diagramName) {
        final EaDiagram diagram;
        if (StringUtils.isNumeric(diagramName)) {
            final int diagramId = Integer.parseInt(diagramName);
            diagram = findDiagramById(eaRepo, diagramId);
        } else {
            diagram = findDiagram(eaRepo, diagramName);
        }
        return diagram;
    }

    public static EaDiagram findDiagram(final EaRepo eaRepo, final String diagramName) {
        return findDiagram(eaRepo, eaRepo.getRootPackage(), diagramName, true);
    }

    private static EaDiagram findDiagram(final EaRepo eaRepo, final Package pkg, final String diagramName, final boolean recursive) {
        if (pkg == null) {
            return null;
        }
//        for (Element element : pkg.GetElements()) {
//            element.GetAttributes()
//        }pkg.GetElements()
        for (final Diagram diagram : pkg.GetDiagrams()) {
            if (diagram.GetName().equals(diagramName) || diagramName.equals(Integer.valueOf(diagram.GetDiagramID()).toString())) {
                log.info("Diagram name = " + diagram.GetName() + " ID: " + diagram.GetDiagramID());
                return new EaDiagram(eaRepo, diagram, getPackagePath(eaRepo, pkg));
            }
        }
        if (recursive) {
            for (final Package p : pkg.GetPackages()) {
                final EaDiagram d = findDiagram(eaRepo, p, diagramName, recursive);
                if (d != null) {
                    return d;
                }
            }
        }
        return null;
    }

    private static String getPackagePath(final EaRepo eaRepo, final Package pkg) {
        final ArrayList<Package> ancestorPackages = new ArrayList<Package>();
        getAncestorPackages(ancestorPackages, eaRepo, pkg);
        final StringBuffer pathName = new StringBuffer();
        Collections.reverse(ancestorPackages);
        for (final Package p : ancestorPackages) {
            pathName.append(FILE_SEPARATOR.value() + p.GetName());
        }
        return pathName.toString();
    }

    private static void getAncestorPackages(final ArrayList<Package> ancestorPackages, final EaRepo eaRepo, final Package pkg) {
        ancestorPackages.add(pkg);
        if (pkg.GetParentID() != 0) {
            getAncestorPackages(ancestorPackages, eaRepo, eaRepo.findPackageByID(pkg.GetParentID()));
        }
    }

    /**
     * Find all UML diagrams inside a specific Package. Non-recursive, searches the top-level (given)
     * package only.
     *
     * @param pkg the Package to search in.
     * @return
     */
    public static List<EaDiagram> findDiagramsInPackage(final EaRepo eaRepo, final Package pkg) {
        if (pkg == null) {
            return Collections.emptyList();
        }
        final List<EaDiagram> result = new ArrayList<EaDiagram>();
        final Collection<Diagram> diagrams;
        try {
            diagrams = pkg.GetDiagrams();
        } catch (final Exception e) {
            log.error("Fuckup in diagram package", e);
            return Collections.emptyList();
        }
        if(diagrams == null) {
            log.error("Fuckup in diagram package " + pkg.GetName());
            return Collections.emptyList();
        }
        for (final Diagram d : diagrams) {
            result.add(new EaDiagram(eaRepo, d, getPackagePath(eaRepo, pkg)));
        }
        for (final Element element : pkg.GetElements()) {
            findDiagramsInElements(eaRepo, pkg, element, result);
        }
        return result;
    }

    /**
     * Some diagrams may reside below elements
     * @param eaRepo
     * @param pkg
     * @param element
     * @param diagramList
     */
    public static void findDiagramsInElements(final EaRepo eaRepo, final Package pkg, final Element element, final List<EaDiagram> diagramList) {
        if(element == null || element.GetElements() == null) {
            return;
        }
        for (final Element child : element.GetElements()) {
            findDiagramsInElements(eaRepo, pkg, child, diagramList);
        }
        for (final Diagram diagram : element.GetDiagrams()) {
            diagramList.add(new EaDiagram(eaRepo, diagram, getPackagePath(eaRepo, pkg)));
        }
    }

    public EaDiagram(final EaRepo repository, final Diagram diagram, final String pathName) {
        eaDiagram = diagram;
        eaRepo = repository;
        logicalPathname = pathName;
        System.out.println(diagram.GetDiagramGUID());
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
            System.out.println("Old style: " + styleString);
            if(! styleString.contains("TREE")) {
                styleString += "TREE=OS;";
                diagramLink.SetStyle(styleString);
                System.out.println("New style: " + styleString);
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
            log.error("Unable to write to file", e);
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
            log.error("Unable to create filename from " + logicalPathname);
            return false;
        }
        final File file = new File(diagramFileName);
        log.debug(eaDiagram.GetDiagramGUID() + ": " + eaDiagram.GetDiagramID() + ": " + file.getAbsolutePath());

        final String urlBase = createUrlForFile(file);
        updateDiagramUrlFile(urlBase);
        if (urlForFileOnly) {
            return true;
        }

        if (eaRepo.getProject().PutDiagramImageToFile(eaDiagram.GetDiagramGUID(), diagramFileName, imageFileFormat.isRaster())) {
            log.info("Diagram generated at: " + diagramFileName);
            if (!file.canRead()) {
                log.error("Unable to read file " + file.getAbsolutePath());
                return false;
            }
            log.info("Adding metadata");

            return true;
        } else {
            log.error("Unable to create diagram:" + diagramFileName);
            return false;
        }
    }

    public static void updateDiagramUrlFile(final String urlBase) {
        File urlFile = null;
        try {
            urlFile = new File(EA_DIAGRAM_URL_FILE.value());
            FileUtils.write(urlFile, urlBase);
        } catch (final IOException e) {
            log.error("Unable to write url to file " + urlFile.getAbsolutePath());
        }
    }

    private String createUrlForFile(final File file) {
        String urlBase = "";
        try {
            final URL diagramUrl = file.toURI().toURL();
            log.info("Diagram url " + diagramUrl);
            urlBase = diagramUrl.toString().replace(diagramUrl.getProtocol(), "").replace(EA_DOC_ROOT_DIR.value(), "")
                    .replace(":", "");
            log.info("-> URLBase: " + urlBase);
        } catch (final MalformedURLException e) {
            log.error("Unable to create url from file " + urlBase);
        }
        return urlBase;
    }

    public String getPathname() {
        return logicalPathname;
    }

    public String getAbsolutePathName(String logicalPathname) {
        String dirRootString = EA_DOC_ROOT_DIR.value();
        final File dirRoot = new File(dirRootString);
        if (!dirRoot.isAbsolute()) {
            final File cwd = new File(USER_DIR.value());
            log.info(dirRootString + " is not a root directory. Using " + cwd);
            dirRootString = cwd + FILE_SEPARATOR.value() + dirRootString;
        }
        return dirRootString + makeWebFriendlyFilename(logicalPathname);
    }

    public String getFilename() {
        final String version = EA_ADD_VERSION.exists() ? eaDiagram.GetVersion() : StringUtils.EMPTY;
        return makeWebFriendlyFilename(eaDiagram.GetName() + version + defaultImageFormat.getFileExtension());
    }

    public String getAbsoluteFilename() {
        return getAbsolutePathName(logicalPathname) + FILE_SEPARATOR.value() + getFilename();
    }

    public static EaDiagram findDiagramById(final EaRepo eaRepo, final int diagramId) {
        final Diagram diagram = eaRepo.findDiagramById(diagramId);
        if(diagram == null) {
            return null;
        }
        final String packagePath = getPackagePath(eaRepo, eaRepo.findPackageByID(diagram.GetPackageID()));
        return new EaDiagram(eaRepo, diagram, packagePath);
    }
}

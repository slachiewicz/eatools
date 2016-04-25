package no.eatools.diagramgen;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.eatools.util.IntCounter;
import no.eatools.util.NameNormalizer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Collection;
import org.sparx.Connector;
import org.sparx.Datatype;
import org.sparx.Diagram;
import org.sparx.DiagramObject;
import org.sparx.Element;
import org.sparx.Package;
import org.sparx.Project;
import org.sparx.Repository;

import static no.eatools.util.EaApplicationProperties.*;

/**
 * Utilities for use with the EA (Enterprise Architect DLL).
 * todo reconsider transactional model, i.e. how/when to open/close the Repos.
 * <p/>
 * Note that the terminology in these methods refer to the corresponding UML elements.
 * E.g. getClassesInPackage means "find and return all elements of UML-type Class in the UML Package".
 * <p/>
 * The class assume that no two elements of same UML type (e.g. Class, Component) and same name may exist
 * in the same namespace (Package).
 * <p/>
 * Date: 21.okt.2008
 *
 * @author AB22273 et al.
 */
public class EaRepo {
// ------------------------------ FIELDS ------------------------------

    /* Boolean flags that can be used as input params */
    public static final boolean RECURSIVE = true;
    public static final boolean NON_RECURSIVE = false;
    private static final transient Logger LOG = LoggerFactory.getLogger(EaRepo.class);


    /* Name of the UML stereotype for an XSDschema package */
    private static final String xsdSchemaStereotype = "XSDschema";

    /* The character encoding to use for XSD generation */
    private static final String xmlEncoding = "UTF-8";
    private File reposFile;
    private Repository repository = null;
    private boolean isOpen = false;
    private String reposString;
    private final Pattern packagePattern;
    private final Package rootPackage;
    private final Map<Integer, EaPackage> packageCache = new HashMap<>();

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * @param repositoryFile local file or database connection string
     */
    public EaRepo(final File repositoryFile) {
        reposFile = repositoryFile;
        final String packagePatternRegexp = EA_PACKAGE_FILTER.value();
        if (StringUtils.isNotBlank(packagePatternRegexp)) {
            packagePattern = Pattern.compile(packagePatternRegexp);
            LOG.info("Looking for packages matching [" + packagePatternRegexp + "]" + packagePattern.pattern());
        } else {
            packagePattern = null;
        }
        rootPackage = findRootPackage();
    }

    /**
     * Find the top level (aka root) package in a given repository.
     * todo check for NPEs.
     *
     * @return the root package or possibly null if there are no root package in the repository.
     * This is normally the "Views" package or the "Model" package, but it may have an arbitrary name.
     */
    private Package findRootPackage() {
        ensureRepoIsOpen();
        final String rootPkgName = EA_ROOTPKG.value();
        System.out.println("root package name = " + rootPkgName);
        for (final Package aPackage : repository.GetModels()) {
            if (aPackage.GetName()
                        .equalsIgnoreCase(rootPkgName)) {
                LOG.debug("Found top level package: " + aPackage.GetName());
                return aPackage;
            }
        }
        throw new RuntimeException("Root pkg '" + rootPkgName + "' not found");
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public Package getRootPackage() {
        return rootPackage;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public String toString() {
        return reposFile.getAbsolutePath() + " " + this.repository.toString();
    }

// -------------------------- OTHER METHODS --------------------------

    private EaDiagram findDiagram(final Package pkg, final String diagramName, final boolean recursive) {
        if (pkg == null) {
            return null;
        }
        LOG.info("Looking for diagram [{}] in package [{}]", diagramName, pkg.GetName());
//        for (Element element : pkg.GetElements()) {
//            element.GetAttributes()
//        }pkg.GetElements()
        for (final Diagram diagram : pkg.GetDiagrams()) {
            if (diagram.GetName()
                       .equals(diagramName) || diagramName.equals(Integer.toString(diagram.GetDiagramID()))) {
                LOG.info("Diagram name = " + diagram.GetName() + " ID: " + diagram.GetDiagramID());
                return new EaDiagram(this, diagram, getPackagePath(pkg));
            }
        }
        if (recursive) {
            for (final Package p : pkg.GetPackages()) {
                final EaDiagram d = findDiagram(p, diagramName, recursive);
                if (d != null) {
                    return d;
                }
            }
        }
        return null;
    }

    private void populateCache(final Package pkg) {
        packageCache.put(pkg.GetPackageID(), new EaPackage(pkg, this));
        for (final Package aPackage : pkg.GetPackages()) {
            populateCache(aPackage);
        }
    }

    /**
     * Closes the Enterprise Architect model repository.
     */
    public void close() {
        if (isOpen && repository != null) {
            LOG.info("Closing repository: " + reposString);
            repository.SetFlagUpdate(true);
            repository.CloseFile();
            repository.Exit();
            repository = null;
        }
        isOpen = false;
    }

    public boolean deleteObjectInPackage(final Package pkg, final String objectName, final Element classifier) {
        if (pkg == null) {
            return false;
        }
        short index = 0;
        short indexToDelete = -1;
        for (final Element element : pkg.GetElements()) {
            if (element.GetName()
                       .equals(objectName)) {
                if ((classifier == null) || (classifier.GetElementID() == element.GetClassifierID())) {
                    indexToDelete = index;
                }
            }
            ++index;
        }
        if (indexToDelete != -1) {
            pkg.GetElements()
               .Delete(indexToDelete);
            pkg.Update();
            pkg.GetElements()
               .Refresh();
            return true;
        }
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    public List<String> findAllMetaTypesInModel() {
        final Set<String> result = new HashSet<>();
        findMetaTypesInPackage(getRootPackage(), result);
        return new ArrayList<>(result);
    }

    private void findMetaTypesInPackage(final Package pkg, final Set<String> result) {
        for (final Element element : pkg.GetElements()) {
            result.add(element.GetMetaType());
        }
        for (final Package aPackage : pkg.GetPackages()) {
            findMetaTypesInPackage(aPackage, result);
        }
    }

    /**
     * Find UML Object elements inside a specific UML Package.
     * Non-recursive, searches the top-level (given) package only.
     *
     * @param pack the Package to search in.
     * @return
     */
    public List<Element> findComponentInstancesInPackage(final Package pack) {
        final List<Element> theComponents = findElementsOfTypeInPackage(pack, EaMetaType.COMPONENT);
        final List<Element> componentInstances = new ArrayList<>();
        for (final Element component : theComponents) {
            if (EaMetaType.COMPONENT.toString()
                                    .equals(component.GetClassifierType())) {
                componentInstances.add(component);
            }
        }
        return componentInstances;
    }

    public EaDiagram findDiagram(final String diagramName) {
        return findDiagram(getRootPackage(), diagramName, true);
    }

    public EaDiagram findDiagramById(final int diagramId) {
        try {
            final Diagram diagram = repository.GetDiagramByID(diagramId);
            if (diagram == null) {
                return null;
            }
            final String packagePath = getPackagePath(findPackageByID(diagram.GetPackageID()));
            return new EaDiagram(this, diagram, packagePath);
        } catch (final Exception e) {
            LOG.error("Could not find diagram with id {} in repos {}", diagramId, this);
            return null;
        }
    }

    public Element findElementByID(final int elementId) {
        if (elementId == 0) {
            return null;
        }
        ensureRepoIsOpen();
        return repository.GetElementByID((elementId));
    }

    /**
     * Find an element of a specific EaMetaType with a given name.
     *
     * @param pack
     * @param type
     * @param name
     * @return null if no match is found.
     */
    public Element findElementOfType(final Package pack, final EaMetaType type, final String name) {
        ensureRepoIsOpen();
        final String trimmedName = name.trim();

        final List<Element> existingElements = findElementsOfTypeInPackage(pack, type);

        for (final Element element : existingElements) {
            if (element.GetName()
                       .equals(trimmedName)) {
                return element;
            }
        }
        return null;
    }

    /**
     * Find UML Node elements inside a specific UML Package.
     * Non-recursive, searches the top-level (given) package only.
     *
     * @param pack
     * @return
     */
    public List<Element> findNodesInPackage(final Package pack) {
        return findElementsOfTypeInPackage(pack, EaMetaType.NODE);
    }

    public Connector findOrCreateAssociation(final Element from, final Element to, final String name) {
        // todo check for existence
//        from.GetConnectors().AddNew(name, )
//        for (Connector c : from.GetConnectors()) {
//            if (c.GetSupplierEnd())
//        }
        return null;
    }

    /**
     * @param definedPackage
     * @param className
     * @return
     */
    public Element findOrCreateClassInPackage(final Package definedPackage, final String className) {
        ensureRepoIsOpen();

        final Element theClass;
        final Optional<Element> candidate = findNamedElementOnList(findClassesInPackage(definedPackage), className);

        if (candidate.isPresent()) {
            return candidate.get();
        }

        theClass = definedPackage.GetElements()
                                 .AddNew(className, EaMetaType.CLASS.toString());
        theClass.Update();
        definedPackage.Update();

        return theClass;
    }

    private Optional<Element> findNamedElementOnList(final List<Element> elementList, final String elementName) {
        ensureRepoIsOpen();
        return elementList.stream()
                          .filter(e -> e.GetName()
                                        .equals(elementName))
                          .findFirst();
    }

    /**
     * Find UML Class elements inside a specific UML Package.
     * Non-recursive, searches the top-level (given) package only.
     *
     * @param pack the Package to serach in.
     * @return
     */
    public List<Element> findClassesInPackage(final Package pack) {
        return findElementsOfTypeInPackage(pack, EaMetaType.CLASS);
    }

    public Element findOrCreateComponentInstanceInPackage(final Package pack, final String name, final Element classifier) {
        final Element component = findOrCreateComponentInPackage(pack, name);
        if (classifier != null) {
            component.SetClassifierID(classifier.GetElementID());
            component.Update();
            component.Refresh();
        }
        return component;
    }

    /**
     * @param definedPackage
     * @param componentName
     * @return
     */
    public Element findOrCreateComponentInPackage(final Package definedPackage, final String componentName) {
        ensureRepoIsOpen();

        final Optional<Element> candidate = findNamedElementOnList(findComponentsInPackage(definedPackage), componentName);

        if (candidate.isPresent()) {
            return candidate.get();
        }

        return addElementInPackage(definedPackage, componentName, EaMetaType.COMPONENT, null);
    }

    /**
     * Find UML Component elements inside a specified UML Package.
     * Non-recursive, searches the top-level (given) package only.
     *
     * @param pack
     * @return
     */
    public List<Element> findComponentsInPackage(final Package pack) {
        return findElementsOfTypeInPackage(pack, EaMetaType.COMPONENT);
    }

    /**
     * Assemble a List of all Model elements of a certain EaMetaType in the given package.
     * Sub-packages are not examined (non-recursive).
     *
     * @param pkg  the Package to look in.
     * @param type the type of Element to look for.
     * @return a List of found Elements, possibly empty, but never null.
     */
    public List<Element> findElementsOfTypeInPackage(final Package pkg, final EaMetaType type) {
        ensureRepoIsOpen();

        if (pkg == null) {
            return Collections.emptyList();
        }

        final EaMetaType safeType = (type == null) ? EaMetaType.NULL : type;

        final List<Element> result = new ArrayList<>();

        for (final Element e : pkg.GetElements()) {
            if (safeType.toString()
                        .equals(e.GetMetaType())) {
                result.add(e);
            }
        }

        return result;
    }

    /**
     * todo move to EaDiagram class...
     *
     * @param pkg  the Package to create the Diagram in.
     * @param name name of the Diagram, if null, the Diagram will have the same name as the Package.
     * @param type the type of UML Diagram to look for or create.
     * @return the Diagram created or found.
     */
    public Diagram findOrCreateDiagramInPackage(final Package pkg, String name, final EaDiagramType type) {
        if (name == null) {
            name = pkg.GetName();
        }
        for (final Diagram d : pkg.GetDiagrams()) {
            if (d.GetName()
                 .equals(name) && (type.toString()
                                       .equals(d.GetType()))) {
                return d;
            }
        }
        final Diagram newDiagram = pkg.GetDiagrams()
                                      .AddNew(name, type.toString());
        pkg.GetDiagrams()
           .Refresh();
        newDiagram.Update();
        pkg.Update();

        return newDiagram;
    }

    public DiagramObject findOrCreateDiagramObject(final Package pkg, final Diagram diagram, final Element reposElement) {
        for (final DiagramObject dObject : diagram.GetDiagramObjects()) {
            if (dObject.GetElementID() == reposElement.GetElementID()) {
                return dObject;
            }
        }
        final DiagramObject diagramObject = diagram.GetDiagramObjects()
                                                   .AddNew("", "");
        diagramObject.SetInstanceID(reposElement.GetElementID());
        diagramObject.SetElementID(reposElement.GetElementID());
        diagramObject.Update();
        diagram.Update();
        pkg.GetDiagrams()
           .Refresh();
        pkg.Update();
        return diagramObject;
    }

    /**
     * @param from the source/originator of the link, aka the "Supplier" in EA terms.
     * @param to   the target/destination of the link, aka the "Client" in EA terms.
     * @param name name of the link. Used to look up already existing links.
     * @return
     */
    public Connector findOrCreateLink(final Element from, final Element to, final String name) {
        // check for existence
        for (final Connector c : to.GetConnectors()) {
            if (c.GetName()
                 .equals(name)) {
                if ((c.GetSupplierID() == to.GetElementID()) && (c.GetClientID() == from.GetElementID())) {
                    return c;
                }
            }
        }

        final Connector c = to.GetConnectors()
                              .AddNew(name, EaMetaType.ASSOCIATION.toString());
        c.SetSupplierID(to.GetElementID());
        if (!c.Update()) {
            LOG.error("Unable to update connector to: " + to.GetName());
            return null;
        }
        to.GetConnectors()
          .Refresh();

        c.SetClientID(from.GetElementID());
        if (!c.Update()) {
            LOG.error("Unable to update connector from: " + from.GetName());
            return null;
        }
        from.GetConnectors()
            .Refresh();

        c.SetDirection(EaLinkDirection.SOURCE_DESTINATION.toString());
        c.Update();

        from.Update();
        to.Update();
        return c;
    }

    /**
     * @param pack
     * @param objectName
     * @param classifier
     * @return
     */
    public Element findOrCreateObjectInPackage(final Package pack, final String objectName, final Element classifier) {
        ensureRepoIsOpen();

        // We allow for same name on different elements of different type, therefore
        // must also check type
        for (final Element element : findObjectsInPackage(pack)) {
            if (element.GetName()
                       .equals(objectName)) {
                if (isOfType(element, classifier)) {
                    return element;
                }
            }
        }
        return addElementInPackage(pack, objectName, EaMetaType.OBJECT, classifier);
    }

    /**
     * Find UML Object elements inside a specific UML Package.
     * Non-recursive, searches the top-level (given) package only.
     *
     * @param pack the Package to serach in.
     * @return
     */
    public List<Element> findObjectsInPackage(final Package pack) {
        return findElementsOfTypeInPackage(pack, EaMetaType.OBJECT);
    }

    public boolean isOfType(final Element theObject, final Element classifier) {
        final int classifierId = theObject.GetClassifierID();
        if (classifier == null) {
            return (classifierId == 0);
        }

        return (classifier.GetElementID() == classifierId);
    }

    private Element addElementInPackage(final Package pack, final String name, final EaMetaType umlType, final Element classifier) {
        ensureRepoIsOpen();

        final Element element = pack.GetElements()
                                    .AddNew(name, umlType.toString());
        pack.GetElements()
            .Refresh();

        if (classifier != null) {
            element.SetClassifierID(classifier.GetElementID());
            element.SetClassifierName(classifier.GetName());
        }

        element.Update();
        pack.Update();
        pack.GetElements()
            .Refresh();
        return element;
    }

    public Package findOrCreatePackage(final Package parent, final String name) {
        return findOrCreatePackage(parent, name, NON_RECURSIVE);
    }

    public Package findOrCreatePackage(final Package parent, final String name, final boolean recursive) {
        ensureRepoIsOpen();
        Package pkg = findPackageByName(name, parent, recursive);
        if (pkg != null) {
            return pkg;
        }
        pkg = parent.GetPackages()
                    .AddNew(name, EaMetaType.PACKAGE.toString());
        pkg.Update();
        parent.Update();
        parent.GetPackages()
              .Refresh();
        parent.GetPackages()
              .Refresh();

        return pkg;
    }

    /**
     * Looks for a subpackage with a given unqualified name within a given EA package. The
     * search is case sensitive. The first matching package ir returned performing a breadth-first search.
     * If more than one package with the same unqualified name exists within the repos, the result may
     * be ambiguous.
     *
     * @param theName   The unqualified package name to look for
     * @param rootPkg   The EA model root package to search within
     * @param recursive Set to true to do a recursive search in package hierarchy,
     *                  false to do a flat search at current level only
     * @return The Package object in the EA model, or null if package was not found.
     */
    Package findPackageByName(final String theName, final Package rootPkg, final boolean recursive) {
        ensureRepoIsOpen();

        if (rootPkg == null) {
            return rootPkg;
        }

        Package nextPkg;

        for (final Package pkg : rootPkg.GetPackages()) {
            if (pkg.GetName()
                   .equals(theName) && packageMatch(pkg)) {
                return pkg;
            }
            if (recursive) {
                nextPkg = findPackageByName(theName, pkg, recursive);

                if (nextPkg != null) {
                    // Found it
                    return nextPkg;
                }
            }
        }
        // No match
        return null;
    }

    /**
     * Alternative name for better code readability internally in this class
     */

    private boolean ensureRepoIsOpen() {
        return open();
    }

    /**
     * Open the Enterprise Architect model repository.
     */
    public boolean open() {
        if (isOpen) {
            return true;
        }
        try {
            reposString = reposFile.getAbsolutePath();
            final String[] reposStrings = reposString.split("db:");
            if (reposStrings.length >= 2) {
                reposString = reposStrings[1];
            }
            LOG.info("Opening model repository: " + reposString);
            LOG.debug("Before new repos " + new Date());
            repository = new Repository();
            LOG.debug("After new repos " + new Date());
            repository.SetSuppressEADialogs(true);
            repository.SetSuppressSecurityDialog(true);
            repository.SetEnableCache(true);
            if (EA_USERNAME.exists() && EA_PASSWORD.exists()) {
                final String username = EA_USERNAME.value();
                final String pwd = EA_PASSWORD.value();
//            log.debug("Username/pwd : [" + username + "]:[" + pwd + "]" );
                repository.OpenFile2(reposString, username, StringUtils.trimToEmpty(pwd));
            } else {
                repository.OpenFile(reposString);
            }
            LOG.debug("After open " + new Date());
            isOpen = true;
        } catch (final Exception e) {
            e.printStackTrace();
            LOG.error(e.toString());
            final String msg = "An error occurred. This might be caused by an incorrect diagramgen-repo connect string.\n" +
                    "Verify that the connect string in the ea.application.properties file is the same as\n" +
                    "the connect string that you can find in Enterprise Architect via the File->Open Project dialog";
            System.out.println(msg);
            return false;
        }
        return true;
    }

    private void populateCache() {
        System.out.println("Start populating package cache " + new Date());
        populateCache(rootPackage);
        System.out.println("Finished populating package cache " + new Date() + " # of Packages " + packageCache.size());
    }

    /**
     * @param namespaceURI
     * @return
     */
    public Package findOrCreatePackageFromNamespace(final String namespaceURI) {
        ensureRepoIsOpen();
        LOG.debug("Looking for package with namespace:" + namespaceURI);

        // todo implement
        return findPackageByName("Klasser", true);
    }

    /**
     * Looks for a subpackage with a given unqualified name within a given EA package. The
     * search is case sensitive. The first matching package ir returned performing a breadth-first search.
     * If more than one package with the same unqualified name exists within the repos, the result may
     * be ambiguous. The search is always performed from the root of the repos.
     *
     * @param theName   The unqualified package name to look for
     * @param recursive Set to true to do a recursive search in package hierarchy,
     *                  false to do a flat search at current level only
     * @return The Package object in the EA model, or null if package was not found.
     */
    public Package findPackageByName(final String theName, final boolean recursive) {
        return findPackageByName(theName, rootPackage, recursive);
    }

    public Element findXsdType(final Package pkg, final String xsdTypeName) {
        final String stereotype = pkg.GetStereotypeEx();
        final String pkgString = pkg.GetName();

        if (stereotype.equals(xsdSchemaStereotype)) {
            LOG.info("Looking for " + xsdTypeName + " inside  package " + pkgString);
            for (final Element element : pkg.GetElements()) {
                if (element.GetName()
                           .equals(xsdTypeName)) {
                    return element;
                }
            }
        } else {
            // Loop through all subpackages in EA model pkg
            for (final Package aPackage : pkg.GetPackages()) {
                final Element element = findXsdType(aPackage, xsdTypeName);
                if (element != null) {
                    return element;
                }
            }
        }
        return null;
    }

    /**
     * Generate all diagrams from the model into the directory path.
     * The package structure of the model is retained as directory structure.
     * All existing diagrams are overwritten.
     */
    public int generateAllDiagramsFromRoot() {
        if (packageCache.isEmpty()) {
            populateCache();
        }
        final IntCounter count = new IntCounter();
        generateAllDiagrams(rootPackage, count);
        return count.count;
    }

    /**
     * Recursive method that finds all diagrams in a package and writes them to file.
     *
     * @param pkg
     * @param diagramCount
     */
    public void generateAllDiagrams(final Package pkg, final IntCounter diagramCount) {
        for (final Package p : pkg.GetPackages()) {
            generateAllDiagrams(p, diagramCount);
        }
        if (!packageMatch(pkg)) {
            LOG.info("--- Skipping package " + pkg.GetName());
            return;
        }

        final List<EaDiagram> diagrams = findDiagramsInPackage(pkg);
        if (!diagrams.isEmpty()) {
            LOG.debug("Generating diagrams in package: " + pkg.GetName());
            diagramCount.count = diagramCount.count + diagrams.size();
            for (final EaDiagram eaDiagram : diagrams) {
                final String diagramUrl = eaDiagram.writeImageToFile(false);
                LOG.debug("Generated diagram: [{}] with url: [{}]", eaDiagram.getName(), diagramUrl);
                repository.CloseDiagram(eaDiagram.getDiagramID()); // Try to avoid the 226 bug.
            }
        }
    }

    public boolean packageMatch(final Package p) {
        if (p == null) {
            return false;
        }
        if (packagePattern == null) {
            return true;
        }
        final Matcher matcher = packagePattern.matcher(p.GetName());
        if (matcher.matches()) {
            LOG.debug("Package match :" + p.GetName());
            return true;
        }
        LOG.debug("Looking for parent match for {} ", p.GetName());
        return packageMatch(findParentPackage(p));
    }

    /**
     * Find all UML diagrams inside a specific Package. Non-recursive, searches the top-level (given)
     * package only.
     *
     * @param pkg the Package to search in.
     * @return
     */
    public List<EaDiagram> findDiagramsInPackage(final Package pkg) {
        if (pkg == null) {
            return Collections.emptyList();
        }
        final List<EaDiagram> result = new ArrayList<>();
        final Collection<Diagram> diagrams;
        try {
            diagrams = pkg.GetDiagrams();
        } catch (final Exception e) {
            LOG.error("Fuckup in diagram package", e);
            return Collections.emptyList();
        }
        if (diagrams == null) {
            LOG.error("Fuckup in diagram package " + pkg.GetName());
            return Collections.emptyList();
        }
        for (final Diagram d : diagrams) {
            result.add(new EaDiagram(this, d, getPackagePath(pkg)));
        }
        for (final Element element : pkg.GetElements()) {
            findDiagramsInElements(pkg, element, result);
        }
        return result;
    }

    /**
     * Creates path on the form /a/b/c
     *
     * @param pkg
     * @return
     */
    public String getPackagePath(final Package pkg) {
        final ArrayList<Package> ancestorPackages = new ArrayList<>();
        getAncestorPackages(ancestorPackages, pkg);
        final StringBuilder pathName = new StringBuilder();
        Collections.reverse(ancestorPackages);
        for (final Package p : ancestorPackages) {
            pathName.append(NameNormalizer.URL_SEPARATOR)
                    .append(p.GetName());
        }
        return pathName.toString();
    }

    private void getAncestorPackages(final ArrayList<Package> ancestorPackages, final Package pkg) {
        ancestorPackages.add(pkg);
        if (pkg.GetParentID() != 0) {
            getAncestorPackages(ancestorPackages, findPackageByID(pkg.GetParentID()));
        }
    }

    public Package findPackageByID(final int packageID) {
        if (packageID == 0) {
            // id=0 means this is the root
            return null;
        }
        if (packageCache.isEmpty()) {
            ensureRepoIsOpen();
            return repository.GetPackageByID(packageID);
        } else {
            return packageCache.get(packageID).me;
        }
    }

    public Package findParentPackage(final Package pack) {
        if (pack == null || pack.GetParentID() == 0) {
            return null;
        }
        final int key = pack.GetParentID();
        LOG.info("Looking for package with id {} ", key);
        if (packageCache.isEmpty()) {
            ensureRepoIsOpen();
            return repository.GetPackageByID(key);
        } else {
            final EaPackage eaPackage = packageCache.get(key);
            LOG.info("Found package in cache {} ", eaPackage != null ? eaPackage.me.GetName() : "not found...");
            return eaPackage != null ? eaPackage.me : null;
        }
    }

    /**
     * Some diagrams may reside below elements
     *
     * @param pkg
     * @param element
     * @param diagramList
     */
    public void findDiagramsInElements(final Package pkg, final Element element, final List<EaDiagram> diagramList) {
        if (element == null || element.GetElements() == null) {
            return;
        }
        for (final Element child : element.GetElements()) {
            findDiagramsInElements(pkg, child, diagramList);
        }
        for (final Diagram diagram : element.GetDiagrams()) {
            diagramList.add(new EaDiagram(this, diagram, getPackagePath(pkg)));
        }
    }

    public void generateHtml(final String path) {
        System.out.println("Generating HTML doc for package " + rootPackage.GetName() + " to " + path);
        repository.GetProjectInterface()
                  .RunHTMLReport(rootPackage.GetPackageGUID(), path, "PNG", "<default>", ".html");
    }

    /**
     * Generates XSD schema file for the package if its UML stereotype is <<XSDschema>>,
     * otherwise a subdirectory corresponding to the UML package is created in
     * directory and the method is called recursively for all its subpackages.
     *
     * @param directory The file system directory for generation
     * @param pkg       The EA model package to process
     */
    public void generateXSD(final File directory, final Package pkg, final String fileSeparator) {
        final Project eaProj = getProject();

        final String stereotype = pkg.GetStereotypeEx();
        final String pkgString = pkg.GetName();

        if (stereotype.equals(xsdSchemaStereotype)) {
            LOG.info("Generate XSD for package " + pkgString);
            eaProj.GenerateXSD(pkg.GetPackageGUID(), directory.getAbsolutePath() + fileSeparator + pkgString + ".xsd", xmlEncoding, null);
        } else {
            // Create subdirectory in generation directory
            final File f = new File(directory, pkgString);

            if (f.mkdirs()) {
                LOG.debug("New subdir at: " + f.getAbsolutePath());
            }

            // Loop through all subpackages in EA model pkg
            for (final Package aPackage : pkg.GetPackages()) {
                generateXSD(f, aPackage, fileSeparator);
            }
        }
    }

    public Project getProject() {
        ensureRepoIsOpen();
        return repository.GetProjectInterface();
    }

    /**
     * Just an early test method to display the internal EA data types
     *
     * @return
     */
    public String getEaDataTypes() {
        ensureRepoIsOpen();

//        Collection<Author> authors = repository.GetAuthors();
//        for (Author a : authors) {
//            System.out.println(a.GetName());
//        }

        final StringBuilder sb = new StringBuilder();
        final Collection<Datatype> dataTypes = repository.GetDatatypes();
        for (final Datatype dt : dataTypes) {
            sb.append(dt.GetName())
              .append(", ");
        }
        return sb.toString();
    }

    /**
     * todo do we need this, then code it right...
     *
     * @param object
     * @param name
     * @param value
     */
    public void setAttributeValue(final Element object, final String name, final String value) {
        ensureRepoIsOpen();
        // @VAR;Variable=name;Value=mittNavnPaaObjekt;Op==;@ENDVAR;@VAR;Variable=attribEn;Value=enverdi;Op==;@ENDVAR;
        object.SetRunState("@VAR;Variable=name;Value=dittnavn;Op==;@ENDVAR;");
        object.Update();
    }

    public EaDiagram createOrUpdateStandardDiagram(final EaElement centralElement) {

        final String diagramName = EaDiagram.createStandardDiagramName(centralElement);

        EaDiagram eaDiagram = centralElement.findDiagram(diagramName);

        if (eaDiagram == null) {
            final Package pack = findPackageByID(centralElement.getPackageID());
            final Collection<Diagram> diagrams = pack.GetDiagrams();
            final Diagram diagram = diagrams.AddNew(diagramName, EaDiagramType.COMPONENT.toString());
            pack.Update();
            repository.SaveAllDiagrams();
            diagrams.Refresh();
            eaDiagram = new EaDiagram(this, diagram, getPackagePath(pack));
            eaDiagram.setParentId(centralElement.getId());
            LOG.info("Created diagram {} below {}", diagramName, centralElement.getName());
        } else {
            eaDiagram.removeAllElements();
            LOG.info("Removed elements from {}", eaDiagram.getName());
        }
        eaDiagram.hideDetails();

        eaDiagram.add(centralElement);

        List<DiagramObject> diagramObjects = new ArrayList<>();
        for (final EaElement eaElement : centralElement.findConnectedElements()) {
            if (eaElement.getMetaType() != EaMetaType.NOTE) {
                diagramObjects.add(eaDiagram.add(eaElement));
            }
        }

        final Project project = getProject();
        boolean layoutResult = doLayout(eaDiagram, project.GUIDtoXML(eaDiagram.getGuid()),
                                        EA_AUTO_DIAGRAM_OPTIONS.toInt(),
                                        EA_AUTO_DIAGRAM_ITERATIONS.toInt(),
                                        EA_AUTO_DIAGRAM_LAYER_SPACING.toInt(),
                                        EA_AUTO_DIAGRAM_COLUMN_SPACING.toInt());

        layoutResult = doLayout(eaDiagram, eaDiagram.getGuid(),
                                EA_AUTO_DIAGRAM_OPTIONS.toInt(),
                                EA_AUTO_DIAGRAM_ITERATIONS.toInt(),
                                EA_AUTO_DIAGRAM_LAYER_SPACING.toInt(),
                                EA_AUTO_DIAGRAM_COLUMN_SPACING.toInt());

        repository.SaveAllDiagrams();

        eaDiagram.adjustElementAppearances();

        repository.SaveAllDiagrams();
        return eaDiagram;
    }

    private boolean doLayout(final EaDiagram eaDiagram, final String guid, final int options, final int iterations, final int layerSpacing, final
    int columnSpacing) {
        final Project project = getProject();
        final boolean result = project.LayoutDiagramEx(guid,
                                                       options,
                                                       iterations,
                                                       layerSpacing,
                                                       columnSpacing,
                                                       false);

        LOG.info("Applied autolayout to [{}] [{}] result: [{}]. Ran with options [{}], iterations {} layerSpacing {} columnSpacing {}",
                 eaDiagram.getName(), guid, result, String.format("%#010x", options), iterations, layerSpacing, columnSpacing);
        return result;
    }
}

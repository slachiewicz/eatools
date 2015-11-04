package no.eatools.diagramgen;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final transient Logger log = LoggerFactory.getLogger(EaRepo.class);
    /* Boolean flags that can be used as input params */
    public static final boolean RECURSIVE = true;
    public static final boolean NON_RECURSIVE = false;


    /* Name of the UML stereotype for an XSDschema package */
    private static final String xsdSchemaStereotype = "XSDschema";

    /* The character encoding to use for XSD generation */
    private static final String xmlEncoding = "UTF-8";
    private File reposFile;
    private Repository repository = null;
    private boolean isOpen = false;
    private String reposString;
    private final Pattern packagePattern;

    /**
     * @param repositoryFile local file or database connection string
     */
    public EaRepo(final File repositoryFile) {
        reposFile = repositoryFile;
        final String packagePatternRegexp = EA_PACKAGE_FILTER.value();
        if (StringUtils.isNotBlank(packagePatternRegexp)) {
            packagePattern = Pattern.compile(packagePatternRegexp);
            log.info("Looking for packages matching [" + packagePatternRegexp + "]" + packagePattern.pattern());
        } else {
            packagePattern = null;
        }
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
            log.info("Opening model repository: " + reposString);
            log.debug("Before new repos " + new Date());
            repository = new Repository();
            log.debug("After new repos " + new Date());
            repository.SetSuppressEADialogs(true);
            repository.SetSuppressSecurityDialog(true);
            if (EA_USERNAME.exists() && EA_PASSWORD.exists()) {
                final String username = EA_USERNAME.value();
                final String pwd = EA_PASSWORD.value();
//            log.debug("Username/pwd : [" + username + "]:[" + pwd + "]" );
                repository.OpenFile2(reposString, username, StringUtils.trimToEmpty(pwd));
            } else {
                repository.OpenFile(reposString);
            }
            log.debug("After open " + new Date());
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

    /**
     * Alternative name for better code readability internally in this class
     */

    private boolean ensureRepoIsOpen() {
        return open();
    }

    /**
     * Closes the Enterprise Architect model repository.
     */
    public void close() {
        if (isOpen && repository != null) {
            log.info("Closing repository: " + reposString);
            repository.SetFlagUpdate(true);
            repository.CloseFile();
            repository.Exit();
            repository = null;
        }
        isOpen = false;
    }

    /**
     * Looks for a subpackage with a given unqualified name within a given EA package. The
     * search is case sensitive. The first matching package ir returned performing a breadth-first search.
     * If more than one package with the same unqualified name exists within the repos, the result may
     * be ambiguous.
     *
     * @param theName   The unqualified package name to look for
     * @param rootPkg   The EA model root package to search whithin
     * @param recursive Set to true to do a recursive search in package hierarchy,
     *                  false to do a flat search at current level only
     * @return The Package object in the EA model, or null if package was not found.
     */
    public Package findPackageByName(final String theName, final Package rootPkg, final boolean recursive) {
        ensureRepoIsOpen();

        if (rootPkg == null) {
            return rootPkg;
        }

        Package nextPkg;

        for (final Package pkg : rootPkg.GetPackages()) {
            if (pkg.GetName().equals(theName)) {
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
        return findPackageByName(theName, getRootPackage(), recursive);
    }

    public Package findPackageByID(final int packageID) {
        if (packageID == 0) {
            // id=0 means this is the root
            return null;
        }
        ensureRepoIsOpen();
        return repository.GetPackageByID(packageID);
    }

    public Element findElementByID(final int elementId) {
        if (elementId == 0) {
            return null;
        }
        ensureRepoIsOpen();
        return repository.GetElementByID((elementId));
    }

    /**
     * Find the top level (aka root) package in a given repository.
     * todo check for NPEs.
     *
     * @return the root package or possibly null if there are no root package in the repository.
     * This is normally the "Views" package or the "Model" package, but it may have an arbitrary name.
     */
    public Package getRootPackage() {
        ensureRepoIsOpen();
        final String rootPkgName = EA_ROOTPKG.value();
        System.out.println("root package name = " + rootPkgName);
        for (final Package aPackage : repository.GetModels()) {
            if (aPackage.GetName().equalsIgnoreCase(rootPkgName)) {
                log.debug("Found top level package: " + aPackage.GetName());
                return aPackage;
            }
        }
        throw new RuntimeException("Root pkg '" + rootPkgName + "' not found");
    }

    public Project getProject() {
        ensureRepoIsOpen();
        return repository.GetProjectInterface();
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
            log.info("Generate XSD for package " + pkgString);
            eaProj.GenerateXSD(pkg.GetPackageGUID(), directory.getAbsolutePath() + fileSeparator + pkgString + ".xsd", xmlEncoding, null);
        } else {
            // Create subdirectory in generation directory
            final File f = new File(directory, pkgString);

            if (f.mkdirs()) {
                log.debug("New subdir at: " + f.getAbsolutePath());
            }

            // Loop through all subpackages in EA model pkg
            for (final Package aPackage : pkg.GetPackages()) {
                generateXSD(f, aPackage, fileSeparator);
            }
        }
    }

    public Element findXsdType(final Package pkg, final String xsdTypeName) {
        final String stereotype = pkg.GetStereotypeEx();
        final String pkgString = pkg.GetName();

        if (stereotype.equals(xsdSchemaStereotype)) {
            log.info("Looking for " + xsdTypeName + " inside  package " + pkgString);
            for (final Element element : pkg.GetElements()) {
                if (element.GetName().equals(xsdTypeName)) {
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
     * Find UML Node elements inside a specific UML Package.
     * Non-recursive, searches the top-level (given) package only.
     *
     * @param pack
     * @return
     */
    public List<Element> findNodesInPackage(final Package pack) {
        return findElementsOfTypeInPackage(pack, EaMetaType.NODE);
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

    /**
     * Find UML Object elements inside a specific UML Package.
     * Non-recursive, searches the top-level (given) package only.
     *
     * @param pack the Package to search in.
     * @return
     */
    public List<Element> findComponentInstancesInPackage(final Package pack) {
        final List<Element> theComponents = findElementsOfTypeInPackage(pack, EaMetaType.COMPONENT);
        final List<Element> componentInstances = new ArrayList<Element>();
        for (final Element component : theComponents) {
            if (EaMetaType.COMPONENT.toString().equals(component.GetClassifierType())) {
                componentInstances.add(component);
            }
        }
        return componentInstances;
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
            if (element.GetName().equals(objectName)) {
                if (isOfType(element, classifier)) {
                    return element;
                }
            }
        }
        return addElementInPackage(pack, objectName, EaMetaType.OBJECT, classifier);
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

    private Element addElementInPackage(final Package pack, final String name, final EaMetaType umlType, final Element classifier) {
        ensureRepoIsOpen();

        final Element element = pack.GetElements().AddNew(name, umlType.toString());
        pack.GetElements().Refresh();

        if (classifier != null) {
            element.SetClassifierID(classifier.GetElementID());
            element.SetClassifierName(classifier.GetName());
        }

        element.Update();
        pack.Update();
        pack.GetElements().Refresh();
        return element;
    }

    public boolean isOfType(final Element theObject, final Element classifier) {
        final int classifierId = theObject.GetClassifierID();
        if (classifier == null) {
            return (classifierId == 0);
        }

        return (classifier.GetElementID() == classifierId);
    }

    private Element findNamedElementOnList(final List<Element> elementList, final String elementName) {
        ensureRepoIsOpen();

        for (final Element element : elementList) {
            if (element.GetName().equals(elementName)) {
                return element;
            }
        }

        return null;
    }

    /**
     * Assemble a List of all Model elements of a certain EaMetaType in the given package.
     * Sub-packages are not examined (non-recursive).
     *
     * @param pkg  the Package to look in.
     * @param type the type of Element to look for.
     * @return a List of found Elements, possibly empty, but never null.
     */
    public List<Element> findElementsOfTypeInPackage(final Package pkg, EaMetaType type) {
        ensureRepoIsOpen();

        if (pkg == null) {
            return Collections.emptyList();
        }

        type = (type == null) ? EaMetaType.NULL : type;

        final List<Element> result = new ArrayList<Element>();

        for (final Element e : pkg.GetElements()) {
            if (type.toString().equals(e.GetMetaType())) {
                result.add(e);
            }
        }

        return result;
    }

    /**
     * Find an element of a specific EaMetaType with a given name.
     *
     * @param pack
     * @param type
     * @param name
     * @return null if no match is found.
     */
    public Element findElementOfType(final Package pack, final EaMetaType type, String name) {
        ensureRepoIsOpen();
        name = name.trim();

        final List<Element> existingElements = findElementsOfTypeInPackage(pack, type);

        for (final Element element : existingElements) {
            if (element.GetName().equals(name)) {
                return element;
            }
        }
        return null;
    }

    public Diagram findDiagramById(final int id) {
        return repository.GetDiagramByID(id);
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

    /**
     * @param namespaceURI
     * @return
     */
    public Package findOrCreatePackageFromNamespace(final String namespaceURI) {
        ensureRepoIsOpen();
        log.debug("Looking for package with namespace:" + namespaceURI);

        // todo implement
        return findPackageByName("Klasser", true);
    }

    /**
     * @param definedPackage
     * @param className
     * @return
     */
    public Element findOrCreateClassInPackage(final Package definedPackage, final String className) {
        ensureRepoIsOpen();

        Element theClass = findNamedElementOnList(findClassesInPackage(definedPackage), className);

        if (theClass != null) {
            return theClass;
        }

        theClass = definedPackage.GetElements().AddNew(className, EaMetaType.CLASS.toString());
        theClass.Update();
        definedPackage.Update();

        return theClass;
    }

    /**
     * @param definedPackage
     * @param componentName
     * @return
     */
    public Element findOrCreateComponentInPackage(final Package definedPackage, final String componentName) {
        ensureRepoIsOpen();

        final Element theComponent = findNamedElementOnList(findComponentsInPackage(definedPackage), componentName);

        if (theComponent != null) {
            return theComponent;
        }

        return addElementInPackage(definedPackage, componentName, EaMetaType.COMPONENT, null);
    }

    public boolean deleteObjectInPackage(final Package pkg, final String objectName, final Element classifier) {
        if (pkg == null) {
            return false;
        }
        short index = 0;
        short indexToDelete = -1;
        for (final Element element : pkg.GetElements()) {
            if (element.GetName().equals(objectName)) {
                if ((classifier == null) || (classifier.GetElementID() == element.GetClassifierID())) {
                    indexToDelete = index;
                }
            }
            ++index;
        }
        if (indexToDelete != -1) {
            pkg.GetElements().Delete(indexToDelete);
            pkg.Update();
            pkg.GetElements().Refresh();
            return true;
        }
        return false;  //To change body of created methods use File | Settings | File Templates.
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
            if (d.GetName().equals(name) && (type.toString().equals(d.GetType()))) {
                return d;
            }
        }
        final Diagram newDiagram = pkg.GetDiagrams().AddNew(name, type.toString());
        pkg.GetDiagrams().Refresh();
        newDiagram.Update();
        pkg.Update();

        return newDiagram;
    }

    public Package findOrCreatePackage(final Package parent, final String name, final boolean recursive) {
        ensureRepoIsOpen();
        Package pkg = findPackageByName(name, parent, recursive);
        if (pkg != null) {
            return pkg;
        }
        pkg = parent.GetPackages().AddNew(name, EaMetaType.PACKAGE.toString());
        pkg.Update();
        parent.Update();
        parent.GetPackages().Refresh();
        parent.GetPackages().Refresh();

        return pkg;
    }

    public Package findOrCreatePackage(final Package parent, final String name) {
        return findOrCreatePackage(parent, name, EaRepo.NON_RECURSIVE);
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
     * @param from the source/originator of the link, aka the "Supplier" in EA terms.
     * @param to   the target/destination of the link, aka the "Client" in EA terms.
     * @param name name of the link. Used to look up already existing links.
     * @return
     */
    public Connector findOrCreateLink(final Element from, final Element to, final String name) {
        // check for existence
        for (final Connector c : to.GetConnectors()) {
            if (c.GetName().equals(name)) {
                if ((c.GetSupplierID() == to.GetElementID()) && (c.GetClientID() == from.GetElementID())) {
                    return c;
                }
            }
        }

        final Connector c = to.GetConnectors().AddNew(name, EaMetaType.ASSOCIATION.toString());
        c.SetSupplierID(to.GetElementID());
        if (!c.Update()) {
            log.error("Unable to update connector to: " + to.GetName());
            return null;
        }
        to.GetConnectors().Refresh();

        c.SetClientID(from.GetElementID());
        if (!c.Update()) {
            log.error("Unable to update connector from: " + from.GetName());
            return null;
        }
        from.GetConnectors().Refresh();

        c.SetDirection(EaLinkDirection.SOURCE_DESTINATION.toString());
        c.Update();

        from.Update();
        to.Update();
        return c;
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
            sb.append(dt.GetName()).append(", ");
        }
        return sb.toString();
    }

    public List<String> findAllMetaTypesInModel() {
        final Set<String> result = new HashSet<String>();
        findMetaTypesInPackage(getRootPackage(), result);
        return new ArrayList<String>(result);
    }

    private void findMetaTypesInPackage(final Package pkg, final Set<String> result) {
        for (final Element element : pkg.GetElements()) {
            result.add(element.GetMetaType());
        }
        for (final Package aPackage : pkg.GetPackages()) {
            findMetaTypesInPackage(aPackage, result);
        }
    }

    public DiagramObject findOrCreateDiagramObject(final Package pkg, final Diagram diagram, final Element reposElement) {
        for (final DiagramObject dObject : diagram.GetDiagramObjects()) {
            if (dObject.GetElementID() == reposElement.GetElementID()) {
                return dObject;
            }
        }
        final DiagramObject diagramObject = diagram.GetDiagramObjects().AddNew("", "");
        diagramObject.SetInstanceID(reposElement.GetElementID());
        diagramObject.SetElementID(reposElement.GetElementID());
        diagramObject.Update();
        diagram.Update();
        pkg.GetDiagrams().Refresh();
        pkg.Update();
        return diagramObject;
    }

    @Override
    public String toString() {
        return reposFile.getAbsolutePath() + " " + this.repository.toString();
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
            log.debug("Package match :" + p.GetName());
            return true;
        }
        log.debug("Looking for parent match");
        return packageMatch(findPackageByID(p.GetParentID()));
    }
}

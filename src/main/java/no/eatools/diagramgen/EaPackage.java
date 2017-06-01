package no.eatools.diagramgen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import no.bouvet.ohs.ea.dd.DDEntry;
import no.bouvet.ohs.ea.dd.DDEntryList;
import no.eatools.util.EaApplicationProperties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Attribute;
import org.sparx.AttributeTag;
import org.sparx.Collection;
import org.sparx.Connector;
import org.sparx.Element;
import org.sparx.Package;
import org.sparx.TaggedValue;

import static java.util.Collections.*;
import static no.bouvet.ohs.ea.dd.DDType.*;
import static no.eatools.diagramgen.EaMetaType.*;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * @author ohs
 */
public class EaPackage {
    private static final transient Logger LOG = LoggerFactory.getLogger(EaPackage.class);

    final String name;
    final EaRepo repos;
    final Package me;
    final Map<Connector, String> connectorMap = new HashMap<>();
    final Set<String> allConnectors = new HashSet<>();
    final EaPackage parent;
    final int id;
    private final int parentId;

    public EaPackage(final String name, final EaRepo repos) {
        this.name = trimToEmpty(name);
        this.repos = repos;
        final EaPackage cachedPkg = repos.findPackageByName(name, true);
        if (cachedPkg == null) {
            LOG.error("Package [{}] not found", name);
            me = null;
            id = 0;
            parentId = 0;
        } else {
            this.me = cachedPkg
                    .unwrap();
            id = me.GetPackageID();
            parentId = me.GetParentID();
        }
        parent = null;
    }

    public EaPackage(final Package pkg, final EaRepo repos, final EaPackage parent) {
        this.repos = repos;
        me = pkg;
        this.parent = parent;
        name = trimToEmpty(pkg.GetName());
        id = pkg.GetPackageID();
        parentId = parent != null ? parent.getId() : 0;
//        me.GetDiagrams().AddNew()
    }

    public static LinkedList<String> hirearchyToList(String nameHierarchy) {
        return new LinkedList<>(Arrays.asList(nameHierarchy.split("->")));
    }

    /**
     * Generate relationships between subpackages based on relationships between contained classes
     */
    public void generatePackageRelationships() {
        if (me == null) {
            LOG.warn("Cannot generate relationships. Package {} is not found", name);
            return;
        }
        deleteExistingConnectors(me);
        generateRelationships(me);
    }

    public void generateDDEntryFile() {
        final DDEntryList attributes = new DDEntryList(true, name);
        generateAttributesInPackage(this, attributes);
        attributes.writeToFile(name, true);
    }

    private void generateAttributesInPackage(final EaPackage pkg, final DDEntryList attributes) {
        System.out.println("Exporting Attributes in package " + pkg.getName() + " id:" + pkg.getId());

        for (final EaPackage eaPackage : repos.findPackages(pkg)) {
            generateAttributesInPackage(eaPackage, attributes);
        }
        final Collection<Element> elements = pkg.unwrap()
                                                .GetElements();
        generateAttributesForElements(attributes, elements);
    }

    private void generateAttributesForElements(final DDEntryList attributes, final Collection<Element> elements) {
        for (final Element element : elements) {
            generateAttributesForElements(attributes, element.GetElements());
            // Skip instances
            final EaElement eaElement = new EaElement(element, repos);
            if (repos.doGenerate(eaElement)) {
                final String msg = "Processing " + eaElement.toString();
                LOG.info(msg);
                System.out.println(msg);
                final DDEntry elementLine = createElementAutoDiagramAndLine(eaElement);
                attributes.add(elementLine);
                for (final Attribute attribute : eaElement.getAttributes()) {
                    attributes.add(createAttributeLine(eaElement, attribute));
                }
            }
        }
    }

    private DDEntry createElementAutoDiagramAndLine(final EaElement eaElement) {
        final EaDiagram eaDiagram;
        final String imageUrl;
        if (EaApplicationProperties.EA_AUTO_DIAGRAM_GENERATE.toBoolean()) {
            eaDiagram = repos.createOrUpdateStandardDiagram(eaElement);
            imageUrl = eaDiagram != null ? eaDiagram.writeImageToFile(true) : EMPTY;
        } else {
            imageUrl = EMPTY;
        }
        eaElement.setImageUrl(imageUrl);
        final String description = eaElement.getNotes();//.replaceAll("\n", "\\\\n").replaceAll("\r", "");

        final List<String> parents = eaElement.findParents()
                                              .stream()
                                              .map(e -> e.getPackageName() + UML_PACKAGE_DELIMITER + e.getName())
                                              .collect(Collectors.toList());
        final DDEntry ddEntry =
                new DDEntry(eaElement.getName(), description, null,
                            eaElement.getType()
                                     .toString(), eaElement.getStereotypeEx(), eaElement.getElementGUID(), eaElement.getVersion(), booleanToYesNo
                                    (false),
                            eaElement.getClassifierType(),
                            ELEMENT, parents, eaElement.getAuthor(), imageUrl, eaElement.getCreated());

        for (final TaggedValue attributeTag : eaElement.getTaggedValuesEx()) {
            ddEntry.addTaggedValue(attributeTag.GetName(), attributeTag.GetValue());
        }
        return ddEntry;
    }

    private DDEntry createAttributeLine(final EaElement element, final Attribute attribute) {
        final String description = attribute.GetNotes(); //.replaceAll("\n", "\\\\n").replaceAll("\r", "");

        final String elementName = element.getName();
        final DDEntry ddEntry =
                new DDEntry(elementName + ATTRIBUTE_DELIMITER + attribute.GetName(), description, attribute.GetLowerBound() +
                        UML_MULTIPLICITY_DELIMITER + attribute.GetUpperBound(),
                            attribute.GetType(), attribute.GetStereotypeEx(), attribute.GetAttributeGUID(), element.getVersion(),
                            booleanToYesNo(attribute.GetIsID()), EMPTY, ATTRIBUTE, emptyList(), element.getAuthor(), EMPTY, element.getCreated());

        LOG.debug("DD Entry created {}", ddEntry);

        if (!checkTaggedValues(attribute.GetTaggedValuesEx(), elementName, ddEntry)) {
            for (final AttributeTag attributeTag : attribute.GetTaggedValuesEx()) {
                LOG.info("TaggedValueEx {}.{}:{}={}", elementName, attribute.GetName(), attributeTag.GetName(), attributeTag.GetValue());
                ddEntry.addTaggedValue(attributeTag.GetName(), attributeTag.GetValue());
            }
        }
        if (!checkTaggedValues(attribute.GetTaggedValues(), elementName, ddEntry)) {
            for (final AttributeTag attributeTag : attribute.GetTaggedValues()) {
                LOG.info("TaggedValue {}.{}:{}={}", elementName, attribute.GetName(), attributeTag.GetName(), attributeTag.GetValue());
                ddEntry.addTaggedValue(attributeTag.GetName(), attributeTag.GetValue());
            }
        }
        return ddEntry;
    }

    /**
     * Hack in order to detect wrong tags which causes a ClassCastException.
     * Sometimes it is a "TaggedValue" instead
     *
     * @param attributeTags
     * @param elementName
     * @param ddEntry
     */
    private boolean checkTaggedValues(final Collection<AttributeTag> attributeTags, final String elementName, final DDEntry ddEntry) {
        boolean hasErrors = false;
        for (final Object attributeTag : attributeTags) {
            if (!(attributeTag instanceof AttributeTag)) {
                LOG.error("Illegal tag [{}] of [{}] {}", attributeTag.getClass()
                                                                     .getName(), elementName, ddEntry);
                hasErrors = true;
            }
        }
        return hasErrors;
    }

    private String booleanToYesNo(final boolean b) {
        return b ? "Yes" : "No";
    }

    private void deleteExistingConnectors(final Package pkg) {
        LOG.info("Deleting old connectors in {}", pkg.GetName());
        final Collection<Connector> connectors = pkg.GetElement()
                                                    .GetConnectors();
        for (short i = 0; i < connectors.GetCount(); i++) {
            connectors.Delete(i);
            pkg.Update();
        }
        for (final Package aPackage : pkg.GetPackages()) {
            deleteExistingConnectors(aPackage);
        }
    }

    /**
     * Recursively generate relationships
     *
     * @param pkg
     */
    private void generateRelationships(final Package pkg) {
        LOG.debug("***********" + pkg.GetName());

        for (final Package aPackage : pkg.GetPackages()) {
            generateRelationships(aPackage);
        }
        for (final Element element : pkg.GetElements()) {
            final EaElement eaElement = new EaElement(element, repos);

            LOG.debug("Finding connectors for Element {}", eaElement.getName());
            for (final Connector connector : eaElement.getConnectors()) {
                connectPackages(pkg, eaElement, connector);
            }
        }
    }

    private void connectPackages(final Package pkg, final EaElement element, final Connector connector) {
        final EaElement other = element.findConnectedElement(connector);
        final String packageConnectorType = DEPENDENCY.toString();
        final String connType = connector.GetType();
        LOG.debug("ConnType : " + connType);

        LOG.debug("Other end: " + other.getName());

        final int otherPackageId = other.getPackageID();
        if (otherPackageId != pkg.GetPackageID()) {
            final Package otherPkg = repos.findPackageByID(otherPackageId);

            final String connectorId = pkg.GetName() + " -> " + otherPkg.GetName();
            final String reverseConnectorId = otherPkg.GetName() + " -> " + pkg.GetName();

            if (allConnectors.contains(connectorId) || allConnectors.contains(reverseConnectorId)) {
                LOG.debug("Already had " + connectorId);
            } else {
                addPackageConnector(pkg, packageConnectorType, otherPkg, connectorId);
            }
            LOG.debug("Update pack 1 " + pkg.Update() + " " + pkg.GetName());
            LOG.debug("Update pack 2 " + otherPkg.Update() + " " + otherPkg.GetName());
        }
    }

    private void addPackageConnector(final Package pkg, final String packageConnectorType, final Package otherPkg, final String connectorName) {
        LOG.debug("Connecting " + connectorName);
        LOG.debug("Adding connector type " + packageConnectorType);
        allConnectors.add(connectorName);
        final Connector newConn = pkg.GetElement()
                                     .GetConnectors()
                                     .AddNew("", packageConnectorType);

        newConn.SetSupplierID(otherPkg.GetElement()
                                      .GetElementID());
        newConn.SetDirection("Unspecified");
        newConn.SetStereotype("xref");

        LOG.debug("Update connector " + newConn.Update());
        pkg.GetConnectors()
           .Refresh();

        LOG.debug("Added " + newConn.GetName() + newConn.GetClientID() + " " + newConn.GetSupplierID());
        LOG.debug(pkg.GetName());
        LOG.debug(otherPkg.GetName());
    }

    public void listElementProperties() {
        for (final Element element : me.GetElements()) {
            new EaElement(element, repos).listProperties();
        }
    }

    public void setTaggedValues(final List<String> taggedValues) {
        setTaggedValuesInPackage(me, taggedValues);
    }

    private void setTaggedValuesInPackage(final Package pkg, final List<String> taggedValues) {
        LOG.debug("************** Setting tagged values in package " + pkg.GetName() + " id:" + pkg.GetPackageID());

        for (final Package aPackage : pkg.GetPackages()) {
            setTaggedValuesInPackage(aPackage, taggedValues);
        }
        if (repos.packageMatch(pkg)) {
            for (final Element element : pkg.GetElements()) {
//                createTaggedValues(element, taggedValues);
                for (final Attribute attribute : element.GetAttributes()) {
                    createTaggedValues(attribute, taggedValues);
                    element.SetName(element.GetName() + "1");
                    element.Update();
                }
                listElementProperties();
            }
            pkg.Update();
        } else {
            LOG.info("************** Skipping package " + pkg.GetName() + " id:" + pkg.GetPackageID());
        }
    }

    private void createTaggedValues(final Element element, final List<String> taggedValues) {
        final Collection<TaggedValue> oldValues = element.GetTaggedValues();
        for (final String taggedValue : taggedValues) {
            if (oldValues.GetByName(taggedValue) == null) {
                oldValues.AddNew(taggedValue, "");
                element.Update();
                element.Refresh();
                LOG.info("************** Set tag " + taggedValue + " for element " + element.GetName());
            }
        }
//        element.Update();
    }

    private void createTaggedValues(final Attribute attribute, final List<String> taggedValues) {
        final Collection<AttributeTag> oldValues = attribute.GetTaggedValues();
        final Set<String> existingTags = new HashSet<>();
        for (final AttributeTag oldValue : oldValues) {
            LOG.debug("Existing tag ############## " + oldValue.GetName());
            existingTags.add(oldValue.GetName());
        }
        for (final String taggedValue : taggedValues) {
            if (!existingTags.contains(taggedValue)) {

                oldValues.AddNew(taggedValue, "xyz");
                oldValues.Refresh();
                attribute.Update();

                final Collection<AttributeTag> attributeTags = attribute.GetTaggedValuesEx();
                attributeTags
                        .AddNew(taggedValue, "abx");
                attributeTags.Refresh();
                attribute.Update();
                LOG.debug("************** Set tag " + taggedValue + " for attribute " + attribute.GetName());
            }
//            if(oldValues.GetByName(taggedValue) == null) {
//                oldValues.AddNew(taggedValue, "");
//                attribute.Update();
//            }
            for (final AttributeTag oldValue : attribute.GetTaggedValues()) {
                LOG.debug("############## After update " + oldValue.GetName());
            }
        }
//        attribute.Update();
    }

    public void listElements(final EaMetaType metaType) {
        final List<String> components = new ArrayList<>();
        components.add(new StringJoiner(";").add(metaType.toString())
                                            .add(" name")
                                            .add("StereoTypes")
                                            .add("Description")
                                            .add("Component type (for instances)")
                                            .add("Created by")
                                            .toString());
        listElements(me, components, metaType);
        final File file = new File(me.GetName() + "_" + metaType + ".csv");
        try {
            FileUtils.writeLines(file, components);
            System.out.println("File created: " + file.getAbsolutePath());
        } catch (final IOException e) {
            LOG.error("Unable to create file {} {}", file.getAbsoluteFile(), e);
        }
    }

    private void listElements(final Package pkg, final List<String> result, final EaMetaType metaType) {
        System.out.println("************** Listing result in package " + pkg.GetName() + " id:" + pkg.GetPackageID());

        for (final Package aPackage : pkg.GetPackages()) {
            listElements(aPackage, result, metaType);
        }
        if (repos.packageMatch(pkg)) {
            for (final Element element : pkg.GetElements()) {
                listElementAttributes(result, metaType, element);
            }
        } else {
            System.out.println("************** Skipping package " + pkg.GetName() + " id:" + pkg.GetPackageID());
        }
    }

    private void listElementAttributes(final List<String> result, final EaMetaType metaType, final Element element) {
        for (final Element element1 : element.GetElements()) {
            listElementAttributes(result, metaType, element1);
        }
        if (metaType.equals(element.GetType())
                || metaType.equals(element.GetClassifierType())) {
            System.out.println("Found  " + metaType + ": " + element.GetName() + " Classifier name :" + element.GetClassifierName() + " type " +
                                       element.GetClassifierType() + " classfierId " + element.GetClassfierID() + " classifierId " +
                                       element.GetClassifierID());
            System.out.println(element.GetAssociationClassConnectorID());
            final Element classifier = repos.findElementByID(element.GetClassfierID());

            final StringJoiner stringJoiner = new StringJoiner(";");
            addToJoiner(element, stringJoiner);
            if (classifier != null) {
                addToJoiner(classifier, stringJoiner);
            }
            result.add(stringJoiner.toString());
        }
    }

    private void addToJoiner(final Element element, final StringJoiner stringJoiner) {
        stringJoiner.add(element.GetName())
                    .add(element.GetStereotypeList())
                    .add(element.GetNotes())
                    .add(element.GetClassifierName())
                    .add(element.GetAuthor());
    }

    public void generateAutoDiagramsRecursively() {
        for (final Element element : me.GetElements()) {
            final EaDiagram eaDiagram = repos.createOrUpdateStandardDiagram(new EaElement(element, repos));
            if (eaDiagram != null) {
                LOG.info("Created/updated [{}] status [{}]", eaDiagram.getName(), eaDiagram.getStatus());
            }
        }
        for (final EaPackage eaPackage : children()) {
            eaPackage.generateAutoDiagramsRecursively();
        }
    }

    public List<EaPackage> children() {
        return repos.findPackages(this);
    }

    /**
     * private static EaDiagram recurseElements(EaRepo eaRepo, org.sparx.Package pkg, String diagramName, boolean recursive) {
     * if (pkg == null) {
     * return null;
     * }
     * for (Element element : pkg.GetElements()) {
     * element.GetAttributes();
     * for (Connector connector : element.GetConnectors()) {
     * Repository repository;
     * for (Connector connector1 : element.GetConnectors()) {
     * if (connector1.GetSupplierEnd().GetObjectType() == ObjectType.otElement) {
     *
     * }
     * }
     * element.GetConnectors().AddNew();
     * connector.GetSupplierEnd();
     * }
     * Collection<Element> elements = pkg.GetElements();
     * for (Element element1 : elements) {
     * Package p;
     * p.GetConnectors()
     * }
     * }
     *
     * if (recursive) {
     * for (Package p : pkg.GetPackages()) {
     * for (Connector connector : p.GetConnectors()) {
     * connector.
     * }
     *
     * EaDiagram d = findDiagram(eaRepo, p, diagramName, recursive);
     * if (d != null) {
     * return d;
     * }
     * }
     * }
     * return null;
     * }
     **/

    public String getName() {
        return name;
    }

    public EaPackage getParent() {
        return parent;
    }

    public int getId() {
        return id;
    }

    public Package unwrap() {
        return me;
    }

    public int getParentId() {
        return parentId;
    }

    @Override
    public String toString() {
        return "EaPackage{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", parentId=" + parentId +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final EaPackage eaPackage = (EaPackage) o;

        if (id != eaPackage.id) return false;
        if (parentId != eaPackage.parentId) return false;
        return name.equals(eaPackage.name);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + id;
        result = 31 * result + parentId;
        return result;
    }

    public Boolean createBaseline(final String versionNo, final String notes) {
        return repos.createBaseline(me.GetPackageGUID(), versionNo, notes);
    }

    public String toHierarchicalString() {
        if (parent != null) {
            return parent.toHierarchicalString() + "->" + name;
        }
        return name;
    }
}

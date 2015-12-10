package no.eatools.diagramgen;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.bouvet.ohs.ea.dd.DDEntry;
import no.bouvet.ohs.ea.dd.DDType;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Attribute;
import org.sparx.Collection;
import org.sparx.Connector;
import org.sparx.Element;
import org.sparx.Package;

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
//    BiMap

    public EaPackage(final String name, final EaRepo repos) {
        this.name = name;
        this.repos = repos;
        this.me = repos.findPackageByName(name, true);
        if(me == null) {
            LOG.error("Package {} not found", name);
        }
    }

    /**
     * Generate relationships between subpackages based on relationships between contained classes
     */
    public void generatePackageRelationships() {
        if(me == null) {
            LOG.warn("Cannot generate relationships. Package {} is not found", name);
            return;
        }
        deleteExistingConnectors(me);
        generateRelationships(me);
    }

    public void generateAttributesFile() {
        final List<String> attributes = new ArrayList<>();
        attributes.add("// Generated at " + ZonedDateTime.now().toString());
        generateAttributesInPackage(this.me, attributes);
        try {
            FileUtils.writeLines(new File("attributes.csv"), "UTF-8", attributes);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void generateAttributesInPackage(final Package pkg, final List<String> attributes) {
        System.out.println("Exporting Attributes in package " + pkg.GetName() + " id:" + pkg.GetPackageID());

        for (final Package aPackage : pkg.GetPackages()) {
            generateAttributesInPackage(aPackage, attributes);
        }
        for (final Element element : pkg.GetElements()) {
            System.out.println(element.GetName());
            attributes.add(createElementLine(element));
            for (final Attribute attribute : element.GetAttributes()) {
                attributes.add(createAttributeLine(element.GetName(), attribute, element.GetVersion()));
            }
        }
    }

    private String createElementLine(final Element element) {
        final String description = element.GetNotes();//.replaceAll("\n", "\\\\n").replaceAll("\r", "");

        final DDEntry ddEntry =
                new DDEntry(element.GetName(), description, null,
                            element.GetType(), element.GetStereotypeEx(), element.GetElementGUID(), element.GetVersion(), booleanToYesNo(false), DDType.ELEMENT);

        return ddEntry.toJson();
    }

    private String createAttributeLine(final String elementName, final Attribute attribute, final String version) {
        final String description = attribute.GetNotes();//.replaceAll("\n", "\\\\n").replaceAll("\r", "");

        final DDEntry ddEntry =
                new DDEntry(elementName + "." + attribute.GetName(), description, attribute.GetLowerBound() + ".." + attribute.GetUpperBound(),
                            attribute.GetType(), attribute.GetStereotypeEx(), attribute.GetAttributeGUID(), version, booleanToYesNo(attribute.GetIsID()), DDType.ATTRIBUTE);

        return ddEntry.toJson();
    }

    private String booleanToYesNo(final boolean b) {
        return b ? "Yes" : "No";
    }

    private void deleteExistingConnectors(final Package pkg) {
        LOG.info("Deleting old connectore in {}", pkg.GetName());
        final Collection<Connector> connectors = pkg.GetElement().GetConnectors();
        for (short i = 0; i < connectors.GetCount(); i++) {
            connectors.Delete(i);
            pkg.Update();
        }
        for (final Package aPackage : pkg.GetPackages()) {
            deleteExistingConnectors(aPackage);
        }
    }

    private void generateRelationships(final Package pkg) {
        System.out.println("***********" + pkg.GetName());

        for (final Package aPackage : pkg.GetPackages()) {
            generateRelationships(aPackage);
        }
        for (final Element element : pkg.GetElements()) {
            System.out.println(element.GetName());
            for (final Connector connector : element.GetConnectors()) {
                final Element other;
                if (connector.GetClientID() == element.GetElementID()) {
                    other = repos.findElementByID(connector.GetSupplierID());
                } else {
                    other = repos.findElementByID(connector.GetClientID());
                }
                final String connectorType;
                final String connType = connector.GetType();
                LOG.debug("ConnType : " + connType);
//                if (EaMetaType.GENERALIZATION.equals(connType)) {
//                    connectorType = EaMetaType.REALIZATION.toString();
//                } else {
                connectorType = EaMetaType.DEPENDENCY.toString();
//                }
                System.out.println("Other end: " + other.GetName());
                final int otherPackageId = other.GetPackageID();
                if (otherPackageId != pkg.GetPackageID()) {
                    final Package otherPkg = repos.findPackageByID(otherPackageId);

                    final String connectorId = pkg.GetName() + " -> " + otherPkg.GetName();
                    final String reverseConnectorId = otherPkg.GetName() + " -> " + pkg.GetName();

                    if (allConnectors.contains(connectorId) || allConnectors.contains(reverseConnectorId)) {
                        LOG.debug("Already had " + connectorId);
                    } else {
                        LOG.debug("Connecting " + connectorId);
                        LOG.debug("Adding connector type " + connectorType);
                        allConnectors.add(connectorId);
                        final Connector newConn = pkg.GetElement().GetConnectors().AddNew("", connectorType);

//                    newConn.
//                    newConn.SetMetaType(connectorType);
//                    newConn.SetClientID(pkg.GetPackageID());
                        newConn.SetSupplierID(otherPkg.GetElement().GetElementID());
                        newConn.SetDirection("Unspecified");
                        newConn.SetStereotype("xref");

                        System.out.println("Update connector " + newConn.Update());
                        pkg.GetConnectors().Refresh();

                        LOG.debug("Added " + newConn.GetName() + newConn.GetClientID() + " " + newConn.GetSupplierID());
                        LOG.debug(pkg.GetName());
                        LOG.debug(otherPkg.GetName());
                    }

                    System.out.println("Update pack 1 " + pkg.Update() + " " + pkg.GetName());
                    System.out.println("Update pack 2 " + otherPkg.Update() + " " + otherPkg.GetName());
                }
//
//                System.out.println(connector.GetName());
//                System.out.println(pkg.GetConnectors().GetCount());
            }
        }
    }

    /**
     private static EaDiagram recurseElements(EaRepo eaRepo, org.sparx.Package pkg, String diagramName, boolean recursive) {
     if (pkg == null) {
     return null;
     }
     for (Element element : pkg.GetElements()) {
     element.GetAttributes();
     for (Connector connector : element.GetConnectors()) {
     Repository repository;
     for (Connector connector1 : element.GetConnectors()) {
     if (connector1.GetSupplierEnd().GetObjectType() == ObjectType.otElement) {

     }
     }
     element.GetConnectors().AddNew();
     connector.GetSupplierEnd();
     }
     Collection<Element> elements = pkg.GetElements();
     for (Element element1 : elements) {
     Package p;
     p.GetConnectors()
     }
     }

     if (recursive) {
     for (Package p : pkg.GetPackages()) {
     for (Connector connector : p.GetConnectors()) {
     connector.
     }

     EaDiagram d = findDiagram(eaRepo, p, diagramName, recursive);
     if (d != null) {
     return d;
     }
     }
     }
     return null;
     }
     **/

}

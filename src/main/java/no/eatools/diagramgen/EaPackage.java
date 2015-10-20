package no.eatools.diagramgen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.bouvet.ohs.ea.dd.DDEntry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sparx.Attribute;
import org.sparx.Collection;
import org.sparx.Connector;
import org.sparx.Element;
import org.sparx.Package;


/**
 * @author ohs
 */
public class EaPackage {
    private static final transient Log LOG = LogFactory.getLog(EaPackage.class);

    final String name;
    final EaRepo repos;
    final Package me;
    final Map<Connector, String> connectorMap = new HashMap<Connector, String>();
    final Set<String> allConnectors = new HashSet<String>();
//    BiMap

    public EaPackage(final String name, final EaRepo repos) {
        this.name = name;
        this.repos = repos;
        this.me = repos.findPackageByName(name, true);
    }

    /**
     * Generate relationships between subpackages based on relationships between contained classes
     */
    public void generatePackageRelationships() {
        deleteExistingConnectors(me);
        generateRelationships(me);
    }

    public void generateAttributesFile() {
        final List<String> attributes = new ArrayList<>();
        generateAttributesInPackage(this.me, attributes);
        try {
            FileUtils.writeLines(new File("attributes.csv"), "UTF-8", attributes);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void generateAttributesInPackage(final Package pkg, final List<String> attributes) {
        System.out.println("***********" + pkg.GetName());

        for (final Package aPackage : pkg.GetPackages()) {
            generateAttributesInPackage(aPackage, attributes);
        }
        for (final Element element : pkg.GetElements()) {
            System.out.println(element.GetName());
            for (final Attribute attribute : element.GetAttributes()) {
                attributes.add(createAttributeLine(element.GetName(), attribute, element.GetVersion()));
            }
        }
    }

    private String createAttributeLine(final String elementName, final Attribute attribute, final String version) {
//        final StringJoiner sj = new StringJoiner(";");
//        sj.add(elementName + "." + attribute.GetName());
//        sj.add(attribute.GetNotes().replaceAll("\n", "\\\\n").replaceAll("\r", ""));
//        sj.add(attribute.GetLowerBound() + ".." + attribute.GetUpperBound());
//        sj.add(booleanToYesNo(attribute.GetIsID()));
//        sj.add(attribute.GetType());
//        sj.add(attribute.GetStereotypeEx());
//        sj.add(attribute.GetAttributeGUID());
//        sj.add(version);

//        String description = attribute.GetNotes().replaceAll("\n", "\\\\n").replaceAll("\r", "");
        final String description = attribute.GetNotes();//.replaceAll("\n", "\\\\n").replaceAll("\r", "");

        final DDEntry ddEntry = new DDEntry(elementName + "." + attribute.GetName(), description,
                attribute.GetLowerBound() + ".." + attribute.GetUpperBound(), attribute.GetType(), attribute.GetStereotypeEx(),
                attribute.GetAttributeGUID(), version, booleanToYesNo(attribute.GetIsID()));

        return ddEntry.toJson();
    }

    private String booleanToYesNo(final boolean b) {
        return b ? "Yes" : "No";
    }

    private void deleteExistingConnectors(final Package pkg) {
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

package no.eatools.diagramgen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    public EaPackage(String name, EaRepo repos) {
        this.name = name;
        this.repos = repos;
        this.me = repos.findPackageByName(name, true);
    }

    /**
     * Generate relationships between subpackages based on relationships between contained classes
     */
    public void generatePackageRelationships() {
        generateRelationships(me);
    }

    private void generateRelationships(Package pkg) {
        System.out.println("***********" + pkg.GetName());
        Collection<Connector> connectors = pkg.GetElement().GetConnectors();
        for (short i = 0; i < connectors.GetCount(); i++) {
            connectors.Delete(i);
            pkg.Update();
        }

        for (Package aPackage : pkg.GetPackages()) {
            generateRelationships(aPackage);
        }
        for (Element element : pkg.GetElements()) {
            System.out.println(element.GetName());
            for (Connector connector : element.GetConnectors()) {
                Element other;
                if (connector.GetClientID() == element.GetElementID()) {
                    other = repos.findElementByID(connector.GetSupplierID());
                } else {
                    other = repos.findElementByID(connector.GetClientID());
                }
                String connectorType;
                String connType = connector.GetType();
                LOG.debug("ConnType : " + connType);
                if (EaMetaType.GENERALIZATION.equals(connType)) {
                    connectorType = EaMetaType.REALIZATION.toString();
                } else {
                    connectorType = EaMetaType.DEPENDENCY.toString();
                }
                System.out.println("Other end: " + other.GetName());
                if (other.GetPackageID() != pkg.GetPackageID()) {
                    Package otherPkg = repos.findPackageByID(other.GetPackageID());

                    String connectorId = element.GetName() + " -> " + other.GetName();
                    String reverseConnectorId = other.GetName() + " -> " + element.GetName();

                    if (allConnectors.contains(connectorId) || allConnectors.contains(reverseConnectorId)) {
                        LOG.debug("Already had " + connectorId);
                    }
// else {
                        LOG.debug("Connecting " + connectorId);
                        LOG.debug("Adding connector type " + connectorType);
                        allConnectors.add(connectorId);
                        Connector newConn = pkg.GetElement().GetConnectors().AddNew(connectorId, connectorType);

//                    newConn.
//                    newConn.SetMetaType(connectorType);
//                    newConn.SetClientID(pkg.GetPackageID());
                        newConn.SetSupplierID(otherPkg.GetElement().GetElementID());
                        newConn.SetDirection("Unspecified");

                        System.out.println("Update connector " + newConn.Update());
                        pkg.GetConnectors().Refresh();

                        System.out.println("Added " + newConn.GetName() + newConn.GetClientID() + " " + newConn.GetSupplierID());
                        LOG.debug(repos.findPackageByID(pkg.GetPackageID()).GetName());
                        LOG.debug(repos.findPackageByID(other.GetPackageID()).GetName());
//                    }
                    System.out.println("Update pack 1 " + pkg.Update() + " " + pkg.GetName());
                    System.out.println("Update pack 2 " + otherPkg.Update() + " " + otherPkg.GetName());
                }

                System.out.println(connector.GetName());
                System.out.println(connector.GetSupplierEnd().GetObjectType());
                System.out.println(connector.GetClientEnd().GetObjectType());
                System.out.println(connector.GetClientEnd().GetEnd());
                System.out.println(pkg.GetConnectors().GetCount());
                for (Connector connector1 : pkg.GetConnectors()) {
                    System.out.println(connector1);
                }
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

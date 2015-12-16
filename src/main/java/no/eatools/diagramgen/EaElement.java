package no.eatools.diagramgen;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Attribute;
import org.sparx.Collection;
import org.sparx.Connector;
import org.sparx.Element;

/**
 * @author ohs
 */
public class EaElement {
    private static final transient Logger LOG = LoggerFactory.getLogger(EaElement.class);
    private final Element theElement;
    final EaRepo repos;

    public EaElement(Element theElement, EaRepo repos) {
        this.theElement = theElement;
        this.repos = repos;
    }

    public List<EaElement> findParents() {
        List<EaElement> result = new ArrayList<>();
        for (Connector connector : getConnectors()) {
            LOG.debug("Element {} has connector of type {}", getName(), connector.GetType());
            if (EaMetaType.GENERALIZATION.toString()
                                         .equals(connector.GetType())
                    && connector.GetClientID() == theElement.GetElementID()) {
                result.add(findConnectedElement(connector));
            }
        }
        return result;
    }

    public String getElementGUID() {
        return theElement.GetElementGUID();
    }

    public String getName() {
        return theElement.GetName();
    }

    public String getNotes() {
        return theElement.GetNotes();
    }

    public EaElement findConnectedElement(Connector connector) {
        if (connector.GetClientID() == theElement.GetElementID()) {
            return new EaElement(repos.findElementByID(connector.GetSupplierID()), repos);
        } else {
            return new EaElement(repos.findElementByID(connector.GetClientID()), repos);
        }
    }

    public int getPackageID() {
        return theElement.GetPackageID();
    }

    public String getPackageName() {
        return repos.findPackageByID(getPackageID()).GetName();
    }

    public Collection<Connector> getConnectors() {
        return theElement.GetConnectors();
    }

    public void listProperties() {
        for (EaElement eaElement : findParents()) {
            System.out.printf("Element %s has parent %s\n", getName(), eaElement.getName());
        }
    }

    public String getType() {
        return theElement.GetType();
    }

    public String getStereotypeEx() {
        return theElement.GetStereotypeEx();
    }

    public String getVersion() {
        return theElement.GetVersion();
    }

    public Collection<Attribute> getAttributes() {
        return theElement.GetAttributes();
    }


}

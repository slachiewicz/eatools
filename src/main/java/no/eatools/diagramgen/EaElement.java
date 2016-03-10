package no.eatools.diagramgen;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Attribute;
import org.sparx.AttributeTag;
import org.sparx.Collection;
import org.sparx.Connector;
import org.sparx.Element;
import org.sparx.TaggedValue;

/**
 * @author ohs
 */
public class EaElement {
    private static final transient Logger LOG = LoggerFactory.getLogger(EaElement.class);
    private final Element theElement;
    final EaRepo repos;

    public EaElement(final Element theElement, final EaRepo repos) {
        this.theElement = theElement;
        this.repos = repos;
    }

    public List<EaElement> findParents() {
        final List<EaElement> result = new ArrayList<>();
        for (final Connector connector : getConnectors()) {
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
        if(EaMetaType.NOTE.equals(getType()) || EaMetaType.TEXT.equals(getType())) {
            return theElement.GetNotes();
        }
        return theElement.GetName();
    }

    public String getNotes() {
        return theElement.GetNotes();
    }

    public EaElement findConnectedElement(final Connector connector) {
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
        System.out.println("Element " + theElement.GetName());
        for (final EaElement eaElement : findParents()) {
            System.out.printf("Element %s has parent %s%n", getName(), eaElement.getName());
        }
        listAttributes();
    }

    private void listAttributes() {
        for (final Attribute attribute : theElement.GetAttributesEx()) {
            listTaggedValues(attribute, " (Ex) ");
        }
        for (final Attribute attribute : theElement.GetAttributes()) {
            listTaggedValues(attribute, " (regular) ");
        }
    }

    private void listTaggedValues(final Attribute attribute, final String prefix) {
        System.out.println("Attribute : " + prefix + attribute.GetName());
        for (final AttributeTag attributeTag : attribute.GetTaggedValuesEx()) {
            System.out.println("Tag (Ex): " + attributeTag.GetName() + " : [" + attributeTag.GetValue() + "]");
        }
        for (final AttributeTag attributeTag : attribute.GetTaggedValues()) {
            System.out.println("Tag : " + attributeTag.GetName() + " : [" + attributeTag.GetValue() + "]");
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


    public Collection<TaggedValue> getTaggedValuesEx() {
        return theElement.GetTaggedValuesEx();
    }

    @Override
    public String toString() {
        return getType() +
                ": " + getName();
    }
}

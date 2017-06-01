package no.eatools.diagramgen.repository;

import org.sparx.Collection;
import org.sparx.Connector;
import org.sparx.ConnectorConstraint;
import org.sparx.ConnectorEnd;
import org.sparx.ConnectorTag;
import org.sparx.CustomProperty;
import org.sparx.Element;
import org.sparx.ObjectType;
import org.sparx.Properties;

/**
 * @author ohs
 */
public class EaConnector {
    private final Connector connector;

    public EaConnector(Connector connector) {
        this.connector = connector;
    }

    public String getAlias() {
        return connector.GetAlias();
    }

    public ConnectorEnd getClientEnd() {
        return connector.GetClientEnd();
    }

    public int getClientID() {
        return connector.GetClientID();
    }

    public int getColor() {
        return connector.GetColor();
    }

    public String getConnectorGUID() {
        return connector.GetConnectorGUID();
    }

    public int getConnectorID() {
        return connector.GetConnectorID();
    }

    public Collection<ConnectorConstraint> getConstraints() {
        return connector.GetConstraints();
    }

    public Collection<Element> getConveyedItems() {
        return connector.GetConveyedItems();
    }

    public Collection<CustomProperty> getCustomProperties() {
        return connector.GetCustomProperties();
    }

    public int getDiagramID() {
        return connector.GetDiagramID();
    }

    public String getDirection() {
        return connector.GetDirection();
    }

    public int getEndPointX() {
        return connector.GetEndPointX();
    }

    public int getEndPointY() {
        return connector.GetEndPointY();
    }

    public String getEventFlags() {
        return connector.GetEventFlags();
    }

    public boolean getIsLeaf() {
        return connector.GetIsLeaf();
    }

    public boolean getIsRoot() {
        return connector.GetIsRoot();
    }

    public boolean getIsSpec() {
        return connector.GetIsSpec();
    }

    public String getLastError() {
        return connector.GetLastError();
    }

    public String getMetaType() {
        return connector.GetMetaType();
    }

    public String getName() {
        return connector.GetName();
    }

    public String getNotes() {
        return connector.GetNotes();
    }

    public ObjectType getObjectType() {
        return connector.GetObjectType();
    }

    public Properties getProperties() {
        return connector.GetProperties();
    }

    public int getRouteStyle() {
        return connector.GetRouteStyle();
    }

    public int getSequenceNo() {
        return connector.GetSequenceNo();
    }

    public int getStartPointX() {
        return connector.GetStartPointX();
    }

    public int getStartPointY() {
        return connector.GetStartPointY();
    }

    public String getStateFlags() {
        return connector.GetStateFlags();
    }

    public String getStereotype() {
        return connector.GetStereotype();
    }

    public String getStereotypeEx() {
        return connector.GetStereotypeEx();
    }

    public String getStyleEx() {
        return connector.GetStyleEx();
    }

    public String getSubtype() {
        return connector.GetSubtype();
    }

    public ConnectorEnd getSupplierEnd() {
        return connector.GetSupplierEnd();
    }

    public int getSupplierID() {
        return connector.GetSupplierID();
    }

    public Collection<ConnectorTag> getTaggedValues() {
        return connector.GetTaggedValues();
    }

    public Collection getTemplateBindings() {
        return connector.GetTemplateBindings();
    }

    public String getTransitionAction() {
        return connector.GetTransitionAction();
    }

    public String getTransitionEvent() {
        return connector.GetTransitionEvent();
    }

    public String getTransitionGuard() {
        return connector.GetTransitionGuard();
    }

    public String getType() {
        return connector.GetType();
    }

    public String getVirtualInheritance() {
        return connector.GetVirtualInheritance();
    }

    public int getWidth() {
        return connector.GetWidth();
    }

}

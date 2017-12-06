package no.eatools.diagramgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Collection;
import org.sparx.Connector;
import org.sparx.ConnectorConstraint;
import org.sparx.ConnectorEnd;
import org.sparx.ConnectorTag;
import org.sparx.CustomProperty;
import org.sparx.Element;
import org.sparx.ObjectType;
import org.sparx.Properties;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * @author ohs
 */
public class EaConnector {
    private static final transient Logger LOG = LoggerFactory.getLogger(EaConnector.class);

    private final Connector me;
    private final EaRepo repos;
    private final EaMetaType eaMetaType;

    public EaConnector(final EaRepo repos, final Connector conn) {
        me = conn;
        this.repos = repos;
        this.eaMetaType = EaMetaType.fromString(me.GetMetaType());
    }

//    public EaConnector(EaRepo repos, EaElement src, EaElement dest, DDEntry.Association association) {
//        this.repos = repos;
//
//        Element from = src.get
//
//        for (final Connector c : to.GetConnectors()) {
//            LOG.debug(describe());
//            if (c.GetName()
//                 .equals(name)) {
//                if ((c.GetSupplierID() == to.GetElementID()) && (c.GetClientID() == from.GetElementID())) {
//                    return c;
//                }
//            }
//        }
//
//        final Connector c = to.GetConnectors()
//                              .AddNew(name, ASSOCIATION.toString());
//        c.SetSupplierID(to.GetElementID());
//        if (!c.Update()) {
//            LOG.error("Unable to update connector to: " + to.GetName());
//            return null;
//        }
//        to.GetConnectors()
//          .Refresh();
//
//        c.SetClientID(from.GetElementID());
//        if (!c.Update()) {
//            LOG.error("Unable to update connector from: " + from.GetName());
//            return null;
//        }
//        from.GetConnectors()
//            .Refresh();
//
//        c.SetDirection(EaLinkDirection.SOURCE_DESTINATION.toString());
//        c.Update();
//
//        from.Update();
//        to.Update();
//
//        this.eaMetaType = EaMetaType.fromString(me.GetMetaType());
//
//        return c;
//    }

    public static boolean equals(final Connector connector, final no.bouvet.ohs.ea.dd.Association association) {
        boolean isEqual = equalsIgnoreCase(trimToEmpty(connector.GetName()), trimToEmpty(association.getName()))
                && (equalsIgnoreCase(trimToEmpty(connector.GetStereotype()), trimToEmpty(association.getStereotypes()))
                && (EaMetaType.fromString(connector.GetMetaType()) == EaMetaType.fromString(association.getType())))
                && (equalsIgnoreCase(trimToEmpty(connector.GetSupplierEnd()
                                                          .GetRole()), trimToEmpty(association.getTargetRole())));
        LOG.debug("Connector and association equal? {} Connector: \n" +
                          "name: [{}]\n " +
                          "stereotype: [{}]\n" +
                          "metaType: [{}]\n" +
                          "supplierRole: [{}]\n" +
                          "Association: [{}]", isEqual, connector.GetName(), connector.GetStereotype(), connector.GetMetaType(), connector.GetSupplierEnd()
                                                                                                                                          .GetRole(), association);
        return isEqual;
    }

    public String getAlias() {
        return me.GetAlias();
    }

    public Element getAssociationClass() {
        return me.GetAssociationClass();
    }

    public ConnectorEnd getClientEnd() {
        return me.GetClientEnd();
    }

    public int getClientID() {
        return me.GetClientID();
    }

    public int getColor() {
        return me.GetColor();
    }

    public String getConnectorGUID() {
        return me.GetConnectorGUID();
    }

    public int getConnectorID() {
        return me.GetConnectorID();
    }

    public Collection<ConnectorConstraint> getConstraints() {
        return me.GetConstraints();
    }

    public Collection<Element> getConveyedItems() {
        return me.GetConveyedItems();
    }

    public Collection<CustomProperty> getCustomProperties() {
        return me.GetCustomProperties();
    }

    public int getDiagramID() {
        return me.GetDiagramID();
    }

    public String getDirection() {
        return me.GetDirection();
    }

    public int getEndPointX() {
        return me.GetEndPointX();
    }

    public int getEndPointY() {
        return me.GetEndPointY();
    }

    public String getEventFlags() {
        return me.GetEventFlags();
    }

    public String getForeignKeyInformation() {
        return me.GetForeignKeyInformation();
    }

    public String getFQStereotype() {
        return me.GetFQStereotype();
    }

    public boolean getIsLeaf() {
        return me.GetIsLeaf();
    }

    public boolean getIsRoot() {
        return me.GetIsRoot();
    }

    public boolean getIsSpec() {
        return me.GetIsSpec();
    }

    public String getLastError() {
        return me.GetLastError();
    }

    public String getMessageArguments() {
        return me.GetMessageArguments();
    }

    public String getMetaType() {
        return me.GetMetaType();
    }

    public String getName() {
        return me.GetName();
    }

    public String getNotes() {
        return me.GetNotes();
    }

    public ObjectType getObjectType() {
        return me.GetObjectType();
    }

    public Properties getProperties() {
        return me.GetProperties();
    }

    public String getReturnValueAlias() {
        return me.GetReturnValueAlias();
    }

    public int getRouteStyle() {
        return me.GetRouteStyle();
    }

    public int getSequenceNo() {
        return me.GetSequenceNo();
    }

    public int getStartPointX() {
        return me.GetStartPointX();
    }

    public int getStartPointY() {
        return me.GetStartPointY();
    }

    public String getStateFlags() {
        return me.GetStateFlags();
    }

    public String getStereotype() {
        return me.GetStereotype();
    }

    public String getStereotypeEx() {
        return me.GetStereotypeEx();
    }

    public String getStyleEx() {
        return me.GetStyleEx();
    }

    public String getSubtype() {
        return me.GetSubtype();
    }

    public ConnectorEnd getSupplierEnd() {
        return me.GetSupplierEnd();
    }

    public int getSupplierID() {
        return me.GetSupplierID();
    }

    public Collection<ConnectorTag> getTaggedValues() {
        return me.GetTaggedValues();
    }

    public Collection getTemplateBindings() {
        return me.GetTemplateBindings();
    }

    public String getTransitionAction() {
        return me.GetTransitionAction();
    }

    public String getTransitionEvent() {
        return me.GetTransitionEvent();
    }

    public String getTransitionGuard() {
        return me.GetTransitionGuard();
    }

    public String getType() {
        return me.GetType();
    }

    public String getVirtualInheritance() {
        return me.GetVirtualInheritance();
    }

    public int getWidth() {
        return me.GetWidth();
    }
}

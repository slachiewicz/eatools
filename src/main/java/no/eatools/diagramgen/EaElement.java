package no.eatools.diagramgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Attribute;
import org.sparx.AttributeTag;
import org.sparx.Collection;
import org.sparx.Connector;
import org.sparx.Diagram;
import org.sparx.Element;
import org.sparx.TaggedValue;

import static no.eatools.diagramgen.EaType.*;

/**
 * @author ohs
 */
public class EaElement {
    private static final transient Logger LOG = LoggerFactory.getLogger(EaElement.class);
    public static final String IMAGE_URL_TAG = "imageUrl";
    private final Element theElement;
    final EaRepo repos;
    final EaMetaType eaMetaType;

    public EaElement(final Element theElement, final EaRepo repos) {
        this.theElement = theElement;
        this.repos = repos;
        eaMetaType = EaMetaType.fromString(theElement.GetMetaType());
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
        final EaType type = getType();
        if (type == Note || type == Text) {
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

    public List<EaElement> findConnectedElements() {
        final List<EaElement> result = new ArrayList<>();
        for (final Connector connector : getConnectors()) {
            result.add(findConnectedElement(connector));
        }
        return result;
    }

    public int getPackageID() {
        return theElement.GetPackageID();
    }

    public String getPackageName() {
        return repos.findPackageByID(getPackageID())
                    .GetName();
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

    /**
     * Action
     * Action                                     ActionPin
     * ActionPin                                  Activity
     * Activity                                   ActivityParameter
     * ActivityPartition                          ActivityPartition
     * Actor                                      ActivityRegion
     * Artifact                                   Actor
     * Boundary                                   Artifact
     * Class                                      Association
     * Collaboration                              Boundary
     * Component                                  CentralBufferNode
     * Constraint                                 Change
     * DataType                                   Class
     * Decision                                   Collaboration
     * Device                                     CollaborationOccurrence
     * Entity                                     Comment
     * Enumeration                                Component
     * Event                                      ConditionalNode
     * ExecutionEnvironment                       Constraint
     * Interaction                                DataStore
     * InteractionFragment                        DataType
     * InteractionOccurrence                      Decision
     * Interface                                  DeploymentSpecification
     * Issue                                      Device
     * LoopNode                                   DiagramFrame
     * MessageEndpoint                            Entity
     * Node                                       EntryPoint
     * Note                                       Enumeration
     * Object                                     Event
     * ObjectNode                                 ExceptionHandler
     * Package                                    ExecutionEnvironment
     * Port                                       ExitPoint
     * PrimitiveType                              ExpansionNode
     * ProvidedInterface                          ExpansionRegion
     * RequiredInterface                          Feature
     * Requirement                                GUIElement
     * Sequence                                   InformationItem
     * State                                      Interaction
     * StateNode                                  InteractionFragment
     * StructuredActivityNode                     InteractionOccurrence
     * Synchronization                            InteractionState
     * Text                                       Interface
     * TimeLine                                   InterruptibleActivityRegion
     * Trigger                                    Issue
     * UMLDiagram                                 Label
     * UseCase                                    LoopNode
     * MergeNode
     * MessageEndpoint
     * Node
     * Note
     * Object
     * ObjectNode
     * Package
     * Parameter
     * Part
     * Port
     * PrimitiveType
     * ProtocolStateMachine
     * ProvidedInterface
     * Region
     * Report
     * RequiredInterface
     * Requirement
     * Risk
     * Screen
     * Sequence
     * Signal
     * State
     * StateMachine
     * StateNode
     * Synchronization
     * Task
     * Text
     * TimeLine
     * Trigger
     * UMLDiagram
     * UseCase
     * User
     *
     * @return
     */
    public EaType getType() {
        return EaType.fromString(theElement.GetType());
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

    public String getClassifierType() {
        return theElement.GetClassifierType();
    }

    public String getAuthor() {
        return theElement.GetAuthor();
    }

    /**
     * @return The EA MetaType transformed to internal enumeration, may differ from unfiltered type for esoteric types not yet discovered.
     */
    public EaMetaType getMetaType() {
        return eaMetaType;
    }

    public int getId() {
        return theElement.GetElementID();
    }

    public EaDiagram findDiagram(final String diagramName) {
        for (final Diagram diagram : theElement.GetDiagrams()) {
            if (diagram.GetName()
                       .equals(diagramName)) {
                final EaDiagram eaDiagram = new EaDiagram(repos, diagram, repos.getPackagePath(repos.findPackageByID(theElement.GetPackageID())));
                LOG.info("Found element diagram for {}: {}:{}", getName(), eaDiagram.getPathname(), eaDiagram.getName());
                return eaDiagram;
            }
        }
        return null;
    }

    /**
     * @return the unfiltered EA MetaType as a String
     */
    public String getEaMetaType() {
        return theElement.GetMetaType();
    }

    public void setImageUrl(final String imageUrl) {
        if (StringUtils.isNotBlank(imageUrl)) {
            updateImageTag(imageUrl);
            updateTaggedValue(IMAGE_URL_TAG, imageUrl);
            LOG.info("Added auto image url as tag [{}]", imageUrl);
        }
    }

    private void updateTaggedValue(final String tageName, final String tagValue) {
        final Collection<TaggedValue> taggedValues = theElement.GetTaggedValuesEx();
        boolean hasOld = false;
        for (final TaggedValue taggedValue : taggedValues) {
            if(tageName.equalsIgnoreCase(taggedValue.GetName())) {
                hasOld = true;
                taggedValue.SetValue(tageName);
                taggedValue.Update();
                taggedValues.Refresh();
                theElement.Refresh();
                theElement.Update();
            }
        }
        if(! hasOld) {
            TaggedValue taggedValue = taggedValues.AddNew(tageName, tagValue);
            taggedValue.Update();
            taggedValues.Refresh();
            theElement.Refresh();
            theElement.Update();
        }
    }

    private void updateImageTag(final String imageUrl) {
        final String oldTag = StringUtils.trimToEmpty(theElement.GetTag()).replaceAll(IMAGE_URL_TAG + ".*\\.png", "");
        final String newTag = StringUtils.trimToEmpty(oldTag) + " " + IMAGE_URL_TAG + "=" + imageUrl;
        theElement.SetTag(newTag);
        theElement.Refresh();
        theElement.Update();
    }
}

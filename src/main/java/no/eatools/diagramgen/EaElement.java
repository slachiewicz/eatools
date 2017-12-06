package no.eatools.diagramgen;

import java.lang.reflect.InvocationTargetException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.bouvet.ohs.ea.dd.Operation;
import no.bouvet.ohs.ea.dd.TagValue;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.*;
import static no.eatools.diagramgen.EaType.*;
import static no.eatools.util.EaApplicationProperties.EA_SERVER_TIMEZONE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

/**
 * @author ohs
 */
public class EaElement {
    private static final transient Logger LOG = LoggerFactory.getLogger(EaElement.class);
    public static final String IMAGE_URL_TAG = "imageUrl";
    private final Element theElement;
    private final EaRepo repos;
    private final EaMetaType eaMetaType;
    private final Set<Operation> operationSet = new HashSet<>();

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
        // Used to track down seemingly missing connectors between elements
        //        LOG.debug("Connector from [{}] via [{}] ", theElement.GetName(), BStringUtils.describe(new EaConnector(connector)));
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

    public String getHierarchicalPackageName() {
        return repos.findEaPackageByID(getPackageID())
                .getHierarchicalName();
    }

    public Collection<Connector> getConnectors() {
        return theElement.GetConnectors();
    }

    public String listProperties() {
        final StringBuilder result = new StringBuilder();
        result.append(" Element " + theElement.GetName());
        result.append("\n");
        for (final EaElement eaElement : findParents()) {
            result.append(" Element ")
                    .append(getName())
                    .append(" has parent ")
                    .append(eaElement.getName());
        }
        result.append("\n");
        result.append(listAttributes());
        result.append(listTaggedValues());
        return result.toString();
    }

    public String listAttributes() {
        final StringBuilder result = new StringBuilder();
        for (final Attribute attribute : theElement.GetAttributesEx()) {
            result.append(listAttributeTaggedValues(attribute, " (Ex) "));
        }
        result.append("\n");
        for (final Attribute attribute : theElement.GetAttributes()) {
            result.append(listAttributeTaggedValues(attribute, " (regular) "));
        }
        result.append("\n");
        return result.toString();
    }

    public String listAttributeTaggedValues(final Attribute attribute, final String prefix) {
        final StringBuilder result = new StringBuilder();
        result.append("Attribute : " + prefix + attribute.GetName());
        for (final AttributeTag attributeTag : attribute.GetTaggedValuesEx()) {
            result.append(" Tag (Ex): " + attributeTag.GetName() + " : [" + attributeTag.GetValue() + "]");
        }
        result.append("\n");
        for (final AttributeTag attributeTag : attribute.GetTaggedValues()) {
            result.append(" Tag : " + attributeTag.GetName() + " : [" + attributeTag.GetValue() + "]");
        }
        result.append("\n");
        return result.toString();
    }

    public String listTaggedValues() {
        final StringBuilder result = new StringBuilder();
        for (final TaggedValue taggedValue : theElement.GetTaggedValuesEx()) {
            result.append(" Tag (Ex): " + taggedValue.GetName() + " : [" + taggedValue.GetValue() + "]");
        }
        for (final TaggedValue taggedValue : theElement.GetTaggedValues()) {
            result.append(" Tag : " + taggedValue.GetName() + " : [" + taggedValue.GetValue() + "]");
        }
        return result.toString();
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
        return fromString(theElement.GetType());
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
        if (isNotBlank(imageUrl)) {
            updateImageTag(imageUrl);
            updateTaggedValue(new TagValue(IMAGE_URL_TAG, imageUrl));
            LOG.info("Added auto image url as tag [{}]", imageUrl);
        }
    }

    /**
     * TODO taggedValuesEx vs TaggedValues?
     *
     * @param tagValue
     */
    public void updateTaggedValue(final TagValue tagValue) {
        LOG.debug("**************** Before: {}", listProperties());
        String tagName = tagValue.getKey();
        String value = tagValue.getValue();
        LOG.debug("[{}] Looking for [{}]:[{}]", getName(), tagName, value);
        final String trimmedName = trimToEmpty(tagName);
        final Collection<TaggedValue> taggedValues = theElement.GetTaggedValues();
        // EA does not enforce uniqueness on tag keys
        final List<Short> indexesToRemove = new ArrayList<>();
        short i = 0;
        for (final TaggedValue taggedValue : taggedValues) {
            LOG.debug("[{}] has tagged value [{}]:[{}]", getName(), taggedValue.GetName(), taggedValue.GetValue());
            if (trimmedName.equalsIgnoreCase(taggedValue.GetName()
                    .trim())) {
                indexesToRemove.add(i);
            }
            ++i;
        }
        LOG.debug("Found [{}] tags with tagName [{}]", indexesToRemove.size(), tagName);
        for (final Short index : indexesToRemove) {
            LOG.debug("Deleting [{}]", index);
            taggedValues.Delete(index);
            theElement.Update();
        }
        taggedValues.Refresh();
        LOG.debug("Now: [{}]", taggedValues);

        final TaggedValue taggedValue = taggedValues.AddNew(trimmedName, value);
        taggedValue.Update();
        taggedValues.Refresh();
        theElement.GetTaggedValuesEx()
                .Refresh();
        theElement.Update();
        theElement.Refresh();
        LOG.debug("Added [{}]:[{}]", trimmedName, taggedValue);
        LOG.debug("************* After: {}", listProperties());
    }

    public void updateAssociation(final no.bouvet.ohs.ea.dd.Association association) {
        final Collection<Connector> connectors = theElement.GetConnectors();

        final String targetPackage = association.getTargetPackage();
        final String target = association.getTarget();
        final List<EaElement> targetElements = repos.findElementsInPackage(targetPackage, target);
        if (targetElements.size() != 1) {
            LOG.warn("Unable to find unique target for connector from [{}] to [{}]:[{}]. Found : {}", getName(), targetPackage, target, targetElements);
            return;
        }
        final EaElement targetElement = targetElements.get(0);
        for (final Connector connector : connectors) {
            try {
                LOG.debug("Connector [{}] ", BeanUtils.describe(new EaConnector(repos, connector))
                        .toString()
                        .replaceAll(",", "\n"));
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            final int otherId = connector.GetSupplierID();

//            final Element other = repos.findElementByID(otherId);
            if (otherId == targetElement.getId()
                    && EaConnector.equals(connector, association)) {
                LOG.info("Updating association");
                updateConnector(association, connector);
                return;
            }
        }
        final Connector connector = connectors.AddNew("", EaMetaType.fromString(association.getType())
                .toEaString());
        connector.SetClientID(theElement.GetElementID());
        connector.SetSupplierID(targetElement.getId());
        updateConnector(association, connector);
        LOG.info("Created association [{}]", association);
    }

    public void createOrUpdateOperation(final no.bouvet.ohs.ea.dd.Operation operation, final boolean overwriteOps) {
        final Collection<Method> methods = theElement.GetMethods();
        if (overwriteOps) {
            deleteMethodsMatching(methods, operation.getName());
        }
        if (operationSet.isEmpty()) {
            updateOperationsCache(methods);
        }
        LOG.debug("Current Methods: \n {}", operationSet.stream()
                .map(no.bouvet.ohs.ea.dd.Operation::getSignature)
                .collect(Collectors.joining("\n")));
        LOG.info("Updating operation ? [{}]", operation.getName());
        if (hasMethodMatching(methods, operation)) {
            LOG.info("Method with same signature already present, not adding or changing, [{}]", operation.getSignature());
        } else {
            final EaMethod eaMethod = addMethod(operation.getName(), operation.getReturnType(), methods, operation.getParameters());
            LOG.info("Added method [{}] ", operation.getSignature());
        }
    }

    private void deleteMethodsMatching(final Collection<Method> methods, final String name) {
        final List<Short> toBeDeleted = new ArrayList<>();
        short index = 0;
        for (final Method method : methods) {
            if (method.GetName()
                    .equalsIgnoreCase(name)) {
                toBeDeleted.add(index);
            }
            ++index;
        }
        for (final Short deleteAt : toBeDeleted) {
            methods.Delete(deleteAt);
            LOG.info("Deleted method [{}] from [{}]", name, getName());
        }
        operationSet.clear();
        theElement.Update();
    }

    private void updateOperationsCache(final Collection<Method> methods) {
        for (final Method method : methods) {
            operationSet.add(operationFromMethod(method));
        }
    }

    private no.bouvet.ohs.ea.dd.Operation operationFromMethod(final Method method) {
        final no.bouvet.ohs.ea.dd.Operation op = new no.bouvet.ohs.ea.dd.Operation(method.GetName(), method.GetReturnType());
        final List<no.bouvet.ohs.ea.dd.Parameter> params = op.getParameters();
        for (final org.sparx.Parameter parameter : method.GetParameters()) {
            params.add(new no.bouvet.ohs.ea.dd.Parameter(parameter.GetName(), parameter.GetType()));
        }
        LOG.debug("Operation: [{}]", op);
        return op;
    }

    private boolean hasMethodMatching(final Collection<Method> methods, final no.bouvet.ohs.ea.dd.Operation operation) {
        if (operationSet.isEmpty()) {
            updateOperationsCache(methods);
        }
        return operationSet.contains(operation);
//        for (Method method : methods) {
//            no.bouvet.ohs.ea.dd.Operation op = new no.bouvet.ohs.ea.dd.Operation();
//            op.getParameters().add(new no.bouvet.ohs.ea.dd.Parameter())
//            if (method.GetName()
//                      .equalsIgnoreCase(operation.getName())) {
//                if (!operation.getReturnType()
//                              .equalsIgnoreCase(method.GetReturnType())) {
//                    return false;
//                        return false;
//                    }
//                }
//                return true;
//            }
//        }
//        return false;
    }

    private void updateConnector(final no.bouvet.ohs.ea.dd.Association association, final Connector connector) {
        if (isNotBlank(association.getStereotypes())) {
            connector.SetStereotype(association.getStereotypes());
        }
        if (isNotBlank(association.getTargetRole())) {
            connector.GetSupplierEnd()
                    .SetRole(association.getTargetRole());
        }
        connector.Update();
        theElement.Update();
    }


    private void updateImageTag(final String imageUrl) {
        final String oldTag = trimToEmpty(theElement.GetTag())
                .replaceAll(IMAGE_URL_TAG + ".*\\.png", "");
        final String newTag = trimToEmpty(oldTag) + " " + IMAGE_URL_TAG + "=" + imageUrl;
        theElement.SetTag(newTag);
        theElement.Refresh();
        theElement.Update();
    }

    public EaMethod addMethod(final String methodName, final String returnType, final Collection<Method> methods, final List<no.bouvet.ohs.ea.dd.Parameter> parameters) {
        final Method method = methods.AddNew(methodName, returnType);
        method.Update();
        methods.Refresh();
        final EaMethod eaMethod = new EaMethod(this, method);
        for (final no.bouvet.ohs.ea.dd.Parameter parameter : parameters) {
            eaMethod.addParameter(parameter.getName(), parameter.getType());
        }
        theElement.Update();
        operationSet.add(operationFromMethod(eaMethod.getTheMethod()));
        return eaMethod;
    }

    public boolean removeMethod(final String methodName, final String returnType) {
        short index = 0;
        for (final Method method : theElement.GetMethods()) {
            if (method.GetName()
                    .equals(methodName) && method.GetReturnType()
                    .equals(returnType)) {
                theElement.GetMethods()
                        .Delete(index);
                theElement.GetMethods()
                        .Refresh();
                operationSet.remove(operationFromMethod(method));
                return true;
            }
            ++index;
        }
        return false;
    }

    public ZonedDateTime getCreated() {
        final ZoneId zoneId;
        try {
            zoneId = ZoneId.of(EA_SERVER_TIMEZONE.value());
        } catch (final DateTimeException e) {
            LOG.error("No valid timeZone for [{}]:[{}]", EA_SERVER_TIMEZONE, EA_SERVER_TIMEZONE.value());
            return null;
        }

        final Instant instant;
        try {
            instant = theElement.GetCreated()
                    .toInstant();
            return ZonedDateTime.ofInstant(instant, zoneId);
        } catch (final Exception e) {
            LOG.error("Unable to convert [{}] to a ZonedDateTime in element [{}]", theElement.GetCreated(), toString());
            return null;
        }
    }
}

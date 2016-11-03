package no.eatools.diagramgen;

/**
 * @author ohs,
 */

import no.bouvet.ohs.jops.Enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings("squid:S00115")
public enum EaType {
    Action,
    ActionPin,
    Activity,
    ActivityParameter,
    ActivityPartition,
    ActivityRegion,
    Actor,
    Artifact,
    Association,
    Boundary,
    CentralBufferNode,
    Change,
    Class,
    Collaboration,
    CollaborationOccurrence,
    Comment,
    Component,
    ConditionalNode,
    Constraint,
    DataStore,
    DataType,
    Decision,
    DeploymentSpecification,
    Device,
    DiagramFrame,
    Entity,
    EntryPoint,
    Enumeration,
    Event,
    ExceptionHandler,
    ExecutionEnvironment,
    ExitPoint,
    ExpansionNode,
    ExpansionRegion,
    Feature,
    GUIElement,
    InformationItem,
    Interaction,
    InteractionFragment,
    InteractionOccurrence,
    InteractionState,
    Interface,
    InterruptibleActivityRegion,
    Issue,
    Label,
    LoopNode,
    MergeNode,
    MessageEndpoint,
    Node,
    Note,
    Object,
    ObjectNode,
    Package,
    Parameter,
    Part,
    Port,
    PrimitiveType,
    ProtocolStateMachine,
    ProvidedInterface,
    Region,
    Report,
    RequiredInterface,
    Requirement,
    Risk,
    Screen,
    Sequence,
    Signal,
    State,
    StateMachine,
    StateNode,
    Synchronization,
    Task,
    Text,
    TimeLine,
    Trigger,
    UMLDiagram,
    UseCase,
    User,
    NULL;

    private static final transient Logger LOG = LoggerFactory.getLogger(EaType.class);

    /**
     * Simple factory for getting safe EaMetaType.
     *
     * @param type
     * @return never null
     */
    public static EaType fromString(final String type) {
        LOG.debug("Looking up type for [{}]", type);
        return Enums.valueOf(EaType.class, type, NULL);
    }
}

package no.eatools;/*
 * Script Name: EAConstants-JScript.js
 * Author: Sparx Systems
 * Purpose: Provides constant values for the Enterprise Architect automation API.
 * Date: 2010-05-31
 */

// =================================================================================================
// ObjectType
// See http://www.sparxsystems.com/uml_tool_guide/sdk_for_enterprise_architect/objecttypeenum.htm
// =================================================================================================
@SuppressWarnings("squid:S00115")
public class EAConstants {
    public static final int otNone = 0;
    public static final int otProject = 1;
    public static final int otRepository = 2;
    public static final int otCollection = 3;
    public static final int otElement = 4;
    public static final int otPackage = 5;
    public static final int otModel = 6;
    public static final int otConnector = 7;
    public static final int otDiagram = 8;
    public static final int otRequirement = 9;
    public static final int otScenario = 10;
    public static final int otConstraint = 11;
    public static final int otTaggedValue = 12;
    public static final int otFile = 13;
    public static final int otEffort = 14;
    public static final int otMetric = 15;
    public static final int otIssue = 16;
    public static final int otRisk = 17;
    public static final int otTest = 18;
    public static final int otDiagramObject = 19;
    public static final int otDiagramLink = 20;
    public static final int otResource = 21;
    public static final int otConnectorEnd = 22;
    public static final int otAttribute = 23;
    public static final int otMethod = 24;
    public static final int otParameter = 25;
    public static final int otClient = 26;
    public static final int otAuthor = 27;
    public static final int otDatatype = 28;
    public static final int otStereotype = 29;
    public static final int otTask = 30;
    public static final int otTerm = 31;
    public static final int otProjectIssues = 32;
    public static final int otAttributeConstraint = 33;
    public static final int otAttributeTag = 34;
    public static final int otMethodConstraint = 35;
    public static final int otMethodTag = 36;
    public static final int otConnectorConstraint = 37;
    public static final int otConnectorTag = 38;
    public static final int otProjectResource = 39;
    public static final int otReference = 40;
    public static final int otRoleTag = 41;
    public static final int otCustomProperty = 42;
    public static final int otPartition = 43;
    public static final int otTransition = 44;
    public static final int otEventProperty = 45;
    public static final int otEventProperties = 46;
    public static final int otPropertyType = 47;
    public static final int otProperties = 48;
    public static final int otProperty = 49;
    public static final int otSwimlaneDef = 50;
    public static final int otSwimlanes = 51;
    public static final int otSwimlane = 52;
    public static final int otModelWatcher = 53;
    public static final int otScenarioStep = 54;
    public static final int otScenarioExtension = 55;
    public static final int otParamTag = 56;
    public static final int otProjectRole = 57;
    public static final int otDocumentGenerator = 58;
    public static final int otMailInterface = 59;

    // =================================================================================================
// MDGMenus
// See http://www.sparxsystems.com/uml_tool_guide/sdk_for_enterprise_architect/mdgmenusenum.htm
// =================================================================================================
    public static final int mgMerge = 1;
    public static final int mgBuildProject = 2;
    public static final int mgRun = 4;

    // =================================================================================================
// EnumXMIType
// See http://www.sparxsystems.com/uml_tool_guide/sdk_for_enterprise_architect/xmitypeenum.htm
// =================================================================================================
    public static final int xmiEADefault = 0;
    public static final int xmiRoseDefault = 1;
    public static final int xmiEA10 = 2;
    public static final int xmiEA11 = 3;
    public static final int xmiEA12 = 4;
    public static final int xmiRose10 = 5;
    public static final int xmiRose11 = 6;
    public static final int xmiRose12 = 7;
    public static final int xmiMOF13 = 8;
    public static final int xmiMOF14 = 9;
    public static final int xmiEA20 = 10;
    public static final int xmiEA21 = 11;
    public static final int xmiEA211 = 12;
    public static final int xmiEA212 = 13;
    public static final int xmiEA22 = 14;
    public static final int xmiEA23 = 15;
    public static final int xmiEA24 = 16;
    public static final int xmiEA241 = 17;
    public static final int xmiEA242 = 18;
    public static final int xmiEcore = 19;
    public static final int xmiBPMN20 = 20;
    public static final int xmiXPDL22 = 21;

    // =================================================================================================
// EnumMVErrorType
// See http://www.sparxsystems.com/uml_tool_guide/sdk_for_enterprise_architect/project_2.htm
// =================================================================================================
    public static final int mvError = 0;
    public static final int mvWarning = 1;
    public static final int mvInformation = 2;
    public static final int mvErrorCritical = 3;

    // =================================================================================================
// CreateModelType
// See http://www.sparxsystems.com/uml_tool_guide/sdk_for_enterprise_architect/createmodelitype_enum.htm
// =================================================================================================
    public static final int cmEAPFromBase = 0;
    public static final int cmEAPFromSQLRepository = 1;

    // =================================================================================================
// EAEditionTypes
// See http://www.sparxsystems.com/uml_tool_guide/sdk_for_enterprise_architect/eaeditiontypes_enum.htm
// =================================================================================================
    public static final int piLite = -1;
    public static final int piDesktop = 0;
    public static final int piProfessional = 1;
    public static final int piCorporate = 2;
    public static final int piBusiness = 3;
    public static final int piSystemEng = 4;
    public static final int piUltimate = 5;

    // =================================================================================================
// ScenarioStepType
// See http://www.sparxsystems.com/uml_tool_guide/sdk_for_enterprise_architect/scenariosteptype.htm
// =================================================================================================
    public static final int stSystem = 0;
    public static final int stActor = 1;

    // =================================================================================================
// ExportPackageXMIFlag
// See http://www.sparxsystems.com/uml_tool_guide/sdk_for_enterprise_architect/exportpackagexmiflag.htm
// =================================================================================================
    public static final int epSaveToStub = 1;
    public static final int epExcludeEAExtensions = 2;

    // =================================================================================================
// CreateBaselineFlag
// See http://www.sparxsystems.com/uml_tool_guide/sdk_for_enterprise_architect/createbaselineflag.htm
// =================================================================================================
    public static final int cbSaveToStub = 1;

    // =================================================================================================
// EnumScenarioDiagramType
// See http://www.sparxsystems.com/uml_tool_guide/sdk_for_enterprise_architect/project_2.htm
// =================================================================================================
    public static final int sdActivity = 0;
    public static final int sdActivityWithActivityParameter = 1;
    public static final int sdActivityWithAction = 2;
    public static final int sdActivityhWithActionPin = 3;
    public static final int sdRuleFlow = 4;
    public static final int sdState = 5;
    public static final int sdSequence = 6;
    public static final int sdRobustness = 7;

    // =================================================================================================
// EnumScenarioTestType
// See http://www.sparxsystems.com/uml_tool_guide/sdk_for_enterprise_architect/project_2.htm
// =================================================================================================
    public static final int stInternal = 0;
    public static final int stExternal = 1;
    public static final int stHorizontalTestSuite = 2;
    public static final int stVerticalTestSuite = 3;

    // =================================================================================================
// EnumCodeSection
// =================================================================================================
    public static final int cpWhole = 0;
    public static final int cpNotes = 1;
    public static final int cpText = 2;

    // =================================================================================================
// EnumRelationSetType
// See http://www.sparxsystems.com/uml_tool_guide/sdk_for_enterprise_architect/enumrelationsettypeenum.htm
// =================================================================================================
    public static final int rsGeneralizeStart = 0;
    public static final int rsGeneralizeEnd = 1;
    public static final int rsRealizeStart = 2;
    public static final int rsRealizeEnd = 3;
    public static final int rsDependStart = 4;
    public static final int rsDependEnd = 5;
    public static final int rsParents = 6;

    // =================================================================================================
// EnumCodeElementType
// =================================================================================================
    public static final int ctInvalid = 0;
    public static final int ctNamespace = 1;
    public static final int ctClass = 2;
    public static final int ctAttribute = 3;
    public static final int ctOperation = 4;
    public static final int ctOperationParam = 5;

    // =================================================================================================
// PropType
// See http://www.sparxsystems.com/uml_tool_guide/sdk_for_enterprise_architect/proptype_enum.htm
// =================================================================================================
    public static final int ptString = 0;
    public static final int ptInteger = 1;
    public static final int ptFloatingPoint = 2;
    public static final int ptBoolean = 3;
    public static final int ptEnum = 4;
    public static final int ptArray = 5;

    // =================================================================================================
// SwimlaneOrientationType
// =================================================================================================
    public static final int soVertical = 0;
    public static final int soHorizontal = 1;

    // =================================================================================================
// ReloadType
// See http://www.sparxsystems.com/uml_tool_guide/sdk_for_enterprise_architect/reloadtype_enum.htm
// =================================================================================================
    public static final int rtNone = 0;
    public static final int rtEntireModel = 1;
    public static final int rtPackage = 2;
    public static final int rtElement = 3;

    // =================================================================================================
// ConstLayoutStyles
// See http://www.sparxsystems.com/uml_tool_guide/sdk_for_enterprise_architect/constlayoutstylesenum.htm
// =================================================================================================
    public static final int lsDiagramDefault = 0x00000000;
    public static final int lsProgramDefault = 0xFFFFFFFF;
    public static final int lsCycleRemoveGreedy = 0x80000000;
    public static final int lsCycleRemoveDFS = 0x40000000;
    public static final int lsLayeringLongestPathSink = 0x30000000;
    public static final int lsLayeringLongestPathSource = 0x20000000;
    public static final int lsLayeringOptimalLinkLength = 0x10000000;
    public static final int lsInitializeNaive = 0x08000000;
    public static final int lsInitializeDFSOut = 0x04000000;
    public static final int lsInitializeDFSIn = 0x0C000000;
    public static final int lsCrossReduceAggressive = 0x02000000;
    public static final int lsLayoutDirectionUp = 0x00010000;
    public static final int lsLayoutDirectionDown = 0x00020000;
    public static final int lsLayoutDirectionLeft = 0x00040000;
    public static final int lsLayoutDirectionRight = 0x00080000;

    // =================================================================================================
// WorkFlowConstants
// =================================================================================================
    public static final int MaxWorkFlowUsers = 50;
    public static final int MaxWorkFlowItems = 100;

    // =================================================================================================
// PromptType
// =================================================================================================
    public static final int promptOK = 1;
    public static final int promptYESNO = 2;
    public static final int promptYESNOCANCEL = 3;
    public static final int promptOKCANCEL = 4;

    // =================================================================================================
// PromptResult
// =================================================================================================
    public static final int resultOK = 1;
    public static final int resultCancel = 2;
    public static final int resultYes = 3;
    public static final int resultNo = 4;

    // =================================================================================================
// WorkFlowResult
// =================================================================================================
    public static final int WorkFlowSucceeded = 1;
    public static final int WorkFlowError = 2;
    public static final int WorkFlowExists = 3;
    public static final int WorkFlowNotFound = 4;
    public static final int WorkFlowLimitReached = 5;
    public static final int WorkFlowDenied = 6;
    public static final int WorkFlowPermitted = 7;
    public static final int WorkFlowIsMember = 8;
    public static final int WorkFlowIsNotMember = 9;
    public static final int WorkFlowBadParam = 10;

    // =================================================================================================
// DocumentType
// =================================================================================================
    public static final int dtRTF = 0;
    public static final int dtHTML = 1;
    public static final int dtPDF = 2;
    public static final int dtDOCX = 3;

    // =================================================================================================
// DocumentBreak
// =================================================================================================
    public static final int breakPage = 0;
    public static final int breakSection = 1;

    // =================================================================================================
// TextAlignment
// =================================================================================================
    public static final int alignLeft = 0;
    public static final int alignCenter = 1;
    public static final int alignRight = 2;
    public static final int alignJustify = 3;

    // =================================================================================================
// MessageFlag
// =================================================================================================
    public static final int mfNone = 0;
    public static final int mfComplete = 1;
    public static final int mfPurple = 2;
    public static final int mfOrange = 3;
    public static final int mfGreen = 4;
    public static final int mfYellow = 5;
    public static final int mfBlue = 6;
    public static final int mfRed = 7;

    private EAConstants() {
    }
}

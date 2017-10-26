package no.eatools.diagramgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.eatools.diagramgen.EaAttributeName.EaAttributeType.*;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * (Diagram) Style options and other options that may be set through SetStyleEx() and SetExtendedStyle().
 * See http://www.sparxsystems.com/enterprise_architect_user_guide/10/extending_uml_models/attribute_values___stylex__pda.html
 *
 * @author ohs
 */
public enum EaAttributeName {
    // StyleEX
    AdvancedConnectorProps(StyleEx), //1; (to show connector property strings)
    AdvancedElementProps(StyleEx), //1; (to show the element property string)
    AdvancedFeatureProps(StyleEx), //1; (to show the feature property string)
    AttPkg(StyleEx), //1; (to show package visible Class members)
    DefaultLang(StyleEx), //Language; (to set the default language for the diagram; Language can be one of the built-in languages such as C++ or Java, or it can be a custom
    // language)
    HandDraw(StyleEx), //1; (to apply hand drawn mode)
    HideConnStereotype(StyleEx), //1; (to hide the connector stereotype labels)
    HideQuals(StyleEx), //0; (to show qualifiers and visibility indicators)
    SeqTopMargin(StyleEx), //50; (to set the height of the top margin on sequence diagrams)
    ShowAsList(StyleEx), //1; (to make the diagram open directly into the Diagram List)
    ShowMaint(StyleEx), //1; (to show the element Maintenance compartment)
    ShowNotes(StyleEx), //1; (to show the element Notes compartment)
    ShowOpRetType(StyleEx), //1; (to show the operation return type)
    ShowTests(StyleEx), //1; (to show the element Testing compartment)
    SuppConnectorLabels(StyleEx), //1; (to suppress all connector labels)
    SuppressBrackets(StyleEx), //1; (to suppress brackets on operations without parameters)
    TConnectorNotation(StyleEx), //Option; (where Option is one of UML 2.1, IDEF1X, or Information Engineering)
    TExplicitNavigability(StyleEx), //1; (to show non-navigable connector ends)
    VisibleAttributeDetail(StyleEx), //1; (to show attribute details on the diagram)
    Whiteboard(StyleEx), //1; (to apply whiteboard mode)
    ExcludeRTF(StyleEx),
    DocAll(StyleEx),
    SuppressFOC(StyleEx),
    MatrixActive(StyleEx),
    SwimlanesActive(StyleEx),
    KanbanActive(StyleEx),
    MatrixLineWidth(StyleEx),
    MatrixLineClr(StyleEx),
    MatrixLocked(StyleEx),
    m_bElementClassifier(StyleEx),
    ProfileData(StyleEx),
    MDGDgm(StyleEx),
    STBLDgm(StyleEx),
    PrintPageHeadFoot(StyleEx),
    SuppressedCompartments(StyleEx),
    Theme(StyleEx),
    SaveTag(StyleEx),

    // Extended
    HideAtts(ExtendedStyle), //0; (to show the element Attributes compartment)
    HideEStereo(ExtendedStyle), //0; (to show element stereotypes in the diagram)
    HideOps(ExtendedStyle), //0; (to show the element Operations compartment)
    HideParents(ExtendedStyle), //0; (to show additional parents of elements in the diagram)
    HideProps(ExtendedStyle), //0; (to show property methods)
    HideRel(ExtendedStyle), //0; (to show relationships)
    HideStereo(ExtendedStyle), //0; (to show attribute and operation stereotypes)
    OpParams(ExtendedStyle), //3; (to show operation parameters)
    ShowCons(ExtendedStyle), //1; (to show the element Constraints compartment)
    ShowIcons(ExtendedStyle), //1; (to use stereotype icons)
    ShowReqs(ExtendedStyle), //1; (to show the element Requirements compartment)
    ShowSN(ExtendedStyle), //1; (to show sequence notes)
    ShowTags(ExtendedStyle), //1; (to show the element Tagged Values compartment)
    SuppCN(ExtendedStyle), //0; (to show collaboration numbers)
    UseAlias(ExtendedStyle), //1; (to use the aliases or elements in the diagram, if available)
    ScalePI(ExtendedStyle),
    PPgs_cx(ExtendedStyle),
    PPgs_cy(ExtendedStyle),
    PSize(ExtendedStyle),
    ShowShape(ExtendedStyle),
    FormName(ExtendedStyle),

    NONE(ExtendedStyle);

    private static final transient Logger LOG = LoggerFactory.getLogger(EaAttributeName.class);

    EaAttributeName(final EaAttributeType type) {
        this.type = type;
    }

    public enum EaAttributeType {
        StyleEx,
        ExtendedStyle
    }

    private final EaAttributeType type;

    public static EaAttributeName fromString(final String value) {
        EaAttributeName result = NONE;
        try {
            result = valueOf(trimToEmpty(value).replaceAll("\\.", "_"));
        } catch (final IllegalArgumentException iae) {
            LOG.error("No EA Attribute name for [{}]", value);
        }
        return result;
    }

    public EaAttributeType getType() {
        return type;
    }

    @Override
    public String toString() {
        return super.toString().replaceAll("_", ".");
    }
}

package no.eatools.diagramgen;

/**
 * Corresponds to the Connector Styles used in EA:
 *
 * @author ohs
 */
public enum EaConnectorStyle {
    STRAIGHT(0),
    TREE(42);

    private final int styleNumber;

    EaConnectorStyle(final int style) {
        this.styleNumber = style;
    }


//    public int getStyleNumber() {
//        EaConnectorStyle.values()
//        return styleNumber;
//    }


}

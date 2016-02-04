package no.eatools.util;

import no.eatools.diagramgen.EaDiagram;

import org.apache.commons.lang.StringUtils;

import static no.bouvet.ohs.jops.SystemPropertySet.*;

/**
 * @author ohs
 */
public class NameNormalizer {

    public static final String URL_SPARATOR = "/";

    private NameNormalizer() {
    }

    public static String makeWebFriendlyFilename(String s) {
        s = StringUtils.replaceChars(s, ' ', '_');
        if (PATH_SEPARATOR.value().equals(URL_SPARATOR)) {
            s = StringUtils.replaceChars(s, '\\', '-');
        } else if (PATH_SEPARATOR.value().equals("\\")) {
            s = StringUtils.replaceChars(s, '/', '-');
        }
        /* Replace Norwegian characters with alternatives */
        s = StringUtils.replace(s, "Æ", "ae");
        s = StringUtils.replace(s, "Ø", "oe");
        s = StringUtils.replace(s, "Å", "aa");
        s = StringUtils.replace(s, "æ", "ae");
        s = StringUtils.replace(s, "ø", "oe");
        s = StringUtils.replace(s, "å", "aa");
        s = StringUtils.lowerCase(s);
        return s;
    }

    public static String nodePathToUrl(String nodePath) {
        return URL_SPARATOR + makeWebFriendlyFilename(nodePath.replaceAll("[\\.]+", URL_SPARATOR)) + EaDiagram.defaultImageFormat.getFileExtension();
    }
}

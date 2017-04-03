package no.eatools.util;

import java.io.File;

import no.eatools.diagramgen.EaDiagram;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.bouvet.ohs.jops.SystemPropertySet.*;
import static no.eatools.util.EaApplicationProperties.*;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * @author ohs
 */
public class NameNormalizer {
    private static final transient Logger LOG = LoggerFactory.getLogger(NameNormalizer.class);

    public static final String URL_SEPARATOR = "/";

    /**
     * The Unix separator character.
     */
    public static final String UNIX_SEPARATOR = "/";

    /**
     * The Windows separator character.
     */
    public static final String WINDOWS_SEPARATOR = "\\";
    public static final String ILLEGAL_CHARS_REGEX = "[#%&<>$!^~'\"@\\+`|=:;\\?\\*\\(\\)]";


    private NameNormalizer() {
    }

    /**
     * Leading separator is removed.
     *
     * @param input path-like in put on the form "abc\def/ghi", i.e. both kind of separators may be present.
     * @param substitutePathSeparators eg true, non OS-specific file-separator is substituted with a "-"
     * @param urlStyle if true, return will have all "/" (URL_SEPARATORS), if false, OS-specific separators are used.
     * @return null, if input==null
     */
    public static String makeWebFriendlyName(final String input, final boolean substitutePathSeparators, final boolean urlStyle) {
        String result = input;

        result = trimToEmpty(replaceChars(result, ' ', '_'));

        final boolean unix = FILE_SEPARATOR.value()
                                           .equals(UNIX_SEPARATOR);

        if (substitutePathSeparators) {
            if (unix) {
                result = replaceChars(result, WINDOWS_SEPARATOR, "-");
            } else {
                result = replaceChars(result, UNIX_SEPARATOR, "-");
            }
        }

        // Remove repeating separators as they will make the normalization fail
        result = result.replaceAll("[\\\\]+", "\\\\");
        result = result.replaceAll("[" + UNIX_SEPARATOR + "]+", UNIX_SEPARATOR);

        result = trimToEmpty(FilenameUtils.normalize(result, urlStyle || unix));

        result = result.replaceAll(ILLEGAL_CHARS_REGEX, "_");
        /* Replace Norwegian characters with alternatives */
        result = replace(result, "Æ", "ae");
        result = replace(result, "Ø", "oe");
        result = replace(result, "Å", "aa");
        result = replace(result, "æ", "ae");
        result = replace(result, "ø", "oe");
        result = replace(result, "å", "aa");
        result = replace(result, "{", "--");
        result = replace(result, "}", "--");
        result = lowerCase(result);
        LOG.debug("Input [{}] Web-friendly [{}]", input, result);

        // Remove leading separator
        return result.replaceFirst("^[\\" + FILE_SEPARATOR.value() + "\\" + URL_SEPARATOR + "]+", "");
    }

    public static String nodePathToUrl(final String nodePath) {
        return URL_SEPARATOR + replace(makeWebFriendlyName(nodePath, false, true), ".", URL_SEPARATOR) + EaDiagram.defaultImageFormat
                .getFileExtension();
    }

    public static boolean isAbsoluteFileName(final String fileName) {
        return new File(fileName).isAbsolute();
    }

    public static File createFile(final int level, final String rootDirName, final String logicalPathname, final String diagramName, final String
            diagramGUID, final String
            diagramVersion, final DiagramNameMode diagramNameMode, final String fileExtension) {

        final File rootDir;
        if (!isAbsoluteFileName(rootDirName)) {
            final File cwd = new File(USER_DIR.value());
            LOG.info("{} is not a root directory. Using {}", rootDirName, cwd);
            rootDir = new File(cwd.getAbsolutePath() + FILE_SEPARATOR.value() + rootDirName);
        } else {
            rootDir = new File(rootDirName);
        }

        final String absolutePath = appendIfMissing(rootDir.getAbsolutePath(), FILE_SEPARATOR.value());
        final String urlPart = createUrlPart(level, logicalPathname, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);


        final File file = new File(FilenameUtils.normalize(absolutePath + urlPart));
        LOG.debug("Created file for diagram [{}] diagramGUID [{}] filename [{}] ", diagramName, diagramGUID, file.getAbsolutePath());

        return file;
    }

    /**
     * @param level
     * @param logicalPathname path on the form "/a/b/c"
     * @param diagramName
     * @param diagramGUID
     * @param diagramVersion
     * @param diagramNameMode
     * @param fileExtension
     * @return
     */
    public static String createUrlPart(final int level, final String logicalPathname, final String diagramName, final String diagramGUID, final String
            diagramVersion, final DiagramNameMode diagramNameMode, final String fileExtension) {
        final StringBuilder urlPart = new StringBuilder();
        String adjustedDiagramName = diagramName.replaceAll("[\\\\\\/]", "_"); // No slashes (back or fwd) in resulting filename

        switch (diagramNameMode) {
            case GUID_AT_START:
                urlPart.append(FILE_SEPARATOR.value())
                       .append(createPath(logicalPathname, level))
                       .append(FILE_SEPARATOR.value())
                       .append(diagramGUID)
                       .append(adjustedDiagramName);
                break;
            case GUID_AT_END:
                urlPart.append(FILE_SEPARATOR.value())
                       .append(createPath(logicalPathname, level))
                       .append(FILE_SEPARATOR.value())
                       .append(adjustedDiagramName)
                       .append(diagramGUID);
                break;
            case FULL_PATH:
            default:
                urlPart.append(logicalPathname)
                       .append(FILE_SEPARATOR.value())
                       .append(adjustedDiagramName);
                break;
        }

        if (EA_ADD_VERSION.exists()) {
            urlPart.append(diagramVersion);
        }
        urlPart.append(fileExtension);

        return makeWebFriendlyName(urlPart.toString(), false, true);
    }

    /**
     *
     * @param logicalPathname, expected to be on the form "/a/b/c"
     * @param level
     * @return path on the form level==0 ? "", level==1 ? "/a", level==2 ? "/a/b"
     */
    public static String createPath(final String logicalPathname, final int level) {
        final String[] paths = logicalPathname.split("\\" + URL_SEPARATOR);
        int index = 1;
        final StringBuilder stringBuilder = new StringBuilder();
        while ((index <= level) && (index < paths.length)) {
            stringBuilder.append(URL_SEPARATOR)
                                         .append(paths[index]);
            ++index;
        }
        return stringBuilder.toString();
    }
}

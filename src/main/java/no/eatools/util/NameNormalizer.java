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


    private NameNormalizer() {
    }

    /**
     * Leading
     *
     * @param input
     * @return null, if input==null
     */
    public static String makeWebFriendlyFilename(final String input, final boolean replaceSeparators) {
        String result = input;

        result = trimToEmpty(replaceChars(result, ' ', '_'));

        final boolean unix = FILE_SEPARATOR.value()
                                           .equals(UNIX_SEPARATOR);
        if(replaceSeparators) {
            if (unix) {
                result = replaceChars(result, WINDOWS_SEPARATOR, "-");
            } else {
                result = replaceChars(result, UNIX_SEPARATOR, "-");
            }
        }

        result = trimToEmpty(FilenameUtils.normalize(result, !replaceSeparators || unix));

        result = result.replaceAll("[:;\\?\\*]", "");
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

        return result.replaceFirst("^[\\" + FILE_SEPARATOR.value() + "]+", "");
    }

    public static String nodePathToUrl(final String nodePath) {
        return URL_SEPARATOR + replace(makeWebFriendlyFilename(nodePath, false), ".", URL_SEPARATOR) + EaDiagram.defaultImageFormat.getFileExtension();
    }

    public static boolean isAbsoluteFileName(final String fileName) {
        return new File(fileName).isAbsolute();
    }

    /**
     * Create a partial URL string for a file. The string is suited for prepending protocol and base address to make up a complete URL.
     *
     * @param fileIn  The file
     * @param rootDir root directory will be removed from the resulting partial URL
     * @return partial URL for the give file, no leading "/"
     * @deprecated
     */
    public static String createUrlPartForFile(final File fileIn, final String rootDir) {
        // remove directory root prefix
//        String adjustedFileName;
//        if (isAbsoluteFileName(rootDir)) {
//            adjustedFileName = makeWebFriendlyFilename(FILE_SEPARATOR.value() + fileIn.getAbsolutePath());
//            final String prefix = makeWebFriendlyFilename(FILE_SEPARATOR.value() + rootDir);
//
//            adjustedFileName = removeStart(adjustedFileName, prefix);
//        } else {
//            adjustedFileName = makeWebFriendlyFilename(FILE_SEPARATOR.value() + fileIn.getPath());
//        }

        return replace("", FILE_SEPARATOR.value(), URL_SEPARATOR);
//        final File baseFile = new File(adjustedFileName);
//
//        String urlBase = "";
//        try {
//            final URL diagramUrl = baseFile.toURI()
//                                           .toURL();
//            LOG.info("Diagram url " + diagramUrl);
//            urlBase = diagramUrl.toString()
//                                .replace(diagramUrl.getProtocol(), "")
//                                .replace(rootDir, "")
//                                .replace(":", "");
//            LOG.info("-> URLBase: " + urlBase);
//        } catch (final MalformedURLException e) {
//            LOG.error("Unable to create url from file " + urlBase);
//        }
//        return urlBase;
    }

    public static File createFile(final String rootDirName, final String logicalPathname, final String diagramName, final String diagramGUID, final String
            diagramVersion, final DiagramNameMode diagramNameMode, final String fileExtension) {
        final StringBuilder fileName = new StringBuilder();

        final File rootDir;
        if (!isAbsoluteFileName(rootDirName)) {
            final File cwd = new File(USER_DIR.value());
            LOG.info("{} is not a root directory. Using {}", rootDirName, cwd);
//            fileName.append(cwd)
//                    .append(FILE_SEPARATOR.value())
//                    .append(rootDirName);
            rootDir = new File(cwd.getAbsolutePath() + FILE_SEPARATOR.value() + rootDirName);
        } else {
//            fileName.append(rootDirName);
            rootDir = new File(rootDirName);
        }
//
//        switch (diagramNameMode) {
//            case GUID_AT_START:
//                fileName.append(FILE_SEPARATOR.value())
//                        .append(diagramGUID)
//                        .append(diagramName);
//                break;
//            case GUID_AT_END:
//                fileName.append(FILE_SEPARATOR.value())
//                        .append(diagramName)
//                        .append(diagramGUID);
//                break;
//            case FULL_PATH:
//            default:
//                fileName.append(logicalPathname)
//                        .append(FILE_SEPARATOR.value())
//                        .append(diagramName);
//                break;
//        }
//
//        if (EA_ADD_VERSION.exists()) {
//            fileName.append(diagramVersion);
//        }
//        fileName.append(fileExtension);

        final String absolutePath = appendIfMissing(rootDir.getAbsolutePath(), FILE_SEPARATOR.value());
        final String urlPart = createUrlPart(logicalPathname, diagramName, diagramGUID, diagramVersion, diagramNameMode, fileExtension);


        final File file = new File(FilenameUtils.normalize(absolutePath + urlPart));
        LOG.debug("Created file for diagram [{}] diagramGUID [{}] filename [{}] ", diagramName, diagramGUID, file.getAbsolutePath());

        return file;
    }

    /**
     *
     * @param logicalPathname
     * @param diagramName
     * @param diagramGUID
     * @param diagramVersion
     * @param diagramNameMode
     * @param fileExtension
     * @return
     */
    public static String createUrlPart(final String logicalPathname, final String diagramName, final String diagramGUID, final String
            diagramVersion, final DiagramNameMode diagramNameMode, final String fileExtension) {
        final StringBuilder urlPart = new StringBuilder();

        switch (diagramNameMode) {
            case GUID_AT_START:
                urlPart.append(FILE_SEPARATOR.value())
                        .append(diagramGUID)
                        .append(diagramName);
                break;
            case GUID_AT_END:
                urlPart.append(FILE_SEPARATOR.value())
                        .append(diagramName)
                        .append(diagramGUID);
                break;
            case FULL_PATH:
            default:
                urlPart.append(logicalPathname)
                        .append(FILE_SEPARATOR.value())
                        .append(diagramName);
                break;
        }

        if (EA_ADD_VERSION.exists()) {
            urlPart.append(diagramVersion);
        }
        urlPart.append(fileExtension);

        return makeWebFriendlyFilename(urlPart.toString(), false);
//        return makeWebFriendlyFilename(prependIfMissing(urlPart.toString(), URL_SEPARATOR), false);
    }

}

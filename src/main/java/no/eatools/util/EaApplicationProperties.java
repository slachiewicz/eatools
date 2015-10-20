package no.eatools.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import no.bouvet.ohs.args4j.Description;
import no.bouvet.ohs.args4j.PropertyMap;
import no.bouvet.ohs.cli.EnumProperty;
import no.bouvet.ohs.jops.Camel;
import no.bouvet.ohs.jops.SystemProperties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class handles the properties that can be used to configure the EA utilities. The property file can:
 * <ul>
 * <li>
 * Be given as a parameter, or
 * </li>
 * <li>
 * Be placed in a file called 'ea.application.properties' (i.e. corresponding to the classname) which
 * must be located in {user.home} or in classpath.
 * </li>
 * </ul>
 * <p>
 * The order of precedence is: parameter, file in {user.home}, file in classpath.
 * </p>
 * The names of the properties in the property file must correspond to the constants defined in
 * this enum class, and all enum-constants must have a corresponding property in the file.
 *
 * @author Ove Scheel
 * @author Per Spilling
 * @since 05.nov.2008 09:25:13
 */
public enum EaApplicationProperties implements EnumProperty<EaApplicationProperties> {
    @Description(text = "Name of .eap file or connection string to database repos.")
    EA_PROJECT,

    @Description(text = "The root package in the repo to generate from. Must be a top level package.")
    EA_ROOTPKG,

    @Description(text = "The directory root to place diagrams in when generating the diagrams.\nNB! Must be given as an absolute pathname or "
            + "relative to cwd.")
    EA_DOC_ROOT_DIR,

    @Description(text = "The loglevel when running the utility. If ommitted, INFO is set.")
    EA_LOGLEVEL,

    @Description(text = "Name or diagramId (internal EA number) of diagram to generate. I ")
    EA_DIAGRAM_TO_GENERATE,

    @Description(text = "Which file to place the url of the generated diagram. This file may be used for scripting after generation or as a "
            + "debugging aid.")
    EA_DIAGRAM_URL_FILE,

    @Description(text = "Only include packages which matches given regexp")
    EA_PACKAGE_FILTER,

    @Description(text = "Username for EA repos")
    EA_USERNAME,

    @Description(text = "Password for EA repos")
    EA_PASSWORD,

    @Description(text = "If present, add diagram version as part of diagram filename")
    EA_ADD_VERSION;

    private static final Log log = LogFactory.getLog(EaApplicationProperties.class);

    private final static Properties applicationProperties = new Properties();
    private static String propsFilename = null;
    private static PropertyMap<EaApplicationProperties> propsMap;

//    public static void init() {
//        _init();
//    }

    public static void init(String propertyFilename, PropertyMap propertyMap) {
        propsFilename = propertyFilename;
        propsMap = propertyMap;
        _init();
    }

    /**
     * Loads development tool properties from the property file propsFilename which can be given as a
     * parameter, be located in {user.home}, or in the classpath. A parameter value will have precedence
     * over a file in the home directory, which will have precedence over a property file in the classpath.
     *
     * @return loaded properties
     */
    static void _init() {
        final String fileSeparator = SystemProperties.FILE_SEPARATOR.value();
        if (StringUtils.isBlank(propsFilename)) {
            propsFilename = getPropertiesFilename();
        }
        File localPropFile = new File(propsFilename);
        File homePropFile = new File(SystemProperties.USER_HOME.value() + fileSeparator + propsFilename);

        if (localPropFile.canRead()) {
            loadPropertiesFromFile(localPropFile);
        } else if (homePropFile.canRead()) {
            loadPropertiesFromFile(homePropFile);
        } else {
            loadPropertiesFromClassPath(propsFilename);
        }
        if (applicationProperties.isEmpty()) {
            String helpMessage =
                    "Couldn't find the property file - The properties should be placed in a file called 'ea.application.properties' (i.e. "
                            + "corresponding\n"
                            +
                            "to the classname) which must be located in {user.home} or in classpath. If there are two property\n" +
                            "files then both files will be read, and the properties in {user.home} will override the properties\n" +
                            "from the file on the classpath. {user.home} = " + SystemProperties.USER_HOME.value();
            System.out.println(helpMessage);
            System.exit(0);
        }

        for (EaApplicationProperties prop : propsMap.keySet()) {
            applicationProperties.setProperty(prop.toString(), propsMap.get(prop));
        }
        // Check that properties in the Enum property set also exist in the property file
        for (EaApplicationProperties prop : EaApplicationProperties.values()) {
            if (applicationProperties.getProperty(prop.keyAsPropertyName()) == null) {
                log.warn("Missing property [" + prop.keyAsPropertyName() + "] in property file: " + propsFilename);
            }
        }
        // Check that properties in the property file also exists in the Enum Property set
        for (Object key : applicationProperties.keySet()) {
            try {
                String enumName = Camel.propertyNameAsConstant((String) key);
                valueOf(enumName);
                log.info("Property [" + key + "(" + enumName + ")] value [" + valueOf(enumName).value() + "]");
            } catch (IllegalArgumentException iae) {
                log.warn("Missing property enum [" + Camel.propertyNameAsConstant((String) key) + "] in " + EaApplicationProperties.class.getName());
            }
        }
    }

    public static String getPropertiesFilename() {
        // Expects a property file with the name of this class:
        String name = EaApplicationProperties.class.getName();
        if (name.lastIndexOf('.') > 0) {
            name = name.substring(name.lastIndexOf('.') + 1);
        }
        final String propsFilename = Camel.camelCaseAsPropertyName(name);
        return propsFilename;
    }

    private static void loadPropertiesFromClassPath(String propsFilename) {
        try {
            applicationProperties.load(EaApplicationProperties.class.getClassLoader().getResourceAsStream(propsFilename));
            log.info("Using properties from classpath");
        } catch (Exception e1) {
            // no need to worry
        }
    }

    private static void loadPropertiesFromFile(File file) {
        try {
            applicationProperties.load(new FileInputStream(file));
            log.info("Using properties from " + file.getAbsolutePath());
        } catch (Exception e) {
            log.info("Unable to load properties from: " + file.getAbsolutePath());
        }
    }

    public static String printAllProperties() {
        StrBuilder sb = new StrBuilder();
        for (EaApplicationProperties prop : propsMap.keySet()) {
            sb.append(prop).append("=");
            if (prop.exists()) {
                sb.append(propsMap.get(prop));
            } else {
                sb.append("--not set--");
            }
        }
        return sb.toString();
    }

    @Override
    public String value() {
        return applicationProperties.getProperty(keyAsPropertyName(), "");
    }

    @Override
    public String value(String defaultValue) {
        return applicationProperties.getProperty(keyAsPropertyName(), defaultValue);
    }

    @Override
    public String keyAsPropertyName() {
        return Camel.toPropertyName(super.toString());
    }

    @Override
    public boolean exists() {
        return StringUtils.isNotBlank(applicationProperties.getProperty(keyAsPropertyName()));
    }
}

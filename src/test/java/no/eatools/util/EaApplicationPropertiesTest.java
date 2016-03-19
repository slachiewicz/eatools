package no.eatools.util;

import org.junit.Before;
import org.junit.Test;

import static no.bouvet.ohs.jops.SystemPropertySet.*;
import static no.eatools.util.EaApplicationProperties.*;
import static org.junit.Assert.*;

/**
 * @author ohs
 */
public class EaApplicationPropertiesTest {

    private boolean isWindows;

    @Before
    public void setUp() throws Exception {
        isWindows = OS_NAME.value()
                           .startsWith("Win");
    }

        @Test
    public void testBaseUrl() throws Exception {
            if (!isWindows) {
                EA_DOC_ROOT_DIR.setValue("/Volume//her");
                assertEquals("/Volume/her/", EA_DOC_ROOT_DIR.value());

                EA_DOC_ROOT_DIR.setValue("/Volume//her/");
                assertEquals("/Volume/her/", EA_DOC_ROOT_DIR.value());

            } else {
                EA_DOC_ROOT_DIR.setValue("C:\\");
                assertEquals("C:\\", EA_DOC_ROOT_DIR.value());

//                EA_DOC_ROOT_DIR.setValue("D:\\\\");
//                assertEquals("D:\\", EA_DOC_ROOT_DIR.value());

//                EA_DOC_ROOT_DIR.setValue("E:///");
//                assertEquals("E:\\", EA_DOC_ROOT_DIR.value());

                EA_DOC_ROOT_DIR.setValue("F:");
                assertEquals("F:\\", EA_DOC_ROOT_DIR.value());

                EA_DOC_ROOT_DIR.setValue("C:/Her\\der");
                assertEquals("C:\\Her\\der\\", EA_DOC_ROOT_DIR.value());
            }
        }
}

package no.eatools.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * @author ohs
 */
public class ObjectGraphTest extends TestCase {
    private static final transient Log LOG = LogFactory.getLog(ObjectGraphTest.class);

    public void testCreateDotGraph() throws Exception {
        String s = "abc";
        LOG.debug(new ObjectGraph().createDotGraph(s));
    }
}

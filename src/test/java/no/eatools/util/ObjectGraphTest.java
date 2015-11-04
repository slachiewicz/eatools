package no.eatools.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

/**
 * @author ohs
 */
public class ObjectGraphTest extends TestCase {
    private static final transient Logger LOG = LoggerFactory.getLogger(ObjectGraphTest.class);

    public void testCreateDotGraph() throws Exception {
        String s = "abc";
        LOG.debug(new ObjectGraph().createDotGraph(s));
    }
}

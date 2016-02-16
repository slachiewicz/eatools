package no.eatools.util;

import java.io.File;
import java.util.Date;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ohs
 */
public class ImageMetadataTest {
    private static final transient Logger LOG = LoggerFactory.getLogger(ImageMetadataTest.class);

    @Test
    public void testReadAndDisplayMetadata() throws Exception {

        new ImageMetadata().readAndDisplayMetadata("Model.png");

    }

    @Test
    public void testDisplayMetadata() throws Exception {

    }

    @Test
    public void testDisplayMetadata1() throws Exception {

    }

    @Test
    public void testWriteCustomMetaData() throws Exception {
        LOG.info("ALl is good");

        new ImageMetadata().writeCustomMetaData(new File("Model.png"), "EA GUID", "CAFE-BABE" + new Date());

    }
}

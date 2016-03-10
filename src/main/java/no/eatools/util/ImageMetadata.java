package no.eatools.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import no.eatools.diagramgen.ImageFileFormat;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author ohs
 */
public class ImageMetadata {
    private static final transient Logger LOG = LoggerFactory.getLogger(ImageMetadata.class);
//    public static final String TEXT_NODE_NAME = "Text";
//    public static final String TEXT_ENTRY_NODE_NAME = "TextEntry";

    public static final String TEXT_NODE_NAME = "tEXt";
    public static final String TEXT_ENTRY_NODE_NAME = "tEXtEntry";

    public static final String KEYWORD = "keyword";
    public static final String VALUE = "value";
    public static final String DEFAULT_FORMAT_NAME = "javax_imageio_png_1.0";


    public static void main(final String[] args) {
        final ImageMetadata meta = new ImageMetadata();
        for (final String arg : args) {
            meta.readAndDisplayMetadata(arg);
        }
    }

    ImageReader openImageFile(final File file) {
        try {
            final ImageInputStream iis = ImageIO.createImageInputStream(file);
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

            if (readers.hasNext()) {

                // pick the first available ImageReader
                final ImageReader reader = readers.next();

                // attach source to the reader
                reader.setInput(iis, true);

                return reader;
            }
        } catch (final Exception e) {
            LOG.error("Error ", e);
        }
        return null;
    }

    void readAndDisplayMetadata(final String fileName) {
        readAndDisplayMetadata(new File(fileName));
    }

    void readAndDisplayMetadata(final File file) {
        try {
            final ImageReader reader = openImageFile(file);
//                final BufferedImage bufferedImage = reader.read(0);

//                writeCustomData(bufferedImage, "Hei", "Haa");

            // read metadata of first image
            final IIOMetadata metadata = reader.getImageMetadata(0);
            LOG.debug("Native format name: {}", metadata.getNativeMetadataFormatName());
            final String[] names = metadata.getMetadataFormatNames();
            for (final String name : names) {
                LOG.debug("Format name: {}", name);
                final Node asTree = metadata.getAsTree(name);
                displayMetadata(asTree);
////                    final IIOMetadataNode child = new IIOMetadataNode("Ove");
////                    child.setAttribute("now", new Date().toString());
//                    asTree.appendChild(child);
//                    displayMetadata(asTree);
//                displayMetadata(findTextNode(asTree));
            }

        } catch (final Exception e) {
            LOG.error("Error ", e);
        }
    }

    Node findTextNode(final IIOMetadata metadata) {
//        metadata.getNativeMetadataFormatName();
        for (final String formatName : metadata.getMetadataFormatNames()) {
            final Node asTree = metadata.getAsTree(formatName);
            final Node textNode = findTextNode(asTree);
            if (textNode != null) {
                return textNode;
            }
        }
        return null;
    }

    Node findTextNode(final Node root) {
        final NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (TEXT_NODE_NAME.equalsIgnoreCase(child.getNodeName())) {
                return child;
            }
            final Node candidate = findTextNode(child);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    public void writeCustomData(final BufferedImage buffImg, final File file, final String key, final String value) throws Exception {
        final ImageWriter writer = ImageIO.getImageWritersByFormatName(ImageFileFormat.PNG.toString()
                                                                                          .toLowerCase())
                                          .next();

        final ImageWriteParam writeParam = writer.getDefaultWriteParam();
        final ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);

        //adding metadata
        final IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);

        Node textNode = findTextNode(metadata);
        if (textNode == null) {
            textNode = new IIOMetadataNode(TEXT_NODE_NAME);
//            final Node root = metadata.getAsTree(metadata.getNativeMetadataFormatName());
//            root.appendChild(textNode);
        } else {
            LOG.debug("---------------- TExt node found ");
        }

        final IIOMetadataNode textEntry = new IIOMetadataNode(TEXT_ENTRY_NODE_NAME);
        textEntry.setAttribute(KEYWORD, key);
        textEntry.setAttribute(VALUE, value);

        textNode.appendChild(textEntry);

////        metadata.mergeTree();
//        final IIOMetadataNode root = new IIOMetadataNode(DEFAULT_FORMAT_NAME);
//        root.appendChild(textNode);
//
//        metadata.mergeTree(DEFAULT_FORMAT_NAME, root);
//
////        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        final FileOutputStream fios = new FileOutputStream(file);
//        final ImageOutputStream stream = ImageIO.createImageOutputStream(fios);
//        writer.setOutput(stream);
//        writer.write(metadata, new IIOImage(buffImg, null, metadata), writeParam);
//
//        stream.flush();
//        stream.close();
//        fios.flush();
//        fios.close();
////        baos.flush();
////        baos.close();
//

//        //writing the data
//        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (final FileOutputStream fios = new FileOutputStream(file);
             final ImageOutputStream stream = ImageIO.createImageOutputStream(fios)) {
            writer.setOutput(stream);
            writer.write(metadata, new IIOImage(buffImg, null, metadata), writeParam);

//            stream.flush();
//            stream.close();
//            fios.flush();
//            fios.close();
//        baos.flush();
//        baos.close();
        }
    }

    void displayMetadata(final Node root) {
//        System.out.println(formatXML(root));
    }

    public String formatXML(final Node input) {
        try {
            final Transformer transformer = TransformerFactory.newInstance()
                                                              .newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "3");

            final StreamResult result = new StreamResult(new StringWriter());
            transformer.transform(new DOMSource(input), result);
            return result.getWriter()
                         .toString();
        } catch (final Exception e) {
            LOG.error("Error in parsing", e);
            return "";
        }
    }

    public void writeCustomMetaData(final File file, final String key, final String value) {
        System.out.println("Opening file " + file.getAbsolutePath());
        LOG.debug("*** Meta before modification ***");
        readAndDisplayMetadata(file);
        LOG.debug("*** / ***");

        final ImageReader reader = openImageFile(file);
        try {
            final File tmpFile = File.createTempFile("metaTmp", ImageFileFormat.PNG.getFileExtension(), file.getParentFile());
//            tmpFile.deleteOnExit();
            LOG.debug("Created tmp file {}", tmpFile.getAbsolutePath());
            writeCustomData(reader.read(0), tmpFile, key, value);
            LOG.debug("*** Meta after modification ***");
            readAndDisplayMetadata(tmpFile);
            LOG.debug("*** / ***");
            file.delete();
            FileUtils.copyFile(tmpFile, file);

            final boolean wasDeleted = tmpFile.delete();
            if (!wasDeleted) {
                System.out.println("Unable to delete " + tmpFile.getAbsolutePath());
                tmpFile.deleteOnExit();
            }
        } catch (final Exception e) {
            LOG.error("Error on creating metadata", e);
        }
    }
}

package no.eatools.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;


/**
 * @author ohs
 */
public class ImageMetadata {
    private static final transient Logger LOG = LoggerFactory.getLogger(ImageMetadata.class);

    public static void main(final String[] args) {
        final ImageMetadata meta = new ImageMetadata();
        final int length = args.length;
        for (int i = 0; i < length; i++)
            meta.readAndDisplayMetadata(args[i]);
    }


    ImageReader openImageFile(final String fileName) {
        try {
            final File file = new File(fileName);
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
        try {
            final File file = new File(fileName);
            final ImageInputStream iis = ImageIO.createImageInputStream(file);
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

            if (readers.hasNext()) {

                // pick the first available ImageReader
                final ImageReader reader = readers.next();

                // attach source to the reader
                reader.setInput(iis, true);
                final BufferedImage bufferedImage = reader.read(0);

//                writeCustomData(bufferedImage, "Hei", "Haa");

                // read metadata of first image
                final IIOMetadata metadata = reader.getImageMetadata(0);

                final String[] names = metadata.getMetadataFormatNames();
                for (final String name : names) {
                    System.out.println("Format name: " + name);
                    final Node asTree = metadata.getAsTree(name);
                    displayMetadata(asTree);
////                    final IIOMetadataNode child = new IIOMetadataNode("Ove");
////                    child.setAttribute("now", new Date().toString());
//                    asTree.appendChild(child);
//                    displayMetadata(asTree);
                }
            }
        } catch (final Exception e) {
            LOG.error("Error ", e);
        }
    }

    public byte[] writeCustomData(final BufferedImage buffImg, final String key, final String value) throws Exception {
        final ImageWriter writer = ImageIO.getImageWritersByFormatName("png")
                                          .next();

        final ImageWriteParam writeParam = writer.getDefaultWriteParam();
        final ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);

        //adding metadata
        final IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);

        final IIOMetadataNode textEntry = new IIOMetadataNode("tEXtEntry");
        textEntry.setAttribute("keyword", key);
        textEntry.setAttribute("value", value);

        final IIOMetadataNode text = new IIOMetadataNode("tEXt");
        text.appendChild(textEntry);

//        metadata.mergeTree();
        final IIOMetadataNode root = new IIOMetadataNode("javax_imageio_png_1.0");
        root.appendChild(text);

        metadata.mergeTree("javax_imageio_png_1.0", root);

        //writing the data
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final FileOutputStream fios = new FileOutputStream("xx.png");
        final ImageOutputStream stream = ImageIO.createImageOutputStream(fios);
        writer.setOutput(stream);
        writer.write(metadata, new IIOImage(buffImg, null, metadata), writeParam);

        stream.flush();
        stream.close();
        fios.flush();
        fios.close();

        return baos.toByteArray();
    }

//    void addMetadata(final String fileName, String key, String value) {
//        try {
//
//            final File file = new File(fileName);
//            final ImageOutputStream ios = ImageIO.createImageOutputStream(file);
//            final Iterator<ImageWriter> readers = ImageIO.getImageWriters();
//
//            if (readers.hasNext()) {
//                // pick the first available ImageReader
//                final ImageReader reader = readers.next();
//
//                // attach source to the reader
//                reader.setInput(iis, true);
//
//                // read metadata of first image
//                final IIOMetadata metadata = reader.getImageMetadata(0);
//
//                final String[] names = metadata.getMetadataFormatNames();
//                for (String name : names) {
//                    System.out.println("Format name: " + name);
//                    Node asTree = metadata.getAsTree(name);
//                    asTree.appendChild(new IIOMetadataNode("Ove"));
//                    asTree.
//                                  displayMetadata(asTree);
//                }
//            }
//        } catch (final Exception e) {
//            LOG.error("Error ", e);
//        }
//    }

    void displayMetadata(final Node root) {
        System.out.println(formatXML(root));
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

}

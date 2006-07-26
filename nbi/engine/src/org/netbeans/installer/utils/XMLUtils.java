/*
 * XMLUtils.java
 *
 */

package org.netbeans.installer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;

/**
 *
 * @author Dmitry Lipin
 */
public class XMLUtils {
    private static final XMLUtils xmlUtils = new XMLUtils();
    private static final String ERROR_SAVING_FILE = "Could not save XML to file ";
    
    /** Creates a new instance of XMLUtils */
    private XMLUtils() {
    }
    
    public static XMLUtils getInstance() {
        return xmlUtils;
    }
    
    public void saveXMLDocument(Document doc, File file,File xsltTransformFile)
    throws TransformerConfigurationException, TransformerException, IOException {
        FileOutputStream outputStream = null;
        try {
            Source xsltSource = new StreamSource(xsltTransformFile);
            Source domSource = new DOMSource(doc);
            outputStream = new FileOutputStream(file);
            Result streamResult = new StreamResult(outputStream);
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer(xsltSource);
            
            transformer.transform(domSource, streamResult);
        } finally {
            if(outputStream!=null) {
                outputStream.close();
            }
        }
    }
}

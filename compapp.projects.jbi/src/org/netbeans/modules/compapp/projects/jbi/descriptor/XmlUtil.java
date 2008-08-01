/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.compapp.projects.jbi.descriptor;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * XML helper methods
 *
 * @author tli
 */
public class XmlUtil {
    /**
     * DOCUMENT ME!
     *
     * @param fileLocation DOCUMENT ME!
     *
     * @throws javax.xml.transform.TransformerConfigurationException DOCUMENT ME!
     * @throws javax.xml.transform.TransformerException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public static void writeToFile(String fileLocation, Document document)
        throws TransformerConfigurationException, TransformerException, 
            FileNotFoundException, UnsupportedEncodingException, IOException {

        File outputFile = new File(fileLocation);
        FileObject outputFO = FileUtil.toFileObject(outputFile);
        
//        if (outputFO != null) {
//            // #129625: Use NB FS API. Alternatively, we could use File API,  
//            // then refresh NB FileSystem.
//            writeToFileObject(outputFO, document);
//            return;
//        } 
        
        PrintWriter pw = new PrintWriter(outputFile, "UTF-8"); // NOI18N
        StreamResult result = new StreamResult(pw);

        try {
            writeTo(result, document);
        } finally {
            if (pw != null) {
                pw.close();
            }
        } 
            
        // #136190: avoid file locking caused by the old fix to #129625
        if (outputFO != null) {
            outputFO.getFileSystem().refresh(true);
        }
    }
    
    public static void writeToOutputStream(OutputStream os, Document document)
        throws TransformerConfigurationException, TransformerException, 
            FileNotFoundException, UnsupportedEncodingException, IOException {
        
        StreamResult result = new StreamResult(os);
        
        try {
            writeTo(result, document);
        } finally {
            if (os != null) {
                os.close();
            }
        }        
    }
    
    public static void writeToFileObject(FileObject fo, Document document)
        throws TransformerConfigurationException, TransformerException, 
            FileNotFoundException, UnsupportedEncodingException, IOException {
        
        FileLock lock = fo.lock();
        OutputStream os = fo.getOutputStream(lock);
        try {
            writeToOutputStream(os, document);
        } finally {
            lock.releaseLock();
        }
    }
    
    private static void writeTo(Result result, Document document)
        throws TransformerConfigurationException, TransformerException, 
            FileNotFoundException, UnsupportedEncodingException {
        
        // Use a Transformer for output
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");   // NOI18N
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");  // NOI18N
        transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");  // NOI18N
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");  // NOI18N
        
        // indent the output to make it more legible... 
        try {
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");  // NOI18N
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");  // NOI18N
        } catch (IllegalArgumentException e) {
            // the JAXP implementation doesn't support indentation, no big deal
        }
        
        transformer.transform(source, result);
    }
    

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws TransformerConfigurationException DOCUMENT ME!
     * @throws TransformerException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public static byte[] writeToBytes(Document document)
        throws TransformerConfigurationException, TransformerException, Exception {
        byte[] ret = null;
        
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(bos);
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");  // NOI18N
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");  // NOI18N
        transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");  // NOI18N
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");  // NOI18N

        // indent the output to make it more legible...
        try {
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");  // NOI18N
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");   // NOI18N
        } catch (IllegalArgumentException e) {
            // the JAXP implementation doesn't support indentation, no big deal
        }
        
        try {
            transformer.transform(source, result); 
            ret = bos.toByteArray();
        } finally {
            if (bos != null) {
                bos.flush();
                bos.close();
            }
        }       
        
        return ret;
    }

    /**
     * Description of the Method
     *
     * @param namespaceAware  Description of the Parameter
     * @param source          Description of the Parameter
     * @return                Description of the Return Value
     * @exception Exception   Description of the Exception
     */
    private static Document createDocument(boolean namespaceAware,
                                           InputSource source)
             throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);
        //factory.setValidating();

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(source);

        document.normalize();

        return document;
    }


    /**
     * Description of the Method
     *
     * @param namespaceAware  Description of the Parameter
     * @param file            Description of the Parameter
     * @return                Description of the Return Value
     * @exception Exception   Description of the Exception
     */
    public static Document createDocument(boolean namespaceAware,
                                           File file)
             throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);
        //factory.setValidating();

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);

        document.normalize();

        return document;
    }

    /**
     * Description of the Method
     *
     * @param namespaceAware  Description of the Parameter
     * @param xml             Description of the Parameter
     * @return                Description of the Return Value
     * @exception Exception   Description of the Exception
     */
    public static Document createDocumentFromXML(boolean namespaceAware,
                                                 String xml)
             throws Exception {
        return createDocument(namespaceAware,
                new InputSource(new StringReader(xml)));
    }
    
    /*
    public static void copyElementAttributes(Element oldElement, Element newElement) {
        NamedNodeMap attrs = oldElement.getAttributes();
        for (int k = 0; k < attrs.getLength(); k++) {
            Node attrNode = attrs.item(k);
            String name = attrNode.getNodeName();
            String value = attrNode.getNodeValue();
            newElement.setAttribute(name, value);
        }
    }
    */
    
    public static Map<String, String> getNamespaceMap(Document document) {
        Map<String, String> nsMap = new HashMap<String, String>();

        NamedNodeMap map = document.getDocumentElement().getAttributes();
        for (int j = 0; j < map.getLength(); j++) {
            Node n = map.item(j);
            String attrName = ((Attr)n).getName();
            String attrValue = ((Attr)n).getValue();
            if (attrName != null && attrValue != null) {
                if (attrName.trim().startsWith("xmlns:")) {
                    nsMap.put(attrValue, attrName.substring(6));
                }
            }
        }

        return nsMap;
    }

    public static QName getAttributeNSName(Element e, String attrName) {
        String attrValue = e.getAttribute(attrName);
        return getNSName(e, attrValue);
    }
    
    private static QName getNSName(Element e, String qname) {
        if (qname == null) {
            return null;
        }
        int i = qname.indexOf(':');
        if (i > 0) {
            String name = qname.substring(i + 1);
            String prefix = qname.substring(0, i);
            return new QName(getNamespaceURI(e, prefix), name);
        } else {
            return new QName(qname);
        }
    }
        
    public static String getNamespaceURI(Element el, String prefix) {
        if ((prefix == null) || (prefix.length() < 1)) {
            return "";
        }
        prefix = prefix.trim();
        try {
            NamedNodeMap map = el.getOwnerDocument().getDocumentElement().getAttributes();
            for (int j = 0; j < map.getLength(); j++) {
                Node n = map.item(j);
                String attrName = ((Attr)n).getName();
                if (attrName != null) {
                    if (attrName.trim().equals("xmlns:" + prefix)) {
                        return ((Attr)n).getValue();
                    }
                }
            }
        } catch (Exception e) {
        }
        
        return "";
    }
    
}

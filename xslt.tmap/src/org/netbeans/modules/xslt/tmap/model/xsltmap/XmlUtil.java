/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.tmap.model.xsltmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class XmlUtil {
    public static String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String UTF8 = "UTF-8"; // NOI18N
    private static final String XSLT_MAP_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + LINE_SEPARATOR
            + "<xsltmap>"
            + LINE_SEPARATOR
            + "</xsltmap>"
            + LINE_SEPARATOR;
    
    private XmlUtil() {
    }
    
    public static FileObject createTemplateXsltMapFo(Project project) throws IOException {
        if (project == null) {
            throw new IllegalArgumentException("project shouldn't be null");
        }
        FileObject xsltMapFo = Util.getXsltMapFo(project);
        if (xsltMapFo != null) {
            return xsltMapFo;
        }

        FileObject projectSource = Util.getProjectSource(project);
        assert projectSource != null;
        
        File xsltMapFile = new File(FileUtil.toFile(projectSource).getPath(),XsltMapConst.XSLTMAP+"."+XsltMapConst.XML);
        if (xsltMapFile != null) {
            xsltMapFile.createNewFile();
        }
        xsltMapFo = FileUtil.toFileObject(xsltMapFile);
        
        Document document = XMLUtil.createDocument("xsltmap",null, null,null);
        document.setXmlStandalone(true);
        
        FileLock fileLock = null;
        OutputStream outputStream = null;
        try {
            fileLock = xsltMapFo.lock();
            outputStream = xsltMapFo.getOutputStream(fileLock);
            XMLUtil.write(document,outputStream,XmlUtil.UTF8);
        } finally {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
            if (fileLock != null) {
                fileLock.releaseLock();
            }
        }
        
//        xsltMapFo = projectSource.getFileObject(XsltMapConst.XSLTMAP+"."+XsltMapConst.XML);
        return xsltMapFo;
    }

    public static FileObject createNewXmlFo(String path, String fileNameNoExt, String namespaceURI) throws IOException {
        if (path == null || fileNameNoExt == null) {
            throw new IllegalArgumentException("path and fileName shouldn't be null");
        }
        
        File xmlFile = new File(path,fileNameNoExt+".xml");
        if (xmlFile != null) {
            xmlFile.createNewFile();
        }
        FileObject xmlFileFo = FileUtil.toFileObject(xmlFile);
        
        Document document = XMLUtil.createDocument("transformmap",namespaceURI, null,null);
        document.setXmlStandalone(true);
        
        FileLock fileLock = null;
        OutputStream outputStream = null;
        try {
            fileLock = xmlFileFo.lock();
            outputStream = xmlFileFo.getOutputStream(fileLock);
            XMLUtil.write(document,outputStream,XmlUtil.UTF8);
        } finally {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
            if (fileLock != null) {
                fileLock.releaseLock();
            }
        }
        
//        xsltMapFo = projectSource.getFileObject(XsltMapConst.XSLTMAP+"."+XsltMapConst.XML);
        return xmlFileFo;
    }

    /**
     * This method is used from anttask so it shouldn't use FileObject at all
     */
    public static Document getDocument(File file) {
        return getDocument(file, false);
    }

    /**
     * This method is used from anttask so it shouldn't use FileObject at all
     */
    public static Document getDocument(File file, boolean supportNamespace) {
//        return getDocument(FileUtil.toFileObject(file));
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        docFactory.setNamespaceAware(supportNamespace);
        DocumentBuilder docBuilder;
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
            return null;
        }
        Document document = null;
        
        
        FileLock fLock = null;
        try {
// TODO a            
//            fLock = fo.lock();
            
            
            document = docBuilder.parse(file);
            document.setXmlStandalone(true);
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            document = null;
        } catch (SAXException ex) {
            ex.printStackTrace();
            document = null;
        } catch(IOException ex) {
            ex.printStackTrace();
            document = null;
        }/* finally {
            if (fLock != null) {
                fLock.releaseLock();
            }
        }*/
        
        return document;        
    }
    
    public static Document getDocument(FileObject fo) {
        if (fo == null || !fo.canRead()) {
            return null;
        }
         
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        DocumentBuilder docBuilder;
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
            return null;
        }
        Document document = null;
        
        
        FileLock fLock = null;
        try {
// TODO a            
//            fLock = fo.lock();
            document = docBuilder.parse(fo.getInputStream());
            document.setXmlStandalone(true);
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            document = null;
        } catch (SAXException ex) {
            ex.printStackTrace();
            document = null;
        } catch(IOException ex) {
            ex.printStackTrace();
            document = null;
        }/* finally {
            if (fLock != null) {
                fLock.releaseLock();
            }
        }*/
        
        return document;
    }
     
    public static  Node getElementByTagName(NodeList children, String tagName) {
        if (children == null || tagName == null) {
            return null;
        }
        Node requredNode = null;
        int length = children.getLength();
        for (int i = 0; i < children.getLength(); i++) {
            Node tmpNode = children.item(i);
            if (tagName.equals(tmpNode.getNodeName())) {
                requredNode = tmpNode;
                break;
            }
        }
        return requredNode;
    }
    
    public static Node getElementByTagName(Document document, String tagName) {
        if (document == null || tagName == null) {
            return null;
        }
        
        NodeList nodes = document.getElementsByTagName(tagName);
        if (nodes == null || nodes.getLength() < 1) {
            return null;
        }
        return nodes.item(0);
    }
    
    public static String getAttrValue(NamedNodeMap attrs, String attrName) {
        if (attrs == null || attrName == null) {
            return null;
        }
        String attrValue = null;
        
        Node attrNode = attrs.getNamedItem(attrName);
        if (attrNode == null || Node.ATTRIBUTE_NODE != attrNode.getNodeType()) {
            return null;
        }
        
        attrValue = attrNode.getNodeValue();
        return attrValue;
    }
    
    public static Attr createAttr(String attrName, String attrValue, Document document) {
        if (attrName == null 
                || attrName.length() == 0
                || attrValue == null 
                || document == null) {
            return null;
        }
        
        Attr attribute = document.createAttribute(attrName);
        attribute.setValue(attrValue);
        return attribute;
    }
    
    
}

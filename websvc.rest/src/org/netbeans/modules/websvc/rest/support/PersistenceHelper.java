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

package org.netbeans.modules.websvc.rest.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author PeterLiu
 */
public class PersistenceHelper {
    
    private static final String PERSISTENCE_DTD_1_0 = "resources/persistence_1_0.dtd";  //NOI18N
    private static final String PERSISTENCE_UNIT_TAG = "persistence-unit";      //NOI18N
    private static final String CLASS_TAG = "class";                //NOI18N
    private static final String PROPERTIES_TAG = "properties";      //NOI18N
    private static final String NAME_ATTR = "name";                 //NOI18N
    private static final String EXCLUDE_UNLISTED_CLASSES_TAG = "exclude-unlisted-classes";      //NOI18N
    
    private static int TIME_TO_WAIT = 300;
    
    public static String getPersistenceUnitName(Project project) {
        FileObject fobj = getPersistenceXML(project);
        
        if (fobj != null) {
            Document document = getDocument(fobj);
            
            Element puElement = getPersistenceUnitElement(document);
            if (puElement != null) {
                return puElement.getAttribute(NAME_ATTR);
            }
        }
        
        return null;
    }
    
    public static void addEntityClasses(Project project, Collection<String> classNames) throws IOException {
        List<String> toAdd = new ArrayList<String>(classNames);
        FileObject fobj = getPersistenceXML(project);
        Document document = getDocument(fobj);
        Element puElement = getPersistenceUnitElement(document);
        NodeList nodes = puElement.getElementsByTagName(CLASS_TAG);
        int length = nodes.getLength();
        
        for (int i = 0; i < length; i++) {
            toAdd.remove(getValue((Element) nodes.item(i)));
        }
        
        Element propElement = getPropertiesElement(document);
        
        for (String className : toAdd) {   
            puElement.insertBefore(createElement(document, CLASS_TAG, className),
                    propElement);
        }
        
        writeDocument(fobj, document);
    }
    
    
    public static void unsetExcludeEnlistedClasses(Project project) throws IOException {
        FileObject fobj = getPersistenceXML(project);
        Document document = getDocument(fobj);
        Element puElement = getPersistenceUnitElement(document);
        NodeList nodes = puElement.getElementsByTagName(EXCLUDE_UNLISTED_CLASSES_TAG);
    
        if (nodes.getLength() > 0) {
            setValue((Element) nodes.item(0), "false");  //NOI18N
        } else {
            puElement.insertBefore(createElement(document, EXCLUDE_UNLISTED_CLASSES_TAG, "false"),  //NOI18N
                    getPropertiesElement(document));
        }
        
        writeDocument(fobj, document);
    }
     
    private static String getValue(Element element) {
        Node child = element.getFirstChild();
        
        if (child instanceof Text) {
            return ((Text) child).getWholeText();
        }
        
        return "";      //NOI18N
    }
    
    private static void setValue(Element element, String value) {
        Node child = element.getFirstChild();
        
        if (child instanceof Text) {
            ((Text) child).setData(value);
        }
    }
    
    private static Element createElement(Document document,
            String tag, String value) {
        Element element = document.createElement(tag);
        Text text = document.createTextNode(value);
        element.appendChild(text);
        
        return element;
    }
    
    private boolean containsValue(Element element, String value) {
        Node child = element.getFirstChild();
        
        if (child instanceof Text) {
            return (((Text) child).getWholeText().equals(value));
        }
        
        return false;
    }
    public static Element getPersistenceUnitElement(Document document) {
        NodeList nodeList = document.getElementsByTagName(PERSISTENCE_UNIT_TAG);
        
        if (nodeList.getLength() > 0) {
            return (Element) nodeList.item(0);
        }
        
        return null;
    }
    
    public static Element getPropertiesElement(Document document) {
       NodeList nodeList = document.getElementsByTagName(PROPERTIES_TAG);
        
        if (nodeList.getLength() > 0) {
            return (Element) nodeList.item(0);
        }
        
        return null;
    }      
    
    private static FileObject getPersistenceXML(Project project) {
        RestSupport rs = project.getLookup().lookup(RestSupport.class);
        if (rs != null) {
            return rs.getPersistenceXml();
        }
        return null;
    }
    
    private static void writeDocument(final FileObject fobj, final Document document) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                FileLock lock = null;
                OutputStream os = null;
                
                try {
                    DocumentType docType = document.getDoctype();
                    TransformerFactory factory = TransformerFactory.newInstance();
                    Transformer transformer = factory.newTransformer();
                    DOMSource source = new DOMSource(document);
                    
                    lock = fobj.lock();
                    os = fobj.getOutputStream(lock);
                    StreamResult result = new StreamResult(os);
                    
                    //transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, docType.getPublicId());
                    //transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, docType.getSystemId());
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");        //NOI18N
                    transformer.setOutputProperty(OutputKeys.METHOD, "xml");        //NOI18N
                    transformer.transform(source, result);
                    
                    //transformer.transform(source, new StreamResult(System.out));
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        }, TIME_TO_WAIT);
    }
    
    private static Document getDocument(FileObject fobj) {
        Document document = null;
        DocumentBuilder builder = getDocumentBuilder();
        
        if (builder == null)
            return null;
        
        FileLock lock = null;
        InputStream is = null;
        
        try {
            lock = fobj.lock();
            is = fobj.getInputStream();
            document = builder.parse(is);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
            if (lock != null) {
                lock.releaseLock();
            }
        }
        
        
        return document;
    }
    
    
    private static DocumentBuilder getDocumentBuilder() {
        DocumentBuilder builder = null;
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setIgnoringComments(false);
        factory.setIgnoringElementContentWhitespace(false);
        factory.setCoalescing(false);
        factory.setExpandEntityReferences(false);
        factory.setValidating(false);
        
        try {
            builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new SunWebDTDResolver());
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return builder;
    }
    
    /**
     *
     *
     */
    private static class SunWebDTDResolver implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId) {
            String dtd = PERSISTENCE_DTD_1_0;
            
            if (dtd != null) {
                InputStream is = this.getClass().getResourceAsStream(dtd);
                InputStreamReader isr = new InputStreamReader(is);
                return new InputSource(isr);
            } else {
                return null;
            }
        }
    }
}

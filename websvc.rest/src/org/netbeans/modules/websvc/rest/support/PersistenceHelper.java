/*
 * PersistenceHelper.java
 *
 * Created on March 30, 2007, 10:02 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBeanModel;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
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
    
    private static String getValue(Element element) {
        Node child = element.getFirstChild();
        
        if (child instanceof Text) {
            return ((Text) child).getWholeText();
        }
        
        return "";      //NOI18N
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
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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

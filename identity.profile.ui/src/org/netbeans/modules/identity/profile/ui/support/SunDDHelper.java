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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.identity.profile.ui.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
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
 * Helper class for manipulating sun dd files.
 *
 * Created on August 7, 2006, 12:11 PM
 *
 * @author ptliu
 */
public class SunDDHelper {
    private static final String SUN_WEB_APP_TAG = "sun-web-app"; //NOI18N
    
    private static final String HTTPSERVLET_SECURITY_PROVIDER = "httpservlet-security-provider";    //NOI18N
    
    private static final String AM_HTTP_PROVIDER = "AMHttpProvider";    //NOI18N
    
    private static final String SECURITY_ROLE_MAPPING_TAG = "security-role-mapping";    //NOI18N
    
    private static final String ROLE_NAME_TAG = "role-name";    //NOI18N
    
    private static final String PRINCIPAL_NAME_TAG = "principal-name";  //NOI18N
    
    private static final String AUTHENTICATED_USERS = "AUTHENTICATED_USERS";    //NOI18N
    
    private static final String SUN_WEB_SYSTEM_ID_2_4 = "http://www.sun.com/software/appserver/dtds/sun-web-app_2_4-1.dtd";   //NOI18N
    
    private static final String SUN_WEB_SYSTEM_ID_2_5 = "http://www.sun.com/software/appserver/dtds/sun-web-app_2_5-0.dtd";   //NOI18N
    
    private static final String SUN_WEB_DTD_2_4 = "resources/sun-web-app_2_4-1.dtd";    //NOI18N
    
    private static final String SUN_WEB_DTD_2_5 = "resources/sun-web-app_2_5-0.dtd";    //NOI18N
    
    private static int TIME_TO_WAIT = 300;
    
    private FileObject sunDD;
    private Document document;
    
    public SunDDHelper(FileObject sunDD) {
        this.sunDD = sunDD;
    }
    
    public void addSecurityRoleMapping() {
        final FileChangeListener fcl = new FileChangeAdapter() {
            public void fileChanged(FileEvent event) {
                boolean isModified = false;
                Element sunWebApp = getSunWebAppElement();
                String value = sunWebApp.getAttribute(HTTPSERVLET_SECURITY_PROVIDER);
                
                if (value == null || !value.equals(AM_HTTP_PROVIDER)) {
                    sunWebApp.setAttribute(HTTPSERVLET_SECURITY_PROVIDER, AM_HTTP_PROVIDER);
                    isModified = true;
                }
                
                Element mapping = getSecurityRoleMapping(AUTHENTICATED_USERS);
                
                if (mapping == null) {
                    sunWebApp.appendChild(createSecurityRoleMapping(AUTHENTICATED_USERS));
                    isModified = true;
                } else {
                    Element principal = getPrincipalName(mapping, AUTHENTICATED_USERS);
                    
                    if (principal == null) {
                        mapping.appendChild(createElement(PRINCIPAL_NAME_TAG, AUTHENTICATED_USERS));
                        isModified = true;
                    }
                }
                
                if (isModified) writeDocument();
                
                sunDD.removeFileChangeListener(this);
            }
        };
        
        sunDD.addFileChangeListener(fcl);
    }
    
    public void removeSecurityRoleMapping() {
        final FileChangeListener fcl = new FileChangeAdapter() {
            public void fileChanged(FileEvent event) {
                boolean isModified = false;
                Element sunWebApp = getSunWebAppElement();
                
                String value = sunWebApp.getAttribute(HTTPSERVLET_SECURITY_PROVIDER);
                
                if (value != null && value.equals(AM_HTTP_PROVIDER)) {
                    sunWebApp.removeAttribute(HTTPSERVLET_SECURITY_PROVIDER);
                    isModified = true;
                }
                
                Element mapping = getSecurityRoleMapping(AUTHENTICATED_USERS);
                
                if (mapping != null) {
                    Element principal = getPrincipalName(mapping, AUTHENTICATED_USERS);
                    
                    if (principal != null) {
                        mapping.removeChild(principal);
                        isModified = true;
                    }
                    
                    if (mapping.getElementsByTagName(PRINCIPAL_NAME_TAG).getLength() == 0) {
                        sunWebApp.removeChild(mapping);
                        isModified = true;
                    }
                }
                
                if (isModified) writeDocument();
                
                sunDD.removeFileChangeListener(this);
            }
        };
        
        sunDD.addFileChangeListener(fcl);
    }
    
    private Element getSunWebAppElement() {
        Document document = getDocument();
        NodeList nodes = document.getElementsByTagName(SUN_WEB_APP_TAG);
        
        return (Element) nodes.item(0);
    }
    
    private Element getSecurityRoleMapping(String value) {
        Document document = getDocument();
        NodeList mappings = document.getElementsByTagName(SECURITY_ROLE_MAPPING_TAG);
        int length = mappings.getLength();
        
        for (int i = 0; i < length; i++) {
            Element mapping = (Element) mappings.item(i);
            NodeList roleNames = mapping.getElementsByTagName(ROLE_NAME_TAG);
            
            if (roleNames.getLength() > 0) {
                Element roleName = (Element) roleNames.item(0);
                
                if (containsValue(roleName, value)) {
                    return mapping;
                }
            }
        }
        
        return null;
    }
    
    private Element createSecurityRoleMapping(String value) {
        Document document = getDocument();
        Element mapping = document.createElement(SECURITY_ROLE_MAPPING_TAG);
        Element roleName = createElement(ROLE_NAME_TAG, value);
        Element principal = createElement(PRINCIPAL_NAME_TAG, value);
        mapping.appendChild(roleName);
        mapping.appendChild(principal);
        
        return mapping;
    }
    
    private Element createElement(String tag, String value) {
        Document document = getDocument();
        Element element = document.createElement(tag);
        Text text = document.createTextNode(value);
        element.appendChild(text);
        
        return element;
    }
    
    private Element getPrincipalName(Element mapping, String value) {
        NodeList nodes = mapping.getElementsByTagName(PRINCIPAL_NAME_TAG);
        int length = nodes.getLength();
        
        for (int i = 0; i < length; i++) {
            Element principal = (Element) nodes.item(i);
            
            if (containsValue(principal, value)) {
                return principal;
            }
        }
        
        return null;
    }
    
    private boolean containsValue(Element element, String value) {
        Node child = element.getFirstChild();
        
        if (child instanceof Text) {
            return (((Text) child).getWholeText().equals(value));
        }
        
        return false;
    }
    
    private void writeDocument() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                FileLock lock = null;
                OutputStream os = null;
                
                try {
                    Document document = getDocument();
                    DocumentType docType = document.getDoctype();
                    TransformerFactory factory = TransformerFactory.newInstance();
                    Transformer transformer = factory.newTransformer();
                    DOMSource source = new DOMSource(document);
                    
                    lock = sunDD.lock();
                    os = sunDD.getOutputStream(lock);
                    StreamResult result = new StreamResult(os);
                    
                    transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, docType.getPublicId());
                    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, docType.getSystemId());
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
    
    private Document getDocument() {
        if (document == null) {
            DocumentBuilder builder = getDocumentBuilder();
            if (builder == null)
                return null;
            
            FileLock lock = null;
            InputStream is = null;
            
            try {
                lock = sunDD.lock();
                is = sunDD.getInputStream();
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
        }
        
        return document;
    }
    
    private DocumentBuilder getDocumentBuilder() {
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
            String dtd = null;
            
            if (systemId.equals(SUN_WEB_SYSTEM_ID_2_4)) {
                dtd = SUN_WEB_DTD_2_4;
            } else if (systemId.equals(SUN_WEB_SYSTEM_ID_2_5)) {
                dtd = SUN_WEB_DTD_2_5;
            }
            
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

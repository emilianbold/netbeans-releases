/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.identity.profile.ui.support;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.modules.identity.profile.ui.support.J2eeProjectHelper.ProjectType;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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
    
    private static final String SERVLET_TAG = "servlet";            //NOI18N
    
    private static final String SERVLET_NAME_TAG = "servlet-name";  //NOI18N
    
    private static final String ENTERPRISE_BEANS_TAG = "enterprise-beans";  //NOI18N
    
    private static final String EJB_TAG = "ejb";            //NOI18N
    
    private static final String EJB_NAME_TAG = "ejb-name";  //NOI18N
    
    private static final String SUN_APPLICATION_CLIENT_TAG = "sun-application-client";  //NOI18N
    
    private static final String SERVICE_REF_TAG = "service-ref";    //NOI18N
    
    private static final String SERVICE_REF_NAME_TAG = "service-ref-name";  //NOI18N
    
    private static final String WEBSERVICE_ENDPOINT_TAG = "webservice-endpoint"; //NOI18N
    
    private static final String PORT_COMPONENT_NAME_TAG = "port-component-name"; //NOI18N
    
    private static final String PORT_INFO_TAG = "port-info";    //NOI18N
    
    private static final String WSDL_PORT_TAG = "wsdl-port";    //NOI18N
    
    private static final String NAMESPACE_URI_TAG = "namespaceURI";     //NOI18N
    
    private static final String LOCALPART_TAG = "localpart";    //NOI18N
    
    private static final String MESSAGE_SECURITY_BINDING_TAG = "message-security-binding";  //NOI18N
    
    private static final String MESSAGE_SECURITY_TAG = "message-security";      //NOI18N
    
    private static final String MESSAGE_TAG = "message";        //NOI18N
    
    private static final String REQUEST_PROTECTION_TAG = "request-protection";  //NOI18N
    
    private static final String RESPONSE_PROTECTION_TAG = "response-protection";    //NOI18N
    
    private static final String AUTH_LAYER_ATTR = "auth-layer";     //NOI18N
    
    private static final String PROVIDER_ID_ATTR = "provider-id";   //NOI18N
    
    private static final String AUTH_SOURCE_ATTR = "auth-source"; //NOI18N
    
    private static final String AM_SERVER_PROVIDER_PREFIX = "AMServerProvider-"; //NOI18N
    
    private static final String AM_CLIENT_PROVIDER = "AMClientProvider";        //NOI18N
    
    private static final String REQUEST_POLICY_AUTH_SOURCE = "content";          //NOI18N
    
    private static final String RESPONSE_POLICY_AUTH_SOURCE = "content";        //NOI18N
    
    private static final String AUTH_LAYER = "SOAP";                            //NOI18N
    
    private static final String AUTHENTICATED_USERS = "AUTHENTICATED_USERS";    //NOI18N
    
    private static final String CONTEXT_ROOT_TAG = "context-root";  //NOI18N
    
    private static final String CLASS_LOADER_TAG = "class-loader";  //NOI18N
    
    private static final String SUN_WEB_SYSTEM_ID_2_4 = "http://www.sun.com/software/appserver/dtds/sun-web-app_2_4-1.dtd";   //NOI18N
    
    private static final String SUN_WEB_SYSTEM_ID_2_5 = "http://www.sun.com/software/appserver/dtds/sun-web-app_2_5-0.dtd";   //NOI18N
    
    private static final String SUN_EJB_SYSTEM_ID_3_0 = "http://www.sun.com/software/appserver/dtds/sun-ejb-jar_3_0-0.dtd";   //NOI18N
    
    private static final String SUN_APPCLIENT_SYSTEM_ID_5_0 = "http://www.sun.com/software/appserver/dtds/sun-application-client_5_0-0.dtd";       //NOI18N
    
    private static final String SUN_WEB_DTD_2_4 = "resources/sun-web-app_2_4-1.dtd";    //NOI18N
    
    private static final String SUN_WEB_DTD_2_5 = "resources/sun-web-app_2_5-0.dtd";    //NOI18N
    
    private static final String SUN_EJB_DTD_3_0 = "resources/sun-ejb-jar_3_0-0.dtd";    //NOI18N
    
    private static final String SUN_APPCLIENT_DTD_5_0 = "resources/sun-application-client_5_0-0.dtd";  //NOI18N
    
    private static final String IDENT = "    ";     //NOI18N
   
    private static int TIME_TO_WAIT = 300;
    
    private FileObject sunDD;
    private Document document;
    private ProjectType type;
    
    public SunDDHelper(FileObject sunDD, ProjectType type) {
        this.sunDD = sunDD;
        this.type = type;
    }
    
    public void addSecurityRoleMapping() {
        //final FileChangeListener fcl = new FileChangeAdapter() {
        //    public void fileChanged(FileEvent event) {
        boolean isModified = false;
        Element sunWebApp = getRootElement();
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
        
        //sunDD.removeFileChangeListener(this);
        //}
        //};
        
        //sunDD.addFileChangeListener(fcl);
    }
    
    public void removeSecurityRoleMapping() {
        //final FileChangeListener fcl = new FileChangeAdapter() {
        //    public void fileChanged(FileEvent event) {
        boolean isModified = false;
        Element sunWebApp = getRootElement();
        
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
        
        //sunDD.removeFileChangeListener(this);
        //}
        //};
        
        //sunDD.addFileChangeListener(fcl);
    }
    
    public void setServiceMessageSecurityBinding(String svcDescName,
            String pcName, String providerId) {
        Element root = getRootElement();
        Element component = null;
        String componentTag = null;
        String componentNameTag = null;
    
        if (type == ProjectType.WEB) {
            componentTag = SERVLET_TAG;
            componentNameTag = SERVLET_NAME_TAG;
        } else if (type == ProjectType.EJB) {
            componentTag = EJB_TAG;
            componentNameTag = EJB_NAME_TAG;
        }
        
        component = getComponentElement(root, componentTag, componentNameTag,
                pcName);
        
        if (component == null) {
            component = createComponentElement(componentTag, componentNameTag,
                    pcName);
            
            if (type == ProjectType.WEB) {
                insertSunWebComponent(root, component);
            } else {
                root.appendChild(component);
            }
        }
        
        Element endpointElement = getElement(component, WEBSERVICE_ENDPOINT_TAG);
        
        if (endpointElement == null) {
            endpointElement = createWebServiceEndpointElement(pcName);
            component.appendChild(endpointElement);
        }
        
        Element binding = getElement(endpointElement, MESSAGE_SECURITY_BINDING_TAG);
        
        if (binding == null) {
            binding = createSecurityBindingElement();
            endpointElement.appendChild(binding);
        }
        
        binding.setAttribute(PROVIDER_ID_ATTR,
                AM_SERVER_PROVIDER_PREFIX + providerId);
        
        writeDocument();
    }
    
    public void setServiceRefMessageSecurityBinding(String serviceRefName,
            String namespaceURI, String localPart) {
        Element root = getRootElement();
        
        if (type == ProjectType.EJB) {
            String ejbName = getEjbName(serviceRefName);
            Element component = getComponentElement(root, EJB_TAG, EJB_NAME_TAG, ejbName);
            if (component == null) {
                component = createEjbElement(ejbName);
                root.appendChild(component);
            }
            
            root = component;
        }
        
        Element serviceRef = getServiceRefElement(root, serviceRefName,
                namespaceURI, localPart);
        
        if (serviceRef == null) {
            serviceRef = createServiceRefElement(serviceRefName,
                    namespaceURI, localPart);
            
            if (type == ProjectType.WEB) {
                insertSunWebComponent(root, serviceRef);
            } else {
                root.appendChild(serviceRef);
            }
        }
        
        Element portInfo = getElement(serviceRef, PORT_INFO_TAG);
        
        Element binding = getElement(portInfo, MESSAGE_SECURITY_BINDING_TAG);
        
        if (binding == null) {
            binding = createSecurityBindingElement();
            portInfo.appendChild(binding);
        }
        
        binding.setAttribute(PROVIDER_ID_ATTR, AM_CLIENT_PROVIDER);
        
        writeDocument();
    }
    
    public void removeServiceMessageSecurityBinding(String svcDescName,
            String pcName) {
        Element root = getRootElement();
        Element component = null;
        String componentTag = null;
        String componentNameTag = null;
        
        if (type == ProjectType.WEB) {
            componentTag = SERVLET_TAG;
            componentNameTag = SERVLET_NAME_TAG;
        } else if (type == ProjectType.EJB) {
            componentTag = EJB_TAG;
            componentNameTag = EJB_NAME_TAG;
        }
        
        component = getComponentElement(root, componentTag, componentNameTag,
                pcName);
        
        if (component != null) {
            Element endpointElement = getElement(component, WEBSERVICE_ENDPOINT_TAG);
            
            if (endpointElement != null) {
                component.removeChild(endpointElement);
            }
        }
        
        writeDocument();
    }
    
    public void removeServiceRefMessageSecurityBinding(String serviceRefName,
            String namespaceURI, String localPart) {
        Element root = getRootElement();
        
        if (type == ProjectType.EJB) {
            String ejbName = getEjbName(serviceRefName);
            root = getComponentElement(root, EJB_TAG, EJB_NAME_TAG, ejbName);
            
            if (root == null) return;
        }
        
        Element serviceRef = getServiceRefElement(root, serviceRefName,
                namespaceURI, localPart);
        
        if (serviceRef != null) {
            root.removeChild(serviceRef);
        }
        
        writeDocument();
    }
    
    public boolean isServiceSecurityEnabled(String svcDescName, String pcName) {
        Element root = getRootElement();
        Element component = null;
        String componentTag = null;
        String componentNameTag = null;
        
        if (type == ProjectType.WEB) {
            componentTag = SERVLET_TAG;
            componentNameTag = SERVLET_NAME_TAG;
        } else if (type == ProjectType.EJB) {
            componentTag = EJB_TAG;
            componentNameTag = EJB_NAME_TAG;
        } else {
            return false;
        }
        
        component = getComponentElement(root, componentTag, componentNameTag,
                pcName);
        
        if (component != null) {
            Element endpointElement = getElement(component, WEBSERVICE_ENDPOINT_TAG);
            
            if (endpointElement != null) {
                Element binding = getElement(endpointElement, MESSAGE_SECURITY_BINDING_TAG);
                
                if (binding != null) {
                    if (binding.getAttribute(PROVIDER_ID_ATTR).startsWith(AM_SERVER_PROVIDER_PREFIX)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    public boolean isClientSecurityEnabled(String serviceRefName,
            String namespaceURI, String localPart) {
        Element root = getRootElement();
        
        if (type == ProjectType.EJB) {
            String ejbName = getEjbName(serviceRefName);
            root = getComponentElement(root, EJB_TAG, EJB_NAME_TAG, ejbName);
            
            if (root == null) return false;
        }
        
        Element serviceRef = getServiceRefElement(root, serviceRefName,
                namespaceURI, localPart);
        
        if (serviceRef != null) {
            Element portInfo = getElement(serviceRef, PORT_INFO_TAG);
            Element binding = getElement(serviceRef, MESSAGE_SECURITY_BINDING_TAG);
            
            if (binding != null) {
                if (AM_CLIENT_PROVIDER.equals(binding.getAttribute(PROVIDER_ID_ATTR))) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private Element getRootElement() {
        String tagName = null;
        
        if (type == ProjectType.WEB) {
            tagName = SUN_WEB_APP_TAG;
        } else if (type == ProjectType.EJB) {
            tagName = ENTERPRISE_BEANS_TAG;
        } else if (type == ProjectType.CLIENT) {
            tagName = SUN_APPLICATION_CLIENT_TAG;
        }
        
        return getElement(tagName);
    }
    
    private Element getComponentElement(Element root, String componentTag,
            String componentNameTag, String componentName) {
        NodeList components = root.getElementsByTagName(componentTag);
        int length = components.getLength();
        
        for (int i = 0; i < length; i++) {
            Element component = (Element) components.item(0);
            NodeList names = component.getElementsByTagName(componentNameTag);
            
            if (names.getLength() > 0) {
                Element name = (Element) names.item(0);
                
                if (containsValue(name, componentName)) {
                    return component;
                }
            }
        }
        
        return null;
    }
    
    private Element getServiceRefElement(Element root, String serviceRefName,
            String namespaceURI, String localPart) {
        Document document = getDocument();
        NodeList serviceRefs = document.getElementsByTagName(SERVICE_REF_TAG);
        int length = serviceRefs.getLength();
        
        for (int i = 0; i < length; i++) {
            Element serviceRef = (Element) serviceRefs.item(i);
            Element refName = getElement(serviceRef, SERVICE_REF_NAME_TAG);
            
            if (refName != null) {
                if (containsValue(refName, serviceRefName)) {
                    Element portInfo = getElement(serviceRef, PORT_INFO_TAG);
                    
                    if (portInfo == null) continue;
                    
                    Element wsdlPort = getElement(portInfo, WSDL_PORT_TAG);
                    
                    if (wsdlPort == null) continue;
                    
                    Element namespaceURIElement = getElement(wsdlPort, NAMESPACE_URI_TAG);
                    
                    if (namespaceURIElement != null) {
                        if (containsValue(namespaceURIElement, namespaceURI)) {
                            Element localPartElement = getElement(wsdlPort, LOCALPART_TAG);
                            
                            if (localPartElement != null) {
                                if (containsValue(localPartElement, localPart)) {
                                    return serviceRef;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    
    private Element createComponentElement(String componentTag,
            String componentNameTag, String componentName) {
        Document document = getDocument();
        Element componentElement = document.createElement(componentTag);
        componentElement.appendChild(createElement(componentNameTag, componentName));
        
        return componentElement;
    }
    
    private Element createServiceRefElement(String serviceRefName,
            String namespaceURI, String localPart) {
        Document document = getDocument();
        Element serviceRef = document.createElement(SERVICE_REF_TAG);
        serviceRef.appendChild(createElement(SERVICE_REF_NAME_TAG,
                serviceRefName));
        Element portInfo = document.createElement(PORT_INFO_TAG);
        serviceRef.appendChild(portInfo);
        Element wsdlPort = document.createElement(WSDL_PORT_TAG);
        wsdlPort.appendChild(createElement(NAMESPACE_URI_TAG, namespaceURI));
        wsdlPort.appendChild(createElement(LOCALPART_TAG, localPart));
        portInfo.appendChild(wsdlPort);
        
        return serviceRef;
    }
    
    private Element getElement(String tagName) {
        Document document = getDocument();
        
        NodeList elements = document.getElementsByTagName(tagName);
        
        if (elements.getLength() > 0) {
            return (Element) elements.item(0);
        }
        
        return null;
    }
    
    private Element getElement(Element component, String tagName) {
        NodeList elements = component.getElementsByTagName(tagName);
        
        if (elements.getLength() > 0) {
            return (Element) elements.item(0);
        }
        
        return null;
    }
    
    private Element createEjbElement(String ejbName) {
        Document document = getDocument();
        Element ejb = document.createElement(EJB_TAG);
        ejb.appendChild(createElement(EJB_NAME_TAG, ejbName));
        
        return ejb;
    }
    
    private Element createWebServiceEndpointElement(String endpointName) {
        Document document = getDocument();
        Element endpoint = document.createElement(WEBSERVICE_ENDPOINT_TAG);
        endpoint.appendChild(createElement(PORT_COMPONENT_NAME_TAG, endpointName));
        
        return endpoint;
    }
    
    private Element createSecurityBindingElement() {
        Document document = getDocument();
        Element binding = document.createElement(MESSAGE_SECURITY_BINDING_TAG);
        binding.setAttribute(AUTH_LAYER_ATTR, AUTH_LAYER);
        Element security = document.createElement(MESSAGE_SECURITY_TAG);
        security.appendChild(document.createElement(MESSAGE_TAG));
        Element request = document.createElement(REQUEST_PROTECTION_TAG);
        request.setAttribute(AUTH_SOURCE_ATTR, REQUEST_POLICY_AUTH_SOURCE);
        security.appendChild(request);
        Element response = document.createElement(RESPONSE_PROTECTION_TAG);
        response.setAttribute(AUTH_SOURCE_ATTR, RESPONSE_POLICY_AUTH_SOURCE);
        security.appendChild(response);
        binding.appendChild(security);
        
        return binding;
    }
    
    private void insertSunWebComponent(Element root, Element component) {
        Element classLoader = getElement(root, CLASS_LOADER_TAG);
        root.insertBefore(component, classLoader);
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
    
    private String getEjbName(String refName) {
        String ejbName = refName.substring(0, refName.indexOf("/"));        //NOI18N
        ejbName = ejbName.substring(ejbName.lastIndexOf(".")+1);            //NOI18N

        return ejbName;
    }
    
    private boolean containsValue(Element element, String value) {
        Node child = element.getFirstChild();
        
        if (child instanceof Text) {
            return (((Text) child).getWholeText().equals(value));
        }
        
        return false;
    }
    
    private void writeDocument() {
        beautify();
        
        //RequestProcessor.getDefault().post(new Runnable() {
        //   public void run() {
        //FileLock lock = null;
        OutputStream os = null;
        
        try {
            Document document = getDocument();
            DocumentType docType = document.getDoctype();
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            DOMSource source = new DOMSource(document);
            
            //lock = sunDD.lock();
            //os = sunDD.getOutputStream(lock);
            os = new FileOutputStream(FileUtil.toFile(sunDD));
            
            StreamResult result = new StreamResult(os);
            
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, docType.getPublicId());
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, docType.getSystemId());
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");        //NOI18N
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");        //NOI18N
            transformer.transform(source, result);
            
            //transformer.transform(source, new StreamResult(System.out));
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
            
            /*
            if (lock != null) {
                lock.releaseLock();
            }
             */
        }
        //}
        //}, TIME_TO_WAIT);
    }
    
    private Document getDocument() {
        if (document == null) {
            DocumentBuilder builder = getDocumentBuilder();
            if (builder == null)
                return null;
            
            //FileLock lock = null;
            InputStream is = null;
            
            try {
                //lock = sunDD.lock();
                //is = sunDD.getInputStream();
                is = new FileInputStream(FileUtil.toFile(sunDD));
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
                /*
                if (lock != null) {
                    lock.releaseLock();
                }
                 */
            }
        }
   
        return document;
    }
    
    private DocumentBuilder getDocumentBuilder() {
        DocumentBuilder builder = null;
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setIgnoringComments(false);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setCoalescing(false);
        factory.setExpandEntityReferences(false);
        factory.setValidating(false);
        
        try {
            builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new SunDTDResolver());
        } catch (ParserConfigurationException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return builder;
    }
    
    private void beautify() {
        beautify(getDocument().getDocumentElement(), "\n");   //NOI18N
    }
    
    private void beautify(Node node, String indent) {
        Document document = getDocument();
        NodeList children = node.getChildNodes();
        int length = children.getLength();
        ArrayList<Node> list = new ArrayList<Node>();
        
        for (int i = 0; i < length; i++) {
            list.add(children.item(i));
        }
        
        for (int i = 0; i < length; i++) {
            Node child = list.get(i);
           
            if (child instanceof Text) continue;
            
            node.insertBefore(document.createTextNode(indent + IDENT), child);
            beautify(child, indent + IDENT);
         
            if (i+1 == length) {
                node.appendChild(document.createTextNode(indent));
            }
        }
    }
    
    /**
     *
     *
     */
    private static class SunDTDResolver implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId) {
            String dtd = null;
            
            if (SUN_WEB_SYSTEM_ID_2_4.equals(systemId)) {
                dtd = SUN_WEB_DTD_2_4;
            } else if (SUN_WEB_SYSTEM_ID_2_5.equals(systemId)) {
                dtd = SUN_WEB_DTD_2_5;
            } else if (SUN_EJB_SYSTEM_ID_3_0.equals(systemId)) {
                dtd = SUN_EJB_DTD_3_0;
            } else if (SUN_APPCLIENT_SYSTEM_ID_5_0.equals(systemId)) {
                dtd = SUN_APPCLIENT_DTD_5_0;
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

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
 * $Id$
 */

package org.netbeans.modules.j2ee.websphere6.config.sync;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.*;
import org.netbeans.modules.j2ee.websphere6.dd.beans.DDXmiConstants;
import org.w3c.dom.*;

/**
 *
 * @author Dmitry Lipin
 */
public class EjbSynchronizer extends Synchronizer{
    
    private static final Logger LOGGER = Logger.getLogger(EjbSynchronizer.class.getName());
    
    private final File ejbjarFile ;
    private final File ibmejbjarbndFile;
    private XPath xpath = null;
    
    private boolean saveEjbJarNeeded = false;
    private boolean saveIbmEjbJarBndNeeded = false;
    private static final String EJB_JAR_PREFIX = DDXmiConstants.EJBJAR_HREF_PREFIX;
    
    /**
     * Synchronizes the ebj-jar.xml with ibm-ebj-jar-bnd.xmi
     */
    
    public EjbSynchronizer(File ejbjarFilePar, File ibmejbjarbndFilePar) {
        try {
            xpath = XPathFactory.newInstance(XPathConstants.DOM_OBJECT_MODEL).newXPath();
        } catch (Exception ex) {
            ex.printStackTrace();
            xpath = null;
        };
        this.ibmejbjarbndFile = ibmejbjarbndFilePar;
        this.ejbjarFile = ejbjarFilePar;
    }
    
    
    public synchronized void syncDescriptors() {
        if ((ejbjarFile != null) && (ibmejbjarbndFile != null)) {
            
            
            try {
                saveEjbJarNeeded = false;
                saveIbmEjbJarBndNeeded = false;
                //xpath.setNamespaceContext(new EjbJarNSC());
                Document ejbjarDocument = loadDocument(ejbjarFile);
                Document ibmejbjarbndDocument = loadDocument(ibmejbjarbndFile);
                
                NodeList beansList = (NodeList) xpath.
                        compile("/ejb-jar/enterprise-beans/*").
                        evaluate(ejbjarDocument, XPathConstants.NODESET);
                Node bindingsRoot = ibmejbjarbndDocument.getDocumentElement();
                
                if (bindingsRoot == null) {
                    return;
                }
                
                NodeList bindingsList = (NodeList) xpath.
                        compile("./"+ DDXmiConstants.EJB_BINDINGS_ID).
                        evaluate(bindingsRoot, XPathConstants.NODESET);
                
                
                for (int i = 0; i < beansList.getLength(); i++) {
                    Node node = beansList.item(i);
                    
                    String id = getBeanId(node);
                    if ((id == null) || (!bindingExists(bindingsRoot, id))) {
                        boolean neadCreateBinding = false;
                        if (id == null) {
                            id = getBeanIdFromBinding(node,bindingsList);
                            if(id==null) {
                                id = createBeanId(node);
                                neadCreateBinding = true;
                            }
                            Attr attribute = ejbjarDocument.createAttribute("id");
                            attribute.setValue(id);
                            node.getAttributes().setNamedItem(attribute);
                            saveEjbJarNeeded = true;
                        }
                        
                        
                        if(neadCreateBinding) {
                            Node binding = constructBinding(ibmejbjarbndDocument,
                                    getBeanName(node),
                                    getBeanId(node),
                                    getBeanType(node));
                            bindingsRoot.appendChild(ibmejbjarbndDocument.createTextNode("    "));
                            bindingsRoot.appendChild(binding);
                            bindingsRoot.appendChild(ibmejbjarbndDocument.createTextNode("\n"));                            
                            saveIbmEjbJarBndNeeded = true;
                        }                        
                    }
                }
                
                for (int i = 0; i < bindingsList.getLength(); i++) {
                    Node node = bindingsList.item(i);
                    
                    String id = getBindingId(node);
                    
                    if (!beanExists(ejbjarDocument, id)) {
                        bindingsRoot.removeChild(node);
                        
                        saveIbmEjbJarBndNeeded = true;
                    }
                }
                
                if (saveEjbJarNeeded) {
                    saveDocument(ejbjarDocument, ejbjarFile);
                }
                if (saveIbmEjbJarBndNeeded) {
                    saveDocument(ibmejbjarbndDocument, ibmejbjarbndFile);
                }
            } catch (XPathExpressionException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (XPathFactoryConfigurationException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (TransformerException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
    }
    private String getBeanIdFromBinding(Node beanNode, NodeList bindingsList) throws XPathFactoryConfigurationException, XPathExpressionException {
        String name = getBeanName(beanNode);
        for(int i=0;i<bindingsList.getLength();i++) {
            Node entBeanNode = ((Node) xpath.compile("./" + DDXmiConstants.ENTERPRISE_BEAN_ID).
                    evaluate(bindingsList.item(i), XPathConstants.NODE));
            Node hrefAttrNode = entBeanNode.getAttributes().getNamedItem("href");
            if(hrefAttrNode==null) { 
                continue;
            }
            String href = hrefAttrNode.getTextContent();
            String beanId = href.substring(href.indexOf(EJB_JAR_PREFIX) + EJB_JAR_PREFIX.length(),
                    href.indexOf(BINDING_SEPARATOR));
            if(beanId.equals(name)) {
                return href.substring(href.indexOf(EJB_JAR_PREFIX) + EJB_JAR_PREFIX.length());
            }
        }
        return null;
    }
    private String getBeanName(Node beanNode) throws XPathFactoryConfigurationException, XPathExpressionException {
        return ((Node) xpath.compile("child::ejb-name").evaluate(beanNode, XPathConstants.NODE)).getTextContent();
    }
    
    private String getBeanId(Node beanNode) {
        Node idNode = beanNode.getAttributes().getNamedItem("id");
        
        if (idNode != null) {
            return idNode.getTextContent();
        } else {
            return null;
        }
    }
    
    private String getBeanType(Node beanNode) throws XPathFactoryConfigurationException, XPathExpressionException {
        
        if (beanNode.getNodeName().equals("session")) {
            return DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_SESSION;
        }
        
        if (beanNode.getNodeName().equals("entity")) {
            String persistenceType = ((Node) xpath.compile("child::persistence-type").evaluate(beanNode, XPathConstants.NODE)).getTextContent();
            if (persistenceType.equals("Container")) {
                return DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_CONTAINER_MANAGED_ENTITY;
            } else {
                return "ejb:BeanManagedEntity";
            }
        }
        
        if (beanNode.getNodeName().equals("message-driven")) {
            return DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_MESSAGEDRIVEN;
        }
        
        return null;
    }
    
    private String createBeanId(Node beanNode) throws XPathFactoryConfigurationException, XPathExpressionException {
        String name = getBeanName(beanNode);
        
        return name + BINDING_SEPARATOR + new Date().getTime();
    }
    
    private boolean beanExists(Document document, String id) throws XPathExpressionException, XPathFactoryConfigurationException {
        
        String path = "/ejb-jar/enterprise-beans/*[@id=\"" + id + "\"]";
        
        Node node = (Node) xpath.compile(path).evaluate(document, XPathConstants.NODE);
        
        return node != null;
    }
    
    private String getBindingId(Node bindingNode) {
        Node idNode = bindingNode.getAttributes().getNamedItem("xmi:id");
        
        if (idNode != null) {
            return idNode.getTextContent();
        } else {
            return null;
        }
    }
    
    private boolean bindingExists(Node root, String id) throws XPathFactoryConfigurationException, XPathExpressionException {
        
        String path = "./" + DDXmiConstants.EJB_BINDINGS_ID + "[@id=\"" + id + "\"]";
        
        Node node = (Node) xpath.compile(path).evaluate(root, XPathConstants.NODE);
        
        return node != null;
    }
    
    private Node constructBinding(Document document, String name, String id, String type) {
        Element binding = document.createElement("ejbBindings");
        binding.setAttribute("jndiName", "ejb/" + id);
        binding.setAttributeNS("http://www.omg.org/XMI", "xmi:id", id);
        
        Element bean = document.createElement(DDXmiConstants.ENTERPRISE_BEAN_ID);
        
        bean.setAttribute("href", EJB_JAR_PREFIX + id);
        bean.setAttributeNS("http://www.omg.org/XMI", "xmi:type", type);
        binding.appendChild(document.createTextNode("\n        "));
        binding.appendChild(bean);
        binding.appendChild(document.createTextNode("\n"));
        return binding;
    }
}

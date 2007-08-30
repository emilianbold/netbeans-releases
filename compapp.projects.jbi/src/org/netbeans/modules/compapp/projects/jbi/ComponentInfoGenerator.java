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

package org.netbeans.modules.compapp.projects.jbi;

import java.util.List;
import org.w3c.dom.DOMException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.netbeans.modules.compapp.projects.jbi.api.JbiDefaultComponentInfo;
import org.netbeans.modules.compapp.projects.jbi.descriptor.XmlUtil;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentDocument;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;

/**
 * Helper class to create component related config files 
 * (ComponentInformation.xml and BindingComponentInformation.xml)
 * for a CompApp project.
 * 
 * @aughor jqian
 */
public class ComponentInfoGenerator implements Serializable {
    
    private String confDir;
    private List<JBIComponentStatus> compList;
        
    private static final String COMPONENT_INFO_FILENAME = "ComponentInformation.xml"; // NOI18N
    private static final String BINDING_COMPONENT_INFO_FILENAME = "BindingComponentInformation.xml"; // NOI18N
    
    public ComponentInfoGenerator(String confDir) {
        this(confDir, JbiDefaultComponentInfo.getJbiDefaultComponentInfo().getComponentList());
    }
    
    public ComponentInfoGenerator(String confDir, 
            List<JBIComponentStatus> compList) {
        this.confDir = confDir;
        this.compList = compList;
    }
    
    public void doIt() {
        JBIComponentDocument document = new JBIComponentDocument();
        try {
            document.getJbiComponentList().addAll(compList);
            
            Document componentDocument = buildComponentDOMTree(document);
            writeTo(confDir, COMPONENT_INFO_FILENAME, componentDocument);
            
            Document bindingComponentDocument = buildBindingComponentDOMTree(document);
            writeTo(confDir, BINDING_COMPONENT_INFO_FILENAME, bindingComponentDocument); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * DOCUMENT ME!
     *
     * @param container DOCUMENT ME!
     *
     * @throws ParserConfigurationException DOCUMENT ME!
     */
    private Document buildComponentDOMTree(JBIComponentDocument container)
    throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document componentDocument = builder.newDocument(); // Create from whole cloth
        
        Element root = componentDocument.createElement(
                JBIComponentDocument.COMP_INFO_LIST_NODE_NAME
                );
        
        componentDocument.appendChild(root);
        
        for (JBIComponentStatus jbiComponent : container.getJbiComponentList()) {
            String type = jbiComponent.getType();
            if ((type.equalsIgnoreCase("Binding") || // NOI18N
                    type.equalsIgnoreCase("Engine")) && // NOI18N
                    !jbiComponent.getName().startsWith("com.sun.")) { // NOI18N
                
                Element componentInfoNode =
                        createComponentInfoNode(componentDocument, jbiComponent);
                root.appendChild(componentInfoNode);
            }
        }
        
        return componentDocument;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param container DOCUMENT ME!
     *
     * @throws ParserConfigurationException DOCUMENT ME!
     */
    private Document buildBindingComponentDOMTree(JBIComponentDocument container)
    throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document bindingComponentDocument = builder.newDocument(); // Create from whole cloth
        
        Element root = bindingComponentDocument.createElement(
                JBIComponentDocument.COMP_INFO_LIST_NODE_NAME
                );
        
        bindingComponentDocument.appendChild(root);
        
        for (JBIComponentStatus jbiComponent : container.getJbiComponentList()) {
            String type = jbiComponent.getType();
            List nsList = jbiComponent.getNamespaceList();
            if ((type != null) && (type.equalsIgnoreCase("Binding")) && // NOI18N
                    (nsList != null) && (nsList.size() > 0) &&
                    !jbiComponent.getName().startsWith("com.sun.")) { // NOI18N
                
                Element componentInfoNode =
                        createComponentInfoNode(bindingComponentDocument, jbiComponent);
                
                for (int i = 0; i < nsList.size(); i++) {
                    String ns = (String)nsList.get(i);
                    if (ns == null || ns.trim().equals("")) { // NOI18N
                        continue;
                    }
                    
                    Element componentNSNode = bindingComponentDocument.createElement(
                            JBIComponentDocument.NAMESPACE_NODE_NAME
                            );
                    Node componentNSTextNode = (Node) bindingComponentDocument.createTextNode(ns);
                    componentNSNode.appendChild(componentNSTextNode);
                    componentInfoNode.appendChild(componentNSNode);
                }
                root.appendChild(componentInfoNode);
            }
        }
        
        return bindingComponentDocument;
    }
    
    private Element createComponentInfoNode(final Document document,
            final JBIComponentStatus jbiComponent)
            throws DOMException {
        
        String componentName = jbiComponent.getName();
        
        Element componentInfoNode = document.createElement(
                JBIComponentDocument.COMP_INFO_NODE_NAME
                );
        
        Element componentDescriptionNode = document.createElement(
                JBIComponentDocument.DESCRIPTION_NODE_NAME
                );
        Node componentDescriptionTextNode = (Node) document.createTextNode(
                jbiComponent.getDescription()
                );
        componentDescriptionNode.appendChild(componentDescriptionTextNode);
        componentInfoNode.appendChild(componentDescriptionNode);
        
        Element componentNameNode = document.createElement(
                JBIComponentDocument.NAME_NODE_NAME
                );
        Node componentNameTextNode = (Node) document.createTextNode(componentName);
        componentNameNode.appendChild(componentNameTextNode);
        componentInfoNode.appendChild(componentNameNode);
        
        Element componentStateNode = document.createElement(
                JBIComponentDocument.STATUS_NODE_NAME
                );
        Node componentStateTextNode = (Node) document.createTextNode(
                jbiComponent.getState()
                );
        componentStateNode.appendChild(componentStateTextNode);
        componentInfoNode.appendChild(componentStateNode);
        
        Element componentTypeNode = document.createElement(
                JBIComponentDocument.TYPE_NODE_NAME
                );
        Node componentTypeTextNode = (Node) document.createTextNode(jbiComponent.getType());
        componentTypeNode.appendChild(componentTypeTextNode);
        componentInfoNode.appendChild(componentTypeNode);
        
        return componentInfoNode;
    }
          
    private void writeTo(String directoryPath, String fileName, Document document)
    throws TransformerConfigurationException, TransformerException, Exception {
        File file = new File(directoryPath);
        
        if ((file.isDirectory() == false) || (file.exists() == false)) {
            throw new Exception("Directory Path: " + directoryPath + " is invalid."); // NOI18N
        }
        
        String fileLocation = file.getAbsolutePath() + File.separator + fileName;
        XmlUtil.writeToFile(fileLocation, document);
    }
}
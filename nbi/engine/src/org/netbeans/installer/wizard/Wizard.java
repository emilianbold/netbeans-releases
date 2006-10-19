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
 *
 * $Id$
 */
package org.netbeans.installer.wizard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;
import org.netbeans.installer.download.DownloadOptions;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.wizard.components.WizardAction;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.conditions.WizardCondition;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Kirill Sorokin
 */
public class Wizard extends SubWizard {
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_COMPONENTS_INSTANCE_URI =
            "resource:org/netbeans/installer/wizard/wizard-components.xml";
    
    public static final String DEFAULT_COMPONENTS_SCHEMA_URI =
            "resource:org/netbeans/installer/wizard/wizard-components.xsd";
    
    public static final String COMPONENTS_INSTANCE_URI_PROPERTY =
            "nbi.wizard.components.instance.uri";
    
    public static final String COMPONENTS_SCHEMA_URI_PROPERTY =
            "nbi.wizard.components.schema.uri";
    
    ////////////////////////////////////////////////////////////////////////////
    // Static
    private static Wizard instance;
    
    private static String componentsInstanceURI =
            DEFAULT_COMPONENTS_INSTANCE_URI;
    
    private static String componentsSchemaURI =
            DEFAULT_COMPONENTS_SCHEMA_URI;
    
    public static synchronized Wizard getInstance() {
        if (instance == null) {
            if (System.getProperty(COMPONENTS_INSTANCE_URI_PROPERTY) != null) {
                componentsInstanceURI =
                        System.getProperty(COMPONENTS_INSTANCE_URI_PROPERTY);
            }
            
            if (System.getProperty(COMPONENTS_SCHEMA_URI_PROPERTY) != null) {
                componentsInstanceURI =
                        System.getProperty(COMPONENTS_SCHEMA_URI_PROPERTY);
            }
            
            instance = new Wizard();
        }
        
        return instance;
    }
    
    public static List<WizardComponent> loadWizardComponents(
            String componentsURI) throws InitializationException {
        return loadWizardComponents(componentsURI, Wizard.class.getClassLoader());
    }
    
    public static List<WizardComponent> loadWizardComponents(
            String componentsURI, ClassLoader loader)
            throws InitializationException {
        try {
            DownloadOptions options = DownloadOptions.getDefaults();
            options.put(DownloadOptions.CLASSLOADER, loader);
            
            File schemaFile =
                    FileProxy.getInstance().getFile(componentsSchemaURI, loader);
            File componentsFile =
                    FileProxy.getInstance().getFile(componentsURI, loader);
            
            SchemaFactory schemaFactory =
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            
            Schema schema = schemaFactory.newSchema(schemaFile);
            
            DocumentBuilderFactory documentBuilderFactory =
                    DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setSchema(schema);
            documentBuilderFactory.setNamespaceAware(true);
            
            DocumentBuilder documentBuilder =
                    documentBuilderFactory.newDocumentBuilder();
            
            Document document = documentBuilder.parse(componentsFile);
            
            return loadWizardComponents(document.getDocumentElement(), loader);
        } catch (DownloadException e) {
            throw new InitializationException(
                    "Could not load components", e);
        } catch (ParserConfigurationException e) {
            throw new InitializationException(
                    "Could not load components", e);
        } catch (SAXException e) {
            throw new InitializationException(
                    "Could not load components", e);
        } catch (IOException e) {
            throw new InitializationException(
                    "Could not load components", e);
        }
    }
    
    private static List<WizardComponent> loadWizardComponents(Node node, ClassLoader loader)
    throws InitializationException {
        List<WizardComponent> wizardComponents = new ArrayList<WizardComponent>();
        
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            NodeList nodeList = (NodeList) xpath.evaluate("./component", node,
                    XPathConstants.NODESET);
            
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node componentNode = nodeList.item(i);
                wizardComponents.add(loadWizardComponent(componentNode, loader));
            }
        } catch (XPathException e) {
            throw new InitializationException(
                    "Could not load components", e);
        }
        
        return wizardComponents;
    }
    
    private static WizardComponent loadWizardComponent(Node node, ClassLoader loader)
    throws InitializationException {
        WizardComponent component = null;
        
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            
            String classname = (String) xpath.evaluate(
                    "./@class", node, XPathConstants.STRING);
            
            component = (WizardComponent) loader.loadClass(classname).newInstance();
            
            Node componentsNode =
                    (Node) xpath.evaluate("./components", node, XPathConstants.NODE);
            
            if (componentsNode != null) {
                List<WizardComponent> childComponents =
                        loadWizardComponents(componentsNode, loader);
                for (WizardComponent childComponent: childComponents) {
                    component.addChildComponent(childComponent);
                }
            }
            
            Node conditionsNode =
                    (Node) xpath.evaluate("./conditions", node, XPathConstants.NODE);
            
            if (conditionsNode != null) {
                List<WizardCondition> conditions = loadWizardConditions(conditionsNode, loader);
                for (WizardCondition condition: conditions) {
                    component.addCondition(condition);
                }
            }
            
            Node propertiesNode =
                    (Node) xpath.evaluate("./properties", node, XPathConstants.NODE);
            if (propertiesNode != null) {
                loadProperties(propertiesNode, component, loader);
            }
        } catch (XPathException e) {
            throw new InitializationException(
                    "Could not load component", e);
        } catch (ClassNotFoundException e) {
            throw new InitializationException(
                    "Could not load component", e);
        } catch (IllegalAccessException e) {
            throw new InitializationException(
                    "Could not load component", e);
        } catch (InstantiationException e) {
            throw new InitializationException(
                    "Could not load component", e);
        }
        
        return component;
    }
    
    private static List<WizardCondition> loadWizardConditions(Node node, ClassLoader loader)
    throws InitializationException {
        List<WizardCondition> wizardConditions = new ArrayList<WizardCondition>();
        
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            
            NodeList nodeList = (NodeList) xpath.evaluate(
                    "./condition", node, XPathConstants.NODESET);
            
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node conditionNode = nodeList.item(i);
                
                String classname = (String) xpath.evaluate(
                        "/@class", conditionNode, XPathConstants.STRING);
                
                WizardCondition condition =
                        (WizardCondition) Class.forName(classname).newInstance();
                
                Node propertiesNode = (Node)
                xpath.evaluate("./properties", node, XPathConstants.NODE);
                loadProperties(propertiesNode, condition, loader);
                wizardConditions.add(condition);
            }
        } catch (XPathException e) {
            throw new InitializationException(
                    "Could not load conditions", e);
        } catch (ClassNotFoundException e) {
            throw new InitializationException(
                    "Could not load conditions", e);
        } catch (IllegalAccessException e) {
            throw new InitializationException(
                    "Could not load conditions", e);
        } catch (InstantiationException e) {
            throw new InitializationException(
                    "Could not load conditions", e);
        }
        
        return wizardConditions;
    }
    
    private static void loadProperties(Node node, Object component, ClassLoader loader)
    throws InitializationException {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            
            NodeList nodeList = (NodeList) xpath.
                    evaluate("./property", node, XPathConstants.NODESET);
            
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node propertyNode = nodeList.item(i);
                
                String name = (String) xpath.
                        evaluate("./@name", propertyNode, XPathConstants.STRING);
                String value = (String) xpath.
                        evaluate("./text()", propertyNode, XPathConstants.STRING);
                
                if (component instanceof WizardComponent) {
                    ((WizardComponent) component).setProperty(name, value);
                } else if (component instanceof WizardCondition) {
                    ((WizardCondition) component).setProperty(name, value);
                }
            }
        } catch (XPathException e) {
            throw new InitializationException(
                    "Could not load propeties", e);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance
    boolean executingAction;
    
    // constructor /////////////////////////////////////////////////////////////
    private Wizard() {
        try {
            wizardComponents = loadWizardComponents(componentsInstanceURI);
        } catch (InitializationException e) {
            ErrorManager.getInstance().notify(ErrorLevel.CRITICAL,
                    "Failed to load wizard components", e);
        }
    }
    
    // wizard lifecycle control methods ////////////////////////////////////////
    public void open() {
        // create the UI
        frame = new WizardFrame(this);
        
        // show the UI
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                frame.setVisible(true);
            }
        });
    }
    
    public void close() {
        frame.setVisible(false);
        frame.dispose();
    }
    
    public void next() {
        if (executingAction) {
            executingAction = false;
        } else {
            super.next();
        }
    }
    
    // other ///////////////////////////////////////////////////////////////////
    public void executeAction(WizardAction action) {
        action.executeComponent(this);
        
        executingAction = true;
        while (executingAction) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                ErrorManager.getInstance().notify(ErrorLevel.DEBUG, "interrupted", e);
            }
        }
    }
}

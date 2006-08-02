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
import org.netbeans.installer.Installer;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.download.DownloadManager;
import org.netbeans.installer.download.DownloadOptions;
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.utils.error.ErrorManager;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.conditions.WizardCondition;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 *
 * @author Kirill Sorokin
 */
public class Wizard {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_COMPONENTS_INSTANCE_URI = 
            "resource:org/netbeans/installer/wizard/wizard-components.xml";
    
    public static final String DEFAULT_COMPONENTS_SCHEMA_URI = 
            "resource:org/netbeans/installer/wizard/wizard-components.xsd";
    
    public static final String COMPONENTS_INSTANCE_URI_PROPERTY = 
            "nbi.wizard.components.instance.uri";
        
    public static final String COMPONENTS_SCHEMA_URI_PROPERTY = 
            "nbi.wizard.components.schema.uri";
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static Wizard instance;
    
    private static String componentsInstanceURI = DEFAULT_COMPONENTS_INSTANCE_URI;
    
    private static String componentsSchemaURI = DEFAULT_COMPONENTS_SCHEMA_URI;
    
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
    
    public static List<WizardComponent> loadWizardComponents(String componentsURI) 
            throws InitializationException {
        try {
            DownloadOptions downloadOptions = DownloadOptions.getDefaults();
            
            File schemaFile = 
                    DownloadManager.getInstance().download(componentsSchemaURI, downloadOptions);
            File componentsFile = 
                    DownloadManager.getInstance().download(componentsURI, downloadOptions);
            
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
            
            return loadWizardComponents(document.getDocumentElement());
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
    
    private static List<WizardComponent> loadWizardComponents(Node node) 
            throws InitializationException {
        List<WizardComponent> wizardComponents = new ArrayList<WizardComponent>();
        
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            NodeList nodeList = (NodeList) xpath.evaluate("./component", node, 
                    XPathConstants.NODESET);
            
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node componentNode = nodeList.item(i);
                wizardComponents.add(loadWizardComponent(componentNode));
            }
        } catch (XPathException e) {
            throw new InitializationException(
                    "Could not load components", e);
        }
        
        return wizardComponents;
    }
    
    private static WizardComponent loadWizardComponent(Node node) 
            throws InitializationException {
        WizardComponent component = null;
        
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            
            String classname = (String) xpath.evaluate(
                    "./@class", node, XPathConstants.STRING);
            
            component = (WizardComponent) Class.forName(classname).newInstance();
            
            Node componentsNode = 
                    (Node) xpath.evaluate("./components", node, XPathConstants.NODE);
            
            if (componentsNode != null) {
                List<WizardComponent> childComponents = 
                        loadWizardComponents(componentsNode);
                for (WizardComponent childComponent: childComponents) {
                    component.addChildComponent(childComponent);
                }
            }
            
            Node conditionsNode = 
                    (Node) xpath.evaluate("./conditions", node, XPathConstants.NODE);
            
            if (conditionsNode != null) {
                List<WizardCondition> conditions = loadWizardConditions(conditionsNode);
                for (WizardCondition condition: conditions) {
                    component.addCondition(condition);
                }
            }
            
            Node propertiesNode = 
                    (Node) xpath.evaluate("./properties", node, XPathConstants.NODE);
            if (propertiesNode != null) {
                loadProperties(propertiesNode, component);
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
    
    private static List<WizardCondition> loadWizardConditions(Node node) 
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
                loadProperties(propertiesNode, condition);
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
    
    private static void loadProperties(Node node, Object component) 
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
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private ProductComponent      productComponent;
    private List<WizardComponent> wizardComponents;
    private int                   currentIndex;
    private WizardFrame           wizardFrame;
    private Wizard                parent;
    
    private Wizard() {
        try {
            wizardComponents = loadWizardComponents(componentsInstanceURI);
        } catch (InitializationException e) {
            ErrorManager.getInstance().notify(ErrorManager.CRITICAL, 
                    "Failed to load wizard components", e);
        }
    }
    
    private Wizard(ProductComponent aProductComponent, Wizard aWizard) {
        // save the product component reference
        productComponent = aProductComponent;
        
        // save the parent wizard reference
        parent = aWizard;
        
        // init the wizard components list
        wizardComponents = productComponent.getWizardComponents();
    }
    
    private Wizard(List<WizardComponent> someWizardComponents, Wizard aWizard) {
        // save the parent wizard reference
        parent = aWizard;
        
        // init the wizard components list
        wizardComponents = someWizardComponents;
        
        // save the product component reference
        productComponent = parent.getProductComponent();
    }
    
    public void start() {
        if (parent == null) {
            // create the UI
            wizardFrame = new WizardFrame(this);
            
            // show the UI
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    wizardFrame.setVisible(true);
                }
            });
        } else {
            wizardFrame = parent.getWizardFrame();
        }
        
        // init the current index
        currentIndex = -1;
        
        // call next() to display the first component
        next();
    }
    
    private WizardComponent getNext() {
        for (int i = currentIndex + 1; i < wizardComponents.size(); i++) {
            WizardComponent component = wizardComponents.get(i);
            if (component.isActive() && component.evaluateConditions()) {
                return component;
            }
        }
        
        return null;
    }
    
    public boolean hasNext() {
        return (getNext() != null) || ((parent != null) && parent.hasNext());
    }
    
    public void next() {
        if (wizardFrame == null) {
            throw new IllegalStateException("Cannot move to the next component - the wizard has not yet been initialized");
        }
        
        WizardComponent component = getNext();
        
        if (component != null) {
            currentIndex = wizardComponents.indexOf(component);
            
            component.executeComponent(this);
        } else {
            finish();
        }
    }
    
    private WizardComponent getPrevious() {
        for (int i = currentIndex - 1; i > -1; i--) {
            WizardComponent component = wizardComponents.get(i);
            if (component.isActive() && component.evaluateConditions()) {
                return component;
            }
        }
        
        return null;
    }
    
    public boolean hasPrevious() {
        return (getPrevious() != null) || ((parent != null) && parent.hasPrevious());
    }
    
    public void previous() {
        if (wizardFrame == null) {
            throw new IllegalStateException("Cannot move to the previous component - the wizard has not yet been initialized");
        }
        
        WizardComponent component = getPrevious();
        
        if (component != null) {
            currentIndex = wizardComponents.indexOf(component);
            
            component.executeComponent(this);
        } else {
            if (parent != null) {
                parent.previous();
            } else {
                throw new IllegalStateException("Cannot move to the previous component - the wizard is at the first component");
            }
        }
    }
    
    public void cancel() {
        if (wizardFrame == null) {
            throw new IllegalStateException("Cannot cancel the wizard - the wizard has not yet been initialized");
        }
        
        // hide and dispose the UI
        wizardFrame.setVisible(false);
        wizardFrame.dispose();
        
        // cancel the installer
        Installer.getInstance().cancel();
    }
    
    public void finish() {
        // check the wizard state
        if (wizardFrame == null) {
            throw new IllegalStateException("Cannot finish the wizard - the wizard has not yet been initialized");
        }
        
        if (parent == null) {
            // hide and dispose the UI
            wizardFrame.setVisible(false);
            wizardFrame.dispose();
            
            // finish the installer
            Installer.getInstance().finish();
        } else {
            parent.next();
        }
    }
    
    public Wizard createSubWizard(ProductComponent productComponent) {
        return new Wizard(productComponent, this);
    }
    
    public Wizard createSubWizard(List<WizardComponent> wizardComponents) {
        return new Wizard(wizardComponents, this);
    }
    
    public WizardFrame getWizardFrame() {
        return wizardFrame;
    }
    
    public ProductComponent getProductComponent() {
        return productComponent;
    }
}
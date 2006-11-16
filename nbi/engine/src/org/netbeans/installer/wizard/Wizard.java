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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.SwingUtilities;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.installer.download.DownloadOptions;
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.wizard.components.WizardComponent;
import static org.netbeans.installer.utils.helper.ErrorLevel.DEBUG;
import static org.netbeans.installer.utils.helper.ErrorLevel.MESSAGE;
import static org.netbeans.installer.utils.helper.ErrorLevel.WARNING;
import static org.netbeans.installer.utils.helper.ErrorLevel.ERROR;
import static org.netbeans.installer.utils.helper.ErrorLevel.CRITICAL;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.wizard.conditions.AndCondition;
import org.netbeans.installer.wizard.conditions.WizardCondition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 *
 * @author Kirill Sorokin
 */
public class Wizard {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String COMPONENTS_INSTANCE_URI_PROPERTY =
            "nbi.wizard.components.instance.uri";
    
    public static final String DEFAULT_COMPONENTS_INSTANCE_URI =
            "resource:org/netbeans/installer/wizard/wizard-components.xml";
    
    public static final String COMPONENTS_SCHEMA_URI_PROPERTY =
            "nbi.wizard.components.schema.uri";
    
    public static final String DEFAULT_COMPONENTS_SCHEMA_URI =
            "resource:org/netbeans/installer/wizard/wizard-components.xsd";
    
    public static final String SILENT_MODE_ACTIVE_PROPERTY =
            "nbi.wizard.silent.mode.active";
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static Wizard instance;
    
    private static String componentsInstanceURI = DEFAULT_COMPONENTS_INSTANCE_URI;
    private static String componentsSchemaURI = DEFAULT_COMPONENTS_SCHEMA_URI;
    
    private static WizardExecutionMode executionMode = WizardExecutionMode.GUI;
    
    public static synchronized Wizard getInstance() {
        if (instance == null) {
            // initialize uri for root wizard's components list
            if (System.getProperty(COMPONENTS_INSTANCE_URI_PROPERTY) != null) {
                componentsInstanceURI =
                        System.getProperty(COMPONENTS_INSTANCE_URI_PROPERTY);
            }
            
            // initialize uri for components list xml schema
            if (System.getProperty(COMPONENTS_SCHEMA_URI_PROPERTY) != null) {
                componentsInstanceURI =
                        System.getProperty(COMPONENTS_SCHEMA_URI_PROPERTY);
            }
            
            // check whether silent mode is active
            if (System.getProperty(SILENT_MODE_ACTIVE_PROPERTY) != null) {
                executionMode = WizardExecutionMode.SILENT;
            }
            
            // create the root wizard and load its components
            instance = new Wizard();
            try {
                instance.components = loadWizardComponents(componentsInstanceURI);
            } catch (InitializationException e) {
                ErrorManager.notify(ErrorLevel.CRITICAL,
                        "Failed to load wizard components", e);
            }
        }
        
        return instance;
    }
    
    public static List<WizardComponent> loadWizardComponents(String componentsURI) throws InitializationException {
        return loadWizardComponents(componentsURI, Wizard.class.getClassLoader());
    }
    
    public static List<WizardComponent> loadWizardComponents(String componentsURI, ClassLoader loader) throws InitializationException {
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
    
    private static List<WizardComponent> loadWizardComponents(Element element, ClassLoader loader) throws InitializationException {
        List<WizardComponent> components = new ArrayList<WizardComponent>();
        
        for (Element child: XMLUtils.getChildren(element, "component")) {
            components.add(loadWizardComponent(child, loader));
        }
        
        return components;
    }
    
    private static WizardComponent loadWizardComponent(Element element, ClassLoader loader) throws InitializationException {
        WizardComponent component = null;
        Element         child     = null;
        
        try {
            String classname = XMLUtils.getAttribute(element, "class");
            
            component = (WizardComponent) loader.loadClass(classname).newInstance();
            
            child = XMLUtils.getChild(element, "components");
            if (child != null) {
                component.addChildren(loadWizardComponents(child, loader));
            }
            
            child = XMLUtils.getChild(element, "conditions");
            if (child != null) {
                component.setCondition(new AndCondition(WizardCondition.loadWizardConditions(child, loader)));
            }
            
            child = XMLUtils.getChild(element, "properties");
            if (child != null) {
                component.getProperties().putAll(XMLUtils.loadProperties(child));
            }
        } catch (ClassNotFoundException e) {
            throw new InitializationException("Could not load component", e);
        } catch (IllegalAccessException e) {
            throw new InitializationException("Could not load component", e);
        } catch (InstantiationException e) {
            throw new InitializationException("Could not load component", e);
        }
        
        return component;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    protected List<WizardComponent> components;
    protected WizardFrame           frame;
    
    private   ProductComponent      productComponent;
    private   int                   currentIndex;
    private   Wizard                parent;
    
    // constructors /////////////////////////////////////////////////////////////////
    private Wizard() {
        this.currentIndex     = -1;
    }
    
    private Wizard(final Wizard parent) {
        this();
        
        this.parent           = parent;
        this.frame            = parent.getFrame();
        this.productComponent = parent.getProductComponent();
    }
    
    private Wizard(final ProductComponent component, final Wizard parent, int index) {
        this(parent);
        
        this.productComponent = component;
        this.components = component.getWizardComponents();
        this.currentIndex = index;
    }
    
    private Wizard(final List<WizardComponent> components, final Wizard parent, int index) {
        this(parent);
        
        this.components = components;
        this.currentIndex = index;
    }
    
    // wizard lifecycle control methods /////////////////////////////////////////////
    public void open() {
        frame = new WizardFrame();
        
        switch (executionMode) {
            case GUI:
                SwingUtilities.invokeLater(new Runnable(){
                    public void run() {
                        frame.setVisible(true);
                    }
                });
                break;
            case SILENT:
                // we don't have to initialize anything for silent mode
                break;
            default:
                ErrorManager.notify(ERROR, "Something terrible has " +
                        "happened - we have an execution mode which is not " +
                        "in its enum");
        }
    }
    
    public void close() {
        switch (executionMode) {
            case GUI:
                frame.setVisible(false);
                break;
            case SILENT:
                // we don't have to initialize anything for silent mode
                break;
            default:
                ErrorManager.notify(ERROR, "Something terrible has " +
                        "happened - we have an execution mode which is not " +
                        "in its enum");
        }
        
        frame.dispose();
    }
    
    // component flow control methods ///////////////////////////////////////////////
    public void next() {
        WizardComponent component = getNext();
        
        // if there is no next component in the current wizard, try to delegate
        // the call to the parent wizard, and if there is no parent wizard... we
        // should be here in the first place
        if (component != null) {
            currentIndex = components.indexOf(component);
            switch (executionMode) {
                case GUI:
                    component.executeForward(this);
                    break;
                case SILENT:
                    component.executeSilently(this);
                    break;
                default:
                    ErrorManager.notify(ERROR, "Something terrible has " +
                            "happened - we have an execution mode which is not " +
                            "in its enum");
            }
            
        } else if (parent != null) {
            parent.next();
        } else {
            ErrorManager.notify(ERROR, "Cannot move to the next " +
                    "element - the wizard is at the last element");
        }
    }
    
    public void previous() {
        WizardComponent component = getPrevious();
        
        // if there is no previous component in the current wizard, try to delegate
        // the call to the parent wizard, and if there is no parent wizard... we
        // should be here in the first place
        if (component != null) {
            currentIndex = components.indexOf(component);
            switch (executionMode) {
                case GUI:
                    component.executeBackward(this);
                    break;
                case SILENT:
                    ErrorManager.notify(ERROR, "Moving backward is " +
                            "not possible in silent mode");
                    break;
                default:
                    ErrorManager.notify(ERROR, "Something terrible has " +
                            "happened - we have an execution mode which is not " +
                            "in its enum");
            }
            
        } else if (parent != null) {
            parent.previous();
        } else {
            ErrorManager.notify(ERROR, "Cannot move to the previous " +
                    "component - the wizard is at the first component");
        }
    }
    
    public void executeComponent(WizardComponent component) {
        switch (executionMode) {
            case GUI:
                component.executeBlocking(this);
                break;
            case SILENT:
                component.executeSilentlyBlocking(this);
                break;
            default:
                ErrorManager.notify(ERROR, "Something terrible has " +
                        "happened - we have an execution mode which is not " +
                        "in its enum");
        }
    }
    
    // informational methods ////////////////////////////////////////////////////////
    public boolean hasPrevious() {
        // if current component is a point of no return - we cannot move backwards,
        // i.e. there is no previous component
        if ((getCurrent() != null) && getCurrent().isPointOfNoReturn()) {
            return false;
        }
        
        for (int i = currentIndex - 1; i > -1; i--) {
            WizardComponent component = components.get(i);
            
            // if the component can be executed backward and its conditions are met,
            // it is the previous one
            if (component.canExecuteBackward() && component.getCondition().evaluate()) {
                return true;
            }
            
            // if the currently examined component is a point of no return and it
            // cannot be executed (since we passed the previous statement) - we have
            // no previous component
            if (component.isPointOfNoReturn()) {
                return false;
            }
        }
        
        // if we got this far, there is not previous component in the current wizard,
        // but no points of no return we encountered either. thus we should ask the
        // parent wizard if it has a previous component
        return (parent != null) && parent.hasPrevious();
    }
    
    public boolean hasNext() {
        // if there is no next component in the current wizard, we should check the
        // parent wizard if it has one
        return (getNext() != null) || ((parent != null) && parent.hasNext());
    }
    
    // getters & setters ////////////////////////////////////////////////////////////
    public WizardFrame getFrame() {
        return frame;
    }
    
    public ProductComponent getProductComponent() {
        return productComponent;
    }
    
    public int getCurrentIndex() {
        return currentIndex;
    }
    
    // factory methods for children /////////////////////////////////////////////////
    public Wizard createSubWizard(ProductComponent component, int index) {
        return new Wizard(component, this, index);
    }
    
    public Wizard createSubWizard(List<WizardComponent> components, int index) {
        return new Wizard(components, this, index);
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private WizardComponent getCurrent() {
        if ((currentIndex > -1) && (currentIndex < components.size())) {
            return components.get(currentIndex);
        } else {
            return null;
        }
    }
    
    private WizardComponent getPrevious() {
        // if current component is a point of no return - we cannot move backwards,
        // i.e. there is no previous component
        if ((getCurrent() != null) && getCurrent().isPointOfNoReturn()) {
            return null;
        }
        
        for (int i = currentIndex - 1; i > -1; i--) {
            WizardComponent component = components.get(i);
            
            // if the component can be executed backward and its conditions are met,
            // it is the previous one
            if (component.canExecuteBackward() && component.getCondition().evaluate()) {
                return component;
            }
            
            // if the currently examined component is a point of no return and it
            // cannot be executed (since we passed the previous statement) - we have
            // no previous component
            if (component.isPointOfNoReturn()) {
                return null;
            }
        }
        
        // if we reached the before-first index and yet could not find a previous
        // component, then there is no previous component
        return null;
    }
    
    private WizardComponent getNext() {
        for (int i = currentIndex + 1; i < components.size(); i++) {
            WizardComponent component = components.get(i);
            
            // if the component can be executed forward and its conditions are met,
            // it is the next one
            if (component.canExecuteForward() && component.getCondition().evaluate()) {
                return component;
            }
        }
        
        // if we reached the after-last index and yet could not find a next
        // component, then there is no next component
        return null;
    }
}
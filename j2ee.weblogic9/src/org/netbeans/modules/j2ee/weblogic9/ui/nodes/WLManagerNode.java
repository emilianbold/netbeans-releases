/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import java.awt.*;

import javax.enterprise.deploy.spi.*;

import org.openide.util.*;
import org.openide.nodes.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;

import org.netbeans.modules.j2ee.weblogic9.*;

/**
 * Node that will appear in the Server Registry and represent a concrete server 
 * instance.
 *
 * @author Kirill Sorokin
 */
public class WLManagerNode extends AbstractNode implements Node.Cookie {
    
    /**
     * The associated deployment manager, i.e. the plugin's wrapper for
     * the server implementation of the DEploymentManager interface
     */
    private WLDeploymentManager deploymentManager;
    
    // properties names
    private static final String DISPLAY_NAME = "displayName"; // NOI18N
    private static final String URL = "url"; // NOI18N
    private static final String USERNAME = "username"; // NOI18N
    private static final String PASSWORD = "password"; // NOI18N
    private static final String SERVER_ROOT = "serverRoot"; // NOI18N
    private static final String DOMAIN_ROOT = "domainRoot"; // NOI18N
    private static final String DEBUGGER_PORT = "debuggerPort"; // NOI18N
    
    /**
     * Path to the node's icon that should reside in the class path
     */
    private static final String ICON = "org/netbeans/modules/j2ee/weblogic9/resources/16x16.gif"; // NOI18N
    
    /**
     * Creates a new instance of the WSManagerNode.
     * 
     * @param children the node's children
     * @param lookup a lookup object that contains the objects required for 
     *      node's customization, such as the deployment manager
     */
    public WLManagerNode(Children children, Lookup lookup) {
        super(children);
        
        // get the deployment manager from the lookup and save it
        this.deploymentManager = (WLDeploymentManager) lookup.lookup(DeploymentManager.class);
                
        // add the node itself to its cookie list
        getCookieSet().add(this);
    }
    
    /**
     * Returns the node's tooltip
     * 
     * @return the node's tooltip
     */
    public String getDisplayName() {
        return deploymentManager.getInstanceProperties().getProperty(InstanceProperties.URL_ATTR);
    }
    
    /**
     * Returns the node's icon when the node is in closed state
     * 
     * @return the node's icon
     */
    public Image getIcon(int type) {
        return Utilities.loadImage(ICON);
    }
    
    /**
     * Returns the node's icon when the node is in open state
     * 
     * @return the node's icon
     */
    public Image getOpenedIcon(int type) {
        return Utilities.loadImage(ICON);
    }
    
    /**
     * Returns the node's associated help article pointer
     * 
     * @return the node's help article
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("j2eeplugins_property_sheet_server_node_weblogic"); //NOI18N
    }
    
    /**
     * Creates and returns the node's properties sheet
     * 
     * @return the node's properties sheet
     */
    protected Sheet createSheet() {
        // create a new sheet
        Sheet sheet = super.createSheet();
        
        // get the sheet's properties' set object
        Sheet.Set properties = sheet.get(Sheet.PROPERTIES);       
        if (properties == null) {
	    properties = Sheet.createPropertiesSet();
            sheet.put(properties);
	}
        
        // declare the new property object and start adding the properties
        Node.Property property;
        
        // DISPLAY NAME
        property = new PropertySupport.ReadWrite(
                       DISPLAY_NAME,
                       String.class,
                       NbBundle.getMessage(WLManagerNode.class, "PROP_displayName"),   // NOI18N
                       NbBundle.getMessage(WLManagerNode.class, "HINT_displayName")   // NOI18N
                   ) {
                       public Object getValue() {
                           return deploymentManager.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
                       }
                       
                       public void setValue(Object value) {
                           deploymentManager.getInstanceProperties().setProperty(InstanceProperties.DISPLAY_NAME_ATTR, (String) value);
                       }
                   };
        properties.put(property);
        
        // URL
        property = new PropertySupport.ReadOnly(
                       URL,
                       String.class,
                       NbBundle.getMessage(WLManagerNode.class, "PROP_url"),   // NOI18N
                       NbBundle.getMessage(WLManagerNode.class, "HINT_url")   // NOI18N
                   ) {
                       public Object getValue() {
                           return deploymentManager.getURI();
                       }
                   };
        properties.put(property);
        
        // USER NAME
        property = new PropertySupport.ReadWrite(
                       USERNAME,
                       String.class,
                       NbBundle.getMessage(WLManagerNode.class, "PROP_username"),   // NOI18N
                       NbBundle.getMessage(WLManagerNode.class, "HINT_username")   // NOI18N
                   ) {
                       public Object getValue() {
                           return deploymentManager.getInstanceProperties().getProperty(InstanceProperties.USERNAME_ATTR);
                       }

                       public void setValue(Object value) {
                           deploymentManager.getInstanceProperties().setProperty(InstanceProperties.USERNAME_ATTR, (String) value);
                       }
                   };
        properties.put(property);
        
        // PASSWORD
        property = new PropertySupport.ReadWrite(
                       PASSWORD,
                       String.class,
                       NbBundle.getMessage(WLManagerNode.class, "PROP_password"),   // NOI18N
                       NbBundle.getMessage(WLManagerNode.class, "HINT_password")   // NOI18N
                   ) {
                       public Object getValue() {
                           String password = deploymentManager.getInstanceProperties().getProperty(InstanceProperties.PASSWORD_ATTR);
                           return password.replaceAll(".", "\\*");
                       }

                       public void setValue(Object value) {
                           deploymentManager.getInstanceProperties().setProperty(InstanceProperties.PASSWORD_ATTR, (String) value);
                       }
                   };
        properties.put(property);
        
        // SERVER ROOT
        property = new PropertySupport.ReadOnly(
                       SERVER_ROOT,
                       String.class,
                       NbBundle.getMessage(WLManagerNode.class, "PROP_serverRoot"),   // NOI18N
                       NbBundle.getMessage(WLManagerNode.class, "HINT_serverRoot")   // NOI18N
                   ) {
                       public Object getValue() {
                           return deploymentManager.getInstanceProperties().getProperty(WLDeploymentFactory.SERVER_ROOT_ATTR);
                       }
                   };
        properties.put(property);
        
        // DOMAIN ROOT
        property = new PropertySupport.ReadOnly(
                       DOMAIN_ROOT,
                       String.class,
                       NbBundle.getMessage(WLManagerNode.class, "PROP_domainRoot"),   // NOI18N
                       NbBundle.getMessage(WLManagerNode.class, "HINT_domainRoot")   // NOI18N
                   ) {
                       public Object getValue() {
                           return deploymentManager.getInstanceProperties().getProperty(WLDeploymentFactory.DOMAIN_ROOT_ATTR);
                       }
                   };
        properties.put(property);
        
        // DEBUGGER PORT
        property = new PropertySupport.ReadWrite(
                       DEBUGGER_PORT,
                       Integer.class,
                       NbBundle.getMessage(WLManagerNode.class, "PROP_debuggerPort"),   // NOI18N
                       NbBundle.getMessage(WLManagerNode.class, "HINT_debuggerPort")   // NOI18N
                   ) {
                       public Object getValue() {
                           String debuggerPort = deploymentManager.getInstanceProperties().getProperty(WLDeploymentFactory.DEBUGGER_PORT_ATTR);
                           return new Integer(debuggerPort);
                       }

                       public void setValue(Object value) {
                           deploymentManager.getInstanceProperties().setProperty(WLDeploymentFactory.DEBUGGER_PORT_ATTR, value.toString());
                       }
                   };
        properties.put(property);
        
        return sheet;
    }
    
    /**
     * A fake implementation of the Object's hashCode() method, in order to 
     * avoid FindBugsTool's warnings
     */
    public int hashCode() {
        return super.hashCode();
    }
    
    /**
     * A fake implementation of the Object's equals() method, in order to 
     * avoid FindBugsTool's warnings
     */
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
}
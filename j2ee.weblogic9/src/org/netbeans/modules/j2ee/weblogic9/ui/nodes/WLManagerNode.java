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

import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.*;
import org.openide.util.Lookup;
import java.awt.Image;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Kirill Sorokin
 */
public class WLManagerNode extends AbstractNode implements Node.Cookie {
    
    private WLDeploymentManager deploymentManager;
    
    private static final String DISPLAY_NAME = "displayName"; // NOI18N
    private static final String URL = "url"; // NOI18N
    private static final String USERNAME = "username"; // NOI18N
    private static final String PASSWORD = "password"; // NOI18N
    private static final String SERVER_ROOT = "serverRoot"; // NOI18N
    private static final String DOMAIN_ROOT = "domainRoot"; // NOI18N
    private static final String DEBUGGER_PORT = "debuggerPort"; // NOI18N
    
    private static final String ICON = "org/netbeans/modules/j2ee/weblogic9/resources/16x16.gif"; // NOI18N
    
    public WLManagerNode(Children children, Lookup lookup) {
        super(children);
        
        this.deploymentManager = (WLDeploymentManager) lookup.lookup(DeploymentManager.class);
                
        getCookieSet().add(this);
    }
    
    public String getDisplayName() {
        return deploymentManager.getInstanceProperties().getProperty(InstanceProperties.URL_ATTR);
    }
    
    public Image getIcon(int type) {
        return Utilities.loadImage(ICON);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("j2eeplugins_property_sheet_server_node_weblogic"); //NOI18N
    }
    
    public Image getOpenedIcon(int type) {
        return Utilities.loadImage(ICON);
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        
        Sheet.Set properties = sheet.get(Sheet.PROPERTIES);       
        if (properties == null) {
	    properties = Sheet.createPropertiesSet();
            sheet.put(properties);
	}
        
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
    
    public int hashCode() {
        return super.hashCode();
    }
    
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
}
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
package org.netbeans.modules.j2ee.jboss4.nodes;

import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBInstantiatingIterator;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.nodes.*;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import javax.enterprise.deploy.spi.DeploymentManager;
import java.awt.Image;
import java.awt.Toolkit;
import java.beans.BeanInfo;
import java.net.URL;
import org.netbeans.modules.j2ee.jboss4.ide.Customizer;
import org.netbeans.modules.j2ee.jboss4.ide.JBJ2eePlatformFactory;
import java.awt.Component;

/**
 *
 * @author Ivan Sidorkin
 */
public class JBManagerNode extends AbstractNode implements Node.Cookie {
    
    private DeploymentManager deploymentManager;
    
    public JBManagerNode(Children children, Lookup lookup) {
        super(children);
        this.deploymentManager = (DeploymentManager) lookup.lookup(DeploymentManager.class);
        getCookieSet().add(this);
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("j2eeplugins_property_sheet_server_node"); //NOI18N
    }
    
    public boolean hasCustomizer() {
        return true;
    }
    
    public Component getCustomizer() {
        return new Customizer(new JBJ2eePlatformFactory().getJ2eePlatformImpl(deploymentManager));
    }
    
    public Sheet createSheet(){
        Sheet sheet = super.createSheet();
        Sheet.Set properties = sheet.get(Sheet.PROPERTIES);
        if (properties == null) {
            properties = Sheet.createPropertiesSet();
            sheet.put(properties);
        }
        final InstanceProperties instanceProperties = InstanceProperties.getInstanceProperties(((JBDeploymentManager)deploymentManager).getUrl());
        String serverDir = (String)InstanceProperties.getInstanceProperties(((JBDeploymentManager)deploymentManager).getUrl()).getProperty(JBInstantiatingIterator.PROPERTY_SERVER_DIR) ;
        String serverName = (String)InstanceProperties.getInstanceProperties(((JBDeploymentManager)deploymentManager).getUrl()).getProperty(JBInstantiatingIterator.PROPERTY_SERVER) ;
        
        Node.Property property=null;
        
        // DISPLAY NAME
        property = new PropertySupport.ReadWrite(
                NbBundle.getMessage(JBManagerNode.class, "LBL_DISPLAY_NAME"), //NOI18N
                String.class,
                NbBundle.getMessage(JBManagerNode.class, "LBL_DISPLAY_NAME"),   // NOI18N
                NbBundle.getMessage(JBManagerNode.class, "HINT_DISPLAY_NAME")   // NOI18N
                ) {
            public Object getValue() {
                return instanceProperties.getProperty(JBInstantiatingIterator.PROPERTY_DISPLAY_NAME);
            }
            
            public void setValue(Object val) {
                instanceProperties.setProperty(JBInstantiatingIterator.PROPERTY_DISPLAY_NAME, (String)val);
            }
        };
        properties.put(property);

        // servewr name
        property = new PropertySupport.ReadOnly(
                NbBundle.getMessage(JBManagerNode.class, "LBL_SERVER_NAME"),    //NOI18N
                String.class,
                NbBundle.getMessage(JBManagerNode.class, "LBL_SERVER_NAME"),   // NOI18N
                NbBundle.getMessage(JBManagerNode.class, "HINT_SERVER_NAME")   // NOI18N
                ) {
            public Object getValue() {
                return instanceProperties.getProperty(JBInstantiatingIterator.PROPERTY_SERVER);
            }
        };
        properties.put(property);
        
        //server location
        property = new PropertySupport.ReadOnly(
                NbBundle.getMessage(JBManagerNode.class, "LBL_SERVER_PATH"),   //NOI18N
                String.class,
                NbBundle.getMessage(JBManagerNode.class, "LBL_SERVER_PATH"),   // NOI18N
                NbBundle.getMessage(JBManagerNode.class, "HINT_SERVER_PATH")   // NOI18N
                ) {
            public Object getValue() {
                return instanceProperties.getProperty(JBInstantiatingIterator.PROPERTY_SERVER_DIR);
            }
        };
        properties.put(property);
        
        //host
        property = new PropertySupport.ReadOnly(
                NbBundle.getMessage(JBManagerNode.class, "LBL_HOST"),    //NOI18N
                String.class,
                NbBundle.getMessage(JBManagerNode.class, "LBL_HOST"),   // NOI18N
                NbBundle.getMessage(JBManagerNode.class, "HINT_HOST")   // NOI18N
                ) {
            public Object getValue() {
                return instanceProperties.getProperty(JBInstantiatingIterator.PROPERTY_HOST);
            }
        };
        properties.put(property);
        
        //port
        property = new PropertySupport.ReadOnly(
                NbBundle.getMessage(JBManagerNode.class, "LBL_PORT"),    //NOI18N
                Integer.TYPE,
                NbBundle.getMessage(JBManagerNode.class, "LBL_PORT"),   // NOI18N
                NbBundle.getMessage(JBManagerNode.class, "HINT_PORT")   // NOI18N
                ) {
            public Object getValue() {
                return new Integer(instanceProperties.getProperty(JBInstantiatingIterator.PROPERTY_PORT));
            }
        };
        properties.put(property);
        
        return sheet;
    }
    
    public Image getIcon(int type) {
        if (type == BeanInfo.ICON_COLOR_16x16) {
            URL resource = getClass().getClassLoader().getResource("org/netbeans/modules/j2ee/jboss4/resources/16x16.gif");//NOI18N
            return Toolkit.getDefaultToolkit().createImage(resource);
        }
        return super.getIcon(type);
    }
    
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
}
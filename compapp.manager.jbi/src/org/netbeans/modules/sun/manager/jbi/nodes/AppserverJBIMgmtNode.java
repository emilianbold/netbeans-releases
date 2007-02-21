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

package org.netbeans.modules.sun.manager.jbi.nodes;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;

import org.netbeans.modules.j2ee.sun.bridge.apis.AppserverMgmtNode;
import org.netbeans.modules.sun.manager.jbi.util.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.util.JBIPropertySupportFactory;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 * Abstract super class for all nodes in JBI manager.
 *
 * @author jqian
 */
public abstract class AppserverJBIMgmtNode extends AppserverMgmtNode {
    
    private AppserverJBIMgmtController appsrvrJBIMgmtController;
    
    private JBIPropertySupportFactory propSupportFactory = 
        JBIPropertySupportFactory.getInstance();
    
    
    /**
     *
     *
     */
    public AppserverJBIMgmtNode(final AppserverJBIMgmtController controller, 
            final Children children, final String nodeType) {
        super(children, nodeType);
        appsrvrJBIMgmtController = controller;
    }
    
    
    /**
     *
     *
     */
    public AppserverJBIMgmtController getAppserverJBIMgmtController() {
        try {
            if(appsrvrJBIMgmtController == null) { 
                getLogger().log(Level.FINE, "AppserverJBIMgmtController is " + "null for [" + getNodeType() + "]");  // NOI18N
            }
        } catch(Exception e) {
            getLogger().log(Level.FINE, e.getMessage(), e);
        }
        return appsrvrJBIMgmtController;
    }
    
    /**
     *
     */
    protected String getNodeDisplayName() {
        return NbBundle.getMessage(AppserverJBIMgmtNode.class, getNodeType());
    }
    
       
    /**
     *
     */
    protected String getNodeShortDescription() {
        return NbBundle.getMessage(AppserverJBIMgmtNode.class, getNodeType() + "_SHORT_DESC");  // NOI18N
    }
    
    /**
     * Creates a properties Sheet for viewing when a user chooses the option
     * from the right-click menu.
     *
     * @returns the Sheet to display when Properties is chosen by the user.
     */
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        
        try {             
            Thread.currentThread().setContextClassLoader(
                    this.getClass().getClassLoader());
            
            Sheet.Set props = sheet.get(Sheet.PROPERTIES);
            props.put(createPropertySupportArray(getSheetProperties()));
            return sheet;
        } catch(RuntimeException rex) {
            return sheet;
        } finally {
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }
    
    
    /**
     * Creates a PropertySupport array from a map of component properties.
     *
     * @param properties The properties of the component.
     * @return An array of PropertySupport objects.
     */
    private PropertySupport[] createPropertySupportArray(final Map attrMap) {
        PropertySupport[] supports = new PropertySupport[attrMap.size()];
        int i = 0;
        
        for(Iterator itr = attrMap.keySet().iterator(); itr.hasNext(); ) {
            Attribute attr = (Attribute) itr.next();
            MBeanAttributeInfo info = (MBeanAttributeInfo) attrMap.get(attr);
            supports[i] = 
                propSupportFactory.getPropertySupport(this, attr, info);
            i++;
        }
        return supports; 
    }
    
    
    /**
     * Returns all the properties of the leaf node to disply in the properties
     * window (or Sheet). This must be overriden in order for the Sheet to be
     * processed.
     *
     * @returns a java.util.Map of all properties to be accessed from the Sheet.
     */
    protected abstract Map getSheetProperties();
    
    /**
     * Sets the property as an attribute to the underlying AMX mbeans. It 
     * usually will delegate to the controller object which is responsible for
     * finding the correct AMX mbean objectname in order to execute a 
     * JMX setAttribute.
     *
     * @param attrName The name of the property to be set.
     * @param value The value retrieved from the property sheet to be set in the
     *        backend.
     * @returns the updated Attribute accessed from the Sheet.
     */
    public abstract Attribute setSheetProperty(String attrName, Object value);
}

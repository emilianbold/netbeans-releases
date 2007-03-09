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

import java.awt.Image;
import java.lang.reflect.Method;
import java.util.Map;

import javax.management.Attribute;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.netbeans.modules.j2ee.sun.bridge.apis.AppserverMgmtController;
import org.netbeans.modules.j2ee.sun.bridge.apis.RefreshAction;
import org.netbeans.modules.sun.manager.jbi.management.JBIFrameworkService;
import org.netbeans.modules.sun.manager.jbi.util.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.util.NodeTypes;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.openide.actions.PropertiesAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;

/**
 * Top node for the JBI lifecycle module.
 *
 * @author jqian
 */
public class JBINode extends AppserverJBIMgmtContainerNode {  
        
    /** Creates a new instance of JBINode */
    public JBINode(final AppserverJBIMgmtController controller) {        
        super(controller, NodeTypes.JBI);
        
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
////                String serverDisplayName = getParentNode().getName(); 
////                controller.setServerInstanceDisplayName(serverDisplayName);
//                controller.setJBINode(JBINode.this);
//            }
//        });
    }
    
    /**
     * Return the actions associated with the menu drop down seen when
     * a user right-clicks on an Applications node in the plugin.
     *
     * @param boolean true/false
     * @return An array of Action objects.
     */
    public Action[] getActions(boolean flag) {
        return new SystemAction[] {
            SystemAction.get(PropertiesAction.class),
            SystemAction.get(RefreshAction.class)
        };
    }
    
    /**
     * 
     */
    public Image getIcon(int type) {
        return new ImageIcon(JBINode.class.getResource(IconConstants.JBI_ICON)).getImage(); 
    }
    
    //  For now, use the same open for open/closed state
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    /**
     * Return the SheetProperties to be displayed for this JVM.
     *
     * @return A java.util.Map containing all JVM properties.
     */
    protected Map<Attribute, MBeanAttributeInfo> getSheetProperties() {                
        JBIFrameworkService frameworkService = getJBIFrameworkService();
        return Utils.getIntrospectedPropertyMap(frameworkService, true);
    }
  
    // FIXME
    public Attribute setSheetProperty(String attrName, Object value) {        
        AppserverJBIMgmtController controller = getAppserverJBIMgmtController();
        controller.setJBIFrameworkServiceDefaultLogProperty((String)value);
        
        // Get the new value
        Object newValue = getDefaultLogPropertyValue();
        return new Attribute(attrName, newValue);
    }
       
    private String getDefaultLogPropertyValue() {
        JBIFrameworkService frameworkService = getJBIFrameworkService();
        return frameworkService.getDefaultLogPropertyValue();
    }
        
    private JBIFrameworkService getJBIFrameworkService() {
        AppserverJBIMgmtController controller = getAppserverJBIMgmtController();
        return controller.getJBIFrameworkService();
    }
    
    public HelpCtx getHelpCtx() { 
        return new HelpCtx(JBINode.class);
    }
}

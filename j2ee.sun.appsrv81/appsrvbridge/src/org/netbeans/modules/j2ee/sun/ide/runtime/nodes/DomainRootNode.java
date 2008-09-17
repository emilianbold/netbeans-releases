/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 */
package org.netbeans.modules.j2ee.sun.ide.runtime.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import javax.swing.Action;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.bridge.apis.AppserverMgmtContainerNode;
import org.netbeans.modules.j2ee.sun.bridge.apis.AppserverMgmtController;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.j2ee.sun.util.NodeTypes;
import org.netbeans.modules.j2ee.sun.ide.controllers.ControllerUtil;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.sun.util.PropertySupportFactory;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 */
public class DomainRootNode extends AppserverMgmtContainerNode  implements  PropertyChangeListener, Runnable{
    
    private static final String NODE_TYPE = NodeTypes.DOMAIN;
    private PropertySupportFactory propSupportFactory = 
                PropertySupportFactory.getInstance();
    private DeploymentManager deployMgr;
    
    
    /**
     *
     */
    public DomainRootNode(final DeploymentManager dm) {
        super(null, NODE_TYPE);
        deployMgr  =dm;
        SunDeploymentManagerInterface sdm=(SunDeploymentManagerInterface)deployMgr;
        sdm.addPropertyChangeListener(this);
        /*
         this is to make sure classes are loaded in IDE classloader if
         user runs an app from the projects tab
        */
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                 refresh();
            }
        });
        
        shutOffJMXAndAMXLogging();
    }
    
    public void refresh(){
        RequestProcessor.getDefault().post(this);
    }
    
    public void run() {
        SunDeploymentManagerInterface sdm=(SunDeploymentManagerInterface)deployMgr;
        if (sdm.isSuspended()) {
            return;
        }
        Thread holder = Thread.currentThread();
        if (sdm.grabInnerDM(holder, true)) {
            try {
                AppserverMgmtController a= ControllerUtil.getAppserverMgmtControllerFromDeployMgr( deployMgr);
                setAppserverMgmtController(a);
                super.refresh();
            } finally {
                sdm.releaseInnerDM(holder);
            }
        } else {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                     refresh();
                }
            });
        }
    }    
    /**
     * Return the actions associated with the menu drop down seen when
     * a user right-clicks on an the node in the plugin. This method here
     * is overidden to eliminate duplicate refresh options at the server
     * node level.
     *
     * @param boolean true/false
     * @return An array of Action objects.
     */
    public Action[] getActions(boolean flag) {
        return new SystemAction[] {};
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
    private PropertySupport[] createPropertySupportArray(final java.util.Map attrMap) {
        PropertySupport[] supports = new PropertySupport[attrMap.size()];
        int i = 0;
        for(Iterator itr = attrMap.keySet().iterator(); itr.hasNext(); ) {
            Attribute attr = (Attribute) itr.next();
            MBeanAttributeInfo info = (MBeanAttributeInfo) attrMap.get(attr);
            supports[i] = 
                propSupportFactory.createLogLevelProperty(this, attr, info);
            i++;
        }
        return supports; 
    }
    
   /**
     * Return the SheetProperties to be displayed for the Domain Node
     *
     * @return A java.util.Map containing all Log levels
    */
    private java.util.Map getSheetProperties() {
        return getAppserverMgmtController().getLogProperties("server"); //NOI18N 
    }
    
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
    public javax.management.Attribute setSheetProperty(String attrName, Object value) {
        try {
            return getAppserverMgmtController().setLogProperties("server", attrName, value); //NOI18N
        } catch (RuntimeException rex) {
            return null;
        }
    }
    
    /**
     *
     *
     */
    private void shutOffJMXAndAMXLogging() {
        //local logger
        
        java.util.logging.Logger.getLogger(
                "org.netbeans.modules.j2ee.sun").
                        setLevel(java.util.logging.Level.OFF);
        
        //amx loggers
        java.util.logging.Logger.getLogger(
                "javax.enterprise.system.tools.admin").
                        setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger(
                "javax.enterprise.system.tools.admin.client").
                        setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger(
                "javax.enterprise.system.tools.admin.server").
                        setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger(
                "javax.enterprise.system.tools.admin.server.mbeans").
                        setLevel(java.util.logging.Level.OFF);
        
        //jmx remote logger
        java.util.logging.Logger.getLogger(
                "javax.management.remote").
                        setLevel(java.util.logging.Level.OFF);
    }

    public void propertyChange(PropertyChangeEvent evt) {

        
        //setAppserverMgmtController(ControllerUtil.getAppserverMgmtControllerFromDeployMgr( deployMgr));
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                refresh();
            }
        });
    }
    


}

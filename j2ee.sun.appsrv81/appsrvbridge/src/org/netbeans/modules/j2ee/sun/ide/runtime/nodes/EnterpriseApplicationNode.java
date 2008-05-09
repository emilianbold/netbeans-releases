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
import com.sun.appserv.management.j2ee.AppClientModule;
import com.sun.appserv.management.j2ee.EJBModule;
import com.sun.appserv.management.j2ee.WebModule;
import java.util.List;
import java.util.Arrays;
import javax.management.ObjectName;
import org.netbeans.modules.j2ee.sun.ide.controllers.AppClientModuleController;
import org.netbeans.modules.j2ee.sun.ide.controllers.ConnectorModuleController;
import org.netbeans.modules.j2ee.sun.ide.controllers.ControllerUtil;
import org.netbeans.modules.j2ee.sun.ide.controllers.EJBModuleController;

import org.netbeans.modules.j2ee.sun.ide.controllers.J2EEApplicationMgmtController;
import org.netbeans.modules.j2ee.sun.ide.controllers.WebModuleController;
import org.netbeans.modules.j2ee.sun.util.NodeTypes;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.j2ee.ResourceAdapterModule;

/**
 */
public class EnterpriseApplicationNode extends AppserverMgmtApplicationsNode {
        
    private static String NODE_TYPE = NodeTypes.ENTERPRISE_APPLICATION;
    
    
    public EnterpriseApplicationNode(J2EEApplicationMgmtController controller) {
        super(getChildNodes(controller), controller, NODE_TYPE, false);//not embedded
        setDisplayName(controller.getName());
    }

    
    /**
     * Returns all the children for this application.
     *
     * @param appName The name of the application.
     * @param controller The controller class to get the data for embedded
     *        components from AMX.
     * @return The Children for this application node.
     */
    static Children getChildNodes(J2EEApplicationMgmtController controller) {
        return createEnterpriseApplicationChildren(controller);
    }
    
   
    
    /**
     * Returns all the children for this application which could be all or 
     * one of either embedded web, ejb, resource, or app client modules.
     *
     * @param appName The name of the application.
     * @param controller The controller class to get the data for embedded
     *        components from AMX.
     * @return The Children for this particular application.
     */
    static Children createEnterpriseApplicationChildren(
            J2EEApplicationMgmtController controller) {
        
        Children children = new Children.Array();
        java.util.Vector nodes = new java.util.Vector();
        
        if(controller.getJ2EEObject() != null){
            //create all embedded ejb module nodes
            WebModuleController [] webControllers = controller.getWebModules();
            for(int i = 0; i < webControllers.length; i++) {
                nodes.add(new WebModuleNode(webControllers[i], true));
            }
            
            
            //create all embedded ejb module nodes
            EJBModuleController [] ejbControllers = controller.getEJBModules();
            for(int i = 0; i < ejbControllers.length; i++) {
                nodes.add(new EJBModuleNode(ejbControllers[i], true));
            }
            
            
            //create all embedded appclient module nodes
            AppClientModuleController [] appClientControllers =
                    controller.getAppClientModules();
            for(int i = 0; i < appClientControllers.length; i++) {
                nodes.add(new AppClientModuleNode(appClientControllers[i], true));
            }
            
            
            //create all embedded appclient module nodes
            ConnectorModuleController [] connectorControllers =
                    controller.getConnectorModules();
            for(int i = 0; i < connectorControllers.length; i++) {
                nodes.add(new ConnectorModuleNode(connectorControllers[i], true));
            }
        }else{
            ObjectName[] subComponents = ControllerUtil.getSubComponentsFromConfig(controller.getName(), controller.getMBeanServerConnection());
            for(int i=0; i<subComponents.length; i++){
                ObjectName oname = subComponents[i];
                String type = Util.getJ2EEType(oname);
                String name = Util.getName(oname);
                if(WebModule.J2EE_TYPE.equals(type)){
                    nodes.add(new WebModuleNode(name));
                }else if(EJBModule.J2EE_TYPE.equals(type)){
                    nodes.add(new EJBModuleNode(name)); 
                }else if (AppClientModule.J2EE_TYPE.equals(type)){
                    nodes.add(new AppClientModuleNode(name));
                }else if(ResourceAdapterModule.J2EE_TYPE.equals(type)){
                    nodes.add(new ConnectorModuleNode(name));
                }
            }
        }
        
        
        Node[] arrayToAdd = new Node[nodes.size()];
        children.add((Node[])nodes.toArray(arrayToAdd));
        return children;
    }
    
}

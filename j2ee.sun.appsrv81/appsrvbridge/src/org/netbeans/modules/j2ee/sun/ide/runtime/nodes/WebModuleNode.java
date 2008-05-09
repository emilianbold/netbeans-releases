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

import java.util.List;
import java.util.Arrays;
import javax.management.ObjectName;
import javax.swing.Action;


import org.netbeans.modules.j2ee.sun.util.NodeTypes;
import com.sun.appserv.management.base.Util;
import org.netbeans.modules.j2ee.sun.ide.controllers.ControllerUtil;
import org.netbeans.modules.j2ee.sun.ide.controllers.WebModuleController;
import org.netbeans.modules.j2ee.sun.ide.runtime.actions.EnableDisableAction;
import org.netbeans.modules.j2ee.sun.ide.runtime.actions.UndeployAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;




/**
 */
public class WebModuleNode extends AppserverMgmtApplicationsNode {
        
    private static String NODE_TYPE = NodeTypes.WEB_APPLICATION;
        
    /**
     *
     *
     */
    public WebModuleNode(WebModuleController controller) {
        super(getChildNodes(controller), controller, NODE_TYPE, false);
        setDisplayName(controller.getDisplayName());
    }

    
    /**
     *
     *
     */
    public WebModuleNode(final WebModuleController controller,
            final boolean isEmbedded) {
        super(getChildNodes(controller), controller, NODE_TYPE, isEmbedded);
        setDisplayName(controller.getDisplayName());
    }
    
    public WebModuleNode(final String name) {
        super(Children.LEAF, null, NODE_TYPE, true);
        setDisplayName(name);
    }
    
    /**
     *
     */
    static Children getChildNodes(WebModuleController controller) {
        return createWebModuleNodeChildren(controller);
    }
    /**
     * Return the actions associated with the menu drop down seen when
     * a user right-clicks on an Applications node in the plugin.
     *
     * @param boolean true/false
     * @return An array of Action objects.
     */
    public Action[] getActions(boolean flag) {
        if(!isEmbedded) {
            return new SystemAction[] {
                SystemAction.get(UndeployAction.class),
                SystemAction.get(EnableDisableAction.class),
                SystemAction.get(PropertiesAction.class)
            };
        } else {
            return new SystemAction[] {
                SystemAction.get(PropertiesAction.class)
            };
        }
    }    
    
    /**
     *
     */
    static Children createWebModuleNodeChildren(WebModuleController controller) {
        
        Children children = new Children.Array();
        java.util.Vector nodes = new java.util.Vector();
        
        //create all embedded servlets
        if(controller.getJ2EEObject() != null){
            String [] servlets = controller.getServlets();
            if(servlets != null && servlets.length > 0) {
                for(int i = 0; i < servlets.length; i++) {
                    nodes.add(new ServletNode(controller, servlets[i]));
                }
            }
        }else{
            ObjectName[] subComponents = ControllerUtil.getSubComponentsFromConfig(controller.getName(), controller.getMBeanServerConnection());
            for(int i=0; i<subComponents.length; i++){
                ObjectName oname = subComponents[i];
                String name = Util.getName(oname);
                nodes.add(new ServletNode(name));
            };
        }
        
        Node[] arrayToAdd = new Node[nodes.size()];
        children.add((Node[])nodes.toArray(arrayToAdd));
        return children;  
    }

}

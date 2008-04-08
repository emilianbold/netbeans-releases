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
import org.netbeans.modules.j2ee.sun.ide.controllers.ControllerUtil;

import com.sun.appserv.management.base.Util;
import org.netbeans.modules.j2ee.sun.util.NodeTypes;
import org.netbeans.modules.j2ee.sun.ide.controllers.ConnectorModuleController;
import org.openide.nodes.Children;
import org.openide.nodes.Node;




/**
 */
public class ConnectorModuleNode extends AppserverMgmtApplicationsNode {
        
    private static String NODE_TYPE = NodeTypes.CONNECTOR_MODULE;       
    
    /**
     *
     *
     */
    public ConnectorModuleNode(final ConnectorModuleController controller,
            final boolean isEmbedded) {
        super(getChildNodes(controller), controller, NODE_TYPE, isEmbedded);
        setDisplayName(controller.getName());
    }

    
    /**
     *
     *
     */
    public ConnectorModuleNode(final ConnectorModuleController controller) {
        super(getChildNodes(controller), controller, NODE_TYPE, false);
        setDisplayName(controller.getName());
    }
    
    public ConnectorModuleNode(final String name) {
        super(Children.LEAF, null, NODE_TYPE, true);
        setDisplayName(name);
    }
    
    /**
     *
     */
    static Children getChildNodes(ConnectorModuleController controller) {
        return createConnectorModuleNodeChildren(controller);
    }
    
    
    /**
     *
     */
    static Children createConnectorModuleNodeChildren(
            ConnectorModuleController controller) {
        Children children = new Children.Array();
        java.util.Vector nodes = new java.util.Vector();
        
        //create all embedded servlets
        if(controller.getJ2EEObject() != null){
            //create all the resource adapters
            String [] names = controller.getResourceAdaptors();
            if(names != null && names.length > 0) {
                for(int i = 0; i < names.length; i++) {
                    nodes.add(new ResourceAdapterNode(controller, names[i]));
                }
            }
        }else{
            ObjectName[] subComponents = ControllerUtil.getSubComponentsFromConfig(controller.getName(), controller.getMBeanServerConnection());
            for(int i=0; i<subComponents.length; i++){
                ObjectName oname = subComponents[i];
                String name = Util.getName(oname);
                nodes.add(new ResourceAdapterNode(name));
            };
        }
        
        Node[] arrayToAdd = new Node[nodes.size()];
        children.add((Node[])nodes.toArray(arrayToAdd));
        return children;  
    }
    
}

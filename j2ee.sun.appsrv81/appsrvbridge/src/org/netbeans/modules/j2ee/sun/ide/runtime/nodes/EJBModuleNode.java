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
import org.netbeans.modules.j2ee.sun.util.NodeTypes;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.j2ee.EntityBean;
import com.sun.appserv.management.j2ee.MessageDrivenBean;
import com.sun.appserv.management.j2ee.StatefulSessionBean;
import com.sun.appserv.management.j2ee.StatelessSessionBean;
import org.netbeans.modules.j2ee.sun.ide.controllers.EJBModuleController;
import org.openide.nodes.Children;
import org.openide.nodes.Node;




/**
 */
public class EJBModuleNode extends AppserverMgmtApplicationsNode {
        
    private static String NODE_TYPE = NodeTypes.EJB_MODULE;
      
    /**
     *
     */
    public EJBModuleNode(final EJBModuleController controller,
            final boolean isEmbedded) {
        super(getChildNodes(controller), controller, NODE_TYPE, isEmbedded);
        setDisplayName(controller.getName());
    }
    
    
    /**
     *
     */
    public EJBModuleNode(final EJBModuleController controller) {
        super(getChildNodes(controller), controller, NODE_TYPE, false);
        setDisplayName(controller.getName());
    }
    
    public EJBModuleNode(final String name) {
        super(Children.LEAF, null, NODE_TYPE, true);
        setDisplayName(name);
    }
    
    /**
     *
     */
    static Children getChildNodes(EJBModuleController controller) {
        return createEJBModuleNodeChildren(controller);
    }
    
    
    /**
     *
     */
    static Children createEJBModuleNodeChildren(EJBModuleController controller) {
        
        Children children = new Children.Array();
        java.util.Vector nodes = new java.util.Vector();

        if(controller.getJ2EEObject() != null){
            //create all embedded stateless ejbs
            String [] statelessNames = controller.getStatelessSessionBeans();
            if(statelessNames != null && statelessNames.length > 0) {
                for(int i = 0; i < statelessNames.length; i++) {
                    nodes.add(new StatelessEjbNode(
                            controller, statelessNames[i]));
                }
            }
            
            //create all embedded stateful ejbs
            String [] statefulNames = controller.getStatefulSessionBeans();
            if(statefulNames != null && statefulNames.length > 0) {
                for(int i = 0; i < statefulNames.length; i++) {
                    nodes.add(new StatefulEjbNode(
                            controller, statefulNames[i]));
                }
            }
            
            //create all embedded message driven ejbs
            String [] messageDrivenNames = controller.getMessageDrivenBeans();
            if(messageDrivenNames != null && messageDrivenNames.length > 0) {
                for(int i = 0; i < messageDrivenNames.length; i++) {
                    nodes.add(new MessageDrivenEjbNode(
                            controller, messageDrivenNames[i]));
                }
            }
            
            //create all embedded entity ejbs
            String [] entityBeans = controller.getEntityBeans();
            if(entityBeans != null && entityBeans.length > 0) {
                for(int i = 0; i < entityBeans.length; i++) {
                    nodes.add(new EntityEjbNode(
                            controller, entityBeans[i]));
                }
            }
        }else{
            ObjectName[] subComponents = ControllerUtil.getSubComponentsFromConfig(controller.getName(), controller.getMBeanServerConnection());
            for(int i=0; i<subComponents.length; i++){
                ObjectName oname = subComponents[i];
                String type = Util.getJ2EEType(oname);
                String name = Util.getName(oname);
                if(EntityBean.J2EE_TYPE.equals(type)){
                    nodes.add(new EntityEjbNode(name));
                }else if(MessageDrivenBean.J2EE_TYPE.equals(type)){
                    nodes.add(new MessageDrivenEjbNode(name)); 
                }else if (StatefulSessionBean.J2EE_TYPE.equals(type)){
                    nodes.add(new StatefulEjbNode(name));
                }else if(StatelessSessionBean.J2EE_TYPE.equals(type)){
                    nodes.add(new StatelessEjbNode(name));
                }
            };
        }
        
        Node[] arrayToAdd = new Node[nodes.size()];
        children.add((Node[])nodes.toArray(arrayToAdd));
        return children;  
    }
    
}

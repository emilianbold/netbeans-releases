/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.j2ee.sun.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServerConnection;
import org.netbeans.modules.j2ee.sun.api.SimpleNodeExtensionProvider;
import org.netbeans.modules.j2ee.sun.bridge.apis.AppserverMgmtContainerNode;


import org.netbeans.modules.j2ee.sun.bridge.apis.AppserverMgmtController;
import org.netbeans.modules.j2ee.sun.bridge.apis.NodeExtension;
import org.netbeans.modules.j2ee.sun.ide.controllers.ControllerUtil;
import org.netbeans.modules.j2ee.sun.ide.controllers.J2EEApplicationMgmtController;
import org.netbeans.modules.j2ee.sun.ide.controllers.EJBModuleController;
import org.netbeans.modules.j2ee.sun.ide.controllers.ConnectorModuleController;
import org.netbeans.modules.j2ee.sun.ide.controllers.WebModuleController;
import org.netbeans.modules.j2ee.sun.ide.controllers.AppClientModuleController;
import org.netbeans.modules.j2ee.sun.ide.controllers.SIPController;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.AdminObjectResourceNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.AppClientModuleNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.ConnectionFactoryNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.ConnectionPoolNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.ConnectorConnectionPoolNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.ConnectorModuleNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.ConnectorResourceNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.CustomResourceNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.DestinationResourceNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.EJBModuleNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.EnterpriseApplicationNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.ExternalResourceNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.JDBCResourceNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.JVMNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.JavaMailSessionResourceNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.PersistenceManagerResourceNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.SIPModuleNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.WebModuleNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class ContainerChildFactory {
    
    private AppserverMgmtController controller;
    
    /** 
     * Public constructor for factory used to create the children of a given 
     * NodeType. 
     *
     * @param controller The AppserverMgmtController used as an interface to
     *        the AMX API necessary for determining the existence of certain
     *        components such as resources, apps, etc. on the server.
     */
    public ContainerChildFactory(AppserverMgmtController controller) {
        this.controller = controller;
    }
    
        public static final class WaitNode extends AbstractNode {
        public WaitNode() {
            super(Children.LEAF);
            setIconBase("org/netbeans/modules/java/navigation/resources/wait"); // NOI18N
            setDisplayName(NbBundle.getMessage(ContainerChildFactory.class, "MSG_BadCredentials")); //NOI18N
            setName(getClass().getName());
        }
        }
    /**
     * Creates the children for a given NodeType. 
     *
     * @param type The NodeType typs for a particular node.
     * @return The Children object containing a Node[] array of children.
     */
        
     public Object[] getChildrenObject(String type) {  
         if ((controller==null)||(controller.getAppserverConnectionSource()==null)){
             return new Node[] {new WaitNode()};
         }

        Object[] children = new Object[] {};
        if(NodeTypes.DOMAIN.equals(type)) {
            children = createDomainRootChildren();
        } else if(NodeTypes.JDBC_RESOURCES.equals(type)) {
            children = createJDBCResourcesChildren();
        } else if(NodeTypes.CONNECTION_POOLS.equals(type)) {
            children = createConnectionPoolChildren();
        } else if(NodeTypes.CONNECTION_FACTORIES.equals(type)) {
            children = createJMSConnectionFactoriesChildren();
        } else if(NodeTypes.DESTINATION_RESOURCES.equals(type)) {
            children = createJMSDestinationResourcesChildren();
        } else if(NodeTypes.PERSISTENCE_MANAGER_RESOURCES.equals(type)) {
            children = createPersistenceManagerResourcesChildren();
        } else if(NodeTypes.CONNECTOR_RESOURCES.equals(type)) {
            children = createConnectorResourcesChildren();
        } else if(NodeTypes.CONNECTOR_CONNECTION_POOLS.equals(type)) {
            children = createConnectorConnectionPoolChildren();
        } else if(NodeTypes.ADMIN_OBJECT_RESOURCES.equals(type)) {
            children = createAdminObjectResourcesChildren();
        } else if(NodeTypes.MAIL_RESOURCES.equals(type)) {
            children = createJavaMailSessionResourcesChildren();
        } else if(NodeTypes.WEB_APPLICATIONS.equals(type)) {
            children = createWebApplicationsChildren();
        } else if(NodeTypes.ENTERPRISE_APPLICATIONS.equals(type)) {
            children = createEnterpriseApplicationsChildren(); 
        } else if(NodeTypes.EJB_MODULES.equals(type)) {
            children = createEJBModulesChildren();
        } else if(NodeTypes.CONNECTOR_MODULES.equals(type)) {
            children = createConnectorModulesChildren();
        } else if(NodeTypes.APP_CLIENT_MODULES.equals(type)) {
            children = createAppClientModulesChildren();
        } else if(NodeTypes.CUSTOM_RESOURCES.equals(type)) {
            children = createCustomResourcesChildren();
        } else if(NodeTypes.EXTERNAL_RESOURCES.equals(type)) {
            children = createExternalResourcesChildren();
         } else if(NodeTypes.SIP_APPLICATIONS.equals(type)) {
             children = createSIPModulesChildren();
        } else {
            children = createContainerChildren(type);
        } 
        
        return children;
    }
      
     Node[] createSIPModulesChildren() {
         SIPController [] controllers = controller.getJ2EEServerMgmtController().getSIPModules();
         Node[] nodes = new Node[controllers.length];
         for(int i = 0; i < controllers.length; i++) {
            SIPController sipControl = controllers[i];
            nodes[i] = new SIPModuleNode(sipControl, sipControl.getSIPType());
         }
         return (controllers.length > 0) ? nodes : new Node[] {};
     }
     
    /**
     *
     */
    Node[] createContainerChildren(String nodeType) {
        String[] childTypes = NodeTypes.getChildTypes(nodeType);
        if(childTypes != null) {
            Node[] nodes = new Node[childTypes.length];
            for(int i = 0; i < childTypes.length; i++) {
                nodes[i] = 
                    new AppserverMgmtContainerNode(controller, childTypes[i]);
            }    
             if (nodeType.equals(NodeTypes.APPLICATIONS)) {
                 nodes = updateSIPNode(nodes);
             }
            return nodes;
        } else {
            return new Node[] {};
        }
    }

     Node[] updateSIPNode(Node[] nodes){
         Node[] baseNodes = nodes;
         if(controller.isSIPEnabled()){
             int size = baseNodes.length + 1;
             Node[] updatedNodes = new Node[baseNodes.length + 1];
             for(int i = 0; i < baseNodes.length; i++) {
                 updatedNodes[i] = baseNodes[i];
             } 
             updatedNodes[baseNodes.length] = new AppserverMgmtContainerNode(controller, NodeTypes.SIP_APPLICATIONS);
             return updatedNodes;
         }else{
             return baseNodes; 
         }
     }
     
    /**
     *
     */
    Node[] createDomainRootChildren() {
        List extNodes = getExtensionNodes();
        Node resourceHolder = getResourcesNode();
        
        Node[] nodes = new Node[] { 
            new AppserverMgmtContainerNode(controller, NodeTypes.APPLICATIONS),
            resourceHolder,
            new JVMNode(controller)
        };

        if (extNodes.size() == 0) {
            return nodes;
        } else {
            List nodeList = new ArrayList(Arrays.asList(nodes));
            nodeList.addAll(extNodes);
            return (Node[]) nodeList.toArray(new Node[nodeList.size()]);
        }
    }
    
   List getExtensionNodes() {
       // I would suggest to use the system classloader 
       // (Lookup.getDefault().lookup(ClassLoader.class)) instead of 
       // this class' custom classloader (this.getClass().getClassLoader()) 
       // to load all the node extensions. This would give the extension
       // classes more flexibility in their NB module dependency declaration.
       //    -Jun 7/28/07
       Iterator ps = javax.imageio.spi.ServiceRegistry.lookupProviders(NodeExtension.class , this.getClass().getClassLoader());
       List nodes = new ArrayList();

       while (ps.hasNext()) {
           NodeExtension cc = (NodeExtension)ps.next();
           if (cc!= null) {
                Node ce = cc.getAppserverExtensionNode(controller);
                if (ce != null){
                    nodes.add(ce);
                }
            }
       }
       
       // Since there are still other modules (e.x., Identity) depending on the 
       // NodeExtension SPI, a new interface SimpleNodeExtenstionProvider  
       // (which only needs a MBeanServerConnection) is introduced to allow 
       // the JBI Manager to be plugged into the appserver node.
       //  -Jun 7/28/07
       MBeanServerConnection connection = controller.getMBeanServerConnection();
       ps = Lookup.getDefault().lookupAll(SimpleNodeExtensionProvider.class).iterator();
       while (ps.hasNext()) {
           SimpleNodeExtensionProvider nep = (SimpleNodeExtensionProvider)ps.next();
           if (nep != null) {
               Node node;
               try {
                   node = nep.getExtensionNode(connection);
                   if (node != null) {
                       nodes.add(node);
                   }
               } catch (Exception ex) {
                   Logger.getLogger("global").log(Level.SEVERE,
                           NbBundle.getMessage(this.getClass(),
                           "WARN_BOGUS_GET_EXTENSION_NODE_IMPL", // NOI18N
                           nep.getClass().getName()));
                   Logger.getLogger("global").log(Level.FINER,
                           NbBundle.getMessage(this.getClass(),
                           "WARN_BOGUS_GET_EXTENSION_NODE_IMPL", // NOI18N
                           nep.getClass().getName()), ex);
               } catch (AssertionError ae) {
                   // deal with JBI failure mode explicitly.
                   Logger.getLogger("global").log(Level.SEVERE,
                           NbBundle.getMessage(this.getClass(),
                           "WARN_BOGUS_GET_EXTENSION_NODE_IMPL", // NOI18N
                           nep.getClass().getName()+".")); // NOI18N
                   Logger.getLogger("global").log(Level.FINER,
                           NbBundle.getMessage(this.getClass(),
                           "WARN_BOGUS_GET_EXTENSION_NODE_IMPL", // NOI18N
                           nep.getClass().getName()), ae);
               }
            }
       }
       
       return nodes;
   } 
   
   Node getResourcesNode(){
       Node resourceHolder = new AppserverMgmtContainerNode(controller, NodeTypes.RESOURCES90);
       if(! ControllerUtil.isGlassFish(this.controller.getDeploymentManager()))
           resourceHolder = new AppserverMgmtContainerNode(controller, NodeTypes.RESOURCES);
         
       return resourceHolder;
   }
    /**
     *
     */
    Node[] createJDBCResourcesChildren() {
    String [] names = controller.getJDBCResources();
        Node[] nodes = new Node[names.length];
        for(int i = 0; i < names.length; i++) {
            nodes[i] = new JDBCResourceNode(controller, names[i]);

        }
        return (names.length > 0) ? nodes : new Node[] {};
    }
    
  
    /**
     *
     */
    Node[] createConnectionPoolChildren() {
        String [] names = controller.getJDBCConnectionPools();
        Node[] nodes = new Node[names.length];
        for(int i = 0; i < names.length; i++) {
            nodes[i] = new ConnectionPoolNode(controller, names[i]);
        }
        return (names.length > 0) ? nodes : new Node[] {};
    }
    
    
    /**
     *
     */
    Node[] createJMSConnectionFactoriesChildren() {
        String [] names = controller.getJMSConnectionFactories();
        if(names != null) {
            Node[] nodes = new Node[names.length];
            for(int i = 0; i < names.length; i++) {
                nodes[i] = new ConnectionFactoryNode(controller, names[i]);
            }
            return (names.length > 0) ? nodes : new Node[] {};
        }
        return new Node[] {};
    }
    
    
    /**
     *
     */
    Node[] createJMSDestinationResourcesChildren() {
        String [] names = controller.getDestinationResources();
        if(names != null) {
            Node[] nodes = new Node[names.length];
            for(int i = 0; i < names.length; i++) {
                nodes[i] = new DestinationResourceNode(controller, names[i]);
            }
            return (names.length > 0) ? nodes : new Node[] {};
        }
        return new Node[] {};            
    }
    
    /**
     *
     */
    Node[] createConnectorResourcesChildren() {
        String [] names = controller.getConnectorResources();
        Node[] nodes = new Node[names.length];
        for(int i = 0; i < names.length; i++) {
            nodes[i] = new ConnectorResourceNode(controller, names[i]);
        }
        return (names.length > 0) ? nodes : new Node[] {};
    }
    
    /**
     *
     */
    Node[] createConnectorConnectionPoolChildren() {
        String [] names = controller.getConnectorConnectionPools();
        Node[] nodes = new Node[names.length];
        for(int i = 0; i < names.length; i++) {
            nodes[i] = new ConnectorConnectionPoolNode(controller, names[i]);
        }
        return (names.length > 0) ? nodes : new Node[] {};
    }
    
    /**
     *
     */
    Node[] createAdminObjectResourcesChildren() {
        String [] names = controller.getAdminObjectResources();
        if(names != null) {
            Node[] nodes = new Node[names.length];
            for(int i = 0; i < names.length; i++) {
               nodes[i] = new AdminObjectResourceNode(controller, names[i]);
            }
            return (names.length > 0) ? nodes : new Node[] {};
        }
        return new Node[] {};
    }
    
    /**
     *
     */
    Node[] createJavaMailSessionResourcesChildren() {
        String [] names = controller.getJavaMailSessionResources();   
        Node[] nodes = new Node[names.length];
        for(int i = 0; i < names.length; i++) {
            nodes[i] = new JavaMailSessionResourceNode(controller, names[i]);
        }
        return (names.length > 0) ? nodes : new Node[] {};
    }
    
    /**
     *
     */
    Node[] createPersistenceManagerResourcesChildren() {
        String [] names = controller.getPersistenceManagerFactoryResources();           
        Node[] nodes = new Node[names.length];
        for(int i = 0; i < names.length; i++) {
            nodes[i] = new PersistenceManagerResourceNode(controller, names[i]);
        }
        return (names.length > 0) ? nodes : new Node[] {};
    }
    
    /**
     *
     */
    Node[] createCustomResourcesChildren() {
        String [] names = controller.getCustomResources();
        Node[] nodes = new Node[names.length];
        for(int i = 0; i < names.length; i++) {
            nodes[i] = new CustomResourceNode(controller, names[i]);
        }
        return (nodes.length > 0) ? nodes : new Node[] {};
    }
    
    /**
     *
     */
    Node[] createExternalResourcesChildren() {
        String [] names = controller.getExternalResources(); 
        Node[] nodes = new Node[names.length];
        for(int i = 0; i < names.length; i++) {
            nodes[i] = new ExternalResourceNode(controller, names[i]);
        }
        return (nodes.length > 0) ? nodes : new Node[] {};
    }
    
   
    /**
     *
     */
    Node[] createWebApplicationsChildren() {
        WebModuleController [] controllers = 
            controller.getJ2EEServerMgmtController().getWebModules();   
        Node[] nodes = new Node[controllers.length];
        for(int i = 0; i < controllers.length; i++) {
            nodes[i] = new WebModuleNode(controllers[i]);
        }
        return (controllers.length > 0) ? nodes : new Node[] {};
    }
    
    
    /**
     *
     */
    Node[] createEnterpriseApplicationsChildren() {
        J2EEApplicationMgmtController [] controllers = 
            controller.getJ2EEServerMgmtController().getApplications();   
        Node[] nodes = new Node[controllers.length];
        for(int i = 0; i < controllers.length; i++) {
            nodes[i] = new EnterpriseApplicationNode(controllers[i]);
        }
        return (controllers.length > 0) ? nodes : new Node[] {};
    }
    
    
    /**
     * Creates all EJB modules deployed to the current instance. 
     *
     * @returns All children nodes.
     */
    Node[] createEJBModulesChildren() {
        EJBModuleController [] controllers = 
            controller.getJ2EEServerMgmtController().getEJBModules(); 
        Node[] nodes = new Node[controllers.length];
        for(int i = 0; i < controllers.length; i++) {
            nodes[i] = new EJBModuleNode(controllers[i]);
        }
        return (controllers.length > 0) ? nodes : new Node[] {};
    }
    
    
    /**
     * Creates all Connector modules deployed to the current instance. 
     *
     * @returns All children nodes.
     */
    Node[] createConnectorModulesChildren() {
        ConnectorModuleController [] controllers = 
            controller.getJ2EEServerMgmtController().getConnectorModules(); 
        Node[] nodes = new Node[controllers.length];
        for(int i = 0; i < controllers.length; i++) {
            nodes[i] = new ConnectorModuleNode(controllers[i]);
        }
        return (controllers.length > 0) ? nodes : new Node[] {};
    }
    
    
    /**
     * Creates all Connector modules deployed to the current instance. 
     *
     * @returns All children nodes.
     */
    Node[] createAppClientModulesChildren() {
        AppClientModuleController [] controllers =  
            controller.getJ2EEServerMgmtController().getAppClientModules(); 
        Node[] nodes = new Node[controllers.length];
        for(int i = 0; i < controllers.length; i++) {
            nodes[i] = new AppClientModuleNode(controllers[i]);
        }
        return (controllers.length > 0) ? nodes : new Node[] {};
    }
    
    
}

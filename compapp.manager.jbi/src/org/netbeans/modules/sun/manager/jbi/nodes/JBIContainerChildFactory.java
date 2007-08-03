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
import java.util.List;
import org.netbeans.modules.sun.manager.jbi.util.NodeTypes;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.management.AdministrationService;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIServiceAssemblyStatus;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIServiceUnitStatus;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 *
 */
public class JBIContainerChildFactory {
    
    private AppserverJBIMgmtController controller;
    
    /**
     * Public constructor for factory used to create the children of a given
     * NodeType.
     *
     * @param controller The AppserverMgmtController used as an interface to
     *        the AMX API necessary for determining the existence of certain
     *        components such as resources, apps, etc. on the server.
     */
    public JBIContainerChildFactory(AppserverJBIMgmtController controller) {
        this.controller = controller;
    }
    
    
    /**
     * Creates the children for a given NodeType.
     *
     * @param type The NodeType typs for a particular node.
     * @return The Children object containing a Node[] array of children.
     */
    public Children getChildren(Node node, String type) {
        Children children = new Children.Array();
        children.add((Node[]) getChildrenObject(node, type));        
        return children;
    }
    
    /**
     * 
     * @param node
     * @param type
     * @return
     */
    public Object[] getChildrenObject(Node node, String type) {
        Object[] children = new Object[] {};
        if (NodeTypes.JBI.equals(type)) {
            children = createJBIChildren();
        } else if (NodeTypes.SERVICE_ENGINES.equals(type)) {
            children = 
                createJBIComponentContainerChildren(
                        JBIComponentStatus.ENGINE_TYPE);
        } else if (NodeTypes.BINDING_COMPONENTS.equals(type)) {
            children = 
                createJBIComponentContainerChildren(
                        JBIComponentStatus.BINDING_TYPE);
        } else if (NodeTypes.SHARED_LIBRARIES.equals(type)) {
            children = 
                createJBIComponentContainerChildren(
                        JBIComponentStatus.NAMESPACE_TYPE);
        } else if (NodeTypes.SERVICE_ASSEMBLIES.equals(type)) {
            children = createServiceAssembliesChildren();
        } else if (NodeTypes.SERVICE_ASSEMBLY.equals(type)) {
            children = 
                createServiceAssemblyChildren(
                        (JBIServiceAssemblyNode) node);
        }
        return children;
    }
    
    
    /**
     * 
     * @return
     */
    private Node[] createJBIChildren() {
        return new Node[] {
                new JBIComponentContainerNode.ServiceEngines(controller), 
                new JBIComponentContainerNode.BindingComponents(controller), 
                new JBIComponentContainerNode.SharedLibraries(controller),
                new JBIServiceAssembliesNode(controller)
        };
    }
    
    /**
     * 
     * @param componentType
     * @return
     */
    private Node[] createJBIComponentContainerChildren(String componentType) {
        AdministrationService adminService = 
                controller.getJBIAdministrationService();
        List<JBIComponentStatus> compList = 
                adminService.getJBIComponentStatusList(componentType);
        
        Node[] nodes = new Node[compList.size()];
        
        int index = 0;
        for (JBIComponentStatus comp : compList) {
            String name = comp.getName();
            String description = comp.getDescription();
            
            Node newNode;
            if (componentType.equals(JBIComponentStatus.ENGINE_TYPE)) {
                newNode = 
                    new JBIComponentNode.ServiceEngine(
                            controller, name, description);
            } else if (componentType.equals(JBIComponentStatus.BINDING_TYPE)) {
                newNode = 
                    new JBIComponentNode.BindingComponent(
                            controller, name, description);
            } else {
                newNode = 
                    new JBIComponentNode.SharedLibrary(
                            controller, name, description);
            }
            nodes[index++] = newNode;
        }
        
        return nodes;
    }  
    
    /**
     * 
     * @return
     */
    private Node[] createServiceAssembliesChildren() {
        AdministrationService adminService =
                controller.getJBIAdministrationService();
        List<JBIServiceAssemblyStatus> assemblyList =
                adminService.getServiceAssemblyStatusList();
        
        Node[] nodes = new Node[assemblyList.size()];
        
        int index = 0;
        for (JBIServiceAssemblyStatus assembly : assemblyList) { 
            String name = assembly.getServiceAssemblyName();
            String description = assembly.getServiceAssemblyDescription();
            nodes[index++] = 
                new JBIServiceAssemblyNode(controller, name, description);
        }
        
        return nodes;
    }
    
    /**
     * 
     * @param node
     * @return
     */
    private Node[] createServiceAssemblyChildren(JBIServiceAssemblyNode node) {
        
        JBIServiceAssemblyStatus assembly = node.getAssembly();
        List unitList = assembly.getJbiServiceUnitStatusList();
        
        Node[] nodes = new Node[unitList.size()];
        
        int index = 0;        
        for (Iterator iter = unitList.iterator(); iter.hasNext();) {
            JBIServiceUnitStatus unit = (JBIServiceUnitStatus) iter.next();
            String unitName = unit.getServiceUnitName();
            String unitDescription = unit.getServiceUnitDescription();
            nodes[index++] = new JBIServiceUnitNode(
                    controller, unitName, unitName, unitDescription);
        }
        
        return nodes;
    }
}

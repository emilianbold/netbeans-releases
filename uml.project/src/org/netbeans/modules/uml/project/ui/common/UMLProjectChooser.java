/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.project.ui.common;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.uml.project.UMLActionProvider;
import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.ui.customizer.UMLProjectProperties;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.nodes.NodeOperation;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
//import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.CookieAction;
import org.openide.util.lookup.Lookups;

import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import java.util.Arrays;
import java.util.Iterator;

// this would create module dependency
//import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModuleContainer;

/**
 * Action that allows selection of the Java project to associate with UML project
 * @author Mike Frisino
 */
public class UMLProjectChooser {
    
   
        
     public Project[] getSelectedUMLProjects(UMLProjectProperties epp) throws UserCancelException {
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        List moduleProjectNodes = new LinkedList();
        
        // Filter out the inappropriate target projects
        // include non uml projects -  == UMLActionProvider
        // eliminate current project -  ?? TODO
        for (int i = 0; i < allProjects.length; i++) {
            if (allProjects[i].getLookup().lookup(UMLActionProvider.class) != null )
            {
                LogicalViewProvider lvp =
                    (LogicalViewProvider) allProjects[i].getLookup().lookup(LogicalViewProvider.class);
                Node mn = lvp.createLogicalView();
                Node n = new FilterNode(mn, new FilterNode.Children(mn), Lookups.singleton(allProjects[i]));
                moduleProjectNodes.add(n);
            }
        }
        Children.Array children = new Children.Array();
        children.add((Node[])moduleProjectNodes.toArray(new Node[moduleProjectNodes.size()]));
        final Node root = new AbstractNode(children);
        String moduleSelector = NbBundle.getMessage(UMLProjectChooser.class, "LBL_UMLProjectSelectorTitle");
        
        // Now filter out those nodes which are already included in subproject list
        UMLProject parent = epp.getProject();
        SubprojectProvider spp = (SubprojectProvider) parent.getLookup().lookup(SubprojectProvider.class);
        if (null != spp) {
            final Set s = spp.getSubprojects();
            NodeAcceptor na = new NodeAcceptor() {
                public boolean acceptNodes(Node[] nodes) {
                    for (int i = 0; i < nodes.length; i++) {
                        if (nodes[i].getParentNode() != root) {
                            return false;
                        }
                        // do not put this test befor the root test...
                        Project p = (Project) nodes[i].getLookup().lookup(Project.class);
                        if (null == p)
                            return false;
                        if (s.contains(p)) return false;
                    }
                    return nodes.length > 0;
                }
            };
            root.setDisplayName(NbBundle.getMessage(UMLProjectChooser.class, "LBL_OpenProjects"));
            Node[] selected = NodeOperation.getDefault().select(moduleSelector, root.getDisplayName(), root, na);
            Project[] modules = new Project[selected.length];
            for (int i = 0; i < modules.length; i++) {
                modules[i] = (Project) selected[i].getLookup().lookup(Project.class);
            }
            return modules;
      }
        else {
            return new Project[0];
        }
    }
     
}

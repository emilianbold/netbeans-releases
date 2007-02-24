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
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
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
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import org.openide.util.lookup.Lookups;

import org.netbeans.api.java.project.JavaProjectConstants;

// this would create module dependency
//import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModuleContainer;

/**
 * Action that allows selection of the Java project to associate with UML project
 * @author Mike Frisino
 */
public class JavaProjectChooser {
    
	// TODO - MCF - decide if we want to stick to this "only open projects" chooser
	// or go with the "file system wide chooser". We discussed the upside/downside
	// extensively at TOI.
    
    public static Project getSelectedJavaProject(UMLProjectProperties epp) throws UserCancelException {
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        List moduleProjectNodes = new LinkedList();
        
        // Filter out the inappropriate projects
        // TODO - complete filtering to best of ability
        // eliminate other uml projects - UMLActionProvider
        // eliminate ear projects - J2eeModuleContainer
        // eliminate current project - uml covered
        // ?? others?
        for (int i = 0; i < allProjects.length; i++) {
            if (allProjects[i].getLookup().lookup(UMLActionProvider.class) == null )
               //     &&
               // allProjects[i].getLookup().lookup(J2eeModuleContainer.class) == null) 
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
        String moduleSelector = NbBundle.getMessage(JavaProjectChooser.class, "LBL_JavaProjectSelectorTitle");
        
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
            
            // TODO - modify to limit selection to 1
            root.setDisplayName(NbBundle.getMessage(JavaProjectChooser.class, "LBL_OpenProjects"));
            Node[] selected = NodeOperation.getDefault().select(moduleSelector, root.getDisplayName(), root, na);
            Project[] modules = new Project[selected.length];
            for (int i = 0; i < modules.length; i++) {
                modules[i] = (Project) selected[i].getLookup().lookup(Project.class);
            }
            return modules[0];
      }
        else {
            return null;
        }
    }
  
    
    // This can be called from wizards when the current project does not
    // yet exist. So it needs less filtering logic.
    public static Project getSelectedJavaProject() throws UserCancelException {
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        List moduleProjectNodes = new LinkedList();
        
        // Filter out the inappropriate projects
        // TODO - complete filtering to best of ability
        // eliminate other uml projects - UMLActionProvider
        // eliminate ear projects - J2eeModuleContainer
        // eliminate current project - uml covered
        // ?? others?
        for (int i = 0; i < allProjects.length; i++) {
            
			//skip if NBModuleProject 
//			if(allProjects[i].getClass().getName().
//					equals("org.netbeans.modules.apisupport.project.NbModuleProject"))
//				continue;
			
            Sources srcs = (Sources) 
                allProjects[i].getLookup().lookup(Sources.class);
            
            if (allProjects[i].getLookup().lookup(UMLActionProvider.class) == null 
               && srcs != null)           
            {
                // now check for Java sources                  
                SourceGroup[] srcGrps = srcs.getSourceGroups(
                        JavaProjectConstants.SOURCES_TYPE_JAVA );
               
                if(srcGrps.length > 0) {                  
                    LogicalViewProvider lvp =
                    (LogicalViewProvider) allProjects[i].getLookup().lookup(LogicalViewProvider.class);
                    Node mn = lvp.createLogicalView();
                    Node n = new FilterNode(mn, new FilterNode.Children(mn), Lookups.singleton(allProjects[i]));
                    moduleProjectNodes.add(n);             
                }
            }
        }
        
        Children.Array children = new Children.Array();
        children.add((Node[])moduleProjectNodes.toArray(new Node[moduleProjectNodes.size()]));
        final Node root = new AbstractNode(children);
        String moduleSelector = NbBundle.getMessage(JavaProjectChooser.class, "LBL_JavaProjectSelectorTitle");
       
        // TODO - modify to limit selection to 1
        root.setDisplayName(NbBundle.getMessage(JavaProjectChooser.class, "LBL_OpenProjects"));
        Node selected = NodeOperation.getDefault().select(moduleSelector, root.getDisplayName(), root);
 
        return (Project) selected.getLookup().lookup(Project.class);
    }
     
}

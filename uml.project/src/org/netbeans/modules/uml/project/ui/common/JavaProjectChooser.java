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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

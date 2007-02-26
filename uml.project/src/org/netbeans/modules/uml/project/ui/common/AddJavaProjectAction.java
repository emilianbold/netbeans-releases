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
import org.netbeans.modules.uml.core.support.Debug;

// this would create module dependency so I am avoiding for now
//import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModuleContainer;

/**
 * Action that allows selection of the Java project to associate with UML project
 * @author Mike Frisino
 */
public class AddJavaProjectAction extends CookieAction
{
	
    // TODO - MXF 
    // this class is currently not being used at all. 
    // It can be removed and nothing will break.

    // I leave it in merely as an example of code that could be used if we
    // wanted to have an ACTION as well as the customizer.
    // This was modeled on similar Add action in the J2EE project. Ask
    // Chris Webster if you have questions. He is the one that pointed it out
    // to me.

    private static final Class[] COOKIE_ARRAY =
        new Class[] {UMLProjectProperties.class};
    
    public Class[] cookieClasses() 
    {
        return COOKIE_ARRAY;
    }
    
    public int mode() 
    {
        return CookieAction.MODE_EXACTLY_ONE;
    }
    
    public void performAction(Node[] activeNodes) 
	{
		    /* NB60TBD
        try 
		{
            UMLProjectProperties epp = 
                (UMLProjectProperties) activeNodes[0].getCookie(
                    UMLProjectProperties.class);
            
            Project targetProject = getSelectedJavaProject(epp);
         
            // epp.addJ2eeSubprojects(moduleProjects);
            // Debug.out.println("MCF - todo");
        }
		
		catch (UserCancelException uce) 
        {
            // this action has been cancelled
        }
		    */
    }
    
    public String getName() 
    {
        return NbBundle.getMessage(
            AddJavaProjectAction.class, "LBL_AddJavaProjectAction");
    }
    
    public HelpCtx getHelpCtx() 
    {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(AddModuleAction.class);
    }
    
    protected boolean asynchronous() 
    {
        // performAction() should run in event thread
        return false;
    }
    
    public static Project getSelectedJavaProject(UMLProjectProperties epp) 
        throws UserCancelException 
    {
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        List moduleProjectNodes = new LinkedList();
        
        // Filter out the inappropriate projects
        // TODO - complete filtering to best of ability
        // eliminate other uml projects - UMLActionProvider
        // eliminate ear projects - J2eeModuleContainer
        // eliminate current project - uml covered
        // ?? others?
        for (int i = 0; i < allProjects.length; i++) {
            if (allProjects[i].getLookup().lookup(
                UMLActionProvider.class) == null )
                // &&
                // allProjects[i].getLookup().lookup(
                // J2eeModuleContainer.class) == null) 
            {
                LogicalViewProvider lvp = (LogicalViewProvider) allProjects[i]
                    .getLookup().lookup(LogicalViewProvider.class);
                
                Node mn = lvp.createLogicalView();
                Node n = new FilterNode(mn, new FilterNode.Children(mn), 
                    Lookups.singleton(allProjects[i]));
                
                moduleProjectNodes.add(n);
            }
        }
        Children.Array children = new Children.Array();
        children.add((Node[])moduleProjectNodes.toArray(
            new Node[moduleProjectNodes.size()]));
        
        final Node root = new AbstractNode(children);
        String moduleSelector = NbBundle.getMessage(
            AddJavaProjectAction.class, 
            "LBL_JavaProjectSelectorTitle"); // NOI18N
        
        UMLProject parent = epp.getProject();
        SubprojectProvider spp = (SubprojectProvider)parent.getLookup()
            .lookup(SubprojectProvider.class);
        
        if (null != spp) 
        {
            final Set s = spp.getSubprojects();
            NodeAcceptor na = new NodeAcceptor() 
            {
                public boolean acceptNodes(Node[] nodes) 
                {
                    for (int i = 0; i < nodes.length; i++) 
                    {
                        if (nodes[i].getParentNode() != root) 
                            return false;
                       
                        // do not put this test befor the root test...
                        Project p = (Project)nodes[i]
                            .getLookup().lookup(Project.class);
                        
                        if (null == p)
                            return false;
                        
                        if (s.contains(p)) 
                            return false;
                    }
                    
                    return nodes.length > 0;
                }
            };
            
            // TODO - modify to limit selection to 1
            root.setDisplayName(NbBundle.getMessage(
                AddJavaProjectAction.class, "LBL_OpenProjects")); // NOI18N
            
            Node[] selected = NodeOperation.getDefault().select(
                moduleSelector, root.getDisplayName(), root, na);
            
            Project[] modules = new Project[selected.length];
            
            for (int i = 0; i < modules.length; i++) 
            {
                modules[i] = (Project) selected[i]
                    .getLookup().lookup(Project.class);
            }
            return modules[0];
        }
        
        else 
            return null;
    }
  
    
    // This can be called from wizards when the current project does not
    // yet exist. So it needs less filtering logic.
    public static Project getSelectedJavaProject() throws UserCancelException
    {
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        List moduleProjectNodes = new LinkedList();
        
        // Filter out the inappropriate projects
        // TODO - complete filtering to best of ability
        // eliminate other uml projects - UMLActionProvider
        // eliminate ear projects - J2eeModuleContainer
        // eliminate current project - uml covered
        // ?? others?
        for (int i = 0; i < allProjects.length; i++)
        {
            if (allProjects[i].getLookup().lookup(
                UMLActionProvider.class) == null)
                // &&
                // allProjects[i].getLookup().lookup(
                //        J2eeModuleContainer.class) == null)
            {
                LogicalViewProvider lvp =
                    (LogicalViewProvider)allProjects[i].getLookup()
                    .lookup(LogicalViewProvider.class);
                
                Node mn = lvp.createLogicalView();
                Node n = new FilterNode(mn, new FilterNode.Children(mn),
                    Lookups.singleton(allProjects[i]));
                
                moduleProjectNodes.add(n);
            }
        }
        
        Children.Array children = new Children.Array();
        children.add((Node[])moduleProjectNodes.toArray(
            new Node[moduleProjectNodes.size()]));
        
        final Node root = new AbstractNode(children);
        String moduleSelector = NbBundle.getMessage(
            AddJavaProjectAction.class,
            "LBL_JavaProjectSelectorTitle"); // NOI18N
        
        NodeAcceptor na = new NodeAcceptor()
        {
            public boolean acceptNodes(Node[] nodes)
            {
                for (int i = 0; i < nodes.length; i++)
                {
                    if (nodes[i].getParentNode() != root)
                        return false;
                    
                    // do not put this test befor the root test...
                    Project p = (Project) nodes[i].getLookup()
                    .lookup(Project.class);
                    
                    if (null == p)
                        return false;
                }
                
                return nodes.length > 0;
            }
        };
        
        // TODO - modify to limit selection to 1
        root.setDisplayName(NbBundle.getMessage(
            AddJavaProjectAction.class, "LBL_OpenProjects")); // NOI18N
        
        Node[] selected = NodeOperation.getDefault().select(
            moduleSelector, root.getDisplayName(), root, na);
        
        Project[] modules = new Project[selected.length];
        
        for (int i = 0; i < modules.length; i++)
        {
            modules[i] = (Project) selected[i]
                .getLookup().lookup(Project.class);
        }
        
        return modules[0];
        
    }
    
    
    public Project[] getSelectedUMLProjects(UMLProjectProperties epp)
    throws UserCancelException
    {
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        List moduleProjectNodes = new LinkedList();
        
        // Filter out the inappropriate target projects
        // include non uml projects -  == UMLActionProvider
        // eliminate current project -  ?? TODO
        for (int i = 0; i < allProjects.length; i++)
        {
            if (allProjects[i].getLookup().lookup(
                UMLActionProvider.class) != null)
            {
                LogicalViewProvider lvp =
                    (LogicalViewProvider) allProjects[i].getLookup()
                    .lookup(LogicalViewProvider.class);
                
                Node mn = lvp.createLogicalView();
                Node n = new FilterNode(mn, new FilterNode.Children(mn),
                    Lookups.singleton(allProjects[i]));
                
                moduleProjectNodes.add(n);
            }
        }
        
        Children.Array children = new Children.Array();
        children.add((Node[])moduleProjectNodes.toArray(
            new Node[moduleProjectNodes.size()]));
        
        final Node root = new AbstractNode(children);
        String moduleSelector = NbBundle.getMessage(
            AddJavaProjectAction.class,
            "LBL_UMLProjectSelectorTitle"); // NOI18N
        
        // Now filter out those nodes which are already included in subproject list
        UMLProject parent = epp.getProject();
        SubprojectProvider spp = (SubprojectProvider)parent.getLookup()
        .lookup(SubprojectProvider.class);
        
        if (null != spp)
        {
            final Set s = spp.getSubprojects();
            
            NodeAcceptor na = new NodeAcceptor()
            {
                public boolean acceptNodes(Node[] nodes)
                {
                    for (int i = 0; i < nodes.length; i++)
                    {
                        if (nodes[i].getParentNode() != root)
                            return false;
                        
                        // do not put this test befor the root test...
                        Project p = (Project) nodes[i].getLookup()
                        .lookup(Project.class);
                        
                        if (null == p)
                            return false;
                        
                        if (s.contains(p))
                            return false;
                    }
                    
                    return nodes.length > 0;
                }
            };
            
            root.setDisplayName(NbBundle.getMessage(
                AddJavaProjectAction.class, "LBL_OpenProjects")); // NOI18N
            
            Node[] selected = NodeOperation.getDefault().select(
                moduleSelector, root.getDisplayName(), root, na);
            
            Project[] modules = new Project[selected.length];
            
            for (int i = 0; i < modules.length; i++)
            {
                modules[i] = (Project) selected[i]
                    .getLookup().lookup(Project.class);
            }
            
            return modules;
        }
        
        else
            return new Project[0];
    }    
}

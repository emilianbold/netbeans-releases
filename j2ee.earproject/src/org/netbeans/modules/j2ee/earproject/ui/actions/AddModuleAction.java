/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject.ui.actions;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModuleContainer;
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

import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.common.ui.customizer.VisualClassPathItem;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
//import org.netbeans.modules.j2ee.api.common.J2eeProjectConstants;
import java.util.Arrays;
import java.util.Iterator;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.api.project.FileOwnerQuery;

/**
 * Action that allows selection and assembly of J2EE module projects.
 * @author Chris Webster
 * @author vince kraemer
 */
public class AddModuleAction extends CookieAction {
    
    private static final Class[] COOKIE_ARRAY =
        new Class[] { AntProjectHelper.class };
    
    public Class[] cookieClasses() {
        return COOKIE_ARRAY;
    }
    
    public int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }
    
    public void performAction(Node[] activeNodes) {
        try {
            AntProjectHelper aph = 
                (AntProjectHelper) activeNodes[0].getLookup().lookup(AntProjectHelper.class);
            Project[] moduleProjects = getSelectedProjects(aph);
            // XXX Vince add code here to add to application.xml and
            // build script
            Project p = FileOwnerQuery.getOwner(aph.getProjectDirectory());
            EarProject ep = (EarProject) p.getLookup().lookup(EarProject.class);
            EarProjectProperties epp = (EarProjectProperties) ep.getProjectProperties();
            epp.addJ2eeSubprojects(moduleProjects);
//            List artifactList = new ArrayList();
//            for (int i = 0; i < moduleProjects.length; i++) {
//                AntArtifact artifacts[] = AntArtifactQuery.findArtifactsByType( 
//                    moduleProjects[i], 
//                    J2eeProjectConstants.ARTIFACT_TYPE_J2EE_ARCHIVE );
//                artifactList.addAll(Arrays.asList(artifacts));
//            }
//            // create the vcpis
//            List newVCPIs = new ArrayList();
//            Iterator iter = artifactList.iterator();
//            while (iter.hasNext()) {
//                AntArtifact art = (AntArtifact) iter.next();
//                VisualClassPathItem vcpi = VisualClassPathItem.create(art,VisualClassPathItem.PATH_IN_WAR_APPLET);
//                    //new VisualClassPathItem(art, VisualClassPathItem.TYPE_ARTIFACT, null, art.getArtifactLocation().toString(), VisualClassPathItem.PATH_IN_WAR_APPLET);
//                vcpi.setRaw(EarProjectProperties.JAR_CONTENT_ADDITIONAL);
//                newVCPIs.add(vcpi);
//            }
//            Object t = epp.get(EarProjectProperties.JAR_CONTENT_ADDITIONAL);
//            if (!(t instanceof List)) {
//                assert false : "jar content isn't a List???";
//                return;
//            }
//            List vcpis = (List) t;
//            newVCPIs.addAll(vcpis);
//            epp.put(EarProjectProperties.JAR_CONTENT_ADDITIONAL, newVCPIs);
//            //epp.updateApplicationXml();
//            epp.store();
//                try {
//                    org.netbeans.api.project.ProjectManager.getDefault().saveProject(epp.getProject());
//                }
//                catch ( java.io.IOException ex ) {
//                    org.openide.ErrorManager.getDefault().notify( ex );
//                }
//            epp.configurationXmlChanged(null);
        } catch (UserCancelException uce) {
            // this action has been cancelled
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(AddModuleAction.class, "LBL_AddModuleAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(AddModuleAction.class);
    }
    
    protected boolean asynchronous() {
        // performAction() should run in event thread
        return false;
    }
    
    private Project[] getSelectedProjects(AntProjectHelper epp) throws UserCancelException {
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        List moduleProjectNodes = new LinkedList();
        for (int i = 0; i < allProjects.length; i++) {
            if (allProjects[i].getLookup().lookup(J2eeModule.class) != null &&
                allProjects[i].getLookup().lookup(J2eeModuleContainer.class) == null) {
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
        String moduleSelector = NbBundle.getMessage(AddModuleAction.class, "LBL_ModuleSelectorTitle");
        
        Project parent = FileOwnerQuery.getOwner(epp.getProjectDirectory());
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
            root.setDisplayName(NbBundle.getMessage(AddModuleAction.class, "LBL_J2EEModules"));
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

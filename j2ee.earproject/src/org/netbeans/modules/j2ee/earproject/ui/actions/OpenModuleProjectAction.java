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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject.ui.actions;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.earproject.ui.ModuleNode;
import org.netbeans.modules.j2ee.earproject.ui.customizer.VisualClassPathItem;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public class OpenModuleProjectAction extends CookieAction {
    
    protected Class[] cookieClasses() {
        return new Class[] { ModuleNode.class };
    }
    
    protected int mode() {
        return CookieAction.MODE_ALL;
    }
    
    public void performAction(Node[] nodes) {
        Project projects[] = new Project[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            VisualClassPathItem vcpi = ((ModuleNode)nodes[i].getCookie(ModuleNode.class)).getVCPI();
            if (VisualClassPathItem.Type.ARTIFACT == vcpi.getType()) {
                projects[i] = ((AntArtifact) vcpi.getObject()).getProject();
            } else {
                continue;
            }
        }
        Set<Project> validProjects = new HashSet<Project>();
        for (int i = 0; i < nodes.length; i++) {
            if (ProjectManager.getDefault().isValid(projects[i])) {
                validProjects.add(projects[i]);
            } // XXX else make project broken?
        }
        if (!validProjects.isEmpty()) {
            OpenProjects.getDefault().open(projects,false);
        }
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    public String getName() {
        return NbBundle.getMessage(OpenModuleProjectAction.class, "LBL_OpenProject");
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
}

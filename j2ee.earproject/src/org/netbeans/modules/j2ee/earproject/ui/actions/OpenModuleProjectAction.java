/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject.ui.actions;

import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.common.ui.customizer.VisualClassPathItem;

import org.netbeans.modules.j2ee.earproject.ui.ModuleNode;

public class OpenModuleProjectAction extends org.openide.util.actions.CookieAction {
        OpenModuleProjectAction() {
            // why do I need this?
        }
        
        protected Class[] cookieClasses() {
            return new Class[] { ModuleNode.class };
        }
        
        protected int mode() {
            return org.openide.util.actions.CookieAction.MODE_ALL;
        }
        
        public void performAction(Node[] nodes) {
            Project args[] = new Project[nodes.length];
            for (int i = 0; i < nodes.length; i++) {
                VisualClassPathItem vcpi = ((ModuleNode)nodes[i].getCookie(ModuleNode.class)).getVCPI();
                if (vcpi.TYPE_ARTIFACT == vcpi.getType()) {
                    args[i] = ((AntArtifact) vcpi.getObject()).getProject();
                } else return;
            }
            org.netbeans.api.project.ui.OpenProjects.getDefault().open(args,false);
        }
        
        public org.openide.util.HelpCtx getHelpCtx() {
            return null;
        }
        
        public String getName() {
            return NbBundle.getMessage(ModuleNode.class, "LBL_OpenProject");
        }
        
        protected boolean asynchronous() {
            return false;
        }
    }

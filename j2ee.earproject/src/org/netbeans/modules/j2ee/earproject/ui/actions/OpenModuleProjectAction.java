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

import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.earproject.ui.customizer.VisualClassPathItem;

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

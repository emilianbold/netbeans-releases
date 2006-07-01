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

package org.netbeans.modules.subversion.ui.status;

import org.netbeans.api.project.*;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.subversion.ui.actions.*;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.util.Context;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Open the Versioning status view for all projects.
 *
 * @author Maros Sandor
 */
public class ShowAllChangesAction extends AbstractAllAction {

    public ShowAllChangesAction() {
    }

    public String getName() {
        return NbBundle.getMessage(ShowAllChangesAction.class, "CTL_MenuItem_ShowAllChanges_Label"); // NOI18N
    }


    public void actionPerformed(ActionEvent e) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                async();
            }
        });
    }

    private void async() {
        try {
            setEnabled(false);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    SvnVersioningTopComponent stc = SvnVersioningTopComponent.getInstance();
                    stc.setContext(null);
                    stc.open();
                }
            });

            Project [] projects = OpenProjects.getDefault().getOpenProjects();

            final Context ctx = SvnUtils.getProjectsContext(projects);
            final String title;
            if (projects.length == 1) {
                Project project = projects[0];
                ProjectInformation pinfo = ProjectUtils.getInformation(project);
                title = pinfo.getDisplayName();
            } else {
                title = NbBundle.getMessage(ShowAllChangesAction.class, "CTL_ShowAllChanges_WindowTitle", Integer.toString(projects.length)); // NOI18N
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    final SvnVersioningTopComponent stc = SvnVersioningTopComponent.getInstance();
                    stc.setContentTitle(title);
                    stc.setContext(ctx);
                    stc.open();
                    stc.requestActive();
                    if (shouldPostRefresh()) {
                        stc.performRefreshAction();
                    }
                }
            });

        } finally {
            setEnabled(true);
        }

    }

    protected boolean shouldPostRefresh() {
        return true;
    }
}


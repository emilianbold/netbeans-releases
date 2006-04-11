/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        return NbBundle.getMessage(ShowAllChangesAction.class, "CTL_MenuItem_ShowAllChanges_Label");
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
                title = NbBundle.getMessage(ShowAllChangesAction.class, "CTL_ShowAllChanges_WindowTitle", Integer.toString(projects.length));
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


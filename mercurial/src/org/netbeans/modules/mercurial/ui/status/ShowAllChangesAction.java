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

package org.netbeans.modules.mercurial.ui.status;

import org.netbeans.api.project.*;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Children.Array;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.util.actions.SystemAction;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.io.File;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Open the Versioning status view for all projects.
 *
 * @author Maros Sandor
 */
public class ShowAllChangesAction extends SystemAction {

    public ShowAllChangesAction() {
    }

    public String getName() {
        return NbBundle.getMessage(ShowAllChangesAction.class, "CTL_MenuItem_ShowAllChanges_Label"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
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
                    HgVersioningTopComponent stc = HgVersioningTopComponent.findInstance();
                    stc.setContext(null);
                    stc.open();
                }
            });

            Project [] projects = OpenProjects.getDefault().getOpenProjects();
            List<Node> allNodes = new ArrayList<Node>();
            for (int i = 0; i < projects.length; i++) {
                AbstractNode node = new AbstractNode(new Children.Array(), projects[i].getLookup());
                allNodes.add(node);
            }

            final VCSContext ctx = VCSContext.forNodes(allNodes.toArray(new Node[allNodes.size()]));
            Set<File> files = ctx.getRootFiles();
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
                    final HgVersioningTopComponent stc = HgVersioningTopComponent.findInstance();
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


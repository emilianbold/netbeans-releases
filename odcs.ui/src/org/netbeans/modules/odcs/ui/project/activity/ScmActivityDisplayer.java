/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.ui.project.activity;

import com.tasktop.c2c.server.profile.domain.activity.ScmActivity;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.ui.project.LinkLabel;
import org.netbeans.modules.odcs.ui.spi.VCSAccessor;
import org.netbeans.modules.odcs.ui.utils.Utils;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class ScmActivityDisplayer extends ActivityDisplayer {

    private ScmActivity activity;
    private final String scmUrl;
    private final ProjectHandle<ODCSProject> projectHandle;
    private Action openIDEAction;

    public ScmActivityDisplayer(ScmActivity activity, ProjectHandle<ODCSProject> projectHandle, String scmUrl, int maxWidth) {
        super(activity.getActivityDate(), maxWidth);
        this.activity = activity;
        this.projectHandle = projectHandle;
        this.scmUrl = scmUrl;
    }

    @Override
    public JComponent getTitleComponent() {
        JComponent titlePanel = super.getTitleComponent();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 0, 0);
        gbc.gridheight = GridBagConstraints.REMAINDER;
        titlePanel.add(new JLabel(NbBundle.getMessage(ScmActivityDisplayer.class, "LBL_Committed")), gbc); //NOI18N
        LinkLabel linkCommit = new LinkLabel(activity.getCommit().getMinimizedCommitId()) {
            @Override
            public void mouseClicked(MouseEvent e) {
                Action action = getOpenIDEAction();
                if (action == null || !action.isEnabled()) {
                    action = getOpenBrowserAction(getCommitUrl());
                }
                action.actionPerformed(new ActionEvent(ScmActivityDisplayer.this, ActionEvent.ACTION_PERFORMED, null));
            }
        };
        linkCommit.setPopupActions(getOpenIDEAction(), getOpenBrowserAction(getCommitUrl()));
        titlePanel.add(linkCommit, gbc);
        JLabel lblRepository = new JLabel(NbBundle.getMessage(ScmActivityDisplayer.class, "LBL_Repository", activity.getCommit().getRepository()));
        titlePanel.add(lblRepository, gbc);
        return titlePanel;
    }

    @Override
    public JComponent getShortDescriptionComponent() {
        JLabel lblCause = new JLabel("<html>" + activity.getCommit().getComment() + "</html>"); //NOI18N
        return lblCause;
    }

    @Override
    public JComponent getDetailsComponent() {
        return null;
    }

    @Override
    String getUserName() {
        return activity.getCommit().getAuthor().toFullName();
    }

    @Override
    public Icon getActivityIcon() {
        return ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_commit.png", true); //NOI18N
    }

    private String getCommitUrl() {
        String url = Utils.getRealUrl(scmUrl);
        if (!url.endsWith("/")) { //NOI18N
            url += "/"; //NOI18N
        }
        url += activity.getCommit().getRepository() + "/" + activity.getCommit().getCommitId(); //NOI18N
        return url;
    }

    private Action getOpenIDEAction() {
        if (openIDEAction == null) {
            openIDEAction = new OpenInIDEAction(NbBundle.getMessage(ScmActivityDisplayer.class, "LBL_OpenIDE"));
        }
        return openIDEAction;
    }

    private class OpenInIDEAction extends AbstractAction {

        private VCSAccessor accessor;

        public OpenInIDEAction(String name) {
            super(name);
            accessor = VCSAccessor.getDefault();
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (isEnabled()) {
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        Action action = accessor.getOpenHistoryAction(projectHandle,
                        activity.getCommit().getRepository(),
                        activity.getCommit().getCommitId());
                        final ActionListener fAction = action;
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (fAction != null) {
                                    fAction.actionPerformed(e);
                                }
                            }
                        });
                    }
                });
            }
        }

        @Override
        public boolean isEnabled() {
            return accessor != null;
        }
    }
}

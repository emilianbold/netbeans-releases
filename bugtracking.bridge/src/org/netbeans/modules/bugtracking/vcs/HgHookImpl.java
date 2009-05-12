/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.vcs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.vcs.VCSHooksConfig.Format;
import org.netbeans.modules.bugtracking.vcs.VCSHooksConfig.PushAction;
import org.netbeans.modules.mercurial.hooks.spi.HgHook;
import org.netbeans.modules.mercurial.hooks.spi.HgHookContext;
import org.netbeans.modules.mercurial.hooks.spi.HgHookContext.LogEntry;
import org.openide.util.NbBundle;

/**
 * Mercurial commit hook implementation
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.mercurial.hooks.spi.HgHook.class)
public class HgHookImpl extends HgHook {
    private HookPanel panel;
    private final String name;
    private static Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.vcshooks.HgHook");   // NOI18N

    public HgHookImpl() {
        this.name = NbBundle.getMessage(HgHookImpl.class, "LBL_VCSHook");       // NOI18N
    }

    @Override
    public HgHookContext beforeCommit(HgHookContext context) throws IOException {
        Repository selectedRepository = panel.getSelectedRepository();

        if(context.getFiles().length == 0) {

            if (selectedRepository != null) {
                BugtrackingOwnerSupport.getInstance().setLooseAssociation(
                        BugtrackingOwnerSupport.ContextType.MAIN_OR_SINGLE_PROJECT,
                        selectedRepository);
            }

            LOG.warning("calling hg beforeCommit for zero files");               // NOI18N
            return null;
        }

        if (selectedRepository != null) {
            BugtrackingOwnerSupport.getInstance().setFirmAssociations(
                    context.getFiles(),
                    selectedRepository);
        }

        File file = context.getFiles()[0];
        LOG.log(Level.FINE, "hg beforeCommit start for " + file);                // NOI18N

        if(panel.addIssueCheckBox.isSelected()) {
            String msg = context.getMessage();

            Format format = VCSHooksConfig.getInstance().getHgIssueFormat();
            String formatString = format.getFormat();
            formatString = formatString.replaceAll("\\{id\\}", "\\{0\\}");           // NOI18N
            formatString = formatString.replaceAll("\\{summary\\}", "\\{1\\}");    // NOI18N
            
            Issue issue = panel.getIssue();
            if (issue == null) {
                LOG.log(Level.FINE, " no issue set for " + file);                   // NOI18N
                return null;
            }
            String issueInfo = new MessageFormat(formatString).format(
                    new Object[] {issue.getID(), issue.getSummary()},
                    new StringBuffer(),
                    null).toString();

            LOG.log(Level.FINER, " svn commit hook issue info '" + issueInfo + "'");     // NOI18N
            if(format.isAbove()) {
                msg = issueInfo + "\n" + msg;                                   // NOI18N
            } else {
                msg = msg + "\n" + issueInfo;                                   // NOI18N
            }
            
            context = new HgHookContext(context.getFiles(), msg, context.getLogEntries());
            return context;
        }
        return super.beforeCommit(context);
    }

    @Override
    public void afterCommit(HgHookContext context) {
        if(context.getFiles().length == 0) {
            LOG.warning("calling hg afterCommit for zero files");               // NOI18N
            return;
        }

        File file = context.getFiles()[0];
        LOG.log(Level.FINE, "hg afterCommit start for " + file);                // NOI18N

        if(!panel.addCommentCheckBox.isSelected() &&
           !panel.addRevisionCheckBox.isSelected() &&
           !panel.resolveCheckBox.isSelected())
        {
            LOG.log(Level.FINER, " nothing to do in hg afterCommit for " + file);   // NOI18N
            return;
        }

        Issue issue = panel.getIssue();
        if (issue == null) {
            LOG.log(Level.FINE, " no issue set for " + file);                   // NOI18N
            return;
        }

        String msg = context.getMessage();
        if(!panel.addCommentCheckBox.isSelected() || msg == null || msg.trim().equals("")) { // NOI18N
            msg = null;
        }
        if(panel.addRevisionCheckBox.isSelected()) {
            String author = context.getLogEntries()[0].getAuthor();
            String changeset = context.getLogEntries()[0].getChangeset();
            Date date = context.getLogEntries()[0].getDate();
            String message = context.getLogEntries()[0].getMessage();

            String formatString = VCSHooksConfig.getInstance().getHgCommentFormat().getFormat();
            formatString = formatString.replaceAll("\\{changeset\\}", "\\{0\\}");           // NOI18N
            formatString = formatString.replaceAll("\\{author\\}",    "\\{1\\}");           // NOI18N
            formatString = formatString.replaceAll("\\{date\\}",      "\\{2\\}");           // NOI18N
            formatString = formatString.replaceAll("\\{message\\}",   "\\{3\\}");           // NOI18N

            msg = new MessageFormat(formatString).format(
                    new Object[] {changeset, author, date, message},
                    new StringBuffer(),
                    null).toString();

            LOG.log(Level.FINER, " hg afterCommit message '" + msg + "'");      // NOI18N
        }
        if(panel.commitRadioButton.isSelected()) {
            issue.addComment(msg, panel.resolveCheckBox.isSelected());
            issue.open();
        } else {
            VCSHooksConfig.getInstance().setHgPushAction(context.getLogEntries()[0].getChangeset(), new PushAction(issue.getID(), msg, panel.resolveCheckBox.isSelected()));
            LOG.log(Level.FINE, "schedulig issue  " + file);                    // NOI18N
        }
        LOG.log(Level.FINE, "hg afterCommit end for " + file);                  // NOI18N
    }

    @Override
    public HgHookContext beforePush(HgHookContext context) throws IOException {
        return super.beforePush(context);
    }

    @Override
    public void afterPush(HgHookContext context) {
        if(context.getFiles().length == 0) {
            LOG.warning("calling after push for zero files");                   // NOI18N
            return;
        }
        File file = context.getFiles()[0];
        LOG.log(Level.FINE, "push hook start for " + file);                     // NOI18N

        Repository repo = BugtrackingOwnerSupport.getInstance().getRepository(file, true);
        if(repo == null) {
            LOG.log(Level.FINE, " could not find issue tracker for " + file);      // NOI18N
            return;
        }
        LogEntry[] entries = context.getLogEntries();
        for (LogEntry logEntry : entries) {

            PushAction pa = VCSHooksConfig.getInstance().popHGPushAction(logEntry.getChangeset());
            if(pa == null) {
                LOG.log(Level.FINE, " no push hook scheduled for " + file);     // NOI18N
                continue;
            }

            Issue issue = repo.getIssue(pa.getIssueID());
            if(issue == null) {
                LOG.log(Level.FINE, " no issue found with id " + pa.getIssueID());  // NOI18N
                continue;
            }

            issue.addComment(pa.getMsg(), panel.resolveCheckBox.isSelected());
        }
        LOG.log(Level.FINE, "push hook end for " + file);                       // NOI18N
    }

    @Override
    public JPanel createComponent(HgHookContext context) {
        Repository[] repos = BugtrackingUtil.getKnownRepositories();
        if(context.getFiles().length == 0) {
            LOG.warning("creating hg hook component for zero files");           // NOI18N
            Repository repoToSelect
                    = BugtrackingOwnerSupport.getInstance()
                      .getRepository(BugtrackingOwnerSupport.ContextType.ALL_PROJECTS);
            panel = new HookPanel(repos, repoToSelect);
        } else {
            File file = context.getFiles()[0];
            Repository repoToSelect = BugtrackingOwnerSupport.getInstance().getRepository(file, false);
            if(repoToSelect == null) {
                LOG.log(Level.FINE, " could not find issue tracker for " + file);  // NOI18N
            }
            panel = new HookPanel(repos, repoToSelect);
        }
        panel.changeRevisionFormatButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onShowRevisionFormat();
            }
        });
        panel.changeIssueFormatButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onShowIssueFormat();
            }
        });
        return panel;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    private void onShowRevisionFormat() {
        FormatPanel p = new FormatPanel(VCSHooksConfig.getInstance().getHgCommentFormat());
        if(BugtrackingUtil.show(p, NbBundle.getMessage(HookPanel.class, "LBL_FormatTitle"), NbBundle.getMessage(HookPanel.class, "LBL_OK"))) {  // NOI18N
            VCSHooksConfig.getInstance().setHgCommentFormat(p.getFormat());
        }
    }

    private void onShowIssueFormat() {
        FormatPanel p = new FormatPanel(VCSHooksConfig.getInstance().getHgIssueFormat());
        if(BugtrackingUtil.show(p, NbBundle.getMessage(HookPanel.class, "LBL_FormatTitle"), NbBundle.getMessage(HookPanel.class, "LBL_OK"))) {  // NOI18N
            VCSHooksConfig.getInstance().setHgIssueFormat(p.getFormat());
        }
    }
}

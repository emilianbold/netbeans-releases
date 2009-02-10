/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.vcshooks;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.mercurial.hooks.spi.HgHook;
import org.netbeans.modules.mercurial.hooks.spi.HgHookContext;
import org.netbeans.modules.mercurial.hooks.spi.HgHookContext.LogEntry;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.mercurial.hooks.spi.HgHook.class)
public class HgHookImpl extends HgHook {
    private HookPanel panel;
    private final String name;
    private static Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.vcshooks.HgHook");
    private Map<File, Repository> fileToRepo = new HashMap<File, Repository>(10);
    private HookSupport support;

    public HgHookImpl() {
        this.name = NbBundle.getMessage(HgHookImpl.class, "LBL_VCSHook");
        support = HookSupport.getInstance();
    }

    @Override
    public HgHookContext beforeCommit(HgHookContext context) throws IOException {
        return super.beforeCommit(context);
    }

    @Override
    public void afterCommit(HgHookContext context) {
        if(context.getFiles().length == 0) {
            LOG.warning("calling hg afterCommit for zero files");
            return;
        }

        File file = context.getFiles()[0];
        LOG.log(Level.FINE, "hg afterCommit start for " + file);

        if(!panel.addCommentCheckBox.isSelected() &&
           !panel.addRevisionCheckBox.isSelected() &&
           !panel.resolveCheckBox.isSelected())
        {
            LOG.log(Level.FINER, " nothing to do in hg afterCommit for " + file);
            return;
        }

        Issue issue = panel.getIssue();
        if (issue == null) {
            LOG.log(Level.FINE, " no issue set for " + file);
            return;
        }
        
        Repository repo = support.getRepository(file, LOG);
        if(repo == null) {
            LOG.log(Level.FINE, " could not find repository for " + file);
            return;
        }

        String msg = context.getMessage();
        if(!panel.addCommentCheckBox.isSelected() || msg == null || msg.trim().equals("")) {
            msg = null;
        }
        if(panel.addRevisionCheckBox.isSelected()) {
            String author = context.getLogEntries()[0].getAuthor();
            String changeset = context.getLogEntries()[0].getChangeset();
            Date date = context.getLogEntries()[0].getDate();
            String message = context.getLogEntries()[0].getMessage();

            String formatString = VCSHooksConfig.getInstance().getSvnCommentFormat();
            formatString = formatString.replaceAll("\\{changeset\\}", "\\{0\\}");           // NOI18N
            formatString = formatString.replaceAll("\\{author\\}",    "\\{1\\}");           // NOI18N
            formatString = formatString.replaceAll("\\{date\\}",      "\\{2\\}");           // NOI18N
            formatString = formatString.replaceAll("\\{message\\}",   "\\{3\\}");           // NOI18N

            msg = new MessageFormat(formatString).format(
                    new Object[] {changeset, author, date, message},
                    new StringBuffer(),
                    null).toString();

            LOG.log(Level.FINER, " hg afterCommit message '" + msg + "'");
        }
        if(panel.commitRadioButton.isSelected()) {
            issue.addComment(msg, panel.resolveCheckBox.isSelected());
        } else {
            VCSHooksConfig.getInstance().setHgPushAction(context.getLogEntries()[0].getChangeset(), new PushAction(issue.getID(), msg, panel.resolveCheckBox.isSelected()));
            LOG.log(Level.FINE, "schedulig issue  " + file);
        }
        LOG.log(Level.FINE, "hg afterCommit end for " + file);
    }

    @Override
    public HgHookContext beforePush(HgHookContext context) throws IOException {
        return super.beforePush(context);
    }

    @Override
    public void afterPush(HgHookContext context) {
        if(context.getFiles().length == 0) {
            LOG.warning("calling after push for zero files");
            return;
        }
        File file = context.getFiles()[0];
        LOG.log(Level.FINE, "push hook start for " + file);

        Repository repo = support.getRepository(file, LOG);
        if(repo == null) {
            LOG.log(Level.FINE, " could not find repository for " + file);
            return;
        }
        LogEntry[] entries = context.getLogEntries();
        for (LogEntry logEntry : entries) {

            PushAction pa = VCSHooksConfig.getInstance().popHGPushAction(logEntry.getChangeset());
            if(pa == null) {
                LOG.log(Level.FINE, " no push hook scheduled for " + file);
                continue;
            }

            Issue issue = repo.getIssue(pa.getIssueID());
            if(issue == null) {
                LOG.log(Level.FINE, " no issue found with id " + pa.getIssueID());
                continue;
            }

            issue.addComment(pa.getMsg(), panel.resolveCheckBox.isSelected());
        }
        LOG.log(Level.FINE, "push hook end for " + file);
    }

    @Override
    public JPanel createComponent(HgHookContext context) {
        if(context.getFiles().length == 0) {
            LOG.warning("creating hg hook component for zero files");
            panel = new HookPanel(null);
        } else {
            File file = context.getFiles()[0];
            Repository repo = support.getRepository(file, LOG);
            if(repo == null) {
                LOG.log(Level.FINE, " could not find repository for " + file);
            }
            panel = new HookPanel(repo);
        }
        panel.changeFormatButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onShowFormat();
            }
        });
        return panel;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    private void onShowFormat() {
        FormatPanel p = new FormatPanel(VCSHooksConfig.getInstance().getHgCommentFormat());
        if(BugtrackingUtil.show(p, NbBundle.getMessage(HookPanel.class, "LBL_FormatTitle"), NbBundle.getMessage(HookPanel.class, "LBL_OK"))) {
            VCSHooksConfig.getInstance().setHgCommentFormat(p.getFormat());
        }
    }

    static class PushAction {
        private final String issueID;
        private final String msg;
        private final boolean close;
        public PushAction(String issueID, String msg, boolean close) {
            this.issueID = issueID;
            this.msg = msg;
            this.close = close;
        }
        public String getIssueID() {
            return issueID;
        }
        public boolean isClose() {
            return close;
        }
        public String getMsg() {
            return msg;
        }
    }
}

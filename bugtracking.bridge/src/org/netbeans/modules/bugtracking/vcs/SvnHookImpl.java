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

import org.netbeans.modules.bugtracking.util.RepositoryComboSupport;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.vcs.VCSHooksConfig.Format;
import org.netbeans.modules.versioning.hooks.SvnHook;
import org.netbeans.modules.versioning.hooks.SvnHookContext;
import org.netbeans.modules.versioning.hooks.SvnHookContext.LogEntry;
import org.openide.util.NbBundle;

/**
 * Subversion commit hook implementation
 * @author Tomas Stupka
 */
public class SvnHookImpl extends SvnHook {

    private static final String[] SUPPORT_ISSUE_INFO_VARIABLES = new String[] {"id", "summary"};                        // NOI18N
    private static final String[] SUPPORT_REVISION_VARIABLES = new String[] {"revision", "author", "date", "message"};  // NOI18N

    private HookPanel panel;
    private final String name;
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.vcshooks.SvnHook");  // NOI18N

    private static final SimpleDateFormat CC_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");    // NOI18N

    public SvnHookImpl() {
        this.name = NbBundle.getMessage(SvnHookImpl.class, "LBL_VCSHook");                              // NOI18N
    }

    @Override
    public SvnHookContext beforeCommit(SvnHookContext context) throws IOException {
        Repository selectedRepository = getSelectedRepository();

        if(context.getFiles().length == 0) {

            if (selectedRepository != null) {
                BugtrackingOwnerSupport.getInstance().setLooseAssociation(
                        BugtrackingOwnerSupport.ContextType.MAIN_OR_SINGLE_PROJECT,
                        selectedRepository);
            }

            LOG.warning("calling svn beforeCommit for zero files");               // NOI18N
            return null;
        }

        if (selectedRepository != null) {
            BugtrackingOwnerSupport.getInstance().setFirmAssociations(
                    context.getFiles(),
                    selectedRepository);
        }

        File file = context.getFiles()[0];
        LOG.log(Level.FINE, "svn beforeCommit start for {0}", file);                // NOI18N

        String msg = context.getMessage();
        List<LogEntry> logEntries = null;
        if(isLinkSelected()) {

            final Format format = VCSHooksConfig.getInstance().getSvnIssueInfoTemplate();
            String formatString = format.getFormat();
            formatString = HookUtils.prepareFormatString(formatString, SUPPORT_ISSUE_INFO_VARIABLES);

            Issue issue = getIssue();
            if (issue == null) {
                LOG.log(Level.FINE, " no issue set for {0}", file);                   // NOI18N
                return null;
            }
            String issueInfo = new MessageFormat(formatString).format(
                    new Object[] {issue.getID(), issue.getSummary()},
                    new StringBuffer(),
                    null).toString();

            LOG.log(Level.FINER, " svn commit hook issue info ''{0}''", issueInfo);     // NOI18N
            if(format.isAbove()) {
                msg = issueInfo + "\n" + msg;                                   // NOI18N
            } else {
                msg = msg + "\n" + issueInfo;                                   // NOI18N
            }
            logEntries = new ArrayList<LogEntry>();
        }
        return new SvnHookContext(context.getFiles(), msg, logEntries);
    }

    @Override
    public void afterCommit(SvnHookContext context) {
        if(panel == null) {
            LOG.fine("no settings for afterCommit");                                // NOI18N
            return;
        }

        if(context.getFiles().length == 0) {
            LOG.warning("calling svn afterCommit for zero files");              // NOI18N
            return;
        }

        File file = context.getFiles()[0];
        LOG.log(Level.FINE, "svn afterCommit start for {0}", file);               // NOI18N

        Issue issue = getIssue();
        if (issue == null) {
            LOG.log(Level.FINE, " no issue set for {0}", file);                   // NOI18N
            return;
        }

        VCSHooksConfig.getInstance().setSvnResolve(isResolveSelected());
        VCSHooksConfig.getInstance().setSvnLink(isLinkSelected());

        if(!isLinkSelected() &&
           !isResolveSelected())
        {
            LOG.log(Level.FINER, " nothing to do in svn afterCommit for {0}", file); // NOI18N
            return;
        }

        
        String msg = null;
        if(isLinkSelected()) {
            List<LogEntry> entries = context.getLogEntries();
            assert entries.size() > 0;
            LogEntry logEntry = entries.get(0);

            String author = logEntry.getAuthor();
            String revisions = getRevisions(entries);
            Date date = logEntry.getDate();
            String message = logEntry.getMessage();

            String formatString = VCSHooksConfig.getInstance().getSvnRevisionTemplate().getFormat();
            formatString = HookUtils.prepareFormatString(formatString, SUPPORT_REVISION_VARIABLES); // NOI18N

            msg = new MessageFormat(formatString).format(
                    new Object[] {
                        revisions,
                        author,
                        date != null ? CC_DATE_FORMAT.format(date) : "",        // NOI18N
                        message},
                    new StringBuffer(),
                    null).toString();
        }
        if(isLinkSelected() || isResolveSelected()) {
            LOG.log(Level.FINER, " svn commit hook message ''{0}'', resolved {1}", new Object[]{msg, isResolveSelected()});     // NOI18N
            issue.addComment(msg, isResolveSelected());
            issue.open();
        }
        
        LOG.log(Level.FINE, "svn commit hook end for {0}", file);                 // NOI18N
        VCSHooksConfig.logHookUsage("SVN", getSelectedRepository());            // NOI18N
    }

    @Override
    public JPanel createComponent(SvnHookContext context) {
        LOG.finer("SvnHookImpl.createComponent()");                             //NOI18N
        File referenceFile;
        if(context.getFiles().length == 0) {
            referenceFile = null;
            LOG.warning("creating svn hook component for zero files");          // NOI18N
        } else {
            referenceFile = context.getFiles()[0];
        }

        panel = new HookPanel(
                    VCSHooksConfig.getInstance().getSvnLink(),
                    VCSHooksConfig.getInstance().getSvnResolve(),
                    false);

        if (referenceFile != null) {
            RepositoryComboSupport.setup(panel, panel.repositoryComboBox, referenceFile);
        } else {
            RepositoryComboSupport.setup(panel, panel.repositoryComboBox, false);
        }
        panel.commitRadioButton.setVisible(false);
        panel.pushRadioButton.setVisible(false);
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

    private String getRevisions(List<LogEntry> entries) {
        StringBuffer sb = new StringBuffer();
        Iterator<LogEntry> it = entries.iterator();
        while(it.hasNext()) {
            LogEntry logEntry = it.next();
            sb.append(logEntry.getRevision());
            if(it.hasNext()) sb.append(", ");                                   // NOI18N
        }
        return sb.toString();
    }

    private void onShowFormat() {
        FormatPanel p =
                new FormatPanel(
                    VCSHooksConfig.getInstance().getSvnRevisionTemplate(),
                    VCSHooksConfig.getDefaultSvnRevisionTemplate(),
                    SUPPORT_REVISION_VARIABLES,
                    VCSHooksConfig.getInstance().getSvnIssueInfoTemplate(),
                    VCSHooksConfig.getDefaultIssueInfoTemplate(),
                    SUPPORT_ISSUE_INFO_VARIABLES);
        if(BugtrackingUtil.show(p, NbBundle.getMessage(HookPanel.class, "LBL_FormatTitle"), NbBundle.getMessage(HookPanel.class, "LBL_OK"))) {  // NOI18N
            VCSHooksConfig.getInstance().setSvnRevisionTemplate(p.getIssueFormat());
            VCSHooksConfig.getInstance().setSvnIssueInfoTemplate(p.getCommitFormat());
        }
    }

    private boolean isLinkSelected() {
        return (panel != null) && panel.linkCheckBox.isSelected();
    }

    private boolean isResolveSelected() {
        return (panel != null) && panel.resolveCheckBox.isSelected();
    }

    private Repository getSelectedRepository() {
        return (panel != null) ? panel.getSelectedRepository() : null;
    }

    private Issue getIssue() {
        return (panel != null) ? panel.getIssue() : null;
    }

}

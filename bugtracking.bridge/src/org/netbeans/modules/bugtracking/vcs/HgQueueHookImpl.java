/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.vcs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import javax.swing.JPanel;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.RepositoryComboSupport;
import org.netbeans.modules.bugtracking.vcs.VCSHooksConfig.Format;
import org.netbeans.modules.bugtracking.vcs.VCSHooksConfig.PushOperation;
import org.netbeans.modules.versioning.hooks.HgQueueHook;
import org.netbeans.modules.versioning.hooks.HgQueueHookContext;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * Mercurial queue hook implementation
 * @author Tomas Stupka
 */
public class HgQueueHookImpl extends HgQueueHook {

    private static final String[] SUPPORTED_ISSUE_INFO_VARIABLES = new String[] {"id", "summary"};                        // NOI18N
    private static final String[] SUPPORTED_REVISION_VARIABLES = new String[] {"changeset", "author", "date", "message"}; // NOI18N
    private static final SimpleDateFormat CC_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");// NOI18N

    private HookPanel panel;
    private final VCSQueueHooksConfig config;

    private final String name;
    private static final String HOOK_NAME = "HG"; //NOI18N
    private final VCSHooksConfig globalConfig;

    public HgQueueHookImpl() {
        name = NbBundle.getMessage(HgQueueHookImpl.class, "LBL_VCSHook"); //NOI18N
        globalConfig = VCSHooksConfig.getInstance(VCSHooksConfig.HookType.HG);
        config = VCSQueueHooksConfig.getInstance(VCSQueueHooksConfig.HookType.HG);
    }

    @Override
    public JPanel createComponent (HgQueueHookContext context) {
        return createComponent(context.getFiles());
    }

    @Override
    public String getDisplayName () {
        return name;
    }

    @Override
    public HgQueueHookContext beforePatchRefresh (HgQueueHookContext context) throws IOException {
        Repository selectedRepository = getSelectedRepository();
        File[] files = context.getFiles();
        if(files.length == 0) {

            if (selectedRepository != null) {
                BugtrackingOwnerSupport.getInstance().setLooseAssociation(
                        BugtrackingOwnerSupport.ContextType.MAIN_OR_SINGLE_PROJECT,
                        selectedRepository);
            }

            HookImpl.LOG.warning("calling beforePatchRefresh for zero files"); //NOI18N
            return null;
        }

        if (selectedRepository != null) {
            BugtrackingOwnerSupport.getInstance().setFirmAssociations(
                    files,
                    selectedRepository);
        }
        String msg = context.getMessage();
        File file = files[0];
        if (isLinkSelected()) {
            Format format = globalConfig.getIssueInfoTemplate();
            String formatString = format.getFormat();
            formatString = HookUtils.prepareFormatString(formatString, SUPPORTED_ISSUE_INFO_VARIABLES);
            
            Issue issue = getIssue();
            if (issue == null) {
                HookImpl.LOG.log(Level.FINE, " no issue set for {0}", file);             // NOI18N
                return null;
            }
            String issueInfo = new MessageFormat(formatString).format(
                    new Object[] {issue.getID(), issue.getSummary()},
                    new StringBuffer(),
                    null).toString();

            HookImpl.LOG.log(Level.FINER, " commit hook issue info ''{0}''", issueInfo); // NOI18N
            if(format.isAbove()) {
                msg = issueInfo + "\n" + msg;                                   // NOI18N
            } else {
                msg = msg + "\n" + issueInfo;                                   // NOI18N
            }                        
            return new HgQueueHookContext(context.getFiles(), msg, context.getPatchId());
        }
        return null;
    }

    @Override
    public void afterPatchRefresh (HgQueueHookContext context) {
        clearSettings(context.getPatchId());
        File[] files = context.getFiles();
        if (panel == null) {
            HookImpl.LOG.fine("no settings for afterPatchRefresh");                            // NOI18N
            return;
        }

        if(files.length == 0) {
            HookImpl.LOG.warning("calling afterPatchRefresh for zero files");               // NOI18N
            return;
        }
        String patchId = context.getPatchId();
        if (patchId == null || patchId.isEmpty()) {
            HookImpl.LOG.warning("calling afterPatchRefresh with no patchId");               // NOI18N
            return;
        }

        File file = files[0];
        HookImpl.LOG.log(Level.FINE, "afterPatchRefresh start for {0}", file);              // NOI18N

        Issue issue = getIssue();
        if (issue == null) {
            HookImpl.LOG.log(Level.FINE, " no issue set for {0}", file);                 // NOI18N
            return;
        }

        globalConfig.setLink(isLinkSelected());
        globalConfig.setResolve(isResolveSelected());
        config.setAfterRefresh(isCommitSelected());

        if ((isLinkSelected() || isResolveSelected())) {
            HookImpl.LOG.log(Level.FINER, " commit hook message will be set after qfinish");     // NOI18N            
            if (isCommitSelected()) {
                config.setFinishPatchAction(context.getPatchId(), new VCSQueueHooksConfig.FinishPatchOperation(issue.getID(),
                    globalConfig.getRevisionTemplate().getFormat(), isResolveSelected(), isLinkSelected(), false));
                HookImpl.LOG.log(Level.FINE, "scheduling issue {0} for file {1} after qfinish", new Object[] { issue.getID(), file }); // NOI18N
            } else {
                config.setFinishPatchAction(context.getPatchId(), new VCSQueueHooksConfig.FinishPatchOperation(issue.getID(),
                    globalConfig.getRevisionTemplate().getFormat(), isResolveSelected(), isLinkSelected(), true));
                HookImpl.LOG.log(Level.FINE, "scheduling push preparations for issue {0} for file {1} after qfinish", new Object[] { issue.getID(), file }); // NOI18N
            }
        } else {
            HookImpl.LOG.log(Level.FINER, " nothing to do in afterPatchRefresh for {0}", file);   // NOI18N
            return;
        }

        HookImpl.LOG.log(Level.FINE, "afterCommit end for {0}", file); // NOI18N
        VCSHooksConfig.logHookUsage(HOOK_NAME, getSelectedRepository()); // NOI18N
    }
    
    @Override
    public HgQueueHookContext beforePatchFinish (HgQueueHookContext context) throws IOException {
        return super.beforePatchFinish(context);
    }

    @Override
    public void afterPatchFinish (HgQueueHookContext context) {
        String patchId = context.getPatchId();
        if (patchId == null) {
            HookImpl.LOG.fine("no patchId in afterPatchFinish");                            // NOI18N
            return;
        }

        File[] files = context.getFiles();
        if(files.length == 0) {
            HookImpl.LOG.warning("calling afterPatchFinish for zero files");               // NOI18N
            return;
        }

        VCSQueueHooksConfig.FinishPatchOperation op = config.popFinishPatchAction(patchId, true);
        if (op == null || !(op.isAddInfo() || op.isClose())) {
            HookImpl.LOG.fine("no settings for afterPatchFinish");                            // NOI18N
            return;
        }
        File file = files[0];
        HookImpl.LOG.log(Level.FINE, "afterPatchFinish start for {0}", file);              // NOI18N

        Repository repository = BugtrackingOwnerSupport.getInstance().getRepository(file, op.getIssueID(), true);
        if (repository == null) {
            HookImpl.LOG.log(Level.FINE, " no issue repository for {0}:{1}", new Object[] { op.getIssueID(), file }); //NOI18N
            return;
        }
        Issue issue = repository.getIssue(op.getIssueID());
        if (issue == null) {
            HookImpl.LOG.log(Level.FINE, " no issue found for {0}", op.getIssueID());                 // NOI18N
            return;
        }

        String msg = null;
        String changeset = context.getLogEntries()[0].getChangeset();
        if (op.isAddInfo()) {
            String formatString = op.getMsg();
            formatString = HookUtils.prepareFormatString(formatString, SUPPORTED_REVISION_VARIABLES);
            Date date = context.getLogEntries()[0].getDate();
            msg = new MessageFormat(formatString).format(
                    new Object[] {
                        changeset,
                        context.getLogEntries()[0].getAuthor(),
                        date == null ? "" : CC_DATE_FORMAT.format(date),        // NOI18N
                        context.getLogEntries()[0].getMessage()},
                    new StringBuffer(),
                    null).toString();
            HookImpl.LOG.log(Level.FINER, " afterPatchFinish message ''{0}''", msg);       // NOI18N
        }
        
        HookImpl.LOG.log(Level.FINER, " commit hook message ''{0}'', resolved {1}", new Object[] { msg, op.isClose() }); //NOI18N
        if (op.isAfterPush()) {
            HookImpl.LOG.log(Level.FINER, " commit hook message will be set after push"); //NOI18N            
            globalConfig.setPushAction(changeset, new PushOperation(issue.getID(), msg, op.isClose()));
            HookImpl.LOG.log(Level.FINE, "schedulig issue {0} for file {1}", new Object[] { issue.getID(), file } ); //NOI18N
        } else {
            issue.addComment(msg, isResolveSelected());
            issue.open();
        }
        HookImpl.LOG.log(Level.FINE, "afterPatchFinish end for {0}", file);                // NOI18N
        VCSHooksConfig.logHookUsage(HOOK_NAME, getSelectedRepository());             // NOI18N
    }

    public HookPanel createComponent(File[] files) {
        return createComponent(files, null);
    }
    public HookPanel createComponent(File[] files, Boolean afterCommit) {
        HookImpl.LOG.finer("HookImpl.createComponent()");                              // NOI18N
        File referenceFile;
        if(files.length == 0) {
            referenceFile = null;
            HookImpl.LOG.warning("creating hook component for zero files");           // NOI18N
        } else {
            referenceFile = files[0];
        }
        
        panel = new HookPanel(
                        globalConfig.getLink(),
                        globalConfig.getResolve(),
                        afterCommit != null ? afterCommit : config.getAfterRefresh());
        panel.commitRadioButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(HgQueueHookImpl.class, "CTL_HgQueueHookImpl.commitRadioButton.ACSD")); //NOI18N
        Mnemonics.setLocalizedText(panel.commitRadioButton, NbBundle.getMessage(HgQueueHookImpl.class, "CTL_HgQueueHookImpl.commitRadioButton.text")); //NOI18N
        
        if (referenceFile != null) {
            RepositoryComboSupport.setup(panel, panel.repositoryComboBox, referenceFile);
        } else {
            RepositoryComboSupport.setup(panel, panel.repositoryComboBox, false);
        }
        panel.changeFormatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onShowFormat();
            }
        });
        return panel;
    }

    private void onShowFormat() {
        FormatPanel p = 
                new FormatPanel(
                    globalConfig.getRevisionTemplate(),
                    globalConfig.getDefaultRevisionTemplate(),
                    SUPPORTED_REVISION_VARIABLES,
                    globalConfig.getIssueInfoTemplate(),
                    globalConfig.getDefaultIssueInfoTemplate(),
                    SUPPORTED_ISSUE_INFO_VARIABLES);
        if(BugtrackingUtil.show(p, NbBundle.getMessage(HookPanel.class, "LBL_FormatTitle"), NbBundle.getMessage(HookPanel.class, "LBL_OK"))) {  // NOI18N
            globalConfig.setRevisionTemplate(p.getIssueFormat());
            globalConfig.setIssueInfoTemplate(p.getCommitFormat());
        }
    }

    private boolean isLinkSelected() {
        return (panel != null) && panel.linkCheckBox.isSelected();
    }

    private boolean isResolveSelected() {
        return (panel != null) && panel.resolveCheckBox.isSelected();
    }

    private boolean isCommitSelected() {
        return (panel != null) && panel.commitRadioButton.isSelected();
    }

    private Repository getSelectedRepository() {
        return (panel != null) ? panel.getSelectedRepository() : null;
    }

    private Issue getIssue() {
        return (panel != null) ? panel.getIssue() : null;
    }

    private void clearSettings (String patchId) {
        config.clearFinishPatchAction(patchId);
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.mercurial.ui.commit;

import java.awt.event.ActionEvent;
import java.io.IOException;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.HgFileNode;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.ui.status.StatusAction;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.netbeans.modules.versioning.hooks.HgHookContext;
import org.netbeans.modules.versioning.hooks.HgHook;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.versioning.diff.SaveBeforeClosingDiffConfirmation;
import org.netbeans.modules.versioning.diff.SaveBeforeCommitConfirmation;
import org.netbeans.modules.versioning.hooks.VCSHooks;
import org.netbeans.modules.versioning.util.IndexingBridge;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;

/**
 * Commit action for mercurial:
 * hg commit -  commit the specified files or all outstanding changes
 *
 * @author John Rice
 */
public class CommitAction extends ContextAction {

    static final String RECENT_COMMIT_MESSAGES = "recentCommitMessage"; // NOI18N

    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        Set<File> ctxFiles = context != null? context.getRootFiles(): null;
        if (!HgUtils.isFromHgRepository(context) || ctxFiles == null || ctxFiles.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Commit"; // NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        final File root = HgUtils.getRootFile(context);
        if (root == null) {
            OutputLogger logger = OutputLogger.getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
            logger.outputInRed( NbBundle.getMessage(CommitAction.class,"MSG_COMMIT_TITLE")); // NOI18N
            logger.outputInRed( NbBundle.getMessage(CommitAction.class,"MSG_COMMIT_TITLE_SEP")); // NOI18N
            logger.outputInRed(
                    NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_NOT_SUPPORTED_INVIEW_INFO")); // NOI18N
            logger.output(""); // NOI18N
            logger.closeLog();
            JOptionPane.showMessageDialog(null,
                    NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_NOT_SUPPORTED_INVIEW"),// NOI18N
                    NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_NOT_SUPPORTED_INVIEW_TITLE"),// NOI18N
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String contentTitle = Utils.getContextDisplayName(context);

        commit(contentTitle, context);
    }

    public static void commit(String contentTitle, final VCSContext ctx) {
        final File repository = HgUtils.getRootFile(ctx);
        if (repository == null) {
            return;
        }

        // show commit dialog
        final CommitPanel panel = new CommitPanel();
        final Collection<HgHook> hooks = VCSHooks.getInstance().getHooks(HgHook.class);

        panel.setHooks(hooks, new HgHookContext(ctx.getRootFiles().toArray( new File[ctx.getRootFiles().size()]), null, new HgHookContext.LogEntry[] {}));
        final CommitTable data = new CommitTable(panel.filesLabel, CommitTable.COMMIT_COLUMNS, new String[] {CommitTableModel.COLUMN_NAME_PATH });

        panel.setCommitTable(data);
        data.setCommitPanel(panel);

        final JButton commitButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(commitButton, org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Commit"));
        commitButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSN_Commit_Action_Commit"));
        commitButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSD_Commit_Action_Commit"));
        final JButton cancelButton = new JButton(org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Cancel")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Cancel"));
        cancelButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSN_Commit_Action_Cancel"));
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSD_Commit_Action_Cancel"));

        final DialogDescriptor dd = new DialogDescriptor(panel,
              org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_CommitDialog_Title", contentTitle), // NOI18N
              true,
              new Object[] {commitButton, cancelButton},
              commitButton,
              DialogDescriptor.DEFAULT_ALIGN,
              new HelpCtx(CommitAction.class),
              null);
        ActionListener al;
        dd.setButtonListener(al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dd.setClosingOptions(new Object[] {commitButton, cancelButton});
                SaveCookie[] saveCookies = panel.getSaveCookies();
                if (cancelButton == e.getSource()) {
                    if (saveCookies.length > 0 && !SaveBeforeClosingDiffConfirmation.allSaved(saveCookies)) {
                        dd.setClosingOptions(new Object[0]);
                    }
                    dd.setValue(cancelButton);
                } else if (commitButton == e.getSource()) {
                    if (saveCookies.length > 0 && !SaveBeforeCommitConfirmation.allSaved(saveCookies)) {
                        dd.setClosingOptions(new Object[0]);
                    } else if (!panel.canCommit()) {
                        dd.setClosingOptions(new Object[0]);
                    }
                    dd.setValue(commitButton);
                }
            }
        });
        computeNodes(data, panel, ctx, repository, cancelButton);
        commitButton.setEnabled(false);
        panel.addVersioningListener(new VersioningListener() {
            @Override
            public void versioningEvent(VersioningEvent event) {
                refreshCommitDialog(panel, data, commitButton);
            }
        });
        data.getTableModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                refreshCommitDialog(panel, data, commitButton);
            }
        });
        commitButton.setEnabled(containsCommitable(data));

        panel.putClientProperty("contentTitle", contentTitle);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

        dialog.addWindowListener(new DialogBoundsPreserver(HgModuleConfig.getDefault().getPreferences(), "hg.commit.dialog")); // NOI18N
        dialog.pack();
        dialog.setVisible(true);

        final String message = panel.getCommitMessage().trim();
        if (!message.isEmpty()) {
            HgModuleConfig.getDefault().setLastCommitMessage(message);
        }
        if (dd.getValue() == DialogDescriptor.CLOSED_OPTION) {
            al.actionPerformed(new ActionEvent(cancelButton, ActionEvent.ACTION_PERFORMED, null));
        } else if (dd.getValue() == commitButton) {
            final Map<HgFileNode, CommitOptions> commitFiles = data.getCommitFiles();
            final Map<File, Set<File>> rootFiles = HgUtils.sortUnderRepository(ctx, true);
            org.netbeans.modules.versioning.util.Utils.insert(HgModuleConfig.getDefault().getPreferences(), RECENT_COMMIT_MESSAGES, message.trim(), 20);
            RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
            HgProgressSupport support = new HgProgressSupport() {
                @Override
                public void perform() {
                    OutputLogger logger = getLogger();
                    performCommit(message, commitFiles, rootFiles, this, logger, hooks);
                }
            };
            support.start(rp, repository, org.openide.util.NbBundle.getMessage(CommitAction.class, "LBL_Commit_Progress")); // NOI18N
        }
    }

    private static void computeNodes(final CommitTable table, final CommitPanel panel, final VCSContext ctx, final File repository, JButton cancel) {
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        final HgProgressSupport support = new HgProgressSupport(NbBundle.getMessage(CommitAction.class, "Progress_Preparing_Commit"), cancel) {
            @Override
            public void perform() {
                try {
                    panel.progressPanel.setVisible(true);
                    // Ensure that cache is uptodate
                    StatusAction.executeStatus(ctx, this);

                    FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
                    File[] roots = ctx.getRootFiles().toArray(new File[ctx.getRootFiles().size()]);

                    File[][] split = Utils.splitFlatOthers(roots);
                    List<File> fileList = new ArrayList<File>();
                    for (int c = 0; c < split.length; c++) {
                        roots = split[c];
                        boolean recursive = c == 1;
                        if (recursive) {
                            Set<File> repositories = HgUtils.getRepositoryRoots(ctx);
                            File[] files = cache.listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);
                            for (int i = 0; i < files.length; i++) {
                                for(int r = 0; r < roots.length; r++) {
                                    if(repositories.contains(Mercurial.getInstance().getRepositoryRoot(files[i])) &&
                                       HgUtils.isParentOrEqual(roots[r], files[i]))
                                    {
                                        if(!fileList.contains(files[i])) {
                                            fileList.add(files[i]);
                                        }
                                    }
                                }
                            }
                        } else {
                            File[] files = HgUtils.flatten(roots, FileInformation.STATUS_LOCAL_CHANGE);
                            for (int i= 0; i<files.length; i++) {
                                if(!fileList.contains(files[i])) {
                                    fileList.add(files[i]);
                                }
                            }
                        }
                    }
                    if(fileList.isEmpty()) {
                        return;
                    }

                    ArrayList<HgFileNode> nodesList = new ArrayList<HgFileNode>(fileList.size());

                    for (Iterator<File> it = fileList.iterator(); it.hasNext();) {
                        File file = it.next();
                        HgFileNode node = new HgFileNode(file);
                        nodesList.add(node);
                    }
                    final HgFileNode[] nodes = nodesList.toArray(new HgFileNode[fileList.size()]);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            table.setNodes(nodes);
                        }
                    });
                } finally {
                    panel.progressPanel.setVisible(false);
                }
            }
        };
        panel.progressPanel.add(support.getProgressComponent());
        panel.progressPanel.setVisible(true);
        support.start(rp);
    }

    private static boolean containsCommitable(CommitTable data) {
        Map<HgFileNode, CommitOptions> map = data.getCommitFiles();
        for(CommitOptions co : map.values()) {
            if(co != CommitOptions.EXCLUDE) {
                return true;
            }
        }
        return false;
    }

    /**
     * User changed a commit action.
     *
     * @param panel
     * @param commit
     */
    private static void refreshCommitDialog(CommitPanel panel, CommitTable table, JButton commit) {
        assert EventQueue.isDispatchThread();
        ResourceBundle loc = NbBundle.getBundle(CommitAction.class);
        Map<HgFileNode, CommitOptions> files = table.getCommitFiles();
        Set<String> stickyTags = new HashSet<String>();
        boolean conflicts = false;

        boolean enabled = commit.isEnabled();

        for (HgFileNode fileNode : files.keySet()) {

            CommitOptions options = files.get(fileNode);
            if (options == CommitOptions.EXCLUDE) {
                continue;
            }
            //stickyTags.add(HgUtils.getCopy(fileNode.getFile()));
            int status = fileNode.getInformation().getStatus();
            if ((status & FileInformation.STATUS_REMOTE_CHANGE) != 0 || status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                enabled = false;
                String msg = (status == FileInformation.STATUS_VERSIONED_CONFLICT) ?
                        loc.getString("MSG_CommitForm_ErrorConflicts") : // NOI18N
                        loc.getString("MSG_CommitForm_ErrorRemoteChanges"); // NOI18N
                panel.setErrorLabel("<html><font color=\"#002080\">" + msg + "</font></html>");  // NOI18N
                conflicts = true;
            }
            //stickyTags.add(HgUtils.getCopy(fileNode.getFile()));

        }

        if (stickyTags.size() > 1) {
            table.setColumns(new String [] { CommitTableModel.COLUMN_NAME_COMMIT, CommitTableModel.COLUMN_NAME_NAME, CommitTableModel.COLUMN_NAME_BRANCH, CommitTableModel.COLUMN_NAME_STATUS,
                                                CommitTableModel.COLUMN_NAME_ACTION, CommitTableModel.COLUMN_NAME_PATH });
        } else {
            table.setColumns(new String [] { CommitTableModel.COLUMN_NAME_COMMIT, CommitTableModel.COLUMN_NAME_NAME, CommitTableModel.COLUMN_NAME_STATUS,
                                                CommitTableModel.COLUMN_NAME_ACTION, CommitTableModel.COLUMN_NAME_PATH });
        }

        String contentTitle = (String) panel.getClientProperty("contentTitle"); // NOI18N

        DialogDescriptor dd = (DialogDescriptor) panel.getClientProperty("DialogDescriptor"); // NOI18N
        String errorLabel;
        if (stickyTags.size() <= 1) {
            String stickyTag = stickyTags.isEmpty() ? null : (String) stickyTags.iterator().next();
            if (stickyTag == null) {
                dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title"), new Object [] { contentTitle })); // NOI18N
                errorLabel = ""; // NOI18N
            } else {
                dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title_Branch"), new Object [] { contentTitle, stickyTag })); // NOI18N
                String msg = MessageFormat.format(loc.getString("MSG_CommitForm_InfoBranch"), new Object [] { stickyTag }); // NOI18N
                errorLabel = "<html><font color=\"#002080\">" + msg + "</font></html>"; // NOI18N
            }
        } else {
            dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title_Branches"), new Object [] { contentTitle })); // NOI18N
            String msg = loc.getString("MSG_CommitForm_ErrorMultipleBranches"); // NOI18N
            errorLabel = "<html><font color=\"#CC0000\">" + msg + "</font></html>"; // NOI18N
        }
        if (!conflicts) {
            panel.setErrorLabel(errorLabel);
            enabled = true;
        }
        commit.setEnabled(enabled && containsCommitable(table));
    }

    public static void performCommit(String message, Map<HgFileNode, CommitOptions> commitFiles,
            Map<File, Set<File>> rootFiles, HgProgressSupport support, OutputLogger logger, Collection<HgHook> hooks) {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        Map<File, List<File>> addCandidates = new HashMap<File, List<File>>();
        Map<File, List<File>> deleteCandidates = new HashMap<File, List<File>>();
        Map<File, List<File>> commitCandidates = new HashMap<File, List<File>>();
        Iterator<HgFileNode> it = commitFiles.keySet().iterator();

        List<String> excPaths = new ArrayList<String>();
        Map<File, Boolean> locallyModifiedExcluded = new HashMap<File, Boolean>();
        List<String> incPaths = new ArrayList<String>();
        while (it.hasNext()) {
             if (support.isCanceled()) {
                 return;
             }
             HgFileNode node = it.next();
             CommitOptions option = commitFiles.get(node);
             File repository = Mercurial.getInstance().getRepositoryRoot(node.getFile());
             if (option != CommitOptions.EXCLUDE) {
                 int  status = cache.getStatus(node.getFile()).getStatus();
                 if ((status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) != 0) {
                     putCandidate(addCandidates, repository, node.getFile());
                 } else  if ((status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY) != 0) {
                     putCandidate(deleteCandidates, repository, node.getFile());
                 }
                 putCandidate(commitCandidates, repository, node.getFile());
                 incPaths.add(node.getFile().getAbsolutePath());
             }else{
                 excPaths.add(node.getFile().getAbsolutePath());
                 if (!Boolean.TRUE.equals(locallyModifiedExcluded.get(repository))) {
                     int status = cache.getCachedStatus(node.getFile()).getStatus();
                     locallyModifiedExcluded.put(repository, (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) == 0); // not interested in excluded locally new files
                 }
             }
        }
        if (support.isCanceled()) {
            return;
        }

        if (!excPaths.isEmpty()) {
            HgModuleConfig.getDefault().addExclusionPaths(excPaths);
        }
        if (!incPaths.isEmpty()) {
            HgModuleConfig.getDefault().removeExclusionPaths(incPaths);
        }

        try {
            logger.outputInRed(
                    NbBundle.getMessage(CommitAction.class,
                    "MSG_COMMIT_TITLE")); // NOI18N
            logger.outputInRed(
                    NbBundle.getMessage(CommitAction.class,
                    "MSG_COMMIT_TITLE_SEP")); // NOI18N
            logger.output(message); // NOI18N

            new Cmd.AddCmd(addCandidates, logger, null, "hg add {0} into {1}").handle();
            new Cmd.RemoveCmd(deleteCandidates, logger, null, "hg delete {0} from {1}").handle();

            File[] hookFiles = null;
            if(hooks.size() > 0) {
                List<File> candidates = new LinkedList<File>();
                for (List<File> values : commitCandidates.values()) {
                    candidates.addAll(values);
                }
                hookFiles = candidates.toArray(new File[candidates.size()]);
            }
            HgHookContext context = new HgHookContext(hookFiles, message, new HgHookContext.LogEntry[] {});
            for (HgHook hook : hooks) {
                try {
                    // XXX handle returned context
                    context = hook.beforeCommit(context);
                    if(context != null) {
                        message = context.getMessage();
                    }
                } catch (IOException ex) {
                    // XXX handle veto
                }
            }
            final Cmd.CommitCmd commitCmd = new Cmd.CommitCmd(commitCandidates, logger, message, support, rootFiles,
                    locallyModifiedExcluded);
            commitCmd.setCommitHooks(context, hooks, hookFiles);
            IndexingBridge.getInstance().runWithoutIndexing(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    commitCmd.handle();
                    return null;
                }
            }, commitCandidates.keySet().toArray(new File[commitCandidates.keySet().size()]));
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        } catch (Exception ex) {
            Mercurial.LOG.log(Level.INFO, "Cannot run commit with disabled indexing", ex); //NOI18N
        } finally {
            cache.refreshAllRoots(rootFiles);
            logger.outputInRed(NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_DONE")); // NOI18N
            logger.output(""); // NOI18N
        }
    }

    private static void putCandidate(Map<File, List<File>> m, File repository, File file) {
        List<File> l = m.get(repository);
        if(l == null) {
            l = new ArrayList<File>();
            m.put(repository, l);
        }
        l.add(file);
    }

    private static boolean commitAfterMerge (boolean locallyModifiedExcluded, File repository) {
        // XXX consider usage of repository to determine if there are any non-included files which have to be committed, too
        // and thus removing the option HgModuleConfig.getDefault().getConfirmCommitAfterMerge()
        if (locallyModifiedExcluded || HgModuleConfig.getDefault().getConfirmCommitAfterMerge()) { // ask before commit?
            NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_AFTER_MERGE_QUERY")); // NOI18N
            descriptor.setTitle(NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_AFTER_MERGE_TITLE")); // NOI18N
            descriptor.setMessageType(JOptionPane.WARNING_MESSAGE);
            descriptor.setOptionType(NotifyDescriptor.YES_NO_OPTION);

            Object res = DialogDisplayer.getDefault().notify(descriptor);
            return res == NotifyDescriptor.YES_OPTION;
        }
        return true;
    }

    private static abstract class Cmd {
        protected final Map<File, List<File>> m;
        protected final OutputLogger logger;
        protected final String logMsgFormat;
        protected final String msg;
        public Cmd(Map<File, List<File>> m, OutputLogger logger, String msg, String logMsgFormat) {
            this.m = m;
            this.logger = logger;
            this.logMsgFormat = logMsgFormat;
            this.msg = msg;
        }
        void handle() throws HgException {
            if(m.isEmpty()) return;
            for (Entry<File, List<File>> e : m.entrySet()) {
                doCmd(e.getKey(), e.getValue());
                for (File f : e.getValue()) {
                    logger.output(MessageFormat.format(logMsgFormat, f.getName(), e.getKey()));
                }
            }
        }
        abstract void doCmd(File repository, List<File> candidates) throws HgException;
        static class AddCmd extends Cmd {
            public AddCmd(Map<File, List<File>> m, OutputLogger logger, String msgFormat, String msg) {
                super(m, logger, msgFormat, msg);
            }
            @Override
            void doCmd(File repository, List<File> candidates) throws HgException {
                HgCommand.doAdd(repository, candidates, logger);
            }
        }
        static class RemoveCmd extends Cmd {
            public RemoveCmd(Map<File, List<File>> m, OutputLogger logger, String msgFormat, String msg) {
                super(m, logger, msgFormat, msg);
            }
            @Override
            void doCmd(File repository, List<File> candidates) throws HgException {
                HgCommand.doRemove(repository, candidates, logger);
            }
        }
        static class CommitCmd extends Cmd {
            private HgHookContext context;
            private Collection<HgHook> hooks;
            private final HgProgressSupport support;
            private File[] hookFiles;
            private final Map<File, Set<File>> rootFilesPerRepository;
            private final Map<File, Boolean> locallyModifiedExcluded;

            public CommitCmd(Map<File, List<File>> m, OutputLogger logger, String commitMessage, HgProgressSupport support,
                    Map<File, Set<File>> rootFilesPerRepository, Map<File, Boolean> locallyModifiedExcluded) {
                super(m, logger, commitMessage, null);
                this.support = support;
                this.rootFilesPerRepository = rootFilesPerRepository;
                this.locallyModifiedExcluded = locallyModifiedExcluded;
            }

            public void setCommitHooks (HgHookContext context, Collection<HgHook> hooks, File[] hookFiles) {
                this.context = context;
                this.hooks = hooks;
                this.hookFiles = hookFiles;
            }

            @Override
            void handle() throws HgException {
                if(m.isEmpty()) return;
                for (Entry<File, List<File>> e : m.entrySet()) {
                    doCmd(e.getKey(), e.getValue());
                }
            }

            @Override
            void doCmd(File repository, List<File> candidates) throws HgException {
                boolean commitAfterMerge = false;
                try {
                    HgCommand.doCommit(repository, candidates, msg, logger);
                } catch (HgException.HgTooLongArgListException e) {
                    Mercurial.LOG.log(Level.INFO, null, e);
                    List<File> reducedCommitCandidates;
                    String offeredFileNames = "";                       //NOI18N
                    Set<File> roots = rootFilesPerRepository.get(repository);
                    if (roots != null && roots.size() < 5) {
                        reducedCommitCandidates = new ArrayList<File>(roots);
                        for (File f : reducedCommitCandidates) {
                            offeredFileNames += "\n" + f.getName();     //NOI18N
                        }
                    } else {
                        reducedCommitCandidates = Collections.EMPTY_LIST;
                        offeredFileNames = "\n" + repository.getName(); //NOI18N
                    }
                    NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(CommitAction.class, "MSG_LONG_COMMAND_QUERY", offeredFileNames)); //NOI18N
                    descriptor.setTitle(NbBundle.getMessage(CommitAction.class, "MSG_LONG_COMMAND_TITLE")); //NOI18N
                    descriptor.setMessageType(JOptionPane.WARNING_MESSAGE);
                    descriptor.setOptionType(NotifyDescriptor.YES_NO_OPTION);

                    Object res = DialogDisplayer.getDefault().notify(descriptor);
                    if (res == NotifyDescriptor.NO_OPTION) {
                        return;
                    }
                    Mercurial.LOG.log(Level.INFO, "CommitAction: committing with a reduced set of files: {0}", reducedCommitCandidates.toString()); //NOI18N
                    HgCommand.doCommit(repository, reducedCommitCandidates, msg, logger);
                } catch (HgException ex) {
                    if (HgCommand.COMMIT_AFTER_MERGE.equals(ex.getMessage())) {
                        // committing after a merge, all modified files have to be committed, even excluded files
                        // ask the user for confirmation
                        if (support.isCanceled()) {
                            return;
                        } else if(!commitAfterMerge(Boolean.TRUE.equals(locallyModifiedExcluded.get(repository)), repository)) {
                            return;
                        } else {
                            HgCommand.doCommit(repository, Collections.EMPTY_LIST, msg, logger);
                            commitAfterMerge = true;
                        }
                    } else {
                        throw ex;
                    }
                }

                HgLogMessage tip = HgCommand.doTip(repository, logger);

                context = new HgHookContext(hookFiles, msg, new HgHookContext.LogEntry(
                        tip.getMessage(),
                        tip.getAuthor(),
                        tip.getCSetShortID(),
                        tip.getDate()));
                for (HgHook hook : hooks) {
                    hook.afterCommit(context);
                }

                if (commitAfterMerge) {
                    logger.output(
                            NbBundle.getMessage(CommitAction.class,
                            "MSG_COMMITED_FILES_AFTER_MERGE"));         //NOI18N
                } else {
                    if (candidates.size() == 1) {
                        logger.output(
                                NbBundle.getMessage(CommitAction.class,
                                "MSG_COMMIT_INIT_SEP_ONE", candidates.size())); //NOI18N
                    } else {
                        logger.output(
                                NbBundle.getMessage(CommitAction.class,
                                "MSG_COMMIT_INIT_SEP", candidates.size())); //NOI18N
                    }
                    for (File f : candidates) {
                        logger.output("\t" + f.getAbsolutePath());      //NOI18N
                    }
                }
                HgUtils.logHgLog(tip, logger);
            }
        }
    }
}


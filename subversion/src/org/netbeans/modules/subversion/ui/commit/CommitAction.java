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

package org.netbeans.modules.subversion.ui.commit;

import java.io.IOException;
import java.text.ParseException;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.SVNBaseDir;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.hooks.spi.SvnHook;
import org.netbeans.modules.subversion.hooks.spi.SvnHookContext;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Commit action
 *
 * @author Petr Kuzel
 */
public class CommitAction extends ContextAction {

    static final String RECENT_COMMIT_MESSAGES = "recentCommitMessage";

    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Commit";    // NOI18N
    }

    @Override
    protected boolean enable(Node[] nodes) {
        if(isDeepRefresh()) {
            // allway true as we have will accept and check for external changes
            // and we don't about them yet
            return true;
        }
        // XXX could be a performace issue, maybe a msg box in commit would be enough
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File[] files = cache.listFiles(getCachedContext(nodes), FileInformation.STATUS_LOCAL_CHANGE);
        return files.length > 0;
    }

    /** Run commit action. Shows UI */
    public static void commit(String contentTitle, final Context ctx) {
        if(!Subversion.getInstance().checkClientAvailable()) {
            return;
        }
        commitChanges(contentTitle, ctx);
    }

    private static boolean isDeepRefresh() {
        String noDeepRefresh = System.getProperty("netbeans.subversion.commit.deepStatusRefresh");  // NOI18N
        return noDeepRefresh != null && !noDeepRefresh.trim().equals("");
    }

    /**
     * Opens the commit dialog displaying changed files from the status cache which belong to the given context.
     * If deepScan switch is enabled, the status for files will be refrehed first and the commit button in the dialog stays disabled until then
     * and it may take a while until the dialog is setup.
     *
     * @param contentTitle
     * @param ctx
     */
    public static void commitChanges(String contentTitle, final Context ctx) {
        final CommitPanel panel = new CommitPanel();
        List<SvnHook> hooks = Subversion.getInstance().getHooks();
        File file = ctx.getRootFiles()[0];
        panel.setHooks(hooks, new SvnHookContext(new File[] { file }, null, null));

        final CommitTable data = new CommitTable(panel.filesLabel, CommitTable.COMMIT_COLUMNS, new String[] { CommitTableModel.COLUMN_NAME_PATH });
        panel.setCommitTable(data);
        final JButton commitButton = new JButton();

        // start backround prepare
        SVNUrl repository = null;
        try {
            repository = getSvnUrl(ctx);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
        }
        SvnProgressSupport prepareSupport = getProgressSupport(ctx, data, commitButton);
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
        prepareSupport.start(rp, repository, org.openide.util.NbBundle.getMessage(CommitAction.class, "BK1009")); // NOI18N

        // show commit dialog
        if (showCommitDialog(panel, data, commitButton, contentTitle, ctx) == commitButton) {
            // if OK setup sequence of add, remove and commit calls
            startCommitTask(panel, data, ctx, hooks);
        } else {
            prepareSupport.cancel();
        }
    }

    private static Set<File> getUnversionedParents(List<File> fileList, boolean onlyCached) {
        Set<File> checked = new HashSet<File>();
        Set<File> ret = new HashSet<File>();
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        for (File file : fileList) {
            File parent = null;
            while((parent = file.getParentFile()) != null) {
                if (checked.contains(parent)) {
                    break;
                }
                checked.add(parent);
                if (fileList.contains(parent)) {
                    break;
                }
                if (!SvnUtils.isManaged(parent)) {
                    break;
                }
                FileInformation info = onlyCached ? cache.getCachedStatus(parent) : cache.getStatus(parent);
                if (info == null) {
                    continue;
                }
                if(info.getStatus() == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY ||
                   info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)
                {
                    ret.add(parent);
                }
            }
        }
        return ret;
    }

    /**
     * Returns a SvnFileNode for each given file
     *
     * @param fileList
     * @return
     */
    private static SvnFileNode[] getFileNodes(List<File> fileList) {
        SvnFileNode[] nodes;
        ArrayList<SvnFileNode> nodesList = new ArrayList<SvnFileNode>(fileList.size());

        for (Iterator<File> it = fileList.iterator(); it.hasNext();) {
            File file = it.next();
            SvnFileNode node = new SvnFileNode(file);
            // initialize node properties
            node.getRelativePath();
            node.getCopy();
            nodesList.add(node);
        }
        nodes = nodesList.toArray(new SvnFileNode[fileList.size()]);
        return nodes;
    }

    /**
     * Opens the commit dlg
     *
     * @param panel
     * @param data
     * @param commitButton
     * @param contentTitle
     * @param ctx
     * @return
     */
    private static Object showCommitDialog(final CommitPanel panel, final CommitTable data, final JButton commitButton, String contentTitle, final Context ctx) {
        org.openide.awt.Mnemonics.setLocalizedText(commitButton, org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Commit")); // NOI18N
        commitButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSN_Commit_Action_Commit")); // NOI18N
        commitButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSD_Commit_Action_Commit")); // NOI18N
        final JButton cancelButton = new JButton(org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Cancel")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSN_Commit_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSD_Commit_Action_Cancel")); // NOI18N
        cancelButton.setDefaultCapable(false);

        commitButton.setEnabled(false);

        DialogDescriptor dd = new DialogDescriptor(panel,
              org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_CommitDialog_Title", contentTitle), // NOI18N
              true,
              new Object[] {commitButton, cancelButton},
              commitButton,
              DialogDescriptor.DEFAULT_ALIGN,
              new HelpCtx(CommitAction.class),
              null);
        panel.addVersioningListener(new VersioningListener() {
            public void versioningEvent(VersioningEvent event) {
                refreshCommitDialog(panel, data, commitButton);
            }
        });
        data.getTableModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                refreshCommitDialog(panel, data, commitButton);
            }
        });
        commitButton.setEnabled(containsCommitable(data));

        panel.putClientProperty("contentTitle", contentTitle);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.addWindowListener(new DialogBoundsPreserver(SvnModuleConfig.getDefault().getPreferences(), "svn.commit.dialog")); // NOI18N
        dialog.pack();
        dialog.setVisible(true);

        return dd.getValue();
    }

    private static void startCommitTask(final CommitPanel panel, final CommitTable data, final Context ctx, final List<SvnHook> hooks) {
        final Map<SvnFileNode, CommitOptions> commitFiles = data.getCommitFiles();
        final String message = panel.getCommitMessage();
        org.netbeans.modules.versioning.util.Utils.insert(SvnModuleConfig.getDefault().getPreferences(), RECENT_COMMIT_MESSAGES, message, 20);

        SVNUrl repository = null;
        try {
            repository = getSvnUrl(ctx);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
        }
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
        SvnProgressSupport support = new SvnProgressSupport() {
            public void perform() {
                performCommit(message, commitFiles, ctx, this, hooks);
            }
        };
        support.start(rp, repository, org.openide.util.NbBundle.getMessage(CommitAction.class, "LBL_Commit_Progress")); // NOI18N
    }

    private static SvnProgressSupport getProgressSupport(final Context ctx, final CommitTable data, final JButton commitButton) {
        SvnProgressSupport support = new SvnProgressSupport() {
            public void perform() {
                try {
                    // get files without exclusions
                    File[] contextFiles = ctx.getFiles();
                    if (contextFiles.length == 0) {
                        return;
                    }

                    // The commits are made non recursively, so
                    // add also the roots to the to be commited list.
                    List<File> rootFiles = ctx.getRoots();
                    Set<File> filesSet = new HashSet<File>();
                    for(File file : contextFiles) {
                        filesSet.add(file);
                    }
                    for(File file : rootFiles) {
                        filesSet.add(file);
                    }
                    contextFiles = filesSet.toArray(new File[filesSet.size()]);

                    FileStatusCache cache = Subversion.getInstance().getStatusCache();
                    if (isDeepRefresh()) {
                        // make a deep refresh to get the not yet notified external changes
                        for (File f : contextFiles) {
                            cache.refreshRecursively(f);
                        }
                    }
                    // get all changed files while honoring the flat folder logic
                    File[][] split = Utils.splitFlatOthers(contextFiles);
                    List<File> fileList = new ArrayList<File>();
                    for (int c = 0; c < split.length; c++) {
                        contextFiles = split[c];
                        boolean recursive = c == 1;
                        if (recursive) {
                            File[] files = cache.listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);
                            for (int i= 0; i < files.length; i++) {
                                for(int r = 0; r < contextFiles.length; r++) {
                                    if( SvnUtils.isParentOrEqual(contextFiles[r], files[i]) ) {
                                        if(!fileList.contains(files[i])) {
                                            fileList.add(files[i]);
                                        }
                                    }
                                }
                            }
                        } else {
                            File[] files = SvnUtils.flatten(contextFiles, FileInformation.STATUS_LOCAL_CHANGE);
                            for (int i= 0; i<files.length; i++) {
                                if(!fileList.contains(files[i])) {
                                    fileList.add(files[i]);
                                }
                            }
                        }
                    }

                    if(fileList.size()==0) {
                        return;
                    }
                    fileList.addAll(getUnversionedParents(fileList, false));
                    SvnFileNode[] nodes = getFileNodes(fileList);
                    data.setNodes(nodes);
                } finally {
                    commitButton.setEnabled(containsCommitable(data));
                }
            }
        };
        return support;
    }

    private static boolean containsCommitable(CommitTable data) {
        Map<SvnFileNode, CommitOptions> map = data.getCommitFiles();
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
        ResourceBundle loc = NbBundle.getBundle(CommitAction.class);
        Map<SvnFileNode, CommitOptions> files = table.getCommitFiles();
        Set<String> stickyTags = new HashSet<String>();
        boolean conflicts = false;

        boolean enabled = commit.isEnabled();
        commit.setEnabled(false);

        for (SvnFileNode fileNode : files.keySet()) {
            CommitOptions options = files.get(fileNode);
            if (options == CommitOptions.EXCLUDE) {
                continue;
            }
            stickyTags.add(fileNode.getCopy());
            int status = fileNode.getInformation().getStatus();
            if ((status & FileInformation.STATUS_REMOTE_CHANGE) != 0 || status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                enabled = false;
                String msg = (status == FileInformation.STATUS_VERSIONED_CONFLICT) ?
                        loc.getString("MSG_CommitForm_ErrorConflicts") :
                        loc.getString("MSG_CommitForm_ErrorRemoteChanges");
                panel.setErrorLabel("<html><font color=\"#002080\">" + msg + "</font></html>");  // NOI18N
                conflicts = true;
            }
        }

        if (stickyTags.size() > 1) {
            table.setColumns(new String [] { CommitTableModel.COLUMN_NAME_NAME, CommitTableModel.COLUMN_NAME_BRANCH, CommitTableModel.COLUMN_NAME_STATUS,
                                                CommitTableModel.COLUMN_NAME_ACTION, CommitTableModel.COLUMN_NAME_PATH });
        } else {
            table.setColumns(new String [] { CommitTableModel.COLUMN_NAME_NAME, CommitTableModel.COLUMN_NAME_STATUS,
                                                CommitTableModel.COLUMN_NAME_ACTION, CommitTableModel.COLUMN_NAME_PATH });
        }

        String contentTitle = (String) panel.getClientProperty("contentTitle"); // NOI18N
        DialogDescriptor dd = (DialogDescriptor) panel.getClientProperty("DialogDescriptor"); // NOI18N
        String errorLabel;
        if (stickyTags.size() <= 1) {
            String stickyTag = stickyTags.size() == 0 ? null : (String) stickyTags.iterator().next();
            if (stickyTag == null) {
                dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title"), new Object [] { contentTitle }));
                errorLabel = ""; // NOI18N
            } else {
                dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title_Branch"), new Object [] { contentTitle, stickyTag }));
                String msg = MessageFormat.format(loc.getString("MSG_CommitForm_InfoBranch"), new Object [] { stickyTag });
                errorLabel = "<html><font color=\"#002080\">" + msg + "</font></html>"; // NOI18N
            }
        } else {
            dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title_Branches"), new Object [] { contentTitle }));
            String msg = loc.getString("MSG_CommitForm_ErrorMultipleBranches");
            errorLabel = "<html><font color=\"#CC0000\">" + msg + "</font></html>"; // NOI18N
        }
        if (!conflicts) {
            panel.setErrorLabel(errorLabel);
            enabled = true;
        }
        commit.setEnabled(enabled && containsCommitable(table));
    }

    protected void performContextAction(Node[] nodes) {
        if(!Subversion.getInstance().checkClientAvailable()) {
            return;
        }
        final Context ctx = getContext(nodes);
        commit(getContextDisplayName(nodes), ctx);
    }

    private static void performCommit(String message, Map<SvnFileNode, CommitOptions> commitFiles, Context ctx, SvnProgressSupport support, List<SvnHook> hooks) {
        SvnClient client = getClient(ctx, support);
        if(client == null) {
            return;
        }
        performCommit(client, message, commitFiles, ctx, support, false, hooks);
    }

    public static void performCommit(String message, Map<SvnFileNode, CommitOptions> commitFiles, Context ctx, SvnProgressSupport support, boolean rootUpdate) {
        SvnClient client = getClient(ctx, support);
        if(client == null) {
            return;
        }
        performCommit(client, message, commitFiles, ctx, support, rootUpdate, new ArrayList<SvnHook>(0));
    }

    public static void performCommit(SvnClient client, String message, Map<SvnFileNode, CommitOptions> commitFiles, Context ctx, SvnProgressSupport support, boolean rootUpdate, List<SvnHook> hooks) {
        try {
            support.setCancellableDelegate(client);
            support.setDisplayName(org.openide.util.NbBundle.getMessage(CommitAction.class, "LBL_Commit_Progress")); // NOI18N

            List<SvnFileNode> addCandidates = new ArrayList<SvnFileNode>();
            List<File> removeCandidates = new ArrayList<File>();
            Set<File> commitCandidates = new LinkedHashSet<File>();
            Set<File> binnaryCandidates = new HashSet<File>();

            Iterator<SvnFileNode> it = commitFiles.keySet().iterator();
            // XXX refactor the olowing loop. there seem to be redundant blocks
            while (it.hasNext()) {
                if(support.isCanceled()) {
                    return;
                }
                SvnFileNode node = it.next();
                CommitOptions option = commitFiles.get(node);
                if (CommitOptions.ADD_BINARY == option) {
                    List<File> l = listUnmanagedParents(node);
                    Iterator<File> dit = l.iterator();
                    while (dit.hasNext()) {
                        if(support.isCanceled()) {
                            return;
                        }
                        File file = dit.next();
                        addCandidates.add(new SvnFileNode(file));
                        commitCandidates.add(file);
                    }

                    if(support.isCanceled()) {
                        return;
                    }
                    binnaryCandidates.add(node.getFile());

                    addCandidates.add(node);
                    commitCandidates.add(node.getFile());
                } else if (CommitOptions.ADD_TEXT == option || CommitOptions.ADD_DIRECTORY == option) {
                    // assute no MIME property or startin gwith text
                    List<File> l = listUnmanagedParents(node);
                    Iterator<File> dit = l.iterator();
                    while (dit.hasNext()) {
                        if(support.isCanceled()) {
                            return;
                        }
                        File file = dit.next();
                        addCandidates.add(new SvnFileNode(file));
                        commitCandidates.add(file);
                    }
                    if(support.isCanceled()) {
                        return;
                    }
                    addCandidates.add(node);
                    commitCandidates.add(node.getFile());
                } else if (CommitOptions.COMMIT_REMOVE == option) {
                    removeCandidates.add(node.getFile());
                    commitCandidates.add(node.getFile());
                } else if (CommitOptions.COMMIT == option) {
                    commitCandidates.add(node.getFile());
                }
            }

            // perform adds
            performAdds(client, support, addCandidates);
            if(support.isCanceled()) {
                return;
            }

            // ensure all ignored properties are set.
            // This is more a hack than a clean solution but still seems to be
            // more reasonable than changing Subverion.isIgnored due to:
            // 1.) we didn't need it until now
            // 2.) the hilarious potential of Subverion.isIgnored and SQ to cause trouble ...
            setIgnoredProperties(client, support, addCandidates);
            if(support.isCanceled()) {
                return;
            }

            // TODO perform removes. especialy package removes where
            // metadata must be replied from SvnMetadata (hold by FileSyatemHandler)

            // set binary mimetype and group commitCandidates by managed trees
            List<List<File>> managedTrees = getManagedTrees(client, support, commitCandidates, binnaryCandidates);
            if(support.isCanceled()) {
                return;
            }

            List<ISVNLogMessage> logs = new ArrayList<ISVNLogMessage>();
            List<File> hookFiles = new ArrayList<File>();
            boolean needLogEntries = false;
            if(hooks.size() > 0) {
                for (List<File> l : managedTrees) {
                    hookFiles.addAll(l);
                }
                SvnHookContext context = new SvnHookContext(hookFiles.toArray(new File[hookFiles.size()]), message, null);
                for (SvnHook hook : hooks) {
                    try {
                        // XXX handle returned context
                        context = hook.beforeCommit(context);
                        if(context != null) {
                            needLogEntries = context.getLogEntries() != null;
                            message = context.getMessage();
                        }
                    } catch (IOException ex) {
                        // XXX handle veto
                    }
                }
            }
            // finally commit
            for (Iterator<List<File>> itCandidates = managedTrees.iterator(); itCandidates.hasNext();) {

                // one commit for each wc
                List<File> commitList = itCandidates.next();

                // handle recursive commits - deleted and copied folders can't be commited non recursively
                List<File> recursiveCommits = getRecursiveCommits(commitList, removeCandidates);
                if(recursiveCommits.size() > 0) {
                    // remove from the commits list all files which are supposed to be commited recursively
                    // or are children from recursively commited folders
                    commitList.removeAll(getAllChildren(recursiveCommits, commitList));

                    // commit recursively
                    File[] files = recursiveCommits.toArray(new File[recursiveCommits.size()]);
                    long revision = client.commit(files, message, true); // true = recursive
                    if(support.isCanceled()) {
                        return;
                    }
                    if(needLogEntries && hooks.size() > 0 && files.length > 0) {
                        addLogMessage(client, logs, files[0], revision);
                    }
                    if(support.isCanceled()) {
                        return;
                    }
                }

                // commit the remaining files non recursively
                if(commitList.size() > 0) {

                    File[] files = commitList.toArray(new File[commitList.size()]);
                    long revision = client.commit(files, message, false); // false = non recursive
                    if(support.isCanceled()) {
                        return;
                    }
                    if(needLogEntries && hooks.size() > 0 && files.length > 0) {
                        addLogMessage(client, logs, files[0], revision);
                    }
                    if(support.isCanceled()) {
                        return;
                    }
                }
                afterCommit(hooks, hookFiles, message, logs);

                // update and refresh
                FileStatusCache cache = Subversion.getInstance().getStatusCache();
                if(rootUpdate) {
                    File[] rootFiles = ctx.getRootFiles();
                    for (int i = 0; i < rootFiles.length; i++) {
                        client.update(rootFiles[i], SVNRevision.HEAD, false);
                    }
                    for (int i = 0; i < rootFiles.length; i++) {
                        cache.refresh(rootFiles[i], FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                    }
                }

                // XXX it's probably already catched by cache's onNotify()
                refreshFiles(cache, commitList);
                if(support.isCanceled()) {
                    return;
                }
                refreshFiles(cache, recursiveCommits);
                if(support.isCanceled()) {
                    return;
                }
            }

        } catch (SVNClientException ex) {
            support.annotate(ex);
        }
    }

    private static void afterCommit(List<SvnHook> hooks, List<File> files, String message, List<ISVNLogMessage> logs) {
        if(hooks.size() == 0) {
            return;
        }
        List<SvnHookContext.LogEntry> entries = new ArrayList<SvnHookContext.LogEntry>(logs.size());
        for (int i = 0; i < logs.size(); i++) {
            entries.add(new SvnHookContext.LogEntry(logs.get(i)));
        }
        SvnHookContext context = new SvnHookContext(files.toArray(new File[files.size()]), message, entries);
        for (SvnHook hook : hooks) {
            hook.afterCommit(context);
        }
    }

    /**
     * Adds a log message for given revision
     * @param client
     * @param logs
     * @param file
     * @param revision
     * @throws org.tigris.subversion.svnclientadapter.SVNClientException
     */
    private static void addLogMessage(ISVNClientAdapter client, List<ISVNLogMessage> logs, File file, long revision) throws SVNClientException {
        SVNRevision rev = SVNRevision.HEAD;
        try {
            rev = SVNRevision.getRevision(String.valueOf(revision));
        } catch (ParseException ex) {
            Subversion.LOG.log(Level.INFO, null, ex);
        }
        if (Subversion.LOG.isLoggable(Level.FINER)) {
            Subversion.LOG.log(Level.FINER, CommitAction.class.getName() + ": getting last commit message for svn hooks");
        }
        ISVNLogMessage[] ls = client.getLogMessages(SvnUtils.getRepositoryRootUrl(file), rev, rev);
        if (ls.length > 0) {
            logs.add(ls[0]);
        }
    }

    /**
     * Groups files by distinct working copies and sets the binary mimetypes
     */
    private static List<List<File>> getManagedTrees(SvnClient client, SvnProgressSupport support, Set<File> commitCandidates, Set<File> binnaryCandidates) throws SVNClientException {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        List<List<File>> managedTrees = new ArrayList<List<File>>();
        for (Iterator<File> itCommitCandidates = commitCandidates.iterator(); itCommitCandidates.hasNext();) {
            File commitCandidateFile = itCommitCandidates.next();

            // set MIME property application/octet-stream
            if(binnaryCandidates.contains(commitCandidateFile)) {
                ISVNProperty prop = client.propertyGet(commitCandidateFile, ISVNProperty.MIME_TYPE);
                if(prop != null) {
                    String s = prop.getValue();
                    if (s == null || s.startsWith("text/")) { // NOI18N
                        client.propertySet(commitCandidateFile, ISVNProperty.MIME_TYPE, "application/octet-stream", false); // NOI18N
                    }
                } else {
                     client.propertySet(commitCandidateFile, ISVNProperty.MIME_TYPE, "application/octet-stream", false); // NOI18N
                }
            }
            if(support.isCanceled()) {
                return null;
            }

            List<File> managedTreesList = null;
            for (Iterator<List<File>> itManagedTrees = managedTrees.iterator(); itManagedTrees.hasNext();) {
                List<File> list = itManagedTrees.next();
                File managedTreeFile = list.get(0);

                File base = SVNBaseDir.getRootDir(new File[] {commitCandidateFile, managedTreeFile});
                if(base != null) {
                    FileInformation status = cache.getStatus(base);
                    if ((status.getStatus() & FileInformation.STATUS_MANAGED) != 0) {
                        // found a list with files from the same working copy
                        managedTreesList = list;
                        break;
                    }
                }
                if(support.isCanceled()) {
                    return null;
                }
            }
            if(managedTreesList == null) {
                // no list for files from the same wc as commitCandidateFile created yet
                managedTreesList = new ArrayList<File>();
                managedTrees.add(managedTreesList);
            }
            managedTreesList.add(commitCandidateFile);
        }

        return managedTrees;
    }

    /**
     * Calls the svn add command on not yet added files
     */
    private static void performAdds(SvnClient client, SvnProgressSupport support, List<SvnFileNode> addCandidates) throws SVNClientException {
        List<File> addFiles = new ArrayList<File>();
        List<File> addDirs = new ArrayList<File>();
        // XXX waht if user denied directory add but wants to add a file in it?
        Iterator<SvnFileNode> it = addCandidates.iterator();
        while (it.hasNext()) {
            if(support.isCanceled()) {
                return;
            }
            SvnFileNode svnFileNode = it.next();
            File file = svnFileNode.getFile();
            if (file.isDirectory()) {
                addDirs.add(file);
            } else if (file.isFile()) {
                addFiles.add(file);
            }
        }
        if(support.isCanceled()) {
            return;
        }

        Iterator<File> itFiles = addDirs.iterator();
        List<File> dirsToAdd = new ArrayList<File>();
        while (itFiles.hasNext()) {
            File dir = itFiles.next();
            if (!dirsToAdd.contains(dir)) {
                dirsToAdd.add(dir);
            }
        }
        if(dirsToAdd.size() > 0) {
            for (File file : dirsToAdd) {
                client.addFile(file);
            }
        }
        if(support.isCanceled()) {
            return;
        }

        if(addFiles.size() > 0) {
            for (File file : addFiles) {
                client.addFile(file);
            }
        }
    }

    /**
     * In case a newly added file contains a ignored file, this mothod ensures the ignored property is also set.
     * Couldn't be done earlier as the file might have been unversioned (no svn add was invoked yet) until this moment.
     *
     * @param client
     * @param support
     * @param addCandidates
     */
    private static void setIgnoredProperties(SvnClient client, SvnProgressSupport support, List<SvnFileNode> addCandidates) {
        for (SvnFileNode fileNode : addCandidates) {
            File file = fileNode.getFile();
            if(file.isDirectory()) {
                File[] children = file.listFiles();
                if(children != null || children.length > 0) {
                    for (File child : children) {
                        final FileStatusCache cache = Subversion.getInstance().getStatusCache();
                        FileInformation info = cache.getStatus(child);
                        if(info.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                            File parent = child.getParentFile();
                            if ((cache.getStatus(parent).getStatus() & FileInformation.STATUS_VERSIONED) == 0) {
                                // ensure parents added status is set
                                cache.refresh(parent, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus();
                            }
                            cache.refresh(child, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns all files which have to be commited recursively (deleted and copied folders)
     */
    private static List<File> getRecursiveCommits(List<File> nonRecursiveComits, List<File> removeCandidates) {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        List<File> recursiveCommits = new ArrayList<File>();

        // 1. if there is at least one directory which isn't removed or copied
        //    we have to commit it nonrecursively ...
        boolean nonRecursiveDirs = false;
        for(File file : nonRecursiveComits) {
            ISVNStatus st = null;
            if( file.isDirectory() &&
                !( removeCandidates.contains(file) ||
                   ((st = cache.getStatus(file).getEntry(file)) != null && st.isCopied())))
            {
                nonRecursiveDirs = true;
                break;
            }
        }
        if(!nonRecursiveDirs) {
            // 2. ... otherwise we may commit all files recursivelly
            recursiveCommits.addAll(recursiveCommits);
            recursiveCommits.addAll(nonRecursiveComits);
        } else {
            // 3. ... well, this is the worst case. we have folders which were deleted or copied
            //        and such have to be commited recursively (svn restriction). On the other hand,
            //        there are also folders which have to be commited and doing it recursivelly
            //        could cause that the commit would also apply to files which because of exclusion or
            //        the (bloody) flat-folder loginc aren't supposed to be commited at all =>
            //        => the commit has to be split in two parts.
            for(File file : nonRecursiveComits) {
                ISVNStatus st = null;
                if(file.isDirectory() &&
                    ( removeCandidates.contains(file) ||
                      ((st = cache.getStatus(file).getEntry(file)) != null && st.isCopied())))
                {
                    recursiveCommits.add(file);
                }
            }
        }

        return recursiveCommits;
    }

    /**
     * Returns all files from the children list which have a parent in or are equal to a folder from the parents list
     */
    private static List<File> getAllChildren(List<File> parents, List<File> children) {
        List<File> ret = new ArrayList<File>();
        if(parents.size() > 0) {
            for(File child : children) {
                File parent = child;
                while(parent != null) {
                    if(parents.contains(parent)) {
                        ret.add(child);
                    }
                    parent = parent.getParentFile();
                }
            }
        }
        return ret;
    }

    private static void refreshFiles(FileStatusCache cache, List<File> files) {
        for (File file : files) {
            cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }
    }

    private static List<File> listUnmanagedParents(SvnFileNode node) {
        List<File> unmanaged = new ArrayList<File>();
        File file = node.getFile();
        File parent = file.getParentFile();
        while (true) {
            if (new File(parent, SvnUtils.SVN_ENTRIES_DIR).canRead()) { // NOI18N
                break;
            }
            unmanaged.add(0, parent);
            parent = parent.getParentFile();
            if (parent == null) {
                break;
            }
        }

        List<File> ret = new ArrayList<File>();
        Iterator<File> it = unmanaged.iterator();
        while (it.hasNext()) {
            File un = it.next();
            ret.add(un);
        }

        return ret;
    }

    private static SvnClient getClient(Context ctx, SvnProgressSupport support) {
        try {
            return Subversion.getInstance().getClient(ctx, support);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true); // should not hapen
            return null;
        }
    }
}

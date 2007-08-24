/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.mercurial.ui.commit;

import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.HgFileNode;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.ActionEvent;
import org.openide.nodes.Node;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.HashSet;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Commit action for mercurial: 
 * hg commit -  commit the specified files or all outstanding changes
 * 
 * @author John Rice
 */
public class CommitAction extends AbstractAction {
    
    static final String RECENT_COMMIT_MESSAGES = "recentCommitMessage"; // NOI18N

    private final VCSContext context;

    public CommitAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }

    public boolean isEnabled () {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        File[] files = cache.listFiles(context, FileInformation.STATUS_LOCAL_CHANGE);
        return files.length > 0;
    }

    public void actionPerformed(ActionEvent e) {
        String contentTitle = Utils.getContextDisplayName(context);

        commit(contentTitle, context);
    }

    public static void commit(String contentTitle, final VCSContext ctx) {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        File[] roots = ctx.getRootFiles().toArray(new File[ctx.getRootFiles().size()]);
        if (roots.length == 0) {
            return;
        }

        final File repository = HgUtils.getRootFile(ctx);
        if (repository == null) return;
        String projName = HgProjectUtils.getProjectName(repository);
        if (projName == null) {
            File projFile = HgUtils.getProjectFile(ctx);
            projName = HgProjectUtils.getProjectName(projFile);
        } 
        final String prjName = projName;

        File[][] split = Utils.splitFlatOthers(roots);
        List<File> fileList = new ArrayList<File>();
        for (int c = 0; c < split.length; c++) {
            roots = split[c];
            boolean recursive = c == 1;
            if (recursive) {
                File[] files = cache.listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);
                for (int i= 0; i < files.length; i++) {
                    for(int r = 0; r < roots.length; r++) {
                        if( HgUtils.isParentOrEqual(roots[r], files[i]) ) {
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
        
        if(fileList.size()==0) {
            return;
        }
        
        // show commit dialog
        final CommitPanel panel = new CommitPanel();
        final CommitTable data = new CommitTable(panel.filesLabel, CommitTable.COMMIT_COLUMNS, new String[] {CommitTableModel.COLUMN_NAME_PATH });
        
        panel.setCommitTable(data);
        
        HgFileNode[] nodes;
        ArrayList<HgFileNode> nodesList = new ArrayList<HgFileNode>(fileList.size());
        
        for (Iterator<File> it = fileList.iterator(); it.hasNext();) {
            File file = it.next();
            HgFileNode node = new HgFileNode(file);
            nodesList.add(node);
        }
        nodes = nodesList.toArray(new HgFileNode[fileList.size()]);
        data.setNodes(nodes);
        
        JComponent component = data.getComponent();
        panel.filesPanel.setLayout(new BorderLayout());
        panel.filesPanel.add(component, BorderLayout.CENTER);
        
        DialogDescriptor dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_CommitDialog_Title", contentTitle)); // NOI18N
        dd.setModal(true);
        final JButton commitButton = new JButton(org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Commit")); // NOI18N
        commitButton.setEnabled(false);
        dd.setOptions(new Object[] {commitButton, org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Cancel")}); // NOI18N
        dd.setHelpCtx(new HelpCtx(CommitAction.class));
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
        
        dialog.addWindowListener(new DialogBoundsPreserver(HgModuleConfig.getDefault().getPreferences(), "hg.commit.dialog")); // NOI18N
        dialog.pack();
        dialog.setVisible(true);
        
        if (dd.getValue() == commitButton) {
            
            final Map<HgFileNode, CommitOptions> commitFiles = data.getCommitFiles();
            final String message = panel.messageTextArea.getText();
            org.netbeans.modules.versioning.util.Utils.insert(HgModuleConfig.getDefault().getPreferences(), RECENT_COMMIT_MESSAGES, message, 20);
            RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository.getAbsolutePath());
            HgProgressSupport support = new HgProgressSupport() {
                public void perform() {
                    performCommit(message, commitFiles, ctx, this, prjName);
                }
            };
            support.start(rp, repository.getAbsolutePath(), org.openide.util.NbBundle.getMessage(CommitAction.class, "LBL_Commit_Progress")); // NOI18N
        }
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
        ResourceBundle loc = NbBundle.getBundle(CommitAction.class);
        Map<HgFileNode, CommitOptions> files = table.getCommitFiles();
        Set<String> stickyTags = new HashSet<String>();
        boolean conflicts = false;

        boolean enabled = commit.isEnabled();

        for (HgFileNode fileNode : files.keySet()) {

            CommitOptions options = files.get(fileNode);
            if (options == CommitOptions.EXCLUDE) continue;
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
            table.setColumns(new String [] { CommitTableModel.COLUMN_NAME_NAME, CommitTableModel.COLUMN_NAME_BRANCH, CommitTableModel.COLUMN_NAME_STATUS,
                                                CommitTableModel.COLUMN_NAME_ACTION, CommitTableModel.COLUMN_NAME_PATH });
        } else {
            table.setColumns(new String [] { CommitTableModel.COLUMN_NAME_NAME, CommitTableModel.COLUMN_NAME_STATUS,
                                                CommitTableModel.COLUMN_NAME_ACTION, CommitTableModel.COLUMN_NAME_PATH });
        }

        String contentTitle = (String) panel.getClientProperty("contentTitle"); // NOI18N
// NOI18N
        DialogDescriptor dd = (DialogDescriptor) panel.getClientProperty("DialogDescriptor"); // NOI18N
        String errorLabel;
        if (stickyTags.size() <= 1) {
            String stickyTag = stickyTags.size() == 0 ? null : (String) stickyTags.iterator().next();
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
    
    private static void performCommit(String message, Map<HgFileNode, CommitOptions> commitFiles, VCSContext ctx, HgProgressSupport support, String prjName) {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        final File repository = HgUtils.getRootFile(ctx);
        List<File> addCandidates = new ArrayList<File>();
        List<File> commitCandidates = new ArrayList<File>();
        Iterator<HgFileNode> it = commitFiles.keySet().iterator();

        while (it.hasNext()) {
             if (support.isCanceled()) {
                 return;
             }
             HgFileNode node = it.next();
             CommitOptions option = commitFiles.get(node);
             if (option != CommitOptions.EXCLUDE) {
                 if ((cache.getStatus(node.getFile()).getStatus() & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) != 0) {
                     addCandidates.add(node.getFile()); 
                 }
                 commitCandidates.add(node.getFile()); 
             }
        }
        if (support.isCanceled()) {
            return;
        }
        try {
            HgUtils.outputMercurialTabInRed(
                    NbBundle.getMessage(CommitAction.class,
                    "MSG_COMMIT_TITLE")); // NOI18N
            HgUtils.outputMercurialTabInRed(
                    NbBundle.getMessage(CommitAction.class,
                    "MSG_COMMIT_TITLE_SEP")); // NOI18N
            if (addCandidates.size() > 0 ) {
                HgCommand.doAdd(repository, addCandidates);
                for (File f : addCandidates) {
                    HgUtils.outputMercurialTab("hg add " + f.getName()); //NOI18N
                }
            }
            HgCommand.doCommit(repository, commitCandidates, message);

            if (commitCandidates.size() == 1) {
                HgUtils.outputMercurialTab(
                        NbBundle.getMessage(CommitAction.class,
                        "MSG_COMMIT_INIT_SEP_ONE", commitCandidates.size(), prjName)); // NOI18N
            } else {
                HgUtils.outputMercurialTab(
                        NbBundle.getMessage(CommitAction.class,
                        "MSG_COMMIT_INIT_SEP", commitCandidates.size(), prjName)); // NOI18N
            }
            for (File f : commitCandidates) {
                HgUtils.outputMercurialTab("\t" + f.getAbsolutePath()); // NOI18N
            }
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        }
        cache.refreshCached(ctx);
        HgUtils.outputMercurialTabInRed(NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_DONE")); // NOI18N
        HgUtils.outputMercurialTab(""); // NOI18N
   }
}


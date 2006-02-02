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

package org.netbeans.modules.subversion.ui.commit;

import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.tigris.subversion.svnclientadapter.SVNClientException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Commit action
 *
 * @author Petr Kuzel
 */
public class CommitAction extends ContextAction {

    protected String getBaseName() {
        return "CTL_MenuItem_Commit";    // NOI18N
    }

    protected void performContextAction(ActionEvent e) {
        Context ctx = getContext();

        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File[] files = cache.listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);

        if (files.length == 0) {
            return;
        }

        // show commit dialog
        CommitPanel panel = new CommitPanel();
        CommitTable data = new CommitTable(panel.filesLabel);
        SvnFileNode[] nodes;
        ArrayList nodesList = new ArrayList(files.length);

        for (int i = 0; i<files.length; i++) {
            File file = files[i];
            SvnFileNode node = new SvnFileNode(file);
            nodesList.add(node);
        }
        nodes = (SvnFileNode[]) nodesList.toArray(new SvnFileNode[files.length]);
        data.setNodes(nodes);

        JComponent component = data.getComponent();
        panel.filesPanel.setLayout(new BorderLayout());
        panel.filesPanel.add(component, BorderLayout.CENTER);

        DialogDescriptor dd = new DialogDescriptor(panel, "Commit");
        dd.setModal(true);
        JButton commitButton = new JButton("Commit");
        dd.setOptions(new Object[] {commitButton, "Cancel"});
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.show();

        if (dd.getValue() == commitButton) {
            Map commitFiles = data.getCommitFiles();
            String message = panel.messageTextArea.getText();
            performCommit(message, commitFiles);
        }

        // if OK setup sequence of add, remove and commit calls
    }

    private void performCommit(String message, Map commitFiles) {
        ProgressHandle progress = ProgressHandleFactory.createHandle("Committing...");
        try {
            progress.start();
            SvnClient client = Subversion.getInstance().getClient();

            List addCandidates = new ArrayList();
            List removeCandidates = new ArrayList();
            List commitCandidates = new ArrayList();

            Iterator it = commitFiles.keySet().iterator();
            while (it.hasNext()) {
                SvnFileNode node = (SvnFileNode) it.next();
                CommitOptions option = (CommitOptions) commitFiles.get(node);
                if (CommitOptions.ADD_BINARY == option) {
                    // set MIME property application/octet-stream
                    addCandidates.add(node);
                    commitCandidates.add(node.getFile());
                } else if (CommitOptions.ADD_TEXT == option) {
                    // assute no MIME property or startin gwith text
                    addCandidates.add(node);
                    commitCandidates.add(node.getFile());
                } else if (CommitOptions.COMMIT_REMOVE == option) {
                    removeCandidates.add(node);
                    commitCandidates.add(node.getFile());
                } else if (CommitOptions.COMMIT == option) {
                    commitCandidates.add(node.getFile());
                }
            }

            // perform adds

            List addFiles = new ArrayList();
            List addDirs = new ArrayList();
            // XXX waht if user denied directory add but wants to add a file in it?
            it = addCandidates.iterator();
            while (it.hasNext()) {
                SvnFileNode svnFileNode = (SvnFileNode) it.next();
                File file = svnFileNode.getFile();
                if (file.isDirectory()) {
                    addDirs.add(file);
                } else if (file.isFile()) {
                    addFiles.add(file);
                }
            }

            // XXX hiearchy sort
            it = addDirs.iterator();
            while (it.hasNext()) {
                File dir = (File) it.next();
                client.addDirectory(dir, false);
            }

            it = addFiles.iterator();
            while (it.hasNext()) {
                File file = (File) it.next();
                client.addFile(file);
            }

            // TODO perform removes

            // finally commit
            File[] files = (File[]) commitCandidates.toArray(new File[0]);
            client.commit(files, message, false);

            // XXX intercapt results and update cache

            FileStatusCache cache = Subversion.getInstance().getStatusCache();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            }
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ex);
        } finally {
            progress.finish();
        }
    }
}

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

import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.SVNClientException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Commit action
 *
 * @author Petr Kuzel
 */
public class CommitAction extends ContextAction {

    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Commit";    // NOI18N
    }

    /** Run commit action.  */
    public static void commit(final Context ctx, SvnProgressSupport support) {
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
        dialog.pack();
        dialog.setVisible(true);

        if (dd.getValue() == commitButton) {

            final Map commitFiles = data.getCommitFiles();
            final String message = panel.messageTextArea.getText();

            SVNUrl repository = getSvnUrl(ctx);
            RequestProcessor rp = Subversion.getInstance().getRequestProccessor(repository);
            support = new SvnProgressSupport(rp) {
                public void perform() {                    
                    performCommit(message, commitFiles, ctx, this);
                }
            };
            support.start("Comitting...");
        }

        // if OK setup sequence of add, remove and commit calls
        
    }
    
    protected void performContextAction(Node[] nodes) {
        final Context ctx = getContext(nodes);
        ProgressSupport support = new ContextAction.ProgressSupport(this, createRequestProcessor(nodes), nodes) {
            public void perform() {
                commit(ctx, this);
            }
        };
        support.start();
    }

    private static void performCommit(String message, Map commitFiles, Context ctx, SvnProgressSupport support) {        
        try {
                                               
            SvnClient client;
            try {
                client = Subversion.getInstance().getClient(ctx, support);
            } catch (SVNClientException ex) {
                ex.printStackTrace(); // should not hapen
                return;
            }                   
            support.setDisplayName("Committing...");

            List addCandidates = new ArrayList();
            List removeCandidates = new ArrayList();
            Set commitCandidates = new LinkedHashSet();

            // FIXME commit dirs with modified svn:ignore pooperty 

            Iterator it = commitFiles.keySet().iterator();
            while (it.hasNext()) {
                if(support.isCanceled()) {
                    return;
                }
                SvnFileNode node = (SvnFileNode) it.next();
                CommitOptions option = (CommitOptions) commitFiles.get(node);
                if (CommitOptions.ADD_BINARY == option) {
                    List l = listUnmanagedParents(node);  // FIXME coved scheduled but nor commited files!
                    Iterator dit = l.iterator();
                    while (dit.hasNext()) {
                        if(support.isCanceled()) {
                            return;
                        }
                        File file = (File) dit.next();
                        addCandidates.add(new SvnFileNode(file));
                        commitCandidates.add(file);
                    }

                    if(support.isCanceled()) {
                        return;
                    }

                    // set MIME property application/octet-stream
                    ISVNProperty prop = client.propertyGet(node.getFile(), ISVNProperty.MIME_TYPE);
                    String s = prop.getValue();
                    if (s == null || s.startsWith("text/")) {
                        client.propertySet(node.getFile(), ISVNProperty.MIME_TYPE, "application/octet-stream", false);
                    }

                    addCandidates.add(node);
                    commitCandidates.add(node.getFile());
                } else if (CommitOptions.ADD_TEXT == option) {
                    // assute no MIME property or startin gwith text
                    List l = listUnmanagedParents(node);
                    Iterator dit = l.iterator();
                    while (dit.hasNext()) {
                        if(support.isCanceled()) {
                            return;
                        }
                        File file = (File) dit.next();
                        addCandidates.add(new SvnFileNode(file));
                        commitCandidates.add(file);
                    }
                    if(support.isCanceled()) {
                        return;
                    }
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
                if(support.isCanceled()) {
                    return;
                }
                SvnFileNode svnFileNode = (SvnFileNode) it.next();
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

            it = addDirs.iterator();
            Set addedDirs = new HashSet();
            while (it.hasNext()) {
                if(support.isCanceled()) {
                    return;
                }
                File dir = (File) it.next();
                if (addedDirs.contains(dir)) {
                    continue;
                }
                client.addDirectory(dir, false);
                addedDirs.add(dir);
            }
            if(support.isCanceled()) {
                return;
            }

            it = addFiles.iterator();
            while (it.hasNext()) {
                if(support.isCanceled()) {
                    return;
                }
                File file = (File) it.next();
                client.addFile(file);
            }

            // TODO perform removes

            // finally commit
            File[] files = (File[]) commitCandidates.toArray(new File[0]);
            client.commit(files, message, false);
            if(support.isCanceled()) {
                return;
            }
            // XXX intercapt results and update cache

            FileStatusCache cache = Subversion.getInstance().getStatusCache();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            }
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ex);
        } 
    }

    private static List listUnmanagedParents(SvnFileNode node) {
        List unmanaged = new ArrayList();
        File file = node.getFile();
        File parent = file.getParentFile();
        while (true) {
            if (new File(parent, ".svn/entries").canRead() || new File(parent, "_svn/entries").canRead()) {
                break;
            }
            unmanaged.add(0, parent);
            parent = parent.getParentFile();
            if (parent == null) {
                break;
            }
        }

        List ret = new ArrayList();
        Iterator it = unmanaged.iterator();
        while (it.hasNext()) {
            File un = (File) it.next();
            ret.add(un);
        }

        return ret;
    }
}

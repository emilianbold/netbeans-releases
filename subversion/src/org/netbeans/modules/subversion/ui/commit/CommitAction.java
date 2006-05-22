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

import org.netbeans.modules.subversion.client.ExceptionHandler;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.SVNBaseDir;
import org.tigris.subversion.svnclientadapter.SVNClientException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
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

    protected boolean enable(Node[] nodes) {
        // XXX could be a performace issue, maybe a msg box in commit would be enough
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File[] files = cache.listFiles(getContext(nodes), FileInformation.STATUS_LOCAL_CHANGE);
        return files.length > 0;
    }

    /** Run commit action. Shows UI */
    public static void commit(final Context ctx) {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File[] roots = ctx.getFiles();
        if (roots.length == 0) {
            return;
        }
        
        File[][] split = SvnUtils.splitFlatOthers(roots);
        List<File> fileList = new ArrayList<File>();
        for (int c = 0; c < split.length; c++) {
            roots = split[c];
            boolean recursive = c == 1;
            if (recursive) {
                File[] files = cache.listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);
                for (int i= 0; i < files.length; i++) {
                    for(int r = 0; r < roots.length; r++) {
                        if( SvnUtils.isParentOrEqual(roots[r], files[i]) ) {
                            fileList.add(files[i]);
                        }                    
                    }                    
                }
            } else {
                File[] files = SvnUtils.flatten(roots, FileInformation.STATUS_LOCAL_CHANGE);
                for (int i= 0; i<files.length; i++) {
                    fileList.add(files[i]);
                }                
            }
        }       

        // show commit dialog
        CommitPanel panel = new CommitPanel();
        CommitTable data = new CommitTable(panel.filesLabel, CommitTable.COMMIT_COLUMNS);
        SvnFileNode[] nodes;
        ArrayList<SvnFileNode> nodesList = new ArrayList<SvnFileNode>(fileList.size());

        for (Iterator<File> it = fileList.iterator(); it.hasNext();) {
            File file = it.next();
            SvnFileNode node = new SvnFileNode(file);
            nodesList.add(node);
        }        
        nodes = nodesList.toArray(new SvnFileNode[fileList.size()]);
        data.setNodes(nodes);

        JComponent component = data.getComponent();
        panel.filesPanel.setLayout(new BorderLayout());
        panel.filesPanel.add(component, BorderLayout.CENTER);

        DialogDescriptor dd = new DialogDescriptor(panel, "Commit");
        dd.setModal(true);
        JButton commitButton = new JButton("Commit");
        dd.setOptions(new Object[] {commitButton, "Cancel"});
        dd.setHelpCtx(new HelpCtx(CommitAction.class));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.pack();
        dialog.setVisible(true);

        if (dd.getValue() == commitButton) {

            final Map<SvnFileNode, CommitOptions> commitFiles = data.getCommitFiles();
            final String message = panel.messageTextArea.getText();

            SVNUrl repository = getSvnUrl(ctx);
            RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
            SvnProgressSupport support = new SvnProgressSupport() {
                public void perform() {                    
                    performCommit(message, commitFiles, ctx, this);
                }
            };
            support.start(rp, "Comitting...");
        }

        // if OK setup sequence of add, remove and commit calls
        
    }
    
    protected void performContextAction(Node[] nodes) {
        final Context ctx = getContext(nodes);
        commit(ctx);
    }

    public static void performCommit(String message, Map<SvnFileNode, CommitOptions> commitFiles, Context ctx, SvnProgressSupport support) {
        try {
                                               
            SvnClient client;
            try {
                client = Subversion.getInstance().getClient(ctx, support);
            } catch (SVNClientException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not hapen
                return;
            }                   
            support.setDisplayName("Committing...");

            List<SvnFileNode> addCandidates = new ArrayList<SvnFileNode>();
            List<SvnFileNode> removeCandidates = new ArrayList<SvnFileNode>();
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
                    removeCandidates.add(node);
                    commitCandidates.add(node.getFile());
                } else if (CommitOptions.COMMIT == option) {
                    commitCandidates.add(node.getFile());
                }
            }

            // perform adds

            List<File> addFiles = new ArrayList<File>();
            List<File> addDirs = new ArrayList<File>();
            // XXX waht if user denied directory add but wants to add a file in it?
            it = addCandidates.iterator();
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
            Set<File> addedDirs = new HashSet<File>();
            while (itFiles.hasNext()) {
                if(support.isCanceled()) {
                    return;
                }
                File dir = itFiles.next();
                if (addedDirs.contains(dir)) {
                    continue;
                }
                client.addDirectory(dir, false);
                addedDirs.add(dir);
            }
            if(support.isCanceled()) {
                return;
            }

            itFiles = addFiles.iterator();
            while (itFiles.hasNext()) {
                if(support.isCanceled()) {
                    return;
                }
                File file = itFiles.next();
                client.addFile(file);
            }

            // TODO perform removes. especialy package removes where
            // metadata must be replied from SvnMetadata (hold by FileSyatemHandler)

            // set binary mimetype and group commitCandidates by managed trees
            FileStatusCache cache = Subversion.getInstance().getStatusCache();
            List<List<File>> managedTrees = new ArrayList<List<File>>();
            for (Iterator<File> itCommitCandidates = commitCandidates.iterator(); itCommitCandidates.hasNext();) {
                File commitCandidateFile = itCommitCandidates.next();
                
                // set MIME property application/octet-stream
                if(binnaryCandidates.contains(commitCandidateFile)) {
                    ISVNProperty prop = client.propertyGet(commitCandidateFile, ISVNProperty.MIME_TYPE);
                    if(prop != null) {
                        String s = prop.getValue();
                        if (s == null || s.startsWith("text/")) {
                            client.propertySet(commitCandidateFile, ISVNProperty.MIME_TYPE, "application/octet-stream", false);
                        }    
                    } else {
                         client.propertySet(commitCandidateFile, ISVNProperty.MIME_TYPE, "application/octet-stream", false);
                    }   
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
                }
                if(managedTreesList == null) {
                    // no list for files from the same wc as commitCandidateFile created yet
                    managedTreesList = new ArrayList<File>();
                    managedTrees.add(managedTreesList);
                }                
                managedTreesList.add(commitCandidateFile);                
            }

            // finally commit            
            for (Iterator<List<File>> itCandidates = managedTrees.iterator(); itCandidates.hasNext();) {
                // one commit for each wc
                List<File> list = itCandidates.next();
                File[] files = list.toArray(new File[0]);
                
                client.commit(files, message, false);
                // XXX it's probably already catched by cache's onNotify()
                for (int i = 0; i < files.length; i++) {
                    cache.refresh(files[i], FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                }
                if(support.isCanceled()) {
                    return;
                }
            }                        

        } catch (SVNClientException ex) {
            ExceptionHandler eh = new ExceptionHandler(ex);
            eh.annotate();
        } 
    }

    private static List<File> listUnmanagedParents(SvnFileNode node) {
        List<File> unmanaged = new ArrayList<File>();
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

        List<File> ret = new ArrayList<File>();
        Iterator<File> it = unmanaged.iterator();
        while (it.hasNext()) {
            File un = it.next();
            ret.add(un);
        }

        return ret;
    }
}

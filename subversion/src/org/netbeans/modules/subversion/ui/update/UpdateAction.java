/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.subversion.ui.update;

import java.util.Iterator;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VersioningOutputManager;
import org.openide.nodes.Node;
import org.openide.awt.StatusDisplayer;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.utils.SVNStatusUtils;

/**
 * Update action
 *
 * @author Petr Kuzel
 */ 
public class UpdateAction extends ContextAction {

    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Update";    // NOI18N
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_IN_REPOSITORY;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }
    
    protected void performContextAction(Node[] nodes) {        
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }        
        performUpdate(nodes);
    }

    private void performUpdate(final Node[] nodes) {
        // FIXME add shalow logic allowing to ignore nested projects
        // look into CVS, it's very tricky:
        // project1/
        //   nbbuild/  (project1)
        //   project2/
        //   src/ (project1)
        //   test/ (project1 but imagine it's in repository, to be updated )
        // Is there a way how to update project1 without updating project2?
        final Context ctx = getContext(nodes);
        ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, nodes) {
            public void perform() {
                update(ctx, this, getContextDisplayName(nodes));
            }
        };                    
        support.start(createRequestProcessor(nodes));
    }

    private static void update(Context ctx, SvnProgressSupport progress, String contextDisplayName) {
               
        File[] roots = ctx.getRootFiles();
        
        final SVNUrl repositoryUrl; 
        try {
            repositoryUrl = SvnUtils.getRepositoryRootUrl(roots[0]);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }        
        
        
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        cache.refreshCached(ctx);
        File[][] split = Utils.splitFlatOthers(roots);
        final List<File> recursiveFiles = new ArrayList<File>();
        final List<File> flatFiles = new ArrayList<File>();
        
        // recursive files
        for (int i = 0; i<split[1].length; i++) {
            recursiveFiles.add(split[1][i]);
        }        
        // flat files
        //File[] flatRoots = SvnUtils.flatten(split[0], getDirectoryEnabledStatus());
        for (int i= 0; i<split[0].length; i++) {            
            flatFiles.add(split[0][i]);
        }
                        
        
        SvnClient client;
        UpdateOutputListener listener = new UpdateOutputListener();
        try {
            client = Subversion.getInstance().getClient(repositoryUrl);            
            client.addNotifyListener(listener);            
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }

        try {                    
            updateRoots(recursiveFiles, progress, client, true);
            if(progress.isCanceled()) {                
                return;
            }
            updateRoots(flatFiles, progress, client, false);
        } catch (SVNClientException e1) {
            progress.annotate(e1);
        } finally {            
            openResults(listener.getResults(), repositoryUrl, contextDisplayName);                
        }
    }

    private static void openResults(final List<FileUpdateInfo> resultsList, final SVNUrl url, final String contextDisplayName) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                UpdateResults results = new UpdateResults(resultsList, url, contextDisplayName);
                VersioningOutputManager vom = VersioningOutputManager.getInstance();
                vom.addComponent(url.toString() + "-UpdateExecutor", results); // NOI18N
            }
        });
    }
    
    private static void updateRoots(List<File> roots, SvnProgressSupport support, SvnClient client, boolean recursive) throws SVNClientException {
        boolean conflict = false;
roots_loop:        
        for (Iterator<File> it = roots.iterator(); it.hasNext();) {
            File root = it.next();
            if(support.isCanceled()) {
                break;
            }
            client.update(root, SVNRevision.HEAD, recursive);
            ISVNStatus status[] = client.getStatus(root, true, false);
            for (int k = 0; k<status.length; k++) {
                ISVNStatus s = status[k];
                if (SVNStatusUtils.isTextConflicted(s) || SVNStatusUtils.isPropConflicted(s)) {
                    conflict = true;
                    break roots_loop;
                }
            }
        }
        if (conflict) {
            StatusDisplayer.getDefault().setStatusText(org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_Update_Conflicts")); // NOI18N
        } else {
            StatusDisplayer.getDefault().setStatusText(org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_Update_Completed")); // NOI18N
        }
        return;
    }

    public static void performUpdate(final Context context, final String contextDisplayName) {                
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }
        if (context == null || context.getRoots().size() == 0) {
            return;
        }        
        
        SVNUrl repository;
        try {
            repository = getSvnUrl(context);   
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }   
        
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
        SvnProgressSupport support = new SvnProgressSupport() {
            public void perform() {
                update(context, this, contextDisplayName);
            }
        };
        support.start(rp, repository, org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_Update_Progress")); // NOI18N
    }
    
    private static class UpdateOutputListener implements ISVNNotifyListener {

        private List<FileUpdateInfo> results;        
        
        public void setCommand(int command) {
        }

        public void logCommandLine(String str) {
        }

        public void logMessage(String logMsg) {
            FileUpdateInfo[] fuis = FileUpdateInfo.createFromLogMsg(logMsg);
            if(fuis != null) {
                for(FileUpdateInfo fui : fuis) {
                    if(fui != null) getResults().add(fui);
                }                
            }                 
        }

        public void logError(String str) {
        }

        public void logRevision(long rev, String str) {
        }

        public void logCompleted(String str) {
        }

        public void onNotify(File file, SVNNodeKind kind) {            
        }
        
        List<FileUpdateInfo> getResults() {
            if(results == null) {
                results = new ArrayList<FileUpdateInfo>();
            }
            return results;
        }
        
    };
    
}

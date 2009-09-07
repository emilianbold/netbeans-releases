/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.subversion.ui.update;

import java.util.Iterator;
import java.util.regex.Matcher;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VersioningOutputManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Update action
 *
 * @author Petr Kuzel
 */ 
public class UpdateAction extends ContextAction {

    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Update";    // NOI18N
    }

    @Override
    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_IN_REPOSITORY;
    }

    @Override
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
        
        SVNUrl repositoryUrl = null;
        try {
            for (File root : roots) {
                repositoryUrl = SvnUtils.getRepositoryRootUrl(root);
                if(repositoryUrl != null) {
                    break;
                } else {
                    Subversion.LOG.log(Level.WARNING, "Could not retrieve repository root for context file {0}", new Object[]{root});
                }
            }
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }        

        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        cache.refreshCached(ctx);
        update(roots, progress, contextDisplayName, repositoryUrl);
    }

    private static void update(File[] roots, SvnProgressSupport progress, String contextDisplayName, SVNUrl repositoryUrl) {
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
            // this isn't clean - the client notifies only files which realy were updated. 
            // The problem here is that the revision in the metadata is set to HEAD even if the file didn't change =>
            // we have to explicitly force the refresh for the relevant context - see bellow in updateRoots
            client.removeNotifyListener(Subversion.getInstance().getRefreshHandler());
            client.addNotifyListener(listener);
            progress.setCancellableDelegate(client);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }

        try {                    
            UpdateNotifyListener l = new UpdateNotifyListener();            
            client.addNotifyListener(l);            
            try {
                updateRoots(recursiveFiles, progress, client, true);
                if(progress.isCanceled()) {                
                    return;
                }
                updateRoots(flatFiles, progress, client, false);
            } finally {
                client.removeNotifyListener(l);
            }
            if (l.causedConflict) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(
                                org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_UpdateCausedConflicts_Prompt"), //NOI18N
                                NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                });
            } else {
                StatusDisplayer.getDefault().setStatusText(org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_Update_Completed")); // NOI18N
            }
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
        for (Iterator<File> it = roots.iterator(); it.hasNext();) {
            File root = it.next();
            if(support.isCanceled()) {
                break;
            }
            long rev = client.update(root, SVNRevision.HEAD, recursive);
            revisionUpdateWorkaround(recursive, root, client, rev);
        }
        return;
    }

    private static void revisionUpdateWorkaround(final boolean recursive, final File root, final SvnClient client, final long revision) throws SVNClientException {
        Utils.post(new Runnable() {
            public void run() {
                // this isn't clean - the client notifies only files which realy were updated.
                // The problem here is that the revision in the metadata is set to HEAD even if the file didn't change         
                List<File> filesToRefresh;
                if (recursive) {
                    filesToRefresh = SvnUtils.listRecursively(root);
                } else {
                    filesToRefresh = new ArrayList<File>();
                    filesToRefresh.add(root);
                    File[] files = root.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            filesToRefresh.add(file);
                        }
                    }
                }
                File[] fileArray = filesToRefresh.toArray(new File[filesToRefresh.size()]);

                SVNRevision.Number svnRevision = null;
                if(revision < -1) {
                    ISVNInfo info = null;
                    try {
                        info = client.getInfoFromWorkingCopy(root); // try to retrieve from local WC first
                        svnRevision = info.getRevision();
                        if(svnRevision == null) {
                            info = client.getInfo(root); // contacts the server
                            svnRevision = info.getRevision();
                        }
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(ex, true, true);
                    }
                } else {
                    svnRevision = new SVNRevision.Number(revision);
                }

                Subversion.getInstance().getStatusCache().patchRevision(fileArray, svnRevision);

                // the cache fires status change events to trigger the annotation refresh.
                // unfortunatelly, we have to call the refresh explicitly for each file from this place
                // as the revision label was changed even if the files status wasn't
                Subversion.getInstance().getStatusCache().getLabelsCache().flushFileLabels(fileArray);
                Subversion.getInstance().refreshAnnotationsAndSidebars(fileArray);
            }
        });
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

    /**
     * Run update on a single file
     * @param file
     */
    public static void performUpdate(final File file) {
        if(!Subversion.getInstance().checkClientAvailable()) {
            return;
        }
        if (file == null) {
            return;
        }

        SVNUrl repository;
        try {
            repository = SvnUtils.getRepositoryRootUrl(file);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }
        final SVNUrl repositoryUrl = repository;

        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
        SvnProgressSupport support = new SvnProgressSupport() {
            public void perform() {
//                FileStatusCache cache = Subversion.getInstance().getStatusCache();
//                cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                update(new File[] {file}, this, file.getAbsolutePath(), repositoryUrl);
                // give FS some time to recognize the change - e.g. MS Win system takes some time to recognize changes on net drives
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        FileObject fo = FileUtil.toFileObject(file);
                        if (fo != null) {
                            fo.refresh(true);
                        }
                    }
                }, 100);
            }
        };
        support.start(rp, repositoryUrl, org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_Update_Progress")); // NOI18N
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

    private static class UpdateNotifyListener implements ISVNNotifyListener {
        private Pattern p = Pattern.compile("C   (.+)");
        boolean causedConflict = false;
        public void logMessage(String msg) {
            // XXX JAVAHL - different msg in case of conflicts
            if(causedConflict) return;
            Matcher m = p.matcher(msg);
            if(m.matches()) {
                causedConflict = true;
            }
        }
        public void setCommand(int arg0)                    { /* boring */  }
        public void logCommandLine(String arg0)             { /* boring */  }
        public void logError(String arg0)                   { /* boring */  }
        public void logRevision(long arg0, String arg1)     { /* boring */  }
        public void logCompleted(String arg0)               { /* boring */  }
        public void onNotify(File arg0, SVNNodeKind arg1)   { /* boring */  }
    }
    
}

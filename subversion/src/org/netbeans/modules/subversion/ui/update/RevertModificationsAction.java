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

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.*;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Reverts local changes.
 *
 * @author Petr Kuzel
 */
public class RevertModificationsAction extends ContextAction {
    
    /** Creates a new instance of RevertModificationsAction */
    public RevertModificationsAction() {
    }
    
    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Revert"; // NOI18N
    }
    
    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_VERSIONED & ~FileInformation.STATUS_VERSIONED_NEWINREPOSITORY;
    }
    
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_VERSIONED & ~FileInformation.STATUS_VERSIONED_NEWINREPOSITORY;
    }
    
    protected void performContextAction(final Node[] nodes) {
        if(!Subversion.getInstance().checkClientAvailable()) {
            return;
        }
        final Context ctx = getContext(nodes);
        final File root = ctx.getRootFiles()[0];
        final SVNUrl rootUrl;
        final SVNUrl url;
        
        try {
            rootUrl = SvnUtils.getRepositoryRootUrl(root);
            url = SvnUtils.getRepositoryUrl(root);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }
        final RepositoryFile repositoryFile = new RepositoryFile(rootUrl, url, SVNRevision.HEAD);
        
        final RevertModifications revertModifications = new RevertModifications(repositoryFile);
        if(!revertModifications.showDialog()) {
            return;
        }
        
        ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, nodes) {
            public void perform() {
                performRevert(revertModifications.getRevisionInterval(), revertModifications.revertNewFiles(), ctx, this);
            }
        };
        support.start(createRequestProcessor(nodes));
    }
    
    /** Recursive revert */
    public static void performRevert(RevertModifications.RevisionInterval revisions, boolean revertNewFiles, Context ctx, SvnProgressSupport support) {
        SvnClient client;
        try {
            client = Subversion.getInstance().getClient(ctx, support);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }
        
        File files[] = ctx.getFiles();
        File[][] split = Utils.splitFlatOthers(files);
        for (int c = 0; c<split.length; c++) {
            if(support.isCanceled()) {
                return;
            }
            files = split[c];
            boolean recursive = c == 1;
            if (recursive == false) {
                files = SvnUtils.flatten(files, FileInformation.STATUS_REVERTIBLE_CHANGE);
            }
            
            try {
                if(revisions != null) {
                    for (int i= 0; i < files.length; i++) {
                        if(support.isCanceled()) {
                            return;
                        }
                        SVNUrl url = SvnUtils.getRepositoryUrl(files[i]);
                        revisions = recountStartRevision(client, url, revisions);
                        if(files[i].exists()) {
                            client.merge(url, revisions.endRevision, url, revisions.startRevision, files[i], false, recursive);
                        } else {
                            assert revisions.startRevision instanceof SVNRevision.Number : "The revision has to be a Number when trying to undelete file!";
                            client.copy(url, files[i], revisions.startRevision);
                        }
                    }
                } else {
                    if(support.isCanceled()) {
                        return;
                    }
                    if(files.length > 0 ) {                        
                        // check for deleted files, we also want to undelete their parents
                        Set<File> deletedFiles = new HashSet<File>();
                        for(File file : files) {
                            deletedFiles.addAll(getDeletedParents(file));
                        }                        
                                
                        client.revert(files, recursive);
                        
                        // revert also deleted parent folders
                        // for all undeleted files
                        client.revert(deletedFiles.toArray(new File[deletedFiles.size()]), false);
                    }
                }
            } catch (SVNClientException ex) {
                support.annotate(ex);
            }
        }
        
        if(support.isCanceled()) {
            return;
        }
        
        if(revertNewFiles) {
            File[] newfiles = Subversion.getInstance().getStatusCache().listFiles(ctx.getRootFiles(), FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
            for (int i = 0; i < newfiles.length; i++) {
                FileObject fo = FileUtil.toFileObject(newfiles[i]);
                try {
                    if(fo != null) {
                        fo.delete();
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
    }
    
    private static Set<File> getDeletedParents(File file) {
        Set<File> ret = new HashSet<File>();
        file = file.getParentFile();
        if(file == null) {
            return ret;
        }        
        
        FileInformation info = Subversion.getInstance().getStatusCache().getStatus(file);
        if( !((info.getStatus() & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) != 0 ||
              (info.getStatus() & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY) != 0) )  
        {
            return ret;
        }        
                
        ret.add(file);        
        
        ret.addAll(getDeletedParents(file));
        return ret;
    }    
    
    /**
     * Folders that were resurrected by "Revert Delete" have not really been created because they already existed.
     * Therefore we must refresh their status manually.
     *
     * @param file
     */
    private static void refreshRecursively(File file) {
        File [] files = file.listFiles();
        if (files != null) {
            for (File child : files) {
                refreshRecursively(child);
            }
        }
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
    }

    private static RevertModifications.RevisionInterval recountStartRevision(SvnClient client, SVNUrl repository, RevertModifications.RevisionInterval ret) throws SVNClientException {
        if(ret.startRevision.equals(SVNRevision.HEAD)) {
            ISVNInfo info = client.getInfo(repository);
            ret.startRevision = info.getRevision();
        }
        long start = Long.parseLong(ret.startRevision.toString());
        if(start > 0) {
            start = start - 1;
        }
        ret.startRevision = new SVNRevision.Number(start);
        return ret;
    }

}

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
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.*;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileStateInvalidException;
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
        final Context ctx = getContext(nodes);
        final File root = ctx.getRootFiles()[0];
        final SVNUrl url = SvnUtils.getRepositoryRootUrl(root);
        final RepositoryFile repositoryFile = new RepositoryFile(url, url, SVNRevision.HEAD);
        
        final RevertModifications revertModifications = new RevertModifications(repositoryFile);
        if(!revertModifications.showDialog()) {
            return;
        }

        ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, nodes) {
            public void perform() {
                performRevert(ctx, revertModifications, this);
            }
        };            
        support.start(createRequestProcessor(nodes));
    }
        
    /** Recursive revert */
    public static void performRevert(Context ctx, RevertModifications revertModifications, SvnProgressSupport support) {
        SvnClient client;
        try {
            client = Subversion.getInstance().getClient(ctx, support);
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ex);
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
                RevertModifications.RevisionInterval revisions = revertModifications.getRevisionInterval();
                if(revisions != null) {
                    for (int i= 0; i<files.length; i++) {
                        if(support.isCanceled()) {
                            return;
                        }
                        SVNUrl url = SvnUtils.getRepositoryUrl(files[i]);
                        revisions = recountStartRevision(client, url, revisions);
                        client.merge(url, revisions.endRevision, url, revisions.startRevision, files[i], false, recursive);                        
                    }
                } else {
                    if(support.isCanceled()) {
                        return;
                    }
                    if(files.length > 0 ) {
                        client.revert(files, recursive);                                               
                    }                    
                }
            } catch (SVNClientException ex) {
                support.annotate(ex);
            }
        }

        if(support.isCanceled()) {
            return;
        }
        
        if(revertModifications.revertNewFiles()) {
            File[] newfiles = Subversion.getInstance().getStatusCache().listFiles(files, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
            for (int i = 0; i < newfiles.length; i++) {                                
                FileObject fo = FileUtil.toFileObject(newfiles[i]);                                    
                try {
                    if(fo != null) {
                        fo.delete();
                    }
                }
                catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }        
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

    private static void addFileSystem(Set filesystems, File file) {
        FileObject fo;
        for (;;) {
            fo = FileUtil.toFileObject(file);
            if (fo != null) break;
            file = file.getParentFile();
            if (file == null) return;
        }
        try {
            filesystems.add(fo.getFileSystem());
        } catch (FileStateInvalidException e) {
            // ignore invalid filesystems
        }
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

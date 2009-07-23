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

package org.netbeans.modules.subversion.ui.update;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.logging.Level;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.*;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.versioning.util.Utils;
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
        File[] roots = ctx.getRootFiles();
        // filter managed roots
        List<File> l = new ArrayList<File>();
        for (File file : roots) {
            if(SvnUtils.isManaged(file)) {
                l.add(file);
            }
        }
        roots = l.toArray(new File[l.size()]);

        if(roots == null || roots.length == 0) return;

        File interestingFile;
        if(roots.length == 1) {
            interestingFile = roots[0];
        } else {
            interestingFile = SvnUtils.getPrimaryFile(roots[0]);
        }

        final SVNUrl rootUrl;
        final SVNUrl url;
        
        try {
            rootUrl = SvnUtils.getRepositoryRootUrl(interestingFile);
            url = SvnUtils.getRepositoryUrl(interestingFile);
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
                        RevertModifications.RevisionInterval targetInterval = recountStartRevision(client, url, revisions);
                        if(files[i].exists()) {
                            client.merge(url, targetInterval.endRevision,
                                         url, targetInterval.startRevision,
                                         files[i], false, recursive);
                        } else {
                            assert targetInterval.startRevision instanceof SVNRevision.Number
                                   : "The revision has to be a Number when trying to undelete file!";
                            client.copy(url, files[i], targetInterval.startRevision);
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
                                
                        // XXX JAVAHL client.revert(files, recursive);
                        for (File file : files) {
                            client.revert(file, recursive);
                        }
                        
                        // revert also deleted parent folders
                        // for all undeleted files
                        if(deletedFiles.size() > 0) {
                            // XXX JAVAHL client.revert(deletedFiles.toArray(new File[deletedFiles.size()]), false);
                            for (File file : deletedFiles) {
                                client.revert(file, false);
                            }    
                        }                        
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
                    Subversion.LOG.log(Level.SEVERE, null, ex);
                }
            }
        }
    }     

    private static List<File> getDeletedParents(File file) {
        List<File> ret = new ArrayList<File>();
        for(File parent = file.getParentFile(); parent != null; parent = parent.getParentFile()) {        
            FileInformation info = Subversion.getInstance().getStatusCache().getStatus(parent);
            if( !((info.getStatus() & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) != 0 ||
                  (info.getStatus() & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY) != 0) )  
            {
                return ret;
            }
            ret.add(parent);                                
        }        
        return ret;
    }
    
    private static RevertModifications.RevisionInterval recountStartRevision(SvnClient client, SVNUrl repository, RevertModifications.RevisionInterval ret) throws SVNClientException {
        SVNRevision currStartRevision = ret.startRevision;
        SVNRevision currEndRevision = ret.endRevision;

        if(currStartRevision.equals(SVNRevision.HEAD)) {
            ISVNInfo info = client.getInfo(repository);
            currStartRevision = info.getRevision();
        }

        long currStartRevNum = Long.parseLong(currStartRevision.toString());
        long newStartRevNum = (currStartRevNum > 0) ? currStartRevNum - 1
                                                    : currStartRevNum;

        return new RevertModifications.RevisionInterval(
                                         new SVNRevision.Number(newStartRevNum),
                                         currEndRevision);
    }

}

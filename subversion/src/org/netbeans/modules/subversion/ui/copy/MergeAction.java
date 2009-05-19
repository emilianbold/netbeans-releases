/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.subversion.ui.copy;

import java.io.File;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class MergeAction extends ContextAction {

    public MergeAction() {        
    }

    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Merge";    // NOI18N        
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }
    
    protected boolean enable(Node[] nodes) {
        return nodes != null && nodes.length == 1 &&  getCachedContext(nodes).getRoots().size() > 0;
    }   
    
    protected void performContextAction(final Node[] nodes) {
        
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }
        
        Context ctx = getContext(nodes);        
        final File[] roots = SvnUtils.getActionRoots(ctx);
        if(roots == null || roots.length == 0) return;

        File interestingFile;
        if(roots.length == 1) {
            interestingFile = roots[0];
        } else {
            interestingFile = SvnUtils.getPrimaryFile(roots[0]);
        }

        SVNUrl rootUrl;
        SVNUrl url;
        try {            
            rootUrl = SvnUtils.getRepositoryRootUrl(interestingFile);
            url = SvnUtils.getRepositoryRootUrl(interestingFile);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }           
        final RepositoryFile repositoryRoot = new RepositoryFile(rootUrl, url, SVNRevision.HEAD);

        final Merge merge = new Merge(repositoryRoot, interestingFile);
        if(merge.showDialog()) {
            ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, nodes) {
                public void perform() {
                    for (File root : roots) {
                        performMerge(merge, repositoryRoot, root, this, roots.length > 1);
                    }
                }
            };
            support.start(createRequestProcessor(nodes));
        }        
    }

    /**
     * Merges changes from the remote url(s) and revisions given bet the Merge controller with
     * the given file
     * @param merge
     * @param repositoryRoot
     * @param file
     * @param support
     * @param partOfMultiFile 
     */
    private void performMerge(Merge merge, RepositoryFile repositoryRoot, File file, SvnProgressSupport support, boolean partOfMultiFile) {
        File[][] split = Utils.splitFlatOthers(new File[] {file} );
        boolean recursive;
        // there can be only 1 root file
        if(split[0].length > 0) {
            recursive = false;
        } else {
            recursive = true;
        }                

        try {
            SvnClient client;
            try {
                client = Subversion.getInstance().getClient(repositoryRoot.getRepositoryUrl());
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, true, true);
                return;
            }

            if(support.isCanceled()) {
                return;
            }
            
            SVNUrl endUrl = merge.getMergeEndRepositoryFile().getFileUrl();
            SVNRevision endRevision = merge.getMergeEndRevision();

            final RepositoryFile mergeStartRepositoryFile = merge.getMergeStartRepositoryFile();
            SVNUrl startUrl = mergeStartRepositoryFile != null ? mergeStartRepositoryFile.getFileUrl() : null;
            if (file.isFile() && partOfMultiFile) {
                // change the filename ONLY for multi-file data objects, not for folders
                endUrl = merge.getMergeEndRepositoryFile().replaceLastSegment(file.getName(), 0).getFileUrl();
                startUrl = mergeStartRepositoryFile != null ? mergeStartRepositoryFile.replaceLastSegment(file.getName(), 0).getFileUrl() : null;
            }
            
            SVNRevision startRevision;
            if(startUrl != null) {                
                startRevision = merge.getMergeStartRevision();
            } else {
                // XXX is this the only way we can do it?
                startUrl = endUrl;
                ISVNLogMessage[] log = client.getLogMessages(startUrl, new SVNRevision.Number(0), new SVNRevision.Number(0), SVNRevision.HEAD, true, false, 0L);
                startRevision = log[0].getRevision();
            }                        
            if(support.isCanceled()) {
                return;
            }
            
            client.merge(startUrl,
                         startRevision,
                         endUrl,
                         endRevision,
                         file,
                         false,
                         recursive);                              

        } catch (SVNClientException ex) {
            support.annotate(ex);
        }
    }
}

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A showDialog of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.copy;

import java.io.File;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.ExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
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
    
    protected void performContextAction(final Node[] nodes) {
        Context ctx = getContext(nodes);        
        final File root = ctx.getRootFiles()[0];
        SVNUrl url = SvnUtils.getRepositoryRootUrl(root);
        final RepositoryFile repositoryRoot = new RepositoryFile(url, url, SVNRevision.HEAD);
     
        final Merge merge = new Merge(repositoryRoot, root);           
        if(merge.showDialog()) {
            ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, nodes) {
                public void perform() {
                    performMerge(merge, repositoryRoot, root, this);
                }
            };
            support.start(createRequestProcessor(nodes));
        }        
    }

    private void performMerge(Merge merge,  RepositoryFile repositoryRoot, File root, SvnProgressSupport support) {
        File[][] split = SvnUtils.splitFlatOthers(new File[] {root} );
        boolean recursive;
        // there can be only 1 root file
        if(split[0].length > 0) {
            recursive = false;
        } else {
            recursive = true;
        }        

        RepositoryFile mergeFromRepository = merge.getMergeFromRepositoryFile();
        boolean mergeAfter = merge.madeAfter();
        RepositoryFile mergeAfterRepository;
        if(mergeAfter) {
            mergeAfterRepository = merge.getMergeAfterRepositoryFile();
        } else {
            mergeAfterRepository = null;
        }           

        try {
            ISVNClientAdapter client;
            try {
                client = Subversion.getInstance().getClient(repositoryRoot.getRepositoryUrl());
            } catch (SVNClientException ex) {
                ErrorManager.getDefault().notify(ex);
                return;
            }

            if(support.isCanceled()) {
                return;
            }

            if(mergeAfter) {
                client.merge(mergeAfterRepository.getFileUrl(), 
                             mergeAfterRepository.getRevision(),
                             mergeFromRepository.getFileUrl(), 
                             mergeFromRepository.getRevision(),
                             root,
                             false,
                             recursive);
            } else {

                ISVNInfo info = client.getInfoFromWorkingCopy(root);
                if(support.isCanceled()) {
                    return;
                }

                SVNUrl fileUrl = null;
                if(info == null) {
                    // oops
                    return;
                }

                client.merge(info.getUrl(), 
                             info.getRevision(), 
                             mergeFromRepository.getFileUrl(), 
                             mergeFromRepository.getRevision(),
                             root,
                             false,
                             recursive);                                    
            }

        } catch (SVNClientException ex) {
            ExceptionHandler eh = new ExceptionHandler(ex);
            eh.annotate();
        }
    }
}

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
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
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
        return ~0; // XXX
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED; // XXX
    }
    
    protected void performContextAction(Node[] nodes) {
        Context ctx = getContext(nodes);        
        
        final File root = ctx.getRootFiles()[0];                        
        SVNUrl url = SvnUtils.getRepositoryRootUrl(root);        
        final RepositoryFile repositoryRoot = new RepositoryFile(url, url, SVNRevision.HEAD);
     
        Merge merge = new Merge(repositoryRoot, root.getName());           
        if(merge.showDialog()) {
            final RepositoryFile mergeFromRepository = merge.getMergeFromRepositoryFile();
            final boolean mergeAfter = merge.madeAfter();
            final RepositoryFile mergeAfterRepository;
            if(mergeAfter) {
                mergeAfterRepository = merge.getMergeAfterRepositoryFile();
            } else {
                mergeAfterRepository = null;
            }           
            
            Runnable run = new Runnable() {
                public void run() {
                    startProgress();
                    try {
                        ISVNClientAdapter client = Subversion.getInstance().getClient(repositoryRoot.getRepositoryUrl());
                        ISVNInfo info = client.getInfo(root); // XXX SvnUtils get the whole RepositoryFile if possible ...
                        SVNUrl fileUrl = null; 
                        if(info != null) {
                            fileUrl = info.getUrl();
                        } else {
                            // XXX
                        }
                        
                        if(mergeAfter) {
                            client.merge(mergeAfterRepository.getFileUrl(), 
                                         mergeAfterRepository.getRevision(),
                                         mergeFromRepository.getFileUrl(), 
                                         mergeFromRepository.getRevision(),
                                         root,
                                         false,
                                         true);                                                                                    
                        } else {
                            client.merge(fileUrl, 
                                         info.getRevision(), 
                                         mergeFromRepository.getFileUrl(), 
                                         mergeFromRepository.getRevision(),
                                         root,
                                         false,
                                         true);                                    
                        }
                        
                    } catch (SVNClientException ex) {
                        ex.printStackTrace(); // should not hapen
                        return;
                    } finally {
                        finished();
                    }
                }
            };
            Subversion.getInstance().postRequest(run);                  
               
        }        
    }
    
}

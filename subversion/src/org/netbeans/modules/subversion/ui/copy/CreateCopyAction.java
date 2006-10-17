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
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class CreateCopyAction extends ContextAction {
    
    /** Creates a new instance of CreateCopyAction */
    public CreateCopyAction() {
        
    }

    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Copy";    // NOI18N
    }

    protected int getFileEnabledStatus() {
        return    FileInformation.STATUS_MANAGED
               & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    protected int getDirectoryEnabledStatus() {
        return    FileInformation.STATUS_MANAGED 
               & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
               & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }
    
    protected void performContextAction(final Node[] nodes) {
        Context ctx = getContext(nodes);

        final File root = ctx.getRootFiles()[0];
        File[] files = Subversion.getInstance().getStatusCache().listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);       
        final boolean hasChanges = files.length > 0;                
        final SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(root);
        final RepositoryFile repositoryRoot = new RepositoryFile(repositoryUrl, repositoryUrl, SVNRevision.HEAD);        

        final CreateCopy createCopy = new CreateCopy(repositoryRoot, root, hasChanges);
        if(createCopy.showDialog()) {
            ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this,  nodes) {
                public void perform() {
                    performCopy(createCopy, repositoryUrl, root, this);
                } 
            };
            support.start(createRequestProcessor(nodes));
        }
    }

    private void performCopy(CreateCopy createCopy, SVNUrl repositoryUrl, File root, SvnProgressSupport support) {
        RepositoryFile toRepositoryFile = createCopy.getRepositoryFile();
        String message = createCopy.getMessage();           
        boolean switchTo = createCopy.getSwitchTo();
        
        
        try {                
            ISVNClientAdapter client;
            try {
                client = Subversion.getInstance().getClient(repositoryUrl);
            } catch (SVNClientException ex) {
                ErrorManager.getDefault().notify(ex);
                return;
            }

            if(!toRepositoryFile.isRepositoryRoot()) {
                SVNUrl folderToCreate = toRepositoryFile.removeLastSegment().getFileUrl();
                ISVNInfo info = null;
                try{
                    info = client.getInfo(folderToCreate);                                                                
                } catch (SVNClientException ex) {                                
                    if(!ExceptionHandler.isWrongUrl(ex.getMessage())) { 
                        throw ex;
                    }
                }            

                if(support.isCanceled()) {
                    return;
                }

                if(info == null) {
                    client.mkdir(folderToCreate,
                                 true, 
                                 "[Netbeans SVN client generated message: create a new folder for copy]: " + message); // NOI18N
                }                            
            }                        

            if(support.isCanceled()) {
                return;
            }

            client.copy(root, toRepositoryFile.getFileUrl(), message);            
            
            if(support.isCanceled()) {
                return;
            }

            if(switchTo) {
                SwitchToAction.performSwitch(toRepositoryFile, root, support);
            }

        } catch (SVNClientException ex) {
            support.annotate(ex);
        }
    }
}

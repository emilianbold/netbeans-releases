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
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
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
        return FileInformation.STATUS_MANAGED
            & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }
    
    protected void performContextAction(final Node[] nodes) {
        Context ctx = getContext(nodes);

        final File root = ctx.getRootFiles()[0];
        File[] files = Subversion.getInstance().getStatusCache().listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);       
        boolean isChanged = files.length > 0;                
        SVNUrl url = SvnUtils.getRepositoryRootUrl(root);
        final RepositoryFile repositoryRoot = new RepositoryFile(url, url, SVNRevision.HEAD);

        final CreateCopy createCopy = new CreateCopy(repositoryRoot, root.getName(), isChanged);
        if(createCopy.showDialog()) {
            ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this,  createRequestProcessor(nodes), nodes) {
                public void perform() {
                    performCopy(createCopy, repositoryRoot, root, this);
                }
            };
            support.start();
        }
    }

    private void performCopy(CreateCopy createCopy, RepositoryFile repositoryRoot, File root, SvnProgressSupport support) {
        RepositoryFile repositoryFolder = createCopy.getRepositoryFile();
        String message = createCopy.getMessage();            
        boolean switchTo = createCopy.getSwitchTo();
        
        try {                
            ISVNClientAdapter client = Subversion.getInstance().getClient(repositoryRoot.getRepositoryUrl());

            if(!repositoryFolder.isRepositoryRoot()) {
                SVNUrl folderToCreate = repositoryFolder.removeLastSegment().getFileUrl();
                ISVNInfo info = null;
                try{
                    info = client.getInfo(folderToCreate);                                                                
                } catch (SVNClientException ex) {                                
                    if(!ExceptionHandler.isWrongUrl(ex)) {
                        throw ex;
                    }
                }            

                if(support.isCanceled()) {
                    return;
                }

                if(info == null) {
                    client.mkdir(folderToCreate,
                                 true, 
                                 "[Netbeans SVN client generated message: create a new folder for the following copy]: " + message); // XXX how shoul be this done                      
                }                            
            }                        

            if(support.isCanceled()) {
                return;
            }

            client.copy(root, repositoryFolder.getFileUrl(), message);

            if(support.isCanceled()) {
                return;
            }

            if(switchTo) {
                SwitchToAction.performSwitch(repositoryFolder, false, repositoryRoot, root, support);
            }

        } catch (SVNClientException ex) {
            ExceptionHandler eh = new ExceptionHandler(ex);
            eh.annotate();
        }
    }
}

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
import org.netbeans.modules.subversion.FileStatusProvider;
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
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Petr Kuzel
 */
public class SwitchToAction extends ContextAction {
    
    /**
     * Creates a new instance of SwitchToAction
     */
    public SwitchToAction() {        
    }

    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Switch";    // NOI18N        
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
        return nodes != null && nodes.length == 1 &&  getContext(nodes).getRoots().size() > 0;
    }        
    
    protected void performContextAction(final Node[] nodes) {
        Context ctx = getContext(nodes);        
        
        final File root = ctx.getRootFiles()[0];                        
        SVNUrl url = SvnUtils.getRepositoryRootUrl(root);
        final RepositoryFile repositoryRoot = new RepositoryFile(url, url, SVNRevision.HEAD);
     
        final SwitchTo switchTo = new SwitchTo(repositoryRoot, root);
        if(switchTo.showDialog()) {
            ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, createRequestProcessor(nodes), nodes) {
                public void perform() {
                    RepositoryFile repository = switchTo.getRepositoryFile();
                    boolean replaceModifications = switchTo.replaceModifications();
                    performSwitch(repository, replaceModifications, repositoryRoot, root, this);
                }
            };
            support.start();
        }        
    }

    static void performSwitch(RepositoryFile repository, boolean replaceModifications, RepositoryFile repositoryRoot, File root, SvnProgressSupport support) {
        try {
            ISVNClientAdapter client;
            try {
                client = Subversion.getInstance().getClient(repositoryRoot.getRepositoryUrl());
            } catch (SVNClientException ex) {
                ErrorManager.getDefault().notify(ex);
                return;
            }
            if(replaceModifications) {
                // get rid of all changes ...
                // doesn't wok for added (new) files
                client.revert(root, true);

                if(support.isCanceled()) {
                    return;
                }
            }
            // ... and switch
            client.switchToUrl(root, repository.getFileUrl(), repository.getRevision(), true);
            FileStatusProvider.getInstance().refreshAllAnnotations(false, true);
        } catch (SVNClientException ex) {
            ExceptionHandler eh = new ExceptionHandler(ex);
            eh.annotate();
        }
    }
}

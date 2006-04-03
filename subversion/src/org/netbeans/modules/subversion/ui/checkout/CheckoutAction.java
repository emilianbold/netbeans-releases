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
package org.netbeans.modules.subversion.ui.checkout;

import java.io.File;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.settings.HistorySettings;
import org.netbeans.modules.subversion.ui.wizards.*;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public final class CheckoutAction extends CallableSystemAction {
           
    public void performAction() {
        CheckoutWizard wizard = new CheckoutWizard();
        if (!wizard.show()) return;
        
        final SVNUrl repository = wizard.getRepositoryRoot();
        final RepositoryFile[] repositoryFiles = wizard.getRepositoryFiles();
        final File file = wizard.getWorkdir();        

        final SvnClient client;
        try {
            client = Subversion.getInstance().getClient(repository);
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ex); // should not happen 
            return;
        }
        
        String displayName = org.openide.util.NbBundle.getMessage(CheckoutAction.class, "BK0001");
        SvnProgressSupport support = new SvnProgressSupport(Subversion.getInstance().getRequestProccessor(repository)) {
            public void perform() {
                try {
                    setDisplayName("checking out ...");
                    setCancellableDelegate(client);
                    checkout(client, repository, repositoryFiles, file, false, this);
                } catch (SVNClientException ex) {
                    ErrorManager.getDefault().notify(ex); 
                    return;
                }
                if(isCanceled()) {
                    return;
                }

                setDisplayName("scaning folders ...");
                setCancellableDelegate(null);
                if (HistorySettings.getFlag(HistorySettings.PROP_SHOW_CHECKOUT_COMPLETED, -1) != 0) {
                    String[] folders = new String[repositoryFiles.length];
                    for (int i = 0; i < repositoryFiles.length; i++) {

                        if(repositoryFiles[i].isRepositoryRoot()) {
                            folders[i] = ".";
                        } else {
                            folders[i] = repositoryFiles[i].getFileUrl().getLastPathSegment();
                        }
                        if(isCanceled()) {
                            return;
                        }
                    }             
                    CheckoutCompleted cc = new CheckoutCompleted(file, folders, true);
                    if(isCanceled()) {
                        return;
                    }
                    cc.scanForProjects(this);
                }
            }
        };
        support.start("checkout");

    }
    
    public String getName() {
        return NbBundle.getMessage(CheckoutAction.class, "CTL_CheckoutAction");
    }
    
    protected void initialize() {
        super.initialize();        
        putValue("noIconInMenu", Boolean.TRUE);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }

    public static void checkout(final SvnClient client,
                                final SVNUrl repository,
                                final RepositoryFile[] repositoryFiles,
                                final File workingDir,
                                final boolean atWorkingDirLevel,
                                final SvnProgressSupport support)
    throws SVNClientException
    {
        for (int i = 0; i < repositoryFiles.length; i++) {
            File destination;
            if(!atWorkingDirLevel) {
                destination = new File(workingDir.getAbsolutePath() +
                                       "/" +  // NOI18N
                                       repositoryFiles[i].getName()); // XXX what if the whole repository is seletcted
                destination = FileUtil.normalizeFile(destination);
                destination.mkdir();
            } else {
                destination = workingDir;
            }
            try {
                if(support!=null && support.isCanceled()) {
                    return;
                }
                client.checkout(repositoryFiles[i].getFileUrl(), destination, repositoryFiles[i].getRevision(), true);
                if(support!=null && support.isCanceled()) {
                    return;                
                }
            } catch (SVNClientException ex) {
                ex.printStackTrace();
                throw ex;
            }
        }
    }

}

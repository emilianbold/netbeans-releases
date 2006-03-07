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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.settings.HistorySettings;
import org.netbeans.modules.subversion.ui.wizards.*;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.lookup.Lookups;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public final class CheckoutAction extends CallableSystemAction {
    
    private RequestProcessor.Task checkOutTask = null;
    private Thread checkOutThread = null;
    private ProgressHandle progressHandle = null;
    
    // XXX dummy
    private Cancellable cancellable = new Cancellable() {
        public boolean cancel() {
            if(checkOutTask!=null) {                    
                checkOutTask.cancel();                                        
            }
            if(checkOutThread!=null) {
                checkOutThread.interrupt();                                        
            }
            if(progressHandle != null) {
                progressHandle.finish();
            }
            // XXX checkout still running ...
            // XXX client.cancleOperation is not implemented yet 
            return true;
        }
    };
        
    public void performAction() {
        CheckoutWizard wizard = new CheckoutWizard();
        if (!wizard.show()) return;
        
        final SVNUrl repository = wizard.getRepositoryRoot();
        final RepositoryFile[] repositoryFiles = wizard.getRepositoryFiles();
        final File file = wizard.getWorkdir();        
        
        RequestProcessor processor = new RequestProcessor("CheckoutActionRP", 1, true);
        checkOutTask = processor.post(new Runnable() {
            public void run() {          
                checkOutThread = Thread.currentThread();
                
                progressHandle = 
                    ProgressHandleFactory.createHandle(org.openide.util.NbBundle.getMessage(CheckoutAction.class, "BK0001"), cancellable);       // NOI18N
                progressHandle.start();                
                try{            
                    try {
                        checkout(repository, repositoryFiles, file, true, false);
                    } catch (SVNClientException ex) {                        
                        org.openide.ErrorManager.getDefault().notify(ex);                                    
                    }
                } finally {
                    progressHandle.finish();
                }
            }
        });
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

    public static void checkout(SVNUrl repository, RepositoryFile repositoryFiles[], File workingDir, boolean scanProject, boolean atWorkingDirLevel) throws SVNClientException {
        SvnClient client;
        try {
            client = Subversion.getInstance().getClient(repository);
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ex); // should not happen 
            return;
        }
        
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
            
            client.checkout(repositoryFiles[i].getFileUrl(), destination, repositoryFiles[i].getRevision(), true);                            
              
        }                           
        
        if (HistorySettings.getFlag(HistorySettings.PROP_SHOW_CHECKOUT_COMPLETED, -1) != 0 && scanProject) {
            String[] folders = new String[repositoryFiles.length];
            for (int i = 0; i < repositoryFiles.length; i++) {
                if(repositoryFiles[i].isRepositoryRoot()) {
                    folders[i] = ".";
                } else {
                    folders[i] = repositoryFiles[i].getFileUrl().getLastPathSegment();
                }                
            }
            CheckoutCompleted cc = new CheckoutCompleted(workingDir, folders, true);
            cc.scanForProjects();
        }                
    }

}

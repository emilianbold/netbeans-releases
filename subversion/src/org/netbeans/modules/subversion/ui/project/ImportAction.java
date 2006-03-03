/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.project;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.subversion.Subversion;
import org.openide.util.actions.NodeAction;
import org.openide.util.*;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.netbeans.api.project.*;
import java.io.*;
import java.util.*;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.ui.checkout.CheckoutAction;
import org.netbeans.modules.subversion.ui.wizards.ImportWizard;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Petr Kuzel
 * XXX also a context action ???
 */
public final class ImportAction extends NodeAction {

    // XXX dummy
    private Cancellable cancellable = new Cancellable() {
        public boolean cancel() {
            if(importTask!=null) {                    
                importTask.cancel();                                        
            }
            if(importThread!=null) {
                importThread.interrupt();                                        
            }            
            if(progressHandle != null) {
                progressHandle.finish();
            }
            // XXX checkout still running ...
            // XXX client.cancleOperation is not implemented yet 
            return true;
        }
    };

    private RequestProcessor.Task importTask;
    private Thread importThread;
    private ProgressHandle progressHandle;
    
    public ImportAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(ImportAction.class, "BK0006");
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    protected boolean enable(Node[] nodes) {
        if (nodes.length == 1) {
            FileStatusCache cache = Subversion.getInstance().getStatusCache();
            File dir = lookupImportDirectory(nodes[0]);
            if (dir != null && dir.isDirectory()) {
                FileInformation status = cache.getStatus(dir);
                // mutually exclusive enablement logic with commit
                if ((status.getStatus() & FileInformation.STATUS_MANAGED) == 0) {
                    // do not allow to import partial/nonatomic project, all must lie under imported common root
                    FileObject fo = FileUtil.toFileObject(dir);
                    Project p = FileOwnerQuery.getOwner(fo);
                    if (p == null) {
                        return true;
                    }
                    FileObject projectDir = p.getProjectDirectory();
                    return FileUtil.isParentOf(projectDir, fo) == false;
                }
            }
        }
        return false;
    }

    protected boolean asynchronous() {
        return false;
    }

    protected void performAction(Node[] nodes) {
        if (nodes.length == 1) {
            final File importDirectory = lookupImportDirectory(nodes[0]);
            if (importDirectory != null) {
                ImportWizard wizard = new ImportWizard(nodes[0].getName());
                if (!wizard.show()) return;
                
                final SVNUrl repositoryUrl = wizard.getRepositoryUrl();
                final SVNUrl repositoryFolderUrl = wizard.getRepositoryFolderUrl(); 
                final String message = wizard.getMessage();        
                final boolean checkout = wizard.checkoutAfterImport();
                final File file = lookupImportDirectory(nodes[0]);                 
                
                RequestProcessor processor = new RequestProcessor("CheckinActionRP", 1, true);
                importTask = processor.post(new Runnable() {
                    public void run() {                      
                        importThread = Thread.currentThread();                         
                        progressHandle = ProgressHandleFactory.createHandle(org.openide.util.NbBundle.getMessage(ImportAction.class, "BK0001"), cancellable);       // NOI18N
                        progressHandle.start();                
                        try{                            
                            doImport(repositoryUrl, repositoryFolderUrl, file, message);
                            if(checkout) {
                                doCheckout(repositoryUrl, repositoryFolderUrl, file);
                            }                            
                        } catch (SVNClientException ex) {
                            org.openide.ErrorManager.getDefault().notify(ex);                                    
                        } finally {
                            progressHandle.finish();            
                        }  
                    }
                });
            }
        }
    }

    /**
     * Perform asynchronous checkin action with preconfigured values.
     */
    private void doImport(SVNUrl repositoryUrl, SVNUrl folderUrl, File file, String message) throws SVNClientException {        
        try {
            SvnClient client = Subversion.getInstance().getClient(folderUrl);
            
            // import into repository ...
            client.doImport(file, folderUrl, message, true);                    
            
        } catch (SVNClientException ex) {
            org.openide.ErrorManager.getDefault().notify(ex); 
            return;
        }                           
    }    

    private void deleteDirectory(File file) {
         File[] files = file.listFiles();
         if(files !=null || files.length > 0) {
             for (int i = 0; i < files.length; i++) {
                 if(files[i].isDirectory()) {
                     deleteDirectory(files[i]);
                 } else {
                    files[i].delete();
                 }             
             }            
         }
         file.delete();
    }
    
    public boolean cancel() {
        return true;
    }

    private void doCheckout(SVNUrl repositoryUrl, SVNUrl repositoryFolderUrl, File file)  throws SVNClientException {
        RepositoryFile[] repositoryFile = new RepositoryFile[] { new RepositoryFile(repositoryUrl, repositoryFolderUrl, SVNRevision.HEAD) };                        
        // XXX doing it this way we probably will get in troubles with the IDE
        File checkoutFile = new File(file.getAbsolutePath() + ".co");             
        CheckoutAction.checkout(repositoryUrl, repositoryFile, checkoutFile, false, true);                         
        File tmpFile = new File(file.getAbsolutePath() + ".tmp");             
        file.renameTo(tmpFile);
        checkoutFile.renameTo(file);                          
        deleteDirectory(tmpFile);               
    }
    
    private File lookupImportDirectory(Node node) {
        File importDirectory = null;
        Project project = (Project) node.getLookup().lookup(Project.class);
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            if (groups.length == 1) {
                FileObject root = groups[0].getRootFolder();
                importDirectory = FileUtil.toFile(root);
            } else {
                importDirectory = FileUtil.toFile(project.getProjectDirectory());
            }
        } else {
            FileObject fo = null;
            Collection fileObjects = node.getLookup().lookup(new Lookup.Template(FileObject.class)).allInstances();
            if (fileObjects.size() > 0) {
                fo = (FileObject) fileObjects.iterator().next();
            } else {
                DataObject dataObject = (DataObject) node.getCookie(DataObject.class);
                if (dataObject instanceof DataShadow) {
                    dataObject = ((DataShadow) dataObject).getOriginal();
                }
                if (dataObject != null) {
                    fo = dataObject.getPrimaryFile();
                }
            }

            if (fo != null) {
                File f = FileUtil.toFile(fo);
                if (f != null && f.isDirectory()) {
                    importDirectory = f;
                }
            }
        }
        return importDirectory;
    }
    
}

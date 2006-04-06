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

import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.util.FileUtils;
import org.openide.ErrorManager;
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
 */
public final class ImportAction extends NodeAction {
    
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
                
                final SvnClient client;
                try {
                    client = Subversion.getInstance().getClient(repositoryUrl);
                } catch (SVNClientException ex) {
                    ErrorManager.getDefault().notify(ex);
                    return;
                }

                performAction(repositoryUrl, repositoryFolderUrl, message, checkout, importDirectory, client);
            }
        }
    }

    private void performAction(final SVNUrl repositoryUrl,
                               final SVNUrl repositoryFolderUrl,
                               final String message,
                               final boolean checkout,
                               final File importDirectory,
                               final SvnClient client)
    {
        RequestProcessor rp = Subversion.getInstance().getRequestProccessor(repositoryUrl);
        SvnProgressSupport support = new SvnProgressSupport(rp) {
            public void perform() {
                try{                   
                    try {
                        client.doImport(importDirectory, repositoryFolderUrl, message, true);
                    } catch (SVNClientException ex) {
                        org.openide.ErrorManager.getDefault().notify(ex);
                        return;
                    }
                    if(isCanceled()) {
                        return;
                    }

                    if(checkout) {                        

                        RepositoryFile[] repositoryFile = new RepositoryFile[] { new RepositoryFile(repositoryUrl, repositoryFolderUrl, SVNRevision.HEAD) };
                        File checkoutFile = new File(importDirectory.getAbsolutePath() + ".co");
                        CheckoutAction.checkout(client, repositoryUrl, repositoryFile, checkoutFile, true, this);
                        if(isCanceled()) {
                            return;
                        }
                        
                        copyMetadata(checkoutFile, importDirectory);
                        refreshRecursively(importDirectory);

                        if(isCanceled()) {
                            return;
                        }
                        FileUtils.deleteRecursively(checkoutFile);
                    }                            
                } catch (SVNClientException ex) {
                    org.openide.ErrorManager.getDefault().notify(ex);                                    
                } 
            }
        };
        support.start("Importing");
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

    private void refreshRecursively(File folder) {
        if (folder == null) return;
        refreshRecursively(folder.getParentFile());
        Subversion.getInstance().getStatusCache().refresh(folder, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
    }

    private void copyMetadata(File sourceFolder, File targetFolder) {
        // XXX there is already somewhere a utility method giving the metadata file suffix - ".svn", "_svn", ...
        FileUtils.copyDirFiles(new File(sourceFolder.getAbsolutePath() + "/.svn"), new File(targetFolder.getAbsolutePath() + "/.svn"), true);
        targetFolder.setLastModified(sourceFolder.lastModified());
        File[] files = sourceFolder.listFiles();
        for (int i = 0; i < files.length; i++) {
            if(files[i].isDirectory() && !files[i].getName().equals(".svn")) {
                copyMetadata(files[i], new File(targetFolder.getAbsolutePath() + "/" + files[i].getName()));
            } else {
                (new File(targetFolder.getAbsolutePath() + "/" + files[i].getName())).setLastModified(files[i].lastModified());
            }
        }
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

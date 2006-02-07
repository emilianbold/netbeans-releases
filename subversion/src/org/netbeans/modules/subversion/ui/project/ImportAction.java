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
import org.netbeans.modules.subversion.SVNRoot;
import org.netbeans.modules.subversion.ui.wizards.Executor;
import org.netbeans.modules.subversion.ui.wizards.ImportWizard;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
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

    protected void performAction(Node[] nodes) {
        if (nodes.length == 1) {
            final File importDirectory = lookupImportDirectory(nodes[0]);
            if (importDirectory != null) {
                ImportWizard wizard = new ImportWizard();
                if (!wizard.show()) return;

                final SVNUrl svnUrl = wizard.getSelectedRoot().getSvnUrl();
                final String message = wizard.getMessage();        
                final File file = lookupImportDirectory(nodes[0]);
                
                RequestProcessor processor = new RequestProcessor("CheckinActionRP", 1, true);
                processor.post(new Runnable() {
                    public void run() {                                  
                        checkin(svnUrl, file, message);
                    }
                });
            }
        }
    }

    /**
     * Perform asynchronous checkin action with preconfigured values.
     */
    public void checkin(final SVNUrl svnUrl, final File file, final String message) {
        Executor.Command cmd = new Executor.Command () {
            protected void executeCommand(final ISVNClientAdapter client) throws SVNClientException {                                 
                client.doImport(file, svnUrl, message, true);                                                    
            }            
        };                        
        
        ProgressHandle progressHandle = 
            ProgressHandleFactory.createHandle(org.openide.util.NbBundle.getMessage(ImportAction.class, "BK0001"));       // NOI18N
        progressHandle.start();                
        try{
            try {
                Executor.getInstance().execute(cmd);
            } catch (SVNClientException ex) {
                org.openide.ErrorManager.getDefault().notify(ex);
                progressHandle.finish();
                return; 
            }    
        } finally {
            progressHandle.finish();            
        }        
              
    }    

    public boolean cancel() {
        return true;
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

    protected boolean asynchronous() {
        return false;
    }

}

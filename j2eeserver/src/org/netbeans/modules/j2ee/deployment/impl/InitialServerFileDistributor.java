/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.deployment.impl;

import org.netbeans.modules.j2ee.deployment.plugins.spi.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentTarget;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentConfigurationProvider;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.model.DeployableObject;
import org.openide.util.NbBundle;
import javax.enterprise.deploy.shared.CommandType;
import org.openide.ErrorManager;
import org.openide.filesystems.Repository;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;

import java.util.*;
import java.io.*;

/**
 *
 * @author  nn136682
 */
public class InitialServerFileDistributor extends ServerProgress {
    ServerString serverString;
    DeploymentTarget dtarget;
    FileDeploymentLayout fileLayout;
    Target target;

    /** Creates a new instance of InitialServerFileDistributor */
    public InitialServerFileDistributor(DeploymentTarget dtarget, Target target) {
        super(dtarget.getServer().getServerInstance());
        this.serverString = dtarget.getServer();
        this.dtarget = dtarget;
        this.target = target;
        fileLayout = serverString.getServerInstance().getFileDeploymentLayout();
    }
    
    public File distribute() {
        setStatusDistributeRunning(NbBundle.getMessage(
            InitialServerFileDistributor.class, "MSG_RunningIncrementalDeploy", target));

        DeploymentConfigurationProvider deployment = dtarget.getDeploymentConfigurationProvider();
        J2eeModule source = dtarget.getModule();
        DeployableObject deployable = deployment.getDeployableObject(null);
        try {
            File dir = fileLayout.getDirectoryForNewApplication(target, deployable);
            _distribute(source.getArchiveContents(), dir);

            if (source instanceof J2eeModuleContainer) {
                J2eeModule[] childModules = ((J2eeModuleContainer)source).getModules(null);
                for (int i=0; i<childModules.length; i++) {
                    String uri = childModules[i].getUrl();
                    DeployableObject childModule = deployment.getDeployableObject(uri);
                    File subdir = fileLayout.getDirectoryForNewModule(dir, uri, childModule);
                    _distribute(childModules[i].getArchiveContents(), dir);
                }
            }

            setStatusDistributeCompleted(NbBundle.getMessage(
                InitialServerFileDistributor.class, "MSG_DoneIncrementalDeploy", deployable));

            return dir;
            
        } catch (Exception e) {
            setStatusDistributeFailed(e.getMessage());
            ErrorManager.getDefault().log(ErrorManager.EXCEPTION, e.getMessage());
        }
        return null;
    }
    
    private void _distribute(Iterator rootedEntries, File dir) {
        LocalFileSystem lfs = null;
        FileLock lock = null;

        try {
            if (! dir.exists())
                dir.mkdirs();
            
            File parent = dir.getParentFile();
            lfs = new LocalFileSystem();
            lfs.setRootDirectory(parent);
            Repository.getDefault().addFileSystem(lfs);
            FileObject destRoot = lfs.findResource(dir.getName());
            
            while(rootedEntries.hasNext()) {
                J2eeModule.RootedEntry entry = (J2eeModule.RootedEntry) rootedEntries.next();
                String relativePath = entry.getRelativePath();
                FileObject sourceFO = entry.getFileObject();
                FileObject destFolder = ServerFileDistributor.findOrCreateParentFolder(destRoot, relativePath);
                FileUtil.copyFile(sourceFO, destFolder, sourceFO.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = NbBundle.getMessage(InitialServerFileDistributor.class, "MSG_IncrementalDeployFailed", e);
            setStatusDistributeFailed(msg);
            throw new RuntimeException(e);
        } finally {
            if (lfs != null)
                Repository.getDefault().removeFileSystem(lfs);
            if (lock != null) {
                try { lock.releaseLock(); } catch(Exception ex) {}
            }
        }
    }    
    
    //ServerProgress methods
    private void setStatusDistributeRunning(String message) {
        notify(createRunningProgressEvent(CommandType.DISTRIBUTE, message));
    }
    private void setStatusDistributeFailed(String message) {
        notify(createFailedProgressEvent(CommandType.DISTRIBUTE, message));
    }
    private void setStatusDistributeCompleted(String message) {
        notify(createCompletedProgressEvent(CommandType.DISTRIBUTE, message)); 
    }

}

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

import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentTarget;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentConfigurationProvider;

import javax.enterprise.deploy.spi.DeploymentConfiguration;
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
    IncrementalDeployment incDeployment;
    Target target;
    boolean inPlace = false;

    /** Creates a new instance of InitialServerFileDistributor */
    public InitialServerFileDistributor(DeploymentTarget dtarget, Target target) {
        super(dtarget.getServer().getServerInstance());
        this.serverString = dtarget.getServer();
        this.dtarget = dtarget;
        this.target = target;
        incDeployment = serverString.getServerInstance().getIncrementalDeployment ();
    }
    
    public File distribute() {
        DeploymentConfigurationProvider deployment = dtarget.getDeploymentConfigurationProvider();
        J2eeModule source = dtarget.getModule();
        DeployableObject deployable = deployment.getDeployableObject(null);
        String name = dtarget.getDeploymentName();
        File dir = incDeployment.getDirectoryForNewApplication (name, target, deployment.getDeploymentConfiguration ());
        try {
            if (dir == null) {
                inPlace = true;
                if (dtarget.getModule().getContentDirectory() != null) {
                    dir = FileUtil.toFile(dtarget.getModule().getContentDirectory());
                }
                if (dir == null) {
                    String msg = NbBundle.getMessage(InitialServerFileDistributor.class, "MSG_InPlaceNoSupport");
                    setStatusDistributeFailed(msg);
                    return null;
                } else {
                    setStatusDistributeCompleted(NbBundle.getMessage(InitialServerFileDistributor.class, "MSG_InPlaceDeployment", dir));//NOI18N
                    return dir;            
                }
            }
            
            setStatusDistributeRunning(NbBundle.getMessage(
                InitialServerFileDistributor.class, "MSG_RunningInitialDeploy", dtarget.getDeploymentName(), dir));

            _distribute(source.getArchiveContents(), dir, null);

            if (source instanceof J2eeModuleContainer) {
                J2eeModule[] childModules = ((J2eeModuleContainer)source).getModules(null);
                for (int i=0; i<childModules.length; i++) {
                    String uri = childModules[i].getUrl();
                    DeployableObject childModule = deployment.getDeployableObject(uri);
                    File subdir = incDeployment.getDirectoryForNewModule(dir, uri, childModule, deployment.getDeploymentConfiguration ());
                    _distribute(childModules[i].getArchiveContents(), subdir, uri);
                }
            }

            setStatusDistributeCompleted(NbBundle.getMessage(
                InitialServerFileDistributor.class, "MSG_DoneInitialDistribute", dtarget.getDeploymentName()));

            return dir;
            
        } catch (Exception e) {
            setStatusDistributeFailed(e.getMessage());
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            if (!inPlace && !cleanup (dir)) {
                setStatusDistributeFailed ("Failed to cleanup the data after unsucesful distribution");
            }
        }
        return null;
    }
    
    public void cleanup () {
        if (inPlace)
            return;
        
        DeploymentConfigurationProvider deployment = dtarget.getDeploymentConfigurationProvider();
        J2eeModule source = dtarget.getModule();
        DeployableObject deployable = deployment.getDeployableObject(null);
        File dir = incDeployment.getDirectoryForNewApplication (target, deployable, deployment.getDeploymentConfiguration ());
        if (!cleanup (dir)) {
            setStatusDistributeFailed ("Failed to cleanup the data after unsucesful distribution");
        }
    }
    
    private boolean cleanup (File f) {
        String chNames[] = f.list ();
        boolean deleted = true;
        for (int i=0; i < chNames.length; i++) {
            File ch = new File (f.getAbsolutePath (), chNames [i]);
            if (ch.isDirectory ()) {
                deleted = deleted && cleanup (ch);
            } else {
                deleted = deleted && ch.delete ();
            }
        }
        deleted = deleted && f.delete ();
        return deleted;
    }
    
    private void _distribute(Iterator rootedEntries, File dir, String childModuleUri) {
        LocalFileSystem lfs = null;
        FileLock lock = null;
        InputStream in = null;
        OutputStream out = null;

        try {
            if (! dir.exists())
                dir.mkdirs();
            
            File parent = dir.getParentFile();
            lfs = new LocalFileSystem();
            lfs.setRootDirectory(parent);
            Repository.getDefault().addFileSystem(lfs);
            FileObject destRoot = lfs.findResource(dir.getName());
            
            FileObject[] garbages = destRoot.getChildren();
            for (int i=0; i<garbages.length; i++) {
                garbages[i].delete();
            }
            
            while(rootedEntries.hasNext()) {
                J2eeModule.RootedEntry entry = (J2eeModule.RootedEntry) rootedEntries.next();
                String relativePath = entry.getRelativePath();
                FileObject sourceFO = entry.getFileObject();
                FileObject destFolder = ServerFileDistributor.findOrCreateParentFolder(destRoot, relativePath);
                if (sourceFO.isData ()) {
                    //try {
                        FileUtil.copyFile(sourceFO, destFolder, sourceFO.getName());
                    /*} catch (java.io.SyncFailedException sfe) {
                        in = sourceFO.getInputStream();
                        FileObject destFO = destFolder.getFileObject(sourceFO.getName(), sourceFO.getExt());
                        if (destFO != null) {
                            lock = destFO.lock();
                            out = destFO.getOutputStream(lock);
                            FileUtil.copy(in, out);
                        }
                    }*/
                }
            }
            
            // copying serverconfiguration files
            DeploymentConfigurationProvider dcp = dtarget.getDeploymentConfigurationProvider();
            DeploymentConfiguration config = dcp.getDeploymentConfiguration();

            //Pending use childModuleUri for getDeploymentPlanFileNames
            DeployableObject deployable = dcp.getDeployableObject(childModuleUri);
            
        } catch (Exception e) {
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

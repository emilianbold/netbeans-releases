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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.deployment.impl;


import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentTarget;
import org.netbeans.modules.j2ee.deployment.execution.ModuleConfigurationProvider;
import javax.enterprise.deploy.spi.Target;
import org.openide.util.NbBundle;
import javax.enterprise.deploy.shared.CommandType;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;

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
        ModuleConfigurationProvider deployment = dtarget.getModuleConfigurationProvider();
        J2eeModule source = dtarget.getModule();
        J2eeModule deployable = deployment.getJ2eeModule(null);
        String name = dtarget.getDeploymentName();
        File dir = incDeployment.getDirectoryForNewApplication (name, target, deployment.getModuleConfiguration());
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

            if (source instanceof J2eeApplication) {
                J2eeModule[] childModules = ((J2eeApplication)source).getModules();
                for (int i=0; i<childModules.length; i++) {
                    String uri = childModules[i].getUrl();
                    J2eeModule childModule = deployment.getJ2eeModule(uri);
                    File subdir = incDeployment.getDirectoryForNewModule(dir, uri, childModule, deployment.getModuleConfiguration());
                    _distribute(childModules[i].getArchiveContents(), subdir, uri);
                }
            }

            setStatusDistributeCompleted(NbBundle.getMessage(
                InitialServerFileDistributor.class, "MSG_DoneInitialDistribute", dtarget.getDeploymentName()));

            return dir;
            
        } catch (Exception e) {
            setStatusDistributeFailed(e.getMessage());
            Logger.getLogger("global").log(Level.INFO, null, e);
            if (!inPlace && !cleanup (dir)) {
                setStatusDistributeFailed ("Failed to cleanup the data after unsucesful distribution");
            }
        }
        return null;
    }
    
    public void cleanup () {
        if (inPlace)
            return;
        
        ModuleConfigurationProvider deployment = dtarget.getModuleConfigurationProvider();
        J2eeModule source = dtarget.getModule();
        J2eeModule deployable = deployment.getJ2eeModule(null);
        File dir = incDeployment.getDirectoryForNewApplication (target, deployable, deployment.getModuleConfiguration());
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
        FileLock lock = null;
        InputStream in = null;
        OutputStream out = null;

        try {
            if (! dir.exists())
                dir.mkdirs();
            
            // original code deleted the project source... that was probably a
            //   bug.
            FileObject destRoot = FileUtil.toFileObject(dir);
            
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
            
        } catch (Exception e) {
            String msg = NbBundle.getMessage(InitialServerFileDistributor.class, "MSG_IncrementalDeployFailed", e);
            setStatusDistributeFailed(msg);
            throw new RuntimeException(e);
        } finally {
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

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

import org.openide.filesystems.*;
import java.io.*;
import java.util.*;
import org.netbeans.modules.j2ee.deployment.plugins.spi.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.common.api.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.spi.status.*;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentTarget;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentConfigurationProvider;
import java.net.URL;

/**
 *
 * @author  nn136682
 */
public class ServerFileDistributor extends ServerProgress {
    ServerInstance instance;
    DeploymentTarget dtarget;
    ModuleUrlResolver urlResolver;
    FileDeploymentLayout fileLayout;
    DeploymentPlanSplitter splitter;
    
    // valued by RootedEntry's
    Iterator rootModuleFiles;
    // keyed by child module URL, valued by collection of RootedEntry's
    Map childModuleFiles;
    // keyed by child module URL, valued by collection of J2eeModule's
    Map childModuleMap;
    
    /** Creates a new instance of ServerFileDistributor */
    public ServerFileDistributor(ServerInstance instance, DeploymentTarget dtarget){
        super(instance);
        this.instance = instance;
        this.dtarget = dtarget;
        fileLayout = instance.getFileDeploymentLayout();
        urlResolver = instance.getModuleUrlResolver();
        splitter =  instance.getServer().getDeploymentPlanSplitter();
        
        //init contents
        try {
            J2eeModule source = dtarget.getModule();
            rootModuleFiles = source.getArchiveContents();
            if (source instanceof J2eeModuleContainer) {
                childModuleFiles = new HashMap();
                childModuleMap = new HashMap();
                J2eeModule[] childModules = ((J2eeModuleContainer)source).getModules(null);
                for (int i=0; i<childModules.length; i++) {
                    childModuleFiles.put(childModules[i].getUrl(), childModules[i].getArchiveContents());
                    childModuleMap.put(childModules[i].getUrl(), childModules[i]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    static Map j2eeTypeMap = null;
    static List getDescriptorPath(J2eeModule module) {
        if (j2eeTypeMap == null) {
            j2eeTypeMap = new HashMap();
            j2eeTypeMap.put(J2eeModule.EJB, Arrays.asList(new String[]{J2eeModule.EJBJAR_XML, J2eeModule.EJBSERVICES_XML}));
            j2eeTypeMap.put(J2eeModule.WAR, Arrays.asList(new String[]{J2eeModule.WEB_XML, J2eeModule.WEBSERVICES_XML}));
            j2eeTypeMap.put(J2eeModule.CLIENT, Arrays.asList(new String[]{J2eeModule.CLIENT_XML}));
            j2eeTypeMap.put(J2eeModule.CONN, Arrays.asList(new String[]{J2eeModule.CONNECTOR_XML}));
            j2eeTypeMap.put(J2eeModule.EAR, Arrays.asList(new String[]{J2eeModule.APP_XML}));
        }
        return (List) j2eeTypeMap.get(module.getModuleType());
    }
    
    private AppChanges createModuleChangeDescriptor(TargetModuleID target) {
        String moduleUrl = urlResolver.getModuleUrl(target);
        J2eeModule module;
        if (target.getParentTargetModuleID() == null)
            module = dtarget.getModule();
        else
            module = (J2eeModule) childModuleMap.get(moduleUrl);
        List descriptorRelativePaths = getDescriptorPath(module);
        
        List serverDescriptorRelativePaths = Arrays.asList(fileLayout.getDeploymentPlanFilenames(target));
        return new AppChanges(descriptorRelativePaths, serverDescriptorRelativePaths);
    }
    
    public boolean checkServiceImplementations() {
        String missing = null;
        if (splitter == null)
            missing = DeploymentPlanSplitter.class.getName();
        if (urlResolver == null)
            missing = ModuleUrlResolver.class.getName();
        if (fileLayout == null) 
            missing = FileDeploymentLayout.class.getName();
            
        if (missing != null) {
            String msg = NbBundle.getMessage(ServerFileDistributor.class, "MSG_MissingServiceImplementations", missing);
            this.setStatusDistributeFailed(msg);
            return false;
        }
        
        return true;
    }
    
    public AppChangeDescriptor distribute(TargetModule targetModule, ModuleChangeReporter mcr) {
        long lastDeployTime = targetModule.getTimestamp();
        TargetModuleID[] childModules = targetModule.getChildTargetModuleID();
        AppChanges changes = new AppChanges();
        //PENDING: whether module need to be stop first
        for (int i=0; childModules != null && i<childModules.length; i++) {
            String url = urlResolver.getModuleUrl(targetModule.delegate());
            Iterator source = (Iterator) childModuleFiles.get(url);
            changes.record(_distribute(source, childModules[i], url, lastDeployTime));
        }
        //PENDING: whether ordering of copying matters
        changes.record(_distribute(rootModuleFiles, targetModule.delegate(), null, lastDeployTime));
        if (mcr != null)
            changes.record(mcr, lastDeployTime);
        setStatusDistributeCompleted(NbBundle.getMessage(
        ServerFileDistributor.class, "MSG_DoneIncrementalDeploy", targetModule));
        return changes;
    }
    
    public AppChanges _test_distribute(Iterator source, TargetModuleID target, long lastDeployTime) {
        return _distribute(source, target, null, lastDeployTime);
    }
    private AppChanges _distribute(Iterator source, TargetModuleID target, String moduleUrl, long lastDeployTime) {
        AppChanges mc = createModuleChangeDescriptor(target);
        if (source == null) {
            System.out.println("WARNING: there is no contents for "+target);
            return mc;
        }
        setStatusDistributeRunning(NbBundle.getMessage(
        ServerFileDistributor.class, "MSG_RunningIncrementalDeploy", target));
        LocalFileSystem lfs = null;
        FileLock lock = null;
        try {
            //get relative-path-key map from FDL
            File dir = fileLayout.getDirectoryForModule(target);
            System.out.println("Distributing files for "+target+" to "+dir);
            File parent = dir.getParentFile();
            dir.mkdirs();
            lfs = new LocalFileSystem();
            lfs.setRootDirectory(parent);
            Repository.getDefault().addFileSystem(lfs);
            FileObject destRoot = lfs.findResource(dir.getName());
            System.out.println("destRoot="+destRoot.getPath());
            
            // create target FOs map keyed by relative paths
            java.util.Enumeration destFiles = destRoot.getChildren (true);
            Map destMap = new HashMap();
            int rootPathLen = destRoot.getPath ().length ();
            for (; destFiles.hasMoreElements (); ) {
                FileObject destFO = (FileObject) destFiles.nextElement ();
                //System.out.println("Dest : relpath="+relPaths[0]+" file="+files[0]);
                destMap.put(destFO.getPath ().substring (rootPathLen), destFO);
            }
            
            // iterate through source changes
            for (Iterator j=source; j.hasNext();) {
                J2eeModule.RootedEntry entry = (J2eeModule.RootedEntry) j.next();
                String relativePath = entry.getRelativePath();
                FileObject sourceFO = entry.getFileObject ();
                FileObject targetFO = (FileObject) destMap.get(relativePath);
                FileObject destFolder;
                if (targetFO == null) {
                    System.out.println("Target for "+sourceFO.getPath()+" not exists, ensure destination containing folder ready");
                    destFolder = createFolderFor(destRoot, relativePath);
                } else {
                    // remove from map to form of to-remove-target-list
                    destMap.remove(relativePath);
                    
                    //check timestamp
                    if (! sourceFO.lastModified().after(targetFO.lastModified())) {
                        System.out.println("Skipping file "+sourceFO.getPath());
                        continue;
                    }
                    destFolder = targetFO.getParent();
                    
                    System.out.println("Prepare for copy by deleting old target file: "+targetFO.getPath());
                    targetFO.delete();
                }
                mc.record(relativePath);
                //PENDING: update progress object or should we not for performance?
                System.out.println("Copying "+sourceFO.getPath()+" to "+destFolder.getPath());
                
                FileUtil.copyFile(sourceFO, destFolder, sourceFO.getName());
            }
            
            // Cleanup destination
            for (Iterator k=destMap.values().iterator(); k.hasNext();) {
                FileObject remainingFO = (FileObject) k.next();
                lock = remainingFO.lock();
                System.out.println("Deleting remaining file "+remainingFO.getPath());
                remainingFO.delete(lock);
                System.out.println("File "+remainingFO.getPath()+" deleted");
                lock.releaseLock();
                lock = null;
            }
            
            // copying serverconfiguration files if changed
            String[] rPaths = fileLayout.getDeploymentPlanFilenames(target);
            File configFile = dtarget.getConfigurationFile();
            if (rPaths == null || rPaths.length == 0 || configFile.lastModified() <= lastDeployTime)
                return mc;
            
            System.out.println("configFile.lastModified="+configFile.lastModified()+" lastDeployTime="+lastDeployTime);
            File[] paths = new File[rPaths.length];
            for (int n=0; n<rPaths.length; n++) {
                paths[n] = new File(FileUtil.toFile(destRoot), rPaths[n]);
            }
            DeploymentConfigurationProvider dcp = dtarget.getDeploymentConfigurationProvider();
            DeploymentConfiguration config = dcp.getDeploymentConfiguration();
            DeployableObject deployable = dcp.getDeployableObject(moduleUrl);
            splitter.writeDeploymentPlanFiles(config, deployable, paths);
            mc.record(rPaths[0]);
            
            return mc;
            
        } catch (Exception e) {
            e.printStackTrace();
            String msg = NbBundle.getMessage(ServerFileDistributor.class, "MSG_IncrementalDeployFailed", e);
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
    
    private FileObject createFolderFor(FileObject dest, String relativePath) throws IOException {
        File parentRelativePath = (new File(relativePath)).getParentFile();
        if (parentRelativePath == null) 
            return dest;
        
        FileObject folder = dest.getFileSystem().findResource(parentRelativePath.getPath());
        if (folder == null)
            folder = FileUtil.createFolder(dest, parentRelativePath.getPath());
        
        System.out.println("Created folder "+folder.getPath());
        return folder;
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

    
    static class AppChanges implements AppChangeDescriptor {
        
        boolean descriptorChanged = false;
        boolean serverDescriptorChanged = false;
        boolean classesChanged = false;
        boolean manifestChanged = false;
        boolean ejbsChanged = false;
        List changedEjbs = Collections.EMPTY_LIST;
        
        List descriptorRelativePaths;
        List serverDescriptorRelativePaths;
        AppChanges() {
        }
        AppChanges(List descriptorRelativePaths, List serverDescriptorRelativePaths) {
            this.descriptorRelativePaths = descriptorRelativePaths;
            this.serverDescriptorRelativePaths = serverDescriptorRelativePaths;
        }
        private void record(AppChanges changes) {
            if (descriptorChanged == false) descriptorChanged = changes.descriptorChanged();
            if (serverDescriptorChanged == false) serverDescriptorChanged = changes.serverDescriptorChanged();
            if (classesChanged == false) classesChanged = changes.classesChanged();
            if (manifestChanged == false) manifestChanged = changes.manifestChanged();
            if (ejbsChanged == false) ejbsChanged = changes.ejbsChanged();
            List ejbs = Arrays.asList(changes.getChangedEjbs());
            if (ejbs.size() > 0)
                changedEjbs.addAll(ejbs);
        }
        
        private void record(String relativePath) {
            if (! classesChanged && relativePath.endsWith(".class")) { //NOI18N
                classesChanged = true;
                return;
            }
            if (! descriptorChanged && descriptorRelativePaths != null && descriptorRelativePaths.contains(relativePath)) {
                descriptorChanged = true;
                System.out.println("Fist descriptor change is from: "+relativePath);
                return;
            }
            if (! serverDescriptorChanged && serverDescriptorRelativePaths != null && serverDescriptorRelativePaths.contains(relativePath)) {
                serverDescriptorChanged = true;
                System.out.println("Fist server descriptor change is from: "+relativePath);
                return;
            }
        }
        private void record(ModuleChangeReporter mcr, long since) {
            if (! ejbsChanged) {
                EjbChangeDescriptor ecd = mcr.getEjbChanges(since);
                ejbsChanged = ecd.ejbsChanged();
                String[] ejbs = ecd.getChangedEjbs();
                if (ejbs != null && ejbs.length > 0)
                    changedEjbs.addAll(Arrays.asList(ejbs));
            }
            if (! manifestChanged)
                manifestChanged = mcr.isManifestChanged(since);
        }
        public boolean classesChanged() { return classesChanged; }
        public boolean descriptorChanged() { return descriptorChanged; }
        public boolean manifestChanged() { return manifestChanged; }
        public boolean serverDescriptorChanged() { return serverDescriptorChanged; }
        
        public boolean ejbsChanged() { return ejbsChanged; }
        public String[] getChangedEjbs() {
            return (String[]) changedEjbs.toArray(new String[]{});
        }
    }
}

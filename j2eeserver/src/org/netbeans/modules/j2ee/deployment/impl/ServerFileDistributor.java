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

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;
import java.io.*;
import java.util.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.common.api.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.spi.status.*;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentTarget;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentConfigurationProvider;

/**
 *
 * @author  nn136682
 */
public class ServerFileDistributor extends ServerProgress {
    ServerInstance instance;
    DeploymentTarget dtarget;
    IncrementalDeployment incremental;
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
        incremental = instance.getIncrementalDeployment ();
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
                    Iterator contents = childModules[i].getArchiveContents();
                    if (contents != null)
                        childModuleFiles.put(childModules[i].getUrl(), contents);
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
    
    private J2eeModule getJ2eeModule(TargetModuleID target) {
        if (target.getParentTargetModuleID() == null)
            return dtarget.getModule();
        else {
            String moduleUrl = incremental.getModuleUrl(target);
            return (J2eeModule) childModuleMap.get(moduleUrl);
        }
    }
    
    private AppChanges createModuleChangeDescriptor(TargetModuleID target) {
        J2eeModule module = getJ2eeModule(target);
        List descriptorRelativePaths = getDescriptorPath(module);
        
        ModuleType moduleType = (ModuleType) module.getModuleType ();
        List serverDescriptorRelativePaths = Arrays.asList(instance.getServer().getDeploymentPlanFiles(moduleType));
        return new AppChanges(descriptorRelativePaths, serverDescriptorRelativePaths, (ModuleType) dtarget.getModule ().getModuleType ());
    }
    
    public AppChangeDescriptor distribute(TargetModule targetModule, ModuleChangeReporter mcr) throws IOException {
        long lastDeployTime = targetModule.getTimestamp();
        TargetModuleID[] childModules = targetModule.getChildTargetModuleID();
        AppChanges changes = new AppChanges();
        File destDir = null;
        
            //PENDING: whether module need to be stop first
            for (int i=0; childModules != null && i<childModules.length; i++) {
                String url = incremental.getModuleUrl(targetModule.delegate());
                destDir = incremental.getDirectoryForModule(childModules[i]);
                Iterator source = (Iterator) childModuleFiles.get(url);
                if (destDir == null)
                    changes.record(_distribute(childModules[i], lastDeployTime));
                else
                    changes.record(_distribute(source, destDir, targetModule.delegate(), url, lastDeployTime));
            }
            
            //PENDING: whether ordering of copying matters
            destDir = incremental.getDirectoryForModule(targetModule.delegate());
            if (destDir == null)
                changes.record(_distribute(targetModule.delegate(), lastDeployTime));
            else
                changes.record(_distribute(rootModuleFiles, destDir, targetModule.delegate(), null, lastDeployTime));
            
            if (mcr != null)
                changes.record(mcr, lastDeployTime);
            
            setStatusDistributeCompleted(NbBundle.getMessage(
                ServerFileDistributor.class, "MSG_DoneIncrementalDeploy", targetModule.getModuleID()));
            
        return changes;
    }
    
    public AppChanges _test_distribute(Iterator source, File destDir, TargetModuleID target, long lastDeployTime) throws IOException {
        return _distribute(source, destDir, target, null, lastDeployTime);
    }
    
    private AppChanges _distribute(TargetModuleID target,  long lastDeployTime) throws IOException {
        AppChanges mc = createModuleChangeDescriptor(target);
        setStatusDistributeRunning(NbBundle.getMessage(
        ServerFileDistributor.class, "MSG_RunningIncrementalDeploy", target));
        
        Iterator content = getJ2eeModule(target).getArchiveContents ();
        
        Date lastDeployed = new Date(lastDeployTime);
        while (content.hasNext ()) {
            J2eeModule.RootedEntry re = (J2eeModule.RootedEntry) content.next ();
            FileObject file = re.getFileObject ();
            if (file.isFolder())
                continue;
            //jar file are created externally and timestamp may not be refreshed
            file.refresh ();
            if (file.lastModified().after(lastDeployed)) {
                String relativePath = re.getRelativePath ();
                mc.record(relativePath);
            }
        }

        return mc;
    }
    
    private AppChanges _distribute(Iterator source, File destDir, TargetModuleID target, String moduleUrl, long lastDeployTime) throws IOException {
        AppChanges mc = createModuleChangeDescriptor(target);
        if (source == null) {
            ErrorManager.getDefault().log(ErrorManager.ERROR, "There is no contents for "+target); //NOI18N
            throw new IOException(NbBundle.getMessage(ServerFileDistributor.class, "MSG_NoContents", target));
        }
        setStatusDistributeRunning(NbBundle.getMessage(
        ServerFileDistributor.class, "MSG_RunningIncrementalDeploy", target));
        FileLock lock = null;
        try {
            //get relative-path-key map from FDL
            File dir = incremental.getDirectoryForModule(target);
            destDir.mkdirs();
            File parent = destDir.getParentFile();
            FileObject destRoot = FileUtil.toFileObject(destDir);
            
            // create target FOs map keyed by relative paths
            java.util.Enumeration destFiles = destRoot.getChildren(true);
            Map destMap = new HashMap();
            int rootPathLen = destRoot.getPath().length();
            for (; destFiles.hasMoreElements(); ) {
                FileObject destFO = (FileObject) destFiles.nextElement();
                destMap.put(destFO.getPath().substring(rootPathLen + 1), destFO);
            }
            
            // iterate through source changes
            for (Iterator j=source; j.hasNext();) {
                J2eeModule.RootedEntry entry = (J2eeModule.RootedEntry) j.next();
                String relativePath = entry.getRelativePath();
                FileObject sourceFO = entry.getFileObject();
                FileObject targetFO = (FileObject) destMap.get(relativePath);
                FileObject destFolder;
                if (sourceFO.isFolder()) {
                    destMap.remove(relativePath);
                    //System.out.println("entering folder:"+relativePath);
                    continue;
                }
                if (targetFO == null) {
                    destFolder = findOrCreateParentFolder(destRoot, relativePath);
                } else {
                    // remove from map to form of to-remove-target-list
                    destMap.remove(relativePath);
                    
                    //check timestamp
                    if (! sourceFO.lastModified().after(targetFO.lastModified())) {
                        //System.out.println("Skipping file "+sourceFO.getPath());
                        continue;
                    }
                    destFolder = targetFO.getParent();
                    
                    //System.out.println("Prepare for copy by deleting old target file: "+targetFO.getPath());
                    targetFO.delete();
                }
                mc.record(relativePath);
                //PENDING: update progress object or should we not for performance?
                //System.out.println("Copying "+sourceFO.getPath()+" to "+destFolder.getPath());
                
                FileUtil.copyFile(sourceFO, destFolder, sourceFO.getName());
            }
            
            ModuleType moduleType = (ModuleType) dtarget.getModule ().getModuleType ();
            String[] rPaths = instance.getServer().getDeploymentPlanFiles(moduleType);
            /*for (int n=0; n < rPaths.length; n++) {
                FileObject removedFO = (FileObject)destMap.remove(rPaths[n]);
                System.out.println("Sparing plan file: "+rPaths[n]+" removedFO="+removedFO);
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
            }*/
            
            // copying serverconfiguration files if changed
            File configFile = dtarget.getConfigurationFile();
            if (rPaths == null || rPaths.length == 0)
                return mc;
            
            File[] paths = new File[rPaths.length];
            for (int n=0; n<rPaths.length; n++) {
                paths[n] = new File(FileUtil.toFile(destRoot), rPaths[n]);
                if (! paths[n].exists() || paths[n].lastModified() < configFile.lastModified())
                    mc.record(rPaths[n]);
            }
            
            if (mc.serverDescriptorChanged()) {
                DeploymentConfigurationProvider dcp = dtarget.getDeploymentConfigurationProvider();
                DeploymentConfiguration config = dcp.getDeploymentConfiguration();
                DeployableObject deployable = dcp.getDeployableObject(moduleUrl);
                splitter.writeDeploymentPlanFiles(config, deployable, paths);
            }
            
            return mc;
            
        } catch (Exception e) {
            String msg = NbBundle.getMessage(ServerFileDistributor.class, "MSG_IncrementalDeployFailed", e);
            setStatusDistributeFailed(msg);
            throw new RuntimeException(e);
        } finally {
            if (lock != null) {
                try { lock.releaseLock(); } catch(Exception ex) {}
            }
        }
    }
    
    /**
     * Find or create parent folder of a file given its root and its relative path.
     * The target file does not need to exist.
     *
     * @param dest FileObject for the root of the target file
     * @param relativePaht relative path of the target file
     * @return the FileObject for the parent folder target file.
     */
    public static FileObject findOrCreateParentFolder(FileObject dest, String relativePath) throws IOException {
        File parentRelativePath = (new File(relativePath)).getParentFile();
        if (parentRelativePath == null)
            return dest;
        
        FileObject folder = dest.getFileSystem().findResource(parentRelativePath.getPath());
        if (folder == null)
            folder = FileUtil.createFolder(dest, parentRelativePath.getPath());
        
        //System.out.println("Created folder "+folder.getPath());
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
        ModuleType moduleType = null;
        List changedFiles = new ArrayList();
        List descriptorRelativePaths;
        List serverDescriptorRelativePaths;
        AppChanges() {
        }
        AppChanges(List descriptorRelativePaths, List serverDescriptorRelativePaths, ModuleType moduleType) {
            this.descriptorRelativePaths = descriptorRelativePaths;
            this.serverDescriptorRelativePaths = serverDescriptorRelativePaths;
            this.moduleType = moduleType;
        }
        private void record(AppChanges changes) {
            if (!descriptorChanged) descriptorChanged = changes.descriptorChanged();
            if (!serverDescriptorChanged) serverDescriptorChanged = changes.serverDescriptorChanged();
            if (!classesChanged) classesChanged = changes.classesChanged();
            if (!manifestChanged) manifestChanged = changes.manifestChanged();
            if (!ejbsChanged) ejbsChanged = changes.ejbsChanged();
            List ejbs = Arrays.asList(changes.getChangedEjbs());
            if (ejbs.size() > 0)
                changedEjbs.addAll(ejbs);
            changedFiles.addAll(changes.changedFiles);
        }
        
        private void record(String relativePath) {
            if (! classesChanged) {
                boolean classes = !moduleType.equals (ModuleType.WAR) || relativePath.startsWith ("WEB-INF/classes/"); //NOI18N
                boolean importantLib = !moduleType.equals (ModuleType.WAR) || relativePath.startsWith ("WEB-INF/lib/"); //NOI18N
                boolean libs = importantLib && (relativePath.endsWith(".jar") || relativePath.endsWith(".zip")); //NOI18N
                if (classes || libs) {
                    classesChanged = true;
                    return;
                }
            }
            if (! descriptorChanged && (
                ((descriptorRelativePaths != null && descriptorRelativePaths.contains(relativePath)) ||
                 (relativePath.startsWith ("WEB-INF") && (relativePath.endsWith (".tld") || relativePath.endsWith (".xml") || relativePath.endsWith (".dtd")))))) {
                descriptorChanged = true;
                return;
            }
            if (! serverDescriptorChanged && serverDescriptorRelativePaths != null && serverDescriptorRelativePaths.contains(relativePath)) {
                serverDescriptorChanged = true;
                return;
            }
            changedFiles.add(new File(relativePath));
        }
        
        private void record(ModuleChangeReporter mcr, long since) {
            EjbChangeDescriptor ecd = mcr.getEjbChanges(since);
            ejbsChanged = ecd.ejbsChanged();
            String[] ejbs = ecd.getChangedEjbs();
            if (ejbs != null && ejbs.length > 0)
                changedEjbs.addAll(Arrays.asList(ejbs));
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
        public File[] getChangedFiles() {
            return (File[]) changedFiles.toArray(new File[changedFiles.size()]);
        }
    }
}

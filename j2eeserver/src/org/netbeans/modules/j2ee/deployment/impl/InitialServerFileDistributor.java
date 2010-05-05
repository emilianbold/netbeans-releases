/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.j2ee.deployment.impl;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.Locale;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentTarget;
import org.netbeans.modules.j2ee.deployment.execution.ModuleConfigurationProvider;
import javax.enterprise.deploy.spi.Target;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.util.NbBundle;
import javax.enterprise.deploy.shared.CommandType;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.openide.util.Utilities;

/**
 *
 * @author  nn136682
 */
public class InitialServerFileDistributor extends ServerProgress {
    
    private static final Logger LOGGER = Logger.getLogger(InitialServerFileDistributor.class.getName());
    
    private final ServerString serverString;
    private final DeploymentTarget dtarget;
    private final IncrementalDeployment incDeployment;
    private final Target target;
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
            LOGGER.log(Level.INFO, null, e);
            setStatusDistributeFailed(e.getMessage());
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

        try {
            // mkdirs()/toFileObject is not not tolerated any more.
            FileObject destRoot = FileUtil.createFolder(dir);
            
            FileObject[] garbages = destRoot.getChildren();
            for (int i=0; i<garbages.length; i++) {
                try {
                    garbages[i].delete();
                } catch (java.io.IOException ioe) {
                    LOGGER.log(Level.FINER, null, ioe);
                    if (Utilities.isWindows()) {                        
                        String ext = garbages[i].getExt().toLowerCase(Locale.ENGLISH);
                        if ("jar".equals(ext) || "zip".equals(ext)) {
                            zeroOutArchive(garbages[i]);
                        } else {
                            throw ioe;
                        }
                    } else {
                        throw ioe;
                    }
                }
            }
            
            while(rootedEntries.hasNext()) {
                J2eeModule.RootedEntry entry = (J2eeModule.RootedEntry) rootedEntries.next();
                String relativePath = entry.getRelativePath();
                FileObject sourceFO = entry.getFileObject();
                FileObject destFolder = ServerFileDistributor.findOrCreateParentFolder(destRoot, relativePath);
                if (sourceFO.isData ()) {
                    copyFile(sourceFO, dir, relativePath);
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.FINER, null, e);
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

    // Make this method speedie quick... since folks can have large
    // projects, but expect the IDE to be as fast or faster that zip or jar
    //
    private void copyFile(FileObject sourceObject, File directory, String relativePath) throws IOException {  
        String ext = sourceObject.getExt();
        if (sourceObject.getSize() == 0 &&
                ("zip".equals(ext) || "jar".equals(ext))) { // NOI18N
            // a zero length jar or zip file is NEVER ok...
            return;
        }
        File destFile = new File(directory, relativePath);
        FileOutputStream os = new FileOutputStream(destFile);
        FileInputStream fis = null;
        InputStream is = null;
        FileChannel in = null;
        FileChannel out = null;
        try {
            File sourceFile = FileUtil.toFile(sourceObject);
            if (null != sourceFile && sourceFile.canRead()) {
                // we are coming from a readable file
                fis = new FileInputStream(sourceFile);
                in = fis.getChannel();
                out = os.getChannel();
                
                long fileSize = sourceFile.length();
                long bufSize = Math.min(65536,fileSize);
                long offset = 0;
                
                do {
                    offset += in.transferTo(offset, bufSize, out);
                } while (offset < fileSize);
            } else {
                is = sourceObject.getInputStream();
                FileUtil.copy(is, os);
            }
        } finally {
            if (null != out) { try { out.close(); } catch (IOException ioe) {} }
            if (null != in) {try { in.close(); } catch (IOException ioe) {} }
            if (null != is) { try { is.close(); } catch (IOException ioe) {} }
            if (null != fis) { try { fis.close(); } catch (IOException ioe) {} }
            if (null != os) { try { os.close(); } catch (IOException ioe) {} }
         }
    }

    private void zeroOutArchive(FileObject garbage) throws IOException, FileAlreadyLockedException {
        OutputStream fileToOverwrite = garbage.getOutputStream();
        try {
            JarOutputStream jos = new JarOutputStream(fileToOverwrite);
            try {
                jos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF")); // NOI18N
                // UTF-8 guaranteed on any platform
                jos.write("Manifest-Version: 1.0\n".getBytes("UTF-8")); // NOI18N
            } finally {
                jos.close();
            }
        } finally {
            fileToOverwrite.close();
        }
    }
}

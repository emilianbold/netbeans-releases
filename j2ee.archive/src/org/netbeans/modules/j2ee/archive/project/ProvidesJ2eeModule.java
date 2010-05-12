/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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


package org.netbeans.modules.j2ee.archive.project;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

public class ProvidesJ2eeModule extends J2eeModuleProvider {
    
    private J2eeModuleProvider inner;
    
    private J2eeModule innerModule;
    
    private AntProjectHelper helper;
    
    private ArchiveProject project;
    
    ProvidesJ2eeModule(AntProjectHelper helper, ArchiveProject proj) {
        this.helper = helper;
        inner = null;
        project = proj;
    }
    
    public void setInner(J2eeModuleProvider inner) {
        this.inner = inner;
        innerModule = J2eeModuleFactory.createJ2eeModule(new InnerModule(inner.getJ2eeModule()));
    }
    
    /**  Return name to be used in deployment for the module.
     * @return name to be used for the module
     */
    @Override
    public String getDeploymentName() {
        return project.getName();
    }
    
    @Override
    public J2eeModule getJ2eeModule() {
        return innerModule;
        
    }
    
    @Override
    public ModuleChangeReporter getModuleChangeReporter() {
        ModuleChangeReporter retVal = null;
        if (null != inner) {
            retVal = inner.getModuleChangeReporter();
        }
        return retVal;
    }
    
    static private final String CONF="conf";
    
    public File getDeploymentConfigurationFile(final String name) {
        File retVal;
        final String dir = (String)project.getArchiveProjectProperties().
                get(ArchiveProjectProperties.PROXY_PROJECT_DIR);
        final File proxyProjectDir =
                FileUtil.toFile(helper.getProjectDirectory().getFileObject(dir));
        retVal = new File(proxyProjectDir,
                SRC_LIT + File.separator + CONF + File.separator + name);  // NOI18N
        return retVal;
    }
    
    private static final String SRC_LIT = "src";                                //NOI18N
    
    public FileObject findDeploymentConfigurationFile(String name) {
        FileObject retVal = null;
        String dir = (String)project.getArchiveProjectProperties().
                get(ArchiveProjectProperties.PROXY_PROJECT_DIR);
        
        try {
            FileObject parent;
            if ("sun-application.xml".equals(name)) {                       // NOI18N
                retVal = helper.getProjectDirectory().getFileObject(dir).
                        getFileObject(SRC_LIT).getFileObject(CONF).       // NOI18N
                        getFileObject(name);
                if (retVal == null) {
                    parent = helper.getProjectDirectory().getFileObject(dir).
                            getFileObject(SRC_LIT).getFileObject(CONF);   // NOI18N
                    retVal = makeDescriptorFromTemplate(parent,name);
                }
            }
            if ("sun-ra.xml".equals(name)) {                                // NOI18N
                retVal = helper.getProjectDirectory().getFileObject(dir).
                        getFileObject(SRC_LIT).getFileObject(CONF).       // NOI18N
                        getFileObject(name);
                if (null == retVal) {
                    retVal = helper.getProjectDirectory().getFileObject(dir).
                            getFileObject(SRC_LIT).getFileObject(name);
                }
                if (retVal == null) {
                    parent = helper.getProjectDirectory().getFileObject(dir).
                            getFileObject(SRC_LIT);
                    retVal = makeDescriptorFromTemplate(parent,name);
                }
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
        }
        return retVal;
    }
    
    private FileObject makeDescriptorFromTemplate(/*final String dir, */ final FileObject parent, final String name) throws IOException {
        FileObject retVal;
        final InputStream is = ProvidesJ2eeModule.class.getResourceAsStream("template-"+name); // NOI18N;
        try {
            FileSystem fs = parent.getFileSystem();
            fs.runAtomicAction(new AtomicCreate(is, parent, name)); //FileSystem.AtomicAction() {
            retVal = parent.getFileObject(name);
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ioe);
                }
            }
        }
        return retVal;
    }
    
    private static class AtomicCreate implements FileSystem.AtomicAction {
        
        private InputStream is;
        private String name;
        private FileObject parent;
        AtomicCreate(InputStream is, FileObject parent, String name) {
            this.is = is;
            this.name = name;
            this.parent = parent;
        }
        @Override
        public void run() throws IOException {
            FileLock flock = null;
            OutputStream os = null;
            FileObject retVal = null;
            try {
                retVal = parent.createData(name);
                flock = retVal.lock();
                os = retVal.getOutputStream(flock);
                FileUtil.copy(is,os);
            } finally {
                if (null != flock) {
                    flock.releaseLock();
                }
                if (is == null || os == null) {
                    // the write probably did not happen.. so delete the file
                    // if it is there.
                    if (null != retVal) {
                        try {
                            retVal.delete();
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                        }
                    }
                }
                if (null != os) {
                    try {
                        os.close();
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ioe);
                    }
                }
                
            }
        }
    }
    
    @Override
    public void setServerInstanceID(String severInstanceID) {
        if (null != inner) {
            inner.setServerInstanceID(severInstanceID);
        }
    }
    
    @Override
    public String getServerInstanceID() {
        String retVal;// = super.getServerInstanceID();
        if (null != inner) {
            retVal = inner.getServerInstanceID();
        } else {
            retVal = helper.getStandardPropertyEvaluator().getProperty(ArchiveProjectProperties.J2EE_SERVER_INSTANCE);
        }
        return retVal;
    }
    
    @Override
    public String getServerID() {
        String inst = getServerInstanceID();
        String retVal = null;
        if (inst != null) {
            String id = null;
            try {
                id = Deployment.getDefault().getServerInstance(inst).getServerID();
            } catch (InstanceRemovedException ex) {
                Logger.getLogger("global").log(Level.INFO, inst, ex);
            }
            if (id != null) {
                retVal = id;
            }
        }
        return null!=retVal?retVal:helper.getStandardPropertyEvaluator().getProperty(ArchiveProjectProperties.J2EE_SERVER_TYPE);
    }
    
    private FileObject getFileObject(String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        FileObject retVal = null;
        if (prop != null) {
            retVal = helper.resolveFileObject(prop);
        }
        return retVal;
    }
    
    /**
     * Returns directory containing definition for enterprise resources needed for
     * the module execution; return null if not supported
     */
    public File getEnterpriseResourceDirectory() {
        File retValue = null;
        // TODO vbk -- should have a listener on this file and use it to update
        //   a property in the project.  not today zurg!
        try {
            FileObject fo = FileUtil.createFolder(helper.getProjectDirectory(),ArchiveProjectProperties.SETUP_DIR_VALUE);
            retValue = FileUtil.toFile(fo);
        } catch (IOException ex) {
            ErrorManager.getDefault().log(helper.getProjectDirectory().getPath());
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return retValue;
    }
    
    void setJ2eeModule(J2eeModule j2eeModule) {
        innerModule = j2eeModule;
    }
    
    private class InnerModule implements J2eeModuleImplementation2 {
        
        private J2eeModule inner;
        
        InnerModule(J2eeModule inner) {
            this.inner = inner;
        }
        
        @Override
        public String getModuleVersion() {
            return inner.getModuleVersion();
        }
        
        @Override
        public J2eeModule.Type getModuleType() {
            return inner.getType();
        }
        
        @Override
        public String getUrl() {
            return inner.getUrl();
        }
        
        @Override
        public FileObject getArchive() throws IOException {
            return getFileObject("dist.archive");           // NOI18N
        }
        
        // TODO - this is not correct. But it works. Investigate.
        @Override
        public Iterator getArchiveContents() throws IOException {
            return inner.getArchiveContents();
        }
        
        @Override
        public FileObject getContentDirectory() throws IOException {
            return null;
        }
        
        // TODO MetadataModel:
        @Override
        public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
            return inner.getMetadataModel(type);
        }
        
        @Override
        public File getResourceDirectory() {
            return inner.getResourceDirectory();
        }
        
        @Override
        public File getDeploymentConfigurationFile(String name) {
            return inner.getDeploymentConfigurationFile(name);
        }
        
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            inner.addPropertyChangeListener(listener);
        }
        
        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            inner.removePropertyChangeListener(listener);
        }
        
    }
}

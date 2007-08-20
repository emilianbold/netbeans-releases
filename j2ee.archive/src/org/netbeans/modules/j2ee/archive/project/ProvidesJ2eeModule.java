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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.archive.project;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.NotImplementedException;

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
    
    public J2eeModule getJ2eeModule() {
        return innerModule;
        
    }
    
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
    
    public void setServerInstanceID(String severInstanceID) {
        if (null != inner) {
            inner.setServerInstanceID(severInstanceID);
        }
    }
    
    public String getServerInstanceID() {
        String retVal;// = super.getServerInstanceID();
        if (null != inner) {
            retVal = inner.getServerInstanceID();
        } else {
            retVal = helper.getStandardPropertyEvaluator().getProperty(ArchiveProjectProperties.J2EE_SERVER_INSTANCE);
        }
        return retVal;
    }
    
    public String getServerID() {
        String inst = getServerInstanceID();
        String retVal = null;
        if (inst != null) {
            String id = Deployment.getDefault().getServerID(inst);
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
    
    private class InnerModule implements J2eeModuleImplementation {
        
        private J2eeModule inner;
        
        InnerModule(J2eeModule inner) {
            this.inner = inner;
        }
        
        public String getModuleVersion() {
            return inner.getModuleVersion();
        }
        
        public Object getModuleType() {
            return inner.getModuleType();
        }
        
        public String getUrl() {
            return inner.getUrl();
        }
        
        public FileObject getArchive() throws IOException {
            return getFileObject("dist.archive");           // NOI18N
        }
        
        // TODO - this is not correct. But it works. Investigate.
        public Iterator getArchiveContents() throws IOException {
            return inner.getArchiveContents();
        }
        
        public FileObject getContentDirectory() throws IOException {
            return null;
        }
        
        // TODO MetadataModel:
        public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
            throw new NotImplementedException();
        }
        
        public File getResourceDirectory() {
            return inner.getResourceDirectory();
        }
        
        public File getDeploymentConfigurationFile(String name) {
            return inner.getDeploymentConfigurationFile(name);
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            inner.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            inner.removePropertyChangeListener(listener);
        }
        
    }
}

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

package org.netbeans.modules.j2ee.clientproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.AppClientProjectProperties;
//import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.client.DDProvider;
//import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.CarImplementation;
import org.netbeans.modules.schema2beans.BaseBean;
//import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.api.client.WebServicesClientConstants;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
//import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * @author jungi
 */
public final class AppClientProvider extends J2eeModuleProvider
        implements CarImplementation, J2eeModule, ModuleChangeReporter, EjbChangeDescriptor, PropertyChangeListener {
    
    public static final String FILE_DD = "application-client.xml";//NOI18N
    
    private final AppClientProject project;
    private final AntProjectHelper helper;
    private Set<J2eeModule.VersionListener> versionListeners;
    
    private long notificationTimeout = 0; // used to suppress repeating the same messages
    
    AppClientProvider(AppClientProject project, AntProjectHelper helper) {
        this.project = project;
        this.helper = helper;
        project.evaluator().addPropertyChangeListener(this);
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject metaInfFo = getMetaInf();
        if (metaInfFo == null) {
            return null;
        }
        return metaInfFo.getFileObject(FILE_DD);
    }
    
    public FileObject[] getJavaSources() {
        return project.getSourceRoots().getRoots();
    }
    
    public FileObject getMetaInf() {
        FileObject metaInf = getFileObject(AppClientProjectProperties.META_INF);
        if (metaInf == null) {
            String version = project.getAPICar().getJ2eePlatformVersion();
            if (needConfigurationFolder(version)) {
                String relativePath = helper.getStandardPropertyEvaluator().getProperty(AppClientProjectProperties.META_INF);
                String path = (relativePath != null ? helper.resolvePath(relativePath) : "");
                showErrorMessage(NbBundle.getMessage(AppClientProvider.class, "MSG_MetaInfCorrupted", project.getName(), path));
            }
        }
        return metaInf;
    }
    
    /** Package-private for unit test only. */
    static boolean needConfigurationFolder(final String version) {
        return AppClientProjectProperties.J2EE_1_3.equals(version) ||
                AppClientProjectProperties.J2EE_1_4.equals(version);
    }
    
    public File getMetaInfAsFile() {
        return getFile(AppClientProjectProperties.META_INF);
    }
    
    public File getEnterpriseResourceDirectory() {
        return getFile(AppClientProjectProperties.RESOURCE_DIR);
    }
    
    public FileObject findDeploymentConfigurationFile(String name) {
        FileObject metaInf = getMetaInf();
        if (metaInf == null) {
            return null;
        }
        return metaInf.getFileObject(name);
    }
    
    public File getDeploymentConfigurationFile(String name) {
        return new File(getMetaInfAsFile(), name);
    }
    
    public ClassPathProvider getClassPathProvider() {
        return (ClassPathProvider) project.getLookup().lookup(ClassPathProvider.class);
    }
    
    public FileObject getArchive() {
        return getFileObject(AppClientProjectProperties.DIST_JAR);
    }
    
    private FileObject getFileObject(String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.resolveFileObject(prop);
        }
        
        return null;
    }
    
    private File getFile(String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.resolveFile(prop);
        }
        return null;
    }
    
    public J2eeModule getJ2eeModule() {
        return this;
    }
    
    public ModuleChangeReporter getModuleChangeReporter() {
        return this;
    }
    
    public boolean useDefaultServer() {
        return true;
    }
    
    public String getServerID() {
        return helper.getStandardPropertyEvaluator().getProperty(AppClientProjectProperties.J2EE_SERVER_TYPE);
    }
    
    public String getServerInstanceID() {
        return helper.getStandardPropertyEvaluator().getProperty(AppClientProjectProperties.J2EE_SERVER_INSTANCE);
    }
    
    public void setServerInstanceID(String serverInstanceID) {
        assert serverInstanceID != null : "passed serverInstanceID cannot be null";
        AppClientProjectProperties.setServerInstance(project, helper, serverInstanceID);
    }
    
    public Iterator getArchiveContents() throws IOException {
        return new IT(getContentDirectory());
    }
    
    public FileObject getContentDirectory() {
        return getFileObject(AppClientProjectProperties.BUILD_CLASSES_DIR);
    }
    
    public FileObject getBuildDirectory() {
        return getFileObject(AppClientProjectProperties.BUILD_DIR);
    }
    
    public File getContentDirectoryAsFile() {
        return getFile(AppClientProjectProperties.BUILD_CLASSES_DIR);
    }
    
    public BaseBean getDeploymentDescriptor(String location) {
        if (J2eeModule.CLIENT_XML.equals(location)){
            AppClient appClient = Utils.getAppClient(project);
            if (appClient != null) {
                //PENDING find a better way to get the BB from WApp and remove the HACK from DDProvider!!
                return DDProvider.getDefault().getBaseBean(appClient);
            }
        }/* else if(J2eeModule.EJBSERVICES_XML.equals(location)){
            Webservices webServices = getWebservices();
            if(webServices != null){
                return org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.getDefault().getBaseBean(webServices);
            }
        }*/
        return null;
    }
    /*
    private Webservices getWebservices() {
        if (Util.isJavaEE5orHigher(project)) {
            WebServicesSupport wss = WebServicesSupport.getWebServicesSupport(project.getProjectDirectory());
            try {
                return org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.getDefault().getMergedDDRoot(wss);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        } else {
            FileObject wsdd = getDD();
            if(wsdd != null) {
                try {
                    return org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.getDefault()
                            .getDDRoot(getDD());
                } catch (IOException e) {
                    ErrorManager.getDefault().log(e.getLocalizedMessage());
                }
            }
        }
        
        return null;
    }
    */
    public FileObject getDD() {
        FileObject metaInfFo = getMetaInf();
        if (metaInfFo==null) {
            return null;
        }
        return metaInfFo.getFileObject(WebServicesClientConstants.WEBSERVICES_DD, "xml"); // NOI18N
    }
    
    public EjbChangeDescriptor getEjbChanges(long timestamp) {
        return this;
    }
    
    public Object getModuleType() {
        return J2eeModule.CLIENT;
    }
    
    public String getModuleVersion() {
        AppClient ac = Utils.getAppClient(project);
        return (ac == null) ? AppClient.VERSION_1_4 /* fallback */ : ac.getVersion().toString();
    }
    
    private Set<J2eeModule.VersionListener> versionListeners() {
        if (versionListeners == null) {
            versionListeners = new HashSet<J2eeModule.VersionListener>();
            AppClient appClient = Utils.getAppClient(project);
            if (appClient != null) {
                PropertyChangeListener l = (PropertyChangeListener) WeakListeners.create(PropertyChangeListener.class, this, appClient);
                appClient.addPropertyChangeListener(l);
            }
        }
        return versionListeners;
    }
    
    public void addVersionListener(J2eeModule.VersionListener vl) {
        versionListeners().add(vl);
    }
    
    public void removeVersionListener(J2eeModule.VersionListener vl) {
        if (versionListeners != null) {
            versionListeners.remove(vl);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(AppClient.PROPERTY_VERSION)) {
            for (J2eeModule.VersionListener vl : versionListeners) {
                String oldVersion = (String) evt.getOldValue();
                String newVersion = (String) evt.getNewValue();
                vl.versionChanged(oldVersion, newVersion);
            }
        } else if (evt.getPropertyName().equals(AppClientProjectProperties.J2EE_SERVER_INSTANCE)) {
            Deployment d = Deployment.getDefault();
            String oldServerID = evt.getOldValue() == null ? null : d.getServerID((String) evt.getOldValue());
            String newServerID = evt.getNewValue() == null ? null : d.getServerID((String) evt.getNewValue());
            fireServerChange(oldServerID, newServerID);
        }  else if (AppClientProjectProperties.RESOURCE_DIR.equals(evt.getPropertyName())) {
            String oldValue = (String)evt.getOldValue();
            String newValue = (String)evt.getNewValue();
            firePropertyChange(
                    PROP_ENTERPRISE_RESOURCE_DIRECTORY,
                    oldValue == null ? null : new File(oldValue),
                    newValue == null ? null : new File(newValue));
        }
    }
    
    public String getUrl() {
        EditableProperties ep =  helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String name = ep.getProperty(AppClientProjectProperties.JAR_NAME);
        return name == null ? "" : ('/' + name);
    }
    
    public boolean isManifestChanged(long timestamp) {
        return false;
    }
    
    public void setUrl(String url) {
        throw new UnsupportedOperationException("Cannot customize URL of Application Client module"); // NOI18N
    }
    
    public boolean ejbsChanged() {
        return false;
    }
    
    public String[] getChangedEjbs() {
        return new String[] {};
    }
    
    public String getJ2eePlatformVersion() {
        return helper.getStandardPropertyEvaluator().getProperty(AppClientProjectProperties.J2EE_PLATFORM);
    }
    
    public FileObject[] getSourceRoots() {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        List<FileObject> roots = new LinkedList<FileObject>();
        FileObject metaInf = getMetaInf();
        if (metaInf != null) {
            roots.add(metaInf);
        }
        for (int i = 0; i < groups.length; i++) {
            roots.add(groups[i].getRootFolder());
        }
        FileObject[] rootArray = new FileObject[roots.size()];
        return roots.toArray(rootArray);
    }
    
    private void showErrorMessage(final String message) {
        // only display the messages if the project is open
        if(new Date().getTime() > notificationTimeout && isProjectOpen()) {
            // DialogDisplayer waits for the AWT thread, blocking the calling
            // thread -- deadlock-prone, see issue #64888. therefore invoking
            // only in the AWT thread
            Runnable r = new Runnable() {
                public void run() {
                    if (!SwingUtilities.isEventDispatchThread()) {
                        SwingUtilities.invokeLater(this);
                    } else {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                    }
                }
            };
            r.run();
            
            // set timeout to suppress the same messages during next 20 seconds (feel free to adjust the timeout
            // using more suitable value)
            notificationTimeout = new Date().getTime() + 20000;
        }
    }
    
    private boolean isProjectOpen() {
        // OpenProjects.getDefault() is null when this method is called upon
        // IDE startup from the project's impl of ProjectOpenHook
        if (OpenProjects.getDefault() != null) {
            Project[] projects = OpenProjects.getDefault().getOpenProjects();
            for (int i = 0; i < projects.length; i++) {
                if (projects[i].equals(project)) {
                    return true;
                }
            }
            return false;
        } else {
            // be conservative -- don't know anything about the project
            // so consider it open
            return true;
        }
    }
    
    private static class IT implements Iterator {
        
        Enumeration ch;
        FileObject root;
        
        private IT(FileObject f) {
            this.ch = f.getChildren(true);
            this.root = f;
        }
        
        public boolean hasNext() {
            return ch.hasMoreElements();
        }
        
        public Object next() {
            FileObject f = (FileObject) ch.nextElement();
            return new FSRootRE(root, f);
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    private static final class FSRootRE implements J2eeModule.RootedEntry {
        
        FileObject f;
        FileObject root;
        
        FSRootRE(FileObject root, FileObject f) {
            this.f = f;
            this.root = root;
        }
        
        public FileObject getFileObject() {
            return f;
        }
        
        public String getRelativePath() {
            return FileUtil.getRelativePath(root, f);
        }
        
    }
    
}

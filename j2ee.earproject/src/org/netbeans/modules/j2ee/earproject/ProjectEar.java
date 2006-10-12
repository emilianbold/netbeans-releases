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

package org.netbeans.modules.j2ee.earproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModuleContainer;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeAppProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 * An enterprise application project's j2eeserver implementation
 *
 * @see ProjectEar
 * @author  vince kraemer
 */
public final class ProjectEar extends J2eeAppProvider
        implements
        J2eeModule,
        ModuleChangeReporter,
        EjbChangeDescriptor,
        PropertyChangeListener,
        EarImplementation,
        J2eeModuleContainer {
    
    public static final String FILE_DD        = "application.xml";//NOI18N
    
    private final EarProject project;
    private Set<J2eeModule.VersionListener> versionListeners;
    
    ProjectEar (EarProject project) { // ], AntProjectHelper helper) {
        this.project = project;
        AntProjectHelper helper = project.getAntProjectHelper();
        helper.getStandardPropertyEvaluator().addPropertyChangeListener(this);
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject dd = null;
        FileObject metaInf = getMetaInf();
        if (metaInf != null) {
            dd = metaInf.getFileObject(FILE_DD);
            if (dd == null) {
                try {
                    dd = EarProjectGenerator.setupDD(J2eeModule.JAVA_EE_5, metaInf, project);
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                }
            }
        }
        return dd;
    }
    
    public FileObject getMetaInf() {
        return project.getOrCreateMetaInfDir();
    }
    
    public File getEnterpriseResourceDirectory() {
        return project.getFile(EarProjectProperties.RESOURCE_DIR);
    }

    public ClassPathProvider getClassPathProvider () {
        return (ClassPathProvider) project.getLookup ().lookup (ClassPathProvider.class);
    }
    
    public FileObject getArchive () {
        return project.getFileObject (EarProjectProperties.DIST_JAR); //NOI18N
    }
    
    public J2eeModule getJ2eeModule () {
        return this;
    }
    
    public ModuleChangeReporter getModuleChangeReporter () {
        return this;
    }
    
    public boolean useDefaultServer () {
        return false;
    }
    
    public String getServerID () {
        return project.getServerID(); //helper.getStandardPropertyEvaluator ().getProperty (EarProjectProperties.J2EE_SERVER_TYPE);
    }
    
    public void setServerInstanceID(String severInstanceID) {
        // TODO: implement when needed
    }

    public String getServerInstanceID () {
        return project.getServerInstanceID(); //helper.getStandardPropertyEvaluator ().getProperty (EarProjectProperties.J2EE_SERVER_INSTANCE);
    }
    
    public Iterator getArchiveContents () throws IOException {
        return new IT (getContentDirectory ());
    }

    public FileObject getContentDirectory() {
        return project.getFileObject (EarProjectProperties.BUILD_DIR); //NOI18N
    }

    public FileObject getBuildDirectory() {
        return project.getFileObject (EarProjectProperties.BUILD_DIR); //NOI18N
    }

    public BaseBean getDeploymentDescriptor (String location) {
        if (! J2eeModule.APP_XML.equals(location)) {
            return null;
        }
        
        Application earApp = getApplication();
        if (earApp != null) {
            //PENDING find a better way to get the BB from WApp and remove the HACK from DDProvider!!
            return DDProvider.getDefault ().getBaseBean (earApp);
        }
        return null;
    }

    private Application getApplication () {
        try {
            return DDProvider.getDefault ().getDDRoot (getDeploymentDescriptor ());
        } catch (IOException e) {
            ErrorManager.getDefault ().log (e.getLocalizedMessage ());
        }
        return null;

    }
    
    public EjbChangeDescriptor getEjbChanges (long timestamp) {
        return this;
    }

    public Object getModuleType () {
        return J2eeModule.EAR;
    }

    public String getModuleVersion () {
        Application app = getApplication();
        return (app == null) ? Application.VERSION_5 /* fallback */ : app.getVersion().toString();
    }

    private Set<J2eeModule.VersionListener> versionListeners() {
        if (versionListeners == null) {
            versionListeners = new HashSet<J2eeModule.VersionListener>();
            Application app = getApplication();
            if (app != null) {
                PropertyChangeListener l = (PropertyChangeListener) WeakListeners.create(PropertyChangeListener.class, this, app);
                app.addPropertyChangeListener(l);
            }
        }
        return versionListeners;
    }

    public void addVersionListener(J2eeModule.VersionListener vl) {
        try {
            versionListeners().add(vl);
        } catch (UnsupportedOperationException uoe) {
            // XXX ignoring a UOE
        }
    }

    public void removeVersionListener(J2eeModule.VersionListener vl) {
        if (versionListeners != null) {
            versionListeners.remove(vl);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Application.PROPERTY_VERSION)) {
            for (J2eeModule.VersionListener vl : versionListeners) {
                String oldVersion = (String) evt.getOldValue();
                String newVersion = (String) evt.getNewValue();
                vl.versionChanged(oldVersion, newVersion);
            }
        } else if (EarProjectProperties.J2EE_SERVER_INSTANCE.equals(evt.getPropertyName())) {
            Deployment d = Deployment.getDefault();
            String oldServerID = evt.getOldValue() == null ? null : d.getServerID((String)evt.getOldValue ());
            String newServerID = evt.getNewValue() == null ? null : d.getServerID((String)evt.getNewValue ());
            fireServerChange (oldServerID, newServerID);
        } else if (EarProjectProperties.RESOURCE_DIR.equals(evt.getPropertyName())) {
            String oldValue = (String)evt.getOldValue();
            String newValue = (String)evt.getNewValue();
            firePropertyChange(
                    PROP_ENTERPRISE_RESOURCE_DIRECTORY, 
                    oldValue == null ? null : new File(oldValue),
                    newValue == null ? null : new File(newValue));
        }
    }
        
    public String getUrl () {
        return "";
    }

    public boolean isManifestChanged (long timestamp) {
        return false;
    }

    public void setUrl (String url) {
        throw new UnsupportedOperationException ("Cannot customize URL of web module"); // NOI18N
    }

    public boolean ejbsChanged () {
        return false;
    }

    public String[] getChangedEjbs () {
        return new String[] {};
    }

    public String getJ2eePlatformVersion () {
        return project.getJ2eePlatformVersion(); // helper.getStandardPropertyEvaluator ().getProperty (EarProjectProperties.J2EE_PLATFORM);
    }
    
    public FileObject[] getSourceRoots() {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject[] roots = new FileObject[groups.length+1];
        roots[0] = getMetaInf();
        for (int i=0; i < groups.length; i++) {
            roots[i+1] = groups[i].getRootFolder();
        }
        
        return roots; 
    }
 
    private static class IT implements Iterator {
        Enumeration ch;
        FileObject root;
        
        private IT (FileObject f) {
            this.ch = f.getChildren (true);
            this.root = f;
        }
        
        public boolean hasNext () {
            return ch.hasMoreElements ();
        }
        
        public Object next () {
            FileObject f = (FileObject) ch.nextElement ();
            return new FSRootRE (root, f);
        }
        
        public void remove () {
            throw new UnsupportedOperationException ();
        }
        
    }

    private static final class FSRootRE implements J2eeModule.RootedEntry {
        FileObject f;
        FileObject root;
        
        FSRootRE (FileObject root, FileObject f) {
            this.f = f;
            this.root = root;
        }
        
        public FileObject getFileObject () {
            return f;
        }
        
        public String getRelativePath () {
            return FileUtil.getRelativePath (root, f);
        }
    }
    
    private Map<String, J2eeModuleProvider> mods = new HashMap<String, J2eeModuleProvider>();
    
    void setModules(Map<String, J2eeModuleProvider> mods) {
        if (null == mods) {
            throw new IllegalArgumentException("mods"); // NOI18N
        }
        this.mods = mods;
    }
    
    public J2eeModule[] getModules(ModuleListener ml) {
        if (null != ml) {
            addModuleListener(ml);
        }
        J2eeModule[] retVal = new J2eeModule[mods.size()];
        int i = 0;
        for (J2eeModuleProvider provider : mods.values()) {
            retVal[i++] = provider.getJ2eeModule();
        }
        return retVal;
    }
    
    public void addModuleProvider(J2eeModuleProvider jmp, String uri) {
        mods.put(uri, jmp);
        J2eeModule jm = jmp.getJ2eeModule();
        fireAddModule(jm);
    }
    
    public void removeModuleProvider(J2eeModuleProvider jmp, String uri) {
        // J2eeModuleProvider tmp = (J2eeModuleProvider) mods.get(uri);
        // if (!tmp.equals(jmp)) {
            // something fishy may be happening here
            // XXX log it
        // }
        J2eeModule jm = jmp.getJ2eeModule();
        fireRemoveModule(jm);
        mods.remove(uri);
    }
    
    private final List<ModuleListener> modListeners = new ArrayList<ModuleListener>();
    
    private void fireAddModule(J2eeModule jm) {
        for (ModuleListener ml : modListeners) {
            try {
                ml.addModule(jm);
            } catch (RuntimeException rex) {
                ErrorManager.getDefault().log(rex.getLocalizedMessage());                
            }
        }
    }

    private void fireRemoveModule(J2eeModule jm) {
        for (ModuleListener ml : modListeners) {
            try {
                ml.removeModule(jm);
            } catch (RuntimeException rex) {
                ErrorManager.getDefault().log(rex.getLocalizedMessage());                
            }
        }
    }
    
    public void addModuleListener(ModuleListener ml) {
        modListeners.add(ml);
    }
    
    public void removeModuleListener(ModuleListener ml){
        modListeners.remove(ml);
    }
    
    /**
     * Returns the provider for the child module specified by given URI.
     * @param uri the child module URI within the J2EE application.
     * @return J2eeModuleProvider object
     */
    public J2eeModuleProvider getChildModuleProvider(String uri) {
        return mods.get(uri);
    }
    
    /**
     * Returns list of providers of every child J2EE module of this J2EE app.
     * @return array of J2eeModuleProvider objects.
     */
    public  J2eeModuleProvider[] getChildModuleProviders() {
        return (J2eeModuleProvider[]) mods.values().toArray(new J2eeModuleProvider[mods.size()]);
    }
    
    public File getDeploymentConfigurationFile(String name) {
        FileObject moduleFolder = getMetaInf();
        File configFolder = FileUtil.toFile(moduleFolder);
        return new File(configFolder, name);
    }

    public FileObject findDeploymentConfigurationFile (String name) {
        FileObject moduleFolder = getMetaInf();
        return moduleFolder == null ? null : moduleFolder.getFileObject(name);
    }
    
    public void addEjbJarModule(EjbJar module) {
        FileObject childFO = module.getDeploymentDescriptor();
        if (childFO == null) {
            childFO = module.getMetaInf();
        }
        addModule(childFO);
    }
    
    public void addWebModule(WebModule module) {
        FileObject childFO = module.getDeploymentDescriptor();
        if (childFO == null) {
            childFO = module.getWebInf();
        }
        addModule(childFO);
    }
    
    public void addCarModule(Car module) {
        FileObject childFO = module.getDeploymentDescriptor();
        if (childFO == null) {
            childFO = module.getMetaInf();
        }
        addModule(childFO);
    }

    private void addModule(final FileObject childFO) {
        Project owner = null;
        if (childFO != null) {
            owner = FileOwnerQuery.getOwner(childFO);
        }
        if (owner == null) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
                    "Unable to add module to the Enterpise Application. Owner project not found."); // NOI18N
        } else {
            ((EarProjectProperties)project.getProjectProperties()).addJ2eeSubprojects(new Project [] {owner});
        }
    }
    
}

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
//import org.netbeans.api.project.*;
//import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeAppProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation;
import org.netbeans.modules.web.api.webmodule.WebModule;
//import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModuleContainer;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleListener;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
//import org.netbeans.modules.j2ee.common.ui.customizer.ArchiveProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathProvider;
//import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation;
import org.openide.filesystems.FileUtil;
import org.xml.sax.SAXException;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/** An enterprise application project's j2eeserver implementation
 *
 * @see ProjectWeb
 * @author  vince kraemer
 */
public final class ProjectEar extends J2eeAppProvider  
  implements 
//    EarImplementation, 
    J2eeModule, 
    ModuleChangeReporter,
    EjbChangeDescriptor, 
    PropertyChangeListener,
    EarImplementation, 
    J2eeModuleContainer {
      
    public static final String FILE_DD        = "application.xml";//NOI18N
    
    private EarProject project;
//    private AntProjectHelper helper;
    private Set versionListeners = null;
    
    ProjectEar (EarProject project) { // ], AntProjectHelper helper) {
        this.project = project;
//        this.helper = helper;
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject metaInfFo = getMetaInf();
        if (metaInfFo==null) {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(NbBundle.getMessage(ProjectEar.class,"MSG_WebInfCorrupted"),
                                             NotifyDescriptor.ERROR_MESSAGE));
            return null;
        }
        return getMetaInf ().getFileObject (FILE_DD);
    }

    /*public String getContextPath () {
        return getConfigSupport ().getWebContextRoot ();
    }
    
    public void setContextPath (String path) {
        getConfigSupport ().setWebContextRoot (path);
    }
    */
    
    public ClassPath getJavaSources () {
        ClassPathProvider cpp = (ClassPathProvider) project.getLookup ().lookup (ClassPathProvider.class);
        if (cpp != null) {
            return cpp.findClassPath (project.getFileObject (EarProjectProperties.SRC_DIR), ClassPath.SOURCE);
        }
        return null;
    }
    
    public FileObject getMetaInf () {
        //throw new java.lang.UnsupportedOperationException("not ready yet");
        return project.getFileObject (EarProjectProperties.META_INF);
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
    
/*    private FileObject getFileObject(String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.resolveFileObject(prop);
        } else {
            return null;
        }
    }
    
    private File getFile(String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.resolveFile(prop);
        } else {
            return null;
        }
    }
 **/
    
    public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule getJ2eeModule () {
        return this;
    }
    
    public org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter getModuleChangeReporter () {
        return this;
    }
    
//    public FileObject getModuleFolder () {
//        //throw new java.lang.UnsupportedOperationException("not ready yet");
//        return getDocumentBase ();
//    }
    
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
    
    public Iterator getArchiveContents () throws java.io.IOException {
        return new IT (getContentDirectory ());
    }

    public FileObject getContentDirectory() {
        return project.getFileObject (EarProjectProperties.BUILD_DIR); //NOI18N
    }

    public FileObject getBuildDirectory() {
        return project.getFileObject (EarProjectProperties.BUILD_DIR); //NOI18N
    }

/*    public File getContentDirectoryAsFile() {
        return getFile (EarProjectProperties.BUILD_DIR); //NOI18N
    }*/

    public org.netbeans.modules.schema2beans.BaseBean getDeploymentDescriptor (String location) {
        if (! J2eeModule.APP_XML.equals(location))
            return null;
        
        Application webApp = getApplication();
        if (webApp != null) {
            //PENDING find a better way to get the BB from WApp and remove the HACK from DDProvider!!
            return DDProvider.getDefault ().getBaseBean (webApp);
        }
        return null;
    }

    private Application getApplication () {
        //throw new UnsupportedOperationException("getWebApp");
        try {
            return DDProvider.getDefault ().getDDRoot (getDeploymentDescriptor ());
        } catch (java.io.IOException e) {
            org.openide.ErrorManager.getDefault ().log (e.getLocalizedMessage ());
        }
        return null;

    }
    
    public org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor getEjbChanges (long timestamp) {
        return this;
    }

    public Object getModuleType () {
        return J2eeModule.EAR;
    }

    public String getModuleVersion () {
        Application app = getApplication ();
        return app.getVersion ().toString();
    }

    private Set versionListeners() {
        if (versionListeners == null) {
            versionListeners = new HashSet();
            Application app = getApplication();
            if (app != null) {
                PropertyChangeListener l = (PropertyChangeListener) org.openide.util.WeakListeners.create(PropertyChangeListener.class, this, app);
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
        if (versionListeners != null)
            versionListeners.remove(vl);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(org.netbeans.modules.j2ee.dd.api.application.Application.PROPERTY_VERSION)) {
            for (Iterator i=versionListeners.iterator(); i.hasNext();) {
                J2eeModule.VersionListener vl = (J2eeModule.VersionListener) i.next();
                String oldVersion = (String) evt.getOldValue();
                String newVersion = (String) evt.getNewValue();
                vl.versionChanged(oldVersion, newVersion);
            }
        }
    }
        
    public String getUrl () {
        return "";
    }

    public boolean isManifestChanged (long timestamp) {
        return false;
    }

    public void setUrl (String url) {
        throw new UnsupportedOperationException ("Cannot customize URL of web module");
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
    
//    private Set versionListeners() {
//        if (versionListeners == null) {
//            versionListeners = new HashSet();
//            WebApp webApp = getWebApp ();
//            if (webApp != null) {
//                PropertyChangeListener l = (PropertyChangeListener) org.openide.util.WeakListener.create(PropertyChangeListener.class, this, webApp);
//                webApp.addPropertyChangeListener(l);
//            }
//        }
//        return versionListeners;
//    }
//
//    public void addVersionListener(J2eeModule.VersionListener vl) {
//        versionListeners().add(vl);
//    }
//
//    public void removeVersionListener(J2eeModule.VersionListener vl) {
//        if (versionListeners != null)
//            versionListeners.remove(vl);
//    }
//
//    public void propertyChange(PropertyChangeEvent evt) {
//        if (evt.getPropertyName().equals(org.netbeans.modules.j2ee.dd.api.web.WebApp.PROPERTY_VERSION)) {
//            for (Iterator i=versionListeners.iterator(); i.hasNext();) {
//                J2eeModule.VersionListener vl = (J2eeModule.VersionListener) i.next();
//                String oldVersion = (String) evt.getOldValue();
//                String newVersion = (String) evt.getNewValue();
//                vl.versionChanged(oldVersion, newVersion);
//            }
//        }
//    }
    
    private static class IT implements Iterator {
        java.util.Enumeration ch;
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
    
    private Map mods = new HashMap();
    
    void setModules(Map mods) {
        if (null == mods) {
            throw new IllegalArgumentException("mods");
        }
        this.mods = mods;
    }
    
    public J2eeModule[] getModules(ModuleListener ml) {
//        return (J2eeModule[]) mods.toArray(new J2eeModule[mods.size()]);
        if (null != ml) {
            addModuleListener(ml);
        }
        J2eeModule[] retVal = new J2eeModule[mods.size()];
        Iterator iter = mods.values().iterator();
        int i = 0;
        while (iter.hasNext()) {
            retVal[i] = ((J2eeModuleProvider)iter.next()).getJ2eeModule();
            i++;
        }
        return retVal;
    }
    
    public void addModuleProvider(J2eeModuleProvider jmp, String uri) {
        mods.put(uri, jmp);
        J2eeModule jm = jmp.getJ2eeModule();
        fireAddModule(jm);
    }
    
    public void removeModuleProvider(J2eeModuleProvider jmp, String uri) {
        J2eeModuleProvider tmp = (J2eeModuleProvider) mods.get(uri);
        if (!tmp.equals(jmp)) {
            // something fishy may be happening here
            // XXX log it
        }
        J2eeModule jm = jmp.getJ2eeModule();
        fireRemoveModule(jm);
    }
    
    private List modListeners = new ArrayList();
    
    private void fireAddModule(J2eeModule jm) {
        for (int i = 0; i < modListeners.size(); i++) {
            ModuleListener ml = (ModuleListener) modListeners.get(i);
            try {
                ml.addModule(jm);
            } catch (RuntimeException rex) {
                org.openide.ErrorManager.getDefault().log(rex.getLocalizedMessage());                
            }
        }
    }


    private void fireRemoveModule(J2eeModule jm) {
        for (int i = 0; i < modListeners.size(); i++) {
            ModuleListener ml = (ModuleListener) modListeners.get(i);
            try {
                ml.removeModule(jm);
            } catch (RuntimeException rex) {
                org.openide.ErrorManager.getDefault().log(rex.getLocalizedMessage());                
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
        return (J2eeModuleProvider) mods.get(uri);
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
        return moduleFolder.getFileObject(name);
    }
    
    public void addEjbJarModule(EjbJar module) {
        Project p = FileOwnerQuery.getOwner(module.getDeploymentDescriptor());
        ((EarProjectProperties)project.getProjectProperties()).addJ2eeSubprojects(new Project [] {p});
    }
    
    public void addWebModule(WebModule module) {
        Project p = FileOwnerQuery.getOwner(module.getDeploymentDescriptor());
        ((EarProjectProperties)project.getProjectProperties()).addJ2eeSubprojects(new Project [] {p});
    }
}

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

package org.netbeans.modules.web.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.*;
import org.netbeans.api.web.dd.DDProvider;
import org.netbeans.api.web.dd.WebApp;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.openide.filesystems.FileUtil;
import org.xml.sax.SAXException;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/** A web module implementation on top of project.
 *
 * @author  Pavel Buzek
 */
public final class ProjectWebModule extends J2eeModuleProvider 
  implements WebModuleImplementation, J2eeModule, ModuleChangeReporter, 
  EjbChangeDescriptor, PropertyChangeListener {
      
    public static final String FOLDER_WEB_INF = "WEB-INF";//NOI18N
    public static final String FOLDER_CLASSES = "classes";//NOI18N
    public static final String FOLDER_LIB     = "lib";//NOI18N
    public static final String FILE_DD        = "web.xml";//NOI18N
    
    private WebProject project;
    private AntProjectHelper helper;
    private Set versionListeners = null;
    private String fakeServerInstId = null; // used to get access to properties of other servers
    
    ProjectWebModule (WebProject project, AntProjectHelper helper) {
        this.project = project;
        this.helper = helper;
        project.evaluator ().addPropertyChangeListener (this);
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject webInfFo = getWebInf();
        if (webInfFo==null) {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(NbBundle.getMessage(ProjectWebModule.class,"MSG_WebInfCorrupted"),
                                             NotifyDescriptor.ERROR_MESSAGE));
            return null;
        }
        return getWebInf ().getFileObject (FILE_DD);
    }

    public String getContextPath () {
        return getConfigSupport ().getWebContextRoot ();
    }
    
    public void setContextPath (String path) {
        getConfigSupport ().setWebContextRoot (path);
    }
    
    public String getContextPath (String serverInstId) {
        fakeServerInstId = serverInstId;
        String result = getConfigSupport ().getWebContextRoot ();
        fakeServerInstId = null;
        return result;
    }
    
    public void setContextPath (String serverInstId, String path) {
        fakeServerInstId = serverInstId;
        getConfigSupport ().setWebContextRoot (path);
        fakeServerInstId = null;
    }
    
    public FileObject getDocumentBase () {
        return getFileObject("web.docbase.dir"); // NOI18N
    }
    
    public ClassPath getJavaSources () {
        ClassPathProvider cpp = (ClassPathProvider) project.getLookup ().lookup (ClassPathProvider.class);
        if (cpp != null) {
            return cpp.findClassPath (getFileObject ("src.dir"), ClassPath.SOURCE);
        }
        return null;
    }
    
    public FileObject getWebInf () {
        return getDocumentBase ().getFileObject (FOLDER_WEB_INF);
    }
    
    public ClassPathProvider getClassPathProvider () {
        return (ClassPathProvider) project.getLookup ().lookup (ClassPathProvider.class);
    }
    
    public FileObject getArchive () {
        return getFileObject ("dist.war"); //NOI18N
    }
    
    private FileObject getFileObject(String propname) {
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
    
    public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule getJ2eeModule () {
        return this;
    }
    
    public org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter getModuleChangeReporter () {
        return this;
    }
    
    public FileObject getModuleFolder () {
        return getDocumentBase ();
    }
    
    public boolean useDefaultServer () {
        return false;
    }
    
    public String getServerID () {
        String inst = getServerInstanceID ();
        if (inst != null) {
            String id = Deployment.getDefault().getServerID(inst);
            if (id != null) {
                return id;
            }
        }
        return helper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.J2EE_SERVER_TYPE);
    }

    public String getServerInstanceID () {
        if (fakeServerInstId != null)
            return fakeServerInstId;
        return helper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.J2EE_SERVER_INSTANCE);
    }
    
    public Iterator getArchiveContents () throws java.io.IOException {
        return new IT (getContentDirectory ());
    }

    public FileObject getContentDirectory() {
        return getFileObject ("build.web.dir"); //NOI18N
    }

    public FileObject getBuildDirectory() {
        return getFileObject ("build.dir"); //NOI18N
    }

    public File getContentDirectoryAsFile() {
        return getFile ("build.web.dir"); //NOI18N
    }

    public org.netbeans.modules.schema2beans.BaseBean getDeploymentDescriptor (String location) {
        if (! J2eeModule.WEB_XML.equals(location))
            return null;

        WebApp webApp = getWebApp ();
        if (webApp != null) {
            //PENDING find a better way to get the BB from WApp and remove the HACK from DDProvider!!
            return DDProvider.getDefault ().getBaseBean (webApp);
        }
        return null;
    }

    private WebApp getWebApp () {
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
        return J2eeModule.WAR;
    }

    public String getModuleVersion () {
        WebApp wapp = getWebApp ();
        return wapp.getVersion ();
    }

    private Set versionListeners() {
        if (versionListeners == null) {
            versionListeners = new HashSet();
            org.netbeans.api.web.dd.WebApp webApp = getWebApp();
            if (webApp != null) {
                PropertyChangeListener l = (PropertyChangeListener) org.openide.util.WeakListeners.create(PropertyChangeListener.class, this, webApp);
                webApp.addPropertyChangeListener(l);
            }
        }
        return versionListeners;
    }

    public void addVersionListener(J2eeModule.VersionListener vl) {
        versionListeners().add(vl);
    }

    public void removeVersionListener(J2eeModule.VersionListener vl) {
        if (versionListeners != null)
            versionListeners.remove(vl);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(org.netbeans.api.web.dd.WebApp.PROPERTY_VERSION)) {
            for (Iterator i=versionListeners.iterator(); i.hasNext();) {
                J2eeModule.VersionListener vl = (J2eeModule.VersionListener) i.next();
                String oldVersion = (String) evt.getOldValue();
                String newVersion = (String) evt.getNewValue();
                vl.versionChanged(oldVersion, newVersion);
            }
        } else if (evt.getPropertyName ().equals (WebProjectProperties.J2EE_SERVER_TYPE)) {
            fireServerChange ((String) evt.getOldValue (), (String) evt.getNewValue ());
        } else if (evt.getPropertyName ().equals (WebProjectProperties.J2EE_SERVER_INSTANCE)) {
            Deployment d = Deployment.getDefault ();
            String oldServerID = evt.getOldValue () == null ? null : d.getServerID ((String) evt.getOldValue ());
            String newServerID = evt.getNewValue () == null ? null : d.getServerID ((String) evt.getNewValue ());
            fireServerChange (oldServerID, newServerID);
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
        return helper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.J2EE_PLATFORM);
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
//        if (evt.getPropertyName().equals(org.netbeans.api.web.dd.WebApp.PROPERTY_VERSION)) {
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
}

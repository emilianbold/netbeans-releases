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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.earproject.UpdateHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.j2ee.common.ui.customizer.ArchiveProjectProperties;
import org.netbeans.modules.j2ee.common.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.ProjectEar;
import org.netbeans.modules.j2ee.dd.api.application.*;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.nodes.Node;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import java.util.Arrays;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;

/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 * 
 * @author Petr Hrebejk
 */
public class EarProjectProperties extends ArchiveProjectProperties {
    
    private EarProject earProject;
    private UpdateHelper updateHelper;
    
    /**
     * Holds value of property bogus.
     */
    private String bogus;
    
    /**
     * Utility field used by bound properties.
     */
    private java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport (this);
    public static final String CLIENT_MODULE_URI = "client.module.uri"; //NOI18N
    
    public EarProjectProperties(Project project, UpdateHelper updateHelper, PropertyEvaluator eval, ReferenceHelper refHelper, AntBasedProjectType abpt) {
        super(project,updateHelper,refHelper, abpt);
        this.updateHelper = updateHelper;
        earProject = (EarProject) project;                
    }
    
    public EarProjectProperties(EarProject project, ReferenceHelper refHelper, AntBasedProjectType abpt ) {
        this (project, project.getUpdateHelper(), project.getUpdateHelper().getAntProjectHelper().getStandardPropertyEvaluator(), refHelper, abpt );
    }
    
    public EarProjectProperties(Project project, AntProjectHelper aph, ReferenceHelper refHelper, AntBasedProjectType abtp) {
        this((EarProject)project, refHelper, abtp );
    }
        
    protected void updateContentDependency(Set deleted, Set added) {
        Application app = null;
        try {
            app = DDProvider.getDefault().getDDRoot(earProject.getAppModule().getDeploymentDescriptor());
        } catch (java.io.IOException ioe) {
            org.openide.ErrorManager.getDefault().log(ioe.getLocalizedMessage());
        }
        if (null != app) {
            // delete the old entries out of the application
            Iterator iter = deleted.iterator();
            while (iter.hasNext()) {
                VisualClassPathItem vcpi = (VisualClassPathItem) iter.next();
                removeItemFromAppDD(app,vcpi);
            }
            // add the new stuff "back"
            iter = added.iterator();
            while (iter.hasNext()) {
                VisualClassPathItem vcpi = (VisualClassPathItem) iter.next();
                addItemToAppDD(app,vcpi);
            }
            try {
                app.write(earProject.getAppModule().getDeploymentDescriptor());
            } catch (java.io.IOException ioe) {
                org.openide.ErrorManager.getDefault().log(ioe.getLocalizedMessage());
            }
            
        }
    }
    
    private void removeItemFromAppDD(Application dd, VisualClassPathItem vcpi) {
        String path = vcpi.getCompletePathInArchive();
        Module m = searchForModule(dd,path);
        if (null != m) {
            dd.removeModule(m);
            setClientModuleUri("");
            Object obj = vcpi.getObject();
            AntArtifact aa;
            Project p;
            if (obj instanceof AntArtifact) {
                aa = (AntArtifact) obj;
                p = aa.getProject();
            } else {
                return;
            }
            J2eeModuleProvider jmp = (J2eeModuleProvider) p.getLookup().lookup(J2eeModuleProvider.class);
            if (null != jmp) {
                J2eeModule jm = jmp.getJ2eeModule();
                if (null != jm)
                    earProject.getAppModule().removeModuleProvider(jmp,path);
            }
                return;
            }
        }
    
    private Module searchForModule(Application dd, String path) {
        Module mods[] = dd.getModule();
        int len = 0;
        if (null != mods)
            len = mods.length;
        for (int i = 0; i < len; i++) {
            String val = mods[i].getEjb();
            if (null != val && val.equals(path))
                return mods[i];
            val = mods[i].getConnector();
            if (null != val && val.equals(path))
                return mods[i];
            val = mods[i].getJava();
            if (null != val && val.equals(path))
                return mods[i];
            Web w = mods[i].getWeb();
            val = null;
            if ( null != w)
                val = w.getWebUri();
            if (null != val && val.equals(path))
                return mods[i];
        }
        return null;
    }
    
    private void addItemToAppDD(Application dd, VisualClassPathItem vcpi) {
        Object obj = vcpi.getObject();
        AntArtifact aa;
        Project p;
        String path = vcpi.getCompletePathInArchive(); //   computePath(vcpi);
        Module mod = null;
        if (obj instanceof AntArtifact) {
            mod = getModFromAntArtifact((AntArtifact) obj, dd, path);
        }
        else if (obj instanceof File) {
            mod = getModFromFile((File) obj, dd, path);
        }
        if (mod != null && mod.getWeb() != null)
            replaceEmptyClientModuleUri(path);
        Module prevMod = searchForModule(dd, path);
        if (null == prevMod && null != mod)
            dd.addModule(mod);
    }
    
    
    private Module getModFromAntArtifact(AntArtifact aa, Application dd, String path) {
        Project p = aa.getProject();
        Module mod = null;
        try {
            J2eeModuleProvider jmp = (J2eeModuleProvider) p.getLookup().lookup(J2eeModuleProvider.class);
            if (null != jmp) {
                jmp.setServerInstanceID(earProject.getServerInstanceID());
                J2eeModule jm = jmp.getJ2eeModule();
                if (null != jm) {
                    earProject.getAppModule().addModuleProvider(jmp,path);
                } else {
                    return null;
                }
                mod = (Module) dd.createBean("Module");
                if (jm.getModuleType() == J2eeModule.EJB) {
                    mod.setEjb(path); // NOI18N
                }
                else if (jm.getModuleType() == J2eeModule.WAR) {
                    Web w = (Web) mod.newWeb(); // createBean("Web");
                    w.setWebUri(path);
                    org.openide.filesystems.FileObject tmp = aa.getScriptFile();
                    if (null != tmp)
                        tmp = tmp.getParent().getFileObject("web/WEB-INF/web.xml"); // NOI18N
                    WebModule wm = null;
                    if (null != tmp)
                        wm = (WebModule) WebModule.getWebModule(tmp);
                    if (null != wm) {
                        w.setContextRoot(wm.getContextPath());
                    }
                    else {
                        int endex = path.length() - 4;
                        if (endex < 1) {
                            endex = path.length();
                        }
                        w.setContextRoot(path.substring(0,endex));
                    }
                     mod.setWeb(w);
                }
                else if (jm.getModuleType() == J2eeModule.CONN) {
                    mod.setConnector(path);
                }
                else if (jm.getModuleType() == J2eeModule.CLIENT) {
                    mod.setJava(path);
                }
            }
        }
        catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            org.openide.ErrorManager.getDefault ().log (cnfe.getLocalizedMessage ());
        }
        return mod;
    }
    
    private void setClientModuleUri(String newVal) {
        put(EarProjectProperties.CLIENT_MODULE_URI,newVal);        
    }
    
    private void replaceEmptyClientModuleUri(String path) {
        // set the context path if it is not set...
        Object foo = get(EarProjectProperties.CLIENT_MODULE_URI);
        if (null == foo) {
            setClientModuleUri(path);
        }
        if (foo instanceof String) {
            String bar = (String) foo;
            if (bar.length() < 1) {
                setClientModuleUri(path);
            }
        }
        
    }
    
    private Module getModFromFile(File f, Application dd, String path) {
            JarFile jar = null;
            Module mod = null;
            try {
                jar= new JarFile((File) f);
                JarEntry ddf = jar.getJarEntry("META-INF/ejb-jar.xml"); // NOI18N
                if (null != ddf) {
                    mod = (Module) dd.createBean("Module"); // NOI18N
                    mod.setEjb(path);
                }
                ddf = jar.getJarEntry("META-INF/ra.xml"); // NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean("Module"); //NOI18N
                    mod.setConnector(path);                    
                } else if (null != ddf && null != mod) {
                    return null; // two timing jar file.
                }
                ddf = jar.getJarEntry("META-INF/application-client.xml"); //NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean("Module"); // NOI18N
                    mod.setJava(path);                    
                } else if (null != ddf && null != mod) {
                    return null; // two timing jar file.
                }
                ddf = jar.getJarEntry("WEB-INF/web.xml"); //NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean("Module"); // NOI18N
                    Web w = (Web) mod.newWeb(); 
                    w.setWebUri(path);
                        int endex = path.length() - 4;
                        if (endex < 1) {
                            endex = path.length();
                        }
                        w.setContextRoot("/"+path.substring(0,endex)); // NOI18N
                    mod.setWeb(w);
                } else if (null != ddf && null != mod) {
                    return null; // two timing jar file.
                }
                ddf = jar.getJarEntry("META-INF/application.xml"); //NOI18N
                if (null != ddf) {
                    return null;
                }
            }
            catch (ClassNotFoundException cnfe) {
                org.openide.ErrorManager.getDefault ().log (cnfe.getLocalizedMessage ());
            }
            catch (java.io.IOException ioe) {
                org.openide.ErrorManager.getDefault ().log (ioe.getLocalizedMessage ());
            }
            finally {
                try {
                    if (null != jar)
                        jar.close();
                }
                catch (java.io.IOException ioe) {
                    // there is little that we can do about this.
                }
            }
            return mod;
        }
    
    /**
     * Called when a change was made to a properties file that might be shared with Ant.
     * <p class="nonnormative">
     * Note: normally you would not use this event to detect property changes.
     * Use the property change listener from {@link PropertyEvaluator} instead to find
     * changes in the interpreted values of Ant properties, possibly coming from multiple
     * properties files.
     * </p>
     * @param ev an event with details of the change
     */
    public void propertiesChanged(AntProjectEvent ev) {
        
    }

    /**
     * Adds a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {

        propertyChangeSupport.addPropertyChangeListener (l);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {

        propertyChangeSupport.removePropertyChangeListener (l);
    }
    
    public Map getModuleMap() {
        Map mods = new HashMap();
        Object o = properties.get(JAR_CONTENT_ADDITIONAL);
        if (null != o && o instanceof PropertyInfo) {
            PropertyInfo pi = (PropertyInfo) o;
            List newV = (List) pi.getValue();
            
            Iterator iter = newV.iterator();
            while (iter.hasNext()) {
                VisualClassPathItem vcpi = (VisualClassPathItem) iter.next();
                String path = vcpi.getCompletePathInArchive(); //   computePath(vcpi);
                Object obj = vcpi.getObject();
                AntArtifact aa;
                Project p;
                if (obj instanceof AntArtifact) {
                    aa = (AntArtifact) obj;
                    p = aa.getProject();
                } else {
                    continue;
                }
                J2eeModuleProvider jmp = (J2eeModuleProvider) p.getLookup().lookup(J2eeModuleProvider.class);
                if (null != jmp) {
                    J2eeModule jm = jmp.getJ2eeModule();
                    if (null != jm) {
                        mods.put(path, jmp);
                    }
                }
            }
        }
        return mods; // earProject.getAppModule().setModules(mods);
    }


    public void addJ2eeSubprojects(Project[] moduleProjects) {
        List artifactList = new ArrayList();
        for (int i = 0; i < moduleProjects.length; i++) {
            AntArtifact artifacts[] = AntArtifactQuery.findArtifactsByType(
                    moduleProjects[i],
                    EjbProjectConstants.ARTIFACT_TYPE_EJBJAR_EAR_ARCHIVE ); //the artifact type is the some for both ejb and war projects
            if (null != artifacts)
                artifactList.addAll(Arrays.asList(artifacts));
            
        }
        // create the vcpis
        List newVCPIs = new ArrayList();
        Iterator iter = artifactList.iterator();
        while (iter.hasNext()) {
            AntArtifact art = (AntArtifact) iter.next();
            VisualClassPathItem vcpi = VisualClassPathItem.create(art,VisualClassPathItem.PATH_IN_WAR_APPLET);
            vcpi.setRaw(EarProjectProperties.JAR_CONTENT_ADDITIONAL);
            newVCPIs.add(vcpi);
        }
        Object t = get(EarProjectProperties.JAR_CONTENT_ADDITIONAL);
        if (!(t instanceof List)) {
            assert false : "jar content isn't a List???";
            return;
        }
        List vcpis = (List) t;
        newVCPIs.addAll(vcpis);
        put(EarProjectProperties.JAR_CONTENT_ADDITIONAL, newVCPIs);
        store();
        try {
            org.netbeans.api.project.ProjectManager.getDefault().saveProject(getProject());
        } catch ( java.io.IOException ex ) {
            org.openide.ErrorManager.getDefault().notify( ex );
        }
    }
    
    String[] getWebUris() {
        Application app = null;
        try {
            app = DDProvider.getDefault ().getDDRoot (earProject.getAppModule().getDeploymentDescriptor ());
        }
        catch (java.io.IOException ioe) {
            org.openide.ErrorManager.getDefault ().log (ioe.getLocalizedMessage ());
        }
        Module mods[] = app.getModule();
        int len = 0;
        if (null != mods)
            len = mods.length;
        ArrayList retList = new ArrayList();
        for (int i = 0; i < len; i++) {
            Web w = mods[i].getWeb();
            if (null != w) {
                retList.add(w.getWebUri());
            }
        }
        return (String[]) retList.toArray(new String[retList.size()]);
        
    }
    
    // XXX - remove this method after completing 54179
    private  boolean projectClosed() {
        Project[] projects = org.netbeans.api.project.ui.OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            if (projects[i].equals(earProject))
                return false;
        }
        return true;
    }
    
}

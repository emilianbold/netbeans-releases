/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.eclipse;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.J2SEProjectType;
import org.netbeans.modules.java.j2seproject.SourceRoots;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.modules.java.project.ProjectClassPathExtender;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Element;


/**
 * DOCDO
 *
 * @author mkrauskopf
 */
final class Importer {
    
    private Set eclProjects;
    private String destination;
    private J2SEProject[] nbProjects;
    private Set recursionCheck = new HashSet();
    private Map loadedProject = new HashMap();
    
    private int nOfProcessed;
    private String progressInfo;
    private boolean done;
    
    private JavaPlatform[] nbPlfs; // All netbeans platforms
    private String nbDefPlfDir; // NetBeans default platform directory
    
    Importer(final Set eclProjects, final String destination) {
        this.eclProjects = eclProjects;
        this.destination = destination;
        this.nbProjects = new J2SEProject[eclProjects.size()];
    }
    
    void startImporting() {
        nbPlfs = JavaPlatformManager.getDefault().getInstalledPlatforms();
        JavaPlatform defPlf = JavaPlatformManager.getDefault().getDefaultPlatform();
        Collection installFolder = defPlf.getInstallFolders();
        if (installFolder.isEmpty()) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "There is not any platform in NetBeans..."); // NOI18N
            return;
        } else {
            nbDefPlfDir = FileUtil.toFile(
                    (FileObject) installFolder.toArray()[0]).getAbsolutePath();
        }
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                ProjectManager.mutex().writeAccess(new Runnable() {
                    public void run() {
                        int pos = 0;
                        for (Iterator it = eclProjects.iterator(); it.hasNext(); ) {
                            EclipseProject eclPrj = (EclipseProject) it.next();
                            nbProjects[pos++] = importProject(eclPrj);
                        }
                        done = true;
                    }
                });
            }
        });
    }
    
    int getNOfProcessed() {
        return nOfProcessed;
    }
    
    String getProgressInfo() {
        return progressInfo;
    }
    
    boolean isDone() {
        return done;
    }
    
    J2SEProject[] getProjects() {
        return nbProjects;
    }
    
    private J2SEProject importProject(EclipseProject eclProject) {
        assert eclProject != null : "Eclipse project cannot be null"; // NOI18N
        
        // recursivity check
        if (!recursionCheck.add(eclProject.getDirectory().toString())) {
            J2SEProject project = (J2SEProject) loadedProject
                    .get(eclProject.getDirectory().getAbsolutePath());
            assert project != null : "nbSubProject cannot be null"; // NOI18N
            return project;
        }
        nOfProcessed++;
        progressInfo = "Processing \"" + eclProject.getName() + "\" project...";
        File nbProjectDir = new File(destination + "/" + eclProject.getName());
        Map eclRoots = eclProject.getAllSourceRoots();
        File[] testDirs = new File[0];
        try {
            File[] srcFiles = new File[eclRoots.size()];
            int j = 0;
            for (Iterator it = eclRoots.keySet().iterator(); it.hasNext(); ) {
                srcFiles[j++] = (File) it.next();
            }
            // create basic NB project
            final AntProjectHelper helper = J2SEProjectGenerator.createProject(
                    nbProjectDir, eclProject.getName(), srcFiles, testDirs, null);
            // get NB project
            J2SEProject nbProject = (J2SEProject) ProjectManager.getDefault().
                    findProject(FileUtil.toFileObject(nbProjectDir));
            ProjectClassPathExtender nbProjectClassPath =
                    (ProjectClassPathExtender) nbProject.getLookup().lookup(ProjectClassPathExtender.class);
            assert nbProjectClassPath != null : "Cannot lookup ProjectClassPathExtender"; // NOI18N
            
            // set labels for source roots
            SourceRoots roots = nbProject.getSourceRoots();
            URL[] rootURLs = roots.getRootURLs();
            String[] labels = new String[rootURLs.length];
            for (int i = 0; i < rootURLs.length; i++) {
                labels[i] = (String) eclRoots.get(new File(rootURLs[i].getFile()));
            }
            roots.putRoots(rootURLs, labels);
            
            // add libraries to classpath
            File[] eclLibs = eclProject.getAllLibrariesFiles();
            for (int i = 0; i < eclLibs.length; i++) {
                if (eclLibs[i].exists()) {
                    FileObject eclLib = FileUtil.toFileObject(eclLibs[i]);
                    nbProjectClassPath.addArchiveFile(eclLib);
                } else {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                            eclLibs[i] + " doesn't exist. Skipping..."); // NOI18N
                }
            }
            
            // create projects the main project depends on
            Collection projects = eclProject.getProjects();
            for (Iterator it = projects.iterator(); it.hasNext(); ) {
                EclipseProject eclSubProject = (EclipseProject) it.next();
                J2SEProject nbSubProject = importProject(eclSubProject);
                AntArtifact[] artifact =
                        AntArtifactQuery.findArtifactsByType(nbSubProject, JavaProjectConstants.ARTIFACT_TYPE_JAR);
                nbProjectClassPath.addAntArtifact(artifact[0]);
            }
            
            // set platform used by an Eclipse project
            setJavaPlatform(eclProject, helper);
            
            ProjectManager.getDefault().saveProject(nbProject);
            loadedProject.put(eclProject.getDirectory().getAbsolutePath(), nbProject);
            return nbProject;
        } catch (IOException e) {
            ErrorManager.getDefault().log(ErrorManager.USER,
                    "Error occured during project importing: " + e); // NOI18N
        }
        return null;
    }
    
    /** Sets <code>JavaPlatform</code> for the given project */
    private void setJavaPlatform(EclipseProject eclProject, final AntProjectHelper helper) {
        //        progressInfo = "Setting JDK for \"" + eclProject.getName() + "\"";
        String eclPlfDir = eclProject.getJDKDirectory();
        // eclPlfDir can be null in a case when a JDK was set for an eclipse
        // project in Eclipse then the directory with JDK was deleted from
        // filesystem and then a project is imported into NetBeans
        if (eclPlfDir == null || eclPlfDir.equals(nbDefPlfDir)) {
            // use default platform
            return;
        }
        for (int i = 0; i < nbPlfs.length; i++) {
            JavaPlatform nbPlf = nbPlfs[i];
            String nbPlfDir = FileUtil.toFile(
                    (FileObject) nbPlf.getInstallFolders().toArray()[0]).getAbsolutePath();
            
            if (nbPlfDir.equals(eclPlfDir)) {
                EditableProperties prop =
                        helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                Element pcd = helper.getPrimaryConfigurationData(true);
                Element el = pcd.getOwnerDocument().createElementNS(
                        J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                        "explicit-platform"); // NOI18N
                pcd.appendChild(el);
                helper.putPrimaryConfigurationData(pcd, true);
                String ver = nbPlf.getSpecification().getVersion().toString();
                String normalizedName = (String)nbPlf.getProperties().get(
                        "platform.ant.name"); // NOI18N
                prop.setProperty(J2SEProjectProperties.JAVAC_SOURCE, ver);
                prop.setProperty(J2SEProjectProperties.JAVAC_TARGET, ver);
                prop.setProperty(J2SEProjectProperties.JAVA_PLATFORM, normalizedName);
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, prop);
                break;
            }
        }
    }
    // if we are not able to find any platform the default is used
}

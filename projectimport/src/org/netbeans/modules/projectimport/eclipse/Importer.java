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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.java.j2seplatform.platformdefinition.PlatformConvertor;
import org.netbeans.modules.java.j2seplatform.wizard.NewJ2SEPlatform;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.J2SEProjectType;
import org.netbeans.modules.java.j2seproject.SourceRoots;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.modules.projectimport.LoggerFactory;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Element;

/**
 * Able to import given Eclipse projects in separate thread with providing
 * information about current state(progress). Converts eclipse projects and
 * their required projects into NetBeans ones and stores them into the given
 * destination.
 *
 * @author mkrauskopf
 */
final class Importer {
    
    /**
     * Logger for this class
     */
    private static final Logger logger =
            LoggerFactory.getDefault().createLogger(Importer.class);
    
    private Set eclProjects;
    private String destination;
    private boolean recursively;
    private J2SEProject[] nbProjects;
    private Set recursionCheck = new HashSet();
    private Map loadedProject = new HashMap();
    
    private int nOfProcessed;
    private String progressInfo;
    private boolean done;
    private Collection warnings;
    
    private JavaPlatform[] nbPlfs; // All netbeans platforms
    private File nbDefPlfFile; // NetBeans default platform directory
    
    Importer(final Set eclProjects, String destination, boolean recursively) {
        this.eclProjects = eclProjects;
        this.destination = destination;
        this.recursively = recursively;
        this.nbProjects = new J2SEProject[eclProjects.size()];
    }
    
    /**
     * Starts importing process in separated thread. Use getters to obtain
     * information about current progress.
     */
    void startImporting() {
        nbPlfs = JavaPlatformManager.getDefault().getInstalledPlatforms();
        JavaPlatform defPlf = JavaPlatformManager.getDefault().getDefaultPlatform();
        Collection installFolder = defPlf.getInstallFolders();
        if (installFolder.isEmpty()) {
            logWarning(NbBundle.getMessage(Importer.class, "MSG_NotValidPlatformsInNB")); // NOI18N
            return;
        } else {
            nbDefPlfFile = FileUtil.toFile((FileObject) installFolder.toArray()[0]);
        }
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                ProjectManager.mutex().writeAccess(new Runnable() {
                    public void run() {
                        try {
                            int pos = 0;
                            for (Iterator it = eclProjects.iterator(); it.hasNext(); ) {
                                EclipseProject eclPrj = (EclipseProject) it.next();
                                nbProjects[pos++] = importProject(eclPrj);
                            }
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                            ErrorManager.getDefault().log(ErrorManager.USER,
                                    "Error occured during project importing: " + ioe); // NOI18N
                        } finally {
                            done = true;
                        }
                    }
                });
            }
        });
    }
    
    /**
     * Returns number of already processed projects.
     */
    int getNOfProcessed() {
        return nOfProcessed;
    }
    
    /**
     * Returns localized message describing current importer activity.
     */
    String getProgressInfo() {
        return progressInfo;
    }
    
    /**
     * Returns whether importer has finished.
     */
    boolean isDone() {
        return done;
    }
    
    Collection getWarnings() {
        return warnings;
    }
    
    /**
     * Gets imported projects. Call after the importing <code>isDone()</code>.
     */
    J2SEProject[] getProjects() {
        return nbProjects;
    }
    
    private J2SEProject importProject(EclipseProject eclProject) throws IOException {
        assert eclProject != null : "Eclipse project cannot be null"; // NOI18N
        
        // recursivity check
        if (!recursionCheck.add(eclProject.getDirectory().toString())) {
            J2SEProject project = (J2SEProject) loadedProject.get(
                    eclProject.getDirectory().getAbsolutePath());
            return project;
        }
        logger.finer("Importing of project: \"" + // NOI18N
                eclProject.getDirectory().getAbsolutePath() + "\" started"); // NOI18N
        nOfProcessed++;
        progressInfo = NbBundle.getMessage(Importer.class,
                "MSG_Progress_ProcessingProject", eclProject.getName()); // NOI18N
        File nbProjectDir = new File(destination + "/" + eclProject.getName());
        Map eclRoots = eclProject.getAllSourceRoots();
        File[] testDirs = new File[0];
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
                findProject(FileUtil.toFileObject(
                FileUtil.normalizeFile(nbProjectDir)));
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
        for (Iterator it = eclProject.getAllLibrariesFiles().iterator(); it.hasNext(); ) {
            File eclLib = (File) it.next();
            if (eclLib.exists()) {
                nbProjectClassPath.addArchiveFile(FileUtil.toFileObject(
                        FileUtil.normalizeFile(eclLib)));
            } else {
                logWarning(NbBundle.getMessage(Importer.class, "MSG_LibraryDoesnExist", // NOI18N
                        eclProject.getName(), eclLib.getAbsolutePath()), true);
            }
        }
        
        // create projects the main project depends on
        if (recursively) {
            Collection projects = eclProject.getProjects();
            for (Iterator it = projects.iterator(); it.hasNext(); ) {
                EclipseProject eclSubProject = (EclipseProject) it.next();
                J2SEProject nbSubProject = importProject(eclSubProject);
                // The project can be null when a cycle dependency is encountered.
                // Just skip the dependency and try the best we can.
                if (nbSubProject != null) {
                    AntArtifact[] artifact =
                            AntArtifactQuery.findArtifactsByType(nbSubProject,
                            JavaProjectConstants.ARTIFACT_TYPE_JAR);
                    nbProjectClassPath.addAntArtifact(
                            artifact[0], artifact[0].getArtifactLocations()[0]);
                } else {
                    logger.warning("Project in directory \"" +  // NOI18N
                            eclProject.getDirectory().getAbsolutePath() +
                            "\" is already being processed. Recursive " + // NOI18N
                            "dependencies reached. "); // NOI18N
                }
            }
        }
        
        // set platform used by an Eclipse project
        setJavaPlatform(eclProject, helper);
        
        ProjectManager.getDefault().saveProject(nbProject);
        logger.finer("Project loaded: " + // NOI18N
                eclProject.getDirectory().getAbsolutePath());
        loadedProject.put(eclProject.getDirectory().getAbsolutePath(), nbProject);
        return nbProject;
    }
    
    /** Sets <code>JavaPlatform</code> for the given project */
    private void setJavaPlatform(EclipseProject eclProject,
            final AntProjectHelper helper) throws IOException {
        //        progressInfo = "Setting JDK for \"" + eclProject.getName() + "\"";
        String eclPlfDir = eclProject.getJDKDirectory();
        // eclPlfDir can be null in a case when a JDK was set for an eclipse
        // project in Eclipse then the directory with JDK was deleted from
        // filesystem and then a project is imported into NetBeans
        if (eclPlfDir == null) {
            return;
        }
        File eclPlfFile = FileUtil.normalizeFile(new File(eclPlfDir));
        if (eclPlfFile.equals(nbDefPlfFile)) { // use default platform
            return;
        }
        JavaPlatform nbPlf = null;
        for (int i = 0; i < nbPlfs.length; i++) {
            JavaPlatform current = nbPlfs[i];
            File nbPlfDir = FileUtil.toFile(
                    (FileObject) current.getInstallFolders().toArray()[0]);
            
            if (nbPlfDir.equals(eclPlfFile)) {
                nbPlf = nbPlfs[i];
                // found
                break;
            }
        }
        // If we are not able to find any platform let's use the "broken
        // platform" which can be easily added by user with "Resolve Reference
        // Problems" feature. Such behaviour is much better then using a default
        // platform when user imports more projects.
        if (nbPlf == null) {
            logger.fine("Creating new platform: " + eclPlfFile.getAbsolutePath()); // NOI18N
            FileObject fo = FileUtil.toFileObject(eclPlfFile);
            if (fo != null) {
                NewJ2SEPlatform plat = NewJ2SEPlatform.create(fo);
                plat.run();
                if (plat.isValid()) {
                    if (plat.findTool("javac")!= null) {    //NOI18N
                        String displayName = createPlatformDisplayName(plat);
                        String antName = createPlatformAntName(displayName);
                        plat.setDisplayName(displayName);
                        plat.setAntName(antName);
                        FileObject platformsFolder = Repository.getDefault().
                                getDefaultFileSystem().findResource(
                                "Services/Platforms/org-netbeans-api-java-Platform"); //NOI18N
                        assert platformsFolder != null;
                        DataObject dobj = PlatformConvertor.create(plat,
                                DataFolder.findFolder(platformsFolder), antName);
                        nbPlf = (JavaPlatform) dobj.getNodeDelegate().getLookup().
                                lookup(JavaPlatform.class);
                        // update installed platform
                        nbPlfs = JavaPlatformManager.getDefault().getInstalledPlatforms();
                    } else {
                        logWarning(NbBundle.getMessage(Importer.class, "MSG_JRECannotBeUsed", // NOI18N
                                eclProject.getName()), true);
                    }
                } else {
                    // tzezula: TODO: User should be notified in the UI and
                    // probably default platform is used (not sure if it is
                    // according to UI spec)
                    logWarning( "Cannot create new J2SE platform, the " + // NOI18N
                            "default platform will be used."); // NOI18N
                }
            } else {
                logWarning(NbBundle.getMessage(Importer.class, "MSG_JDKDoesnExistUseDefault", // NOI18N
                        eclProject.getName(), eclPlfFile.getAbsolutePath()), true);
            }
        }
        // tzezula: The platform is changed to explicit one only in case when
        // the platform already existed or it was successfully created
        if (nbPlf != null) {
            Element pcd = helper.getPrimaryConfigurationData(true);
            Element el = pcd.getOwnerDocument().createElementNS(
                    J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                    "explicit-platform"); // NOI18N
            pcd.appendChild(el);
            helper.putPrimaryConfigurationData(pcd, true);
            EditableProperties prop =
                    helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            String ver = nbPlf.getSpecification().getVersion().toString();
            String normalizedName = (String)nbPlf.getProperties().get(
                    "platform.ant.name"); // NOI18N
            prop.setProperty(J2SEProjectProperties.JAVAC_SOURCE, ver);
            prop.setProperty(J2SEProjectProperties.JAVAC_TARGET, ver);
            prop.setProperty(J2SEProjectProperties.JAVA_PLATFORM, normalizedName);
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, prop);
        } else {
            logWarning("Setting of platform for project \"" // NOI18N
                    + eclProject.getName() + "\" failed."); // NOI18N
        }
    }
    
    private void logWarning(String message) {
        logWarning(message, false);
    }
    
    /**
     * Delegates to ErrorManager. When the <code>isGUIWarning</code> is true,
     * the warning will be also shown to the user after importing is done.
     */
    private void logWarning(String message, boolean isGUIWarning) {
        if (isGUIWarning) {
            if (warnings == null) {
                warnings = new ArrayList();
            }
            warnings.add(message);
        }
        logger.warning(message);
    }
    
    
    private static String createPlatformDisplayName(JavaPlatform plat) {
        Map m = plat.getSystemProperties();
        String vmName = (String)m.get("java.vm.name");              //NOI18N
        String vmVersion = (String)m.get("java.vm.version");        //NOI18N
        StringBuffer displayName = new StringBuffer();
        if (vmName != null)
            displayName.append(vmName);
        if (vmVersion != null) {
            if (displayName.length()>0) {
                displayName.append(" ");
            }
            displayName.append(vmVersion);
        }
        return displayName.toString();
    }
    
    private String createPlatformAntName(String displayName) {
        assert displayName != null && displayName.length() > 0;
        String antName = PropertyUtils.getUsablePropertyName(displayName);
        if (platformExists(antName)) {
            String baseName = antName;
            int index = 1;
            antName = baseName + Integer.toString(index);
            while (platformExists(antName)) {
                index ++;
                antName = baseName + Integer.toString(index);
            }
        }
        return antName;
    }
    
    /**
     * Checks if the platform of given antName is already installed
     */
    private boolean platformExists(String antName) {
        assert antName != null && antName.length() > 0;
        for (int i=0; i< nbPlfs.length; i++) {
            String otherName = (String) nbPlfs[i].getProperties().get("platform.ant.name");  //NOI18N
            if (antName.equals(otherName)) {
                return true;
            }
        }
        return false;
    }
}

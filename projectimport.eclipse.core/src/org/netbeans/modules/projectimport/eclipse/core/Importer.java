/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seplatform.platformdefinition.PlatformConvertor;
import org.netbeans.modules.java.j2seplatform.wizard.NewJ2SEPlatform;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeUpdater;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Able to import given Eclipse projects in separate thread with providing
 * information about current state(progress). Converts eclipse projects and
 * their required projects into NetBeans ones and stores them into the given
 * destination.
 *
 * @author mkrauskopf
 */
final class Importer {
    
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(Importer.class.getName());

    private final List<EclipseProject> eclProjects;
    private final String destination;
    private final List<Project> nbProjects;
    
    private int nOfProcessed;
    private String progressInfo;
    private boolean done;
    private List<String> warnings = new ArrayList<String>();
    
    private JavaPlatform[] nbPlfs; // All netbeans platforms
    private List<JavaPlatform> justCreatedPlatforms = new ArrayList<JavaPlatform>(); // platforms created during import
    private File nbDefPlfFile; // NetBeans default platform directory
    
    Importer(final List<EclipseProject> eclProjects, String destination) {
        this.eclProjects = eclProjects;
        this.destination = destination;
        this.nbProjects = new ArrayList<Project>();
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
                                nbProjects.add(importProject(eclPrj, warnings));
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
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
    
    List<String> getWarnings() {
        return warnings;
    }
    
    /**
     * Gets imported projects. Call after the importing <code>isDone()</code>.
     */
    Project[] getProjects() {
        return nbProjects.toArray(new Project[nbProjects.size()]);
    }
    
    private Project importProject(EclipseProject eclProject, List<String> importProblems) throws IOException {
        assert eclProject != null : "Eclipse project cannot be null"; // NOI18N
        
        // create global libraries, etc.
        eclProject.setupEvaluatedContainers();
        
        // create ENV variables in build.properties
        eclProject.setupEnvironmentVariables();
        
        nOfProcessed++;
        progressInfo = NbBundle.getMessage(Importer.class,
                "MSG_Progress_ProcessingProject", eclProject.getName()); // NOI18N
        
        ProjectImportModel model = new ProjectImportModel(eclProject, destination, getJavaPlatform(eclProject));
        Project p = eclProject.getProjectTypeFactory().createProject(model, importProblems);
        
        if (eclProject.getProjectTypeFactory() instanceof ProjectTypeUpdater) {
            ProjectTypeUpdater updater = (ProjectTypeUpdater)eclProject.getProjectTypeFactory();
            String key = updater.calculateKey(model);
            EclipseProjectReference ref = new EclipseProjectReference(p, 
                    eclProject.getDirectory().getAbsolutePath(), 
                    eclProject.getWorkspace().getDirectory().getAbsolutePath(), "0", key);
            EclipseProjectReference.write(p, ref);
        }
        assert p != null;
        return p;
    }
    
    /** Sets <code>JavaPlatform</code> for the given project */
    private JavaPlatform getJavaPlatform(EclipseProject eclProject) throws IOException {
        //        progressInfo = "Setting JDK for \"" + eclProject.getName() + "\"";
        String eclPlfDir = eclProject.getJDKDirectory();
        // eclPlfDir can be null in a case when a JDK was set for an eclipse
        // project in Eclipse then the directory with JDK was deleted from
        // filesystem and then a project is imported into NetBeans
        if (eclPlfDir == null) {
            return null;
        }
        File eclPlfFile = FileUtil.normalizeFile(new File(eclPlfDir));
        if (eclPlfFile.equals(nbDefPlfFile)) { // use default platform
            return null;
        }
        JavaPlatform nbPlf = null;
        List<JavaPlatform> all = new ArrayList<JavaPlatform>(justCreatedPlatforms);
        all.addAll(Arrays.<JavaPlatform>asList(nbPlfs));
        for (JavaPlatform current : all) {
            Collection instFolders = current.getInstallFolders();
            if (instFolders.isEmpty()) {
                logger.fine("Java platform \"" + current.getDisplayName() + // NOI18N
                        "\" is not valid. Skipping..."); // NOI18N
                continue;
            }
            File nbPlfDir = FileUtil.toFile((FileObject) instFolders.toArray()[0]);
            if (nbPlfDir.equals(eclPlfFile)) {
                nbPlf = current;
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
                        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(displayName, null);
                        if (platforms.length > 0) {
                            return platforms[0];
                        }
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
                        justCreatedPlatforms.add(nbPlf);
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
        return nbPlf;
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
            warnings.add(message);
        }
        logger.warning(message);
    }
    
    
    private static String createPlatformDisplayName(JavaPlatform plat) {
        Map<String, String> m = plat.getSystemProperties();
        String vmVersion = m.get("java.specification.version");        //NOI18N
        StringBuffer displayName = new StringBuffer("JDK ");
        if (vmVersion != null) {
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

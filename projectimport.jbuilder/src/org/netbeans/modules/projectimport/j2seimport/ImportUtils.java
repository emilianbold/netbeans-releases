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

package org.netbeans.modules.projectimport.j2seimport;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.j2seplatform.api.J2SEPlatformCreator;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.J2SEProjectType;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Element;


/**
 *
 * @author Radek Matous
 */
public final class ImportUtils {
    public  static final Logger logger =
            LoggerFactory.getDefault().createLogger(ImportUtils.class);
    
    ImportProcessImpl importProcess = null;
    
    
    public static final ImportProcess createImportProcess(final FileObject projectDirectory,
            final Collection allPrjDefs, boolean includeDependencies)  {
        return new ImportProcessImpl(projectDirectory, allPrjDefs, includeDependencies);
    }
    
    //This method is expected just for testing purposes
    public static ImportUtils createInstance() {
        return new ImportUtils();
    }
    
        
    public final void importAllProjects(final FileObject projectDirectory,
            final Collection allPrjDefs, WarningContainer warnings, boolean includeDependencies) throws IOException {
        for (Iterator it = allPrjDefs.iterator(); it.hasNext();) {
            ProjectModel projectDefinition = (ProjectModel)it.next();
            FileObject importLocation = FileUtil.createFolder(projectDirectory, projectDefinition.getName());
            if (includeDependencies) {
                importProject(importLocation, projectDefinition, warnings, false);
            } else {
                J2SEProject nbProject = importProjectWithoutDependencies(importLocation, projectDefinition, warnings, false);
                ProjectManager.getDefault().saveProject(nbProject);
            }
        }
    }
    
    
    public final J2SEProject importProject(final FileObject projectDirectory,
            final ProjectModel projectDefinition, WarningContainer warnings, boolean isDependency) throws IOException {
        
        J2SEProject nbProject = importProjectWithoutDependencies(projectDirectory, projectDefinition, warnings, isDependency);
        
        for (Iterator it = projectDefinition.getDependencies().iterator(); it.hasNext();) {
            ProjectModel subPrjDef = (ProjectModel)it.next();
            FileObject importLocation = FileUtil.createFolder(projectDirectory.getParent(), subPrjDef.getName());
            J2SEProject subJ2SEProject = importProject(importLocation, subPrjDef, warnings, true);
            addDependency(nbProject, subJ2SEProject);
        }
        
        
        ProjectManager.getDefault().saveProject(nbProject);
        return nbProject;
    }
    
    
    public final J2SEProject importProjectWithoutDependencies(final FileObject projectDirectory,
            final ProjectModel projectDefinition, WarningContainer warnings, boolean isDependency) throws IOException {
        
        J2SEProject nbProject = null;
        {
            Project prj = ProjectManager.getDefault().findProject(projectDirectory);
            if (prj != null) {
                if (prj instanceof J2SEProject) {
                    nbProject = (J2SEProject)prj;
                    logger.warning("Project already exists: " + projectDirectory.getPath());
                } else {
                    throw new IllegalStateException();
                }
            }
        }
        
        if (nbProject == null) {
            if (!projectDirectory.isFolder()) {
                throw new IllegalArgumentException();//NOI18N
            }
            File[] srcFiles = getSourceRoots(projectDefinition);
            File destination = FileUtil.toFile(projectDirectory);
            
            if (!isDependency) {
                addProgresInfo(projectDefinition.getName());
            }
            AntProjectHelper helper = J2SEProjectGenerator.createProject(destination, projectDefinition.getName(), srcFiles, new File[]{}, null, null, null);
            
            assert helper != null;
            nbProject = (J2SEProject)ProjectManager.getDefault().findProject(projectDirectory);
            assert nbProject != null;
            
            
            if (!isDependency) {
                addProgresInfo(projectDefinition.getName());    
            }            
            
            //import source roots
            warnings.addAll(addSourceRoots(projectDefinition, nbProject));
            
            
            if (!isDependency) {
                addProgresInfo(projectDefinition.getName());    
            }            
            //import libraries
            try {
                warnings.addAll(addLibraries(projectDefinition, nbProject));
            } catch(IOException iex) {
                ImportUtils.addWarning(warnings, iex.getLocalizedMessage());
            }
            
            
            if (!isDependency) {
                addProgresInfo(projectDefinition.getName());    
            }            
            //import user libraries
            try {
                warnings.addAll(addUserLibraries(projectDefinition, nbProject));
            } catch(IOException iex) {
                ImportUtils.addWarning(warnings, iex.getLocalizedMessage());
            }
            
            if (!isDependency) {
                addProgresInfo(projectDefinition.getName());    
            }            
            if (projectDefinition.getJDKDirectory() != null) {
                warnings.addAll(addJavaPlatform(projectDefinition, helper));
            }
            
            
            if (importProcess != null) {
                importProcess.addWarnings(projectDefinition.getWarnings());
                importProcess.addProjectToOpen(nbProject);            
            }
            
            
        }
        return nbProject;
    }
    
    private WarningContainer/*<String> warnings*/ addJavaPlatform(final ProjectModel prjDefinition,
            final AntProjectHelper helper) {
        
        WarningContainer warnings = new WarningContainer();
        JavaPlatform platform = null;
        
        if (JavaPlatformManager.getDefault() == null) {
            ImportUtils.addWarning(warnings,"critical error: default platform manager isn't reachable");//NOI18N
            return warnings;
        }
        
        {
            JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
            if (defaultPlatform != null && isRepresentationOfPlatform(
                    defaultPlatform,prjDefinition.getJDKDirectory(), warnings)) {
                //no special handling for default platform
                return warnings;
            }
        }
        
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (int i = 0; i < platforms.length; i++) {
            if (isRepresentationOfPlatform(platforms[i],prjDefinition.getJDKDirectory(), warnings)) {
                platform = platforms[i];
                break;
            }
        }
        
        if (platform == null) {
            try{
                platform = createNewJavaPlatform(prjDefinition, warnings);
            } catch(IOException iex) {
                ImportUtils.addWarning(warnings,iex.getLocalizedMessage());
            }
        }
        
        if (platform != null) {
            setJavaPlatform(platform, helper);
        } else {
            ImportUtils.addWarning(warnings,"Setting of platform for project \"" // NOI18N
                    + prjDefinition.getName() + "\" failed."); // NOI18N();
        }
        
        return warnings;
    }
    
    private void setJavaPlatform(final JavaPlatform platform, final AntProjectHelper helper) {
        Element pcd = helper.getPrimaryConfigurationData(true);
        Element el = pcd.getOwnerDocument().createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                "explicit-platform"); // NOI18N
        
        pcd.appendChild(el);
        helper.putPrimaryConfigurationData(pcd, true);
        EditableProperties prop = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String ver = platform.getSpecification().getVersion().toString();
        String normalizedName = (String)platform.getProperties().get("platform.ant.name"); // NOI18N
        
        prop.setProperty(J2SEProjectProperties.JAVAC_SOURCE, ver);
        prop.setProperty(J2SEProjectProperties.JAVAC_TARGET, ver);
        prop.setProperty(J2SEProjectProperties.JAVA_PLATFORM, normalizedName);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, prop);
    }
    
    private JavaPlatform createNewJavaPlatform(final ProjectModel prjDefinition,
            final WarningContainer warnings) throws IOException {
        JavaPlatform retVal = null;
        FileObject foForJDKDirectory = FileUtil.toFileObject(prjDefinition.getJDKDirectory());
        
        if (foForJDKDirectory == null) {
            addWarning(warnings, NbBundle.getMessage(ImportUtils.class, "MSG_JDKDoesnExistUseDefault", // NOI18N
                    prjDefinition.getName(), prjDefinition.getJDKDirectory().getAbsolutePath()) );
            
            return null;
        }

        JavaPlatform platform = J2SEPlatformCreator.createJ2SEPlatform(foForJDKDirectory);
        
        if (platform.findTool("javac")!= null) {//NOI18N
            retVal = platform;
            // update installed platform - probably some trick (XXX still necessary? -jglick)
            JavaPlatformManager.getDefault().getInstalledPlatforms();
        }
        
        return retVal;
    }
    
    private boolean isRepresentationOfPlatform(final JavaPlatform platform,
            final File jdkDirectory, final WarningContainer warnings) {
        Collection installFolders = platform.getInstallFolders();
        
        //shouldn't occure according to javadoc but there is possible some sort of inconsistency
        boolean invalidDefaultPlatform = installFolders.isEmpty();
        
        if (invalidDefaultPlatform) {
            addWarning(warnings, NbBundle.getMessage(ImportUtils.class, "MSG_NotValidPlatformsInNB"));//NOI18N
        }
        
        return invalidDefaultPlatform ? false :
            jdkDirectory.equals(FileUtil.toFile((FileObject) installFolders.toArray()[0]));
    }
    
    
    public  void addDependency(final J2SEProject nbProject,
            final J2SEProject nbSubProject) throws IOException {
        ProjectClassPathExtender nbClsPath = (ProjectClassPathExtender)
        nbProject.getLookup().lookup(ProjectClassPathExtender.class);
        
        AntArtifact[] artifact = AntArtifactQuery.findArtifactsByType(nbSubProject,
                JavaProjectConstants.ARTIFACT_TYPE_JAR);
        
        nbClsPath.addAntArtifact(artifact[0], artifact[0].getArtifactLocations()[0]);
    }
    
    private WarningContainer/*<String> warnings*/ addSourceRoots(final ProjectModel projectDefinition,
            final J2SEProject nbProject) {
        
        SourceRoots roots = nbProject.getSourceRoots();
        URL[] rootURLs = roots.getRootURLs();
        String[] labels = new String[rootURLs.length];
        Map srcRoots2import = new HashMap();
        
        for (Iterator it = projectDefinition.getSourceRoots().iterator(); it.hasNext(); ) {
            ProjectModel.SourceRoot srcEntry = (ProjectModel.SourceRoot)it.next();
            File srcFolder = FileUtil.normalizeFile(srcEntry.getDirectory());
            //assert srcFolder.exists();
            srcRoots2import.put(srcFolder, srcEntry.getLabel());
        }
        
        for (int i = 0; i < rootURLs.length; i++) {
            File f = new File(rootURLs[i].getFile());
            String lb = (String) srcRoots2import.get(f);
            labels[i] = (lb != null) ? lb :f.getName();
            //assert labels[i] != null;
        }
        
        roots.putRoots(rootURLs, labels);
        return WarningContainer.EMPTY;
    }
    
    
    
    private WarningContainer/*<String> warnings*/ addUserLibraries(
            final ProjectModel projectDefinition, final J2SEProject nbProject) throws IOException {
        
        WarningContainer warnings = new WarningContainer();
        
        for (Iterator it = projectDefinition.getUserLibraries().iterator(); it.hasNext();) {
            ProjectModel.UserLibrary userLibrary = (ProjectModel.UserLibrary)it.next();
            ProjectClassPathExtender nbClsPath = (ProjectClassPathExtender) nbProject.getLookup().lookup(ProjectClassPathExtender.class);
            assert nbClsPath != null;
            List allLibs = getAllLibraries(null, userLibrary);
            
            for (Iterator itUL = allLibs.iterator(); itUL.hasNext();) {
                ProjectModel.Library lEntry = (ProjectModel.Library)itUL.next();
                try {
                    warnings.addAll(addLibrary(nbClsPath, lEntry, projectDefinition));
                } catch(IOException iex) {
                    ImportUtils.addWarning(warnings,iex.getLocalizedMessage());
                }
                
            }
        }
        
        
        return warnings;
    }

    private List getAllLibraries(List allLibs, final ProjectModel.UserLibrary userLibrary) {
        allLibs = (allLibs == null) ? new ArrayList() : allLibs;
        allLibs.addAll(userLibrary.getLibraries());
        for (Iterator it = userLibrary.getDependencies().iterator(); it.hasNext();) {
            ProjectModel.UserLibrary uLDep = (ProjectModel.UserLibrary) it.next();
            getAllLibraries(allLibs, uLDep);
        }
        return allLibs;
    }
    
    private WarningContainer/*<String> warnings*/ addLibraries(final ProjectModel projectDefinition,
            final J2SEProject nbProject) throws IOException {
        
        WarningContainer warnings = new WarningContainer();
        ProjectClassPathExtender nbClsPath = (ProjectClassPathExtender) nbProject.getLookup().lookup(ProjectClassPathExtender.class);
        assert nbClsPath != null;
        
        for (Iterator it = projectDefinition.getLibraries().iterator(); it.hasNext();) {
            ProjectModel.Library lEntry = (ProjectModel.Library)it.next();
            try {
                warnings.addAll(addLibrary(nbClsPath, lEntry, projectDefinition));
            } catch(IOException iex) {
                ImportUtils.addWarning(warnings,iex.getLocalizedMessage());
            }
        }
        
        return warnings;
    }
    
    
    private WarningContainer  addLibrary(final ProjectClassPathExtender nbClsPath,
            final ProjectModel.Library lEntry, final ProjectModel projectDefinition) throws IOException {
        WarningContainer warnings = new WarningContainer();
        FileObject archiv = FileUtil.toFileObject(lEntry.getArchiv());
        
        nbClsPath.addArchiveFile(archiv);
        return warnings;
    }
    
    
    private static void addWarning(final WarningContainer warnings, final String warning) {
        StringBuffer sbuf = new StringBuffer(NbBundle.getMessage(ImportUtils.class, "MSG_ImportWarning"));//NOI18N
        sbuf.append(" ").append(warning);//NOI18N
        
        String warningPlusPrefix = sbuf.toString();
        warnings.add(warningPlusPrefix, false);
        logger.warning(warningPlusPrefix);
    }
    
    private File[] getSourceRoots(final ProjectModel projectDefinition) {
        Collection/*<ProjectDefinition.SourceRootEntry>*/ sourceRootEntries = projectDefinition.getSourceRoots();
        File[] retVal = new File[sourceRootEntries.size()];
        
        int i = 0;
        for (Iterator it = sourceRootEntries.iterator(); it.hasNext();i++) {
            ProjectModel.SourceRoot entry = (ProjectModel.SourceRoot)it.next();
            retVal[i] = entry.getDirectory();
        }
        
        return retVal;
    }
    
    private void addProgresInfo(String projectName) {
        if (importProcess != null) {
            importProcess.increase();
            int step = importProcess.getCurrentStep();
            int idx = (step % ImportProcessImpl.PROGRESS_MESSAGES.length);
            importProcess.setCurrentStatus(NbBundle.getMessage(ImportUtils.class, ImportProcessImpl.PROGRESS_MESSAGES[idx],projectName));
            logger.fine(importProcess.toString());
        }
    }
    
    
    /** Creates a new instance of Utilities */
    private  ImportUtils() {}
    
    private static final class ImportProcessImpl implements ImportProcess {
        final int numberOfSteps;
        int currentStep = -1;
        boolean isFinished;
        String currentStatus = "";//NOI18N
        final FileObject projectDirectory;
        final Collection allPrjDefs;
        final WarningContainer warnings;
        final ImportUtils importer;
        Collection projectsToOpen;
        final ProgressHandle ph;
        boolean includeDependencies;
        static final String[] PROGRESS_MESSAGES = new String[] {"PRGS_ProjectImportStarted",
                "PRGS_ImportSourceRoots",
                "PRGS_ImportLibraries",
                "PRGS_ImportUserLibraries",
                "PRGS_ImportPlatform"
        };
        
        private ImportProcessImpl(final FileObject projectDirectory,
                final Collection allPrjDefs, boolean includeDependencies) {
            this.projectDirectory = projectDirectory;
            this.allPrjDefs = allPrjDefs;
            this.warnings = new WarningContainer();
            this.importer = ImportUtils.createInstance();
            this.importer.importProcess = this;
            this.numberOfSteps = allPrjDefs.size()*PROGRESS_MESSAGES.length;
            projectsToOpen = new ArrayList();
            this.includeDependencies = includeDependencies;
            this.ph = ProgressHandleFactory.createHandle(projectDirectory.getPath());//NOI18N
            assert this.ph != null;
        }
        
        public synchronized int getNumberOfSteps() {
            return numberOfSteps;
        }
        
        public synchronized int getCurrentStep() {
            return currentStep;
        }
        
        public synchronized String getCurrentStatus() {
            return currentStatus;
        }
        
        private synchronized void setCurrentStatus(String currentStatus) {
            this.currentStatus = currentStatus;
        }
        
        
        public void startImport(boolean asynchronous) {
            if (isFinished()) {
                throw new IllegalStateException();
            }

            ph.start(getNumberOfSteps());            
            if (asynchronous) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        ProjectManager.mutex().writeAccess(new Runnable() {
                            public void run() {
                                startImport();
                            }
                        });
                    }
                });
                
            } else {
                ProjectManager.mutex().writeAccess(new Runnable() {
                    public void run() {
                        startImport();
                    }});
            }
        }
        
        private void startImport() {
            increase();
            try {
                importer.importAllProjects(projectDirectory, allPrjDefs,
                        warnings, includeDependencies);
            } catch(IOException iex) {
                //warnings.add(iex.getLocalizedMessage());
                addWarning(warnings, iex.getLocalizedMessage());
            } finally {
                setFinished();
            }
        }
        
        public synchronized boolean isFinished() {
            return isFinished;
        }
        
        private synchronized void increase() {
            assert currentStep <= numberOfSteps;
                        
            this.currentStep = this.currentStep+1;
            ph.progress(getCurrentStatus(),this.currentStep);
        }
        
        private synchronized void setFinished() {
            ph.finish();
            currentStep = numberOfSteps;
            isFinished = true;
        }
        
        public synchronized WarningContainer getWarnings() {
            return warnings;
        }
        
        private synchronized void addWarnings(WarningContainer warnings) {
            this.warnings.addAll(warnings);
        }
        
        public synchronized String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("current step: ").append(new Integer(getCurrentStep()).toString()).//NOI18N
                    append(" from: ").append(getNumberOfSteps()).append(" current info: ").append(getCurrentStatus());//NOI18N
            return sb.toString();
        }

        private void addProjectToOpen(Project prj) {
            projectsToOpen.add(prj);
        }
        
        public Project[] getProjectsToOpen() {
            return (Project[])projectsToOpen.toArray(new Project[projectsToOpen.size()]);
        }

        public ProgressHandle getProgressHandle() {
            return ph;
        }
    }
}

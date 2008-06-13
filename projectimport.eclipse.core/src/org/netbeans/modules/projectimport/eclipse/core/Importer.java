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
    
    /**
     * 
     * @param eclProjects list of eclipse projects to import
     * @param destination destination location for NetBeans projects; can be null
     *  in which case NetBeans projects should be generated to the same folder as 
     *  eclipse projects
     */
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
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                ProjectManager.mutex().writeAccess(new Runnable() {
                    public void run() {
                        try {
                            int pos = 0;
                            for (Iterator it = eclProjects.iterator(); it.hasNext(); ) {
                                EclipseProject eclPrj = (EclipseProject) it.next();
                                Project p = importProject(eclPrj, warnings);
                                if (p != null) {
                                    nbProjects.add(p);
                                }
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

        List<String> projectImportProblems = new ArrayList<String>();
        
        // evaluate classpath containers
        eclProject.evaluateContainers(projectImportProblems);
        
        // create global libraries, etc.
        eclProject.setupEvaluatedContainers(projectImportProblems);
        
        // create ENV variables in build.properties
        eclProject.setupEnvironmentVariables(projectImportProblems);
        
        nOfProcessed++;
        progressInfo = NbBundle.getMessage(Importer.class,
                "MSG_Progress_ProcessingProject", eclProject.getName()); // NOI18N
        
        String dest;
        Project alreadyImported = null;
        if (destination == null) {
            dest = eclProject.getDirectory().getAbsolutePath();
            File f = new File(dest);
            if (f.exists()) {
                alreadyImported = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
            }
        } else {
            dest = FileUtil.normalizeFile(new File(destination+File.separator+eclProject.getDirectory().getName())).getAbsolutePath();
        }
        ProjectImportModel model = new ProjectImportModel(eclProject, dest, 
                JavaPlatformSupport.getJavaPlatformSupport().getJavaPlatform(eclProject, projectImportProblems), nbProjects);
        Project p;
        if (alreadyImported != null) {
            p = alreadyImported;
            projectImportProblems.add("Existing NetBeans project was found and will be used intead.");
        } else {
            if (!eclProject.isImportSupported()) {
                importProblems.add("Unkown project type - it cannot be imported.");
                return null;
            }
            p = eclProject.getProjectTypeFactory().createProject(model, projectImportProblems);
        
            if (eclProject.getProjectTypeFactory() instanceof ProjectTypeUpdater) {
                ProjectTypeUpdater updater = (ProjectTypeUpdater)eclProject.getProjectTypeFactory();
                String key = updater.calculateKey(model);
                EclipseProjectReference ref = new EclipseProjectReference(p, 
                        eclProject.getDirectory().getAbsolutePath(), 
                        eclProject.getWorkspace() != null ? eclProject.getWorkspace().getDirectory().getAbsolutePath() : null, "0", key);
                EclipseProjectReference.write(p, ref);
            }
            assert p != null;
        }
        if (projectImportProblems.size() > 0) {
            importProblems.add("Project "+eclProject.getName()+" import problems:");
            for (String s : projectImportProblems) {
                importProblems.add(" "+s);
            }
        }
        return p;
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

}

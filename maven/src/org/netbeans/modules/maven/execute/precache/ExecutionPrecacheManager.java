/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.execute.precache;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.execute.MavenJavaExecutor;
import org.netbeans.modules.maven.queries.MavenFileOwnerQueryImpl;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Listens on open project and changed files and tried to guess which
 * projects shall precache run/debug/build action executions..
 * @author mkleint
 */
public class ExecutionPrecacheManager implements PropertyChangeListener, ChangeListener {

    private final RequestProcessor REQ = new RequestProcessor("Maven Execution Caching", 1);
    
    private Project mainMavenProject;
    
    private Set<FileObject> roots = new HashSet<FileObject>();
    
    private final Set<FileSystem> filesystems = new HashSet<FileSystem>();
    private final FileChangeListener listener = new FileObjectListener();
    
    private static ExecutionPrecacheManager instance;
    
//    private final Map<Project, TimedWeakReference<HashMap<String, MavenJavaExecutor>>> cache =
//            new WeakHashMap<Project, TimedWeakReference<HashMap<String, MavenJavaExecutor>>>();
    
    public static ExecutionPrecacheManager getInstance() {
        if (instance == null) {
            instance = new ExecutionPrecacheManager();
        }
        return instance;
    }
    
    public void startMonitoring() {
        checkMainProject();
        checkMavenProjectRoots();
        OpenProjects.getDefault().addPropertyChangeListener(this);
        MavenFileOwnerQueryImpl.getInstance().addChangeListener(this);
    }
    
    public void stopMonitoring() {
        MavenFileOwnerQueryImpl.getInstance().removeChangeListener(this);
        OpenProjects.getDefault().removePropertyChangeListener(this);
        if (mainMavenProject != null) {
            NbMavenProject maven = mainMavenProject.getLookup().lookup(NbMavenProject.class);
            maven.removePropertyChangeListener(this);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (OpenProjects.PROPERTY_MAIN_PROJECT.equals(evt.getPropertyName())) {
            checkMainProject();
        }
        if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
            // maybe ignore when the project reload is caused by finished build.
            // how to figure?
            doPrepareProject((Project)evt.getSource(), true);
        }
    }
    
    public void stateChanged(ChangeEvent e) {
        checkMavenProjectRoots();
    }


    private void checkMainProject() {
        boolean changed = false;
        Project prj = OpenProjects.getDefault().getMainProject();
        if (prj != null) {
            NbMavenProject maven = prj.getLookup().lookup(NbMavenProject.class);
            if (maven != null && prj != mainMavenProject) {
                changed = true;
            }
        } else if (mainMavenProject != null) {
            changed = true;
        }
        if (changed) {
            if (mainMavenProject != null) {
                //TODO clean up old project caches..
                NbMavenProject maven = mainMavenProject.getLookup().lookup(NbMavenProject.class);
                maven.removePropertyChangeListener(this);
            }
            mainMavenProject = prj;
            if (mainMavenProject != null) {
                NbMavenProject maven = mainMavenProject.getLookup().lookup(NbMavenProject.class);
                maven.addPropertyChangeListener(this);
                //TODO start checking for new project..
            }
        }
    }

    private void checkMavenProjectRoots() {
        roots = MavenFileOwnerQueryImpl.getInstance().getOpenedProjectRoots();
        Set<FileSystem> newfs = new HashSet<FileSystem>();
        Set<FileSystem> olds = new HashSet<FileSystem>();
        olds.addAll(filesystems);
        for (FileObject fo : roots) {
            try {
                FileSystem fs = fo.getFileSystem();
                newfs.add(fs);
                if (!filesystems.contains(fs)) {
                    filesystems.add(fs);
                    fs.addFileChangeListener(listener);
                }
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        olds.removeAll(newfs);
        for (FileSystem fs : olds) {
            fs.removeFileChangeListener(listener);
        }
    }
    
    private void doPrepareProject(Project prj, boolean reset) {
        System.out.println("preparing project=" + prj.getProjectDirectory() + " reset=" + reset);
    }
    
    
    
    private class FileObjectListener implements FileChangeListener {

        public void fileFolderCreated(FileEvent fe) {
            //do anything?
        }

        public void fileDataCreated(FileEvent fe) {
            checkBuildTrigger(fe.getFile());
        }

        public void fileChanged(FileEvent fe) {
            checkBuildTrigger(fe.getFile());
        }

        public void fileDeleted(FileEvent fe) {
            checkBuildTrigger(fe.getFile());
        }

        public void fileRenamed(FileRenameEvent fe) {
            //TODO?
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            //not interesting..
        }
        
        private void checkBuildTrigger(FileObject fo) {
            if (checkRoots(fo)) {
                Project prj = FileOwnerQuery.getOwner(fo);
                if (prj != null) {
                    NbMavenProject watch = prj.getLookup().lookup(NbMavenProject.class);
                    if (watch != null) {
                        String name = fo.getNameExt();
                        boolean reset = "pom.xml".equals(name) || "profiles.xml".equals(name) || "nbactions.xml".equals(name);
                        doPrepareProject(prj, reset);
                        if (mainMavenProject != null) {
                            SubprojectProvider subs = mainMavenProject.getLookup().lookup(SubprojectProvider.class);
                            if (subs.getSubprojects().contains(prj)) {
                                doPrepareProject(mainMavenProject, reset);
                            }
                        }
                    }
                }
            }
            //TODO - check settings.xml and clear the cache..
        }
        
        private boolean checkRoots(FileObject curr) {
            Set<FileObject> rts = new HashSet<FileObject>();
            rts.addAll(roots);
            for (FileObject fo : rts) {
                if (FileUtil.isParentOf(fo, curr)) {
                    return true;
                }
            }
            return false;
        }

    }

}

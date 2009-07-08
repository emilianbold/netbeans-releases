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
package org.netbeans.modules.maven;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.Artifact;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.queries.MavenFileOwnerQueryImpl;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 * finds subprojects (projects this one depends on) that are locally available
 * and can be build as one unit. Uses maven multiproject infrastructure. (maven.multiproject.includes)
 * @author  Milos Kleint
 */
public class SubprojectProviderImpl implements SubprojectProvider {

    private final NbMavenProjectImpl project;
    private final NbMavenProject watcher;
    private List<ChangeListener> listeners;
    private ChangeListener listener2;
    private PropertyChangeListener propertyChange;

    /** Creates a new instance of SubprojectProviderImpl */
    public SubprojectProviderImpl(NbMavenProjectImpl proj, NbMavenProject watcher) {
        project = proj;
        this.watcher = watcher;
        listeners = new ArrayList<ChangeListener>();
        propertyChange = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
                    fireChange();
                }
            }
        };
        listener2 = new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                fireChange();
            }
        };
        MavenFileOwnerQueryImpl.getInstance().addChangeListener(
                WeakListeners.change(listener2,
                MavenFileOwnerQueryImpl.getInstance()));
    }


    public Set<? extends Project> getSubprojects() {
        Set<Project> projects = new HashSet<Project>();
        File basedir = FileUtil.toFile(project.getProjectDirectory());
        try {
            addProjectModules(basedir, projects, project.getOriginalMavenProject().getModules());
        } catch (InterruptedException x) {
            // can be interrupted in the open project dialog..
            return Collections.emptySet();
        }
        addOpenedCandidates(projects);
        projects.remove(project);
        return projects;
    }

    private void addOpenedCandidates(Set<Project> resultset) {
        Set<Project> opened = MavenFileOwnerQueryImpl.getInstance().getOpenedProjects();
        @SuppressWarnings("unchecked")
        List<Artifact> compileArtifacts = project.getOriginalMavenProject().getCompileArtifacts();
        List<String> artPaths = new ArrayList<String>();
        for (Artifact ar : compileArtifacts) {
            artPaths.add(project.getArtifactRelativeRepositoryPath(ar));
        }
        for (Project prj : opened) {
            String prjpath = ((NbMavenProjectImpl)prj).getArtifactRelativeRepositoryPath();
            if (artPaths.contains(prjpath)) {
                resultset.add(prj);
            }
        }
    }

    private boolean isProcessed(Set<Project> resultset, FileObject projectDir) {

        for (Project p : resultset) {
            if (p.getProjectDirectory().equals(projectDir)) {
                return true;
            }
        }

        return false;
    }

    private void addProjectModules(File basedir, Set<Project> resultset, List modules) throws InterruptedException {
        if (modules == null || modules.size() == 0) {
            return;
        }
        Iterator it = modules.iterator();
        while (it.hasNext()) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            String path = (String) it.next();
            File sub = new File(basedir, path);
            File projectFile = FileUtil.normalizeFile(sub);
            if (projectFile.exists()) {
                FileObject projectDir = FileUtil.toFileObject(projectFile);
                if (projectDir != null && !isProcessed(resultset, projectDir)) {
                    Project proj = processOneSubproject(projectDir);
                    NbMavenProjectImpl mv = proj != null ? proj.getLookup().lookup(NbMavenProjectImpl.class) : null;
                    if (mv != null) {
                        // ignore the pom type projects when resolving subprojects..
                        // maybe make an user settable option??
                        if (!NbMavenProject.TYPE_POM.equalsIgnoreCase(mv.getProjectWatcher().getPackagingType())) {
                            resultset.add(proj);
                        }
                        addProjectModules(FileUtil.toFile(mv.getProjectDirectory()),
                                resultset, mv.getOriginalMavenProject().getModules());
                    }
                } else {
                    // HUH?
                    ErrorManager.getDefault().log("fileobject not found=" + sub); //NOI18N
                }

            } else {
                ErrorManager.getDefault().log("project file not found=" + sub); //NOI18N
            }



        }
    }

    private Project processOneSubproject(FileObject projectDir) {


        try {
            return ProjectManager.getDefault().findProject(projectDir);
        } catch (IOException exc) {
            ErrorManager.getDefault().notify(exc);
        }

        return null;
    }

    public synchronized void addChangeListener(ChangeListener changeListener) {
        if (listeners.size() == 0) {
            watcher.addPropertyChangeListener(propertyChange);
        }
        listeners.add(changeListener);
    }

    public synchronized void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
        if (listeners.size() == 0) {
            watcher.removePropertyChangeListener(propertyChange);
        }
    }

    private void fireChange() {
        List<ChangeListener> lists = new ArrayList<ChangeListener>();
        synchronized (this) {
            lists.addAll(listeners);
        }
        for (ChangeListener listener : lists) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }
}

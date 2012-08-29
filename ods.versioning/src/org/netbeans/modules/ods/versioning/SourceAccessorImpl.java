/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ods.versioning;

import com.tasktop.c2c.server.scm.domain.ScmLocation;
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import com.tasktop.c2c.server.scm.domain.ScmType;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.favorites.api.Favorites;
import org.netbeans.modules.ods.api.ODSProject;
import org.netbeans.modules.ods.client.api.ODSException;
import org.netbeans.modules.ods.ui.spi.VCSAccessor;
import org.netbeans.modules.ods.versioning.spi.ApiProvider;
import org.netbeans.modules.team.ui.common.NbProjectHandleImpl;
import org.netbeans.modules.team.ui.spi.NbProjectHandle;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.SourceAccessor;
import org.netbeans.modules.team.ui.spi.SourceHandle;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.openide.windows.WindowManager;

/**
 *
 * @author Milan Kubec, Jan Becicka, Tomas Stupka
 */

@ServiceProviders( { @ServiceProvider(service=SourceAccessor.class),
    @ServiceProvider(service=VCSAccessor.class)
})
public class SourceAccessorImpl extends VCSAccessor {

    public SourceAccessorImpl() { }
    
    @Override
    public Class<ODSProject> type() {
        return ODSProject.class;
    }
    
    @Override
    public List<SourceHandle> getSources(ProjectHandle<ODSProject> prjHandle) {
        return getSources(prjHandle, null, false);
    }

    @Override
    public Action getOpenSourcesAction(SourceHandle srcHandle) {
        return getDefaultAction(srcHandle);
    }

    @Override
    public Action getDefaultAction(SourceHandle srcHandle) {
        assert srcHandle instanceof SourceHandleImpl;
        SourceHandleImpl impl = (SourceHandleImpl) srcHandle;
        return new GetSourcesFromCloudAction(new ProjectAndRepository(impl.getProjectHandle(), impl.getRepository()), impl);
    }

    @Override
    public Action getDefaultAction(final NbProjectHandle prj) {
        return new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Project project = ((NbProjectHandleImpl) prj).getProject();
                if (project == null) {
                    ((NbProjectHandleImpl) prj).remove();
                } else {
                    OpenProjects.getDefault().open(new Project[]{project}, false);
                    WindowManager.getDefault().findTopComponent("projectTabLogical_tc").requestActive(); // NOI18N
                    selectProject(project);
                }
            }

            private void selectProject(final Project p) {
                final ExplorerManager em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("projectTabLogical_tc")).getExplorerManager(); // NOI18N

                Node root = em.getRootContext();
                // Node projNode = root.getChildren ().findChild( p.getProjectDirectory().getName () );
                Node projNode = null;
                for (Node n : root.getChildren().getNodes()) {
                    Project prj = n.getLookup().lookup(Project.class);
                    if (prj != null && prj.getProjectDirectory().equals(p.getProjectDirectory())) {
                        projNode = n;
                        break;
                    }
                }
                if (projNode == null) {
                    // fallback..
                    projNode = root.getChildren().findChild(ProjectUtils.getInformation(p).getName());
                }

                if (projNode != null) {
                    try {
                        em.setSelectedNodes(new Node[]{projNode});
                    } catch (Exception ignore) {
                        // may ignore it
                    }
                }

            }
        };
    }

    @Override
    public Action getOpenOtherAction(final SourceHandle src) {

        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectChooser.setProjectsFolder(src.getWorkingDirectory());
                JFileChooser chooser = ProjectChooser.projectChooser();
                chooser.setCurrentDirectory(src.getWorkingDirectory());
                chooser.setMultiSelectionEnabled(true);

                int option = chooser.showOpenDialog(WindowManager.getDefault().getMainWindow()); // Sow the chooser

                if (option == JFileChooser.APPROVE_OPTION) {

                    final File[] projectDirs;
                    if (chooser.isMultiSelectionEnabled()) {
                        projectDirs = chooser.getSelectedFiles();
                    } else {
                        projectDirs = new File[]{chooser.getSelectedFile()};
                    }

                    ArrayList<Project> projects = new ArrayList<Project>(projectDirs.length);
                    for (File d : projectDirs) {
                        try {
                            Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(d));
                            projects.add(p);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IllegalArgumentException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }

                    Project projectsArray[] = new Project[projects.size()];
                    projects.toArray(projectsArray);


                    OpenProjects.getDefault().open(
                            projectsArray, // Put the project into OpenProjectList
                            false);
                    WindowManager.getDefault().findTopComponent("projectTabLogical_tc").requestActive(); // NOI18N
                }
            }
        };
    }

    @Override
    public Action getOpenFavoritesAction(final SourceHandle src) {

        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WindowManager.getDefault().findTopComponent("favorites").requestActive(); // NOI18N
                try {
                    FileObject fo = FileUtil.toFileObject(src.getWorkingDirectory());
                    Favorites.getDefault().selectWithAddition(fo);
                } catch (IOException ex) {
                    printStackTrace(ex);
                } catch (IllegalArgumentException ex) {
                    printStackTrace(ex);
                } catch (NullPointerException ex) {
                    printStackTrace(ex);
                }
             }
        };
    }
    
    @Override
    public Action getOpenHistoryAction (ProjectHandle<ODSProject> prjHandle, String repositoryName, String commitId) {
        assert !EventQueue.isDispatchThread();
        List<SourceHandle> sources = getSources(prjHandle, repositoryName, true);
        Action action = null;
        if (!sources.isEmpty()) {
            SourceHandleImpl sourceHandle = (SourceHandleImpl) sources.get(0);
            final File workdir = sourceHandle.getWorkingDirectory();
            if (workdir != null) {
                ApiProvider[] providers = getProvidersFor(ScmType.GIT); // support only git for now
                for (ApiProvider p : providers) {
                    action = p.createOpenHistoryAction(workdir, commitId);
                    if (action != null) {
                        break;
                    }
                }
            }
        }
        return action;
    }

    private static void printStackTrace(Throwable t) {
        Logger.getLogger(SourceAccessorImpl.class.getName()).log(Level.FINE, t.getMessage(), t);
    }

    private List<SourceHandle> getSources (ProjectHandle<ODSProject> prjHandle, String repositoryName, boolean onlySupported) {
        ODSProject project = prjHandle.getTeamProject();
        List<SourceHandle> handlesList = new ArrayList<SourceHandle>();
        
        try {
            if (project != null) {
                Collection<ScmRepository> repositories = project.getRepositories();
                for (ScmRepository repository : repositories) {
                    if (repositoryName != null && !repositoryName.equals(repository.getName())) {
                        continue;
                    }
                    boolean supported;
                    if (repository.getScmLocation() == ScmLocation.CODE2CLOUD) {
                        supported = isSupported(repository.getType());
                    } else {
                        supported = isSupported(null);
                    }
                    if (onlySupported && !supported) {
                        continue;
                    }
                    SourceHandleImpl srcHandle = new SourceHandleImpl((ProjectHandle<ODSProject>)prjHandle, repository, supported);
                    handlesList.add(srcHandle);
                }
            }
        } catch (ODSException ex) {
            Logger.getLogger(SourceAccessorImpl.class.getName()).log(ex instanceof ODSException.ODSCanceledException
                    ? Level.FINE
                    : Level.INFO, prjHandle.getId(), ex);
        }
        
        return handlesList.isEmpty() ? Collections.<SourceHandle>emptyList() : handlesList;
    }

    public static class ProjectAndRepository {
        public ProjectHandle<ODSProject> project;
        public ScmRepository repository;
        public String externalScmType;
        public ProjectAndRepository(ProjectHandle<ODSProject> project, ScmRepository repository) {
            this.project = project;
            this.repository = repository;
        }
    }

    static boolean isSupported (ScmType type) {
        boolean supported;
        if (type == null) {
            supported = Lookup.getDefault().lookup(ApiProvider.class) != null;
        } else {
            supported = getProvidersFor(type).length > 0;
        }
        return supported;
    }

    static ApiProvider[] getProvidersFor (ScmType type) {
        Collection<? extends ApiProvider> allProviders = Lookup.getDefault().lookupAll(ApiProvider.class);
        List<ApiProvider> providers = new ArrayList<ApiProvider>(allProviders.size());
        for (ApiProvider prov : allProviders) {
            if (type == null || prov.accepts(type.name())) {
                providers.add(prov);
            }
        }
        return providers.toArray(new ApiProvider[providers.size()]);
    }
}

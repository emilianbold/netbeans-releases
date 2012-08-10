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

package org.netbeans.modules.kenai.ui;

import org.netbeans.modules.team.ui.common.NbProjectHandleImpl;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.ui.api.KenaiServer;
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
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author Milan Kubec, Jan Becicka
 */
@ServiceProvider(service=SourceAccessor.class)
public class SourceAccessorImpl extends SourceAccessor<KenaiProject> {
    private static SourceAccessor instance;

    public static SourceAccessor getDefault() {
        if(instance == null) {
            instance = new SourceAccessorImpl();
        }
        return instance;
    }

    private Map<SourceHandle,ProjectAndFeature> handlesMap = new HashMap<SourceHandle,ProjectAndFeature>();

    @Override
    public Class<KenaiProject> type() {
        return KenaiProject.class;
    }
    
    @Override
    public List<SourceHandle> getSources(ProjectHandle<KenaiProject> prjHandle) {

        KenaiProject project = prjHandle.getTeamProject();
        List<SourceHandle> handlesList = new ArrayList<SourceHandle>();

        if (project != null) {
            try {
                for (KenaiFeature feature : project.getFeatures(Type.SOURCE)) {
                    SourceHandle srcHandle = new SourceHandleImpl(prjHandle, feature);
                    handlesList.add(srcHandle);
                    handlesMap.put(srcHandle, new ProjectAndFeature(prjHandle.getTeamProject(), feature, ((SourceHandleImpl) srcHandle).getExternalScmType()));
                }
            } catch (KenaiException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return handlesList.isEmpty() ? Collections.<SourceHandle>emptyList() : handlesList;

    }

    @Override
    public Action getOpenSourcesAction(SourceHandle srcHandle) {
        return new GetSourcesFromKenaiAction(handlesMap.get(srcHandle), srcHandle);
    }

    @Override
    public Action getDefaultAction(SourceHandle srcHandle) {
        return new GetSourcesFromKenaiAction(handlesMap.get(srcHandle), srcHandle);
    }

    @Override
    public Action getDefaultAction(final NbProjectHandle prj) {
        return new AbstractAction() {

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
            public void actionPerformed(ActionEvent e) {
                ProjectChooser.setProjectsFolder(src.getWorkingDirectory());
                JFileChooser chooser = ProjectChooser.projectChooser();
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

    private static void printStackTrace(Throwable t) {
        Logger.getLogger(SourceAccessorImpl.class.getName()).log(Level.FINE, t.getMessage(), t);
    }


    public static class ProjectAndFeature {

        public KenaiProject kenaiProject;
        public KenaiFeature feature;
        public String externalScmType;

        public ProjectAndFeature(KenaiProject name, KenaiFeature ftr, String externalScmType) {
            kenaiProject = name;
            feature = ftr;
            this.externalScmType=externalScmType;
        }
    }

}

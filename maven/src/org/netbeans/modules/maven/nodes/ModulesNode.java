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

package org.netbeans.modules.maven.nodes;

import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;

/**
 * display the modules for pom packaged project
 * @author Milos Kleint 
 */
public class ModulesNode extends AbstractNode {

    /** Creates a new instance of ModulesNode */
    public ModulesNode(NbMavenProjectImpl proj) {
        super(new ModulesChildren(proj));
        setName("Modules"); //NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(ModulesNode.class, "LBL_Modules"));
    }

    @Override
    public Action[] getActions(boolean bool) {
        return new Action[]{};
    }

    private Image getIcon(boolean opened) {
        Image badge = ImageUtilities.loadImage("org/netbeans/modules/maven/modules-badge.png", true); //NOI18N
        return ImageUtilities.mergeImages(NodeUtils.getTreeFolderIcon(opened), badge, 8, 8);
    }

    @Override
    public Image getIcon(int type) {
        return getIcon(false);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(true);
    }


    static class ModulesChildren extends Children.Keys<NbMavenProjectImpl> {

        private NbMavenProjectImpl project;
        private PropertyChangeListener listener;

        ModulesChildren(NbMavenProjectImpl proj) {
            project = proj;
            listener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
                        loadModules();
                    }
                }
            };
        }

        @Override
        public void addNotify() {
            setKeys(Collections.<NbMavenProjectImpl>emptyList());
            loadModules();
            NbMavenProject.addPropertyChangeListener(project, listener);
        }

        @Override
        public void removeNotify() {
            NbMavenProject.removePropertyChangeListener(project, listener);
            setKeys(Collections.<NbMavenProjectImpl>emptyList());
        }

        protected Node[] createNodes(NbMavenProjectImpl proj) {
            boolean isPom = "pom".equals(proj.getOriginalMavenProject().getPackaging());
            LogicalViewProvider prov = proj.getLookup().lookup(LogicalViewProvider.class);
            return new Node[]{new ProjectFilterNode(project, proj, prov.createLogicalView(), isPom)};
        }

        private void loadModules() {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    Collection<NbMavenProjectImpl> modules = new ArrayList<NbMavenProjectImpl>();
                    File base = project.getOriginalMavenProject().getBasedir();
                    for (Iterator it = project.getOriginalMavenProject().getModules().iterator(); it.hasNext();) {
                        String elem = (String) it.next();
                        File projDir = FileUtil.normalizeFile(new File(base, elem));
                        FileObject fo = FileUtil.toFileObject(projDir);
                        if (fo != null) {
                            try {
                                Project prj = ProjectManager.getDefault().findProject(fo);
                                if (prj != null && prj.getLookup().lookup(NbMavenProjectImpl.class) != null) {
                                    modules.add((NbMavenProjectImpl) prj);
                                }
                            } catch (IllegalArgumentException ex) {
                                ex.printStackTrace();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            //TODO broken module reference.. show as such..
                        }
                    }
                    setKeys(modules);
                }
            });
        }
    }

    private static class ProjectFilterNode extends FilterNode {

        private NbMavenProjectImpl project;
        private NbMavenProjectImpl parent;

        ProjectFilterNode(NbMavenProjectImpl parent, NbMavenProjectImpl proj, Node original, boolean isPom) {
            super(original, isPom ? new ModulesChildren(proj) : Children.LEAF);
//            disableDelegation(DELEGATE_GET_ACTIONS);
            project = proj;
            this.parent = parent;
        }

        @Override
        public Action[] getActions(boolean b) {
            ArrayList<Action> lst = new ArrayList<Action>();
            lst.add(new OpenProjectAction(project));
            lst.add(new RemoveModuleAction(parent, project));
//            lst.addAll(Arrays.asList(super.getActions(b)));
            return lst.toArray(new Action[lst.size()]);
        }

        @Override
        public Action getPreferredAction() {
            return new OpenProjectAction(project);
        }
    }

    private static class RemoveModuleAction extends AbstractAction {

        private NbMavenProjectImpl project;
        private NbMavenProjectImpl parent;

        public RemoveModuleAction(NbMavenProjectImpl parent, NbMavenProjectImpl proj) {
            putValue(Action.NAME, org.openide.util.NbBundle.getMessage(ModulesNode.class, "BTN_Remove_Module"));
            project = proj;
            this.parent = parent;
        }

        public void actionPerformed(ActionEvent e) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(org.openide.util.NbBundle.getMessage(ModulesNode.class, "MSG_Remove_Module"), NotifyDescriptor.YES_NO_OPTION);
            Object ret = DialogDisplayer.getDefault().notify(nd);
            if (ret == NotifyDescriptor.YES_OPTION) {
                FileObject fo = FileUtil.toFileObject(parent.getPOMFile());
                ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                    public void performOperation(POMModel model) {
                        List<String> modules = model.getProject().getModules();
                        if (modules != null) {
                            for (String path : modules) {
                                File rel = new File(parent.getPOMFile().getParent(), path);
                                File norm = FileUtil.normalizeFile(rel);
                                FileObject folder = FileUtil.toFileObject(norm);
                                if (folder != null && folder.equals(project.getProjectDirectory())) {
                                    model.startTransaction();
                                    model.getProject().removeModule(path);
                                    break;
                                }
                            }
                        }
                    }
                };
                org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(fo, Collections.singletonList(operation));
                //TODO is the manual reload necessary if pom.xml file is being saved?
                NbMavenProject.fireMavenProjectReload(project);
            }
        }
    }

    private static class OpenProjectAction extends AbstractAction implements Runnable {

        private NbMavenProjectImpl project;

        public OpenProjectAction(NbMavenProjectImpl proj) {
            putValue(Action.NAME, org.openide.util.NbBundle.getMessage(ModulesNode.class, "BTN_Open_Project"));
            project = proj;
        }

        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(this);
        }

        public void run() {
            OpenProjects.getDefault().open(new Project[]{project}, false);
        }
    }
}

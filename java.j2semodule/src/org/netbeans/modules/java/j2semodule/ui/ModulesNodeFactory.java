/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.j2semodule.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.java.j2semodule.J2SEModularProject;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Dusan Balek
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-java-j2semodule", position=200)
public final class ModulesNodeFactory implements NodeFactory {

    private static final Logger LOG = Logger.getLogger(ModulesNodeFactory.class.getName());

    @Override
    public NodeList createNodes(Project p) {
        J2SEModularProject project = p.getLookup().lookup(J2SEModularProject.class);
        assert project != null;
        return new ModulesNodeList(project);
    }

    private static class ModulesNodeList implements NodeList<FileObject>, ChangeListener {

        private final J2SEModularProject project;
        private final FileChangeListener rootsListener;
        private final List<File> listensOn = Collections.synchronizedList(new LinkedList<>());
        private final Runnable changeTask;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public ModulesNodeList(J2SEModularProject project) {
            this.project = project;
            changeTask = () -> {
                stateChanged(null);
            };
            rootsListener = new FileChangeAdapter() {
                @Override
                public void fileFolderCreated(FileEvent fe) {
                    fe.runWhenDeliveryOver(changeTask);
                }
                @Override
                public void fileDataCreated(FileEvent fe) {
                    fe.runWhenDeliveryOver(changeTask);
                }
                @Override
                public void fileDeleted(FileEvent fe) {
                    fe.runWhenDeliveryOver(changeTask);
                }
            };
        }

        @Override
        public List<FileObject> keys() {
            List<FileObject> result = new ArrayList<>();

            File[] removeFrom;
            synchronized (listensOn) {
                removeFrom = listensOn.toArray(new File[listensOn.size()]);
                listensOn.clear();
            }
            for (File file : removeFrom) {
                FileUtil.removeFileChangeListener(rootsListener, file);
            }

            if (this.project.getProjectDirectory() == null || !this.project.getProjectDirectory().isValid()) {
                return Collections.emptyList();
            }
            final SourceGroup[] groups = ProjectUtils.getSources(this.project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_MODULES);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE,
                        "Module source groups: {0}",  //NOI18N
                        Arrays.toString(groups));
            }

            final VisibilityQuery vq = VisibilityQuery.getDefault();
            for (SourceGroup group : groups) {
                FileObject root = group.getRootFolder();
                final File rootFile = FileUtil.toFile(root);
                if (rootFile == null) {
                    continue;
                }
                FileUtil.addFileChangeListener(rootsListener, rootFile);
                listensOn.add(rootFile);

                for (FileObject child : root.getChildren()) {
                    if (!child.isFolder() || !child.isValid()) {
                        continue;
                    }
                    if (!vq.isVisible(child)) {
                        continue;
                    }
                    if (child.getFileObject("module-info", "java") == null) {
                        continue;
                    }
                    final File childFile = FileUtil.toFile(child);
                    if (childFile == null) {
                        continue;
                    }
                    result.add(child);
                }
            }
            return result;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        @Override
        public Node node(FileObject key) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void addNotify() {
            ProjectUtils.getSources(this.project).addChangeListener(this);
        }

        @Override
        public void removeNotify() {
            ProjectUtils.getSources(this.project).removeChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(changeSupport::fireChange);
        }
    }    
}

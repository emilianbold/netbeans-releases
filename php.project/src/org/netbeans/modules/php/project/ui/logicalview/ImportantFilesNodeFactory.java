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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.logicalview;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 * @author Tomas Mysik
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-php-project", position=200)
public class ImportantFilesNodeFactory implements NodeFactory {
    static final Logger LOGGER = Logger.getLogger(ImportantFilesNodeFactory.class.getName());

    public ImportantFilesNodeFactory() {
    }

    @Override
    public NodeList<?> createNodes(Project p) {
        final PhpProject project = p.getLookup().lookup(PhpProject.class);
        return new ImportantFilesChildrenList(project);
    }

    private static class ImportantFilesChildrenList implements NodeList<Node>, PropertyChangeListener {
        private final PhpProject project;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public ImportantFilesChildrenList(PhpProject project) {
            this.project = project;
        }

        @Override
        public void addNotify() {
            ProjectPropertiesSupport.addProjectPropertyChangeListener(project, this);
        }

        @Override
        public void removeNotify() {
            ProjectPropertiesSupport.removeProjectPropertyChangeListener(project, this);
        }

        @Override
        public List<Node> keys() {
            if (project.hasConfigFiles()) {
                return Collections.<Node>singletonList(new Nodes.DummyNode(new ImportantFilesRootNode(project, new ImportantFilesChildFactory(project))));
            }
            return Collections.emptyList();
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
        public Node node(Node key) {
            return key;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (PhpProject.PROP_FRAMEWORKS.equals(evt.getPropertyName())) {
                // avoid deadlocks
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        fireChange();
                    }
                });
            }
        }

        void fireChange() {
            changeSupport.fireChange();
        }
    }

    private static class ImportantFilesRootNode extends AbstractNode implements PropertyChangeListener {

        @StaticResource
        private static final String CONFIG_BADGE_IMAGE = "org/netbeans/modules/php/project/ui/resources/config-badge.gif"; // NOI18N

        final ImportantFilesChildFactory childFactory;


        public ImportantFilesRootNode(PhpProject project, ImportantFilesChildFactory childFactory) {
            super(Children.create(childFactory, true));

            this.childFactory = childFactory;

            ProjectPropertiesSupport.addWeakProjectPropertyChangeListener(project, this);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(ImportantFilesNodeFactory.class, "LBL_ImportantFiles");
        }

        @Override
        public Image getIcon(int type) {
            return getIcon(true);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(false);
        }

        private Image getIcon(boolean opened) {
            Image badge = ImageUtilities.loadImage(CONFIG_BADGE_IMAGE, false);
            return ImageUtilities.mergeImages(UiUtils.getTreeFolderIcon(opened), badge, 8, 8);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (PhpProject.PROP_FRAMEWORKS.equals(evt.getPropertyName())) {
                childFactory.refresh();
            }
        }
    }

    private static class ImportantFilesChildFactory extends ChildFactory.Detachable<Pair<PhpFrameworkProvider, FileObject>> {

        private static final RequestProcessor RP = new RequestProcessor(ImportantFilesChildFactory.class.getName(), Runtime.getRuntime().availableProcessors());
        static final int FILE_CHANGE_DELAY = 300; // ms

        private final PhpProject project;
        private final FileChangeListener fileChangeListener = new ImportantFilesListener();
        final RequestProcessor.Task fsChange = RP.create(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        });

        public ImportantFilesChildFactory(PhpProject project) {
            this.project = project;
        }

        @Override
        protected boolean createKeys(List<Pair<PhpFrameworkProvider, FileObject>> toPopulate) {
            toPopulate.addAll(getImportantFiles());
            return true;
        }

        @Override
        protected Node createNodeForKey(Pair<PhpFrameworkProvider, FileObject> key) {
            try {
                return new ImportantFileNode(key, ProjectPropertiesSupport.getSourcesDirectory(project));
            } catch (DataObjectNotFoundException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
            return null;
        }

        public void refresh() {
            refresh(false);
        }

        @Override
        public void addNotify() {
            super.addNotify();
            attachListener();
        }

        List<Pair<PhpFrameworkProvider, FileObject>> getImportantFiles() {
            List<Pair<PhpFrameworkProvider, FileObject>> files = new LinkedList<>();

            final PhpModule phpModule = project.getPhpModule();
            final PhpVisibilityQuery phpVisibilityQuery = PhpVisibilityQuery.forProject(project);
            for (PhpFrameworkProvider frameworkProvider : project.getFrameworks()) {
                for (File file : frameworkProvider.getConfigurationFiles(phpModule)) {
                    final FileObject fileObject = FileUtil.toFileObject(file);
                    // XXX non-existing files are simply ignored
                    if (fileObject != null) {
                        if (fileObject.isFolder()) {
                            Exception ex = new IllegalStateException("No folders allowed among configuration files ["
                                    + fileObject.getNameExt() + " for " + frameworkProvider.getIdentifier() + "]");
                            LOGGER.log(Level.INFO, ex.getMessage(), ex);
                            continue;
                        }
                        if (phpVisibilityQuery.isVisible(fileObject)) {
                            files.add(Pair.of(frameworkProvider, fileObject));
                        } else {
                            LOGGER.log(Level.INFO, "File {0} ignored (not visible)", fileObject.getPath());
                        }
                    }
                }
            }

            return files;
        }

        private void attachListener() {
            try {
                FileSystem fileSystem = ProjectPropertiesSupport.getSourcesDirectory(project).getFileSystem();
                fileSystem.addFileChangeListener(FileUtil.weakFileChangeListener(fileChangeListener, fileSystem));
            } catch (FileStateInvalidException exc) {
                LOGGER.log(Level.WARNING, exc.getMessage(), exc);
            }
        }

        void refreshNodes() {
            fsChange.schedule(FILE_CHANGE_DELAY);
        }

        private class ImportantFilesListener extends FileChangeAdapter {
            @Override
            public void fileRenamed(FileRenameEvent fe) {
                fileChange();
            }
            @Override
            public void fileDataCreated(FileEvent fe) {
                fileChange();
            }
            @Override
            public void fileDeleted(FileEvent fe) {
                fileChange();
            }
            private void fileChange() {
                refreshNodes();
            }
        };
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    private static final class ImportantFileNode extends FilterNode {
        private final Pair<PhpFrameworkProvider, FileObject> pair;
        private final FileObject sourceDir;

        public ImportantFileNode(Pair<PhpFrameworkProvider, FileObject> pair, FileObject sourceDir) throws DataObjectNotFoundException {
            super(DataObject.find(pair.second()).getNodeDelegate());
            this.pair = pair;
            this.sourceDir = sourceDir;
        }

        @Override
        public String getShortDescription() {
            String filepath = FileUtil.getRelativePath(sourceDir, pair.second());
            if (filepath == null) {
                filepath = FileUtil.getFileDisplayName(pair.second());
            }
            return NbBundle.getMessage(ImportantFileNode.class, "LBL_ImportantFileTooltip", filepath, pair.first().getName());
        }
    }
}

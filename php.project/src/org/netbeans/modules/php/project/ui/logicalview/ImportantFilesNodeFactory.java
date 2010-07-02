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
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

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
            ProjectPropertiesSupport.addPropertyChangeListener(project, this);
        }

        @Override
        public void removeNotify() {
            ProjectPropertiesSupport.removePropertyChangeListener(project, this);
        }

        @Override
        public List<Node> keys() {
            if (project.hasConfigFiles()) {
                return Collections.<Node>singletonList(new Nodes.DummyNode(new ImportantFilesRootNode(project)));
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
            if (PhpProject.PROP_CONFIG_FILES.equals(evt.getPropertyName())) {
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
        private final PhpProject project;

        public ImportantFilesRootNode(PhpProject project) {
            super(createChildren(project));
            this.project = project;

            ProjectPropertiesSupport.addWeakPropertyChangeListener(project, this);
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
            Image badge = ImageUtilities.loadImage("org/netbeans/modules/php/project/ui/resources/config-badge.gif", false); // NOI18N
            return ImageUtilities.mergeImages(UiUtils.getTreeFolderIcon(opened), badge, 8, 8);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (PhpProject.PROP_FRAMEWORKS.equals(evt.getPropertyName())) {
                // avoid deadlocks
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setChildren(createChildren(project));
                    }
                });
            }
        }

        private static Children createChildren(PhpProject project) {
            return Children.create(new ImportantFilesChildFactory(project), false);
        }
    }

    private static class ImportantFilesChildFactory extends Nodes.FileChildFactory {

        public ImportantFilesChildFactory(PhpProject project) {
            super(project);
        }

        @Override
        protected List<Node> getNodes() {
            List<Node> list = new LinkedList<Node>();
            final PhpModule phpModule = project.getPhpModule();
            for (PhpFrameworkProvider frameworkProvider : project.getFrameworks()) {
                for (File file : frameworkProvider.getConfigurationFiles(phpModule)) {
                    final FileObject fileObject = FileUtil.toFileObject(file);
                    // XXX non-existing files are simply ignored
                    if (fileObject != null) {
                        try {
                            list.add(new FileNode(DataObject.find(fileObject), project));
                        } catch (DataObjectNotFoundException ex) {
                            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                        }
                    }
                }
            }
            return list;
        }
    }

    private static class FileNode extends FilterNode {

        public FileNode(DataObject dataObject, PhpProject project) {
            super(dataObject.getNodeDelegate(), getChildren(dataObject, project));
        }

        private static org.openide.nodes.Children getChildren(DataObject dobj, PhpProject project) {
            if (dobj instanceof DataFolder) {
                return new Nodes.DummyChildren(dobj.getNodeDelegate(), new PhpSourcesFilter(project)) {
                    @Override
                    protected Node copyNode(Node originalNode) {
                        return originalNode.cloneNode();
                    }
                };
            }
            return Children.LEAF;
        }
    }
}

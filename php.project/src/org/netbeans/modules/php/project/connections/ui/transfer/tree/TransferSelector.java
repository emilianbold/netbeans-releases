/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections.ui.transfer.tree;

import java.awt.BorderLayout;
import java.awt.Image;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.UIManager;
import org.netbeans.modules.php.project.connections.TransferFile;
import org.netbeans.modules.php.project.connections.ui.transfer.TransferFilesChooser.TransferType;
import org.netbeans.modules.php.project.connections.ui.transfer.TransferFilesChooserPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

public final class TransferSelector extends TransferFilesChooserPanel implements ExplorerManager.Provider {
    private static final long serialVersionUID = 8754314324676665313L;

    private final TransferType transferType;
    private final TransferSelectorModel model;
    private final ExplorerManager explorerManager;


    public TransferSelector(Set<TransferFile> transferFiles, TransferType transferType, long timestamp) {
        this.transferType = transferType;

        model = new TransferSelectorModel(transferFiles, timestamp);
        explorerManager = new ExplorerManager();

        RootChildren rootChildren = new RootChildren(transferFiles);
        explorerManager.setRootContext(new RootNode(rootChildren));

        initComponents();

        CheckTreeView treeView = new CheckTreeView(model);
        treeView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TransferSelector.class, "ACSN_TransferFilesTree"));
        treeView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TransferSelector.class, "ACSD_TransferFilesTree"));
        add(treeView, BorderLayout.CENTER);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public void addChangeListener(TransferFilesChangeListener listener) {
        model.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(TransferFilesChangeListener listener) {
        model.removeChangeListener(listener);
    }

    @Override
    public Set<TransferFile> getSelectedFiles() {
        return model.getSelected();
    }

    @Override
    public TransferFilesChooserPanel getEmbeddablePanel() {
        return this;
    }

    @Override
    public boolean hasAnyTransferableFiles() {
        return !model.getData().isEmpty();
    }

    Node create(TransferFile transferFile) {
        if (transferFile.isDirectory()) {
            return new FolderNode(transferFile);
        }
        return new FileNode(transferFile);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    private static class CheckTreeView extends BeanTreeView {
        private static final long serialVersionUID = 9856432132154L;

        public CheckTreeView(TransferSelectorModel model) {
            CheckListener listener = new CheckListener(model);
            tree.addMouseListener(listener);
            tree.addKeyListener(listener);

            CheckRenderer renderer = new CheckRenderer(model);
            tree.setCellRenderer(renderer);

            tree.setEditable(false);
        }
    }

    private class FileNode extends AbstractNode {
        private static final String RESOURCE_ICON_FILE_DOWNLOAD = "org/netbeans/modules/php/project/ui/resources/fileDownload.gif"; // NOI18N
        private static final String RESOURCE_ICON_FILE_UPLOAD = "org/netbeans/modules/php/project/ui/resources/fileUpload.gif"; // NOI18N

        protected FileNode(TransferFile transferFile, Children children, Lookup lookup) {
            super(children, lookup);
            setDisplayName(transferFile.getName());
        }

        protected FileNode(TransferFile transferFile) {
            this(transferFile, Children.LEAF, Lookups.singleton(transferFile));
        }

        protected FileNode(Children children) {
            super(children);
        }

        @Override
        public boolean canCopy() {
            return false;
        }

        @Override
        public Image getIcon(int type) {
            return getIcon();
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon();
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }

        @Override
        public Action getPreferredAction() {
            return null;
        }

        private Image getIcon() {
            if (transferType.equals(TransferType.UPLOAD)) {
                return ImageUtilities.loadImage(RESOURCE_ICON_FILE_UPLOAD, false);
            }
            return ImageUtilities.loadImage(RESOURCE_ICON_FILE_DOWNLOAD, false);
        }
    }

    private class FolderNode extends FileNode {
        // see org.netbeans.swing.plaf.LFCustoms
        private static final String EXPLORER_FOLDER_ICON = "Nb.Explorer.Folder.icon"; // NOI18N
        private static final String EXPLORER_FOLDER_OPENED_ICON = "Nb.Explorer.Folder.openedIcon"; // NOI18N

        private static final String RESOURCE_ICON_FOLDER = "org/netbeans/modules/php/project/ui/resources/folder.gif"; // NOI18N
        private static final String RESOURCE_ICON_FOLDER_OPENED = "org/netbeans/modules/php/project/ui/resources/folderOpen.gif"; // NOI18N

        protected FolderNode(TransferFile transferFile) {
            super(transferFile, new FileChildren(transferFile), Lookups.singleton(transferFile));
        }

        protected FolderNode(Children children) {
            super(children);
        }

        @Override
        public Image getIcon(int type) {
            Object icon = UIManager.get(EXPLORER_FOLDER_ICON);
            if (icon instanceof Image) {
                return (Image) icon;
            }
            return ImageUtilities.loadImage(RESOURCE_ICON_FOLDER, false);
        }

        @Override
        public Image getOpenedIcon(int type) {
            Object icon = UIManager.get(EXPLORER_FOLDER_OPENED_ICON);
            if (icon instanceof Image) {
                return (Image) icon;
            }
            return ImageUtilities.loadImage(RESOURCE_ICON_FOLDER_OPENED, false);
        }
    }

    private class RootNode extends FolderNode {

        public RootNode(RootChildren children) {
            super(children);

            String nameKey = null;
            if (children.hasProjectRoot()) {
                nameKey = "LBL_SourceFiles"; // NOI18N
            } else {
                nameKey = "LBL_SelectFilesForTransfer"; // NOI18N
            }
            setDisplayName(NbBundle.getMessage(TransferSelector.class, nameKey));
        }
    }

    private class RootChildren extends Children.Keys<TransferFile> {
        private final boolean projectRoot;

        public RootChildren(Set<TransferFile> transferFiles) {
            // do not allow select source-dir
            boolean projRoot = false;
            List<TransferFile> roots = new LinkedList<TransferFile>();
            for (TransferFile file : transferFiles) {
                if (file.isProjectRoot()) {
                    roots.clear();
                    roots.addAll(file.getChildren());
                    projRoot = true;
                    break;
                }
                if (file.isRoot()) {
                    roots.add(file);
                }
            }
            projectRoot = projRoot;

            Collections.sort(roots, new TransferFileComparator());
            setKeys(roots);
        }

        public boolean hasProjectRoot() {
            return projectRoot;
        }

        @Override
        protected Node[] createNodes(TransferFile file) {
            return new Node[] {TransferSelector.this.create(file)};
        }
    }

    private class FileChildren extends Children.Keys<TransferFile> {

        public FileChildren(TransferFile transferFile) {
            List<TransferFile> children = transferFile.getChildren();
            Collections.sort(children, new TransferFileComparator());
            setKeys(children);
        }

        @Override
        protected Node[] createNodes(TransferFile file) {
            return new Node[] {TransferSelector.this.create(file)};
        }
    }

    private static class TransferFileComparator implements Comparator<TransferFile> {

        @Override
        public int compare(TransferFile o1, TransferFile o2) {
            boolean isDir1 = o1.isDirectory();
            boolean isDir2 = o2.isDirectory();
            if ((isDir1 && isDir2)
                    || (!isDir1 && !isDir2)) {
                return o1.getName().compareTo(o2.getName());
            }
            if (isDir1) {
                return -1;
            }
            return 1;
        }
    }
}

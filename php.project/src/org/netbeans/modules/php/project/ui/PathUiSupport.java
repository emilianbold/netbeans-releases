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

package org.netbeans.modules.php.project.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.ButtonModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.ant.FileChooser;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.classpath.BasePathSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;

/**
 * @author Petr Hrebejk, Tomas Mysik
 */
public final class PathUiSupport {

    private PathUiSupport() {
    }

    public static DefaultListModel createListModel(Iterator<BasePathSupport.Item> it) {
        DefaultListModel model = new DefaultListModel();
        while (it.hasNext()) {
            model.addElement(it.next());
        }
        return model;
    }

    public static Iterator<BasePathSupport.Item> getIterator(DefaultListModel model) {
        // XXX Better performing impl. would be nice
        return getList(model).iterator();
    }

    public static List<BasePathSupport.Item> getList(DefaultListModel model) {
        return Collections.list(NbCollections.checkedEnumerationByFilter(model.elements(),
                BasePathSupport.Item.class, true));
    }

    /** Moves items up in the list. The indices array will contain
     * indices to be selected after the change was done.
     */
    public static int[] moveUp(DefaultListModel listModel, int[] indices) {

        if (indices == null || indices.length == 0) {
            assert false : "MoveUp button should be disabled";
        }

        // Move the items up
        for (int i = 0; i < indices.length; i++) {
            Object item = listModel.get(indices[i]);
            listModel.remove(indices[i]);
            listModel.add(indices[i] - 1, item);
        }

        // Keep the selection a before
        for (int i = 0; i < indices.length; i++) {
            indices[i] -= 1;
        }
        return indices;

    }

    public static boolean canMoveUp(ListSelectionModel selectionModel) {
        return selectionModel.getMinSelectionIndex() > 0;
    }

    /** Moves items down in the list. The indices array will contain
     * indices to be selected after the change was done.
     */
    public static int[] moveDown(DefaultListModel listModel, int[] indices) {

        if (indices == null || indices.length == 0) {
            assert false : "MoveDown button should be disabled";
        }

        // Move the items up
        for (int i = indices.length - 1; i >= 0; i--) {
            Object item = listModel.get(indices[i]);
            listModel.remove(indices[i]);
            listModel.add(indices[i] + 1, item);
        }

        // Keep the selection a before
        for (int i = 0; i < indices.length; i++) {
            indices[i] += 1;
        }
        return indices;

    }

    public static boolean canMoveDown(ListSelectionModel selectionModel, int modelSize) {
        int iMax = selectionModel.getMaxSelectionIndex();
        return iMax != -1 && iMax < modelSize - 1;
    }

    /** Removes selected indices from the model. Returns the index to be selected
     */
    public static int[] remove(DefaultListModel listModel, int[] indices) {

        if (indices == null || indices.length == 0) {
            assert false : "Remove button should be disabled";
        }

        // Remove the items
        for (int i = indices.length - 1; i >= 0; i--) {
            listModel.remove(indices[i]);
        }

        if (!listModel.isEmpty()) {
            // Select reasonable item
            int selectedIndex = indices[indices.length - 1] - indices.length  + 1;
            if (selectedIndex > listModel.size() - 1) {
                selectedIndex = listModel.size() - 1;
            }
            return new int[] {selectedIndex};
        }
        return new int[] {};
    }

    public static int[] addFolders(DefaultListModel listModel, int[] indices, String[] files) {

        int lastIndex = indices == null || indices.length == 0 ? listModel.getSize() - 1 : indices[indices.length - 1];
        int[] indexes = new int[files.length];
        for (int i = 0, delta = 0; i + delta < files.length;) {
            int current = lastIndex + 1 + i;
            BasePathSupport.Item item = BasePathSupport.Item.create(files[i + delta], null);
            if (!listModel.contains(item)) {
                listModel.add(current, item);
                indexes[delta + i] = current;
                i++;
            } else {
                indexes[i + delta] = listModel.indexOf(item);
                delta++;
            }
        }
        return indexes;
    }

    public static class ClassPathListCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 619725480128831307L;

        private static final String RESOURCE_ICON_BROKEN_BADGE
                = "org/netbeans/modules/php/project/ui/resources/brokenProjectBadge.gif"; //NOI18N
        private static final String RESOURCE_ICON_CLASSPATH
                = "org/netbeans/modules/php/project/ui/resources/referencedClasspath.gif"; //NOI18N

        private static final ImageIcon ICON_BROKEN_BADGE = ImageUtilities.loadImageIcon(RESOURCE_ICON_BROKEN_BADGE, false);
        private static final ImageIcon ICON_CLASSPATH = ImageUtilities.loadImageIcon(RESOURCE_ICON_CLASSPATH, false);
        private static ImageIcon ICON_FOLDER = null;
        private static ImageIcon ICON_BROKEN_FOLDER = null;

        private final PropertyEvaluator evaluator;
        private final FileObject projectFolder;

        private static final Map<String, String> WELL_KNOWN_PATHS_NAMES = new HashMap<String, String>();
        static {
            WELL_KNOWN_PATHS_NAMES.put(PhpProjectProperties.GLOBAL_INCLUDE_PATH,
                    NbBundle.getMessage(PathUiSupport.class, "LBL_GlobalIncludePath_DisplayName"));
        };

        // used for global include path (no evaluator, no project folder)
        public ClassPathListCellRenderer() {
            this(null, null);
        }

        public ClassPathListCellRenderer(PropertyEvaluator evaluator, FileObject projectFolder) {
            super();

            this.evaluator = evaluator;
            this.projectFolder = projectFolder;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            BasePathSupport.Item item = (BasePathSupport.Item) value;

            super.getListCellRendererComponent(list, getDisplayName(item), index, isSelected, cellHasFocus);
            setIcon(getIcon(item));
            setToolTipText(getToolTipText(item));
            return this;
        }

        private String getDisplayName(BasePathSupport.Item item) {
            switch (item.getType()) {
                case CLASSPATH:
                    String name = WELL_KNOWN_PATHS_NAMES.get(BasePathSupport.getAntPropertyName(item.getReference()));
                    return name == null ? item.getReference() : name;
                    //break;
                default:
                    if (item.isBroken()) {
                        return NbBundle.getMessage(PathUiSupport.class, "LBL_MissingFile", getFileRefName(item));
                    }
                    File f = new File(item.getFilePath());
                    if (f.isAbsolute()) {
                        return f.getAbsolutePath();
                    }
                    return PropertyUtils.resolveFile(FileUtil.toFile(projectFolder), item.getFilePath()).getAbsolutePath();
                    //break;
            }
        }

        private static Icon getIcon(BasePathSupport.Item item) {
            switch (item.getType()) {
                case CLASSPATH:
                    return ICON_CLASSPATH;
                    //break;
                default:
                    if (item.isBroken()) {
                        if (ICON_BROKEN_FOLDER == null) {
                            ICON_BROKEN_FOLDER = new ImageIcon(ImageUtilities.mergeImages(getFolderIcon().getImage(),
                                    ICON_BROKEN_BADGE.getImage(), 7, 7));
                        }
                        return ICON_BROKEN_FOLDER;
                    }
                    return getFolderIcon();
                    //break;
            }
        }

        private String getToolTipText(BasePathSupport.Item item) {
            switch (item.getType()) {
                case FOLDER:
                    if (item.isBroken()) {
                        if (evaluator != null) {
                            return evaluator.evaluate(item.getReference());
                        }
                        return item.getReference();
                    }
                    String path = item.getFilePath();
                    File f = new File(path);
                    if (!f.isAbsolute()) {
                        assert projectFolder != null : "project folder cannot be null because not absolute path given [" + f + "]";
                        f = PropertyUtils.resolveFile(FileUtil.toFile(projectFolder), path);
                        return f.getAbsolutePath();
                    }
                    //break;
            }
            return null;
        }

        private static ImageIcon getFolderIcon() {
            if (ICON_FOLDER == null) {
                DataFolder dataFolder = DataFolder.findFolder(FileUtil.getConfigRoot());
                ICON_FOLDER = new ImageIcon(dataFolder.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
            }
            return ICON_FOLDER;
        }

        private String getFileRefName(BasePathSupport.Item item) {
            switch (item.getType()) {
                case FOLDER:
                    return item.getFilePath();
                    //break;
                default:
                    return item.getReference();
                    //break;
            }
        }
    }

    public static final class EditMediator implements ActionListener, ListSelectionListener {

        private final PhpProject project;
        private final JList list;
        private final DefaultListModel listModel;
        private final ListSelectionModel selectionModel;
        private final ButtonModel addFolder;
        private final ButtonModel remove;
        private final ButtonModel moveUp;
        private final ButtonModel moveDown;
        private final FileChooserDirectoryHandler directoryHandler;

        private EditMediator(JList list, ButtonModel addFolder,
                ButtonModel remove, ButtonModel moveUp, ButtonModel moveDown,
                FileChooserDirectoryHandler directoryHandler) {
            this(null, list, addFolder, remove, moveUp, moveDown, directoryHandler);
        }

        private EditMediator(PhpProject project, JList list, ButtonModel addFolder, ButtonModel remove, FileChooserDirectoryHandler directoryHandler) {
            this(project, list, addFolder, remove, null, null, directoryHandler);
        }

        private EditMediator(PhpProject project, JList list, ButtonModel addFolder,
                ButtonModel remove, ButtonModel moveUp, ButtonModel moveDown,
                FileChooserDirectoryHandler directoryHandler) {
            assert directoryHandler != null;

            this.list = list;
            if (!(list.getModel() instanceof DefaultListModel)) {
                throw new IllegalArgumentException("The list's model has to be of class DefaultListModel");
            }

            this.listModel = (DefaultListModel) list.getModel();
            this.selectionModel = list.getSelectionModel();

            this.addFolder = addFolder;
            this.remove = remove;
            this.moveUp = moveUp;
            this.moveDown = moveDown;

            this.project = project;
            this.directoryHandler = directoryHandler;
        }

        public static void register(PhpProject project, JList list, ButtonModel addFolder,
                ButtonModel remove, ButtonModel moveUp, ButtonModel moveDown,
                FileChooserDirectoryHandler directoryHandler) {

            EditMediator em = new EditMediator(project, list, addFolder, remove, moveUp, moveDown, directoryHandler);

            // Register the listener on all buttons
            addFolder.addActionListener(em);
            remove.addActionListener(em);
            moveUp.addActionListener(em);
            moveDown.addActionListener(em);
            // On list selection
            em.selectionModel.addListSelectionListener(em);
            // Set the initial state of the buttons
            em.valueChanged(null);
        }

        public static void register(PhpProject project, JList list, ButtonModel addFolder,
                ButtonModel remove, FileChooserDirectoryHandler directoryHandler) {

            EditMediator em = new EditMediator(project, list, addFolder, remove, directoryHandler);

            // Register the listener on all buttons
            addFolder.addActionListener(em);
            remove.addActionListener(em);
            // On list selection
            em.selectionModel.addListSelectionListener(em);
            // Set the initial state of the buttons
            em.valueChanged(null);
        }

        // for global include path (no project available)
        public static void register(JList list, ButtonModel addFolder,
                ButtonModel remove, ButtonModel moveUp, ButtonModel moveDown,
                FileChooserDirectoryHandler directoryHandler) {

            EditMediator em = new EditMediator(list, addFolder, remove, moveUp, moveDown, directoryHandler);

            // Register the listener on all buttons
            addFolder.addActionListener(em);
            remove.addActionListener(em);
            moveUp.addActionListener(em);
            moveDown.addActionListener(em);
            // On list selection
            em.selectionModel.addListSelectionListener(em);
            // Set the initial state of the buttons
            em.valueChanged(null);
        }

        /** Handles button events
         */
        public void actionPerformed(ActionEvent e) {

            Object source = e.getSource();
            if (source == addFolder) {
                addFolders();
            } else if (source == remove) {
                int[] newSelection = PathUiSupport.remove(listModel, list.getSelectedIndices());
                list.setSelectedIndices(newSelection);
            } else if (moveUp != null && source == moveUp) {
                int[] newSelection = PathUiSupport.moveUp(listModel, list.getSelectedIndices());
                list.setSelectedIndices(newSelection);
            } else if (moveDown != null && source == moveDown) {
                int[] newSelection = PathUiSupport.moveDown(listModel, list.getSelectedIndices());
                list.setSelectedIndices(newSelection);
            }
        }

        /** Handles changes in the selection
         */
        public void valueChanged(ListSelectionEvent e) {
            // addFolder allways enabled
            remove.setEnabled(selectionModel.getMinSelectionIndex() != -1);
            if (moveUp != null) {
                moveUp.setEnabled(PathUiSupport.canMoveUp(selectionModel));
            }
            if (moveDown != null) {
                moveDown.setEnabled(PathUiSupport.canMoveDown(selectionModel, listModel.getSize()));
            }
        }

        private void addFolders() {
            JFileChooser chooser = null;
            if (project != null) {
                chooser = new FileChooser(project.getHelper(), false);
            } else {
                // XXX maybe select fs root
                chooser = new JFileChooser();
            }
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setMultiSelectionEnabled(true);
            chooser.setDialogTitle(NbBundle.getMessage(PathUiSupport.class, "LBL_AddFolders_DialogTitle"));
            chooser.setCurrentDirectory(directoryHandler.getCurrentDirectory());
            int option = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(list));
            if (option == JFileChooser.APPROVE_OPTION) {
                String[] files;
                try {
                    if (chooser instanceof FileChooser) {
                        files = ((FileChooser) chooser).getSelectedPaths();
                    } else {
                        File[] selectedFiles = chooser.getSelectedFiles();
                        files = new String[selectedFiles.length];

                        for (int i = 0; i < selectedFiles.length; i++) {
                            files[i] = selectedFiles[i].getAbsolutePath();
                        }
                    }
                } catch (IOException ex) {
                    // TODO add localized message
                    Exceptions.printStackTrace(ex);
                    return;
                }

                int[] newSelection = PathUiSupport.addFolders(listModel, list.getSelectedIndices(), files);
                list.setSelectedIndices(newSelection);
                // remember last folder
                directoryHandler.setCurrentDirectory(chooser.getCurrentDirectory());
            }
        }

        public interface FileChooserDirectoryHandler {
            File getCurrentDirectory();
            void setCurrentDirectory(File currentDirectory);
        }
    }
}

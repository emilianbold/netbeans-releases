/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.makeproject.configurations.ui;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.cnd.api.utils.FileChooser;
import org.netbeans.modules.cnd.makeproject.ui.utils.ListEditorPanel;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.loaders.CoreElfObject;
import org.netbeans.modules.cnd.loaders.ExeObject;
import org.netbeans.modules.cnd.loaders.OrphanedElfObject;
import org.netbeans.modules.cnd.loaders.ShellDataObject;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.packaging.FileElement;
import org.netbeans.modules.cnd.makeproject.packaging.FileElement.FileType;
import org.netbeans.modules.cnd.makeproject.ui.utils.PathPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

public class PackagingFilesPanel extends ListEditorPanel {

    private String baseDir;
    private JTable targetList;
    private MyTableCellRenderer myTableCellRenderer = new MyTableCellRenderer();
    private JButton addButton;
    private JButton addFileOrDirectoryButton;
    private JButton addFilesButton;
    private PackagingFilesOuterPanel packagingFilesOuterPanel;

    public PackagingFilesPanel(List<FileElement> fileList, String baseDir) {
        super(fileList.toArray(), new JButton[]{new JButton(), new JButton(), new JButton()});
        this.baseDir = baseDir;
        this.addButton = extraButtons[0];
        this.addFileOrDirectoryButton = extraButtons[1];
        this.addFilesButton = extraButtons[2];

        addButton.setText("Add [Empty]");
        addButton.addActionListener(new AddButtonAction());
        addFileOrDirectoryButton.setText("Add File");
        addFileOrDirectoryButton.addActionListener(new AddFileOrDirectoryButtonAction());
        addFilesButton.setText("Add Files from Directory");
        addFilesButton.addActionListener(new AddFilesButtonAction());

        getEditButton().setVisible(false);
        getDefaultButton().setVisible(false);
    }
    
    public void setOuterPanel(PackagingFilesOuterPanel packagingFilesOuterPanel) {
        this.packagingFilesOuterPanel = packagingFilesOuterPanel;
    }

    class AddButtonAction implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            addObjectAction(new FileElement(FileType.UNKNOWN, "", "")); // FIXUP
        }
    }

    class AddFileOrDirectoryButtonAction implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            String seed = null;
            if (FileChooser.getCurrectChooserFile() != null) {
                seed = FileChooser.getCurrectChooserFile().getPath();
            }
            if (seed == null) {
                seed = baseDir;
            }
            FileChooser fileChooser = new FileChooser("File", "Select", FileChooser.FILES_ONLY, null, seed, false);
            PathPanel pathPanel = new PathPanel();
            fileChooser.setAccessory(pathPanel);
            fileChooser.setMultiSelectionEnabled(true);
            int ret = fileChooser.showOpenDialog(null); // FIXUP
            if (ret == FileChooser.CANCEL_OPTION) {
                return;
            }
            File[] files = fileChooser.getSelectedFiles();
            for (int i = 0; i < files.length; i++) {
                String itemPath;
                if (PathPanel.getMode() == PathPanel.REL_OR_ABS) {
                    itemPath = IpeUtils.toAbsoluteOrRelativePath(baseDir, files[i].getPath());
                } else if (PathPanel.getMode() == PathPanel.REL) {
                    itemPath = IpeUtils.toRelativePath(baseDir, files[i].getPath());
                } else {
                    itemPath = files[i].getPath();
                }
                itemPath = FilePathAdaptor.mapToRemote(itemPath);
                itemPath = FilePathAdaptor.normalize(itemPath);
                String perm;
                if (files[i].getName().endsWith(".exe") || files[i].isDirectory() || isExecutable(files[i])) {
                    perm = packagingFilesOuterPanel.getDirPermTextField().getText();
                }
                else {
                    perm = packagingFilesOuterPanel.getFilePermTextField().getText();
                }
                addObjectAction(new FileElement(
                        FileType.FILE,
                        itemPath,
                        files[i].getName(),
                        perm,
                        packagingFilesOuterPanel.getOwnerTextField().getText(),
                        packagingFilesOuterPanel.getGroupTextField().getText()
                )); // FIXUP: softlink
            }
        }
    }
    
    private boolean isExecutable(File file) {
        FileObject fo = null;
        try {
            fo = FileUtil.toFileObject(file.getCanonicalFile());
        } catch (IOException e) {
            return false;
        }
        
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(fo);
        } catch (DataObjectNotFoundException e) {
            return false;
        }
        if (dataObject instanceof ExeObject || dataObject instanceof ShellDataObject) {
            if (dataObject instanceof OrphanedElfObject || dataObject instanceof CoreElfObject) {
                return false;
            }
            return true;
        }
        return false;
    }
    
    class AddFilesButtonAction implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            String seed = null;
            if (FileChooser.getCurrectChooserFile() != null) {
                seed = FileChooser.getCurrectChooserFile().getPath();
            }
            if (seed == null) {
                seed = baseDir;
            }
            FileChooser fileChooser = new FileChooser("File", "Select", FileChooser.DIRECTORIES_ONLY, null, seed, false);
            PathPanel pathPanel = new PathPanel();
            fileChooser.setAccessory(pathPanel);
            fileChooser.setMultiSelectionEnabled(false);
            int ret = fileChooser.showOpenDialog(null); // FIXUP
            if (ret == FileChooser.CANCEL_OPTION) {
                return;
            }
            File dir = fileChooser.getSelectedFile();
            addFilesFromDirectory(dir, dir);
        }
        
        private void addFilesFromDirectory(File origDir, File dir) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    addFilesFromDirectory(origDir, files[i]);
                }
                else {
                    String path;
                    if (PathPanel.getMode() == PathPanel.REL_OR_ABS) {
                        path = IpeUtils.toAbsoluteOrRelativePath(baseDir, files[i].getPath());
                    } else if (PathPanel.getMode() == PathPanel.REL) {
                        path = IpeUtils.toRelativePath(baseDir, files[i].getPath());
                    } else {
                        path = files[i].getPath();
                    }
                    path = FilePathAdaptor.mapToRemote(path);
                    path = FilePathAdaptor.normalize(path);
                    String toFile = IpeUtils.toRelativePath(origDir.getParentFile().getAbsolutePath(), files[i].getPath());
                    toFile = FilePathAdaptor.mapToRemote(toFile);
                    toFile = FilePathAdaptor.normalize(toFile);
                    String perm;
                    if (files[i].getName().endsWith(".exe") || files[i].isDirectory() || isExecutable(files[i])) {
                        perm = packagingFilesOuterPanel.getDirPermTextField().getText();
                    }
                    else {
                        perm = packagingFilesOuterPanel.getFilePermTextField().getText();
                    }
                    addObjectAction(new FileElement(
                            FileType.FILE,
                            path,
                            toFile,
                            perm,
                            packagingFilesOuterPanel.getOwnerTextField().getText(),
                            packagingFilesOuterPanel.getGroupTextField().getText()
                    )); // FIXUP: softlink
                }
            }
        }
    }

    @Override
    public Object copyAction(Object o) {
        FileElement elem = (FileElement) o;
        return new FileElement(elem.getType(), new String(elem.getFrom()), new String(elem.getTo()));
    }

    @Override
    public String getCopyButtonText() {
        return "Duplicate";
    }

    @Override
    public String getListLabelText() {
        return "Files:";
    }

    // Overrides ListEditorPanel
    @Override
    public int getSelectedIndex() {
        int index = getTargetList().getSelectedRow();
        if (index >= 0 && index < listData.size()) {
            return index;
        } else {
            return 0;
        }
    }

    @Override
    protected void setSelectedIndex(int i) {
        getTargetList().getSelectionModel().setSelectionInterval(i, i);
    }

    @Override
    protected void setData(Vector data) {
        getTargetList().setModel(new MyTableModel());
        // Set column sizes
        getTargetList().getColumnModel().getColumn(0).setPreferredWidth(40);
        getTargetList().getColumnModel().getColumn(0).setMaxWidth(40);
        if (getTargetList().getColumnModel().getColumnCount() >= 4) {
            getTargetList().getColumnModel().getColumn(3).setPreferredWidth(50);
            getTargetList().getColumnModel().getColumn(3).setMaxWidth(50);
        }
        if (getTargetList().getColumnModel().getColumnCount() >= 6) {
            getTargetList().getColumnModel().getColumn(4).setPreferredWidth(50);
            getTargetList().getColumnModel().getColumn(4).setMaxWidth(50);
            getTargetList().getColumnModel().getColumn(5).setPreferredWidth(50);
            getTargetList().getColumnModel().getColumn(5).setMaxWidth(50);
        }
        //
        getTargetList().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getTargetList().getSelectionModel().addListSelectionListener(new TargetSelectionListener());
        // Left align table header
        ((DefaultTableCellRenderer) getTargetList().getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    private class TargetSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            checkSelection();
        }
    }

    @Override
    protected void ensureIndexIsVisible(int selectedIndex) {
        // FIXUP...
        //targetList.ensureIndexIsVisible(selectedIndex);
        //java.awt.Rectangle rect = targetList.getCellRect(selectedIndex, 0, true);
        //targetList.scrollRectToVisible(rect);
    }

    @Override
    protected Component getViewComponent() {
        return getTargetList();
    }

    private JTable getTargetList() {
        if (targetList == null) {
            targetList = new MyTable();
            setData(null);
        }
        return targetList;
    }

    class MyTable extends JTable {

        public MyTable() {
//	    //setTableHeader(null); // Hides table headers
//	    if (getRowHeight() < 19)
//		setRowHeight(19);
            getAccessibleContext().setAccessibleDescription(""); // NOI18N
            getAccessibleContext().setAccessibleName(""); // NOI18N
        }

        @Override
        public boolean getShowHorizontalLines() {
            return false;
        }

        @Override
        public boolean getShowVerticalLines() {
            return false;
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            return myTableCellRenderer;
        }        //        @Override
//	public TableCellEditor getCellEditor(int row, int col) {
//	    //TableColumn col = getTargetList().getColumnModel().getColumn(1);
//	    if (col == 0) {
//		return super.getCellEditor(row, col);
//	    }
//	    else if (col == 1) {
//		LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem)listData.elementAt(row);
//		Project project = projectItem.getProject(baseDir);
//		if (project == null) {
//		    return super.getCellEditor(row, col);
//		}
//		else {
//		    MakeArtifact[] artifacts = MakeArtifact.getMakeArtifacts(project);
//		    JComboBox comboBox = new JComboBox();
//		    for (int i = 0; i < artifacts.length; i++)
//			comboBox.addItem(new MakeArtifactWrapper(artifacts[i]));
//		    return new DefaultCellEditor(comboBox);
//		}
//	    }
//	    else {
//		// col 2
//		LibraryItem libraryItem = (LibraryItem)listData.elementAt(row);
//		if (libraryItem instanceof LibraryItem.ProjectItem) {
//		    LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem)listData.elementAt(row);
//		    JCheckBox checkBox = new JCheckBox();
//		    checkBox.setSelected(((LibraryItem.ProjectItem)libraryItem).getMakeArtifact().getBuild());
//		    return new DefaultCellEditor(checkBox);
//		}
//		else {
//		    return super.getCellEditor(row, col);
//		}
//	    }
//	}
    }

    class MyTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, color, isSelected, hasFocus, row, col);
            FileElement elem = (FileElement) listData.elementAt(row);
            File file = new File(IpeUtils.toAbsolutePath(baseDir, elem.getFrom()));
            if (!file.exists() || elem.getFrom().length() == 0) {
                elem.setType(FileType.UNKNOWN);
            }
            else if (file.isDirectory()) {
                elem.setType(FileType.DIRECTORY);
            }
            else {
                elem.setType(FileType.FILE);
            }
            if (col == 0) {
                if (elem.getType() == FileType.DIRECTORY) {
                    label.setText("dir"); // NOI18N
                } else if (elem.getType() == FileType.FILE) {
                    label.setText("file"); // NOI18N
                } else if (elem.getType() == FileType.SOFTLINK) {
                    label.setText("link"); // NOI18N
                } else if (elem.getType() == FileType.UNKNOWN) {
                    label.setText(""); // NOI18N
                } else {
                    assert false;
                    label.setText(""); // NOI18N
                }
            } else if (col == 1) {
                if (!isSelected) {
                    label = new JLabel();
                }
                label.setToolTipText(file.getAbsolutePath());
                if (!isSelected && !file.exists()) {
                    label.setForeground(Color.RED);
                }
                label.setText(elem.getFrom());
            }
//            else {
//                label.setText(elem.getTo());
//            }
            return label;
        }
    }
    
    /*
     * Can be overridden to show fewer colums
     */
    public int getActualColumnCount() {
        return 6;
    }

    class MyTableModel extends DefaultTableModel {

        private String[] columnNames = {"Type", "File or Directory", "Package File Path", "Permission", "Owner", "Group"}; // FIXUP

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public int getColumnCount() {
            return getActualColumnCount();
        }

        @Override
        public int getRowCount() {
            return listData.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
//            return listData.elementAt(row);
            FileElement elem = (FileElement) listData.elementAt(row);
            if (col == 0) {
                return elem.getType();
            }
            if (col == 1) {
                return elem.getFrom();
            }
            if (col == 2) {
                return elem.getTo();
            }
            if (col == 3) {
                return elem.getPermission();
            }
            if (col == 4) {
                return elem.getOwner();
            }
            if (col == 5) {
                return elem.getGroup();
            }
            assert false;
            return null;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == 0) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            FileElement elem = (FileElement) listData.elementAt(row);
            if (col == 0) {
                ; // Nothing
            } else if (col == 1) {
                elem.setFrom((String) value);

                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else if (col == 2) {
                elem.setTo((String) value);

                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else if (col == 3) {
                elem.setPermission((String) value);

                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else if (col == 4) {
                elem.setOwner((String) value);

                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else if (col == 5) {
                elem.setGroup((String) value);

                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else {
                assert false;
            }
        }
    }
    /** Look up i18n strings here */
    private static ResourceBundle bundle;

    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(PackagingFilesPanel.class);
        }
        return bundle.getString(s);
    }
}

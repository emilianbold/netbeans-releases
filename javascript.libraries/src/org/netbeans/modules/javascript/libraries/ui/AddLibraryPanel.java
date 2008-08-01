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

package org.netbeans.modules.javascript.libraries.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.modules.javascript.libraries.util.JSLibraryData;
import org.netbeans.modules.javascript.libraries.util.JSLibraryProjectUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Quy Nguyen <quynguyen@netbeans.org>
 */
public final class AddLibraryPanel extends JPanel {
    private final AddLibraryTableModel model;
    private final File projectFld;
    private final File baseDir;
    private final Project project;
    private final JButton okButton;
    
    private final CellEditorListener editListener;
    
    /** Creates new form AddLibraryPanel */
    public AddLibraryPanel(Project project, LibraryChooser.Filter filter, JButton okButton) {
        this.project = project;
        this.projectFld = FileUtil.toFile(project.getProjectDirectory());
        File base;
        try {
            base = new File(JSLibraryProjectUtils.getJSLibrarySourcePath(project)).getCanonicalFile();
        } catch (IOException ex) {
            base = new File(JSLibraryProjectUtils.getJSLibrarySourcePath(project));
        }
        this.baseDir = base;
        this.okButton = okButton;
        
        initComponents();
        
        this.model = new AddLibraryTableModel(project, filter);
        librariesTable.setModel(model);
        
        this.editListener = new CellEditorListener() {
            public void editingStopped(ChangeEvent e) {
                processTextChange();
            }

            public void editingCanceled(ChangeEvent e) {
            }
            
        };
        
        // browse button column
        TableColumn column = librariesTable.getColumnModel().getColumn(3);
        column.setCellRenderer(new ButtonColumnRenderer());
        column.setCellEditor(new ButtonColumnCellEditor());
        column.getCellEditor().addCellEditorListener(editListener);
        
        // destination column
        column = librariesTable.getColumnModel().getColumn(2);
        column.setCellRenderer(new CustomTextRenderer());
        
        librariesTable.getDefaultEditor(Object.class).addCellEditorListener(editListener);
        librariesTable.getDefaultEditor(Boolean.class).addCellEditorListener(editListener);
        
        initTableLayout();
    }

    private void initTableLayout() {
        librariesTable.setRowHeight(librariesTable.getRowHeight() + 4);
        librariesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        librariesTable.setIntercellSpacing(new java.awt.Dimension(0, 0));        
        librariesTable.getParent().setBackground(librariesTable.getBackground());

        TableColumn column = librariesTable.getColumnModel().getColumn(0);
        JTableHeader header = librariesTable.getTableHeader();
        column.setMaxWidth(24 + SwingUtilities.computeStringWidth(header.getFontMetrics(header.getFont()), String.valueOf(column.getHeaderValue())));
	
        librariesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        column = librariesTable.getColumnModel().getColumn(1);
        column.setPreferredWidth(175);
        
        column = librariesTable.getColumnModel().getColumn(2);
        column.setPreferredWidth(700);
        column.setMinWidth(75);
        
        column = librariesTable.getColumnModel().getColumn(3);
        column.setMaxWidth(24 + SwingUtilities.computeStringWidth(header.getFontMetrics(header.getFont()), String.valueOf(column.getHeaderValue())));        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tableScrollPane = new javax.swing.JScrollPane();
        librariesTable = new javax.swing.JTable();
        tableLabel = new javax.swing.JLabel();
        errorMessage = new javax.swing.JLabel();
        instructionsLabel = new javax.swing.JLabel();

        librariesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3"
            }
        ));
        librariesTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        librariesTable.setShowHorizontalLines(false);
        librariesTable.setShowVerticalLines(false);
        tableScrollPane.setViewportView(librariesTable);
        librariesTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddLibraryPanel.class, "AddLibraryPanel.librariesTable.AccessibleContext.accessibleDescription")); // NOI18N

        tableLabel.setLabelFor(librariesTable);
        org.openide.awt.Mnemonics.setLocalizedText(tableLabel, org.openide.util.NbBundle.getMessage(AddLibraryPanel.class, "AddLibraryPanel.tableLabel.text", new Object[] {})); // NOI18N

        instructionsLabel.setText(org.openide.util.NbBundle.getMessage(AddLibraryPanel.class, "AddLibraryPanel.instructionsLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, tableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 908, Short.MAX_VALUE)
                    .add(errorMessage)
                    .add(tableLabel)
                    .add(instructionsLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(instructionsLabel)
                .add(38, 38, 38)
                .add(tableLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tableScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 248, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 20, Short.MAX_VALUE)
                .add(errorMessage)
                .addContainerGap())
        );

        tableScrollPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddLibraryPanel.class, "AddLibraryPanel.tableScrollPane.AccessibleContext.accessibleName")); // NOI18N
        errorMessage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddLibraryPanel.class, "AddLibraryPanel.errorMessage.AccessibleContext.accessibleName")); // NOI18N
        instructionsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddLibraryPanel.class, "AddLibraryPanel.instructionsLabel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorMessage;
    private javax.swing.JLabel instructionsLabel;
    private javax.swing.JTable librariesTable;
    private javax.swing.JLabel tableLabel;
    private javax.swing.JScrollPane tableScrollPane;
    // End of variables declaration//GEN-END:variables
    
    private class ButtonColumnRenderer extends JButton implements TableCellRenderer {
        private JButton renderButton;
        
        public ButtonColumnRenderer() {
            renderButton = new JButton(NbBundle.getMessage(AddLibraryPanel.class, "AddLibraryPanel_BrowseButton"));
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            boolean enabled = ((Boolean)AddLibraryPanel.this.model.getValueAt(row, 0)).booleanValue();
            renderButton.setEnabled(enabled);
            return renderButton;
        }
        
    }
    
    private class CustomTextRenderer extends DefaultTableCellRenderer {
        public CustomTextRenderer() {
            super();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String str = (value != null) ? value.toString() : AddLibraryPanel.this.model.getValueAt(row, 2).toString();
            Component result = super.getTableCellRendererComponent(table, str, isSelected, hasFocus, row, column);
            
            boolean rowSelected = ((Boolean)AddLibraryPanel.this.model.getValueAt(row, 0)).booleanValue();
            result.setEnabled(rowSelected);
            return result;
        }
    }

    public List<JSLibraryData> getSelectedLibraries() {
        return model.getSelectedLibraries();
    }
    
    public void folderCreationFailed() {
        errorMessage.setText(NbBundle.getMessage(AddLibraryPanel.class, "AddLibraryPanel_CouldNotCreateError"));
        errorMessage.setVisible(true);
        okButton.setEnabled(false);        
    }
    
    private void processTextChange() {
        boolean hasErrors = false;
        String newErrorMsg = null;
        
        for (int i = 0; i < model.getRowCount(); i++) {
            AddLibraryTableModel.LibraryRowModel rowModel = model.getLibraryModel(i);
            if (!rowModel.isSelected()) {
                continue;
            }
            
            String destination = rowModel.getDestination();
            File fileName = new File(destination);
            File folder = fileName.isAbsolute() ? fileName : new File(projectFld, fileName.getPath());
            try {
                folder = folder.getCanonicalFile();
            } catch (IOException ex) {
                if (!hasErrors) {
                    hasErrors = true;
                    newErrorMsg = NbBundle.getMessage(AddLibraryPanel.class, "AddLibraryPanel_InvalidFolderError");
                } else {
                    newErrorMsg = NbBundle.getMessage(AddLibraryPanel.class, "AddLibraryPanel_MultipleErrors");
                }
                
                rowModel.setError(true);
                continue;
            }
            
            folder = FileUtil.normalizeFile(folder);

            if (!isParentFolder(baseDir, folder) && !isParentFolder(projectFld, folder)) {
                if (!hasErrors) {
                    hasErrors = true;
                    newErrorMsg = NbBundle.getMessage(AddLibraryPanel.class, "AddLibraryPanel_OutOfRangeError");
                } else {
                    newErrorMsg = NbBundle.getMessage(AddLibraryPanel.class, "AddLibraryPanel_MultipleErrors");
                }
                
                rowModel.setError(true);
            } else {
                rowModel.setError(false);
            }
        }
        
        if (!hasErrors) {
            errorMessage.setText("");
            errorMessage.setVisible(false);
            okButton.setEnabled(true);
        } else {
            errorMessage.setText(newErrorMsg);
            errorMessage.setVisible(true);
            okButton.setEnabled(false);
        }
    }
    
    private static boolean isParentFolder(File parent, File child) {
        if (!parent.isDirectory()) {
            return false;
        }
        
        Set<File> visitedParents = new HashSet<File>();
        for (File nextParent = child; nextParent != null && !visitedParents.contains(nextParent);
                nextParent = nextParent.getParentFile()) {
            visitedParents.add(nextParent);
            if (nextParent.equals(parent)) {
                return true;
            }
        }
        
        return false;
    }
    
    private class ButtonColumnCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button;
        private String lastSelection = "";
        private String currentFileName = "";
        
        public ButtonColumnCellEditor() {
            button = new JButton();
            button.setOpaque(false);
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    boolean stopped = false;
                    try {
                        JFileChooser chooser = new JFileChooser();
                        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
                        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        File fileName = new File(currentFileName);
                        File folder = fileName.isAbsolute() ? fileName : new File(projectFld, fileName.getPath());
                        chooser.setSelectedFile(folder);
                        if (folder.exists()) {
                            chooser.setSelectedFile(folder);
                        } else {
                            chooser.setSelectedFile(new File(JSLibraryProjectUtils.getJSLibrarySourcePath(project)));
                        }
                        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(AddLibraryPanel.this)) {
                            File selected = FileUtil.normalizeFile(chooser.getSelectedFile());
                            lastSelection = selected.getPath();
                            fireEditingStopped();
                            stopped = true;
                        } else {
                            lastSelection = currentFileName;
                            fireEditingCanceled();
                            stopped = true;
                        }
                    } finally {
                        if (!stopped)
                            fireEditingCanceled();
                    }
                }
                
            });
        }

        public Object getCellEditorValue() {
            return lastSelection;
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentFileName = AddLibraryPanel.this.model.getValueAt(row, 2).toString();
            boolean enabled = ((Boolean)AddLibraryPanel.this.model.getValueAt(row, 0)).booleanValue();
            button.setEnabled(enabled);
            
            return button;
        }
    }
    
}

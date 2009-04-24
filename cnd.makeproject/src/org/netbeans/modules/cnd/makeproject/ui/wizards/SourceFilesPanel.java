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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.cnd.api.utils.FileChooser;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.SourceFileFilter;
import org.openide.util.NbBundle;

public class SourceFilesPanel extends javax.swing.JPanel {

    private Vector<FolderEntry> data = new Vector<FolderEntry>();
    private SourceFileTable sourceFileTable = null;
    private String baseDir;
    private String wd;

    /** Creates new form SourceFilesPanel */
    public SourceFilesPanel() {
        initComponents();
        scrollPane.getViewport().setBackground(java.awt.Color.WHITE);

        getAccessibleContext().setAccessibleDescription(getString("SourceFilesPanelAD"));
        addButton.getAccessibleContext().setAccessibleDescription(getString("AddButtonAD"));
        deleteButton.getAccessibleContext().setAccessibleDescription(getString("DeleteButtonAD"));
        refresh();
        initFocus();
    }

    public void setSeed(String baseDir, String wd) {
        this.baseDir = baseDir;
        this.wd = wd;
    }

    public void initFocus() {
        IpeUtils.requestFocus(addButton);
    }

    public List<FolderEntry> getListData() {
        return data;
    }

    private static class CustomFileFilter extends SourceFileFilter {

        String[] suffixes;

        CustomFileFilter(String suffixesString) {
            StringTokenizer st = new StringTokenizer(suffixesString);
            Vector<String> vec = new Vector<String>();
            while (st.hasMoreTokens()) {
                String nextToken = st.nextToken();
                if (nextToken.charAt(0) == '.') {
                    nextToken = nextToken.substring(1);
                }
                vec.add(nextToken);
            }
            suffixes = vec.toArray(new String[vec.size()]);
        }

        public String getDescription() {
            return ""; // NOI18N
        }

        public String[] getSuffixes() {
            return suffixes;
        }
    }

    private class TargetSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            validateSelection();
        }
    }

    private void validateSelection() {
        addButton.setEnabled(true);
        if (data.size() == 0 || sourceFileTable.getSelectedRow() < 0) {
            deleteButton.setEnabled(false);
        } else {
            deleteButton.setEnabled(true);
        }
    }

    private void refresh() {
        scrollPane.setViewportView(sourceFileTable = new SourceFileTable()); // FIXUP: how to refresh ??
        sourceFilesLabel.setLabelFor(sourceFileTable);
        validateSelection();
    }

    class SourceFileTable extends JTable {

        public SourceFileTable() {
            //setTableHeader(null); // Hides table headers
            setModel(new MyTableModel());
            // Left align table header
            ((DefaultTableCellRenderer) getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

            getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            getSelectionModel().addListSelectionListener(new TargetSelectionListener());
            getAccessibleContext().setAccessibleDescription(getString("SourceFileTableAD"));
        }

        @Override
        public boolean getShowHorizontalLines() {
            return false;
        }

        @Override
        public boolean getShowVerticalLines() {
            return false;
        }

//        @Override
//        public TableCellRenderer getCellRenderer(int row, int column) {
//            return new MyTableCellRenderer();
//        }

//        @Override
//        public TableCellEditor getCellEditor(int row, int col) {
//            JCheckBox checkBox = new JCheckBox();
//            return new DefaultCellEditor(checkBox);
//        }

//        @Override
//        public void setValueAt(Object value, int row, int col) {
//            if (col == 0) {
//                FolderEntry fileEntry = data.elementAt(row);
//                fileEntry.setAddSubfoldersSelected(!fileEntry.isAddSubfoldersSelected());
//            }
//        }
    }

    class MyTableModel extends DefaultTableModel {

        @Override
        public String getColumnName(int col) {
            return " " + getString("TABLE_COLUMN_1_TXT"); // NOI18N
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            return data.elementAt(row).getFolderName();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

//    class MyTableCellRenderer extends DefaultTableCellRenderer {
//
//        @Override
//        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int col) {
//            if (col == 0) {
//                JCheckBox checkBox = new JCheckBox();
//                checkBox.setBackground(Color.WHITE);
//                checkBox.setSelected((data.elementAt(row)).isAddSubfoldersSelected());
//                //checkBox.setText(((FileEntry)data.elementAt(row)).getFile().getPath());
//                return checkBox;
//            } else {
//                return super.getTableCellRendererComponent(table, color, isSelected, hasFocus, row, col);
//            }
//        }
//    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sourceFilesLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        list = new javax.swing.JList();
        buttonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        sourceFilesLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("SourceFileFoldersMN").charAt(0));
        sourceFilesLabel.setLabelFor(list);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle"); // NOI18N
        sourceFilesLabel.setText(bundle.getString("SourceFileFoldersLbl")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(sourceFilesLabel, gridBagConstraints);

        scrollPane.setViewportView(list);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(scrollPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        addButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("AddButtonMN").charAt(0));
        addButton.setText(bundle.getString("AddButtonTxt")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 0, 0);
        buttonPanel.add(addButton, gridBagConstraints);

        deleteButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("DeleteButtonMn").charAt(0));
        deleteButton.setText(bundle.getString("DeleteButtonTxt")); // NOI18N
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        buttonPanel.add(deleteButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        int index = sourceFileTable.getSelectedRow();
        if (index < 0 || index >= data.size()) {
            return;
        }
        data.remove(index);
        refresh();
        if (data.size() > 0) {
            if (data.size() > index) {
                sourceFileTable.getSelectionModel().setSelectionInterval(index, index);
            } else {
                sourceFileTable.getSelectionModel().setSelectionInterval(index - 1, index - 1);
            }
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        String seed = null;
        if (FileChooser.getCurrectChooserFile() != null) {
            seed = FileChooser.getCurrectChooserFile().getPath();
        }
        if (seed == null) {
            if (wd != null && wd.length() > 0 && !IpeUtils.isPathAbsolute(wd)) {
                seed = baseDir + File.separator + wd;
            } else if (wd != null) {
                seed = wd;
            } else {
                seed = baseDir;
            }
        }
        FileChooser fileChooser = new FileChooser(getString("FOLDER_CHOOSER_TITLE_TXT"), getString("FOLDER_CHOOSER_BUTTON_TXT"), FileChooser.DIRECTORIES_ONLY, null, seed, true);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == FileChooser.CANCEL_OPTION) {
            return;
        }
        if (!fileChooser.getSelectedFile().exists() || !fileChooser.getSelectedFile().isDirectory()) {
            // FIXUP: error message
            return;
        }
        data.add(new FolderEntry(fileChooser.getSelectedFile(), IpeUtils.toAbsoluteOrRelativePath(baseDir, fileChooser.getSelectedFile().getPath())));
        refresh();
    }//GEN-LAST:event_addButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JList list;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JLabel sourceFilesLabel;
    // End of variables declaration//GEN-END:variables

    private static String getString(String s) {
        return NbBundle.getMessage(SourceFilesPanel.class, s);
    }
}

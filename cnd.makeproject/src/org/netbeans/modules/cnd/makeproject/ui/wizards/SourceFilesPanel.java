/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.awt.Color;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.ui.CndUIUtilities;
import org.openide.util.NbBundle;

public class SourceFilesPanel extends javax.swing.JPanel {

    private final Color defaultTextFieldFg;
    private List<FolderEntry> sourceData = new ArrayList<FolderEntry>();
    private List<FolderEntry> testData = new ArrayList<FolderEntry>();
    private SourceFileTable sourceFileTable = null;
    private SourceFileTable testFileTable = null;
    private String baseDir;
    private String wd;
    private ChangeListener listener;

    /** Creates new form SourceFilesPanel */
    public SourceFilesPanel(ChangeListener listener, boolean showTestFolders) {
        initComponents();

        defaultTextFieldFg = excludePatternTextField.getForeground();

        if (!showTestFolders) {
            addButton1.setVisible(false);
            deleteButton1.setVisible(false);
            jSeparator1.setVisible(false);
            scrollPane1.setVisible(false);
            sourceFilesLabel1.setVisible(false);
        }

        scrollPane.getViewport().setBackground(java.awt.Color.WHITE);

        getAccessibleContext().setAccessibleDescription(getString("SourceFilesPanelAD"));
        addButton.getAccessibleContext().setAccessibleDescription(getString("AddButtonAD"));
        deleteButton.getAccessibleContext().setAccessibleDescription(getString("DeleteButtonAD"));
        refresh();
        initFocus();
        this.listener = listener;
        excludePatternTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }
        });
    }

    private void update() {
        String excludeStr = excludePatternTextField.getText();
        try {
            Pattern.compile(excludeStr);
            excludePatternTextField.setForeground(defaultTextFieldFg);
        } catch (PatternSyntaxException ex) {
            excludePatternTextField.setForeground(Color.RED);
        }
        if (listener != null) {
            listener.stateChanged(null);
        }
    }

    public void setSeed(String baseDir, String wd) {
        this.baseDir = baseDir;
        this.wd = wd;
    }

    public void setFoldersFilter(String regex) {
        excludePatternTextField.setText(regex);
    }

    public String getFoldersFilter() {
        return excludePatternTextField.getText();
    }

    public FileFilter getFileFilter() {
        Pattern excludePattern = null;

        String excludeStr = excludePatternTextField.getText().trim();
        if (!excludeStr.isEmpty()) {
            try {
                excludePattern = Pattern.compile(excludeStr.trim());
            } catch (PatternSyntaxException ex) {
                // ignore
            }
        }

        if (excludePattern == null) {
            // by default exclude nothing
            excludePattern = Pattern.compile("^$"); // NOI18N
        }

        return new RegexpExcludeFileFilter(excludePattern);
    }

    public final void initFocus() {
        CndUIUtilities.requestFocus(addButton);
    }

    public List<FolderEntry> getSourceListData() {
        return sourceData;
    }

    public List<FolderEntry> getTestListData() {
        return testData;
    }

    private class TargetSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            validateSelection();
        }
    }

    private void validateSelection() {
        addButton.setEnabled(true);
        if (sourceData.isEmpty() || sourceFileTable.getSelectedRow() < 0) {
            deleteButton.setEnabled(false);
        } else {
            deleteButton.setEnabled(true);
        }

        addButton1.setEnabled(true);
        if (testData.isEmpty() || testFileTable.getSelectedRow() < 0) {
            deleteButton1.setEnabled(false);
        } else {
            deleteButton1.setEnabled(true);
        }
    }

    private void refresh() {
        scrollPane.setViewportView(sourceFileTable = new SourceFileTable(sourceData, getString("TABLE_COLUMN_SOURCE_TXT")));
        sourceFilesLabel.setLabelFor(sourceFileTable);
        scrollPane1.setViewportView(testFileTable = new SourceFileTable(testData, getString("TABLE_COLUMN_TEST_TXT")));
        sourceFilesLabel1.setLabelFor(testFileTable);
        validateSelection();
    }

    private static final class RegexpExcludeFileFilter implements FileFilter {

        private final Pattern excludePattern;

        public RegexpExcludeFileFilter(Pattern excludeFilter) {
            this.excludePattern = excludeFilter;
        }

        @Override
        public boolean accept(File pathname) {
            return !excludePattern.matcher(pathname.getName()).find();
        }
    }

    private final class SourceFileTable extends JTable {

        public SourceFileTable(List<FolderEntry> data, String columnTitle) {
            //setTableHeader(null); // Hides table headers
            setModel(new MyTableModel(data, columnTitle));
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
    }

    private final class MyTableModel extends DefaultTableModel {
        private List<FolderEntry> data;
        private String columnTitle;

        public MyTableModel(List<FolderEntry> data, String columnTitle) {
            this.data = data;
            this.columnTitle = columnTitle;
        }

        @Override
        public String getColumnName(int col) {
            return " " + columnTitle; // NOI18N
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getRowCount() {
            if (data == null) {
                return 0;
            }
            return data.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (data == null) {
                return null;
            }
            return data.get(row).getFolderName();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sourceFilesLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        list = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        excludePatternLabel = new javax.swing.JLabel();
        excludePatternTextField = new javax.swing.JTextField();
        seeAlsoLabel = new javax.swing.JLabel();
        sourceFilesLabel1 = new javax.swing.JLabel();
        scrollPane1 = new javax.swing.JScrollPane();
        list1 = new javax.swing.JList();
        addButton1 = new javax.swing.JButton();
        deleteButton1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        sourceFilesLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("SourceFileFoldersMN").charAt(0));
        sourceFilesLabel.setLabelFor(list);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle"); // NOI18N
        sourceFilesLabel.setText(bundle.getString("SourceFileFoldersLbl")); // NOI18N

        scrollPane.setViewportView(list);

        addButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("AddButtonMN").charAt(0));
        addButton.setText(bundle.getString("AddButtonTxt")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        deleteButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("DeleteButtonMn").charAt(0));
        deleteButton.setText(bundle.getString("DeleteButtonTxt")); // NOI18N
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        excludePatternLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("SourceFilesPanel.excludePatternLabel.mn").charAt(0));
        excludePatternLabel.setLabelFor(excludePatternTextField);
        excludePatternLabel.setText(org.openide.util.NbBundle.getMessage(SourceFilesPanel.class, "SourceFilesPanel.excludePatternLabel.text")); // NOI18N

        seeAlsoLabel.setText(org.openide.util.NbBundle.getMessage(SourceFilesPanel.class, "SourceFilesPanel.seeAlsoLabel.text")); // NOI18N

        sourceFilesLabel1.setLabelFor(list1);
        sourceFilesLabel1.setText(bundle.getString("SourceFileFoldersLbl")); // NOI18N

        scrollPane1.setViewportView(list1);

        addButton1.setText(bundle.getString("AddButtonTxt")); // NOI18N
        addButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButton1ActionPerformed(evt);
            }
        });

        deleteButton1.setText(bundle.getString("DeleteButtonTxt")); // NOI18N
        deleteButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(excludePatternLabel)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                    .addComponent(sourceFilesLabel1)
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                    .addComponent(excludePatternTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(deleteButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(deleteButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(seeAlsoLabel)
                .addContainerGap(96, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(sourceFilesLabel)
                .addContainerGap(85, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(sourceFilesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton))
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(excludePatternLabel)
                .addGap(2, 2, 2)
                .addComponent(excludePatternTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(seeAlsoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sourceFilesLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton1)
                        .addGap(4, 4, 4)
                        .addComponent(deleteButton1)
                        .addContainerGap())
                    .addComponent(scrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void deleteFile(List<FolderEntry> data, SourceFileTable table) {
        int index = table.getSelectedRow();
        if (index < 0 || index >= data.size()) {
            return;
        }
        data.remove(index);
        refresh();
        if (data.size() > 0) {
            if (data.size() > index) {
                table.getSelectionModel().setSelectionInterval(index, index);
            } else {
                table.getSelectionModel().setSelectionInterval(index - 1, index - 1);
            }
        }
    }

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
       deleteFile(sourceData, sourceFileTable);
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void addFile(List<FolderEntry> data) {
        String seed = null;
        if (FileChooser.getCurrectChooserFile() != null) {
            seed = FileChooser.getCurrectChooserFile().getPath();
        }
        if (seed == null) {
            if (wd != null && wd.length() > 0 && !CndPathUtilitities.isPathAbsolute(wd)) {
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
        data.add(new FolderEntry(fileChooser.getSelectedFile(), CndPathUtilitities.toAbsoluteOrRelativePath(baseDir, fileChooser.getSelectedFile().getPath())));
        refresh();
    }

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
       addFile(sourceData);
    }//GEN-LAST:event_addButtonActionPerformed

    private void addButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButton1ActionPerformed
        addFile(testData);
    }//GEN-LAST:event_addButton1ActionPerformed

    private void deleteButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButton1ActionPerformed
        deleteFile(testData, testFileTable);
    }//GEN-LAST:event_deleteButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addButton1;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton deleteButton1;
    private javax.swing.JLabel excludePatternLabel;
    private javax.swing.JTextField excludePatternTextField;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JList list;
    private javax.swing.JList list1;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JScrollPane scrollPane1;
    private javax.swing.JLabel seeAlsoLabel;
    private javax.swing.JLabel sourceFilesLabel;
    private javax.swing.JLabel sourceFilesLabel1;
    // End of variables declaration//GEN-END:variables

    private static String getString(String s) {
        return NbBundle.getMessage(SourceFilesPanel.class, s);
    }
}

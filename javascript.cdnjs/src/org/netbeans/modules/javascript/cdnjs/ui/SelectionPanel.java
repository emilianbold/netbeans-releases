/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.cdnjs.ui;

import java.awt.Dialog;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.javascript.cdnjs.Library;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel for customization of CDNJS libraries.
 *
 * @author Jan Stola
 */
public class SelectionPanel extends javax.swing.JPanel {
    /** Panel for searching CDNJS libraries. */
    private SearchPanel searchPanel;
    /** Selected libraries. */
    private final List<Library.Version> libraries;
    /** Model for a table of selected libraries. */
    private final LibraryTableModel tableModel;
    /** Web-root, i.e., the folder the library folder is relative to. */
    private final File webRoot;

    /**
     * Creates a new {@code SelectionPanel}.
     * 
     * @param existingLibraries libraries present in the project already.
     * @param webRoot web-root.
     * @param libraryFolder library folder (relative path from web-root).
     */
    public SelectionPanel(Library.Version[] existingLibraries, File webRoot, String libraryFolder) {
        libraries = new ArrayList<>(Arrays.asList(existingLibraries));
        Collections.sort(libraries, new LibraryVersionComparator());
        this.webRoot = webRoot;
        tableModel = new LibraryTableModel();
        initComponents();
        folderField.setText(libraryFolder);
        librariesTable.getSelectionModel().addListSelectionListener(new Listener());
    }

    /**
     * Returns the selected libraries.
     * 
     * @return selected libraries.
     */
    public Library.Version[] getSelectedLibraries() {
        return libraries.toArray(new Library.Version[libraries.size()]);
    }

    /**
     * Returns the selected library folder.
     * 
     * @return library folder.
     */
    public String getLibraryFolder() {
        return folderField.getText();
    }

    /**
     * Shows the search panel.
     */
    @NbBundle.Messages({"SelectionPanel.dialog.title=CDNJS Libraries"})
    private void showSearchPanel() {
        SearchPanel panel = getSearchPanel();
        panel.activate();
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                Bundle.SelectionPanel_dialog_title(),
                true,
                new Object[] {
                    panel.getAddButton(),
                    panel.getCancelButton()
                },
                panel.getAddButton(),
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                null
        );
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        panel.deactivate();
        if (descriptor.getValue() == panel.getAddButton()) {
            addLibrary(panel.getSelectedVersion());
        }
    }

    /**
     * Returns the panel for searching CNDJS libraries.
     * 
     * @return panel for searching CDNJS libraries.
     */
    private SearchPanel getSearchPanel() {
        if (searchPanel == null) {
            searchPanel = new SearchPanel();
        }
        return searchPanel;
    }

    /**
     * Adds the specified library among the selected ones.
     * 
     * @param libraryVersion version of a library to add. 
     */
    private void addLibrary(Library.Version libraryVersion) {
        String newLibraryName = libraryVersion.getLibrary().getName();
        Library.Version versionToReplace = findLibrary(newLibraryName);
        if (versionToReplace != null) {
            libraries.remove(versionToReplace);
        }
        libraries.add(libraryVersion);
        Collections.sort(libraries, new LibraryVersionComparator());
        tableModel.fireTableDataChanged();
    }

    /**
     * Finds a selected version of a library with the specified name.
     * 
     * @param libraryName name of the library to find.
     * @return selected version of a library or {@code null} if there
     * is no version selected for a given library.
     */
    private Library.Version findLibrary(String libraryName) {
        for (Library.Version existingVersion : libraries) {
            String existingLibraryName = existingVersion.getLibrary().getName();
            if (libraryName.equals(existingLibraryName)) {
                return existingVersion;
            }
        }
        return null;
    }

    /**
     * Removes the libraries selected in the table from the set of selected libraries.
     */
    private void removeSelectedLibraries() {
        int[] selectedRows = librariesTable.getSelectedRows();
        int length = selectedRows.length;
        for (int i=1; i<=length; i++) {
            libraries.remove(selectedRows[length-i]);
        }
        tableModel.fireTableDataChanged();
    }

    /**
     * Shows the dialog for the selection of the library folder.
     */
    @NbBundle.Messages({"SelectionPanel.browseDialog.title=Select directory for JS libraries"})
    private void showBrowseDialog() {
        File libraryFolder = PropertyUtils.resolveFile(webRoot, getLibraryFolder());
        File selectedDir = new FileChooserBuilder(SelectionPanel.class)
                .setDirectoriesOnly(true)
                .setTitle(Bundle.SelectionPanel_browseDialog_title())
                .setDefaultWorkingDirectory(libraryFolder)
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (selectedDir != null) {
            String relativePath = PropertyUtils.relativizeFile(webRoot, selectedDir);
            String path;
            if (relativePath == null) {
                path = selectedDir.getAbsolutePath();
            } else {
                path = relativePath;
            }
            folderField.setText(path);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        librariesScrollPane = new javax.swing.JScrollPane();
        librariesTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        folderLabel = new javax.swing.JLabel();
        folderField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        librariesTable.setModel(tableModel);
        librariesScrollPane.setViewportView(librariesTable);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "SelectionPanel.addButton.text")); // NOI18N
        addButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "SelectionPanel.removeButton.text")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(folderLabel, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "SelectionPanel.folderLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "SelectionPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(formListener);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(folderLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(folderField))
                    .addComponent(librariesScrollPane))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(addButton)
                        .addComponent(removeButton, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addComponent(browseButton))
                .addGap(0, 0, 0))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addButton, browseButton, removeButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton))
                    .addComponent(librariesScrollPane))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(folderLabel)
                    .addComponent(folderField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addGap(0, 0, 0))
        );
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == addButton) {
                SelectionPanel.this.addButtonActionPerformed(evt);
            }
            else if (evt.getSource() == removeButton) {
                SelectionPanel.this.removeButtonActionPerformed(evt);
            }
            else if (evt.getSource() == browseButton) {
                SelectionPanel.this.browseButtonActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        showSearchPanel();
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        removeSelectedLibraries();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        showBrowseDialog();
    }//GEN-LAST:event_browseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JTextField folderField;
    private javax.swing.JLabel folderLabel;
    private javax.swing.JScrollPane librariesScrollPane;
    private javax.swing.JTable librariesTable;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Selection listener for libraries table.
     */
    class Listener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            boolean emptySelection = (librariesTable.getSelectedRowCount() == 0);
            removeButton.setEnabled(!emptySelection);
        }
    }

    /**
     * Comparator of {@code Library.Version}s.
     */
    static class LibraryVersionComparator implements Comparator<Library.Version> {
        @Override
        public int compare(Library.Version o1, Library.Version o2) {
            String name1 = o1.getLibrary().getName();
            String name2 = o2.getLibrary().getName();
            return name1.compareTo(name2);
        }        
    }

    /**
     * Model for the libraries table.
     */
    class LibraryTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return libraries.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        @NbBundle.Messages({
            "SelectionPanel.table.libraryColumn=Library",
            "SelectionPanel.table.versionColumn=Version"
        })
        public String getColumnName(int column) {
            String columnName;
            if (column == 0) {
                columnName = Bundle.SelectionPanel_table_libraryColumn();
            } else {
                columnName = Bundle.SelectionPanel_table_versionColumn();
            }
            return columnName;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Library.Version libraryVersion = libraries.get(rowIndex);
            Object value;
            if (columnIndex == 0) {
                value = libraryVersion.getLibrary().getName();
            } else {
                value = libraryVersion.getName();
            }
            return value;
        }
        
    }

}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.versioning.system.cvss.ui.actions.commit;

import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.util.FilePathCellRenderer;
import org.netbeans.modules.versioning.util.TableSorter;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumnModel;
import java.awt.Component;
import java.util.*;
import org.netbeans.modules.versioning.util.SortedTable;

/**
 * View that displays nodes in the Synchronize view. 
 * 
 * @author Maros Sandor
 */
class CommitTable implements AncestorListener, TableModelListener {

    private CommitTableModel    tableModel;
    private JTable              table;
    private JComponent          component;
    
    private TableSorter sorter;
    private String[]    columns;

    public CommitTable(JLabel label) {
        tableModel = new CommitTableModel();
        tableModel.addTableModelListener(this);
        sorter = new TableSorter(tableModel);
        table = new SortedTable(sorter);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(String.class, new CommitStringsCellRenderer());
        table.setDefaultEditor(CommitOptions.class, new CommitOptionsCellEditor());
        table.getTableHeader().setReorderingAllowed(true);
        int height = new JLabel("FONTSIZE").getPreferredSize().height * 6 / 5;  // NOI18N
        table.setRowHeight(height);
        table.addAncestorListener(this);
        component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        label.setLabelFor(table);
        table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommitTable.class, "ACSD_CommitTable"));  // NOI18N
    }

    public void ancestorAdded(AncestorEvent event) {
        setDefaultColumnSizes();
    }

    /**
     * Sets sizes of Commit table columns, kind of hardcoded.
     */ 
    private void setDefaultColumnSizes() {
        int width = table.getWidth();
        TableColumnModel columnModel = table.getColumnModel();
        if (columns == null || columnModel == null) return; // unsure when this methed will be called (component realization) 
        if (columnModel.getColumnCount() != columns.length) return; 
        if (columns.length == 4) {
            for (int i = 0; i < columns.length; i++) {
                String col = columns[i];
                sorter.setColumnComparator(i, null);
                sorter.setSortingStatus(i, TableSorter.NOT_SORTED);
                if (col.equals(CommitSettings.COLUMN_NAME_NAME)) {
                    sorter.setColumnComparator(i, new FileNameComparator());
                    columnModel.getColumn(i).setPreferredWidth(width * 30 / 100);
                } else if (col.equals(CommitSettings.COLUMN_NAME_STATUS)) {
                    sorter.setColumnComparator(i, new StatusComparator());
                    sorter.setSortingStatus(i, TableSorter.ASCENDING);
                    columnModel.getColumn(i).setPreferredWidth(width * 15 / 100);
                } else if (col.equals(CommitSettings.COLUMN_NAME_ACTION)) {
                    columnModel.getColumn(i).setPreferredWidth(width * 15 / 100);
                } else {
                    columnModel.getColumn(i).setPreferredWidth(width * 40 / 100);
                }
            }
        } else if (columns.length == 5) {
            for (int i = 0; i < columns.length; i++) {
                String col = columns[i];
                sorter.setColumnComparator(i, null);
                sorter.setSortingStatus(i, TableSorter.NOT_SORTED);
                if (col.equals(CommitSettings.COLUMN_NAME_NAME)) {
                    sorter.setColumnComparator(i, new FileNameComparator());
                    columnModel.getColumn(i).setPreferredWidth(width * 25 / 100);
                } else if (col.equals(CommitSettings.COLUMN_NAME_STICKY)) {
                        columnModel.getColumn(i).setPreferredWidth(width * 15 / 100);
                } else if (col.equals(CommitSettings.COLUMN_NAME_STATUS)) {
                    sorter.setColumnComparator(i, new StatusComparator());
                    sorter.setSortingStatus(i, TableSorter.ASCENDING);
                    columnModel.getColumn(i).setPreferredWidth(width * 15 / 100);
                } else if (col.equals(CommitSettings.COLUMN_NAME_ACTION)) {
                    columnModel.getColumn(i).setPreferredWidth(width * 15 / 100);
                } else {
                    columnModel.getColumn(i).setPreferredWidth(width * 30 / 100);
                }
            }
        }
    }

    public void ancestorMoved(AncestorEvent event) {
    }

    public void ancestorRemoved(AncestorEvent event) {
    }
    
    void setColumns(String[] cols) {
        if (Arrays.equals(columns, cols)) return;
        columns = cols;
        tableModel.setColumns(cols);
        setDefaultColumnSizes();
    }

    void setNodes(CvsFileNode[] nodes) {
        tableModel.setNodes(nodes);
    }
    
    public CommitSettings.CommitFile[] getCommitFiles() {
        return tableModel.getCommitFiles();
    }
    
    public JComponent getComponent() {
        return component;
    }

    void dataChanged() {
        int idx = table.getSelectedRow();
        tableModel.fireTableDataChanged();
        if (idx != -1) table.getSelectionModel().addSelectionInterval(idx, idx);
    }

    TableModel getTableModel() {
        return tableModel;
    }

    public void tableChanged(TableModelEvent e) {
        // change in commit options may alter name rendering (strikethrough)
        table.repaint();
    }
    
    private class CommitOptionsCellEditor extends DefaultCellEditor {
        
        private final Object[] addOptions = new Object [] {
                CommitOptions.ADD_TEXT,
                CommitOptions.ADD_BINARY,
                CommitOptions.EXCLUDE
            };
        private final Object[] commitOptions = new Object [] {
                CommitOptions.COMMIT,
                CommitOptions.EXCLUDE
            };

        private final Object[] removeOptions = new Object [] {
                CommitOptions.COMMIT_REMOVE,
                CommitOptions.EXCLUDE
            };

        public CommitOptionsCellEditor() {
            super(new JComboBox());
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            int fileStatus = tableModel.getCommitFile(sorter.modelIndex(row)).getNode().getInformation().getStatus();
            JComboBox combo = (JComboBox) editorComponent;
            if (fileStatus == FileInformation.STATUS_VERSIONED_DELETEDLOCALLY || fileStatus == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
                combo.setModel(new DefaultComboBoxModel(removeOptions));
            } else if ((fileStatus & FileInformation.STATUS_IN_REPOSITORY) == 0 && fileStatus != FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) {
                combo.setModel(new DefaultComboBoxModel(addOptions));
            } else {
                combo.setModel(new DefaultComboBoxModel(commitOptions));
            }
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
    }

    private static class CommitStringsCellRenderer extends DefaultTableCellRenderer {

        private FilePathCellRenderer pathRenderer = new FilePathCellRenderer();

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            int col = table.convertColumnIndexToModel(column);
            if (col == 0) {
                TableSorter sorter = (TableSorter) table.getModel();
                CommitTableModel model = (CommitTableModel) sorter.getTableModel();
                CommitSettings.CommitFile commitFile = model.getCommitFile(sorter.modelIndex(row));
                if (!isSelected) {
                    value = "<html>" + CvsVersioningSystem.getInstance().getAnnotator().annotateNameHtml(  // NOI18N
                            commitFile.getNode().getFile().getName(), commitFile.getNode().getInformation(), null);
                }
                if (commitFile.getOptions() == CommitOptions.EXCLUDE) {
                    value = "<html><s>" + value + "</s></html>"; // NOI18N
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else if (col == 3) {
                return pathRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        }
    }
    
    private class StatusComparator extends Utils.ByImportanceComparator {
        public int compare(Object o1, Object o2) {
            Integer row1 = (Integer) o1;
            Integer row2 = (Integer) o2;
            return super.compare(tableModel.getCommitFile(row1.intValue()).getNode().getInformation(), 
                                 tableModel.getCommitFile(row2.intValue()).getNode().getInformation());
        }
    }
    
    private class FileNameComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Integer row1 = (Integer) o1;
            Integer row2 = (Integer) o2;
            return tableModel.getCommitFile(row1.intValue()).getNode().getName().compareToIgnoreCase(
                    tableModel.getCommitFile(row2.intValue()).getNode().getName());
        }
    }
}

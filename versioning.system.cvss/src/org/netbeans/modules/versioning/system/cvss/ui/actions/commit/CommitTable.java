/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.commit;

import org.openide.awt.Mnemonics;
import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.util.TableSorter;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.util.FilePathCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.*;
import java.io.File;

/**
 * View that displays nodes in the Synchronize view. 
 * 
 * @author Maros Sandor
 */
class CommitTable implements MouseListener {

    private CommitTableModel    tableModel;
    private JTable              table;
    private JComponent          component;
    
    private TableSorter sorter;

    public CommitTable(File [] roots) {
        tableModel = new CommitTableModel(CvsVersioningSystem.getInstance().getFileTableModel(roots, FileInformation.STATUS_LOCAL_CHANGE));
        sorter = new TableSorter(tableModel);
        table = new JTable(sorter);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(String.class, new CommitStringsCellRenderer());
        table.setDefaultEditor(CommitOptions.class, new CommitOptionsCellEditor());
        table.getTableHeader().setReorderingAllowed(true);
        sorter.setTableHeader(table.getTableHeader());
        table.setRowHeight(table.getRowHeight() * 6 / 5);
        component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        table.addMouseListener(this);
        sorter.setColumnComparator(1, new StatusComparator());
        sorter.setSortingStatus(1, TableSorter.ASCENDING);
        
        Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(ss.width / 2 - 420);
    }

    public CommitSettings.CommitFile[] getCommitFiles() {
        return tableModel.getCommitFiles();
    }
    
    public JComponent getComponent() {
        return component;
    }

    private void showPopup(MouseEvent e) {
        int [] selectedrows = table.getSelectedRows();
        if (selectedrows.length == 0) {
            int row = table.rowAtPoint(e.getPoint());
            if (row == -1) return;
            table.getSelectionModel().setSelectionInterval(row, row);
            selectedrows = new int [] { row };
        }

        List selectedFiles = new ArrayList();
        ListSelectionModel selectionModel = table.getSelectionModel();
        int min = selectionModel.getMinSelectionIndex();
        if (min == -1) {
            return;            
        }
        int max = selectionModel.getMaxSelectionIndex();
        for (int i = min; i <= max; i++) {
            if (selectionModel.isSelectedIndex(i)) {
                int idx = sorter.modelIndex(i);
                selectedFiles.add(tableModel.getNodeAt(idx).getFile());
            }
        }
        
        JPopupMenu menu = getPopup();
//        menu.show(table, e.getX(), e.getY());
    }

    private JPopupMenu getPopup() {
        JPopupMenu menu = new JPopupMenu();
        Action [] actions = Annotator.getActions();
        for (int i = 0; i < actions.length; i++) {
            Action action = actions[i];
            if (action == null) {
                menu.add(new JSeparator());                
            } else {
                JMenuItem item = menu.add(action);
                Mnemonics.setLocalizedText(item, item.getText());
            }
        }
        return menu;
    }
    
    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    void dataChanged() {
        tableModel.fireTableDataChanged();
    }

    TableModel getTableModel() {
        return tableModel;
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
            int fileStatus = tableModel.getNodeAt(sorter.modelIndex(row)).getInformation().getStatus();
            JComboBox combo = (JComboBox) editorComponent;
            if (fileStatus == FileInformation.STATUS_VERSIONED_DELETEDLOCALLY || fileStatus == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
                combo.setModel(new DefaultComboBoxModel(removeOptions));
            } else if ((fileStatus & FileInformation.STATUS_IN_REPOSITORY) == 0) {
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
                CvsFileNode node = model.getNodeAt(sorter.modelIndex(row));
                if (!isSelected) {
                    value = Annotator.annotateNameHtml(node.getFile(), node.getInformation().getStatus());
                }
                if (CvsModuleConfig.getDefault().isExcludedFromCommit(node.getFile().getAbsolutePath())) {
                    if (isSelected) {
                        value = "<html><s>" + value + "</s></html>";
                    } else {
                        String name = (String) value;
                        value = "<html><s>" + name.substring(6, name.length() - 7) + "</s></html>";
                    }
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
            return super.compare(tableModel.getNodeAt(row1.intValue()).getInformation(), tableModel.getNodeAt(row2.intValue()).getInformation());
        }
    }
    
}

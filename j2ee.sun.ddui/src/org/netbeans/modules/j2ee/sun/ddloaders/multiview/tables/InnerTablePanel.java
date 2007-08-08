/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.Utils;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;


/** From j2ee/ddloaders module, but heavily restructured and cleaned up.
 * 
 * @author pfiala
 * @author Peter Williams
 */
public class InnerTablePanel extends BaseSectionNodeInnerPanel {

    private final TablePanel tablePanel;
    private JTable table;

    public InnerTablePanel(final SectionNodeView sectionNodeView, final InnerTableModel model, ASDDVersion version) {
        super(sectionNodeView, version);

        tablePanel = new TablePanel(model);
        initUserComponents(model);
        
        scheduleRefreshView();
    }
    
    protected void initUserComponents(final InnerTableModel model) {
        setAlignmentX(LEFT_ALIGNMENT);
        setOpaque(false);
        
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                int type = e.getType();
                if (type == TableModelEvent.INSERT || type == TableModelEvent.DELETE) {
                    scheduleRefreshView();
                }
            }
        });
        
        table = tablePanel.getTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editCell(table.getSelectedRow(), table.getSelectedColumn());
                }
            }
        });
        
        InputMap inputMap = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "selectNextColumnCell"); // NOI18N
        inputMap.put(KeyStroke.getKeyStroke("shift ENTER"), "selectPreviousColumnCell"); // NOI18N
        setLayout(new BorderLayout());
        add(tablePanel, BorderLayout.WEST);
        
        setColumnWidths();
        setButtonListeners();
        setColumnEditors(model);
    }

    protected void setButtonListeners() {
        getAddButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addRowActionPerformed(e);
            }
        });
        getEditButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editRowActionPerformed(e);
            }
        });
        getRemoveButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeRowActionPerformed(e);
            }
        });
    }
    
    protected void addRowActionPerformed(ActionEvent e) {
        stopCellEditing(table);
        
        InnerTableModel model = (InnerTableModel) table.getModel();
        selectCell(model.addRow(), 0);
        model.modelUpdatedFromUI();
        Utils.scrollToVisible(tablePanel);
    }
    
    protected void editRowActionPerformed(ActionEvent e) {
        editCell(table.getSelectedRow(), table.getSelectedColumn());
    }
    
    protected void removeRowActionPerformed(ActionEvent e) {
        int row = table.getSelectedRow();
        final int column = table.getSelectedColumn();
        final TableCellEditor cellEditor = table.getCellEditor(row, column);
        if (cellEditor != null) {
            cellEditor.cancelCellEditing();
        }
        InnerTableModel model = (InnerTableModel) table.getModel();
        int rowCount = model.getRowCount() - 1;
        model.removeRow(row);
        model.modelUpdatedFromUI();
        if (row >= rowCount) {
            row = rowCount - 1;
        }
        if (row >= 0) {
            final int n = row;
            Utils.runInAwtDispatchThread(new Runnable() {
                public void run() {
                    selectCell(n, column);
                }
            });
        }
    }
    

    private void stopCellEditing(final JTable table) {
        table.editCellAt(-1, -1); // finish possible editing
    }

    private void setColumnEditors(InnerTableModel model) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableCellEditor cellEditor = model.getCellEditor(i);
            if (cellEditor != null) {
                columnModel.getColumn(i).setCellEditor(cellEditor);
            }
        }
    }

    public JTable getTable() {
        return table;
    }

    public JButton getAddButton() {
        return tablePanel.getAddButton();
    }

    public JButton getEditButton() {
        return tablePanel.getEditButton();
    }

    public JButton getRemoveButton() {
        return tablePanel.getRemoveButton();
    }

    public void setColumnWidths() {
        final JTable theTable = tablePanel.getTable();
        InnerTableModel tableModel = (InnerTableModel) theTable.getModel();
        TableColumnModel columnModel = theTable.getColumnModel();
        for (int i = 0, n = columnModel.getColumnCount(); i < n; i++) {
            int width = tableModel.getDefaultColumnWidth(i);
            final TableColumn column = columnModel.getColumn(i);
            column.setPreferredWidth(width);
            column.setWidth(width);
        }
    }

    protected void editCell(final int row, final int column) {
        if (table.isCellEditable(row, column)) {
            selectCell(row, column);
            Utils.runInAwtDispatchThread(new Runnable() {
                public void run() {
                    final JTable table = getTable();
                    table.editCellAt(row, column);
                    Component editorComponent = table.getEditorComponent();
                    editorComponent.requestFocus();
                }
            });
        }
    }

    private void selectCell(final int row, final int column) {
        Utils.runInAwtDispatchThread(new Runnable() {
            public void run() {
                final JTable table = getTable();
                table.getSelectionModel().setLeadSelectionIndex(row);
                table.getColumnModel().getSelectionModel().setLeadSelectionIndex(column);
                table.requestFocus();
            }
        });
    }

    @Override
    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        ((InnerTableModel) getTable().getModel()).tableChanged();
        scheduleRefreshView();
    }

    private class TablePanel extends DefaultTablePanel {

        /**
         * Creates a new InnerTablePanel.
         *
         * @param model DefaultTableModel for included table
         */
        public TablePanel(final AbstractTableModel model) {
            super(model);
            final JTable table = getTable();
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    int selectedRowCount = table.getSelectedRowCount();
                    removeButton.setEnabled(selectedRowCount > 0);
                    editButton.setEnabled(selectedRowCount == 1);
                }
            });
        }

        public JButton getAddButton() {
            return addButton;
        }

        public JButton getEditButton() {
            return editButton;
        }

        public JButton getRemoveButton() {
            return removeButton;
        }
    }
}

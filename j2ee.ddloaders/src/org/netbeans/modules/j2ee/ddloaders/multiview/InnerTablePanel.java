/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author pfiala
 */
public class InnerTablePanel extends SectionInnerPanel {

    private final TablePanel tablePanel;
    private final XmlMultiViewDataObject dataObject;
    private JTable table;

    protected void setButtonListeners(final InnerTableModel model) {
        final JTable table = getTable();
        getAddButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopCellEditing(table);
                selectCell(model.addRow(), 0);
            }
        });
        getEditButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editCell(table.getSelectedRow(), table.getSelectedColumn());
            }
        });
        getRemoveButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopCellEditing(table);
                int row = table.getSelectedRow();
                final int column = table.getSelectedColumn();
                model.removeRow(row);
                int rowCount = model.getRowCount();
                if (row >= rowCount) {
                    row = rowCount - 1;
                }
                if (row >= 0) {
                    selectCell(row, column);
                }
            }
        });
    }

    private void stopCellEditing(final JTable table) {
        table.editCellAt(-1, -1); // finish possible editing
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

        public void modelUpdatedFromUI() {
            if (dataObject != null) {
                dataObject.modelUpdatedFromUI();
            }
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

    public InnerTablePanel(SectionNodeView sectionNodeView, final AbstractTableModel model) {
        super(sectionNodeView);
        this.dataObject = (XmlMultiViewDataObject) sectionNodeView.getDataObject();
        tablePanel = new TablePanel(model);
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                if (dataObject != null) {
                    dataObject.modelUpdatedFromUI();
                }
                if (e.getType() == TableModelEvent.DELETE || e.getType() == TableModelEvent.INSERT) {
                    adjustHeight();
                }
            }
        });
        table = tablePanel.getTable();
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(final TableModelEvent e) {
                int type = e.getType();
                if (type == TableModelEvent.UPDATE) {
                    final int column = e.getColumn() + 1;
                    if (column < model.getColumnCount()) {
                        //editCell(e.getFirstRow(), column);
                    }
                }
            }
        });
        table.setPreferredSize(table.getPreferredSize());
        table.setCellSelectionEnabled(true);
        InputMap inputMap = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "selectNextColumnCell"); //NOI18N
        inputMap.put(KeyStroke.getKeyStroke("shift ENTER"), "selectPreviousColumnCell");    //NOI18N
        setLayout(new BorderLayout());
        add(tablePanel, BorderLayout.WEST);
        setColumnWidths();
        if (model instanceof InnerTableModel) {
            InnerTableModel innerTableModel = (InnerTableModel) model;
            setButtonListeners(innerTableModel);
            setColumnEditors(innerTableModel);
        }
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
        final JTable table = tablePanel.getTable();
        InnerTableModel tableModel = (InnerTableModel) table.getModel();
        TableColumnModel columnModel = table.getColumnModel();
        int tableWidth = 0;
        for (int i = 0, n = columnModel.getColumnCount(); i < n; i++) {
            int width = tableModel.getDefaultColumnWidth(i);
            tableWidth += width;
            columnModel.getColumn(i).setPreferredWidth(width);
        }
        Dimension size = table.getPreferredSize();
        size.width = tableWidth;
        table.setPreferredSize(size);
    }

    public void adjustHeight() {
        JTable table = getTable();
        Dimension size = table.getPreferredSize();
        table.setPreferredSize(null);
        size.height = table.getPreferredSize().height;
        table.setPreferredSize(size);
        Utils.scrollToVisible(InnerTablePanel.this);
    }

    public void dataFileChanged() {
        adjustHeight();
    }

    protected void editCell(final int row, final int column) {
        if (table.isCellEditable(row, column)) {
            selectCell(row, column);
            SwingUtilities.invokeLater(new Runnable() {
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
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final JTable table = getTable();
                table.getSelectionModel().setLeadSelectionIndex(row);
                table.getColumnModel().getSelectionModel().setLeadSelectionIndex(column);
                table.requestFocus();
            }
        });
    }

    public JComponent getErrorComponent(String errorId) {
        return null;
    }

    public void setValue(JComponent source, Object value) {
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }

}

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
import javax.swing.table.DefaultTableModel;
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

    private class TablePanel extends DefaultTablePanel {

        /**
         * Creates a new InnerTablePanel.
         *
         * @param model DefaultTableModel for included table
         */
        public TablePanel(final DefaultTableModel model) {
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

    public InnerTablePanel(SectionNodeView sectionNodeView, DefaultTableModel model, TableCellEditor tableCellEditor) {
        super(sectionNodeView);
        this.dataObject = (XmlMultiViewDataObject) sectionNodeView.getDataObject();
        tablePanel = new TablePanel(model);
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                if (dataObject != null) {
                    dataObject.modelUpdatedFromUI();
                }
            }
        });
        table = tablePanel.getTable();
        if (tableCellEditor != null) {
            table.setCellEditor(tableCellEditor);
        }

        table.setPreferredSize(table.getPreferredSize());
        setLayout(new BorderLayout());
        add(tablePanel, BorderLayout.WEST);
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

    public void setColumnWidths(int[] widths) {
        final JTable table = tablePanel.getTable();
        TableColumnModel columnModel = table.getColumnModel();
        int tableWidth = 0;
        for (int i = 0, n = widths.length; i < n; i++) {
            int width = widths[i];
            tableWidth += width;
            columnModel.getColumn(i).setPreferredWidth(width);
        }
        Dimension size = table.getPreferredSize();
        size.width = tableWidth;
        table.setPreferredSize(size);
    }

    public InnerTablePanel(SectionNodeView sectionNodeView, DefaultTableModel model) {
        this(sectionNodeView, model, null);

    }

    public JComponent getErrorComponent(String errorId) {
        return null;
    }

    public void setValue(JComponent source, Object value) {
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }

}

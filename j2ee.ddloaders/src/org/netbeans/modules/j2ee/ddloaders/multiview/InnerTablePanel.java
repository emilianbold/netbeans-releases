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
import org.netbeans.modules.xml.multiview.ui.SectionView;

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

    private class TablePanel extends DefaultTablePanel {

        /**
         * Creates a new InnerTablePanel.
         *
         * @param model DefaultTableModel for included table
         */
        public TablePanel(final DefaultTableModel model) {
            super(model);
            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    model.addRow((Object[]) null);
                    modelUpdatedFromUI();
                }
            });
            final JTable table = getTable();
            removeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    model.removeRow(table.getSelectedRow());
                    modelUpdatedFromUI();
                }
            });
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    removeButton.setEnabled(table.getSelectedRow() >= 0);
                    modelUpdatedFromUI();
                }
            });
        }

        private void modelUpdatedFromUI() {
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

    public InnerTablePanel(SectionView sectionView, final XmlMultiViewDataObject dataObject, DefaultTableModel model,
            TableCellEditor tableCellEditor) {
        super(sectionView);
        this.dataObject = dataObject;
        tablePanel = new TablePanel(model);
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                if (dataObject != null) {
                    dataObject.modelUpdatedFromUI();
                }
            }
        });
        final JTable table = tablePanel.getTable();
        if (tableCellEditor != null) {
            table.setCellEditor(tableCellEditor);
        }

        table.setPreferredSize(table.getPreferredSize());
        setLayout(new BorderLayout());
        add(tablePanel, BorderLayout.WEST);
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
        for (int i = 0, n = widths.length; i < n; i++) {
            columnModel.getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    public InnerTablePanel(SectionView sectionView, final XmlMultiViewDataObject dataObject, DefaultTableModel model) {
        this(sectionView, dataObject, model, null);

    }

    public JComponent getErrorComponent(String errorId) {
        return null;
    }

    public void setValue(JComponent source, Object value) {
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }

}

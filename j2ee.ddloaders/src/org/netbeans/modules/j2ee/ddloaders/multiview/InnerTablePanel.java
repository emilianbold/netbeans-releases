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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author pfiala
 */
public class InnerTablePanel extends SectionInnerPanel {

    private final TablePanel tablePanel;
    private final XmlMultiViewDataObject dataObject;

    private static class TablePanel extends DefaultTablePanel {

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
                }
            });
            final JTable table = getTable();
            removeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    model.removeRow(table.getSelectedRow());
                }
            });
            editButton.setVisible(false);
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    removeButton.setEnabled(table.getSelectedRow() >= 0);
                }
            });
        }

    }

    public InnerTablePanel(SectionView sectionView, final XmlMultiViewDataObject dataObject, DefaultTableModel model,
            TableCellEditor tableCellEditor) {
        super(sectionView);
        this.dataObject = dataObject;
        tablePanel = new TablePanel(model);
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                dataObject.modelUpdatedFromUI();
            }
        });
        if (tableCellEditor != null) {
            tablePanel.getTable().setCellEditor(tableCellEditor);
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

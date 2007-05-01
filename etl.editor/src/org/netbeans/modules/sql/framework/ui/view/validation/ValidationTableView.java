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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.framework.ui.view.validation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.netbeans.modules.sql.framework.model.ValidationInfo;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.view.ButtonTableHeader;
import org.netbeans.modules.sql.framework.ui.view.SortableTableModel;


/**
 * @author Ritesh Adval
 */
public class ValidationTableView extends JPanel {

    private static URL warningImgUrl = ValidationTableView.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/Warning.png");

    private static URL errorImgUrl = ValidationTableView.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/Error.png");

    private static ImageIcon errorImg;

    private static ImageIcon warningImg;

    private JTable table;

    private IGraphView graphView;

    private TableCellRenderer cellRenderer;

    private String maxLengthStr = "THIS IS MAX LENGTH STRING";

    static {
        errorImg = new ImageIcon(errorImgUrl);
        warningImg = new ImageIcon(warningImgUrl);
    }

    public ValidationTableView(IGraphView gView) {
        this.graphView = gView;

        initGui();
    }

    private void initGui() {
        this.setLayout(new BorderLayout());

        ValidationTableModel model = new ValidationTableModel(Collections.EMPTY_LIST);
        SortableTableModel sortModel = new SortableTableModel(model);

        table = new JTable(sortModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getSelectionModel().addListSelectionListener(new TableListSelectionListener());

        TableColumnModel cModel = table.getColumnModel();

        //set the header with sort button
        ButtonTableHeader bHeader = new ButtonTableHeader();
        bHeader.setColumnModel(cModel);
        bHeader.setReorderingAllowed(false);
        table.setTableHeader(bHeader);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);

        //add scrollpane to this panel
        this.add(scrollPane, BorderLayout.CENTER);

        //add table cell renderer
        cellRenderer = new TableCellRenderer();
        table.setDefaultRenderer(Integer.class, cellRenderer);
        table.setDefaultRenderer(String.class, cellRenderer);

        //add mouse handler on table
        table.addMouseListener(new TableMouseAdapter());
    }

    public void setValidationInfos(List vInfos) {
        ValidationTableModel model = new ValidationTableModel(vInfos);
        SortableTableModel sortModel = new SortableTableModel(model);
        table.setModel(sortModel);

        //      set icon column size
        TableColumn column1 = table.getColumnModel().getColumn(0);
        column1.setResizable(false);
        column1.setMinWidth(30);
        column1.setPreferredWidth(30);
        column1.setMaxWidth(30);

        Iterator it = vInfos.iterator();

        int maxLength = 0;

        while (it.hasNext()) {
            ValidationInfo vInfo = (ValidationInfo) it.next();
            int newLength = vInfo.getDescription().length();

            if (newLength > maxLength) {
                maxLength = newLength;
                maxLengthStr = vInfo.getDescription();
            }
        }

    }

    private void setDescriptionColumnWidth(int width) {
        //      set description column size
        TableColumn column2 = table.getColumnModel().getColumn(1);
        column2.setMinWidth(100);
        column2.setPreferredWidth(width);
    }

    public void clearView() {
        ValidationTableModel model = new ValidationTableModel(Collections.EMPTY_LIST);
        SortableTableModel sortModel = new SortableTableModel(model);
        table.setModel(sortModel);
    }

    class TableMouseAdapter extends MouseAdapter {
        /**
         * Invoked when a mouse button has been pressed on a component.
         */
        public void mousePressed(MouseEvent e) {
            int row = table.getSelectedRow();
            SortableTableModel sortModel = (SortableTableModel) table.getModel();
            ValidationTableModel model = (ValidationTableModel) sortModel.getActualModel();
            int actualModelRow = sortModel.getActualModelRow(row);
            ValidationInfo vInfo = model.getValidationInfo(actualModelRow);
            ValidationHandlerFactory factory = new ValidationHandlerFactory(graphView);
            ValidationHandler vHandler = factory.getValidationHandler(vInfo);

            if (e.getClickCount() == 2) {
                Object validatedObject = vInfo.getValidatedObject(); 
                if (vHandler != null && validatedObject != null) {
                    vHandler.editValue(validatedObject);
                }
            }
        }
    }

    class TableCellRenderer extends DefaultTableCellRenderer {

        /**
         * Returns the default table cell renderer.
         * 
         * @param tbl the <code>JTable</code>
         * @param value the value to assign to the cell at <code>[row, column]</code>
         * @param isSelected true if cell is selected
         * @param isFocus true if cell has focus
         * @param row the row of the cell to render
         * @param column the column of the cell to render
         * @return the default table cell renderer
         */
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = null;

            if (value instanceof Integer) {
                int type = ((Integer) value).intValue();

                if (type == ValidationInfo.VALIDATION_WARNING) {
                    label = new JLabel(warningImg);
                    label.setToolTipText("Warning");
                } else {
                    label = new JLabel(errorImg);
                    label.setToolTipText("Error");
                }
                return label;
            }
            label = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

            label.setToolTipText(value.toString());
            return label;

        }

        public void paint(Graphics g) {
            FontMetrics fm = g.getFontMetrics();
            if (fm != null) {
                setDescriptionColumnWidth(fm.stringWidth(maxLengthStr) + 10);
            }

            super.paint(g);
        }

    }

    class TableListSelectionListener implements ListSelectionListener {

        /*
         * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
        public void valueChanged(ListSelectionEvent e) {
            int row = table.getSelectedRow();
            if (row != -1) {
                SortableTableModel sortModel = (SortableTableModel) table.getModel();
                ValidationTableModel model = (ValidationTableModel) sortModel.getActualModel();
                int actualModelRow = sortModel.getActualModelRow(row);
                if (actualModelRow != -1) {
                    ValidationInfo vInfo = model.getValidationInfo(actualModelRow);
                    ValidationHandlerFactory factory = new ValidationHandlerFactory(graphView);
                    factory.higlightInvalidNode(vInfo);
                }
            }
        }
    }
}


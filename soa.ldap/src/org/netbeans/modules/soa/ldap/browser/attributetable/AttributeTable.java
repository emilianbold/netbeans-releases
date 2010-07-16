/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.ldap.browser.attributetable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class AttributeTable extends JTable {

    private ExpandCollapseHandler expandCollapseHandler
            = new ExpandCollapseHandler();
    private ExpandAction expandAction = new ExpandAction();
    private CollapseAction collapseAction = new CollapseAction();

    private NameCellRenderer nameCellRenderer = new NameCellRenderer();
    private ValueCellRenderer valueCellRenderer = new ValueCellRenderer();

    public AttributeTable(AttributeTableModel model) {
        super(model);

        addMouseListener(expandCollapseHandler);

        TableColumnModel tableColumnModel = getColumnModel();

        TableColumn nameColumn = tableColumnModel.getColumn(0);
        TableColumn valueColumn = tableColumnModel.getColumn(1);

        nameColumn.setHeaderValue(NbBundle.getMessage(getClass(), 
                "ATTRIBUTE_TYPE_COLUMN_NAME")); // NOI18N
        nameColumn.setCellRenderer(nameCellRenderer);

        valueColumn.setHeaderValue(NbBundle.getMessage(getClass(), 
                "ATTRIBUTE_VALUE_COLUMN_NAME")); // NOI18N
        valueColumn.setCellRenderer(valueCellRenderer);

        getSelectionModel().setSelectionMode(ListSelectionModel
                .SINGLE_SELECTION);

        InputMap inputMap = getInputMap();
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
                "collapseRow"); // NOI18N
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
                "expandRow"); // NOI18N

        actionMap.put("collapseRow", collapseAction); // NOI18N
        actionMap.put("expandRow", expandAction); // NOI18N
    }

    public void setAttributeTableModel(AttributeTableModel model) {
        setModel(model);

        TableColumnModel tableColumnModel = getColumnModel();

        TableColumn nameColumn = tableColumnModel.getColumn(0);
        TableColumn valueColumn = tableColumnModel.getColumn(1);

        nameColumn.setHeaderValue(NbBundle.getMessage(getClass(),
                "ATTRIBUTE_TYPE_COLUMN_NAME")); // NOI18N
        nameColumn.setCellRenderer(nameCellRenderer);

        valueColumn.setHeaderValue(NbBundle.getMessage(getClass(),
                "ATTRIBUTE_VALUE_COLUMN_NAME")); // NOI18N
        valueColumn.setCellRenderer(valueCellRenderer);
    }

    @Override
    public int getRowHeight() {
        int height = super.getRowHeight();

        height += 2;

        return height;
    }

    private boolean expandCollapseRow(int row, 
            Boolean expectedCurrentExpandedStatus)
    {
        if (0 <= row && row < getRowCount()) {
            AttributeTableModel attributeTableModel
                    = (AttributeTableModel) getModel();

            AttributeNode attributeNode = getAttributeNode(row);

            if (attributeNode != null 
                    && attributeNode.getAttributeValueCount() > 0)
            {
                boolean expandedStatus = attributeNode.isExpanded();

                if (expectedCurrentExpandedStatus == null
                        || expectedCurrentExpandedStatus == expandedStatus)
                {
                    attributeNode.setExpanded(!expandedStatus);
                    return true;
                }
            }
        }
        return false;
    }

    private int getRow(AttributeTableRow rowInstance) {
        AttributeTableModel attributeTableModel
                = (AttributeTableModel) getModel();
        if (attributeTableModel == null) {
            return -1;
        }

        return attributeTableModel.getRowIndex(rowInstance);
    }

    private AttributeTableRow getAttributeTableRow(int row) {
        if (row < 0 || row >= getRowCount()) {
            return null;
        }

        AttributeTableModel attributeTableModel = (AttributeTableModel)
                getModel();

        if (attributeTableModel == null) {
            return null;
        }

        return attributeTableModel.getRow(row);
    }

    private AttributeNode getAttributeNode(int row) {
        AttributeTableRow attributeTableRow = getAttributeTableRow(row);
        return (attributeTableRow instanceof AttributeNode)
                ? (AttributeNode) attributeTableRow : null;
    }

    private AttributeValue getAttributeValue(int row) {
        AttributeTableRow attributeTableRow = getAttributeTableRow(row);
        return (attributeTableRow instanceof AttributeValue) 
                ? (AttributeValue) attributeTableRow : null;
    }

    private boolean isInExpandCollapseArea(Point point) {
        int row = rowAtPoint(point);
        int viewColumn = columnAtPoint(point);

        if (row < 0 || row >= getRowCount() || viewColumn < 0) {
            return false;
        }
    
        int modelColumn = convertColumnIndexToModel(viewColumn);

        if (modelColumn != 0) {
            return false;
        }

        Rectangle cellRect = getCellRect(row, viewColumn, false);
        if (cellRect == null) {
            return false;
        }

        if (point.x > cellRect.x + 16) {
            return false;
        }
        
        
        return true;
    }

    private class ExpandCollapseHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            Point point = e.getPoint();

            if (isInExpandCollapseArea(point)) {
                expandCollapseRow(rowAtPoint(point), null);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            Point point = e.getPoint();
            if (!e.isPopupTrigger()
                    && e.getClickCount() % 2 == 0
                    && !isInExpandCollapseArea(point))
            {
                expandCollapseRow(rowAtPoint(point), null);
            }
        }
    }

    private static class ValueCellRenderer extends DefaultTableCellRenderer {

        private Color myForeground = null;

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus,
                int row, int column)
        {
            isSelected = isSelected | hasFocus;
            hasFocus = false;
            
            myForeground = null;

            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);

            if (!isSelected && value instanceof AttributeValueCount) {
                myForeground = Color.GRAY;
            }
            
            return this;
        }

        @Override
        public Color getForeground() {
            return (myForeground != null) ? myForeground
                    : super.getForeground();
        }
    }

    private static class NameCellRenderer
            extends DefaultTableCellRenderer
    {
        private CellBorderDecorator borderDecorator = new CellBorderDecorator();

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus,
                int row, int column)
        {
            isSelected = isSelected | hasFocus;
            hasFocus = false;

            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);

            if (row >= 0) {
                int leftInset = 16;

                borderDecorator.setDefaultCellBorder(getBorder());

                AttributeTableModel attributeTableModel = (AttributeTableModel)
                        table.getModel();

                AttributeTableRow attributeTableRow = attributeTableModel
                        .getRow(row);

                Boolean expanded;
                if (attributeTableRow instanceof AttributeNode) {
                    AttributeNode attributeNode = (AttributeNode)
                            attributeTableRow;
                    if (attributeNode.getAttributeValueCount() > 0) {
                        expanded = Boolean.valueOf(attributeNode.isExpanded());
                    } else {
                        expanded = null;
                    }
                } else {
                    expanded = null;
                    leftInset += 16;
                }

                borderDecorator.setExpanded(expanded);
                borderDecorator.setLeftInset(leftInset);

                setBorder(borderDecorator);
            }

            return this;
        }
    }
    
    private class CollapseAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            int selectedRow = getSelectedRow();
            if (!expandCollapseRow(selectedRow, true)) {
                AttributeValue attributeValue = getAttributeValue(selectedRow);

                if (attributeValue != null) {
                    AttributeNode node = attributeValue.getAttributeNode();

                    int row = getRow(node);

                    if (row >= 0 && row < getRowCount()) {
                        getSelectionModel().setSelectionInterval(row, row);
                    }
                }
            }
        }
    }

    private class ExpandAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            int selectedRow = getSelectedRow();
            expandCollapseRow(selectedRow, false);
        }
    }

    private static class CellBorderDecorator implements Border {
        private Border defaultCellBorder;
        private Boolean expanded;
        private int leftInset;

        void setDefaultCellBorder(Border defaultCellBorder) {
            this.defaultCellBorder = defaultCellBorder;
        }

        void setLeftInset(int leftInset) {
            this.leftInset = leftInset;
        }

        void setExpanded(Boolean expanded) {
            this.expanded = expanded;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                int width, int height)
        {
            if (expanded != null) {
                Insets insets;
                if (defaultCellBorder != null) {
                    insets = defaultCellBorder.getBorderInsets(c);
                } else {
                    insets = new Insets(0, 0, 0, 0);
                }

                int cx = x + 8;
                int cy = y + insets.top + (height - insets.bottom
                        - insets.top) / 2;

                Color oldColor = g.getColor();

                g.setColor(Color.WHITE);
                g.fillRect(cx - 3, cy - 3, 7, 7);

                g.setColor(Color.DARK_GRAY);
                g.drawRect(cx - 4, cy - 4, 8, 8);

                g.setColor(Color.DARK_GRAY);
                g.drawLine(cx - 2, cy, cx + 2, cy);
                if (!expanded) {
                    g.drawLine(cx, cy - 2, cx, cy + 2);
                }

                g.setColor(oldColor);
            }

            if (defaultCellBorder != null) {
                defaultCellBorder.paintBorder(c, g, x, y, width, height);
            }
        }

        public Insets getBorderInsets(Component c) {
            Insets insets;
            if (defaultCellBorder != null) {
                insets = defaultCellBorder.getBorderInsets(c);
                insets.left += leftInset;
            } else {
                insets = new Insets(0, leftInset, 0, 0);
            }
            return insets;
        }

        public boolean isBorderOpaque() {
            return false;
        }
    }
}

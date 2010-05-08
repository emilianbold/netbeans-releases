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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.php.project.connections.ui.transfer.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * @author Radek Matous
 */
final class TransferFilterTable extends JTable {
    private static final long serialVersionUID = -1378410694823823371L;
    private static final int DARKER_COLOR_COMPONENT = 10;

    private final TransferFileTableModel model;
    private int rowHeightFontBase = -1;

    TransferFilterTable(TransferFileTableModel model) {
        super(model);
        //getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FileConfirmationTable.class, "ACN_UnitTable")); // NOI18N
        //getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UnitTab.class, "ACD_UnitTable")); // NOI18N
        this.model = model;

        setShowGrid(false);
        setColumnsSize();
        setIntercellSpacing(new Dimension(0, 0));
        revalidate();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        getParent().setBackground(getBackground());
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        String tip = null;
        Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);
        int realColumnIndex = convertColumnIndexToModel(colIndex);
        tip = model.getToolTipText(rowIndex, realColumnIndex);
        return tip != null ? tip : super.getToolTipText(e);
    }

    private void setColumnsSize() {
        int columnCount = model.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            TableColumn activeColumn = getColumnModel().getColumn(i);
            activeColumn.setPreferredWidth(model.getPreferredWidth(getTableHeader(), i));
        }
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        Color bgColor = getBackground();
        Color bgColorDarker = getDarkerColor(bgColor);

        TransferFileUnit u = model.getUnitAtRow(row);
        if (u != null && !u.canBeMarked()) {
            c.setForeground(Color.gray);
        } else {
            c.setFont(getFont());
            if (isRowSelected(row)) {
                c.setForeground(getSelectionForeground());
            } else {
                c.setForeground(getForeground());
            }
        }
        if (!isCellSelected(row, column)) {
            if (row % 2 == 0) {
                c.setBackground(bgColorDarker);
            } else {
                c.setBackground(bgColor);
            }
        }
        if (rowHeightFontBase < 0) {
            Font font =  c.getFont();
            FontMetrics fontMetrics = c.getFontMetrics(font);
            int def = new JTable().getRowHeight();
            rowHeightFontBase = Math.max(def, fontMetrics.getHeight());
            setRowHeight(rowHeightFontBase);
        }

        return c;
    }

    static Color getDarkerColor(Color color) {
        return new Color(
                Math.abs(color.getRed() - DARKER_COLOR_COMPONENT),
                Math.abs(color.getGreen() - DARKER_COLOR_COMPONENT),
                Math.abs(color.getBlue() - DARKER_COLOR_COMPONENT));
    }

    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new TransferFilterTableHeader();
    }

    private final class TransferFilterTableHeader extends JTableHeader {
        private static final long serialVersionUID = 19524897514214165L;

        private SortColumnHeaderRenderer sortingRenderer;
        int selectedRow = -1;

        TransferFilterTableHeader() {
            super(TransferFilterTable.this.getColumnModel());
            setTable(TransferFilterTable.this);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    setSelectedRow();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    assert sortingRenderer != null : "Sorting renderer cannot be null.";

                    TransferFileUnit selectedUnit = getSelectedUnit();

                    int column = getTable().columnAtPoint(e.getPoint());
                    sortingRenderer.setSorting(column);
                    repaint();

                    scroolToUnit(selectedUnit);
                }
            });
            getModel().addUpdateUnitListener(new TransferFileTableChangeListener() {
                @Override
                public void updateUnitsChanged() {
                    assert sortingRenderer != null : "Sorting renderer cannot be null.";
                    setSelectedRow();
                    TransferFileUnit selectedUnit = getSelectedUnit();
                    sortingRenderer.sort();
                    scroolToUnit(selectedUnit);
                }
                @Override
                public void filterChanged() {
                }
            });
            setReorderingAllowed(false);
        }

        @Override
        public void setDraggedColumn(TableColumn aColumn) {
            if (aColumn != null && aColumn.getModelIndex() == 0) {
                //don't allow the first column to be dragged
                return;
            }
            super.setDraggedColumn(aColumn);
        }

        @Override
        public void setDefaultRenderer(TableCellRenderer defaultRenderer) {
            if (!(defaultRenderer instanceof SortColumnHeaderRenderer)) {
                sortingRenderer = new SortColumnHeaderRenderer(getModel(), defaultRenderer);
                defaultRenderer = sortingRenderer;
            }
            super.setDefaultRenderer(defaultRenderer);
        }

        void setSelectedRow() {
            selectedRow = getTable().getSelectedRow();
        }

        // preserve selected row in order to scroll to it
        TransferFileUnit getSelectedUnit() {
            TransferFileUnit selectedUnit = null;
            TransferFileTableModel m = getModel();
            if (selectedRow != -1) {
                selectedUnit = m.getUnitAtRow(selectedRow);
            }
            return selectedUnit;
        }

        void scroolToUnit(TransferFileUnit selectedUnit) {
            if (selectedUnit == null) {
                return;
            }
            TransferFileTableModel m = getModel();
            int newRow = m.getRowForUnit(selectedUnit);
            assert newRow != -1 : String.format("Previoulsy selecte unit %s has to be found.", selectedUnit);
            getTable().getSelectionModel().setSelectionInterval(newRow, newRow);
            Rectangle rect = getTable().getCellRect(newRow, 0, true);
            getTable().scrollRectToVisible(rect);
        }

        private TransferFileTableModel getModel() {
            return (TransferFileTableModel) TransferFilterTable.this.getModel();
        }
    }
}

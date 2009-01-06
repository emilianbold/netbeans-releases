/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.dataview.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.border.IconBorder;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PipelineEvent;
import org.jdesktop.swingx.decorator.PipelineListener;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * The class {@code JXTableRowHeader} is used to create a column which contains
 * row header cells. By default a table will not show a row header. The user may
 * manually add a row header to the {@code JScrollPane} row header view port.
 *
 * @see javax.swing.JScrollPane
 * @author Ahimanikya Satapathy
 */
public class JXTableRowHeader extends JComponent {

    private static class InternalTableColumnModel extends DefaultTableColumnModel {

        public InternalTableColumnModel() {
            TableColumnExt col = new TableColumnExt(0, 75);
            col.setEditable(false);
            col.setHeaderValue("#");
            col.setToolTipText("Row Number");
            col.setSortable(false);
            addColumn(col);
        }
    }

    public JTableHeader getTableHeader() {
        JTableHeader header = headerTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        return header;
    }

    private class HeaderResizeListener implements TableModelListener {

        public void tableChanged(TableModelEvent e) {
            // pack before setting preferred width.
            headerTable.packAll();

            TableColumn column = headerTable.getColumnModel().getColumn(0);
            column.setPreferredWidth(column.getPreferredWidth() + 20);

            if (column.getPreferredWidth() != getWidth()) {
                headerTable.setPreferredScrollableViewportSize(new Dimension(
                        column.getPreferredWidth(), 0));
            }
        }
    }
    private static Icon rightArrow = new Icon() {

        public int getIconWidth() {
            return 8;
        }

        public int getIconHeight() {
            return 8;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.drawLine(x + 4, y + 4, x + 4, y + 4);
            g.translate(x + 4, y + 4);
            g.fillPolygon(new Polygon(new int[]{0, 5, 0}, new int[]{-5, 0, 5}, 3));
        }
    };
    private IconBorder iconBorder = new IconBorder();

    private class RowHeaderColumnRenderer extends DefaultTableRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, rowIndex, columnIndex);

            if (isSelected) {
                iconBorder.setIcon(rightArrow);
                Border origBorder = ((JComponent) comp).getBorder();
                Border border = new CompoundBorder(origBorder, iconBorder);
                ((JComponent) comp).setBorder(border);
                comp.setBackground(UIManager.getColor("Table.selectionBackground"));
                comp.setForeground(UIManager.getColor("Table.selectionForeground"));
            }
            return comp;
        }
    }
    /**
     * The headerTable used to create the row header column.
     */
    private final JXTableDecorator headerTable;
    private JTable table;

    /**
     * Create a row header from the given {@code JTable}. This row header will
     * have the same {@code TableModel} and {@code ListSelectionModel} as the
     * incoming table.
     *
     * @param table
     *            the table for which to produce a row header.
     */
    public JXTableRowHeader(JTable table) {
        this.table = table;
        this.headerTable = new JXTableDecorator(table.getModel(),
                new InternalTableColumnModel(), table.getSelectionModel()) {

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return getRowName(rowIndex);
            }
        };

        setLayout(new GridLayout(1, 1));

        this.headerTable.getModel().addTableModelListener(new HeaderResizeListener());
        this.headerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.headerTable.getTableHeader().setReorderingAllowed(false);
        this.headerTable.getTableHeader().setResizingAllowed(false);
        this.headerTable.setSortable(false);

        add(this.headerTable);
        TableColumn column = this.headerTable.getColumnModel().getColumn(0);

        // pack before setting preferred width.
        this.headerTable.packAll();

        column.setPreferredWidth(column.getPreferredWidth() + 20);
        column.setCellRenderer(createDefaultRenderer());
        this.headerTable.setPreferredScrollableViewportSize(new Dimension(
                column.getPreferredWidth(), 0));

        this.headerTable.setInheritsPopupMenu(true);
        this.headerTable.setRowHeight(table.getRowHeight());
        this.headerTable.setShowGrid(true, true);
        this.headerTable.setGridColor(ResultSetJXTable.GRID_COLOR);
        this.headerTable.setHighlighters(HighlighterFactory.createAlternateStriping(Color.WHITE, ResultSetJXTable.ALTERNATE_ROW_COLOR));

        JXTable jxTable = (JXTable) table;
        jxTable.getFilters().addPipelineListener(new PipelineListener() {

            public void contentsChanged(PipelineEvent e) {
                FilterPipeline pipeline = (FilterPipeline) e.getSource();
                // need to refresh the rows for header table
                headerTable.getFilters().getSortController().setSortKeys(pipeline.getSortController().getSortKeys());
                //headerTable.updateOnFilterContentChanged();
                // #445-swingx: header not updated
                headerTable.getTableHeader().repaint();
            }
        });
    }

    /**
     * Returns a default renderer to be used when no row header renderer is
     * defined by the constructor.
     *
     * @return the default row header renderer
     */
    protected TableCellRenderer createDefaultRenderer() {
        // TODO get a rollover enabled renderer
        //return new ColumnHeaderRenderer();
        return new RowHeaderColumnRenderer();
    }

    /**
     * Sets the default renderer to be used when no <code>headerRenderer</code>
     * is defined by a <code>TableColumn</code>.
     *
     * @param defaultRenderer
     *            the default renderer
     */
    public void setDefaultRenderer(TableCellRenderer defaultRenderer) {
        headerTable.getColumn(0).setCellRenderer(defaultRenderer);
    }

    /**
     * Returns the default renderer used when no <code>headerRenderer</code>
     * is defined by a <code>TableColumn</code>.
     *
     * @return the default renderer
     */
    public TableCellRenderer getDefaultRenderer() {
        return headerTable.getColumn(0).getCellRenderer();
    }

    /**
     * Returns the rectangle containing the header tile at <code>row</code>.
     * When the <code>row</code> parameter is out of bounds this method uses
     * the same conventions as the <code>JTable</code> method
     * <code>getCellRect</code>.
     *
     * @return the rectangle containing the header tile at <code>row</code>
     * @see JTable#getCellRect
     */
    public Rectangle getHeaderRect(int row) {
        return headerTable.getCellRect(row, 0, true);
    }

    public void setTable(JTable table) {
        this.table = table;
        headerTable.setModel(table.getModel());
        headerTable.setSelectionModel(table.getSelectionModel());
    }

    /**
     * @return the table
     */
    public JTable getTable() {
        return table;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        return headerTable.getToolTipText(event);
    }

    /**
     * Returns the index of the row that <code>point</code> lies in, or -1 if
     * the result is not in the range [0, <code>getRowCount()</code>-1].
     *
     * @param point
     *            the location of interest
     * @return the index of the row that <code>point</code> lies in, or -1 if
     *         the result is not in the range [0, <code>getRowCount()</code>-1]
     * @see JTable#columnAtPoint
     */
    public int rowAtPoint(Point point) {
        return headerTable.rowAtPoint(point);
    }

    /**
     * Returns the row name for this row.
     * <p>
     * This implementation returns the row as a counting number ({@code row + 1}).
     *
     * @param row
     *            the row in the view being required.
     * @return the name of the row at position {@code row} in the view where the
     *         first row is row 0.
     */
    public String getRowName(int row) {
        return Integer.toString(row + 1);
    }
}

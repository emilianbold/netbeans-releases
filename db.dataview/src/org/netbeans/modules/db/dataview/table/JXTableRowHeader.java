/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.dataview.table;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.border.IconBorder;
import org.jdesktop.swingx.decorator.HighlighterFactory;
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
public final class JXTableRowHeader extends JComponent {

    private static class InternalTableColumnModel extends DefaultTableColumnModel {

        public InternalTableColumnModel() {
            TableColumnExt col = new TableColumnExt(0, 75);
            col.setEditable(false);
            col.setHeaderValue("#");
            col.setToolTipText("Row number");
            col.setSortable(false);
            addColumn(col);
        }
    }

    private static class CountingTableModel implements TableModel {
        private int count;
        
        Set<TableModelListener> listeners = new HashSet<TableModelListener>();

        public void setCount(int count) {
            this.count = count;
            for(TableModelListener tml: listeners) {
                tml.tableChanged(new TableModelEvent(this));
            }
        }
        
        @Override
        public void addTableModelListener(TableModelListener tl) {
            listeners.add(tl);
        }

        @Override
        public Class<?> getColumnClass(int i) {
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int i) {
            return "Row number";
        }

        @Override
        public int getRowCount() {
            return this.count;
        }

        @Override
        public Object getValueAt(int row, int col) {
            return Integer.toString(row + 1);
        }

        @Override
        public boolean isCellEditable(int i, int i1) {
            return false;
        }

        @Override
        public void removeTableModelListener(TableModelListener tl) {
            listeners.remove(tl);
        }

        @Override
        public void setValueAt(Object o, int i, int i1) {
            throw new NoSuchMethodError();
        }
    }

    private final PropertyChangeListener backingTableListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            JTable t = (JTable) pce.getSource();
            if(pce.getPropertyName().equals("rowSorter") || pce.getPropertyName().equals("sorter")) {
                if(backingSorter != null) {
                    backingSorter.removeRowSorterListener(rowSorterListener);
                }
                if(pce.getNewValue() != null) {
                    backingSorter = (RowSorter) pce.getNewValue();
                    backingSorter.addRowSorterListener(rowSorterListener);
                }
                updateRowCount();
            } else if (pce.getPropertyName().equals("rowHeight")) {
                headerTable.setRowHeight((Integer) pce.getNewValue());
            } else if ("model".equals(pce.getPropertyName())) {
                if (backingTableModel != null) {
                    backingTableModel.removeTableModelListener(
                            tableModelListener);
                }
                backingTableModel = (TableModel) pce.getNewValue();
                if (backingTableModel != null) {
                    backingTableModel.addTableModelListener(tableModelListener);
                }
                updateRowCount();
            }
        }
    };

    private void updateRowCount() {
        // Run after all TableModelListeners are processed. We need the Row
        // Sorter to contain valid values. Ugly solution, but works.
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                ctm.setCount(backingTable.getRowCount());
            }
        });
    }

    private TableModelListener tableModelListener = new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent e) {
            updateRowCount();
        }
    };
    
    private RowSorterListener rowSorterListener = new RowSorterListener() {
        @Override
        public void sorterChanged(RowSorterEvent rse) {
            updateRowCount();
        }
    };
    
    public JTableHeader getTableHeader() {
        JTableHeader header = headerTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        return header;
    }

    private static Icon rightArrow = new Icon() {

        @Override
        public int getIconWidth() {
            return 8;
        }

        @Override
        public int getIconHeight() {
            return 8;
        }

        @Override
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
    private final CountingTableModel ctm = new CountingTableModel();
    private final JXTable headerTable;
    private JXTable backingTable;
    private TableModel backingTableModel;
    private RowSorter<?> backingSorter;

    /**
     * Create a row header from the given {@code JTable}. This row header will
     * have the same {@code TableModel} and {@code ListSelectionModel} as the
     * incoming table.
     *
     * @param table
     *            the table for which to produce a row header.
     */
    public JXTableRowHeader(JXTable table) {
        assert table != null : "JXTableRowHeader needs to be instanciated with a JXTable";

        this.backingTable = table;
        this.backingTableModel = table.getModel();
        this.backingTableModel.addTableModelListener(tableModelListener);

        headerTable = new JXTableDecorator(ctm,
                new JXTableRowHeader.InternalTableColumnModel());

        ctm.setCount(backingTable.getRowCount());
        backingTable.addPropertyChangeListener(backingTableListener);
        headerTable.setRowHeight(backingTable.getRowHeight());
        headerTable.setSelectionModel(backingTable.getSelectionModel());

        setLayout(new GridLayout(1, 1));

        this.headerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.headerTable.getTableHeader().setReorderingAllowed(false);
        this.headerTable.getTableHeader().setResizingAllowed(false);

        add(this.headerTable);
        TableColumn column = this.headerTable.getColumnModel().getColumn(0);

        // pack before setting preferred width.
        this.headerTable.packAll();

        TableCellRenderer defaultRenderer = createDefaultRenderer();

        Component c = defaultRenderer.getTableCellRendererComponent(
                headerTable, "00000", false, false, 0, 0);              //NOI18N

        column.setPreferredWidth((int) c.getMinimumSize().getWidth() + 10);
        column.setCellRenderer(createDefaultRenderer());
        this.headerTable.setPreferredScrollableViewportSize(new Dimension(
                column.getPreferredWidth(), 0));

        this.headerTable.setInheritsPopupMenu(true);
        this.headerTable.setShowGrid(true, true);
        this.headerTable.setGridColor(ResultSetJXTable.GRID_COLOR);
        this.headerTable.setHighlighters(
                HighlighterFactory.createAlternateStriping(
                ResultSetJXTable.ROW_COLOR, ResultSetJXTable.ALTERNATE_ROW_COLOR));
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
        return new JXTableRowHeader.RowHeaderColumnRenderer();
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        return headerTable.getToolTipText(event);
    }
}

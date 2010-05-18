/*
 * Copyright 09/23/08 Sun Microsystems, Inc. All Rights Reserved
 * Use is subject to license terms.
 *
 * @(#)TableSorter.java 1.2 08/09/23
 */
package org.netbeans.modules.dlight.visualizers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

final class TableSorter extends AbstractTableModel implements TableModelListener {

    private static final boolean TRACE = Boolean.getBoolean("table.sorter.trace");//NOI18N
    private int indexes[];
    private List<Integer> sortingColumns = new Vector<Integer>();
    private int compares;
    private TableModel model;
    List<Boolean> sortingColumnsOrder = new Vector<Boolean>();

    public TableSorter() {
        indexes = new int[0]; // For consistency.
    }

    List<Integer> getSortingColumns() {
        return sortingColumns;
    }

    public TableSorter(TableModel model) {
        setModel(model);
    }

    private void setModel(TableModel tablemodel) {
        if (model != null) {
            model.removeTableModelListener(this);
        }
        model = tablemodel;
        this.sortingColumns.clear();
        this.sortingColumnsOrder.clear();
        fireTableStructureChanged();
        fireTableChanged(new TableModelEvent(this));
        tablemodel.addTableModelListener(this);
        if (TRACE) {
            System.out.printf("Sorter: @%d setModel\n", hashCode());//NOI18N
        }
        reallocateIndexes();

    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        super.addTableModelListener(l);
        if (model != null) {
            this.model.addTableModelListener(l);
        }
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        super.removeTableModelListener(l);
        if (model != null) {
            model.removeTableModelListener(l);
        }
    }

    public synchronized int getRowCount() {
        return model != null ? model.getRowCount() : 0;
    }

    public int getColumnCount() {
        return model != null ? model.getColumnCount() : 0;
    }

    @Override
    public String getColumnName(int i) {
        return model.getColumnName(i);
    }

    @Override
    public Class<?> getColumnClass(int i) {
        return model.getColumnClass(i);
    }

    @Override
    public boolean isCellEditable(int i, int j) {
        return model.isCellEditable(i, j);
    }

    public int compareRowsByColumn(int row1, int row2, int column) {
        Class<?> type = model.getColumnClass(column);
        TableModel data = model;

        // Check for nulls

        Object o1 = data.getValueAt(row1, column);
        Object o2 = data.getValueAt(row2, column);

        // If both values are null return 0
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) { // Define null less than everything.
            return -1;
        } else if (o2 == null) {
            return 1;
        }

        /* We copy all returned values from the getValue call in case
        an optimised model is reusing one object to return many values.
        The Number subclasses in the JDK are immutable and so will not be used in
        this way but other subclasses of Number might want to do this to save
        space and avoid unnecessary heap allocation.
         */
        if (type.getSuperclass() == java.lang.Number.class) {
            Number n1 = (Number) data.getValueAt(row1, column);
            double d1 = n1.doubleValue();
            Number n2 = (Number) data.getValueAt(row2, column);
            double d2 = n2.doubleValue();

            if (d1 < d2) {
                return -1;
            } else if (d1 > d2) {
                return 1;
            } else {
                return 0;
            }
        } else if (type == java.util.Date.class) {
            Date d1 = (Date) data.getValueAt(row1, column);
            long n1 = d1.getTime();
            Date d2 = (Date) data.getValueAt(row2, column);
            long n2 = d2.getTime();

            if (n1 < n2) {
                return -1;
            } else if (n1 > n2) {
                return 1;
            } else {
                return 0;
            }
        } else if (type == String.class) {
            String s1 = (String) data.getValueAt(row1, column);
            String s2 = (String) data.getValueAt(row2, column);
            int result = s1.compareTo(s2);

            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            } else {
                return 0;
            }
        } else if (type == Boolean.class) {
            Boolean bool1 = (Boolean) data.getValueAt(row1, column);
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean) data.getValueAt(row2, column);
            boolean b2 = bool2.booleanValue();

            if (b1 == b2) {
                return 0;
            } else if (b1) // Define false < true
            {
                return 1;
            } else {
                return -1;
            }
        } else {
            Object v1 = data.getValueAt(row1, column);
            String s1 = v1.toString();
            Object v2 = data.getValueAt(row2, column);
            String s2 = v2.toString();
            int result = s1.compareTo(s2);

            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public int compare(int row1, int row2) {
        compares++;
        for (int level = 0; level < sortingColumns.size(); level++) {
            Integer column = sortingColumns.get(level);
            int result = compareRowsByColumn(row1, row2, column.intValue());
            if (result != 0) {
                return sortingColumnsOrder.get(level) ? result : -result;
            }
        }
        return 0;
    }

    public synchronized void reallocateIndexes() {
        int rowCount = model.getRowCount();

        // Set up a new array of indexes with the right number of elements
        // for the new data model.
        indexes = new int[rowCount];

        // Initialise with the identity mapping.
        for (int row = 0; row < rowCount; row++) {
            indexes[row] = row;
        }
    }

    public void tableChanged(TableModelEvent e) {
        if (TRACE) {
            System.out.printf("Sorter: @%d tableChanged\n", hashCode());//NOI18N
        }
        reallocateIndexes();

        fireTableChanged(e);
    }

    public synchronized void checkModel() {
        if (indexes.length != model.getRowCount()) {
            if (TRACE) {
                System.out.printf("Sorter: @%d not informed of a change in model.\n", hashCode());//NOI18N
            }
            reallocateIndexes();
        }
    }

    public void sort(Object sender) {
        checkModel();

        compares = 0;
        // n2sort();
        // qsort(0, indexes.length-1);
        shuttlesort(indexes.clone(), indexes, 0, indexes.length);
        //  System.out.println("Compares: "+compares);
    }

    public void n2sort() {
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = i + 1; j < getRowCount(); j++) {
                if (compare(indexes[i], indexes[j]) == -1) {
                    swap(i, j);
                }
            }
        }
    }

    // This is a home-grown implementation which we have not had time
    // to research - it may perform poorly in some circumstances. It
    // requires twice the space of an in-place algorithm and makes
    // NlogN assigments shuttling the values between the two
    // arrays. The number of compares appears to vary between N-1 and
    // NlogN depending on the initial order but the main reason for
    // using it here is that, unlike qsort, it is stable.
    public void shuttlesort(int from[], int to[], int low, int high) {
        if (high - low < 2) {
            return;
        }
        int middle = (low + high) / 2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);

        int p = low;
        int q = middle;

        /* This is an optional short-cut; at each recursive call,
        check to see if the elements in this subset are already
        ordered.  If so, no further comparisons are needed; the
        sub-array can just be copied.  The array must be copied rather
        than assigned otherwise sister calls in the recursion might
        get out of sinc.  When the number of elements is three they
        are partitioned so that the first set, [low, mid), has one
        element and and the second, [mid, high), has two. We skip the
        optimisation when the number of elements is three or less as
        the first compare in the normal merge will produce the same
        sequence of steps. This optimisation seems to be worthwhile
        for partially ordered lists but some analysis is needed to
        find out how the performance drops to Nlog(N) as the initial
        order diminishes - it may drop very quickly.  */

        if (high - low >= 4 && compare(from[middle - 1], from[middle]) <= 0) {
            for (int i = low; i < high; i++) {
                to[i] = from[i];
            }
            return;
        }

        // A normal merge.

        for (int i = low; i < high; i++) {
            if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
                to[i] = from[p++];
            } else {
                to[i] = from[q++];
            }
        }
    }

    public void swap(int i, int j) {
        int tmp = indexes[i];
        indexes[i] = indexes[j];
        indexes[j] = tmp;
    }

    // The mapping only affects the contents of the data rows.
    // Pass all requests to these rows through the mapping array: "indexes".
    public synchronized Object getValueAt(int aRow, int aColumn) {
        checkModel();
        return model.getValueAt(indexes[aRow], aColumn);
    }

    @Override
    public synchronized void setValueAt(Object aValue, int aRow, int aColumn) {
        checkModel();
        model.setValueAt(aValue, indexes[aRow], aColumn);
    }

    public void sortByColumn(int column) {
        sortByColumn(column, true);
    }

    public void sortByColumn(int column, boolean ascending) {
//      this.ascending = ascending;
        sortingColumnsOrder.clear();
        sortingColumnsOrder.add(Boolean.valueOf(ascending));
        sortingColumns.clear();
        sortingColumns.add(Integer.valueOf(column));
        sort(this);
        fireTableChanged(new TableModelEvent(this));
    }

    public void clickOnColumn(int column){
        boolean ascending = true;
        if (TableSorter.this.sortingColumns.contains(column)) {
            ascending = !TableSorter.this.sortingColumnsOrder.get(TableSorter.this.sortingColumns.indexOf(column));
        }
        sortByColumn(column, ascending);
    }

    // There is no-where else to put this.
    // Add a mouse listener to the Table to trigger a table sort
    // when a column heading is clicked in the JTable.
    public void addMouseListenerToHeaderInTable(JTable table) {
        final TableSorter sorter = this;
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (e.getClickCount() == 1 && column != -1) {
                    //System.out.println("Sorting ...");
                    //  int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK;
                    //boolean ascending = (shiftPressed == 0);
                    //if we have already in sortedColumns change
                    boolean ascending = true;
                    if (TableSorter.this.sortingColumns.contains(column)) {
                        ascending = !TableSorter.this.sortingColumnsOrder.get(TableSorter.this.sortingColumns.indexOf(column));
                    }
                    sorter.sortByColumn(column, ascending);
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.setDefaultRenderer(new MultiSortTableCellHeaderRenderer());
        th.addMouseListener(listMouseListener);
    }
}

class MultiSortTableCellHeaderRenderer extends DefaultTableCellRenderer {

    protected SortIcon sortIcon = new SortIcon(8);

    public MultiSortTableCellHeaderRenderer() {
        setHorizontalAlignment(0);
        setHorizontalTextPosition(10);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        JTableHeader tableHeader = table.getTableHeader();
        Color fg = null;
        Color bg = null;
        Border border = null;
        Icon icon = null;

        if (hasFocus) {
            fg = UIManager.getColor("TableHeader.focusCellForeground"); // NOI18N
            bg = UIManager.getColor("TableHeader.focusCellBackground"); // NOI18N
            border = UIManager.getBorder("TableHeader.focusCellBorder"); // NOI18N
        }

        if (fg == null) {
            fg = tableHeader.getForeground();
        }
        if (bg == null) {
            bg = tableHeader.getBackground();
        }
        if (border == null) {
            border = UIManager.getBorder("TableHeader.cellBorder"); // NOI18N
        }
        //   if (!tableHeader.isPaintingForPrint() && table.getRowSorter() != null) {
        icon = getSortIcon(table, table.convertColumnIndexToModel(column));
        //  }

        setFont(tableHeader.getFont());
        setText(value != null && value != "" ? value.toString() : " "); // NOI18N
        setBorder(border);

        setIcon(icon);

        return this;
    }

    protected Icon getSortIcon(JTable table, int column) {
        if (!(table.getModel() instanceof TableSorter)) {
            return null;
        }
        TableSorter tableSorter = (TableSorter) table.getModel();
        //   List sortKeys = table.getRowSorter().getSortKeys();
        List<Integer> columns = tableSorter.getSortingColumns();
        if (columns == null || columns.size() == 0) {
            return null;
        }

        int priority = 0;
        sortIcon.setPriority(priority);
        int index = columns.indexOf(Integer.valueOf(column));
        if (index == -1) {
            return null;
        }
        sortIcon.setSortOrder(tableSorter.sortingColumnsOrder.get(index).booleanValue());

//      for (SortKey sortKey : sortKeys) {
//        if (sortKey.getColumn() == column) {
//          sortIcon.setPriority(priority);
//          sortIcon.setSortOrder(sortKey.getSortOrder());
//          return sortIcon;
//        }
//
//        priority++;
//      }

        return sortIcon;
    }

    final static class SortIcon implements Icon, SwingConstants {

        private static int NONE = 0;
        private int baseSize;
        private int size;
        private int direction = NONE;
        private BasicArrowButton iconRenderer;
        private double[] sizePercentages = {1.0, .85, .70, .55, .40, .25, .10};

        public SortIcon(int size) {
            this.baseSize = this.size = size;
            iconRenderer = new BasicArrowButton(direction);
        }

        public void setPriority(int priority) {
            size = (int) (baseSize * sizePercentages[priority]);
        }

        public void setSortOrder(boolean ascending) {
            direction = ascending ? SOUTH : NORTH;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (direction != NONE) {
                iconRenderer.paintTriangle(g, x, y, size, direction, true);
            }
        }

        public int getIconWidth() {
            return size;
        }

        public int getIconHeight() {
            return size / 2;
        }
    }
}

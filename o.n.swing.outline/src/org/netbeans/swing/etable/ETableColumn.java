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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.swing.etable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * Special type of TableColumn object used by ETable. 
 * @author David Strupl
 */
public class ETableColumn extends TableColumn implements Comparable<ETableColumn> {
    
    /** Used as a key or part of a key by the persistence mechanism. */
    private static final String PROP_PREFIX = "ETableColumn-";
    
    /** Used as a key or part of a key by the persistence mechanism. */
    private static final String PROP_WIDTH = "Width";
    
    /** Used as a key or part of a key by the persistence mechanism. */
    private static final String PROP_PREFERRED_WIDTH = "PreferredWidth";
    
    /** Used as a key or part of a key by the persistence mechanism. */
    private static final String PROP_SORT_RANK = "SortRank";
    
    /** Used as a key or part of a key by the persistence mechanism. */
    private static final String PROP_COMPARATOR = "Comparator";
    
    /** Used as a key or part of a key by the persistence mechanism. */
    private static final String PROP_HEADER_VALUE = "HeaderValue";
    
    /** Used as a key or part of a key by the persistence mechanism. */
    private static final String PROP_MODEL_INDEX = "ModelIndex";
    
    /** Used as a key or part of a key by the persistence mechanism. */
    private static final String PROP_ASCENDING = "Ascending";
    
    /** */
    private int sortRank = 0;
    /** */
    private Comparator<ETable.RowMapping> comparator;
    /** */
    private boolean ascending = true;
    /** */
    private boolean headerRendererSetExternally = false;
    /** */
    private ETable table;
    /** */
    private Icon customIcon;
    
    /** Header renderer created by createDefaultHeaderRenderer. */
    private TableCellRenderer myHeaderRenderer;
    private Comparator nestedComparator;
    
    /** Creates a new instance of ETableColumn */
    public ETableColumn(ETable table) {
        super();
        this.table = table;
    }
    
    /** Creates a new instance of ETableColumn */
    public ETableColumn(int modelIndex, ETable table) {
        super(modelIndex);
        this.table = table;
    }
    
    /** Creates a new instance of ETableColumn */
    public ETableColumn(int modelIndex, int width, ETable table) {
        super(modelIndex, width);
        this.table = table;
    }
    
    /** Creates a new instance of ETableColumn */
    public ETableColumn(int modelIndex, int width, TableCellRenderer cellRenderer, TableCellEditor cellEditor, ETable table) {
        super(modelIndex, width, cellRenderer, cellEditor);
        this.table = table;
    }
    
    /**
     * This method marks this column as sorted. Value 0 of the parameter rank
     * means that this column is not sorted.
     * @param rank value 1 means that this is the most important sorted
     *        column, number 2 means second etc.
     * @param ascending true means ascending
     * @since 1.3
     * @deprecated This method has no effect if the column was not already sorted before.
     *             Use {@link ETableColumnModel#setColumnSorted(org.netbeans.swing.etable.ETableColumn, boolean, int)} instead.
     */
    @Deprecated
    public void setSorted(int rank, boolean ascending) {
        if (!isSortingAllowed() && (rank != 0 || comparator != null)) {
            throw new IllegalStateException("Cannot sort an unsortable column.");
        }
        this.ascending = ascending;
        sortRank = rank;
        if (rank != 0) {
            comparator = getRowComparator(getModelIndex(), ascending);
        } else {
            comparator = null;
        }
    }
    
    /**
     * Returns true if the table is sorted using this column.
     */
    public boolean isSorted() {
        return comparator != null;
    }
    
    /**
     * Rank value 1 means that this is the most important column
     * (with respect to the table sort), value 2 means second etc.
     * Please note: the column has to be already sorted when calling this method.
     * @param newRank value 1 means that this is the most important sorted
     *        column, number 2 means second etc.
     * @since 1.3
     */
    public void setSortRank(int newRank) {
        if (!isSortingAllowed() && newRank != 0) {
            throw new IllegalStateException("Cannot sort an unsortable column.");
        }
        sortRank = newRank;
    }
    
    /**
     * Rank value 1 means that this is the most important column
     * (with respect to the table sort), value 2 means second etc.
     * To ask for the value of rank makes sense only when isSorted() returns
     * true. If isSorted() returns false this method should return 0.
     */
    public int getSortRank() {
        return sortRank;
    }
    
    /**
     * Returns the comparator used for sorting. The returned comparaotor
     * operates over ETable.RowMapping objects.
     */
    Comparator<ETable.RowMapping> getComparator() {
        return comparator;
    }
    
    /**
     * Checks whether the sort order is ascending (true means ascending,
     * false means descending).
     * @return true for ascending order
     */
    public boolean isAscending() {
        return ascending;
    }
    
    /**
     * Sets the sort order. Please note: the column has to be already
     * sorted when calling this method otherwise IllegalStateException
     * is thrown.
     * @param ascending true means ascending
     * @since 1.3
     */
    public void setAscending(boolean ascending) {
        if (!isSortingAllowed()) {
            throw new IllegalStateException("Cannot sort an unsortable column.");
        }
        if (! isSorted()) {
            return;
        }
        if (this.ascending == ascending) {
            return;
        }
        Comparator<ETable.RowMapping> c = getRowComparator(getModelIndex(), ascending);
        if (c == null) {
            throw new IllegalStateException("getRowComparator returned null for " + this); // NOI18N
        }
        this.ascending = ascending;
        this.comparator = c;
    }
    
    /**
     * Allows to set the header renderer. If this method is not called
     * we use our special renderer created by method 
     * createDefaultHeaderRenderer().
     */
    @Override
    public void setHeaderRenderer(TableCellRenderer tcr) {
        headerRendererSetExternally = true;
        super.setHeaderRenderer(tcr);
    }
    
    /**
     * Use a special renderer (result of calling createDefaultHeaderRenderer)
     * if it was not set by setHeaderRenderer.
     */
    @Override
    public TableCellRenderer getHeaderRenderer() {
        if (headerRendererSetExternally) {
            return super.getHeaderRenderer();
        }
	return createDefaultHeaderRenderer();
    }

    /**
     * The column can be hidden if this method returns true.
     */
    public boolean isHidingAllowed() {
        return true;
    }
    
    /**
     * The column can be sorted if this method returns true.
     */
    public boolean isSortingAllowed() {
        return true;
    }
    
    /**
     * Allows setting a custom icon for this column.
     */
    public void setCustomIcon(Icon i) {
        customIcon = i;
    }
    
    /**
     * Computes preferred width of the column by checking all the
     * data in the given column. If the resize parameter is true
     * it also directly resizes the column to the computed size (besides
     * setting the preferred size).
     */
    void updatePreferredWidth(JTable table, boolean resize) {
        TableModel dataModel = table.getModel();
        int rows = dataModel.getRowCount();
        if (rows == 0) {
            return;
        }
        int sum = 0;
        int max = 15;
        for (int i = 0; i < rows; i++) {
            Object data = dataModel.getValueAt(i, modelIndex);
            int estimate = estimatedWidth(data, table);
            sum += estimate;
            if (estimate > max) {
                max = estimate;
            }
        }
        max += 5;
        setPreferredWidth(max);
        if (resize) {
            resize(max, table);
        }
    }

    /**
     * Forces the table to resize given column.
     */
    private void resize(int newWidth, JTable table) {
        int oldWidth = getWidth();
        JTableHeader header = table.getTableHeader();
        if (header == null) {
            return;
        }
        header.setResizingColumn(this);
        final int oldMin = getMinWidth();
        final int oldMax = getMaxWidth();
        setMinWidth(newWidth);
        setMaxWidth(newWidth);
        setWidth(newWidth);
        // The trick is to restore the original values
        // after the table has be layouted. During layout this column
        // has fixed width (by setting min==max==preffered)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setMinWidth(oldMin);
                setMaxWidth(oldMax);
            }
        });
        Container container;
        if ((header.getParent() == null) ||
                ((container = header.getParent().getParent()) == null) ||
                !(container instanceof JScrollPane)) {
            header.setResizingColumn(null);
            return;
        }
        
        if (!container.getComponentOrientation().isLeftToRight() &&
                ! header.getComponentOrientation().isLeftToRight()) {
            if (table != null) {
                JViewport viewport = ((JScrollPane)container).getViewport();
                int viewportWidth = viewport.getWidth();
                int diff = newWidth - oldWidth;
                int newHeaderWidth = table.getWidth() + diff;
                
                /* Resize a table */
                Dimension tableSize = table.getSize();
                tableSize.width += diff;
                table.setSize(tableSize);
                
                /* If this table is in AUTO_RESIZE_OFF mode and
                 * has a horizontal scrollbar, we need to update
                 * a view's position.
                 */
                if ((newHeaderWidth >= viewportWidth) &&
                        (table.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF)) {
                    Point p = viewport.getViewPosition();
                    p.x = Math.max(0, Math.min(newHeaderWidth - viewportWidth, p.x + diff));
                    viewport.setViewPosition(p);
                }
            }
        }
        header.setResizingColumn(null);
    }
    
    /**
     * @returns width in pixels of the graphical representation of the data.
     */
    private int estimatedWidth(Object dataObject, JTable table) {
        TableCellRenderer cr = getCellRenderer();
        if (cr == null) {
            Class c = table.getModel().getColumnClass(modelIndex);
            cr = table.getDefaultRenderer(c);
        }
        Component c = cr.getTableCellRendererComponent(table, dataObject, false,
                false, 0, table.getColumnModel().getColumnIndex(getIdentifier()));
        return c.getPreferredSize().width;
    }
    
    /**
     * Method allowing to read stored values.
     * The stored values should be only those that the user has customized,
     * it does not make sense to store the values that were set using 
     * the initialization code because the initialization code can be run
     * in the same way after restart.
     */
    public void readSettings(Properties p, int index, String propertyPrefix) {
        String myPrefix = propertyPrefix + PROP_PREFIX + Integer.toString(index) + "-";
        String s0 = p.getProperty(myPrefix + PROP_MODEL_INDEX);
        if (s0 != null) {
            modelIndex = Integer.parseInt(s0);
        }
        String s1 = p.getProperty(myPrefix + PROP_WIDTH);
        if (s1 != null) {
            width = Integer.parseInt(s1);
        }
        String s2 = p.getProperty(myPrefix + PROP_PREFERRED_WIDTH);
        if (s2 != null) {
            setPreferredWidth(Integer.parseInt(s2));
        }
        ascending = true;
        String s4 = p.getProperty(myPrefix + PROP_ASCENDING);
        if ("false".equals(s4)) {
            ascending = false;
        }
        String s3 = p.getProperty(myPrefix + PROP_SORT_RANK);
        if (s3 != null) {
            sortRank = Integer.parseInt(s3);
            if (sortRank > 0) {
                comparator = getRowComparator(modelIndex, ascending);
            }
        }
        headerValue = p.getProperty(myPrefix + PROP_HEADER_VALUE);
    }

    /**
     * Method allowing to store customization values.
     * The stored values should be only those that the user has customized,
     * it does not make sense to store the values that were set using 
     * the initialization code because the initialization code can be run
     * in the same way after restart.
     */
    public void writeSettings(Properties p, int index, String propertyPrefix) {
        String myPrefix = propertyPrefix + PROP_PREFIX + Integer.toString(index) + "-";
        p.setProperty(myPrefix + PROP_MODEL_INDEX, Integer.toString(modelIndex));
        p.setProperty(myPrefix + PROP_WIDTH, Integer.toString(width));
        p.setProperty(myPrefix + PROP_PREFERRED_WIDTH, Integer.toString(getPreferredWidth()));
        p.setProperty(myPrefix + PROP_SORT_RANK, Integer.toString(sortRank));
        p.setProperty(myPrefix + PROP_ASCENDING, ascending ? "true" : "false");
        if (headerValue != null) {
            p.setProperty(myPrefix + PROP_HEADER_VALUE, headerValue.toString());
        }
    }

    /*
     * Implementing interface Comparable.
     */
    public int compareTo(ETableColumn obj) {
        if (modelIndex < obj.modelIndex) {
            return -1;
        }
        if (modelIndex > obj.modelIndex) {
            return 1;
        }
        return 0;
    }

    /**
     * Allow subclasses to supply special row comparator object.
     */
    protected Comparator<ETable.RowMapping> getRowComparator(int column, boolean ascending) {
        if (ascending) {
            return new RowComparator(column);
        } else {
            return new FlippingComparator(new RowComparator(column));
        }
    }

    /** Method allowing to set custom comparator for sorting the rows belonging to
     * same parent in tree-like part of table. The comparator operates on the types
     * of the given column, e.g. the class of the given column in table-like part
     * or node in tree-line part of table.
     *
     * @param c comparator or null for using the default comparator
     * @since 1.3
     */
    public void setNestedComparator (Comparator c) {
        nestedComparator = c;
    }

    /** Returns comparator for sorting the rows belonging to
     * same parent in tree-like part of table.
     *
     * @return comparator or null if no nested comparator was set and the default comparator will be used
     * @since 1.3
     */
    public Comparator getNestedComparator () {
        return nestedComparator;
    }
    
    /**
     * Overriden to return our special header renderer.
     * @see javax.swing.table.TableColumn#createDefaultHeaderRenderer()
     */
    @Override
    protected TableCellRenderer createDefaultHeaderRenderer() {
        if (myHeaderRenderer == null) {
            myHeaderRenderer = new ETableHeaderRenderer();
        }
        return myHeaderRenderer;
    }

    /**
     * An icon pointing up. It is used if the LAF does not supply
     * special icon.
     */
    private static class SortUpIcon implements Icon {
        
        public SortUpIcon() {
        }
        
        public int getIconWidth() {
            return 8;
        }
        
        public int getIconHeight() {
            return 8;
        }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.BLACK);
            g.drawLine(x    , y + 2, x + 8, y + 2);
            g.drawLine(x    , y + 2, x + 4, y + 6);
            g.drawLine(x + 8, y + 2, x + 4, y + 6);
        }
    }

    /**
     * An icon pointing down. It is used if the LAF does not supply
     * special icon.
     */
    private static class SortDownIcon implements Icon {
        
        public SortDownIcon() {
        }
        
        public int getIconWidth() {
            return 8;
        }
        
        public int getIconHeight() {
            return 8;
        }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.BLACK);
            g.drawLine(x    , y + 6, x + 8, y + 6);
            g.drawLine(x    , y + 6, x + 4, y + 2);
            g.drawLine(x + 8, y + 6, x + 4, y + 2);
        }
    }
    
    /**
     * Comparator reversing the order of the sorted objects (with
     * respect to the original comparator.
     */
    static class FlippingComparator implements Comparator<ETable.RowMapping> {
        private Comparator<ETable.RowMapping> origComparator;
        public FlippingComparator(Comparator<ETable.RowMapping> orig) {
            origComparator = orig;
        }

        public int compare(ETable.RowMapping o1, ETable.RowMapping o2) {
            return -origComparator.compare(o1, o2);
        }
        
        public Comparator<ETable.RowMapping> getOriginalComparator() {
            return origComparator;
        }
    }
    
    /**
     * Special renderer painting sorting icons and also special icon
     * for the QuickFilter columns.
     */
    private class ETableHeaderRenderer extends DefaultTableCellRenderer implements UIResource {

        private TableCellRenderer headerRenderer = new JTable().getTableHeader().getDefaultRenderer();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component res = headerRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if( res instanceof JLabel ) {
                JLabel label = (JLabel)res;
            String valueString = "";
            if (value != null) {
                valueString = value.toString();
            }
            if (table instanceof ETable) {
                ETable et = (ETable)table;
                valueString = et.getColumnDisplayName(valueString);
            }
            Icon sortIcon = null;

            List<TableColumn> sortedColumns = ((ETableColumnModel) table.getColumnModel ()).getSortedColumns ();

            if (sortRank != 0) {
                if (sortedColumns.size () > 1) {
                    label.setText((value == null) ?
                        Integer.toString(sortRank) :
                        sortRank+" "+valueString);
                }
                // don't use deriveFont() - see #49973 for details
                label.setFont (new Font (getFont ().getName (), Font.BOLD, getFont ().getSize ()));

                if (ascending) {
                    sortIcon = UIManager.getIcon("ETableHeader.ascendingIcon");
                    if (sortIcon == null) {
                        sortIcon = new SortUpIcon();
                    }
                } else {
                    sortIcon = UIManager.getIcon("ETableHeader.descendingIcon");
                    if (sortIcon == null) {
                        sortIcon = new SortDownIcon();
                    }
                }
            } else { // sortRank == 0
                label.setText(valueString);
            }
            if (sortIcon == null) {
                if (customIcon == null) {
                    Icon dummy = new Icon() {
                        public void paintIcon(Component c, Graphics g, int x, int y) {
                        }
                        public int getIconWidth() {
                            return 0;
                        }
                        public int getIconHeight() {
                            return 0;
                        }
                    };
                    label.setIcon(dummy);
                } else {
                    label.setIcon(customIcon);
                }
            } else {
                if (customIcon == null) {
                    label.setIcon(sortIcon);
                } else {
                    label.setIcon(mergeIcons(customIcon, sortIcon, 16, 0, this));
                }
            }
            }
            return res;
        }
    }
    
    /**
     * Comparator used for sorting the rows according to value in
     * a given column. Operates on the RowMapping objects.
     */
    public class RowComparator implements Comparator<ETable.RowMapping> {
        protected int column;
        public RowComparator(int column) {
            this.column = column;
        }
        @SuppressWarnings("unchecked")
        public int compare(ETable.RowMapping rm1, ETable.RowMapping rm2) {
            Object obj1 = rm1.getModelObject(column);
            Object obj2 = rm2.getModelObject(column);
            obj1 = table.transformValue(obj1);
            obj2 = table.transformValue(obj2);
            if (obj1 == null && obj2 == null) {
                return 0;
            }
            if (obj1 == null) {
                return -1;
            }
            if (obj2 == null) {
                return 1;
            }
            
            // check nested comparator
            if (getNestedComparator () != null) {
                return getNestedComparator ().compare (obj1, obj2);
            } else if ((obj1 instanceof Comparable) && (obj1.getClass().isAssignableFrom(obj2.getClass()))){
                Comparable c1 = (Comparable) obj1;
                return c1.compareTo(obj2);
            }
            return obj1.toString().compareTo(obj2.toString());
        }
    }
    
    /**
     * Utility method merging 2 icons.
     */
    private static final Icon mergeIcons(Icon icon1, Icon icon2, int x, int y, Component c) {
        int w = 0, h = 0;
        if (icon1 != null) {
            w = icon1.getIconWidth();
            h = icon1.getIconHeight();
        }
        if (icon2 != null) {
            w = icon2.getIconWidth()  + x > w ? icon2.getIconWidth()   + x : w;
            h = icon2.getIconHeight() + y > h ? icon2.getIconHeight()  + y : h;
        }
        if (w < 1) w = 16;
        if (h < 1) h = 16;
        
        java.awt.image.ColorModel model = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment ().
                                          getDefaultScreenDevice ().getDefaultConfiguration ().
                                          getColorModel (java.awt.Transparency.BITMASK);
        java.awt.image.BufferedImage buffImage = new java.awt.image.BufferedImage (model,
             model.createCompatibleWritableRaster (w, h), model.isAlphaPremultiplied (), null);
        
        java.awt.Graphics g = buffImage.createGraphics ();
        if (icon1 != null) {
            icon1.paintIcon(c, g, 0, 0);
        }
        if (icon2 != null) {
            icon2.paintIcon(c, g, x, y);
        }
        g.dispose();
        
        return new ImageIcon(buffImage);
    }

}

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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import org.netbeans.swing.etable.ETable.RowMapping;

/**
 *
 * @author David Strupl
 */
public class ETableColumnModel extends DefaultTableColumnModel {

    /** Used as a key or part of a key by the persistence mechanism. */
    private static final String NUMBER_OF_COLUMNS = "ColumnsNumber";
    
    /** Used as a key or part of a key by the persistence mechanism. */
    private static final String NUMBER_OF_HIDDEN_COLUMNS = "HiddenColumnsNumber";
    
    /** Used as a key or part of a key by the persistence mechanism. */
    private static final String PROP_HIDDEN_PREFIX = "Hidden";
    
    /**
     * List<ETableColumn>: holds list of sorted columns in this model.
     * If the list is empty if no sorting is applied.
     */
    protected transient List<TableColumn> sortedColumns = new ArrayList<TableColumn>();
    
    /**
     * List<ETableColumn>: holds list of columns that were hidden by the
     * user. The columns contained here are not contained in the inherited
     * tableColumns list.
     */
    protected List<TableColumn> hiddenColumns = new ArrayList<TableColumn>();
    
    /**
     * Allows the user to customize the list of the visible columns using
     * a hierarchy in a special TableColumnSelector.
     */
    private TableColumnSelector.TreeNode columnHierarchyRoot;
    
    /** Creates a new instance of ETableColumnModel */
    public ETableColumnModel() {
        super();
    }
    
    /**
     * Method allowing to read stored values.
     * The stored values should be only those that the user has customized,
     * it does not make sense to store the values that were set using 
     * the initialization code because the initialization code can be run
     * in the same way after restart.
     */
    public void readSettings(Properties p, String propertyPrefix, ETable table) {
        tableColumns = new Vector<TableColumn>();
        sortedColumns = new ArrayList<TableColumn>();
        String s = p.getProperty(propertyPrefix + NUMBER_OF_COLUMNS);
        int numColumns = Integer.parseInt(s);
        for (int i = 0; i < numColumns; i++) {
            ETableColumn etc = (ETableColumn)table.createColumn(i);
            etc.readSettings(p, i, propertyPrefix);
            addColumn(etc);
            if (etc.getComparator() != null) {
                int j = 0;
                for ( ; j < sortedColumns.size(); j++) {
                    ETableColumn setc = (ETableColumn)sortedColumns.get(j);
                    if (setc.getSortRank() > etc.getSortRank()) {
                        break;
                    }
                }
                sortedColumns.add(j, etc);
            }
        }
        hiddenColumns = new ArrayList<TableColumn>();
        String sh = p.getProperty(propertyPrefix + NUMBER_OF_HIDDEN_COLUMNS);
        int numHiddenColumns = Integer.parseInt(sh);
        for (int i = 0; i < numHiddenColumns; i++) {
            ETableColumn etc = new ETableColumn(table);
            etc.readSettings(p, i, propertyPrefix + PROP_HIDDEN_PREFIX);
            hiddenColumns.add(etc);
        }
    }

    /**
     * Method allowing to store customization values.
     * The stored values should be only those that the user has customized,
     * it does not make sense to store the values that were set using 
     * the initialization code because the initialization code can be run
     * in the same way after restart.
     */
    public void writeSettings(Properties p, String propertyPrefix) {
        int i = 0;
        int numColumns = tableColumns.size();
        p.setProperty(propertyPrefix + NUMBER_OF_COLUMNS, Integer.toString(numColumns));
        for (Iterator it = tableColumns.iterator(); it.hasNext(); ) {
            Object obj = it.next();
            if (obj instanceof ETableColumn) {
                ETableColumn etc = (ETableColumn) obj;
                etc.writeSettings(p, i++, propertyPrefix);
            }
        }
        i = 0;
        int numHiddenColumns = hiddenColumns.size();
        p.setProperty(propertyPrefix + NUMBER_OF_HIDDEN_COLUMNS, Integer.toString(numHiddenColumns));
        for (Iterator it = hiddenColumns.iterator(); it.hasNext(); ) {
            Object obj = it.next();
            if (obj instanceof ETableColumn) {
                ETableColumn etc = (ETableColumn) obj;
                etc.writeSettings(p, i++, propertyPrefix + PROP_HIDDEN_PREFIX);
            }
        }
    }

    /**
     * @return a comparator for sorting the rows of the table. The comparator
     * operates over ETable.RowMappings objects.
     */
    public Comparator<RowMapping> getComparator() {
        if (sortedColumns.isEmpty()) {
            return new ETable.OriginalRowComparator();
        }
        return new CompoundComparator();
    }

     /** This method marks this column as sorted. Value 0 of the parameter rank
     * means that this column is not sorted.
      * @param etc column in ETable column model
      * @param ascending true means ascending
      * @param newRank value 1 means that this is the most important sorted
     *        column, number 2 means second etc.
     * @since 1.3
     */
    @SuppressWarnings("deprecation")
    public void setColumnSorted(ETableColumn etc, boolean ascending, int newRank) {
        if (! etc.isSortingAllowed()) {
            return;
        }
        
        // TODO: check the implementation!
        
         boolean wasSorted = sortedColumns.contains(etc);
         if (wasSorted) {
             etc.setAscending(ascending);
             etc.setSortRank(newRank);
             sortedColumns.remove(etc);
         } else {
            etc.setSorted(newRank, ascending);
         }
         sortedColumns.add(newRank-1, etc);
    }
    
    /**
     *
     */
    @SuppressWarnings("deprecation")
    void toggleSortedColumn(ETableColumn etc, boolean cleanAll) {
        if (! etc.isSortingAllowed()) {
            return;
        }
        boolean wasSorted = sortedColumns.contains(etc);
        if (cleanAll) {
            clearSortedColumns(etc);
        }
        if (wasSorted) {
            if (etc.isAscending()) {
                etc.setAscending(false);
            } else {
                sortedColumns.remove(etc);
                etc.setSorted(0, false);
            }
            updateRanks();
        } else {
            etc.setSorted(sortedColumns.size()+1, true);
            sortedColumns.add(etc);
        }
    }
    
    /** 
     * Makes the given column hidden or visible according to the parameter
     * hidden.
     */
    public void setColumnHidden(TableColumn column, boolean hidden) {
        if (hidden) {
            if (! hiddenColumns.contains(column)) {
                if (tableColumns.contains(column)) {
                    removeColumn(column);
                    hiddenColumns.add(column);
                }
            }
        } else {
            if (! tableColumns.contains(column)) {
                if (hiddenColumns.contains(column)) {
                    hiddenColumns.remove(column);
                    addColumn(column);
                }
            }
        }
    }
    
    public boolean isColumnHidden(TableColumn tc) {
        return hiddenColumns.contains(tc);
    }
    
    /**
     * Makes the whole table unsorted.
     */
    @SuppressWarnings("deprecation")
    public void clearSortedColumns() {
        for (Iterator it = sortedColumns.iterator(); it.hasNext(); ) {
            Object o = it.next();
            if (o instanceof ETableColumn) {
                ETableColumn etc = (ETableColumn)o;
                etc.setSorted(0, false);
            }
        }
        sortedColumns = new ArrayList<TableColumn>();
    }
    
    /**
     * Makes the whole table unsorted except for one column.
     */
    @SuppressWarnings("deprecation")
    void clearSortedColumns(TableColumn notThisOne) {
        boolean wasSorted = sortedColumns.contains(notThisOne);
        for (Iterator it = sortedColumns.iterator(); it.hasNext(); ) {
            Object o = it.next();
            if ((o instanceof ETableColumn) && (o != notThisOne)) {
                ETableColumn etc = (ETableColumn)o;
                etc.setSorted(0, false);
            }
        }
        sortedColumns = new ArrayList<TableColumn>();
        if (wasSorted) {
            sortedColumns.add(notThisOne);
        }
    }
    
    /**
     * Reasigns sorting ranks to ETableColumns contained in sortedColumns list.
     */
    private void updateRanks() {
        int i = 1;
        for (Iterator it = sortedColumns.iterator(); it.hasNext(); i++) {
            Object o = it.next();
            if (o instanceof ETableColumn) {
                ETableColumn etc = (ETableColumn)o;
                if (etc.isSorted()) {
                    etc.setSortRank(i);
                }
            }
        }
    }

    List<TableColumn> getSortedColumns () {
        return sortedColumns;
    }
    
    /**
     * Comparator that delegates to individual comparators supplied by
     * ETableColumns. It uses only the columns contained in the sortedColumns
     * list.
     */
    private class CompoundComparator implements Comparator<RowMapping> {
        private Comparator<RowMapping> original;
        public CompoundComparator() {
            original = new ETable.OriginalRowComparator();
        }
        public int compare(RowMapping o1, RowMapping o2) {
            for (Iterator it = sortedColumns.iterator(); it.hasNext(); ) {
                Object o = it.next();
                if (o instanceof ETableColumn) {
                    ETableColumn etc = (ETableColumn)o;
                    Comparator<RowMapping> c = etc.getComparator();
                    if (c != null) {
                        int res = c.compare(o1, o2);
                        if (res != 0) {
                            return res;
                        }
                    }
                }
            }
            return original.compare(o1, o2);
        }
    }

    /**
     * Allows the user to customize the list of the visible columns using
     * a hierarchy in a special TableColumnSelector.
     */
    public TableColumnSelector.TreeNode getColumnHierarchyRoot() {
        return columnHierarchyRoot;
    }

    /**
     * Allows the user to customize the list of the visible columns using
     * a hierarchy in a special TableColumnSelector.
     */
    public void setColumnHierarchyRoot(TableColumnSelector.TreeNode columnHierarchyRoot) {
        this.columnHierarchyRoot = columnHierarchyRoot;
    }
}

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
package org.netbeans.modules.mashup.db.ui.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * TableModel implementation that manages instances of classes which implement the
 * RowEntry interface (defined within as a static class).
 * 
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class RowEntryTableModel implements TableModel {

    private static transient final Logger mLogger = Logger.getLogger(RowEntryTableModel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /**
     * Defines contract for implementation classes that want to be rendered by a JTable
     * through the RowEntryTableModel.
     * 
     * @author Jonathan Giron
     * @version $Revision$
     */
    public static interface RowEntry {

        /**
         * method getValue is used to get a row entry value
         * 
         * @param column is the value to get.
         * @return Object which is the value of the row entry.
         */
        public Object getValue(int column);

        /**
         * method isEditable returns true if an object is editable
         * 
         * @param column is the column to be checked
         * @return boolean true if editable
         */
        public boolean isEditable(int column);

        /**
         * method setEditable is used to set a row object editable.
         * 
         * @param column is the column to be set.
         * @param newState is the state for the object.
         */
        public void setEditable(int column, boolean newState);

        /**
         * method setValue is used to set a row entry value
         * 
         * @param column is the value to set.
         * @param newValue is the new value to use.
         */
        public void setValue(int column, Object newValue);
    }

    /* Default column header prefix to use if columnHeaders is null. */
    private static final String DEF_HEADER = "Column ";

    /* Log4J category string */
    private static final String LOG_CATEGORY = RowEntryTableModel.class.getName();
    /** (Optional) Holds column header names */
    protected String[] columnHeaders;
    /** Holds default editability state for each column */
    protected boolean[] editable;
    /** rows is the list of rows in the table. */
    protected ArrayList rows;

    /* Set of registered TableModelListeners */
    private Set listeners;

    /**
     * Creates a new instance of RowEntryTableModel
     * 
     * @param defaultEditable array of booleans indicating default editability for each
     *        column
     */
    public RowEntryTableModel(boolean[] defaultEditable) {
        this();

        if (defaultEditable.length == 0) {
            throw new IllegalArgumentException("Must supply a non-empty boolean[] instance for defaultEditable."); // NOI18N
        }

        editable = defaultEditable;
    }

    /**
     * Creates a new instance of RowEntryTableModel
     * 
     * @param headerLabels array of Strings representing column header labels
     * @param defaultEditable array of booleans indicating default editability for each
     *        column
     */
    public RowEntryTableModel(String[] headerLabels, boolean[] defaultEditable) {
        this(defaultEditable);

        if ((headerLabels != null) && (headerLabels.length != editable.length)) {
            throw new IllegalArgumentException("When supplied, headerLabels must have same array size as defaultEditable.");
        }

        columnHeaders = headerLabels;
    }

    /** Creates a new default instance of RowEntryTableModel. */
    protected RowEntryTableModel() {
        listeners = new HashSet(1);
        rows = new ArrayList(10);
    }

    /**
     * Adds the given RowEntry to the end of the table.
     * 
     * @param newData RowEntry to add
     */
    public void addRowEntry(RowEntry newData) {
        int index = 0;
        synchronized (rows) {
            rows.add(newData);
            index = rows.size();
        }

        fireTableModelEvent(new TableModelEvent(this, index + 1, index + 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }

    /**
     * Adds a listener to the list that is notified each time a change to the data model
     * occurs.
     * 
     * @param l the TableModelListener
     */
    public void addTableModelListener(TableModelListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    /**
     * Clears model of all row entries.
     */
    public void clear() {
        synchronized (rows) {
            int victimCount = rows.size();
            if (victimCount != 0) {
                rows.clear();
                fireTableModelEvent(new TableModelEvent(this, 0, victimCount - 1, TableModelEvent.DELETE));
            }
        }
    }

    /**
     * Signals to all listeners that table data have changed, and that views should
     * refresh their renderings to reflect the new state of the model.
     */
    public void fireTableDataChanged() {
        fireTableModelEvent(new TableModelEvent(this));
    }

    /**
     * Returns the most specific superclass for all the cell values in the column. This is
     * used by the <code>JTable</code> to set up a default renderer and editor for the
     * column.
     * 
     * @param columnIndex the index of the column
     * @return the common ancestor class of the object values in the model.
     */
    public Class getColumnClass(int columnIndex) {
        Object o = getValueAt(0, columnIndex);
        return (o != null) ? o.getClass() : Object.class;
    }

    /**
     * Returns the number of columns in the model. A <code>JTable</code> uses this
     * method to determine how many columns it should create and display by default.
     * 
     * @return the number of columns in the model
     * @see #getRowCount
     */
    public int getColumnCount() {
        return editable.length;
    }

    /**
     * Returns the name of the column at <code>columnIndex</code>. This is used to
     * initialize the table's column header name. Note: this name does not need to be
     * unique; two columns in a table can have the same name.
     * 
     * @param columnIndex the index of the column
     * @return the name of the column
     */
    public String getColumnName(int columnIndex) {
        return (columnHeaders != null) ? columnHeaders[columnIndex] : (DEF_HEADER + (columnIndex + 1));
    }

    /**
     * Returns the number of rows in the model. A <code>JTable</code> uses this method
     * to determine how many rows it should display. This method should be quick, as it is
     * called frequently during rendering.
     * 
     * @return the number of rows in the model
     * @see #getColumnCount
     */
    public int getRowCount() {
        return rows.size();
    }

    /**
     * method getRowEntries returns a list of row entries.
     * 
     * @return List of entries
     */
    public synchronized List getRowEntries() {
        return (ArrayList) rows.clone();
    }

    /**
     * Gets the RowEntries at the given row indices.
     * 
     * @param indices array of indices indicating RowEntry items to get.
     * @return List of deleted RowEntry items
     */
    public synchronized List getRowEntries(int[] indices) {
        ArrayList group = new ArrayList(indices.length);

        for (int i = 0; i < indices.length; i++) {
            Object o = rows.get(indices[i]);
            if (o instanceof RowEntry) {
                group.add(o);
            }
        }

        return group;
    }

    /**
     * Gets the RowEntry at the given row index.
     * 
     * @param rowIndex location of RowEntry to retrieve
     * @return RowEntry at rowIndex
     */
    public RowEntry getRowEntry(int rowIndex) {
        return (RowEntry) rows.get(rowIndex);
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     * 
     * @param rowIndex the row whose value is to be queried
     * @param columnIndex the column whose value is to be queried
     * @return the value Object at the specified cell
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = null;
        Object rowData = rows.get(rowIndex);

        if (rowData instanceof RowEntry) {
            value = ((RowEntry) rowData).getValue(columnIndex);
        }

        return value;
    }

    /**
     * Returns true if the cell at <code>rowIndex</code> and <code>columnIndex</code>
     * is editable. Otherwise, <code>setValueAt</code> on the cell will not change the
     * value of that cell. NOTE: Editable state uses AND logic to combine the default
     * state of a column, as supplied in the constructor, with the per-row state. If a
     * column is defined as uneditable by default, the per-row state is ignored.
     * 
     * @param rowIndex the row whose value to be queried
     * @param columnIndex the column whose value to be queried
     * @return true if the cell is editable
     * @see #setValueAt
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        boolean defaultState = editable[columnIndex];

        Object rowData = rows.get(rowIndex);
        if (rowData instanceof RowEntry) {
            // Editable state uses AND logic: if default state is uneditable,
            // it's always uneditable.
            defaultState &= ((RowEntry) rowData).isEditable(columnIndex);
        }

        return defaultState;
    }

    /**
     * Checks whether the given RowEntry exists in the model, using the given comparator
     * as the standard of comparison.
     * 
     * @param aRow RowEntry to compare against contents of model
     * @param comparator Comparator instance defining the meaning of duplication between
     *        two given RowEntries.
     * @return true if comparator determines that aRow matches an entry in this model,
     *         false otherwise.
     */
    public boolean isDuplicated(RowEntry aRow, Comparator comparator) {
        boolean result = false;

        if (rows != null && aRow != null && comparator != null) {
            // Since we're sorting (an irreversible operation), use a clone.
            ArrayList rowsCopy = (ArrayList) rows.clone();

            Collections.sort(rowsCopy, comparator); // must sort before searching
            result = (Collections.binarySearch(rowsCopy, aRow, comparator) == 0);
        }

        return result;
    }

    /**
     * Removes a listener from the list that is notified each time a change to the data
     * model occurs.
     * 
     * @param l the TableModelListener
     */
    public void removeTableModelListener(TableModelListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
     * Adds all RowEntry elements in the given List to the model, after clearing the
     * current contents of the list.
     * 
     * @param rowEntries List of elements to be added. Any elements which do not implement
     *        RowEntry will not be added.
     */
    public synchronized void setRowEntries(Collection rowEntries) {
        if (rowEntries == null) {
            throw new IllegalArgumentException("Must supply non-null reference for rowEntries.");
        }

        synchronized (rows) {
            rows.clear();

            Iterator iter = rowEntries.iterator();
            while (iter.hasNext()) {
                Object o = iter.next();
                if (o instanceof RowEntry) {
                    rows.add(o);
                }
            }

        //fireTableModelEvent(new TableModelEvent(this));
        }
    }

    /**
     * Sets the value in the cell at <code>columnIndex</code> and <code>rowIndex</code>
     * to <code>aValue</code>.
     * 
     * @param aValue the new value
     * @param rowIndex the row whose value is to be changed
     * @param columnIndex the column whose value is to be changed
     * @see #getValueAt
     * @see #isCellEditable
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        doSetValueAt(aValue, rowIndex, columnIndex);
        fireTableModelEvent(new TableModelEvent(this, rowIndex, rowIndex, columnIndex));
    }

    /**
     * Actually performs the low-level work of setting the value, without firing a table
     * model event. The calling method is responsible for creating the appropriate model
     * event and firing it off.
     * 
     * @param aValue the new value
     * @param rowIndex the row whose value is to be changed
     * @param columnIndex the column whose value is to be changed
     * @return true if set succeeded, false otherwise.
     */
    private boolean doSetValueAt(Object aValue, int rowIndex, int columnIndex) {
        boolean success = false;

        Object rowData = rows.get(rowIndex);
        if (rowData instanceof RowEntry) {
            try {
                ((RowEntry) rowData).setValue(columnIndex, aValue);
                success = true;
            } catch (Exception e) {
                mLogger.errorNoloc(mLoc.t("EDIT081: Error while setting value for row {0}, col{1}", rowIndex, columnIndex), e);
                success = false;
            }
            mLogger.infoNoloc(mLoc.t("EDIT082: After setValue at row{0}, col{1}:  value ={2}; rowData ={3}", rowIndex, columnIndex, aValue, rowData));
        }

        return success;
    }

    /* Fires given TableModelEvent to all registered listeners. */
    private void fireTableModelEvent(TableModelEvent ev) {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }

        while (it.hasNext()) {
            ((TableModelListener) it.next()).tableChanged(ev);
        }
    }
}


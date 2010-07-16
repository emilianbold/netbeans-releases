/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.dm.virtual.db.ui.wizard;

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
import java.util.logging.Logger;
import java.util.logging.Level;
import org.openide.util.NbBundle;

/**
 * TableModel implementation that manages instances of classes which implement the
 * RowEntry interface (defined within as a static class).
 * 
 * @author Ahimanikya Satapathy
 */
public class RowEntryTableModel implements TableModel {

    private static transient final Logger mLogger = Logger.getLogger(RowEntryTableModel.class.getName());
    public static interface RowEntry {

        public Object getValue(int column);

        public boolean isEditable(int column);

        public void setEditable(int column, boolean newState);

        public void setValue(int column, Object newValue);
    }

    private static final String DEF_HEADER = "Column ";

    protected String[] columnHeaders;
    protected boolean[] editable;
    protected ArrayList rows;

    private Set listeners;

    public RowEntryTableModel(boolean[] defaultEditable) {
        this();

        if (defaultEditable.length == 0) {
            throw new IllegalArgumentException(NbBundle.getMessage(RowEntryTableModel.class, "MSG_EmptyInstance")); // NOI18N
        }

        editable = defaultEditable;
    }

    public RowEntryTableModel(String[] headerLabels, boolean[] defaultEditable) {
        this(defaultEditable);

        if ((headerLabels != null) && (headerLabels.length != editable.length)) {
            throw new IllegalArgumentException(NbBundle.getMessage(RowEntryTableModel.class, "MSG_headerLabels"));
        }

        columnHeaders = headerLabels;
    }

    protected RowEntryTableModel() {
        listeners = new HashSet(1);
        rows = new ArrayList(10);
    }

    public void addRowEntry(RowEntry newData) {
        int index = 0;
        synchronized (rows) {
            rows.add(newData);
            index = rows.size();
        }

        fireTableModelEvent(new TableModelEvent(this, index + 1, index + 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }

    public void addTableModelListener(TableModelListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void clear() {
        synchronized (rows) {
            int victimCount = rows.size();
            if (victimCount != 0) {
                rows.clear();
                fireTableModelEvent(new TableModelEvent(this, 0, victimCount - 1, TableModelEvent.DELETE));
            }
        }
    }

    public void fireTableDataChanged() {
        fireTableModelEvent(new TableModelEvent(this));
    }

    public Class getColumnClass(int columnIndex) {
        Object o = getValueAt(0, columnIndex);
        return (o != null) ? o.getClass() : Object.class;
    }

    public int getColumnCount() {
        return editable.length;
    }

    public String getColumnName(int columnIndex) {
        return (columnHeaders != null) ? columnHeaders[columnIndex] : (DEF_HEADER + (columnIndex + 1));
    }

    public int getRowCount() {
        return rows.size();
    }

    public synchronized List getRowEntries() {
        return (ArrayList) rows.clone();
    }

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

    public RowEntry getRowEntry(int rowIndex) {
        return (RowEntry) rows.get(rowIndex);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = null;
        Object rowData = rows.get(rowIndex);

        if (rowData instanceof RowEntry) {
            value = ((RowEntry) rowData).getValue(columnIndex);
        }

        return value;
    }

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

    public void removeTableModelListener(TableModelListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public synchronized void setRowEntries(Collection rowEntries) {
        if (rowEntries == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(RowEntryTableModel.class, "MSG_NullRef_rowEntries"));
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

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        doSetValueAt(aValue, rowIndex, columnIndex);
        fireTableModelEvent(new TableModelEvent(this, rowIndex, rowIndex, columnIndex));
    }

    private boolean doSetValueAt(Object aValue, int rowIndex, int columnIndex) {
        boolean success = false;

        Object rowData = rows.get(rowIndex);
        if (rowData instanceof RowEntry) {
            try {
                ((RowEntry) rowData).setValue(columnIndex, aValue);
                success = true;
            } catch (Exception e) {
                mLogger.log(Level.SEVERE,NbBundle.getMessage(RowEntryTableModel.class, "LOG_ErrorSettingValue",rowIndex,columnIndex),e);
                success = false;
            }
            //mLogger.infoNoloc(mLoc.t("EDIT082: After setValue at row{0}, col{1}:  value ={2}; rowData ={3}", rowIndex, columnIndex, aValue, rowData));
            mLogger.log(Level.INFO,NbBundle.getMessage(RowEntryTableModel.class, "LOG_SetValue_Row", rowIndex, columnIndex, aValue)+rowData);
        }

        return success;
    }

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


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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous
 */
public abstract class TransferFileTableModel extends AbstractTableModel {

    private final List<TransferFileUnit> fileData = new ArrayList<TransferFileUnit>();
    private final List<TransferFileTableChangeListener> listeners = new CopyOnWriteArrayList<TransferFileTableChangeListener>();
    volatile String filter = ""; // NOI18N

    public static enum Type {
        UPLOAD,
        DOWNLOAD
    }

    public TransferFileTableModel() {
    }

    protected abstract Type getType();

    protected abstract String getTabTitle();

    protected abstract String getFirstColumnName();

    List<TransferFileUnit> getData() {
        return new ArrayList<TransferFileUnit>(fileData);
    }

    static Map<Integer, Boolean> captureState(List<TransferFileUnit> files) {
        Map<Integer, Boolean> retval = new HashMap<Integer, Boolean>(files.size());
        for (TransferFileUnit unit : files) {
            retval.put(unit.getId(), unit.isMarked());
        }
        return retval;
    }

    static void restoreState(List<TransferFileUnit> newFiles, Map<Integer, Boolean> capturedState, boolean isMarkedAsDefault) {
        for (TransferFileUnit unit : newFiles) {
            Boolean isChecked = capturedState.get(unit.getId());
            if (isChecked != null) {
                if (isChecked.booleanValue() && !unit.isMarked() && unit.canBeMarked()) {
                    unit.setMarked(true);
                } else if (!isChecked.booleanValue() && unit.isMarked() && unit.canBeMarked()) {
                    unit.setMarked(false);
                }
            } else if (isMarkedAsDefault && !unit.isMarked() && unit.canBeMarked()) {
                unit.setMarked(true);
            }
        }
    }

    public static boolean isMarkedAsDefault() {
        return true;
    }

    @Override
    public Object getValueAt(int row, int col) {
        Object res = null;

        TransferFileUnit u = getUnitAtRow(row);
        if (u != null) {
            switch(col) {
                case 0:
                    res = u.isMarked() ? Boolean.TRUE : Boolean.FALSE;
                    break;
                case 1:
                    res = u.getDisplayName();
                    break;
                default:
                    assert false : "Unknown column index: " + col;
            }
        }
        return res;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Class<?> getColumnClass(int c) {
        Class<?> res = null;
        switch(c) {
            case 0:
                res = Boolean.class;
                break;
            case 1:
                res = String.class;
                break;
        }
        return res;
    }

    public String getTabTooltipText() {
        return null;
    }

    public String getToolTipText(int row, int col) {
        String retval = null;
        if (col == 0) {
            retval = getTooltipForCheckBox(row);
        } else if (col == 1) {
            retval = (String) getValueAt(row, 1);
        }
        return retval;
    }

    private String getTooltipForCheckBox(int row) {
        return NbBundle.getMessage(TransferFileTableModel.class, "FileConfirmation_TooltipCheckBox", (String) getValueAt(row, 1));
    }

    public int getMinWidth(JTableHeader header, int col) {
        return header.getHeaderRect(col).width;
    }

    public int getPreferredWidth(JTableHeader header, int col) {
        if (col == 1) {
            return getMinWidth(header, col) * 4;
        }
        return getMinWidth(header, col);
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return getFirstColumnName();
            case 1:
                return NbBundle.getMessage(TransferFileTableModel.class, "FileConfirmationTableModel_Columns_RelativePath");
            default:
                assert false : "Unknown column index: " + column;
        }
        return super.getColumnName(column);
    }

    public final void sort(final int columnIndex, final boolean sortAscending) {
        if (columnIndex == 0) {
            Collections.sort(fileData, new MarkedComparator(sortAscending));
        } else if (columnIndex == 1) {
            Collections.sort(fileData, new PathComparator(sortAscending));
        } else {
            assert false : "Unknown column index: " + columnIndex;
        }
        fireTableDataChanged();
    }

    protected final void setData(List<TransferFileUnit> files) {
        assert files != null;
        fileData.clear();
        fileData.addAll(files);
        fireTableDataChanged();
    }

    public void setFilter(final String filter, final Runnable runAfterwards) {
        assert filter != null;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TransferFileTableModel.this.filter = filter.toLowerCase();
                fireFilterChange();
                if (runAfterwards != null) {
                    runAfterwards.run();
                }
            }
        });
    }

    public String getFilter() {
        assert filter != null;
        return filter;
    }

    public void addUpdateUnitListener(TransferFileTableChangeListener l) {
        listeners.add(l);
    }

    public void removeUpdateUnitListener(TransferFileTableChangeListener l) {
        listeners.remove(l);
    }

    void fireUpdataUnitChange() {
        for (TransferFileTableChangeListener l : listeners) {
            l.updateUnitsChanged();
        }
    }

    void fireFilterChange() {
        for (TransferFileTableChangeListener l : listeners) {
            l.filterChanged();
        }
    }

    public List<TransferFileUnit> getVisibleFileUnits() {
        List<TransferFileUnit> dataCopy = getData();
        String filterCopy = filter;
        List<TransferFileUnit> retval = new ArrayList<TransferFileUnit>(dataCopy.size());
        for (TransferFileUnit file : dataCopy) {
            if (file.isVisible(filterCopy)) {
                retval.add(file);
            }
        }
        return retval;
    }

    @Override
    public int getRowCount() {
        return getVisibleFileUnits().size();
    }

    public int getRawItemCount() {
        return fileData.size();
    }

    public List<TransferFileUnit> getAllUnits() {
        List<TransferFileUnit> allUnits = getData();
        List<TransferFileUnit> retval = new ArrayList<TransferFileUnit>(allUnits.size());
        for (TransferFileUnit fUnit : allUnits) {
            if (fUnit.getTransferFile().isFile()) {
                retval.add(fUnit);
            }
        }
        return retval;
    }

    public List<TransferFileUnit> getMarkedUnits() {
        List<TransferFileUnit> allUnits = getAllUnits();
        List<TransferFileUnit> retval = new ArrayList<TransferFileUnit>(allUnits.size());
        for (TransferFileUnit fUnit : allUnits) {
            assert fUnit.getTransferFile().isFile() : "Only files can be visible";
            if (fUnit.isMarked()) {
                retval.add(fUnit);
            }
        }
        return retval;
    }

    public TransferFileUnit getUnitAtRow(int row) {
        assert getVisibleFileUnits().size() > row : String.format("Unknown row index [%d, size %d]", row, getVisibleFileUnits().size());
        if (row < 0) {
            return null;
        }
        return getVisibleFileUnits().get(row);
    }

    public int getRowForUnit(TransferFileUnit unit) {
        int i = 0;
        for (TransferFileUnit u : getVisibleFileUnits()) {
            if (unit.equals(u)) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col == 0 && Boolean.class.equals(getColumnClass(col));
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        assert columnIndex == 0 : "Only first column is editable.";
        assert aValue instanceof Boolean : aValue + " must be instance of Boolean.";
        TransferFileUnit u = getUnitAtRow(rowIndex);
        if (u != null
                && (Boolean) aValue != u.isMarked()) {
            u.setMarked(!u.isMarked());
            fireUpdataUnitChange();
        }
    }

    private static class MarkedComparator implements Comparator<TransferFileUnit> {
        private final boolean sortAscending;

        public MarkedComparator(boolean sortAscending) {
            this.sortAscending = sortAscending;
        }

        @Override
        public int compare(TransferFileUnit o1, TransferFileUnit o2) {
            TransferFileUnit unit1 = sortAscending ? o1 : o2;
            TransferFileUnit unit2 = sortAscending ? o2 : o1;
            if (unit1.isMarked() && unit2.isMarked()) {
                return TransferFileUnit.compare(unit1, unit2);
            } else if (unit1.isMarked()) {
                return -1;
            } else if (unit2.isMarked()) {
                return 1;
            }
            return TransferFileUnit.compare(unit1, unit2);
        }
    }

    private static class PathComparator implements Comparator<TransferFileUnit> {
        private final boolean sortAscending;

        public PathComparator(boolean sortAscending) {
            this.sortAscending = sortAscending;
        }

        @Override
        public int compare(TransferFileUnit o1, TransferFileUnit o2) {
            TransferFileUnit unit1 = sortAscending ? o1 : o2;
            TransferFileUnit unit2 = sortAscending ? o2 : o1;
            return TransferFileUnit.compare(unit1, unit2);
        }
    }
}

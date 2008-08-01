/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.php.project.connections.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import org.openide.util.NbBundle;

/**
 *
 * @author Radek Matous
 */
public abstract class TransferFileTableModel extends AbstractTableModel {

    private List<TransferFileUnit> fileData = Collections.emptyList();
    private List<TransferFileTableChangeListener> listeners =
            new ArrayList<TransferFileTableChangeListener>();
    private String filter = "";//NOI18N
    private Comparator<TransferFileUnit> fileCmp;

    public static enum Type {

        UPLOAD, DOWNLOAD
    }

    /** Creates a new instance of CategoryTableModel */
    public TransferFileTableModel() {
    }

    List<TransferFileUnit> getData() {
        return fileData;
    }

    static Map<Integer, Boolean> captureState(List<TransferFileUnit> files) {
        Map<Integer, Boolean> retval = new HashMap<Integer, Boolean>();
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

    public abstract Type getType();

    public abstract Object getValueAt(int row, int col);

    @Override
    public abstract Class getColumnClass(int c);

    public abstract boolean isSortAllowed(Object columnIdentifier);

    protected abstract Comparator<TransferFileUnit> getComparator(final Object columnIdentifier, final boolean sortAscending);

    public String getTabTooltipText() {
        return null;
    }

    public abstract String getTabTitle();

    public final String getDecoratedTabTitle() {
        int count = getItemCount();
        int rawCount = getRawItemCount();
        String countInfo = (count == rawCount) ? String.valueOf(rawCount) :
            NbBundle.getMessage(TransferFileTableModel.class, "FileConfirmationUI_Tabs_CountFormat", count, rawCount);
        String newName = NbBundle.getMessage(TransferFileTableModel.class,
                "FileConfirmationUI_Tabs_NameFormat", getTabTitle(), countInfo);
        return (rawCount == 0) ? getTabTitle() : newName;
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

    public int getMinWidth(JTableHeader header, int col) {
        return header.getHeaderRect(col).width;
    }

    public abstract int getPreferredWidth(JTableHeader header, int col);

    protected Comparator<TransferFileUnit> getDefaultComparator() {
        return new Comparator<TransferFileUnit>() {

            public int compare(TransferFileUnit o1, TransferFileUnit o2) {
                return TransferFileUnit.compare(o1, o2);
            }
        };
    }

    public final void sort(Object columnIdentifier, boolean sortAscending) {
        if (columnIdentifier == null) {
            setFileUnitComparator(getDefaultComparator());
        } else {
            setFileUnitComparator(getComparator(columnIdentifier, sortAscending));
        }
        fireTableDataChanged();
    }

    private String getTooltipForCheckBox(int row) {
        String key0 = "FileConfirmation_TooltipCheckBox";
        return (key0 != null) ? NbBundle.getMessage(TransferFileTableModel.class, key0, (String) getValueAt(row, 1)) : null;
    }

    //private final void setData(List<FileUnit> files,  Comparator<FileUnit> unitCmp) {
    private final void setData(List<TransferFileUnit> files, Comparator<TransferFileUnit> unitCmp) {
        this.fileCmp = unitCmp != null ? unitCmp : getDefaultComparator();
        if (files != null) {
            this.fileData = Collections.emptyList();
            this.fileData = new ArrayList<TransferFileUnit>();
            this.fileData.addAll(files);
        } else {
            assert fileData != null;
        }
        if (unitCmp != null) {
            Collections.sort(fileData, unitCmp);
        }
        this.fireTableDataChanged();
    }

    public final void setFileUnitComparator(Comparator<TransferFileUnit> comparator) {
        setData(null, comparator);
    }

    public final void setData(List<TransferFileUnit> files) {
        setData(files, null);
    }

    public void setFilter(final String filter, final Runnable runAfterwards) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                synchronized (TransferFileTableModel.class) {
                    TransferFileTableModel.this.filter = filter.toLowerCase();
                }
                fireFilterChange();
                if (runAfterwards != null) {
                    runAfterwards.run();
                }
            }
        });
    }

    public String getFilter() {
        synchronized (TransferFileTableModel.class) {
            return this.filter == null ? "" : this.filter;
        }
    }

    public void addUpdateUnitListener(TransferFileTableChangeListener l) {
        listeners.add(l);
    }

    public void removeUpdateUnitListener(TransferFileTableChangeListener l) {
        listeners.remove(l);
    }

    void fireUpdataUnitChange() {
        assert listeners != null : "UpdateUnitListener found.";
        for (TransferFileTableChangeListener l : listeners) {
            l.updateUnitsChanged();
        }
    }

    void fireButtonsChange() {
        assert listeners != null : "UpdateUnitListener found.";
        for (TransferFileTableChangeListener l : listeners) {
            l.buttonsChanged();
        }
    }

    void fireFilterChange() {
        assert listeners != null : "UpdateUnitListener found.";
        for (TransferFileTableChangeListener l : listeners) {
            l.filterChanged();
        }
    }

    List<TransferFileUnit> getVisibleFileUnits() {
        return getVisibleFileUnits(getData(), getFilter(), true);
    }

    private List<TransferFileUnit> getVisibleFileUnits(List<TransferFileUnit> fileUnits, String filter, boolean filterAlsoStandardModules) {
        List<TransferFileUnit> retval = new ArrayList<TransferFileUnit>();
        for (TransferFileUnit file : fileUnits) {
            if (filterAlsoStandardModules) {
                if (file.isVisible(filter)) {
                    retval.add(file);
                }
            } else {
                if (file.isVisible(filter)) {
                    retval.add(file);
                }
            }
        }
        return retval;
    }

    public int getRowCount() {
        return getVisibleFileUnits().size();
    }

    public int getRawItemCount() {
        return fileData.size();
    }

    public int getItemCount() {
        return getVisibleFileUnits().size();
    }

    public List<TransferFileUnit> getMarkedFileUnits() {
        List<TransferFileUnit> retval = new ArrayList<TransferFileUnit>();
        List<TransferFileUnit> visibleFileUnits = getMarkedUnits();
        for (TransferFileUnit transferFileUnit : visibleFileUnits) {
            if (transferFileUnit.getTransferFile().isFile()) {
                retval.add(transferFileUnit);
            }
        }
        return retval;
    }

    public List<TransferFileUnit> getFilteredUnits() {
        List<TransferFileUnit> retval = new ArrayList<TransferFileUnit>();
        List<TransferFileUnit> allUnits = getData();
        for (TransferFileUnit fUnit : allUnits) {
            if (fUnit.isMarked()) {
                if (fUnit.getTransferFile().isFile()) {
                    retval.add(fUnit);
                }
            }
        }
        return retval;
    }

    public List<TransferFileUnit> getMarkedUnits() {
        List<TransferFileUnit> markedUnits = new ArrayList<TransferFileUnit>();
        List<TransferFileUnit> units = getData();

        for (TransferFileUnit u : units) {
            if (u.isMarked()) {
                markedUnits.add(u);
            }
        }
        return markedUnits;
    }

    public TransferFileUnit getUnitAtRow(int row) {
        return getVisibleFileUnits().size() <= row ? null : getVisibleFileUnits().get(row);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col == 0 && Boolean.class.equals(getColumnClass(col));
    }
}

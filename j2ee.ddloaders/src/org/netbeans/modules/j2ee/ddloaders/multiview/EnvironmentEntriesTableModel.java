/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EnvEntry;

/**
 * @author pfiala
 */
class EnvironmentEntriesTableModel extends InnerTableModel {
    private Ejb ejb;
    private static final String[] COLUMN_NAMES = new String[]{"Entry name", "Entry type", "Entry value", "Description"};
    private static final int[] COLUMN_WIDTHS = new int[]{100, 120, 100, 150};

    public EnvironmentEntriesTableModel(Ejb ejb) {
        super(COLUMN_NAMES, COLUMN_WIDTHS);
        this.ejb = ejb;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        EnvEntry envEntry = ejb.getEnvEntry(rowIndex);
        switch (columnIndex) {
            case 0:
                envEntry.setEnvEntryName((String) value);
                break;
            case 1:
                envEntry.setEnvEntryType((String) value);
                break;
            case 2:
                envEntry.setEnvEntryValue((String) value);
                break;
            case 3:
                envEntry.setDescription((String) value);
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public int getRowCount() {
        return ejb.getEnvEntry().length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        EnvEntry envEntry = ejb.getEnvEntry(rowIndex);
        switch (columnIndex) {
            case 0:
                return envEntry.getEnvEntryName();
            case 1:
                return envEntry.getEnvEntryType();
            case 2:
                return envEntry.getEnvEntryValue();
            case 3:
                return envEntry.getDefaultDescription();
        }
        return null;
    }

    public int addRow() {
        EnvEntry entry = ejb.newEnvEntry();
        ejb.addEnvEntry(entry);
        int row = getRowCount() - 1;
        fireTableRowsInserted(row, row);
        return row;
    }

    public void removeRow(int row) {
        fireTableStructureChanged(); // cancel editing
        ejb.removeEnvEntry(ejb.getEnvEntry(row));
        fireTableRowsDeleted(row, row);
    }
}

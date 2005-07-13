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



/**
 * @author pfiala
 */
public class EnvironmentEntriesTableModel extends InnerTableModel {

    private EjbHelper ejbHelper;
    private static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_EntryName"),
                                                  Utils.getBundleMessage("LBL_EntryType"),
                                                  Utils.getBundleMessage("LBL_EntryValue"),
                                                  Utils.getBundleMessage("LBL_Description")};
    private static final int[] COLUMN_WIDTHS = new int[]{100, 120, 100, 150};

    public EnvironmentEntriesTableModel(EjbHelper ejbHelper) {
        super(null, COLUMN_NAMES, COLUMN_WIDTHS);
        this.ejbHelper = ejbHelper;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        EjbHelper.EnvEntryHelper envEntryHelper = ejbHelper.getEnvEntryHelper(rowIndex);
        switch (columnIndex) {
            case 0:
                envEntryHelper.setEnvEntryName((String) value);
                break;
            case 1:
                envEntryHelper.setEnvEntryType((String) value);
                break;
            case 2:
                envEntryHelper.setEnvEntryValue((String) value);
                break;
            case 3:
                envEntryHelper.setDescription((String) value);
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public int getRowCount() {
        return ejbHelper.getEnvEntryCount();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        EjbHelper.EnvEntryHelper envEntryHelper = ejbHelper.getEnvEntryHelper(rowIndex);
        switch (columnIndex) {
            case 0:
                return envEntryHelper.getEnvEntryName();
            case 1:
                return envEntryHelper.getEnvEntryType();
            case 2:
                return envEntryHelper.getEnvEntryValue();
            case 3:
                return envEntryHelper.getDefaultDescription();
        }
        return null;
    }

    public int addRow() {
        ejbHelper.newEnvEntry();
        int row = getRowCount() - 1;
        return row;
    }

    public void removeRow(final int row) {
        ejbHelper.removeEnvEntry(row);
    }
}

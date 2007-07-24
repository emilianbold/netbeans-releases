/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.projects.jbi.ui.deployInfo;

import java.util.List;
import org.openide.ErrorManager;
import javax.swing.table.AbstractTableModel;


/**
 * JBI deployInfo component Table Model
 *
 * @author Tientien Li
 */
public class ComponentTableModel extends AbstractTableModel {
    /** Column labels used */
    private List<String> mColumnNames = null;

    /** Data for this model */
    private List<ComponentObject> mComponentObjects = null;

    /**
     * Constructor for the DependencyModel object
     *
     * @param data row data to populate table model with
     * @param columnNames column titles to populate table model with
     */
    public ComponentTableModel(List<ComponentObject> componentObjects, List<String> columnNames) {
        mComponentObjects = componentObjects;
        mColumnNames = columnNames;
    }

    /**
     * get column count
     *
     * @return int
     */
    public int getColumnCount() {
        return mColumnNames.size();
    }

    /**
     * get row count
     *
     * @return int
     */
    public int getRowCount() {
        return mComponentObjects.size();
    }

    /**
     * return name of column given column index
     *
     * @param col int
     *
     * @return String
     */
    public String getColumnName(int col) {
        return mColumnNames.get(col);
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /**
     * Get the valueAt attribute of the model object
     *
     * @param row row position
     * @param col column position
     *
     * @return The valueAt value
     */
    public Object getValueAt(int row, int col) {
        Object obj = null;

        try {
            if ((getRowCount() > 0) && ((row != -1) && (col != -1))) {
                ComponentObject tableEntry = mComponentObjects.get(row);

                if (tableEntry != null) {
                    // set the right data for each column
                    if (col == 0) {
                        obj = tableEntry.getType();
                    } else if (col == 1) {
                        obj = tableEntry.getName();
                    }
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        return obj;
    }

    /**
     * reset the data 
     *
     * @param componentObjects row data to set
     */
    public void setData(List<ComponentObject> componentObjects) {
        mComponentObjects = componentObjects;        
        fireTableDataChanged();
    }
}

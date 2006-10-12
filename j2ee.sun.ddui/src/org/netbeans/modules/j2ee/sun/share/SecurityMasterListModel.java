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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * SecurityMasterListModel.java
 *
 * Created on January 22, 2004, 12:39 PM
 */

package org.netbeans.modules.j2ee.sun.share;

import java.util.Arrays;
import java.util.Vector;
import java.util.ResourceBundle;
import java.text.MessageFormat;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Peter Williams
 */
public class SecurityMasterListModel extends AbstractTableModel {

    private static final ResourceBundle bundle = ResourceBundle.getBundle(
        "org.netbeans.modules.j2ee.sun.share.Bundle");	// NOI18N

    public static final String DUPLICATE_PRINCIPAL = bundle.getString("ERR_PrincipalExists");	// NOI18N
    public static final String DUPLICATE_GROUP = bundle.getString("ERR_GroupExists");	// NOI18N

    public static final String [] PRINCIPAL_COLUMN_NAMES = {
        bundle.getString("LBL_PrincipalColumnName"),
        bundle.getString("LBL_PrincipalClassColumnName")
    };

    public static final String [] GROUP_COLUMN_NAMES = {
        bundle.getString("LBL_GroupColumnName")
    };

    // !PW FIXME will likely have to replace this with LinkedHashMap to have
    //     decent performance adding and editing entries in large lists (> 25)
    private Vector masterList;

    private final String [] columnNames;
    private final int columnCount;

    private String duplicateErrorPattern;

    /** Creates a new instance of SecurityMasterListModel */
    private SecurityMasterListModel(String dupErrorPattern) {
        this(dupErrorPattern, GROUP_COLUMN_NAMES, 1);
    }

    private SecurityMasterListModel(String dupErrorPattern, String [] colNames, int columns) {
        assert colNames.length == columns; // # column names == # columns

        duplicateErrorPattern = dupErrorPattern;
        columnNames = colNames;
        columnCount = columns;
        masterList = new Vector();
    }

    private SecurityMasterListModel(String dupErrorPattern, String [] colNames, Object [] objects, int columns) {
        assert colNames.length == columns; // # column names == # columns

        duplicateErrorPattern = dupErrorPattern;
        columnNames = colNames;
        columnCount = columns;
        masterList = new Vector(Arrays.asList(objects));
    }

    /** Manipulation methods
     */
    /** add element
     */
    public void addElement(Object obj) {
        int index = masterList.size();
        masterList.add(obj);	
        fireTableRowsInserted(index, index);
    }

    /** remove element
     */
    public boolean removeElement(Object obj) {
        int index = masterList.indexOf(obj);
        boolean result = masterList.removeElement(obj);
        if(index >= 0) {
            fireTableRowsDeleted(index, index);
        }
        return result;
    }

    public void removeElementAt(int index) {
        if(index >= 0 || index < masterList.size())  {
            masterList.removeElementAt(index);
            fireTableRowsDeleted(index, index);
        }
    }

    public void removeElements(int[] indices) {
        // !PW FIXME this method has an unwritten requirement that the
        // list of indices passed in is ordered in ascending numerical order.
        if(indices.length > 0) {
            for(int i = indices.length-1; i >= 0; i--) {
                if(indices[i] >= 0 || indices[i] < masterList.size())  {
                    masterList.removeElementAt(indices[i]);
                }
            }
            fireTableRowsUpdated(indices[0], indices[indices.length-1]);
        }
    }

    /** replace element
     */
    public boolean replaceElement(Object oldObj, Object newObj) {
        boolean result = false;
        int index = masterList.indexOf(oldObj);
        if(index != -1) {
            masterList.setElementAt(newObj, index);
            fireTableRowsUpdated(index, index);
        }
        return result;
    }

    /**
     * Implementation of missing pieces of TableModel interface
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        if(rowIndex >= 0 && rowIndex < masterList.size() && columnIndex >= 0 && columnIndex < columnCount) {
            Object entry = masterList.get(rowIndex);

            if(entry instanceof String) {
                assert columnCount == 1 : "Invalid object for getValueAt() in SecurityMasterListModel.";
                result = entry;
            } else if(entry instanceof PrincipalNameMapping) {
                PrincipalNameMapping principalEntry = (PrincipalNameMapping) masterList.get(rowIndex);
                if(columnIndex == 0) {
                    result = principalEntry.getPrincipalName();
                } else {
                    result = principalEntry.getClassName();
                }
            } else {
                assert false : "Invalid object for getValueAt() in SecurityMasterListModel.";
            }
        } 
        return result;
    }

    public int getRowCount() {
        return masterList.size();
    }

    public int getColumnCount() {
        return columnCount;
    }

    public String getColumnName(int column) {
        if(column >= 0 && column < columnCount) {
            return columnNames[column];
        } 
        return null;
    }

    /** Other public access methods
     */
    public boolean contains(Object obj) {
        return masterList.contains(obj);
    }

    public String getDuplicateErrorMessage(String roleName) {
        Object [] args = { roleName };
        return MessageFormat.format(duplicateErrorPattern, args);		
    }

    public Object getRow(int rowIndex) {
        Object result = null;
        if(rowIndex >= 0 && rowIndex < masterList.size()) {
            result = masterList.get(rowIndex);
        } 
        return result;
    }

    /** Principal Name List
     */
    private static SecurityMasterListModel principalMaster = new SecurityMasterListModel(DUPLICATE_PRINCIPAL, PRINCIPAL_COLUMN_NAMES, 2);

    /** Retrieves the principal role name ListModel
     * @return The ListModel representing the global principal role list.
     */
    public static SecurityMasterListModel getPrincipalMasterModel() {
        return principalMaster;
    }

    /** Group Name List
     */
    private static SecurityMasterListModel groupMaster = new SecurityMasterListModel(DUPLICATE_GROUP);

    /** Retrieves the group role name ListModel
     * @return The ListModel representing the global group role list.
     */
    public static SecurityMasterListModel getGroupMasterModel() {
        return groupMaster;
    }
}

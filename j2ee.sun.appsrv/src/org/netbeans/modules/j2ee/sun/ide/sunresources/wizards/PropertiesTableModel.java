/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * PropertiesTableModel.java
 *
 * Created on October 1, 2003, 9:18 AM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.util.Vector;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author  nityad
 */
public class PropertiesTableModel extends javax.swing.table.AbstractTableModel {
    private static java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.Bundle"); // NOI18N
    private Vector data = null;
    /** Creates a new instance of PropertiesTableModel */
    public PropertiesTableModel(ResourceConfigData data) {
        this.data = data.getProperties();   //NOI18N
    }
    
    public int getColumnCount() {
        return 2;
    }
    
    public int getRowCount() {
        return data.size();
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        NameValuePair pair = (NameValuePair)data.elementAt(rowIndex);
        if (columnIndex == 0) 
            return pair.getParamName();
        else
            return pair.getParamValue();
    }
    
    public String getColumnName(int col) {
        if (0 == col) 
            return bundle.getString("COL_HEADER_NAME"); //NOI18N
        if (1 == col)
            return bundle.getString("COL_HEADER_VALUE"); //NOI18N
        throw new RuntimeException(bundle.getString("COL_HEADER_ERR_ERR_ERR")); //NOI18N
    }
    
    public boolean isCellEditable(int row, int col) {
       return true;
    }
    
    public void setValueAt(Object value, int row, int col) {
        if((row >=0) && (row < data.size())){
            NameValuePair property = (NameValuePair)data.elementAt(row);
            if (col == 0){
                if(! isNotUnique((String)value))
                    property.setParamName((String)value);
            }else if (col == 1)
                property.setParamValue((String)value);
        }    
        fireTableDataChanged();
    }

    //Fix for bug#5026041 - Table should not accept duplicate prop names.
    private boolean isNotUnique(String newVal){
        for(int i=0; i<data.size()-1; i++){
            NameValuePair pair = (NameValuePair)data.elementAt(i);
            if(pair.getParamName().equals(newVal)){
                NotifyDescriptor d = new NotifyDescriptor.Message(bundle.getString("Err_DuplicateValue"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return true;
            }    
        }
        return false;
    }
    
    public void setData(ResourceConfigData data) {
        this.data = data.getProperties();
        fireTableDataChanged();
    }
    
    private boolean changed = false;
}

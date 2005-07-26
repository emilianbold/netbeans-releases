/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.mbeanwizard.tablemodel;

import java.util.ArrayList;
import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.mbeanwizard.MBeanWrapperAttribute;
import org.netbeans.modules.jmx.MBeanAttribute;

/**
 *
 * @author an156382
 */
public class MBeanWrapperAttributeTableModel extends MBeanAttributeTableModel {
    
    //other idx inherited
    public static final int IDX_ATTR_SELECTION        = 0;
    
    private int firstEditableRow = 0;
    
    /**
     * Creates a new instance of MBeanWrapperAttributeTableModel 
     */
    public MBeanWrapperAttributeTableModel() {
        
        bundle = NbBundle.getBundle(MBeanAttributeTableModel.class);
        
        data = new ArrayList();
        
        columnNames = new String[5];
        
        String ss = bundle.getString("LBL_AttrSelection");// NOI18N
        String sn = bundle.getString("LBL_AttrName");// NOI18N
        String st = bundle.getString("LBL_AttrType");// NOI18N
        String sa = bundle.getString("LBL_AttrAccess");// NOI18N
        String sd = bundle.getString("LBL_AttrDescription");// NOI18N
        
        columnNames[IDX_ATTR_SELECTION]              = ss;
        columnNames[super.IDX_ATTR_NAME +1]          = sn;
        columnNames[super.IDX_ATTR_TYPE +1]          = st;
        columnNames[super.IDX_ATTR_ACCESS +1]        = sa;
        columnNames[super.IDX_ATTR_DESCRIPTION +1]   = sd;
        
        lastIndex=0; 
    }
    
    public void setFirstEditableRow(int row) {
        firstEditableRow = row;
    }
    
    public int getFirstEditableRow() {
        return firstEditableRow;
    }
    
    /**
     * Overridden method from superclass 
     */
    public Object getValueAt(int row, int col) {
        
    MBeanWrapperAttribute attr = (MBeanWrapperAttribute)data.get(row);
        switch(col) {
            case 0: return attr.isSelected();
            case 1: return attr.getName();
            case 2: return attr.getTypeName();
            case 3: return attr.getAccess();
            case 4: return attr.getDescription();
            default: System.out.println("Error getValueAt " +// NOI18N
                    "MBeanWrapperAttributeTableModel " + col);// NOI18N
            break;
        }
        return null;
    }
    
    /**
     * Overriden method from superclass
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        
        if (rowIndex < this.size()){
            MBeanWrapperAttribute attr = (MBeanWrapperAttribute)data.get(rowIndex);
            switch(columnIndex) {
                case 0: attr.setSelected((Boolean)aValue);
                break;
                case 1: attr.setName((String)aValue);
                break;
                case 2: attr.setTypeName((String)aValue);
                break;
                case 3: attr.setAccess((String)aValue);
                break;
                case 4: attr.setDescription((String)aValue);
                break;
                default: System.out.println("Error setValueAt " +// NOI18N
                        "MBeanWrapperAttributeTableModel " + columnIndex);// NOI18N
                break;
            }
        }
    }
    
    public MBeanWrapperAttribute getWrapperAttribute(int row) {
        return (MBeanWrapperAttribute) data.get(row);
    }
    
    public void addRow(MBeanAttribute attr) {
        MBeanWrapperAttribute mba = new MBeanWrapperAttribute(
                true,
                attr.getName(),
                attr.getTypeName(),
                attr.getAccess(),
                attr.getDescription());
        data.add(mba);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
    
    public void addRow() {
        MBeanWrapperAttribute mba = new MBeanWrapperAttribute(
                true,
                WizardConstants.ATTR_NAME_DEFVALUE + lastIndex,
                WizardConstants.STRING_OBJ_NAME,
                WizardConstants.ATTR_ACCESS_READ_WRITE,
                WizardConstants.ATTR_DESCR_DEFVALUE_PREFIX + lastIndex +
                WizardConstants.ATTR_DESCR_DEFVALUE_SUFFIX);
        lastIndex++;
        data.add(mba);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
}

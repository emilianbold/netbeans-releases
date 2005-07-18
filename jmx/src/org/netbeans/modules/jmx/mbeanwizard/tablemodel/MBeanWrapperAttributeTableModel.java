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
    
    /**
     * Creates a new instance of MBeanWrapperAttributeTableModel 
     */
    public MBeanWrapperAttributeTableModel() {
        
        bundle = NbBundle.getBundle(MBeanAttributeTableModel.class);
        
        data = new ArrayList();
        
        columnNames = new String[5];
        
        String ss = bundle.getString("LBL_AttrSelection");
        String sn = bundle.getString("LBL_AttrName");
        String st = bundle.getString("LBL_AttrType");
        String sa = bundle.getString("LBL_AttrAccess");
        String sd = bundle.getString("LBL_AttrDescription");
        
        columnNames[IDX_ATTR_SELECTION]              = ss;
        columnNames[super.IDX_ATTR_NAME +1]          = sn;
        columnNames[super.IDX_ATTR_TYPE +1]          = st;
        columnNames[super.IDX_ATTR_ACCESS +1]        = sa;
        columnNames[super.IDX_ATTR_DESCRIPTION +1]   = sd;
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
            default: System.out.println("Error getValueAt " +
                    "MBeanWrapperAttributeTableModel " + col);
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
                case 3: attr.setAccess((String)aValue);
                break;
                case 4: attr.setDescription((String)aValue);
                break;
                default: System.out.println("Error setValueAt " +
                        "MBeanWrapperAttributeTableModel " + columnIndex);
                break;
            }
        }
    }
    
    public MBeanWrapperAttribute getAttribute(int row) {
        return (MBeanWrapperAttribute) data.get(row);
    }
    
    //fake method for screenshots
    /**
     * Overriden method from superclass
     *//*
    public void addMyRows() {
        
        MBeanWrapperAttribute mba = new MBeanWrapperAttribute(
                true,
                "Count",
                "int",
                WizardConstants.ATTR_ACCESS_READ_WRITE,
                "");
        MBeanWrapperAttribute mba2 = new MBeanWrapperAttribute(
                true,
                "CloseOK",
                "boolean",
                WizardConstants.ATTR_ACCESS_READ_WRITE,
                "");
        data.add(mba);
        data.add(mba2);
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }*/
    
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
}

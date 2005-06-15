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
import org.netbeans.modules.jmx.mbeanwizard.mbeanstructure.MBeanAttribute;


/**
 * Class implementing the table model for the mbean attribute table
 *
 */
public class MBeanAttributeTableModel extends AbstractJMXTableModel {
    
    public static final int IDX_ATTR_NAME        = 0;
    public static final int IDX_ATTR_TYPE        = 1;
    public static final int IDX_ATTR_ACCESS      = 2;
    public static final int IDX_ATTR_DESCRIPTION = 3;
    
    /**
     * Constructor
     */
    public MBeanAttributeTableModel() {
        super();
        
        bundle = NbBundle.getBundle(MBeanAttributeTableModel.class);
        
        data = new ArrayList();
        
        columnNames = new String[4];
        
        String sn = bundle.getString("LBL_AttrName");
        
        String st = bundle.getString("LBL_AttrType");
        String sa = bundle.getString("LBL_AttrAccess");
        String sd = bundle.getString("LBL_AttrDescription");
        
        columnNames[IDX_ATTR_NAME]          = sn;
        columnNames[IDX_ATTR_TYPE]          = st;
        columnNames[IDX_ATTR_ACCESS]        = sa;
        columnNames[IDX_ATTR_DESCRIPTION]   = sd;
        
    }
    
    /**
     * Creates a new MBean Attribute object
     * @return MBeanAttribute the created Attribute with default values
     */
    public MBeanAttribute createNewAttribute() {
        
        return new MBeanAttribute(
                WizardConstants.ATTR_NAME_DEFVALUE + this.getRowCount(),
                WizardConstants.STRING_OBJ_NAME,
                WizardConstants.ATTR_ACCESS_READ_WRITE,
                WizardConstants.ATTR_DESCR_DEFVALUE_PREFIX + this.getRowCount() +
                WizardConstants.ATTR_DESCR_DEFVALUE_SUFFIX);
    }
    
    /**
     * Returns an attribute accordind to his index in the attribute list
     * @param index the index of the attribute
     * @return MBeanAttribute the attribute
     */
    public MBeanAttribute getAttribute(int index) {
        return (MBeanAttribute)data.get(index);
    }
    
    /**
     * Overridden method from superclass 
     */
    public Object getValueAt(int row, int col) {
        MBeanAttribute attr = (MBeanAttribute)data.get(row);
        switch(col) {
            case 0: return attr.getAttrName();
            case 1: return attr.getAttrType();
            case 2: return attr.getAttrAccess();
            case 3: return attr.getAttrDescription();
            default: System.out.println("Error getValueAt " +
                    "MBeanAttributeTableModel " + col);
            break;
        }
        return null;
    }
    
    /**
     * Overriden method from superclass
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < this.size()){
            MBeanAttribute attr = (MBeanAttribute)data.get(rowIndex);
            switch(columnIndex) {
                case 0: attr.setAttrName((String)aValue);
                break;
                case 1: attr.setAttrType((String)aValue);
                break;
                case 2: attr.setAttrAccess((String)aValue);
                break;
                case 3: attr.setAttrDescription((String)aValue);
                break;
                default: System.out.println("Error setValueAt " +
                        "MBeanAttributeTableModel " + columnIndex);
                break;
            }
        }
    }
    
    /**
     * Overriden method from superclass
     */
    public void addRow() {
        
        MBeanAttribute mba = createNewAttribute();
        data.add(mba);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
}

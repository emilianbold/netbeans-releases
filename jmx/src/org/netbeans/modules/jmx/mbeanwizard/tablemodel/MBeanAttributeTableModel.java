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
import org.netbeans.modules.jmx.MBeanAttribute;


/**
 * Class implementing the table model for the mbean attribute table
 *
 */
public class MBeanAttributeTableModel extends AbstractJMXTableModel {
    
    public static final int IDX_ATTR_NAME        = 0;
    public static final int IDX_ATTR_TYPE        = 1;
    public static final int IDX_ATTR_ACCESS      = 2;
    public static final int IDX_ATTR_DESCRIPTION = 3;
    
    private int lastIndex;
    
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
        
        lastIndex = 0;
    }
    
    /**
     * Creates a new MBean Attribute object
     * @return MBeanAttribute the created Attribute with default values
     */
    public MBeanAttribute createNewAttribute() {
        
        MBeanAttribute attribute = new MBeanAttribute(
                WizardConstants.ATTR_NAME_DEFVALUE + lastIndex,
                WizardConstants.STRING_OBJ_NAME,
                WizardConstants.ATTR_ACCESS_READ_WRITE,
                WizardConstants.ATTR_DESCR_DEFVALUE_PREFIX + lastIndex +
                WizardConstants.ATTR_DESCR_DEFVALUE_SUFFIX);
        lastIndex++;
        return attribute;
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
            case 0: return attr.getName();
            case 1: return attr.getTypeName();
            case 2: return attr.getAccess();
            case 3: return attr.getDescription();
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
                case 0: attr.setName((String)aValue);
                break;
                case 1: attr.setTypeName((String)aValue);
                break;
                case 2: attr.setAccess((String)aValue);
                break;
                case 3: attr.setDescription((String)aValue);
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

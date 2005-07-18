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

package org.netbeans.modules.jmx.actions.dialog;

import org.netbeans.modules.jmx.MBeanAttribute;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanAttributeTableModel;

/**
 *
 * @author tl156378
 */
public class AddMBeanAttributeTableModel extends MBeanAttributeTableModel {
    
    private int firstEditable = 0;
    
    /**
     * Method returning wheter the cell (r,c) is editable or not
     * @param r the row of the cell
     * @param c the column of the cell
     * @return boolean true if the cell is editable
     */
    public boolean isCellEditable(int r, int c) {
        if (r < firstEditable)
            return false;
        else 
            return super.isCellEditable(r,c);
    }

    public void setFirstEditable(int firstEditable) {
        this.firstEditable = firstEditable;
    }
    
    public int getFirstEditable() {
        return firstEditable;
    }
    
    public void addAttribute(MBeanAttribute attribute) {
        data.add(attribute);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
}

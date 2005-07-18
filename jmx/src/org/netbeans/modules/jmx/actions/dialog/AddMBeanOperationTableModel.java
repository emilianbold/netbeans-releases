/*
 * AddMBeanOperationTableModel.java
 *
 * Created on July 7, 2005, 5:47 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.jmx.actions.dialog;

import org.netbeans.modules.jmx.MBeanOperation;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanMethodTableModel;


/**
 *
 * @author tl156378
 */
public class AddMBeanOperationTableModel extends MBeanMethodTableModel {
    
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
    
    public void addOperation(MBeanOperation operation) {
        data.add(operation);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
}

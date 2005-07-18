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
import javax.swing.JTable;
import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.MBeanOperationException;


/**
 * Class implementing the table model for the mbean operation exception 
 * popup table
 * 
 */
public class OperationExceptionTableModel extends AbstractJMXTableModel{
    
    public static final int IDX_EXCEPTION_NAME           = 0;
    public static final int IDX_EXCEPTION_DESCR          = 1;
        
    /**
     * Constructor
     */
    public OperationExceptionTableModel ()
    {
        super();
        bundle = NbBundle.getBundle(OperationExceptionTableModel.class);
        data = new ArrayList();
        columnNames = new String[2];
        String sen = bundle.getString("LBL_ExceptionClass");// NOI18N
        String sed = bundle.getString("LBL_ExceptionDescription");// NOI18N
        columnNames[IDX_EXCEPTION_NAME] = sen;
        columnNames[IDX_EXCEPTION_DESCR] = sed;
    }

    /**
     * Instantiates a new exception; called when a line is added to the 
     * popup table
     * @return MBeanOperationException the created exception
     */
    public MBeanOperationException createNewException() {
        
        return new MBeanOperationException(
                WizardConstants.METH_EXCEP_CLASS_DEFVALUE,
                WizardConstants.METH_EXCEP_DESCR_DEFVALUE);
    }
    
    /**
     * Returns the operation exception at index index
     * @return MBeanOperationException the operation exception at index index
     */
    public MBeanOperationException getException(int index) {
        return (MBeanOperationException)data.get(index);
    } 
    
    /**
     * Sets the operation exception at index index to a new object
     * @param index the index
     * @param excep the Operation exception to set
     */
    public void setException(int index, MBeanOperationException excep) {
        if (index < data.size()) {
            data.set(index, excep);
        }
    }
    
    /**
     * Overriden method from superclass
     */
    public Object getValueAt(int row, int col) {
        MBeanOperationException excep = (MBeanOperationException)data.get(row);
        switch(col) {
            case 0: return excep.getExceptionClass();
            case 1: return excep.getExceptionDescription();
            default: System.out.println("Error getValueAt " +// NOI18N
                    "OperationExceptionTableModel " + col);// NOI18N
                break;
        }
        return null;
    }
    
    /**
     * Overriden method from superclass
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < this.size()){
            MBeanOperationException excep = 
                    (MBeanOperationException)data.get(rowIndex);
            switch(columnIndex) {
            case 0: excep.setExceptionClass((String)aValue);
                break;
            case 1: excep.setExceptionDescription((String)aValue);
                break;
            default: System.out.println("Error setValueAt " +// NOI18N
                    "OperationExceptionTableModel " + columnIndex);// NOI18N
                break;    
            }
        }
    }
    
    /**
     * Overriden method from superclass
     */
    public void addRow() {
        
        MBeanOperationException mboe = createNewException();
        data.add(mboe);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
}

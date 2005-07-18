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
import org.netbeans.modules.jmx.MBeanOperationParameter;
import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.WizardConstants;


/**
 * Class implementing the table model for the mbean operation parameter popup table
 * 
 */
public class OperationParameterTableModel extends AbstractJMXTableModel{
    
    public static final int IDX_OP_PARAM_NAME          = 0;
    public static final int IDX_OP_PARAM_TYPE          = 1;
    public static final int IDX_OP_PARAM_DESCRIPTION   = 2;
        
    /**
     * Constructor
     */
    public OperationParameterTableModel ()
    {
        super();
        bundle = NbBundle.getBundle(OperationParameterTableModel.class);
        data = new ArrayList();

        columnNames = new String[3];
        String sopn = bundle.getString("LBL_OperationParameterName");
        String sopt = bundle.getString("LBL_OperationParameterType");
        String sopd = bundle.getString("LBL_OperationParameterDescription");
        
        columnNames[IDX_OP_PARAM_NAME]        = sopn;
        columnNames[IDX_OP_PARAM_TYPE]        = sopt;        
        columnNames[IDX_OP_PARAM_DESCRIPTION] = sopd;
    }
    
    /**
     * Instantiates a new parameter; called when a line is added to the 
     * popup table
     * @return MBeanOperationParameter the created parameter
     */
    public MBeanOperationParameter createNewParameter() {
        
        return new MBeanOperationParameter(
                WizardConstants.METH_PARAM_NAME_DEFVALUE + this.getRowCount(),
                WizardConstants.STRING_OBJ_NAME,
                WizardConstants.METH_PARAM_DESCR_DEFVALUE_PREFIX + 
                this.getRowCount() +
                        WizardConstants.METH_PARAM_DESCR_DEFVALUE_SUFFIX);
    }
    
    /**
     * Returns the operation parameter at index index
     * @return MBeanOperationParameter the operation parameter at index index
     */
    public MBeanOperationParameter getParameter(int index) {
        return (MBeanOperationParameter)data.get(index);
    } 
    
    /**
     * Sets the operation parameter at index index to a new object
     * @param index the index
     * @param param the Operation parameter to set
     */
    public void setParameter(int index, MBeanOperationParameter param) {
        if (index < data.size()) {
            data.set(index, param);
        }
    }
    
    /**
     * Overriden method from superclass
     */
    public Object getValueAt(int row, int col) {
        MBeanOperationParameter param = (MBeanOperationParameter)data.get(row);
        switch(col) {
            case 0: return param.getParamName();
            case 1: return param.getParamType();
            case 2: return param.getParamDescription();
            default: System.out.println("Error getValueAt " +
                    "OperationParameterTableModel " + col);
                break;
        }
        return null;
    }
    
    /**
     * Overriden method from superclass
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < this.size()){
            MBeanOperationParameter param = 
                    (MBeanOperationParameter)data.get(rowIndex);
            switch(columnIndex) {
                case 0: param.setParamName((String)aValue);
                break;
                case 1: param.setParamType((String)aValue);
                break;
                case 2: param.setParamDescription((String)aValue);
                break;
                default: System.out.println("Error setValueAt " +
                        "OperationParameterTableModel " + columnIndex);
                break;    
            }
        }
    }
    
    /**
     * Overriden method from superclass
     */
    public void addRow() {
        
        MBeanOperationParameter mbop = createNewParameter();
        data.add(mbop);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
}

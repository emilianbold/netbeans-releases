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
import org.netbeans.modules.jmx.MBeanOperation;
import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.MBeanOperationException;
import org.netbeans.modules.jmx.MBeanOperationParameter;


/**
 * Class implementing the table model for the mbean operation table
 *
 */
public class MBeanOperationTableModel extends AbstractJMXTableModel {
    public static final int IDX_METH_NAME          = 0;
    public static final int IDX_METH_TYPE          = 1;
    public static final int IDX_METH_PARAM         = 2;
    public static final int IDX_METH_EXCEPTION     = 3;
    public static final int IDX_METH_DESCRIPTION   = 4;
    
    private int lastIndex;
    
    /**
     * Constructor
     */
    public MBeanOperationTableModel() {
        super();
        
        bundle = NbBundle.getBundle(MBeanOperationTableModel.class);
        
        data = new ArrayList();
        
        columnNames = new String[5];
        
        String sn = bundle.getString("LBL_MethodName");// NOI18N
        String st = bundle.getString("LBL_MethodReturnType");// NOI18N
        String sp = bundle.getString("LBL_MethodParamType");// NOI18N
        String se = bundle.getString("LBL_MethodExceptionType");// NOI18N
        String sd = bundle.getString("LBL_MethodDescription");// NOI18N
        
        columnNames[IDX_METH_NAME]          = sn;
        columnNames[IDX_METH_TYPE]          = st;
        columnNames[IDX_METH_PARAM]         = sp;
        columnNames[IDX_METH_EXCEPTION]     = se;
        columnNames[IDX_METH_DESCRIPTION]   = sd;
        
        lastIndex = 0;
    }
    
    /**
     * Instantiates a new operation; called when a line is added to the
     * table
     * @return MBeanOperation the created operation
     */
    public MBeanOperation createNewOperation() {
        MBeanOperation oper = new MBeanOperation(
                WizardConstants.METH_NAME_DEFVALUE + lastIndex,
                WizardConstants.VOID_RET_TYPE,
                null,
                null,
                WizardConstants.METH_DESCR_DEFVALUE_PREFIX + lastIndex +
                WizardConstants.METH_DESCR_DEFVALUE_SUFFIX);
        lastIndex++;
        return oper;
    }
    
    public void clear() {
        data.clear();
    }
    
    /**
     * Returns the operation at index index
     * @return MBeanOperation the operation at index index
     */
    public MBeanOperation getOperation(int index) {
        return (MBeanOperation)data.get(index);
    }
    
    /**
     * Overriden method from superclass
     */
    public Object getValueAt(int row, int col) {
        MBeanOperation oper = (MBeanOperation)data.get(row);
        switch(col) {
            case 0: return oper.getName();
            case 1: return oper.getReturnTypeName();
            case 2: return oper.getParametersList();
            case 3: return oper.getExceptionsList();
            case 4: return oper.getDescription();
            default: System.out.println("Error getValueAt " +// NOI18N
                    "MBeanMethodTableModel " + col);// NOI18N
            break;
        }
        return null;
    }
    
    /**
     * Overriden method from superclass
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < this.size()){
            MBeanOperation oper = (MBeanOperation)data.get(rowIndex);
            switch(columnIndex) {
                case 0: oper.setName((String)aValue);
                break;
                case 1: oper.setReturnTypeName((String)aValue);
                break;
                case 2: oper.setParametersList(
                        (ArrayList<MBeanOperationParameter>)aValue);
                break;
                case 3: oper.setExceptionsList(
                        (ArrayList<MBeanOperationException>)aValue);
                break;
                case 4: oper.setDescription((String)aValue);
                break;
                default: System.out.println("Error setValueAt " +// NOI18N
                        "MBeanMethodTableModel " + columnIndex);// NOI18N
                break;
            }
        }
    }
    
    /**
     * Overriden method from superclass
     */
    public void addRow() {
        
        MBeanOperation mbo = createNewOperation();
        data.add(mbo);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
    
    public void addRow(MBeanOperation op) {
        MBeanOperation mbo = new MBeanOperation(
                op.getName(),
                op.getReturnTypeName(),
                op.getParametersList(),
                op.getExceptionsList(),
                op.getDescription());
        data.add(mbo);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
}

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

package org.netbeans.modules.jmx.mbeanwizard.popup;
import javax.swing.JButton;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.OperationExceptionTableModel;
import javax.swing.JTextField;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanOperationTableModel;
import javax.swing.JPanel;
import org.netbeans.modules.jmx.mbeanwizard.table.WrapperOperationExceptionPopupTable;


/**
 * Class implementing the exception popup window
 * 
 */
public class WrapperOperationExceptionPopup extends OperationExceptionPopup {

    public WrapperOperationExceptionPopup(JPanel ancestorPanel, 
            MBeanOperationTableModel methodModel,
            JTextField textField, int editedRow) {
        
        super(ancestorPanel, methodModel, textField, editedRow);
    }
    
    protected JButton[] getUsedButtons() {
        return new JButton[] {
                closeJButton
        };
    }
    
    protected void initJTable() {
        
        popupTableModel = new OperationExceptionTableModel();
        popupTable = new WrapperOperationExceptionPopupTable(popupTableModel);
        popupTable.setName("ExcepPopupTable");// NOI18N
    }
    
}
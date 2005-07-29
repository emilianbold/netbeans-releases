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
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanOperationTableModel;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.OperationParameterTableModel;
import org.netbeans.modules.jmx.mbeanwizard.listener.AddTableRowListener;
import org.netbeans.modules.jmx.mbeanwizard.listener.RemTableRowListener;
import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.mbeanwizard.table.WrapperOperationParameterPopupTable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.MBeanOperationParameter;



/**
 * Class implementing the parameter popup window
 *
 */
public class WrapperOperationParameterPopup extends OperationParameterPopup {
    
    /**
     * Constructor
     * @param ancestorPanel the parent panel of the popup; here the wizard
     * @param textField the text field to fill with the popup information
     * @param result the intermediate structure which storespopup information
     * @param wiz the parent-window's wizard panel
     */
    public WrapperOperationParameterPopup(JPanel ancestorPanel, 
            MBeanOperationTableModel model,
            JTextField textField, int editedRow, FireEvent wiz) {
        
        super(ancestorPanel, model, textField, editedRow, wiz);
    }
    
    protected void initJTable() {
        
        popupTableModel = new OperationParameterTableModel();
        popupTable = new WrapperOperationParameterPopupTable(popupTableModel);
        popupTable.setName("ParamPopupTable");// NOI18N
    }
    
     protected JButton[] getUsedButtons() {
        return new JButton[] {
                closeJButton
        };
    }
}

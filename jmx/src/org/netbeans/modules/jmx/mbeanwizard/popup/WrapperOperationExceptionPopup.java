/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
    
    protected void setDimensions(String str) {
        setTitle(str);
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBounds(350,250,POPUP_WIDTH,POPUP_HEIGHT);
        setVisible(true);
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
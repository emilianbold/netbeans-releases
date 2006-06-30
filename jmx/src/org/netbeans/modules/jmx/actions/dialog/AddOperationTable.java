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


package org.netbeans.modules.jmx.actions.dialog;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.mbeanwizard.renderer.ComboBoxRenderer;
import org.netbeans.modules.jmx.mbeanwizard.renderer.TextFieldRenderer;
import org.netbeans.modules.jmx.mbeanwizard.table.OperationTable;
import org.netbeans.modules.jmx.mbeanwizard.renderer.OperationParameterPanelRenderer;
import org.netbeans.modules.jmx.mbeanwizard.renderer.OperationExceptionPanelRenderer;

/**
 * Class responsible for the operation table shown when you use Add Operations...
 * popup action in the contextual management menu.
 * @author tl156378
 */
public class AddOperationTable extends OperationTable {
    
    /**
     * Constructor
     * @param model the table model of this table
     * @param wiz the wizard panel
     * @param ancestorPanel <CODE>JPanel</CODE>
     */
    public AddOperationTable(JPanel ancestorPanel, AbstractTableModel model,
            FireEvent wiz) {
        super(ancestorPanel,model,wiz);
    }
        
   /**
     * Returns the cell renderer for the table according to the column
     * @param row the row to be considered
     * @param column the column to be considered
     * @return TableCellRenderer the cell renderer
     */
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        AddMBeanOperationTableModel addOpModel = 
                (AddMBeanOperationTableModel) this.getModel();
        int firstEditable = addOpModel.getFirstEditable();
        
        if(row < firstEditable) {
            switch (column) {
                case AddMBeanOperationTableModel.IDX_METH_NAME :
                    return new TextFieldRenderer(new JTextField(), true, false);
                case AddMBeanOperationTableModel.IDX_METH_TYPE :
                    return new TextFieldRenderer(new JTextField(), true, false);
                case AddMBeanOperationTableModel.IDX_METH_PARAM :
                    JTextField paramField = new JTextField();
                    paramField.setEditable(false);
                    paramField.setName("methParamTextField"); // NOI18N
                    JButton paramButton =
                            new JButton(WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                    paramButton.setMargin(new java.awt.Insets(2,2,2,2));
                    paramButton.setEnabled(false);
                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(paramField, BorderLayout.CENTER);
                    panel.add(paramButton, BorderLayout.EAST);
                    return new OperationParameterPanelRenderer(panel, paramField);
                case AddMBeanOperationTableModel.IDX_METH_EXCEPTION :
                    JTextField excepField = new JTextField();
                    excepField.setEditable(false);
                    excepField.setName("methExcepTextField"); // NOI18N
                    JButton excepButton = 
                          new JButton(WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                    excepButton.setMargin(new java.awt.Insets(2,2,2,2));
                    excepButton.setEnabled(false);
                    JPanel excepPanel = new JPanel(new BorderLayout());
                    excepPanel.add(excepField, BorderLayout.CENTER);
                    excepPanel.add(excepButton, BorderLayout.EAST);
                    return 
                        new OperationExceptionPanelRenderer(excepPanel, excepField);
                case AddMBeanOperationTableModel.IDX_METH_DESCRIPTION :
                    return new TextFieldRenderer(new JTextField(), true, false);
                default :
                    return super.getCellRenderer(row,column);
            }
        }
            
        return super.getCellRenderer(row,column);
    }
}

/*
 * AddOperationTable.java
 *
 * Created on July 8, 2005, 9:45 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
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
import org.netbeans.modules.jmx.mbeanwizard.renderer.OperationExceptionPanelRenderer;
import org.netbeans.modules.jmx.mbeanwizard.renderer.OperationParameterPanelRenderer;
import org.netbeans.modules.jmx.mbeanwizard.renderer.WrapperTextFieldRenderer;
import org.netbeans.modules.jmx.mbeanwizard.table.OperationTable;

/**
 *
 * @author tl156378
 */
public class AddOperationTable extends OperationTable {
    
    /**
     * Constructor
     * @param model the table model of this table
     * @param wiz the wizard panel
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
                    return new WrapperTextFieldRenderer(new JTextField(), false);
                case AddMBeanOperationTableModel.IDX_METH_TYPE :
                    JComboBox typeBox = WizardHelpers.instanciateRetTypeJComboBox();
                    return new ComboBoxRenderer(typeBox,false);
                case AddMBeanOperationTableModel.IDX_METH_PARAM :
                    JTextField paramField = new JTextField();
                    paramField.setEditable(false);
                    paramField.setName("methParamTextField");
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
                    excepField.setName("methExcepTextField");
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
                    return new WrapperTextFieldRenderer(new JTextField(), false);
                default : 
                    return super.getCellRenderer(row,column);
            }
        }
            
        return super.getCellRenderer(row,column);
    }
}

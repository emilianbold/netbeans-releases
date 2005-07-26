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

package org.netbeans.modules.jmx.mbeanwizard;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.jmx.mbeanwizard.table.WrapperAttributeTable;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanWrapperAttributeTableModel;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanWrapperOperationTableModel;
import org.netbeans.modules.jmx.MBeanDO;
import org.netbeans.modules.jmx.MBeanOperation;
import org.netbeans.modules.jmx.MBeanAttribute;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.jmi.javamodel.JavaClass;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JLabel;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;


/**
 *
 * @author an156382
 */
public class MBeanWrapperAttributePanel extends MBeanAttributePanel {
    
    /** Creates a new instance of WrapperPanel */
    public MBeanWrapperAttributePanel(WrapperAttributesWizardPanel wiz) {
        super(wiz);
        initWrapperComponents();
        String str = NbBundle.getMessage(MBeanWrapperAttributePanel.class,"LBL_Attribute_Panel");// NOI18N
        setName(str);
    }
    
    protected void initJTables() {
        
        attributeModel = new MBeanWrapperAttributeTableModel(); 
        attributeTable = new WrapperAttributeTable(attributeModel,wiz); 
    }
    
    /**
     * Overriding super class method to force emty treatment
     * The reason is a Layout manager change that is incompatible
     * This method is therefore empty thus it is called by the constructor
     * of the super class
     * The real treatment of component initialization is in 
     * initWrapperComponents() which is called later
     */
    protected void initComponents() {
    }
    
    protected void initWrapperComponents() {
        
        initJTables();
        
        attrColumnModel = attributeTable.getColumnModel();
        affectAttributeTableComponents(attrColumnModel);
    }
    /*
    protected void affectAttributeTableComponents(TableColumnModel columnModel) {
        
        TableColumn selColumn = columnModel.getColumn(
                MBeanWrapperAttributeTableModel.IDX_ATTR_SELECTION);
        selColumn.setPreferredWidth(7);
        // creates the scroll pane and add the attribute table to it.
        JScrollPane attributeJTableScrollPane = new JScrollPane(attributeTable);
        // we are in a gridbag layout; size of the JTable has to be specified
        attributeJTableScrollPane.setPreferredSize(new java.awt.Dimension(500,145));
        JPanel firstInternalAttributePanel = new JPanel();
        
        firstInternalAttributePanel.setLayout(new BorderLayout());
        
        // adds the two previously defined panels to the container
        firstInternalAttributePanel.add(attributeJTableScrollPane, 
                BorderLayout.CENTER);
        
        JLabel tableLabel = new JLabel(NbBundle.getMessage(MBeanWrapperAttributePanel.class, 
                "LBL_AttrTable_FromExistingClass"));
        
        add(tableLabel, BorderLayout.NORTH);
        add(firstInternalAttributePanel,BorderLayout.CENTER);
    }
            */
      private boolean AttributeNameAlreadyChecked() {
            
            ArrayList attributeNames = new ArrayList(attributeModel.size());
            
            //get all the attribute names
            for (int i=0; i < attributeModel.size(); i++)
                attributeNames.add(attributeModel.getAttribute(i).getName());
            
            // if the list does not contain any doubled bloom no further treatment
            if (!NameAlreadyContained(attributeNames))
                return false;
            
            // else remove all attributes that are not checked
            removeUnchecked(attributeNames);
            
            // verify that no doubled bloom any more
            return (NameAlreadyContained(attributeNames));
        }
        
        private ArrayList<String> removeUnchecked(ArrayList<String> array) {
            if (array.size() > 0) {
                int k = 0; // counter for array element
                for (Iterator<String> iter = array.iterator(); iter.hasNext();) {
                    String elem = iter.next();
                    
                    if (!((MBeanWrapperAttribute)attributeModel.getAttribute(k)).isSelected()) 
                        iter.remove(); // remove all non checked elements
          
                    k++;
                }
            }
            return array; //return the array containing all checked elements
        }
    
        private boolean NameAlreadyContained(ArrayList<String> attributeNames) {

            for (int i=0; i < attributeNames.size(); i++) {
                int count = 0;
                String currentValue = attributeNames.get(i);
                for(int j=0; j < attributeNames.size(); j++) {
                    String compareValue = attributeNames.get(j);
                    if (compareValue.equals(currentValue))
                        count ++;
                    if (count >= 2)
                        return true;
                }
            }
            return false;
        }
    
    /**
     * Inner static class which defines the wizard descriptor and fills it with 
     * user information
     */
    public static class WrapperAttributesWizardPanel extends AttributesWizardPanel
            implements org.openide.WizardDescriptor.FinishablePanel {
        private MBeanWrapperAttributePanel panel = null;
        
        /**
         * Implementation of the FinishablePanel Interface; provides the Finish
         * Button to be always enabled
         * @return boolean true if the panel can be the last one 
         * and enables the finish button
         */
        public boolean isFinishPanel() {
            return true;
        }
        
        /**
         * Method which enables the next button
         * @return boolean true if the information in the panel 
         * is sufficient to go to the next step
         */
        public boolean isValid() {
            
            boolean attrValid = true;
            String msg = WizardConstants.EMPTY_STRING;
           
            if (getPanel() != null) {
                if (getPanel().AttributeNameAlreadyChecked()) { 
                    attrValid = false;
                    msg = NbBundle.getMessage(MBeanAttrAndMethodPanel.class,"LBL_State_Same_Attribute_Name");// NOI18N
                }
                setErrorMsg(msg);
            }
            return attrValid;
        }
        
        /**
         * Displays the given message in the wizard's message area.
         *
         * @param  message  message to be displayed, or <code>null</code>
         *                  if the message area should be cleared
         */
        private void setErrorMsg(String message) {
            if (wiz != null) {
                wiz.putProperty(WizardConstants.WIZARD_ERROR_MESSAGE,
                        message);
            }
        }
        
        /**
         * Method which fires an event to notify that there was a change in 
         * the data
         */
        public void event() {
            fireChangeEvent();
        }
        
        /**
         * Method returning the corresponding panel; here 
         * the MBeanAttrAndMethodPanel
         * @return Component the panel
         */
        public Component getComponent() { return getPanel(); }
        
        private MBeanWrapperAttributePanel getPanel() {
            if (panel == null) {
                panel = new MBeanWrapperAttributePanel(this);
            }
            
            return panel;
        }
        
        /**
         * Method which reads the in the model already contained data
         * @param settings an object containing the contents of the 
         *        attribute table
         */
        public void readSettings(Object settings) {
            //TODO complete here
            wiz = (WizardDescriptor) settings;
            MBeanDO mbdo = null;
            try {
                mbdo = org.netbeans.modules.jmx.Introspector.introspectClass(
                        (JavaClass)wiz.getProperty(WizardConstants.PROP_MBEAN_EXISTING_CLASS));
                
                List<MBeanAttribute> attributes = mbdo.getAttributes();
                for (Iterator<MBeanAttribute> it = attributes.iterator(); it.hasNext();) {
                    ((MBeanWrapperAttributeTableModel) getPanel().getAttributeModel()).addRow(it.next()); 
                }
                ((MBeanWrapperAttributeTableModel) getPanel().getAttributeModel()).setFirstEditableRow(attributes.size());
                
                event();
            } catch (Exception e) {e.printStackTrace();}
            wiz.putProperty(WizardConstants.WIZARD_ERROR_MESSAGE, "");// NOI18N
        }
        
        /**
         * Method called to store information from the GUI into the wizard map
         * @param settings the object containing the data to store
         */
        public void storeSettings(Object settings) {
            WizardDescriptor wiz = (WizardDescriptor) settings;
        }
        
        /**
         * Returns a help context
         * @return HelpCtxt the help context
         */
        public HelpCtx getHelp() {
            return new HelpCtx("jmx_instrumenting_app");// NOI18N
        }
    }
}

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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.GenericWizardPanel;

import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanAttributeTableModel;
import org.netbeans.modules.jmx.mbeanwizard.table.AttributeTable;

import org.openide.WizardDescriptor;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.JLabel;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.*;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.MBeanAttribute;
import org.openide.WizardDescriptor.FinishablePanel;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 *
 * MBean Attribute and Operation Panel: Manages the components responsible for 
 * MBean Attribute and Operation description
 *
 */
public class MBeanAttributePanel extends JPanel implements DocumentListener,
        ListSelectionListener{
    private boolean DEBUG = false;
    protected ResourceBundle bundle; 
   
    protected AttributesWizardPanel wiz;
    protected JTable attributeTable;
    protected MBeanAttributeTableModel attributeModel;
    
    JButton attrRemoveJButton;
    protected JLabel tableLabel;
    
    protected TableColumnModel attrColumnModel;
    
    /**
     * Panel constructor: Fills a wizard descriptor with the user data
     * @param <code>wiz</code> the wizard panel
     */
    public MBeanAttributePanel(AttributesWizardPanel wiz) {
        //super(new GridLayout(1,1));
        super(new BorderLayout(0, 5));
        this.wiz = wiz;
        bundle = NbBundle.getBundle(MBeanAttributePanel.class);
        initComponents();
        
        //String str = NbBundle.getMessage(MBeanAttributePanel.class,"LBL_Attribute_Panel");// NOI18N
        String str = bundle.getString("LBL_Attribute_Panel");// NOI18N
        setName(str);
        wiz.setErrorMsg(" ");// NOI18N
        
    }
    
    protected void initJTables() {
        
        attributeModel = new MBeanAttributeTableModel();
        attributeTable = new AttributeTable(attributeModel,wiz);
        attributeTable.setName("attributeTable");// NOI18N
        
        // Accessibility
        //attributeTable.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MBeanAttributePanel.class,"ACCESS_ATTRIBUTES_TABLE"));// NOI18N
        //attributeTable.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MBeanAttributePanel.class,"ACCESS_ATTRIBUTES_TABLE_DESCRIPTION"));// NOI18N
        attributeTable.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_ATTRIBUTES_TABLE"));// NOI18N
        attributeTable.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_ATTRIBUTES_TABLE_DESCRIPTION"));// NOI18N
    }
    
    /**
     * Returns the table model of the attribute table
     * @return <code>MBeanAttributeTableModel</code> the attribute table
     */
    public MBeanAttributeTableModel getAttributeModel() {
        return attributeModel;
    }
    
    protected void initComponents() {
        
        initJTables();
        
        attrColumnModel = attributeTable.getColumnModel();
        affectAttributeTableComponents(attrColumnModel);
    }
    
    protected void affectAttributeTableComponents(TableColumnModel columnModel) {
        
        // creates the scroll pane and add the attribute table to it.
        JScrollPane attributeJTableScrollPane = new JScrollPane(attributeTable);
        
        // defines the attribute add and remove buttons
        JButton attrAddJButton = new JButton();
        Mnemonics.setLocalizedText(attrAddJButton,
                //NbBundle.getMessage(MBeanAttributePanel.class,"BUTTON_add_attr"));//NOI18N
                bundle.getString("BUTTON_add_attr"));// NOI18N
        //attrRemoveJButton = new JButton(NbBundle.getMessage(MBeanAttributePanel.class,"BUTTON_rem_attr"));// NOI18N
        attrRemoveJButton = new JButton(bundle.getString("BUTTON_rem_attr"));// NOI18N
        Mnemonics.setLocalizedText(attrRemoveJButton,
                //NbBundle.getMessage(MBeanAttributePanel.class,"BUTTON_rem_attr"));//NOI18N
                bundle.getString("BUTTON_rem_attr"));// NOI18N
        
        attrAddJButton.setName("attrAddJButton");// NOI18N
        attrRemoveJButton.setName("attrRemoveJButton");// NOI18N
        
        //TODO factorise the addActionListeners. this is a copy paste from 
        // AddTableRowListener a little modified
        // attrAddJButton.addActionListener(new AddTableRowListener(
        // attributeTable, attributeModel, attrRemoveJButton));
        attrAddJButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                attributeModel.addRow();
                
                // if the model has at least one line, enable the
                // corresponding remove button
                if (attributeModel.size() != 0)
                    attrRemoveJButton.setEnabled(true);
                
                wiz.event();
            }
        });
        
        //TODO factorise the addActionListeners. this is a copy paste from 
        // RemTableRowListener a little modified 
        // attrRemoveJButton.addActionListener(new RemTableRowListener(
        // attributeTable, attributeModel, attrRemoveJButton));
        attrRemoveJButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final int selectedRow = attributeTable.getSelectedRow();
                
                //No row selected
                if (selectedRow == -1) return;
                
                try {
                    attributeModel.remRow(selectedRow, attributeTable);
                    attributeModel.selectNextRow(selectedRow, attributeTable);
                    
                } catch (Exception ex) {
                    System.out.println("Exception here : ");// NOI18N
                    ex.printStackTrace();
                }
                
                // if the model has no rows, disable the remove button
                if (attributeModel.size() == 0)
                    attrRemoveJButton.setEnabled(false);
                
                wiz.event();
            }
        });
        
        // remove button is first disabled
        attrRemoveJButton.setEnabled(false);
        
        // defines the panel involving the attributes table and the 2 buttons
        JPanel attributeJPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel firstInternalAttributePanel = new JPanel();
        firstInternalAttributePanel.setLayout(new BorderLayout());
        attributeJPanel.add(attrAddJButton);
        attributeJPanel.add(attrRemoveJButton);
        
        // adds the two previously defined panels to the container
        firstInternalAttributePanel.add(attributeJTableScrollPane, 
                BorderLayout.CENTER);
        firstInternalAttributePanel.add(attributeJPanel, BorderLayout.SOUTH);
      
        // init labels
        tableLabel = new JLabel();
        Mnemonics.setLocalizedText(tableLabel,
                     bundle.getString("LBL_AttrTable"));//NOI18N
        tableLabel.setLabelFor(attributeTable);
        
        add(tableLabel, BorderLayout.NORTH);
        add(firstInternalAttributePanel, BorderLayout.CENTER);
        
        //attrAddJButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MBeanAttributePanel.class,"ACCESS_ADD_ATTRIBUTE"));// NOI18N
        //attrAddJButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MBeanAttributePanel.class,"ACCESS_ADD_ATTRIBUTE_DESCRIPTION"));// NOI18N
        //attrRemoveJButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MBeanAttributePanel.class,"ACCESS_REMOVE_ATTRIBUTE"));// NOI18N
        //attrRemoveJButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MBeanAttributePanel.class,"ACCESS_REMOVE_ATTRIBUTE_DESCRIPTION"));// NOI18N
        attrAddJButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_ADD_ATTRIBUTE"));// NOI18N
        attrAddJButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_ADD_ATTRIBUTE_DESCRIPTION"));// NOI18N
        attrRemoveJButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_REMOVE_ATTRIBUTE"));// NOI18N
        attrRemoveJButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_REMOVE_ATTRIBUTE_DESCRIPTION"));// NOI18N
        
        
    }
    
    private boolean AttributeNameAlreadyContained() {
        
        ArrayList attributeNames = new ArrayList(attributeModel.size());
        //get all the attribute names
        for (int i=0; i < attributeModel.size(); i++) {
            attributeNames.add(attributeModel.getAttribute(i).getName());
        }
        
        for (int i=0; i < attributeNames.size(); i++) {
            int count = 0;
            String currentValue = ((String)attributeNames.get(i));
            for(int j=0; j < attributeNames.size(); j++) {
                String compareValue = ((String)attributeNames.get(j));
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
    public static class AttributesWizardPanel extends GenericWizardPanel
            implements FinishablePanel, FireEvent {
        private MBeanAttributePanel panel = null;
        protected WizardDescriptor wiz = null;
        
        public WizardDescriptor getWiz() {
            return wiz;
        }
        
        /**
         * Implementation of the FinishablePanel Interface; provides the Finish
         * Button to be always enabled
         * @return boolean true if the panel can be the last one 
         * and enables the finish button
         */
        public boolean isFinishPanel() {
            return isValid();
        }
        
        /**
         * Method which enables the next button
         * @return boolean true if the information in the panel 
         * is sufficient to go to the next step
         */
        public boolean isValid() {
            
            boolean attrValid = true;
            boolean opValid = true;
            String msg = WizardConstants.EMPTY_STRING;
            
            if (getPanel() != null) {
                if (getPanel().AttributeNameAlreadyContained()) {
                    attrValid = false;
                    //msg = NbBundle.getMessage(MBeanAttributePanel.class,"LBL_State_Same_Attribute_Name");// NOI18N
                    msg = getPanel().bundle.getString("LBL_State_Same_Attribute_Name");// NOI18N
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
                        message);    //NOI18N
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
        
        private MBeanAttributePanel getPanel() {
            if (panel == null) {
                panel = new MBeanAttributePanel(this);
            }
            
            return panel;
        }
        
        /**
         * Method which reads the in the model already contained data
         * @param settings an object containing the contents of the 
         *        attribute table
         */
        public void readSettings(Object settings) {
            wiz = (WizardDescriptor) settings;
            
            //read the contents of the attribute table
            MBeanAttributeTableModel attrModel = getPanel().attributeModel;
            
            attrModel.clear();
            
            String nbAttrsStr = (String) wiz.getProperty(WizardConstants.PROP_ATTR_NB);
            int nbAttrs = 0;
            if (nbAttrsStr != null)
                nbAttrs = new Integer(nbAttrsStr);
            
            for (int i = 0 ; i < nbAttrs ; i++) {
                
                String name = (String) wiz.getProperty(WizardConstants.PROP_ATTR_NAME + i);
                
                String type = (String)wiz.getProperty(WizardConstants.PROP_ATTR_TYPE + i);
                
                String access = (String)wiz.getProperty(WizardConstants.PROP_ATTR_RW + i);
                
                String descr = (String)wiz.getProperty(WizardConstants.PROP_ATTR_DESCR + i);
                
                attrModel.addRow(new MBeanAttribute(name,type,access,descr));
                
            }
            wiz.putProperty(WizardConstants.WIZARD_ERROR_MESSAGE, "");// NOI18N
        }
        
        /**
         * Method called to store information from the GUI into the wizard map
         * @param settings the object containing the data to store
         */
        public void storeSettings(Object settings) {
            WizardDescriptor wiz = (WizardDescriptor) settings;
            
            //stores all values from the table in the model even with keyboard
            //navigation
            getPanel().attributeTable.editingStopped(new ChangeEvent(this));
            
            //read the contents of the attribute table
            MBeanAttributeTableModel attrModel = getPanel().attributeModel;
            
            int nbAttrs = attrModel.size();
            
            wiz.putProperty(WizardConstants.PROP_ATTR_NB, 
                    new Integer(nbAttrs).toString());
            
            for (int i = 0 ; i < nbAttrs ; i++) {
                
                // the current attribute (number i)
                MBeanAttribute attr = attrModel.getAttribute(i);
                
                wiz.putProperty(WizardConstants.PROP_ATTR_NAME + i,
                        attr.getName());
                
                wiz.putProperty(WizardConstants.PROP_ATTR_TYPE + i,
                        attr.getTypeName());
                
                wiz.putProperty(WizardConstants.PROP_ATTR_RW + i,
                        attr.getAccess());
                
                wiz.putProperty(WizardConstants.PROP_ATTR_DESCR + i,
                        attr.getDescription());
            }
        }
        
        /**
         * Returns a help context
         * @return HelpCtxt the help context
         */
        public HelpCtx getHelp() {
            return new HelpCtx("jmx_instrumenting_app");// NOI18N
        }
    }
    
    /**
     * Fires that a value changed
     * @param evt A list selection event
     */
    public void valueChanged(ListSelectionEvent evt) {
    }
    
    /**
     * Implementing method
     * @param e a DocumentEvent
     */
    public void changedUpdate( DocumentEvent e ) {}
    /**
     * Implementing method
     * @param e a DocumentEvent
     */
    public void insertUpdate( DocumentEvent e )  {}
    /**
     * Implementing method
     * @param e a DocumentEvent
     */
    public void removeUpdate( DocumentEvent e )  {}
}
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.jmx.mbeanwizard;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.jmx.mbeanwizard.table.WrapperAttributeTable;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanWrapperAttributeTableModel;
import org.netbeans.modules.jmx.MBeanDO;
import org.netbeans.modules.jmx.MBeanAttribute;
import org.netbeans.modules.jmx.common.WizardConstants;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.lang.model.type.TypeMirror;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.jmx.JavaModelHelper;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;


/**
 *
 * @author an156382
 */
public class MBeanWrapperAttributePanel extends MBeanAttributePanel 
    implements ListSelectionListener{
    
    private int orderNumber = Integer.MAX_VALUE;
    
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
        attributeTable.setName("wrapperAttributeTable");// NOI18N
        attributeTable.getSelectionModel().addListSelectionListener(this);
        
        // Accessibility
        //attributeTable.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MBeanWrapperAttributePanel.class,"ACCESS_ATTRIBUTES_TABLE"));// NOI18N
        //attributeTable.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MBeanWrapperAttributePanel.class,"ACCESS_WRAPPED_ATTRIBUTES_TABLE_DESCRIPTION"));// NOI18N
    
        attributeTable.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_ATTRIBUTES_TABLE"));// NOI18N
        attributeTable.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_WRAPPED_ATTRIBUTES_TABLE_DESCRIPTION"));// NOI18N
    
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
    
    public MBeanWrapperAttributeTableModel getModel() {
        return (MBeanWrapperAttributeTableModel)attributeModel;
    }
    
    protected void affectAttributeTableComponents(TableColumnModel columnModel) {
        super.affectAttributeTableComponents(columnModel);
        //changing label text for the table label
        Mnemonics.setLocalizedText(tableLabel,
                     bundle.getString("LBL_AttrTable_FromExistingClass"));//NOI18N 
        tableLabel.setLabelFor(attributeTable);
        
        // second label definition to get a label text on multiple lines; a new
        // label is added to the labelPanel
        JLabel complementaryLabel = new JLabel(bundle.getString("LBL_AttrTable_FromExistingClass_suite"));//NOI18N
        labelPanel.add(complementaryLabel, java.awt.BorderLayout.SOUTH);
        
        attrRemoveJButton.setName("wrapperAttributeRemoveButton");//NOI18N 
        
        /* New ActionListener for the remove button that overrides the one from
         * the super class: Now, to be able to remove a line, it must not be 
         * an introspected attribute
         */ 
        attrRemoveJButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final int selectedRow = attributeTable.getSelectedRow();
                int firstEditableRow = getModel().getFirstEditableRow();
                
                //No row selected
                if (selectedRow == -1) return;
                
                if (selectedRow >= firstEditableRow) { // remove allowed
                    try {
                        //attrRemoveJButton.setEnabled(true);
                        attributeModel.remRow(selectedRow, attributeTable);
                        attributeModel.selectNextRow(selectedRow, attributeTable);  
                    } catch (Exception ex) {
                        //System.out.println("Exception here : ");// NOI18N
                        //ex.printStackTrace();
                    }
                } else {
                    attrRemoveJButton.setEnabled(false);
                }
                
                // if the model has no rows, disable the remove button
                if (attributeModel.size() == getModel().getFirstEditableRow())
                    attrRemoveJButton.setEnabled(false);
                
                wiz.event();
            }
        });
        
        //add(panel, BorderLayout.NORTH);
    }
    
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
            return false;
        }
        
        /**
         * Method which enables the next button
         * @return boolean true if the information in the panel 
         * is sufficient to go to the next step
         */
        public boolean isValid() {
            
            boolean attrValid = true;
            String msg = null;
           
            if (getPanel() != null) {
                if (getPanel().AttributeNameAlreadyChecked()) { 
                    attrValid = false;
                    //msg = NbBundle.getMessage(MBeanWrapperAttributePanel.class,"LBL_State_Same_Attribute_Name");// NOI18N
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
                wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
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
            
            wiz = (WizardDescriptor) settings;
            
            // if the user loads the panel for the first time, perform introspection
            // else do nothing ...
            int oNumber = (Integer)wiz.getProperty(WizardConstants.PROP_USER_ORDER_NUMBER);
            if (oNumber != getPanel().orderNumber) { // the user loads the panel for the first time
                getPanel().getModel().clear();
                MBeanDO mbdo = null;
                try {
                    mbdo = JavaModelHelper.getMBeanLikeModel(
                            (JavaSource)wiz.getProperty(WizardConstants.PROP_MBEAN_EXISTING_CLASS));
                    
                    List<MBeanAttribute> attributes = mbdo.getAttributes();
                    for (Iterator<MBeanAttribute> it = attributes.iterator(); it.hasNext();) {
                        ((MBeanWrapperAttributeTableModel) getPanel().getAttributeModel()).addRow(it.next());
                    }
                    ((MBeanWrapperAttributeTableModel) getPanel().getAttributeModel()).setFirstEditableRow(attributes.size());
                    
                    event();
                } catch (Exception e) {e.printStackTrace();}
                
                String nbAddedAttrStr = (String)wiz.getProperty(WizardConstants.PROP_ATTR_NB);
                
                int nbAddedAttr = 0;
                
                if (nbAddedAttrStr != null)
                    nbAddedAttr = new Integer(nbAddedAttrStr);
                
                for (int i=0; i < nbAddedAttr; i++) {
                    getPanel().getModel().addRow(
                            new MBeanWrapperAttribute(
                            true, 
                            (String)wiz.getProperty(WizardConstants.PROP_ATTR_NAME + i),
                            (String)wiz.getProperty(WizardConstants.PROP_ATTR_TYPE + i),
                            (String)wiz.getProperty(WizardConstants.PROP_ATTR_RW + i),
                            (String)wiz.getProperty(WizardConstants.PROP_ATTR_DESCR + i),
                            (TypeMirror) wiz.getProperty(WizardConstants.PROP_ATTR_TYPE_MIRROR + i)));
                            
                }
                getPanel().orderNumber = oNumber;
            } 
            
            setErrorMsg(null);
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
            MBeanWrapperAttributeTableModel attrModel = 
                    (MBeanWrapperAttributeTableModel)getPanel().attributeModel;
            
            int nbAttrs = attrModel.size();
            int firstEditableRow = attrModel.getFirstEditableRow();
            
            // counter for the attribute storage
            int j = 0;
            
            // two loops; one for the wrapped atributes and the other for the
            // attributes added by the user
            for (int i = firstEditableRow ; i < nbAttrs; i++) {
                
                // the current attribute (number i)
                MBeanWrapperAttribute attr = attrModel.getWrapperAttribute(i);
                
                    wiz.putProperty(WizardConstants.PROP_ATTR_NAME + j,
                            attr.getName());
                    
                    wiz.putProperty(WizardConstants.PROP_ATTR_TYPE + j,
                            attr.getTypeName());
                    wiz.putProperty(WizardConstants.PROP_ATTR_TYPE_MIRROR + j,
                            attr.getTypeMirror());
                    
                    wiz.putProperty(WizardConstants.PROP_ATTR_RW + j,
                            attr.getAccess());
                    
                    wiz.putProperty(WizardConstants.PROP_ATTR_DESCR + j,
                            attr.getDescription());
                    j++;
            }
            
            for (int i = 0 ; i < firstEditableRow; i++) {
                
                // the current attribute (number i)
                MBeanWrapperAttribute attr = attrModel.getWrapperAttribute(i);
                
                    wiz.putProperty(WizardConstants.PROP_INTRO_ATTR_NAME + i,
                            attr.getName());
                    
                    wiz.putProperty(WizardConstants.PROP_INTRO_ATTR_TYPE + i,
                            attr.getTypeName());
                    wiz.putProperty(WizardConstants.PROP_INTRO_ATTR_TYPE_MIRROR + i,
                            attr.getTypeMirror());
                    
                    wiz.putProperty(WizardConstants.PROP_INTRO_ATTR_RW + i,
                            attr.getAccess());
                    
                    wiz.putProperty(WizardConstants.PROP_INTRO_ATTR_DESCR + i,
                            attr.getDescription());
                    
                    wiz.putProperty(WizardConstants.PROP_INTRO_ATTR_SELECT + i, 
                            attr.isSelected());
            }
            
            // sets the number of introspected attributes and the number of
            // added attributes
            wiz.putProperty(WizardConstants.PROP_INTRO_ATTR_NB, 
                    new Integer(firstEditableRow).toString());
            wiz.putProperty(WizardConstants.PROP_ATTR_NB, 
                    new Integer(nbAttrs - firstEditableRow).toString());
        }
        
        /**
         * Returns a help context
         * @return HelpCtxt the help context
         */
        public HelpCtx getHelp() {
            return new HelpCtx("jmx_instrumenting_from_existing_app");// NOI18N
        }
    }
    
    public void valueChanged(ListSelectionEvent evt) {
        boolean enable = (attributeTable.getSelectedRow() < 
                ((MBeanWrapperAttributeTableModel)attributeModel).getFirstEditableRow());
            attrRemoveJButton.setEnabled(!enable);
    }    
}

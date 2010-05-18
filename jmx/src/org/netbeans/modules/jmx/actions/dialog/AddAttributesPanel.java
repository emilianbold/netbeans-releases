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

package org.netbeans.modules.jmx.actions.dialog;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.JavaModelHelper;
import org.netbeans.modules.jmx.MBeanAttribute;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel which is used to ask new attributes to user.
 * @author  tl156378
 */
public class AddAttributesPanel extends javax.swing.JPanel 
        implements FireEvent, ListSelectionListener {
    
    /** class to add Attributes to */
    private JavaSource currentClass;
    
    private AddMBeanAttributeTableModel attributeModel;
    private AddAttributeTable attributeTable;
    
    private ResourceBundle bundle;
    
    private JButton btnOK;
    
    /**
     * Returns all the specified attributes by user.
     * @return <CODE>MBeanAttribute[]</CODE> specified attributes by user
     */
    public MBeanAttribute[] getAttributes() {
        MBeanAttribute[] attributes = new MBeanAttribute[
                attributeModel.getRowCount() - attributeModel.getFirstEditable()];
        for (int i = 0; i < attributes.length; i++)
            attributes[i] = attributeModel.getAttribute(
                    attributeModel.getFirstEditable() + i);
        return attributes;
    }
     
    /** 
     * Creates new form AddAttributesPanel.
     * @param  node  node selected when the Add MBean Attributes action was invoked
     */
    public AddAttributesPanel(Node node) {
        bundle = NbBundle.getBundle(AddAttributesPanel.class);
        
        DataObject dob = (DataObject)node.getCookie(DataObject.class);
        FileObject fo = null;
        if (dob != null) fo = dob.getPrimaryFile();
        
        currentClass = JavaModelHelper.getSource(fo);
        
        // init tags
        
        initComponents();
        
        attributeModel = new AddMBeanAttributeTableModel();
        attributeTable = new AddAttributeTable(attributeModel,this);
        attributeTable.setName("attributeTable"); // NOI18N
        //attributeTable.setBorder(new javax.swing.border.EtchedBorder());
        jScrollPane1.setViewportView(attributeTable);
        attributeTable.getSelectionModel().addListSelectionListener(this);
        attrTableLabel.setLabelFor(attributeTable);
        
        //discovery of existing Attributes
        MBeanAttribute[] existAttributes = null;
        try {
            existAttributes = JavaModelHelper.getAttributes(currentClass);
        }catch(IOException ioex) {
            existAttributes = new MBeanAttribute[0];
        }
        
        for (int i = 0; i < existAttributes.length; i++)
            attributeModel.addAttribute(existAttributes[i]);
        attributeModel.setFirstEditable(existAttributes.length);
        
        removeButton.setEnabled(false);
        addButton.addActionListener(
                new AddTableRowListenerWithFireEvent(attributeTable, attributeModel,
                removeButton, this));
        removeButton.addActionListener(new RemTableRowListenerWithFireEvent(
                attributeTable, attributeModel, removeButton,this));
        
        // init labels
        Mnemonics.setLocalizedText(attrTableLabel,
                bundle.getString("LBL_Attributes")); // NOI18N
        Mnemonics.setLocalizedText(addButton,
                bundle.getString("LBL_Button_AddAttribute")); // NOI18N
        Mnemonics.setLocalizedText(removeButton,
                bundle.getString("LBL_Button_RemoveAttribute")); // NOI18N
        
        // for accessibility
        attributeTable.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_ATTRIBUTES_TABLE")); // NOI18N
        attributeTable.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_ATTRIBUTES_TABLE_DESCRIPTION")); // NOI18N
        addButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_ADD_ATTRIBUTE")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_ADD_ATTRIBUTE_DESCRIPTION")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REMOVE_ATTRIBUTE")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REMOVE_ATTRIBUTE_DESCRIPTION")); // NOI18N
        
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_PANEL"));// NOI18N
    }
    
    public void event() {
        removeButton.setEnabled(
                (attributeModel.getRowCount() > attributeModel.getFirstEditable()));
        btnOK.setEnabled(isAcceptable());
    }
    
    private boolean isAcceptable() {
        // First check the type that has been entered...
        boolean isOK = true;
         for (int i=0; i < attributeModel.size(); i++) {
            String type = attributeModel.getAttribute(i).getTypeName();
            isOK = JavaModelHelper.checkKnownType(currentClass, type);
            if(!isOK) {
                stateLabel.setText(bundle.getString("LBL_State_Bad_Type") + type); // NOI18N
                return false;
            } 
         }
         
         
        if (!(attributeModel.getRowCount() > attributeModel.getFirstEditable())) {
            stateLabel.setText(bundle.getString("LBL_NoAttribute")); // NOI18N
            return false;
        } else if (AttributeNameAlreadyContained()) {
            stateLabel.setText(bundle.getString("LBL_State_Same_Attribute_Name")); // NOI18N
            return false;
        } else {
            stateLabel.setText(""); // NOI18N
            return true;
        }
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
     * Displays a configuration dialog and updates the MBean options
     * according to the user's settings.
     * @return <CODE>boolean</CODE> true only if specified attributes are correct.
     */
    public boolean configure() {
        
        // create and display the dialog:
        MessageFormat formAttribute = 
                new MessageFormat(bundle.getString("LBL_AddAttributesAction.Title")); // NOI18N
        String itfName = ""; // NOI18N
        try {
            itfName = JavaModelHelper.getManagementInterfaceSimpleName(currentClass);
        }catch(IOException ioe) {
            // 
        }
        Object[] args = {itfName};
        String title = formAttribute.format(args);
        
        btnOK = new JButton(bundle.getString("LBL_OK")); // NOI18N
        btnOK.setEnabled(isAcceptable());
        btnOK.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_OK_DESCRIPTION")); // NOI18N
        
        Object returned = DialogDisplayer.getDefault().notify(
                new DialogDescriptor(
                this,
                title,
                true,                       //modal
                new Object[] {btnOK, DialogDescriptor.CANCEL_OPTION},
                btnOK,                      //initial value
                        DialogDescriptor.DEFAULT_ALIGN,
                        new HelpCtx("jmx_mbean_update_attributes_operations"), // NOI18N
                        (ActionListener) null
                        ));
                
                if (returned == btnOK) {
                    return true;
                }
                return false;
    }
    
    /**
     * Returns the MBean class to add Attributes.
     * @return <CODE>JavaSource</CODE> the MBean class
     */
    public JavaSource getMBeanClass() {
        return currentClass;
    }
    
    public void valueChanged(ListSelectionEvent e) {
        int firstEditable = attributeModel.getFirstEditable();
        if ((attributeTable.getSelectedRow() != -1) &&
                (attributeTable.getSelectedRow() < firstEditable))
            removeButton.setEnabled(false);
        else if ((attributeTable.getSelectedRow() != -1) &&
                (attributeTable.getSelectedRow() >= firstEditable))
            removeButton.setEnabled(true);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        buttonsPanel = new javax.swing.JPanel();
        leftPanel = new javax.swing.JPanel();
        removeButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        stateLabel = new javax.swing.JLabel();
        attrTableLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(500, 300));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 12);
        add(jScrollPane1, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.BorderLayout());

        leftPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        leftPanel.add(removeButton, gridBagConstraints);

        addButton.setName("attrAddJButton");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        leftPanel.add(addButton, gridBagConstraints);

        buttonsPanel.add(leftPanel, java.awt.BorderLayout.WEST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 12);
        add(buttonsPanel, gridBagConstraints);

        stateLabel.setForeground(java.awt.SystemColor.activeCaption);
        stateLabel.setMaximumSize(new java.awt.Dimension(0, 18));
        stateLabel.setMinimumSize(new java.awt.Dimension(0, 18));
        stateLabel.setName("stateLabel");
        stateLabel.setPreferredSize(new java.awt.Dimension(0, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(stateLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 5, 12);
        add(attrTableLabel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel attrTableLabel;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JButton removeButton;
    private javax.swing.JLabel stateLabel;
    // End of variables declaration//GEN-END:variables
    
}

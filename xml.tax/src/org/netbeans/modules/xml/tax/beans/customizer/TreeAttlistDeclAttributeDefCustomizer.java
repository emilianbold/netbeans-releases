/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tax.beans.customizer;

import java.beans.PropertyChangeEvent;

import org.netbeans.tax.TreeException;
import org.netbeans.tax.TreeAttlistDeclAttributeDef;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeAttlistDeclAttributeDefCustomizer extends AbstractTreeCustomizer {

    /** Serial Version UID */
    private static final long serialVersionUID =2877621716093964464L;    

    
    //
    // init
    //

    /** */
    public TreeAttlistDeclAttributeDefCustomizer () {
	super();

        initComponents();

        elementNameLabel.setDisplayedMnemonic(Util.getChar("TreeAttributeDeclCustomizer.elemNameLabel.mne")); // NOI18N
        nameLabel.setDisplayedMnemonic(Util.getChar("MNE_xmlName")); // NOI18N
        typeLabel.setDisplayedMnemonic(Util.getChar("MNE_dtdAttDefType")); // NOI18N
        enumeratedLabel.setDisplayedMnemonic(Util.getChar("DTDAttDefNode.enumeratedLabel.mne")); // NOI18N
        defaultTypeLabel.setDisplayedMnemonic(Util.getChar("DTDAttDefNode.defaultTypeLabel.mne")); // NOI18N
        defaultValueLabel.setDisplayedMnemonic(Util.getChar("DTDAttDefNode.defaultValueLabel.mne")); // NOI18N
        
    }

    //
    // itself
    //

    /**
     */
    protected final TreeAttlistDeclAttributeDef getAttributeDef () {
        return (TreeAttlistDeclAttributeDef)getTreeObject();
    }

    /**
     */
    protected final void safePropertyChange (PropertyChangeEvent pche) {
        super.safePropertyChange (pche);
        
	if (pche.getPropertyName().equals (TreeAttlistDeclAttributeDef.PROP_NAME)) {
	    updateNameComponent();
	} else if (pche.getPropertyName().equals (TreeAttlistDeclAttributeDef.PROP_TYPE)) {
	    updateTypeComponent();
	} else if (pche.getPropertyName().equals (TreeAttlistDeclAttributeDef.PROP_ENUMERATED_TYPE)) {
	    updateEnumeratedTypeComponent();
	} else if (pche.getPropertyName().equals (TreeAttlistDeclAttributeDef.PROP_DEFAULT_TYPE)) {
	    updateDefaultTypeComponent();
	} else if (pche.getPropertyName().equals (TreeAttlistDeclAttributeDef.PROP_DEFAULT_VALUE)) {
	    updateDefaultValueComponent();
	}
    }

    
    /**
     */
    protected final void updateElementNameComponent () {
        elementNameField.setText (getAttributeDef().getElementName());
    }

    /**
     */
    protected final void updateNameComponent () {
        nameField.setText (getAttributeDef().getName());
    }

    /**
     */
    protected final void updateAttributeDefName () {
	try {
	    getAttributeDef().setName (nameField.getText());
	} catch (TreeException exc) {
	    updateNameComponent();
	    Util.notifyTreeException (exc);
	}
    }
    
    /**
     */
    protected final void updateTypeComponent () {
        typeField.setText (getAttributeDef().getTypeName());
    }

    /**
     */
    protected final void updateAttributeDefType () {
	try {
	    getAttributeDef().setType
		(TreeAttlistDeclAttributeDef.findType (typeField.getText()),
		 getAttributeDef().getEnumeratedType());
	} catch (TreeException exc) {
	    updateTypeComponent();
	    Util.notifyTreeException (exc);
	}
    }
    
    /**
     */
    protected final void updateEnumeratedTypeComponent () {
        enumeratedField.setText (null2text (getAttributeDef().getEnumeratedTypeString()));
    }

    /**
     */
    protected final void updateAttributeDefEnumeratedType () {
	try {
	    getAttributeDef().setType
		(getAttributeDef().getType(),
		 TreeAttlistDeclAttributeDef.createEnumeratedType (text2null (enumeratedField.getText())));
	} catch (TreeException exc) {
	    updateEnumeratedTypeComponent();
	    Util.notifyTreeException (exc);
	}
    }
    
    /**
     */
    protected final void updateDefaultTypeComponent () {
        defaultTypeField.setText (null2text (getAttributeDef().getDefaultTypeName()));
    }

    /**
     */
    protected final void updateAttributeDefDefaultType () {
	try {
	    getAttributeDef().setDefaultType
		(TreeAttlistDeclAttributeDef.findDefaultType (text2null (defaultTypeField.getText())),
		 getAttributeDef().getDefaultValue());
	} catch (TreeException exc) {
	    updateDefaultTypeComponent();
	    Util.notifyTreeException (exc);
	}
    }
    
    /**
     */
    protected final void updateDefaultValueComponent () {
        defaultValueField.setText (null2text (getAttributeDef().getDefaultValue()));
    }
        
    /**
     */
    protected final void updateAttributeDefDefaultValue () {
	try {
	    getAttributeDef().setDefaultType (getAttributeDef().getDefaultType(), text2null (defaultValueField.getText()));
	} catch (TreeException exc) {
	    updateDefaultValueComponent();
	    Util.notifyTreeException (exc);
	}
    }
    
    
    /**
     */
    protected final void initComponentValues () {
	updateElementNameComponent();
        updateNameComponent();
        updateTypeComponent();
        updateEnumeratedTypeComponent();
        updateDefaultTypeComponent();
        updateDefaultValueComponent();
    }

    
    /**
     */
    protected final void updateReadOnlyStatus (boolean editable) {
        nameField.setEditable (editable);
        typeField.setEditable (editable);
        enumeratedField.setEditable (editable);
        defaultTypeField.setEditable (editable);
        defaultValueField.setEditable (editable);
    }    

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        elementNameLabel = new javax.swing.JLabel();
        elementNameField = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        typeField = new javax.swing.JTextField();
        enumeratedLabel = new javax.swing.JLabel();
        enumeratedField = new javax.swing.JTextField();
        defaultTypeLabel = new javax.swing.JLabel();
        defaultTypeField = new javax.swing.JTextField();
        defaultValueLabel = new javax.swing.JLabel();
        defaultValueField = new javax.swing.JTextField();
        
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        elementNameLabel.setText(Util.getString("TreeAttributeDeclCustomizer.elemNameLabel.text"));
        elementNameLabel.setLabelFor(elementNameField);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(elementNameLabel, gridBagConstraints1);
        
        elementNameField.setEditable(false);
        elementNameField.setColumns(23);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.weightx = 1.0;
        add(elementNameField, gridBagConstraints1);
        
        nameLabel.setText(Util.getString("PROP_xmlName"));
        nameLabel.setLabelFor(nameField);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(nameLabel, gridBagConstraints1);
        
        nameField.setColumns(23);
        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFieldActionPerformed(evt);
            }
        });
        
        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                nameFieldFocusLost(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.weightx = 1.0;
        add(nameField, gridBagConstraints1);
        
        typeLabel.setText(Util.getString("PROP_dtdAttDefType"));
        typeLabel.setLabelFor(typeField);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(typeLabel, gridBagConstraints1);
        
        typeField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeFieldActionPerformed(evt);
            }
        });
        
        typeField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                typeFieldFocusLost(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.weightx = 1.0;
        add(typeField, gridBagConstraints1);
        
        enumeratedLabel.setText(Util.getString("DTDAttDefNode.enumeratedLabel.text"));
        enumeratedLabel.setLabelFor(enumeratedField);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(enumeratedLabel, gridBagConstraints1);
        
        enumeratedField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enumeratedFieldActionPerformed(evt);
            }
        });
        
        enumeratedField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                enumeratedFieldFocusLost(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        add(enumeratedField, gridBagConstraints1);
        
        defaultTypeLabel.setText(Util.getString("DTDAttDefNode.defaultTypeLabel.text"));
        defaultTypeLabel.setLabelFor(defaultTypeField);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 4;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(defaultTypeLabel, gridBagConstraints1);
        
        defaultTypeField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultTypeFieldActionPerformed(evt);
            }
        });
        
        defaultTypeField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                defaultTypeFieldFocusLost(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 4;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints1.weightx = 1.0;
        add(defaultTypeField, gridBagConstraints1);
        
        defaultValueLabel.setText(Util.getString("DTDAttDefNode.defaultValueLabel.text"));
        defaultValueLabel.setLabelFor(defaultValueField);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 5;
        gridBagConstraints1.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints1.weighty = 1.0;
        add(defaultValueLabel, gridBagConstraints1);
        
        defaultValueField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultValueFieldActionPerformed(evt);
            }
        });
        
        defaultValueField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                defaultValueFieldFocusLost(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 5;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(defaultValueField, gridBagConstraints1);
        
    }//GEN-END:initComponents

    private void defaultValueFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultValueFieldActionPerformed
        // Add your handling code here:
        updateAttributeDefDefaultValue();
    }//GEN-LAST:event_defaultValueFieldActionPerformed

    private void defaultValueFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_defaultValueFieldFocusLost
        // Add your handling code here:
        updateAttributeDefDefaultValue();
    }//GEN-LAST:event_defaultValueFieldFocusLost

    private void defaultTypeFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_defaultTypeFieldFocusLost
        // Add your handling code here:
        updateAttributeDefDefaultType();
    }//GEN-LAST:event_defaultTypeFieldFocusLost

    private void defaultTypeFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultTypeFieldActionPerformed
        // Add your handling code here:
        updateAttributeDefDefaultType();
    }//GEN-LAST:event_defaultTypeFieldActionPerformed

    private void enumeratedFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enumeratedFieldActionPerformed
        // Add your handling code here:
        updateAttributeDefEnumeratedType();
    }//GEN-LAST:event_enumeratedFieldActionPerformed

    private void enumeratedFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_enumeratedFieldFocusLost
        // Add your handling code here:
        updateAttributeDefEnumeratedType();
    }//GEN-LAST:event_enumeratedFieldFocusLost

    private void typeFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_typeFieldFocusLost
        // Add your handling code here:
        updateAttributeDefType();
    }//GEN-LAST:event_typeFieldFocusLost

    private void typeFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeFieldActionPerformed
        // Add your handling code here:
        updateAttributeDefType();
    }//GEN-LAST:event_typeFieldActionPerformed

    private void nameFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusLost
        // Add your handling code here:
        updateAttributeDefName();
    }//GEN-LAST:event_nameFieldFocusLost

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // Add your handling code here:
        updateAttributeDefName();
    }//GEN-LAST:event_nameFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel elementNameLabel;
    private javax.swing.JTextField elementNameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JTextField typeField;
    private javax.swing.JLabel enumeratedLabel;
    private javax.swing.JTextField enumeratedField;
    private javax.swing.JLabel defaultTypeLabel;
    private javax.swing.JTextField defaultTypeField;
    private javax.swing.JLabel defaultValueLabel;
    private javax.swing.JTextField defaultValueField;
    // End of variables declaration//GEN-END:variables

}

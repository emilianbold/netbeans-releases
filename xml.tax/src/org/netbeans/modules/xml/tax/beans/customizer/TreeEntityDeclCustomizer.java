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

import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;

import org.netbeans.tax.TreeEntityDecl;
import org.netbeans.tax.TreeException;

/**
 *
 * @author  Libor Kramolis, Vladimir Zboril
 * @version 0.1
 */
public class TreeEntityDeclCustomizer extends AbstractTreeCustomizer {

    /** Serial Version UID */
    private static final long serialVersionUID = -4905667144375255810L;


    /** */
    private static final String TYPE_GENERAL   = "General"; // NOI18N
    /** */
    private static final String TYPE_PARAMETER = "Parameter"; // NOI18N
    /** */
    private static final String[] typeItems = { TYPE_GENERAL, TYPE_PARAMETER }; 

    
    
    /** */
    public TreeEntityDeclCustomizer () {
	super();

        initComponents();
        nameLabel.setDisplayedMnemonic (Util.getChar ("LAB_ElementName_mn")); // NOI18N
        typeLabel.setDisplayedMnemonic (Util.getChar ("LAB_EntityType_mn")); // NOI18N
        internalRadio.setMnemonic (Util.getChar ("RAD_Internal_mn")); // NOI18N
        externalRadio.setMnemonic (Util.getChar ("RAD_External_mn")); // NOI18N
        unparsedRadio.setMnemonic (Util.getChar ("RAD_Unparsed_mn")); // NOI18N
    }


    /**
     */
    protected final TreeEntityDecl getEntityDecl () {
        return (TreeEntityDecl)getTreeObject();
    }


    /**
     */
    protected void safePropertyChange (PropertyChangeEvent pche) {
        super.safePropertyChange (pche);
        
        if (pche.getPropertyName().equals (TreeEntityDecl.PROP_PARAMETER)) {
            updateParameterComponent();
        } else if (pche.getPropertyName().equals (TreeEntityDecl.PROP_NAME)) {
            updateNameComponent();
        } else if (pche.getPropertyName().equals (TreeEntityDecl.PROP_INTERNAL_TEXT)) {
            updateInternalTextComponent();
        } else if (pche.getPropertyName().equals (TreeEntityDecl.PROP_PUBLIC_ID)) {
            updatePublicIdComponent();
        } else if (pche.getPropertyName().equals (TreeEntityDecl.PROP_SYSTEM_ID)) {
            updateSystemIdComponent();
        } else if (pche.getPropertyName().equals (TreeEntityDecl.PROP_NOTATION_NAME)) {
            updateNotationComponent();
        } else if (pche.getPropertyName().equals (TreeEntityDecl.PROP_TYPE)) {
            updateTypeComponent();
        }
    }

    /**
     */
    protected final void updateEntityDeclParameter () {
        if ( typeCombo.getSelectedItem() == null ) {
            return;
        }

  	try {
  	    getEntityDecl().setParameter (typeCombo.getSelectedItem() == TYPE_PARAMETER);
  	} catch (TreeException exc) {
	    updateParameterComponent();
  	    Util.notifyTreeException (exc);
  	}
    }
    
    /**
     */
    protected final void updateParameterComponent () {
        if (getEntityDecl().isParameter()) {
            typeCombo.setSelectedItem (TYPE_PARAMETER);
        } else {
            typeCombo.setSelectedItem (TYPE_GENERAL);
        }
    }
    
    /**
     */
    protected final void updateEntityDeclName () {
	try {
	    getEntityDecl().setName (nameField.getText());
	} catch (TreeException exc) {
	    updateNameComponent();
	    Util.notifyTreeException (exc);
	}
    }
    
    /**
     */
    protected final void updateNameComponent () {
        nameField.setText (getEntityDecl().getName());
    }
    

 
    /**
     */
    protected final void updateEntityDeclInternalText () {
  	try {
            getEntityDecl().setInternalText (text2null (internValueField.getText()));
  	} catch (TreeException exc) {
	    updateInternalTextComponent();
  	    Util.notifyTreeException (exc);
  	}
    }
    
    /**
     */
    protected final void updateInternalTextComponent () {
        internValueField.setText (null2text (getEntityDecl().getInternalText()));
    }

    /**
     */
    protected final void updateEntityDeclPublicId () {
        try {
            if ( externalRadio.isSelected() ) {
                getEntityDecl().setPublicId (text2null (externPublicField.getText()));
            } else if ( unparsedRadio.isSelected() ) {
                getEntityDecl().setPublicId (text2null (unparsedPublicField.getText()));
            }
  	} catch (TreeException exc) {
	    updatePublicIdComponent();
  	    Util.notifyTreeException (exc);
  	}
    }
    
    /**
     */
    protected final void updatePublicIdComponent () {
        externPublicField.setText (null2text (getEntityDecl().getPublicId()));
        unparsedPublicField.setText (null2text (getEntityDecl().getPublicId()));
    }

    /**
     */
    protected final void updateEntityDeclSystemId () {
        try {
            if ( externalRadio.isSelected() ) {
                getEntityDecl().setSystemId (text2null (externSystemField.getText()));
            } else if ( unparsedRadio.isSelected() ) {
                getEntityDecl().setSystemId (text2null (unparsedSystemField.getText()));
            }
  	} catch (TreeException exc) {
	    updateSystemIdComponent();
  	    Util.notifyTreeException (exc);
  	}
    }
    
    /**
     */
    protected final void updateSystemIdComponent () {
        externSystemField.setText (null2text (getEntityDecl().getSystemId()));
        unparsedSystemField.setText (null2text (getEntityDecl().getSystemId()));
    }

    
    /**
     */
    protected final void updateEntityDeclNotationName () {
  	try {
            getEntityDecl().setNotationName (text2null (unparsedNotationField.getText()));
  	} catch (TreeException exc) {
	    updateNotationComponent();
  	    Util.notifyTreeException (exc);
  	}
    }
    
    /**
     */
    protected final void updateNotationComponent () {
        unparsedNotationField.setText (null2text (getEntityDecl().getNotationName()));
    }
    
    /**
     */
    protected final void updateTypeComponent () {
        CardLayout cl = (CardLayout)typeCardPanel.getLayout();
        if ( getEntityDecl().getType() == TreeEntityDecl.TYPE_INTERNAL ) {
            internalRadio.setSelected (true);
            cl.show (typeCardPanel, "internalPanel"); // NOI18N
        } else if ( getEntityDecl().getType() == TreeEntityDecl.TYPE_EXTERNAL ) {
            externalRadio.setSelected (true);
            cl.show (typeCardPanel, "externalPanel"); // NOI18N
        } else {
            unparsedRadio.setSelected (true);
            cl.show (typeCardPanel, "unparsedPanel"); // NOI18N
        }
    }
    
  
    /**
     */
    protected final void initComponentValues () {
        updateParameterComponent();
        updateNameComponent();
        updateInternalTextComponent();
        updatePublicIdComponent();
        updateSystemIdComponent();
        updateNotationComponent();
        updateTypeComponent();
    }


    /**
     */
    protected void updateReadOnlyStatus (boolean editable) {
        nameField.setEditable (editable);
        typeCombo.setEnabled (editable);
        internalRadio.setEnabled (editable);
        externalRadio.setEnabled (editable);
        unparsedRadio.setEnabled (editable);
        internValueField.setEditable (editable);
        externPublicField.setEditable (editable);
        externSystemField.setEditable (editable);
        unparsedPublicField.setEditable (editable);
        unparsedSystemField.setEditable (editable);
        unparsedNotationField.setEditable (editable);
    }    


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        buttonGroup = new javax.swing.ButtonGroup();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        typeCombo = new javax.swing.JComboBox(typeItems);
        entityTypePanel = new javax.swing.JPanel();
        entityLabel = new javax.swing.JLabel();
        internalRadio = new javax.swing.JRadioButton();
        externalRadio = new javax.swing.JRadioButton();
        unparsedRadio = new javax.swing.JRadioButton();
        typeCardPanel = new javax.swing.JPanel();
        internalPanel = new javax.swing.JPanel();
        internValueLabel = new javax.swing.JLabel();
        internValueField = new javax.swing.JTextField();
        externalPanel = new javax.swing.JPanel();
        externPublicLabel = new javax.swing.JLabel();
        externPublicField = new javax.swing.JTextField();
        externSystemLabel = new javax.swing.JLabel();
        externSystemField = new javax.swing.JTextField();
        unparsedPanel = new javax.swing.JPanel();
        unparsedPublicLabel = new javax.swing.JLabel();
        unparsedPublicField = new javax.swing.JTextField();
        unparsedSystemLabel = new javax.swing.JLabel();
        unparsedSystemField = new javax.swing.JTextField();
        unparsedNotationLabel = new javax.swing.JLabel();
        unparsedNotationField = new javax.swing.JTextField();
        
        
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        nameLabel.setText(Util.getString("LAB_ElementName"));
        nameLabel.setLabelFor(nameField);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(nameLabel, gridBagConstraints1);
        
        nameField.setColumns(30);
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
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.weightx = 1.0;
        add(nameField, gridBagConstraints1);
        
        typeLabel.setText(Util.getString("LAB_EntityType"));
        typeLabel.setLabelFor(typeCombo);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(typeLabel, gridBagConstraints1);
        
        typeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeComboActionPerformed(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(typeCombo, gridBagConstraints1);
        
        entityTypePanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints2;
        
        entityLabel.setText(Util.getString("LAB_EntityType2"));
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        entityTypePanel.add(entityLabel, gridBagConstraints2);
        
        internalRadio.setSelected(true);
        internalRadio.setText(Util.getString("RAD_Internal"));
        buttonGroup.add(internalRadio);
        internalRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                internalRadioActionPerformed(evt);
            }
        });
        
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        entityTypePanel.add(internalRadio, gridBagConstraints2);
        
        externalRadio.setText(Util.getString("RAD_External"));
        buttonGroup.add(externalRadio);
        externalRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                externalRadioActionPerformed(evt);
            }
        });
        
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 1;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        entityTypePanel.add(externalRadio, gridBagConstraints2);
        
        unparsedRadio.setText(Util.getString("RAD_Unparsed"));
        buttonGroup.add(unparsedRadio);
        unparsedRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unparsedRadioActionPerformed(evt);
            }
        });
        
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 2;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weightx = 1.0;
        entityTypePanel.add(unparsedRadio, gridBagConstraints2);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        add(entityTypePanel, gridBagConstraints1);
        
        typeCardPanel.setLayout(new java.awt.CardLayout());
        
        internalPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints3;
        
        internValueLabel.setText(Util.getString("LAB_Internal_Text"));
        gridBagConstraints3 = new java.awt.GridBagConstraints();
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.gridy = 0;
        gridBagConstraints3.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints3.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints3.weighty = 1.0;
        internalPanel.add(internValueLabel, gridBagConstraints3);
        
        internValueField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                internValueFieldActionPerformed(evt);
            }
        });
        
        internValueField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                internValueFieldFocusLost(evt);
            }
        });
        
        gridBagConstraints3 = new java.awt.GridBagConstraints();
        gridBagConstraints3.gridx = 1;
        gridBagConstraints3.gridy = 0;
        gridBagConstraints3.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints3.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints3.weightx = 1.0;
        internalPanel.add(internValueField, gridBagConstraints3);
        
        typeCardPanel.add(internalPanel, "internalPanel");
        
        externalPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints4;
        
        externPublicLabel.setText(Util.getString("LAB_External_PublicId"));
        gridBagConstraints4 = new java.awt.GridBagConstraints();
        gridBagConstraints4.gridx = 0;
        gridBagConstraints4.gridy = 0;
        gridBagConstraints4.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
        externalPanel.add(externPublicLabel, gridBagConstraints4);
        
        externPublicField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                externPublicFieldActionPerformed(evt);
            }
        });
        
        externPublicField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                externPublicFieldFocusLost(evt);
            }
        });
        
        gridBagConstraints4 = new java.awt.GridBagConstraints();
        gridBagConstraints4.gridx = 1;
        gridBagConstraints4.gridy = 0;
        gridBagConstraints4.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints4.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints4.weightx = 1.0;
        externalPanel.add(externPublicField, gridBagConstraints4);
        
        externSystemLabel.setText(Util.getString("LAB_External_SystemId"));
        gridBagConstraints4 = new java.awt.GridBagConstraints();
        gridBagConstraints4.gridx = 0;
        gridBagConstraints4.gridy = 1;
        gridBagConstraints4.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints4.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints4.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints4.weighty = 1.0;
        externalPanel.add(externSystemLabel, gridBagConstraints4);
        
        externSystemField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                externSystemFieldActionPerformed(evt);
            }
        });
        
        externSystemField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                externSystemFieldFocusLost(evt);
            }
        });
        
        gridBagConstraints4 = new java.awt.GridBagConstraints();
        gridBagConstraints4.gridx = 1;
        gridBagConstraints4.gridy = 1;
        gridBagConstraints4.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints4.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints4.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints4.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints4.weightx = 1.0;
        externalPanel.add(externSystemField, gridBagConstraints4);
        
        typeCardPanel.add(externalPanel, "externalPanel");
        
        unparsedPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints5;
        
        unparsedPublicLabel.setText(Util.getString("LAB_Unparsed_PublicId"));
        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 0;
        gridBagConstraints5.gridy = 0;
        gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints5.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
        unparsedPanel.add(unparsedPublicLabel, gridBagConstraints5);
        
        unparsedPublicField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unparsedPublicFieldActionPerformed(evt);
            }
        });
        
        unparsedPublicField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                unparsedPublicFieldFocusLost(evt);
            }
        });
        
        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 1;
        gridBagConstraints5.gridy = 0;
        gridBagConstraints5.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints5.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints5.weightx = 1.0;
        unparsedPanel.add(unparsedPublicField, gridBagConstraints5);
        
        unparsedSystemLabel.setText(Util.getString("LAB_Unparsed_SystemId"));
        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 0;
        gridBagConstraints5.gridy = 1;
        gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints5.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
        unparsedPanel.add(unparsedSystemLabel, gridBagConstraints5);
        
        unparsedSystemField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unparsedSystemFieldActionPerformed(evt);
            }
        });
        
        unparsedSystemField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                unparsedSystemFieldFocusLost(evt);
            }
        });
        
        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 1;
        gridBagConstraints5.gridy = 1;
        gridBagConstraints5.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints5.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints5.weightx = 1.0;
        unparsedPanel.add(unparsedSystemField, gridBagConstraints5);
        
        unparsedNotationLabel.setText(Util.getString("LAB_Unparsed_NotationName"));
        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 0;
        gridBagConstraints5.gridy = 2;
        gridBagConstraints5.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints5.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints5.weighty = 1.0;
        unparsedPanel.add(unparsedNotationLabel, gridBagConstraints5);
        
        unparsedNotationField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unparsedNotationFieldActionPerformed(evt);
            }
        });
        
        unparsedNotationField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                unparsedNotationFieldFocusLost(evt);
            }
        });
        
        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 1;
        gridBagConstraints5.gridy = 2;
        gridBagConstraints5.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints5.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints5.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints5.weightx = 1.0;
        unparsedPanel.add(unparsedNotationField, gridBagConstraints5);
        
        typeCardPanel.add(unparsedPanel, "unparsedPanel");
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(typeCardPanel, gridBagConstraints1);
        
    }//GEN-END:initComponents

    private void unparsedNotationFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_unparsedNotationFieldFocusLost
        // Add your handling code here:
        updateEntityDeclNotationName();
    }//GEN-LAST:event_unparsedNotationFieldFocusLost

    private void unparsedNotationFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unparsedNotationFieldActionPerformed
        // Add your handling code here:
        updateEntityDeclNotationName();
    }//GEN-LAST:event_unparsedNotationFieldActionPerformed

    private void unparsedSystemFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_unparsedSystemFieldFocusLost
        // Add your handling code here:
        updateEntityDeclSystemId();
    }//GEN-LAST:event_unparsedSystemFieldFocusLost

    private void unparsedSystemFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unparsedSystemFieldActionPerformed
        // Add your handling code here:
        updateEntityDeclSystemId();
    }//GEN-LAST:event_unparsedSystemFieldActionPerformed

    private void unparsedPublicFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_unparsedPublicFieldFocusLost
        // Add your handling code here:
        updateEntityDeclPublicId();
    }//GEN-LAST:event_unparsedPublicFieldFocusLost

    private void unparsedPublicFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unparsedPublicFieldActionPerformed
        // Add your handling code here:
        updateEntityDeclPublicId();
    }//GEN-LAST:event_unparsedPublicFieldActionPerformed

    private void externSystemFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_externSystemFieldFocusLost
        // Add your handling code here:
        updateEntityDeclSystemId();
    }//GEN-LAST:event_externSystemFieldFocusLost

    private void externSystemFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_externSystemFieldActionPerformed
        // Add your handling code here:
        updateEntityDeclSystemId();
    }//GEN-LAST:event_externSystemFieldActionPerformed

    private void externPublicFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_externPublicFieldActionPerformed
        // Add your handling code here:
        updateEntityDeclPublicId();
    }//GEN-LAST:event_externPublicFieldActionPerformed

    private void externPublicFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_externPublicFieldFocusLost
        // Add your handling code here:
        updateEntityDeclPublicId();
    }//GEN-LAST:event_externPublicFieldFocusLost

    private void internValueFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_internValueFieldFocusLost
        // Add your handling code here:
        updateEntityDeclInternalText();
    }//GEN-LAST:event_internValueFieldFocusLost

    private void internValueFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_internValueFieldActionPerformed
        // Add your handling code here:
        updateEntityDeclInternalText();
    }//GEN-LAST:event_internValueFieldActionPerformed

    private void nameFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusLost
        // Add your handling code here:
        updateEntityDeclName();
    }//GEN-LAST:event_nameFieldFocusLost

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // Add your handling code here:
        updateEntityDeclName();
    }//GEN-LAST:event_nameFieldActionPerformed

    private void unparsedRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unparsedRadioActionPerformed
        // Add your handling code here:
        CardLayout cl = (CardLayout)typeCardPanel.getLayout();
        cl.show (typeCardPanel, "unparsedPanel"); // NOI18N
    }//GEN-LAST:event_unparsedRadioActionPerformed

    private void externalRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_externalRadioActionPerformed
        // Add your handling code here:
        CardLayout cl = (CardLayout)typeCardPanel.getLayout();
        cl.show (typeCardPanel, "externalPanel"); // NOI18N
    }//GEN-LAST:event_externalRadioActionPerformed

    private void internalRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_internalRadioActionPerformed
        // Add your handling code here:
        CardLayout cl = (CardLayout)typeCardPanel.getLayout();
        cl.show (typeCardPanel, "internalPanel"); // NOI18N
    }//GEN-LAST:event_internalRadioActionPerformed

    private void typeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeComboActionPerformed
        unparsedRadio.setEnabled (typeCombo.getSelectedIndex() != 1);
        if (unparsedRadio.isSelected()) {
            internalRadio.setSelected (true);
            internalRadioActionPerformed (evt);
        }
        updateEntityDeclParameter();
    }//GEN-LAST:event_typeComboActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JComboBox typeCombo;
    private javax.swing.JPanel entityTypePanel;
    private javax.swing.JLabel entityLabel;
    private javax.swing.JRadioButton internalRadio;
    private javax.swing.JRadioButton externalRadio;
    private javax.swing.JRadioButton unparsedRadio;
    private javax.swing.JPanel typeCardPanel;
    private javax.swing.JPanel internalPanel;
    private javax.swing.JLabel internValueLabel;
    private javax.swing.JTextField internValueField;
    private javax.swing.JPanel externalPanel;
    private javax.swing.JLabel externPublicLabel;
    private javax.swing.JTextField externPublicField;
    private javax.swing.JLabel externSystemLabel;
    private javax.swing.JTextField externSystemField;
    private javax.swing.JPanel unparsedPanel;
    private javax.swing.JLabel unparsedPublicLabel;
    private javax.swing.JTextField unparsedPublicField;
    private javax.swing.JLabel unparsedSystemLabel;
    private javax.swing.JTextField unparsedSystemField;
    private javax.swing.JLabel unparsedNotationLabel;
    private javax.swing.JTextField unparsedNotationField;
    // End of variables declaration//GEN-END:variables
    
}

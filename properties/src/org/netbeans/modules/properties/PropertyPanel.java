/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.properties;


import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.*;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.DialogDescriptor;


/**
 * Panel for customizing <code>Element.ItemElem</code> element.
 *
 * @author  Peter Zavadsky
 * @see Element.ItemElem
 */
public class PropertyPanel extends JPanel {
    
    /** Element to customize. */
    private Element.ItemElem element;
    

    /** Creates new <code>PropertyPanel</code>. 
     * @param element element to customize */
    public PropertyPanel(Element.ItemElem element) {
        this.element = element;
        
        initComponents();
        initAccessibility();             
                
        keyText.setText(UtilConvert.unicodesToChars(element.getKey()));
        valueText.setText(UtilConvert.unicodesToChars(element.getValue()));
        commentText.setText(UtilConvert.unicodesToChars(element.getComment()));

        // Unregister Enter on text fields so default button could work.
        keyText.getKeymap().removeKeyStrokeBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        valueText.getKeymap().removeKeyStrokeBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        commentText.getKeymap().removeKeyStrokeBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        
        HelpCtx.setHelpIDString(this, Util.HELP_ID_ADDING);
    }

    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(PropertyPanel.class).getString("ACS_PropertyPanel"));                
        
        keyLabel.setLabelFor(keyText);
        valueLabel.setLabelFor(valueText);
        commentLabel.setLabelFor(commentText);
        
        keyText.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(PropertyPanel.class).getString("ACS_PropertyPanel"));                
        valueText.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(PropertyPanel.class).getString("ACS_PropertyPanel"));                
        commentText.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(PropertyPanel.class).getString("ACS_PropertyPanel"));                
        
        keyLabel.setDisplayedMnemonic((NbBundle.getBundle(PropertyPanel.class).getString("LBL_KeyLabel_Mnem")).charAt(0));
        valueLabel.setDisplayedMnemonic((NbBundle.getBundle(PropertyPanel.class).getString("LBL_ValueLabel_Mnem")).charAt(0));
        commentLabel.setDisplayedMnemonic((NbBundle.getBundle(PropertyPanel.class).getString("LBL_CommentLabel_Mnem")).charAt(0));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        keyLabel = new javax.swing.JLabel();
        keyText = new JTextField(25);
        valueLabel = new javax.swing.JLabel();
        valueText = new JTextField(25);
        commentLabel = new javax.swing.JLabel();
        commentText = new JTextField(25);

        setLayout(new java.awt.GridBagLayout());

        keyLabel.setText(NbBundle.getBundle(PropertyPanel.class).getString("LBL_KeyLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(keyLabel, gridBagConstraints);

        keyText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyTextActionPerformed(evt);
            }
        });

        keyText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                keyTextFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 7, 0, 11);
        add(keyText, gridBagConstraints);

        valueLabel.setText(NbBundle.getBundle(PropertyPanel.class).getString("LBL_ValueLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(valueLabel, gridBagConstraints);

        valueText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueTextActionPerformed(evt);
            }
        });

        valueText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                valueTextFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 0, 11);
        add(valueText, gridBagConstraints);

        commentLabel.setText(NbBundle.getBundle(PropertyPanel.class).getString("LBL_CommentLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 11, 0);
        add(commentLabel, gridBagConstraints);

        commentText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commentTextActionPerformed(evt);
            }
        });

        commentText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                commentTextFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 11, 11);
        add(commentText, gridBagConstraints);

    }//GEN-END:initComponents

    private void valueTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_valueTextFocusLost
        valueTextHandler();
    }//GEN-LAST:event_valueTextFocusLost

    private void workaround11364(ActionEvent evt) {
        JRootPane root = getRootPane();
        if (root != null) {
            JButton defaultButton = root.getDefaultButton();
            if (defaultButton != null) {
                defaultButton.doClick();
            }
        }
    }

    private void valueTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valueTextActionPerformed
        valueTextHandler();
        workaround11364(evt);
    }//GEN-LAST:event_valueTextActionPerformed

    
    /** Value text field event handler. */
    private void valueTextHandler() {
        element.getValueElem().setValue(
            UtilConvert.charsToUnicodes(
                UtilConvert.escapeJavaSpecialChars(
                    UtilConvert.escapeLineContinuationChar(
                        UtilConvert.escapeOutsideSpaces(valueText.getText())
                    )
                )
            )
        );
    }
    
    private void keyTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyTextFocusLost
        keyTextHandler();
    }//GEN-LAST:event_keyTextFocusLost

    private void keyTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyTextActionPerformed
        keyTextHandler();
        workaround11364(evt);
    }//GEN-LAST:event_keyTextActionPerformed

    /** Key text field event handler. */
    private void keyTextHandler() {
        element.getKeyElem().setValue(UtilConvert.charsToUnicodes(UtilConvert.escapeJavaSpecialChars(UtilConvert.escapePropertiesSpecialChars(keyText.getText()))));
    }
    
    private void commentTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_commentTextFocusLost
        commentTextHandler();
    }//GEN-LAST:event_commentTextFocusLost

    private void commentTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commentTextActionPerformed
        commentTextHandler();
        workaround11364(evt);
    }//GEN-LAST:event_commentTextActionPerformed

    /** Comment text field event handler. */
    private void commentTextHandler() {
        element.getCommentElem().setValue(UtilConvert.charsToUnicodes(commentText.getText()));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel keyLabel;
    private javax.swing.JTextField keyText;
    private javax.swing.JTextField valueText;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JTextField commentText;
    private javax.swing.JLabel commentLabel;
    // End of variables declaration//GEN-END:variables

}

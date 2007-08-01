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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * AddMIDletPanel.java
 *
 * Created on 16. duben 2004, 9:43
 */
package org.netbeans.modules.mobility.project.ui.customizer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Adam Sotona
 */
public class AddMIDletPanel extends JPanel implements DocumentListener, ActionListener {
    
    private boolean fillName;
    private DialogDescriptor dd;
    
    /** Creates new form AddMIDletPanel */
    public AddMIDletPanel(String name, String clazz, String icon, DefaultComboBoxModel classes, DefaultComboBoxModel icons) {
        initComponents();
        initAccessibility();
        jComboBoxClass.setModel(classes);
        jComboBoxIcon.setModel(icons);
        this.fillName = name == null;
        if (fillName) {
            if (clazz != null) jTextFieldName.setText(clazz.substring(clazz.lastIndexOf('.')+1));
        } else {
            jTextFieldName.setText(name);
        }
        jComboBoxClass.setSelectedItem(clazz);
        jComboBoxIcon.setSelectedItem(icon);
        jTextFieldName.getDocument().addDocumentListener(this);
        jComboBoxClass.addActionListener(this);
        jComboBoxIcon.addActionListener(this);
        Component editor = jComboBoxClass.getEditor().getEditorComponent();
        if (editor instanceof JTextComponent) ((JTextComponent)editor).getDocument().addDocumentListener(this);
        editor = jComboBoxIcon.getEditor().getEditorComponent();
        if (editor instanceof JTextComponent) ((JTextComponent)editor).getDocument().addDocumentListener(this);
    }
    
    protected void setDialogDescriptor(final DialogDescriptor desc) {
        this.dd = desc;
        dd.setValid(isValid());
    }
    
    public String getName() {
        return jTextFieldName.getText().trim();
    }
    
    public String getClazz() {
        final String n = (String)jComboBoxClass.getEditor().getItem();
        return n == null ? "" : n.trim(); //NOI18N
    }
    
    public String getIcon() {
        final String i = (String)jComboBoxIcon.getEditor().getItem();
        return i == null ? "" : i.trim(); //NOI18N
    }
    
    private boolean isValidClassName(final String s) {
        if (s.startsWith(".") || s.endsWith(".")) return false; //NOI18N
        final StringTokenizer stk = new StringTokenizer(s, "."); //NOI18N
        while (stk.hasMoreTokens()) if (!Utilities.isJavaIdentifier(stk.nextToken())) return false;
        return true;
    }
    
    public boolean isValid() {
        if (getName().length() == 0 || getName().indexOf(',') >= 0) {
            errorPanel.setErrorBundleMessage("ERR_AddMID_InvalidName"); //NOI18N
            return false;
        }
        if (getClazz().length() == 0 || !isValidClassName(getClazz())) {
            errorPanel.setErrorBundleMessage("ERR_AddMID_InvalidClass"); //NOI18N
            return false;
        }
        if (getIcon().indexOf(',') >= 0) {
            errorPanel.setErrorBundleMessage("ERR_AddMID_InvalidIcon"); //NOI18N
            return false;
        }
        errorPanel.setErrorBundleMessage(null);
        return true;
    }
    
    public void actionPerformed(@SuppressWarnings("unused")
	final ActionEvent e) {
        if (fillName) {
            final String clazz = getClazz();
            jTextFieldName.setText(clazz.substring(clazz.lastIndexOf('.')+1));
        }
        if (dd != null) dd.setValid(isValid());
    }
    
    public void changedUpdate(@SuppressWarnings("unused")
	final DocumentEvent e) {
        fillName = false;
        if (dd != null) dd.setValid(isValid());
    }
    
    public void insertUpdate(final DocumentEvent e) {
        changedUpdate(e);
    }
    
    public void removeUpdate(final DocumentEvent e) {
        changedUpdate(e);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelName = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();
        jLabelClass = new javax.swing.JLabel();
        jComboBoxClass = new javax.swing.JComboBox();
        jLabelIcon = new javax.swing.JLabel();
        jComboBoxIcon = new javax.swing.JComboBox();
        errorPanel = new org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabelName.setLabelFor(jTextFieldName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelName, NbBundle.getMessage(AddMIDletPanel.class, "LBL_AddMIDlet_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabelName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 12);
        add(jTextFieldName, gridBagConstraints);
        jTextFieldName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddMIDletPanel.class, "ACSD_AddMIDlet_Name")); // NOI18N

        jLabelClass.setLabelFor(jComboBoxClass);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelClass, NbBundle.getMessage(AddMIDletPanel.class, "LBL_AddMIDlet_Class")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabelClass, gridBagConstraints);

        jComboBoxClass.setEditable(true);
        jComboBoxClass.setPreferredSize(new java.awt.Dimension(250, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 12);
        add(jComboBoxClass, gridBagConstraints);
        jComboBoxClass.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddMIDletPanel.class, "ACSD_AddMIDlet_Class")); // NOI18N

        jLabelIcon.setLabelFor(jComboBoxIcon);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelIcon, NbBundle.getMessage(AddMIDletPanel.class, "LBL_AddMIDlet_Icon")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabelIcon, gridBagConstraints);

        jComboBoxIcon.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 12);
        add(jComboBoxIcon, gridBagConstraints);
        jComboBoxIcon.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddMIDletPanel.class, "ACSD_AddMIDlet_Icon")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(errorPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddMIDletPanel.class, "ACSN_AddMIDlet"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddMIDletPanel.class, "ACSD_AddMIDlet"));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel errorPanel;
    private javax.swing.JComboBox jComboBoxClass;
    private javax.swing.JComboBox jComboBoxIcon;
    private javax.swing.JLabel jLabelClass;
    private javax.swing.JLabel jLabelIcon;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JTextField jTextFieldName;
    // End of variables declaration//GEN-END:variables
    
}

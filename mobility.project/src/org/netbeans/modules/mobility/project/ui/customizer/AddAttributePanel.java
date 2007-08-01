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
 * AddAttributePanel.java
 *
 * Created on April 22, 2004, 5:05 PM
 */
package org.netbeans.modules.mobility.project.ui.customizer;

import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  David Kaspar
 */
public class AddAttributePanel extends javax.swing.JPanel implements ActionListener, DocumentListener {
    
    private DialogDescriptor dd;
    private boolean editing;
    private HashSet<String> keys;
    private boolean mandatory;
    private String originalKey;
    private CustomizerJad.StorableTableModel tableModel;
    final private HashSet<String> bannedNames;
    final private HashSet<String> riskyNames;
    private HashSet<String> allNames;
    
    /** Creates new form AddAttributePanel */
    public AddAttributePanel() {
        initComponents();
        initAccessibility();
        cPlacement.setModel(new DefaultComboBoxModel(new String[] {
            NbBundle.getMessage(AddAttributePanel.class, "LBL_Attr_Placement_Both"), //NOI18N
            NbBundle.getMessage(AddAttributePanel.class, "LBL_Attr_Placement_JAD"), //NOI18N
            NbBundle.getMessage(AddAttributePanel.class, "LBL_Attr_Placement_Manifest") //NOI18N
        }));
        bannedNames = new HashSet<String>();
        bannedNames.add("MIDlet-Jar-RSA-SHA1"); // NOI18N
        bannedNames.add("MIDlet-Jar-Size"); // NOI18N
        bannedNames.add("MIDlet-Jar-URL"); // NOI18N
        bannedNames.add("MIDlet-Permissions"); // NOI18N
        bannedNames.add("MIDlet-Permissions-Opt"); // NOI18N
        riskyNames = new HashSet<String>();
        riskyNames.add("MicroEdition-Configuration"); // NOI18N
        riskyNames.add("MicroEdition-Profile"); // NOI18N
    }
    
    public void init(final boolean editing, final CustomizerJad.StorableTableModel tableModel, final HashSet<String> keys, final String key, final String value) {
        this.keys = keys;
        this.editing = editing;
        this.tableModel = tableModel;
        this.allNames = new HashSet<String>(Arrays.asList(tableModel.getAllAttrs()));
        if (!tableModel.isMIDP2()) {
            riskyNames.add("MIDlet-Install-Notify"); //NOI18N
            riskyNames.add("MIDlet-Delete-Notify"); //NOI18N
            riskyNames.add("MIDlet-Delete-Confirm"); //NOI18N
        }
        originalKey = key;
        jLabel4.setVisible("MIDlet-Version".equals(key)); //NOI18N
        if (editing) {
            cKey.setModel(new DefaultComboBoxModel(tableModel.getNonMandatory()));
            cKey.getEditor().setItem(key);
            tValue.setText(value);
            mandatory = tableModel.containsInMandatory(key);
            cKey.setEnabled(!mandatory);
        } else {
            final Vector<String> datas = new Vector<String>();
            final String[] attrs = tableModel.getAllAttrs();
            for (int a = 0; a < attrs.length; a ++)
                if (! keys.contains(attrs[a]))
                    datas.add(attrs[a]);
            cKey.setModel(new DefaultComboBoxModel(datas));
            tValue.setText(""); //NOI18N
            mandatory = false;
            cKey.setEnabled(true);
        }
        final Object comp = cKey.getEditor().getEditorComponent();
        if (comp instanceof JTextComponent)
            ((JTextComponent) comp).getDocument().addDocumentListener(this);
        tValue.getDocument().addDocumentListener(this);
        isValid();
        final Boolean b = tableModel.getPlacement(key);
        cPlacement.setSelectedIndex(b == null ? 0 : (b.booleanValue() ? 1 : 2));
    }
    
    public String getKey() {
        return cKey.getEditor().getItem().toString();
    }
    
    public String getValue() {
        return tValue.getText();
    }
    
    public Boolean getPlacement() {
        final int i = cPlacement.getSelectedIndex();
        return i == 0 ? null : (i == 1 ? Boolean.TRUE : Boolean.FALSE);
    }
    
    protected void setDialogDescriptor(final DialogDescriptor desc) {
        this.dd = desc;
        dd.setHelpCtx(new HelpCtx(AddAttributePanel.class));
        actionPerformed(null);
    }
    
    public boolean isValid() {
        if (allNames.contains(getKey())) {
            cPlacement.setEnabled(false);
            cPlacement.setSelectedIndex(0);
        } else {
            cPlacement.setEnabled(true);
        }
        errorPanel.setErrorBundleMessage(null);
        if (getKey() == null  ||  "".equals(getKey())) { //NOI18N
            errorPanel.setErrorBundleMessage("ERR_AddAttr_InvAttName"); //NOI18N
            return false;
        }
        if (bannedNames.contains(getKey())) {
            errorPanel.setErrorBundleMessage("ERR_AddAttr_" + getKey()); // NOI18N
            return false;
        }
        if (riskyNames.contains(getKey())) {
            errorPanel.setErrorBundleMessage("WARN_AddAttr_" + getKey()); // NOI18N
        }
        if (getKey().startsWith("MIDlet-Certificate-")) { // NOI18N
            errorPanel.setErrorBundleMessage("ERR_AddAttr_Certificate"); // NOI18N
            return false;
        }
        if (getKey().startsWith("MIDlet-Push-")) { // NOI18N
            errorPanel.setErrorBundleMessage("ERR_AddAttr_Push"); // NOI18N
            return false;
        }
        if (editing) {
            if (mandatory) {
                if (getValue() == null  ||  "".equals(getValue())) { //NOI18N
                    errorPanel.setErrorBundleMessage("ERR_AddAttr_EmptyMandAttVal"); //NOI18N
                    return false;
                }
            }
        } else if (tableModel.containsInMandatory(getKey()))
                if (getValue() == null  ||  "".equals(getValue())) { //NOI18N
                errorPanel.setErrorBundleMessage("ERR_AddAttr_EmptyMandAttVal"); //NOI18N
                return false;
        }
        if (!getKey().equals(originalKey)  && keys.contains(getKey())) {
            errorPanel.setErrorBundleMessage("ERR_AddAttr_AttNameExists"); //NOI18N
            return false;
        }
        if (getKey().startsWith("MIDlet-")) { // NOI18N
            final String tmp = getKey().substring("MIDlet-".length()); // NOI18N
            try {
                Integer.parseInt(tmp);
                errorPanel.setErrorBundleMessage("ERR_AddAttr_MIDlet"); // NOI18N
                return false;
            } catch (NumberFormatException e) {
                if (!tableModel.isAcceptable(getKey())) {
                    errorPanel.setErrorBundleMessage("WARN_AddAttr_MIDlet"); // NOI18N
                    return true;
                }
            }
        }
//        if (!tableModel.isAcceptable(getKey())) {
//            errorPanel.setErrorBundleMessage("ERR_AddAttr_InvAttName"); //NOI18N
//            return false;
//        }
        return true;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        cKey = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        tValue = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        cPlacement = new javax.swing.JComboBox();
        errorPanel = new org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel();

        setPreferredSize(new java.awt.Dimension(500, 150));
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(cKey);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AddAttributePanel.class, "LBL_Attr_Key")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 5);
        add(jLabel1, gridBagConstraints);

        cKey.setEditable(true);
        cKey.setPreferredSize(new java.awt.Dimension(300, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        add(cKey, gridBagConstraints);

        jLabel2.setLabelFor(tValue);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(AddAttributePanel.class, "LBL_Attr_Value")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 5);
        add(jLabel2, gridBagConstraints);

        tValue.setPreferredSize(new java.awt.Dimension(300, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        add(tValue, gridBagConstraints);

        jLabel4.setLabelFor(tValue);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, NbBundle.getMessage(AddAttributePanel.class, "LBL_Attr_Expl")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddAttributePanel.class, "ACSD_AddAttribute_Hint")); // NOI18N

        jLabel3.setLabelFor(cPlacement);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, NbBundle.getMessage(AddAttributePanel.class, "LBL_Attr_Placement")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 5);
        add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        add(cPlacement, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(errorPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddAttributePanel.class, "ACSN_AddAttribute"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddAttributePanel.class, "ACSD_AddAttribute"));
    }
    
    public void actionPerformed(@SuppressWarnings("unused")
	final java.awt.event.ActionEvent e) {
        dd.setValid(isValid());
    }
    
    public void changedUpdate(@SuppressWarnings("unused")
	final javax.swing.event.DocumentEvent e) {
        actionPerformed(null);
    }
    
    public void insertUpdate(@SuppressWarnings("unused")
	final javax.swing.event.DocumentEvent e) {
        actionPerformed(null);
    }
    
    public void removeUpdate(@SuppressWarnings("unused")
	final javax.swing.event.DocumentEvent e) {
        actionPerformed(null);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cKey;
    private javax.swing.JComboBox cPlacement;
    private org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel errorPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField tValue;
    // End of variables declaration//GEN-END:variables
    
}

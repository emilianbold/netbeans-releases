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

package org.netbeans.modules.project.libraries.ui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.modules.project.libraries.LibraryTypeRegistry;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;



/**
 *
 * @author  tom
 */
public class NewLibraryPanel extends javax.swing.JPanel {

    private LibrariesModel model;
    private Map<Integer,String> typeMap;

    private DialogDescriptor dd;

    private static final Pattern VALID_LIBRARY_NAME = Pattern.compile("[-._a-zA-Z0-9]+"); // NOI18N

    /** Creates new form NewLibraryPanel */
    public NewLibraryPanel (LibrariesModel model, String preselectedLibraryType) {
        this.model = model;
        initComponents();
        this.name.setColumns(25);
        this.name.getDocument().addDocumentListener(new javax.swing.event.DocumentListener () {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                nameChanged();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                nameChanged();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                nameChanged();
            }

        });
        this.initModel (preselectedLibraryType);
        Color c = javax.swing.UIManager.getColor("nb.errorForeground"); //NOI18N
        if (c == null) {
            c = new Color(89,79,191);  // RGB suggested by Bruce in #28466
        }
        status.setForeground(c);
    }
    
    void setDialogDescriptor(DialogDescriptor dd) {
        this.dd = dd;
    }
    
    public String getLibraryType () {
        return typeMap.get(libraryType.getSelectedIndex());
    }
    
    public String getLibraryName () {
        return this.name.getText();
    }

    public void addNotify() {
        super.addNotify();
        this.name.selectAll();
    }


    private void initModel (String preselectedLibraryType) {
        this.typeMap = new HashMap<Integer,String>();
        this.name.setText (NbBundle.getMessage (NewLibraryPanel.class,"TXT_NewLibrary"));
        LibraryTypeRegistry regs = LibraryTypeRegistry.getDefault();
        LibraryTypeProvider[] providers = regs.getLibraryTypeProviders();
        int index = 0;
        for (int i=0; i< providers.length; i++) {
            String type = providers[i].getLibraryType();
            if (type.equals(preselectedLibraryType)) {
                index = i;
            }
            typeMap.put(i ,type);
            String displayName = providers[i].getDisplayName();
            if (displayName == null) {
                displayName = providers[i].getLibraryType();
            }            
            this.libraryType.addItem (displayName);
        }
        if (this.libraryType.getItemCount() > 0) {
            this.libraryType.setSelectedIndex(index);
        }
    }


    private void nameChanged () {
        String name = this.name.getText();
        boolean valid = false;
        String message;
        if (name.length() == 0) {
            message = NbBundle.getMessage(NewLibraryPanel.class,"ERR_InvalidName");
        }
        else {
            valid = LibrariesCustomizer.isValidName (model, name);
            if (valid) {
                if (isReasonableAntProperty(name)) {
                    message = " ";   //NOI18N
                } else {
                    valid = false;
                    message = NbBundle.getMessage(NewLibraryPanel.class,"ERR_InvalidCharacters");
                }
            }
            else {
                message = NbBundle.getMessage(NewLibraryPanel.class, "ERR_ExistingName", name);
            }
        }
        if (dd != null) {
            dd.setValid(valid);
        }
        this.status.setText(message);
    }
    
    private boolean isReasonableAntProperty(String name) {
        // XXX: there is method in PropertyUtils.isUsablePropertyName()
        // which should be used here but that would create dependency
        // on ant/project modules which is not desirable.
        // XXX: The restriction of display name should be fixed in promo F
        return VALID_LIBRARY_NAME.matcher(name).matches();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel2 = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        libraryType = new javax.swing.JComboBox();
        status = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLabel2.setLabelFor(name);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(NewLibraryPanel.class, "CTL_LibraryName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 6);
        add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 6, 12);
        add(name, gridBagConstraints);
        name.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewLibraryPanel.class, "AD_LibraryName")); // NOI18N

        jLabel1.setLabelFor(libraryType);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(NewLibraryPanel.class, "CTL_LibraryType")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 6);
        add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 12);
        add(libraryType, gridBagConstraints);
        libraryType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewLibraryPanel.class, "AD_LibraryType")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 12, 12);
        add(status, gridBagConstraints);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle"); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("AD_NewLibraryPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox libraryType;
    private javax.swing.JTextField name;
    private javax.swing.JLabel status;
    // End of variables declaration//GEN-END:variables
    
}

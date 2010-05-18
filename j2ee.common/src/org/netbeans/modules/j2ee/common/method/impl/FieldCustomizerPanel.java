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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.common.method.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.common.method.MethodModel;

/** 
 * Customizer for FieldElement
 *
 * @author Petr Hamernik
 * @author Martin Adamek
 */
public class FieldCustomizerPanel extends JPanel {

    public static final String NAME = "name";
    public static final String RETURN_TYPE = "returnType";
    public static final String INTERFACES = "interfaces";

    /**
     * Create new FieldCustomizer component
     *
     * @param element      The field to be customized
     * @param description
     * @param localGetter
     * @param localSetter
     * @param remoteGetter
     * @param remoteSetter
     */
    public FieldCustomizerPanel(MethodModel.Variable element, String description, boolean localEnabled, boolean remoteEnabled,
            boolean localGetter, boolean localSetter, boolean remoteGetter, boolean remoteSetter) {
        initComponents();

        descriptionTextField.setText(description);
        localGetterCheckBox.setEnabled(localEnabled);
        localSetterCheckBox.setEnabled(localEnabled);
        remoteGetterCheckBox.setEnabled(remoteEnabled);
        remoteSetterCheckBox.setEnabled(remoteEnabled);
        localGetterCheckBox.setSelected(localGetter && localEnabled);
        localSetterCheckBox.setSelected(localSetter && localEnabled);
        remoteGetterCheckBox.setSelected(remoteGetter && remoteEnabled);
        remoteSetterCheckBox.setSelected(remoteSetter && remoteEnabled);
        nameTextField.setText(element.getName());
        typeTextField.setText(element.getType());
        
        nameTextField.getDocument().addDocumentListener(new SimpleListener(NAME));
        typeTextField.getDocument().addDocumentListener(new SimpleListener(RETURN_TYPE));
        SimpleListener interfacesListener = new SimpleListener(INTERFACES);
        localGetterCheckBox.addActionListener(interfacesListener);
        localSetterCheckBox.addActionListener(interfacesListener);
        remoteGetterCheckBox.addActionListener(interfacesListener);
        remoteSetterCheckBox.addActionListener(interfacesListener);
    }

    public void addNotify() {
        super.addNotify();

        // select the name
        int nameLength = nameTextField.getText().length();
        nameTextField.setCaretPosition(0);
        nameTextField.moveCaretPosition(nameLength);
        nameTextField.requestFocus();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nameTextField.requestFocus();
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        descriptionTextField = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        addToLocalLabel = new javax.swing.JLabel();
        localGetterCheckBox = new javax.swing.JCheckBox();
        localSetterCheckBox = new javax.swing.JCheckBox();
        addToRemoteLabel = new javax.swing.JLabel();
        remoteGetterCheckBox = new javax.swing.JCheckBox();
        remoteSetterCheckBox = new javax.swing.JCheckBox();
        typeTextField = new javax.swing.JTextField();
        errorTextField = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));

        nameLabel.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(FieldCustomizerPanel.class, "CTL_Name_")); // NOI18N

        nameTextField.setColumns(30);

        descriptionLabel.setLabelFor(descriptionTextField);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(FieldCustomizerPanel.class, "CTL_Description")); // NOI18N

        typeLabel.setLabelFor(typeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(typeLabel, org.openide.util.NbBundle.getMessage(FieldCustomizerPanel.class, "CTL_Type")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addToLocalLabel, org.openide.util.NbBundle.getMessage(FieldCustomizerPanel.class, "LBL_AddToLocalInterface")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(localGetterCheckBox, org.openide.util.NbBundle.getMessage(FieldCustomizerPanel.class, "CTL_Getter_Local")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(localSetterCheckBox, org.openide.util.NbBundle.getMessage(FieldCustomizerPanel.class, "CTL_Setter_Local")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addToRemoteLabel, org.openide.util.NbBundle.getMessage(FieldCustomizerPanel.class, "LBL_AddToRemoteInterface")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(remoteGetterCheckBox, org.openide.util.NbBundle.getMessage(FieldCustomizerPanel.class, "CTL_Getter_Remote")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(remoteSetterCheckBox, org.openide.util.NbBundle.getMessage(FieldCustomizerPanel.class, "CTL_Setter_Remote")); // NOI18N

        errorTextField.setEditable(false);
        errorTextField.setText(org.openide.util.NbBundle.getMessage(FieldCustomizerPanel.class, "FieldCustomizerPanel.errorTextField.text")); // NOI18N
        errorTextField.setBorder(null);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(errorTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(42, 42, 42)
                                .add(typeLabel))
                            .add(layout.createSequentialGroup()
                                .add(37, 37, 37)
                                .add(nameLabel))
                            .add(descriptionLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                            .add(descriptionTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                            .add(typeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(addToLocalLabel)
                            .add(addToRemoteLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(localGetterCheckBox)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(localSetterCheckBox))
                            .add(layout.createSequentialGroup()
                                .add(remoteGetterCheckBox)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(remoteSetterCheckBox)))
                        .add(190, 190, 190)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(nameLabel)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(descriptionLabel)
                    .add(descriptionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(typeLabel)
                    .add(typeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(localGetterCheckBox)
                    .add(localSetterCheckBox)
                    .add(addToLocalLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addToRemoteLabel)
                    .add(remoteGetterCheckBox)
                    .add(remoteSetterCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(errorTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        nameTextField.getAccessibleContext().setAccessibleDescription(null);
        descriptionTextField.getAccessibleContext().setAccessibleDescription(null);
        localGetterCheckBox.getAccessibleContext().setAccessibleName(null);
        localGetterCheckBox.getAccessibleContext().setAccessibleDescription(null);
        localSetterCheckBox.getAccessibleContext().setAccessibleName(null);
        localSetterCheckBox.getAccessibleContext().setAccessibleDescription(null);
        remoteGetterCheckBox.getAccessibleContext().setAccessibleName(null);
        remoteGetterCheckBox.getAccessibleContext().setAccessibleDescription(null);
        remoteSetterCheckBox.getAccessibleContext().setAccessibleName(null);
        remoteSetterCheckBox.getAccessibleContext().setAccessibleDescription(null);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addToLocalLabel;
    private javax.swing.JLabel addToRemoteLabel;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextField descriptionTextField;
    private javax.swing.JTextField errorTextField;
    private javax.swing.JCheckBox localGetterCheckBox;
    private javax.swing.JCheckBox localSetterCheckBox;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JCheckBox remoteGetterCheckBox;
    private javax.swing.JCheckBox remoteSetterCheckBox;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JTextField typeTextField;
    // End of variables declaration//GEN-END:variables

    public void setError(String message) {
        errorTextField.setText(message);
    }

    public String getMethodName() {
        return nameTextField.getText();
    }
    
    public String getReturnType() {
        return typeTextField.getText();
    }
    
    public String getDescription() {
        return descriptionTextField.getText();
    }

    public boolean isLocalGetter() {
        return localGetterCheckBox.isSelected();
    }

    public boolean isLocalSetter() {
        return localSetterCheckBox.isSelected();
    }

    public boolean isRemoteGetter() {
        return remoteGetterCheckBox.isSelected();
    }

    public boolean isRemoteSetter() {
        return remoteSetterCheckBox.isSelected();
    }

    /**
     * Listener on text fields. 
     * Fires change event for specified property of this JPanel, 
     * old and new value of event is null. 
     * After receiving event, client can get property value by 
     * calling {@link #getProperty(String)}
     */
    private class SimpleListener implements DocumentListener, ActionListener {
        
        private final String propertyName;
        
        public SimpleListener(String propertyName) {
            this.propertyName = propertyName;
        }
        
        public void insertUpdate(DocumentEvent documentEvent) { fire(); }
        
        public void removeUpdate(DocumentEvent documentEvent) { fire(); }
        
        public void changedUpdate(DocumentEvent documentEvent) {}
        
        public void actionPerformed(ActionEvent actionEvent) { fire(); }

        private void fire() {
            firePropertyChange(propertyName, null, null);
        }
        
    }
}

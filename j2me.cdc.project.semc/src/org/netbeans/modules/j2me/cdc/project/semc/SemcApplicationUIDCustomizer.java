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

package org.netbeans.modules.j2me.cdc.project.semc;

import java.awt.Color;
import java.io.File;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author  suchys
 */
public class SemcApplicationUIDCustomizer extends javax.swing.JPanel {
    
    boolean uidValid = true;
    private String uidString;
    private File sdkInstallation;
    
    private final static String[] MODEL_ITEMS = new String[] { 
        NbBundle.getMessage(SemcApplicationUIDCustomizer.class, "LBL_UID_0"), 
        NbBundle.getMessage(SemcApplicationUIDCustomizer.class, "LBL_UID_1"), 
        NbBundle.getMessage(SemcApplicationUIDCustomizer.class, "LBL_UID_2"), 
        NbBundle.getMessage(SemcApplicationUIDCustomizer.class, "LBL_UID_3"), 
        NbBundle.getMessage(SemcApplicationUIDCustomizer.class, "LBL_UID_4"), 
    };
    
    private final static int[] MODEL_VALUES = new int[]{
        0x0,
        0x2,
        0x7,
        0xA,
        0xE
    };

    /** Creates new form ApplicationUIDCustomizer */
    public SemcApplicationUIDCustomizer(String uidString, File sdkInstallation) {
        this.sdkInstallation = sdkInstallation;
        this.uidString = uidString;
        
        initComponents();
        Color nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            nbErrorForeground = new Color(255, 0, 0); 
        }        
        errorLabel.setForeground(nbErrorForeground);
        
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(MODEL_ITEMS));
        if (uidString.length() > 0){
            int uid = Integer.parseInt(uidString.substring(0, 1), 16);
            for (int i = 0; i < MODEL_VALUES.length; i++){
                if (uid == MODEL_VALUES[i]){
                    jComboBox1.setSelectedIndex(i);
                    idTextField.setText(uidString.substring(1));
                    break;
                }
            }
        } else {
            jComboBox1.setSelectedIndex(4);
        }

        this.idTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                computeUID();
            }
            public void insertUpdate(DocumentEvent e) {
                computeUID();
            }
            public void removeUpdate(DocumentEvent e) {
                computeUID();
            }
        });
        computeUID();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        idTextField = new javax.swing.JTextField();
        errorLabel = new javax.swing.JLabel();
        resultValueLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SemcApplicationUIDCustomizer.class, "LBL_AppUID")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(SemcApplicationUIDCustomizer.class, "LBL_ResultUID")); // NOI18N

        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        idTextField.setColumns(7);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(errorLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(idTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(resultValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(idTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(resultValueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(errorLabel)
                .addContainerGap())
        );

        jComboBox1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemcApplicationUIDCustomizer.class, "ACSN_jComboBox1")); // NOI18N
        jComboBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SemcApplicationUIDCustomizer.class, "ACSD_jComboBox1")); // NOI18N
        idTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemcApplicationUIDCustomizer.class, "ACSN_idTextField")); // NOI18N
        idTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SemcApplicationUIDCustomizer.class, "ACSD_idTextField")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
       computeUID();
    }//GEN-LAST:event_jComboBox1ActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorLabel;
    private javax.swing.JTextField idTextField;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel resultValueLabel;
    // End of variables declaration//GEN-END:variables
    
    private void computeUID(){
        String text = idTextField.getText();
        int length = text.length();
        if (length != 7){
            uidValid = false;
        } else {
            uidValid = true;
            for (int i = 0; i < length; i++){
                if (Character.digit(text.charAt(i), 16) == -1){
                    uidValid = false;
                    break;
                }
            }
        }
        int domain = MODEL_VALUES[jComboBox1.getSelectedIndex()];
        String tmpUid = (Integer.toHexString(domain) + idTextField.getText()).toUpperCase();
        resultValueLabel.setText("0x" + tmpUid); //NOI18N
        
        boolean duplicity = false;
        if (sdkInstallation != null){
            File f = new File (sdkInstallation, "\\epoc32\\release\\winscw\\udeb\\PProLauncher" + tmpUid + ".exe"); //NOI18N
            if ( f.exists() && !tmpUid.equals(uidString)){
                uidValid = false;
                duplicity = true;
            }
        }
        if (!uidValid) {
            if (!duplicity)
                errorLabel.setText(NbBundle.getMessage(SemcApplicationUIDCustomizer.class, "ERR_WrongRange")); //NOI18N
            else 
                errorLabel.setText(NbBundle.getMessage(SemcApplicationUIDCustomizer.class, "ERR_UIDAlreadyExists", tmpUid)); //NOI18N
            firePropertyChange(NotifyDescriptor.PROP_VALID, Boolean.TRUE, Boolean.FALSE);
        } else {
            errorLabel.setText(" "); //NOI18N
            firePropertyChange(NotifyDescriptor.PROP_VALID, Boolean.FALSE, Boolean.TRUE);
        }
    }
    
    String getUID(){
        return (Integer.toHexString(MODEL_VALUES[jComboBox1.getSelectedIndex()]) + idTextField.getText()).toUpperCase();
    }
}

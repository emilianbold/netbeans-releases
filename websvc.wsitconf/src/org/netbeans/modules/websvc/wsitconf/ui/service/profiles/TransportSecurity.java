/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitconf.ui.service.profiles;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.AlgoSuiteModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.HttpsToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.TransportToken;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author  Martin Grebac
 */
public class TransportSecurity extends javax.swing.JPanel {

    private boolean inSync = false;

    private WSDLComponent comp;
    
    /**
     * Creates new form TransportSecurity
     */
    public TransportSecurity(WSDLComponent comp) {
        super();
        initComponents();
        this.comp = comp;

        inSync = true;
        layoutCombo.removeAllItems();
        layoutCombo.addItem(ComboConstants.STRICT);
        layoutCombo.addItem(ComboConstants.LAX);
        layoutCombo.addItem(ComboConstants.LAXTSFIRST);
        layoutCombo.addItem(ComboConstants.LAXTSLAST);
        
        algoSuiteCombo.removeAllItems();
        algoSuiteCombo.addItem(ComboConstants.BASIC256);
        algoSuiteCombo.addItem(ComboConstants.BASIC192);
        algoSuiteCombo.addItem(ComboConstants.BASIC128);
        algoSuiteCombo.addItem(ComboConstants.TRIPLEDES);
        algoSuiteCombo.addItem(ComboConstants.BASIC256RSA15);
        algoSuiteCombo.addItem(ComboConstants.BASIC192RSA15);
        algoSuiteCombo.addItem(ComboConstants.BASIC128RSA15);
        algoSuiteCombo.addItem(ComboConstants.TRIPLEDESRSA15);
        algoSuiteCombo.addItem(ComboConstants.BASIC256SHA256);
        algoSuiteCombo.addItem(ComboConstants.BASIC192SHA256);
        algoSuiteCombo.addItem(ComboConstants.BASIC128SHA256);
        algoSuiteCombo.addItem(ComboConstants.TRIPLEDESSHA256);
        algoSuiteCombo.addItem(ComboConstants.BASIC256SHA256RSA15);
        algoSuiteCombo.addItem(ComboConstants.BASIC192SHA256RSA15);
        algoSuiteCombo.addItem(ComboConstants.BASIC128SHA256RSA15);
        algoSuiteCombo.addItem(ComboConstants.TRIPLEDESSHA256RSA15);

        inSync = false;
        sync();
    }
    
    private void sync() {
        inSync = true;

        WSDLComponent secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);        
        WSDLComponent tokenKind = SecurityTokensModelHelper.getTokenElement(secBinding, TransportToken.class);
        HttpsToken token = (HttpsToken) SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
        if (token instanceof HttpsToken) {
            setChBox(requireCertificateChBox, SecurityTokensModelHelper.isRequireClientCertificate(token));
        }

        setCombo(algoSuiteCombo, AlgoSuiteModelHelper.getAlgorithmSuite(secBinding));
      
        setCombo(layoutCombo, SecurityPolicyModelHelper.getMessageLayout(comp));
        
        inSync = false;
    }

    public void setValue(javax.swing.JComponent source) {

        if (inSync) return;

        WSDLComponent secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);        

        if (source.equals(requireCertificateChBox)) {
            WSDLComponent tokenKind = SecurityTokensModelHelper.getTokenElement(secBinding, TransportToken.class);
            HttpsToken token = (HttpsToken) SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
            SecurityTokensModelHelper.setRequireClientCertificate(token, requireCertificateChBox.isSelected());
        }
        if (source.equals(layoutCombo)) {
            SecurityPolicyModelHelper.setLayout(secBinding, (String) layoutCombo.getSelectedItem());
        }
        if (source.equals(algoSuiteCombo)) {
            AlgoSuiteModelHelper.setAlgorithmSuite(secBinding, (String) algoSuiteCombo.getSelectedItem());
        }
    }

    private void setCombo(JComboBox combo, String item) {
        if (item == null) {
            combo.setSelectedIndex(0);
        } else {
            combo.setSelectedItem(item);
        }
    }

    private void setChBox(JCheckBox chBox, Boolean enable) {
        if (enable == null) {
            chBox.setSelected(false);
        } else {
            chBox.setSelected(enable);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        requireCertificateChBox = new javax.swing.JCheckBox();
        algoSuiteLabel = new javax.swing.JLabel();
        algoSuiteCombo = new javax.swing.JComboBox();
        layoutLabel = new javax.swing.JLabel();
        layoutCombo = new javax.swing.JComboBox();

        org.openide.awt.Mnemonics.setLocalizedText(requireCertificateChBox, org.openide.util.NbBundle.getMessage(TransportSecurity.class, "LBL_RequireClientCertificate")); // NOI18N
        requireCertificateChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        requireCertificateChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        requireCertificateChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                requireCertificateChBoxActionPerformed(evt);
            }
        });

        algoSuiteLabel.setLabelFor(algoSuiteCombo);
        org.openide.awt.Mnemonics.setLocalizedText(algoSuiteLabel, org.openide.util.NbBundle.getMessage(TransportSecurity.class, "LBL_AlgoSuiteLabel")); // NOI18N

        algoSuiteCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                algoSuiteComboActionPerformed(evt);
            }
        });

        layoutLabel.setLabelFor(layoutCombo);
        org.openide.awt.Mnemonics.setLocalizedText(layoutLabel, org.openide.util.NbBundle.getMessage(TransportSecurity.class, "LBL_LayoutLabel")); // NOI18N

        layoutCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                layoutComboActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layoutLabel)
                            .add(algoSuiteLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layoutCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(algoSuiteCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(requireCertificateChBox))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {algoSuiteCombo, layoutCombo}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(algoSuiteLabel)
                    .add(algoSuiteCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(layoutLabel)
                    .add(layoutCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(requireCertificateChBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {algoSuiteCombo, layoutCombo}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }// </editor-fold>//GEN-END:initComponents

    private void layoutComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_layoutComboActionPerformed
        setValue(layoutCombo);
    }//GEN-LAST:event_layoutComboActionPerformed

    private void algoSuiteComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_algoSuiteComboActionPerformed
        setValue(algoSuiteCombo);
    }//GEN-LAST:event_algoSuiteComboActionPerformed

    private void requireCertificateChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_requireCertificateChBoxActionPerformed
        setValue(requireCertificateChBox);
    }//GEN-LAST:event_requireCertificateChBoxActionPerformed
            
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox algoSuiteCombo;
    private javax.swing.JLabel algoSuiteLabel;
    private javax.swing.JComboBox layoutCombo;
    private javax.swing.JLabel layoutLabel;
    private javax.swing.JCheckBox requireCertificateChBox;
    // End of variables declaration//GEN-END:variables
    
}

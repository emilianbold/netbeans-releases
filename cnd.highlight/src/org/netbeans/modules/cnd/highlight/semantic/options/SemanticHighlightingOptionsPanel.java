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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.highlight.semantic.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.UIManager;
import org.openide.util.NbBundle;

/**
 *
 * @author  Sergey Grinev
 */
public class SemanticHighlightingOptionsPanel extends javax.swing.JPanel implements ActionListener{
    
    public SemanticHighlightingOptionsPanel() {
        initComponents();
        initMnemonics();
        cbKeepMarks.addActionListener(this);
        setName("TAB_SemanticHighlightingTab"); // NOI18N (used as a pattern...)
        if (!SemanticHighlightingOptions.SEMANTIC_ADVANCED) {
            cbClassFields.setVisible(false);
            cbFunctionNames.setVisible(false);
        }
        if( "Windows".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            setOpaque( false );
        }
    }

    // for OptionsPanelSupport
    private boolean isChanged = false;
    
    void applyChanges() {
        SemanticHighlightingOptions.setEnableMarkOccurences(cbMarkOccurrences.isSelected());
        SemanticHighlightingOptions.setKeepMarks(cbKeepMarks.isSelected());
        SemanticHighlightingOptions.setEnableMacros(cbMacros.isSelected());
        SemanticHighlightingOptions.setDifferSystemMacros(cbSysMacro.isSelected());
        SemanticHighlightingOptions.setEnableClassFields(cbClassFields.isSelected());
        SemanticHighlightingOptions.setEnableFunctionNames(cbFunctionNames.isSelected());
        isChanged = false;
    }

    void update() {
        cbMarkOccurrences.setSelected(SemanticHighlightingOptions.getEnableMarkOccurences());
        cbKeepMarks.setSelected(SemanticHighlightingOptions.getKeepMarks());
        cbMacros.setSelected(SemanticHighlightingOptions.getEnableMacros());
        cbSysMacro.setSelected(SemanticHighlightingOptions.getDifferSystemMacros());
        cbClassFields.setSelected(SemanticHighlightingOptions.getEnableClassFields());
        cbFunctionNames.setSelected(SemanticHighlightingOptions.getEnableFunctionNames());
        updateValidation();
    }
    
    void cancel() {
        isChanged = false;
    }

    boolean isChanged() {
        return isChanged;
    }

    public void actionPerformed(ActionEvent e) {
        isChanged = true;
    }
    
    private void updateValidation() {
        cbKeepMarks.setEnabled(cbMarkOccurrences.isSelected());
        cbSysMacro.setEnabled(cbMacros.isSelected());
    }

    private void initMnemonics() {
        cbMarkOccurrences.setMnemonic(getString("EnableMarkOccurrences_Mnemonic").charAt(0));
        cbKeepMarks.setMnemonic(getString("KeepMarks_Mnemonic").charAt(0));
        cbMacros.setMnemonic(getString("EnableMacros_Mnemonic").charAt(0));
        cbSysMacro.setMnemonic(getString("DifferSystemMacros_Mnemonic").charAt(0));
        cbClassFields.setMnemonic(getString("ShowClassFields_Mnemonic").charAt(0));
        cbFunctionNames.setMnemonic(getString("ShowFunctionNames_Mnemonic").charAt(0));

        cbMarkOccurrences.getAccessibleContext().setAccessibleDescription(getString("EnableMarkOccurrences_AD"));
        cbKeepMarks.getAccessibleContext().setAccessibleDescription(getString("KeepMarks_AD"));
        cbMacros.getAccessibleContext().setAccessibleDescription(getString("EnableMacros_AD"));
        cbSysMacro.getAccessibleContext().setAccessibleDescription(getString("DifferSystemMacros_AD"));
        cbClassFields.getAccessibleContext().setAccessibleDescription(getString("ShowClassFields_AD"));
        cbFunctionNames.getAccessibleContext().setAccessibleDescription(getString("ShowFunctionNames_AD"));
}
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbKeepMarks = new javax.swing.JCheckBox();
        cbMarkOccurrences = new javax.swing.JCheckBox();
        cbMacros = new javax.swing.JCheckBox();
        cbClassFields = new javax.swing.JCheckBox();
        cbFunctionNames = new javax.swing.JCheckBox();
        cbSysMacro = new javax.swing.JCheckBox();

        cbKeepMarks.setText(getString("KeepMarks"));
        cbKeepMarks.setOpaque(false);

        cbMarkOccurrences.setText(getString("EnableMarkOccurrences"));
        cbMarkOccurrences.setOpaque(false);
        cbMarkOccurrences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMarkOccurrencesActionPerformed(evt);
            }
        });

        cbMacros.setText(getString("EnableMacros"));
        cbMacros.setOpaque(false);
        cbMacros.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMacrosActionPerformed(evt);
            }
        });

        cbClassFields.setText(getString("ShowClassFields"));
        cbClassFields.setOpaque(false);

        cbFunctionNames.setText(getString("ShowFunctionNames"));
        cbFunctionNames.setOpaque(false);

        cbSysMacro.setText(getString("DifferSystemMacros"));
        cbSysMacro.setOpaque(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(29, 29, 29)
                        .add(cbKeepMarks))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(cbMarkOccurrences))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(21, 21, 21)
                                .add(cbSysMacro))
                            .add(cbMacros)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cbClassFields)
                            .add(cbFunctionNames))))
                .addContainerGap(283, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(cbMarkOccurrences)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbKeepMarks)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbMacros)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbSysMacro)
                .add(18, 18, 18)
                .add(cbClassFields)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbFunctionNames)
                .addContainerGap(112, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbMarkOccurrencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbMarkOccurrencesActionPerformed
        updateValidation();
    }//GEN-LAST:event_cbMarkOccurrencesActionPerformed

    private void cbMacrosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbMacrosActionPerformed
        updateValidation();
    }//GEN-LAST:event_cbMacrosActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbClassFields;
    private javax.swing.JCheckBox cbFunctionNames;
    private javax.swing.JCheckBox cbKeepMarks;
    private javax.swing.JCheckBox cbMacros;
    private javax.swing.JCheckBox cbMarkOccurrences;
    private javax.swing.JCheckBox cbSysMacro;
    // End of variables declaration//GEN-END:variables

    private static String getString(String key) {
        return NbBundle.getMessage(SemanticHighlightingOptionsPanel.class, key);
    }
}

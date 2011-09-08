/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.newproject;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.openide.WizardDescriptor;
import static org.netbeans.modules.maven.j2ee.newproject.Bundle.*;

class EELevelPanelVisual extends JPanel {

    EELevelPanelVisual() {
        initComponents();
        getAccessibleContext().setAccessibleDescription(LBL_EESettings());
    }

    @Override public String getName() {
        return LBL_EESettings();
    }

    void readSettings(WizardDescriptor wiz) {
        Integer eeLevel = (Integer) wiz.getProperty(BasicEEWizardIterator.PROP_EE_LEVEL);
        if (eeLevel != null) {
            cmbEEVersion.setSelectedIndex(eeLevel);
        }
    }

    void storeSettings(WizardDescriptor wiz) {
        wiz.putProperty(BasicEEWizardIterator.PROP_EE_LEVEL, cmbEEVersion.getSelectedIndex());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblEEVersion = new javax.swing.JLabel();
        cmbEEVersion = new javax.swing.JComboBox();

        org.openide.awt.Mnemonics.setLocalizedText(lblEEVersion, org.openide.util.NbBundle.getMessage(EELevelPanelVisual.class, "EELevelPanelVisual.lblEEVersion.text")); // NOI18N

        cmbEEVersion.setModel(new DefaultComboBoxModel(BasicEEWizardIterator.eeLevels()));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblEEVersion)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbEEVersion, 0, 242, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEEVersion)
                    .addComponent(cmbEEVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(264, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cmbEEVersion;
    private javax.swing.JLabel lblEEVersion;
    // End of variables declaration//GEN-END:variables

}

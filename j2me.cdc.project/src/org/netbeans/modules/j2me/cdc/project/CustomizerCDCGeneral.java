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

package org.netbeans.modules.j2me.cdc.project;

import javax.swing.JPanel;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.util.NbBundle;

/**
 *
 * @author  Adam Sotona
 */
public class CustomizerCDCGeneral extends JPanel implements CustomizerPanel, VisualPropertyGroup {
    
    private static final String[] PROPERTY_NAMES = new String[] {
        CDCPropertiesDescriptor.APPLICATION_NAME,
        CDCPropertiesDescriptor.APPLICATION_DESCRIPTION,
        CDCPropertiesDescriptor.APPLICATION_DESCRIPTION_DETAIL,
        CDCPropertiesDescriptor.APPLICATION_VENDOR
    };
    
    private VisualPropertySupport vps;
    
    /** Creates new form CustomizerCDCGeneral */
    public CustomizerCDCGeneral() {
        initComponents();
    }
    public void initValues(ProjectProperties props, String configuration) {
        vps = VisualPropertySupport.getDefault(props);
        vps.register(jCheckBox1, configuration, this);
    }
    
    public void initGroupValues(boolean useDefault) {
        vps.register(appNameTextField, CDCPropertiesDescriptor.APPLICATION_NAME, useDefault);
        vps.register(descriptionTextField, CDCPropertiesDescriptor.APPLICATION_DESCRIPTION, useDefault);
        vps.register(detailTextField, CDCPropertiesDescriptor.APPLICATION_DESCRIPTION_DETAIL, useDefault);
        vps.register(vendorTextField, CDCPropertiesDescriptor.APPLICATION_VENDOR, useDefault);
        jLabel5.setEnabled(!useDefault);
        descrLabel.setEnabled(!useDefault);
        detailDescrLabel.setEnabled(!useDefault);
        vendorLabel.setEnabled(!useDefault);
    }
        
    public String[] getGroupPropertyNames() {
        return PROPERTY_NAMES;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel5 = new javax.swing.JLabel();
        appNameTextField = new javax.swing.JTextField();
        vendorLabel = new javax.swing.JLabel();
        vendorTextField = new javax.swing.JTextField();
        descrLabel = new javax.swing.JLabel();
        descriptionTextField = new javax.swing.JTextField();
        detailDescrLabel = new javax.swing.JLabel();
        detailTextField = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();

        jLabel5.setLabelFor(appNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(CustomizerCDCGeneral.class, "LBL_CustomizerCDCGeneral_AppName")); // NOI18N

        vendorLabel.setLabelFor(vendorTextField);
        org.openide.awt.Mnemonics.setLocalizedText(vendorLabel, org.openide.util.NbBundle.getMessage(CustomizerCDCGeneral.class, "LBL_CustomizerCDCGeneral_vendorLabel")); // NOI18N

        descrLabel.setLabelFor(descriptionTextField);
        org.openide.awt.Mnemonics.setLocalizedText(descrLabel, org.openide.util.NbBundle.getMessage(CustomizerCDCGeneral.class,"LBL_CustomizerCDCGeneral_descrLabel")); // NOI18N

        detailDescrLabel.setLabelFor(detailTextField);
        org.openide.awt.Mnemonics.setLocalizedText(detailDescrLabel, org.openide.util.NbBundle.getMessage(CustomizerCDCGeneral.class,"LBL_CustomizerCDCGeneral_detailDescrLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, NbBundle.getMessage(CustomizerCDCGeneral.class, "LBL_UseDefault")); // NOI18N
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(12, 12, 12)
                        .addComponent(appNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(vendorLabel)
                        .addGap(5, 5, 5)
                        .addComponent(vendorTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(descrLabel)
                        .addGap(41, 41, 41)
                        .addComponent(descriptionTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(detailDescrLabel)
                        .addGap(11, 11, 11)
                        .addComponent(detailTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(appNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(vendorLabel)
                    .addComponent(vendorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(descrLabel)
                    .addComponent(descriptionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(detailDescrLabel)
                    .addComponent(detailTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(170, Short.MAX_VALUE))
        );

        jLabel5.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerCDCGeneral.class, "ACSN_CustomizerCDCGeneral_AppName")); // NOI18N
        jLabel5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCDCGeneral.class, "ACSD_CustomizerCDCGeneral_AppNam")); // NOI18N
        vendorLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerCDCGeneral.class, "ACSN_CustomizerCDCGeneral_vendorLabel")); // NOI18N
        vendorLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCDCGeneral.class, "ACSD_CustomizerCDCGeneral_vendorLabel")); // NOI18N
        descrLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerCDCGeneral.class, "ACSN_CustomizerCDCGeneral_descrLabel")); // NOI18N
        descrLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCDCGeneral.class, "ACSD_CustomizerCDCGeneral_descrLabel")); // NOI18N
        detailDescrLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerCDCGeneral.class, "ACSN_CustomizerCDCGeneral_detailDescrLabel")); // NOI18N
        detailDescrLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCDCGeneral.class, "ACSD_CustomizerCDCGeneral_detailDescrLabel")); // NOI18N
        jCheckBox1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerCDCGeneral.class, "ACSN_UseDefault")); // NOI18N
        jCheckBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCDCGeneral.class, "ACSD_UseDefault")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField appNameTextField;
    private javax.swing.JLabel descrLabel;
    private javax.swing.JTextField descriptionTextField;
    private javax.swing.JLabel detailDescrLabel;
    private javax.swing.JTextField detailTextField;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel vendorLabel;
    private javax.swing.JTextField vendorTextField;
    // End of variables declaration//GEN-END:variables
    
}

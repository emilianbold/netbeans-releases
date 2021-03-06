/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.codegen;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import static org.netbeans.modules.maven.codegen.Bundle.*;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Profile;
import org.openide.DialogDescriptor;
import org.openide.NotificationLineSupport;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
@NbBundle.Messages({"NewProfilePanel_cbPlugins_text2=&Generate Plugins section",
                    "NewProfilePanel_cbDependencies_text2=Generate &Dependencies section"})
public class NewProfilePanel extends javax.swing.JPanel {
    private POMModel model;
    private NotificationLineSupport nls;

    public NewProfilePanel(POMModel model) {
        initComponents();
        this.model = model;
        boolean pomPackaging = "pom".equals(model.getProject().getPackaging()); //NOI18N
        if (!pomPackaging) {
            Mnemonics.setLocalizedText(cbPlugins, NewProfilePanel_cbPlugins_text2()); // NOI18N
            Mnemonics.setLocalizedText(cbDependencies, NewProfilePanel_cbDependencies_text2()); // NOI18N
        }
        txtId.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkId();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                checkId();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                checkId();
            }
        });
    }

    /** For gaining access to DialogDisplayer instance to manage
     * warning messages
     */
    public void attachDialogDisplayer(DialogDescriptor dd) {
        nls = dd.getNotificationLineSupport();
        if (nls == null) {
            nls = dd.createNotificationLineSupport();
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        assert nls != null : " The notificationLineSupport was not attached to the panel."; //NOI18N
    }

    private void checkId() {
        String id = txtId.getText().trim();
        Profile existing = model.getProject().findProfileById(id);
        if (existing != null) {
            nls.setErrorMessage(NbBundle.getMessage(NewProfilePanel.class, "ERR_SameProfileId"));
        } else {
            nls.clearMessages();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblId = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        cbActProperty = new javax.swing.JCheckBox();
        cbActOS = new javax.swing.JCheckBox();
        cbActFile = new javax.swing.JCheckBox();
        cbPlugins = new javax.swing.JCheckBox();
        cbDependencies = new javax.swing.JCheckBox();

        lblId.setLabelFor(txtId);
        org.openide.awt.Mnemonics.setLocalizedText(lblId, org.openide.util.NbBundle.getMessage(NewProfilePanel.class, "NewProfilePanel.lblId.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbActProperty, org.openide.util.NbBundle.getMessage(NewProfilePanel.class, "NewProfilePanel.cbActProperty.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbActOS, org.openide.util.NbBundle.getMessage(NewProfilePanel.class, "NewProfilePanel.cbActOS.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbActFile, org.openide.util.NbBundle.getMessage(NewProfilePanel.class, "NewProfilePanel.cbActFile.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbPlugins, org.openide.util.NbBundle.getMessage(NewProfilePanel.class, "NewProfilePanel.cbPlugins.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbDependencies, org.openide.util.NbBundle.getMessage(NewProfilePanel.class, "NewProfilePanel.cbDependencies.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbDependencies)
                    .addComponent(cbPlugins)
                    .addComponent(cbActFile)
                    .addComponent(cbActOS)
                    .addComponent(cbActProperty)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblId)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtId, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblId)
                    .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(cbActProperty)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbActOS)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbActFile)
                .addGap(18, 18, 18)
                .addComponent(cbPlugins)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbDependencies)
                .addContainerGap(64, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbActFile;
    private javax.swing.JCheckBox cbActOS;
    private javax.swing.JCheckBox cbActProperty;
    private javax.swing.JCheckBox cbDependencies;
    private javax.swing.JCheckBox cbPlugins;
    private javax.swing.JLabel lblId;
    private javax.swing.JTextField txtId;
    // End of variables declaration//GEN-END:variables

    String getProfileId() {
        return txtId.getText();
    }


    boolean isActivation() {
        return isActiovationByFile() || isActiovationByOS() || isActiovationByProperty();
    }

    boolean isActiovationByProperty() {
        return cbActProperty.isSelected();
    }

    boolean isActiovationByOS() {
        return cbActOS.isSelected();
    }

    boolean isActiovationByFile() {
        return cbActFile.isSelected();
    }

    boolean generateDependencies() {
        return cbDependencies.isSelected();
    }

    boolean generatePlugins() {
        return cbPlugins.isSelected();
    }

}

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

package org.netbeans.modules.keyring.jps;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.openide.util.NbBundle;

final class JPSConfigPanel extends javax.swing.JPanel {

    private static final Logger LOG = Logger.getLogger(JPSConfigPanel.class.getName());

    private final JPSConfigOptionsPanelController controller;
    private final DocumentListener docL = new DocumentListener() {
        @Override public void insertUpdate(DocumentEvent e) {
            controller.changed();
        }
        @Override public void removeUpdate(DocumentEvent e) {
            controller.changed();
        }
        @Override public void changedUpdate(DocumentEvent e) {}
    };

    JPSConfigPanel(JPSConfigOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
    }

    void load() {
        JTextField[] fields = {configFileField, implField, contextField, mapField};
        for (JTextField field : fields) {
            field.getDocument().removeDocumentListener(docL);
        }
        JPSConfig c = new JPSConfig();
        configFileField.setText(c.getConfigFile());
        implField.setText(c.getImpl());
        contextField.setText(c.getContext());
        mapField.setText(c.getMap());
        for (JTextField field : fields) {
            field.getDocument().addDocumentListener(docL);
        }
    }

    void store() {
        JPSConfig c = new JPSConfig();
        c.setConfigFile(configFileField.getText());
        c.setImpl(implField.getText());
        c.setContext(contextField.getText());
        c.setMap(mapField.getText());
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    boolean valid() {
        try {
            new CredentialStoreProxy(configFileField.getText(), implField.getText(), contextField.getText(), mapField.getText());
            LOG.fine("config passed all sanity checks");
            return true;
        } catch (Exception x) {
            LOG.log(Level.FINE, "invalid config: {0}", x.toString()); // XXX where can errors be displayed?
            return false;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configFileLabel = new javax.swing.JLabel();
        configFileField = new javax.swing.JTextField();
        configFileButton = new javax.swing.JButton();
        configFileHint = new javax.swing.JLabel();
        implLabel = new javax.swing.JLabel();
        implField = new javax.swing.JTextField();
        implButton = new javax.swing.JButton();
        implHint = new javax.swing.JLabel();
        advancedPanel = new javax.swing.JPanel();
        contextLabel = new javax.swing.JLabel();
        contextField = new javax.swing.JTextField();
        mapLabel = new javax.swing.JLabel();
        mapField = new javax.swing.JTextField();
        restartLabel = new javax.swing.JLabel();

        configFileLabel.setLabelFor(configFileField);
        org.openide.awt.Mnemonics.setLocalizedText(configFileLabel, NbBundle.getMessage(JPSConfigPanel.class, "JPSConfigPanel.configFileLabel.text")); // NOI18N

        configFileField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(configFileButton, NbBundle.getMessage(JPSConfigPanel.class, "JPSConfigPanel.configFileButton.text")); // NOI18N
        configFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configFileButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(configFileHint, NbBundle.getMessage(JPSConfigPanel.class, "JPSConfigPanel.configFileHint.text")); // NOI18N

        implLabel.setLabelFor(implField);
        org.openide.awt.Mnemonics.setLocalizedText(implLabel, NbBundle.getMessage(JPSConfigPanel.class, "JPSConfigPanel.implLabel.text")); // NOI18N

        implField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(implButton, NbBundle.getMessage(JPSConfigPanel.class, "JPSConfigPanel.implButton.text")); // NOI18N
        implButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                implButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(implHint, NbBundle.getMessage(JPSConfigPanel.class, "JPSConfigPanel.implHint.text")); // NOI18N

        advancedPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(NbBundle.getMessage(JPSConfigPanel.class, "JPSConfigPanel.advancedPanel.border.title"))); // NOI18N

        contextLabel.setLabelFor(contextField);
        org.openide.awt.Mnemonics.setLocalizedText(contextLabel, NbBundle.getMessage(JPSConfigPanel.class, "JPSConfigPanel.contextLabel.text")); // NOI18N

        mapLabel.setLabelFor(mapField);
        org.openide.awt.Mnemonics.setLocalizedText(mapLabel, NbBundle.getMessage(JPSConfigPanel.class, "JPSConfigPanel.mapLabel.text")); // NOI18N

        javax.swing.GroupLayout advancedPanelLayout = new javax.swing.GroupLayout(advancedPanel);
        advancedPanel.setLayout(advancedPanelLayout);
        advancedPanelLayout.setHorizontalGroup(
            advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contextLabel)
                    .addComponent(mapLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mapField, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
                    .addComponent(contextField, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE))
                .addContainerGap())
        );
        advancedPanelLayout.setVerticalGroup(
            advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contextLabel)
                    .addComponent(contextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mapLabel)
                    .addComponent(mapField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(restartLabel, NbBundle.getMessage(JPSConfigPanel.class, "JPSConfigPanel.restartLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(configFileLabel)
                            .addComponent(implLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(configFileField, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                            .addComponent(implField, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(configFileButton, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(implButton, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(restartLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(implHint))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(configFileHint))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(advancedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configFileLabel)
                    .addComponent(configFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(configFileButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(configFileHint)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(implLabel)
                    .addComponent(implField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(implButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(implHint)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(advancedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(restartLabel)
                .addContainerGap(13, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void configFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configFileButtonActionPerformed
    JFileChooser chooser = new JFileChooser();
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        configFileField.setText(chooser.getSelectedFile().getAbsolutePath());
        configFileField.setCaretPosition(configFileField.getText().length());
    }
}//GEN-LAST:event_configFileButtonActionPerformed

private void implButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_implButtonActionPerformed
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new FileFilter() {
        @Override public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".jar");
        }
        @Override public String getDescription() {
            return "*.jar";
        }
    });
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        implField.setText(chooser.getSelectedFile().getAbsolutePath());
        implField.setCaretPosition(implField.getText().length());
    }
}//GEN-LAST:event_implButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel advancedPanel;
    private javax.swing.JButton configFileButton;
    private javax.swing.JTextField configFileField;
    private javax.swing.JLabel configFileHint;
    private javax.swing.JLabel configFileLabel;
    private javax.swing.JTextField contextField;
    private javax.swing.JLabel contextLabel;
    private javax.swing.JButton implButton;
    private javax.swing.JTextField implField;
    private javax.swing.JLabel implHint;
    private javax.swing.JLabel implLabel;
    private javax.swing.JTextField mapField;
    private javax.swing.JLabel mapLabel;
    private javax.swing.JLabel restartLabel;
    // End of variables declaration//GEN-END:variables

}

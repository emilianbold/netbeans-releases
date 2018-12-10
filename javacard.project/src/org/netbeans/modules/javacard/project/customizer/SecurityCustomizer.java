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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.customizer;

import org.netbeans.modules.javacard.project.JCProjectProperties;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.modules.javacard.common.GuiUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;

/**
 * Security customizer
 *
 */
public class SecurityCustomizer extends JPanel implements DocumentListener, FocusListener, ActionListener {

    private Category cat;
    private final JComponent[] enableDisable;
    L l = new L();
    private JCProjectProperties props;

    /** Creates new form Security */
    public SecurityCustomizer(JCProjectProperties props, Category cat) {
        this.props = props;
        initComponents();
        enableDisable = new JComponent[]{
                    aliasPasswordLabel, aliasPasswordField, browseButton,
                    keystorePasswordLabel, keystorePasswordField, keystoreAliasLabel,
                    keystoreAliasField, keystoreField
                };
        this.cat = cat;
        keystoreField.setDocument(props.KEYSTORE_DOCUMENT);
        keystoreAliasField.setDocument(props.KEYSTORE_ALIAS_DOCUMENT);
        keystorePasswordField.setDocument(props.KEYSTORE_PASSWORD_DOCUMENT);
        aliasPasswordField.setDocument(props.KEYSTORE_ALIAS_PASSWORD_DOCUMENT);
        signButton.setModel(props.SIGN_JAR_BUTTON_MODEL);
        keystoreField.getDocument().addDocumentListener(this);
        keystoreField.addFocusListener(this);
        signButton.addActionListener(l);
        Mnemonics.setLocalizedText(signButton, signButton.getText());
        Mnemonics.setLocalizedText(keystoreLabel, keystoreLabel.getText());
        Mnemonics.setLocalizedText(browseButton, browseButton.getText());
        l.actionPerformed(null);
        updateState();
        HelpCtx.setHelpIDString(this, "org.netbeans.modules.javacard.BuildSecurityPanel"); //NOI18N
        GuiUtils.prepareContainer(this);
    }

    void updateState() {
        File f = new File(keystoreField.getText());
        if (signButton.isSelected()) {
            if (f.exists() && f.isFile() && f.canRead()) {
                cat.setValid(true);
                cat.setErrorMessage(null);
            } else {
                String msg = NbBundle.getMessage(SecurityCustomizer.class,
                        "MSG_BAD_FILE", keystoreField.getText()); //NOI18N
                cat.setValid(!signButton.isSelected());
                cat.setErrorMessage(msg);
            }
        } else {
            cat.setValid(true);
            cat.setErrorMessage(null);
        }
    }

    private void change() {
        updateState();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        signButton = new javax.swing.JCheckBox();
        keystoreLabel = new javax.swing.JLabel();
        keystoreField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        keystorePasswordLabel = new javax.swing.JLabel();
        keystoreAliasLabel = new javax.swing.JLabel();
        keystoreAliasField = new javax.swing.JTextField();
        aliasPasswordLabel = new javax.swing.JLabel();
        aliasPasswordField = new javax.swing.JPasswordField();
        keystorePasswordField = new javax.swing.JPasswordField();
        jLabel1 = new javax.swing.JLabel();

        signButton.setSelected(true);
        signButton.setText(org.openide.util.NbBundle.getMessage(SecurityCustomizer.class, "SecurityCustomizer.signButton.text", new Object[] {})); // NOI18N

        keystoreLabel.setText(org.openide.util.NbBundle.getMessage(SecurityCustomizer.class, "SecurityCustomizer.keystoreLabel.text", new Object[] {})); // NOI18N

        keystoreField.setText(org.openide.util.NbBundle.getMessage(SecurityCustomizer.class, "SecurityCustomizer.keystoreField.text", new Object[] {})); // NOI18N

        browseButton.setText(org.openide.util.NbBundle.getMessage(SecurityCustomizer.class, "SecurityCustomizer.browseButton.text", new Object[] {})); // NOI18N
        browseButton.addActionListener(this);

        keystorePasswordLabel.setLabelFor(keystorePasswordField);
        keystorePasswordLabel.setText(org.openide.util.NbBundle.getMessage(SecurityCustomizer.class, "SecurityCustomizer.keystorePasswordLabel.text")); // NOI18N

        keystoreAliasLabel.setLabelFor(keystoreAliasField);
        keystoreAliasLabel.setText(org.openide.util.NbBundle.getMessage(SecurityCustomizer.class, "SecurityCustomizer.keystoreAliasLabel.text")); // NOI18N

        keystoreAliasField.setText(org.openide.util.NbBundle.getMessage(SecurityCustomizer.class, "SecurityCustomizer.keystoreAliasField.text")); // NOI18N
        keystoreAliasField.setToolTipText(org.openide.util.NbBundle.getMessage(SecurityCustomizer.class, "SecurityCustomizer.keystoreAliasField.toolTipText")); // NOI18N

        aliasPasswordLabel.setLabelFor(keystorePasswordField);
        aliasPasswordLabel.setText(org.openide.util.NbBundle.getMessage(SecurityCustomizer.class, "SecurityCustomizer.aliasPasswordLabel.text")); // NOI18N

        aliasPasswordField.setText(org.openide.util.NbBundle.getMessage(SecurityCustomizer.class, "SecurityCustomizer.aliasPasswordField.text")); // NOI18N
        aliasPasswordField.setToolTipText(org.openide.util.NbBundle.getMessage(SecurityCustomizer.class, "SecurityCustomizer.aliasPasswordField.toolTipText")); // NOI18N

        keystorePasswordField.setText(org.openide.util.NbBundle.getMessage(SecurityCustomizer.class, "SecurityCustomizer.keystorePasswordField.text")); // NOI18N
        keystorePasswordField.setToolTipText(org.openide.util.NbBundle.getMessage(SecurityCustomizer.class, "SecurityCustomizer.keystorePasswordField.toolTipText")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(SecurityCustomizer.class, "SecurityCustomizer.jLabel1.text")); // NOI18N
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(signButton)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(keystoreLabel)
                                    .addComponent(keystorePasswordLabel)
                                    .addComponent(keystoreAliasLabel)
                                    .addComponent(aliasPasswordLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(keystorePasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
                                    .addComponent(aliasPasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
                                    .addComponent(keystoreAliasField, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
                                    .addComponent(keystoreField, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseButton)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(signButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keystoreLabel)
                    .addComponent(keystoreField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keystorePasswordLabel)
                    .addComponent(keystorePasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keystoreAliasLabel)
                    .addComponent(keystoreAliasField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aliasPasswordLabel)
                    .addComponent(aliasPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                .addGap(26, 26, 26))
        );
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == browseButton) {
            SecurityCustomizer.this.browseButtonActionPerformed(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String curr = keystoreField.getText();
        File file = new File(curr).getParentFile();
        if (file == null) {
            file = FileUtil.toFile(props.getProject().getProjectDirectory());
        }
        FileChooserBuilder b = new FileChooserBuilder(SecurityCustomizer.class).setTitle(
                NbBundle.getMessage(SecurityCustomizer.class, "BROWSE_FOR_KEYSTORE")).
                setFilesOnly(true);
        if (file.exists() && file.isDirectory()) {
            b.setDefaultWorkingDirectory(file);
        }
        File f;
        if ((f = b.showOpenDialog()) != null) {
            if (!new File(curr).equals(f)) {
                keystoreField.setText(f.getAbsolutePath());
            }
        }
    }//GEN-LAST:event_browseButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPasswordField aliasPasswordField;
    private javax.swing.JLabel aliasPasswordLabel;
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField keystoreAliasField;
    private javax.swing.JLabel keystoreAliasLabel;
    private javax.swing.JTextField keystoreField;
    private javax.swing.JLabel keystoreLabel;
    private javax.swing.JPasswordField keystorePasswordField;
    private javax.swing.JLabel keystorePasswordLabel;
    private javax.swing.JCheckBox signButton;
    // End of variables declaration//GEN-END:variables

    public void insertUpdate(DocumentEvent e) {
        change();
    }

    public void removeUpdate(DocumentEvent e) {
        change();
    }

    public void changedUpdate(DocumentEvent e) {
        change();
    }

    public void focusGained(FocusEvent e) {
        ((JTextField) e.getComponent()).selectAll();
    }

    public void focusLost(FocusEvent e) {
        //do nothing
    }

    private class L implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            boolean en = signButton.isSelected();
            for (JComponent jc : enableDisable) {
                jc.setEnabled(en);
            }
            updateState();
        }
    }
}

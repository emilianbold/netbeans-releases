/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.php.project.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class BrowseTestSources extends JPanel {
    private static final long serialVersionUID = 1463321897654268L;

    private final PhpProject phpProject;
    private DialogDescriptor dialogDescriptor;
    private NotificationLineSupport notificationLineSupport;

    public BrowseTestSources(PhpProject phpProject, String title) {
        assert phpProject != null;
        assert title != null;

        this.phpProject = phpProject;

        initComponents();
        infoLabel.setText(title);
        testSourcesTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                processUpdate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                processUpdate();
            }

            private void processUpdate() {
                validateTestSources();
            }
        });
    }

    /**
     * @return <code>true</code> if OK button is chosen.
     */
    public boolean open() {
        dialogDescriptor = new DialogDescriptor(
                this,
                NbBundle.getMessage(BrowseTestSources.class, "LBL_DirectoryForProject", ProjectUtils.getInformation(phpProject).getDisplayName()),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                null);
        notificationLineSupport = dialogDescriptor.createNotificationLineSupport();
        dialogDescriptor.setValid(false);
        return DialogDisplayer.getDefault().notify(dialogDescriptor) == DialogDescriptor.OK_OPTION;
    }

    public String getTestSources() {
        return testSourcesTextField.getText();
    }

    void validateTestSources() {
        assert notificationLineSupport != null;

        String testSources = testSourcesTextField.getText();
        String error = Utils.validateTestSources(phpProject, testSources);
        if (error != null) {
            notificationLineSupport.setErrorMessage(error);
            dialogDescriptor.setValid(false);
            return;
        }

        String warning = Utils.warnTestSources(phpProject, testSources);
        if (warning != null) {
            notificationLineSupport.setWarningMessage(warning);
            dialogDescriptor.setValid(true);
            return;
        }

        notificationLineSupport.setInformationMessage(NbBundle.getMessage(BrowseTestSources.class, "LBL_PhpUnitIncludePathInfo"));
        dialogDescriptor.setValid(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        infoLabel = new JLabel();
        testSourcesLabel = new JLabel();
        testSourcesTextField = new JTextField();
        testSourcesBrowseButton = new JButton();

        Mnemonics.setLocalizedText(infoLabel, "dummy"); // NOI18N

        testSourcesLabel.setLabelFor(testSourcesTextField);

        Mnemonics.setLocalizedText(testSourcesLabel, NbBundle.getMessage(BrowseTestSources.class, "BrowseTestSources.testSourcesLabel.text"));
        Mnemonics.setLocalizedText(testSourcesBrowseButton, NbBundle.getMessage(BrowseTestSources.class, "BrowseTestSources.testSourcesBrowseButton.text"));
        testSourcesBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                testSourcesBrowseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(infoLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(testSourcesLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(testSourcesTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(testSourcesBrowseButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(infoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(testSourcesLabel)
                    .addComponent(testSourcesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(testSourcesBrowseButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void testSourcesBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_testSourcesBrowseButtonActionPerformed
        Utils.browseTestSources(testSourcesTextField, phpProject);
    }//GEN-LAST:event_testSourcesBrowseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel infoLabel;
    private JButton testSourcesBrowseButton;
    private JLabel testSourcesLabel;
    private JTextField testSourcesTextField;
    // End of variables declaration//GEN-END:variables

}

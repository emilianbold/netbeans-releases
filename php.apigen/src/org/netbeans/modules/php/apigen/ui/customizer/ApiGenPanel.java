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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.apigen.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.apigen.ApiGenProvider;
import org.netbeans.modules.php.apigen.ui.ApiGenPreferences;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

final class ApiGenPanel extends JPanel implements HelpCtx.Provider {

    private static final long serialVersionUID = -54768321324347L;

    private final Category category;
    private final PhpModule phpModule;


    ApiGenPanel(Category category, PhpModule phpModule) {
        assert category != null;
        assert phpModule != null;

        this.category = category;
        this.phpModule = phpModule;

        this.category.setStoreListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                storeData();
            }
        });

        initComponents();

        targetTextField.setText(ApiGenPreferences.getTarget(phpModule, false));
        titleTextField.setText(ApiGenPreferences.getTitle(phpModule));

        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        targetTextField.getDocument().addDocumentListener(defaultDocumentListener);
        titleTextField.getDocument().addDocumentListener(defaultDocumentListener);
        validateData();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.apigen.ui.customizer.ApiGen"); // NOI18N
    }

    private String getTarget() {
        return targetTextField.getText().trim();
    }

    private String getTitle() {
        return titleTextField.getText().trim();
    }

    @NbBundle.Messages({
        "ApiGenPanel.error.invalidTitle=Title must be provided.",
        "ApiGenPanel.warn.nbWillAskForDir=NetBeans will ask for the directory before generating documentation."
    })
    void validateData() {
        // errors
        String target = getTarget();
        if (StringUtils.hasText(target)) {
            String error = FileUtils.validateDirectory(getTarget(), true);
            if (error != null) {
                category.setErrorMessage(error);
                category.setValid(false);
                return;
            }
        }
        if (!StringUtils.hasText(getTitle())) {
            category.setErrorMessage(Bundle.ApiGenPanel_error_invalidTitle());
            category.setValid(false);
            return;
        }

        // warnings
        String warning = null;
        if (!StringUtils.hasText(target)) {
            warning = Bundle.ApiGenPanel_warn_nbWillAskForDir();
        }

        category.setErrorMessage(warning);
        category.setValid(true);
    }

    void storeData() {
        ApiGenPreferences.setTarget(phpModule, getTarget());
        ApiGenPreferences.setTitle(phpModule, getTitle());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        targetLabel = new JLabel();
        targetTextField = new JTextField();
        targetButton = new JButton();
        titleLabel = new JLabel();
        titleTextField = new JTextField();

        targetLabel.setLabelFor(targetTextField);
        Mnemonics.setLocalizedText(targetLabel, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.targetLabel.text")); // NOI18N
        targetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                targetButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(targetButton, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.targetButton.text")); // NOI18N
        targetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                targetButtonActionPerformed(evt);
            }
        });

        titleLabel.setLabelFor(titleTextField);
        Mnemonics.setLocalizedText(titleLabel, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.titleLabel.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(targetLabel)
                    .addComponent(titleLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(targetTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(targetButton))
                    .addComponent(titleTextField, GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(targetLabel)
                    .addComponent(targetTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(targetButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(titleLabel)
                    .addComponent(titleTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("ApiGenPanel.target.title=Select a directory for documentation")
    private void targetButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_targetButtonActionPerformed
        File target = new FileChooserBuilder(ApiGenProvider.lastDirFor(phpModule))
                .setTitle(Bundle.ApiGenPanel_target_title())
                .setDirectoriesOnly(true)
                .setFileHiding(true)
                .setDefaultWorkingDirectory(FileUtil.toFile(phpModule.getSourceDirectory()))
                .showOpenDialog();
        if (target != null) {
            target = FileUtil.normalizeFile(target);
            targetTextField.setText(target.getAbsolutePath());
        }
    }//GEN-LAST:event_targetButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton targetButton;
    private JLabel targetLabel;
    private JTextField targetTextField;
    private JLabel titleLabel;
    private JTextField titleTextField;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processChange();
        }

        private void processChange() {
            validateData();
        }

    }

}

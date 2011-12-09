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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
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

    private static final String SEPARATOR = ","; // NOI18N

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
        init();
    }

    private void init() {
        targetTextField.setText(ApiGenPreferences.getTarget(phpModule, false));
        titleTextField.setText(ApiGenPreferences.getTitle(phpModule));
        configTextField.setText(ApiGenPreferences.getConfig(phpModule));
        charsetsTextField.setText(StringUtils.implode(ApiGenPreferences.getCharsets(phpModule), SEPARATOR));

        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        targetTextField.getDocument().addDocumentListener(defaultDocumentListener);
        titleTextField.getDocument().addDocumentListener(defaultDocumentListener);
        configTextField.getDocument().addDocumentListener(defaultDocumentListener);
        charsetsTextField.getDocument().addDocumentListener(defaultDocumentListener);
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

    private String getConfig() {
        return configTextField.getText().trim();
    }

    private List<String> getCharsets() {
        String charsets = charsetsTextField.getText().trim();
        if (StringUtils.hasText(charsets)) {
            return StringUtils.explode(charsets, SEPARATOR);
        }
        return Collections.emptyList();
    }

    @NbBundle.Messages({
        "ApiGenPanel.error.relativeTarget=Absolute path for target directory must be provided.",
        "ApiGenPanel.error.invalidTitle=Title must be provided.",
        "ApiGenPanel.error.invalidCharsets=Charsets must be provided.",
        "ApiGenPanel.warn.nbWillAskForDir=NetBeans will ask for the directory before generating documentation.",
        "ApiGenPanel.warn.targetDirWillBeCreated=Target directory will be created.",
        "ApiGenPanel.warn.missingCharset=Project encoding ''{0}'' nout found within specified charsets.",
        "ApiGenPanel.warn.configNotNeon=Neon file is expected for configuration."
    })
    void validateData() {
        // errors
        // target
        String target = getTarget();
        if (StringUtils.hasText(target)) {
            File targetDir = new File(target);
            if (targetDir.exists()) {
                String error = FileUtils.validateDirectory(target, true);
                if (error != null) {
                    category.setErrorMessage(error);
                    category.setValid(false);
                    return;
                }
            } else {
                if (!targetDir.isAbsolute()) {
                    category.setErrorMessage(Bundle.ApiGenPanel_error_relativeTarget());
                    category.setValid(false);
                    return;
                }
            }
        }
        // title
        if (!StringUtils.hasText(getTitle())) {
            category.setErrorMessage(Bundle.ApiGenPanel_error_invalidTitle());
            category.setValid(false);
            return;
        }
        // config
        String config = getConfig();
        if (StringUtils.hasText(config)) {
            String error = FileUtils.validateFile(config, false);
            if (error != null) {
                category.setErrorMessage(error);
                category.setValid(false);
                return;
            }
        }
        // charsets
        if (getCharsets().isEmpty()) {
            category.setErrorMessage(Bundle.ApiGenPanel_error_invalidCharsets());
            category.setValid(false);
            return;
        }

        // warnings
        // charsets
        String defaultCharset = ApiGenPreferences.getDefaultCharset(phpModule);
        if (getCharsets().indexOf(defaultCharset) == -1) {
            category.setErrorMessage(Bundle.ApiGenPanel_warn_missingCharset(defaultCharset));
            category.setValid(true);
            return;
        }
        // target
        if (!StringUtils.hasText(target)) {
            category.setErrorMessage(Bundle.ApiGenPanel_warn_nbWillAskForDir());
            category.setValid(true);
            return;
        }
        if (!new File(target).exists()) {
            category.setErrorMessage(Bundle.ApiGenPanel_warn_targetDirWillBeCreated());
            category.setValid(true);
            return;
        }
        // config
        if (StringUtils.hasText(config)) {
            File configFile = new File(config);
            if (!configFile.getName().endsWith(".neon")) { // NOI18N
                category.setErrorMessage(Bundle.ApiGenPanel_warn_configNotNeon());
                category.setValid(true);
                return;
            }
        }

        // everything ok
        category.setErrorMessage(null);
        category.setValid(true);
    }

    void storeData() {
        ApiGenPreferences.setTarget(phpModule, getTarget());
        ApiGenPreferences.setTitle(phpModule, getTitle());
        ApiGenPreferences.setConfig(phpModule, getConfig());
        ApiGenPreferences.setCharsets(phpModule, getCharsets());
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
        configLabel = new JLabel();
        configTextField = new JTextField();
        configButton = new JButton();
        charsetsLabel = new JLabel();
        charsetsTextField = new JTextField();
        charsetsInfoLabel = new JLabel();

        targetLabel.setLabelFor(targetTextField);
        Mnemonics.setLocalizedText(targetLabel, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.targetLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(targetButton, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.targetButton.text")); // NOI18N
        targetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                targetButtonActionPerformed(evt);
            }
        });

        titleLabel.setLabelFor(titleTextField);
        Mnemonics.setLocalizedText(titleLabel, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.titleLabel.text")); // NOI18N

        configLabel.setLabelFor(configTextField);
        Mnemonics.setLocalizedText(configLabel, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.configLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(configButton, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.configButton.text")); // NOI18N
        configButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configButtonActionPerformed(evt);
            }
        });

        charsetsLabel.setLabelFor(charsetsTextField);
        Mnemonics.setLocalizedText(charsetsLabel, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.charsetsLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(charsetsInfoLabel, NbBundle.getMessage(ApiGenPanel.class, "ApiGenPanel.charsetsInfoLabel.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(targetLabel)
                    .addComponent(titleLabel)
                    .addComponent(configLabel)
                    .addComponent(charsetsLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(charsetsInfoLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(configTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(configButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(targetTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(targetButton))
                    .addComponent(titleTextField)
                    .addComponent(charsetsTextField)))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {configButton, targetButton});

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
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(configLabel)
                    .addComponent(configTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(configButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(charsetsLabel)
                    .addComponent(charsetsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(charsetsInfoLabel)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("ApiGenPanel.target.title=Select directory for documentation")
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

    @NbBundle.Messages("ApiGenPanel.config.title=Select configuration for documentation")
    private void configButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_configButtonActionPerformed
        File config = new FileChooserBuilder(ApiGenProvider.lastDirFor(phpModule))
                .setTitle(Bundle.ApiGenPanel_config_title())
                .setFilesOnly(true)
                .setDefaultWorkingDirectory(FileUtil.toFile(phpModule.getSourceDirectory()))
                .showOpenDialog();
        if (config != null) {
            config = FileUtil.normalizeFile(config);
            configTextField.setText(config.getAbsolutePath());
        }
    }//GEN-LAST:event_configButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel charsetsInfoLabel;
    private JLabel charsetsLabel;
    private JTextField charsetsTextField;
    private JButton configButton;
    private JLabel configLabel;
    private JTextField configTextField;
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

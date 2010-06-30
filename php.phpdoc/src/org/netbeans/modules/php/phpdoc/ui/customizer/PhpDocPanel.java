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

package org.netbeans.modules.php.phpdoc.ui.customizer;

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
import org.netbeans.modules.php.phpdoc.PhpDocumentorProvider;
import org.netbeans.modules.php.phpdoc.ui.PhpDocPreferences;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

final class PhpDocPanel extends JPanel {
    private static final long serialVersionUID = 5643218762231L;

    private final Category category;
    private final PhpModule phpModule;

    PhpDocPanel(Category category, PhpModule phpModule) {
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

        dirTextField.setText(PhpDocPreferences.getPhpDocDir(phpModule, false));
        titleTextField.setText(PhpDocPreferences.getPhpDocTitle(phpModule));

        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        dirTextField.getDocument().addDocumentListener(defaultDocumentListener);
        titleTextField.getDocument().addDocumentListener(defaultDocumentListener);
        validateData();
    }

    private String getPhpDocDir() {
        return dirTextField.getText().trim();
    }

    private String getPhpDocTitle() {
        return titleTextField.getText().trim();
    }

    void validateData() {
        // errors
        String phpDocDir = getPhpDocDir();
        if (StringUtils.hasText(phpDocDir)) {
            String error = FileUtils.validateDirectory(getPhpDocDir());
            if (error != null) {
                category.setErrorMessage(error);
                category.setValid(false);
                return;
            }
        }
        if (!StringUtils.hasText(getPhpDocTitle())) {
            category.setErrorMessage(NbBundle.getMessage(PhpDocPanel.class, "MSG_InvalidTitle"));
            category.setValid(false);
            return;
        }

        // warnings
        String warning = null;
        if (!StringUtils.hasText(phpDocDir)) {
            warning = NbBundle.getMessage(PhpDocPanel.class, "MSG_NbWillAskForDir");
        }

        category.setErrorMessage(warning);
        category.setValid(true);
    }

    void storeData() {
        String phpDocDir = getPhpDocDir();
        if (StringUtils.hasText(phpDocDir)) {
            PhpDocPreferences.setPhpDocDir(phpModule, phpDocDir);
        }
        PhpDocPreferences.setPhpDocTitle(phpModule, getPhpDocTitle());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dirLabel = new JLabel();
        dirTextField = new JTextField();
        dirButton = new JButton();
        titleLabel = new JLabel();
        titleTextField = new JTextField();

        dirLabel.setLabelFor(dirTextField);

        Mnemonics.setLocalizedText(dirLabel, NbBundle.getMessage(PhpDocPanel.class, "PhpDocPanel.dirLabel.text"));
        Mnemonics.setLocalizedText(dirButton, NbBundle.getMessage(PhpDocPanel.class, "PhpDocPanel.dirButton.text"));
        dirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dirButtonActionPerformed(evt);
            }
        });

        titleLabel.setLabelFor(titleTextField);

        Mnemonics.setLocalizedText(titleLabel, NbBundle.getMessage(PhpDocPanel.class, "PhpDocPanel.titleLabel.text"));
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(dirLabel)
                    .addComponent(titleLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dirTextField, GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(dirButton))
                    .addComponent(titleTextField, GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(dirLabel)
                    .addComponent(dirTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(dirButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(titleLabel)
                    .addComponent(titleTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void dirButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_dirButtonActionPerformed
        File phpDocDir = new FileChooserBuilder(PhpDocumentorProvider.class.getName() + PhpDocumentorProvider.PHPDOC_LAST_FOLDER_SUFFIX + phpModule.getName())
                .setTitle(NbBundle.getMessage(PhpDocPanel.class, "LBL_SelectDocFolder"))
                .setDirectoriesOnly(true)
                .setFileHiding(true)
                .setDefaultWorkingDirectory(FileUtil.toFile(phpModule.getSourceDirectory()))
                .showOpenDialog();
        if (phpDocDir != null) {
            phpDocDir = FileUtil.normalizeFile(phpDocDir);
            dirTextField.setText(phpDocDir.getAbsolutePath());
        }
    }//GEN-LAST:event_dirButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton dirButton;
    private JLabel dirLabel;
    private JTextField dirTextField;
    private JLabel titleLabel;
    private JTextField titleTextField;
    // End of variables declaration//GEN-END:variables

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

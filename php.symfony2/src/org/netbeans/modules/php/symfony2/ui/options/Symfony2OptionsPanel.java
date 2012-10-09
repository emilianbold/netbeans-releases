/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.symfony2.ui.options;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Panel for SYmfony2 options.
 */
@OptionsPanelController.Keywords(keywords={"php", "symfony", "symfony2", "framework", "sf", "sf2"}, location=UiUtils.OPTIONS_PATH, tabTitle= "#LBL_OptionsName")
@NbBundle.Messages("LBL_ZipFilesFilter=Zip File (*.zip)")
public class Symfony2OptionsPanel extends JPanel {

    private static final long serialVersionUID = -4674683641321L;
    private static final String SANDBOX_LAST_FOLDER_SUFFIX = ".sandbox"; // NOI18N
    private static final FileFilter ZIP_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            return f.isFile()
                    && f.getName().toLowerCase().endsWith(".zip"); // NOI18N
        }
        @Override
        public String getDescription() {
            return Bundle.LBL_ZipFilesFilter();
        }
    };

    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public Symfony2OptionsPanel() {
        initComponents();
        errorLabel.setText(" "); // NOI18N

        initListeners();
    }

    private void initListeners() {
        sandboxTextField.getDocument().addDocumentListener(new DocumentListener() {
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
                fireChange();
            }
        });
        ignoreCacheCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                fireChange();
            }
        });
    }

    public String getSandbox() {
        return sandboxTextField.getText();
    }

    public void setSandbox(String sandbox) {
        sandboxTextField.setText(sandbox);
    }

    public boolean getIgnoreCache() {
        return ignoreCacheCheckBox.isSelected();
    }

    public void setIgnoreCache(boolean ignoreCache) {
        ignoreCacheCheckBox.setSelected(ignoreCache);
    }

    public void setError(String message) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.errorForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public void setWarning(String message) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.warningForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sandboxLabel = new JLabel();
        sandboxTextField = new JTextField();
        sandboxBrowseButton = new JButton();
        sandboxInfoLabel = new JLabel();
        ignoreCacheCheckBox = new JCheckBox();
        errorLabel = new JLabel();
        noteLabel = new JLabel();
        downloadLabel = new JLabel();

        sandboxLabel.setLabelFor(sandboxTextField);
        Mnemonics.setLocalizedText(sandboxLabel, NbBundle.getMessage(Symfony2OptionsPanel.class, "Symfony2OptionsPanel.sandboxLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(sandboxBrowseButton, NbBundle.getMessage(Symfony2OptionsPanel.class, "Symfony2OptionsPanel.sandboxBrowseButton.text")); // NOI18N
        sandboxBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sandboxBrowseButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(sandboxInfoLabel, NbBundle.getMessage(Symfony2OptionsPanel.class, "Symfony2OptionsPanel.sandboxInfoLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(ignoreCacheCheckBox, NbBundle.getMessage(Symfony2OptionsPanel.class, "Symfony2OptionsPanel.ignoreCacheCheckBox.text")); // NOI18N
        Mnemonics.setLocalizedText(errorLabel, "ERROR"); // NOI18N
        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(Symfony2OptionsPanel.class, "Symfony2OptionsPanel.noteLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(downloadLabel, NbBundle.getMessage(Symfony2OptionsPanel.class, "Symfony2OptionsPanel.downloadLabel.text")); // NOI18N
        downloadLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                downloadLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                downloadLabelMousePressed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
                .addComponent(sandboxLabel)

                .addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()

                        .addComponent(sandboxInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(0, 0, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup()
                        .addComponent(sandboxTextField)

                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(sandboxBrowseButton)))).addGroup(layout.createSequentialGroup()

                .addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(ignoreCacheCheckBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(errorLabel).addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(0, 0, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup()
                .addContainerGap()

                .addComponent(downloadLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()

                .addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(sandboxLabel).addComponent(sandboxTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(sandboxBrowseButton)).addPreferredGap(ComponentPlacement.RELATED).addComponent(sandboxInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addComponent(ignoreCacheCheckBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(downloadLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED, 125, Short.MAX_VALUE).addComponent(errorLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("LBL_SelectSandbox=Select Symfony Standard Edition (.zip)")
    private void sandboxBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_sandboxBrowseButtonActionPerformed
        File sandbox = new FileChooserBuilder(Symfony2OptionsPanel.class.getName() + SANDBOX_LAST_FOLDER_SUFFIX)
                .setTitle(Bundle.LBL_SelectSandbox())
                .setFilesOnly(true)
                .setFileFilter(ZIP_FILE_FILTER)
                .showOpenDialog();
        if (sandbox != null) {
            sandbox = FileUtil.normalizeFile(sandbox);
            sandboxTextField.setText(sandbox.getAbsolutePath());
        }
    }//GEN-LAST:event_sandboxBrowseButtonActionPerformed

    private void downloadLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_downloadLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_downloadLabelMouseEntered

    private void downloadLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_downloadLabelMousePressed
        try {
            URL url = new URL("http://symfony.com/download"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_downloadLabelMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel downloadLabel;
    private JLabel errorLabel;
    private JCheckBox ignoreCacheCheckBox;
    private JLabel noteLabel;
    private JButton sandboxBrowseButton;
    private JLabel sandboxInfoLabel;
    private JLabel sandboxLabel;
    private JTextField sandboxTextField;
    // End of variables declaration//GEN-END:variables
}

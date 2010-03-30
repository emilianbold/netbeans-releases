/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.zend.ui.options;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.zend.ZendScript;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public final class ZendOptionsPanel extends JPanel {
    private static final long serialVersionUID = -13564875423210L;
    private static final String ZEND_LAST_FOLDER_SUFFIX = ".zend";

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public ZendOptionsPanel() {
        initComponents();

        // hide default options
        defaultParametersLabel.setVisible(false);
        defaultParametersForProjectTextField.setVisible(false);

        // not set in Design because of windows (panel too wide then)
        zendScriptUsageLabel.setText(NbBundle.getMessage(ZendOptionsPanel.class, "LBL_ZendUsage"));
        errorLabel.setText(" "); // NOI18N

        zendTextField.getDocument().addDocumentListener(new DocumentListener() {
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

        providerRegistrationButton.setEnabled(ZendScript.validate(getZend()) == null);
    }

    public String getZend() {
        return zendTextField.getText();
    }

    public void setZend(String zend) {
        zendTextField.setText(zend);
    }

    public String getDefaultParamsForProject() {
        return defaultParametersForProjectTextField.getText();
    }

    public void setDefaultParamsForProject(String params) {
        defaultParametersForProjectTextField.setText(params);
    }

    public void setError(String message) {
        providerRegistrationButton.setEnabled(false);
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.errorForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public void setWarning(String message) {
        providerRegistrationButton.setEnabled(false);
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.warningForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public void clearError() {
        setError(" "); // NOI18N
        providerRegistrationButton.setEnabled(true);
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        zendLabel = new JLabel();
        zendTextField = new JTextField();
        browseButton = new JButton();
        searchButton = new JButton();
        zendScriptUsageLabel = new JLabel();
        providerRegistrationInfoLabel = new JLabel();
        providerRegistrationButton = new JButton();
        defaultParametersLabel = new JLabel();
        defaultParametersForProjectTextField = new JTextField();
        noteLabel = new JLabel();
        includePathInfoLabel = new JLabel();
        installationInfoLabel = new JLabel();
        learnMoreLabel = new JLabel();
        errorLabel = new JLabel();

        setFocusTraversalPolicy(null);

        zendLabel.setLabelFor(zendTextField);

        Mnemonics.setLocalizedText(zendLabel, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.zendLabel.text"));
        Mnemonics.setLocalizedText(browseButton, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.browseButton.text"));
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.searchButton.text"));
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        zendScriptUsageLabel.setLabelFor(this);


        Mnemonics.setLocalizedText(zendScriptUsageLabel, "HINT");
        Mnemonics.setLocalizedText(providerRegistrationInfoLabel, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.providerRegistrationInfoLabel.text"));
        Mnemonics.setLocalizedText(providerRegistrationButton, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.providerRegistrationButton.text"));
        providerRegistrationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                providerRegistrationButtonActionPerformed(evt);
            }
        });

        defaultParametersLabel.setLabelFor(defaultParametersForProjectTextField);
        Mnemonics.setLocalizedText(defaultParametersLabel, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.defaultParametersLabel.text")); // NOI18N

        noteLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.noteLabel.text")); // NOI18N

        includePathInfoLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(includePathInfoLabel, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.includePathInfoLabel.text")); // NOI18N

        installationInfoLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(installationInfoLabel, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.installationInfoLabel.text")); // NOI18N

        learnMoreLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(learnMoreLabel, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.learnMoreLabel.text"));
        learnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                learnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                learnMoreLabelMousePressed(evt);
            }
        });

        errorLabel.setLabelFor(this);

        Mnemonics.setLocalizedText(errorLabel, "ERROR");
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(includePathInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(95, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(installationInfoLabel)
                .addContainerGap(186, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(466, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(providerRegistrationInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(defaultParametersLabel)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(defaultParametersForProjectTextField, GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                            .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(errorLabel)
                            .addGap(447, 447, 447))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(zendLabel)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(zendScriptUsageLabel)
                                    .addContainerGap())
                                .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                                        .addComponent(providerRegistrationButton)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(zendTextField, GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
                                            .addPreferredGap(ComponentPlacement.RELATED)
                                            .addComponent(browseButton)
                                            .addPreferredGap(ComponentPlacement.RELATED)
                                            .addComponent(searchButton)))
                                    .addGap(0, 0, 0))))
                        .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {browseButton, searchButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(zendLabel)
                    .addComponent(zendTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton)
                    .addComponent(browseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(zendScriptUsageLabel)
                        .addGap(18, 18, 18)
                        .addComponent(providerRegistrationInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(providerRegistrationButton))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(defaultParametersLabel)
                    .addComponent(defaultParametersForProjectTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(includePathInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(installationInfoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addComponent(errorLabel)
                .addGap(0, 0, 0))
        );

        zendLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.zendLabel.AccessibleContext.accessibleName")); // NOI18N
        zendLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.zendLabel.AccessibleContext.accessibleDescription")); // NOI18N
        zendTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.zendTextField.AccessibleContext.accessibleName")); // NOI18N
        zendTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.zendTextField.AccessibleContext.accessibleDescription")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.browseButton.AccessibleContext.accessibleName")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.browseButton.AccessibleContext.accessibleDescription")); // NOI18N
        searchButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.searchButton.AccessibleContext.accessibleName")); // NOI18N
        searchButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.searchButton.AccessibleContext.accessibleDescription")); // NOI18N
        zendScriptUsageLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.zendScriptUsageLabel.AccessibleContext.accessibleName")); // NOI18N
        zendScriptUsageLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.zendScriptUsageLabel.AccessibleContext.accessibleDescription")); // NOI18N
        defaultParametersLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.defaultParametersLabel.AccessibleContext.accessibleName")); // NOI18N
        defaultParametersLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.defaultParametersLabel.AccessibleContext.accessibleDescription")); // NOI18N
        defaultParametersForProjectTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.defaultParametersForProjectTextField.AccessibleContext.accessibleName")); // NOI18N
        defaultParametersForProjectTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.defaultParametersForProjectTextField.AccessibleContext.accessibleDescription")); // NOI18N
        noteLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.noteLabel.AccessibleContext.accessibleName")); // NOI18N
        noteLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.noteLabel.AccessibleContext.accessibleDescription")); // NOI18N
        includePathInfoLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.includePathInfoLabel.AccessibleContext.accessibleName")); // NOI18N
        includePathInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.includePathInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        installationInfoLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.installationInfoLabel.AccessibleContext.accessibleName")); // NOI18N
        installationInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.installationInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        learnMoreLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.learnMoreLabel.AccessibleContext.accessibleName")); // NOI18N
        learnMoreLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.learnMoreLabel.AccessibleContext.accessibleDescription")); // NOI18N
        errorLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.errorLabel.AccessibleContext.accessibleName")); // NOI18N
        errorLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.errorLabel.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        File zendScript = new FileChooserBuilder(ZendOptionsPanel.class.getName() + ZEND_LAST_FOLDER_SUFFIX)
                .setTitle(NbBundle.getMessage(ZendOptionsPanel.class, "LBL_SelectZend"))
                .setFilesOnly(true)
                .showOpenDialog();
        if (zendScript != null) {
            zendScript = FileUtil.normalizeFile(zendScript);
            zendTextField.setText(zendScript.getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void searchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
         String zendScript = UiUtils.SearchWindow.search(new UiUtils.SearchWindow.SearchWindowSupport() {
            @Override
            public List<String> detect() {
                return FileUtils.findFileOnUsersPath(ZendScript.SCRIPT_NAME);
            }

            @Override
            public String getWindowTitle() {
                return NbBundle.getMessage(ZendOptionsPanel.class, "LBL_ZendScriptsTitle");
            }

            @Override
            public String getListTitle() {
                return NbBundle.getMessage(ZendOptionsPanel.class, "LBL_ZendScripts");
            }

            @Override
            public String getPleaseWaitPart() {
                return NbBundle.getMessage(ZendOptionsPanel.class, "LBL_ZendScriptsPleaseWaitPart");
            }

            @Override
            public String getNoItemsFound() {
                return NbBundle.getMessage(ZendOptionsPanel.class, "LBL_NoZendScriptsFound");
            }
        });
        if (zendScript != null) {
            zendTextField.setText(zendScript);
        }
    }//GEN-LAST:event_searchButtonActionPerformed

    private void learnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_learnMoreLabelMouseEntered

    private void learnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMousePressed
        try {
            URL url = new URL("http://framework.zend.com/manual/en/introduction.installation.html"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_learnMoreLabelMousePressed

    private void providerRegistrationButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_providerRegistrationButtonActionPerformed
        // #180347
        boolean register = true;
        final String zendScript = getZend();
        if (!zendScript.equals(ZendOptions.getInstance().getZend())) {
            NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(ZendOptionsPanel.class, "MSG_RegisterUsingUnsavedZendScript", zendScript),
                    NotifyDescriptor.YES_NO_OPTION);
            register = DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION;
        }
        if (register) {
            ZendScript.registerNetBeansProvider(zendScript);
        }
    }//GEN-LAST:event_providerRegistrationButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton browseButton;
    private JTextField defaultParametersForProjectTextField;
    private JLabel defaultParametersLabel;
    private JLabel errorLabel;
    private JLabel includePathInfoLabel;
    private JLabel installationInfoLabel;
    private JLabel learnMoreLabel;
    private JLabel noteLabel;
    private JButton providerRegistrationButton;
    private JLabel providerRegistrationInfoLabel;
    private JButton searchButton;
    private JLabel zendLabel;
    private JLabel zendScriptUsageLabel;
    private JTextField zendTextField;
    // End of variables declaration//GEN-END:variables

}

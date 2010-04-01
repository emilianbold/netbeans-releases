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

/*
 * SymfonyOptionsPanel.java
 *
 * Created on 11.6.2009, 13:02:10
 */

package org.netbeans.modules.php.symfony.ui.options;

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
import org.netbeans.modules.php.symfony.SymfonyScript;
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
public class SymfonyOptionsPanel extends JPanel {
    private static final long serialVersionUID = -1384644114740L;
    private static final String SYMFONY_LAST_FOLDER_SUFFIX = ".symfony";

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public SymfonyOptionsPanel() {
        initComponents();

        // not set in Design because of windows (panel too wide then)
        symfonyScriptUsageLabel.setText(NbBundle.getMessage(SymfonyOptionsPanel.class, "LBL_SymfonyUsage"));
        errorLabel.setText(" "); // NOI18N

        symfonyTextField.getDocument().addDocumentListener(new DocumentListener() {
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
    }

    public String getSymfony() {
        return symfonyTextField.getText();
    }

    public void setSymfony(String symfony) {
        symfonyTextField.setText(symfony);
    }

    public String getDefaultParamsForProject() {
        return defaultParametersForProjectTextField.getText();
    }

    public void setDefaultParamsForProject(String params) {
        defaultParametersForProjectTextField.setText(params);
    }

    public String getDefaultParamsForApps() {
        return defaultParametersForAppsTextField.getText();
    }

    public void setDefaultParamsForApps(String params) {
        defaultParametersForAppsTextField.setText(params);
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        symfonyLabel = new JLabel();
        symfonyTextField = new JTextField();
        browseButton = new JButton();
        searchButton = new JButton();
        symfonyScriptUsageLabel = new JLabel();
        runningInfoLabel = new JLabel();
        defaultParametersLabel = new JLabel();
        defaultParametersForProjectLabel = new JLabel();
        defaultParametersForProjectTextField = new JTextField();
        jLabel1 = new JLabel();
        defaultParametersForAppsLabel = new JLabel();
        defaultParametersForAppsTextField = new JTextField();
        noteLabel = new JLabel();
        includePathInfoLabel = new JLabel();
        installationInfoLabel = new JLabel();
        learnMoreLabel = new JLabel();
        errorLabel = new JLabel();

        setFocusTraversalPolicy(null);

        symfonyLabel.setLabelFor(symfonyTextField);

        Mnemonics.setLocalizedText(symfonyLabel, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.symfonyLabel.text"));
        Mnemonics.setLocalizedText(browseButton, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.browseButton.text"));
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.searchButton.text"));
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        symfonyScriptUsageLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(symfonyScriptUsageLabel, "HINT"); // NOI18N

        runningInfoLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(runningInfoLabel, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.runningInfoLabel.text")); // NOI18N

        defaultParametersLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(defaultParametersLabel, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.defaultParametersLabel.text")); // NOI18N

        defaultParametersForProjectLabel.setLabelFor(defaultParametersForProjectTextField);
        Mnemonics.setLocalizedText(defaultParametersForProjectLabel, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.defaultParametersForProjectLabel.text")); // NOI18N

        jLabel1.setLabelFor(this);
        Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.jLabel1.text")); // NOI18N

        defaultParametersForAppsLabel.setLabelFor(defaultParametersForAppsTextField);
        Mnemonics.setLocalizedText(defaultParametersForAppsLabel, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.defaultParametersForAppsLabel.text")); // NOI18N

        noteLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.noteLabel.text")); // NOI18N

        includePathInfoLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(includePathInfoLabel, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.includePathInfoLabel.text")); // NOI18N

        installationInfoLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(installationInfoLabel, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.installationInfoLabel.text")); // NOI18N

        learnMoreLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(learnMoreLabel, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.learnMoreLabel.text"));
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
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(includePathInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(138, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(installationInfoLabel)
                .addContainerGap(206, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(learnMoreLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(509, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(defaultParametersLabel)
                        .addContainerGap())
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(runningInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(errorLabel)
                                .addGap(447, 447, 447))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(symfonyLabel)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(symfonyScriptUsageLabel)
                                        .addContainerGap())
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(symfonyTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(browseButton)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(searchButton)
                                        .addGap(0, 0, 0)))))
                        .addComponent(noteLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(defaultParametersForAppsLabel)
                    .addComponent(defaultParametersForProjectLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(defaultParametersForProjectTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                            .addComponent(defaultParametersForAppsTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE))
                        .addGap(0, 0, 0))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {browseButton, searchButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(symfonyLabel)
                    .addComponent(symfonyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton)
                    .addComponent(browseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(symfonyScriptUsageLabel)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(runningInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(defaultParametersLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(defaultParametersForProjectLabel)
                    .addComponent(defaultParametersForProjectTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(defaultParametersForAppsLabel)
                    .addComponent(defaultParametersForAppsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(noteLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(includePathInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(installationInfoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(learnMoreLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorLabel)
                .addGap(0, 0, 0))
        );

        symfonyLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.symfonyLabel.AccessibleContext.accessibleName")); // NOI18N
        symfonyLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.symfonyLabel.AccessibleContext.accessibleDescription")); // NOI18N
        symfonyTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.symfonyTextField.AccessibleContext.accessibleName")); // NOI18N
        symfonyTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.symfonyTextField.AccessibleContext.accessibleDescription")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.browseButton.AccessibleContext.accessibleName")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.browseButton.AccessibleContext.accessibleDescription")); // NOI18N
        searchButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.searchButton.AccessibleContext.accessibleName")); // NOI18N
        searchButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.searchButton.AccessibleContext.accessibleDescription")); // NOI18N
        symfonyScriptUsageLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.symfonyScriptUsageLabel.AccessibleContext.accessibleName")); // NOI18N
        symfonyScriptUsageLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.symfonyScriptUsageLabel.AccessibleContext.accessibleDescription")); // NOI18N
        runningInfoLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.runningInfoLabel.AccessibleContext.accessibleName")); // NOI18N
        runningInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.runningInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        defaultParametersLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.defaultParametersLabel.AccessibleContext.accessibleName")); // NOI18N
        defaultParametersLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.defaultParametersLabel.AccessibleContext.accessibleDescription")); // NOI18N
        defaultParametersForProjectLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.defaultParametersForProjectLabel.AccessibleContext.accessibleName")); // NOI18N
        defaultParametersForProjectLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.defaultParametersForProjectLabel.AccessibleContext.accessibleDescription")); // NOI18N
        defaultParametersForProjectTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.defaultParametersForProjectTextField.AccessibleContext.accessibleName")); // NOI18N
        defaultParametersForProjectTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.defaultParametersForProjectTextField.AccessibleContext.accessibleDescription")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.jLabel1.AccessibleContext.accessibleName")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.jLabel1.AccessibleContext.accessibleDescription")); // NOI18N
        defaultParametersForAppsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.defaultParametersForAppsLabel.AccessibleContext.accessibleName")); // NOI18N
        defaultParametersForAppsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.defaultParametersForAppsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        defaultParametersForAppsTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.defaultParametersForAppsTextField.AccessibleContext.accessibleName")); // NOI18N
        defaultParametersForAppsTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.defaultParametersForAppsTextField.AccessibleContext.accessibleDescription")); // NOI18N
        noteLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.noteLabel.AccessibleContext.accessibleName")); // NOI18N
        noteLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.noteLabel.AccessibleContext.accessibleDescription")); // NOI18N
        includePathInfoLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.includePathInfoLabel.AccessibleContext.accessibleName")); // NOI18N
        includePathInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.includePathInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        installationInfoLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.installationInfoLabel.AccessibleContext.accessibleName")); // NOI18N
        installationInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.installationInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        learnMoreLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.learnMoreLabel.AccessibleContext.accessibleName")); // NOI18N
        learnMoreLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.learnMoreLabel.AccessibleContext.accessibleDescription")); // NOI18N
        errorLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.errorLabel.AccessibleContext.accessibleName")); // NOI18N
        errorLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.errorLabel.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        File symfonyScript = new FileChooserBuilder(SymfonyOptionsPanel.class.getName() + SYMFONY_LAST_FOLDER_SUFFIX)
                .setTitle(NbBundle.getMessage(SymfonyOptionsPanel.class, "LBL_SelectSymfony"))
                .setFilesOnly(true)
                .showOpenDialog();
        if (symfonyScript != null) {
            symfonyScript = FileUtil.normalizeFile(symfonyScript);
            symfonyTextField.setText(symfonyScript.getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void searchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
         String symfonyScript = UiUtils.SearchWindow.search(new UiUtils.SearchWindow.SearchWindowSupport() {
            @Override
            public List<String> detect() {
                return FileUtils.findFileOnUsersPath(SymfonyScript.SCRIPT_NAME);
            }

            @Override
            public String getWindowTitle() {
                return NbBundle.getMessage(SymfonyOptionsPanel.class, "LBL_SymfonyScriptsTitle");
            }

            @Override
            public String getListTitle() {
                return NbBundle.getMessage(SymfonyOptionsPanel.class, "LBL_SymfonyScripts");
            }

            @Override
            public String getPleaseWaitPart() {
                return NbBundle.getMessage(SymfonyOptionsPanel.class, "LBL_SymfonyScriptsPleaseWaitPart");
            }

            @Override
            public String getNoItemsFound() {
                return NbBundle.getMessage(SymfonyOptionsPanel.class, "LBL_NoSymfonyScriptsFound");
            }
        });
        if (symfonyScript != null) {
            symfonyTextField.setText(symfonyScript);
        }
    }//GEN-LAST:event_searchButtonActionPerformed

    private void learnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_learnMoreLabelMouseEntered

    private void learnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMousePressed
        try {
            URL url = new URL("http://www.symfony-project.org/installation"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_learnMoreLabelMousePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton browseButton;
    private JLabel defaultParametersForAppsLabel;
    private JTextField defaultParametersForAppsTextField;
    private JLabel defaultParametersForProjectLabel;
    private JTextField defaultParametersForProjectTextField;
    private JLabel defaultParametersLabel;
    private JLabel errorLabel;
    private JLabel includePathInfoLabel;
    private JLabel installationInfoLabel;
    private JLabel jLabel1;
    private JLabel learnMoreLabel;
    private JLabel noteLabel;
    private JLabel runningInfoLabel;
    private JButton searchButton;
    private JLabel symfonyLabel;
    private JLabel symfonyScriptUsageLabel;
    private JTextField symfonyTextField;
    // End of variables declaration//GEN-END:variables

}

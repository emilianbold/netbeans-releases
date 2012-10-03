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
package org.netbeans.modules.hudson.php.ui.options;

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
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.hudson.php.commands.PpwScript;
import org.netbeans.modules.hudson.php.options.HudsonOptions;
import org.netbeans.modules.hudson.php.options.HudsonOptionsValidator;
import org.netbeans.modules.php.api.util.FileUtils;
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
 *
 */
@OptionsPanelController.Keywords(keywords={"php hudson"}, location=UiUtils.OPTIONS_PATH, tabTitle= "#LBL_OptionsName")
public class HudsonOptionsPanel extends JPanel {

    private static final long serialVersionUID = -464132465732132L;

    private static final String PPW_LAST_FOLDER_SUFFIX = ".ppw";
    private static final String JOB_CONFIG_LAST_FOLDER_SUFFIX = ".jobConfig";
    private static final String DEFAULT_JOB_CONFIG = HudsonOptions.getInstance().getDefaultJobConfig();

    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public HudsonOptionsPanel() {
        initComponents();
        init();
    }

    @NbBundle.Messages({
        "# {0} - short script name",
        "# {1} - long script name",
        "LBL_PpwHint=Full path of PPW script (typically {0} or {1}).",
        "TXT_PpwNote=<html>PHP Project Wizard (PPW) is used to generate the scripts and configuration files necessary for the build automation.</html>",
        "# {0} - path separator",
        "LBL_JobConfigHint=Full path of job config (typically <some-directory>{0}php-template{0}config.xml).",
        "TXT_JobConfigNote=<html>Template for Jenkins Jobs for PHP Projects is used for new job configurations.</html>"
    })
    private void init() {
        ppwHintLabel.setText(Bundle.LBL_PpwHint(PpwScript.SCRIPT_NAME, PpwScript.SCRIPT_NAME_LONG));
        ppwNoteLabel.setText(Bundle.TXT_PpwNote());
        jobConfigHintLabel.setText(Bundle.LBL_JobConfigHint(File.separator));
        jobConfigNoteLabel.setText(Bundle.TXT_JobConfigNote());
        errorLabel.setText(" "); // NOI18N
        checkDefaultButtonState();

        // listeners
        DocumentListener documentListener = new DefaultDocumentListener();
        ppwTextField.getDocument().addDocumentListener(documentListener);
        jobConfigTextField.getDocument().addDocumentListener(documentListener);
        jobConfigTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkDefaultButtonState();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                checkDefaultButtonState();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                checkDefaultButtonState();
            }
        });
    }

    public String getPpw() {
        return ppwTextField.getText();
    }

    public void setPpw(String ppw) {
        ppwTextField.setText(ppw);
    }

    public String getJobConfig() {
        return jobConfigTextField.getText();
    }

    public void setJobConfig(String jobConfig) {
        jobConfigTextField.setText(jobConfig);
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

    void checkDefaultButtonState() {
        jobConfigDefaultButton.setEnabled(!DEFAULT_JOB_CONFIG.equals(getJobConfig()));
    }


    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ppwLabel = new JLabel();
        ppwTextField = new JTextField();
        ppwBrowseButton = new JButton();
        ppwSearchButton = new JButton();
        ppwHintLabel = new JLabel();
        note1Label = new JLabel();
        ppwNoteLabel = new JLabel();
        ppwInstallationInfoLabel = new JLabel();
        ppwLearnMoreLabel = new JLabel();
        jobConfigLabel = new JLabel();
        jobConfigTextField = new JTextField();
        jobConfigBrowseButton = new JButton();
        jobConfigDefaultButton = new JButton();
        jobConfigHintLabel = new JLabel();
        note2Label = new JLabel();
        jobConfigNoteLabel = new JLabel();
        jobConfigInstallationInfoLabel = new JLabel();
        jobConfigLearnMoreLabel = new JLabel();
        jobConfigDownloadLabel = new JLabel();
        errorLabel = new JLabel();

        ppwLabel.setLabelFor(ppwTextField);
        Mnemonics.setLocalizedText(ppwLabel, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.ppwLabel.text")); // NOI18N
        ppwLearnMoreLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                ppwLearnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                ppwLearnMoreLabelMousePressed(evt);
            }
        });
        Mnemonics.setLocalizedText(ppwBrowseButton, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.ppwBrowseButton.text")); // NOI18N
        ppwBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ppwBrowseButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(ppwSearchButton, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.ppwSearchButton.text")); // NOI18N
        ppwSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ppwSearchButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(ppwHintLabel, "HINT"); // NOI18N
        Mnemonics.setLocalizedText(note1Label, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.note1Label.text")); // NOI18N
        Mnemonics.setLocalizedText(ppwNoteLabel, "PPW NOTE"); // NOI18N
        Mnemonics.setLocalizedText(ppwInstallationInfoLabel, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.ppwInstallationInfoLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(ppwLearnMoreLabel, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.ppwLearnMoreLabel.text")); // NOI18N
        ppwLearnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                ppwLearnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                ppwLearnMoreLabelMousePressed(evt);
            }
        });

        jobConfigLabel.setLabelFor(jobConfigTextField);
        Mnemonics.setLocalizedText(jobConfigLabel, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.jobConfigLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(jobConfigBrowseButton, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.jobConfigBrowseButton.text")); // NOI18N
        jobConfigBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jobConfigBrowseButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(jobConfigDefaultButton, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.jobConfigDefaultButton.text")); // NOI18N
        jobConfigDefaultButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jobConfigDefaultButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(jobConfigHintLabel, "HINT"); // NOI18N
        Mnemonics.setLocalizedText(note2Label, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.note2Label.text")); // NOI18N
        Mnemonics.setLocalizedText(jobConfigNoteLabel, "CONFIG NOTE"); // NOI18N
        Mnemonics.setLocalizedText(jobConfigInstallationInfoLabel, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.jobConfigInstallationInfoLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(jobConfigLearnMoreLabel, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.jobConfigLearnMoreLabel.text")); // NOI18N
        jobConfigLearnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                jobConfigLearnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                jobConfigLearnMoreLabelMousePressed(evt);
            }
        });
        Mnemonics.setLocalizedText(jobConfigDownloadLabel, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.jobConfigDownloadLabel.text")); // NOI18N
        jobConfigDownloadLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                jobConfigDownloadLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                jobConfigDownloadLabelMousePressed(evt);
            }
        });
        Mnemonics.setLocalizedText(errorLabel, "ERROR"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
                .addComponent(ppwLabel)

                .addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
                        .addComponent(ppwHintLabel)

                        .addGap(0, 0, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup()
                        .addComponent(ppwTextField)

                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(ppwBrowseButton).addPreferredGap(ComponentPlacement.RELATED).addComponent(ppwSearchButton)))).addGroup(layout.createSequentialGroup()

                .addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(errorLabel).addComponent(note1Label, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(0, 0, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup()

                .addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(jobConfigLabel).addComponent(note2Label, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
                        .addComponent(jobConfigHintLabel)

                        .addGap(0, 0, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup()
                        .addComponent(jobConfigTextField)

                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(jobConfigBrowseButton).addPreferredGap(ComponentPlacement.RELATED).addComponent(jobConfigDefaultButton)))).addGroup(layout.createSequentialGroup()
                .addContainerGap()

                .addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(ppwInstallationInfoLabel).addComponent(ppwLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(ppwNoteLabel).addComponent(jobConfigNoteLabel).addComponent(jobConfigInstallationInfoLabel).addComponent(jobConfigLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(jobConfigDownloadLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {jobConfigBrowseButton, jobConfigDefaultButton, ppwBrowseButton, ppwSearchButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()

                .addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(ppwTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(ppwLabel).addComponent(ppwBrowseButton).addComponent(ppwSearchButton)).addPreferredGap(ComponentPlacement.RELATED).addComponent(ppwHintLabel).addPreferredGap(ComponentPlacement.RELATED).addComponent(note1Label, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(ppwNoteLabel).addPreferredGap(ComponentPlacement.RELATED).addComponent(ppwInstallationInfoLabel).addPreferredGap(ComponentPlacement.RELATED).addComponent(ppwLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(jobConfigTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(jobConfigLabel).addComponent(jobConfigBrowseButton).addComponent(jobConfigDefaultButton)).addPreferredGap(ComponentPlacement.RELATED).addComponent(jobConfigHintLabel).addPreferredGap(ComponentPlacement.RELATED).addComponent(note2Label, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(jobConfigNoteLabel).addPreferredGap(ComponentPlacement.RELATED).addComponent(jobConfigInstallationInfoLabel).addPreferredGap(ComponentPlacement.RELATED).addComponent(jobConfigLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(jobConfigDownloadLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(errorLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ppwLearnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_ppwLearnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_ppwLearnMoreLabelMouseEntered

    private void ppwLearnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_ppwLearnMoreLabelMousePressed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("https://github.com/sebastianbergmann/php-project-wizard")); // NOI18N
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_ppwLearnMoreLabelMousePressed

    @NbBundle.Messages("LBL_SelectPpw=Select PPW script")
    private void ppwBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_ppwBrowseButtonActionPerformed
        File ppwScript = new FileChooserBuilder(HudsonOptionsPanel.class.getName() + PPW_LAST_FOLDER_SUFFIX)
                .setTitle(Bundle.LBL_SelectPpw())
                .setFilesOnly(true)
                .showOpenDialog();
        if (ppwScript != null) {
            ppwScript = FileUtil.normalizeFile(ppwScript);
            ppwTextField.setText(ppwScript.getAbsolutePath());
        }
    }//GEN-LAST:event_ppwBrowseButtonActionPerformed

    @NbBundle.Messages({
        "LBL_PpwScriptsTitle=PPW scripts",
        "LBL_PpwScripts=&PPW scripts:",
        "LBL_PpwScriptsPleaseWaitPart=PPW scripts",
        "LBL_NoPpwScriptsFound=No PPW scripts found."
    })
    private void ppwSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_ppwSearchButtonActionPerformed
        String ppwScript = UiUtils.SearchWindow.search(new UiUtils.SearchWindow.SearchWindowSupport() {
            @Override
            public List<String> detect() {
                return FileUtils.findFileOnUsersPath(PpwScript.SCRIPT_NAME, PpwScript.SCRIPT_NAME_LONG);
            }
            @Override
            public String getWindowTitle() {
                return Bundle.LBL_PpwScriptsTitle();
            }
            @Override
            public String getListTitle() {
                return Bundle.LBL_PpwScripts();
            }
            @Override
            public String getPleaseWaitPart() {
                return Bundle.LBL_PpwScriptsPleaseWaitPart();
            }
            @Override
            public String getNoItemsFound() {
                return Bundle.LBL_NoPpwScriptsFound();
            }
        });
        if (ppwScript != null) {
            ppwTextField.setText(ppwScript);
        }
    }//GEN-LAST:event_ppwSearchButtonActionPerformed

    private void jobConfigLearnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_jobConfigLearnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jobConfigLearnMoreLabelMouseEntered

    private void jobConfigLearnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_jobConfigLearnMoreLabelMousePressed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://jenkins-php.org/")); // NOI18N
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_jobConfigLearnMoreLabelMousePressed

    private void jobConfigDownloadLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_jobConfigDownloadLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jobConfigDownloadLabelMouseEntered

    private void jobConfigDownloadLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_jobConfigDownloadLabelMousePressed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(
                    new URL("https://github.com/sebastianbergmann/php-jenkins-template/blob/master/config.xml")); // NOI18N
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_jobConfigDownloadLabelMousePressed

    @NbBundle.Messages({
        "LBL_SelectJobConfig=Select job config (config.xml)",
        "TXT_JobConfigDesciption=Hudson job config file (config.xml)"
    })
    private void jobConfigBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jobConfigBrowseButtonActionPerformed
        File jobConfig = new FileChooserBuilder(HudsonOptionsPanel.class.getName() + JOB_CONFIG_LAST_FOLDER_SUFFIX)
                .setTitle(Bundle.LBL_SelectJobConfig())
                .setFilesOnly(true)
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        }
                        return HudsonOptionsValidator.JOB_CONFIG_NAME.equals(f.getName());
                    }
                    @Override
                    public String getDescription() {
                        return Bundle.TXT_JobConfigDesciption();
                    }
                }).showOpenDialog();
        if (jobConfig != null) {
            jobConfig = FileUtil.normalizeFile(jobConfig);
            jobConfigTextField.setText(jobConfig.getAbsolutePath());
        }
    }//GEN-LAST:event_jobConfigBrowseButtonActionPerformed

    private void jobConfigDefaultButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jobConfigDefaultButtonActionPerformed
        jobConfigTextField.setText(DEFAULT_JOB_CONFIG);
    }//GEN-LAST:event_jobConfigDefaultButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel errorLabel;
    private JButton jobConfigBrowseButton;
    private JButton jobConfigDefaultButton;
    private JLabel jobConfigDownloadLabel;
    private JLabel jobConfigHintLabel;
    private JLabel jobConfigInstallationInfoLabel;
    private JLabel jobConfigLabel;
    private JLabel jobConfigLearnMoreLabel;
    private JLabel jobConfigNoteLabel;
    private JTextField jobConfigTextField;
    private JLabel note1Label;
    private JLabel note2Label;
    private JButton ppwBrowseButton;
    private JLabel ppwHintLabel;
    private JLabel ppwInstallationInfoLabel;
    private JLabel ppwLabel;
    private JLabel ppwLearnMoreLabel;
    private JLabel ppwNoteLabel;
    private JButton ppwSearchButton;
    private JTextField ppwTextField;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultDocumentListener implements DocumentListener {

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

    }

}

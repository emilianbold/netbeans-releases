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
import org.netbeans.modules.hudson.php.commands.PpwScript;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
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
public class HudsonOptionsPanel extends JPanel {

    private static final long serialVersionUID = -464132465732132L;

    private static final String PPW_LAST_FOLDER_SUFFIX = ".ppw";

    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public HudsonOptionsPanel() {
        initComponents();
        init();
    }

    @NbBundle.Messages({
        "LBL_PpwHint=Full path of PPW script (typically {0} or {1}).",
        "TXT_PpwNote=<html>PHP Project Wizard (PPW) is used to generate the scripts and configuration files necessary for the build automation.</html>"
    })
    private void init() {
        ppwHintLabel.setText(Bundle.LBL_PpwHint(PpwScript.SCRIPT_NAME, PpwScript.SCRIPT_NAME_LONG));
        ppwNoteLabel.setText(Bundle.TXT_PpwNote());
        errorLabel.setText(" "); // NOI18N

        // listeners
        ppwTextField.getDocument().addDocumentListener(new DocumentListener() {
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

    public String getPpw() {
        return ppwTextField.getText();
    }

    public void setPpw(String ppw) {
        ppwTextField.setText(ppw);
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
        errorLabel = new JLabel();

        ppwLabel.setLabelFor(ppwTextField);
        Mnemonics.setLocalizedText(ppwLabel, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.ppwLabel.text")); // NOI18N

        ppwTextField.setText(NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.ppwTextField.text")); Mnemonics.setLocalizedText(ppwBrowseButton, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.ppwBrowseButton.text")); // NOI18N
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
        Mnemonics.setLocalizedText(errorLabel, "ERROR"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(errorLabel)
                    .addComponent(note1Label, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(ppwLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ppwHintLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ppwTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(ppwBrowseButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(ppwSearchButton))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(ppwInstallationInfoLabel)
                    .addComponent(ppwLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(ppwNoteLabel))
                .addContainerGap(118, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {ppwBrowseButton, ppwSearchButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(ppwTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(ppwLabel)
                    .addComponent(ppwBrowseButton)
                    .addComponent(ppwSearchButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(ppwHintLabel)
                .addGap(18, 18, 18)
                .addComponent(note1Label, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(ppwNoteLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(ppwInstallationInfoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(ppwLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED, 115, Short.MAX_VALUE)
                .addComponent(errorLabel))
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel errorLabel;
    private JLabel note1Label;
    private JButton ppwBrowseButton;
    private JLabel ppwHintLabel;
    private JLabel ppwInstallationInfoLabel;
    private JLabel ppwLabel;
    private JLabel ppwLearnMoreLabel;
    private JLabel ppwNoteLabel;
    private JButton ppwSearchButton;
    private JTextField ppwTextField;
    // End of variables declaration//GEN-END:variables
}

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

package org.netbeans.modules.php.project.phpunit;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.environment.PhpEnvironment;
import org.netbeans.modules.php.project.ui.Utils;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Tomas Mysik
 */
public class PhpUnitOptionsPanel extends JPanel {
    private static final long serialVersionUID = 1284325558169934603L;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public PhpUnitOptionsPanel() {
        initComponents();

        errorLabel.setText(" "); // NOI18N
        scriptInfoLabel.setText(NbBundle.getMessage(PhpUnitOptionsPanel.class,
                "LBL_PhpUnitScriptInfo", Utilities.isWindows() ? "bat" : "sh")); // NOI18N

        phpUnitTextField.getDocument().addDocumentListener(new DocumentListener() {

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

    public String getPhpUnit() {
        return phpUnitTextField.getText();
    }

    public void setPhpUnit(String phpUnit) {
        phpUnitTextField.setText(phpUnit);
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








        phpUnitLabel = new JLabel();
        phpUnitTextField = new JTextField();
        phpUnitBrowseButton = new JButton();
        phpUnitSearchButton = new JButton();
        scriptInfoLabel = new JLabel();
        noteLabel = new JLabel();
        phpUnitInfoLabel = new JLabel();
        phpUnitPhp53InfoLabel = new JLabel();
        installationInfoLabel = new JLabel();
        learnMoreLabel = new JLabel();
        errorLabel = new JLabel();
        Mnemonics.setLocalizedText(phpUnitLabel, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitLabel.text"));
        Mnemonics.setLocalizedText(phpUnitBrowseButton, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitBrowseButton.text"));
        phpUnitBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                phpUnitBrowseButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(phpUnitSearchButton, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitSearchButton.text"));
        phpUnitSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                phpUnitSearchButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(scriptInfoLabel, "HINT");
        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.noteLabel.text"));
        Mnemonics.setLocalizedText(phpUnitInfoLabel, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitInfoLabel.text"));
        Mnemonics.setLocalizedText(phpUnitPhp53InfoLabel, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitPhp53InfoLabel.text"));
        Mnemonics.setLocalizedText(installationInfoLabel, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.installationInfoLabel.text"));
        Mnemonics.setLocalizedText(learnMoreLabel, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.learnMoreLabel.text"));
        learnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                learnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                learnMoreLabelMousePressed(evt);
            }
        });
        Mnemonics.setLocalizedText(errorLabel, "ERROR");
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, 0)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(errorLabel)
                            .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(phpUnitLabel)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                    .addComponent(scriptInfoLabel)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(phpUnitTextField, GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(phpUnitBrowseButton)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(phpUnitSearchButton))))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(phpUnitInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(phpUnitPhp53InfoLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(installationInfoLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(phpUnitLabel)
                    .addComponent(phpUnitTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(phpUnitSearchButton)
                    .addComponent(phpUnitBrowseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(scriptInfoLabel)
                .addGap(18, 18, 18)
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(phpUnitInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(phpUnitPhp53InfoLabel)
                .addGap(18, 18, 18)
                .addComponent(installationInfoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addComponent(errorLabel)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void phpUnitBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_phpUnitBrowseButtonActionPerformed
        Utils.browsePhpUnit(this, phpUnitTextField);
}//GEN-LAST:event_phpUnitBrowseButtonActionPerformed

    private void phpUnitSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_phpUnitSearchButtonActionPerformed
        String phpUnit = UiUtils.SearchWindow.search(new UiUtils.SearchWindow.SearchWindowSupport() {

            @Override
            public List<String> detect() {
                return PhpEnvironment.get().getAllPhpUnits();
            }

            @Override
            public String getWindowTitle() {
                return NbBundle.getMessage(PhpUnitOptionsPanel.class, "LBL_PhpUnitsTitle");
            }

            @Override
            public String getListTitle() {
                return NbBundle.getMessage(PhpUnitOptionsPanel.class, "LBL_PhpUnits");
            }

            @Override
            public String getPleaseWaitPart() {
                return NbBundle.getMessage(PhpUnitOptionsPanel.class, "LBL_PhpUnitsPleaseWaitPart");
            }

            @Override
            public String getNoItemsFound() {
                return NbBundle.getMessage(PhpUnitOptionsPanel.class, "LBL_NoPhpUnitsFound");
            }
        });
        if (phpUnit != null) {
            phpUnitTextField.setText(phpUnit);
        }
}//GEN-LAST:event_phpUnitSearchButtonActionPerformed

    private void learnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
}//GEN-LAST:event_learnMoreLabelMouseEntered

    private void learnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMousePressed
        try {
            URL url = new URL("http://www.phpunit.de/manual/current/en/installation.html"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
}//GEN-LAST:event_learnMoreLabelMousePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel errorLabel;
    private JLabel installationInfoLabel;
    private JLabel learnMoreLabel;
    private JLabel noteLabel;
    private JButton phpUnitBrowseButton;
    private JLabel phpUnitInfoLabel;
    private JLabel phpUnitLabel;
    private JLabel phpUnitPhp53InfoLabel;
    private JButton phpUnitSearchButton;
    private JTextField phpUnitTextField;
    private JLabel scriptInfoLabel;
    // End of variables declaration//GEN-END:variables

}

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
 * SmartyOptionsPanel.java
 *
 * Created on 11.6.2009, 13:02:10
 */

package org.netbeans.modules.php.smarty.ui.options;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Martin Fousek
 */
public class SmartyOptionsPanel extends JPanel {
    private static final long serialVersionUID = -1384644114740L;

    private final transient ChangeSupport changeSupport = new ChangeSupport(this);

    public SmartyOptionsPanel() {
        initComponents();

        errorLabel.setText(" "); // NOI18N
        setDepthOfScanningComboBox(); //NOI18N

        setOpenDelimiter(SmartyOptions.getInstance().getDefaultOpenDelimiter());
        setCloseDelimiter(SmartyOptions.getInstance().getDefaultCloseDelimiter());
        setDepthOfScanning(SmartyOptions.getInstance().getScanningDepth());

        openDelimiterTextField.getDocument().addDocumentListener(new SmartyDocumentListener());
        closeDelimiterTextField.getDocument().addDocumentListener(new SmartyDocumentListener());
        depthOfScanningComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireChange();
            }
        });
    }

    private void setDepthOfScanningComboBox() {
        for (int i = 0; i < 4; i++) {
            depthOfScanningComboBox.addItem(String.valueOf(i));
        }
    }

    public String getCloseDelimiter() {
        return closeDelimiterTextField.getText();
    }

    public void setCloseDelimiter(String closeDelimiter) {
        this.closeDelimiterTextField.setText(closeDelimiter);
    }

    public String getOpenDelimiter() {
        return openDelimiterTextField.getText();
    }

    public void setOpenDelimiter(String openDelimiter) {
        this.openDelimiterTextField.setText(openDelimiter);
    }

    public int getDepthOfScanning() {
        return depthOfScanningComboBox.getSelectedIndex();
    }

    public void setDepthOfScanning(int depth) {
        this.depthOfScanningComboBox.setSelectedIndex(depth);
    }

    public void setError(String message) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.errorForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public void setWarning(String message) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(Color.ORANGE); // NOI18N
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









        openDelimiterLabel = new JLabel();
        closeDelimiterLabel = new JLabel();
        closeDelimiterTextField = new JTextField();
        errorLabel = new JLabel();
        learnMoreLabel = new JLabel();
        installationInfoLabel = new JLabel();
        jSeparator1 = new JSeparator();
        depthOfScanningLabel = new JLabel();
        depthOfScanningNoteLabel = new JLabel();
        depthOfScanningComboBox = new JComboBox();
        openDelimiterTextField = new JTextField();
        Mnemonics.setLocalizedText(openDelimiterLabel, NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.openDelimiterLabel.text"));
        Mnemonics.setLocalizedText(closeDelimiterLabel, NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.closeDelimiterLabel.text"));
        closeDelimiterTextField.setText(NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.closeDelimiterTextField.text")); // NOI18N
        Mnemonics.setLocalizedText(errorLabel, NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.errorLabel.text"));
        Mnemonics.setLocalizedText(learnMoreLabel, NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.learnMoreLabel.text"));
        learnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                learnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                learnMoreLabelMousePressed(evt);
            }
        });
        Mnemonics.setLocalizedText(installationInfoLabel, NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.installationInfoLabel.text"));
        Mnemonics.setLocalizedText(depthOfScanningLabel, NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.depthOfScanningLabel.text"));
        Mnemonics.setLocalizedText(depthOfScanningNoteLabel, NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.depthOfScanningNoteLabel.text"));
        openDelimiterTextField.setText(NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.openDelimiterTextField.text")); // NOI18N
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jSeparator1, GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
                    .addComponent(errorLabel)
                    .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(installationInfoLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(depthOfScanningLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(depthOfScanningComboBox, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(depthOfScanningNoteLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(closeDelimiterLabel)
                            .addComponent(openDelimiterLabel))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                            .addComponent(openDelimiterTextField)
                            .addComponent(closeDelimiterTextField, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                        .addGap(13, 13, 13)))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {closeDelimiterTextField, openDelimiterTextField});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(openDelimiterLabel)
                    .addComponent(openDelimiterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(closeDelimiterLabel)
                    .addComponent(closeDelimiterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(depthOfScanningLabel)
                    .addComponent(depthOfScanningNoteLabel)
                    .addComponent(depthOfScanningComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED, 161, Short.MAX_VALUE)
                .addComponent(installationInfoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(errorLabel)
                .addGap(6, 6, 6))
        );

        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void learnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_learnMoreLabelMouseEntered

    private void learnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMousePressed
        try {
            URL url = new URL("http://www.smarty.net/manual/en/installing.smarty.basic.php"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_learnMoreLabelMousePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel closeDelimiterLabel;
    private JTextField closeDelimiterTextField;
    private JComboBox depthOfScanningComboBox;
    private JLabel depthOfScanningLabel;
    private JLabel depthOfScanningNoteLabel;
    private JLabel errorLabel;
    private JLabel installationInfoLabel;
    private JSeparator jSeparator1;
    private JLabel learnMoreLabel;
    private JLabel openDelimiterLabel;
    private JTextField openDelimiterTextField;
    // End of variables declaration//GEN-END:variables

    private final class SmartyDocumentListener implements DocumentListener {
        
        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }
        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }
        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }
        private void processUpdate() {
            fireChange();
        }
    }
    
}

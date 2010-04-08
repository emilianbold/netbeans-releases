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

package org.netbeans.modules.javacard.project.deps.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.javacard.project.deps.DependencyKind;
import org.netbeans.modules.javacard.project.deps.DeploymentStrategy;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

final class ChooseDeploymentStrategyPanelVisual extends JPanel implements ItemListener, FocusListener, ActionListener, DocumentListener {
    private final ChangeSupport supp = new ChangeSupport(this);
    private static final String CLIENT_PROP_DEP_KIND = "_dependencyKind";
    private final WizardDescriptor wiz;
    private final JTextField sigField = new JTextField();
    private final JButton browseButton = new JButton();

    ChooseDeploymentStrategyPanelVisual(WizardDescriptor wiz) {
        this.wiz = wiz;
        setLayout (new GridBagLayout());
        setBorder (BorderFactory.createEmptyBorder (12,12,12,12));
        HelpCtx.setHelpIDString(this, "org.netbeans.modules.javacard.ChangeLibraryDeploymentStrategy"); //NOI18N
        sigField.addFocusListener(this);
        sigField.getDocument().addDocumentListener(this);
        browseButton.addActionListener(this);
    }

    void setDependencyKind(DependencyKind kind) {
        removeAll();
        ButtonGroup grp = new ButtonGroup();
        if (kind == null) {
            return;
        }
        List<DeploymentStrategy> l = new ArrayList<DeploymentStrategy> (kind.supportedDeploymentStrategies());
        Collections.sort(l);
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridy = 0;
        buttonConstraints.fill = GridBagConstraints.BOTH;
        buttonConstraints.anchor = GridBagConstraints.NORTHWEST;
        buttonConstraints.weightx = 1.0F;
        buttonConstraints.weighty = 1.0F;
        buttonConstraints.gridwidth = GridBagConstraints.REMAINDER;
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridy = 1;
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        labelConstraints.anchor = GridBagConstraints.NORTHWEST;
        labelConstraints.insets = new Insets(0, 20, 12, 0);
        labelConstraints.weightx = 1.0F;
        labelConstraints.weighty = 1.0F;
        labelConstraints.gridwidth = GridBagConstraints.REMAINDER;
        Border empty = BorderFactory.createEmptyBorder();
        Color ctrl = UIManager.getColor("control"); //NOI18N
        ctrl = ctrl == null ? Color.GRAY : ctrl;
        Font font = null;
        for (DeploymentStrategy d : l) {
            JRadioButton button = new JRadioButton (d.toString());
            button.putClientProperty(CLIENT_PROP_DEP_KIND, d);
            button.addItemListener(this);
            grp.add(button);
            //And this is why people say Swing is verbose...
            JTextArea area = new JTextArea(d.getDescription());
            if (font == null) {
                font = area.getFont();
                if (font != null) {
                    font = font.deriveFont(Font.ITALIC);
                }
            }
            if (font != null) { //may be during initialization on some L&Fs
                area.setFont(font);
            }
            area.setBorder (empty);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            JScrollPane desc = new JScrollPane (area);
            desc.setBorder(empty);
            desc.setViewportBorder(empty);
            area.setBackground (ctrl);
            desc.setBackground (ctrl);
            desc.getViewport().setBackground(ctrl);
            area.getCaret().setVisible(false);
            add (button, buttonConstraints);
            add (desc, labelConstraints);
            if (d == DeploymentStrategy.DEPLOY_TO_CARD && !kind.isProjectDependency()) {
                Mnemonics.setLocalizedText(browseButton, NbBundle.getMessage(
                        ChooseDeploymentStrategyPanelVisual.class, "LBL_BROWSE")); //NOI18N
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.gridx = 0;
                gbc.gridy = buttonConstraints.gridy + 1;
                gbc.anchor = GridBagConstraints.WEST;
                int dist = Utilities.isMac() ? 12 : 5;
                int left = button.getIcon() != null ? button.getIcon().getIconWidth() + button.getIconTextGap() : button.getIconTextGap();
                left = Math.max (16, left);
                gbc.insets = new Insets (24, left, dist, dist);
                JLabel sigLabel = new JLabel(NbBundle.getMessage(ChooseDeploymentStrategyPanelVisual.class, "SIG_FILE"));
                add (sigLabel, gbc);
                gbc.insets = new Insets (24, 0, dist, dist);
                gbc.gridx++;
                gbc.weightx = 1.0F;
                add (sigField, gbc);
                gbc.gridx++;
                gbc.weightx = 0;
                add (browseButton, gbc);
                buttonConstraints.gridy+=4;
                labelConstraints.gridy +=4;
            }
            buttonConstraints.gridy +=2;
            labelConstraints.gridy += 2;
        }
        //XXX remove once build-time support for new project metadata is
        //implemented
        labelConstraints.gridy++;
        JTextArea area = new JTextArea(NbBundle.getMessage(
                ChooseDeploymentStrategyPanelVisual.class,
                "WARNING_DEPLOYMENT_STRATEGY")); //NOI18N
        Color errColor = UIManager.getColor("nb.errorForeground"); //NOI18N
        errColor = errColor == null ? Color.RED : errColor;
        area.setForeground (errColor);
        JScrollPane lbl = new JScrollPane (area);
        area.setBackground (ctrl);
        lbl.setBackground (ctrl);
        lbl.getViewport().setBackground (ctrl);
        area.getCaret().setVisible(false);
        area.setBorder(empty);
        lbl.setBorder(empty);
        lbl.setViewportBorder(empty);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        add (lbl, labelConstraints);
        insertUpdate(null);
    }

    public DeploymentStrategy getDeploymentStrategy() {
        for (Component c : getComponents()) {
            if (c instanceof JRadioButton && ((JRadioButton)c).isSelected()) {
                JRadioButton r = (JRadioButton) c;
                return (DeploymentStrategy) r.getClientProperty(CLIENT_PROP_DEP_KIND);
            }
        }
        return null;
    }

    public File getSignatureFile() {
        if (sigField.getText().trim().length() > 0) {
            return new File (sigField.getText());
        }
        return null;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        JDialog dlg = (JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, this);
        if (dlg != null) {
            Dimension d = dlg.getPreferredSize();
            dlg.setSize (d);
        }
    }

    public void removeChangeListener(ChangeListener listener) {
        supp.removeChangeListener(listener);
    }

    public void fireChange() {
        supp.fireChange();
    }

    public void addChangeListener(ChangeListener listener) {
        supp.addChangeListener(listener);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ChooseDeploymentStrategyPanelVisual.class,
                "WIZARD_STEP_CHOOSE_DEPLOYMENT_STRATEGY"); //NOI18N
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    public void itemStateChanged(ItemEvent e) {
        insertUpdate(null);
        fireChange();
        insertUpdate(null);
    }

    boolean updating;
    void setDeploymentStrategy(DeploymentStrategy deploymentStrategy) {
        updating = true;
        try {
            for (Component c : getComponents()) {
                if (deploymentStrategy == null) {
                    if (c instanceof JRadioButton) {
                        ((JRadioButton) c).setSelected(false);
                    }
                } else if (c instanceof JRadioButton && deploymentStrategy == ((JRadioButton) c).getClientProperty(CLIENT_PROP_DEP_KIND)) {
                    ((JRadioButton)c).setSelected(true);
                }
            }
        } finally {
            updating = false;
        }
        insertUpdate(null);
    }

    @Override
    public void focusGained(FocusEvent e) {
        ((JTextComponent) e.getSource()).selectAll();
    }

    @Override
    public void focusLost(FocusEvent e) {
        //do nothing
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        File f = new FileChooserBuilder(ChooseDeploymentStrategyPanelVisual.class).setTitle(NbBundle.getMessage(ChooseDeploymentStrategyPanelVisual.class, "NAME_SIG_FILE_PANEL")).setFileFilter(new SigFilter()).showOpenDialog();
        if (f != null) {
            sigField.setText(f.getAbsolutePath());
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        DeploymentStrategy deploymentStrategy = getDeploymentStrategy();
        sigField.setEnabled (deploymentStrategy == DeploymentStrategy.DEPLOY_TO_CARD);
        browseButton.setEnabled(deploymentStrategy == DeploymentStrategy.DEPLOY_TO_CARD);
        if (wiz != null && DeploymentStrategy.DEPLOY_TO_CARD.equals(getDeploymentStrategy())) {
            if (sigField.isDisplayable() && sigField.getText().trim().length() == 0) {
                wiz.setValid(false);
                wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(ChooseDeploymentStrategyPanelVisual.class, "ERR_SIG_FILE_REQUIRED"));  //NOI18N
                return;
            }
            File f = new File(sigField.getText());
            if (sigField.isDisplayable() && !f.exists()) {
                wiz.setValid(false);
                wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(ChooseDeploymentStrategyPanelVisual.class, "ERR_SIG_FILE_DOES_NOT_EXIST"));  //NOI18N
                return;
            }
            if (sigField.isDisplayable() && f.isDirectory()) {
                wiz.setValid(false);
                wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(ChooseDeploymentStrategyPanelVisual.class, "ERR_SIG_FILE_IS_DIR"));  //NOI18N
                return;
            }
        } else if (wiz == null || !DeploymentStrategy.DEPLOY_TO_CARD.equals(deploymentStrategy)) {
            return;
        }
        wiz.setValid(true);
        wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        insertUpdate(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        insertUpdate(e);
    }

    void setSigFile(File sigFile) {
        if (sigFile == null) {
            sigField.setText("");
        } else {
            sigField.setText(sigFile.getAbsolutePath());
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}


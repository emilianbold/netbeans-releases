/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.makeproject.ui.options;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

/**
 * Replaces the old project system options panel.
 */
public class ProjectOptionsPanel extends JPanel {

    private boolean changed;
    private boolean listen = false;
    private ArrayList<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();
    private DocumentListener documentListener;

    /** Creates new form ProjectOptionsPanel */
    public ProjectOptionsPanel() {
        initComponents();
        // Accessible Description
        reuseCheckBox.getAccessibleContext().setAccessibleDescription(getString("REUSE_CHECKBOX_AD"));
        saveCheckBox.getAccessibleContext().setAccessibleDescription(getString("SAVE_CHECKBOX_AD"));
        dependencyCheckingCheckBox.getAccessibleContext().setAccessibleDescription(getString("DEPENDENCY_CHECKBOX_AD"));
//        platformComboBox.getAccessibleContext().setAccessibleDescription(getString("DEFAULT_PLATFORM_AD"));
        filePathcomboBox.getAccessibleContext().setAccessibleDescription(getString("FILE_PATH_AD"));
        makeOptionsTextField.getAccessibleContext().setAccessibleDescription(getString("MAKE_OPTIONS_AD"));
        filePathTxt.getAccessibleContext().setAccessibleDescription(getString("FILE_PATH_TXT_AD"));
        filePathTxt.getAccessibleContext().setAccessibleName(getString("FILE_PATH_TXT_AN"));


        documentListener = new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                validateFields();
            }

            public void removeUpdate(DocumentEvent e) {
                validateFields();
            }

            public void changedUpdate(DocumentEvent e) {
                validateFields();
            }
        };

        makeOptionsTextField.getDocument().addDocumentListener(documentListener);
        setName("TAB_ProjectsTab"); // NOI18N (used as a pattern...)

        if ("Windows".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
            setOpaque(false);
        } else {
            Color c = getBackground();
            Color cc = new Color(c.getRed(), c.getGreen(), c.getBlue());
            filePathTxt.setBackground(cc);
        }
    }

    public void update() {
        listen = false;
        MakeOptions makeOptions = MakeOptions.getInstance();
        dependencyCheckingCheckBox.setSelected(makeOptions.getDepencyChecking());
        makeOptionsTextField.setText(makeOptions.getMakeOptions());
        filePathcomboBox.removeAllItems();
        for (int i = 0; i < MakeOptions.PathModeNames.length; i++) {
            filePathcomboBox.addItem(MakeOptions.PathModeNames[i]);
        }
        filePathcomboBox.setSelectedIndex(makeOptions.getPathMode());
        saveCheckBox.setSelected(makeOptions.getSave());
        reuseCheckBox.setSelected(makeOptions.getReuse());
        viewBinaryFilesCheckBox.setSelected(makeOptions.getViewBinaryFiles());

        listen = true;
        changed = false;
    }

    /** Apply changes */
    public void applyChanges() {
        MakeOptions makeOptions = MakeOptions.getInstance();

        makeOptions.setDepencyChecking(dependencyCheckingCheckBox.isSelected());
        makeOptions.setMakeOptions(makeOptionsTextField.getText());
        makeOptions.setPathMode(filePathcomboBox.getSelectedIndex());
        makeOptions.setSave(saveCheckBox.isSelected());
        makeOptions.setReuse(reuseCheckBox.isSelected());
        makeOptions.setViewBinaryFiles(viewBinaryFilesCheckBox.isSelected());

        changed = false;
    }

    /** What to do if user cancels the dialog (nothing) */
    public void cancel() {
        changed = false;
    }

    /**
     * Lets NB know if the data in the panel is valid and OK should be enabled
     * 
     * @return Returns true if all data is valid
     */
    public boolean dataValid() {
        return true;
    }

    /**
     * Lets caller know if any data has been changed.
     * 
     * @return True if anything has been changed
     */
    public boolean isChanged() {
        return changed;
    }

    private void validateFields() {
        PropertyChangeEvent pce = new PropertyChangeEvent(this, OptionsPanelController.PROP_VALID, this, this);
        firePropertyChange(pce);
    }

    public void firePropertyChange(PropertyChangeEvent evt) {
        ArrayList<PropertyChangeListener> newList = new ArrayList<PropertyChangeListener>();
        newList.addAll(propertyChangeListeners);
        for (PropertyChangeListener listener : newList) {
            listener.propertyChange(evt);
        }
    }

    private static String getString(String key) {
        return NbBundle.getMessage(ProjectOptionsPanel.class, key);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        makeOptionsLabel = new javax.swing.JLabel();
        makeOptionsTextField = new javax.swing.JTextField();
        makeOptionsTxt = new javax.swing.JLabel();
        filePathLabel = new javax.swing.JLabel();
        filePathcomboBox = new javax.swing.JComboBox();
        filePathTxt = new javax.swing.JTextArea();
        saveCheckBox = new javax.swing.JCheckBox();
        reuseCheckBox = new javax.swing.JCheckBox();
        dependencyCheckingCheckBox = new javax.swing.JCheckBox();
        viewBinaryFilesCheckBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();

        setLayout(new java.awt.GridBagLayout());

        makeOptionsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("MAKE_OPTIONS_MN").charAt(0));
        makeOptionsLabel.setLabelFor(makeOptionsTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle"); // NOI18N
        makeOptionsLabel.setText(bundle.getString("MAKE_OPTIONS")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(makeOptionsLabel, gridBagConstraints);

        makeOptionsTextField.setColumns(45);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 6);
        add(makeOptionsTextField, gridBagConstraints);

        makeOptionsTxt.setText(bundle.getString("MAKE_OPTIONS_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 0, 12);
        add(makeOptionsTxt, gridBagConstraints);

        filePathLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("FILE_PATH_MN").charAt(0));
        filePathLabel.setLabelFor(filePathcomboBox);
        filePathLabel.setText(bundle.getString("FILE_PATH")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(filePathLabel, gridBagConstraints);

        filePathcomboBox.setMinimumSize(new java.awt.Dimension(75, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 4, 0, 6);
        add(filePathcomboBox, gridBagConstraints);

        filePathTxt.setEditable(false);
        filePathTxt.setLineWrap(true);
        filePathTxt.setText(bundle.getString("FILE_PATH_MODE_TXT")); // NOI18N
        filePathTxt.setWrapStyleWord(true);
        filePathTxt.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 10.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(filePathTxt, gridBagConstraints);

        saveCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("SAVE_CHECKBOX_MN").charAt(0));
        saveCheckBox.setText(bundle.getString("SAVE_CHECKBOX_TXT")); // NOI18N
        saveCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 6, 6);
        add(saveCheckBox, gridBagConstraints);

        reuseCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("REUSE_CHECKBOX_MN").charAt(0));
        reuseCheckBox.setText(bundle.getString("REUSE_CHECKBOX_TXT")); // NOI18N
        reuseCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(reuseCheckBox, gridBagConstraints);

        dependencyCheckingCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("DEPENDENCY_CHECKING_MN").charAt(0));
        dependencyCheckingCheckBox.setText(bundle.getString("DEPENDENCY_CHECKING_TXT")); // NOI18N
        dependencyCheckingCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dependencyCheckingCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dependencyCheckingCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(dependencyCheckingCheckBox, gridBagConstraints);

        viewBinaryFilesCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("DISPLAY_BINARY_FILES_MN").charAt(0));
        viewBinaryFilesCheckBox.setText(org.openide.util.NbBundle.getMessage(ProjectOptionsPanel.class, "DISPLAY_BINARY_FILES_TXT")); // NOI18N
        viewBinaryFilesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewBinaryFilesCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(viewBinaryFilesCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSeparator1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void dependencyCheckingCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dependencyCheckingCheckBoxActionPerformed
// TODO add your handling code here:

        PropertyChangeEvent pce = new PropertyChangeEvent(this, OptionsPanelController.PROP_VALID, this, this);
        firePropertyChange(pce);
//        pce = new PropertyChangeEvent(this, "buran" + OptionsPanelController.PROP_VALID, this, this);
//        firePropertyChange(pce);
    }//GEN-LAST:event_dependencyCheckingCheckBoxActionPerformed

    private void viewBinaryFilesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewBinaryFilesCheckBoxActionPerformed
        PropertyChangeEvent pce = new PropertyChangeEvent(this, OptionsPanelController.PROP_VALID, this, this);
        firePropertyChange(pce);
    }//GEN-LAST:event_viewBinaryFilesCheckBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox dependencyCheckingCheckBox;
    private javax.swing.JLabel filePathLabel;
    private javax.swing.JTextArea filePathTxt;
    private javax.swing.JComboBox filePathcomboBox;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel makeOptionsLabel;
    private javax.swing.JTextField makeOptionsTextField;
    private javax.swing.JLabel makeOptionsTxt;
    private javax.swing.JCheckBox reuseCheckBox;
    private javax.swing.JCheckBox saveCheckBox;
    private javax.swing.JCheckBox viewBinaryFilesCheckBox;
    // End of variables declaration//GEN-END:variables
}

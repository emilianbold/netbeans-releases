/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools.wizards;

/*
 * TestBagSettingsPanel.java
 *
 * Created on April 10, 2002, 1:45 PM
 */

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;

import org.openide.util.HelpCtx;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/** Wizard Panel with Test Bag Settings configuration
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class TestBagSettingsPanel extends JPanel {

    static final long serialVersionUID = 6692306744377282694L;
    private static final String DEFAULT_NAME=NbBundle.getMessage(TestBagSettingsPanel.class, "CTL_DefaultName"); // NOI18N
    
    public final Panel panel = new Panel();
    
    private class Panel extends Object implements WizardDescriptor.FinishPanel {
        
        /** adds ChangeListener of current Panel
         * @param changeListener ChangeListener */    
        public void addChangeListener(ChangeListener changeListener) {}    

        /** returns current Panel
         * @return Component */    
        public Component getComponent() {
            return TestBagSettingsPanel.this;
        }    

        /** returns Help Context
         * @return HelpCtx */    
        public HelpCtx getHelp() {
            return new HelpCtx(TestBagSettingsPanel.class);
        }

        /** read settings from given Object
         * @param obj TemplateWizard with settings */    
        public void readSettings(Object obj) {
            WizardSettings set=WizardSettings.get(obj);
            if (set.bagAttrs!=null)
                attrField.setText(set.bagAttrs);
            if (set.bagIncludes!=null)
                includeField.setText(set.bagIncludes);
            if (set.bagExcludes!=null)
                excludeField.setText(set.bagExcludes);
            ideRadio.setSelected(set.bagIDEExecutor);
            codeRadio.setSelected(!set.bagIDEExecutor);
        }

        /** removes Change Listener of current Panel
         * @param changeListener ChangeListener */    
        public void removeChangeListener(ChangeListener changeListener) {}

        /** stores settings to given Object
         * @param obj TemplateWizard with settings */    
        public void storeSettings(Object obj) {
            WizardSettings set=WizardSettings.get(obj);
            String name=nameField.getText();
            if (DEFAULT_NAME.equals(name))
                name=null;
            set.bagName=name;
            set.bagAttrs=attrField.getText();
            set.bagIncludes=includeField.getText();
            set.bagExcludes=excludeField.getText();
            set.bagIDEExecutor=ideRadio.isSelected();
        }

        /** test current Panel state for data validity
         * @return boolean true if data are valid and Wizard can continue */    
        public boolean isValid() {
            return true;
        }

        private void fireStateChanged() {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    if (nameField.getText().equals ("")) { // NOI18N
                        nameField.setText(DEFAULT_NAME);
                        nameField.selectAll();
                    }
                }
            });            
        }
    }
    
    /** Creates new form TestBagPanel */
    public TestBagSettingsPanel() {
        setName(NbBundle.getMessage(TestBagSettingsPanel.class, "LBL_TestBagSettings")); // NOI18N
        initComponents();
        DocumentListener list=new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {panel.fireStateChanged();}
            public void removeUpdate(DocumentEvent e) {panel.fireStateChanged();}
            public void changedUpdate(DocumentEvent e) {panel.fireStateChanged();}
        };
        nameField.getDocument().addDocumentListener(list);
        panel.fireStateChanged();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup = new javax.swing.ButtonGroup();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        executorLabel = new javax.swing.JLabel();
        ideRadio = new javax.swing.JRadioButton();
        codeRadio = new javax.swing.JRadioButton();
        attrLabel = new javax.swing.JLabel();
        attrField = new javax.swing.JTextField();
        includeLabel = new javax.swing.JLabel();
        includeField = new javax.swing.JTextField();
        excludeLabel = new javax.swing.JLabel();
        excludeField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setDisplayedMnemonic(NbBundle.getMessage(TestBagSettingsPanel.class, "MNM_TestBagName").charAt(0));
        nameLabel.setLabelFor(nameField);
        nameLabel.setText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "LBL_TestBagName"));
        nameLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "TTT_BagName", new Object[] {}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(nameLabel, gridBagConstraints);

        nameField.setToolTipText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "TTT_BagName", new Object[] {}));
        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nameFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        add(nameField, gridBagConstraints);

        executorLabel.setDisplayedMnemonic(NbBundle.getMessage(TestBagSettingsPanel.class, "MNM_TestBagExecutor").charAt(0));
        executorLabel.setText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "LBL_TestBagExecutor"));
        executorLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "TTT_TestBagExecutor", new Object[] {}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 12);
        add(executorLabel, gridBagConstraints);

        ideRadio.setMnemonic(NbBundle.getMessage(TestBagSettingsPanel.class, "MNM_TestBagExecIDE").charAt(0));
        ideRadio.setSelected(true);
        ideRadio.setText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "LBL_TestBagExecutorIDE"));
        ideRadio.setToolTipText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "TTT_TestBagExecutor", new Object[] {}));
        buttonGroup.add(ideRadio);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        add(ideRadio, gridBagConstraints);

        codeRadio.setMnemonic(NbBundle.getMessage(TestBagSettingsPanel.class, "MNM_TestBagExecCode").charAt(0));
        codeRadio.setText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "LBL_TestBagExecutorCode"));
        codeRadio.setToolTipText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "TTT_TestBagExecutor", new Object[] {}));
        buttonGroup.add(codeRadio);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 10.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(codeRadio, gridBagConstraints);

        attrLabel.setDisplayedMnemonic(NbBundle.getMessage(TestBagSettingsPanel.class, "MNM_TestBagAttrs").charAt(0));
        attrLabel.setLabelFor(attrField);
        attrLabel.setText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "LBL_TestBagAttributes"));
        attrLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "TTT_TestBagAttributes", new Object[] {}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(attrLabel, gridBagConstraints);

        attrField.setToolTipText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "TTT_TestBagAttributes", new Object[] {}));
        attrField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                attrFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        add(attrField, gridBagConstraints);

        includeLabel.setDisplayedMnemonic(NbBundle.getMessage(TestBagSettingsPanel.class, "MNM_TestBagExecInclude").charAt(0));
        includeLabel.setLabelFor(includeField);
        includeLabel.setText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "LBL_TestBagExecInclude"));
        includeLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "TTT_TestBagInclude", new Object[] {}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(includeLabel, gridBagConstraints);

        includeField.setToolTipText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "TTT_TestBagInclude", new Object[] {}));
        includeField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                includeFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        add(includeField, gridBagConstraints);

        excludeLabel.setDisplayedMnemonic(NbBundle.getMessage(TestBagSettingsPanel.class, "MNM_TestBagExecExclude").charAt(0));
        excludeLabel.setLabelFor(excludeField);
        excludeLabel.setText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "LBL_TestBagExecExclude"));
        excludeLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "TTT_TestBagExclude", new Object[] {}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(excludeLabel, gridBagConstraints);

        excludeField.setToolTipText(org.openide.util.NbBundle.getMessage(TestBagSettingsPanel.class, "TTT_TestBagExclude", new Object[] {}));
        excludeField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                excludeFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        add(excludeField, gridBagConstraints);

    }//GEN-END:initComponents

    private void excludeFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_excludeFieldFocusGained
        excludeField.selectAll();
    }//GEN-LAST:event_excludeFieldFocusGained

    private void includeFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_includeFieldFocusGained
        includeField.selectAll();
    }//GEN-LAST:event_includeFieldFocusGained

    private void attrFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_attrFieldFocusGained
        attrField.selectAll();
    }//GEN-LAST:event_attrFieldFocusGained

    private void nameFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusGained
        nameField.selectAll();
    }//GEN-LAST:event_nameFieldFocusGained

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel excludeLabel;
    private javax.swing.JTextField includeField;
    private javax.swing.JRadioButton ideRadio;
    private javax.swing.JTextField nameField;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JLabel includeLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel executorLabel;
    private javax.swing.JRadioButton codeRadio;
    private javax.swing.JTextField attrField;
    private javax.swing.JLabel attrLabel;
    private javax.swing.JTextField excludeField;
    // End of variables declaration//GEN-END:variables
    
}

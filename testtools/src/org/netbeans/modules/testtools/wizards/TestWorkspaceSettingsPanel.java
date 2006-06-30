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
 * TestWorkspaceSettingsPanel.java
 *
 * Created on April 10, 2002, 1:43 PM
 */

import java.io.File;
import java.awt.Component;
import java.awt.CardLayout;
import javax.swing.JPanel;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeEvent;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/** Wizard Panel with Test Workspace Settings configuration
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class TestWorkspaceSettingsPanel extends JPanel {
    
    static final long serialVersionUID = 6910738027583517330L;
    
    private boolean stop=true;
    private static final String netbeansPath="../../../nb_all/nbbuild/netbeans"; // NOI18N
    private static final String xtestPath="../../../nb_all/xtest"; // NOI18N
    private static final String jemmyPath="../../../nb_all/jemmy/builds"; // NOI18N
    private static final String jellyPath="../../../nb_all/jellytools/builds"; // NOI18N
    private String jemmyHome=jemmyPath;
    private String jellyHome=jellyPath;
    private TemplateWizard wizard;
    private ChangeListener listener=null;
    private static File netHome=new File(System.getProperty("netbeans.home",".")); // NOI18N

    public final Panel panel = new Panel();
    
    private class Panel extends Object implements WizardDescriptor.FinishPanel {
    
        /** adds ChangeListener of current Panel
         * @param changeListener ChangeListener */    
        public void addChangeListener(ChangeListener changeListener) {
            if (listener != null) throw new IllegalStateException ();
            listener = changeListener;
        }    

        /** returns current Panel
         * @return Component */    
        public Component getComponent() {
            return TestWorkspaceSettingsPanel.this;
        }    

        /** returns Help Context
         * @return HelpCtx */    
        public HelpCtx getHelp() {
            return new HelpCtx(TestWorkspaceSettingsPanel.class);
        }

        /** read settings from given Object
         * @param obj TemplateWizard with settings */    
        public void readSettings(Object obj) {
            WizardSettings set=WizardSettings.get(obj);
            wizard=(TemplateWizard)obj;
            DataFolder df=null;
            stop=true;
            try {
                df=wizard.getTargetFolder();
                stop=(wizard.getTargetName()!=null && wizard.getTargetName().indexOf(' ')>=0) 
                || WizardIterator.detectBuildScript(df);
            } catch (Exception e) {}
            if (stop)
                ((CardLayout)getLayout()).show(TestWorkspaceSettingsPanel.this, "stop"); // NOI18N
            else {
                ((CardLayout)getLayout()).show(TestWorkspaceSettingsPanel.this, "ok"); // NOI18N
                if (set.workspaceLevel<0)
                    levelCombo.setSelectedIndex(WizardIterator.detectWorkspaceLevel(df));
                if (set.defaultType!=null) 
                    typeField.setText(set.defaultType);
                if (set.defaultAttributes!=null) 
                    attrField.setText(set.defaultAttributes);
                updatePanel();
            }
        }

        /** removes Change Listener of current Panel
         * @param changeListener ChangeListener */    
        public void removeChangeListener(ChangeListener changeListener) {
            listener = null;
        }

        /** stores settings to given Object
         * @param obj TemplateWizard with settings */    
        public void storeSettings(Object obj) {
            WizardSettings set=WizardSettings.get(obj);
            set.workspaceLevel=levelCombo.getSelectedIndex();
            set.netbeansHome=netbeansField.getText();
            set.xtestHome=xtestField.getText();
            set.defaultType=typeField.getText();
            set.defaultAttributes=attrField.getText();
            set.typeJemmyHome=jemmyHome;
            set.typeJellyHome=jellyHome;
        }

        /** test current Panel state for data validity
         * @return boolean true if data are valid and Wizard can continue */    
        public boolean isValid() {
            return (!stop)&&(!netHome.equals(new File(netbeansField.getText())));
        }

        private void fireStateChanged() {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    if (listener != null) {
                        listener.stateChanged (new ChangeEvent (this));
                    }
                }
            });            
        }
    }
    
    /** Creates new form TestWorkspacePanel */
    public TestWorkspaceSettingsPanel() {
        setName(NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "LBL_TestWorkspacePanelName")); // NOI18N
        initComponents();
        DocumentListener list=new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {panel.fireStateChanged();}
            public void removeUpdate(DocumentEvent e) {panel.fireStateChanged();}
            public void changedUpdate(DocumentEvent e) {panel.fireStateChanged();}
        };
        netbeansField.getDocument().addDocumentListener(list);
        panel.fireStateChanged();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        panel2 = new javax.swing.JPanel();
        levelLabel = new javax.swing.JLabel();
        levelCombo = new javax.swing.JComboBox();
        typeLabel = new javax.swing.JLabel();
        typeField = new javax.swing.JTextField();
        attrLabel = new javax.swing.JLabel();
        attrField = new javax.swing.JTextField();
        separator1 = new javax.swing.JSeparator();
        advancedCheck = new javax.swing.JCheckBox();
        netbeansLabel = new javax.swing.JLabel();
        netbeansField = new javax.swing.JTextField();
        xtestLabel = new javax.swing.JLabel();
        xtestField = new javax.swing.JTextField();
        netbeansButton = new javax.swing.JButton();
        xtestButton = new javax.swing.JButton();
        separator2 = new javax.swing.JSeparator();
        stopLabel = new javax.swing.JLabel();

        setLayout(new java.awt.CardLayout());

        panel2.setLayout(new java.awt.GridBagLayout());

        levelLabel.setDisplayedMnemonic(NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "MNM_TestWorkspaceLevel").charAt(0) );
        levelLabel.setLabelFor(levelCombo);
        levelLabel.setText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "LBL_TestWorkspaceLevel"));
        levelLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "TTT_TestWorkspaceLevel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        panel2.add(levelLabel, gridBagConstraints);

        levelCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "On top of the module (repository / module)", "One level lower (repository / module / package)", "Two levels lower (repository / module / package / package)", "Out of CVS structute (for local use only)" }));
        levelCombo.setToolTipText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "TTT_TestWorkspaceLevel"));
        levelCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                levelComboActionPerformed(evt);
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
        panel2.add(levelCombo, gridBagConstraints);

        typeLabel.setDisplayedMnemonic(NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "MNM_TestWorkspaceDefaultType").charAt(0) );
        typeLabel.setLabelFor(typeField);
        typeLabel.setText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "LBL_TestWorkspaceDefaultTestType"));
        typeLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "TTT_TestWorkspaceDefaultTT"));
        typeLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        panel2.add(typeLabel, gridBagConstraints);

        typeField.setToolTipText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "TTT_TestWorkspaceDefaultTT"));
        typeField.setEnabled(false);
        typeField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                typeFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        panel2.add(typeField, gridBagConstraints);

        attrLabel.setDisplayedMnemonic(NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "MNM_TestWorkspaceDefaultAttrs").charAt(0) );
        attrLabel.setLabelFor(attrField);
        attrLabel.setText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "LBL_TestWorkspaceDefaultAttributes"));
        attrLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "TTT_TestWorkspaceAttrs"));
        attrLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        panel2.add(attrLabel, gridBagConstraints);

        attrField.setToolTipText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "TTT_TestWorkspaceAttrs"));
        attrField.setEnabled(false);
        attrField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                attrFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        panel2.add(attrField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 0.01;
        gridBagConstraints.insets = new java.awt.Insets(17, 0, 0, 11);
        panel2.add(separator1, gridBagConstraints);

        advancedCheck.setMnemonic(NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "MNM_TestWorkspaceAdvanced").charAt(0) );
        advancedCheck.setText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "LBL_AdvancedSettings"));
        advancedCheck.setToolTipText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "TTT_AdvancedSettings"));
        advancedCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advancedCheckActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(17, 12, 0, 0);
        panel2.add(advancedCheck, gridBagConstraints);

        netbeansLabel.setDisplayedMnemonic(NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "MNM_TestWorkspaceNetbeansHome").charAt(0) );
        netbeansLabel.setLabelFor(netbeansField);
        netbeansLabel.setText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "LBL_TestWorkspaceNetbeansHome"));
        netbeansLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "TTT_NetbeansHome"));
        netbeansLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        panel2.add(netbeansLabel, gridBagConstraints);

        netbeansField.setToolTipText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "TTT_NetbeansHome"));
        netbeansField.setEnabled(false);
        netbeansField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                netbeansFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        panel2.add(netbeansField, gridBagConstraints);

        xtestLabel.setDisplayedMnemonic(NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "MNM_TestWorkspaceXTestHome").charAt(0) );
        xtestLabel.setLabelFor(xtestField);
        xtestLabel.setText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "LBL_TestWorkspaceXTestHome"));
        xtestLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "TTT_XTestHome"));
        xtestLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        panel2.add(xtestLabel, gridBagConstraints);

        xtestField.setToolTipText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "TTT_XTestHome"));
        xtestField.setEnabled(false);
        xtestField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                xtestFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 11, 0);
        panel2.add(xtestField, gridBagConstraints);

        netbeansButton.setText("...");
        netbeansButton.setToolTipText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "TTT_NetbeansHome"));
        netbeansButton.setMinimumSize(new java.awt.Dimension(30, 20));
        netbeansButton.setPreferredSize(new java.awt.Dimension(30, 20));
        netbeansButton.setEnabled(false);
        netbeansButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                netbeansButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        panel2.add(netbeansButton, gridBagConstraints);
        netbeansButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "CTL_NetbeansHomeCust"));

        xtestButton.setText("...");
        xtestButton.setToolTipText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "TTT_XTestHome"));
        xtestButton.setMinimumSize(new java.awt.Dimension(30, 20));
        xtestButton.setPreferredSize(new java.awt.Dimension(30, 20));
        xtestButton.setEnabled(false);
        xtestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xtestButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        panel2.add(xtestButton, gridBagConstraints);
        xtestButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "TTT_XTestHomeCust"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.01;
        gridBagConstraints.insets = new java.awt.Insets(17, 5, 0, 0);
        panel2.add(separator2, gridBagConstraints);

        add(panel2, "ok");

        stopLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        stopLabel.setText(org.openide.util.NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "MSG_TestWorkspaceExists"));
        add(stopLabel, "stop");

    }//GEN-END:initComponents

    private void xtestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xtestButtonActionPerformed
        File home=WizardIterator.showFileChooser(this, NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "TITLE_SelectXTestHome"), true, false); // NOI18N
        if (home!=null) 
            xtestField.setText(home.getAbsolutePath());
    }//GEN-LAST:event_xtestButtonActionPerformed

    private void netbeansButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_netbeansButtonActionPerformed
        File home=WizardIterator.showFileChooser(this, NbBundle.getMessage(TestWorkspaceSettingsPanel.class, "Title_SelectNetbeansHome"), true, false); // NOI18N
        if (home!=null) 
            netbeansField.setText(home.getAbsolutePath());
    }//GEN-LAST:event_netbeansButtonActionPerformed

    private void xtestFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_xtestFieldFocusGained
        xtestField.selectAll();
    }//GEN-LAST:event_xtestFieldFocusGained

    private void netbeansFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_netbeansFieldFocusGained
        netbeansField.selectAll();
    }//GEN-LAST:event_netbeansFieldFocusGained

    private void attrFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_attrFieldFocusGained
        attrField.selectAll();
    }//GEN-LAST:event_attrFieldFocusGained

    private void typeFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_typeFieldFocusGained
        typeField.selectAll();
    }//GEN-LAST:event_typeFieldFocusGained

    private void levelComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_levelComboActionPerformed
        updatePanel();
    }//GEN-LAST:event_levelComboActionPerformed

    private void advancedCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advancedCheckActionPerformed
        updatePanel();
    }//GEN-LAST:event_advancedCheckActionPerformed

    private void updatePanel() {
        boolean advanced=advancedCheck.isSelected();
        levelLabel.setEnabled(!advanced);
        levelCombo.setEnabled(!advanced);
        typeLabel.setEnabled(advanced);
        typeField.setEnabled(advanced);
        attrLabel.setEnabled(advanced);
        attrField.setEnabled(advanced);
        netbeansLabel.setEnabled(advanced);
        netbeansField.setEnabled(advanced);
        netbeansButton.setEnabled(advanced);
        xtestLabel.setEnabled(advanced);
        xtestField.setEnabled(advanced);
        xtestButton.setEnabled(advanced);
        if (!advanced) {
            switch (levelCombo.getSelectedIndex()) {
                 case 0:netbeansField.setText(netbeansPath);
                        xtestField.setText(xtestPath);
                        jemmyHome=jemmyPath;
                        jellyHome=jellyPath;
                        break;
                 case 1:netbeansField.setText("../"+netbeansPath); // NOI18N
                        xtestField.setText("../"+xtestPath); // NOI18N
                        jemmyHome="../"+jemmyPath; // NOI18N
                        jellyHome="../"+jellyPath; // NOI18N
                        break;
                 case 2:netbeansField.setText("../../"+netbeansPath); // NOI18N
                        xtestField.setText("../../"+xtestPath); // NOI18N
                        jemmyHome="../../"+jemmyPath; // NOI18N
                        jellyHome="../../"+jellyPath; // NOI18N
                        break;
                 case 3:String home=System.getProperty("netbeans.home"); // NOI18N
                        netbeansLabel.setEnabled(true);
                        netbeansField.setEnabled(true);
                        netbeansButton.setEnabled(true);
                        if (!new File(home+File.separator+"xtest-distribution").exists())  // NOI18N
                            home=System.getProperty("netbeans.user"); // NOI18N
                        xtestField.setText(home+File.separator+"xtest-distribution"); // NOI18N
                        jemmyHome=home+File.separator+"modules"+File.separator+"ext"; // NOI18N
                        jellyHome=jemmyHome;
                        break;
             }
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel panel2;
    private javax.swing.JSeparator separator2;
    private javax.swing.JSeparator separator1;
    private javax.swing.JComboBox levelCombo;
    private javax.swing.JTextField typeField;
    private javax.swing.JButton xtestButton;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JButton netbeansButton;
    private javax.swing.JTextField netbeansField;
    private javax.swing.JLabel stopLabel;
    private javax.swing.JTextField xtestField;
    private javax.swing.JLabel levelLabel;
    private javax.swing.JLabel netbeansLabel;
    private javax.swing.JLabel xtestLabel;
    private javax.swing.JTextField attrField;
    private javax.swing.JCheckBox advancedCheck;
    private javax.swing.JLabel attrLabel;
    // End of variables declaration//GEN-END:variables
    
}

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
 * TestTypeAdvancedSettingsPanel.java
 *
 * Created on April 10, 2002, 1:44 PM
 */

import java.io.File;
import java.awt.Component;
import java.awt.CardLayout;
import javax.swing.JPanel;
import java.util.StringTokenizer;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;

import org.openide.util.HelpCtx;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/** Wizard Panel with Test Type Advanced Settings configuration
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class TestTypeAdvancedSettingsPanel extends JPanel {

    static final long serialVersionUID = 2537129285375022017L;

    private File baseDir=null;
    private String netbeansHome=null;
    
    public final Panel panel = new Panel();

    private class Panel extends Object implements WizardDescriptor.Panel {

        /** adds ChangeListener of current Panel
         * @param l ChangeListener */    
        public void addChangeListener(ChangeListener l) {}    

        /** returns current Panel
         * @return Component */    
        public Component getComponent() {
            return TestTypeAdvancedSettingsPanel.this;
        }    

        /** returns Help Context
         * @return HelpCtx */    
        public HelpCtx getHelp() {
            return new HelpCtx(TestTypeAdvancedSettingsPanel.class);
        }

        /** read settings from given Object
         * @param obj TemplateWizard with settings */    
        public void readSettings(Object obj) {
            WizardSettings set=WizardSettings.get(obj);
            if (set.typeJVMSuffix!=null)
                jvmField.setText(set.typeJVMSuffix);
            if (set.typeExcludes!=null)
                compileField.setText(set.typeExcludes);
            if (set.typeCompPath!=null)
                compileField.setText(set.typeCompPath);
            if (set.typeExecPath!=null)
                executeField.setText(set.typeExecPath);
            if (set.typeJemmyHome!=null)
                jemmyField.setText(set.typeJemmyHome);
            if (set.typeJellyHome!=null)
                jellyField.setText(set.typeJellyHome);
            TemplateWizard wizard=(TemplateWizard)obj;
            if (baseDir==null) try {
                baseDir=FileUtil.toFile(wizard.getTargetFolder().getPrimaryFile());
                if (set.startFromWorkspace) {
                    netbeansHome=set.netbeansHome;
                } else {
                    baseDir=baseDir.getParentFile();
                    XMLDocument doc=new XMLDocument(DataObject.find(wizard.getTargetFolder().getPrimaryFile().getFileObject("build","xml"))); // NOI18N
                    netbeansHome=doc.getProperty("netbeans.home","location"); // NOI18N
                }
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            }
        }

        /** removes Change Listener of current Panel
         * @param l ChangeListener */    
        public void removeChangeListener(ChangeListener l) {}

        /** stores settings to given Object
         * @param obj TemplateWizard with settings */    
        public void storeSettings(Object obj) {
            WizardSettings set=WizardSettings.get(obj);
            set.typeJVMSuffix=jvmField.getText();
            set.typeExcludes=excludesField.getText();
            set.typeCompPath=compileField.getText();
            set.typeExecPath=executeField.getText();
            set.typeJemmyHome=jemmyField.getText();
            set.typeJellyHome=jellyField.getText();
        }

        /** test current Panel state for data validity
         * @return boolean true if data are valid and Wizard can continue */    
        public boolean isValid() {
            return true;
        }

    }
    
    /** Creates new form TestTypeAdvancedSettingsPanel */
    public TestTypeAdvancedSettingsPanel() {
        setName(NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "LBL_TestTypeAdvancedPanelName")); // NOI18N
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup = new javax.swing.ButtonGroup();
        excludesLabel = new javax.swing.JLabel();
        excludesField = new javax.swing.JTextField();
        compileLabel = new javax.swing.JLabel();
        compileField = new javax.swing.JTextField();
        compileButton = new javax.swing.JButton();
        executeLabel = new javax.swing.JLabel();
        executeField = new javax.swing.JTextField();
        executeButton = new javax.swing.JButton();
        jvmLabel = new javax.swing.JLabel();
        jvmField = new javax.swing.JTextField();
        jemmyLabel = new javax.swing.JLabel();
        jemmyField = new javax.swing.JTextField();
        jemmyButton = new javax.swing.JButton();
        jellyLabel = new javax.swing.JLabel();
        jellyField = new javax.swing.JTextField();
        jellyButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        excludesLabel.setDisplayedMnemonic(NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "MNM_TestTypeCompExclude").charAt(0) );
        excludesLabel.setLabelFor(excludesField);
        excludesLabel.setText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "LBL_TestTypeCompExclPattern"));
        excludesLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "TTT_TestTypeCompExc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(excludesLabel, gridBagConstraints);

        excludesField.setText("**/data/**");
        excludesField.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "TTT_TestTypeCompExc"));
        excludesField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                excludesFieldFocusGained(evt);
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
        add(excludesField, gridBagConstraints);

        compileLabel.setDisplayedMnemonic(NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "MNM_TestTypeCompClassPath").charAt(0) );
        compileLabel.setLabelFor(compileField);
        compileLabel.setText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "LBL_TestTypeCompClassPath"));
        compileLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "TTT_CompileClassPath"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(compileLabel, gridBagConstraints);

        compileField.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "TTT_CompileClassPath"));
        compileField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                compileFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(compileField, gridBagConstraints);

        compileButton.setText("...");
        compileButton.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "TTT_CompileClassPath"));
        compileButton.setMinimumSize(new java.awt.Dimension(30, 20));
        compileButton.setPreferredSize(new java.awt.Dimension(30, 20));
        compileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compileButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(compileButton, gridBagConstraints);
        compileButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "CTL_CompClassPathCust"));

        executeLabel.setDisplayedMnemonic(NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "MNM_TestTypeExecExtraJARs").charAt(0) );
        executeLabel.setLabelFor(executeField);
        executeLabel.setText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "LBL_TestTypeExtraJARs"));
        executeLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "TTT_ExecExtraJARs"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(executeLabel, gridBagConstraints);

        executeField.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "TTT_ExecExtraJARs"));
        executeField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                executeFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(executeField, gridBagConstraints);

        executeButton.setText("...");
        executeButton.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "TTT_ExecExtraJARs"));
        executeButton.setMinimumSize(new java.awt.Dimension(30, 20));
        executeButton.setPreferredSize(new java.awt.Dimension(30, 20));
        executeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executeButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(executeButton, gridBagConstraints);
        executeButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "CTL_ExecutionExtraJarsCust"));

        jvmLabel.setDisplayedMnemonic(NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "MNM_TestTypeCMDSuffix").charAt(0) );
        jvmLabel.setLabelFor(jvmField);
        jvmLabel.setText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "LBL_TestTypeCommandLineSuffix"));
        jvmLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "TTT_TestTypeSuffix"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(jvmLabel, gridBagConstraints);

        jvmField.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "TTT_TestTypeSuffix"));
        jvmField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jvmFieldFocusGained(evt);
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
        add(jvmField, gridBagConstraints);

        jemmyLabel.setDisplayedMnemonic(NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "MNM_TestTypeJemmyHome").charAt(0) );
        jemmyLabel.setLabelFor(jemmyField);
        jemmyLabel.setText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "LBL_TestTypeJemmyJARHome"));
        jemmyLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "TTT_JemmyJARHome"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(jemmyLabel, gridBagConstraints);
        jemmyLabel.getAccessibleContext().setAccessibleName("Jemmy JAR Home: ");

        jemmyField.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "TTT_JemmyJARHome"));
        jemmyField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jemmyFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(jemmyField, gridBagConstraints);

        jemmyButton.setText("...");
        jemmyButton.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "TTT_JemmyJARHome"));
        jemmyButton.setMinimumSize(new java.awt.Dimension(30, 20));
        jemmyButton.setPreferredSize(new java.awt.Dimension(30, 20));
        jemmyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jemmyButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(jemmyButton, gridBagConstraints);
        jemmyButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "CTL_JemmyJARHomeCust"));

        jellyLabel.setDisplayedMnemonic(NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "MNM_TestTypeJellyHome").charAt(0) );
        jellyLabel.setLabelFor(jellyField);
        jellyLabel.setText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "LBL_TestTypeJellyJARHome"));
        jellyLabel.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "TTT_JellyJarHome"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(jellyLabel, gridBagConstraints);
        jellyLabel.getAccessibleContext().setAccessibleName("Jelly JAR Home: ");

        jellyField.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "TTT_JellyJarHome"));
        jellyField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jellyFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 11, 0);
        add(jellyField, gridBagConstraints);

        jellyButton.setText("...");
        jellyButton.setToolTipText(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "TTT_JellyJarHome"));
        jellyButton.setMinimumSize(new java.awt.Dimension(30, 20));
        jellyButton.setPreferredSize(new java.awt.Dimension(30, 20));
        jellyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jellyButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 11, 11);
        add(jellyButton, gridBagConstraints);
        jellyButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "CTL_JellyJARHomeCust"));

    }//GEN-END:initComponents

    private String substitutePath(File file, File dir, String subst) {
        try {
            if (!(dir.exists() && file.exists()))
                return null;
            String d=dir.getCanonicalPath()+file.separator;
            String f=file.getCanonicalPath();
            if (f.startsWith(d))
                return subst+'/'+f.substring(d.length()).replace('\\','/');
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }
        return null;
    }
    
    private String add(String path, File elem) {
        String file=null;
        if (netbeansHome!=null)
            file=substitutePath(elem, new File(baseDir, netbeansHome), "${netbeans.home}"); // NOI18N
        if (file==null && netbeansHome!=null)
            file=substitutePath(elem, new File(netbeansHome), "${netbeans.home}"); // NOI18N
        if (file==null)
            file=substitutePath(elem, new File(System.getProperty("netbeans.home")), "${netbeans.home}"); // NOI18N
        if (file==null)
            file=substitutePath(elem, baseDir, ".."); // NOI18N
        if (file==null)
            file=elem.getAbsolutePath();
        if (path.length()==0) 
            return file;
        StringTokenizer tok=new StringTokenizer(path, ":;"); // NOI18N
        while (tok.hasMoreTokens())
            if (file.equals(tok.nextToken())) return path;
        return path+';'+file;
    }    
    
    private void executeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executeButtonActionPerformed
        File elem=WizardIterator.showFileChooser(this, NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "LBL_TestTypeSelectJAR"), false, true); // NOI18N
        if (elem!=null) {
            executeField.setText(add(executeField.getText(), elem));
        }
    }//GEN-LAST:event_executeButtonActionPerformed

    private void compileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compileButtonActionPerformed
        File jar=WizardIterator.showFileChooser(this, NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "LBL_TestTypeSelectClassPathElement"), true, true); // NOI18N
        if (jar!=null) {
            compileField.setText(add(compileField.getText(), jar));
        }
    }//GEN-LAST:event_compileButtonActionPerformed

    private void jellyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jellyButtonActionPerformed
        File home=WizardIterator.showFileChooser(this, NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "LBL_TestTypeSelectJellyHome"), true, false); // NOI18N
        if (home!=null) 
            jellyField.setText(home.getAbsolutePath());
    }//GEN-LAST:event_jellyButtonActionPerformed

    private void jemmyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jemmyButtonActionPerformed
        File home=WizardIterator.showFileChooser(this, NbBundle.getMessage(TestTypeAdvancedSettingsPanel.class, "LBL_TestTypeSelectJemmyHome"), true, false); // NOI18N
        if (home!=null) 
            jemmyField.setText(home.getAbsolutePath());
    }//GEN-LAST:event_jemmyButtonActionPerformed

    private void jellyFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jellyFieldFocusGained
        jellyField.selectAll();
    }//GEN-LAST:event_jellyFieldFocusGained

    private void jemmyFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jemmyFieldFocusGained
        jemmyField.selectAll();
    }//GEN-LAST:event_jemmyFieldFocusGained

    private void jvmFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jvmFieldFocusGained
        jvmField.selectAll();
    }//GEN-LAST:event_jvmFieldFocusGained

    private void executeFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_executeFieldFocusGained
        executeField.selectAll();
    }//GEN-LAST:event_executeFieldFocusGained

    private void compileFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_compileFieldFocusGained
        compileField.selectAll();
    }//GEN-LAST:event_compileFieldFocusGained

    private void excludesFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_excludesFieldFocusGained
        excludesField.selectAll();
    }//GEN-LAST:event_excludesFieldFocusGained
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel executeLabel;
    private javax.swing.JTextField jellyField;
    private javax.swing.JLabel jellyLabel;
    private javax.swing.JButton jellyButton;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JTextField excludesField;
    private javax.swing.JTextField jemmyField;
    private javax.swing.JLabel excludesLabel;
    private javax.swing.JTextField jvmField;
    private javax.swing.JLabel jemmyLabel;
    private javax.swing.JLabel jvmLabel;
    private javax.swing.JButton jemmyButton;
    private javax.swing.JButton executeButton;
    private javax.swing.JTextField compileField;
    private javax.swing.JTextField executeField;
    private javax.swing.JButton compileButton;
    private javax.swing.JLabel compileLabel;
    // End of variables declaration//GEN-END:variables
    
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.mbeanwizard;

import java.awt.Component;
import javax.swing.event.*;
import java.util.ResourceBundle;
import org.openide.loaders.TemplateWizard;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.awt.Mnemonics;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.common.WizardHelpers;
import org.netbeans.modules.jmx.common.GenericWizardPanel;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JTextField;
import org.netbeans.api.java.source.JavaSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.jmx.ClassButton;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jmx.JavaModelHelper;

/**
 *
 * Class handling the graphical part of the standard MBean wizard panel
 *
 */
public class MBeanOptionsPanel extends javax.swing.JPanel
{
    private MBeanOptionsWizardPanel wiz;
    private ResourceBundle bundle;
    private ClassButton classButton = null;
    private Project project = null;
    
    private Integer orderNumber = 0;
    
    // temporary: for now, the user can proceed to the next panel automaticely
    private boolean mbeanNameSelected = true;
    private boolean descHasChanged = false;
    private boolean updateNameRunning = false;
    private boolean mbeanFromExistingClass = false;
    private boolean preRegParamSelected = false;
    private boolean mbeanRegIntfSelected = false;
    private static enum mbeanType {StandardMBean, DynamicMBean, ExtendedStandardMBean}private static mbeanType selectedMBeanType;
    
    /**
     * Create the wizard panel component and set up some basic properties
     * @param wiz a wizard panel to fill with user information
     */
    public MBeanOptionsPanel (final MBeanOptionsWizardPanel wiz) 
    {
        this.wiz = wiz;
        bundle = NbBundle.getBundle(MBeanOptionsPanel.class);
        initComponents ();
        mbeanDescriptionJTextField.addFocusListener(new FocusListener() {
           private String description = null;
           public void focusGained(FocusEvent e) {
               description = mbeanDescriptionJTextField.getText();
           }
           public void focusLost(FocusEvent e) {
               if (!description.equals(mbeanDescriptionJTextField.getText())) {
                   descHasChanged = true;
               }
           }
        });
        mbeanDescriptionJTextField.setName(  "mbeanDescriptionJTextField");// NOI18N
        // attach a documentlistener to the class text field to update the panel
        // each time the user fills something in to make sure it is not empty
        // and increments the order number hemce he changes the class to load
        classSelectionJTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent evt) {
                orderNumber++;
                wiz.fireEvent(); 
            }
            public void insertUpdate(DocumentEvent evt) {
                orderNumber++;
                wiz.fireEvent();
            }
            public void removeUpdate(DocumentEvent evt) {
                orderNumber++;
                wiz.fireEvent();
            }
        });
        // init flags
        selectedMBeanType = mbeanType.StandardMBean;
        
        // init labels
        Mnemonics.setLocalizedText(generatedFileJLabel,
                     bundle.getString("LBL_mbean_other_created_interface"));//NOI18N
        Mnemonics.setLocalizedText(mbeanDecriptionJLabel,
                     bundle.getString("LBL_mbean_description"));//NOI18N
        Mnemonics.setLocalizedText(mbeanTypeJLabel,
                     bundle.getString("LBL_mbean_type"));//NOI18N
        Mnemonics.setLocalizedText(standardMBeanJRadioButton,
                     bundle.getString("LBL_standard_mbean_type"));//NOI18N
        Mnemonics.setLocalizedText(extendedMBeanJRadioButton,
                     bundle.getString("LBL_extended_standard_mbean_type"));//NOI18N
        Mnemonics.setLocalizedText(dynamicMBeanJRadioButton,
                     bundle.getString("LBL_dynamic_mbean_type"));//NOI18N
        
        Mnemonics.setLocalizedText(fromExistingClassJCheckBox,
                     bundle.getString("LBL_from_existing_class"));//NOI18N
        Mnemonics.setLocalizedText(classSelectionJLabel,
                     bundle.getString("LBL_class_selection"));//NOI18N
        Mnemonics.setLocalizedText(mbeanRegistrationJCheckBox,
                     bundle.getString("LBL_registrationCbx"));//NOI18N
        Mnemonics.setLocalizedText(preRegisterParamJCheckBox,
                     bundle.getString("LBL_preRegisterParamCbx"));//NOI18N
        
        // Provide a name in the title bar.
        setName(bundle.getString("LBL_Standard_Options_Panel"));// NOI18N
        
        // Accessibility      
        generatedFileJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_GENERATED_FILE"));// NOI18N
        generatedFileJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_GENERATED_FILE_DESCRIPTION"));// NOI18N
        mbeanDescriptionJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_DESCRIPTION"));// NOI18N
        mbeanDescriptionJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_DESCRIPTION_DESCRIPTION"));// NOI18N
        
        mbeanDecriptionJLabel.setLabelFor(mbeanDescriptionJTextField);
        
        fromExistingClassJCheckBox.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_FROM_JAVA_CLASS"));// NOI18N
        fromExistingClassJCheckBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_FROM_JAVA_CLASS_DESCRIPTION"));// NOI18N
        classSelectionJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_FROM_JAVA_CLASS_VALUE"));// NOI18N
        classSelectionJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_FROM_JAVA_CLASS_VALUE_DESCRIPTION"));// NOI18N
        browseButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_FROM_JAVA_CLASS_BROWSE_VALUE"));// NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_FROM_JAVA_CLASS_BROWSE_VALUE_DESCRIPTION"));// NOI18N
        standardMBeanJRadioButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_STANDARD_MBEAN"));// NOI18N
        standardMBeanJRadioButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_STANDARD_MBEAN_DESCRIPTION"));// NOI18N
        extendedMBeanJRadioButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_EXTENDED_MBEAN"));// NOI18N
        extendedMBeanJRadioButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_EXTENDED_MBEAN_DESCRIPTION"));// NOI18N
        dynamicMBeanJRadioButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_DYNAMIC_MBEAN"));// NOI18N
        dynamicMBeanJRadioButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_DYNAMIC_MBEAN_DESCRIPTION"));// NOI18N
        mbeanRegistrationJCheckBox.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_REGISTRATION"));// NOI18N
        mbeanRegistrationJCheckBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_REGISTRATION_DESCRIPTION"));// NOI18N
        preRegisterParamJCheckBox.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_REGISTRATION_KEEP"));// NOI18N
        preRegisterParamJCheckBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_REGISTRATION_KEEP_DESCRIPTION"));// NOI18N
        
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_PANEL"));// NOI18N
    }
    
    public void setProject(Project project) {
        if (classButton == null)
            classButton = new ClassButton(browseButton,classSelectionJTextField,
                WizardHelpers.getSourceGroups(project));
    }
 
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mbeanTypeButtonGroup = new javax.swing.ButtonGroup();
        northCenterPanel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        mbeanDecriptionJLabel = new javax.swing.JLabel();
        mbeanDescriptionJTextField = new javax.swing.JTextField();
        generatedFileJLabel = new javax.swing.JLabel();
        generatedFileJTextField = new javax.swing.JTextField();
        mbeanTypeJLabel = new javax.swing.JLabel();
        standardMBeanJRadioButton = new javax.swing.JRadioButton();
        extendedMBeanJRadioButton = new javax.swing.JRadioButton();
        dynamicMBeanJRadioButton = new javax.swing.JRadioButton();
        fromExistingClassJCheckBox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        mbeanRegistrationJCheckBox = new javax.swing.JCheckBox();
        preRegisterParamJCheckBox = new javax.swing.JCheckBox();
        classSelectionJPanel = new javax.swing.JPanel();
        classSelectionJLabel = new javax.swing.JLabel();
        classSelectionJTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        setMinimumSize(new java.awt.Dimension(0, 0));
        setPreferredSize(new java.awt.Dimension(0, 0));
        northCenterPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        northCenterPanel.add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        northCenterPanel.add(mbeanDecriptionJLabel, gridBagConstraints);

        mbeanDescriptionJTextField.setName("mbeanDescriptionJTextField");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        northCenterPanel.add(mbeanDescriptionJTextField, gridBagConstraints);

        generatedFileJLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        generatedFileJLabel.setLabelFor(generatedFileJTextField);
        generatedFileJLabel.setMaximumSize(new java.awt.Dimension(1000, 1000));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        northCenterPanel.add(generatedFileJLabel, gridBagConstraints);

        generatedFileJTextField.setEditable(false);
        generatedFileJTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        generatedFileJTextField.setName("generatedFileJTextField");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        northCenterPanel.add(generatedFileJTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        northCenterPanel.add(mbeanTypeJLabel, gridBagConstraints);

        mbeanTypeButtonGroup.add(standardMBeanJRadioButton);
        standardMBeanJRadioButton.setSelected(true);
        standardMBeanJRadioButton.setName("StandardMBean");
        standardMBeanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                standardMBeanJRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 17, 0, 0);
        northCenterPanel.add(standardMBeanJRadioButton, gridBagConstraints);

        mbeanTypeButtonGroup.add(extendedMBeanJRadioButton);
        extendedMBeanJRadioButton.setName("ExtendedStandardMBean");
        extendedMBeanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extendedStandardMBeanJRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 17, 0, 0);
        northCenterPanel.add(extendedMBeanJRadioButton, gridBagConstraints);

        mbeanTypeButtonGroup.add(dynamicMBeanJRadioButton);
        dynamicMBeanJRadioButton.setName("DynamicMBean");
        dynamicMBeanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dynamicMBeanJRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 17, 0, 0);
        northCenterPanel.add(dynamicMBeanJRadioButton, gridBagConstraints);

        fromExistingClassJCheckBox.setName("ExistingClassCheckBox");
        fromExistingClassJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fromExistingClassJCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        northCenterPanel.add(fromExistingClassJCheckBox, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        mbeanRegistrationJCheckBox.setName("ImplementMBeanItf");
        mbeanRegistrationJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mbeanRegistrationJCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(mbeanRegistrationJCheckBox, gridBagConstraints);

        preRegisterParamJCheckBox.setEnabled(false);
        preRegisterParamJCheckBox.setName("PreRegisterParam");
        preRegisterParamJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preRegisterParamJCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        jPanel1.add(preRegisterParamJCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        northCenterPanel.add(jPanel1, gridBagConstraints);

        classSelectionJPanel.setLayout(new java.awt.GridBagLayout());

        classSelectionJLabel.setLabelFor(classSelectionJTextField);
        classSelectionJLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        classSelectionJPanel.add(classSelectionJLabel, gridBagConstraints);

        classSelectionJTextField.setEnabled(false);
        classSelectionJTextField.setName("ExistingClassTextField");
        classSelectionJTextField.setPreferredSize(new java.awt.Dimension(160, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        classSelectionJPanel.add(classSelectionJTextField, gridBagConstraints);

        browseButton.setText("jButton1");
        browseButton.setEnabled(false);
        browseButton.setName("browseButton");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        classSelectionJPanel.add(browseButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        northCenterPanel.add(classSelectionJPanel, gridBagConstraints);

        add(northCenterPanel, java.awt.BorderLayout.NORTH);

    }
    // </editor-fold>//GEN-END:initComponents

    private void preRegisterParamJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preRegisterParamJCheckBoxActionPerformed
        preRegParamSelected = preRegisterParamJCheckBox.isSelected();//GEN-HEADEREND:event_preRegisterParamJCheckBoxActionPerformed
    }//GEN-LAST:event_preRegisterParamJCheckBoxActionPerformed

    private void mbeanRegistrationJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mbeanRegistrationJCheckBoxActionPerformed
        mbeanRegIntfSelected = mbeanRegistrationJCheckBox.isSelected();//GEN-HEADEREND:event_mbeanRegistrationJCheckBoxActionPerformed
        preRegisterParamJCheckBox.setEnabled(mbeanRegIntfSelected);
    }//GEN-LAST:event_mbeanRegistrationJCheckBoxActionPerformed

    private void fromExistingClassJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fromExistingClassJCheckBoxActionPerformed
        //disable the components which the user can't choose//GEN-HEADEREND:event_fromExistingClassJCheckBoxActionPerformed
        mbeanFromExistingClass = fromExistingClassJCheckBox.isSelected();
        
        mbeanTypeJLabel.setEnabled(!mbeanFromExistingClass);
        standardMBeanJRadioButton.setEnabled(!mbeanFromExistingClass);
        dynamicMBeanJRadioButton.setEnabled(!mbeanFromExistingClass);
        
        extendedMBeanJRadioButton.setSelected(mbeanFromExistingClass);
        classSelectionJLabel.setEnabled(mbeanFromExistingClass);
        classSelectionJTextField.setEnabled(mbeanFromExistingClass);
        browseButton.setEnabled(mbeanFromExistingClass);
        
        wiz.storeSettings(wiz.templateWiz);
        wiz.readSettings(wiz.templateWiz);
        wiz.fireEvent();
    }//GEN-LAST:event_fromExistingClassJCheckBoxActionPerformed

    private void dynamicMBeanJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dynamicMBeanJRadioButtonActionPerformed
        wiz.storeSettings(wiz.templateWiz);//GEN-HEADEREND:event_dynamicMBeanJRadioButtonActionPerformed
        wiz.readSettings(wiz.templateWiz);
        wiz.fireEvent();
    }//GEN-LAST:event_dynamicMBeanJRadioButtonActionPerformed

    private void extendedStandardMBeanJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extendedStandardMBeanJRadioButtonActionPerformed
        wiz.storeSettings(wiz.templateWiz);//GEN-HEADEREND:event_extendedStandardMBeanJRadioButtonActionPerformed
        wiz.readSettings(wiz.templateWiz);
        wiz.fireEvent();
    }//GEN-LAST:event_extendedStandardMBeanJRadioButtonActionPerformed

    private void standardMBeanJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_standardMBeanJRadioButtonActionPerformed
        wiz.storeSettings(wiz.templateWiz);//GEN-HEADEREND:event_standardMBeanJRadioButtonActionPerformed
        wiz.readSettings(wiz.templateWiz);
        wiz.fireEvent();
    }//GEN-LAST:event_standardMBeanJRadioButtonActionPerformed
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel classSelectionJLabel;
    private javax.swing.JPanel classSelectionJPanel;
    private javax.swing.JTextField classSelectionJTextField;
    private javax.swing.JRadioButton dynamicMBeanJRadioButton;
    private javax.swing.JRadioButton extendedMBeanJRadioButton;
    private javax.swing.JCheckBox fromExistingClassJCheckBox;
    private javax.swing.JLabel generatedFileJLabel;
    private javax.swing.JTextField generatedFileJTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel mbeanDecriptionJLabel;
    private javax.swing.JTextField mbeanDescriptionJTextField;
    private javax.swing.JCheckBox mbeanRegistrationJCheckBox;
    private javax.swing.ButtonGroup mbeanTypeButtonGroup;
    private javax.swing.JLabel mbeanTypeJLabel;
    private javax.swing.JPanel northCenterPanel;
    private javax.swing.JCheckBox preRegisterParamJCheckBox;
    private javax.swing.JRadioButton standardMBeanJRadioButton;
    // End of variables declaration//GEN-END:variables
    
    /**
     *
     * Class handling the standard MBean wizard panel
     *
     */
    public static class MBeanOptionsWizardPanel extends GenericWizardPanel
            implements org.openide.WizardDescriptor.FinishablePanel 
    {    
        private MBeanOptionsPanel panel = null;
        private String projectLocation   = null;
        private ResourceBundle bundle = null;
        private TemplateWizard templateWiz = null;
        private JTextField mbeanNameTextField = null;
        //private JTextField createdFileTextField = null;
        private JTextField projectTextField = null;
        private WizardDescriptor.Panel mbeanTargetWiz = null;
        private Project project;
        /**
         * Constructor
         */
        public MBeanOptionsWizardPanel() {
        }
        
        /**
         * Implementation of the FinishablePanel Interface; provides the Finish Button to be always enabled 
         * @return finish true if the panel can be the last one and enables the finish button
         */
        public boolean isFinishPanel() //{ return isValid();}
        {
            if (!getPanel().fromExistingClassJCheckBox.isSelected())
                return isValid();
            else
                return false;
        }
        
        /**
         * Method returning the corresponding panel; here the StandardMBeanPanel
         * @return panel the panel
         */
        public Component getComponent () { return getPanel(); }
        
        /**
         * Method returning the project location
         * @return projectLocation the project location
         */
        public String getProjectLocation() { return projectLocation; }
        
        private MBeanOptionsPanel getPanel() 
        {
            if (panel == null) {
                panel = new MBeanOptionsPanel(this);
            }
            return panel;
        }
        
        /**
         * Method which returns the mbean type which has been selected
         * Either Standard ExtendedStandard or Dynamic
         * @return mbeanType the mbean type
         */
        public mbeanType mbeanTypeSelected() {
            return getPanel().selectedMBeanType;
        }
        
        /**
         * Method which returns whether the description has changed or not
         * @return descHasChanged true if the description has changed
         */
        public boolean descHasChanged() {
            return getPanel().descHasChanged;
        }

        /**
         * Method which enables the next button
         * @return boolean true if the information in the panel is sufficient 
         *  to go to the next step
         */
        public boolean isValid () {
            
            return (fileNotExists() && isFromExistingClassValid());
        }
        
        /**
         * Tests if the file already exists and displays an error message
         * @return boolean true if the file to be generated does not exist
         */
        public boolean fileNotExists() {
            if (WizardHelpers.fileExists(
                    getPanel().generatedFileJTextField.getText())) {
                setErrorMsg(  "The file " + // NOI18N
                        WizardHelpers.getFileName(
                            getPanel().generatedFileJTextField.getText()) +
                          " already exists.");// NOI18N
              return false;
            }
            return true;
        }
        
        /**
         * Tests if the mbean has to wrap an existing resource
         * If yes, the boolean returns whether the class to wrap exists and is
         * accessible from the project classpath
         * As long as the resource is not valid (i.e does not exist or is not
         * accessible) an error message is displayed
         * @return boolean if the option to wrap a class as mbean is checked
         * and the class is accessible from the classpath
         */
        public boolean isFromExistingClassValid() {
            boolean fromExistingClass = getPanel().classSelectionJTextField.isEnabled();
            if(fromExistingClass) {
                if(getPanel().classSelectionJTextField.getText().equals("")) { // NOI18N
                    setErrorMsg("Specify a class to wrap.");// NOI18N
                    return false;
                }
                
                
                String fullClassName = getPanel().
                        classSelectionJTextField.getText();
                
                String filePath =
                        (String)templateWiz.getProperty(WizardConstants.PROP_MBEAN_FILE_PATH);
                
                //gives an abstract representation of the directory
                File file = new File(filePath);
                FileObject fo = FileUtil.toFileObject(file);
                
                try {
                //resolves all accessible classes for the current project
                //classpath
                JavaSource mbeanClass = JavaModelHelper.findClassInProject(project, fullClassName);
               
                // checks that the class is neither null nor an interface nor
                // abstract
                if (mbeanClass == null) {
                    setErrorMsg(  "The specified class does not exist.");// NOI18N
                    return false;
                }
                
                boolean isInterface = JavaModelHelper.isInterface(mbeanClass);
                boolean isAbstract = JavaModelHelper.isAbstract(mbeanClass);
                if (isInterface) {
                    setErrorMsg(  "The specified class is an Interface.");// NOI18N
                    return false;
                }
                if (isAbstract) {
                    setErrorMsg(  "The specified class is abstract.");// NOI18N
                    return false;
                }
                }catch(IOException ioe) {
                    ioe.printStackTrace();
                    setErrorMsg(ioe.toString());
                }
            }
            setErrorMsg(null);
            return true;
        }        
        
        /**
         * Displays the given message in the wizard's message area.
         *
         * @param  message  message to be displayed, or <code>null</code>
         *                  if the message area should be cleared
         */
        private void setErrorMsg(String message) {
            if (templateWiz != null) {
                templateWiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
                        message);    //NOI18N
            }
        }
/*        
        private String getProjectDisplayedName() {
            Project project = Templates.getProject(templateWiz);
            return ProjectUtils.getInformation(project).getDisplayName();
        }
 **/
/*
        private void initTargetComponentDef(Component root) {
            if (root.getClass().equals(JTextField.class)) {
                JTextField rootTextField = (JTextField) root;
                if (rootTextField.isEditable()) {
                    mbeanNameTextField = ((JTextField) root);
                } else if (!rootTextField.getText().equals(getProjectDisplayedName())) {
                    if (projectTextField != null) {
                        createdFileTextField = ((JTextField) root);
                    }
                } else {
                    projectTextField = ((JTextField) root);
                }
            } else if ((root instanceof Container) && (root != getComponent())) {
                Component[] components = ((Container) root).getComponents();
                for (int i = 0; i < components.length; i++) {
                    initTargetComponentDef(components[i]);
                }
            }
        }
*/    
        /**
         * Method which fires an event to notify that there was a change in the data
         */
        public void fireEvent() {
            fireChangeEvent();
        }
        
        /**
         * Add a changeListener on the mbeanTargetWiz which each time that a change 
         * event happens, store and reload the mbeanOptionsWiz.
         * @param mbeanTargetWiz <CODE>Panel</CODE> 
         * @param mbeanOptionsWiz <CODE>Panel</CODE> 
         * @param wizard <CODE>WizardDescriptor</CODE> a wizard
         */
        public void setListenerEnabled(
                final WizardDescriptor.Panel mbeanTargetWiz,
                final WizardDescriptor.Panel mbeanOptionsWiz,
                final TemplateWizard wizard) {
            bundle = NbBundle.getBundle(JMXMBeanIterator.class);
            templateWiz = wizard;
            this.mbeanTargetWiz = mbeanTargetWiz;
            //initTargetComponentDef(mbeanTargetWiz.getComponent());
            mbeanTargetWiz.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent evt) {
                    mbeanOptionsWiz.storeSettings(wizard);
                    mbeanOptionsWiz.readSettings(wizard);
                    updateMBeanDesc(wizard);
                }
            });
        }
        
        private void updateMBeanDesc(TemplateWizard wizard) {
            if (!descHasChanged()) {
                String mbeanName = (String)
                        wizard.getProperty(WizardConstants.PROP_MBEAN_NAME);
                if ((mbeanName == null) || (mbeanName.equals(  ""))) {// NOI18N
                    mbeanName =   "MBean";// NOI18N
                }
                String desc = mbeanName + WizardConstants.MBEAN_DESCR_DEFVALUE;
                getPanel().mbeanDescriptionJTextField.setText(desc);
            }
        }
        
        /**
         * Method which Called to read information from the wizard map in order to populate
         * the GUI correctly
         * @param settings the object containing the data
         */
        public void readSettings (Object settings) 
        {
            
            templateWiz = (TemplateWizard) settings;
            //initTargetComponentDef(mbeanTargetWiz.getComponent());
            bundle = NbBundle.getBundle(JMXMBeanIterator.class);
            
            project = Templates.getProject(templateWiz);
            
            getPanel().setProject(project);
            
            String clzz =  bundle.getString("LBL_mbean_other_created_class");// NOI18N
            String itf = bundle.getString("LBL_mbean_other_created_interface");// NOI18N
            //String descr = bundle.getString("LBL_mbean_description");// NOI18N

            int maxWidth = 0;
            Mnemonics.setLocalizedText(getPanel().generatedFileJLabel, clzz);
            maxWidth = (int) getPanel().generatedFileJLabel.getSize().getWidth();
            Mnemonics.setLocalizedText(getPanel().generatedFileJLabel, itf);
            maxWidth = (int) getPanel().generatedFileJLabel.getSize().getWidth() > maxWidth ? (int) getPanel().generatedFileJLabel.getSize().getWidth() : maxWidth;
            maxWidth = (int) getPanel().mbeanDecriptionJLabel.getSize().getWidth() > maxWidth ? (int) getPanel().mbeanDecriptionJLabel.getSize().getWidth() : maxWidth;
            
            int maxHeight = 0;
            Mnemonics.setLocalizedText(getPanel().generatedFileJLabel, clzz);
            maxHeight = (int) getPanel().generatedFileJLabel.getSize().getHeight();
            Mnemonics.setLocalizedText(getPanel().generatedFileJLabel, itf);
            maxHeight = (int) getPanel().generatedFileJLabel.getSize().getHeight() > maxHeight ? (int) getPanel().generatedFileJLabel.getSize().getHeight() : maxHeight;
            maxHeight = (int) getPanel().mbeanDecriptionJLabel.getSize().getHeight() > maxHeight ? (int) getPanel().mbeanDecriptionJLabel.getSize().getHeight() : maxHeight;
            
            //The first time, the size is 0. We update size ONLY is maxSize != 0
            if(maxWidth != 0) {
                getPanel().generatedFileJLabel.setMinimumSize(new java.awt.Dimension(maxWidth, maxHeight));
                getPanel().generatedFileJLabel.setPreferredSize(new java.awt.Dimension(maxWidth, maxHeight));                
                getPanel().generatedFileJTextField.setMinimumSize(new java.awt.Dimension(maxWidth, maxHeight));
                getPanel().generatedFileJTextField.setPreferredSize(new java.awt.Dimension(maxWidth, maxHeight));                
                
                getPanel().mbeanDecriptionJLabel.setMinimumSize(new java.awt.Dimension(maxWidth, maxHeight));
                getPanel().mbeanDecriptionJLabel.setPreferredSize(new java.awt.Dimension(maxWidth, maxHeight));
                getPanel().mbeanDescriptionJTextField.setMinimumSize(new java.awt.Dimension(maxWidth, maxHeight));
                getPanel().mbeanDescriptionJTextField.setPreferredSize(new java.awt.Dimension(maxWidth, maxHeight));
            }
            
            // set tag for mbean type radio Button 
            String mbeanType = 
                    (String) templateWiz.getProperty(WizardConstants.PROP_MBEAN_TYPE);
            if (mbeanType != null) {
                getPanel().standardMBeanJRadioButton.setSelected(
                        mbeanType.equals(WizardConstants.MBEAN_STANDARDMBEAN));
                getPanel().extendedMBeanJRadioButton.setSelected(
                        mbeanType.equals(WizardConstants.MBEAN_EXTENDED));
                getPanel().dynamicMBeanJRadioButton.setSelected(
                        mbeanType.equals(WizardConstants.MBEAN_DYNAMICMBEAN));
            }
            
            String mbeanName = (String) 
                templateWiz.getProperty(WizardConstants.PROP_MBEAN_NAME);
            String mbeanFilePath = (String) 
                templateWiz.getProperty(WizardConstants.PROP_MBEAN_FILE_PATH);
            
            if ((mbeanName != null) && (mbeanFilePath != null)) {
                String suffix = null;
                
                if (getPanel().dynamicMBeanJRadioButton.isSelected()) {
                    suffix = WizardConstants.MBEAN_SUPPORT_SUFFIX;
                    Mnemonics.setLocalizedText(getPanel().generatedFileJLabel,
                     bundle.getString("LBL_mbean_other_created_class"));//NOI18N
                } else {
                    Mnemonics.setLocalizedText(getPanel().generatedFileJLabel,
                     bundle.getString("LBL_mbean_other_created_interface"));//NOI18N
                    suffix = WizardConstants.MBEAN_ITF_SUFFIX;
                }
                getPanel().generatedFileJTextField.setText(mbeanFilePath + 
                        File.separator + mbeanName +
                        suffix +   "." + WizardConstants.JAVA_EXT);// NOI18N

            } else {
                getPanel().generatedFileJTextField.setText(  "");// NOI18N
            }
            
            
            String desc = (String) 
                templateWiz.getProperty (WizardConstants.PROP_MBEAN_DESCRIPTION);
            if (desc == null) {
                if (mbeanName == null) {
                    desc =   "MBean" + WizardConstants.MBEAN_DESCR_DEFVALUE;// NOI18N
                } else {
                    desc = mbeanName + WizardConstants.MBEAN_DESCR_DEFVALUE;
                }
            } 
            getPanel().mbeanDescriptionJTextField.setText(desc);
            updateMBeanDesc(templateWiz);
            
            
            Integer orderNumber = (Integer)templateWiz.getProperty(
                    WizardConstants.PROP_USER_ORDER_NUMBER);
            
            if (orderNumber == null)
                orderNumber = 0;
            
            getPanel().orderNumber = orderNumber;
        }
        
        /**
         * Method called to store information from the GUI into the wizard map
         * @param settings the object containing the data to store
         */
        public void storeSettings (Object settings) 
        {            
            TemplateWizard wiz = (TemplateWizard) settings;
            
            project = Templates.getProject(templateWiz);
            
            //String mbeanName = mbeanNameTextField.getText();
            //wiz.putProperty (WizardConstants.PROP_MBEAN_NAME, mbeanName);
           
            /*
            Project project = Templates.getProject(wiz);
            if (createdFileTextField != null) {
                String filePath = WizardHelpers.getFolderPath(
                        createdFileTextField.getText());
                String packName =
                        WizardHelpers.getPackageName(project,filePath);
                wiz.putProperty(WizardConstants.PROP_MBEAN_FILE_PATH,filePath);
                wiz.putProperty(WizardConstants.PROP_MBEAN_PACKAGE_NAME, packName);
                wiz.putProperty(WizardConstants.PROP_MBEAN_PACKAGE_PATH,
                        packName.replace('.', File.separatorChar));
            }
             **/
            // storage of the description field
            String description = getPanel().mbeanDescriptionJTextField.getText();
            if ( (description != null) && (!description.equals(  "")) ) {// NOI18N
                wiz.putProperty (WizardConstants.PROP_MBEAN_DESCRIPTION, 
                        description);
            } else {
                wiz.putProperty (WizardConstants.PROP_MBEAN_DESCRIPTION,"");// NOI18N
            }
            // storage of the existing class to wrap if there is one
            if (getPanel().mbeanFromExistingClass) {
                wiz.putProperty (WizardConstants.PROP_MBEAN_EXISTING_CLASS, 
                        getExistingClass());
            } else {
                wiz.putProperty (WizardConstants.PROP_MBEAN_EXISTING_CLASS, 
                        null);
            }
            // store mbean type
            if (getPanel().standardMBeanJRadioButton.isSelected())  
                wiz.putProperty (WizardConstants.PROP_MBEAN_TYPE, 
                        WizardConstants.MBEAN_STANDARDMBEAN);
            else {
                if (getPanel().dynamicMBeanJRadioButton.isSelected())   
                    wiz.putProperty (WizardConstants.PROP_MBEAN_TYPE, 
                            WizardConstants.MBEAN_DYNAMICMBEAN);
                else { 
                    wiz.putProperty (WizardConstants.PROP_MBEAN_TYPE, 
                            WizardConstants.MBEAN_EXTENDED);
                }
            } 
           
           // store wether to implement mbeanregistration interface
           if (getPanel().mbeanRegIntfSelected) {
                wiz.putProperty(WizardConstants.PROP_MBEAN_IMPL_REG_ITF, 
                        getPanel().mbeanRegIntfSelected);
                if (getPanel().preRegParamSelected) 
                    wiz.putProperty(WizardConstants.PROP_MBEAN_PRE_REG_PARAM,  
                        getPanel().preRegParamSelected);
           }
            
           wiz.putProperty(WizardConstants.PROP_USER_ORDER_NUMBER, 
                        getPanel().orderNumber);
        }    
        
        private JavaSource getExistingClass() {
            String fullClassName = getPanel().
                    classSelectionJTextField.getText();
            try {
                return JavaModelHelper.findClassInProject(project, fullClassName);
            }catch(IOException ioe) {
                ioe.printStackTrace();
            }
            return null;
        }
        
        public HelpCtx getHelp() {
           return new HelpCtx("jmx_instrumenting_from_existing_app");  // NOI18N
        } 
    }
}

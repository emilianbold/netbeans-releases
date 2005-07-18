/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.mbeanwizard;

import java.awt.Component;
import javax.swing.event.*;
import java.util.ResourceBundle;

import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.openide.loaders.TemplateWizard;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.awt.Mnemonics;

import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.GenericWizardPanel;
import java.awt.Container;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import javax.swing.JTextField;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * Class handling the graphical part of the standard MBean wizard panel
 *
 */
public class StandardMBeanPanel extends javax.swing.JPanel
{
    private StandardMBeanWizardPanel wiz;
    private ResourceBundle bundle;
    
    // temporary: for now, the user can proceed to the next panel automaticely
    private boolean mbeanNameSelected = true;
    private boolean descHasChanged = false;
    private boolean updateNameRunning = false;
    private boolean mbeanFromExistingClass = false;
    private boolean mbeanRegIntfSelected = false;
    private static enum mbeanType {StandardMBean, DynamicMBean, ExtendedStandardMBean};
    private static mbeanType selectedMBeanType;
    
    /**
     * Create the wizard panel component and set up some basic properties
     * @param wiz a wizard panel to fill with user information
     */
    public StandardMBeanPanel (final StandardMBeanWizardPanel wiz) 
    {
        this.wiz = wiz;
        bundle = NbBundle.getBundle(StandardMBeanPanel.class);
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
        classSelectionJTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent evt) {
                wiz.fireEvent(); 
            }
            public void insertUpdate(DocumentEvent evt) {
                wiz.fireEvent();
            }
            public void removeUpdate(DocumentEvent evt) {
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
        Mnemonics.setLocalizedText(mbeanRegistrationJCheckBox,
                     bundle.getString("LBL_registrationCbx"));//NOI18N
        Mnemonics.setLocalizedText(preRegisterParamJCheckBox,
                     bundle.getString("LBL_preRegisterParamCbx"));//NOI18N
        
        // Provide a name in the title bar.
        setName(bundle.getString("LBL_Standard_Panel"));// NOI18N
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
        classSelectionJTextField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        mbeanRegistrationJCheckBox = new javax.swing.JCheckBox();
        preRegisterParamJCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        northCenterPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        northCenterPanel.add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        northCenterPanel.add(mbeanDecriptionJLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        northCenterPanel.add(mbeanDescriptionJTextField, gridBagConstraints);

        generatedFileJLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        generatedFileJLabel.setLabelFor(generatedFileJTextField);
        generatedFileJLabel.setMaximumSize(new java.awt.Dimension(1000, 1000));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        northCenterPanel.add(generatedFileJLabel, gridBagConstraints);

        generatedFileJTextField.setEditable(false);
        generatedFileJTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        generatedFileJTextField.setName("generatedFileJTextField");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        northCenterPanel.add(generatedFileJTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
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
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        northCenterPanel.add(standardMBeanJRadioButton, gridBagConstraints);

        mbeanTypeButtonGroup.add(extendedMBeanJRadioButton);
        extendedMBeanJRadioButton.setName("ExtendedStandardMBean");
        extendedMBeanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extendedStandardMBeanJRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 12, 0);
        northCenterPanel.add(extendedMBeanJRadioButton, gridBagConstraints);

        mbeanTypeButtonGroup.add(dynamicMBeanJRadioButton);
        dynamicMBeanJRadioButton.setName("DynamicMBean");
        dynamicMBeanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dynamicMBeanJRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 12, 0);
        northCenterPanel.add(dynamicMBeanJRadioButton, gridBagConstraints);

        fromExistingClassJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fromExistingClassJCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        northCenterPanel.add(fromExistingClassJCheckBox, gridBagConstraints);

        classSelectionJTextField.setEnabled(false);
        classSelectionJTextField.setMinimumSize(new java.awt.Dimension(4, 15));
        classSelectionJTextField.setPreferredSize(new java.awt.Dimension(160, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        northCenterPanel.add(classSelectionJTextField, gridBagConstraints);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        mbeanRegistrationJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mbeanRegistrationJCheckBoxActionPerformed(evt);
            }
        });

        jPanel1.add(mbeanRegistrationJCheckBox);

        preRegisterParamJCheckBox.setEnabled(false);
        jPanel1.add(preRegisterParamJCheckBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        northCenterPanel.add(jPanel1, gridBagConstraints);

        add(northCenterPanel, java.awt.BorderLayout.NORTH);

    }
    // </editor-fold>//GEN-END:initComponents

    private void mbeanRegistrationJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mbeanRegistrationJCheckBoxActionPerformed
        mbeanRegIntfSelected = mbeanRegistrationJCheckBox.isSelected();
        preRegisterParamJCheckBox.setEnabled(mbeanRegIntfSelected);
    }//GEN-LAST:event_mbeanRegistrationJCheckBoxActionPerformed

    private void fromExistingClassJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fromExistingClassJCheckBoxActionPerformed
        //disable the components which the user can't choose
        mbeanFromExistingClass = fromExistingClassJCheckBox.isSelected();
        
        mbeanTypeJLabel.setEnabled(!mbeanFromExistingClass);
        standardMBeanJRadioButton.setEnabled(!mbeanFromExistingClass);
        dynamicMBeanJRadioButton.setEnabled(!mbeanFromExistingClass);
        
        extendedMBeanJRadioButton.setSelected(mbeanFromExistingClass);
        classSelectionJTextField.setEnabled(mbeanFromExistingClass);
        //browseJButton.setEnabled(mbeanFromExistingClass);
        
        wiz.storeSettings(wiz.templateWiz);
        wiz.readSettings(wiz.templateWiz);
        wiz.fireEvent();
    }//GEN-LAST:event_fromExistingClassJCheckBoxActionPerformed

    private void dynamicMBeanJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dynamicMBeanJRadioButtonActionPerformed
        wiz.storeSettings(wiz.templateWiz);
        wiz.readSettings(wiz.templateWiz);
        wiz.fireEvent();
    }//GEN-LAST:event_dynamicMBeanJRadioButtonActionPerformed

    private void extendedStandardMBeanJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extendedStandardMBeanJRadioButtonActionPerformed
        wiz.storeSettings(wiz.templateWiz);
        wiz.readSettings(wiz.templateWiz);
        wiz.fireEvent();
    }//GEN-LAST:event_extendedStandardMBeanJRadioButtonActionPerformed

    private void standardMBeanJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_standardMBeanJRadioButtonActionPerformed
        wiz.storeSettings(wiz.templateWiz);
        wiz.readSettings(wiz.templateWiz);
        wiz.fireEvent();
    }//GEN-LAST:event_standardMBeanJRadioButtonActionPerformed
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
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
    public static class StandardMBeanWizardPanel extends GenericWizardPanel
            implements org.openide.WizardDescriptor.FinishablePanel 
    {    
        private StandardMBeanPanel panel = null;
        private String projectLocation   = null;
        private ResourceBundle bundle = null;
        private TemplateWizard templateWiz = null;
        private JTextField mbeanNameTextField = null;
        private JTextField createdFileTextField = null;
        private JTextField projectTextField = null;
        private WizardDescriptor.Panel mbeanTargetWiz = null;
        
        /**
         * Constructor
         */
        public StandardMBeanWizardPanel() {
        }
        
        /**
         * Implementation of the FinishablePanel Interface; provides the Finish Button to be always enabled 
         * @return finish true if the panel can be the last one and enables the finish button
         */
        public boolean isFinishPanel() { return isValid();}
        
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
        
        private StandardMBeanPanel getPanel() 
        {
            if (panel == null) {
                panel = new StandardMBeanPanel(this);
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
             
             if (getPanel().classSelectionJTextField.isEnabled() && 
                     !getPanel().classSelectionJTextField.getText().equals(  "")) // NOI18N
             {
                 
                 String fullClassName = getPanel().
                         classSelectionJTextField.getText();
                 
                 String filePath = WizardHelpers.getFolderPath(
                         createdFileTextField.getText());
                 
                 //gives an abstract representation of the directory
                 File file = new File(filePath);
                 FileObject fo = FileUtil.toFileObject(file);
                 
                 //resolves all accessible classes for the default project 
                 //classpath
                 JavaModelPackage pkg = JavaModel.getDefaultExtent();
                 
                 //checks if the class to wrap (i.e the class specified by 
                 //the user) is accessible from the project classpath
                 JavaClass mbeanClass = (JavaClass) pkg.getJavaClass().resolve(
                         fullClassName);
                 
                 if ((mbeanClass == null) || 
                         (mbeanClass.getClass().getName().startsWith(
                           "org.netbeans.jmi.javamodel.UnresolvedClass"))) {// NOI18N
                     setErrorMsg(  "The specified class does not exist");// NOI18N
                     
                     return false;
                    }  
             } else {
                 //condition on checked box but empty resource to load
                 if (getPanel().classSelectionJTextField.isEnabled() && 
                     getPanel().classSelectionJTextField.getText().equals(  "")) {// NOI18N
                     setErrorMsg(  "Specify an Existing class");// NOI18N
                     return false;
                 }
             }
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
                templateWiz.putProperty(WizardConstants.WIZARD_ERROR_MESSAGE, 
                        message);    //NOI18N
            }
        }
        
        private String getProjectDisplayedName() {
            Project project = Templates.getProject(templateWiz);
            return ProjectUtils.getInformation(project).getDisplayName();
        }

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
            initTargetComponentDef(mbeanTargetWiz.getComponent());
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
            initTargetComponentDef(mbeanTargetWiz.getComponent());
            bundle = NbBundle.getBundle(JMXMBeanIterator.class);
            
            
            String clzz =  bundle.getString("LBL_mbean_other_created_class");// NOI18N
            String itf = bundle.getString("LBL_mbean_other_created_interface");// NOI18N
            String descr = bundle.getString("LBL_mbean_description");// NOI18N

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
        }
        
        /**
         * Method called to store information from the GUI into the wizard map
         * @param settings the object containing the data to store
         */
        public void storeSettings (Object settings) 
        {            
            TemplateWizard wiz = (TemplateWizard) settings;
            
            String mbeanName = mbeanNameTextField.getText();
            wiz.putProperty (WizardConstants.PROP_MBEAN_NAME, mbeanName);
            wiz.putProperty (WizardConstants.PROP_JUNIT_CLASSNAME, 
                    mbeanName + WizardConstants.JUNIT_TEST_SUFFIX);
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
            // storage of the description field
            String description = getPanel().mbeanDescriptionJTextField.getText();
            if ( (description != null) && (!description.equals(  "")) ) {// NOI18N
                wiz.putProperty (WizardConstants.PROP_MBEAN_DESCRIPTION, 
                        description);
            } else {
                wiz.putProperty (WizardConstants.PROP_MBEAN_DESCRIPTION,   "");// NOI18N
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
            
        }    
        
        private JavaClass getExistingClass() {
            String fullClassName = getPanel().
                    classSelectionJTextField.getText();
            
            String filePath = WizardHelpers.getFolderPath(
                    createdFileTextField.getText());
            
            //gives an abstract representation of the directory
            File file = new File(filePath);
            FileObject fo = FileUtil.toFileObject(file);
            
            //resolves all accessible classes for the default project
            //classpath
            JavaModelPackage pkg = JavaModel.getDefaultExtent();
            
            //checks if the class to wrap (i.e the class specified by
            //the user) is accessible from the project classpath
            JavaClass mbeanClass = (JavaClass) pkg.getJavaClass().resolve(
                    fullClassName);
            return mbeanClass;
        }
        
        public HelpCtx getHelp() {
           return new HelpCtx(  "jmx_instrumenting_app");  // NOI18N
        } 
    }
}

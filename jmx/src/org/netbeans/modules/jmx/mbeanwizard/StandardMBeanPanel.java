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
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.openide.loaders.TemplateWizard;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.common.WizardHelpers;
import org.netbeans.modules.jmx.common.GenericWizardPanel;
import java.awt.Container;
import javax.swing.JTextField;
import org.netbeans.api.project.ProjectUtils;
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
    //private static enum mbeanType {StandardMBean, DynamicMBean, ExtendedStandardMBean};
    //private static mbeanType selectedMBeanType;
    
    /**
     * Create the wizard panel component and set up some basic properties
     * @param wiz a wizard panel to fill with user information
     */
    public StandardMBeanPanel (final StandardMBeanWizardPanel wiz) 
    {
        this.wiz = wiz;
        bundle = NbBundle.getBundle(StandardMBeanPanel.class);
        initComponents ();
        /* A transferer dans l'autre fichier
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
         **/
        // attach a documentlistener to the class text field to update the panel
        // each time the user fills something in to make sure it is not empty
        /*
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
         Nexiste plus dans ce fichier*/
        

        // init flags // does not exist any more
        //selectedMBeanType = mbeanType.StandardMBean;
        
        // init labels
        /* Does not exist any more
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
        */
        // Provide a name in the title bar.
        setName(bundle.getString("LBL_Standard_Panel"));// NOI18N
        
        // Accessibility   
        /* Does not exist any more
        generatedFileJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_GENERATED_FILE"));// NOI18N
        generatedFileJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_GENERATED_FILE_DESCRIPTION"));// NOI18N
        mbeanDescriptionJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_DESCRIPTION"));// NOI18N
        mbeanDescriptionJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_DESCRIPTION_DESCRIPTION"));// NOI18N
        
        mbeanDecriptionJLabel.setLabelFor(mbeanDescriptionJTextField);
        
        fromExistingClassJCheckBox.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_FROM_JAVA_CLASS"));// NOI18N
        fromExistingClassJCheckBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_FROM_JAVA_CLASS_DESCRIPTION"));// NOI18N
        classSelectionJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_FROM_JAVA_CLASS_VALUE"));// NOI18N
        classSelectionJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_FROM_JAVA_CLASS_VALUE_DESCRIPTION"));// NOI18N
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
        */
    }
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mbeanTypeButtonGroup = new javax.swing.ButtonGroup();
        mbeanDecriptionJLabel = new javax.swing.JLabel();
        mbeanDescriptionJTextField = new javax.swing.JTextField();
        generatedFileJLabel = new javax.swing.JLabel();
        generatedFileJTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        mbeanDecriptionJLabel.setLabelFor(mbeanDescriptionJTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(mbeanDecriptionJLabel, gridBagConstraints);

        mbeanDescriptionJTextField.setName("mbeanDescriptionJTextField"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        add(mbeanDescriptionJTextField, gridBagConstraints);

        generatedFileJLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        generatedFileJLabel.setLabelFor(generatedFileJTextField);
        generatedFileJLabel.setMaximumSize(new java.awt.Dimension(1000, 1000));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(generatedFileJLabel, gridBagConstraints);

        generatedFileJTextField.setEditable(false);
        generatedFileJTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        generatedFileJTextField.setName("generatedFileJTextField"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        add(generatedFileJTextField, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel generatedFileJLabel;
    private javax.swing.JTextField generatedFileJTextField;
    private javax.swing.JLabel mbeanDecriptionJLabel;
    private javax.swing.JTextField mbeanDescriptionJTextField;
    private javax.swing.ButtonGroup mbeanTypeButtonGroup;
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
        public boolean isFinishPanel() { return false;} //{ return isValid();}
        
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
        /* does not exist any more *
        public mbeanType mbeanTypeSelected() {
            return getPanel().selectedMBeanType;
        }
        */
        /**
         * Method which returns whether the description has changed or not
         * @return descHasChanged true if the description has changed
         */
        /* transfered
        public boolean descHasChanged() {
            return getPanel().descHasChanged;
        }
        */
        /**
         * Method which enables the next button
         * @return boolean true if the information in the panel is sufficient 
         *  to go to the next step
         */
        public boolean isValid () {
            
            //return (fileNotExists() && isFromExistingClassValid());
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
            templateWiz = wizard;
            this.mbeanTargetWiz = mbeanTargetWiz;
            initTargetComponentDef(mbeanTargetWiz.getComponent());
            mbeanTargetWiz.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent evt) {
                    mbeanOptionsWiz.storeSettings(wizard);
                    mbeanOptionsWiz.readSettings(wizard);
                    //updateMBeanDesc(wizard);
                }
            });
        }
        /*
         * transfered
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
        */
        
        /**
         * Method which Called to read information from the wizard map in order to populate
         * the GUI correctly
         * @param settings the object containing the data
         */
        public void readSettings (Object settings) 
        {
            
            templateWiz = (TemplateWizard) settings;
            initTargetComponentDef(mbeanTargetWiz.getComponent());
            
            
            //String clzz =  bundle.getString("LBL_mbean_other_created_class");// NOI18N
            //String itf = bundle.getString("LBL_mbean_other_created_interface");// NOI18N
            //String descr = bundle.getString("LBL_mbean_description");// NOI18N
            
            /* transfered
            
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
            */
            String mbeanName = (String) 
                templateWiz.getProperty(WizardConstants.PROP_MBEAN_NAME);
            String mbeanFilePath = (String) 
                templateWiz.getProperty(WizardConstants.PROP_MBEAN_FILE_PATH);
            
            /* transfered
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
             **/
            
            /* transfered
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
             */
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
            Project project = Templates.getProject(wiz);
            if (createdFileTextField != null) {
                String filePath = WizardHelpers.getFolderPath(
                        createdFileTextField.getText());
                String packName =
                        WizardHelpers.getPackageName(project,filePath);
                wiz.putProperty(WizardConstants.PROP_MBEAN_FILE_PATH,filePath);
                wiz.putProperty(WizardConstants.PROP_MBEAN_PACKAGE_NAME, packName);
                
            }
            
            /* transfered
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
            */
        }    
        /* transfered
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
        */
        public HelpCtx getHelp() {
           return new HelpCtx(  "tutorial");  // NOI18N
        } 
    }
}

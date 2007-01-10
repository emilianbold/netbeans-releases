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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.GenericWizardPanel;
import java.awt.Container;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.jmx.ClassButton;
import org.netbeans.modules.jmx.JavaModelHelper;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
/**
 *
 * Class handling the graphical part of the standard MBean wizard panel
 *
 */
public class MBeanPanel extends javax.swing.JPanel
{
    private MBeanPanelWizardPanel wiz;
    private ResourceBundle bundle;
    private ClassButton classButton = null;
   
    // temporary: for now, the user can proceed to the next panel automaticely
    private boolean mbeanNameSelected = true;
    private boolean descHasChanged = false;
    private boolean updateNameRunning = false;
    private Integer orderNumber = 0;
    /**
     * Create the wizard panel component and set up some basic properties
     * @param wiz a wizard panel to fill with user information
     */
    public MBeanPanel(final MBeanPanelWizardPanel wiz) 
    {
        this.wiz = wiz;
        bundle = NbBundle.getBundle(MBeanPanel.class);
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
        
        // init labels
        Mnemonics.setLocalizedText(generatedFileJLabel,
                     bundle.getString("LBL_mbean_other_created_interface"));//NOI18N
        Mnemonics.setLocalizedText(mbeanDecriptionJLabel,
                     bundle.getString("LBL_mbean_description"));//NOI18N
        Mnemonics.setLocalizedText(classSelectionJLabel,
                     bundle.getString("LBL_class_selection"));//NOI18N
        // Provide a name in the title bar.
        setName(bundle.getString("LBL_Standard_Panel"));// NOI18N
        
        // Accessibility   
        generatedFileJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_GENERATED_FILE"));// NOI18N
        generatedFileJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_GENERATED_FILE_DESCRIPTION"));// NOI18N
        mbeanDescriptionJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_DESCRIPTION"));// NOI18N
        mbeanDescriptionJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_DESCRIPTION_DESCRIPTION"));// NOI18N
        browseButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_FROM_JAVA_CLASS_BROWSE_VALUE"));// NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_FROM_JAVA_CLASS_BROWSE_VALUE_DESCRIPTION"));// NOI18N

        mbeanDecriptionJLabel.setLabelFor(mbeanDescriptionJTextField);
       
        classSelectionJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_FROM_JAVA_CLASS_VALUE"));// NOI18N
        classSelectionJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_FROM_JAVA_CLASS_VALUE_DESCRIPTION"));// NOI18N
    }
    
    public void setProject(Project project) {
        if (classButton == null && wiz.mbeanType.equals(WizardConstants.MBEAN_FROM_EXISTING_CLASS))
            classButton = new ClassButton(browseButton,classSelectionJTextField,WizardHelpers.getSourceGroups(project));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        mbeanTypeButtonGroup = new javax.swing.ButtonGroup();
        mbeanDescriptionJTextField = new javax.swing.JTextField();
        generatedFileJTextField = new javax.swing.JTextField();
        mbeanDecriptionJLabel = new javax.swing.JLabel();
        generatedFileJLabel = new javax.swing.JLabel();
        classSelectionJLabel = new javax.swing.JLabel();
        classSelectionJTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();

        mbeanDescriptionJTextField.setName("mbeanDescriptionJTextField");

        generatedFileJTextField.setEditable(false);
        generatedFileJTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        generatedFileJTextField.setName("generatedFileJTextField");

        mbeanDecriptionJLabel.setText("jLabel1");

        generatedFileJLabel.setText("jLabel2");

        classSelectionJLabel.setText("jLabel3");

        classSelectionJTextField.setName("ExistingClassTextField");
        classSelectionJTextField.setPreferredSize(new java.awt.Dimension(160, 19));

        browseButton.setText("jButton1");
        browseButton.setName("browseButton");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(generatedFileJLabel)
                    .add(mbeanDecriptionJLabel)
                    .add(classSelectionJLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mbeanDescriptionJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(classSelectionJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(generatedFileJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(generatedFileJLabel)
                    .add(generatedFileJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(mbeanDecriptionJLabel)
                    .add(mbeanDescriptionJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(27, 27, 27)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(classSelectionJLabel)
                    .add(classSelectionJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel classSelectionJLabel;
    private javax.swing.JTextField classSelectionJTextField;
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
    public static class MBeanPanelWizardPanel extends GenericWizardPanel
            implements org.openide.WizardDescriptor.FinishablePanel 
    {    
        private MBeanPanel panel = null;
        private String projectLocation   = null;
        private ResourceBundle bundle = null;
        private TemplateWizard templateWiz = null;
        private JTextField mbeanNameTextField = null;
        private JTextField createdFileTextField = null;
        private JTextField projectTextField = null;
        private WizardDescriptor.Panel mbeanTargetWiz = null;
        private String mbeanType;
        private Project project;
        
        public MBeanPanelWizardPanel(String mbeanType) {
            bundle = NbBundle.getBundle(MBeanPanelWizardPanel.class);
            this.mbeanType = mbeanType;
        }
        
        /**
         * Implementation of the FinishablePanel Interface; provides the Finish Button to be always enabled 
         * @return finish true if the panel can be the last one and enables the finish button
         */
        public boolean isFinishPanel() { 
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
        
        private MBeanPanel getPanel() 
        {
            if (panel == null) {
                panel = new MBeanPanel(this);
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
            if(!mbeanType.equals(WizardConstants.MBEAN_FROM_EXISTING_CLASS))
                return true;
            
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
            setErrorMsg(WizardConstants.EMPTYSTRING);
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
            
            project = Templates.getProject(templateWiz);
            
            getPanel().setProject(project);
            
            String mbeanName = (String) 
                templateWiz.getProperty(WizardConstants.PROP_MBEAN_NAME);
            String mbeanFilePath = (String) 
                templateWiz.getProperty(WizardConstants.PROP_MBEAN_FILE_PATH);
            System.out.println(" getPanel()" + getPanel());
            System.out.println(" getPanel().generatedFileJLabel" + getPanel().generatedFileJLabel);
            System.out.println(" bundle " + bundle);
            Mnemonics.setLocalizedText(getPanel().generatedFileJLabel,
            bundle.getString("LBL_mbean_other_created_interface"));//NOI18N

            getPanel().generatedFileJTextField.setText(mbeanFilePath + 
                        File.separator + mbeanName +
                        "MXBean" + "." + WizardConstants.JAVA_EXT);// NOI18N
            
            String itf = bundle.getString("LBL_mbean_other_created_interface");// NOI18N
            String descr = bundle.getString("LBL_mbean_description");// NOI18N
            
            
            int maxWidth = 0;
            
            Mnemonics.setLocalizedText(getPanel().generatedFileJLabel, itf);
            maxWidth = (int) getPanel().generatedFileJLabel.getSize().getWidth();
            Mnemonics.setLocalizedText(getPanel().generatedFileJLabel, itf);
            maxWidth = (int) getPanel().generatedFileJLabel.getSize().getWidth() > maxWidth ? (int) getPanel().generatedFileJLabel.getSize().getWidth() : maxWidth;
            maxWidth = (int) getPanel().mbeanDecriptionJLabel.getSize().getWidth() > maxWidth ? (int) getPanel().mbeanDecriptionJLabel.getSize().getWidth() : maxWidth;
            
            int maxHeight = 0;
            Mnemonics.setLocalizedText(getPanel().generatedFileJLabel, itf);
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
            
            if ((mbeanName != null) && (mbeanFilePath != null)) {
                String suffix = null;
                if(mbeanType.equals(WizardConstants.MBEAN_DYNAMICMBEAN)) {
                    Mnemonics.setLocalizedText(getPanel().generatedFileJLabel,
                     bundle.getString("LBL_mbean_other_created_class"));//NOI18N
                    suffix = WizardConstants.MBEAN_SUPPORT_SUFFIX;
                } else {
                    if(mbeanType.equals(WizardConstants.MBEAN_STANDARDMBEAN) || mbeanType.equals(WizardConstants.MBEAN_EXTENDED)) {
                        suffix = WizardConstants.MBEAN_ITF_SUFFIX;
                    } else
                        if(mbeanType.equals(WizardConstants.MXBEAN))
                            suffix = WizardConstants.MXBEAN_SUFFIX;
                        
                    Mnemonics.setLocalizedText(getPanel().generatedFileJLabel,
                        bundle.getString("LBL_mbean_other_created_interface"));//NOI18N
                }
                
                getPanel().generatedFileJTextField.setText(mbeanFilePath +
                        File.separator + mbeanName +
                        suffix +   "." + WizardConstants.JAVA_EXT);// NOI18N
                
            } else {
                getPanel().generatedFileJTextField.setText(  "");// NOI18N
            }
            
            if(mbeanType.equals(WizardConstants.MBEAN_FROM_EXISTING_CLASS)) {
                getPanel().classSelectionJLabel.setVisible(true);
                getPanel().classSelectionJTextField.setVisible(true);
                getPanel().browseButton.setVisible(true);
            } else {
                getPanel().classSelectionJLabel.setVisible(false);
                getPanel().classSelectionJTextField.setVisible(false);
                getPanel().browseButton.setVisible(false);
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
            
            // storage of the description field
            String description = getPanel().mbeanDescriptionJTextField.getText();
            if ( (description != null) && (!description.equals(  "")) ) {// NOI18N
                wiz.putProperty (WizardConstants.PROP_MBEAN_DESCRIPTION, 
                        description);
            } else {
                wiz.putProperty (WizardConstants.PROP_MBEAN_DESCRIPTION,   "");// NOI18N
            }
           
            wiz.putProperty (WizardConstants.PROP_MBEAN_EXISTING_CLASS, 
                        getExistingClass());
             
            wiz.putProperty (WizardConstants.PROP_MBEAN_TYPE, 
                        mbeanType);
            
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
           return new HelpCtx(  "jmx_instrumenting_app");  // NOI18N
        } 
    }
}

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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
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
        Mnemonics.setLocalizedText(isMXBean,bundle.getString("LBL_IsMXBean"));//NOI18N
       
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
        isMXBean.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_IS_MXBEAN"));// NOI18N
        isMXBean.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_IS_MXBEAN_DESCRIPTION"));// NOI18N
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mbeanTypeButtonGroup = new javax.swing.ButtonGroup();
        mbeanDescriptionJTextField = new javax.swing.JTextField();
        generatedFileJTextField = new javax.swing.JTextField();
        mbeanDecriptionJLabel = new javax.swing.JLabel();
        generatedFileJLabel = new javax.swing.JLabel();
        classSelectionJLabel = new javax.swing.JLabel();
        classSelectionJTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        isMXBean = new javax.swing.JCheckBox();

        mbeanDescriptionJTextField.setName("mbeanDescriptionJTextField"); // NOI18N

        generatedFileJTextField.setEditable(false);
        generatedFileJTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        generatedFileJTextField.setName("generatedFileJTextField"); // NOI18N

        mbeanDecriptionJLabel.setLabelFor(mbeanDescriptionJTextField);
        mbeanDecriptionJLabel.setText("jLabel1");

        generatedFileJLabel.setLabelFor(generatedFileJTextField);
        generatedFileJLabel.setText("jLabel2");

        classSelectionJLabel.setLabelFor(classSelectionJTextField);
        classSelectionJLabel.setText("jLabel3");

        classSelectionJTextField.setName("ExistingClassTextField"); // NOI18N
        classSelectionJTextField.setPreferredSize(new java.awt.Dimension(160, 19));

        browseButton.setText("jButton1");
        browseButton.setName("browseButton"); // NOI18N

        isMXBean.setMnemonic('i');
        isMXBean.setText("isMXBean");
        isMXBean.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        isMXBean.setMargin(new java.awt.Insets(0, 0, 0, 0));
        isMXBean.setName("isMXBeanCheckBox"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(generatedFileJLabel)
                            .addComponent(mbeanDecriptionJLabel)
                            .addComponent(classSelectionJLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mbeanDescriptionJTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(classSelectionJTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseButton))
                            .addComponent(generatedFileJTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)))
                    .addComponent(isMXBean))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(generatedFileJLabel)
                    .addComponent(generatedFileJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mbeanDecriptionJLabel)
                    .addComponent(mbeanDescriptionJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(classSelectionJLabel)
                    .addComponent(classSelectionJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(isMXBean)
                .addContainerGap(42, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel classSelectionJLabel;
    private javax.swing.JTextField classSelectionJTextField;
    private javax.swing.JLabel generatedFileJLabel;
    private javax.swing.JTextField generatedFileJTextField;
    private javax.swing.JCheckBox isMXBean;
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
            
            if(getPanel().classSelectionJTextField.getText().equals(WizardConstants.EMPTYSTRING)) { // NOI18N
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
                if (templateWiz.getProperty(WizardDescriptor.PROP_ERROR_MESSAGE) == null) {
                    templateWiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
                }
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
                    if(!"ComboBox.textField".equals(rootTextField.getName()))
                        mbeanNameTextField = (rootTextField);
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
                    if(mbeanType.equals(WizardConstants.MBEAN_STANDARDMBEAN) || 
                       mbeanType.equals(WizardConstants.MBEAN_EXTENDED) ||
                       mbeanType.equals(WizardConstants.MBEAN_FROM_EXISTING_CLASS)) {
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
                getPanel().isMXBean.setVisible(true);
            } else {
                getPanel().classSelectionJLabel.setVisible(false);
                getPanel().classSelectionJTextField.setVisible(false);
                getPanel().browseButton.setVisible(false);
                getPanel().isMXBean.setVisible(false);
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
            wiz.putProperty (WizardConstants.PROP_MBEAN_EXISTING_CLASS_IS_MXBEAN, 
                        getPanel().isMXBean.isSelected());
             
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
            if(mbeanType.equals(WizardConstants.MXBEAN) ||
               mbeanType.equals(WizardConstants.MBEAN_STANDARDMBEAN))
                return new HelpCtx("tutorial");
            else
            return new HelpCtx(  "jmx_instrumenting_app");  // NOI18N
        } 
    }
}

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
package org.netbeans.modules.jmx.configwizard;

import java.awt.Component;
import java.util.ResourceBundle;
import javax.swing.event.*;

import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.loaders.TemplateWizard;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.awt.Mnemonics;

import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.common.GenericWizardPanel;
import org.netbeans.modules.jmx.common.WizardHelpers;
import java.awt.Container;
import java.io.File;
import javax.swing.JTextField;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;

/**
 *
 * Class handling the graphical part of the standard Agent wizard panel
 *
 */
public class ConfigPanel extends javax.swing.JPanel
{
    private ConfigWizardPanel wiz;
    private ResourceBundle bundle;
    
    /**
     * Create the wizard panel component and set up some basic properties.
     * @param wiz <CODE>WizardDescriptor</CODE> the wizard
     */
    public ConfigPanel (ConfigWizardPanel wiz) 
    {
        this.wiz = wiz;
        bundle = NbBundle.getBundle(ConfigPanel.class);
        initComponents ();
        
        Mnemonics.setLocalizedText(rmiAccessFileJLabel,
                     bundle.getString("LBL_RMI_Access_File"));//NOI18N
        Mnemonics.setLocalizedText(rmiPasswordFileJLabel,
                     bundle.getString("LBL_RMI_Password_File"));//NOI18N
        
        
        Mnemonics.setLocalizedText(threadContentionJCheckBox,
                                   bundle.getString("LBL_ThreadContention"));//NOI18N
        
        
        //Set tooltips
        threadContentionJCheckBox.setToolTipText(bundle.getString("TLTP_Thread_Contention"));//NOI18N
        
        // Provide a name in the title bar.
        setName(bundle.getString("LBL_Config_Panel")); // NOI18N
                
        //Accessibility
        threadContentionJCheckBox.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_OTHER_CONTENTION")); // NOI18N
        threadContentionJCheckBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_OTHER_CONTENTION_DESCRIPTION"));// NOI18N

        rmiAccessFileJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_RMI_ACCESS_FILE")); // NOI18N
        rmiAccessFileJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_RMI_ACCESS_FILE_DESCRIPTION"));// NOI18N
        
        rmiPasswordFileJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_RMI_PASSWORD_FILE"));// NOI18N
        rmiPasswordFileJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_RMI_PASSWORD_FILE_DESCRIPTION"));// NOI18N

        getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_PANEL"));// NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        northPanel = new javax.swing.JPanel();
        northWestCenterPanel = new javax.swing.JPanel();
        rmiAccessFileJLabel = new javax.swing.JLabel();
        rmiPasswordFileJLabel = new javax.swing.JLabel();
        rmiAccessFileJTextField = new javax.swing.JTextField();
        rmiPasswordFileJTextField = new javax.swing.JTextField();
        threadContentionJCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        northPanel.setLayout(new java.awt.BorderLayout());

        northWestCenterPanel.setLayout(new java.awt.GridBagLayout());

        rmiAccessFileJLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        rmiAccessFileJLabel.setLabelFor(rmiAccessFileJTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        northWestCenterPanel.add(rmiAccessFileJLabel, gridBagConstraints);

        rmiPasswordFileJLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        rmiPasswordFileJLabel.setLabelFor(rmiPasswordFileJTextField);
        rmiPasswordFileJLabel.setAlignmentX(0.5F);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        northWestCenterPanel.add(rmiPasswordFileJLabel, gridBagConstraints);

        rmiAccessFileJTextField.setEditable(false);
        rmiAccessFileJTextField.setName("rmiAccessFileJTextField");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        northWestCenterPanel.add(rmiAccessFileJTextField, gridBagConstraints);

        rmiPasswordFileJTextField.setEditable(false);
        rmiPasswordFileJTextField.setName("rmiPasswordFileJTextField");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        northWestCenterPanel.add(rmiPasswordFileJTextField, gridBagConstraints);

        threadContentionJCheckBox.setText("jCheckBox1");
        threadContentionJCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        threadContentionJCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        threadContentionJCheckBox.setName("threadContentionJCheckBox");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(13, 0, 0, 0);
        northWestCenterPanel.add(threadContentionJCheckBox, gridBagConstraints);

        northPanel.add(northWestCenterPanel, java.awt.BorderLayout.CENTER);

        add(northPanel, java.awt.BorderLayout.NORTH);

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel northPanel;
    private javax.swing.JPanel northWestCenterPanel;
    private javax.swing.JLabel rmiAccessFileJLabel;
    private javax.swing.JTextField rmiAccessFileJTextField;
    private javax.swing.JLabel rmiPasswordFileJLabel;
    private javax.swing.JTextField rmiPasswordFileJTextField;
    private javax.swing.JCheckBox threadContentionJCheckBox;
    // End of variables declaration//GEN-END:variables
    
    /**
     *
     * Class handling the standard Agent wizard panel
     *
     */
    public static class ConfigWizardPanel extends GenericWizardPanel 
            implements org.openide.WizardDescriptor.FinishablePanel
    {    
        private ConfigPanel panel = null;
        private String projectLocation   = null;
        private TemplateWizard templateWiz = null;
        private WizardDescriptor.Panel targetWiz = null;
        private JTextField createdFileTextField = null;
        private JTextField projectTextField = null;
        private JTextField nameTextField = null;
        private transient ResourceBundle bundle;
        
        public Component getComponent () { return getPanel(); }
        
        /**
         * Returns the project location path.
         * @return <CODE>String</CODE> project location path
         */
        public String getProjectLocation() { return projectLocation; }
        
        private ConfigPanel getPanel() 
        {
            if (panel == null) {
                panel = new ConfigPanel(this);
            }
            return panel;
        }
        
        //implementation of the FinishablePanel Interface
        //provides the Finish Button to be always enabled 
        public boolean isFinishPanel() { return false;}

        public boolean isValid ()
        {
            if (WizardHelpers.fileExists(
                    getPanel().rmiAccessFileJTextField.getText())) {
                setErrorMsg( "The file " + // NOI18N
                        WizardHelpers.getFileName(
                            getPanel().rmiAccessFileJTextField.getText()) +
                         " already exists.");// NOI18N
                return false;
            } else if (WizardHelpers.fileExists(
                    getPanel().rmiPasswordFileJTextField.getText())) {
                setErrorMsg( "The file " + // NOI18N
                        WizardHelpers.getFileName(
                            getPanel().rmiPasswordFileJTextField.getText()) +
                         " already exists.");// NOI18N
                return false;
            } 
            setErrorMsg( "");// NOI18N
            return true;
        }
        
        /**
         * Fire a change event (designed to be used from out of this class).
         */
        public void fireEvent() {
            fireChangeEvent();
        }
        
        private String getProjectDisplayedName() {
            Project project = Templates.getProject(templateWiz);
            return ProjectUtils.getInformation(project).getDisplayName();
        }
        
        private void initTargetComponentDef(Component root) {
            if (root.getClass().equals(JTextField.class)) {
                JTextField rootTextField = (JTextField) root;
                if (rootTextField.isEditable()) {
                    if (nameTextField == null) {
                        nameTextField = ((JTextField) root);
                    }
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
         * Displays the given message in the wizard's message area.
         *
         * @param  message  message to be displayed, or <code>null</code>
         *                  if the message area should be cleared
         */
        private void setErrorMsg(String message) {
            if (templateWiz != null) {
                templateWiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
                                        message);
            }
        }
        
        /**
         * Add a changeListener on the targetWiz which each time that a change 
         * event happens, store and reload the optionsWiz.
         * @param targetWiz <CODE>Panel</CODE> 
         * @param optionsWiz <CODE>Panel</CODE> 
         * @param wizard <CODE>WizardDescriptor</CODE> a wizard
         */
        public void setListenerEnabled(
                final WizardDescriptor.Panel targetWiz,
                final WizardDescriptor.Panel optionsWiz,
                final TemplateWizard wizard) {
            bundle = NbBundle.getBundle(JMXConfigWizardIterator.class);
            templateWiz = wizard;
            this.targetWiz = targetWiz;
            initTargetComponentDef(targetWiz.getComponent());
            targetWiz.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent evt) {
                    optionsWiz.storeSettings(wizard);
                    optionsWiz.readSettings(wizard);
                }
            });
        }

        //=====================================================================
        // Called to read information from the wizard map in order to populate
        // the GUI correctly.
        //=====================================================================
        public void readSettings (Object settings) 
        {
            templateWiz = (TemplateWizard) settings;
            String filePath = (String) 
                templateWiz.getProperty(WizardConstants.PROP_CONFIG_FILE_PATH);
            String targetName = templateWiz.getTargetName();
            String rmiAccessFileName =  "";// NOI18N
            String rmiPasswordFileName =  "";// NOI18N
            if ((targetName != null) && (filePath != null)) {
                rmiAccessFileName = filePath + File.separator + targetName +
                         "." + WizardConstants.ACCESS_EXT;// NOI18N
                rmiPasswordFileName = filePath + File.separator + targetName + 
                         "." + WizardConstants.PASSWORD_EXT;// NOI18N
            }
            getPanel().rmiAccessFileJTextField.setText(rmiAccessFileName);
            getPanel().rmiPasswordFileJTextField.setText(rmiPasswordFileName);
            Boolean threadContention = (Boolean)
                    templateWiz.getProperty(WizardConstants.
                    THREAD_CONTENTION_MONITOR); 
            if(threadContention != null)
               getPanel().threadContentionJCheckBox.
                       setSelected(threadContention);
        }
        
        public void storeSettings(Object settings) {
            templateWiz = (TemplateWizard) settings;
            if (createdFileTextField != null) {
                String filePath = WizardHelpers.getFolderPath(
                        createdFileTextField.getText());
                templateWiz.putProperty(
                        WizardConstants.PROP_CONFIG_FILE_PATH,filePath);
            }
            if (nameTextField != null) {
                Templates.setTargetName(templateWiz,nameTextField.getText());
            }
            Boolean threadContention = 
                    getPanel().threadContentionJCheckBox.isSelected();
            templateWiz.putProperty(WizardConstants.THREAD_CONTENTION_MONITOR, 
                    threadContention);
        }
              
        public HelpCtx getHelp() {
           return new HelpCtx( "mgt_properties");  // NOI18N
        }   
        
    }

}

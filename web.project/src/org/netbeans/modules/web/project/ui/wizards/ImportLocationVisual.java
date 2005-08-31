/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui.wizards;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.ProjectUtils;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ui.support.ProjectChooser;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModuleContainer;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.web.project.ui.*;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author  Pavel Buzek, Radko Najman
 */
public class ImportLocationVisual extends SettingsPanel implements HelpCtx.Provider {
    
    private ImportWebProjectWizardIterator.ThePanel panel;
    private Document moduleDocument;
    private Document nameDocument;
    private boolean contextModified = false;
    private boolean locationModified = false;
    private boolean locationComputed = false;
    private WizardDescriptor wizardDescriptor;
    
    private J2eeVersionWarningPanel warningPanel;

    private String generatedProjectName = "";
    private int generatedProjectNameIndex = 0;

    private List serverInstanceIDs;
    private List earProjects;
    
    private static final String J2EE_SPEC_13_LABEL = NbBundle.getMessage(ImportLocationVisual.class, "J2EESpecLevel_13"); //NOI18N
    private static final String J2EE_SPEC_14_LABEL = NbBundle.getMessage(ImportLocationVisual.class, "J2EESpecLevel_14"); //NOI18N

    /** Creates new form TestPanel */
    public ImportLocationVisual (ImportWebProjectWizardIterator.ThePanel panel) {
        this.panel = panel;
        initComponents ();
        initServerInstances();
        initEnterpriseApplications();
        
        setJ2eeVersionWarningPanel();
        
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportLocationVisual.class, "ACS_NWP1_NamePanel_A11YDesc"));  // NOI18N

        setName(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_ImportTitle")); //NOI18N
        
        moduleDocument = moduleLocationTextField.getDocument ();
        nameDocument = projectNameTextField.getDocument();

        DocumentListener pl = new DocumentListener () {
            public void changedUpdate(DocumentEvent e) {
                dataChanged(e);
            }

            public void insertUpdate(DocumentEvent e) {
                dataChanged(e);
            }

            public void removeUpdate(DocumentEvent e) {
                dataChanged(e);
            }
        };
        moduleLocationTextField.getDocument().addDocumentListener(pl);

        projectNameTextField.getDocument().addDocumentListener (new DocumentListener (){
            public void changedUpdate(DocumentEvent e) {
                dataChanged(e);
            }

            public void insertUpdate(DocumentEvent e) {
                dataChanged(e);
            }

            public void removeUpdate(DocumentEvent e) {
                dataChanged(e);
            }
        });
        
        projectLocationTextField.getDocument().addDocumentListener (new DocumentListener (){
            public void changedUpdate(DocumentEvent e) {
                fireChanges();
            }

            public void insertUpdate(DocumentEvent e) {
                fireChanges();
            }

            public void removeUpdate(DocumentEvent e) {
                fireChanges();
            }
        });     

    }
    
    void read(WizardDescriptor settings) {
        wizardDescriptor = settings;
        
        File projectLocation = (File) settings.getProperty ("projdir");  //NOI18N
        if (projectLocation == null || projectLocation.getParentFile() == null || !projectLocation.getParentFile().isDirectory ())
            projectLocation = ProjectChooser.getProjectsFolder();
        else
            projectLocation = projectLocation.getParentFile();
        
        if(generatedProjectNameIndex == 0) {
            generatedProjectName = (String) settings.getProperty ("name"); //NOI18N
            if (generatedProjectName == null) {
                    generatedProjectNameIndex = FoldersListSettings.getDefault().getNewProjectCount() + 1;
                    String formater = NbBundle.getMessage(PanelSourceFolders.class,"LBL_NPW1_DefaultProjectName");
                    while ((generatedProjectName = validFreeProjectName(projectLocation, formater, generatedProjectNameIndex)) == null)
                        generatedProjectNameIndex++;                
                    settings.putProperty (NewWebProjectWizardIterator.PROP_NAME_INDEX, new Integer(generatedProjectNameIndex));
            }
            projectNameTextField.setText(generatedProjectName);
            moduleLocationTextField.selectAll();
        }
    }

    void store (WizardDescriptor settings) {
        File srcRoot = null;
        String srcPath = moduleLocationTextField.getText();
        if (srcPath.length() > 0) {
            srcRoot = FileUtil.normalizeFile(new File(srcPath));
        }
        settings.putProperty (WizardProperties.SOURCE_ROOT, srcRoot);
        settings.putProperty (WizardProperties.NAME, projectNameTextField.getText().trim());

        final String projectLocation = projectLocationTextField.getText().trim();
        if (projectLocation.length() >= 0) {
            settings.putProperty (WizardProperties.PROJECT_DIR, new File(projectLocation));
        }

        String contextPath = jTextFieldContextPath.getText().trim();
        if (!contextPath.startsWith("/")) //NOI18N
            contextPath = "/" + contextPath; //NOI18N
        settings.putProperty(WizardProperties.CONTEXT_PATH, contextPath);
        final Integer nameIndex = projectNameTextField.getText().equals(generatedProjectName) ?
                new Integer(generatedProjectNameIndex) : null;
        settings.putProperty(NewWebProjectWizardIterator.PROP_NAME_INDEX, nameIndex);
        
        settings.putProperty(WizardProperties.SET_AS_MAIN, setAsMainCheckBox.isSelected() ? Boolean.TRUE : Boolean.FALSE );
        settings.putProperty(WizardProperties.SERVER_INSTANCE_ID, getSelectedServer());
        settings.putProperty(WizardProperties.J2EE_LEVEL, getSelectedJ2eeSpec());
        settings.putProperty(WizardProperties.EAR_APPLICATION, getSelectedEarApplication());
        
        if (warningPanel != null && warningPanel.getDowngradeAllowed()) {
            settings.putProperty(WizardProperties.JAVA_PLATFORM, warningPanel.getJava14PlatformName());
            settings.putProperty(WizardProperties.SOURCE_LEVEL, "1.4"); // NOI18N
        }
    }

    boolean valid (WizardDescriptor settings) {
        String sourceLocationPath = moduleLocationTextField.getText().trim();
        if (sourceLocationPath.length() == 0) {
            setErrorMessage("MSG_ProvideExistingSourcesLocation"); //NOI18N
            return false;
        }
        File f = new File (sourceLocationPath);
        if (!f.isDirectory() || !f.canRead()) {
            setErrorMessage("MSG_IllegalSources"); //NOI18N
            return false;
        }

        String projectLocationPath = projectLocationTextField.getText().trim();
        f = new File(projectLocationPath);
        String projectName = projectNameTextField.getText().trim();
        f = new File(f, projectName);
        f = PanelProjectLocationVisual.getCanonicalFile(f);
        if(f == null || !projectName.equals(f.getName())) {
            settings.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(ImportLocationVisual.class, "MSG_ProvideProjectName"));
            return false; // Invalid project name
        }

        if(projectLocationPath.length() == 0) {
            setErrorMessage("MSG_ProvideProjectFolder"); //NOI18N
            return false;
        }
        File projectLocation;
        if (locationComputed)
            projectLocation = ProjectChooser.getProjectsFolder();
        else
            projectLocation = new File(projectLocationPath);
        if (projectLocation.exists() && !projectLocation.canWrite()) {
            // Read only project location
            setErrorMessage("MSG_ProjectLocationRO"); //NOI18N
            return false;
        }

        File destFolder = new File(projectLocationPath);
        File[] kids = destFolder.listFiles();
        if ( destFolder.exists() && kids != null && kids.length > 0) {
            String file = null;
            for (int i=0; i< kids.length; i++) {
                String childName = kids[i].getName();
                if ("nbproject".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage (ImportLocationVisual.class,"TXT_NetBeansProject");
                }
                else if ("build".equals(childName)) {    //NOI18N
                    file = NbBundle.getMessage (ImportLocationVisual.class,"TXT_BuildFolder");
                }
                else if ("WEB-INF".equals(childName)) {    //NOI18N
                    file = NbBundle.getMessage (ImportLocationVisual.class,"TXT_WebInfFolder");
                }
                else if ("dist".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage (ImportLocationVisual.class,"TXT_DistFolder");
                }
//                else if ("build.xml".equals(childName)) {   //NOI18N
//                    file = NbBundle.getMessage (PanelSourceFolders.class,"TXT_BuildXML");
//                }
                else if ("manifest.mf".equals(childName)) { //NOI18N
                    file = NbBundle.getMessage (ImportLocationVisual.class,"TXT_Manifest");
                }
                if (file != null) {
                    String format = NbBundle.getMessage (ImportLocationVisual.class,"MSG_ProjectFolderInvalid");
                    wizardDescriptor.putProperty( "WizardPanel_errorMessage", MessageFormat.format(format, new Object[] {file}));  //NOI18N
                    return false;
                }
            }
        }

        setErrorMessage(null);
        return true;
    }

    private void setErrorMessage(String messageId) {
        wizardDescriptor.putProperty( "WizardPanel_errorMessage",
                messageId == null ? null : NbBundle.getMessage(ImportLocationVisual.class, messageId));
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelSrcLocationDesc = new javax.swing.JLabel();
        jLabelSrcLocation = new javax.swing.JLabel();
        moduleLocationTextField = new javax.swing.JTextField();
        jButtonSrcLocation = new javax.swing.JButton();
        jLabelPrjLocationDesc = new javax.swing.JLabel();
        jLabelPrjName = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        jLabelPrjLocation = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        jButtonPrjLocation = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanelOptions = new javax.swing.JPanel();
        jLabelEnterprise = new javax.swing.JLabel();
        jComboBoxEnterprise = new javax.swing.JComboBox();
        setAsMainCheckBox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        serverInstanceLabel = new javax.swing.JLabel();
        serverInstanceComboBox = new javax.swing.JComboBox();
        j2eeSpecLabel = new javax.swing.JLabel();
        j2eeSpecComboBox = new javax.swing.JComboBox();
        jLabelContextPath = new javax.swing.JLabel();
        jTextFieldContextPath = new javax.swing.JTextField();
        warningPlaceHolderPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabelSrcLocationDesc, NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationSrcDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jLabelSrcLocationDesc, gridBagConstraints);

        jLabelSrcLocation.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_ImportLocation_LabelMnemonic").charAt(0));
        jLabelSrcLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelSrcLocation.setLabelFor(moduleLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelSrcLocation, NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationSrc_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabelSrcLocation, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(moduleLocationTextField, gridBagConstraints);
        moduleLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_IW_ImportLocation_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonSrcLocation, NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_BrowseLocation_Button"));
        jButtonSrcLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSrcLocationActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonSrcLocation, gridBagConstraints);
        jButtonSrcLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_IW_ImportLocationBrowse_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabelPrjLocationDesc, NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationPrjDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jLabelPrjLocationDesc, gridBagConstraints);

        jLabelPrjName.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ProjectName_LabelMnemonic").charAt(0));
        jLabelPrjName.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelPrjName.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelPrjName, NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ProjectName_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 11);
        add(jLabelPrjName, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 11);
        add(projectNameTextField, gridBagConstraints);
        projectNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NWP1_ProjectName_A11YDesc"));

        jLabelPrjLocation.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ProjectLocation_LabelMnemonic").charAt(0));
        jLabelPrjLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelPrjLocation.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelPrjLocation, NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_CreatedProjectFolder_Lablel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabelPrjLocation, gridBagConstraints);

        projectLocationTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                projectLocationTextFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(projectLocationTextField, gridBagConstraints);
        projectLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NPW1_ProjectLocation_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonPrjLocation, NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_BrowseLocation_Button"));
        jButtonPrjLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrjLocationActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonPrjLocation, gridBagConstraints);
        jButtonPrjLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NWP1_BrowseLocation_A11YDesc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jSeparator1, gridBagConstraints);

        jPanelOptions.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabelEnterprise, NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_AddToEnterprise_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        jPanelOptions.add(jLabelEnterprise, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        jPanelOptions.add(jComboBoxEnterprise, gridBagConstraints);
        jComboBoxEnterprise.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NWP1_AddToEnterpriseComboBox_A11YDesc"));

        setAsMainCheckBox.setMnemonic(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_SetAsMain_CheckBoxMnemonic").charAt(0));
        setAsMainCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(setAsMainCheckBox, NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_SetAsMain_CheckBox"));
        setAsMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 11, 0);
        jPanelOptions.add(setAsMainCheckBox, gridBagConstraints);
        setAsMainCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NWP1_SetAsMain_A11YDesc"));

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(serverInstanceLabel, NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_Server"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 11);
        jPanel1.add(serverInstanceLabel, gridBagConstraints);

        serverInstanceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverInstanceComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(serverInstanceComboBox, gridBagConstraints);
        serverInstanceComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportLocationVisual.class, "ACS_NWP1_Server_ComboBox_A11YDesc"));

        j2eeSpecLabel.setDisplayedMnemonic(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_J2EESpecLevel_CheckBoxMnemonic").charAt(0));
        org.openide.awt.Mnemonics.setLocalizedText(j2eeSpecLabel, NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_J2EESpecLevel_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 11);
        jPanel1.add(j2eeSpecLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(j2eeSpecComboBox, gridBagConstraints);
        j2eeSpecComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NPW1_J2EESpecLevel_A11YDesc"));

        jLabelContextPath.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ContextPath_CheckBoxMnemonic").charAt(0));
        jLabelContextPath.setLabelFor(jTextFieldContextPath);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelContextPath, NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ContextPath_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        jPanel1.add(jLabelContextPath, gridBagConstraints);

        jTextFieldContextPath.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldContextPathKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        jPanel1.add(jTextFieldContextPath, gridBagConstraints);
        jTextFieldContextPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NWP1_ContextPath_A11YDesc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelOptions.add(jPanel1, gridBagConstraints);

        warningPlaceHolderPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelOptions.add(warningPlaceHolderPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanelOptions, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void serverInstanceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverInstanceComboBoxActionPerformed
        String prevSelectedItem = (String)j2eeSpecComboBox.getSelectedItem();
        String servInsID = (String)serverInstanceIDs.get(serverInstanceComboBox.getSelectedIndex());
        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(servInsID);
        Set supportedVersions = j2eePlatform.getSupportedSpecVersions();
        j2eeSpecComboBox.removeAllItems();
        if (supportedVersions.contains(J2eeModule.J2EE_14)) j2eeSpecComboBox.addItem(J2EE_SPEC_14_LABEL);
        if (supportedVersions.contains(J2eeModule.J2EE_13)) j2eeSpecComboBox.addItem(J2EE_SPEC_13_LABEL);
        if (prevSelectedItem != null)
            j2eeSpecComboBox.setSelectedItem(prevSelectedItem);
    }//GEN-LAST:event_serverInstanceComboBoxActionPerformed

    private void projectLocationTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_projectLocationTextFieldKeyReleased
        locationModified = true;
    }//GEN-LAST:event_projectLocationTextFieldKeyReleased

    private void jTextFieldContextPathKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldContextPathKeyReleased
        contextModified = true;
    }//GEN-LAST:event_jTextFieldContextPathKeyReleased

    private void jButtonPrjLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrjLocationActionPerformed
        JFileChooser chooser = org.netbeans.modules.web.project.ui.FileChooser.createDirectoryChooser(
                "ImportLocationVisual.Project", projectLocationTextField.getText()); //NOI18N
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = chooser.getSelectedFile();
            projectLocationTextField.setText( projectDir.getAbsolutePath());
        }            
    }//GEN-LAST:event_jButtonPrjLocationActionPerformed

    private void jButtonSrcLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSrcLocationActionPerformed
        JFileChooser chooser = FileChooser.createDirectoryChooser(
                "ImportLocationVisual.Sources", moduleLocationTextField.getText()); //NOI18N
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = chooser.getSelectedFile();
            moduleLocationTextField.setText( projectDir.getAbsolutePath());
        }            
    }//GEN-LAST:event_jButtonSrcLocationActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox j2eeSpecComboBox;
    private javax.swing.JLabel j2eeSpecLabel;
    private javax.swing.JButton jButtonPrjLocation;
    private javax.swing.JButton jButtonSrcLocation;
    private javax.swing.JComboBox jComboBoxEnterprise;
    private javax.swing.JLabel jLabelContextPath;
    private javax.swing.JLabel jLabelEnterprise;
    private javax.swing.JLabel jLabelPrjLocation;
    private javax.swing.JLabel jLabelPrjLocationDesc;
    private javax.swing.JLabel jLabelPrjName;
    private javax.swing.JLabel jLabelSrcLocation;
    private javax.swing.JLabel jLabelSrcLocationDesc;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelOptions;
    private javax.swing.JSeparator jSeparator1;
    protected javax.swing.JTextField jTextFieldContextPath;
    public javax.swing.JTextField moduleLocationTextField;
    public javax.swing.JTextField projectLocationTextField;
    public javax.swing.JTextField projectNameTextField;
    private javax.swing.JComboBox serverInstanceComboBox;
    private javax.swing.JLabel serverInstanceLabel;
    private javax.swing.JCheckBox setAsMainCheckBox;
    private javax.swing.JPanel warningPlaceHolderPanel;
    // End of variables declaration//GEN-END:variables

    /** Handles changes in the project name and project directory
     */
    private void dataChanged(DocumentEvent e) {
        try {
            if (e.getDocument() == moduleDocument) {
                String moduleFolder = moduleLocationTextField.getText().trim();
                FileObject fo;
                try {
                    fo = FileUtil.toFileObject(new File(moduleFolder));
                } catch (IllegalArgumentException exc) {
                    return;
                }

                if (fo != null && !locationComputed)
                    if (!FileSearchUtility.containsWebInf(fo) && !locationModified)
                        projectLocationTextField.setText(moduleFolder);
                    else
                        computeLocation();
            } else if (e.getDocument() == nameDocument) {
                if (!contextModified)
                    jTextFieldContextPath.setText("/" + projectNameTextField.getText().replace(' ', '_'));
                if (locationComputed)
                    computeLocation();
            }
        } finally {
            // all changes should be processed to update possible error messages
            fireChanges();
        }
    }
    
    private void fireChanges() {
        panel.fireChangeEvent();
    }
    
    private void computeLocation() {
        if (locationModified) //modified by the user, don't compute the location
            return;
        
        File projectLocation = ProjectChooser.getProjectsFolder();
        StringBuffer folder = new StringBuffer(projectLocation.getAbsolutePath());
        if (!folder.toString().endsWith(File.separator))
            folder.append(File.separatorChar);
        folder.append(projectNameTextField.getText().trim());
        projectLocationTextField.setText(folder.toString());
        locationComputed = true;
    }
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ImportLocationVisual.class);
    }

    private void initServerInstances() {
        String[] servInstIDs = Deployment.getDefault().getServerInstanceIDs();
        serverInstanceIDs = new ArrayList();
        for (int i = 0; i < servInstIDs.length; i++) {
            J2eePlatform j2eePlat = Deployment.getDefault().getJ2eePlatform(servInstIDs[i]);
            if (j2eePlat != null && j2eePlat.getSupportedModuleTypes().contains(J2eeModule.WAR)) {
                serverInstanceIDs.add(servInstIDs[i]);
                serverInstanceComboBox.addItem(Deployment.getDefault().getServerInstanceDisplayName(servInstIDs[i]));
            }
        }
        if (serverInstanceIDs.size() > 0) {
            serverInstanceComboBox.setSelectedIndex(0);
        } else {
            serverInstanceComboBox.setEnabled(false);
            j2eeSpecComboBox.setEnabled(false);
        }
    }

    private Project getSelectedEarApplication() {
        int idx = jComboBoxEnterprise.getSelectedIndex();
        return (idx <= 0) ? null : (Project) earProjects.get(idx - 1);
    }
    
    private void initEnterpriseApplications() {
        jComboBoxEnterprise.addItem(NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_AddToEnterprise_None"));
        jComboBoxEnterprise.setSelectedIndex(0);
        
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        earProjects = new ArrayList();
        for (int i = 0; i < allProjects.length; i++) {
            J2eeModuleContainer container = (J2eeModuleContainer) allProjects[i].getLookup().lookup(J2eeModuleContainer.class);
            ProjectInformation projectInfo = ProjectUtils.getInformation(allProjects[i]);
            if (container != null) {
                earProjects.add(projectInfo.getProject());
                jComboBoxEnterprise.addItem(projectInfo.getDisplayName());
            }
        }
        if (earProjects.size() <= 0) {
            jComboBoxEnterprise.setEnabled(false);
        }
    }

    private String getSelectedJ2eeSpec() {
        Object item = j2eeSpecComboBox.getSelectedItem();
        return item == null ? null : item.equals(J2EE_SPEC_14_LABEL) ? J2eeModule.J2EE_14 : J2eeModule.J2EE_13;
    }
    
    private String getSelectedServer() {
        int idx = serverInstanceComboBox.getSelectedIndex();
        return idx == -1 ? null : (String)serverInstanceIDs.get(idx);
    }

    private String validFreeProjectName (final File parentFolder, final String formater, final int index) {
        String name = MessageFormat.format (formater, new Object[]{new Integer (index)});                
        File file = new File (parentFolder, name);
        return file.exists() ? null : name;
    }
    
    private void setJ2eeVersionWarningPanel() {
        String warningType = J2eeVersionWarningPanel.findWarningType();
        if (warningType == null)
            return;
        
        warningPanel = new J2eeVersionWarningPanel(warningType);
        warningPlaceHolderPanel.add(warningPanel, java.awt.BorderLayout.CENTER);
    }
}

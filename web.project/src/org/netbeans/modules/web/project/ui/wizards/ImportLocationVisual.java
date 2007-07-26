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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui.wizards;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerManager;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.web.project.Utils;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ui.support.ProjectChooser;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.web.project.ui.*;
import org.netbeans.spi.project.ui.templates.support.Templates;

/**
 *
 * @author  Pavel Buzek, Radko Najman
 */
public class ImportLocationVisual extends SettingsPanel implements HelpCtx.Provider {
    
    private final ImportWebProjectWizardIterator.ThePanel panel;
    private final Document moduleDocument;
    private final Document nameDocument;
    private boolean contextModified = false;
    private boolean locationModified = false;
    private WizardDescriptor wizardDescriptor;
    
    private J2eeVersionWarningPanel warningPanel;

    private String generatedProjectName = "";
    private int generatedProjectNameIndex = 0;
    private final DefaultComboBoxModel serversModel = new DefaultComboBoxModel();
    private List earProjects;
    
    private static final String J2EE_SPEC_13_LABEL = NbBundle.getMessage(ImportLocationVisual.class, "J2EESpecLevel_13"); //NOI18N
    private static final String J2EE_SPEC_14_LABEL = NbBundle.getMessage(ImportLocationVisual.class, "J2EESpecLevel_14"); //NOI18N
    private static final String JAVA_EE_5_LABEL = NbBundle.getMessage(ImportLocationVisual.class, "JavaEESpecLevel_50"); //NOI18N
    
    /** Creates new form TestPanel */
    public ImportLocationVisual (ImportWebProjectWizardIterator.ThePanel panel) {
        this.panel = panel;
        initComponents ();
        initServers(FoldersListSettings.getDefault().getLastUsedServer());
        // preselect the first item in the j2ee spec combo
        if (j2eeSpecComboBox.getModel().getSize() > 0) {
            j2eeSpecComboBox.setSelectedIndex(0);
        }
        initEnterpriseApplications();
        
        setJ2eeVersionWarningPanel();
        
        //resize panel to show all components
        computeSize();
        
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportLocationVisual.class, "ACS_NWP1_NamePanel_A11YDesc"));  // NOI18N

        setName(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_ImportTitle")); //NOI18N
        
        moduleDocument = moduleLocationTextField.getDocument ();
        nameDocument = projectNameTextField.getDocument();
        
        moduleLocationTextField.getDocument().addDocumentListener(new DocumentListener () {
            public void changedUpdate(DocumentEvent e) {
                locationDataChanged(e);
            }
            public void insertUpdate(DocumentEvent e) {
                locationDataChanged(e);
            }
            public void removeUpdate(DocumentEvent e) {
                locationDataChanged(e);
            }
        });
        
        projectNameTextField.getDocument().addDocumentListener (new DocumentListener () {
            public void changedUpdate(DocumentEvent e) {
                nameDataChanged(e);
            }
            public void insertUpdate(DocumentEvent e) {
                nameDataChanged(e);
            }
            public void removeUpdate(DocumentEvent e) {
                nameDataChanged(e);
            }
        });
        
        projectLocationTextField.getDocument().addDocumentListener (new DocumentListener () {
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
    
    private void computeSize() {
        double srcLocLabelLength = jLabelSrcLocationDesc.getFontMetrics(jLabelSrcLocationDesc.getFont()).getStringBounds(jLabelSrcLocationDesc.getText(), getGraphics()).getWidth() * 0.85;
        int width = new Double(srcLocLabelLength).intValue();
        if (width < 500)
            width = 500;
        int height = jComboBoxEnterprise.getFontMetrics(jComboBoxEnterprise.getFont()).getHeight() * 13 + 160;
        if (height > 340) {
            Dimension dim = new Dimension(width, height);
            setMinimumSize(dim);
            setPreferredSize(dim);
        }
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
                    String formater = NbBundle.getMessage(ImportLocationVisual.class,"LBL_NPW1_DefaultProjectName");
                    while ((generatedProjectName = validFreeProjectName(projectLocation, formater, generatedProjectNameIndex)) == null)
                        generatedProjectNameIndex++;                
                    settings.putProperty (NewWebProjectWizardIterator.PROP_NAME_INDEX, Integer.valueOf(generatedProjectNameIndex));
            }
            // no project name needs to be generated
            //projectNameTextField.setText(generatedProjectName);
            moduleLocationTextField.selectAll();
        }
    }

    void store (WizardDescriptor settings) {
        File srcRoot = null;
        String srcPath = moduleLocationTextField.getText();
        if (srcPath.length() > 0) {
            srcRoot = FileUtil.normalizeFile(new File(srcPath));
        }
        if (srcRoot != null)
            FoldersListSettings.getDefault().setLastUsedImportLocation(srcRoot);
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
                Integer.valueOf(generatedProjectNameIndex) : null;
        settings.putProperty(NewWebProjectWizardIterator.PROP_NAME_INDEX, nameIndex);
        
        settings.putProperty(WizardProperties.SET_AS_MAIN, setAsMainCheckBox.isSelected() ? Boolean.TRUE : Boolean.FALSE );
        settings.putProperty(WizardProperties.SERVER_INSTANCE_ID, getSelectedServer());
        settings.putProperty(WizardProperties.J2EE_LEVEL, getSelectedJ2eeSpec());
        settings.putProperty(WizardProperties.EAR_APPLICATION, getSelectedEarApplication());
        
        if (warningPanel != null && warningPanel.getDowngradeAllowed()) {
            settings.putProperty(WizardProperties.JAVA_PLATFORM, warningPanel.getSuggestedJavaPlatformName());
            
            String j2ee = getSelectedJ2eeSpec();
            if (j2ee != null) {
                String warningType = J2eeVersionWarningPanel.findWarningType(j2ee);
                FoldersListSettings fls = FoldersListSettings.getDefault();
                String srcLevel = "1.6"; //NOI18N
                if (warningType.equals(J2eeVersionWarningPanel.WARN_SET_SOURCE_LEVEL_14) && fls.isAgreedSetSourceLevel14())
                    srcLevel = "1.4"; //NOI18N
                else if (warningType.equals(J2eeVersionWarningPanel.WARN_SET_SOURCE_LEVEL_15) && fls.isAgreedSetSourceLevel15())
                    srcLevel = "1.5"; //NOI18N
                
                settings.putProperty(WizardProperties.SOURCE_LEVEL, srcLevel);
            }            
        } else
            settings.putProperty(WizardProperties.SOURCE_LEVEL, null);
    }

    boolean valid (WizardDescriptor settings) {
       String sourceLocationPath = moduleLocationTextField.getText().trim();
        if (sourceLocationPath.length() == 0) {
            setErrorMessage("MSG_ProvideExistingSourcesLocation"); //NOI18N
            return false;
        }
        File f = new File (sourceLocationPath);
        if (!f.isDirectory() || !f.canRead()) {
	    String format = NbBundle.getMessage(ImportLocationVisual.class, "MSG_IllegalSources"); //NOI18N
	    wizardDescriptor.putProperty( "WizardPanel_errorMessage", MessageFormat.format(format, new Object[] {sourceLocationPath})); //NOI18N
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
        File projectLocation = new File(projectLocationPath);
        if (projectLocation.exists() && !projectLocation.canWrite()) {
            // Read only project location
            setErrorMessage("MSG_ProjectLocationRO"); //NOI18N
            return false;
        }

        File destFolder = FileUtil.normalizeFile(new File(projectLocationPath));
	
	// #47611: if there is a live project still residing here, forbid project creation.
        if (destFolder.isDirectory()) {
            FileObject destFO = FileUtil.toFileObject(destFolder);
            assert destFO != null : "No FileObject for " + destFolder;
            boolean clear = false;
            try {
                clear = ProjectManager.getDefault().findProject(destFO) == null;
            } catch (IOException e) {
                // need not report here; clear remains false -> error
            }
            if (!clear) {
		setErrorMessage("MSG_ProjectFolderHasDeletedProject"); //NOI18N
		return false;
            }
        }

	
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
//                else if ("WEB-INF".equals(childName)) {    //NOI18N
//                    file = NbBundle.getMessage (ImportLocationVisual.class,"TXT_WebInfFolder");
//                }
                else if ("dist".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage (ImportLocationVisual.class,"TXT_DistFolder");
                }
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
        if (getSelectedServer() == null) {
            String errMsg = NbBundle.getMessage(PanelOptionsVisual.class, "MSG_NoServer");
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", errMsg); // NOI18N
            return false;
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
        addServerButton = new javax.swing.JButton();
        warningPlaceHolderPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabelSrcLocationDesc, NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationSrcDesc")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jLabelSrcLocationDesc, gridBagConstraints);

        jLabelSrcLocation.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_ImportLocation_LabelMnemonic").charAt(0));
        jLabelSrcLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelSrcLocation.setLabelFor(moduleLocationTextField);
        jLabelSrcLocation.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationSrc_Label")); // NOI18N
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
        moduleLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_IW_ImportLocation_A11YDesc")); // NOI18N

        jButtonSrcLocation.setMnemonic(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_BrowseLocation_MNE").charAt(0));
        jButtonSrcLocation.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_BrowseLocation_Button")); // NOI18N
        jButtonSrcLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSrcLocationActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonSrcLocation, gridBagConstraints);
        jButtonSrcLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_IW_ImportLocationBrowse_A11YDesc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelPrjLocationDesc, NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationPrjDesc")); // NOI18N
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
        jLabelPrjName.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ProjectName_Label")); // NOI18N
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
        projectNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NWP1_ProjectName_A11YDesc")); // NOI18N

        jLabelPrjLocation.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ProjectLocation_LabelMnemonic").charAt(0));
        jLabelPrjLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelPrjLocation.setLabelFor(projectLocationTextField);
        jLabelPrjLocation.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_CreatedProjectFolder_Label")); // NOI18N
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
        projectLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NPW1_ProjectLocation_A11YDesc")); // NOI18N

        jButtonPrjLocation.setMnemonic(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_BrowseProjectFolder_MNE").charAt(0));
        jButtonPrjLocation.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_BrowseProjectLocation_Button")); // NOI18N
        jButtonPrjLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrjLocationActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonPrjLocation, gridBagConstraints);
        jButtonPrjLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NWP1_BrowseLocation_A11YDesc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jSeparator1, gridBagConstraints);

        jPanelOptions.setLayout(new java.awt.GridBagLayout());

        jLabelEnterprise.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_AddToEnterprise_LabelMnemonic").charAt(0));
        jLabelEnterprise.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_AddToEnterprise_Label")); // NOI18N
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
        jComboBoxEnterprise.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NWP1_AddToEnterpriseComboBox_A11YDesc")); // NOI18N

        setAsMainCheckBox.setMnemonic(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_SetAsMain_CheckBoxMnemonic").charAt(0));
        setAsMainCheckBox.setSelected(true);
        setAsMainCheckBox.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_SetAsMain_CheckBox")); // NOI18N
        setAsMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanelOptions.add(setAsMainCheckBox, gridBagConstraints);
        setAsMainCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NWP1_SetAsMain_A11YDesc")); // NOI18N

        jPanel1.setLayout(new java.awt.GridBagLayout());

        serverInstanceLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_Server_LabelMnemonic").charAt(0));
        serverInstanceLabel.setLabelFor(serverInstanceComboBox);
        serverInstanceLabel.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_Server")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 11);
        jPanel1.add(serverInstanceLabel, gridBagConstraints);

        serverInstanceComboBox.setModel(serversModel);
        serverInstanceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverInstanceComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(serverInstanceComboBox, gridBagConstraints);
        serverInstanceComboBox.getAccessibleContext().setAccessibleName("Server");
        serverInstanceComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportLocationVisual.class, "ACS_NWP1_Server_ComboBox_A11YDesc")); // NOI18N

        j2eeSpecLabel.setDisplayedMnemonic(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_J2EESpecLevel_CheckBoxMnemonic").charAt(0));
        j2eeSpecLabel.setLabelFor(j2eeSpecComboBox);
        j2eeSpecLabel.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_J2EESpecLevel_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 11);
        jPanel1.add(j2eeSpecLabel, gridBagConstraints);

        j2eeSpecComboBox.setPrototypeDisplayValue("MMMMMMMMM" /* "Java EE 5" */);
        j2eeSpecComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j2eeSpecComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(j2eeSpecComboBox, gridBagConstraints);
        j2eeSpecComboBox.getAccessibleContext().setAccessibleName("J2EE Version");
        j2eeSpecComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NPW1_J2EESpecLevel_A11YDesc")); // NOI18N

        jLabelContextPath.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ContextPath_CheckBoxMnemonic").charAt(0));
        jLabelContextPath.setLabelFor(jTextFieldContextPath);
        jLabelContextPath.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ContextPath_Label")); // NOI18N
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
        jTextFieldContextPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NWP1_ContextPath_A11YDesc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addServerButton, org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_AddServer")); // NOI18N
        addServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServerButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        jPanel1.add(addServerButton, gridBagConstraints);
        addServerButton.getAccessibleContext().setAccessibleName(null);
        addServerButton.getAccessibleContext().setAccessibleDescription(null);

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
    }// </editor-fold>//GEN-END:initComponents

    private void j2eeSpecComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j2eeSpecComboBoxActionPerformed
        setJ2eeVersionWarningPanel();
    }//GEN-LAST:event_j2eeSpecComboBoxActionPerformed

    private void addServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addServerButtonActionPerformed
        ServerInstanceWrapper serverInstanceWrapper = (ServerInstanceWrapper) serversModel.getSelectedItem();
        String selectedServerInstanceID = null;
        if (serverInstanceWrapper != null) {
            selectedServerInstanceID = serverInstanceWrapper.getServerInstanceID();
        }
        String lastSelectedJ2eeSpecLevel = (String) j2eeSpecComboBox.getSelectedItem();
        String newServerInstanceID = ServerManager.showAddServerInstanceWizard();
        if (newServerInstanceID != null) {
            selectedServerInstanceID = newServerInstanceID;
            // clear the spec level selection
            lastSelectedJ2eeSpecLevel = null;
            j2eeSpecComboBox.setSelectedItem(null);
        }
        // refresh the list of servers
        initServers(selectedServerInstanceID);
        if (lastSelectedJ2eeSpecLevel != null) {
            j2eeSpecComboBox.setSelectedItem(lastSelectedJ2eeSpecLevel);
        }
}//GEN-LAST:event_addServerButtonActionPerformed

    private void serverInstanceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverInstanceComboBoxActionPerformed
        String prevSelectedItem = (String) j2eeSpecComboBox.getSelectedItem();
        // update the j2ee spec list according to the selected server
        ServerInstanceWrapper serverInstanceWrapper = (ServerInstanceWrapper) serversModel.getSelectedItem();
        if (serverInstanceWrapper != null) {
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceWrapper.getServerInstanceID());
            Set supportedVersions = j2eePlatform.getSupportedSpecVersions(J2eeModule.WAR);
            j2eeSpecComboBox.removeAllItems();
            if (supportedVersions.contains(J2eeModule.JAVA_EE_5)) {
                j2eeSpecComboBox.addItem(JAVA_EE_5_LABEL);
            }
            if (supportedVersions.contains(J2eeModule.J2EE_14)) {
                j2eeSpecComboBox.addItem(J2EE_SPEC_14_LABEL);
            }
            if (supportedVersions.contains(J2eeModule.J2EE_13)) {
                j2eeSpecComboBox.addItem(J2EE_SPEC_13_LABEL);
            }
            if (prevSelectedItem != null) {
                j2eeSpecComboBox.setSelectedItem(prevSelectedItem);
            }
        } else {
            j2eeSpecComboBox.removeAllItems();
        }
        // revalidate the form
        panel.fireChangeEvent();
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
        chooser.setDialogTitle(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_BrowseProjectFolder"));
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = chooser.getSelectedFile();
            projectLocationTextField.setText( projectDir.getAbsolutePath());
        }            
    }//GEN-LAST:event_jButtonPrjLocationActionPerformed

    private void jButtonSrcLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSrcLocationActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_BrowseExistingSource"));
        
        if (moduleLocationTextField.getText().length() > 0 && getProjectLocation().exists()) {
            chooser.setSelectedFile(getProjectLocation());
        } else {
            // honor the contract in issue 58987
            File currentDirectory = null;
            FileObject existingSourcesFO = Templates.getExistingSourcesFolder(wizardDescriptor);
            if (existingSourcesFO != null) {
                File existingSourcesFile = FileUtil.toFile(existingSourcesFO);
                if (existingSourcesFile != null && existingSourcesFile.isDirectory()) {
                    currentDirectory = existingSourcesFile;
                }
            }
            if (currentDirectory != null) {
                chooser.setCurrentDirectory(currentDirectory);
            } else {
                File lastUsedImportLoc = (File) FoldersListSettings.getDefault().getLastUsedImportLocation();
                if (lastUsedImportLoc != null)
                    chooser.setCurrentDirectory(lastUsedImportLoc.getParentFile());
                else                    
                    chooser.setSelectedFile(ProjectChooser.getProjectsFolder());
            }
        }
        
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = FileUtil.normalizeFile(chooser.getSelectedFile());
            moduleLocationTextField.setText(projectDir.getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonSrcLocationActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addServerButton;
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

    private String lastComputedPrjName = null;
    private String computeProjectName() {
        String cPrjName = null;
        FileObject fo = FileUtil.toFileObject(getProjectLocation());
        if (fo != null) {
            cPrjName = fo.getName();
        }
        return cPrjName;
    }
    
    private String lastComputedPrjFolder = null;
    private String computeProjectFolder() {
        return getProjectLocation().getAbsolutePath();
    }
    
    private String lastComputedContextPath = null;
    private String computeContextPath() {
        return Utils.createDefaultContext(projectNameTextField.getText());
    }
    
    boolean ignoreLocEvent = false;
    // handles changes in Location
    private void locationDataChanged(DocumentEvent de) {
        if (!ignoreLocEvent) {
            ignoreLocEvent = true;
            if (de.getDocument() == moduleDocument) {
                updateProjectName();
                updateProjectFolder();
            }
            ignoreLocEvent = false;
        }
        fireChanges();
    }
    
    boolean ignoreNameEvent = false;
    // handles changes in Project Name
    private void nameDataChanged(DocumentEvent de) {
        if (!ignoreNameEvent) {
            ignoreNameEvent = true;
            if (de.getDocument() == nameDocument) {
                updateProjectFolder();
                updateContextPath();
            }
            ignoreNameEvent = false;
        }
        fireChanges();
    }
    
    private void updateProjectName() {
        String prjName = computeProjectName();
        if ((lastComputedPrjName != null) && (!lastComputedPrjName.equals(projectNameTextField.getText().trim()))) {
            return;
        }
        lastComputedPrjName = prjName;
        if (prjName != null) {
            projectNameTextField.setText(prjName);
        }
    }
    
    private void updateProjectFolder() {
        String prjFolder = computeProjectFolder();
        if ((lastComputedPrjFolder != null) && (!lastComputedPrjFolder.equals(projectLocationTextField.getText().trim()))) {
            return;
        }
        lastComputedPrjFolder = prjFolder;
        if (prjFolder != null) {
            projectLocationTextField.setText(prjFolder);
        } else {
            projectLocationTextField.setText(""); // NOI18N
        }
    }
    
    private void updateContextPath() {
        String ctxPath = computeContextPath();
        if ((lastComputedContextPath != null) && (!lastComputedContextPath.equals(jTextFieldContextPath.getText().trim()))) {
            return;
        }
        lastComputedContextPath = ctxPath;
        if (ctxPath != null) {
            jTextFieldContextPath.setText(ctxPath);
        }
    }
    
    private void fireChanges() {
        panel.fireChangeEvent();
    }
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ImportLocationVisual.class);
    }
    
    /**
     * Init servers model
     * @param selectedServerInstanceID preselected instance or null if non is preselected
     */
    private void initServers(String selectedServerInstanceID) {
        // init the list of server instances
        serversModel.removeAllElements();
        Set<ServerInstanceWrapper> servers = new TreeSet<ServerInstanceWrapper>();
        ServerInstanceWrapper selectedItem = null;
        boolean sjasFound = false;
        for (String serverInstanceID : Deployment.getDefault().getServerInstanceIDs()) {
            String displayName = Deployment.getDefault().getServerInstanceDisplayName(serverInstanceID);
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
            if (displayName != null && j2eePlatform != null && j2eePlatform.getSupportedModuleTypes().contains(J2eeModule.WAR)) {
                ServerInstanceWrapper serverWrapper = new ServerInstanceWrapper(serverInstanceID, displayName);
                // decide whether this server should be preselected
                if (selectedItem == null || !sjasFound) {
                    if (selectedServerInstanceID != null) {
                        if (selectedServerInstanceID.equals(serverInstanceID)) {
                            selectedItem = serverWrapper;
                        }
                    } else {
                        // preselect the best server ;)
                        String shortName = Deployment.getDefault().getServerID(serverInstanceID);
                        if ("J2EE".equals(shortName)) { // NOI18N
                            selectedItem = serverWrapper;
                            sjasFound = true;
                        }
                        else
                        if ("JBoss4".equals(shortName)) { // NOI18N
                            selectedItem = serverWrapper;
                        }
                    }
                }
                servers.add(serverWrapper);
            }
        }
        for (ServerInstanceWrapper item : servers) {
            serversModel.addElement(item);
        }
        if (selectedItem != null) {
            // set the preselected item
            serversModel.setSelectedItem(selectedItem);
        } else if (serversModel.getSize() > 0) {
            // set the first item
            serversModel.setSelectedItem(serversModel.getElementAt(0));
        }
    }

    private Project getSelectedEarApplication() {
        int idx = jComboBoxEnterprise.getSelectedIndex();
        return (idx <= 0) ? null : (Project) earProjects.get(idx - 1);
    }
    
    private void initEnterpriseApplications() {
        jComboBoxEnterprise.addItem(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_AddToEnterprise_None"));
        jComboBoxEnterprise.setSelectedIndex(0);
        
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        earProjects = new ArrayList();
        for (int i = 0; i < allProjects.length; i++) {
            J2eeApplicationProvider j2eeAppProvider = allProjects[i].getLookup().lookup(J2eeApplicationProvider.class);
	    if (j2eeAppProvider != null) {
		J2eeApplication j2eeApplication = (J2eeApplication) j2eeAppProvider.getJ2eeModule();
		ProjectInformation projectInfo = ProjectUtils.getInformation(allProjects[i]);
		if (j2eeApplication != null) {
		    earProjects.add(projectInfo.getProject());
		    jComboBoxEnterprise.addItem(projectInfo.getDisplayName());
		}
	    }
        }
        if (earProjects.size() <= 0) {
            jComboBoxEnterprise.setEnabled(false);
        }
    }

    private String getSelectedJ2eeSpec() {
        Object item = j2eeSpecComboBox.getSelectedItem();
        String value = null;
        if (item != null){
            if (item.equals(JAVA_EE_5_LABEL)) value = J2eeModule.JAVA_EE_5;
            else if (item.equals(J2EE_SPEC_14_LABEL)) value = J2eeModule.J2EE_14;
            else if (item.equals(J2EE_SPEC_13_LABEL)) value = J2eeModule.J2EE_13;
        }
        return value;
    }
    
    private String getSelectedServer() {
        ServerInstanceWrapper serverInstanceWrapper = (ServerInstanceWrapper) serversModel.getSelectedItem();
        if (serverInstanceWrapper == null) {
            return null;
        }
        return serverInstanceWrapper.getServerInstanceID();
    }

    private String validFreeProjectName (final File parentFolder, final String formater, final int index) {
        String name = MessageFormat.format (formater, new Object[]{Integer.valueOf(index)});                
        File file = new File (parentFolder, name);
        return file.exists() ? null : name;
    }
    
    private void setJ2eeVersionWarningPanel() {
        String j2ee = getSelectedJ2eeSpec();
        if (j2ee == null)
            return;
        String warningType = J2eeVersionWarningPanel.findWarningType(j2ee);
        if (warningType == null && warningPanel == null)
            return;
        if (warningPanel == null) {
            warningPanel = new J2eeVersionWarningPanel(warningType);
            warningPlaceHolderPanel.add(warningPanel, java.awt.BorderLayout.CENTER);
            warningPanel.setWarningType(warningType);
        } else {
            warningPanel.setWarningType(warningType);
        }
    }
    
    public File getProjectLocation() {
        return getAsFile(moduleLocationTextField.getText());
    }
    
    private File getAsFile(String filename) {
        return FileUtil.normalizeFile(new File(filename));
    }

    /**
     * Server instance wrapper represents server instances in the servers combobox.
     * @author sherold
     */
    private static class ServerInstanceWrapper implements Comparable {

        private final String serverInstanceID;
        private final String displayName;

        ServerInstanceWrapper(String serverInstanceID, String displayName) {
            this.serverInstanceID = serverInstanceID;
            this.displayName = displayName;
        }

        public String getServerInstanceID() {
            return serverInstanceID;
        }

        public String toString() {
            return displayName;
        }

        public int compareTo(Object o) {
            return toString().compareTo(o.toString());
        }
    }
}

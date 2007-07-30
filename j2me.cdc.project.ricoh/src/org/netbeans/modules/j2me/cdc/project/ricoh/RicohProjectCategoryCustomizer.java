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
 * Microsystems, Inc. 
 * Portions Copyrighted 2006 Ricoh Corporation 
 * All Rights Reserved.
 */

package org.netbeans.modules.j2me.cdc.project.ricoh;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JButton;
import javax.xml.parsers.*;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.*;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import ricoh.util.dom.*;

/**
 *
 * @author  suchys, esanchez, Adam Sotona
 */
public class RicohProjectCategoryCustomizer extends JPanel implements CustomizerPanel, VisualPropertyGroup {
    
    private static String[] PROPERTY_NAMES = new String[] {
        RicohPropertiesDescriptor.RICOH_UID,                          
        RicohPropertiesDescriptor.RICOH_ICON,                         
        RicohPropertiesDescriptor.RICOH_PHONE,                        
        RicohPropertiesDescriptor.RICOH_FAX,                          
        RicohPropertiesDescriptor.RICOH_EMAIL,                        
        RicohPropertiesDescriptor.RICOH_DALP_VERSION,                 
        RicohPropertiesDescriptor.RICOH_DALP_CODEBASE,                
        RicohPropertiesDescriptor.RICOH_DALP_INFO_IS_ABREVIATION_USED,
        RicohPropertiesDescriptor.RICOH_ICON_INVERT,                  
        RicohPropertiesDescriptor.RICOH_DALP_INFO_ABBREVIATION,       
        RicohPropertiesDescriptor.RICOH_DALP_ARGUMENT,                
        RicohPropertiesDescriptor.RICOH_DALP_INSTALL_DESTINATION,     
        RicohPropertiesDescriptor.RICOH_DALP_INSTALL_DESTINATION,     
        RicohPropertiesDescriptor.RICOH_DALP_INSTALL_DESTINATION,     
        RicohPropertiesDescriptor.RICOH_DALP_INSTALL_DESTINATION,     
        RicohPropertiesDescriptor.RICOH_DALP_INSTALL_DESTINATION,     
        RicohPropertiesDescriptor.RICOH_DALP_INSTALL_DESTINATION,     
        RicohPropertiesDescriptor.RICOH_DALP_INSTALL_WORKDIR,         
        RicohPropertiesDescriptor.RICOH_DALP_INSTALL_WORKDIR,         
        RicohPropertiesDescriptor.RICOH_DALP_INSTALL_WORKDIR,         
        RicohPropertiesDescriptor.RICOH_DALP_INSTALL_WORKDIR,         
        RicohPropertiesDescriptor.RICOH_DALP_INSTALL_WORKDIR,         
        RicohPropertiesDescriptor.RICOH_DALP_INSTALL_WORKDIR,         
        RicohPropertiesDescriptor.RICOH_DALP_RESOURCES_DSDK_VERSION,  
        RicohPropertiesDescriptor.RICOH_DALP_RESOURCES_DSDK_VERSION,  
        RicohPropertiesDescriptor.RICOH_DALP_APPDESC_EXECAUTH,        
        RicohPropertiesDescriptor.RICOH_DALP_APPDESC_EXECAUTH,        
        RicohPropertiesDescriptor.RICOH_DALP_DISPLAYMODE_HVGA,        
        RicohPropertiesDescriptor.RICOH_DALP_DISPLAYMODE_VGA,         
        RicohPropertiesDescriptor.RICOH_DALP_DISPLAYMODE_WVGA,        
        RicohPropertiesDescriptor.RICOH_DALP_DISPLAYMODE_4LINE,       
        RicohPropertiesDescriptor.RICOH_DALP_APPDESC_AUTORUN,         
        RicohPropertiesDescriptor.RICOH_DALP_INSTALL_MODE_AUTO,       
        RicohPropertiesDescriptor.RICOH_DALP_DISPLAYMODE_COLOR,       
        RicohPropertiesDescriptor.RICOH_DALP_APPDESC_VISIBLE,         
        RicohPropertiesDescriptor.RICOH_DALP_MANAGE_DISABLE          
    };
    
    private File   dalpFile = null;
    private String projectName;
    private VisualPropertySupport vps;
    private String projectDir = ""; //NOI18N
    private File platformDir = new File(""); //NOI18N
    private boolean comboBoxInitDone = false;
    private boolean changedDalpInfo = false;
    
    /** Creates new form RicohProjectCategoryCustomizer */
    public RicohProjectCategoryCustomizer() {
        initComponents();
    }
    
  
    public void initValues(ProjectProperties props, String configuration) {
        vps = VisualPropertySupport.getDefault(props);
        vps.register(jCheckBox1, configuration, this);
        projectDir = FileUtil.toFile(props.getProjectDirectory()).getAbsolutePath();
        String pName = null;
        if (configuration != null) pName = (String)props.get(VisualPropertySupport.prefixPropertyName(configuration, DefaultPropertiesDescriptor.PLATFORM_ACTIVE));
        if (pName == null) pName = (String)props.get(DefaultPropertiesDescriptor.PLATFORM_ACTIVE);
        if (pName != null) {
            JavaPlatform p[] = JavaPlatformManager.getDefault().getPlatforms(pName, null);
            if (p.length > 0) platformDir = FileUtil.toFile((FileObject)p[0].getInstallFolders().iterator().next());
        }
    }
    
    public String[] getGroupPropertyNames() {
        return PROPERTY_NAMES;
    }
    
    public void initGroupValues(boolean useDefault) {
        vps.register(applicationUID, RicohPropertiesDescriptor.RICOH_UID, useDefault);  
        vps.register(applicationIcon, RicohPropertiesDescriptor.RICOH_ICON, useDefault);
        vps.register(telephoneNumber, RicohPropertiesDescriptor.RICOH_PHONE, useDefault);    
        vps.register(faxNumber, RicohPropertiesDescriptor.RICOH_FAX, useDefault);
        vps.register(email, RicohPropertiesDescriptor.RICOH_EMAIL, useDefault);
        vps.register(dalpVersionTextField, RicohPropertiesDescriptor.RICOH_DALP_VERSION, useDefault);
        vps.register(codeBaseTextField, RicohPropertiesDescriptor.RICOH_DALP_CODEBASE, useDefault);
        vps.register(useAbbreviationCheckBox, RicohPropertiesDescriptor.RICOH_DALP_INFO_IS_ABREVIATION_USED, useDefault);
        vps.register(invertBitmapCheckBox, RicohPropertiesDescriptor.RICOH_ICON_INVERT, useDefault);
        vps.register(abbrevationTextField, RicohPropertiesDescriptor.RICOH_DALP_INFO_ABBREVIATION, useDefault);
        vps.register(startupArgumentsTextField, RicohPropertiesDescriptor.RICOH_DALP_ARGUMENT, useDefault);
        vps.register(installHDDRadioButton, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_DESTINATION, useDefault);
        vps.register(installSDVMRadioButton, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_DESTINATION, useDefault);
        vps.register(installOtherSDRadioButton, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_DESTINATION, useDefault);
        vps.register(installOtherSDRadioButton1, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_DESTINATION, useDefault);
        vps.register(installOtherSDRadioButton2, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_DESTINATION, useDefault);
        vps.register(installOtherSDRadioButton3, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_DESTINATION, useDefault);
        vps.register(workingDirHDDRadioButton, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_WORKDIR, useDefault);
        vps.register(workingDirSDVMRadioButton, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_WORKDIR, useDefault);
        vps.register(workingDirOtherSDRadioButton, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_WORKDIR, useDefault);
        vps.register(workingDirOtherSDRadioButton1, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_WORKDIR, useDefault);
        vps.register(workingDirOtherSDRadioButton2, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_WORKDIR, useDefault);
        vps.register(workingDirOtherSDRadioButton3, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_WORKDIR, useDefault);
        vps.register(dalpSpecSDKJ1RadioButton, RicohPropertiesDescriptor.RICOH_DALP_RESOURCES_DSDK_VERSION, useDefault);
        vps.register(dalpSpecSDKJ2RadioButton, RicohPropertiesDescriptor.RICOH_DALP_RESOURCES_DSDK_VERSION, useDefault);
        vps.register(guestRadioButton, RicohPropertiesDescriptor.RICOH_DALP_APPDESC_EXECAUTH, useDefault);
        vps.register(adminRadioButton, RicohPropertiesDescriptor.RICOH_DALP_APPDESC_EXECAUTH, useDefault);
        vps.register(hvgaCheckBox, RicohPropertiesDescriptor.RICOH_DALP_DISPLAYMODE_HVGA, useDefault);
        vps.register(vgaCheckBox, RicohPropertiesDescriptor.RICOH_DALP_DISPLAYMODE_VGA, useDefault);
        vps.register(wvgaCheckBox, RicohPropertiesDescriptor.RICOH_DALP_DISPLAYMODE_WVGA, useDefault);
        vps.register(lcdCheckBox, RicohPropertiesDescriptor.RICOH_DALP_DISPLAYMODE_4LINE, useDefault);
        vps.register(autoRunCheckBox, RicohPropertiesDescriptor.RICOH_DALP_APPDESC_AUTORUN, useDefault);
        vps.register(autoInstallCheckBox, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_MODE_AUTO, useDefault);
        vps.register(colorEnableCheckBox, RicohPropertiesDescriptor.RICOH_DALP_DISPLAYMODE_COLOR, useDefault);
        vps.register(visibleCheckBox, RicohPropertiesDescriptor.RICOH_DALP_APPDESC_VISIBLE, useDefault);
        vps.register(disableDalpManagementCheckBox, RicohPropertiesDescriptor.RICOH_DALP_MANAGE_DISABLE, useDefault);
 
        jLabel1.setEnabled(!useDefault);
        jLabel2.setEnabled(!useDefault);
        jLabel3.setEnabled(!useDefault);
        jLabel4.setEnabled(!useDefault);
        jLabel5.setEnabled(!useDefault);
        jLabel7.setEnabled(!useDefault);
        jLabel9.setEnabled(!useDefault);
        advancedButton.setEnabled(!useDefault);                      
        browseApplicationIcon.setEnabled(!useDefault);
        changeApplicationUID.setEnabled(!useDefault);
    }  
    
    public File getDalpFile() {
        File projectDir = new File(this.projectDir);
        File [] contents = projectDir.listFiles();
        for (int i = 0; i < contents.length; i++)
        {
            if (contents[i].getName().endsWith(".dalp")) //NOI18N
                return contents[i];
        }
        return null;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        advancedOptionsPanel = new javax.swing.JPanel();
        advancedOptionConfigPanel = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        dalpVersionTextField = new javax.swing.JTextField();
        codeBaseTextField = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        iconPathOrAbbrevLabel = new javax.swing.JLabel();
        abbrevationTextField = new javax.swing.JTextField();
        useAbbreviationCheckBox = new javax.swing.JCheckBox();
        jLabel28 = new javax.swing.JLabel();
        startupArgumentsTextField = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        installHDDRadioButton = new javax.swing.JRadioButton();
        installSDVMRadioButton = new javax.swing.JRadioButton();
        installOtherSDRadioButton = new javax.swing.JRadioButton();
        workingDirOtherSDRadioButton = new javax.swing.JRadioButton();
        workingDirSDVMRadioButton = new javax.swing.JRadioButton();
        workingDirHDDRadioButton = new javax.swing.JRadioButton();
        jLabel31 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel16 = new javax.swing.JLabel();
        dalpSpecSDKJ1RadioButton = new javax.swing.JRadioButton();
        dalpSpecSDKJ2RadioButton = new javax.swing.JRadioButton();
        jLabel25 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        guestRadioButton = new javax.swing.JRadioButton();
        adminRadioButton = new javax.swing.JRadioButton();
        jLabel29 = new javax.swing.JLabel();
        hvgaCheckBox = new javax.swing.JCheckBox();
        vgaCheckBox = new javax.swing.JCheckBox();
        wvgaCheckBox = new javax.swing.JCheckBox();
        lcdCheckBox = new javax.swing.JCheckBox();
        autoRunCheckBox = new javax.swing.JCheckBox();
        autoInstallCheckBox = new javax.swing.JCheckBox();
        colorEnableCheckBox = new javax.swing.JCheckBox();
        jSeparator2 = new javax.swing.JSeparator();
        visibleCheckBox = new javax.swing.JCheckBox();
        invertBitmapCheckBox = new javax.swing.JCheckBox();
        installOtherSDRadioButton1 = new javax.swing.JRadioButton();
        installOtherSDRadioButton2 = new javax.swing.JRadioButton();
        installOtherSDRadioButton3 = new javax.swing.JRadioButton();
        workingDirOtherSDRadioButton1 = new javax.swing.JRadioButton();
        workingDirOtherSDRadioButton2 = new javax.swing.JRadioButton();
        workingDirOtherSDRadioButton3 = new javax.swing.JRadioButton();
        disableDalpManagementCheckBox = new javax.swing.JCheckBox();
        appTypeButtonGroup = new javax.swing.ButtonGroup();
        installLocationButtonGroup = new javax.swing.ButtonGroup();
        workingDirectoryButtonGroup = new javax.swing.ButtonGroup();
        basepathButtonGroup = new javax.swing.ButtonGroup();
        dalpVersionSpecButtonGroup = new javax.swing.ButtonGroup();
        execLevelButtonGroup = new javax.swing.ButtonGroup();
        energySaveButtonGroup = new javax.swing.ButtonGroup();
        iconLocationButtonGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        applicationUID = new javax.swing.JTextField();
        changeApplicationUID = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        browseApplicationIcon = new javax.swing.JButton();
        applicationIcon = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        telephoneNumber = new javax.swing.JTextField();
        faxNumber = new javax.swing.JTextField();
        email = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        advancedButton = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel15, "Advanced Options");

        jLabel18.setLabelFor(dalpVersionTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/cdc/project/ricoh/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel18, bundle.getString("LBL_DalpVersion")); // NOI18N

        jLabel14.setLabelFor(codeBaseTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel14, bundle.getString("LBL_CodebaseURL")); // NOI18N

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel22, bundle.getString("LBL_IconOptions:")); // NOI18N

        iconPathOrAbbrevLabel.setLabelFor(abbrevationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(iconPathOrAbbrevLabel, bundle.getString("LBL_Abbreviation")); // NOI18N
        iconPathOrAbbrevLabel.setEnabled(false);

        abbrevationTextField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(useAbbreviationCheckBox, bundle.getString("LBL_UseAbbrevCheckBox")); // NOI18N
        useAbbreviationCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useAbbreviationCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel28.setLabelFor(startupArgumentsTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel28, bundle.getString("LBL_StartupArgs")); // NOI18N

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel30, bundle.getString("LBL_InstallLocation")); // NOI18N

        installLocationButtonGroup.add(installHDDRadioButton);
        installHDDRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(installHDDRadioButton, bundle.getString("LBL_InstallLocHDD")); // NOI18N
        installHDDRadioButton.setActionCommand("hdd"); // NOI18N
        installHDDRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        installHDDRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        installLocationButtonGroup.add(installSDVMRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(installSDVMRadioButton, bundle.getString("LBL_InstallLocVM")); // NOI18N
        installSDVMRadioButton.setActionCommand("sdcard"); // NOI18N
        installSDVMRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        installSDVMRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        installLocationButtonGroup.add(installOtherSDRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(installOtherSDRadioButton, bundle.getString("LBL_InstallLocOther")); // NOI18N
        installOtherSDRadioButton.setActionCommand("sdcard0"); // NOI18N
        installOtherSDRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        installOtherSDRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        workingDirectoryButtonGroup.add(workingDirOtherSDRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(workingDirOtherSDRadioButton, bundle.getString("LBL_WorkingDirOther")); // NOI18N
        workingDirOtherSDRadioButton.setActionCommand("sdcard0"); // NOI18N
        workingDirOtherSDRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        workingDirOtherSDRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        workingDirectoryButtonGroup.add(workingDirSDVMRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(workingDirSDVMRadioButton, bundle.getString("LBL_WorkingDirVM")); // NOI18N
        workingDirSDVMRadioButton.setActionCommand("sdcard"); // NOI18N
        workingDirSDVMRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        workingDirSDVMRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        workingDirectoryButtonGroup.add(workingDirHDDRadioButton);
        workingDirHDDRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(workingDirHDDRadioButton, bundle.getString("LBL_WorkingDirHDD")); // NOI18N
        workingDirHDDRadioButton.setActionCommand("hdd"); // NOI18N
        workingDirHDDRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        workingDirHDDRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel31.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel31, bundle.getString("LBL_WorkingDir")); // NOI18N

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel16, bundle.getString("LBL_DalpSpecification")); // NOI18N

        dalpVersionSpecButtonGroup.add(dalpSpecSDKJ1RadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(dalpSpecSDKJ1RadioButton, bundle.getString("LBL_SDKJ_Spec_1_0")); // NOI18N
        dalpSpecSDKJ1RadioButton.setActionCommand("1.0"); // NOI18N
        dalpSpecSDKJ1RadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dalpSpecSDKJ1RadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dalpSpecSDKJ1RadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dalpSpecSDKJ1RadioButtonActionPerformed(evt);
            }
        });

        dalpVersionSpecButtonGroup.add(dalpSpecSDKJ2RadioButton);
        dalpSpecSDKJ2RadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(dalpSpecSDKJ2RadioButton, bundle.getString("LBL_SDKJ_Spec_2_0")); // NOI18N
        dalpSpecSDKJ2RadioButton.setActionCommand("2.0"); // NOI18N
        dalpSpecSDKJ2RadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dalpSpecSDKJ2RadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dalpSpecSDKJ2RadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dalpSpecSDKJ2RadioButtonActionPerformed(evt);
            }
        });

        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel25, bundle.getString("LBL_ApplicationExecution")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel27, bundle.getString("LBL_ExecLvl")); // NOI18N

        execLevelButtonGroup.add(guestRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(guestRadioButton, bundle.getString("LBL_AppExecLvlGuest")); // NOI18N
        guestRadioButton.setActionCommand("guest"); // NOI18N
        guestRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        guestRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        execLevelButtonGroup.add(adminRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(adminRadioButton, bundle.getString("LBL_AppExecLvlAdmin")); // NOI18N
        adminRadioButton.setActionCommand("admin"); // NOI18N
        adminRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        adminRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel29.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel29, bundle.getString("LBL_DisplayModes")); // NOI18N

        hvgaCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(hvgaCheckBox, bundle.getString("LBL_HVGA")); // NOI18N
        hvgaCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        hvgaCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(vgaCheckBox, bundle.getString("LBL_VGA")); // NOI18N
        vgaCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        vgaCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(wvgaCheckBox, bundle.getString("LBL_WVGA")); // NOI18N
        wvgaCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        wvgaCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(lcdCheckBox, bundle.getString("LBL_4LineLED")); // NOI18N
        lcdCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        lcdCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(autoRunCheckBox, bundle.getString("LBL_EnableAutoRun")); // NOI18N
        autoRunCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        autoRunCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        autoInstallCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(autoInstallCheckBox, bundle.getString("LBL_EnableAutoInstall")); // NOI18N
        autoInstallCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        autoInstallCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(colorEnableCheckBox, bundle.getString("LBL_EnableColorDisplay")); // NOI18N
        colorEnableCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        colorEnableCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        visibleCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(visibleCheckBox, bundle.getString("LBL_Visible")); // NOI18N
        visibleCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        visibleCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        invertBitmapCheckBox.setText(bundle.getString("LBL_InvertIconBitmap")); // NOI18N
        invertBitmapCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        invertBitmapCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        installLocationButtonGroup.add(installOtherSDRadioButton1);
        org.openide.awt.Mnemonics.setLocalizedText(installOtherSDRadioButton1, bundle.getString("LBL_InstallLocSlot2")); // NOI18N
        installOtherSDRadioButton1.setActionCommand("sdcard1"); // NOI18N
        installOtherSDRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        installOtherSDRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        installLocationButtonGroup.add(installOtherSDRadioButton2);
        org.openide.awt.Mnemonics.setLocalizedText(installOtherSDRadioButton2, bundle.getString("LBL_InstallLocSlot3")); // NOI18N
        installOtherSDRadioButton2.setActionCommand("sdcard2"); // NOI18N
        installOtherSDRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        installOtherSDRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));

        installLocationButtonGroup.add(installOtherSDRadioButton3);
        org.openide.awt.Mnemonics.setLocalizedText(installOtherSDRadioButton3, bundle.getString("LBL_InstallLocSlot4")); // NOI18N
        installOtherSDRadioButton3.setActionCommand("sdcard3"); // NOI18N
        installOtherSDRadioButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        installOtherSDRadioButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));

        workingDirectoryButtonGroup.add(workingDirOtherSDRadioButton1);
        org.openide.awt.Mnemonics.setLocalizedText(workingDirOtherSDRadioButton1, bundle.getString("LBL_WorkingDirSlot2")); // NOI18N
        workingDirOtherSDRadioButton1.setActionCommand("sdcard1"); // NOI18N
        workingDirOtherSDRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        workingDirOtherSDRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        workingDirectoryButtonGroup.add(workingDirOtherSDRadioButton2);
        org.openide.awt.Mnemonics.setLocalizedText(workingDirOtherSDRadioButton2, bundle.getString("LBL_WorkingDirSlot3")); // NOI18N
        workingDirOtherSDRadioButton2.setActionCommand("sdcard2"); // NOI18N
        workingDirOtherSDRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        workingDirOtherSDRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));

        workingDirectoryButtonGroup.add(workingDirOtherSDRadioButton3);
        org.openide.awt.Mnemonics.setLocalizedText(workingDirOtherSDRadioButton3, bundle.getString("LBL_WorkingDirSlot4")); // NOI18N
        workingDirOtherSDRadioButton3.setActionCommand("sdcard3"); // NOI18N
        workingDirOtherSDRadioButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        workingDirOtherSDRadioButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout advancedOptionConfigPanelLayout = new org.jdesktop.layout.GroupLayout(advancedOptionConfigPanel);
        advancedOptionConfigPanel.setLayout(advancedOptionConfigPanelLayout);
        advancedOptionConfigPanelLayout.setHorizontalGroup(
            advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(advancedOptionConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 625, Short.MAX_VALUE)
                    .add(advancedOptionConfigPanelLayout.createSequentialGroup()
                        .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel15)
                            .add(jLabel22)
                            .add(jLabel28)
                            .add(advancedOptionConfigPanelLayout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(advancedOptionConfigPanelLayout.createSequentialGroup()
                                        .add(iconPathOrAbbrevLabel)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(abbrevationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE))
                                    .add(advancedOptionConfigPanelLayout.createSequentialGroup()
                                        .add(useAbbreviationCheckBox)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(invertBitmapCheckBox)
                                        .add(117, 117, 117))))
                            .add(advancedOptionConfigPanelLayout.createSequentialGroup()
                                .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel14)
                                    .add(jLabel18))
                                .add(41, 41, 41)
                                .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(advancedOptionConfigPanelLayout.createSequentialGroup()
                                        .add(dalpVersionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 71, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(259, 259, 259))
                                    .add(codeBaseTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)))
                            .add(startupArgumentsTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
                            .add(advancedOptionConfigPanelLayout.createSequentialGroup()
                                .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(installSDVMRadioButton)
                                    .add(installHDDRadioButton)
                                    .add(installOtherSDRadioButton)
                                    .add(jLabel30)
                                    .add(installOtherSDRadioButton1)
                                    .add(installOtherSDRadioButton2)
                                    .add(installOtherSDRadioButton3))
                                .add(95, 95, 95)
                                .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(workingDirOtherSDRadioButton3)
                                    .add(workingDirOtherSDRadioButton2)
                                    .add(workingDirOtherSDRadioButton1)
                                    .add(jLabel31)
                                    .add(workingDirSDVMRadioButton)
                                    .add(workingDirHDDRadioButton)
                                    .add(workingDirOtherSDRadioButton))
                                .add(143, 143, 143)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparator1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(visibleCheckBox)
                            .add(advancedOptionConfigPanelLayout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(dalpSpecSDKJ1RadioButton)
                                    .add(dalpSpecSDKJ2RadioButton)))
                            .add(jLabel16)
                            .add(jLabel25)
                            .add(jLabel27)
                            .add(advancedOptionConfigPanelLayout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(adminRadioButton)
                                    .add(guestRadioButton)))
                            .add(jLabel29)
                            .add(autoRunCheckBox)
                            .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, autoInstallCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, advancedOptionConfigPanelLayout.createSequentialGroup()
                                    .add(10, 10, 10)
                                    .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(vgaCheckBox)
                                        .add(hvgaCheckBox)
                                        .add(wvgaCheckBox)
                                        .add(lcdCheckBox))))
                            .add(colorEnableCheckBox))))
                .addContainerGap())
        );
        advancedOptionConfigPanelLayout.setVerticalGroup(
            advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(advancedOptionConfigPanelLayout.createSequentialGroup()
                .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(advancedOptionConfigPanelLayout.createSequentialGroup()
                        .add(jLabel15)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel18)
                            .add(dalpVersionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel14)
                            .add(codeBaseTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel22)
                        .add(6, 6, 6)
                        .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(useAbbreviationCheckBox)
                            .add(invertBitmapCheckBox))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(iconPathOrAbbrevLabel)
                            .add(abbrevationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel28)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(startupArgumentsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel30)
                            .add(jLabel31))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(installHDDRadioButton)
                            .add(workingDirHDDRadioButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(workingDirSDVMRadioButton)
                            .add(installSDVMRadioButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(workingDirOtherSDRadioButton)
                            .add(installOtherSDRadioButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(installOtherSDRadioButton1)
                            .add(workingDirOtherSDRadioButton1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(installOtherSDRadioButton2)
                            .add(workingDirOtherSDRadioButton2))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(installOtherSDRadioButton3)
                            .add(workingDirOtherSDRadioButton3)))
                    .add(advancedOptionConfigPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(advancedOptionConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                            .add(advancedOptionConfigPanelLayout.createSequentialGroup()
                                .add(jLabel16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(dalpSpecSDKJ1RadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(dalpSpecSDKJ2RadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel25)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel27)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(guestRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(adminRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel29)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(hvgaCheckBox)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(vgaCheckBox)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(wvgaCheckBox)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lcdCheckBox)
                                .add(11, 11, 11)
                                .add(autoRunCheckBox)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(autoInstallCheckBox)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(colorEnableCheckBox)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(visibleCheckBox)))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        advancedOptionConfigPanelLayout.linkSize(new java.awt.Component[] {abbrevationTextField, codeBaseTextField, dalpVersionTextField, jLabel14, jLabel22, useAbbreviationCheckBox}, org.jdesktop.layout.GroupLayout.VERTICAL);

        advancedOptionConfigPanelLayout.linkSize(new java.awt.Component[] {installHDDRadioButton, installSDVMRadioButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        jLabel15.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_AdvancedOptions")); // NOI18N
        jLabel15.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_AdvancedOptions")); // NOI18N
        jLabel18.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_DalpVersion")); // NOI18N
        jLabel18.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_DalpVersion")); // NOI18N
        dalpVersionTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_DalpVersionTextField")); // NOI18N
        dalpVersionTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_DalpVersionTextField")); // NOI18N
        codeBaseTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CodeBaseTextField")); // NOI18N
        codeBaseTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CodeBaseTextField")); // NOI18N
        jLabel14.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_CodebaseURL")); // NOI18N
        jLabel14.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_CodebaseURL")); // NOI18N
        jLabel22.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_IconOptions")); // NOI18N
        jLabel22.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_IconOptions")); // NOI18N
        iconPathOrAbbrevLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_Abbreviation")); // NOI18N
        iconPathOrAbbrevLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_Abbreviation")); // NOI18N
        abbrevationTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_AbbreviationTextField")); // NOI18N
        abbrevationTextField.getAccessibleContext().setAccessibleDescription("KEY ACSD_AbbreviationTextField : RB org/netbeans/modules/j2me/cdc/project/ricoh/Bundle");
        useAbbreviationCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_UseAbbrevCheckBox")); // NOI18N
        useAbbreviationCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_UseAbbrevCheckBox")); // NOI18N
        jLabel28.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_StartupArgs")); // NOI18N
        jLabel28.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_StartupArgs")); // NOI18N
        startupArgumentsTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_StartupArgumentsTextField")); // NOI18N
        startupArgumentsTextField.getAccessibleContext().setAccessibleDescription("KEY ACSD_StartupArgumentsTextField : RB org/netbeans/modules/j2me/cdc/project/ricoh/Bundle");
        jLabel30.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_InstallLocation")); // NOI18N
        jLabel30.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_InstallLocation")); // NOI18N
        installHDDRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_InstallLocHDD")); // NOI18N
        installHDDRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_InstallLocHDD")); // NOI18N
        installSDVMRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_InstallLocVM")); // NOI18N
        installSDVMRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_InstallLocVM")); // NOI18N
        installOtherSDRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_InstallLocOther")); // NOI18N
        installOtherSDRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_InstallLocOther")); // NOI18N
        workingDirOtherSDRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_WorkingDirOther")); // NOI18N
        workingDirOtherSDRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_WorkingDirOther")); // NOI18N
        workingDirSDVMRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_WorkingDirVM")); // NOI18N
        workingDirSDVMRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_WorkingDirVM")); // NOI18N
        workingDirHDDRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_WorkingDirHDD")); // NOI18N
        workingDirHDDRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_WorkingDirHDD")); // NOI18N
        jLabel31.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_WorkingDir")); // NOI18N
        jLabel31.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_WorkingDir")); // NOI18N
        jLabel16.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_DalpVersion")); // NOI18N
        jLabel16.getAccessibleContext().setAccessibleDescription("KEY ACSD_DalpVersion : RB org/netbeans/modules/j2me/cdc/project/ricoh/Bundle");
        dalpSpecSDKJ1RadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_SDKJ_Spec_1_0")); // NOI18N
        dalpSpecSDKJ1RadioButton.getAccessibleContext().setAccessibleDescription("KEY ACSD_SDKJ_Spec_1_0 : RB org/netbeans/modules/j2me/cdc/project/ricoh/Bundle");
        dalpSpecSDKJ2RadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_SDKJ_Spec_2_0")); // NOI18N
        dalpSpecSDKJ2RadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_SDKJ_Spec_2_0")); // NOI18N
        jLabel25.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_ApplicationExecution")); // NOI18N
        jLabel25.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_ApplicationExecution")); // NOI18N
        jLabel27.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_ExecLvl")); // NOI18N
        jLabel27.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_ExecLvl")); // NOI18N
        guestRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_AppExecLvlGuest")); // NOI18N
        guestRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_AppExecLvlGuest")); // NOI18N
        adminRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_AppExecLvlAdmin")); // NOI18N
        adminRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_AppExecLvlAdmin")); // NOI18N
        jLabel29.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_DisplayModes")); // NOI18N
        jLabel29.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_DisplayModes")); // NOI18N
        hvgaCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_HVGA")); // NOI18N
        hvgaCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_HVGA")); // NOI18N
        vgaCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_VGA")); // NOI18N
        vgaCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_VGA")); // NOI18N
        wvgaCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_WVGA")); // NOI18N
        wvgaCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_WVGA")); // NOI18N
        lcdCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_4LineLED")); // NOI18N
        lcdCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_4LineLED")); // NOI18N
        autoRunCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_EnableAutoRun")); // NOI18N
        autoRunCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_EnableAutoRun")); // NOI18N
        autoInstallCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_EnableAutoInstall")); // NOI18N
        autoInstallCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_EnableAutoInstall")); // NOI18N
        colorEnableCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_EnableColorDisplay")); // NOI18N
        colorEnableCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_EnableColorDisplay")); // NOI18N
        visibleCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_Visible")); // NOI18N
        visibleCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_Visible")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(disableDalpManagementCheckBox, bundle.getString("LBL_DalpManagement")); // NOI18N
        disableDalpManagementCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        disableDalpManagementCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout advancedOptionsPanelLayout = new org.jdesktop.layout.GroupLayout(advancedOptionsPanel);
        advancedOptionsPanel.setLayout(advancedOptionsPanelLayout);
        advancedOptionsPanelLayout.setHorizontalGroup(
            advancedOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(advancedOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(disableDalpManagementCheckBox)
                .addContainerGap(284, Short.MAX_VALUE))
            .add(advancedOptionConfigPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        advancedOptionsPanelLayout.setVerticalGroup(
            advancedOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, advancedOptionsPanelLayout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(advancedOptionConfigPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(disableDalpManagementCheckBox))
        );

        disableDalpManagementCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_DalpManagement")); // NOI18N
        disableDalpManagementCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_DalpManagement")); // NOI18N

        advancedOptionsPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_ProjectAdvancedCustomizerPanel")); // NOI18N

        jLabel1.setLabelFor(applicationUID);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "LBL_ApplicationUID")); // NOI18N

        applicationUID.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(changeApplicationUID, org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "LBL_ChangeUID")); // NOI18N
        changeApplicationUID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeApplicationUIDActionPerformed(evt);
            }
        });

        jLabel2.setLabelFor(applicationIcon);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "LBL_ApplicationIcon")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseApplicationIcon, org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "LBL_BrowseIcon")); // NOI18N
        browseApplicationIcon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseApplicationIconActionPerformed(evt);
            }
        });

        jLabel3.setLabelFor(telephoneNumber);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "LBL_PhoneNumber")); // NOI18N

        jLabel4.setLabelFor(faxNumber);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "LBL_FaxNumber")); // NOI18N

        jLabel5.setLabelFor(email);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, bundle.getString("LBL_Email")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, bundle.getString("LBL_VendorContact")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, bundle.getString("LBL_AppInfo")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(advancedButton, bundle.getString("BUTTON_Advanced")); // NOI18N
        advancedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advancedButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "LBL_UseDefault")); // NOI18N
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBox1)
                    .add(jLabel9)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(jLabel1)
                                .add(25, 25, 25)
                                .add(applicationUID, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(jLabel2)
                                .add(22, 22, 22)
                                .add(applicationIcon, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(changeApplicationUID, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                            .add(browseApplicationIcon, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)))
                    .add(jLabel7)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, advancedButton)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(jLabel5)
                            .add(jLabel4))
                        .add(28, 28, 28)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, faxNumber, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
                            .add(telephoneNumber, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
                            .add(email, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jCheckBox1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel9)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(applicationUID, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(changeApplicationUID))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(applicationIcon, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseApplicationIcon))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel7)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(telephoneNumber, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(faxNumber, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(email, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(advancedButton)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {email, faxNumber, telephoneNumber}, org.jdesktop.layout.GroupLayout.VERTICAL);

        applicationUID.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_applicationUID")); // NOI18N
        applicationIcon.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_applicationIcon")); // NOI18N
        telephoneNumber.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_telephoneNumber")); // NOI18N
        faxNumber.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_faxNumber")); // NOI18N
        email.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_email")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void dalpSpecSDKJ2RadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dalpSpecSDKJ2RadioButtonActionPerformed
// TODO add your handling code here:
        colorEnableCheckBox.setEnabled(true);
        this.hvgaCheckBox.setEnabled(true);
        this.vgaCheckBox.setEnabled(true);
        this.wvgaCheckBox.setEnabled(true);
        this.lcdCheckBox.setEnabled(true);
        this.workingDirHDDRadioButton.setEnabled(true);
        this.workingDirOtherSDRadioButton.setEnabled(true);
        this.workingDirOtherSDRadioButton1.setEnabled(true);
        this.workingDirOtherSDRadioButton2.setEnabled(true);
        this.workingDirOtherSDRadioButton3.setEnabled(true);
        this.workingDirSDVMRadioButton.setEnabled(true);
    }//GEN-LAST:event_dalpSpecSDKJ2RadioButtonActionPerformed

    private void dalpSpecSDKJ1RadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dalpSpecSDKJ1RadioButtonActionPerformed
// TODO add your handling code here:
        colorEnableCheckBox.setEnabled(false);
        this.hvgaCheckBox.setEnabled(false);
        this.vgaCheckBox.setEnabled(false);
        this.wvgaCheckBox.setEnabled(false);
        this.lcdCheckBox.setEnabled(false);
        this.workingDirHDDRadioButton.setEnabled(false);
        this.workingDirOtherSDRadioButton.setEnabled(false);
        this.workingDirOtherSDRadioButton1.setEnabled(false);
        this.workingDirOtherSDRadioButton2.setEnabled(false);
        this.workingDirOtherSDRadioButton3.setEnabled(false);
        this.workingDirSDVMRadioButton.setEnabled(false);
    }//GEN-LAST:event_dalpSpecSDKJ1RadioButtonActionPerformed

    private void advancedButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_advancedButtonActionPerformed
    {//GEN-HEADEREND:event_advancedButtonActionPerformed
// TODO add your handling code here:
        visibleCheckBox.setEnabled(false);        
        if (disableDalpManagementCheckBox.isSelected() == false)
            visibleCheckBox.setEnabled(true);
        else
            visibleCheckBox.setEnabled(false);
        
        JButton closingOption = new JButton(NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "LBL_ButtonAdvancedClose"));
        closingOption.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_ButtonAdvancedClose"));
        closingOption.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_ButtonAdvancedClose"));
        final DialogDescriptor dd = new DialogDescriptor(advancedOptionsPanel, NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "TITLE_AdvancedOptionsDialog"), true, 
                new Object[]{closingOption}, closingOption, DialogDescriptor.DEFAULT_ALIGN, HelpCtx.DEFAULT_HELP, null); //NOI18N
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        //this.advancedOptionsDialog.setTitle(NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "TITLE_AdvancedOptionsDialog")); //NOI18N
        //this.advancedOptionsDialog.pack();
    }//GEN-LAST:event_advancedButtonActionPerformed

    private void changeApplicationUIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeApplicationUIDActionPerformed
        RicohApplicationUIDCustomizer customizer = new RicohApplicationUIDCustomizer(applicationUID.getText(), platformDir);
        final DialogDescriptor dd = new DialogDescriptor(customizer, NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "TITLE_ChangeUID")); //NOI18N
        customizer.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ( NotifyDescriptor.PROP_VALID.equals(evt.getPropertyName())){
                    dd.setValid(((Boolean)evt.getNewValue()).booleanValue());
                }
            }
        });
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        if( dd.getValue() == DialogDescriptor.OK_OPTION ){
            applicationUID.setText(customizer.getUID());
        }
    }//GEN-LAST:event_changeApplicationUIDActionPerformed

    private void browseApplicationIconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseApplicationIconActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory())
                    return true;
                String ext = f.toString().toLowerCase();
                int i = ext.lastIndexOf('.');
                if (i != -1){
                    ext = ext.substring(i+1);
                }
                if (ext.equalsIgnoreCase("bmp")){
                    return true;
                }
                return false;
            }
            public String getDescription() {
                return NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "LBL_LogoImageFiles");
            }
        });
        String workDir = applicationIcon.getText();
        if (workDir.equals("")) {
            workDir = projectDir;
        }
        chooser.setSelectedFile(new File(workDir));
        chooser.setDialogTitle(NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "TITLE_BrowseLogo"));
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            applicationIcon.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_browseApplicationIconActionPerformed

   

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField abbrevationTextField;
    private javax.swing.JRadioButton adminRadioButton;
    private javax.swing.JButton advancedButton;
    private javax.swing.JPanel advancedOptionConfigPanel;
    private javax.swing.JPanel advancedOptionsPanel;
    private javax.swing.ButtonGroup appTypeButtonGroup;
    private javax.swing.JTextField applicationIcon;
    private javax.swing.JTextField applicationUID;
    private javax.swing.JCheckBox autoInstallCheckBox;
    private javax.swing.JCheckBox autoRunCheckBox;
    private javax.swing.ButtonGroup basepathButtonGroup;
    private javax.swing.JButton browseApplicationIcon;
    private javax.swing.JButton changeApplicationUID;
    private javax.swing.JTextField codeBaseTextField;
    private javax.swing.JCheckBox colorEnableCheckBox;
    private javax.swing.JRadioButton dalpSpecSDKJ1RadioButton;
    private javax.swing.JRadioButton dalpSpecSDKJ2RadioButton;
    private javax.swing.ButtonGroup dalpVersionSpecButtonGroup;
    private javax.swing.JTextField dalpVersionTextField;
    private javax.swing.JCheckBox disableDalpManagementCheckBox;
    private javax.swing.JTextField email;
    private javax.swing.ButtonGroup energySaveButtonGroup;
    private javax.swing.ButtonGroup execLevelButtonGroup;
    private javax.swing.JTextField faxNumber;
    private javax.swing.JRadioButton guestRadioButton;
    private javax.swing.JCheckBox hvgaCheckBox;
    private javax.swing.ButtonGroup iconLocationButtonGroup;
    private javax.swing.JLabel iconPathOrAbbrevLabel;
    private javax.swing.JRadioButton installHDDRadioButton;
    private javax.swing.ButtonGroup installLocationButtonGroup;
    private javax.swing.JRadioButton installOtherSDRadioButton;
    private javax.swing.JRadioButton installOtherSDRadioButton1;
    private javax.swing.JRadioButton installOtherSDRadioButton2;
    private javax.swing.JRadioButton installOtherSDRadioButton3;
    private javax.swing.JRadioButton installSDVMRadioButton;
    private javax.swing.JCheckBox invertBitmapCheckBox;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JCheckBox lcdCheckBox;
    private javax.swing.JTextField startupArgumentsTextField;
    private javax.swing.JTextField telephoneNumber;
    private javax.swing.JCheckBox useAbbreviationCheckBox;
    private javax.swing.JCheckBox vgaCheckBox;
    private javax.swing.JCheckBox visibleCheckBox;
    private javax.swing.JRadioButton workingDirHDDRadioButton;
    private javax.swing.JRadioButton workingDirOtherSDRadioButton;
    private javax.swing.JRadioButton workingDirOtherSDRadioButton1;
    private javax.swing.JRadioButton workingDirOtherSDRadioButton2;
    private javax.swing.JRadioButton workingDirOtherSDRadioButton3;
    private javax.swing.JRadioButton workingDirSDVMRadioButton;
    private javax.swing.ButtonGroup workingDirectoryButtonGroup;
    private javax.swing.JCheckBox wvgaCheckBox;
    // End of variables declaration//GEN-END:variables
    
}

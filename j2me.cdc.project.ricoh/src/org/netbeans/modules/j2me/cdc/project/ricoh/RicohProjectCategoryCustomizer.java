/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
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
 * "Portions Copyrighted 2006 Ricoh Corporation"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2me.cdc.project.ricoh;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JButton;
import javax.xml.parsers.*;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import ricoh.util.dom.*;

/**
 *
 */
public class RicohProjectCategoryCustomizer extends JPanel implements CustomizerPanel, VisualPropertyGroup {

    private static String[] PROPERTY_NAMES = new String[]{RicohPropertiesDescriptor.RICOH_UID, RicohPropertiesDescriptor.RICOH_ICON, RicohPropertiesDescriptor.RICOH_PHONE, RicohPropertiesDescriptor.RICOH_FAX, RicohPropertiesDescriptor.RICOH_EMAIL, RicohPropertiesDescriptor.RICOH_DALP_VERSION, RicohPropertiesDescriptor.RICOH_DALP_CODEBASE, RicohPropertiesDescriptor.RICOH_DALP_INFO_IS_ABREVIATION_USED, RicohPropertiesDescriptor.RICOH_ICON_INVERT, RicohPropertiesDescriptor.RICOH_DALP_INFO_ABBREVIATION, RicohPropertiesDescriptor.RICOH_DALP_ARGUMENT, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_DESTINATION, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_WORKDIR, RicohPropertiesDescriptor.RICOH_DALP_RESOURCES_DSDK_VERSION, RicohPropertiesDescriptor.RICOH_DALP_APPDESC_EXECAUTH, RicohPropertiesDescriptor.RICOH_DALP_DISPLAYMODE_HVGA, RicohPropertiesDescriptor.RICOH_DALP_DISPLAYMODE_VGA, RicohPropertiesDescriptor.RICOH_DALP_DISPLAYMODE_WVGA, RicohPropertiesDescriptor.RICOH_DALP_DISPLAYMODE_4LINE, RicohPropertiesDescriptor.RICOH_DALP_APPDESC_AUTORUN, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_MODE_AUTO, RicohPropertiesDescriptor.RICOH_DALP_DISPLAYMODE_COLOR, RicohPropertiesDescriptor.RICOH_DALP_APPDESC_VISIBLE, RicohPropertiesDescriptor.RICOH_DALP_MANAGE_DISABLE};

    private VisualPropertySupport vps;
    private String projectDir = ""; //NOI18N
    private File platformDir = new File(""); //NOI18N
    private JTextField installLocationTextField = new JTextField();
    private JTextField workingDirTextField      = new JTextField();

    /** Creates new form RicohProjectCategoryCustomizer */
    public RicohProjectCategoryCustomizer() {
        initComponents();
        postInit();
        this.invertBitmapCheckBox.setVisible(false);
        installLocationTextField.setVisible(false);
        this.add(installLocationTextField);
        workingDirTextField.setVisible(false);
        this.add(workingDirTextField);
    }


    public void initValues(ProjectProperties props, String configuration) {
        vps = VisualPropertySupport.getDefault(props);
        vps.register(jCheckBox1, configuration, this);
        projectDir = FileUtil.toFile(props.getProjectDirectory()).getAbsolutePath();
        String pName = null;
        if (configuration != null) {
            pName = (String) props.get(VisualPropertySupport.prefixPropertyName(configuration, DefaultPropertiesDescriptor.PLATFORM_ACTIVE));
        }
        if (pName == null) {
            pName = (String) props.get(DefaultPropertiesDescriptor.PLATFORM_ACTIVE);
        }
        if (pName != null) {
            JavaPlatform[] p = JavaPlatformManager.getDefault().getPlatforms(pName, null);
            if (p.length > 0) {
                platformDir = FileUtil.toFile(p[0].getInstallFolders().iterator().next());
            }
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
//        vps.register(installHDDRadioButton, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_DESTINATION, useDefault);
//        vps.register(installSDVMRadioButton, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_DESTINATION, useDefault);
//        vps.register(installOtherSDRadioButton, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_DESTINATION, useDefault);
        vps.register(installLocationTextField, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_DESTINATION, useDefault);
//        vps.register(workingDirHDDRadioButton, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_WORKDIR, useDefault);
//        vps.register(workingDirSDVMRadioButton, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_WORKDIR, useDefault);
        vps.register(workingDirTextField, RicohPropertiesDescriptor.RICOH_DALP_INSTALL_WORKDIR, useDefault);
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
        useAbbreviationCheckBoxActionPerformed(null);
        if (dalpSpecSDKJ1RadioButton.isSelected()) {
            dalpSpecSDKJ1RadioButtonActionPerformed(null);
        }
        if (dalpSpecSDKJ2RadioButton.isSelected()) {
            dalpSpecSDKJ2RadioButtonActionPerformed(null);
        }
    }

    public File getDalpFile() {
        File projectDir = new File(this.projectDir);
        File[] contents = projectDir.listFiles();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i].getName().endsWith(".dalp")) {
                //NOI18N
                return contents[i];
            }
        }
        return null;
    }

    @Override
    public void addNotify() {
        updateLocationsSelection();
        panelEnabler(!disableDalpManagementCheckBox.isSelected(), advancedOptionConfigPanel);
        startListening();
        super.addNotify();
    }    
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        stopListening();
    }
    
    private void updateLocationsSelection(){
        String selection = installLocationTextField.getText();
        if ("hdd".equals(selection)){
            installHDDRadioButton.setSelected(true);
            installLocationComboBox.setEnabled(false);
        } else if ("sdcard".equals(selection)) {
            installSDVMRadioButton.setSelected(true);
            installLocationComboBox.setEnabled(false);
        } else if (selection.startsWith("sdcard")){
            int index = 0;
            try {
                index = Integer.parseInt(selection.substring(6)) - 1;
            } catch (Exception e){}
            installOtherSDRadioButton.setSelected(true);
            installLocationComboBox.setSelectedIndex(index);
            installLocationComboBox.setEnabled(true);
        } else {
            installHDDRadioButton.setSelected(true);
            installLocationComboBox.setEnabled(false);
        }

        selection = workingDirTextField.getText();
        if ("hdd".equals(selection)){
            workingDirHDDRadioButton.setSelected(true);
            workingDirComboBox.setEnabled(false);
        } else if ("sdcard".equals(selection)) {
            workingDirSDVMRadioButton.setSelected(true);
            workingDirComboBox.setEnabled(false);
        } else if (selection.startsWith("sdcard")){
            int index = 0;
            try {
                index = Integer.parseInt(selection.substring(5)) - 1;
            } catch (Exception e){}
            workingDirOtherSDRadioButton.setSelected(true);
            workingDirComboBox.setSelectedIndex(index);
            workingDirComboBox.setEnabled(true);
        } else {
            workingDirHDDRadioButton.setSelected(true);
            workingDirComboBox.setEnabled(false);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        appTypeButtonGroup = new javax.swing.ButtonGroup();
        installLocationButtonGroup = new javax.swing.ButtonGroup();
        workingDirectoryButtonGroup = new javax.swing.ButtonGroup();
        basepathButtonGroup = new javax.swing.ButtonGroup();
        dalpVersionSpecButtonGroup = new javax.swing.ButtonGroup();
        execLevelButtonGroup = new javax.swing.ButtonGroup();
        energySaveButtonGroup = new javax.swing.ButtonGroup();
        iconLocationButtonGroup = new javax.swing.ButtonGroup();
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
        visibleCheckBox = new javax.swing.JCheckBox();
        invertBitmapCheckBox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel30 = new javax.swing.JLabel();
        installHDDRadioButton = new javax.swing.JRadioButton();
        installSDVMRadioButton = new javax.swing.JRadioButton();
        installOtherSDRadioButton = new javax.swing.JRadioButton();
        installLocationComboBox = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        workingDirHDDRadioButton = new javax.swing.JRadioButton();
        workingDirSDVMRadioButton = new javax.swing.JRadioButton();
        workingDirOtherSDRadioButton = new javax.swing.JRadioButton();
        workingDirComboBox = new javax.swing.JComboBox();
        disableDalpManagementCheckBox = new javax.swing.JCheckBox();
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

        jLabel15.setFont(jLabel15.getFont().deriveFont(jLabel15.getFont().getStyle() | java.awt.Font.BOLD, jLabel15.getFont().getSize()+1));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel15, org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "LBL_AdvancedOptions")); // NOI18N

        jLabel18.setLabelFor(dalpVersionTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/cdc/project/ricoh/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel18, bundle.getString("LBL_DalpVersion")); // NOI18N

        dalpVersionTextField.setNextFocusableComponent(codeBaseTextField);

        codeBaseTextField.setNextFocusableComponent(useAbbreviationCheckBox);

        jLabel14.setLabelFor(codeBaseTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel14, bundle.getString("LBL_CodebaseURL")); // NOI18N

        jLabel22.setFont(jLabel22.getFont().deriveFont(jLabel22.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel22, bundle.getString("LBL_IconOptions:")); // NOI18N

        iconPathOrAbbrevLabel.setLabelFor(abbrevationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(iconPathOrAbbrevLabel, bundle.getString("LBL_Abbreviation")); // NOI18N
        iconPathOrAbbrevLabel.setEnabled(false);

        abbrevationTextField.setEnabled(false);
        abbrevationTextField.setNextFocusableComponent(startupArgumentsTextField);

        org.openide.awt.Mnemonics.setLocalizedText(useAbbreviationCheckBox, bundle.getString("LBL_UseAbbrevCheckBox")); // NOI18N
        useAbbreviationCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useAbbreviationCheckBox.setNextFocusableComponent(invertBitmapCheckBox);
        useAbbreviationCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useAbbreviationCheckBoxActionPerformed(evt);
            }
        });

        jLabel28.setFont(jLabel28.getFont().deriveFont(jLabel28.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel28.setLabelFor(startupArgumentsTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel28, bundle.getString("LBL_StartupArgs")); // NOI18N

        startupArgumentsTextField.setNextFocusableComponent(installHDDRadioButton);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel16.setFont(jLabel16.getFont().deriveFont(jLabel16.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel16, bundle.getString("LBL_DalpSpecification")); // NOI18N

        dalpVersionSpecButtonGroup.add(dalpSpecSDKJ1RadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(dalpSpecSDKJ1RadioButton, bundle.getString("LBL_SDKJ_Spec_1_0")); // NOI18N
        dalpSpecSDKJ1RadioButton.setActionCommand("1.0"); // NOI18N
        dalpSpecSDKJ1RadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dalpSpecSDKJ1RadioButton.setNextFocusableComponent(dalpSpecSDKJ2RadioButton);
        dalpSpecSDKJ1RadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dalpSpecSDKJ1RadioButtonActionPerformed(evt);
            }
        });

        dalpVersionSpecButtonGroup.add(dalpSpecSDKJ2RadioButton);
        dalpSpecSDKJ2RadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(dalpSpecSDKJ2RadioButton, bundle.getString("LBL_SDKJ_Spec_2_0")); // NOI18N
        dalpSpecSDKJ2RadioButton.setActionCommand("2.0"); // NOI18N
        dalpSpecSDKJ2RadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dalpSpecSDKJ2RadioButton.setNextFocusableComponent(guestRadioButton);
        dalpSpecSDKJ2RadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dalpSpecSDKJ2RadioButtonActionPerformed(evt);
            }
        });

        jLabel25.setFont(jLabel25.getFont().deriveFont(jLabel25.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel25, bundle.getString("LBL_ApplicationExecution")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel27, bundle.getString("LBL_ExecLvl")); // NOI18N

        execLevelButtonGroup.add(guestRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(guestRadioButton, bundle.getString("LBL_AppExecLvlGuest")); // NOI18N
        guestRadioButton.setActionCommand("guest"); // NOI18N
        guestRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        guestRadioButton.setNextFocusableComponent(adminRadioButton);

        execLevelButtonGroup.add(adminRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(adminRadioButton, bundle.getString("LBL_AppExecLvlAdmin")); // NOI18N
        adminRadioButton.setActionCommand("admin"); // NOI18N
        adminRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        adminRadioButton.setNextFocusableComponent(hvgaCheckBox);

        jLabel29.setFont(jLabel29.getFont().deriveFont(jLabel29.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel29, bundle.getString("LBL_DisplayModes")); // NOI18N

        hvgaCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(hvgaCheckBox, bundle.getString("LBL_HVGA")); // NOI18N
        hvgaCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hvgaCheckBox.setNextFocusableComponent(vgaCheckBox);

        org.openide.awt.Mnemonics.setLocalizedText(vgaCheckBox, bundle.getString("LBL_VGA")); // NOI18N
        vgaCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vgaCheckBox.setNextFocusableComponent(wvgaCheckBox);

        org.openide.awt.Mnemonics.setLocalizedText(wvgaCheckBox, bundle.getString("LBL_WVGA")); // NOI18N
        wvgaCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        wvgaCheckBox.setNextFocusableComponent(lcdCheckBox);

        org.openide.awt.Mnemonics.setLocalizedText(lcdCheckBox, bundle.getString("LBL_4LineLED")); // NOI18N
        lcdCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        lcdCheckBox.setNextFocusableComponent(autoRunCheckBox);

        org.openide.awt.Mnemonics.setLocalizedText(autoRunCheckBox, bundle.getString("LBL_EnableAutoRun")); // NOI18N
        autoRunCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        autoRunCheckBox.setNextFocusableComponent(autoInstallCheckBox);

        autoInstallCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(autoInstallCheckBox, bundle.getString("LBL_EnableAutoInstall")); // NOI18N
        autoInstallCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        autoInstallCheckBox.setNextFocusableComponent(colorEnableCheckBox);

        org.openide.awt.Mnemonics.setLocalizedText(colorEnableCheckBox, bundle.getString("LBL_EnableColorDisplay")); // NOI18N
        colorEnableCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        colorEnableCheckBox.setNextFocusableComponent(visibleCheckBox);

        visibleCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(visibleCheckBox, bundle.getString("LBL_Visible")); // NOI18N
        visibleCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        invertBitmapCheckBox.setText(bundle.getString("LBL_InvertIconBitmap")); // NOI18N
        invertBitmapCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        invertBitmapCheckBox.setNextFocusableComponent(abbrevationTextField);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel30.setFont(jLabel30.getFont().deriveFont(jLabel30.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel30, bundle.getString("LBL_InstallLocation")); // NOI18N

        installLocationButtonGroup.add(installHDDRadioButton);
        installHDDRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(installHDDRadioButton, bundle.getString("LBL_InstallLocHDD")); // NOI18N
        installHDDRadioButton.setActionCommand("hdd"); // NOI18N
        installHDDRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        installHDDRadioButton.setNextFocusableComponent(installSDVMRadioButton);

        installLocationButtonGroup.add(installSDVMRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(installSDVMRadioButton, bundle.getString("LBL_InstallLocVM")); // NOI18N
        installSDVMRadioButton.setActionCommand("sdcard"); // NOI18N
        installSDVMRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        installSDVMRadioButton.setNextFocusableComponent(installOtherSDRadioButton);

        installLocationButtonGroup.add(installOtherSDRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(installOtherSDRadioButton, bundle.getString("LBL_InstallLocOther")); // NOI18N
        installOtherSDRadioButton.setActionCommand("sdcard0"); // NOI18N
        installOtherSDRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        installLocationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SD Card Slot 1", "SD Card Slot 2", "SD Card Slot 3" }));
        installLocationComboBox.setNextFocusableComponent(workingDirHDDRadioButton);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel30)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(installOtherSDRadioButton)
                        .addGap(6, 6, 6)
                        .addComponent(installLocationComboBox, 0, 141, Short.MAX_VALUE))
                    .addComponent(installHDDRadioButton)
                    .addComponent(installSDVMRadioButton))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel30)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(installHDDRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(installSDVMRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(installOtherSDRadioButton)
                    .addComponent(installLocationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {installHDDRadioButton, installSDVMRadioButton});

        jLabel30.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_InstallLocation")); // NOI18N
        jLabel30.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_InstallLocation")); // NOI18N
        installHDDRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_InstallLocHDD")); // NOI18N
        installHDDRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_InstallLocHDD")); // NOI18N
        installSDVMRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_InstallLocVM")); // NOI18N
        installSDVMRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_InstallLocVM")); // NOI18N
        installOtherSDRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_InstallLocOther")); // NOI18N
        installOtherSDRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_InstallLocOther")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jPanel2, gridBagConstraints);

        jLabel31.setFont(jLabel31.getFont().deriveFont(jLabel31.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel31, bundle.getString("LBL_WorkingDir")); // NOI18N

        workingDirectoryButtonGroup.add(workingDirHDDRadioButton);
        workingDirHDDRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(workingDirHDDRadioButton, bundle.getString("LBL_WorkingDirHDD")); // NOI18N
        workingDirHDDRadioButton.setActionCommand("hdd"); // NOI18N
        workingDirHDDRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        workingDirHDDRadioButton.setNextFocusableComponent(workingDirSDVMRadioButton);

        workingDirectoryButtonGroup.add(workingDirSDVMRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(workingDirSDVMRadioButton, bundle.getString("LBL_WorkingDirVM")); // NOI18N
        workingDirSDVMRadioButton.setActionCommand("sdcard"); // NOI18N
        workingDirSDVMRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        workingDirSDVMRadioButton.setNextFocusableComponent(workingDirOtherSDRadioButton);

        workingDirectoryButtonGroup.add(workingDirOtherSDRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(workingDirOtherSDRadioButton, bundle.getString("LBL_WorkingDirOther")); // NOI18N
        workingDirOtherSDRadioButton.setActionCommand("sdcard0"); // NOI18N
        workingDirOtherSDRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        workingDirComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SD Card Slot 1", "SD Card Slot 2", "SD Card Slot 3" }));
        workingDirComboBox.setNextFocusableComponent(dalpSpecSDKJ1RadioButton);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel31)
                    .addComponent(workingDirHDDRadioButton)
                    .addComponent(workingDirSDVMRadioButton)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(workingDirOtherSDRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(workingDirComboBox, 0, 132, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel31)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(workingDirHDDRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(workingDirSDVMRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(workingDirOtherSDRadioButton)
                    .addComponent(workingDirComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jLabel31.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_WorkingDir")); // NOI18N
        jLabel31.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_WorkingDir")); // NOI18N
        workingDirHDDRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_WorkingDirHDD")); // NOI18N
        workingDirHDDRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_WorkingDirHDD")); // NOI18N
        workingDirSDVMRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_WorkingDirVM")); // NOI18N
        workingDirSDVMRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_WorkingDirVM")); // NOI18N
        workingDirOtherSDRadioButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_WorkingDirOther")); // NOI18N
        workingDirOtherSDRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_WorkingDirOther")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jPanel3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(disableDalpManagementCheckBox, bundle.getString("LBL_DalpManagement")); // NOI18N
        disableDalpManagementCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        disableDalpManagementCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disableDalpManagementCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout advancedOptionConfigPanelLayout = new javax.swing.GroupLayout(advancedOptionConfigPanel);
        advancedOptionConfigPanel.setLayout(advancedOptionConfigPanelLayout);
        advancedOptionConfigPanelLayout.setHorizontalGroup(
            advancedOptionConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, advancedOptionConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(advancedOptionConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel22)
                    .addComponent(jLabel28)
                    .addGroup(advancedOptionConfigPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(advancedOptionConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(advancedOptionConfigPanelLayout.createSequentialGroup()
                                .addComponent(iconPathOrAbbrevLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(abbrevationTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE))
                            .addGroup(advancedOptionConfigPanelLayout.createSequentialGroup()
                                .addComponent(useAbbreviationCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(invertBitmapCheckBox)
                                .addGap(117, 117, 117))))
                    .addGroup(advancedOptionConfigPanelLayout.createSequentialGroup()
                        .addGroup(advancedOptionConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addComponent(jLabel18))
                        .addGap(41, 41, 41)
                        .addGroup(advancedOptionConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(advancedOptionConfigPanelLayout.createSequentialGroup()
                                .addComponent(dalpVersionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(259, 259, 259))
                            .addComponent(codeBaseTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)))
                    .addComponent(startupArgumentsTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
                    .addComponent(disableDalpManagementCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedOptionConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(visibleCheckBox)
                    .addGroup(advancedOptionConfigPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(advancedOptionConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dalpSpecSDKJ1RadioButton)
                            .addComponent(dalpSpecSDKJ2RadioButton)))
                    .addComponent(jLabel16)
                    .addComponent(jLabel25)
                    .addComponent(jLabel27)
                    .addGroup(advancedOptionConfigPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(advancedOptionConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(adminRadioButton)
                            .addComponent(guestRadioButton)))
                    .addComponent(jLabel29)
                    .addComponent(autoRunCheckBox)
                    .addGroup(advancedOptionConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(autoInstallCheckBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, advancedOptionConfigPanelLayout.createSequentialGroup()
                            .addGap(10, 10, 10)
                            .addGroup(advancedOptionConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(vgaCheckBox)
                                .addComponent(hvgaCheckBox)
                                .addComponent(wvgaCheckBox)
                                .addComponent(lcdCheckBox))))
                    .addComponent(colorEnableCheckBox))
                .addGap(35, 35, 35))
        );
        advancedOptionConfigPanelLayout.setVerticalGroup(
            advancedOptionConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedOptionConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(advancedOptionConfigPanelLayout.createSequentialGroup()
                    .addComponent(jLabel15)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(advancedOptionConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel18)
                        .addComponent(dalpVersionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(advancedOptionConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel14)
                        .addComponent(codeBaseTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel22)
                    .addGap(6, 6, 6)
                    .addGroup(advancedOptionConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(useAbbreviationCheckBox)
                        .addComponent(invertBitmapCheckBox))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(advancedOptionConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(iconPathOrAbbrevLabel)
                        .addComponent(abbrevationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel28)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(startupArgumentsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(disableDalpManagementCheckBox)
                    .addContainerGap(18, Short.MAX_VALUE))
                .addGroup(advancedOptionConfigPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(advancedOptionConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
                        .addGroup(advancedOptionConfigPanelLayout.createSequentialGroup()
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(dalpSpecSDKJ1RadioButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(dalpSpecSDKJ2RadioButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel25)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel27)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(guestRadioButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(adminRadioButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel29)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(hvgaCheckBox)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(vgaCheckBox)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(wvgaCheckBox)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(lcdCheckBox)
                            .addGap(11, 11, 11)
                            .addComponent(autoRunCheckBox)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(autoInstallCheckBox)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(colorEnableCheckBox)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(visibleCheckBox)
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
        );

        advancedOptionConfigPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {abbrevationTextField, codeBaseTextField, dalpVersionTextField, jLabel14, jLabel22, useAbbreviationCheckBox});

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
        disableDalpManagementCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_DalpManagement")); // NOI18N
        disableDalpManagementCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_DalpManagement")); // NOI18N

        advancedOptionConfigPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_ProjectAdvancedCustomizerPanel")); // NOI18N
        advancedOptionConfigPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_ProjectAdvancedCustomizerPanel")); // NOI18N

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
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox1)
                    .addComponent(jLabel9)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(25, 25, 25)
                                .addComponent(applicationUID, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(22, 22, 22)
                                .addComponent(applicationIcon, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(changeApplicationUID, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                            .addComponent(browseApplicationIcon, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)))
                    .addComponent(jLabel7)
                    .addComponent(advancedButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4))
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(faxNumber, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                            .addComponent(telephoneNumber, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                            .addComponent(email, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(applicationUID, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(changeApplicationUID))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(applicationIcon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseApplicationIcon))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(telephoneNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(faxNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(advancedButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {email, faxNumber, telephoneNumber});

        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_ApplicationUID")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_ApplicationUID")); // NOI18N
        applicationUID.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_applicationUID")); // NOI18N
        changeApplicationUID.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_ChangeUID")); // NOI18N
        changeApplicationUID.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_ChangeUID")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_ApplicationIcon")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_ApplicationIcon")); // NOI18N
        browseApplicationIcon.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_BrowseIcon")); // NOI18N
        browseApplicationIcon.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_BrowseIcon")); // NOI18N
        applicationIcon.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_applicationIcon")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_PhoneNumber")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_PhoneNumber")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_FaxNumber")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_FaxNumber")); // NOI18N
        jLabel5.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_Email")); // NOI18N
        jLabel5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_Email")); // NOI18N
        telephoneNumber.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_telephoneNumber")); // NOI18N
        faxNumber.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_faxNumber")); // NOI18N
        email.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_email")); // NOI18N
        advancedButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_BUTTON_Advanced")); // NOI18N
        advancedButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_BUTTON_Advanced")); // NOI18N
        jCheckBox1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_UseDefault")); // NOI18N
        jCheckBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_UseDefault")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_ProjectCustomizerPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_ProjectCustomizerPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void postInit() {
        installLocationComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_installLocationComboBox")); //NOI18N
        installLocationComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_installLocationComboBox")); //NOI18N
        workingDirComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_workingDirComboBox")); //NOI18N
        workingDirComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_workingDirComboBox")); //NOI18N
    }
    
    private void dalpSpecSDKJ2RadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dalpSpecSDKJ2RadioButtonActionPerformed
// TODO add your handling code here:
        colorEnableCheckBox.setEnabled(true);
        this.hvgaCheckBox.setEnabled(true);
        this.vgaCheckBox.setEnabled(true);
        this.wvgaCheckBox.setEnabled(true);
        this.lcdCheckBox.setEnabled(true);
        this.workingDirHDDRadioButton.setEnabled(true);
        this.workingDirOtherSDRadioButton.setEnabled(true);        
        this.workingDirSDVMRadioButton.setEnabled(true);
        if (workingDirOtherSDRadioButton.isSelected())
            this.workingDirComboBox.setEnabled(true);
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
        this.workingDirComboBox.setEnabled(false);
        this.workingDirSDVMRadioButton.setEnabled(false);
    }//GEN-LAST:event_dalpSpecSDKJ1RadioButtonActionPerformed

    private void advancedButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_advancedButtonActionPerformed
    {//GEN-HEADEREND:event_advancedButtonActionPerformed
// TODO add your handling code here:
        visibleCheckBox.setEnabled(false);
        if (disableDalpManagementCheckBox.isSelected() == false) {
            visibleCheckBox.setEnabled(true);
        } else {
            visibleCheckBox.setEnabled(false);
        }
        JButton closingOption = new JButton(NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "LBL_ButtonAdvancedClose"));
        closingOption.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSN_ButtonAdvancedClose"));
        closingOption.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "ACSD_ButtonAdvancedClose"));
        final DialogDescriptor dd = new DialogDescriptor(advancedOptionConfigPanel, NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "TITLE_AdvancedOptionsDialog"), true, new Object[]{closingOption}, closingOption, DialogDescriptor.DEFAULT_ALIGN, HelpCtx.DEFAULT_HELP, null); //NOI18N
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        //this.advancedOptionsDialog.setTitle(NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "TITLE_AdvancedOptionsDialog")); //NOI18N
        //this.advancedOptionsDialog.pack();
    }//GEN-LAST:event_advancedButtonActionPerformed

    private void changeApplicationUIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeApplicationUIDActionPerformed
        RicohApplicationUIDCustomizer customizer = new RicohApplicationUIDCustomizer(applicationUID.getText(), platformDir);
        final DialogDescriptor dd = new DialogDescriptor(customizer, NbBundle.getMessage(RicohProjectCategoryCustomizer.class, "TITLE_ChangeUID")); //NOI18N
        customizer.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (NotifyDescriptor.PROP_VALID.equals(evt.getPropertyName())) {
                    dd.setValid(((Boolean) evt.getNewValue()).booleanValue());
                }
            }
        });
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            applicationUID.setText(customizer.getUID());
        }
    }//GEN-LAST:event_changeApplicationUIDActionPerformed

    private void browseApplicationIconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseApplicationIconActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new FileFilter() {

            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String ext = f.toString().toLowerCase();
                int i = ext.lastIndexOf('.');
                if (i != -1) {
                    ext = ext.substring(i + 1);
                }
                if (ext.equalsIgnoreCase("bmp")) {
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
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            //NOI18N
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            applicationIcon.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_browseApplicationIconActionPerformed

private void useAbbreviationCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useAbbreviationCheckBoxActionPerformed
    if (useAbbreviationCheckBox.isSelected()) {
        this.abbrevationTextField.setEnabled(true);
        this.applicationIcon.setEnabled(false);
        this.browseApplicationIcon.setEnabled(false);
        this.invertBitmapCheckBox.setEnabled(false);
        iconPathOrAbbrevLabel.setEnabled(true);
    } else {
        this.abbrevationTextField.setEnabled(false);
        this.applicationIcon.setEnabled(true);
        this.browseApplicationIcon.setEnabled(true);
        this.invertBitmapCheckBox.setEnabled(true);
        iconPathOrAbbrevLabel.setEnabled(false);
    }
}//GEN-LAST:event_useAbbreviationCheckBoxActionPerformed

private void disableDalpManagementCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableDalpManagementCheckBoxActionPerformed

    panelEnabler(!disableDalpManagementCheckBox.isSelected(), advancedOptionConfigPanel);
    if (!disableDalpManagementCheckBox.isSelected()) {
        updateLocationsSelection();
        //checkboxes (which auto check state before setting ant properties)
        this.useAbbreviationCheckBoxActionPerformed(evt);
    }
}//GEN-LAST:event_disableDalpManagementCheckBoxActionPerformed

    private void panelEnabler(boolean enable, Container comp){
        Component[] compArr = comp.getComponents();
        for (int i = 0; i < compArr.length; i++) {
            if (compArr[i] != disableDalpManagementCheckBox){
                compArr[i].setEnabled(enable);
            }
            if (compArr[i] instanceof Container && ((Container)compArr[i]).getComponentCount() != 0){
                panelEnabler(enable, ((Container)compArr[i]));
            }
        }
        
        if (enable){
            if (dalpSpecSDKJ1RadioButton.isSelected()) {
                dalpSpecSDKJ1RadioButtonActionPerformed(null);
            }
            if (dalpSpecSDKJ2RadioButton.isSelected()) {
                dalpSpecSDKJ2RadioButtonActionPerformed(null);
            }            
        }
    }

    private void startListening(){
        installLocationComboBox.addActionListener(aListener);
        workingDirComboBox.addActionListener(aListener);
        installOtherSDRadioButton.addActionListener(aListener);
        installSDVMRadioButton.addActionListener(aListener);
        installHDDRadioButton.addActionListener(aListener);
        workingDirOtherSDRadioButton.addActionListener(aListener);
        workingDirSDVMRadioButton.addActionListener(aListener);   
        workingDirHDDRadioButton.addActionListener(aListener);        
    }

    private void stopListening(){
        installLocationComboBox.removeActionListener(aListener);
        workingDirComboBox.removeActionListener(aListener);
        installOtherSDRadioButton.removeActionListener(aListener);
        installSDVMRadioButton.removeActionListener(aListener);
        installHDDRadioButton.removeActionListener(aListener);
        workingDirOtherSDRadioButton.removeActionListener(aListener);
        workingDirSDVMRadioButton.removeActionListener(aListener);   
        workingDirHDDRadioButton.removeActionListener(aListener);        
    }
    
    private ActionListener aListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            stopListening();
            Object source = e.getSource();
            if (source.equals(installLocationComboBox)){
                installLocationTextField.setText("sdcard" + (installLocationComboBox.getSelectedIndex() + 1)); //NOI18N
            } else if (source.equals(workingDirComboBox)) {
                workingDirTextField.setText("sdcard" + (workingDirComboBox.getSelectedIndex() + 1)); //NOI18N
            } else if (source.equals(installOtherSDRadioButton)) {
                installLocationComboBox.setEnabled(true);
                installLocationTextField.setText("sdcard" + (installLocationComboBox.getSelectedIndex() + 1)); //NOI18N
            } else if (source.equals(installSDVMRadioButton)) {
                installLocationComboBox.setEnabled(false);
                installLocationTextField.setText("sdcard");
            } else if (source.equals(installHDDRadioButton)) {
                installLocationComboBox.setEnabled(false);
                installLocationTextField.setText("hdd");
            } else if (source.equals(workingDirOtherSDRadioButton)) {
                workingDirComboBox.setEnabled(true);
                workingDirTextField.setText("sdcard" + (workingDirComboBox.getSelectedIndex() + 1)); //NOI18N                
            } else if (source.equals(workingDirSDVMRadioButton)) {
                workingDirComboBox.setEnabled(false);
                workingDirTextField.setText("sdcard");
            } else if (source.equals(workingDirHDDRadioButton)) {
                workingDirComboBox.setEnabled(false);
                workingDirTextField.setText("hdd");
            } 
            startListening();
        }
    };
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField abbrevationTextField;
    private javax.swing.JRadioButton adminRadioButton;
    private javax.swing.JButton advancedButton;
    private javax.swing.JPanel advancedOptionConfigPanel;
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
    private javax.swing.JComboBox installLocationComboBox;
    private javax.swing.JRadioButton installOtherSDRadioButton;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBox lcdCheckBox;
    private javax.swing.JTextField startupArgumentsTextField;
    private javax.swing.JTextField telephoneNumber;
    private javax.swing.JCheckBox useAbbreviationCheckBox;
    private javax.swing.JCheckBox vgaCheckBox;
    private javax.swing.JCheckBox visibleCheckBox;
    private javax.swing.JComboBox workingDirComboBox;
    private javax.swing.JRadioButton workingDirHDDRadioButton;
    private javax.swing.JRadioButton workingDirOtherSDRadioButton;
    private javax.swing.JRadioButton workingDirSDVMRadioButton;
    private javax.swing.ButtonGroup workingDirectoryButtonGroup;
    private javax.swing.JCheckBox wvgaCheckBox;
    // End of variables declaration//GEN-END:variables
}

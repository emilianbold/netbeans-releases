/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2013 Sun
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
package org.netbeans.modules.j2me.project.wizard;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.modules.j2me.project.J2MEProjectUtils;
import org.netbeans.modules.j2me.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.j2me.project.wizard.J2MEProjectWizardIterator.WizardType;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.modules.SpecificationVersion;
import org.openide.util.*;

/**
 * @author Theofanis Oikonomou
 */
public class PlatformDevicesPanel extends SettingsPanel {
    
    private final WizardType type;
    private PanelConfigureProject panel;
    private ComboBoxModel platformsModel;
    private DefaultComboBoxModel devicesModel;
    private JavaPlatformChangeListener jpcl;
    private ButtonGroup configurationsGroup;
    private ButtonGroup profilesGroup;
    private final HashMap<String, J2MEPlatform.J2MEProfile> name2profile = new HashMap<>();
    private int firstConfigurationWidth = -1;
    private ArrayList<JCheckBox> optionalPackages;
    private J2MEProjectProperties props;    

    public PlatformDevicesPanel(J2MEProjectProperties properties) {
        this.props = properties;
        this.panel = null;
        this.type = null;
        initialize();
    }

    PlatformDevicesPanel(PanelConfigureProject panel, WizardType type) {
        this.panel = panel;
        this.type = type;
        initialize();
    }

    private void initialize() {
        preInitComponents();
        initComponents();
        postInitComponents();
    }

    private void preInitComponents() {
        platformsModel = J2MEProjectUtils.createPlatformComboBoxModel();
        devicesModel = new DefaultComboBoxModel();
        configurationsGroup = J2MEProjectUtils.getConfigurationsButtonGroup();
        profilesGroup = J2MEProjectUtils.getProfilesButtonGroup();
        optionalPackages = J2MEProjectUtils.getOptionalPackages();
    }

    private void postInitComponents() {
        if (panel != null && type != null) {
            lblOptionalPackages.setVisible(false);
            jScrollPane1.setVisible(false);
        }
        initJdkPlatform();
        initPlatformAndDevices();        

        jpcl = new JavaPlatformChangeListener();
        JavaPlatformManager.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(jpcl, JavaPlatformManager.getDefault()));
    }

    private void initJdkPlatform() {
        jdkComboBox.setRenderer(J2MEProjectUtils.createJDKPlatformListCellRenderer());
        ComboBoxModel jdkCompoboxModel = J2MEProjectUtils.createJDKPlatformComboBoxModel();
//        if (jdkCompoboxModel.getSize() == 1) {
//            //If the only element in model is Default J2SE 1.7 Platform, do not use it
//            final JavaPlatform javaPlatform = PlatformUiSupport.getPlatform(jdkCompoboxModel.getSelectedItem());
//            if (javaPlatform.getSpecification().getVersion().compareTo(new SpecificationVersion("1.8")) < 0) { //NOI18N
//                jdkCompoboxModel = new DefaultComboBoxModel();
//            }
//        }
        jdkComboBox.setModel(jdkCompoboxModel);
    }

    private void initPlatformAndDevices() {        
        J2MEPlatform platform = getPlatform();
        platformComboBox.setModel(platformsModel);
        // copied from CustomizerLibraries
        if (!UIManager.getLookAndFeel().getClass().getName().toUpperCase().contains("AQUA")) {  //NOI18N
            platformComboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE); // NOI18N
            deviceComboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE); // NOI18N
        }
        initDeviceConfigurations();
        initDeviceProfiles();
        initOptionalPackages();
        if (platform == null) {
            if (platformComboBox.getItemCount() > 0) {
                platformComboBox.setSelectedIndex(0);
            }
        } else {
            boolean found = false;
            for (int i = 0; i < platformComboBox.getItemCount(); i++) {
                J2MEPlatform platf = (J2MEPlatform) platformComboBox.getItemAt(i);
                if (platf.getDisplayName().equals(platform.getDisplayName())) {
                    platformComboBox.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
            if (!found && platformComboBox.getItemCount() > 0) {
                platformComboBox.setSelectedIndex(0);
            }
        }
        deviceComboBox.setModel(devicesModel);
    }

    private void initDeviceConfigurations() {
        configurationPanel.removeAll();
        Enumeration<AbstractButton> configurations = configurationsGroup.getElements();
        while (configurations.hasMoreElements()) {
            JRadioButton btn = (JRadioButton) configurations.nextElement();
            configurationPanel.add(btn);
            final Dimension preferredSize = btn.getPreferredSize();
            if (firstConfigurationWidth == -1) {
                firstConfigurationWidth = preferredSize.width + 30;
                btn.setPreferredSize(new java.awt.Dimension(firstConfigurationWidth, preferredSize.height));
            } else {
                btn.setPreferredSize(new java.awt.Dimension(firstConfigurationWidth, preferredSize.height));
            }
        }
    }

    private void initDeviceProfiles() {
        profilePanel.removeAll();
        Enumeration<AbstractButton> profiles = profilesGroup.getElements();
        while (profiles.hasMoreElements()) {
            JRadioButton btn = (JRadioButton) profiles.nextElement();
            btn.setPreferredSize(new java.awt.Dimension(firstConfigurationWidth, btn.getPreferredSize().height));
            profilePanel.add(btn);
        }
    }

    private void initOptionalPackages() {
        String platformApis = null;
        if(props != null) {
            platformApis = props.getEvaluator().getProperty(J2MEProjectProperties.PLATFORM_APIS);
        }
        if(platformApis == null) {
            platformApis = "";
        }
        optionalPackagesPanel.removeAll();
        for (JCheckBox cb : optionalPackages) {
            if(platformApis.contains(cb.getActionCommand())) {
                cb.setSelected(true);
            }
            optionalPackagesPanel.add(cb, new GridBagConstraints(0, GridBagConstraints.RELATIVE, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        }
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                props.putAdditionalProperty(J2MEProjectProperties.PLATFORM_APIS, getOptionalAPI());
            }
        };
        for (JCheckBox cb : optionalPackages) {
            cb.addActionListener(actionListener);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblPlatform = new javax.swing.JLabel();
        platformComboBox = new javax.swing.JComboBox();
        btnManagePlatforms = new javax.swing.JButton();
        lblDevice = new javax.swing.JLabel();
        deviceComboBox = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        lblConfiguration = new javax.swing.JLabel();
        configurationPanel = new javax.swing.JPanel();
        lblProfile = new javax.swing.JLabel();
        profilePanel = new javax.swing.JPanel();
        lblOptionalPackages = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        optionalPackagesPanel = new javax.swing.JPanel();
        lblJDKPath = new javax.swing.JLabel();
        jdkComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        lblPlatform.setLabelFor(platformComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(lblPlatform, org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "LBL_PanelOptions_Platform_ComboBox")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(lblPlatform, gridBagConstraints);
        lblPlatform.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "ACSN_labelPlatform")); // NOI18N
        lblPlatform.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "ACSD_labelPlatform")); // NOI18N

        platformComboBox.setModel(platformsModel);
        platformComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                platformComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        add(platformComboBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btnManagePlatforms, org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "LBL_PanelOptions_Manage_Button")); // NOI18N
        btnManagePlatforms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnManagePlatformsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(btnManagePlatforms, gridBagConstraints);
        btnManagePlatforms.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "ACSN_buttonManagePlatforms")); // NOI18N
        btnManagePlatforms.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "ACSD_buttonManagePlatforms")); // NOI18N

        lblDevice.setLabelFor(deviceComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(lblDevice, org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "LBL_PanelOptions_Device_ComboBox")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(lblDevice, gridBagConstraints);
        lblDevice.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "ACSN_labelDevice")); // NOI18N
        lblDevice.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "ACSD_labelDevice")); // NOI18N

        deviceComboBox.setModel(devicesModel);
        deviceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deviceComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        add(deviceComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 13, 0);
        add(jSeparator1, gridBagConstraints);

        lblConfiguration.setLabelFor(configurationPanel);
        org.openide.awt.Mnemonics.setLocalizedText(lblConfiguration, org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "LBL_PanelOptions_Configuration_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(lblConfiguration, gridBagConstraints);
        lblConfiguration.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "ACSN_labelConfiguration")); // NOI18N
        lblConfiguration.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "ACSD_labelConfiguration")); // NOI18N

        configurationPanel.setLayout(new java.awt.GridLayout(1, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 0);
        add(configurationPanel, gridBagConstraints);

        lblProfile.setLabelFor(profilePanel);
        org.openide.awt.Mnemonics.setLocalizedText(lblProfile, org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "LBL_PanelOptions_Profile_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        add(lblProfile, gridBagConstraints);
        lblProfile.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "ACSN_labelProfile")); // NOI18N
        lblProfile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "ACSD_labelProfile")); // NOI18N

        profilePanel.setLayout(new java.awt.GridLayout(1, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 12, 0);
        add(profilePanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(lblOptionalPackages, org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "J2MEPlatformPanel.lblOptionalPackages.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        add(lblOptionalPackages, gridBagConstraints);

        optionalPackagesPanel.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(optionalPackagesPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        lblJDKPath.setLabelFor(jdkComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(lblJDKPath, org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "LBL_PanelOptions_JDkPath_ComboBox")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        add(lblJDKPath, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(jdkComboBox, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "ACSN_PanelOptionsVisual")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PlatformDevicesPanel.class, "ACSD_PanelOptionsVisual")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void deviceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deviceComboBoxActionPerformed
        updateConfigsAndProfiles();
    }//GEN-LAST:event_deviceComboBoxActionPerformed

    private void platformComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_platformComboBoxActionPerformed
        updateDevices();
    }//GEN-LAST:event_platformComboBoxActionPerformed

    private void btnManagePlatformsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManagePlatformsActionPerformed
        PlatformsCustomizer.showCustomizer(getPlatform());
        preInitComponents();
        initJdkPlatform();
        initPlatformAndDevices();
        if (panel != null) {
            panel.fireChangeEvent();
        }
    }//GEN-LAST:event_btnManagePlatformsActionPerformed

    private synchronized void updateDevices() {
        boolean embedded = false;
        final J2MEPlatform platform = getPlatform();
        String deviceName = null;
        devicesModel.setSelectedItem(null);
        devicesModel.removeAllElements();
        if (platform != null) {
            J2MEPlatform.Device select = null;
            final J2MEPlatform.Device devices[] = platform.getDevices();
            for (int i = 0; i < devices.length; i++) {
                final J2MEPlatform.J2MEProfile p[] = devices[i].getProfiles();
                for (int j = 0; j < p.length; j++) {
                    if (!embedded || (p[j].isDefault() && J2MEPlatform.J2MEProfile.TYPE_PROFILE.equals(p[j].getType()))) {
                        devicesModel.addElement(devices[i]);
                        if (devices[i].getName().equals(deviceName)) {
                            devicesModel.setSelectedItem(devices[i]);
                        }
                        if (select == null) {
                            select = devices[i];
                        }
                        j = p.length;
                    }
                }

            }
            if (embedded && select == null) { // NOI18N
                // no NG embedded support in this SDK
                for (int i = 0; i < devices.length; i++) {
                    final J2MEPlatform.J2MEProfile p[] = devices[i].getProfiles();
                    for (int j = 0; j < p.length; j++) {
                        if (p[j].isDefault() && J2MEPlatform.J2MEProfile.TYPE_PROFILE.equals(p[j].getType())
                                && p[j].getName().equals("IMP")) {
                            devicesModel.addElement(devices[i]);
                            if (devices[i].getName().equals(deviceName)) {
                                devicesModel.setSelectedItem(devices[i]);
                            }
                            if (select == null) {
                                select = devices[i];
                            }
                            j = p.length;
                        }
                    }

                }
            }
            if (devicesModel.getSelectedItem() == null && select != null) {
                devicesModel.setSelectedItem(select);
            }
        }
        updateConfigsAndProfiles();
        updateOptionalPackages();
    }

    private synchronized void updateConfigsAndProfiles() {
        final J2MEPlatform.Device device = getDevice();
        String defCfg = null, defProf = null;
        name2profile.clear();
        if (device != null) {
            final J2MEPlatform.J2MEProfile p[] = device.getProfiles();
            for (int i = 0; i < p.length; i++) {
                name2profile.put(p[i].toString(), p[i]);
                if (p[i].isDefault()) {
                    if (J2MEPlatform.J2MEProfile.TYPE_CONFIGURATION.equals(p[i].getType())) {
                        defCfg = p[i].toString();
                    } else if (J2MEPlatform.J2MEProfile.TYPE_PROFILE.equals(p[i].getType())) {
                        defProf = p[i].toString();
                    }
                }
            }
        }
        updateGroup(configurationsGroup, name2profile.keySet(), defCfg);
        updateGroup(profilesGroup, name2profile.keySet(), defProf);
    }

    private synchronized void updateOptionalPackages() {
        final J2MEPlatform.Device device = getDevice();
        String defCfg = null, defProf = null;
        name2profile.clear();
        if (device != null) {
            final J2MEPlatform.J2MEProfile p[] = device.getProfiles();
            for (int i = 0; i < p.length; i++) {
                name2profile.put(p[i].toString(), p[i]);
                if (p[i].isDefault()) {
                    if (J2MEPlatform.J2MEProfile.TYPE_CONFIGURATION.equals(p[i].getType())) {
                        defCfg = p[i].toString();
                    } else if (J2MEPlatform.J2MEProfile.TYPE_PROFILE.equals(p[i].getType())) {
                        defProf = p[i].toString();
                    }
                }
            }
        }
        updateGroup(configurationsGroup, name2profile.keySet(), defCfg);
        updateGroup(profilesGroup, name2profile.keySet(), defProf);
    }

    private void updateGroup(final ButtonGroup grp, final Set<String> enabled, final String def) {
        final Enumeration en = grp.getElements();
        JRadioButton defB = null;
        while (en.hasMoreElements()) {
            final JRadioButton btn = (JRadioButton) en.nextElement();
            final String name = btn.getActionCommand();
            btn.setEnabled(enabled.contains(name));
            if (def != null && def.equals(name)) {
                defB = btn;
            }
        }
        final ButtonModel m = grp.getSelection();
        if ((m == null || !m.isEnabled()) && defB != null) {
            grp.setSelected(defB.getModel(), true);
        }
    }

    private J2MEPlatform getPlatform() {
        return (J2MEPlatform) platformComboBox.getSelectedItem();
    }

    private J2MEPlatform.Device getDevice() {
        return (J2MEPlatform.Device) devicesModel.getSelectedItem();
    }

    private String getConfiguration() {
        final ButtonModel m = configurationsGroup.getSelection();
        return m == null ? "" : m.getActionCommand();
    }

    private String getProfile() {
        final ButtonModel m = profilesGroup.getSelection();
        return m == null ? "" : m.getActionCommand();
    }
    
    private JavaPlatform getJdkPlatform() {
        return PlatformUiSupport.getPlatform(jdkComboBox.getSelectedItem());
    }

    private void setBottomPanelAreaVisible(boolean visible) {
        jSeparator1.setVisible(visible);
        lblDevice.setVisible(visible);
        lblConfiguration.setVisible(visible);
        lblProfile.setVisible(visible);
        deviceComboBox.setVisible(visible);
        configurationPanel.setVisible(visible);
        profilePanel.setVisible(visible);
    }

    @Override
    boolean valid(WizardDescriptor settings) {
        //TODO: check whether at least one valid J2ME platform is available

        if (getPlatform() == null) {
            setBottomPanelAreaVisible(false);
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(PlatformDevicesPanel.class, "WARN_PanelOptionsVisual.noMEPlatform")); // NOI18N
            return false;
        }

        if (jdkComboBox.getSelectedItem() == null) {
            setBottomPanelAreaVisible(false);
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(PlatformDevicesPanel.class, "WARN_PanelOptionsVisual.noSEPlatform")); // NOI18N
            return false;
        } else {
            final Object item = jdkComboBox.getSelectedItem();
            if (item != null) {
                final JavaPlatform platform = PlatformUiSupport.getPlatform(item);
                if (platform.getSpecification().getVersion().compareTo(new SpecificationVersion("1.8")) < 0) { //NOI18N
                    settings.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                            NbBundle.getMessage(PlatformDevicesPanel.class, "WARN_PanelOptionsVisual.missingJDK")); // NOI18N
                }
            }
        }
        setBottomPanelAreaVisible(true);
        return true;
    }

    @Override
    synchronized void read(WizardDescriptor d) {
        String platform = d.getProperty(J2MEProjectWizardIterator.PLATFORM) != null
                ? ((J2MEPlatform) d.getProperty(J2MEProjectWizardIterator.PLATFORM)).getName()
                : getPreferences().get(J2MEProjectWizardIterator.PLATFORM, null);
        String device = (String) d.getProperty(J2MEProjectWizardIterator.DEVICE);
        if (device == null) {
            device = getPreferences().get(J2MEProjectWizardIterator.DEVICE, null);
        }
        String configuration = (String) d.getProperty(J2MEProjectWizardIterator.CONFIGURATION);
        if (configuration == null) {
            configuration = getPreferences().get(J2MEProjectWizardIterator.CONFIGURATION, null);
        }
        String profile = (String) d.getProperty(J2MEProjectWizardIterator.PROFILE);
        if (profile == null) {
            profile = getPreferences().get(J2MEProjectWizardIterator.PROFILE, null);
        }
        JavaPlatform jdkPlatform = (JavaPlatform) d.getProperty(J2MEProjectWizardIterator.JDK_PLATFORM);
        if (platform != null) {
            for (int i = 0; i < platformComboBox.getItemCount(); i++) {
                J2MEPlatform itemAt = (J2MEPlatform) platformComboBox.getItemAt(i);
                if (platform.equals(itemAt.getName())) {
                    platformComboBox.setSelectedItem(itemAt);
                }
            }
        }
        if (device != null) {
            for (int i = 0; i < deviceComboBox.getItemCount(); i++) {
                J2MEPlatform.Device itemAt = (J2MEPlatform.Device) deviceComboBox.getItemAt(i);
                if (device.equals(itemAt.getName())) {
                    deviceComboBox.setSelectedItem(itemAt);
                }
            }
        }
        if (configuration != null) {
            Enumeration<AbstractButton> configurations = configurationsGroup.getElements();
            while (configurations.hasMoreElements()) {
                JRadioButton btn = (JRadioButton) configurations.nextElement();
                if (btn.getActionCommand().equals(configuration)) {
                    configurationsGroup.setSelected(btn.getModel(), true);
                    break;
                }
            }
        }
        if (profile != null) {
            Enumeration<AbstractButton> profiles = profilesGroup.getElements();
            while (profiles.hasMoreElements()) {
                JRadioButton btn = (JRadioButton) profiles.nextElement();
                if (btn.getActionCommand().equals(profile)) {
                    profilesGroup.setSelected(btn.getModel(), true);
                    break;
                }
            }
        }
        if (jdkPlatform != null) {
            for (int i = 0; i < jdkComboBox.getItemCount(); i++) {
                JavaPlatform itemAt = PlatformUiSupport.getPlatform(jdkComboBox.getItemAt(i));
                if (itemAt.equals(jdkPlatform)) {
                    jdkComboBox.setSelectedItem(itemAt);
                    break;
                }
            }
        }
    }

    @Override
    void validate(WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }

    @Override
    void store(WizardDescriptor d) {
        d.putProperty(J2MEProjectWizardIterator.PLATFORM, platformComboBox.getSelectedIndex() == -1 ? "" : getPlatform());
        d.putProperty(J2MEProjectWizardIterator.DEVICE, deviceComboBox.getSelectedIndex() == -1 ? "" : getDevice().getName());
        d.putProperty(J2MEProjectWizardIterator.CONFIGURATION, getConfiguration());
        d.putProperty(J2MEProjectWizardIterator.PROFILE, getProfile());
        d.putProperty(J2MEProjectWizardIterator.JDK_PLATFORM, jdkComboBox.getSelectedIndex() == -1 ? "" : getJdkPlatform()); //This is enough to store in wizard iterator
        d.putProperty(J2MEProjectWizardIterator.OPTIONAL_API, getOptionalAPI()); //This is enough to store in wizard iterator

        getPreferences().put(J2MEProjectWizardIterator.PLATFORM, platformComboBox.getSelectedIndex() == -1 ? "" : getPlatform().getName());
        getPreferences().put(J2MEProjectWizardIterator.DEVICE, deviceComboBox.getSelectedIndex() == -1 ? "" : getDevice().getName());
        getPreferences().put(J2MEProjectWizardIterator.CONFIGURATION, getConfiguration());
        getPreferences().put(J2MEProjectWizardIterator.PROFILE, getProfile());
    }
    
    private String getOptionalAPI() {
        StringBuilder sb = new StringBuilder();
        for (JCheckBox cb : optionalPackages) {
            if (props == null || cb.isSelected()) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(cb.getActionCommand());
            }
        }
        return sb.toString();
    }

    protected final Preferences getPreferences() {
        return NbPreferences.forModule(PlatformDevicesPanel.class);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnManagePlatforms;
    private javax.swing.JPanel configurationPanel;
    private javax.swing.JComboBox deviceComboBox;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox jdkComboBox;
    private javax.swing.JLabel lblConfiguration;
    private javax.swing.JLabel lblDevice;
    private javax.swing.JLabel lblJDKPath;
    private javax.swing.JLabel lblOptionalPackages;
    private javax.swing.JLabel lblPlatform;
    private javax.swing.JLabel lblProfile;
    private javax.swing.JPanel optionalPackagesPanel;
    private javax.swing.JComboBox platformComboBox;
    private javax.swing.JPanel profilePanel;
    // End of variables declaration//GEN-END:variables

    private class JavaPlatformChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            PlatformDevicesPanel.this.panel.fireChangeEvent();
        }
    }
}

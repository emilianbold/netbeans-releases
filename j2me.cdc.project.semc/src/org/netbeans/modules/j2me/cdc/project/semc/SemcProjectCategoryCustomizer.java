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
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2me.cdc.project.semc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
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
import org.openide.util.NbBundle;

/**
 *
 * @author  suchys
 */
public class SemcProjectCategoryCustomizer extends JPanel implements CustomizerPanel, VisualPropertyGroup {
    
    private static String[] PROPERTY_NAMES = new String[] {
        SEMCPropertiesDescriptor.SEMC_APPLICATION_UID,
        SEMCPropertiesDescriptor.SEMC_APPLICATION_ICON,
        SEMCPropertiesDescriptor.SEMC_APPLICATION_ICON_SPLASH,
        SEMCPropertiesDescriptor.SEMC_APPLICATION_ICON_SPLASH_INSTALLONLY,
        SEMCPropertiesDescriptor.SEMC_APPLICATION_ICON_COUNT,
        SEMCPropertiesDescriptor.SEMC_APPLICATION_CAPS,
        SEMCPropertiesDescriptor.SEMC_CERTIFICATE,
        SEMCPropertiesDescriptor.SEMC_PRIVATEKEY,
        SEMCPropertiesDescriptor.SEMC_PASSWORD
    };
    
    private JTextField iconCountField = new JTextField(); //only holder
    private VisualPropertySupport vps;
    private String projectDir = ""; //NOI18N
    private File sdkLocation = new File(""); //NOI18N        
    
    /** Creates new form SavaJeProjectCategoryCustomizer */
    public SemcProjectCategoryCustomizer() {
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
            if (p.length > 0) sdkLocation = FileUtil.toFile((FileObject)p[0].getInstallFolders().iterator().next());
        }
    }
    
    public String[] getGroupPropertyNames() {
        return PROPERTY_NAMES;
    }
    
    public void initGroupValues(boolean useDefault) {
        vps.register(appUidTextField, SEMCPropertiesDescriptor.SEMC_APPLICATION_UID, useDefault);
        appUidTextField.setEditable(false);
        vps.register(applicationIcon, SEMCPropertiesDescriptor.SEMC_APPLICATION_ICON, useDefault);
        applicationIcon.setEditable(false);
        vps.register(splashImageTextField, SEMCPropertiesDescriptor.SEMC_APPLICATION_ICON_SPLASH, useDefault);
        vps.register(installLogoOnly, SEMCPropertiesDescriptor.SEMC_APPLICATION_ICON_SPLASH_INSTALLONLY, useDefault);
        vps.register(iconCountField, SEMCPropertiesDescriptor.SEMC_APPLICATION_ICON_COUNT, useDefault);
        vps.register(applicationCapabilities, SEMCPropertiesDescriptor.SEMC_APPLICATION_CAPS, useDefault);
        vps.register(certificateField, SEMCPropertiesDescriptor.SEMC_CERTIFICATE, useDefault);
        vps.register(privateKeyField, SEMCPropertiesDescriptor.SEMC_PRIVATEKEY, useDefault);
        vps.register(passwordField, SEMCPropertiesDescriptor.SEMC_PASSWORD, useDefault);
        jLabel1.setEnabled(!useDefault);
        jLabel2.setEnabled(!useDefault);
        jLabel3.setEnabled(!useDefault);
        jLabel4.setEnabled(!useDefault);
        jLabel5.setEnabled(!useDefault);
        jLabel6.setEnabled(!useDefault);
        jLabel7.setEnabled(!useDefault);
        jLabel8.setEnabled(!useDefault);
        jLabel9.setEnabled(!useDefault);
        browseIcon.setEnabled(!useDefault);
        browseLogoButton.setEnabled(!useDefault);
        browseCertificateButton.setEnabled(!useDefault);
        browsePrivateKeyButton.setEnabled(!useDefault);
        chnageUIDButton.setEnabled(!useDefault);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        applicationIcon = new javax.swing.JTextField();
        browseIcon = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        appUidTextField = new javax.swing.JTextField();
        chnageUIDButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        applicationCapabilities = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        splashImageTextField = new javax.swing.JTextField();
        browseLogoButton = new javax.swing.JButton();
        installLogoOnly = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        certificateField = new javax.swing.JTextField();
        privateKeyField = new javax.swing.JTextField();
        browseCertificateButton = new javax.swing.JButton();
        browsePrivateKeyButton = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        jCheckBox1 = new javax.swing.JCheckBox();

        jLabel3.setLabelFor(applicationIcon);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"LBL_AppIcon")); // NOI18N

        applicationIcon.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(browseIcon, org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"LBL_BrowseIcon")); // NOI18N
        browseIcon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseIconActionPerformed(evt);
            }
        });

        jLabel1.setLabelFor(appUidTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/cdc/project/semc/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("LBL_AppUID")); // NOI18N

        appUidTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(chnageUIDButton, bundle.getString("LBL_AppUIDChange")); // NOI18N
        chnageUIDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chnageUIDButtonActionPerformed(evt);
            }
        });

        jLabel2.setLabelFor(applicationCapabilities);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class, "LBL_AppCapabilities")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class, "LBL_AppCapabilitiesLabel")); // NOI18N

        jLabel5.setLabelFor(splashImageTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class, "LBL_LogoLocation")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseLogoButton, org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class, "LBL_BrowseLogo")); // NOI18N
        browseLogoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLogoButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(installLogoOnly, org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class, "LBL_LogoInstalTimeOnly")); // NOI18N
        installLogoOnly.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        installLogoOnly.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel6.setLabelFor(certificateField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, bundle.getString("LBL_CertificateLocation")); // NOI18N

        jLabel7.setLabelFor(privateKeyField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, bundle.getString("LBL_PrivateKeyLocation")); // NOI18N

        jLabel8.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, bundle.getString("LBL_Password")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseCertificateButton, org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class, "LBL_BrowseCertificate")); // NOI18N
        browseCertificateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseCertificateButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(browsePrivateKeyButton, org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class, "LBL_BrowsePrivateKey")); // NOI18N
        browsePrivateKeyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browsePrivateKeyButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, bundle.getString("LBL_PasswordUnsecure")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, NbBundle.getMessage(SemcProjectCategoryCustomizer.class, "LBL_UseDefault")); // NOI18N
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBox1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel2)
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel4)
                            .add(applicationCapabilities, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(jLabel1))
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(jLabel5)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(installLogoOnly)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(splashImageTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                                    .add(applicationIcon, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                                    .add(appUidTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(browseIcon)
                                    .add(chnageUIDButton)
                                    .add(browseLogoButton)))))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel6)
                            .add(jLabel7)
                            .add(jLabel8))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel9)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, passwordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, certificateField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, privateKeyField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, browseCertificateButton)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, browsePrivateKeyButton))))))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {browseIcon, browseLogoButton, chnageUIDButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jCheckBox1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(appUidTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1)
                    .add(chnageUIDButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(applicationIcon, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3)
                    .add(browseIcon))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(splashImageTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseLogoButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(installLogoOnly)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(applicationCapabilities, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(browseCertificateButton)
                    .add(certificateField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(browsePrivateKeyButton)
                    .add(privateKeyField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel9)
                .addContainerGap(56, Short.MAX_VALUE))
        );

        applicationIcon.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSN_CustomizerSE_ApplicationIcon")); // NOI18N
        applicationIcon.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSD_CustomizerSE_ApplicationIcon")); // NOI18N
        browseIcon.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSN_CustomizerSE_Browse")); // NOI18N
        browseIcon.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSD_CustomizerSE_Browse")); // NOI18N
        appUidTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSN_CustomizerSE_ApplicationUID")); // NOI18N
        appUidTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSD_CustomizerSE_ApplicationUID")); // NOI18N
        chnageUIDButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSN_CustomizerSE_Change")); // NOI18N
        chnageUIDButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSD_CustomizerSE_Change")); // NOI18N
        applicationCapabilities.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSN_CustomizerSE_ApplicationCapabilities")); // NOI18N
        applicationCapabilities.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSD_CustomizerSE_ApplicationCapabilities")); // NOI18N
        splashImageTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSN_CustomizerSE_ApplicationLogo")); // NOI18N
        splashImageTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSD_CustomizerSE_ApplicationLogo")); // NOI18N
        browseLogoButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSN_CustomizerSE_Browse")); // NOI18N
        browseLogoButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSD_CustomizerSE_Browse")); // NOI18N
        installLogoOnly.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSN_CustomizerSE_ShowLogo")); // NOI18N
        installLogoOnly.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSD_CustomizerSE_ShowLogo")); // NOI18N
        certificateField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSN_CustomizerSE_CertificateLocation")); // NOI18N
        certificateField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSD_CustomizerSE_CertificateLocation")); // NOI18N
        privateKeyField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSN_CustomizerSE_PKLocation")); // NOI18N
        privateKeyField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSD_CustomizerSE_PKLocation")); // NOI18N
        browseCertificateButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSN_CustomizerSE_Browse")); // NOI18N
        browseCertificateButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSD_CustomizerSE_Browse")); // NOI18N
        browsePrivateKeyButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSN_CustomizerSE_Browse")); // NOI18N
        browsePrivateKeyButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSD_CustomizerSE_Browse")); // NOI18N
        passwordField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSN_CustomizerSE_Password")); // NOI18N
        passwordField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSD_CustomizerSE_Password")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSN_CustomizerSE")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SemcProjectCategoryCustomizer.class,"ACSD_CustomizerSE")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browsePrivateKeyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browsePrivateKeyButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        
        String workDir = privateKeyField.getText();
        if (workDir.trim().length() == 0) workDir = projectDir; 
        chooser.setSelectedFile(new File(workDir));
        chooser.setDialogTitle(NbBundle.getMessage(SemcProjectCategoryCustomizer.class, "TITLE_BrowsePrivateKey"));
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            privateKeyField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_browsePrivateKeyButtonActionPerformed

    private void browseCertificateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseCertificateButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        
        String workDir = certificateField.getText();
        if (workDir.trim().length() == 0) workDir = projectDir; 
        chooser.setSelectedFile(new File(workDir));
        chooser.setDialogTitle(NbBundle.getMessage(SemcProjectCategoryCustomizer.class, "TITLE_BrowseCertificate"));
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            certificateField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_browseCertificateButtonActionPerformed

    private void browseLogoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLogoButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
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
                if (ext.equals("jpeg") || ext.equals("jpg") || ext.equals("png") || ext.equals("gif") ||ext.equals("bmp") ||ext.equals("mbm")){
                    return true;
                }
                return false;
            }
            public String getDescription() {
                return NbBundle.getMessage(SemcProjectCategoryCustomizer.class, "LBL_LogoImageFiles");
            }
        });
        String workDir = splashImageTextField.getText();
        if (workDir.trim().length() == 0) workDir = projectDir; 
        chooser.setSelectedFile(new File(workDir));
        chooser.setDialogTitle(NbBundle.getMessage(SemcProjectCategoryCustomizer.class, "TITLE_BrowseLogo"));
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            splashImageTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_browseLogoButtonActionPerformed

    private void chnageUIDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chnageUIDButtonActionPerformed
        SemcApplicationUIDCustomizer customizer = new SemcApplicationUIDCustomizer(appUidTextField.getText(), sdkLocation);
        final DialogDescriptor dd = new DialogDescriptor(customizer, NbBundle.getMessage(SemcProjectCategoryCustomizer.class, "TITLE_ChangeUID")); //NOI18N
        customizer.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ( NotifyDescriptor.PROP_VALID.equals(evt.getPropertyName())){
                    dd.setValid(((Boolean)evt.getNewValue()).booleanValue());
                }
            }
        });
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        if( dd.getValue() == DialogDescriptor.OK_OPTION ){
            appUidTextField.setText(customizer.getUID());
        }
    }//GEN-LAST:event_chnageUIDButtonActionPerformed

    private void browseIconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseIconActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        MBMThumbnailAccessory accessory = new MBMThumbnailAccessory(chooser, sdkLocation);
        chooser.setAccessory(accessory);
        
        String workDir = applicationIcon.getText();
        if (workDir.trim().length() == 0) workDir = projectDir; 
        chooser.setSelectedFile(new File(workDir));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory() || f.getName().toLowerCase().endsWith(".mbm")){ //NOI18N
                    return true;
                }
                return false;
            }
            public String getDescription() {
                return NbBundle.getMessage(SemcProjectCategoryCustomizer.class, "MSG_MBM_IconType"); //NOI18N
            }
        });
        chooser.setDialogTitle(NbBundle.getMessage(SemcProjectCategoryCustomizer.class, "TITLE_BrowseIcon")); //NOI18N
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            applicationIcon.setText(file.getAbsolutePath());
            int iconCount = accessory.getIconCount();
            if (iconCount%2 != 0){
                iconCount = 0;
            } else {
                iconCount = iconCount/2;
            }
            iconCountField.setText(String.valueOf(iconCount));  
        }
    }//GEN-LAST:event_browseIconActionPerformed

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField appUidTextField;
    private javax.swing.JTextField applicationCapabilities;
    private javax.swing.JTextField applicationIcon;
    private javax.swing.JButton browseCertificateButton;
    private javax.swing.JButton browseIcon;
    private javax.swing.JButton browseLogoButton;
    private javax.swing.JButton browsePrivateKeyButton;
    private javax.swing.JTextField certificateField;
    private javax.swing.JButton chnageUIDButton;
    private javax.swing.JCheckBox installLogoOnly;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JTextField privateKeyField;
    private javax.swing.JTextField splashImageTextField;
    // End of variables declaration//GEN-END:variables

}

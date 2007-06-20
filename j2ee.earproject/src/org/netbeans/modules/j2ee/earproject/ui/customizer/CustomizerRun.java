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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JPanel;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.earproject.ProjectEar;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public final class CustomizerRun extends JPanel implements ArchiveCustomizerPanel, HelpCtx.Provider {
    
    // Helper for storing properties
    private final VisualPropertySupport vps;
    // VisualPropertySupport currently does not support more combo boxes when
    // item values differ from their display texts - see #53893
    private final VisualPropertySupport vps1;
    //private VisualArchiveIncludesSupport vws;
//    private final ProjectEar wm;
    
    private String[] serverInstanceIDs;
    private String[] serverNames;
    
    /** Whether this panel was already initialized. */
    private boolean initialized;
    
    private final EarProjectProperties earProperties;
    
    public CustomizerRun(final EarProjectProperties earProperties, final ProjectEar wm) {
        this.earProperties = earProperties;
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_A11YDesc")); // NOI18N
//        vws = new VisualArchiveIncludesSupport( earProperties.getProject(),
//        this.wm = wm;
        vps = new VisualPropertySupport(earProperties);
        vps1 = new VisualPropertySupport(earProperties);
        clientModuleUriCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateEnabled();
            }
        });
        updateEnabled();
        String j2eeVersion = earProperties.getProject().getJ2eePlatformVersion();
        if (J2eeModule.JAVA_EE_5.equals(j2eeVersion)) {
            jTextFieldVersion.setText(EarProjectProperties.JAVA_EE_SPEC_50_LABEL);
        } else if (J2eeModule.J2EE_14.equals(j2eeVersion)) {
            jTextFieldVersion.setText(EarProjectProperties.J2EE_SPEC_14_LABEL);
        }
    }
    
    private boolean isWebModuleSelected() {
        return earProperties.isWebUri((String) clientModuleUriCombo.getSelectedItem());
    }
    
    private void updateEnabled() {
        boolean isWebUri = isWebModuleSelected();
        
        // #100098
        if (!isWebUri && jCheckBoxDisplayBrowser.isSelected()) {
            //jCheckBoxDisplayBrowser.setSelected(false);   // cannot be used because of ActionListener
            jCheckBoxDisplayBrowser.doClick();
        }
        jCheckBoxDisplayBrowser.setEnabled(isWebUri);
        handleWebModuleRelated();
        
        jTextMainClass.setEnabled(!isWebUri);
        jTextArgs.setEnabled(!isWebUri);
        jTextVMOptions.setEnabled(!isWebUri);
        jLabelMainClass.setEnabled(!isWebUri);
        jLabelArgs.setEnabled(!isWebUri);
        jLabelVMOptions.setEnabled(!isWebUri);
        jLabelVMOptionsExample.setEnabled(!isWebUri);
    }
    
    public void initValues() {
        if (initialized) {
            return;
        }
        initServerInstances();
        
        vps.register(jCheckBoxDisplayBrowser, EarProjectProperties.DISPLAY_BROWSER);
        vps.register(jTextFieldRelativeURL, EarProjectProperties.LAUNCH_URL_RELATIVE);
        vps.register(jComboBoxServer, serverNames, serverInstanceIDs, EarProjectProperties.J2EE_SERVER_INSTANCE);
        
        vps.register(jTextMainClass, EarProjectProperties.APPCLIENT_MAIN_CLASS);
        vps.register(jTextArgs, EarProjectProperties.APPCLIENT_ARGS);
        vps.register(jTextVMOptions, EarProjectProperties.APPCLIENT_JVM_OPTIONS);
        
//        EarProjectProperties.PropertyDescriptor.Saver contextPathSaver = new EarProjectProperties.PropertyDescriptor.Saver() {
//            public void save(WebProjectProperties.PropertyInfo propertyInfo) {
//                final String serverInstId = (String) earProperties.get(EarProjectProperties.J2EE_SERVER_INSTANCE);
//                final String path = (String) propertyInfo.getValue();
//                final String oldValue = (String) propertyInfo.getOldValue();
//                if(path != null && !path.equals(oldValue)) {
//                    wm.setContextPath(serverInstId, path);
//                }
//            }
//        };
//        if (earProperties.get(EarProjectProperties.CLIENT_MODULE_URI) == null) {
//            EarProjectProperties.PropertyDescriptor propertyDescriptor = new EarProjectProperties.PropertyDescriptor(
//                    EarProjectProperties.CLIENT_MODULE_URI, null, EarProjectProperties.STRING_PARSER, contextPathSaver);
//            final String contextPath = wm.getContextPath();
//            EarProjectProperties.PropertyInfo propertyInfo =
//                    earProperties.new PropertyInfo(propertyDescriptor, contextPath, contextPath);
//            earProperties.initProperty(WebProjectProperties.CLIENT_MODULE_URI, propertyInfo);
//        }
        
        String[] displayUris = getDisplayUris();
        vps1.register(clientModuleUriCombo, displayUris, getUris(displayUris),
                EarProjectProperties.CLIENT_MODULE_URI);
        
        handleWebModuleRelated();
        initialized = true;
    }
    
    private void handleWebModuleRelated() {
        boolean enabled = jCheckBoxDisplayBrowser.isEnabled() && jCheckBoxDisplayBrowser.isSelected();
        jLabelContextPathDesc.setEnabled(enabled);
        jLabelRelativeURL.setEnabled(enabled);
        jTextFieldRelativeURL.setEnabled(enabled);
    }
    
    private String[] getDisplayUris() {
        Collection<String> uris = new LinkedHashSet<String>();
        uris.addAll(Arrays.asList(earProperties.getWebUris()));
        uris.addAll(Arrays.asList(earProperties.getAppClientUris()));
        return uris.toArray(new String[uris.size()]);
    }
    
    private String[] getUris(final String[] displayUris) {
        String appClient = (String) earProperties.get(earProperties.APPLICATION_CLIENT);
        String[] uris = new String[displayUris.length];
        for (int i = 0; i < uris.length; i++) {
            if (earProperties.isWebUri(displayUris[i])) {
                uris[i] = displayUris[i];
            } else if (earProperties.isAppClientUri(displayUris[i])) {
                if (displayUris[i].equals(appClient)) {
                    uris[i] = earProperties.getClientModuleUriForAppClient();
                } else {
                    uris[i] = displayUris[i];
                }
            } else {
                assert false : "Nor web module neither application client: " + displayUris[i]; // NOI18N
            }
        }
        return uris;
    }
    
    private int getLongestVersionLength() {
        return Math.max(
                EarProjectProperties.JAVA_EE_SPEC_50_LABEL.length(),
                EarProjectProperties.J2EE_SPEC_14_LABEL.length());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelContextPath = new javax.swing.JLabel();
        jLabelServer = new javax.swing.JLabel();
        jComboBoxServer = new javax.swing.JComboBox();
        clientModuleUriCombo = new javax.swing.JComboBox();
        webInfoPanel = new javax.swing.JPanel();
        jLabelContextPathDesc = new javax.swing.JLabel();
        jLabelRelativeURL = new javax.swing.JLabel();
        jTextFieldRelativeURL = new javax.swing.JTextField();
        jCheckBoxDisplayBrowser = new javax.swing.JCheckBox();
        clientInfoPanel = new javax.swing.JPanel();
        jLabelVMOptionsExample = new javax.swing.JLabel();
        jTextVMOptions = new javax.swing.JTextField();
        jLabelVMOptions = new javax.swing.JLabel();
        jLabelArgs = new javax.swing.JLabel();
        jLabelMainClass = new javax.swing.JLabel();
        jTextMainClass = new javax.swing.JTextField();
        jTextArgs = new javax.swing.JTextField();
        filler = new javax.swing.JLabel();
        jLabelVersion = new javax.swing.JLabel();
        jTextFieldVersion = new javax.swing.JTextField();

        jLabelContextPath.setLabelFor(clientModuleUriCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelContextPath, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_ClientModuleURI_JLabel")); // NOI18N

        jLabelServer.setLabelFor(jComboBoxServer);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelServer, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Server_JLabel")); // NOI18N

        jComboBoxServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxServerActionPerformed(evt);
            }
        });

        webInfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_WebModInfo"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelContextPathDesc, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_ContextPathDesc_JLabel")); // NOI18N

        jLabelRelativeURL.setLabelFor(jTextFieldRelativeURL);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelRelativeURL, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_RelativeURL_JLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxDisplayBrowser, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_DisplayBrowser_JCheckBox")); // NOI18N
        jCheckBoxDisplayBrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDisplayBrowserActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout webInfoPanelLayout = new org.jdesktop.layout.GroupLayout(webInfoPanel);
        webInfoPanel.setLayout(webInfoPanelLayout);
        webInfoPanelLayout.setHorizontalGroup(
            webInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(webInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(webInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBoxDisplayBrowser)
                    .add(jLabelContextPathDesc)
                    .add(webInfoPanelLayout.createSequentialGroup()
                        .add(jLabelRelativeURL)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextFieldRelativeURL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)))
                .addContainerGap())
        );
        webInfoPanelLayout.setVerticalGroup(
            webInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(webInfoPanelLayout.createSequentialGroup()
                .add(jCheckBoxDisplayBrowser)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelContextPathDesc)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(webInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelRelativeURL)
                    .add(jTextFieldRelativeURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTextFieldRelativeURL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_RelativeURL_A11YDesc")); // NOI18N
        jCheckBoxDisplayBrowser.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_DisplayBrowser_A11YDesc")); // NOI18N

        clientInfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_ClientInfo"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelVMOptionsExample, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options_Example")); // NOI18N

        jLabelVMOptions.setLabelFor(jTextVMOptions);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelVMOptions, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options")); // NOI18N

        jLabelArgs.setLabelFor(jTextArgs);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelArgs, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Args_JLabel")); // NOI18N

        jLabelMainClass.setLabelFor(jTextMainClass);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelMainClass, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_MainClass_JLabel")); // NOI18N

        org.jdesktop.layout.GroupLayout clientInfoPanelLayout = new org.jdesktop.layout.GroupLayout(clientInfoPanel);
        clientInfoPanel.setLayout(clientInfoPanelLayout);
        clientInfoPanelLayout.setHorizontalGroup(
            clientInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(clientInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(clientInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelMainClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelArgs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelVMOptions))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(clientInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelVMOptionsExample)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextVMOptions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextArgs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextMainClass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE))
                .addContainerGap())
        );
        clientInfoPanelLayout.setVerticalGroup(
            clientInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(clientInfoPanelLayout.createSequentialGroup()
                .add(clientInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelMainClass)
                    .add(jTextMainClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(clientInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelArgs)
                    .add(jTextArgs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(clientInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelVMOptions)
                    .add(jTextVMOptions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jLabelVMOptionsExample))
        );

        jLabelVersion.setLabelFor(jTextFieldVersion);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelVersion, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Version_JLabel")); // NOI18N

        jTextFieldVersion.setColumns(getLongestVersionLength());
        jTextFieldVersion.setEditable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(webInfoPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(filler)
                        .add(139, 139, 139)
                        .add(clientModuleUriCombo, 0, 498, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabelContextPath))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabelServer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jComboBoxServer, 0, 498, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabelVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextFieldVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(clientInfoPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(filler)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabelContextPath)
                        .add(clientModuleUriCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelServer)
                    .add(jComboBoxServer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelVersion)
                    .add(jTextFieldVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(30, 30, 30)
                .add(webInfoPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(clientInfoPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(180, Short.MAX_VALUE))
        );

        jComboBoxServer.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_Server_A11YDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void jCheckBoxDisplayBrowserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDisplayBrowserActionPerformed
    handleWebModuleRelated();
}//GEN-LAST:event_jCheckBoxDisplayBrowserActionPerformed
    
    private void jComboBoxServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxServerActionPerformed
//        if (jComboBoxServer.getSelectedIndex() == -1 || !initialized)
//            return;
//        String newCtxPath = null ; // wm.getContextPath(serverInstanceIDs [jComboBoxServer.getSelectedIndex ()]);
    }//GEN-LAST:event_jComboBoxServerActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel clientInfoPanel;
    private javax.swing.JComboBox clientModuleUriCombo;
    private javax.swing.JLabel filler;
    private javax.swing.JCheckBox jCheckBoxDisplayBrowser;
    private javax.swing.JComboBox jComboBoxServer;
    private javax.swing.JLabel jLabelArgs;
    private javax.swing.JLabel jLabelContextPath;
    private javax.swing.JLabel jLabelContextPathDesc;
    private javax.swing.JLabel jLabelMainClass;
    private javax.swing.JLabel jLabelRelativeURL;
    private javax.swing.JLabel jLabelServer;
    private javax.swing.JLabel jLabelVMOptions;
    private javax.swing.JLabel jLabelVMOptionsExample;
    private javax.swing.JLabel jLabelVersion;
    private javax.swing.JTextField jTextArgs;
    private javax.swing.JTextField jTextFieldRelativeURL;
    private javax.swing.JTextField jTextFieldVersion;
    private javax.swing.JTextField jTextMainClass;
    private javax.swing.JTextField jTextVMOptions;
    private javax.swing.JPanel webInfoPanel;
    // End of variables declaration//GEN-END:variables
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerRun.class);
    }
    
    private void initServerInstances() {
        String j2eeSpec = (String)earProperties.get(EarProjectProperties.J2EE_PLATFORM);
        String[] servInstIDs = Deployment.getDefault().getServerInstanceIDs();
        Deployment deployment = Deployment.getDefault();
        Map<String, String> servers = new TreeMap<String, String>();
        for (int i = 0; i < servInstIDs.length; i++) {
            String instanceID = servInstIDs[i];
            J2eePlatform j2eePlat = deployment.getJ2eePlatform(instanceID);
            String servInstDisplayName = Deployment.getDefault().getServerInstanceDisplayName(servInstIDs[i]);
            if (servInstDisplayName != null
                    && j2eePlat != null && j2eePlat.getSupportedModuleTypes().contains(J2eeModule.EAR)
                    && j2eePlat.getSupportedSpecVersions(J2eeModule.EAR).contains(j2eeSpec)) {
                servers.put(servInstDisplayName, instanceID);
            }
        }
        serverInstanceIDs = servers.values().toArray(new String[servers.size()]);
        serverNames = servers.keySet().toArray(new String[servers.size()]);
    }

}

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
    
    private void updateEnabled() {
        boolean displayInBrowser = jCheckBoxDisplayBrowser.isSelected();
        boolean isWebUri = earProperties.isWebUri((String) clientModuleUriCombo.getSelectedItem());
        jTextFieldRelativeURL.setEnabled(displayInBrowser && isWebUri);
        jLabelContextPathDesc.setEnabled(displayInBrowser && isWebUri);
        jLabelRelativeURL.setEnabled(displayInBrowser && isWebUri);
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
        
        jTextFieldRelativeURL.setEditable(jCheckBoxDisplayBrowser.isSelected());
        initialized = true;
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
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelContextPath = new javax.swing.JLabel();
        jLabelServer = new javax.swing.JLabel();
        jComboBoxServer = new javax.swing.JComboBox();
        jCheckBoxDisplayBrowser = new javax.swing.JCheckBox();
        clientModuleUriCombo = new javax.swing.JComboBox();
        webInfoPanel = new javax.swing.JPanel();
        jLabelContextPathDesc = new javax.swing.JLabel();
        jLabelRelativeURL = new javax.swing.JLabel();
        jTextFieldRelativeURL = new javax.swing.JTextField();
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

        setLayout(new java.awt.GridBagLayout());

        jLabelContextPath.setLabelFor(clientModuleUriCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelContextPath, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_ClientModuleURI_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jLabelContextPath, gridBagConstraints);

        jLabelServer.setLabelFor(jComboBoxServer);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelServer, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Server_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabelServer, gridBagConstraints);

        jComboBoxServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxServerActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(jComboBoxServer, gridBagConstraints);
        jComboBoxServer.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_Server_A11YDesc"));

        jCheckBoxDisplayBrowser.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxDisplayBrowser, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_DisplayBrowser_JCheckBox"));
        jCheckBoxDisplayBrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDisplayBrowserActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jCheckBoxDisplayBrowser, gridBagConstraints);
        jCheckBoxDisplayBrowser.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_DisplayBrowser_A11YDesc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
        add(clientModuleUriCombo, gridBagConstraints);

        webInfoPanel.setLayout(new java.awt.GridBagLayout());

        webInfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_WebModInfo")));
        org.openide.awt.Mnemonics.setLocalizedText(jLabelContextPathDesc, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_ContextPathDesc_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        webInfoPanel.add(jLabelContextPathDesc, gridBagConstraints);

        jLabelRelativeURL.setLabelFor(jTextFieldRelativeURL);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelRelativeURL, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_RelativeURL_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        webInfoPanel.add(jLabelRelativeURL, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        webInfoPanel.add(jTextFieldRelativeURL, gridBagConstraints);
        jTextFieldRelativeURL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_RelativeURL_A11YDesc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(webInfoPanel, gridBagConstraints);

        clientInfoPanel.setLayout(new java.awt.GridBagLayout());

        clientInfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_ClientInfo")));
        org.openide.awt.Mnemonics.setLocalizedText(jLabelVMOptionsExample, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options_Example"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        clientInfoPanel.add(jLabelVMOptionsExample, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        clientInfoPanel.add(jTextVMOptions, gridBagConstraints);

        jLabelVMOptions.setLabelFor(jTextVMOptions);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelVMOptions, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        clientInfoPanel.add(jLabelVMOptions, gridBagConstraints);

        jLabelArgs.setLabelFor(jTextArgs);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelArgs, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Args_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        clientInfoPanel.add(jLabelArgs, gridBagConstraints);

        jLabelMainClass.setLabelFor(jTextMainClass);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelMainClass, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_MainClass_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        clientInfoPanel.add(jLabelMainClass, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        clientInfoPanel.add(jTextMainClass, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        clientInfoPanel.add(jTextArgs, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(clientInfoPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weighty = 1.0;
        add(filler, gridBagConstraints);

        jLabelVersion.setLabelFor(jTextFieldVersion);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelVersion, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Version_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        add(jLabelVersion, gridBagConstraints);

        jTextFieldVersion.setColumns(getLongestVersionLength());
        jTextFieldVersion.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 0);
        add(jTextFieldVersion, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void jComboBoxServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxServerActionPerformed
//        if (jComboBoxServer.getSelectedIndex() == -1 || !initialized)
//            return;
//        String newCtxPath = null ; // wm.getContextPath(serverInstanceIDs [jComboBoxServer.getSelectedIndex ()]);
    }//GEN-LAST:event_jComboBoxServerActionPerformed
    
    private void jCheckBoxDisplayBrowserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDisplayBrowserActionPerformed
        updateEnabled();
    }//GEN-LAST:event_jCheckBoxDisplayBrowserActionPerformed
    
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

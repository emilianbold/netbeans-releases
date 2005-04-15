/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.util.ArrayList;
import org.netbeans.modules.j2ee.earproject.ProjectEar;

import javax.swing.JPanel;

import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.openide.util.HelpCtx;

import org.netbeans.modules.j2ee.earproject.ui.customizer.ArchiveCustomizerPanel;
import org.netbeans.modules.j2ee.earproject.ui.customizer.VisualPropertySupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
//import org.netbeans.modules.j2ee.common.ui.customizer.VisualArchiveIncludesSupport;
import org.netbeans.modules.j2ee.earproject.ui.customizer.VisualClasspathSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
//import org.netbeans.modules.j2ee.common.ui.customizer.CustomizerGeneral;

public class CustomizerRun extends JPanel implements ArchiveCustomizerPanel, HelpCtx.Provider {
    
    // Helper for storing properties
    private VisualPropertySupport vps;
    // VisualPropertySupport currently does not support more combo boxes when
    // item values differ from their display texts - see #53893
    private VisualPropertySupport vps1;
    //private VisualArchiveIncludesSupport vws;
    private VisualClasspathSupport vws;
    private ProjectEar wm;

    private String[] serverInstanceIDs;
    private String[] serverNames;
    boolean initialized = false;

    private EarProjectProperties webProperties;

    /** Creates new form CustomizerCompile */
    public CustomizerRun(EarProjectProperties webProperties, ProjectEar wm) {
        this.webProperties = webProperties;
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_A11YDesc")); //NOI18N
//        vws = new VisualArchiveIncludesSupport( webProperties.getProject(),
        vws = new VisualClasspathSupport( webProperties.getProject(),
            (String) webProperties.get(EarProjectProperties.J2EE_PLATFORM),
                                            jTableAddContent,
                                            jButtonAddJar,
                                            jButtonAddLib,
                                            jButtonAddProject,
                                            new javax.swing.JButton(), // edit button
                                            jButtonRemove, 
                                            new javax.swing.JButton(), 
                                            new javax.swing.JButton(),true);

        this.wm = wm;
        vps = new VisualPropertySupport(webProperties);
        vps1 = new VisualPropertySupport(webProperties);
    }
    
    public void initValues() {
        initialized = false;
        initServerInstances();

        vps.register(jCheckBoxDisplayBrowser, EarProjectProperties.DISPLAY_BROWSER);
        vps.register(jTextFieldRelativeURL, EarProjectProperties.LAUNCH_URL_RELATIVE);
        vps.register(jComboBoxServer, serverNames, serverInstanceIDs, EarProjectProperties.J2EE_SERVER_INSTANCE);
        vps.register(vws, EarProjectProperties.RUN_CLASSPATH);

//        EarProjectProperties.PropertyDescriptor.Saver contextPathSaver = new EarProjectProperties.PropertyDescriptor.Saver() {
//            public void save(WebProjectProperties.PropertyInfo propertyInfo) {
//                final String serverInstId = (String) webProperties.get(EarProjectProperties.J2EE_SERVER_INSTANCE);
//                final String path = (String) propertyInfo.getValue();
//                final String oldValue = (String) propertyInfo.getOldValue();
//                if(path != null && !path.equals(oldValue)) {
//                    wm.setContextPath(serverInstId, path);
//                }
//            }
//        };
        if (webProperties.get(EarProjectProperties.CLIENT_MODULE_URI) == null) {
//            EarProjectProperties.PropertyDescriptor propertyDescriptor = new EarProjectProperties.PropertyDescriptor(
//                    EarProjectProperties.CLIENT_MODULE_URI, null, EarProjectProperties.STRING_PARSER, contextPathSaver);
//            final String contextPath = wm.getContextPath();
//            EarProjectProperties.PropertyInfo propertyInfo =
//                    webProperties.new PropertyInfo(propertyDescriptor, contextPath, contextPath);
//            webProperties.initProperty(WebProjectProperties.CLIENT_MODULE_URI, propertyInfo);
        }
//
        vps1.register(clientModuleUriCombo, webProperties.getWebUris(),
            EarProjectProperties.CLIENT_MODULE_URI);

        jTextFieldRelativeURL.setEditable(jCheckBoxDisplayBrowser.isSelected());
        initialized = true;
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
        jLabelContextPathDesc = new javax.swing.JLabel();
        jLabelRelativeURL = new javax.swing.JLabel();
        jTextFieldRelativeURL = new javax.swing.JTextField();
        jLabelEmbeddedCP = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableAddContent = new javax.swing.JTable();
        jButtonAddJar = new javax.swing.JButton();
        jButtonAddLib = new javax.swing.JButton();
        jButtonAddProject = new javax.swing.JButton();
        jButtonRemove = new javax.swing.JButton();
        clientModuleUriCombo = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        jLabelContextPath.setLabelFor(clientModuleUriCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelContextPath, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_ClientModuleURI_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jLabelContextPath, gridBagConstraints);

        jLabelServer.setLabelFor(jComboBoxServer);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelServer, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Server_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jLabelServer, gridBagConstraints);

        jComboBoxServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxServerActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 0);
        add(jComboBoxServer, gridBagConstraints);
        jComboBoxServer.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/earproject/ui/customizer/Bundle").getString("ACS_CustomizeRun_Server_A11YDesc"));

        jCheckBoxDisplayBrowser.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxDisplayBrowser, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_DisplayBrowser_JCheckBox"));
        jCheckBoxDisplayBrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDisplayBrowserActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jCheckBoxDisplayBrowser, gridBagConstraints);
        jCheckBoxDisplayBrowser.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/earproject/ui/customizer/Bundle").getString("ACS_CustomizeRun_DisplayBrowser_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabelContextPathDesc, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_ContextPathDesc_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jLabelContextPathDesc, gridBagConstraints);

        jLabelRelativeURL.setLabelFor(jTextFieldRelativeURL);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelRelativeURL, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_RelativeURL_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jLabelRelativeURL, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 11, 0);
        add(jTextFieldRelativeURL, gridBagConstraints);
        jTextFieldRelativeURL.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/earproject/ui/customizer/Bundle").getString("ACS_CustomizeRun_RelativeURL_A11YDesc"));

        jLabelEmbeddedCP.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelEmbeddedCP.setLabelFor(jTableAddContent);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelEmbeddedCP, org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("LBL_CustomizerRun_EmbeddedClasspathElements_JLabel"));
        jLabelEmbeddedCP.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        add(jLabelEmbeddedCP, gridBagConstraints);

        jTableAddContent.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(jTableAddContent);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 10.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jScrollPane2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJar, org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("LBL_CustomizeWAR_AddJar_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jButtonAddJar, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLib, org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("LBL_CustomizeCompile_Classpath_AddLibrary_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jButtonAddLib, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddProject, org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("LBL_CustomizeCompile_Classpath_AddProject_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonAddProject, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemove, org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("LBL_CustomizeCompile_Classpath_Remove_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonRemove, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 0);
        add(clientModuleUriCombo, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void jComboBoxServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxServerActionPerformed
        if (jComboBoxServer.getSelectedIndex() == -1 || !initialized)
            return;
        String newCtxPath = null ; // wm.getContextPath(serverInstanceIDs [jComboBoxServer.getSelectedIndex ()]);
    }//GEN-LAST:event_jComboBoxServerActionPerformed

    private void jCheckBoxDisplayBrowserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDisplayBrowserActionPerformed
        boolean editable = jCheckBoxDisplayBrowser.isSelected();
        
        jLabelContextPathDesc.setEnabled(editable);
        jLabelRelativeURL.setEnabled(editable);
        jTextFieldRelativeURL.setEditable(editable);
    }//GEN-LAST:event_jCheckBoxDisplayBrowserActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox clientModuleUriCombo;
    private javax.swing.JButton jButtonAddJar;
    private javax.swing.JButton jButtonAddLib;
    private javax.swing.JButton jButtonAddProject;
    private javax.swing.JButton jButtonRemove;
    private javax.swing.JCheckBox jCheckBoxDisplayBrowser;
    private javax.swing.JComboBox jComboBoxServer;
    private javax.swing.JLabel jLabelContextPath;
    private javax.swing.JLabel jLabelContextPathDesc;
    private javax.swing.JLabel jLabelEmbeddedCP;
    private javax.swing.JLabel jLabelRelativeURL;
    private javax.swing.JLabel jLabelServer;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTableAddContent;
    private javax.swing.JTextField jTextFieldRelativeURL;
    // End of variables declaration//GEN-END:variables

    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerRun.class);
    }
    
    private void initServerInstances() {
        String j2eeSpec = (String)webProperties.get(EarProjectProperties.J2EE_PLATFORM);
        String[] servInstIDs = Deployment.getDefault().getServerInstanceIDs();
        java.util.List servInstIDsList = new ArrayList();
        java.util.List servNamesList = new ArrayList();
        Deployment deployment = Deployment.getDefault();
        for (int i = 0; i < servInstIDs.length; i++) {
            String instanceID = servInstIDs[i];
            J2eePlatform j2eePlat = deployment.getJ2eePlatform(instanceID);
            String servInstDisplayName = Deployment.getDefault().getServerInstanceDisplayName(servInstIDs[i]);
            if (servInstDisplayName != null
                && j2eePlat != null && j2eePlat.getSupportedModuleTypes().contains(J2eeModule.EAR)
                && j2eePlat.getSupportedSpecVersions().contains(j2eeSpec)) {
                servInstIDsList.add(instanceID);
                servNamesList.add(servInstDisplayName);
            }
        }
        serverInstanceIDs = (String[]) servInstIDsList.toArray(new String[servInstIDsList.size()]);
        serverNames = (String[]) servNamesList.toArray(new String[servNamesList.size()]);
    }
}

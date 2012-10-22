/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.customizer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.j2ee.ExecutionChecker;
import static org.netbeans.modules.maven.j2ee.ExecutionChecker.CLIENTURLPART;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.maven.j2ee.Wrapper;
import org.netbeans.modules.maven.j2ee.utils.LoggingUtils;
import org.netbeans.modules.maven.j2ee.web.WebModuleImpl;
import org.netbeans.modules.maven.j2ee.web.WebModuleProviderImpl;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  mkleint
 */
public class CustomizerRunWeb extends BaseRunCustomizer {
    public static final String PROP_SHOW_IN_BROWSER = "netbeans.deploy.showBrowser"; //NOI18N
    
    private WebModule module;
    private WebModuleProviderImpl moduleProvider;

    private NetbeansActionMapping run;
    private NetbeansActionMapping debug;
    private NetbeansActionMapping profile;

    private boolean isRunCompatible;
    private boolean isDebugCompatible;
    private boolean isProfileCompatible;

    private String oldUrl;
    private String oldContextPath;
    

    public CustomizerRunWeb(final ModelHandle2 handle, Project project) {
        super(handle, project);
        initComponents();
        module = WebModule.getWebModule(project.getProjectDirectory());
        moduleProvider = project.getLookup().lookup(WebModuleProviderImpl.class);
        
        if (module != null) {
            final Profile j2eeProfile = module.getJ2eeProfile();
            final String version;
            if (j2eeProfile.equals(Profile.JAVA_EE_6_WEB)) {
                version = Profile.JAVA_EE_6_FULL.getDisplayName();
                initServerComboForJavaEE6();
            } else {
                version = j2eeProfile.getDisplayName();
                initServerComboForNonJavaEE6();
            }

            loadServerModel(comServer, J2eeModule.Type.WAR, j2eeProfile);
            txtJ2EEVersion.setText(version);
            txtContextPath.setText(module.getContextPath());
        }
        comProfile.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Profile prf = (Profile)value;
                String val = "Web";
                if (Profile.JAVA_EE_6_FULL.equals(prf)) {
                    val = "Full";
                }
                return super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
            }
        });

        initValues();
        initDeployOnSaveComponent(jCheckBoxDeployOnSave, dosDescription);
        initServerComponent(comServer, lblServer);
    }

    private void initServerComboForJavaEE6() {
        lblProfile.setVisible(true);
        comProfile.setVisible(true);
        comProfile.setEnabled(true);
        comProfile.setModel(new DefaultComboBoxModel(new Object[] { Profile.JAVA_EE_6_WEB, Profile.JAVA_EE_6_FULL}));
        Profile prop = module.getJ2eeProfile();
        if (prop != null) {
            comProfile.setSelectedItem(prop);
        } else {
            comProfile.setSelectedItem(Profile.JAVA_EE_6_WEB);
        }
        comProfile.addActionListener(new ActionListener() {
            private Profile modified;
            private ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {

                @Override
                public void performOperation(POMModel model) {
                org.netbeans.modules.maven.model.pom.Project root = model.getProject();
                    if (Profile.JAVA_EE_6_FULL.equals(modified)) {
                        Properties props = root.getProperties();
                        if (props == null) {
                            props = model.getFactory().createProperties();
                            root.setProperties(props);
                        }
                        props.setProperty(MavenJavaEEConstants.HINT_J2EE_VERSION, modified.toPropertiesString());
                        replaceDependency(model, "javaee-web-api", "javaee-api");
                    } else {
                        Properties props = root.getProperties();
                        if (props != null && props.getProperty(MavenJavaEEConstants.HINT_J2EE_VERSION) != null) {
                            props.setProperty(MavenJavaEEConstants.HINT_J2EE_VERSION, null);
                            if (props.getProperties().size() == 0) {
                                ((AbstractDocumentComponent)root).removeChild("properties", props);
                            }
                            replaceDependency(model, "javaee-api", "javaee-web-api");
                        }
                    }
                }
            };
            @Override
            public void actionPerformed(ActionEvent e) {
                Profile p = (Profile) comProfile.getSelectedItem();
                modified = p;
                handle.removePOMModification(operation);
                handle.addPOMModification(operation);
            }
        });
    }

    private void initServerComboForNonJavaEE6() {
        lblProfile.setVisible(false);
        comProfile.setVisible(false);
        comProfile.setEnabled(false);
    }
    
    private void initValues() {
        List<NetbeansActionMapping> actionMappings = handle.getActionMappings(handle.getActiveConfiguration()).getActions();

        if (actionMappings == null || actionMappings.isEmpty()) {
            run = ModelHandle2.getDefaultMapping(ActionProvider.COMMAND_RUN, project);
            debug = ModelHandle2.getDefaultMapping(ActionProvider.COMMAND_DEBUG, project);
            profile = ModelHandle2.getDefaultMapping(ActionProvider.COMMAND_PROFILE, project);
        } else {
            for (NetbeansActionMapping actionMapping : actionMappings) {
                String actionName = actionMapping.getActionName();

                if (ActionProvider.COMMAND_RUN.equals(actionName)) {
                    run = actionMapping;
                }
                if (ActionProvider.COMMAND_DEBUG.equals(actionName)) {
                    debug = actionMapping;
                }
                if (ActionProvider.COMMAND_PROFILE.equals(actionName)) { // NOI18N
                    profile = actionMapping;
                }
            }
        }

        isRunCompatible = checkMapping(run);
        isDebugCompatible = checkMapping(debug);
        isProfileCompatible = checkMapping(profile);

        if (isRunCompatible) {
            if (run != null) {
                oldUrl = run.getProperties().get(CLIENTURLPART);
            } else if (debug != null) {
                oldUrl = debug.getProperties().get(CLIENTURLPART);
            }
        }
        
        if (oldUrl != null) {
            txtRelativeUrl.setText(oldUrl);
        } else {
            oldUrl = ""; //NOI18N
        }
        txtRelativeUrl.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent arg0) {
                applyRelUrl();
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                applyRelUrl();
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                applyRelUrl();
            }
        });

        
        String browser = (String)project.getProjectDirectory().getAttribute(PROP_SHOW_IN_BROWSER);
        boolean bool = browser != null ? Boolean.parseBoolean(browser) : true;
        cbBrowser.setSelected(bool);
        updateContextPathEnablement();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblServer = new javax.swing.JLabel();
        comServer = new javax.swing.JComboBox();
        lblJ2EEVersion = new javax.swing.JLabel();
        txtJ2EEVersion = new javax.swing.JTextField();
        lblContextPath = new javax.swing.JLabel();
        txtContextPath = new javax.swing.JTextField();
        cbBrowser = new javax.swing.JCheckBox();
        lblHint1 = new javax.swing.JLabel();
        lblRelativeUrl = new javax.swing.JLabel();
        txtRelativeUrl = new javax.swing.JTextField();
        lblHint2 = new javax.swing.JLabel();
        lblProfile = new javax.swing.JLabel();
        comProfile = new javax.swing.JComboBox();
        jCheckBoxDeployOnSave = new javax.swing.JCheckBox();
        dosDescription = new javax.swing.JLabel();

        lblServer.setLabelFor(comServer);
        org.openide.awt.Mnemonics.setLocalizedText(lblServer, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_Server")); // NOI18N

        comServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comServerActionPerformed(evt);
            }
        });

        lblJ2EEVersion.setLabelFor(txtJ2EEVersion);
        org.openide.awt.Mnemonics.setLocalizedText(lblJ2EEVersion, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_J2EE_Version")); // NOI18N

        txtJ2EEVersion.setEditable(false);

        lblContextPath.setLabelFor(txtContextPath);
        org.openide.awt.Mnemonics.setLocalizedText(lblContextPath, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_Context_Path")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbBrowser, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_Display_on_Run")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint1, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_Hint1")); // NOI18N

        lblRelativeUrl.setLabelFor(txtRelativeUrl);
        org.openide.awt.Mnemonics.setLocalizedText(lblRelativeUrl, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_Relative_URL")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint2, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_Hint2")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblProfile, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "CustomizerRunWeb.lblProfile.text")); // NOI18N

        comProfile.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxDeployOnSave, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "CustomizerRunWeb.jCheckBoxDeployOnSave.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dosDescription, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "CustomizerRunWeb.dosDescription.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBoxDeployOnSave)
                        .addContainerGap(404, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cbBrowser, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblHint1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(lblRelativeUrl)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lblHint2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 324, Short.MAX_VALUE))
                                    .addComponent(txtRelativeUrl, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblContextPath)
                                    .addComponent(lblJ2EEVersion)
                                    .addComponent(lblServer))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(comServer, javax.swing.GroupLayout.Alignment.TRAILING, 0, 430, Short.MAX_VALUE)
                                    .addComponent(txtContextPath, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtJ2EEVersion, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblProfile)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(comProfile, 0, 199, Short.MAX_VALUE)))))
                        .addGap(0, 0, 0))))
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(dosDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblServer)
                    .addComponent(comServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblJ2EEVersion)
                    .addComponent(txtJ2EEVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProfile)
                    .addComponent(comProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblContextPath)
                    .addComponent(txtContextPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addComponent(cbBrowser)
                .addGap(16, 16, 16)
                .addComponent(lblHint1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRelativeUrl)
                    .addComponent(txtRelativeUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblHint2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxDeployOnSave)
                .addGap(5, 5, 5)
                .addComponent(dosDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        txtJ2EEVersion.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "WebRunCustomizerPanel.txtJ2EEVersion.AccessibleContext.accessibleDescription")); // NOI18N
        txtContextPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "WebRunCustomizerPanel.txtContextPath.AccessibleContext.accessibleDescription")); // NOI18N
        cbBrowser.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "WebRunCustomizerPanel.cbBrowser.AccessibleContext.accessibleDescription")); // NOI18N
        txtRelativeUrl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "WebRunCustomizerPanel.txtRelativeUrl.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void comServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comServerActionPerformed
        updateContextPathEnablement();
    }//GEN-LAST:event_comServerActionPerformed

    private void updateContextPathEnablement() {
        Wrapper wp = (Wrapper)comServer.getSelectedItem();
        if (wp == null || 
                (ExecutionChecker.DEV_NULL.equals(wp.getServerID()) && wp.getSessionServerInstanceId() == null)) {
            if (txtContextPath.isEnabled()) {
                txtContextPath.setEnabled(false);
                oldContextPath = txtContextPath.getText();
                txtContextPath.setText(NbBundle.getMessage(CustomizerRunWeb.class, "WebRunCustomizerPanel.contextPathDisabled"));
            }
        } else {
            if (!txtContextPath.isEnabled()) {
                txtContextPath.setEnabled(true);
                if (oldContextPath != null) {
                    txtContextPath.setText(oldContextPath);
                } else {
                    txtContextPath.setText(module.getContextPath());
                }
            }
        }
    }
    
    private boolean checkMapping(NetbeansActionMapping map) {
        if (map == null) {
            return false;
        }
        Iterator it = map.getGoals().iterator();
        while (it.hasNext()) {
            String goal = (String) it.next();
            if (goal.indexOf("netbeans-deploy-plugin") > -1) { //NOI18N
                return true;
            }
        }
        if (map.getProperties().containsKey(MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY)) {
            return true;
        }
        return false;
    }
    
    private void applyRelUrl() {
        String newUrl = txtRelativeUrl.getText().trim();
        if (!newUrl.equals(oldUrl)) {
            if (isRunCompatible) {
                run.addProperty(CLIENTURLPART, newUrl);
                ModelHandle2.setUserActionMapping(run, handle.getActionMappings());
                handle.markAsModified(handle.getActionMappings());
            }
            if (isDebugCompatible) {
                debug.addProperty(CLIENTURLPART, newUrl);
                ModelHandle2.setUserActionMapping(debug, handle.getActionMappings());
                handle.markAsModified(handle.getActionMappings());
            }
            if (isProfileCompatible) {
                profile.addProperty(CLIENTURLPART, newUrl);
                ModelHandle2.setUserActionMapping(profile, handle.getActionMappings());
                handle.markAsModified(handle.getActionMappings());
            }
        }
    }

    @Override
    public void applyChangesInAWT() {
        assert SwingUtilities.isEventDispatchThread();
        boolean bool = cbBrowser.isSelected();
        try {
            project.getProjectDirectory().setAttribute(PROP_SHOW_IN_BROWSER, bool ? null : Boolean.FALSE.toString());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        Object obj = comServer.getSelectedItem();
        if (obj != null) {
            LoggingUtils.logUsage(CustomizerRunWeb.class, "USG_PROJECT_CONFIG_MAVEN_SERVER", new Object[] { obj.toString() }, "maven"); //NOI18N
        }
    }

    @Override
    public void applyChanges() {
        changeServer(comServer);
        changeContextPath();
    }
    
    private void changeContextPath() {
        moduleProvider = project.getLookup().lookup(WebModuleProviderImpl.class);
        if (txtContextPath.isEnabled()) {
            WebModuleImpl impl = moduleProvider.getModuleImpl();
            impl.setContextPath(txtContextPath.getText().trim());
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbBrowser;
    private javax.swing.JComboBox comProfile;
    private javax.swing.JComboBox comServer;
    private javax.swing.JLabel dosDescription;
    private javax.swing.JCheckBox jCheckBoxDeployOnSave;
    private javax.swing.JLabel lblContextPath;
    private javax.swing.JLabel lblHint1;
    private javax.swing.JLabel lblHint2;
    private javax.swing.JLabel lblJ2EEVersion;
    private javax.swing.JLabel lblProfile;
    private javax.swing.JLabel lblRelativeUrl;
    private javax.swing.JLabel lblServer;
    private javax.swing.JTextField txtContextPath;
    private javax.swing.JTextField txtJ2EEVersion;
    private javax.swing.JTextField txtRelativeUrl;
    // End of variables declaration//GEN-END:variables



    private void replaceDependency(POMModel model, String oldArt, String newArt) {
        Dependency d = ModelUtils.checkModelDependency(model, "javax", oldArt, false);
        if (d != null) {
            d.setArtifactId(newArt);
        }
    }
    
}

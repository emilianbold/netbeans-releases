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

package org.netbeans.modules.maven.j2ee.ui.customizer.impl;

import java.io.IOException;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.j2ee.ExecutionChecker;
import static org.netbeans.modules.maven.j2ee.ExecutionChecker.CLIENTURLPART;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.maven.j2ee.utils.Server;
import org.netbeans.modules.maven.j2ee.ui.customizer.BaseRunCustomizer;
import org.netbeans.modules.maven.j2ee.utils.LoggingUtils;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.modules.maven.j2ee.web.WebModuleImpl;
import org.netbeans.modules.maven.j2ee.web.WebModuleProviderImpl;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.modules.web.browser.api.BrowserUISupport.BrowserComboBoxModel;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 * @author Martin Janicek
 */
public class CustomizerRunWeb extends BaseRunCustomizer {

    public static final String PROP_SHOW_IN_BROWSER = "netbeans.deploy.showBrowser"; //NOI18N

    private BrowserComboBoxModel browserModel;

    private WebModule module;

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
        if (module != null) {
            contextPathTField.setText(module.getContextPath());
        }

        initValues();
        initServerModel(serverCBox, serverLabel, J2eeModule.Type.WAR);
        initVersionModel(javaeeVersionCBox, javaeeVersionLabel, J2eeModule.Type.WAR);
        initDeployOnSave(jCheckBoxDeployOnSave, dosDescription);
    }
    
    @Override
    public void applyChangesInAWT() {
        assert SwingUtilities.isEventDispatchThread();
        try {
            if (showBrowserCheckBox.isSelected()) {
                project.getProjectDirectory().setAttribute(PROP_SHOW_IN_BROWSER, null);
            } else {
                project.getProjectDirectory().setAttribute(PROP_SHOW_IN_BROWSER, Boolean.FALSE.toString());
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        Object obj = serverCBox.getSelectedItem();
        if (obj != null) {
            LoggingUtils.logUsage(CustomizerRunWeb.class, "USG_PROJECT_CONFIG_MAVEN_SERVER", new Object[] {obj.toString() }, "maven"); //NOI18N
        }
    }

    @Override
    public void applyChanges() {
        changeServer(serverCBox);
        changeContextPath();
        
        MavenProjectSupport.setBrowserID(project, browserModel.getSelectedBrowserId());
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


        String browser = (String) project.getProjectDirectory().getAttribute(PROP_SHOW_IN_BROWSER);
        boolean bool = browser != null ? Boolean.parseBoolean(browser) : true;
        showBrowserCheckBox.setSelected(bool);
        updateContextPathEnablement();
    }


    private boolean checkMapping(NetbeansActionMapping map) {
        if (map != null) {
            for (String goal : map.getGoals()) {
                if (goal.indexOf("netbeans-deploy-plugin") > -1) { //NOI18N
                    return true;
                }
            }
            if (map.getProperties().containsKey(MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY)) {
                return true;
            }
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

    private void changeContextPath() {
        final WebModuleProviderImpl moduleProvider = project.getLookup().lookup(WebModuleProviderImpl.class);
        if (contextPathTField.isEnabled()) {
            WebModuleImpl impl = moduleProvider.getModuleImpl();
            impl.setContextPath(contextPathTField.getText().trim());
        }
    }

    private JComboBox<WebBrowser> createBrowserComboBox() {
        String selectedBrowser = MavenProjectSupport.getBrowserID(project);
        browserModel = BrowserUISupport.createBrowserModel(selectedBrowser, true);
        browserCBox = BrowserUISupport.createBrowserPickerComboBox(browserModel.getSelectedBrowserId(), true, false, browserModel);
        browserCBox.setModel(browserModel);
        browserCBox.setRenderer(BrowserUISupport.createBrowserRenderer());

        return browserCBox;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        serverLabel = new javax.swing.JLabel();
        serverCBox = new javax.swing.JComboBox();
        javaeeVersionLabel = new javax.swing.JLabel();
        contextPathLabel = new javax.swing.JLabel();
        contextPathTField = new javax.swing.JTextField();
        showBrowserCheckBox = new javax.swing.JCheckBox();
        lblRelativeUrl = new javax.swing.JLabel();
        txtRelativeUrl = new javax.swing.JTextField();
        lblHint2 = new javax.swing.JLabel();
        jCheckBoxDeployOnSave = new javax.swing.JCheckBox();
        dosDescription = new javax.swing.JLabel();
        javaeeVersionCBox = new javax.swing.JComboBox();
        browserLabel = new javax.swing.JLabel();
        browserCBox = createBrowserComboBox();

        serverLabel.setLabelFor(serverCBox);
        org.openide.awt.Mnemonics.setLocalizedText(serverLabel, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_Server")); // NOI18N

        serverCBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverCBoxActionPerformed(evt);
            }
        });

        javaeeVersionLabel.setLabelFor(javaeeVersionCBox);
        org.openide.awt.Mnemonics.setLocalizedText(javaeeVersionLabel, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_J2EE_Version")); // NOI18N

        contextPathLabel.setLabelFor(contextPathTField);
        org.openide.awt.Mnemonics.setLocalizedText(contextPathLabel, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_Context_Path")); // NOI18N

        contextPathTField.setMinimumSize(new java.awt.Dimension(4, 24));
        contextPathTField.setPreferredSize(new java.awt.Dimension(4, 24));

        org.openide.awt.Mnemonics.setLocalizedText(showBrowserCheckBox, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_Display_on_Run")); // NOI18N

        lblRelativeUrl.setLabelFor(txtRelativeUrl);
        org.openide.awt.Mnemonics.setLocalizedText(lblRelativeUrl, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_Relative_URL")); // NOI18N

        txtRelativeUrl.setMinimumSize(new java.awt.Dimension(4, 24));
        txtRelativeUrl.setPreferredSize(new java.awt.Dimension(4, 24));

        org.openide.awt.Mnemonics.setLocalizedText(lblHint2, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_Hint2")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxDeployOnSave, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "CustomizerRunWeb.jCheckBoxDeployOnSave.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dosDescription, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "CustomizerRunWeb.dosDescription.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browserLabel, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "CustomizerRunWeb.browserLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(browserLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblRelativeUrl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(javaeeVersionLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(serverLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(contextPathLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(serverCBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(contextPathTField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(javaeeVersionCBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblHint2, javax.swing.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
                            .addComponent(txtRelativeUrl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(browserCBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(dosDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 609, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBoxDeployOnSave)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(showBrowserCheckBox)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverLabel)
                    .addComponent(serverCBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(javaeeVersionCBox, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(javaeeVersionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contextPathTField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contextPathLabel))
                .addGap(8, 8, 8)
                .addComponent(lblHint2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRelativeUrl)
                    .addComponent(txtRelativeUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(browserLabel)
                    .addComponent(browserCBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(showBrowserCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxDeployOnSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dosDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        contextPathTField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "WebRunCustomizerPanel.txtContextPath.AccessibleContext.accessibleDescription")); // NOI18N
        showBrowserCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "WebRunCustomizerPanel.cbBrowser.AccessibleContext.accessibleDescription")); // NOI18N
        txtRelativeUrl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "WebRunCustomizerPanel.txtRelativeUrl.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void serverCBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverCBoxActionPerformed
        updateContextPathEnablement();
    }//GEN-LAST:event_serverCBoxActionPerformed

    private void updateContextPathEnablement() {
        Server wp = (Server) serverCBox.getSelectedItem();
        if (wp == null || ExecutionChecker.DEV_NULL.equals(wp.getServerID())) {
            if (contextPathTField.isEnabled()) {
                contextPathTField.setEnabled(false);
                oldContextPath = contextPathTField.getText();
                contextPathTField.setText(NbBundle.getMessage(CustomizerRunWeb.class, "WebRunCustomizerPanel.contextPathDisabled"));
            }
        } else {
            if (!contextPathTField.isEnabled()) {
                contextPathTField.setEnabled(true);
                if (oldContextPath != null) {
                    contextPathTField.setText(oldContextPath);
                } else {
                    contextPathTField.setText(module.getContextPath());
                }
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox browserCBox;
    private javax.swing.JLabel browserLabel;
    private javax.swing.JLabel contextPathLabel;
    private javax.swing.JTextField contextPathTField;
    private javax.swing.JLabel dosDescription;
    private javax.swing.JCheckBox jCheckBoxDeployOnSave;
    private javax.swing.JComboBox javaeeVersionCBox;
    private javax.swing.JLabel javaeeVersionLabel;
    private javax.swing.JLabel lblHint2;
    private javax.swing.JLabel lblRelativeUrl;
    private javax.swing.JComboBox serverCBox;
    private javax.swing.JLabel serverLabel;
    private javax.swing.JCheckBox showBrowserCheckBox;
    private javax.swing.JTextField txtRelativeUrl;
    // End of variables declaration//GEN-END:variables

}

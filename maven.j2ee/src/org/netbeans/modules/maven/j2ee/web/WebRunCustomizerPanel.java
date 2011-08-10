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

package org.netbeans.modules.maven.j2ee.web;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.swing.JList;
import javax.swing.event.AncestorEvent;
import org.netbeans.modules.maven.j2ee.POHImpl;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.customizer.support.CheckBoxUpdater;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.j2ee.ExecutionChecker;
import static org.netbeans.modules.maven.j2ee.ExecutionChecker.CLIENTURLPART;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.maven.j2ee.SessionContent;
import org.netbeans.modules.maven.j2ee.Wrapper;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  mkleint
 */
public class WebRunCustomizerPanel extends javax.swing.JPanel {
    public static final String PROP_SHOW_IN_BROWSER = "netbeans.deploy.showBrowser"; //NOI18N
    private Project project;
    private ModelHandle handle;
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
    private ComboBoxUpdater<Wrapper> listener;
    
    private CheckBoxUpdater deployOnSaveUpdater;
    
    /** Creates new form WebRunCustomizerPanel */
    public WebRunCustomizerPanel(final ModelHandle handle, Project project) {
        initComponents();
        this.handle = handle;
        this.project = project;
        module = WebModule.getWebModule(project.getProjectDirectory());
        moduleProvider = project.getLookup().lookup(WebModuleProviderImpl.class);
        assert moduleProvider != null;
        assert module != null;
        loadComboModel();
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

        Profile p = module.getJ2eeProfile();
        String version = p.equals(Profile.JAVA_EE_6_WEB) ? Profile.JAVA_EE_6_FULL.getDisplayName() : p.getDisplayName();
        txtJ2EEVersion.setText(version);
        WebModuleImpl impl = moduleProvider.getWebModuleImplementation();
        if (Profile.JAVA_EE_6_WEB.equals(impl.getDescriptorJ2eeProfile())) {
            lblProfile.setVisible(true);
            comProfile.setVisible(true);
            comProfile.setEnabled(true);
            comProfile.setModel(new DefaultComboBoxModel(new Object[] { Profile.JAVA_EE_6_WEB, Profile.JAVA_EE_6_FULL}));
            Profile prop = impl.getPropertyJ2eeProfile();
            if (prop != null) {
                comProfile.setSelectedItem(prop);
            } else {
                comProfile.setSelectedItem(Profile.JAVA_EE_6_WEB);
            }
            comProfile.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Profile p = (Profile) comProfile.getSelectedItem();
                    org.netbeans.modules.maven.model.pom.Project root = handle.getPOMModel().getProject();
                    if (p.equals(Profile.JAVA_EE_6_FULL)) {
                        Properties props = root.getProperties();
                        if (props == null) {
                            props = handle.getPOMModel().getFactory().createProperties();
                            root.setProperties(props);
                        }
                        replaceDependency("javaee-web-api", "javaee-api");
                        props.setProperty(MavenJavaEEConstants.HINT_J2EE_VERSION, p.toPropertiesString());
                        handle.markAsModified(handle.getPOMModel());
                    } else {
                        Properties props = root.getProperties();
                        if (props != null && props.getProperty(MavenJavaEEConstants.HINT_J2EE_VERSION) != null) {
                            props.setProperty(MavenJavaEEConstants.HINT_J2EE_VERSION, null);
                            if (props.getProperties().size() == 0) {
                                ((AbstractDocumentComponent)root).removeChild("properties", props);
                            }
                            replaceDependency("javaee-api", "javaee-web-api");
                            handle.markAsModified(handle.getPOMModel());
                        }
                    }
                }

            });
        } else {
            lblProfile.setVisible(false);
            comProfile.setVisible(false);
            comProfile.setEnabled(false);
        }

        txtContextPath.setText(impl.getContextPath());
        initValues();
    }
    
    private void initValues() {
        listener = Wrapper.createComboBoxUpdater(handle, comServer, lblServer);
        
        run = ModelHandle.getActiveMapping(ActionProvider.COMMAND_RUN, project);
        debug = ModelHandle.getActiveMapping(ActionProvider.COMMAND_DEBUG, project);
        profile = ModelHandle.getActiveMapping("profile", project); // NOI18N

        isRunCompatible = checkMapping(run);
        isDebugCompatible = checkMapping(debug);
        isProfileCompatible = checkMapping(profile);

        oldUrl = isRunCompatible ? run.getProperties().getProperty(CLIENTURLPART) : //NOI18N
                                   debug.getProperties().getProperty(CLIENTURLPART); //NOI18N
        
        if (oldUrl != null) {
            txtRelativeUrl.setText(oldUrl);
        } else {
            oldUrl = ""; //NOI18N
        }
        txtRelativeUrl.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent arg0) {
                applyRelUrl();
            }

            public void removeUpdate(DocumentEvent arg0) {
                applyRelUrl();
            }

            public void changedUpdate(DocumentEvent arg0) {
                applyRelUrl();
            }
        });

        deployOnSaveUpdater = new CheckBoxUpdater(jCheckBoxDeployOnSave) {
            @Override
            public Boolean getValue() {
                String s = handle.getRawAuxiliaryProperty(MavenJavaEEConstants.HINT_DEPLOY_ON_SAVE, true);
                if (s != null) {
                    return Boolean.valueOf(s);
                } else {
                    return null;
                }
            }

            @Override
            public void setValue(Boolean value) {
                handle.setRawAuxiliaryProperty(MavenJavaEEConstants.HINT_DEPLOY_ON_SAVE, 
                        value == null ? null : Boolean.toString(value), true);
            }

            @Override
            public boolean getDefaultValue() {
                return true;
            }
        };
        
        String browser = (String)project.getProjectDirectory().getAttribute(PROP_SHOW_IN_BROWSER);
        boolean bool = browser != null ? Boolean.parseBoolean(browser) : true;
        cbBrowser.setSelected(bool);
        updateContextPathEnablement();
        addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                updateDoSEnablement();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }
    
    private void updateDoSEnablement() {
        String cos = handle.getRawAuxiliaryProperty(Constants.HINT_COMPILE_ON_SAVE, true);
        boolean enabled = cos != null && ("all".equalsIgnoreCase(cos) || "app".equalsIgnoreCase(cos)); // NOI18N
        jCheckBoxDeployOnSave.setEnabled(enabled);
        dosDescription.setEnabled(enabled);
    }
    
    private void loadComboModel() {
        String[] ids = Deployment.getDefault().getServerInstanceIDs(Collections.singletonList(J2eeModule.Type.WAR), module.getJ2eeProfile());
        Collection<Wrapper> col = new ArrayList<Wrapper>();
//        Wrapper selected = null;
        SessionContent sc = project.getLookup().lookup(SessionContent.class);
        if (sc != null && sc.getServerInstanceId() != null) {
            col.add(new Wrapper(ExecutionChecker.DEV_NULL, sc.getServerInstanceId()));
        } else {
            col.add(new Wrapper(ExecutionChecker.DEV_NULL));
        }
        for (int i = 0; i < ids.length; i++) {
            Wrapper wr = new Wrapper(ids[i]);
            col.add(wr);
//            if (selectedId.equals(ids[i])) {
//                selected = wr;
//            }
            
        }
        comServer.setModel(new DefaultComboBoxModel(col.toArray()));
//        if (selected != null) {
//            comServer.setSelectedItem(selected);
//        }
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
        org.openide.awt.Mnemonics.setLocalizedText(lblServer, org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "LBL_Server")); // NOI18N

        comServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comServerActionPerformed(evt);
            }
        });

        lblJ2EEVersion.setLabelFor(txtJ2EEVersion);
        org.openide.awt.Mnemonics.setLocalizedText(lblJ2EEVersion, org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "LBL_J2EE_Version")); // NOI18N

        txtJ2EEVersion.setEditable(false);

        lblContextPath.setLabelFor(txtContextPath);
        org.openide.awt.Mnemonics.setLocalizedText(lblContextPath, org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "LBL_Context_Path")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbBrowser, org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "LBL_Display_on_Run")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint1, org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "LBL_Hint1")); // NOI18N

        lblRelativeUrl.setLabelFor(txtRelativeUrl);
        org.openide.awt.Mnemonics.setLocalizedText(lblRelativeUrl, org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "LBL_Relative_URL")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint2, org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "LBL_Hint2")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblProfile, org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "WebRunCustomizerPanel.lblProfile.text")); // NOI18N

        comProfile.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxDeployOnSave, org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "WebRunCustomizerPanel.jCheckBoxDeployOnSave.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dosDescription, org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "WebRunCustomizerPanel.dosDescription.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBoxDeployOnSave)
                        .addContainerGap(368, Short.MAX_VALUE))
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
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 243, Short.MAX_VALUE))
                                    .addComponent(txtRelativeUrl, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblContextPath)
                                    .addComponent(lblJ2EEVersion)
                                    .addComponent(lblServer))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(comServer, javax.swing.GroupLayout.Alignment.TRAILING, 0, 387, Short.MAX_VALUE)
                                    .addComponent(txtContextPath, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtJ2EEVersion, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblProfile)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(comProfile, 0, 160, Short.MAX_VALUE)))))
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

        txtJ2EEVersion.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "WebRunCustomizerPanel.txtJ2EEVersion.AccessibleContext.accessibleDescription")); // NOI18N
        txtContextPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "WebRunCustomizerPanel.txtContextPath.AccessibleContext.accessibleDescription")); // NOI18N
        cbBrowser.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "WebRunCustomizerPanel.cbBrowser.AccessibleContext.accessibleDescription")); // NOI18N
        txtRelativeUrl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "WebRunCustomizerPanel.txtRelativeUrl.AccessibleContext.accessibleDescription")); // NOI18N
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
                txtContextPath.setText(NbBundle.getMessage(WebRunCustomizerPanel.class, "WebRunCustomizerPanel.contextPathDisabled"));
            }
        } else {
            if (!txtContextPath.isEnabled()) {
                txtContextPath.setEnabled(true);
                WebModuleImpl impl = moduleProvider.getWebModuleImplementation();
                if (oldContextPath != null) {
                    txtContextPath.setText(oldContextPath);
                } else {
                    txtContextPath.setText(impl.getContextPath());
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
                run.getProperties().setProperty( CLIENTURLPART, newUrl); //NOI18N
                ModelHandle.setUserActionMapping(run, handle.getActionMappings());
                handle.markAsModified(handle.getActionMappings());
            }
            if (isDebugCompatible) {
                debug.getProperties().setProperty( CLIENTURLPART, newUrl); //NOI18N
                ModelHandle.setUserActionMapping(debug, handle.getActionMappings());
                handle.markAsModified(handle.getActionMappings());
            }
            if (isProfileCompatible) {
                profile.getProperties().setProperty( CLIENTURLPART, newUrl); //NOI18N
                ModelHandle.setUserActionMapping(profile, handle.getActionMappings());
                handle.markAsModified(handle.getActionMappings());
            }
        }
    }

    void applyChangesInAWT() {
        assert SwingUtilities.isEventDispatchThread();
        boolean bool = cbBrowser.isSelected();
        try {
            project.getProjectDirectory().setAttribute(PROP_SHOW_IN_BROWSER, bool ? null : Boolean.FALSE.toString());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        // USG logging
        Object obj = comServer.getSelectedItem();
        if (obj != null) {
            LogRecord record = new LogRecord(Level.INFO, "USG_PROJECT_CONFIG_MAVEN_SERVER");  //NOI18N
            record.setLoggerName(POHImpl.USG_LOGGER_NAME);
            record.setParameters(new Object[] { obj.toString() });
            POHImpl.USG_LOGGER.log(record);
        }
    }

    //this megod is called after the model was saved.
    void applyChanges() {
        assert !SwingUtilities.isEventDispatchThread();

        //#109507 workaround
        SessionContent sc = project.getLookup().lookup(SessionContent.class);
        if (listener.getValue() != null) {
            sc.setServerInstanceId(null);
        }
        //TODO - not sure this is necessary since the PoHImpl listens on project changes.
        //any save of teh project shall effectively caus ethe module server change..
        POHImpl poh = project.getLookup().lookup(POHImpl.class);
        poh.hackModuleServerChange();
        moduleProvider = project.getLookup().lookup(WebModuleProviderImpl.class);
        if (txtContextPath.isEnabled()) {
            String contextPath = txtContextPath.getText().trim();
            WebModuleImpl impl = moduleProvider.getWebModuleImplementation();
            impl.setContextPath(contextPath);
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



    private void replaceDependency(String oldArt, String newArt) {
        Dependency d = ModelUtils.checkModelDependency(handle.getPOMModel(), "javax", oldArt, false);
        if (d != null) {
            d.setArtifactId(newArt);
        }
    }

    public static boolean isDeployOnSave(Project project) {
        //try to apply the hint if it exists.
        AuxiliaryProperties prop = project.getLookup().lookup(AuxiliaryProperties.class);
        String deployOnSave = prop.get(MavenJavaEEConstants.HINT_DEPLOY_ON_SAVE, true);
        if (deployOnSave != null) {
            return Boolean.parseBoolean(deployOnSave);
        } else {
            return true;
        }
    }
    
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import org.netbeans.modules.maven.j2ee.POHImpl;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.j2ee.ExecutionChecker;
import static org.netbeans.modules.maven.j2ee.ExecutionChecker.CLIENTURLPART;
import org.netbeans.modules.maven.j2ee.SessionContent;
import org.netbeans.modules.maven.j2ee.Wrapper;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Exceptions;

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
    private ComboBoxUpdater<Wrapper> listener;
    
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
                if (prf.equals(Profile.JAVA_EE_6_FULL)) {
                    val = "Full";
                }
                return super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
            }
        });

        Profile p = module.getJ2eeProfile();
        String version = p.equals(Profile.JAVA_EE_6_WEB) ? Profile.JAVA_EE_6_FULL.toPropertiesString() : p.toPropertiesString();
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
                        props.setProperty(Constants.HINT_J2EE_VERSION, p.toPropertiesString());
                        handle.markAsModified(handle.getPOMModel());
                    } else {
                        Properties props = root.getProperties();
                        if (props != null && props.getProperty(Constants.HINT_J2EE_VERSION) != null) {
                            props.setProperty(Constants.HINT_J2EE_VERSION, null);
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

        initValues();
        txtContextPath.setText(impl.getContextPath());
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

        
        String browser = (String)project.getProjectDirectory().getAttribute(PROP_SHOW_IN_BROWSER);
        boolean bool = browser != null ? Boolean.parseBoolean(browser) : true;
        cbBrowser.setSelected(bool);
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

        lblServer.setLabelFor(comServer);
        org.openide.awt.Mnemonics.setLocalizedText(lblServer, org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "LBL_Server")); // NOI18N

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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cbBrowser)
                    .add(lblHint1)
                    .add(layout.createSequentialGroup()
                        .add(lblRelativeUrl)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(lblHint2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 237, Short.MAX_VALUE))
                            .add(txtRelativeUrl, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblContextPath)
                            .add(lblJ2EEVersion)
                            .add(lblServer))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, comServer, 0, 374, Short.MAX_VALUE)
                            .add(txtContextPath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(txtJ2EEVersion, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblProfile)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comProfile, 0, 159, Short.MAX_VALUE)))))
                .add(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblServer)
                    .add(comServer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblJ2EEVersion)
                    .add(txtJ2EEVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblProfile)
                    .add(comProfile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblContextPath)
                    .add(txtContextPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(20, 20, 20)
                .add(cbBrowser)
                .add(16, 16, 16)
                .add(lblHint1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblRelativeUrl)
                    .add(txtRelativeUrl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblHint2)
                .addContainerGap(97, Short.MAX_VALUE))
        );

        txtJ2EEVersion.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "WebRunCustomizerPanel.txtJ2EEVersion.AccessibleContext.accessibleDescription")); // NOI18N
        txtContextPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "WebRunCustomizerPanel.txtContextPath.AccessibleContext.accessibleDescription")); // NOI18N
        cbBrowser.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "WebRunCustomizerPanel.cbBrowser.AccessibleContext.accessibleDescription")); // NOI18N
        txtRelativeUrl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebRunCustomizerPanel.class, "WebRunCustomizerPanel.txtRelativeUrl.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

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
        if (map.getProperties().containsKey(Constants.ACTION_PROPERTY_DEPLOY)) {
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

    String applyChangesInAWT() {
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
        return txtContextPath.getText().trim();
    }

    //this megod is called after the model was saved.
    void applyChanges(String contextPath) {
        assert !SwingUtilities.isEventDispatchThread();

        //#109507 workaround
        SessionContent sc = project.getLookup().lookup(SessionContent.class);
        sc.setServerInstanceId(null);
        //TODO - not sure this is necessary since the PoHImpl listens on project changes.
        //any save of teh project shall effectively caus ethe module server change..
        POHImpl poh = project.getLookup().lookup(POHImpl.class);
        poh.setContextPath(contextPath);
        poh.hackModuleServerChange();
        moduleProvider = project.getLookup().lookup(WebModuleProviderImpl.class);
        
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbBrowser;
    private javax.swing.JComboBox comProfile;
    private javax.swing.JComboBox comServer;
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

}

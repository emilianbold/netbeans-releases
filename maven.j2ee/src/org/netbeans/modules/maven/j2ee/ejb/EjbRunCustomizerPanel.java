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

package org.netbeans.modules.maven.j2ee.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import org.apache.maven.profiles.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.j2ee.POHImpl;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.maven.j2ee.ExecutionChecker;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Properties;


/**
 *
 * @author  mkleint
 */
public class EjbRunCustomizerPanel extends javax.swing.JPanel {

    private Project project;
    private ModelHandle handle;
    private EjbJar module;
    private ComboBoxUpdater<Wrapper> listener;

    /**
     * Creates new form EjbRunCustomizerPanel
     */
    public EjbRunCustomizerPanel(ModelHandle handle, Project project) {
        initComponents();
        this.handle = handle;
        this.project = project;
        module = EjbJar.getEjbJar(project.getProjectDirectory());
        loadComboModel();
        if (module != null) {
            txtJ2EEVersion.setText(module.getJ2eePlatformVersion());
        }
        initValues();
    }

    private void initValues() {
        listener = new ComboBoxUpdater<Wrapper>(comServer, lblServer) {

            public Wrapper getDefaultValue() {
                Wrapper wr = null;
                String id = handle.getProject().getProperties().getProperty(Constants.HINT_DEPLOY_J2EE_SERVER_ID);
                if (id != null) {
                    wr = findWrapperByInstance(id);
                }
                if (wr == null) {
                    String str = handle.getProject().getProperties().getProperty(Constants.HINT_DEPLOY_J2EE_SERVER);
                    if (str == null) {
                        str = handle.getProject().getProperties().getProperty(Constants.HINT_DEPLOY_J2EE_SERVER_OLD);
                    }

                    if (str != null) {
                        wr = findWrapperByType(str);
                    }
                }
                return wr;
            }

            public Wrapper getValue() {
                Wrapper wr = null;
                String id = handle.getNetbeansPrivateProfile(false).getProperties().getProperty(Constants.HINT_DEPLOY_J2EE_SERVER_ID);
                if (id != null) {
                    wr = findWrapperByInstance(id);
                }
                if (wr == null) {
                    POMModel model = handle.getPOMModel();
                    Properties props = model.getProject().getProperties();
                    String str = null;
                    if (props != null) {
                        str = props.getProperty(Constants.HINT_DEPLOY_J2EE_SERVER);
                    }
                    if (str == null) {
                        org.netbeans.modules.maven.model.pom.Profile prof = handle.getNetbeansPublicProfile(false);
                        if (prof != null) {
                            props = prof.getProperties();
                            if (props != null) {
                                str = props.getProperty(Constants.HINT_DEPLOY_J2EE_SERVER_OLD);
                            }
                        }
                    }
                    if (str != null) {
                        wr = findWrapperByType(str);
                    }
                }
                return wr;
            }

            public void setValue(Wrapper wr) {
                if (wr == null) {
                    return;
                }
                String sID = wr.getServerID();
                String iID = wr.getServerInstanceID();
                Profile privateProf = handle.getNetbeansPrivateProfile(false);
                //remove old deprecated data.
                org.netbeans.modules.maven.model.pom.Profile pub = handle.getNetbeansPublicProfile(false);
                if (pub != null) {
                    Properties props = pub.getProperties();
                    if (props != null) {
                        pub.getProperties().setProperty(Constants.HINT_DEPLOY_J2EE_SERVER_OLD, null);
                    }
                }
                POMModel model = handle.getPOMModel();
                if (ExecutionChecker.DEV_NULL.equals(iID)) {
                    //check if someone moved the property to netbeans-private profile, remove from there then.
                    Properties props = model.getProject().getProperties();
                    if (privateProf != null) {
                        if (privateProf.getProperties().getProperty(Constants.HINT_DEPLOY_J2EE_SERVER) != null) {
                            privateProf.getProperties().remove(Constants.HINT_DEPLOY_J2EE_SERVER);
                        } else {
                            if (props != null) {
                                props.setProperty(Constants.HINT_DEPLOY_J2EE_SERVER, null);
                                handle.markAsModified(handle.getPOMModel());
                            }
                        }
                        privateProf.getProperties().remove(Constants.HINT_DEPLOY_J2EE_SERVER_ID);
                        handle.markAsModified(handle.getProfileModel());
                    } else {
                        if (props != null) {
                            props.setProperty(Constants.HINT_DEPLOY_J2EE_SERVER, null);
                            handle.markAsModified(handle.getPOMModel());
                        }
                    }
                } else {
                    Properties props = model.getProject().getProperties();
                    if (props == null) {
                        props = model.getFactory().createProperties();
                        model.getProject().setProperties(props);
                    }

                    //check if someone moved the property to netbeans-private profile, remove from there then.
                    if (privateProf != null && privateProf.getProperties().getProperty(Constants.HINT_DEPLOY_J2EE_SERVER) != null) {
                        privateProf.getProperties().setProperty(Constants.HINT_DEPLOY_J2EE_SERVER, sID);
                    } else {
                        props.setProperty(Constants.HINT_DEPLOY_J2EE_SERVER, sID);
                        handle.markAsModified(handle.getPOMModel());
                    }
                    handle.getNetbeansPrivateProfile().getProperties().setProperty(Constants.HINT_DEPLOY_J2EE_SERVER_ID, iID);
                    handle.markAsModified(handle.getProfileModel());
                }
            }
        };
    }

    private Wrapper findWrapperByInstance(String instanceId) {
        for (int i = 0; i < comServer.getModel().getSize(); i++) {
            Wrapper wr = (Wrapper) comServer.getModel().getElementAt(i);
            if (instanceId.equals(wr.getServerInstanceID())) {
                return wr;
            }
        }
        return null;
    }

    private Wrapper findWrapperByType(String serverId) {
        for (int i = 0; i < comServer.getModel().getSize(); i++) {
            Wrapper wr = (Wrapper) comServer.getModel().getElementAt(i);
            if (serverId.equals(wr.getServerID())) {
                return wr;
            }
        }
        return null;
    }

    private void loadComboModel() {
        String[] ids = Deployment.getDefault().getServerInstanceIDs(new Object[]{J2eeModule.EJB});
        Collection<Wrapper> col = new ArrayList<Wrapper>();
//        Wrapper selected = null;
        col.add(new Wrapper(ExecutionChecker.DEV_NULL));
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

    void applyChanges() {
        //#109507 workaround
        POHImpl poh = project.getLookup().lookup(POHImpl.class);
        poh.hackModuleServerChange();
        EjbModuleProviderImpl moduleProvider = project.getLookup().lookup(EjbModuleProviderImpl.class);

        moduleProvider.loadPersistedServerId();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblServer = new javax.swing.JLabel();
        comServer = new javax.swing.JComboBox();
        lblJ2EEVersion = new javax.swing.JLabel();
        txtJ2EEVersion = new javax.swing.JTextField();

        lblServer.setText(org.openide.util.NbBundle.getMessage(EjbRunCustomizerPanel.class, "LBL_Server")); // NOI18N

        lblJ2EEVersion.setText(org.openide.util.NbBundle.getMessage(EjbRunCustomizerPanel.class, "LBL_J2EE_Version")); // NOI18N

        txtJ2EEVersion.setEditable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblJ2EEVersion)
                    .add(lblServer))
                .add(14, 14, 14)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(comServer, 0, 277, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, txtJ2EEVersion, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE))
                .addContainerGap())
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
                    .add(txtJ2EEVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(239, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox comServer;
    private javax.swing.JLabel lblJ2EEVersion;
    private javax.swing.JLabel lblServer;
    private javax.swing.JTextField txtJ2EEVersion;
    // End of variables declaration//GEN-END:variables

    private class Wrapper {

        private String id;

        public Wrapper(String serverid) {
            id = serverid;
        }

        public String getServerInstanceID() {
            return id;
        }

        public String getServerID() {
            if (ExecutionChecker.DEV_NULL.equals(id)) {
                return ExecutionChecker.DEV_NULL;
            }
            return POHImpl.privateGetServerId(id);
        }

        @Override
        public String toString() {
            if (ExecutionChecker.DEV_NULL.equals(id)) {
                return org.openide.util.NbBundle.getMessage(EjbRunCustomizerPanel.class, "MSG_No_Server");
            }
            ServerInstance si = Deployment.getDefault().getServerInstance(id);
            if (si != null) {
                try {
                    return si.getDisplayName();
                } catch (InstanceRemovedException ex) {
                    Logger.getLogger(EjbRunCustomizerPanel.class.getName()).log(Level.FINE, "", ex);
                }
            }
            return "";
        }
    }
}
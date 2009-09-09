/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerListUI;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.NativeProjectProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configurations;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

public class RemoteDevelopmentAction extends AbstractAction implements Presenter.Menu, Presenter.Popup {

    /** Key for remembering project in JMenuItem
     */
    private static final String HOST_ENV = "org.netbeans.modules.cnd.makeproject.ui.RemoteHost"; // NOI18N
    private static final String CONF = "org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration"; // NOI18N
    private static final String PROJECT = "org.netbeans.modules.cnd.makeproject.api.configurations.MakeProject"; // NOI18N
    private JMenu subMenu;
    private MakeProject project;

    public RemoteDevelopmentAction(MakeProject project) {
        super(NbBundle.getMessage(RemoteDevelopmentAction.class, "LBL_RemoteDevelopmentAction_Name"), // NOI18N
                null);
        this.project = project;
    }

    public void actionPerformed(java.awt.event.ActionEvent ev) {
        // no operation
    }

    public JMenuItem getPopupPresenter() {
        createSubMenu();
        return subMenu;
    }

    public JMenuItem getMenuPresenter() {
        createSubMenu();
        return subMenu;
    }

    private void createSubMenu() {
        if (subMenu == null) {
            String label = NbBundle.getMessage(RemoteDevelopmentAction.class, "LBL_RemoteDevelopmentAction_Name"); // NOI18N
            subMenu = new JMenu(label);
        }

        subMenu.removeAll();
        
        MakeConfiguration mconf = project.getActiveConfiguration();
        ExecutionEnvironment currExecEnv = project.getDevelopmentHostExecutionEnvironment();
        if (mconf == null || currExecEnv == null) {
            return;
        }

        ActionListener jmiActionListener = new MenuItemActionListener();
        for (ServerRecord record : ServerList.getRecords()) {
            JRadioButtonMenuItem jmi = new JRadioButtonMenuItem(record.getServerDisplayName(), currExecEnv.equals(record.getExecutionEnvironment()));
            subMenu.add(jmi);
            jmi.putClientProperty(HOST_ENV, record.getExecutionEnvironment());
            jmi.putClientProperty(CONF, mconf);
            jmi.putClientProperty(PROJECT, project);
            jmi.addActionListener(jmiActionListener);
        }

        // Now add the Manage... action. Do it in it's own menu item othervise it will get shifted to the right.
        subMenu.add(new JSeparator());
        JMenuItem managePlatformsItem = new JMenuItem(NbBundle.getMessage(RemoteDevelopmentAction.class, "LBL_ManagePlatforms_Name")); // NOI18N
        subMenu.add(managePlatformsItem);
        managePlatformsItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ServerListUI.showServerListDialog();
            }
        });
    }

    private static class MenuItemActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JMenuItem) {
                JMenuItem jmi = (JMenuItem) e.getSource();
                ExecutionEnvironment execEnv = (ExecutionEnvironment) jmi.getClientProperty(HOST_ENV);
                MakeConfiguration mconf = (MakeConfiguration) jmi.getClientProperty(CONF);
                if (mconf != null && execEnv != null) {
                    ServerRecord record = ServerList.get(execEnv);
                    if (!record.isSetUp()) {
                        if (!record.setUp()) {
                            return;
                        }
                    }
                    DevelopmentHostConfiguration dhc = new DevelopmentHostConfiguration(execEnv);
                    DevelopmentHostConfiguration oldDhc = mconf.getDevelopmentHost();
                    mconf.setDevelopmentHost(dhc);
                    mconf.setCompilerSet(new CompilerSet2Configuration(dhc));
//                    PlatformConfiguration platformConfiguration = mconf.getPlatform();
//                    platformConfiguration.propertyChange(new PropertyChangeEvent(
//                            jmi, DevelopmentHostConfiguration.PROP_DEV_HOST, oldDhc, dhc));
                    Project project = (Project) jmi.getClientProperty(PROJECT);
                    NativeProjectProvider npp = project.getLookup().lookup(NativeProjectProvider.class);
                    npp.propertyChange(new PropertyChangeEvent(this, Configurations.PROP_ACTIVE_CONFIGURATION, null, mconf));

                    ConfigurationDescriptorProvider configurationDescriptorProvider =
                            project.getLookup().lookup(ConfigurationDescriptorProvider.class);
                    ConfigurationDescriptor configurationDescriptor = configurationDescriptorProvider.getConfigurationDescriptor();
                    configurationDescriptor.setModified(true); 
                }
            }
        }
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.tha.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.MissingResourceException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configurations;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.tha.support.THAProjectSupport;
import org.netbeans.modules.cnd.tha.ui.THAIndicatorDelegator;
import org.netbeans.modules.cnd.tha.ui.THAIndicatorsTopComponent;
import org.netbeans.modules.dlight.perfan.tha.api.THAConfiguration;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author mt154047
 */
public final class THAMainProjectAction extends AbstractAction implements PropertyChangeListener {

    private Project currentProject = null;
    private final Action sensorMainAction;

    public THAMainProjectAction() {
        super(loc("LBL_THAMainProjectAction")); // NOI18N
        sensorMainAction = MainProjectSensitiveActions.mainProjectSensitiveAction(new ProjectActionPerformerImpl(), null, null);
        sensorMainAction.addPropertyChangeListener(this);
        putValue("command", "THAProfile"); // NOI18N
        putValue(Action.SHORT_DESCRIPTION, loc("HINT_THAMainProjectAction")); // NOI18N
        putValue("iconBase", "org/netbeans/modules/cnd/tha/resources/bomb24.png"); // NOI18N
        putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/tha/resources/bomb16.png", false)); // NOI18N
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                setEnabled(isEnabled());
            }
        });

    }

    private static String loc(String key, String... params) {
        try {
            return NbBundle.getMessage(THAActionsProvider.class, key, params);
        } catch (MissingResourceException e) {
            e.printStackTrace();
            return key;
        }
    }

    @Override
    public boolean isEnabled() {
        return sensorMainAction.isEnabled();
    }

    public void actionPerformed(ActionEvent e) {
        if (!THAProjectSupport.isSupported(currentProject)) {
            return;
        }
        //show dialog here with the configuration
        JButton startB = new JButton("Start");//NOI18N
        Object[] options = new Object[]{DialogDescriptor.CANCEL_OPTION, startB};
        THAConfigurationPanel configurationPanel = new THAConfigurationPanel();
        DialogDescriptor dialogDescriptor = new DialogDescriptor(configurationPanel, "Configure Profile", true, options, startB, DialogDescriptor.BOTTOM_ALIGN, null, null);//NOI18N
        Object ret = DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (ret != startB) {
            return;
//                    reconfigurator.reconfigure(panel.getCFlags(), panel.getCppFlags());
        }

        final THAConfiguration thaConfiguration = configurationPanel.getTHAConfiguration();
        final THAActionsProvider provider = new THAActionsProvider(currentProject, thaConfiguration);
        DLightExecutorService.submit(new Runnable() {

            public void run() {
                if (!THAActionsProvider.start(provider.dlightTargetListener, currentProject, thaConfiguration)) {
                    return;
                }
                UIThread.invoke(new Runnable() {

                    public void run() {
                        THAIndicatorsTopComponent topComponent = THAIndicatorDelegator.getInstance().getProjectComponent(provider, currentProject, thaConfiguration);
                        topComponent.open();
                        topComponent.requestActive();
                    }
                });

            }
        }, "Start THA");//NOI18N

    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("mainProject") && evt.getNewValue() == null) {
            sensorMainAction.setEnabled(false);
            setEnabled(isEnabled());
            return;
        }
        if ("enabled".equals(evt.getPropertyName())) {
            setEnabled(isEnabled());
            System.out.println("Source=" + evt.getSource()); // NOI18N
        }
        if (!evt.getPropertyName().equals("mainProject") && !Configurations.PROP_ACTIVE_CONFIGURATION.equals(evt.getPropertyName())) { // NOI18N
            return;
        }
        sensorMainAction.setEnabled(isEnabledFor());
        setEnabled(isEnabled());
    }

    private boolean isEnabledFor() {
        if (currentProject == null) {
            return false;
        }
        NativeProject nativeProject = currentProject.getLookup().lookup(NativeProject.class);

        if (nativeProject == null) {
            return false;
        }
        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(currentProject);
        MakeConfiguration mc = mcd.getActiveConfiguration();
        return THAProjectSupport.isSupported(currentProject);
    }

    private final class ProjectActionPerformerImpl implements ProjectActionPerformer {

        public boolean enable(Project project) {
            if (project != currentProject && currentProject != null) {
                //remove property change listener
                MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(THAMainProjectAction.this.currentProject);
                if (mcd != null){
                    MakeConfiguration mc = mcd.getActiveConfiguration();
                    mc.removePropertyChangeListener(THAMainProjectAction.this);
                    Configurations c = mcd.getConfs();
                    if (c != null){
                        c.removePropertyChangeListener(THAMainProjectAction.this);
                    }
                }
            }

            boolean isEnabled = THAProjectSupport.isSupported(project);
            if (isEnabled) {
                NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);

                if (nativeProject == null) {
                    return false;
                }
                THAMainProjectAction.this.currentProject = project;
                MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(THAMainProjectAction.this.currentProject);
                MakeConfiguration mc = mcd.getActiveConfiguration();
                mc.addPropertyChangeListener(THAMainProjectAction.this);
                Configurations c = mcd.getConfs();
                c.addPropertyChangeListener(THAMainProjectAction.this);

            } else {
                THAMainProjectAction.this.currentProject = null;
            }

            return isEnabled;
        }

        public void perform(Project project) {
        }
    }
}

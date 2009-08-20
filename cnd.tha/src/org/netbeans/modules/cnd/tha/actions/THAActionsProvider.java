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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.tha.support.THAProjectSupport;
import org.netbeans.modules.cnd.tha.support.THAConfigurationImpl;
import org.netbeans.modules.cnd.tha.ui.THAIndicatorDelegator;
import org.netbeans.modules.cnd.tha.ui.THAIndicatorsTopComponent;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.api.execution.DLightTargetListener;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

public final class THAActionsProvider implements DLightTargetListener {

    private Action startAndAnalyze;
    private Action start;
    private Action enableCollect;
    private Action stop;
    private Action enableDataRaces;
    private Action enableDeadlocks;
    private int pid;
    private DLightTarget target;
    private static Action startThreadAnalyzerConfiguration;

    static {
        startThreadAnalyzerConfiguration = MainProjectSensitiveActions.mainProjectSensitiveAction(new ProjectActionPerformer() {

            public synchronized boolean enable(final Project project) {
                return THAProjectSupport.isSupported(project);
            }

            public void perform(final Project project) {
                if (!THAProjectSupport.isSupported(project)) {
                    return;
                }
                THAIndicatorsTopComponent topComponent = THAIndicatorDelegator.getInstance().getProjectComponent(project);
                topComponent.open();
                topComponent.requestActive();
            }
        }, loc("LBL_THAMainProjectAction"), null); // NOI18N

        startThreadAnalyzerConfiguration.putValue("command", "THAProfile"); // NOI18N
        startThreadAnalyzerConfiguration.putValue(Action.SHORT_DESCRIPTION, loc("HINT_THAMainProjectAction")); // NOI18N
        startThreadAnalyzerConfiguration.putValue("iconBase", "org/netbeans/modules/cnd/tha/resources/bomb24.png"); // NOI18N
        startThreadAnalyzerConfiguration.putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/tha/resources/bomb16.png", false)); // NOI18N
    }
    private RemoveInstrumentationAction removeInstrumentation;
    private final Project project;
    private final static Map<Project, THAActionsProvider> cache = new HashMap<Project, THAActionsProvider>();

    private THAActionsProvider(Project project) {
        this.project = project;
        THAProjectSupport.getSupportFor(project).addDLightTargetListener(this);
        initActions();
    }

    public static final synchronized THAActionsProvider getSupportFor(Project project) {
        if (!THAProjectSupport.isSupported(project)){
            return null;
        }

        if (cache.containsKey(project)) {
            return cache.get(project);
        }



        THAActionsProvider support = new THAActionsProvider(project);
        cache.put(project, support);

        return support;
    }

    public static Action getStartTHAConfigurationAction() {
        return startThreadAnalyzerConfiguration;
    }

    public Action getStartThreadAnalysisAction() {
        return startAndAnalyze;
    }

    public Action getEnableCollectAction() {
        return enableCollect;
    }

    public Action getStopAction(){
        return stop;
    }

    public Action getEnableDataraceAction() {
        return enableDataRaces;
    }

    public Action getEnableDeadlockAction() {
        return enableDeadlocks;
    }

    public Action getStartAction() {
        return start;
    }

    public Action getRemoveInstrumentationAction() {
        return removeInstrumentation;
    }

    private void initActions() {
        removeInstrumentation = new RemoveInstrumentationAction();
        startAndAnalyze = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (project == null) {
                    return;
                }

                THAProjectSupport support = THAProjectSupport.getSupportFor(project);

                if (support == null) {
                    return;
                }

                if (!support.isConfiguredForInstrumentation()) {
                    boolean instrResult = support.doInstrumentation();
                    if (!instrResult) {
                        return;
                    }
                }

                startAndAnalyze.setEnabled(false);
                start.setEnabled(false);
                enableCollect.setEnabled(false);
                

                // Initiate RUN ...
                ActionProvider ap = project.getLookup().lookup(ActionProvider.class);

                if (ap != null) {
                    if (Arrays.asList(ap.getSupportedActions()).contains("custom.action")) { // NOI18N
                        ap.invokeAction("custom.action", Lookups.fixed(THAConfigurationImpl.create(true))); // NOI18N
                    }
                }

            }
        };
        startAndAnalyze.setEnabled(THAProjectSupport.getSupportFor(project) != null);
        if (startAndAnalyze.isEnabled()) {
            removeInstrumentation.setProject(project);
        }
        startAndAnalyze.putValue("command", "THAProfile"); // NOI18N
        startAndAnalyze.putValue(Action.SHORT_DESCRIPTION, loc("HINT_THAMainProjectAction")); // NOI18N
        startAndAnalyze.putValue("iconBase", "org/netbeans/modules/cnd/tha/resources/bomb24.png"); // NOI18N
        startAndAnalyze.putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/tha/resources/bomb24.png", false)); // NOI18N
        start = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (project == null) {
                    return;
                }

                THAProjectSupport support = THAProjectSupport.getSupportFor(project);

                if (support == null) {
                    return;
                }

                if (!support.isConfiguredForInstrumentation()) {
                    boolean instrResult = support.doInstrumentation();
                    if (!instrResult) {
                        return;
                    }
                }
                startAndAnalyze.setEnabled(false);
                start.setEnabled(false);
                enableCollect.setEnabled(true);


                // Initiate RUN ...
                ActionProvider ap = project.getLookup().lookup(ActionProvider.class);

                if (ap != null) {
                    if (Arrays.asList(ap.getSupportedActions()).contains("custom.action")) { // NOI18N
                        ap.invokeAction("custom.action", Lookups.fixed(THAConfigurationImpl.create(false))); // NOI18N
                    }
                }

            }
        };
        start.setEnabled(THAProjectSupport.getSupportFor(project) != null);
        if (start.isEnabled()) {
            removeInstrumentation.setProject(project);
        }


        start.putValue("command", "THAProfile"); // NOI18N
        start.putValue(Action.SHORT_DESCRIPTION, loc("HINT_THAMainProjectAction")); // NOI18N
        start.putValue("iconBase", "org/netbeans/modules/cnd/tha/resources/runProject24.png"); // NOI18N
        start.putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/tha/resources/runProject24.png", false)); // NOI18N

        enableCollect = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                startCollect();
            }
        };
        stop = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (project == null) {
                    return;
                }

                THAProjectSupport support = THAProjectSupport.getSupportFor(project);
                support.stop();

            }
        };
        stop.setEnabled(false);


        stop.putValue("command", "THAProfileStop"); // NOI18N
        stop.putValue(Action.SHORT_DESCRIPTION, loc("HINT_THAMainProjectAction")); // NOI18N
        stop.putValue("iconBase", "org/netbeans/modules/cnd/tha/resources/Kill24.gif"); // NOI18N
        stop.putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/tha/resources/Kill24.gif", false)); // NOI18N

        enableDataRaces = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
            }
        };
        enableDataRaces.setEnabled(false);
        enableDeadlocks = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
            }
        };
        enableDeadlocks.setEnabled(false);



    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(THAActionsProvider.class, key, params);
    }

    private final class RemoveInstrumentationAction extends AbstractAction implements PropertyChangeListener {

        private Project lastValidatedProject;
        private volatile Future statusVerifyTask;

        public RemoveInstrumentationAction() {
            super(loc("LBL_THARemoveInstrumentation")); // NOI18N
            putValue(Action.SHORT_DESCRIPTION, loc("HINT_THARemoveInstrumentation")); // NOI18N
            lastValidatedProject = null;
            setEnabled(false);
        }

        public synchronized void setProject(final Project project) {
            if (lastValidatedProject == project) {
                return;
            }

            if (lastValidatedProject != null) {
                THAProjectSupport support = THAProjectSupport.getSupportFor(lastValidatedProject);
                if (support != null) {
                    support.removeProjectConfigurationChangedListener(this);
                }
            }

            if (statusVerifyTask != null) {
                statusVerifyTask.cancel(true);
                statusVerifyTask = null;
            }

            setEnabled(false);

            lastValidatedProject = project;

            if (project != null) {
                THAProjectSupport support = THAProjectSupport.getSupportFor(project);

                if (support != null) {
                    support.addProjectConfigurationChangedListener(this);
                }

                revalidate(lastValidatedProject);
            }
        }

        public void actionPerformed(ActionEvent e) {
            assert lastValidatedProject != null;

            if (THAProjectSupport.getSupportFor(lastValidatedProject).undoInstrumentation()) {
                setEnabled(false);
            }
        }

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            revalidate(lastValidatedProject);
        }

        private void revalidate(final Project lastValidatedProject) {
            setEnabled(false);

            if (statusVerifyTask != null) {
                statusVerifyTask.cancel(true);
            }

            final THAProjectSupport support = THAProjectSupport.getSupportFor(lastValidatedProject);

            if (support == null) {
                setEnabled(false);
            } else {
                statusVerifyTask = DLightExecutorService.submit(new Callable<Boolean>() {

                    public Boolean call() throws Exception {
                        boolean result = false;

                        try {
                            result = support.isConfiguredForInstrumentation();
                        } catch (Exception ex) {
                        }

                        setEnabled(result);
                        return result;
                    }
                }, "RemoveInstrumentationAction state verification task"); // NOI18N
            }
        }
    }

    public void targetStateChanged(DLightTargetChangeEvent event) {
        this.target = event.target;
        switch (event.state) {
            case INIT:
            case STARTING:
                break;
            case RUNNING:
                targetStarted(event.status);
                break;
            case FAILED:
            case STOPPED:
                targetFailed();
                break;
            case TERMINATED:
                targetFinished(event.status);
                break;
            case DONE:
                targetFinished(event.status);
                break;
        }
    }

    public void startCollect() {
        if (0 < pid) {
            CommonTasksSupport.sendSignal(target.getExecEnv(), pid, "USR1", null); // NOI18N
        }
    }

    private void targetStarted(int pid) {
        //means stop button should be enabled
        this.pid = pid;
        //enableCollect.setEnabled(false);
        stop.setEnabled(true);

    }

    private void targetFailed() {
        //back all as it was
        start.setEnabled(THAProjectSupport.getSupportFor(project) != null);
        startAndAnalyze.setEnabled(THAProjectSupport.getSupportFor(project) != null);
        enableCollect.setEnabled(THAProjectSupport.getSupportFor(project) != null);
        stop.setEnabled(false);
    }

    private void targetFinished(Integer status) {
        start.setEnabled(THAProjectSupport.getSupportFor(project) != null);
        startAndAnalyze.setEnabled(THAProjectSupport.getSupportFor(project) != null);
        enableCollect.setEnabled(THAProjectSupport.getSupportFor(project) != null);
        stop.setEnabled(false);
    }

    private class EnableCollectAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand() == null) {
                return;//nothing to do
            } else {
                //use it as a pid and send a signal
            }
        }
    }
}

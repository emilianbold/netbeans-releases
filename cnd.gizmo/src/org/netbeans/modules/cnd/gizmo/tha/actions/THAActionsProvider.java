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
package org.netbeans.modules.cnd.gizmo.tha.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.gizmo.GizmoToolsController;
import org.netbeans.modules.cnd.gizmo.tha.THAProjectSupport;
import org.netbeans.modules.cnd.gizmo.tha.THAConfigurationImpl;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

public final class THAActionsProvider {

    private final static THAActionsProvider instance = new THAActionsProvider();
    private Action startThreadAnalyzer;
    private RemoveInstrumentationAction removeInstrumentation;

    private THAActionsProvider() {
        initActions();
    }

    public static THAActionsProvider getDefault() {
        return instance;
    }

    public Action getStartThreadAnalysisAction() {
        return startThreadAnalyzer;
    }

    public Action getRemoveInstrumentationAction() {
        return removeInstrumentation;
    }

    private void initActions() {
        startThreadAnalyzer = MainProjectSensitiveActions.mainProjectSensitiveAction(new ProjectActionPerformer() {

            public synchronized boolean enable(final Project project) {
                boolean result = THAProjectSupport.getSupportFor(project) != null;

                if (result) {
                    removeInstrumentation.setProject(project);
                }

                return result;
            }

            public void perform(final Project project) {
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

                // Enable THATool ...
                GizmoToolsController.getDefault().enableTool(project, "dlight.tool.tha"); // NOI18N

                // TODO: need a way to disable the THA tool (not to show-up on common run)
                //



                // Initiate RUN ...
                ActionProvider ap = project.getLookup().lookup(ActionProvider.class);

                if (ap != null) {
                    if (Arrays.asList(ap.getSupportedActions()).contains("run")) { // NOI18N
                        ap.invokeAction("run", Lookups.fixed(THAConfigurationImpl.getDefault())); // NOI18N
                    }
                }


                // Disable ???
            }
        }, loc("LBL_THAMainProjectAction"), null); // NOI18N

        startThreadAnalyzer.putValue("command", "THAProfile"); // NOI18N
        startThreadAnalyzer.putValue(Action.SHORT_DESCRIPTION, loc("HINT_THAMainProjectAction")); // NOI18N
        startThreadAnalyzer.putValue("iconBase", "org/netbeans/modules/cnd/gizmo/resources/bomb24.png"); // NOI18N
        startThreadAnalyzer.putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/gizmo/resources/bomb16.png", false)); // NOI18N

        removeInstrumentation = new RemoveInstrumentationAction();
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
}

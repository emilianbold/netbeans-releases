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
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.event.EventListenerList;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.tha.support.THAProjectSupport;
import org.netbeans.modules.cnd.tha.ui.THAIndicatorDelegator;
import org.netbeans.modules.cnd.tha.ui.THAIndicatorsTopComponent;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.api.execution.DLightTargetListener;
import org.netbeans.modules.dlight.perfan.tha.api.THAConfiguration;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

public final class THAActionsProvider {
    public static final String SUSPEND_COMMAND = "THAProfileSuspend";//NOI18N
    public static final String RESUME_COMMAND = "THAProfileResume";//NOI18N
    public static final String STOP_COMMAND = "THAProfileStop";//NOI18N
    private Action suspendDataCollection;
    private Action resumeDataCollection;
    private Action stop;
    private int pid;
    private DLightTarget target;
    private static Action startThreadAnalyzerConfiguration;
        /** A list of event listeners for this component. */
    private final  EventListenerList listenerList = new EventListenerList();


    static {
        startThreadAnalyzerConfiguration = MainProjectSensitiveActions.mainProjectSensitiveAction(new ProjectActionPerformer() {

            public synchronized boolean enable(final Project project) {
                return THAProjectSupport.isSupported(project);
            }

            public void perform(final Project project) {
                if (!THAProjectSupport.isSupported(project)) {
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

                THAConfiguration thaConfiguration = configurationPanel.getTHAConfiguration();
                THAActionsProvider provider = new THAActionsProvider(project, thaConfiguration);
                if (!start(provider.dlightTargetListener, project, thaConfiguration)){
                    return;
                }
                THAIndicatorsTopComponent topComponent = THAIndicatorDelegator.getInstance().getProjectComponent(provider, project, thaConfiguration);
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
    private final THAConfiguration thaConfiguration;
    private final DLightTargetListener dlightTargetListener;
    //private final static Map<Project, THAActionsProvider> cache = new HashMap<Project, THAActionsProvider>();

    private THAActionsProvider(Project project, THAConfiguration thaConfiguration) {
        this.project = project;
        this.thaConfiguration = thaConfiguration;
        this.dlightTargetListener = new DLightTargetListenerImpl();
//        THAProjectSupport.getSupportFor(project).addDLightTargetListener(this);
        initActions();
    }

    public static final synchronized THAActionsProvider getSupportFor(Project project, THAConfiguration thaConfiguration) {
        if (!THAProjectSupport.isSupported(project)) {
            return null;
        }

//        if (cache.containsKey(project)) {
//            return cache.get(project);
//        }



        THAActionsProvider support = new THAActionsProvider(project, thaConfiguration);
        // cache.put(project, support);

        return support;
    }

 /**
     * Removes an <code>ActionListener</code> from the button.
     * If the listener is the currently set <code>Action</code>
     * for the button, then the <code>Action</code>
     * is set to <code>null</code>.
     *
     * @param l the listener to be removed
     */
    public void removeActionListener(ActionListener l) {
       listenerList.remove(ActionListener.class, l);
    }
    /**
     * Adds an <code>ActionListener</code> to the button.
     * @param l the <code>ActionListener</code> to be added
     */
    public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }

    private final String getActionCommand(){
        return "THAProfile";//NOI18N
    }
/**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the <code>event</code>
     * parameter.
     *
     * @param event  the <code>ActionEvent</code> object
     * @see EventListenerList
     */
    protected void fireActionPerformed(ActionEvent event) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        ActionEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ActionListener.class) {
                // Lazily create the event:
                if (e == null) {
                      String actionCommand = event.getActionCommand();
                      if(actionCommand == null) {
                         actionCommand = getActionCommand();
                      }
                      e = new ActionEvent(THAActionsProvider.this,
                                          ActionEvent.ACTION_PERFORMED,
                                          actionCommand,
                                          event.getWhen(),
                                          event.getModifiers());
                }
                ((ActionListener)listeners[i+1]).actionPerformed(e);
            }
        }
    }

    public static Action getStartTHAConfigurationAction() {
        return startThreadAnalyzerConfiguration;
    }

    public Action getResumeCollectionAction() {
        return resumeDataCollection;
    }

    public Action getStopAction() {
        return stop;
    }

    public Action getSuspendCollectionAction() {
        return suspendDataCollection;
    }

    public Action getRemoveInstrumentationAction() {
        return removeInstrumentation;
    }

    private void initActions() {
        removeInstrumentation = new RemoveInstrumentationAction();
        suspendDataCollection = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (project == null) {
                    return;
                }
                sendSignal();
                resumeDataCollection.setEnabled(true);
                suspendDataCollection.setEnabled(false);
                fireActionPerformed(e);
            }
        };
        suspendDataCollection.putValue("command", SUSPEND_COMMAND); // NOI18N
        suspendDataCollection.putValue(Action.SHORT_DESCRIPTION, loc("HINT_THASuspendDataCollection")); // NOI18N
        suspendDataCollection.putValue("iconBase", "org/netbeans/modules/cnd/tha/resources/Pause24.gif"); // NOI18N
        suspendDataCollection.putValue(Action.SMALL_ICON, ImageUtilities.mergeImages(ImageUtilities.loadImage("org/netbeans/modules/cnd/tha/resources/Pause24.gif", false),
                ImageUtilities.loadImage("org/netbeans/modules/cnd/tha/resources/experiment.png", false), 10, 10)); // NOI18N
        suspendDataCollection.putValue("disabledIcon", ImageUtilities.mergeImages(ImageUtilities.loadImage("org/netbeans/modules/cnd/tha/resources/Pause24_disabled.gif", false),
                ImageUtilities.loadImage("org/netbeans/modules/cnd/tha/resources/experiment_disabled.png", false), 10, 10)); // NOI18N
        suspendDataCollection.setEnabled(false);
        resumeDataCollection = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                sendSignal();
                resumeDataCollection.setEnabled(false);
                suspendDataCollection.setEnabled(true);
                fireActionPerformed(e);
            }
        };
        resumeDataCollection.putValue("command",RESUME_COMMAND); // NOI18N
        resumeDataCollection.putValue(Action.SHORT_DESCRIPTION, loc("HINT_THAResumeDataCollection")); // NOI18N
        resumeDataCollection.putValue("iconBase", "org/netbeans/modules/cnd/tha/resources/Continue24.gif"); // NOI18N
        resumeDataCollection.putValue(Action.SMALL_ICON, ImageUtilities.mergeImages(ImageUtilities.loadImage("org/netbeans/modules/cnd/tha/resources/Continue24.gif", false),
                ImageUtilities.loadImage("org/netbeans/modules/cnd/tha/resources/experiment.png", false), 10, 10)); // NOI18N
        resumeDataCollection.putValue("disabledIcon", ImageUtilities.mergeImages(ImageUtilities.loadImage("org/netbeans/modules/cnd/tha/resources/Continue24_disabled.gif", false),
                ImageUtilities.loadImage("org/netbeans/modules/cnd/tha/resources/experiment_disabled.png", false), 10, 10)); // NOI18N
        resumeDataCollection.setEnabled(false);

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
        stop.putValue("command", STOP_COMMAND); // NOI18N
        stop.putValue(Action.SHORT_DESCRIPTION, loc("HINT_THAStopDataCollection")); // NOI18N
        stop.putValue("iconBase", "org/netbeans/modules/cnd/tha/resources/Kill24.gif"); // NOI18N
        stop.putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/tha/resources/Kill24.gif", false)); // NOI18N       
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

    static private boolean start(DLightTargetListener listener, Project project, THAConfiguration thaConfiguration) {
        THAProjectSupport support = THAProjectSupport.getSupportFor(project);

        if (support == null) {
            return false;
        }

        if (!support.isConfiguredForInstrumentation()) {
            boolean instrResult = support.doInstrumentation();
            if (!instrResult) {
                return false;
            }
        }
        // Initiate RUN ...
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);

        if (ap != null) {
            if (Arrays.asList(ap.getSupportedActions()).contains("custom.action")) { // NOI18N
                ap.invokeAction("custom.action", Lookups.fixed(thaConfiguration, listener)); // NOI18N
            }
        }
        return true;
    }

    void sendSignal() {
        if (0 < pid) {
            CommonTasksSupport.sendSignal(target.getExecEnv(), pid, "USR1", null); // NOI18N
        }
    }

    private void targetStarted(int pid) {
        //means stop button should be enabled
        this.pid = pid;
        suspendDataCollection.setEnabled(thaConfiguration.collectFromBeginning());
        resumeDataCollection.setEnabled(!thaConfiguration.collectFromBeginning());
        stop.setEnabled(true);

    }

    private void targetFailed() {
        //back all as it was
        suspendDataCollection.setEnabled(false);
        resumeDataCollection.setEnabled(false);
        stop.setEnabled(false);
        fireActionPerformed(new ActionEvent(THAActionsProvider.this, ActionEvent.ACTION_PERFORMED, STOP_COMMAND));
    }

    private void targetFinished(Integer status) {
        suspendDataCollection.setEnabled(false);
        resumeDataCollection.setEnabled(false);
        stop.setEnabled(false);
        fireActionPerformed(new ActionEvent(THAActionsProvider.this, ActionEvent.ACTION_PERFORMED, STOP_COMMAND));
    }
    
    private final class DLightTargetListenerImpl implements DLightTargetListener{

        public void targetStateChanged(DLightTargetChangeEvent event) {
            THAActionsProvider.this.targetStateChanged(event);
        }
        
    }
}

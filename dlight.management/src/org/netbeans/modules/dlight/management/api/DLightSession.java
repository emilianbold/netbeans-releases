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
package org.netbeans.modules.dlight.management.api;


import org.netbeans.modules.dlight.management.api.impl.DataStorageManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.collector.spi.DataCollector;
import org.netbeans.modules.dlight.execution.api.DLightSessionReference;
import org.netbeans.modules.dlight.execution.api.DLightTarget;
import org.netbeans.modules.dlight.execution.api.DLightTargetListener;
import org.netbeans.modules.dlight.indicator.api.Indicator;
import org.netbeans.modules.dlight.indicator.spi.IndicatorDataProvider;
import org.netbeans.modules.dlight.management.api.impl.DLightToolAccessor;
import org.netbeans.modules.dlight.storage.spi.DataStorage;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.visualizer.spi.Visualizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 * This class represents D-Light Session.
 * 
 */
public final class DLightSession implements DLightTargetListener, DLightSessionReference{
    private static int sessionCount = 0;
    private static final Logger log = DLightLogger.getLogger(DLightSession.class);
    private List<ExecutionContext> contexts = new ArrayList<ExecutionContext>();
    private List<SessionStateListener> sessionStateListeners = null;
    private List<DataStorage> storages = null;
    private List<Visualizer> visualizers = null;
    private SessionState state;
    private Task sessionTask;
    private final int sessionID;
    private String description = null;
    private List<ExecutionContextListener> contextListeners;
    private boolean isActive;

    public enum SessionState {

        CONFIGURATION,
        STARTING,
        RUNNING,
        PAUSED,
        ANALYZE,
    }

    /**
     * Created new DLightSession instance. This should not be called directly.
     * Instead DLightManager.newSession() should be used.
     *
     */
    DLightSession() {
        this.state = SessionState.CONFIGURATION;
        sessionID = sessionCount++;
    }

    void addExecutionContext(ExecutionContext context) {
        contexts.add(context);
        context.validateTools();
    }

    void setExecutionContext(ExecutionContext context) {
        clearExecutionContext();
        addExecutionContext(context);
    }

    void clearExecutionContext() {
        assertState(SessionState.CONFIGURATION);

        for (ExecutionContext c : contexts) {
            c.clear();
        }

        contexts.clear();
    }

    public void addSessionStateListener(SessionStateListener listener) {
        if (sessionStateListeners == null) {
            sessionStateListeners = new ArrayList<SessionStateListener>();
        }

        if (!sessionStateListeners.contains(listener)) {
            sessionStateListeners.add(listener);
        }
    }

    public void removeSessionStateListener(SessionStateListener listener) {
        if (sessionStateListeners == null) {
            return;
        }

        sessionStateListeners.remove(listener);
    }

    public SessionState getState() {
        return state;
    }

    public String getDescription() {
        if (description == null) {
            String targets = ""; // NOI18N
            if (contexts.isEmpty()) {
                targets = "no targets"; // NOI18N
            } else {
                for (ExecutionContext context : contexts) {
                    targets += context.getTarget().toString() + "; "; // NOI18N
                }
            }
            description = "Session #" + sessionID + " (" + targets + ")"; // NOI18N
        }
        return description;
    }

    boolean isRunning() {
        return state == SessionState.RUNNING;
    }

    void addVisualizer(Visualizer visualizer) {
        if (visualizers == null) {
            visualizers = new ArrayList<Visualizer>();
        }

        if (!visualizers.contains(visualizer)) {
            visualizers.add(visualizer);
        }
    }

    public void revalidate() {
        for (ExecutionContext c : contexts) {
            c.validateTools();
        }
    }

    synchronized void stop() {
        // TODO: review later....
        for (ExecutionContext c : contexts) {
            final DLightTarget target = c.getTarget();
            target.terminate();
        }
    }

    void start() {
        Runnable sessionRunnable = new Runnable() {
            boolean hasValidContext = true;

            public void run() {
                DataStorageManager.getInstance().clearActiveStorages();

                if (storages != null) {
                    storages.clear();
                }
                
                for (ExecutionContext context : contexts) {
                    boolean result = prepareContext(context);
                    hasValidContext &= result;
                }

                if (!hasValidContext) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("No tool passed validation... ")); // NOI18N
                    return;
                }

                setState(SessionState.STARTING);

                // TODO: For now add target listeners to
                // first target.... (from the first context)
                boolean f = false;

                for (ExecutionContext context : contexts) {
                    if (!f) {
                        context.getTarget().addTargetListener(DLightSession.this);
                        f = true;
                    }
                    context.getTarget().start();
                }
            }
        };

        sessionTask = RequestProcessor.getDefault().post(sessionRunnable);
    }

    private boolean prepareContext(ExecutionContext context) {
        final DLightTarget target = context.getTarget();

        context.validateTools(true);

        List<DLightTool> validTools = new ArrayList<DLightTool>();

        for (DLightTool tool : context.getTools()) {
            if (tool.getValidationStatus().isOK()) {
                validTools.add(tool);
            }
        }

        if (validTools.isEmpty()) {
            return false;
        }

        DataCollector notAttachableDataCollector = null;
        List<DataCollector> collectors = new ArrayList<DataCollector>();
        for (DLightTool tool : validTools) {
            List<DataCollector> toolCollectors = tool.getCollectors();
            //TODO: no algorithm here:) should be better
            for (DataCollector c : toolCollectors) {
                if (!collectors.contains(c)) {
                    collectors.add(c);
                }
            }
        }

        for (DataCollector toolCollector : collectors) {
            DataStorage storage = DataStorageManager.getInstance().getDataStorageFor(toolCollector);

            if (storage != null) {
                if (notAttachableDataCollector == null && !toolCollector.isAttachable()) {
                    notAttachableDataCollector = toolCollector;
                }
                toolCollector.init(storage, target);
                if (storages == null) {
                    storages = new ArrayList<DataStorage>();
                }
                if (!storages.contains(storage)) {
                    storages.add(storage);
                }
            } else {
                // Cannot find storage for this collector!
                log.severe("Cannot find storage for collector " + toolCollector);
            }

            target.addTargetListener(toolCollector);
        }

        for (DLightTool tool : validTools) {
            // Try to subscribe every IndicatorDataProvider to every Indicator
            List<IndicatorDataProvider> idps = DLightToolAccessor.getDefault().getIndicatorDataProviders(tool);
            if (idps != null) {
                for (IndicatorDataProvider idp : idps) {
                  List<Indicator> indicators = DLightToolAccessor.getDefault().getIndicators(tool);
                    for (Indicator i : indicators) {
                        boolean wasSubscribed = idp.subscribe(i);
                        if (wasSubscribed) {
                            target.addTargetListener(idp);
                            log.info("I have subscribed indicator " + i + " to indicatorDataProvider " + idp);
                        }
                    }
                }
            }
        }

        //and now if we have collectors which cannot be attached let's substitute target
        //the question is is it possible in case target is the whole system: WebTierTarget
        //or SystemTarget
        if (notAttachableDataCollector != null && target.canBeSubstituted()) {
            target.substitute(notAttachableDataCollector.getCmd(), notAttachableDataCollector.getArgs());
        }

        return true;

//    activeTasks = new ArrayList<DLightExecutorTask>();

    // TODO: For now: assume that ANY collector can attach to the process!
    // Here we need to start (paused!) the target; subscribe collectors as listeners .... ;


//    if (selectedTools != null) {
//      for (TargetRunnerListener l : tool.getTargetRunnerListeners()) {
//        runner.addTargetRunnerListener(l);
//      }
//    }
////    RequestProcessor.getDefault().post(runner);
//    runner.run();
    }

    List<DataStorage> getStorages() {
        return (storages == null) ? Collections.<DataStorage>emptyList() : storages;
    }

    void close() {
        // Unsubscribe listeners
        if (sessionStateListeners != null) {
            sessionStateListeners.clear();
            sessionStateListeners = null;
        }
    }

    public void targetStarted(DLightTarget target) {
        setState(SessionState.RUNNING);
    }

    public void targetFinished(DLightTarget target, int result) {
        setState(SessionState.ANALYZE);
        target.removeTargetListener(this);
    }

    private void setState(SessionState state) {
        SessionState oldState = this.state;
        this.state = state;

        if (sessionStateListeners != null) {
            for (SessionStateListener l : sessionStateListeners.toArray(new SessionStateListener[0])) {
                l.sessionStateChanged(this, oldState, state);
            }
        }
    }

    List<Visualizer> getVisualizers() {
        return visualizers;
    }

    // Proxy method to contexts
    public void addExecutionContextListener(ExecutionContextListener listener) {
        if (contextListeners == null) {
            contextListeners = new ArrayList<ExecutionContextListener>();
        }

        if (!contextListeners.contains(listener)) {
            contextListeners.add(listener);
        }

        updateContextListeners();
    }

    // Proxy method to contexts
    public void removeExecutionContextListener(ExecutionContextListener listener) {
        if (contextListeners == null) {
            return;
        }

        contextListeners.remove(listener);
        updateContextListeners();
    }

    private void updateContextListeners() {
        for (ExecutionContext c : contexts) {
            c.setListeners(contextListeners);
        }
    }

    public List<DLightTool> getTools() {
        List<DLightTool> result = new ArrayList<DLightTool>();
        for (ExecutionContext c : contexts) {
            result.addAll(c.getTools());
        }
        return result;
    }

    public List<Indicator> getIndicators() {
        List<Indicator> result = new ArrayList<Indicator>();
        for (ExecutionContext c : contexts) {
            result.addAll(c.getIndicators());
        }
        return result;
    }

    private void assertState(SessionState expectedState) {
        if (this.state != expectedState) {
            throw new IllegalStateException("Session is in illegal state " + this.state + "; Must be in " + expectedState);
        }
    }

    public boolean isActive() {
        return isActive;
    }

    void setActive(boolean b) {
        isActive = b;
    }
}

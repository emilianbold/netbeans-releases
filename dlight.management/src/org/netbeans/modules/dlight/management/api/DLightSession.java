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

import java.awt.EventQueue;
import org.netbeans.modules.dlight.api.execution.DLightSessionContext;
import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import org.netbeans.modules.dlight.management.api.impl.DataStorageManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTargetListener;
import org.netbeans.modules.dlight.api.execution.SubstitutableTarget;
import org.netbeans.modules.dlight.api.impl.DLightSessionContextAccessor;
import org.netbeans.modules.dlight.api.impl.DLightTargetAccessor;
import org.netbeans.modules.dlight.api.impl.DLightSessionInternalReference;
import org.netbeans.modules.dlight.api.impl.DLightToolAccessor;
import org.netbeans.modules.dlight.management.api.impl.DataFiltersManager;
import org.netbeans.modules.dlight.management.api.impl.SessionDataFiltersSupport;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.api.datafilter.DataFilterListener;
import org.netbeans.modules.dlight.spi.impl.IndicatorAccessor;
import org.netbeans.modules.dlight.spi.impl.IndicatorRepairActionProviderAccessor;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;

/**
 * This class represents D-Light Session.
 * 
 */
public final class DLightSession implements DLightTargetListener, DLightSessionInternalReference {

    private static int sessionCount = 0;
    private static final Logger log = DLightLogger.getLogger(DLightSession.class);
    private List<ExecutionContext> contexts = new ArrayList<ExecutionContext>();
    private List<SessionStateListener> sessionStateListeners = null;
    private List<DataStorage> storages = null;
    private List<ServiceInfoDataStorage> serviceInfoDataStorages = null;
    private List<DataCollector> collectors = null;
    private Map<String, Map<String, Visualizer>> visualizers = null;
    private SessionState state;
    private final int sessionID;
    private String description = null;
    private List<ExecutionContextListener> contextListeners;
    private boolean isActive;
    private final DLightSessionContext sessionContext;
    private final String name;
    private boolean closeOnExit = false;
    private final SessionDataFiltersSupport dataFiltersSupport;

    public static enum SessionState {

        CONFIGURATION,
        STARTING,
        RUNNING,
        PAUSED,
        ANALYZE,
        CLOSED
    }

    public void targetStateChanged(DLightTargetChangeEvent event) {
        switch (event.state) {
            case RUNNING:
                targetStarted(event.target);
                break;
            case FAILED:
                targetFinished(event.target);
                break;
            case TERMINATED:
                targetFinished(event.target);
                break;
            case DONE:
                targetFinished(event.target);
                break;
            case STOPPED:
                targetFinished(event.target);
                return;
        }
    }

    /**
     * Created new DLightSession instance. This should not be called directly.
     * Instead DLightManager.newSession() should be used.
     *
     */
    DLightSession(String name) {
        this.state = SessionState.CONFIGURATION;
        this.name = name;
        sessionID = sessionCount++;
        sessionContext = DLightSessionContextAccessor.getDefault().newContext();
        dataFiltersSupport = new SessionDataFiltersSupport();
    }

    public DLightSessionContext getSessionContext() {
        return sessionContext;
    }

    void cleanVisualizers() {
        if (visualizers == null) {
            return;
        }
        visualizers.clear();
        visualizers = null;
    }

    List<ExecutionContext> getExecutionContexts() {
        return contexts;
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

    public String getDisplayName() {
        return name == null ? getDescription() : name;
    }

    boolean isRunning() {
        return state == SessionState.RUNNING;
    }

    boolean hasVisualizer(String toolName, String visualizerID) {
        if (visualizers == null || !visualizers.containsKey(toolName)) {
            return false;
        }
        Map<String, Visualizer> toolVisualizers = visualizers.get(toolName);
        return toolVisualizers.containsKey(visualizerID);
    }

    List<Visualizer> getVisualizers() {
        if (visualizers == null) {
            return null;
        }
        List<Visualizer> result = new ArrayList<Visualizer>();
        for (String toolName : visualizers.keySet()) {
            Map<String, Visualizer> toolVisualizers = visualizers.get(toolName);
            for (String visID : toolVisualizers.keySet()) {
                result.add(toolVisualizers.get(visID));
            }
        }
        return result;

    }

    Visualizer getVisualizer(String toolName, String visualizerID) {
        if (visualizers == null || !visualizers.containsKey(toolName)) {
            return null;
        }
        Map<String, Visualizer> toolVisualizers = visualizers.get(toolName);
        return toolVisualizers.get(visualizerID);
    }

    Visualizer putVisualizer(String toolName, String id, Visualizer visualizer) {
        if (visualizers == null) {
            visualizers = new HashMap<String, Map<String, Visualizer>>();
        }

        Map<String, Visualizer> toolVisualizers = visualizers.get(toolName);
        if (toolVisualizers == null) {
            toolVisualizers = new HashMap<String, Visualizer>();
            visualizers.put(toolName, toolVisualizers);
        }
        Visualizer oldVis = toolVisualizers.put(id, visualizer);
        return oldVis;

    }

    public void revalidate() {
        for (ExecutionContext c : contexts) {
            c.validateTools();
        }
    }

    void closeOnExit() {
        closeOnExit = true;
    }

    synchronized void stop() {
        if (state == SessionState.ANALYZE) {
            return;
        }

        setState(SessionState.ANALYZE);

        for (ExecutionContext c : contexts) {
            final DLightTarget target = c.getTarget();
            target.removeTargetListener(this);
            DLightExecutorService.submit(new Runnable() {

                public void run() {
                    DLightTargetAccessor.getDefault().getDLightTargetExecution(target).terminate(target);
                }
            }, "Stop DLight session's target " + target.toString()); // NOI18N
        }
    }

    void start() {
        Runnable sessionRunnable = new Runnable() {

            public void run() {
                DataStorageManager.getInstance().clearActiveStorages(DLightSession.this);

                if (storages != null) {
                    storages.clear();
                }

                if (serviceInfoDataStorages != null) {
                    serviceInfoDataStorages.clear();
                }

                if (collectors != null) {
                    collectors.clear();
                }

                for (ExecutionContext context : contexts) {
                    prepareContext(context);
                }

                setState(SessionState.STARTING);

                // TODO: For now add target listeners to
                // first target.... (from the first context)
                boolean f = false;

                final DLightTargetAccessor targetAccess =
                        DLightTargetAccessor.getDefault();

                for (ExecutionContext context : contexts) {
                    DLightTarget target = context.getTarget();

                    if (!f) {
                        target.addTargetListener(DLightSession.this);
                        f = true;
                    }

                    DLightTarget.ExecutionEnvVariablesProvider envProvider =
                            context.getDLightTargetExecutionEnvProvider();

                    targetAccess.getDLightTargetExecution(target).start(target, envProvider);
                }
            }
        };

        DLightExecutorService.submit(sessionRunnable, "DLight session"); // NOI18N
    }

    void addDataFilterListener(DataFilterListener listener) {
        dataFiltersSupport.addDataFilterListener(listener);
    }

    private boolean prepareContext(ExecutionContext context) {
        final DLightTarget target = context.getTarget();

        context.validateTools(context.getDLightConfiguration().getConfigurationOptions(false).validateToolsRequiredUserInteraction());

        List<DLightTool> validTools = new ArrayList<DLightTool>();
        StringBuilder toolNames = new StringBuilder();

        for (DLightTool tool : context.getTools()) {
            validTools.add(tool);
            toolNames.append(tool.getName() + ServiceInfoDataStorage.DELIMITER);
        }

        if (validTools.isEmpty()) {
            return false;
        }

        DataCollector notAttachableDataCollector = null;

        if (collectors == null) {
            collectors = new ArrayList<DataCollector>();
        }

        if (context.getDLightConfiguration().getConfigurationOptions(false).areCollectorsTurnedOn()) {
            for (DLightTool tool : validTools) {
                List<DataCollector<?>> toolCollectors = context.getDLightConfiguration().getConfigurationOptions(false).getCollectors(tool);
                //TODO: no algorithm here:) should be better
                for (DataCollector c : toolCollectors) {
                    if (!collectors.contains(c)) {
                        if (c.getValidationStatus().isValid()) {//for valid collectors only
                            collectors.add(c);
                        }
                    }
                }
            }
        } else {
            collectors.clear();
        }

        Collection<IndicatorDataProvider> idproviders = new ArrayList<IndicatorDataProvider>();
        StringBuilder idpsNames = new StringBuilder();
        StringBuilder collectorNames = new StringBuilder();

        //if we have IDP which are collectors add them into the list of collectors
        for (DLightTool tool : validTools) {
            // Try to subscribe every IndicatorDataProvider to every Indicator
            //there can be the situation when IndicatorDataProvider is collector
            //and not attacheble
            List<Indicator> subscribedIndicators = new ArrayList<Indicator>();
            List<IndicatorDataProvider<?>> idps = context.getDLightConfiguration().
                    getConfigurationOptions(false).getIndicatorDataProviders(tool);

            if (idps != null) {
                for (IndicatorDataProvider idp : idps) {
                    if (idp.getValidationStatus().isValid()) {
                        if (idp instanceof DLightTarget.ExecutionEnvVariablesProvider) {
                            context.addDLightTargetExecutionEnviromentProvider((DLightTarget.ExecutionEnvVariablesProvider) idp);
                        }

                        if (idp instanceof DataCollector) {
                            if (!collectors.contains((DataCollector) idp)) {
                                collectors.add((DataCollector) idp);
                            }
                            if (notAttachableDataCollector == null && !((DataCollector) idp).isAttachable()) {
                                notAttachableDataCollector = ((DataCollector) idp);
                            }
                        } else {
                            idproviders.add(idp);
                        }

                        idpsNames.append(idp.getName() + ServiceInfoDataStorage.DELIMITER);
                        List<Indicator<?>> indicators = DLightToolAccessor.getDefault().getIndicators(tool);

                        for (Indicator i : indicators) {
                            target.addTargetListener(i);
                            boolean wasSubscribed = idp.subscribe(i);
                            if (wasSubscribed) {
                                if (!subscribedIndicators.contains(i)) {
                                    subscribedIndicators.add(i);
                                }
                                target.addTargetListener(idp);
                                if (log.isLoggable(Level.FINE)) {
                                    log.fine("I have subscribed indicator " + i + " to indicatorDataProvider " + idp); // NOI18N
                                }
                            }
                        }
                    }
                }
            }

            List<Indicator<?>> indicators = DLightToolAccessor.getDefault().getIndicators(tool);
            for (Indicator i : indicators) {
                if (!subscribedIndicators.contains(i)) {
                    IndicatorAccessor.getDefault().setRepairActionProviderFor(i, IndicatorRepairActionProviderAccessor.getDefault().createNew(context.getDLightConfiguration(), tool, target));
                }
            }
        }

        if (collectors != null && collectors.size() > 0) {
            for (DataCollector toolCollector : collectors) {
                collectorNames.append(toolCollector.getName() + ServiceInfoDataStorage.DELIMITER);
                DataStorage storage = DataStorageManager.getInstance().getDataStorageFor(this, toolCollector);

                if (toolCollector instanceof DLightTarget.ExecutionEnvVariablesProvider) {
                    context.addDLightTargetExecutionEnviromentProvider((DLightTarget.ExecutionEnvVariablesProvider) toolCollector);
                }

                if (storage != null) {
                    // init storage with the target values
                    DLightTarget.Info targetInfo = DLightTargetAccessor.getDefault().getDLightTargetInfo(target);

                    Map<String, String> info = targetInfo.getInfo();

                    for (String key : info.keySet()) {
                        storage.put(key, info.get(key));
                    }

                    toolCollector.init(storage, target);
                    addDataFilterListener(toolCollector);

                    if (toolCollector instanceof IndicatorDataProvider) {
                        IndicatorDataProvider idp = (IndicatorDataProvider) toolCollector;
                        idp.init(storage);
                    }

                    if (storages == null) {
                        storages = new ArrayList<DataStorage>();
                    }

                    if (!storages.contains(storage)) {
                        storages.add(storage);
                    }

                    if (notAttachableDataCollector == null && !toolCollector.isAttachable()) {
                        notAttachableDataCollector = toolCollector;
                    }
                } else {
                    // Cannot find storage for this collector!
                    log.severe("Cannot find storage for collector " + toolCollector); // NOI18N
                }

                target.addTargetListener(toolCollector);
            }
        } else {
            //should initialize at least ServiceInfoDataStorage For the Session
            ServiceInfoDataStorage serviceInfoDataStorage = DataStorageManager.getInstance().getServiceInfoDataStorage(this);
            DLightTarget.Info targetInfo = DLightTargetAccessor.getDefault().getDLightTargetInfo(target);
            Map<String, String> info = targetInfo.getInfo();
            for (String key : info.keySet()) {
                serviceInfoDataStorage.put(key, info.get(key));
            }
            if (serviceInfoDataStorages == null) {
                serviceInfoDataStorages = new ArrayList<ServiceInfoDataStorage>();
            }
            if (!serviceInfoDataStorages.contains(serviceInfoDataStorage)) {
                serviceInfoDataStorages.add(serviceInfoDataStorage);
            }
        }

        //We should init IDPs with the ServiceInfoDataStorage
        ServiceInfoDataStorage storage = null;
        if (storages != null && !storages.isEmpty()) {
            for (DataStorage st : storages) {
                st.put(ServiceInfoDataStorage.TOOL_NAMES, toolNames.toString());
                st.put(ServiceInfoDataStorage.CONFIFURATION_NAME, context.getDLightConfiguration().getConfigurationName());
                st.put(ServiceInfoDataStorage.IDP_NAMES, idpsNames.toString());
                st.put(ServiceInfoDataStorage.COLLECTOR_NAMES, collectorNames.toString());

            }
            storage = storages.get(0);//I need any, no matter what it is if it contains info needed
        } else if (serviceInfoDataStorages != null && !serviceInfoDataStorages.isEmpty()) {
            for (ServiceInfoDataStorage st : serviceInfoDataStorages) {
                st.put(ServiceInfoDataStorage.TOOL_NAMES, toolNames.toString());
                st.put(ServiceInfoDataStorage.CONFIFURATION_NAME, context.getDLightConfiguration().getConfigurationName());
                st.put(ServiceInfoDataStorage.IDP_NAMES, idpsNames.toString());
                st.put(ServiceInfoDataStorage.COLLECTOR_NAMES, collectorNames.toString());
            }
            storage = serviceInfoDataStorages.get(0);
        }

        if (storage != null) {
            for (IndicatorDataProvider idp : idproviders) {
                idp.init(storage);
                addDataFilterListener(idp);
            }
        }

        //and now if we have collectors which cannot be attached let's substitute target
        //the question is is it possible in case target is the whole system: WebTierTarget
        //or SystemTarget
        if (notAttachableDataCollector != null && target instanceof SubstitutableTarget) {
            ((SubstitutableTarget) target).substitute(notAttachableDataCollector.getCmd(), notAttachableDataCollector.getArgs());
        }

        // at the end, initialize data filters (_temporarily_ here, as info
        // about filters is stored in target's info...

        DLightTarget.Info targetInfo = DLightTargetAccessor.getDefault().getDLightTargetInfo(target);
        Map<String, String> info = targetInfo.getInfo();

        for (String key : info.keySet()) {
            DataFilter filter = DataFiltersManager.getInstance().createFilter(key, info.get(key));
            if (filter != null) {
                dataFiltersSupport.addFilter(filter);
            }
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

    /**
     * Returns storages this session is using
     * @return data storage list this session is using to save data if any
     */
    public List<DataStorage> getStorages() {
        return (storages == null) ? Collections.<DataStorage>emptyList() : storages;
    }

    public List<ServiceInfoDataStorage> getServiceInfoDataStorages() {
        //plus
        return serviceInfoDataStorages == null ? Collections.<ServiceInfoDataStorage>emptyList() : serviceInfoDataStorages;
    }

    void close() {
        // Unsubscribe listeners
        setState(SessionState.CLOSED);

        if (sessionStateListeners != null) {
            sessionStateListeners.clear();
            sessionStateListeners = null;
        }

        dataFiltersSupport.removeAllListeners();

        if (!EventQueue.isDispatchThread()) {
            DataStorageManager.getInstance().closeSession(this);
            cleanVisualizers();
        } else {
            DLightExecutorService.submit(new Runnable() {

                public void run() {
                    DataStorageManager.getInstance().closeSession(DLightSession.this);
                    cleanVisualizers();
                }
            }, "DLight Session " + this.getDisplayName() + " is closing..");//NOI18N
        }
    }

    private void targetStarted(DLightTarget target) {
        setState(SessionState.RUNNING);
    }

    private void targetFinished(DLightTarget target) {
        setState(SessionState.ANALYZE);
        target.removeTargetListener(this);
        if (closeOnExit) {
            close();
        }
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

    DLightTool getToolByName(String toolName) {
        if (toolName == null) {
            throw new IllegalArgumentException("Cannot use NULL as a tool name ");//NOI18N
        }

        for (ExecutionContext c : contexts) {
            DLightTool tool = c.getToolByName(toolName);
            if (tool != null) {
                return tool;
            }
        }
        return null;
    }

    public List<Indicator> getIndicators() {
        List<Indicator> result = new ArrayList<Indicator>();

        for (ExecutionContext c : contexts) {
            result.addAll(c.getIndicators());
        }

        return result;
    }

    boolean containsIndicator(Indicator indicator) {
        return getIndicators().contains(indicator);
    }

    private void assertState(SessionState expectedState) {
        if (this.state != expectedState) {
            throw new IllegalStateException("Session is in illegal state " + this.state + "; Must be in " + expectedState); // NOI18N
        }
    }

    public boolean isActive() {
        return isActive;
    }

    void setActive(boolean b) {
        isActive = b;
    }
}

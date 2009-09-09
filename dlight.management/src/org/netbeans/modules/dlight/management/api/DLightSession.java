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
import org.netbeans.modules.dlight.api.impl.DLightTargetAccessor;
import org.netbeans.modules.dlight.api.impl.DLightSessionInternalReference;
import org.netbeans.modules.dlight.api.impl.DLightToolAccessor;
import org.netbeans.modules.dlight.management.api.impl.DataFiltersManager;
import org.netbeans.modules.dlight.management.api.impl.SessionDataFiltersSupport;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.api.datafilter.DataFilterListener;
import org.netbeans.modules.dlight.api.datafilter.DataFilterManager;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.impl.ServiceInfoDataStorageImpl;
import org.netbeans.modules.dlight.management.api.impl.DataProvidersManager;
import org.netbeans.modules.dlight.management.timeline.TimeIntervalDataFilter;
import org.netbeans.modules.dlight.spi.dataprovider.DataProvider;
import org.netbeans.modules.dlight.spi.dataprovider.DataProviderFactory;
import org.netbeans.modules.dlight.spi.impl.IndicatorAccessor;
import org.netbeans.modules.dlight.spi.impl.IndicatorDataProviderAccessor;
import org.netbeans.modules.dlight.spi.impl.IndicatorRepairActionProviderAccessor;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.indicator.IndicatorNotificationsListener;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerDataProvider;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.windows.InputOutput;

/**
 * This class represents D-Light Session.
 * 
 */
public final class DLightSession implements DLightTargetListener, DataFilterManager, DLightSessionIOProvider, DLightSessionInternalReference {
    private long startTimestamp = 0;
    private static int sessionCount = 0;
    private static final Logger log = DLightLogger.getLogger(DLightSession.class);
    private List<ExecutionContext> contexts = new ArrayList<ExecutionContext>();
    private List<SessionStateListener> sessionStateListeners = null;
    private final List<IndicatorNotificationsListener> indicatorNotificationListeners = Collections.synchronizedList(new ArrayList<IndicatorNotificationsListener>());
    private List<DataStorage> storages = null;
    private ServiceInfoDataStorage serviceInfoDataStorage = null;
    private List<DataCollector> collectors = null;
    private Map<String, Map<String, Visualizer>> visualizers = null;
    private SessionState state;
    private final int sessionID;
    private String description = null;
    private List<ExecutionContextListener> contextListeners;
    private boolean isActive;
    private final String name;
    private boolean closeOnExit = false;
    private final SessionDataFiltersSupport dataFiltersSupport;
    private InputOutput io;

    public static enum SessionState {

        CONFIGURATION,
        STARTING,
        RUNNING,
        PAUSED,
        ANALYZE,
        CLOSED
    }

    public final long getStartTime() {
        return startTimestamp;
    }

    public void targetStateChanged(DLightTargetChangeEvent event) {
        switch (event.state) {
            case RUNNING:
                startTimestamp = System.nanoTime();
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
        dataFiltersSupport = new SessionDataFiltersSupport();
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
            return Collections.emptyList();
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

                serviceInfoDataStorage = new ServiceInfoDataStorageImpl();

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

                    DLightSession.this.io = targetAccess.getDLightTargetExecution(target).start(target, envProvider);
                }
            }
        };

        DLightExecutorService.submit(sessionRunnable, "DLight session"); // NOI18N
    }

    public void addDataFilterListener(DataFilterListener listener) {
        dataFiltersSupport.addDataFilterListener(listener);
    }

   public void removeDataFilterListener(DataFilterListener listener) {
        dataFiltersSupport.removeDataFilterListener(listener);
    }

    public DLightSessionIOProvider getDLigthSessionIOProvider(){
        return this;
    }
    
    public InputOutput getInputOutput(){
        if (this.state == SessionState.CONFIGURATION){
            return null;//nothing to return, we are in configuration state
        }
        return io;
    }
    
    public void addDataFilter(DataFilter filter, boolean isAdjusting) {
        //if the filter is TimeIntervalFilter: remove first
        if (filter instanceof TimeIntervalDataFilter){
            dataFiltersSupport.cleanAll(TimeIntervalDataFilter.class, false);
        }
        dataFiltersSupport.addFilter(filter, isAdjusting);
        //if filter is added - refresh all visualizers
        if (!isAdjusting){
            for (Visualizer v : getVisualizers()){
                v.refresh();
            }
        }
        
    }
    
    public void cleanAllDataFilter() {
        dataFiltersSupport.cleanAll();
    }

    public void cleanAllDataFilter(Class clazz) {
        dataFiltersSupport.cleanAll(clazz);
    }

    public <T extends DataFilter> Collection<T> getDataFilter(Class<T> clazz) {
        return dataFiltersSupport.getDataFilter(clazz);
    }

    public boolean removeDataFilter(DataFilter filter) {
        return dataFiltersSupport.removeFilter(filter);
    }


    public final void addIndicatorNotificationListener(IndicatorNotificationsListener l) {
        if (l == null) {
            return;
        }
        if (!indicatorNotificationListeners.contains(l)) {
            indicatorNotificationListeners.add(l);
        }
    }

    /**
     * On the next run you will not get any notification.
     * But you will still get them during the current session execution
     * @param l
     * @return
     */
    public final boolean removeIndicatorNotificationListener(IndicatorNotificationsListener l) {
        if (l == null) {
            return false;
        }
        //it is still not enouph, if user just want to stop getting notification
        //we do not provide such mechanism right now
        return indicatorNotificationListeners.remove(l);
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
                        //now subscribe listeners
                        for (IndicatorNotificationsListener l : indicatorNotificationListeners) {
                            IndicatorDataProviderAccessor.getDefault().addIndicatorDataProviderListener(idp, l);
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
                            if (i instanceof DataFilterListener) {
                                addDataFilterListener((DataFilterListener)i);
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

        // init storage with the target values
        DLightTarget.Info targetInfo = DLightTargetAccessor.getDefault().getDLightTargetInfo(target);
        for (Map.Entry<String, String> entry : targetInfo.getInfo().entrySet()) {
            serviceInfoDataStorage.put(entry.getKey(), entry.getValue());
        }
        serviceInfoDataStorage.put(ServiceInfoDataStorage.TOOL_NAMES, toolNames.toString());
        serviceInfoDataStorage.put(ServiceInfoDataStorage.CONFIFURATION_NAME, context.getDLightConfiguration().getConfigurationName());
        serviceInfoDataStorage.put(ServiceInfoDataStorage.IDP_NAMES, idpsNames.toString());
        serviceInfoDataStorage.put(ServiceInfoDataStorage.COLLECTOR_NAMES, collectorNames.toString());

        if (collectors != null && collectors.size() > 0) {
            for (DataCollector toolCollector : collectors) {
                collectorNames.append(toolCollector.getName() + ServiceInfoDataStorage.DELIMITER);
                Map<DataStorageType, DataStorage> currentStorages = DataStorageManager.getInstance().getDataStoragesFor(this, toolCollector);

                if (toolCollector instanceof DLightTarget.ExecutionEnvVariablesProvider) {
                    context.addDLightTargetExecutionEnviromentProvider((DLightTarget.ExecutionEnvVariablesProvider) toolCollector);
                }

                if (currentStorages != null && !currentStorages.isEmpty()) {

                    toolCollector.init(currentStorages, target);
                    addDataFilterListener(toolCollector);

                    if (toolCollector instanceof IndicatorDataProvider) {
                        IndicatorDataProvider idp = (IndicatorDataProvider) toolCollector;
                        idp.init(serviceInfoDataStorage);
                    }

                    if (storages == null) {
                        storages = new ArrayList<DataStorage>();
                    }

                    for (DataStorage storage : currentStorages.values()) {
                        if (!storages.contains(storage)) {
                            storages.add(storage);
                        }
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
        }

        for (IndicatorDataProvider idp : idproviders) {
            idp.init(serviceInfoDataStorage);
            addDataFilterListener(idp);
        }

        // at the end, initialize data filters (_temporarily_ here, as info
        // about filters is stored in target's info...

        Map<String, String> info = targetInfo.getInfo();

        for (String key : info.keySet()) {
            DataFilter filter = DataFiltersManager.getInstance().createFilter(key, info.get(key));
            if (filter != null) {
                dataFiltersSupport.addFilter(filter, false);
            }
        }
        //Do it at the very end to apply filters
        //and now if we have collectors which cannot be attached let's substitute target
        //the question is is it possible in case target is the whole system: WebTierTarget
        //or SystemTarget
        if (notAttachableDataCollector != null && target instanceof SubstitutableTarget) {
            ((SubstitutableTarget) target).substitute(notAttachableDataCollector.getCmd(), notAttachableDataCollector.getArgs());
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

    public ServiceInfoDataStorage getServiceInfoDataStorage() {
        return serviceInfoDataStorage;
    }

    /**
     * Creates data provider for data model scheme if matching data storage exists.
     *
     * @param dataModelScheme
     * @param metadata
     * @return
     */
    public DataProvider createDataProvider(DataModelScheme dataModelScheme, DataTableMetadata dataMetadata) {

        // Get a list of all provider factories that can create providers
        // for required dataModelScheme.
        Collection<DataProviderFactory> providerFactories = DataProvidersManager.getInstance().getDataProviderFactories(dataModelScheme);

        // If not found - just return null
        if (providerFactories.size() == 0) {
            return null;
        }

        // Now, when we found all providerFactories that can create provider
        // to serve requested dataModel, search for
        // suitable storage to attach provider to...
        //
        // Will return the first one on success.
        //
        // TODO: should priorities be setuped in case when several providerFactories/storages pairs found?
        //

        final List<DataStorage> availableStorages = getStorages();

        for (DataProviderFactory providerFactory : providerFactories) {
            for (DataStorage storage : availableStorages) {
                // Check that in case this dataProvider requires some Tables to be
                // provided by storage, the storage has this data

                if (!providerFactory.validate(storage)) {
                    continue;
                }

                // Now check that this storage has required table ...

                if (dataMetadata != null && !storage.hasData(dataMetadata)) {
                    continue;
                }

                // Now when we know that this providerFactory creates provider
                // that can be attached to this storage, do attachment and return it

                DataProvider provider = DataProvidersManager.getInstance().createProvider(providerFactory);
                provider.attachTo(storage);
                provider.attachTo(serviceInfoDataStorage);
                addDataFilterListener(provider);
                return provider;
            }
        }

        return null;
//
//        for (DataStorage storage : getStorages()) {
//            if (visDataMetadata != null && !storage.hasData(visDataMetadata)) {
//                continue;
//            }
//
//            for (DataStorageType dss : storage.getStorageTypes()) {
//                DataProvider dataProvider = DataProvidersManager.getInstance().getDataProviderFor(dss, visDataModelScheme);
//                if (dataProvider != null) {
//                    dataProvider.attachTo(serviceInfoDataStorage);
//                    dataProvider.attachTo(storage);
//                    addDataFilterListener(dataProvider);
//                    return dataProvider;
//                }
//            }
//        }
    }

    /**
     * Creates visualizer data provider for data model scheme.
     *
     * @param dataModelScheme
     * @param metadata
     * @return
     */
    public VisualizerDataProvider createVisualizerDataProvider(DataModelScheme dataModelScheme) {
        VisualizerDataProvider dataProvider = DataProvidersManager.getInstance().getDataProviderFor(dataModelScheme);
        if (dataProvider != null && !(dataProvider instanceof DataProvider)) {
            dataProvider.attachTo(serviceInfoDataStorage);
            return dataProvider;
        } else {
            return null;
        }
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

    public List<Indicator<?>> getIndicators() {
        List<Indicator<?>> result = new ArrayList<Indicator<?>>();

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

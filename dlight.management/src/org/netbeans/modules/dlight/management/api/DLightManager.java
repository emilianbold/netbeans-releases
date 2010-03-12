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

import org.netbeans.modules.dlight.management.api.DLightSession.SessionState;
import org.netbeans.modules.dlight.management.ui.spi.DetailsViewEmptyContentProvider;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationManager;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.execution.DLightSessionConfiguration;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightToolkitManagement.DLightSessionHandler;
import org.netbeans.modules.dlight.api.impl.DLightSessionConfigurationAccessor;
import org.netbeans.modules.dlight.api.impl.DLightSessionHandlerAccessor;
import org.netbeans.modules.dlight.api.impl.DLightSessionInternalReference;
import org.netbeans.modules.dlight.api.impl.DLightToolkitManager;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import org.netbeans.modules.dlight.api.visualizer.TableBasedVisualizerConfiguration;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.management.api.impl.DLightSessionsStorage;
import org.netbeans.modules.dlight.management.api.impl.DataProvidersManager;
import org.netbeans.modules.dlight.management.api.impl.DataStorageManager;
import org.netbeans.modules.dlight.management.api.impl.VisualizerProvider;
import org.netbeans.modules.dlight.management.ui.spi.EmptyVisualizerContainerProvider;
import org.netbeans.modules.dlight.management.ui.spi.IndicatorsComponentProvider;
import org.netbeans.modules.dlight.spi.dataprovider.DataProvider;
import org.netbeans.modules.dlight.spi.dataprovider.DataProviderFactory;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.spi.impl.IndicatorAccessor;
import org.netbeans.modules.dlight.spi.impl.IndicatorActionListener;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerDataProvider;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * D-Light manager 
 */
@ServiceProvider(service = org.netbeans.modules.dlight.api.impl.DLightToolkitManager.class)
public final class DLightManager implements DLightToolkitManager, IndicatorActionListener {

    private static final Logger log = DLightLogger.getLogger(DLightManager.class);
    private final List<DLightSessionListener> sessionListeners = Collections.synchronizedList(new ArrayList<DLightSessionListener>());
    private final List<DLightSession> sessions = new ArrayList<DLightSession>();
    private DLightSession activeSession;
    private final DLightManagerSessionStateListener sessionStateListener = new DLightManagerSessionStateListener();
    //we should manage somehow sessions with the same shared storage

    /**
     *
     */
    public DLightManager() {
        for (DLightSessionListener l : IndicatorsComponentProvider.getInstance().getIndicatorComponentListeners()) {
            addDLightSessionListener(l);
        }
        //this.addDLightSessionListener(IndicatorsComponentProvider.getInstance().getIndicatorComponentListener());
    }

    public static DLightManager getDefault() {
        return (DLightManager) Lookup.getDefault().lookup(DLightToolkitManager.class);
    }

    private DLightSession createNewSession(DLightTarget target, String configurationName) {
        return createNewSession(target, configurationName, null);
    }

    private DLightSession createNewSession(DLightTarget target, String configurationName, String sessionName) {
        return createNewSession(target, DLightConfigurationManager.getInstance().getConfigurationByName(configurationName), sessionName);
    }

    private DLightSession createNewSession(DLightTarget target, DLightConfiguration configuration) {
        return createNewSession(target, configuration, null);
    }

    private DLightSession createNewSession(DLightTarget target, DLightConfiguration configuration, String sessionName) {
        // TODO: For now just create new session every time we set a target...
        DLightSession session = newSession(null, target, configuration, sessionName);
        setActiveSession(session);
        if (session != null) {
            session.addSessionStateListener(sessionStateListener);//we should not remove it later, it will be removed automatically when session will be closed
        }
        return session;
   }

    public DLightSession createNewSession(DLightSessionConfiguration sessionConfiguration){
        //in case session need to be opened in read/only mode we should set its state to the Analyze
        DLightSessionConfigurationAccessor accessor = DLightSessionConfigurationAccessor.getDefault();
        DLightConfiguration configuration = accessor.getDLightConfiguration(sessionConfiguration);
        if (configuration == null){
            configuration = DLightConfigurationManager.getInstance().getConfigurationByName(accessor.getDLightConfigurationName(sessionConfiguration));
        }
        DLightSession session = 
                newSession(accessor.getSharedStorageUniqueKey(sessionConfiguration), accessor.getDLightTarget(sessionConfiguration), configuration, accessor.getSessionName(sessionConfiguration));
        setActiveSession(session);
        if (accessor.getSessionMode(sessionConfiguration) == DLightSessionConfiguration.Mode.ANALYZE){
            session.setState(SessionState.ANALYZE);
        }
        if (session != null) {
            session.addSessionStateListener(sessionStateListener);//we should not remove it later, it will be removed automatically when session will be closed
        }
        return session;
    }

    public DLightSessionHandler createSession(DLightSessionConfiguration sessionConfiguration){
        return DLightSessionHandlerAccessor.getDefault().create(createNewSession(sessionConfiguration));
    }

//    public DLightSessionHandler createSession(DLightTarget target, String configurationName) {
//        return createSession(target, configurationName, null);
//    }
//
//    public DLightSessionHandler createSession(DLightTarget target, String configurationName, String sessionName) {
//        return DLightSessionHandlerAccessor.getDefault().create(createNewSession(target, configurationName, sessionName));
//    }
//
//    public DLightSessionHandler createSession(DLightTarget target, DLightConfiguration configuration) {
//        return createSession(target, configuration, null);
//    }
//
//    public DLightSessionHandler createSession(DLightTarget target, DLightConfiguration configuration, String sessionName) {
//        return DLightSessionHandlerAccessor.getDefault().create(createNewSession(target, configuration, sessionName));
//    }

    public void closeSessionOnExit(DLightSession session) {
        SessionState currentSessionState = session.getState();
        if (currentSessionState != SessionState.ANALYZE) {
            session.closeOnExit();
        } else {
            session.close();
        }
    }

    private void cleanupSession(DLightSession session) {
        List<Visualizer<?>> visualizers = session.getVisualizers();
        if (visualizers != null) {
            for (Visualizer<?> v : visualizers) {
                VisualizerContainer vc = (VisualizerContainer) SwingUtilities.getAncestorOfClass(VisualizerContainer.class, v.getComponent());
                // TODO: It could be so, that Visualizer is already closed - in this case vc will be null
                //       Should visualizer be removed from a session on it's closure?
                if (vc != null) {
                    vc.removeVisualizer(v);
                }
            }
        }
        List<Indicator<?>> indicators = session.getIndicators();
        for (Indicator<?> ind : indicators) {
            IndicatorAccessor.getDefault().removeIndicatorActionListener(ind, this);
        }

        sessions.remove(session);
        notifySessionRemoved(session);

        if (sessions.isEmpty()) {
            setActiveSession(null);
        } else {
            setActiveSession(sessions.get(sessions.size() - 1));//last one will be active
        }
    }

    public void closeSession(DLightSession session) {
        if (session.isRunning()) {
            Object result = DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Confirmation(
                    loc("DLightManager.disposeRunningContext.Message", session.getDescription()), // NOI18N
                    loc("DLightManager.disposeRunningContext.Title"), NotifyDescriptor.YES_NO_OPTION)); // NOI18N

            if (result == NotifyDescriptor.NO_OPTION) {
                return;
            }

            session.stop();
            session.close();

        }
    }

    public DLightSession getActiveSession() {
        return activeSession;
    }

    public List<DLightSession> getSessionsList() {
        return sessions;
    }

    public Collection<DataStorage> getStorageByKey(String storageUniqueKey){
        return DataStorageManager.getInstance().getStorages(storageUniqueKey);        

    }

    public DataProvider createDataProvider(String storageUniqueKey, DataModelScheme dataModelScheme, DataTableMetadata dataMetadata) {
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

        final Collection<DataStorage> availableStorages = DataStorageManager.getInstance().getStorages(storageUniqueKey);
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
                ServiceInfoDataStorage serviceInfoDataStorage = DataStorageManager.getInstance().getServiceInfoDataStorageFor(storageUniqueKey);
                provider.attachTo(serviceInfoDataStorage);
                //what to to withe the filters???
//                provider.dataFiltersChanged(this.dataFiltersSupport.getFilters(), false);
//                addDataFilterListener(provider);
                return provider;
            }
        }

        return null;
    }

    public DLightSession setActiveSession(DLightSession newActiveSession) {
        DLightSession oldActiveSession = activeSession;
        if (newActiveSession != oldActiveSession) {

            activeSession = newActiveSession;

            if (oldActiveSession != null) {
                oldActiveSession.setActive(false);
            }

            if (newActiveSession != null) {
                newActiveSession.setActive(true);
            }

            notifySessionActivated(oldActiveSession, newActiveSession);
        }

        return activeSession;
    }

    /**
     * Sets session as active and starts it
     * @param dlightSession
     */
    public void startSession(DLightSession dlightSession) {
        setActiveSession(dlightSession);
        dlightSession.start();
    }

    public void stopActiveSession() {
        activeSession.stop();
    }

    public void stopSession(DLightSession session) {
        session.stop();
    }

    public void addDLightSessionListener(DLightSessionListener listener) {
        if (listener == null) {
            return;
        }
        if (!sessionListeners.contains(listener)) {
            sessionListeners.add(listener);

            for (DLightSession s : sessions) {
                listener.sessionAdded(s);
            }

            listener.activeSessionChanged(null, activeSession);
        }
    }

    public void removeDLightSessionListener(DLightSessionListener listener) {
        sessionListeners.remove(listener);
    }

    private DLightSession newSession(String sharedStorageKey, DLightTarget target, DLightConfiguration configuration, String sessionName) {
        DLightSession session = new DLightSession(sharedStorageKey, sessionName);
        session.setExecutionContext(new ExecutionContext(target, configuration));
        sessions.add(session);
        List<Indicator<?>> indicators = session.getIndicators();
        for (Indicator<?> ind : indicators) {
            IndicatorAccessor.getDefault().addIndicatorActionListener(ind, this);
        }
        notifySessionAdded(session);
        return session;
    }

    private DLightSession findIndicatorOwner(Indicator<?> ind) {
        for (DLightSession session : sessions) {
            if (session.containsIndicator(ind)) {
                return session;
            }
        }
        return activeSession;
    }

    private Visualizer<?> openVisualizer(String toolID, final VisualizerConfiguration visualizerConfiguration, DLightSession dlightSession) {
        Visualizer<?> visualizer = null;

        //Check if we have already instance in the session:

        if (dlightSession.hasVisualizer(toolID, visualizerConfiguration.getID())) {
            visualizer = dlightSession.getVisualizer(toolID, visualizerConfiguration.getID());
            @SuppressWarnings("unchecked")
            Visualizer<VisualizerConfiguration> v = (Visualizer<VisualizerConfiguration>) visualizer;
            v.updateVisualizerConfiguration(visualizerConfiguration);
            if (visualizer instanceof SessionStateListener) {
                dlightSession.addSessionStateListener((SessionStateListener) visualizer);
                ((SessionStateListener) visualizer).sessionStateChanged(dlightSession, null, dlightSession.getState());
            }
            VisualizerContainer container = visualizer.getDefaultContainer();
            DLightTool tool = dlightSession.getToolByID(toolID);
            if (tool != null) {
                container.addVisualizer(toolID, tool.getDetailedName(), visualizer);
            } else {
                container.addVisualizer(toolID, toolID, visualizer);
            }
            container.showup();
            visualizer.refresh();
            return visualizer;
        }

        /*
         * Two conditions should be met in order to create and open visualizer:
         *
         * 1. registered factory that can create Visualizer with such a visualizerID
         *    should exist;
         *
         * 2. there should be a DataProvider that:
         *    a) hand can 'talk' to one of *already configured!* DataStorage (i.e. registered DataStorage
         *    should support the same DataStorageScheme as provider does)
         *
         *    b) can serve Visualizer's needs (i.e. implements StackDataProvider for CallersCaleesVisualizer) and
         *
         * Scope: ExecutionContext
         *
         */

        DataProvider dataProvider = null;

        DataModelScheme visualizerDataScheme = visualizerConfiguration.getSupportedDataScheme();

        if (visualizerConfiguration instanceof TableBasedVisualizerConfiguration) {
            TableBasedVisualizerConfiguration vc = (TableBasedVisualizerConfiguration) visualizerConfiguration;
            DataTableMetadata tableMetadata = vc.getMetadata();

            dataProvider = dlightSession.createDataProvider(visualizerDataScheme, tableMetadata);
        } else {
            dataProvider = dlightSession.createDataProvider(visualizerDataScheme, null);
        }


        if (dataProvider != null) {
            // Found! Can create visualizer with this id for this dataProvider
            visualizer = VisualizerProvider.getInstance().createVisualizer(visualizerConfiguration, dataProvider);
            if (visualizer instanceof SessionStateListener) {
                dlightSession.addSessionStateListener((SessionStateListener) visualizer);
                ((SessionStateListener) visualizer).sessionStateChanged(dlightSession, null, dlightSession.getState());
            }
        }

        //there is one more changes to find VisualizerDataProvider without any storage attached

        if (visualizer == null) {
            VisualizerDataProvider visDataProvider = dlightSession.createVisualizerDataProvider(visualizerConfiguration.getSupportedDataScheme());
            if (visDataProvider != null) {
                visualizer = VisualizerProvider.getInstance().createVisualizer(visualizerConfiguration, visDataProvider);
                if (visualizer instanceof SessionStateListener) {
                    dlightSession.addSessionStateListener((SessionStateListener) visualizer);
                    ((SessionStateListener) visualizer).sessionStateChanged(dlightSession, null, dlightSession.getState());
                }
            }
        }

        if (visualizer == null) {
            log.fine("Unable to find factory to create Visualizer with ID == " + visualizerConfiguration.getID()); // NOI18N
            return null;
        }

        VisualizerContainer container = visualizer.getDefaultContainer();
        DLightTool tool = dlightSession.getToolByID(toolID);
        if (tool != null) {
            container.addVisualizer(toolID, tool.getDetailedName(), visualizer);
        } else {
            container.addVisualizer(toolID, toolID, visualizer);
        }
        container.showup();
        dlightSession.putVisualizer(toolID, visualizerConfiguration.getID(), visualizer);
        return visualizer;
    }

    private void openEmptyVisualizer(String vcID, String toolID, DLightSession session) {
        DetailsViewEmptyContentProvider emptyContentProvider = Lookup.getDefault().lookup(DetailsViewEmptyContentProvider.class);
        if (emptyContentProvider == null) {
            emptyContentProvider = DefaultDetailsViewEmptyContentProvider.instance;
        }
        JComponent view = null;
        boolean isFirstView = true;
        if (Lookup.getDefault().lookup(EmptyVisualizerContainerProvider.class) == null) {
            return;
        }
        DLightTool tool = session.getToolByID(toolID);
        if (tool == null) {
            return;//nothing to show
        }
        Collection<? extends EmptyVisualizerContainerProvider> providers = Lookup.getDefault().lookupAll(EmptyVisualizerContainerProvider.class);
        VisualizerContainer container = Lookup.getDefault().lookup(EmptyVisualizerContainerProvider.class).getEmptyVisualizerContainer();
        if (vcID != null) {

            for (EmptyVisualizerContainerProvider provider : providers) {
                VisualizerContainer vC = provider.getEmptyVisualizerContainer(vcID);
                if (vC != null) {
                    container = vC;
                    break;
                }
            }
        }

        if (container == null) {
            for (EmptyVisualizerContainerProvider provider : providers) {
                VisualizerContainer vC = provider.getEmptyVisualizerContainer();
                if (vC != null) {
                    container = vC;
                    break;
                }
            }
        }

        if (container == null) {
            return;
        }

        String name = toolID;
        if (tool != null) {
            name = tool.getDetailedName();
        }
        for (ExecutionContext context : session.getExecutionContexts()) {
            view = emptyContentProvider.getEmptyView(context.getDLightConfiguration(), tool, context.getTarget());
            if (isFirstView) {
                container.setContent(name, view);
                isFirstView = false;
            } else {
                container.addContent(name, view);
            }
        }
        container.showup();
    }

    private void notifySessionRemoved(DLightSession session) {
        synchronized (sessionListeners) {
            for (DLightSessionListener l : sessionListeners) {
                l.sessionRemoved(session);
            }
        }
    }

    private void notifySessionAdded(DLightSession session) {
        synchronized (sessionListeners) {
            for (DLightSessionListener l : sessionListeners) {
                l.sessionAdded(session);
            }
        }
    }

    private void notifySessionActivated(DLightSession oldActiveSession, DLightSession newActiveSession) {
        synchronized (sessionListeners) {
            for (DLightSessionListener l : sessionListeners) {
                l.activeSessionChanged(oldActiveSession, newActiveSession);
            }
        }
    }

    private void resetContext(ExecutionContext context) {
        // TODO: implement
    }

    private static String loc(String key) {
        return NbBundle.getMessage(DLightManager.class, key);
    }

    private static String loc(String key, Object param1) {
        return NbBundle.getMessage(DLightManager.class, key, param1);
    }

    public void startSession(DLightSessionHandler handler) {
        DLightSessionInternalReference reference = DLightSessionHandlerAccessor.getDefault().getSessionReferenceImpl(handler);
        if (!(reference instanceof DLightSession)) {
            throw new IllegalArgumentException("Illegal Argument, reference you are trying to use " + // NOI18N
                    "to start D-Light session is invalid");//NOI18N
        }

        startSession((DLightSession) reference);
    }

    public void stopSession(DLightSessionHandler handler) {
        DLightSessionInternalReference reference = DLightSessionHandlerAccessor.getDefault().getSessionReferenceImpl(handler);

        if (!(reference instanceof DLightSession)) {
            throw new IllegalArgumentException("Illegal Argument, reference you are trying to use " + // NOI18N
                    "to stop D-Light session is invalid");//NOI18N
        }

        stopSession((DLightSession) reference);
    }

    public void revalidateSessions() {
        for (DLightSession session : sessions) {
            session.revalidate();
        }
    }

    public void openVisualizerForIndicator(Indicator<?> source, VisualizerConfiguration vc) {
        DLightSession session = findIndicatorOwner(source);
        //set active session
        setActiveSession(session);
        boolean found = openVisualizer(IndicatorAccessor.getDefault().getToolID(source), vc, session) != null;
        if (!found) {
            openEmptyVisualizer(vc.getID(), IndicatorAccessor.getDefault().getToolID(source), session);
        }
    }

    public final DataProvider getDataProviderFor(String storageUniqueKey, VisualizerConfiguration visualizerConfiguration){
        DataProvider dataProvider = null;

        DataModelScheme visualizerDataScheme = visualizerConfiguration.getSupportedDataScheme();

        if (visualizerConfiguration instanceof TableBasedVisualizerConfiguration) {
            TableBasedVisualizerConfiguration vc = (TableBasedVisualizerConfiguration) visualizerConfiguration;
            DataTableMetadata tableMetadata = vc.getMetadata();

            dataProvider = createDataProvider(storageUniqueKey, visualizerDataScheme, tableMetadata);
        } else {
            dataProvider = createDataProvider(storageUniqueKey, visualizerDataScheme, null);
        }

        return dataProvider;
    }


    public final void openVisualizer(DLightSession session, String toolID, VisualizerConfiguration vc) {
        setActiveSession(session);
        boolean found = openVisualizer(toolID, vc, session) != null;
        if (!found) {
            openEmptyVisualizer(vc.getID(), toolID, session);
        }
    }

    public void mouseClickedOnIndicator(Indicator<?> source) {
        DLightSession session = findIndicatorOwner(source);
        //set active session
        setActiveSession(session);
        List<VisualizerConfiguration> list = IndicatorAccessor.getDefault().getVisualizerConfigurations(source);
        boolean found = false;
        if (list != null) {
            for (VisualizerConfiguration vc : list) {
                if (openVisualizer(IndicatorAccessor.getDefault().getToolID(source), vc, session) != null) {
                    found = true;
                    break;
                }

            }
        }
        if (!found) {
            openEmptyVisualizer(null, IndicatorAccessor.getDefault().getToolID(source), session);
        }
    }

    /**
     * Opens session using the ID
     * @param sessionID
     * @return
     */
    public DLightSessionHandler open(String sessionID) {
        return DLightSessionHandlerAccessor.getDefault().create(DLightSessionsStorage.getInstance().openSession(sessionID));
    }


    public String save(DLightSessionHandler reference) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String save(String dir, DLightSessionHandler reference) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String save(ExecutionEnvironment env, String dir, DLightSessionHandler handler) {
        DLightSessionInternalReference reference = DLightSessionHandlerAccessor.getDefault().getSessionReferenceImpl(handler);
        if (!(reference instanceof DLightSession)) {
            throw new IllegalArgumentException("Illegal Argument, reference you are trying to use " + // NOI18N
                    "to start D-Light session is invalid");//NOI18N
        }

        return null;
    }

    private class DLightManagerSessionStateListener implements SessionStateListener {

        public void sessionStateChanged(DLightSession session, SessionState oldState, SessionState newState) {
            if (newState == SessionState.CLOSED) {
                cleanupSession(session);
            }
        }
    }

    private static final class DefaultDetailsViewEmptyContentProvider implements DetailsViewEmptyContentProvider {

        private static final DefaultDetailsViewEmptyContentProvider instance = new DefaultDetailsViewEmptyContentProvider();
        private final JPanel p;

        DefaultDetailsViewEmptyContentProvider() {
            p = new JPanel();
            p.setLayout(new BorderLayout());
            p.add(new JLabel(loc("DLightManager.noData")), BorderLayout.CENTER); //NOI18N

        }

        public JComponent getEmptyView(DLightConfiguration configuration, DLightTool tool, DLightTarget targetToValidateWith) {
            return p;

        }
    }
}

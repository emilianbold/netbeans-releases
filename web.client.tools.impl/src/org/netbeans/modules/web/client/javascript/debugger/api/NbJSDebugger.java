/**
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
package org.netbeans.modules.web.client.javascript.debugger.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import java.util.logging.Level;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;
import org.netbeans.api.debugger.DebuggerEngine.Destructor;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.web.client.javascript.debugger.NbJSDebuggerConstants;
import org.netbeans.modules.web.client.javascript.debugger.http.ui.models.HttpActivitiesModel;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSEditorUtil;
import org.netbeans.modules.web.client.tools.common.dbgp.Feature;
import org.netbeans.modules.web.client.javascript.debugger.filesystem.URLContentProvider;
//import org.netbeans.modules.web.client.javascript.debugger.filesystem.URLContentProviderImpl;
import org.netbeans.modules.web.client.javascript.debugger.filesystem.URLFileObject;
import org.netbeans.modules.web.client.javascript.debugger.filesystem.URLFileObjectFactory;
import org.netbeans.modules.web.client.javascript.debugger.http.ui.models.HttpActivitiesWrapper;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSBreakpoint;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSCallStackFrame;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebugger;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerConsoleEvent;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerConsoleEventListener;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerEvent;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerEventListener;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerState;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSHttpMessageEvent;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSSource;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSURILocation;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSWindow;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSBreakpointImpl;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSFactory;
import org.netbeans.modules.web.client.tools.javascript.debugger.spi.JSDebuggerFactory;
import org.netbeans.modules.web.client.tools.javascript.debugger.spi.JSDebuggerFactoryLookup;
import org.netbeans.modules.web.client.javascript.debugger.models.NbJSPreferences;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.NbJSBreakpoint;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.NbJSFileObjectBreakpoint;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.NbJSURIBreakpoint;
import org.netbeans.modules.web.client.tools.api.JSAbstractLocation;
import org.netbeans.modules.web.client.tools.api.JSLocation;
import org.netbeans.modules.web.client.tools.api.JSToNbJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.NbJSLocation;
import org.netbeans.modules.web.client.tools.api.NbJSToJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.WebClientToolsProjectUtils;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSHttpMessageEventListener;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.text.Line;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * This is the NetBEans IDE aware JavaScript debugger implementation.
 *
 * @author Sandip V. Chitale <sandipchitale@netbeans.org>
 */
public final class NbJSDebugger {

    private static final Object SESSION_LOCK = new Object();

    private final WebClientToolsProjectUtils.Browser browser;
    private final URI uri;
    private Lookup lookup;
    private final List<JSDebuggerEventListener> listeners;
    private final List<JSHttpMessageEventListener> httpListeners;
    private final PropertyChangeSupport propertyChangeSupport;
    public static final String PROPERTY_SELECTED_FRAME = "selectedFrame"; // NOI18N
    private JSCallStackFrame selectedFrame;
    public static final String PROPERTY_SOURCES = JSDebugger.PROPERTY_SOURCES;
    public static final String PROPERTY_WINDOWS = JSDebugger.PROPERTY_WINDOWS;
    public static final String PROPERTY_RELOADSOURCES = JSDebugger.PROPERTY_RELOADSOURCES;
    private URLContentProvider contentProvider;
    private JSDebugger debugger;
    private HashMap<Breakpoint, JSBreakpointImpl> breakpointsMap = new HashMap<Breakpoint, JSBreakpointImpl>();
    private StartDebuggerTask startDebuggerTask;
    private final boolean ignoreQueryStrings;
    
    private class JSDebuggerEventListenerImpl implements JSDebuggerEventListener {

        public void onDebuggerEvent(JSDebuggerEvent debuggerEvent) {
            final JSDebuggerState debuggerState = debuggerEvent.getDebuggerState();
            setState(debuggerState);
        }
    }
    
    private class JSDebuggerConsoleEventListenerImpl implements JSDebuggerConsoleEventListener {

        public void onConsoleEvent(JSDebuggerConsoleEvent consoleEvent) {
            if (console != null) {
                String message = consoleEvent.getMessage();
                switch (consoleEvent.getType()) {
                    case STDERR:
                        console.getErr().println(message);
                        break;
                    case STDOUT:
                        console.getOut().println(message);
                        break;
                }
            }
        }
    }

    private class JSHttpMessageEventListenerImpl implements JSHttpMessageEventListener {

        public void onHttpMessageEvent(JSHttpMessageEvent jsHttpMessageEvent) {

            if (jsHttpMessageEvent != null) {
                fireJSHttpMessageEvent(jsHttpMessageEvent);
            }
        }
    }

    /* Joelle: Create your Implementation of JSHttpMessageEventListenerImpl here*/
    private class DebuggerManagerListenerImpl extends DebuggerManagerAdapter {

        @Override
        public void breakpointAdded(Breakpoint bp) {
            if (bp instanceof NbJSBreakpoint) {
                setBreakpoint((NbJSBreakpoint) bp);
            }
        }

        @Override
        public void breakpointRemoved(Breakpoint bp) {
            if (bp instanceof NbJSBreakpoint) {
                removeBreakpoint(bp);
            }
        }
    }

    private class PropertyChangeListenerImpl implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(JSDebugger.PROPERTY_SOURCES) ||
                    evt.getPropertyName().equals(JSDebugger.PROPERTY_WINDOWS) ||
                    evt.getPropertyName().equals(JSDebugger.PROPERTY_RELOADSOURCES)) {
                try {
                    propertyChangeSupport.firePropertyChange(
                            evt.getPropertyName(),
                            evt.getOldValue(),
                            evt.getNewValue());
                } catch (RuntimeException re) {
                    Log.getLogger().log(Level.INFO, re.getMessage(), re);
                }
            }

            if (evt.getPropertyName().equals(JSDebugger.PROPERTY_SOURCES) ||
                    evt.getPropertyName().equals(JSDebugger.PROPERTY_WINDOWS)) {
                openEditorsForWindows();
            } else if (evt.getPropertyName().equals(JSDebugger.PROPERTY_RELOADSOURCES)) {
                refreshCacheForSources((JSSource[])evt.getNewValue());
            }
        }
    }

    private class BreakpointPropertyChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            if (source instanceof NbJSBreakpoint) {
                NbJSBreakpoint bp = (NbJSBreakpoint) source;
                if (evt.getPropertyName().equals(Breakpoint.PROP_ENABLED)) {
                    updateBreakpoint(bp);
                } else if (evt.getPropertyName().equals(Line.PROP_LINE_NUMBER)) {
                    // For now treat it as a remove and add
                    // This is because the line number is
                    // part of breakpoint id.
                    removeBreakpoint(bp);
                    setBreakpoint((NbJSBreakpoint) bp);
                } else if (evt.getPropertyName().equals(NbJSBreakpoint.PROP_UPDATED)) {
                    updateBreakpoint(bp);
                }
            }
        }
    }

    private class PreferenceChangeListenerImpl implements PreferenceChangeListener {

        public void preferenceChange(PreferenceChangeEvent evt) {
            String pref = evt.getKey();
            if (NbJSPreferences.PROPERTIES.PROP_HTTP_MONITOR_ENABLED.equals(pref)) {
                setBooleanFeatures(Feature.Name.HTTP_MONITOR, Boolean.parseBoolean(evt.getNewValue()));
            } else if (NbJSPreferences.PROPERTIES.PROP_HTTP_MONITOR_OPENED.equals(pref)) {
                NbJSPreferences preferences = NbJSPreferences.getInstance();
                
                boolean enabled = preferences.getHttpMonitorEnabled();
                boolean monitorOpened = Boolean.parseBoolean(evt.getNewValue());

                if (monitorOpened && enabled) {
                    setBooleanFeatures(Feature.Name.HTTP_MONITOR, true);
                }
            }
        }
    }
    
    private class StartDebuggerTask implements Cancellable, Runnable {
        
        private boolean success = false;
        ProgressHandle handle;
        
        public StartDebuggerTask() {
            this.handle = ProgressHandleFactory.createHandle(
                    NbBundle.getMessage(NbJSDebugger.class, "MSG_TASK_INITIALIZING_JAVASCRIPT_DEBUGGER"), this);
        }
        
        
        public void run() {
            handle.start();
            try {
                success = debugger.startDebugging();
            } catch (Exception ex) {
                Log.getLogger().log(Level.INFO, "Unexpected exception while starting debugger", ex);
            }
            handle.finish();
        }
        
        public boolean cancel() {
            try {
                debugger.cancelStartDebugging();
                return true;
            } catch (Exception ex) {
                Log.getLogger().log(Level.INFO, "Unexpected exception while canceling debugger start", ex);
                return false;
            }
        }
        
        public boolean taskSucceeded() {
            return success;
        }
        
    }
    
    private JSDebuggerEventListener debuggerListener;
    private JSDebuggerConsoleEventListener debuggerConsoleEventListener;
    private JSHttpMessageEventListener httpMessageEventListener;
    private PropertyChangeListener propertyChangeListener;
    private DebuggerManagerListenerImpl debuggerManagerListener;
    private BreakpointPropertyChangeListener breakpointPropertyChangeListener;
    private PreferenceChangeListener preferenceChangeListener;
    private InputOutput console;

    NbJSDebugger(URI uri, HtmlBrowser.Factory browser, Lookup lookup, JSDebugger debugger) {
        this.uri = uri;
        this.browser = (browser == WebClientToolsProjectUtils.getInternetExplorerBrowser()) ?
            WebClientToolsProjectUtils.Browser.INTERNET_EXPLORER : WebClientToolsProjectUtils.Browser.FIREFOX;
        this.lookup = lookup;
        this.debugger = debugger;

        listeners = new CopyOnWriteArrayList<JSDebuggerEventListener>();
        propertyChangeSupport = new PropertyChangeSupport(this);
        contentProvider = new NbJSDebuggerContentProvider(this);

        // Add listener to JSDebugger
        debuggerListener = new JSDebuggerEventListenerImpl();
        this.debugger.addJSDebuggerEventListener(WeakListeners.create(
                JSDebuggerEventListener.class,
                debuggerListener,
                this.debugger));

        debuggerConsoleEventListener = new JSDebuggerConsoleEventListenerImpl();
        this.debugger.addJSDebuggerConsoleEventListener(WeakListeners.create(
                JSDebuggerConsoleEventListener.class,
                debuggerConsoleEventListener,
                this.debugger));

        // Add HttpMessageEventListener
        httpListeners = new CopyOnWriteArrayList<JSHttpMessageEventListener>();
        httpMessageEventListener = new JSHttpMessageEventListenerImpl();
        this.debugger.addJSHttpMessageEventListener(WeakListeners.create(
                JSHttpMessageEventListener.class,
                httpMessageEventListener,
                this.debugger));

        // Add DebuggerManagerListener
        debuggerManagerListener = new DebuggerManagerListenerImpl();
        DebuggerManager.getDebuggerManager().addDebuggerListener(debuggerManagerListener);



        propertyChangeListener = new PropertyChangeListenerImpl();
        this.debugger.addPropertyChangeListener(WeakListeners.propertyChange(propertyChangeListener, this.debugger));


        breakpointPropertyChangeListener = new BreakpointPropertyChangeListener();

        console = IOProvider.getDefault().getIO(
                NbBundle.getMessage(NbJSDebugger.class, "TITLE_CONSOLE", getURI(), getID()), true); // NOI18N

        NbJSPreferences prefs = NbJSPreferences.getInstance();
        ignoreQueryStrings = prefs.getIgnoreQueryStrings();
    }

    public static void startDebugging(URI uri, HtmlBrowser.Factory browser, Lookup lookup) {

        JSDebuggerFactory factory = JSDebuggerFactoryLookup.getFactory(browser, uri);
        if (factory == null) {
            // No factory to handle the browser.
            try {
                URLDisplayer.getDefault().showURL(uri.toURL());
            } catch (MalformedURLException e) {
                // TODO
            }
        } else {
            DebuggerManager manager = DebuggerManager.getDebuggerManager();

            // disallow multiple Firefox debugging sessions
            if (browser == WebClientToolsProjectUtils.getFirefoxBrowser()) {
                synchronized (SESSION_LOCK) {
                    Session runningSession = null;
                    Session[] sessions = manager.getSessions();
                    for (Session session : sessions) {
                        NbJSDebugger runningNbJSDebugger = session.lookupFirst(null, NbJSDebugger.class);
                        if (runningNbJSDebugger != null && runningNbJSDebugger.getBrowser() == WebClientToolsProjectUtils.Browser.FIREFOX) {
                            runningSession = session;
                            break;
                        }
                    }
                    if (runningSession != null) {
                        boolean reload = displayReloadSessionDialog();
                        if (!reload) {
                            return;
                        } else {
                            runningSession.kill();
                        }
                    }
                }
            }

            NbJSDebugger nbJSdebugger = new NbJSDebugger(uri, browser, lookup, factory.startDebugging(browser, uri));
            List<? super Object> services = new ArrayList<Object>();
            services.add(nbJSdebugger);

            HttpActivitiesWrapper wrapper = new HttpActivitiesWrapper(new HttpActivitiesModel(nbJSdebugger));
            //HttpActivitiesModel activitiesModel = new HttpActivitiesModel(nbJSdebugger);
            services.add(wrapper);

            NbJSToJSLocationMapper nbJSToJSLocation = lookup.lookup(NbJSToJSLocationMapper.class);
            if (nbJSToJSLocation != null) {
                services.add(nbJSToJSLocation);
            }
            JSToNbJSLocationMapper jSToNbJSLocation = lookup.lookup(JSToNbJSLocationMapper.class);
            if (jSToNbJSLocation != null) {
                services.add(jSToNbJSLocation);
            }
            
            DebuggerInfo debuggerInfo = DebuggerInfo.create(
                    NbJSDebuggerConstants.DEBUG_INFO_ID,
                    services.toArray());
            manager.startDebugging(debuggerInfo);
        }
    }

    public void startJSDebugging() {
        if (debugger != null) {
            startDebuggerTask = new StartDebuggerTask();
            RequestProcessor.Task task = RequestProcessor.getDefault().post(startDebuggerTask);
            task.waitFinished();
            
            synchronized(this) {
                if (startDebuggerTask != null) {
                    boolean success = startDebuggerTask.taskSucceeded();
                    startDebuggerTask = null;

                    if (!success) {
                        setState(JSDebuggerState.DISCONNECTED);
                    }
                }
            }
        }
    }

    public WebClientToolsProjectUtils.Browser getBrowser() {
        return browser;
    }

    public boolean supportsHttpMonitor() {
        return browser == WebClientToolsProjectUtils.Browser.FIREFOX;
    }

    public String getID() {
        if (debugger != null) {
            return debugger.getID();
        }
        return null;
    }

    public boolean isIgnoringQueryStrings() {
        return ignoreQueryStrings;
    }

    // Event listener
    public void addJSDebuggerEventListener(JSDebuggerEventListener debuggerEventListener) {
        listeners.add(debuggerEventListener);
    }

    public void removeJSDebuggerEventListener(JSDebuggerEventListener debuggerEventListener) {
        listeners.remove(debuggerEventListener);
    }

    private void fireJSDebuggerEvent(JSDebuggerEvent debuggerEvent) {
        for (JSDebuggerEventListener listener : listeners) {
            try {
                listener.onDebuggerEvent(debuggerEvent);
            } catch (RuntimeException re) {
                Log.getLogger().log(Level.INFO, re.getMessage(), re);
            }
        }
    }


    // Http Event listener
    public void addJSHttpMessageEventListener(JSHttpMessageEventListener httpMessageEventListener) {
        httpListeners.add(httpMessageEventListener);
    }

    public void removeJSHttpMessageEventListener(JSHttpMessageEventListener httpMessageEventListener) {
        httpListeners.remove(httpMessageEventListener);
    }

    private void fireJSHttpMessageEvent(JSHttpMessageEvent httpMessageEvent) {

        //Logger.getLogger(this.getClass().getName()).info("****** HTTP MESSAGE RECEIVED AND TRIGGERED ********: " + httpMessageEvent);
        for (JSHttpMessageEventListener httpListener : httpListeners) {
            try {
                httpListener.onHttpMessageEvent(httpMessageEvent);
            } catch (RuntimeException re) {
                Log.getLogger().log(Level.INFO, re.getMessage(), re);
            }
        }
    }


    // Property Change Listeners
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    // State
    private JSDebuggerState state = JSDebuggerState.NOT_CONNECTED;

    public synchronized JSDebuggerState getState() {
        return state;
    }

    public void setBooleanFeatures(Feature.Name feature, boolean value) {
        if (debugger != null) {
            if (Feature.Name.HTTP_MONITOR.equals(feature)) {
                synchronized (this) {
                    debugger.setBooleanFeature(feature, value);
                }
            } else {
                throw new UnsupportedOperationException("Setting features for Feature: " + feature + " has yet to be implmented");
            }
        }
    }

    synchronized void setState(JSDebuggerState state) {      
        this.state = state;
        if (state == JSDebuggerState.STARTING_INIT) {
            // Set the initial feature set
            NbJSPreferences preferences = NbJSPreferences.getInstance();

            if (preferenceChangeListener == null) {
                preferenceChangeListener = new PreferenceChangeListenerImpl();
                NbJSPreferences nbJSPreferences = NbJSPreferences.getInstance();
                nbJSPreferences.addPreferenceChangeListener(preferenceChangeListener);
            }
            
            debugger.setBooleanFeature(Feature.Name.SHOW_FUNCTIONS, preferences.getShowFunctions());
            debugger.setBooleanFeature(Feature.Name.SHOW_CONSTANTS, preferences.getShowConstants());
            debugger.setBooleanFeature(Feature.Name.SUSPEND_ON_FIRST_LINE, preferences.getSuspendOnFirstLine());
            debugger.setBooleanFeature(Feature.Name.SUSPEND_ON_EXCEPTIONS, preferences.getSuspendOnExceptions());
            debugger.setBooleanFeature(Feature.Name.SUSPEND_ON_ERRORS, preferences.getSuspendOnErrors());
            debugger.setBooleanFeature(Feature.Name.SUSPEND_ON_DEBUGGERKEYWORD, preferences.getSuspendOnDebuggerKeyword());
            debugger.setBooleanFeature(Feature.Name.IGNORE_QUERY_STRINGS, ignoreQueryStrings);

            debugger.setBooleanFeature(Feature.Name.HTTP_MONITOR, preferences.getHttpMonitorEnabled() && preferences.getHttpMonitorOpened());
            
            setBreakPoints();
        }
        if (state == JSDebuggerState.STARTING_READY) {
            if (console != null) {
                console.getOut().println(NbBundle.getMessage(NbJSDebugger.class, "MSG_CONSOLE_JSDEBUGGER_STARTED") + getURI()); // NOI18N
            }
        }
        if (state.getState() == JSDebuggerState.State.SUSPENDED) {
            setCurrentSession();
            JSCallStackFrame[] callStackFrames = getCallStackFrames();
            if (callStackFrames != null && callStackFrames.length > 0) {
                selectFrame(callStackFrames[0]);
            } else {
                selectFrame(null);
            }
        } else {
            selectFrame(null);
        }
        JSDebuggerEvent resourcedDebuggerEvent =
                new JSDebuggerEvent(NbJSDebugger.this, this.state);
        fireJSDebuggerEvent(resourcedDebuggerEvent);
        if (state.getState() == JSDebuggerState.State.DISCONNECTED) {            
            if (debuggerManagerListener != null) {
                DebuggerManager.getDebuggerManager().removeDebuggerListener(debuggerManagerListener);
                debuggerManagerListener = null;
            }
            
            if (preferenceChangeListener != null) {
                NbJSPreferences nbJSPreferences = NbJSPreferences.getInstance();
                nbJSPreferences.removePreferenceChangeListener(preferenceChangeListener);
                preferenceChangeListener = null;
            }

            if (console != null) {
                console.getOut().println(NbBundle.getMessage(NbJSDebugger.class, "MSG_CONSOLE_CLOSE_JAVASCRIPT_DEBUGGER"));
                console.closeInputOutput();
                console = null;
            }
            
            if (startDebuggerTask != null) {
                startDebuggerTask.cancel();
                startDebuggerTask = null;
            }
        }
    }

    private void setCurrentSession() {
        DebuggerManager manager = DebuggerManager.getDebuggerManager();
        
        for (Session nextSession : manager.getSessions()) {
            NbJSDebugger debuggr = nextSession.lookupFirst(null, NbJSDebugger.class);
            if (debuggr == this) {
                manager.setCurrentSession(nextSession);
                return;
            }
        }
        
        Log.getLogger().warning("Could not find session for javascript debugger");
    }
    
    private void setBreakPoints() {
        for (Breakpoint bp : DebuggerManager.getDebuggerManager().getBreakpoints()) {
            if (bp instanceof NbJSBreakpoint) {
                setBreakpoint((NbJSBreakpoint) bp);
            }
        }
    }

    private void setBreakpoint(final NbJSBreakpoint bp) {
        JSDebuggerState currentState = getState();
        
        if (currentState.getState() == JSDebuggerState.State.DISCONNECTED ||
                currentState.getState() == JSDebuggerState.State.NOT_CONNECTED) {
            return;
        }
        
        JSBreakpointImpl bpImpl = breakpointsMap.get(bp);
        if (bpImpl != null) {
            return;
        }

        JSURILocation jsURILocation = null;
        if (bp instanceof NbJSFileObjectBreakpoint) {
            jsURILocation = (JSURILocation) getJSLocation(((NbJSFileObjectBreakpoint) bp).getLocation());
        } else if (bp instanceof NbJSURIBreakpoint) {
            jsURILocation = ((NbJSURIBreakpoint) bp).getLocation();
        }
        
        if (jsURILocation != null) {
            bpImpl = new JSBreakpointImpl(jsURILocation);
            //TODO set the type correctly for other types of breakpoints
            bpImpl.setType(JSBreakpoint.Type.LINE);
            bpImpl.setEnabled(bp.isEnabled());

            bpImpl.setHitValue(0);
            bpImpl.setHitCondition(HIT_COUNT_FILTERING_STYLE.EQUAL);
            HIT_COUNT_FILTERING_STYLE hitCountFilteringStyle = bp.getHitCountFilteringStyle();
            int hitCountFilter = bp.getHitCountFilter();
            if (hitCountFilteringStyle != null && hitCountFilter > 0) {
                bpImpl.setHitValue(bp.getHitCountFilter());
                bpImpl.setHitCondition(hitCountFilteringStyle);
            }

            String condition = bp.getCondition();
            if (condition == null) {
                condition = "";
            }
            bpImpl.setCondition(condition);
            final JSBreakpointImpl tmpBreakpointImp = bpImpl;
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    List<String> bpIds = debugger.setBreakpoint(tmpBreakpointImp);
                    if (bpIds != null) {
                        for (String bpId : bpIds) {
                            tmpBreakpointImp.addId(bpId);
                        }
                        
                        breakpointsMap.put(bp, tmpBreakpointImp);
                        bp.addPropertyChangeListener(WeakListeners.propertyChange(breakpointPropertyChangeListener, bp));
                    }
                }
            });
        }
    }

    private void removeBreakpoint(Breakpoint bp) {
        JSDebuggerState currentState = getState();
        if (currentState.getState() == JSDebuggerState.State.DISCONNECTED ||
                currentState.getState() == JSDebuggerState.State.NOT_CONNECTED) {
            return;
        }
        JSBreakpointImpl bpImpl = breakpointsMap.get(bp);
        if (bpImpl != null) {
            boolean removed = false;
            for (String id : bpImpl.getIds()) {
                removed = debugger.removeBreakpoint(id) || removed;
            }
            
            if (removed) {
                breakpointsMap.remove(bp);
            }
        }
    }

    private void updateBreakpoint(NbJSBreakpoint bp) {
        JSDebuggerState currentState = getState();
        if (currentState.getState() == JSDebuggerState.State.DISCONNECTED ||
                currentState.getState() == JSDebuggerState.State.NOT_CONNECTED) {
            return;
        }
        JSBreakpointImpl bpImpl = breakpointsMap.get(bp);
        if (bpImpl == null) {
            Log.getLogger().log(Level.INFO, "Cannot update non existing breakpoint");   //NOI18N
            return;
        }

        Boolean enabled = bp.isEnabled();
        int line = -1;
        int hitValue = bp.getHitCountFilter();
        HIT_COUNT_FILTERING_STYLE hitCondition = bp.getHitCountFilteringStyle();
        String condition = bp.getCondition();
        if (hitCondition == null) {
            hitValue = 0;
            hitCondition = HIT_COUNT_FILTERING_STYLE.EQUAL;
        }
        if (condition == null) {
            condition = "";
        }

        for (String id : bpImpl.getIds()) {
            debugger.updateBreakpoint(id, enabled, line, hitValue, hitCondition, condition);
        }
    }

    private JSLocation getJSLocation(JSAbstractLocation nbJSLocation) {
        Session session = null;
        for (Session nextSession : DebuggerManager.getDebuggerManager().getSessions()) {
            NbJSDebugger debuggr = nextSession.lookupFirst(null, NbJSDebugger.class);
            if (debuggr == this) {
                session = nextSession;
                break;
            }
        }
        
        if (session != null) {
            NbJSToJSLocationMapper nbJSToJSLocationMapper = session.lookupFirst(null, NbJSToJSLocationMapper.class);
            if (nbJSToJSLocationMapper != null) {
                JSLocation jsLocation = null;
                if (nbJSLocation instanceof NbJSLocation) {
                    jsLocation = nbJSToJSLocationMapper.getJSLocation((NbJSLocation) nbJSLocation, null);
                }
                return jsLocation;
            }
        }
        return null;
    }

    // Windows
    public JSWindow[] getWindows() throws IllegalStateException {
        if (debugger != null) {
            return debugger.getWindows();
        }
        return JSWindow.EMPTY_ARRAY;
    }

    // Sources
    public JSSource[] getSources() {
        if (debugger != null) {
            return debugger.getSources();
        }
        return JSSource.EMPTY_ARRAY;
    }

    public FileObject getFileObjectForSource(JSSource source) {
        return getFileObjectForSource(source, false);
    }

    public FileObject getFileObjectForSource(JSSource source, boolean modifyActualURL) {
        JSToNbJSLocationMapper mapper = null;
        JSSource originalSource = source;

        if (lookup != null) {
            mapper = lookup.lookup(JSToNbJSLocationMapper.class);
        }

        if (ignoreQueryStrings) {
            URI originalURI = source.getLocation().getURI();
            source = JSFactory.createJSSource(getURIWithoutQuery(originalURI).toString());
        }

        JSLocation location = source.getLocation();

        if (mapper != null) {
            NbJSLocation foLocation = mapper.getNbJSLocation(location, lookup);
            if (foLocation != null && foLocation instanceof NbJSFileObjectLocation) {
                return ((NbJSFileObjectLocation) foLocation).getFileObject();
            }
        }

        if (ignoreQueryStrings && (modifyActualURL || !isURLLoaded(source))) {
            return getURLFileObjectForSource(originalSource, source);
        } else if (ignoreQueryStrings) {
            return sourceToURLFileObject(source);
        } else {
            return getURLFileObjectForSource(source);
        }
    }

    private boolean isURLLoaded(JSSource source) {
        URI srcURI = source.getLocation().getURI();
        try {
            return URLFileObjectFactory.findFileObject(contentProvider, srcURI.toURL()) != null;
        } catch (MalformedURLException ex) {
            return false;
        }
    }

    private URI getURIWithoutQuery(URI originalURI) {
            try {
                URI uriWithoutQuery = new URI(
                        originalURI.getScheme(),
                        originalURI.getUserInfo(),
                        originalURI.getHost(),
                        originalURI.getPort(),
                        originalURI.getPath(),
                        null,
                        originalURI.getFragment());
                return uriWithoutQuery;
            } catch (URISyntaxException ex) {
                Log.getLogger().log(Level.INFO, "Could not remove query string from URI: " + originalURI.toASCIIString());
                return originalURI;
            }
    }


    private URLFileObject sourceToURLFileObject(JSSource source) {
        URI srcURI = source.getLocation().getURI();
        try {
            return getURLFileObject(srcURI.toURL());
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    private FileObject getURLFileObjectForSource(JSSource actualSource, JSSource sourceWithoutQuery) {
        URLFileObject result = sourceToURLFileObject(sourceWithoutQuery);
        if (result != null) {
            try {
                result.setActualURL(actualSource.getLocation().getURI().toURL());
            } catch (MalformedURLException ex) {
                Log.getLogger().log(Level.INFO, "URI with query string could not be converted to URL: " +
                        actualSource.getLocation().getURI().toASCIIString());
            }
        }

        return result;
    }

    public FileObject getURLFileObjectForSource(JSSource source) {
        return getURLFileObjectForSource(source, false);
    }

    public FileObject getURLFileObjectForSource(JSSource source, boolean modifyActualURL) {
        if (ignoreQueryStrings) {
            URI originalURI = source.getLocation().getURI();
            JSSource sourceWithoutQuery = JSFactory.createJSSource(getURIWithoutQuery(originalURI).toString());
            if (modifyActualURL) {
                return getURLFileObjectForSource(source, sourceWithoutQuery);
            } else {
                return sourceToURLFileObject(sourceWithoutQuery);
            }
        } else {
            return sourceToURLFileObject(source);
        }
    }

    // Callstack
    public JSCallStackFrame[] getCallStackFrames() throws IllegalStateException {
        if (debugger != null) {
            return debugger.getCallStackFrames(); // TODO
        }
        return JSCallStackFrame.EMPTY_ARRAY;
    }

    public boolean isSelectedFrame(JSCallStackFrame selectedFrame) {
        return this.selectedFrame == selectedFrame;
    }

    public JSCallStackFrame getSelectedFrame() {
        return selectedFrame;
    }

    public boolean isSessionSuspended() {
        if (debugger != null) {
            return debugger.isSuspended();
        }
        return false;
    }

    // Breakpoints
    public void setBreakpoint(NbJSFileObjectBreakpoint breakpoint) {
    }

    public boolean isSuspendedWindow(JSWindow window) {
        return window.isSuspended();
    }

    public void selectFrame(JSCallStackFrame selectedFrame) {
        JSCallStackFrame oldSelectedFrame = this.selectedFrame;
        this.selectedFrame = selectedFrame;
        propertyChangeSupport.firePropertyChange(PROPERTY_SELECTED_FRAME,
                oldSelectedFrame,
                this.selectedFrame);
    }

    public NbJSToJSLocationMapper getNbJSToJSLocation() {
        if (lookup != null) {
            return lookup.lookup(NbJSToJSLocationMapper.class);
        }
        return null;
    }

    public JSToNbJSLocationMapper getJSToNbJSLocation() {
        if (lookup != null) {
            return lookup.lookup(JSToNbJSLocationMapper.class);
        }
        return null;
    }

    public void resume() {
        if (debugger != null) {
            debugger.resume();
        }
    }

    public void pause() {
        if (debugger != null) {
            debugger.pause();
            if (console != null) {
                console.getOut().println(NbBundle.getMessage(NbJSDebugger.class, "MSG_WILL_PAUSE"));
            }
        }
    }

    public void stepInto() {
        if (debugger != null) {
            debugger.stepInto();
        }
    }

    public void stepOver() {
        if (debugger != null) {
            debugger.stepOver();
        }
    }

    public void stepOut() {
        if (debugger != null) {
            debugger.stepOut();
        }
    }

    public void runToCursor() {
        JSURILocation location = null;
        if (debugger != null) {
            EditorContextDispatcher dispatcher = EditorContextDispatcher.getDefault();
            FileObject fileObject = dispatcher.getCurrentFile();
            int line = dispatcher.getCurrentLineNumber();
            if (fileObject instanceof URLFileObject) {
                try {
                    location = new JSURILocation(fileObject.getURL().toString(), line, 0);
                } catch (FileStateInvalidException ex) {
                    Log.getLogger().log(Level.INFO, ex.getLocalizedMessage(), ex);
                }
            } else {
                JSAbstractLocation abstractLocation = new NbJSFileObjectLocation(fileObject, line);
                location = (JSURILocation) getJSLocation(abstractLocation);
            }
            if (location != null) {
                debugger.runToCursor(location);
            }
        }
    }

    public boolean isRunningTo(JSLocation location) {
        return false;
    }

    public void finish(boolean terminate, Destructor destructor) {
        if (debugger != null) {
            debugger.finish(terminate);
        }

        // Now terminate the engine
        destructor.killEngine();
    }

    InputStream getInputStreamForURL(URL url) {
        if (debugger != null) {
            return debugger.getInputStreamForURL(url);
        }
        return null;
    }

    private URLFileObject getURLFileObject(URL url) {
        return URLFileObjectFactory.getFileObject(contentProvider, url);
    }

    public FileSystem getURLFileSystem() {
        return URLFileObjectFactory.getFileSystem(contentProvider);
    }

    /**
     * Get the URI for which the debugger was launched.
     * @return URI for the file.
     */
    public URI getURI() {
        return uri;
    }

    private void openEditorsForWindows() {
        JSWindow[] windows = getWindows();
        // Now open the editor for top level windows
        for (JSWindow window : windows) {
            String strURI = window.getURI();
            JSSource source = JSFactory.createJSSource(strURI);
            NbJSEditorUtil.openFileObject(getFileObjectForSource(source, true));
        }
    }
    
    private void refreshCacheForSources(JSSource[] sources) {
        if (sources == null || sources.length == 0) {
            return;
        }
        
        for (JSSource source : sources) {
            try {
                URI sourceURI = source.getLocation().getURI();
                URI uriWithoutQuery = ignoreQueryStrings ? getURIWithoutQuery(sourceURI) : null;

                URLFileObject urlFO = URLFileObjectFactory.findFileObject(contentProvider,
                        uriWithoutQuery != null ? uriWithoutQuery.toURL() : sourceURI.toURL());

                boolean invalidated = false;
                if (urlFO != null && uriWithoutQuery != null) {
                    URL lastActualURL = urlFO.getActualURL();
                    URL newActualURL = sourceURI.toURL();
                    if (!lastActualURL.toExternalForm().equals(newActualURL.toExternalForm())) {
                        urlFO.setActualURL(sourceURI.toURL());
                        invalidated = true;
                    }
                }
                
                if (urlFO != null && !invalidated) {
                    urlFO.invalidate();
                }
            } catch (MalformedURLException ex) {
                Log.getLogger().log(Level.FINE, "Could not convert URI to URL", ex);
            }
        }        
    }

    private static boolean displayReloadSessionDialog() {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(NbJSDebugger.class, "RELOAD_DEBUGGER_QUESTION"),
                NotifyDescriptor.OK_CANCEL_OPTION);
        Object result = DialogDisplayer.getDefault().notify(nd);
        return result == NotifyDescriptor.OK_OPTION;
    }
}

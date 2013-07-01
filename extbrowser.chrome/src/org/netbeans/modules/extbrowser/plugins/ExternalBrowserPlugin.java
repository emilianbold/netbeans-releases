/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.extbrowser.plugins;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.extbrowser.chrome.ChromeBrowserImpl;
import org.netbeans.modules.extbrowser.plugins.chrome.WebKitDebuggingTransport;
import org.netbeans.modules.netserver.api.WebSocketReadHandler;
import org.netbeans.modules.netserver.api.WebSocketServer;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.browser.api.ResizeOption;
import org.netbeans.modules.web.browser.api.ResizeOptions;
import org.netbeans.modules.web.browser.spi.ExternalModificationsSupport;
import org.netbeans.modules.web.webkit.debugging.api.TransportStateException;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.WebKitUIManager;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ProxyLookup;


/**
 * Support class running a WebSocket server for communication with browser plugins.
 */
public final class ExternalBrowserPlugin {
    /** ID of 'reload of save' feature. */
    static final String FEATURE_ROS = "RoS"; // NOI18N
    
    public static final String UTF_8 = "UTF-8";                    // NOI18N

    private static final int PORT = 8008;
    
    private static final Logger LOG = Logger.getLogger(
            ExternalBrowserPlugin.class.getCanonicalName());

    public static ExternalBrowserPlugin getInstance(){
        return INSTANCE;
    }
    
    private final List<MessageListener> listeners;

    private static final ExternalBrowserPlugin INSTANCE = new ExternalBrowserPlugin();
    
    private static final RequestProcessor RP = new RequestProcessor("ExternalBrowserPlugin", 5); // NOI18N

    @NbBundle.Messages({"# {0} - port", "ServerStartFailed=Internal WebSocket server failed to start "
            + "and communication with the external Chrome browser will not work. Check the IDE log "
            + "for more information. This is likely caused by multiple instances of NetBeans "
            + "running at the same time or some other application using port {0}"})
    private ExternalBrowserPlugin() {
        listeners = new CopyOnWriteArrayList<MessageListener>();
        try {
            server = new WebSocketServer(new InetSocketAddress("localhost", PORT), new BrowserPluginHandler()); // NOI18N
            server.start();

            Thread shutdown = new Thread(){
                @Override
                public void run() {
                    List<BrowserTabDescriptor> browserTabs = new ArrayList<BrowserTabDescriptor>(knownBrowserTabs);
                    for (BrowserTabDescriptor tab : browserTabs) {
                        tab.deinitialize();
                    }
                    server.stop();
                }
            };
            Runtime.getRuntime().addShutdownHook( shutdown);
        }
        catch (IOException e) {
            LOG.log( Level.INFO , null , e);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    Bundle.ServerStartFailed(""+PORT), NotifyDescriptor.Message.ERROR_MESSAGE));
        }
    }
    
    public boolean isServerRunning() {
        return server != null;
    }

    /**
     * Register that given URL was opened in external browser and browser
     * should confirm it by sending a message back to IDE. This handshake
     * will store ID if the browser tab and use it for all consequent external
     * browser requests.
     */
    public void register(URL tempURL, URL realUrl, ChromeBrowserImpl browserImpl) {
        awaitingBrowserResponse.put(urlToString(tempURL), new Pair(browserImpl, realUrl));
    }

    private String urlToString(URL url) {
        try {
            // try to 'normalize' the URL
            return url.toURI().toASCIIString().toLowerCase();
        } catch (URISyntaxException ex) {
            return url.toExternalForm();
        }
    }

    /**
     * Show URL in browser in given browser tab.
     */
    public void showURLInTab(final BrowserTabDescriptor tab, final URL url) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                tab.init();
                server.sendMessage(tab.keyForFeature(FEATURE_ROS), createReloadMessage(tab.tabID, url));
            }
        });
    }

    public void close(final BrowserTabDescriptor tab, final boolean closeTab) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                tab.deinitialize();
                if (closeTab) {
                    server.sendMessage(tab.keyForFeature(FEATURE_ROS), createCloseTabMessage(tab.tabID));
                }
            }
        });
    }
    
    public void attachWebKitDebugger(BrowserTabDescriptor tab) {
        server.sendMessage(tab.keyForFeature(FEATURE_ROS), createAttachDebuggerMessage(tab.tabID));
    }

    public void detachWebKitDebugger(BrowserTabDescriptor tab) {
        if (tab != null) {
            server.sendMessage(tab.keyForFeature(FEATURE_ROS), createDetachDebuggerMessage(tab.tabID));
        }
    }

    public void sendWebKitDebuggerCommand(BrowserTabDescriptor tab, JSONObject command) {
        server.sendMessage(tab.keyForFeature(FEATURE_ROS), createDebuggerCommandMessage(tab.tabID, command));
    }
    
    public void addMessageListener(MessageListener listener){
        listeners.add(listener);
    }
    
    public void removeMessageListener(MessageListener listener ){
        listeners.remove(listener);
    }

    private void removeKey( SelectionKey key ) {
        for(Iterator<BrowserTabDescriptor> iterator = knownBrowserTabs.iterator() ; iterator.hasNext() ; ) {
            BrowserTabDescriptor browserTab = iterator.next();
            if (key.equals(browserTab.keyForFeature(FEATURE_ROS))) {
                browserTab.deinitialize();
                browserTab.browserImpl.wasClosed();
            }
            browserTab.unregisterKey(key);
            if (!browserTab.isAnyKeyRegistered()) {
                // SelectionKey of the last feature (that was interested in this tab)
                // was removed => we can forget this tab.
                iterator.remove();
            }
        }
    }

    /**
     * Notifies {@code MessageDispatcher}(s) that correspond to the given
     * {@code SelectionKey} about a new message.
     *
     * @param message message to dispatch.
     * @param key origin of the message.
     */
    private void notifyDispatchers(final String message, SelectionKey key) {
        for (BrowserTabDescriptor browserTab : knownBrowserTabs) {
            String featureId = browserTab.featureForKey(key);
            if (featureId != null) {
                Lookup lookup = browserTab.browserImpl.getLookup();
                final MessageDispatcherImpl dispatcher = lookup.lookup(MessageDispatcherImpl.class);
                if (dispatcher != null) {
                    dispatcher.dispatchMessage(featureId, message);
                }
            }
        }
    }
    
    private void fireMessageEvent( Message msg ) {
        for (MessageListener listener : listeners) {
            listener.messageReceived(msg);
        }
    }

    /**
     * Sends a message to the specified feature of the specified web-browser pane.
     *
     * @param message message to deliver.
     * @param impl web-pane where the message should be sent.
     * @param featureId ID of the feature the message is related to.
     */
    public void sendMessage(String message, ChromeBrowserImpl impl, String featureId) {
        for (BrowserTabDescriptor browserTab : knownBrowserTabs) {
            if (browserTab.browserImpl == impl) {
                SelectionKey key = browserTab.keyForFeature(featureId);
                if (key != null) {
                    server.sendMessage(key, message);
                }
            }
        }
    }
    
    private void closeOtherDebuggingSessionsWithPageInspector(int tabId) {
        for (BrowserTabDescriptor browserTab : knownBrowserTabs) {
            if ( tabId != browserTab.tabID && browserTab.isPageInspectorActive()) {
                close(browserTab, false);
            }
        }
    }

    class BrowserPluginHandler implements WebSocketReadHandler {
        /** Name of the attribute of the INIT message that holds the version information. */
        private static final String VERSION = "version"; // NOI18N
        private static final String URL = "url";        // NOI18N

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.common.websocket.WebSocketReadHandler#read(java.nio.channels.SelectionKey, byte[], java.lang.Integer)
         */
        @Override
        public void read( SelectionKey key, byte[] data, Integer dataType ) {
            if ( dataType != null && dataType != 1 ){
                return;
            }
            String message = new String( data , Charset.forName( UTF_8));
            Message msg = Message.parse(message);
            if (msg == null || (msg.getType() == null)) {
                notifyDispatchers(message, key);
                return;
            }
            fireMessageEvent(msg);
            Message.MessageType type = msg.getType();
            switch (type) {
                case INIT:
                    handleInit(msg, key );
                    break;
                case CLOSE:
                    handleClose( msg , key );
                    break;
                case URLCHANGE:
                    handleURLChange(msg, key);
                    break;
                case INSPECT:
                    handleInspect(msg, key);
                    break;
                case ATTACH_DEBUGGER:
                    break;
                case DETACH_DEBUGGER:
                    break;
                case DEBUGGER_DETACHED:
                    handleDebuggerDetached(msg);
                    break;
                case DEBUGGER_COMMAND_RESPONSE:
                    handleDebuggerResponse( msg , key );
                    break;
                case LOAD_RESIZE_OPTIONS:
                    handleLoadResizeOptions(key);
                    break;
                case SAVE_RESIZE_OPTIONS:
                    handleSaveResizeOptions(msg.getValue());
                    break;
                case RESOURCE_CHANGED:
                    handleResourceChanged(msg.getValue());
                    break;
                case READY:
                    break;
                default:
                    assert false : "Unknown message type: " + type;
            }

        }

        private void handleInit( Message message , SelectionKey key ){
            String version = (String)message.getValue().get(VERSION);
            String url = (String)message.getValue().get(URL);
            int tabId = message.getTabId();
            if (version == null || url == null || tabId == -1) {
                return;
            }
            final Pair p;
            if (isSupportedVersion(version)) {
                p = getAwaitingPair(url);
            } else {
                p = null;
            }
            ChromeBrowserImpl browserImpl = p != null ? p.impl : null;
            if (browserImpl == null) {
                Map map = new HashMap();
                map.put( Message.TAB_ID, tabId );
                map.put("status","notaccepted");       // NOI18N
                map.put("version", getNetBeansVersion()); // NOI18N
                Message msg = new Message( Message.MessageType.INIT , map );
                server.sendMessage(key, msg.toStringValue());
            } else  {
                final BrowserTabDescriptor tab = new BrowserTabDescriptor(tabId, browserImpl);
                tab.registerKeyForFeature(FEATURE_ROS, key);
                browserImpl.setBrowserTabDescriptor(tab);
                knownBrowserTabs.add(tab);
                Map map = new HashMap();
                map.put( Message.TAB_ID, tabId );
                map.put("status","accepted");       // NOI18N
                map.put("version", getNetBeansVersion()); // NOI18N
                Message msg = new Message( Message.MessageType.INIT , map );
                server.sendMessage(key, msg.toStringValue());
                
                // update temp URL with real one:
                assert p.realURL != null;
                showURLInTab(tab, p.realURL);
            }
        }

        /**
         * Determines whether the specified version of the INIT message/protocol
         * is supported or not.
         * 
         * @param version version to check.
         * @return {@code true} when the version is supported,
         * returns {@code false} otherwise.
         */
        private boolean isSupportedVersion(String version) {
            return version.startsWith("1."); // NOI18N
        }

        /**
         * Returns the version of NetBeans (sent by IDE to browser extension).
         * 
         * @return version of NetBeans.
         */
        private String getNetBeansVersion() {
            return "7.3"; // NOI18N
        }

        private void handleDebuggerDetached(Message message) {
            int tabId = message.getTabId();
            if ( tabId == -1 ){
                return;
            }
            deinitializeTab(tabId, false);
        }
        
        private Pair getAwaitingPair(String url) {
            if (url.startsWith("chrome")) {
                // ignore internal chrome URLs:
                return null;
            }
            URL u = null;
            try {
                u = new URL(url);
            } catch (MalformedURLException ex) {
                LOG.log(Level.WARNING, "cannot parse URL: {0}", url); // NOI18N
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "processing URL: {0}", (u == null) ? null : urlToString(u)); // NOI18N
                for (String awaiting : awaitingBrowserResponse.keySet()) {
                    LOG.log(Level.FINE, "awaiting URL: {0}", awaiting);  // NOI18N
                }
            }
            Pair pair = (u == null) ? null : awaitingBrowserResponse.remove(urlToString(u));
            ChromeBrowserImpl browserImpl = pair != null ? pair.impl : null;

            // XXX: workaround: when Web Project is run it is started as "http:/localhost/aa" but browser URL is
            // "http:/localhost/aa/"
            if (browserImpl == null && url.endsWith("/")) { // NOI18N
                try {
                    u = new URL(url.substring(0, url.length()-1));
                    pair = awaitingBrowserResponse.remove(urlToString(u));
                    browserImpl = pair != null ? pair.impl : null;
                } catch (MalformedURLException ex) {
                    LOG.log(Level.WARNING, "cannot parse URL: {0}", url);   // NOI18N
                }
            }
            // XXX: on Mac, file URLs are open with localhost as the host instead of ""
            if (browserImpl == null && (u != null) && "file".equals(u.getProtocol()) // NOI18N
                    && "localhost".equals(u.getHost()))                 // NOI18N
            {
                try {
                    u = new URL(u.getProtocol(), "", u.getPort(), u.getFile()); // NOI18N
                    pair = awaitingBrowserResponse.remove(urlToString(u));
                } catch (MalformedURLException ex) {
                    LOG.log(Level.WARNING, "cannot parse URL: {0}", url);// NOI18N
                }
            }
            return pair;
        }

        private void handleClose( Message message, SelectionKey key  ){
            int tabId = message.getTabId();
            if ( tabId == -1 ){
                return;
            }
            deinitializeTab(tabId, true);
        }
        
        private boolean deinitializeTab(int tabId, boolean close) {
            for(Iterator<BrowserTabDescriptor> iterator = knownBrowserTabs.iterator() ; iterator.hasNext() ; ) {
                BrowserTabDescriptor browserTab = iterator.next();
                if ( tabId == browserTab.tabID ) {
                    browserTab.deinitialize();
                    browserTab.disableReInitialization();
                    if (close) {
                        iterator.remove();
                        browserTab.browserImpl.wasClosed();
                    }
                    return true;
                }
            }
            return false;
        }

        private void handleDebuggerResponse( Message message, SelectionKey key  ){
            int tabId = message.getTabId();
            JSONObject response = (JSONObject)message.getValue().get("response" );
            assert tabId != -1;
            assert response != null;
            if ( tabId == -1 || response == null) {
                return;
            }
            for(BrowserTabDescriptor browserTab : knownBrowserTabs) {
                if (tabId == browserTab.tabID && browserTab.getCallback() != null) {
                    Response resp;
                    String error = (String)response.get("error"); // NOI18N
                    resp = new Response(response, (error == null) ? null : new TransportStateException(error));
                    browserTab.getCallback().handleResponse(resp);
                }
            }
        }

        private void handleURLChange( Message message, SelectionKey key  ){
            int tabId = message.getTabId();
            String url = (String)message.getValue().get(URL);
            if ( tabId == -1 ){
                return;
            }
            for (BrowserTabDescriptor browserTab : knownBrowserTabs) {
                if ( tabId == browserTab.tabID ) {
                    browserTab.browserImpl.urlHasChanged(url);
                    return;
                }
            }
        }

        /**
         * Handles a request for web-page inspection.
         *
         * @param message initial message of the inspection.
         * @param key origin of the message.
         */
        private void handleInspect(Message message, SelectionKey key) {
            final PageInspector inspector = PageInspector.getDefault();
            if (inspector == null) {
                LOG.log(Level.INFO, "No PageInspector found: ignoring the request for page inspection!"); // NOI18N
            } else {
                int tabId = message.getTabId();

                // Find if the tab is known to RoS already
                BrowserTabDescriptor browserTab = null;
                for (BrowserTabDescriptor descriptor : knownBrowserTabs) {
                    if (descriptor.tabID == tabId) {
                        browserTab = descriptor;
                    }
                }
                if (browserTab == null) {
                    // Tab not opened from the IDE => using a dummy ExtBrowserImpl
                    ChromeBrowserImpl impl = new ChromeBrowserImpl(null, true) {
                        @Override
                        public void setURL(URL url) {
                            throw new UnsupportedOperationException();
                        }
                        @Override
                        public void close(boolean closeTab) {
                        }
                    };
                    browserTab = new BrowserTabDescriptor(tabId, impl);
                    knownBrowserTabs.add(browserTab);
                }
                browserTab.registerKeyForFeature(PageInspector.MESSAGE_DISPATCHER_FEATURE_ID, key);
                final Lookup context = browserTab.browserImpl.getLookup();
                final Lookup projectContext = browserTab.browserImpl.getProjectContext();
                RemoteScriptExecutor executor = context.lookup(RemoteScriptExecutor.class);
                if (executor != null) {
                    executor.activate();
                }
                // Do not block WebSocket thread
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        inspector.inspectPage(new ProxyLookup(context, projectContext));
                    }
                });
            }
        }

        private void handleLoadResizeOptions(SelectionKey key) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("resizeOptions", createLoadResizeOptionsMessage(ResizeOptions.getDefault().loadAll())); // NOI18N
            Message msg = new Message(Message.MessageType.LOAD_RESIZE_OPTIONS, map);
            server.sendMessage(key, msg.toStringValue());
        }

        @SuppressWarnings("unchecked")
        private String createLoadResizeOptionsMessage(List<ResizeOption> resizeOptions) {
            JSONArray result = new JSONArray();
            for (ResizeOption resizeOption : resizeOptions) {
                result.add(mapResizeOption(resizeOption));
            }
            return result.toJSONString();
        }

        @SuppressWarnings("unchecked")
        private JSONObject mapResizeOption(ResizeOption resizeOption) {
            JSONObject mapped = new JSONObject();
            mapped.put("type", JSONObject.escape(resizeOption.getType().name())); // NOI18N
            mapped.put("displayName", JSONObject.escape(resizeOption.getDisplayName())); // NOI18N
            mapped.put("width", resizeOption.getWidth()); // NOI18N
            mapped.put("height", resizeOption.getHeight()); // NOI18N
            mapped.put("showInToolbar", resizeOption.isShowInToolbar()); // NOI18N
            mapped.put("isDefault", resizeOption.isDefault()); // NOI18N
            return mapped;
        }

        private void handleSaveResizeOptions(JSONObject value) {
            JSONArray options = (JSONArray) value.get("resizeOptions"); // NOI18N
            List<ResizeOption> resizeOptions = new ArrayList<ResizeOption>(options.size());
            for (Object item : options) {
                JSONObject option = (JSONObject) item;
                resizeOptions.add(ResizeOption.create(
                        ResizeOption.Type.valueOf(String.valueOf(option.get("type"))), // NOI18N
                        String.valueOf(option.get("displayName")), // NOI18N
                        Integer.valueOf(String.valueOf(option.get("width"))), // NOI18N
                        Integer.valueOf(String.valueOf(option.get("height"))), // NOI18N
                        Boolean.valueOf(String.valueOf(option.get("showInToolbar"))), // NOI18N
                        Boolean.valueOf(String.valueOf(option.get("isDefault"))))); // NOI18N
            }
            ResizeOptions.getDefault().saveAll(resizeOptions);
        }

        @Override
        public void accepted(SelectionKey key) {
        }

        @Override
        public void closed(SelectionKey key) {
            removeKey( key );
        }

        private void handleResourceChanged(JSONObject value) {
            final String content = String.valueOf(value.get("content"));
            JSONObject resource = (JSONObject)value.get("resource");
            final String url = String.valueOf(resource.get("url"));
            final String type = String.valueOf(resource.get("type"));
            RP.post(new Runnable() {
                @Override
                public void run() {
                    ExternalModificationsSupport.handle(url, type, content);
                }
            });
        }

    }

    private String createReloadMessage(int tabId, URL newURL) {
        Map params = new HashMap();
        params.put( Message.TAB_ID, tabId );
        if (newURL != null) {
            try {
                String u = reformatFileURL(newURL.toURI().toString());
                params.put( "url", u ); // NOI18N
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        Message msg = new Message( Message.MessageType.RELOAD, params);
        return msg.toStringValue();
    }

    // changes "file:/some" to "file:///some"
    private static String reformatFileURL(String url) {
        if (!url.startsWith("file:")) {
            return url;
        }
        url = url.substring(5);
        while (url.length() > 0 && url.startsWith("/")) {
            url = url.substring(1);
        }
        return "file:///"+url;
    }
    
    private String createCloseTabMessage(int tabId) {
        Map params = new HashMap();
        params.put( Message.TAB_ID, tabId );
        Message msg = new Message( Message.MessageType.CLOSE, params);
        return msg.toStringValue();
    }

    private String createAttachDebuggerMessage(int tabId) {
        Map params = new HashMap();
        params.put( Message.TAB_ID, tabId );
        Message msg = new Message( Message.MessageType.ATTACH_DEBUGGER, params);
        return msg.toStringValue();
    }

    private String createDetachDebuggerMessage(int tabId) {
        Map params = new HashMap();
        params.put( Message.TAB_ID, tabId );
        Message msg = new Message( Message.MessageType.DETACH_DEBUGGER, params);
        return msg.toStringValue();
    }

    private String createDebuggerCommandMessage(int tabId, JSONObject params2) {
        JSONObject data = new JSONObject();
        data.put(Message.TAB_ID, tabId );
        data.put("command", params2);
        Message msg = new Message( Message.MessageType.DEBUGGER_COMMAND, data);
        return msg.toStringValue();
    }

    private WebSocketServer server;

    private final Map<String,Pair> awaitingBrowserResponse = new HashMap<String,Pair>();

    private static class Pair {
        ChromeBrowserImpl impl;
        URL realURL;

        public Pair(ChromeBrowserImpl impl, URL realURL) {
            this.impl = impl;
            this.realURL = realURL;
        }

    }

    private List<BrowserTabDescriptor> knownBrowserTabs = new ArrayList<BrowserTabDescriptor>();

    /**
     * Descriptor of tab opened in the external browser.
     */
    public static class BrowserTabDescriptor {
        /** Maps IDs of features (related to this tab) to their corresponding sockets. */
        private final Map<String,SelectionKey> keyMap = new HashMap<String,SelectionKey>();
        private int tabID;
        private ChromeBrowserImpl browserImpl;
        private ResponseCallback callback;
        private boolean initialized;
        private boolean doNotInitialize;
        private Session session;
        private Lookup consoleLogger;
        private Lookup networkMonitor;

        public BrowserTabDescriptor(int tabID, ChromeBrowserImpl browserImpl) {
            this.tabID = tabID;
            this.browserImpl = browserImpl;
        }

        /**
         * Registers the given selection key for the specified feature.
         *
         * @param featureId ID of the feature.
         * @param key selection key for the feature.
         */
        synchronized void registerKeyForFeature(String featureId, SelectionKey key) {
            keyMap.put(featureId, key);
        }

        /**
         * Returns selection key registered for the specified feature.
         *
         * @param featureId ID of the feature.
         * @return selection key registered for the specified feature
         * or {@code null} if there is no such feature.
         */
        synchronized SelectionKey keyForFeature(String featureId) {
            return keyMap.get(featureId);
        }

        /**
         * Unregisters the specified key.
         *
         * @param key selection key to unregister.
         */
        synchronized void unregisterKey(SelectionKey key) {
            keyMap.values().removeAll(Collections.singleton(key));
        }

        /**
         * Determines whether any key/feature is registerd for this tab.
         *
         * @return {@code true} if any key/feature is registered for this tab,
         * returns {@code false} otherwise.
         */
        synchronized boolean isAnyKeyRegistered() {
            return !keyMap.isEmpty();
        }

        /**
         * Returns ID of the feature for which the specified selection key
         * is registered.
         *
         * @param key selection key of the feature we are interested in.
         * @return ID of the feature for which the specified selection key
         * is registered or {@code null} when there is no such feature.
         */
        synchronized String featureForKey(SelectionKey key) {
            String featureId = null;
            for (Map.Entry<String,SelectionKey> entry : keyMap.entrySet()) {
                if (entry.getValue() == key) {
                    featureId = entry.getKey();
                }
            }
            return featureId;
        }

        public void setCallback(ResponseCallback callback) {
            this.callback = callback;
        }

        private ResponseCallback getCallback() {
            return callback;
        }

        private void init() {
            if (initialized || !browserImpl.hasEnhancedMode() || doNotInitialize ||
                    browserImpl.getBrowserFeatures() == null ||
                    !browserImpl.getBrowserFeatures().isNetBeansIntegrationEnabled()) {
                return;
            }
            initialized = true;
            
            // perform session closing before creating a new one:
            PageInspector inspector = PageInspector.getDefault();
            if (inspector != null && browserImpl.getBrowserFeatures().isPageInspectorEnabled()) {
                // #219241 - "Web inspection is broken when switching 2 projects with different configuration"
                // a solution is to close previous debugging sessions:
                ExternalBrowserPlugin.getInstance().closeOtherDebuggingSessionsWithPageInspector(tabID);
            }

            // lookup which contains Project instance if URL being opened is from a project:
            Lookup projectContext = browserImpl.getProjectContext();
            
            WebKitDebuggingTransport transport = browserImpl.getLookup().lookup(WebKitDebuggingTransport.class);
            WebKitDebugging webkitDebugger = browserImpl.getLookup().lookup(WebKitDebugging.class);
            if (webkitDebugger == null || projectContext == null) {
                return;
            }
            transport.attach();
            if (browserImpl.getBrowserFeatures().isLiveHTMLEnabled()) {
                webkitDebugger.getDebugger().enableDebuggerInLiveHTMLMode();
            } else {
                webkitDebugger.getDebugger().enable();
            }
            if (browserImpl.getBrowserFeatures().isJsDebuggerEnabled()) {
                session = WebKitUIManager.getDefault().createDebuggingSession(webkitDebugger, projectContext);
            }
            if (browserImpl.getBrowserFeatures().isConsoleLoggerEnabled()) {
                consoleLogger = WebKitUIManager.getDefault().createBrowserConsoleLogger(webkitDebugger, projectContext);
            }
            if (browserImpl.getBrowserFeatures().isNetworkMonitorEnabled()) {
                networkMonitor = WebKitUIManager.getDefault().createNetworkMonitor(webkitDebugger, projectContext);
            }

            if (inspector != null && browserImpl.getBrowserFeatures().isPageInspectorEnabled()) {
                inspector.inspectPage(new ProxyLookup(browserImpl.getLookup(), browserImpl.getProjectContext()));
            }
        }

        public boolean isInitialized() {
            return initialized;
        }

        private void deinitialize() {
            if (!initialized || !browserImpl.hasEnhancedMode()) {
                return;
            }
            initialized = false;
            WebKitDebuggingTransport transport = browserImpl.getLookup().lookup(WebKitDebuggingTransport.class);
            WebKitDebugging webkitDebugger = browserImpl.getLookup().lookup(WebKitDebugging.class);
            if (webkitDebugger == null) {
                return;
            }
            if (session != null) {
                WebKitUIManager.getDefault().stopDebuggingSession(session);
            }
            session = null;
            if (consoleLogger != null) {
                WebKitUIManager.getDefault().stopBrowserConsoleLogger(consoleLogger);
            }
            consoleLogger = null;
            if (networkMonitor != null) {
                WebKitUIManager.getDefault().stopNetworkMonitor(networkMonitor);
            }
            networkMonitor = null;
            MessageDispatcherImpl dispatcher = browserImpl.getLookup().lookup(MessageDispatcherImpl.class);
            if (dispatcher != null) {
                dispatcher.dispatchMessage(PageInspector.MESSAGE_DISPATCHER_FEATURE_ID, null);
            }
            if (webkitDebugger.getDebugger().isEnabled()) {
                webkitDebugger.getDebugger().disable();
            }
            webkitDebugger.reset();
            transport.detach();
        }

        /**
         * Do not attempt to re-attach when the debugging was canceled
         * by the user explicitly.
         */
        private void disableReInitialization() {
            doNotInitialize = true;
        }

        public void reEnableReInitialization() {
            doNotInitialize = false;
        }
        
        public boolean isPageInspectorActive() {
            return PageInspector.getDefault() != null &&
                browserImpl.getBrowserFeatures() != null &&
                browserImpl.getBrowserFeatures().isPageInspectorEnabled();
        }

    }
    
}

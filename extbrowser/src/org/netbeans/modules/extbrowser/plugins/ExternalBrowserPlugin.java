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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.extbrowser.ExtBrowserImpl;
import org.netbeans.modules.extbrowser.ExtWebBrowser;
import org.netbeans.modules.netserver.websocket.WebSocketReadHandler;
import org.netbeans.modules.netserver.websocket.WebSocketServer;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;


/**
 * Support class running a WebSocket server for communication with browser plugins.
 */
public final class ExternalBrowserPlugin {
    /** ID of 'reload of save' feature. */
    private static final String FEATURE_ROS = "RoS"; // NOI18N
    
    private static final int PORT = 8008;
    
    private static final Logger LOG = Logger.getLogger( 
            ExternalBrowserPlugin.class.getCanonicalName());

    public static ExternalBrowserPlugin getInstance(){
        return INSTANCE;
    }
    
    private static final ExternalBrowserPlugin INSTANCE = new ExternalBrowserPlugin();
    
    private ExternalBrowserPlugin() {
        try {
            server = new WebSocketServer(new InetSocketAddress(PORT)){
              
                @Override
                public void close(SelectionKey key) throws IOException {
                    removeKey( key );
                    super.close(key);
                }
            };
            server.setWebSocketReadHandler( new BrowserPluginHandler() );
            new Thread( server ).start();
            
            Thread shutdown = new Thread(){
                @Override
                public void run() {
                    server.stop();
                }
            };
            Runtime.getRuntime().addShutdownHook( shutdown);
        }
        catch (IOException e) {
            LOG.log( Level.WARNING , null , e);
        }
    }
    
    /**
     * Register that given URL was opened in external browser and browser
     * should confirm it by sending a message back to IDE. This handshake
     * will store ID if the browser tab and use it for all consequent external 
     * browser requests.
     */
    public void register(URL url, ExtBrowserImpl browserImpl) {
        awaitingBrowserResponse.put(url.toExternalForm(), browserImpl);
    }

    /**
     * Show URL in browser in given browser tab.
     */
    public void showURLInTab(BrowserTabDescriptor tab, URL url) {
        server.sendMessage(tab.keyForFeature(FEATURE_ROS), createReloadMessage(tab.tabID, url));
    }

    private void removeKey( SelectionKey key ) {
        notifyDispatchers(null, key); // Notify MessageDispatcher(s) about the closed socket
        for(Iterator<BrowserTabDescriptor> iterator = knownBrowserTabs.iterator() ; iterator.hasNext() ; ) {
            BrowserTabDescriptor browserTab = iterator.next();
            if (key.equals(browserTab.keyForFeature(FEATURE_ROS))) {
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

    /**
     * Sends a message to the specified feature of the specified web-browser pane.
     * 
     * @param message message to deliver.
     * @param impl web-pane where the message should be sent.
     * @param featureId ID of the feature the message is related to.
     */
    public void sendMessage(String message, ExtBrowserImpl impl, String featureId) {
        for (BrowserTabDescriptor browserTab : knownBrowserTabs) {
            if (browserTab.browserImpl == impl) {
                SelectionKey key = browserTab.keyForFeature(featureId);
                if (key != null) {
                    server.sendMessage(key, message);
                }
            }
        }
    }

    class BrowserPluginHandler implements WebSocketReadHandler {

        private static final String URL = "url";        // NOI18N

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.common.websocket.WebSocketReadHandler#read(java.nio.channels.SelectionKey, byte[], java.lang.Integer)
         */
        @Override
        public void read( SelectionKey key, byte[] data, Integer dataType ) {
            if ( dataType != null && dataType != 1 ){
                return;
            }
            String message = new String( data , Charset.forName( WebSocketServer.UTF_8));
            Message msg = Message.parse(message);
            if (msg == null || (msg.getType() == null)) {
                notifyDispatchers(message, key);
                return;
            }
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
                default:
                    assert false;
            }
            
        }
        
        private void handleInit( Message message , SelectionKey key ){
            String url = message.getValue(URL);
            String tabId = message.getValue(Message.TAB_ID);
            if ( url == null || tabId == null ){
                return;
            }
            URL u = null;
            try {
                u = new URL(url);
            } catch (MalformedURLException ex) {
                LOG.log(Level.WARNING, "cannot parse URL: {0}", url); // NOI18N
            }
            ExtBrowserImpl browserImpl = (u == null) ? null : awaitingBrowserResponse.remove(u.toExternalForm());
            
            // XXX: workaround: when Web Project is run it is started as "http:/localhost/aa" but browser URL is
            // "http:/localhost/aa/"
            if (browserImpl == null && url.endsWith("/")) { // NOI18N
                try {
                    u = new URL(url.substring(0, url.length()-1));
                    browserImpl = awaitingBrowserResponse.remove(u.toExternalForm());
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
                    browserImpl = awaitingBrowserResponse.remove(u.toExternalForm());
                } catch (MalformedURLException ex) {
                    LOG.log(Level.WARNING, "cannot parse URL: {0}", url);// NOI18N
                }
            }
            if (browserImpl == null) {
                Map<String,String> map = new HashMap<String, String>();
                map.put( Message.TAB_ID, tabId );
                map.put("status","notaccepted");       // NOI18N
                Message msg = new Message( Message.MessageType.INIT , map );
                server.sendMessage(key, msg.toString());
            } else  {
                BrowserTabDescriptor tab = new BrowserTabDescriptor(tabId, browserImpl);
                tab.registerKeyForFeature(FEATURE_ROS, key);
                browserImpl.setBrowserTabDescriptor(tab);
                knownBrowserTabs.add(tab);
                Map<String,String> map = new HashMap<String, String>();
                map.put( Message.TAB_ID, tabId );
                map.put("status","accepted");       // NOI18N
                Message msg = new Message( Message.MessageType.INIT , map );
                server.sendMessage(key, msg.toString());
            }
        }
        
        private void handleClose( Message message, SelectionKey key  ){
            String tabId = message.getValue( Message.TAB_ID );
            if ( tabId == null ){
                return;
            }
            for(Iterator<BrowserTabDescriptor> iterator = knownBrowserTabs.iterator() ; iterator.hasNext() ; ) {
                BrowserTabDescriptor browserTab = iterator.next();
                if ( tabId.equals( browserTab.tabID )) {
                    iterator.remove();
                    browserTab.browserImpl.wasClosed();
                    return;
                }
            }
        }

        private void handleURLChange( Message message, SelectionKey key  ){
            String tabId = message.getValue( Message.TAB_ID );
            if ( tabId == null ){
                return;
            }
            for(Iterator<BrowserTabDescriptor> iterator = knownBrowserTabs.iterator() ; iterator.hasNext() ; ) {
                BrowserTabDescriptor browserTab = iterator.next();
                if ( tabId.equals( browserTab.tabID )) {
                    browserTab.browserImpl.urlHasChanged();
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
                String tabId = message.getValue(Message.TAB_ID);
                
                // Find if the tab is known to RoS already
                BrowserTabDescriptor browserTab = null;
                for (BrowserTabDescriptor descriptor : knownBrowserTabs) {
                    if (descriptor.tabID.equals(tabId)) {
                        browserTab = descriptor;
                    }
                }
                if (browserTab == null) {
                    // Tab not opened from the IDE => using a dummy ExtBrowserImpl
                    ExtBrowserImpl impl = new ExtBrowserImpl() {
                        { extBrowserFactory = new ExtWebBrowser(); }
                        @Override
                        protected void loadURLInBrowser(URL url) {
                            throw new UnsupportedOperationException();
                        }
                    };
                    browserTab = new BrowserTabDescriptor(tabId, impl);
                    knownBrowserTabs.add(browserTab);
                }
                browserTab.registerKeyForFeature(PageInspector.MESSAGE_DISPATCHER_FEATURE_ID, key);
                final Lookup context = browserTab.browserImpl.getLookup();
                RemoteScriptExecutor executor = context.lookup(RemoteScriptExecutor.class);
                if (executor != null) {
                    executor.activate();
                }
                // Do not block WebSocket thread
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        inspector.inspectPage(context);
                    }
                });
            }
        }

        @Override
        public void accepted(SelectionKey key) {
        }

        @Override
        public void closed(SelectionKey key) {
        }
        
    }
    
    private String createReloadMessage(String tabId, URL newURL) {
        Map<String, String> params = new HashMap<String, String>();
        params.put( Message.TAB_ID, tabId );
        if (newURL != null) {
            try {
                params.put( "url", newURL.toURI().toString() ); // NOI18N
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        Message msg = new Message( Message.MessageType.RELOAD, params);
        return msg.toString();
    }
    
    private WebSocketServer server;

    private Map<String,ExtBrowserImpl> awaitingBrowserResponse = new HashMap<String,ExtBrowserImpl>();
    
    private List<BrowserTabDescriptor> knownBrowserTabs = new ArrayList<BrowserTabDescriptor>();
    
    /**
     * Descriptor of tab opened in the external browser.
     */
    public static class BrowserTabDescriptor {
        /** Maps IDs of features (related to this tab) to their correponding sockets. */
        private Map<String,SelectionKey> keyMap = new HashMap<String,SelectionKey>();
        private String tabID;
        private ExtBrowserImpl browserImpl;

        public BrowserTabDescriptor(String tabID, ExtBrowserImpl browserImpl) {
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
        
    }
    
}

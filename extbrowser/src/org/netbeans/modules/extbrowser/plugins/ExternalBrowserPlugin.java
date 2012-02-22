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
import org.netbeans.modules.web.common.websocket.WebSocketReadHandler;
import org.netbeans.modules.web.common.websocket.WebSocketServer;
import org.openide.util.Exceptions;


/**
 * Support class running a WebSocket server for communication with browser plugins.
 */
public final class ExternalBrowserPlugin {
    
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
        awatingBrowserResponse.put(url, browserImpl);
    }

    /**
     * Show URL in browser in given browser tab.
     */
    public void showURLInTab(BrowserTabDescriptor tab, URL url) {
        server.sendMessage(tab.key, createReloadMessage(tab.tabID, url));

    }

    private void removeKey( SelectionKey key ) {
        for(Iterator<BrowserTabDescriptor> iterator = knownBrowserTabs.iterator() ; iterator.hasNext() ; ) {
            BrowserTabDescriptor browserTab = iterator.next();
            if ( key.equals( browserTab.key )) {
                iterator.remove();
                browserTab.browserImpl.wasClosed();
            }
        }
    }
    
    /**
     * Just an example/placeholder.
     */
    public Object getDOM(BrowserTabDescriptor browserTab) {
        if (browserTab != null && browserTab.key != null ){
            server.sendMessage(browserTab.key, "a message to retrieve DOM description for "+ browserTab.tabID);
            // wait for response and return it
            return new Object(/* data from response*/);
        }
        return null;
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
            if ( msg == null ){
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
                LOG.log(Level.WARNING, "cannot parse URL: "+url);
            }
            ExtBrowserImpl browserImpl = awatingBrowserResponse.remove(u);
            
            // XXX: workaround: when Web Project is run it is started as "http:/localhost/aa" but browser URL is
            // "http:/localhost/aa/"
            if (browserImpl == null && url.endsWith("/")) {
                try {
                    u = new URL(url.substring(0, url.length()-1));
                    browserImpl = awatingBrowserResponse.remove(u);
                } catch (MalformedURLException ex) {
                    LOG.log(Level.WARNING, "cannot parse URL: "+url);
                }
            }
            if (browserImpl == null) {
                Map<String,String> map = new HashMap<String, String>();
                map.put( Message.TAB_ID, tabId );
                map.put("status","notaccepted");       // NOI18N
                Message msg = new Message( Message.MessageType.INIT , map );
                server.sendMessage(key, msg.toString());
            } else  {
                BrowserTabDescriptor tab = new BrowserTabDescriptor(key, tabId, browserImpl);
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
                params.put( "url", newURL.toURI().toString() );
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        Message msg = new Message( Message.MessageType.RELOAD, params);
        return msg.toString();
    }
    
    private WebSocketServer server;

    private Map<URL,ExtBrowserImpl> awatingBrowserResponse = new HashMap<URL,ExtBrowserImpl>();
    
    private List<BrowserTabDescriptor> knownBrowserTabs = new ArrayList<BrowserTabDescriptor>();
    
    /**
     * Descriptor of tab opened in the external browser.
     */
    public static class BrowserTabDescriptor {
        private SelectionKey key;
        private String tabID;
        private ExtBrowserImpl browserImpl;

        public BrowserTabDescriptor(SelectionKey key, String tabID, ExtBrowserImpl browserImpl) {
            this.key = key;
            this.tabID = tabID;
            this.browserImpl = browserImpl;
        }
    }
    
}

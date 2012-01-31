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
package org.netbeans.modules.web.common.reload;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.web.common.reload.Message.MessageType;
import org.netbeans.modules.web.common.websocket.WebSocketReadHandler;
import org.netbeans.modules.web.common.websocket.WebSocketServer;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
final class ExternalBrowserPlugin implements BrowserPlugin {
    
    private static final int PORT = 8008;
    
    private static final Logger LOG = Logger.getLogger( 
            BrowserReload.class.getCanonicalName());
    
    private ExternalBrowserPlugin() {
        try {
            server = new WebSocketServer(new InetSocketAddress(PORT)){
              
                @Override
                public void close(SelectionKey key) {
                    removeKey( key );
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
            
            url2File = new ConcurrentHashMap<String, FileObject>();
        }
        catch (IOException e) {
            LOG.log( Level.WARNING , null , e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.reload.BrowserPlugin#register(org.openide.filesystems.FileObject, java.lang.String)
     */
    @Override
    public void register( FileObject localFileObject, String browserUrl ) {
        url2File.put( browserUrl, localFileObject );        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.reload.BrowserPlugin#canReload(org.openide.filesystems.FileObject)
     */
    @Override
    public boolean canReload( FileObject fileObject ) {
        Couple<BrowserPlugin, ? extends Couple> couple = BrowserReload.
            getInstance().getPluginMap().get( fileObject );
        if ( couple == null ){
            return false;
        }
        Couple<?,?> communicationCouple = couple.getEnd();
        return communicationCouple != null && communicationCouple.getStart()!= null 
            && communicationCouple.getEnd()!= null ;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.reload.BrowserPlugin#reload(org.openide.filesystems.FileObject)
     */
    @Override
    public void reload( FileObject fileObject ) {
        Couple<BrowserPlugin, ? extends Couple> couple = BrowserReload.
            getInstance().getPluginMap().get( fileObject );
        Couple<?,?> communicationCouple = couple.getEnd();
        SelectionKey selectionKey = (SelectionKey)communicationCouple.getStart();
        if ( selectionKey == null ){
            return;
        }
        String id = communicationCouple.getEnd().toString();
            
        server.sendMessage(selectionKey, createReloadMessage(id) );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.reload.BrowserPlugin#clear(org.openide.filesystems.FileObject)
     */
    @Override
    public void clear( FileObject fileObject ) {
    }
    
    private void removeKey( SelectionKey key ) {
         Map<FileObject, Couple<BrowserPlugin, ? extends Couple>> pluginMap = 
             BrowserReload.getInstance().getPluginMap();
         for(Iterator<Entry<FileObject,Couple<BrowserPlugin, ? extends Couple>>> 
             iterator = pluginMap.entrySet().iterator() ; iterator.hasNext() ; )
         {
             Entry<FileObject, Couple<BrowserPlugin, ? extends Couple>> entry = 
                 iterator.next();
             Couple<BrowserPlugin, ? extends Couple> couple = entry.getValue();
             if ( couple == null ){
                 continue;
             }
             Couple<?,?> commCouple = couple.getEnd();
             if ( key.equals( commCouple.getStart() )) {
                 iterator.remove();
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
            if ( msg == null ){
                return;
            }
            MessageType type = msg.getType();
            switch (type) {
                case INIT:
                    handleInit(msg, key );
                    break;
                case CLOSE:
                    handleClose( msg , key );
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
            FileObject localFile = url2File.remove( url );
            if ( localFile == null ){
                Map<String,String> map = new HashMap<String, String>();
                map.put( Message.TAB_ID, tabId );
                map.put("status","notaccepted");       // NOI18N
                Message msg = new Message( MessageType.INIT , map );
                server.sendMessage(key, msg.toString());
            }
            else  {
                BrowserReload.getInstance().getPluginMap().put( localFile , 
                        new Couple<BrowserPlugin,Couple>( 
                                ExternalBrowserPlugin.getInstance() , 
                                    new Couple(key,tabId)) );
                Map<String,String> map = new HashMap<String, String>();
                map.put( Message.TAB_ID, tabId );
                map.put("status","accepted");       // NOI18N
                Message msg = new Message( MessageType.INIT , map );
                server.sendMessage(key, msg.toString());
            }
        }
        
        private void handleClose( Message message, SelectionKey key  ){
            String tabId = message.getValue( Message.TAB_ID );
            if ( tabId == null ){
                return;
            }
            Map<FileObject, Couple<BrowserPlugin, ? extends Couple>> pluginMap = 
                BrowserReload.getInstance().getPluginMap();
            for(Iterator<Entry<FileObject,Couple<BrowserPlugin, ? extends Couple>>> 
                iterator = pluginMap.entrySet().iterator() ; iterator.hasNext() ; )
            {
                Entry<FileObject, Couple<BrowserPlugin, ? extends Couple>> entry = 
                    iterator.next();
                Couple<BrowserPlugin, ? extends Couple> couple = entry.getValue();
                if ( couple == null ){
                    continue;
                }
                Couple<?,?> commCouple = couple.getEnd();
                if ( tabId.equals( commCouple.getEnd() )) {
                    iterator.remove();
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
    
    private String createReloadMessage(String tabId) {
        Message msg = new Message( MessageType.RELOAD, 
                Collections.singletonMap( Message.TAB_ID, tabId ));
        return msg.toString();
    }
    
    public static ExternalBrowserPlugin getInstance(){
        return INSTANCE;
    }
    
    private WebSocketServer server;
    private Map<String, FileObject> url2File;
    private static final ExternalBrowserPlugin INSTANCE = new ExternalBrowserPlugin();

}

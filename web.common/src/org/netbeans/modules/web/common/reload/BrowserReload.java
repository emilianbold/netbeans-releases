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
public class BrowserReload {
    
    private static final int PORT = 8008;
    
    private static final Logger LOG = Logger.getLogger( 
            BrowserReload.class.getCanonicalName());
    
    private BrowserReload(){
        try {
            server = new WebSocketServer(new InetSocketAddress(PORT));
            server.setWebSocketReadHandler( new BrowserPluginHandler() );
            new Thread( server ).start();
            
            Thread shutdown = new Thread(){
                @Override
                public void run() {
                    server.stop();
                }
            };
            Runtime.getRuntime().addShutdownHook( shutdown);
            
            file2Id = new ConcurrentHashMap<FileObject, String>();
            url2File = new ConcurrentHashMap<String, FileObject>();
            file2Socket = new ConcurrentHashMap<FileObject, SelectionKey>();
        }
        catch (IOException e) {
            LOG.log( Level.WARNING , null , e);
        }
    }
    
    public void register( FileObject localFileObject, String browserUrl ){
        // TODO : probably url should be modified somehow for identification it in the browser plugin
        if ( browserUrl == null || localFileObject == null ){
            return;
        }
        url2File.put( browserUrl, localFileObject );
    }
    
    public boolean canReload( FileObject fileObject ){
        String tabId = file2Id.get( fileObject );
        if ( tabId == null ){
            return false;
        }
        SelectionKey selectionKey = file2Socket.get( fileObject );
        if ( selectionKey == null ){
            return false;
        }
        return true;
    }
    
    public void reload( FileObject fileObject ){
        String tabId = file2Id.get( fileObject );
        if ( tabId == null ){
            return;
        }
        SelectionKey selectionKey = file2Socket.get( fileObject );
        if ( selectionKey == null ){
            return;
        }
            
        server.sendMessage(selectionKey, createReloadMessage(tabId) );
    }
    
    public static BrowserReload getInstance(){
        return INSTANCE;
    }
    
    private String createReloadMessage(String tabId) {
        Message msg = new Message( MessageType.RELOAD, 
                Collections.singletonMap( Message.TAB_ID, tabId ));
        return msg.toString();
    }
    
    public class BrowserPluginHandler implements WebSocketReadHandler {

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
                file2Socket.put( localFile , key );
                file2Id.put( localFile , tabId );
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
            FileObject fileObject = null;
            for (Iterator<Entry<FileObject,String>> iterator = file2Id.entrySet().iterator(); 
                iterator.hasNext() ; ) 
            {
                Entry<FileObject, String> entry = iterator.next();
                String id = entry.getValue();
                if ( tabId.equals( id )){
                    fileObject = entry.getKey();
                    iterator.remove();
                    break;
                }
            }
            if ( fileObject != null ){
                file2Socket.remove( fileObject );
                try {
                    server.close(key);
                }
                catch(IOException e){
                    LOG.log( Level.INFO, null , e );
                }
            }
        }
        
    }

    private WebSocketServer server;
    private static final BrowserReload INSTANCE = new BrowserReload();
    private Map<String, FileObject> url2File;
    private Map<FileObject,String> file2Id;
    private Map<FileObject,SelectionKey> file2Socket;
}

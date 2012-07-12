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
package org.netbeans.modules.netserver.websocket;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.netserver.ReadHandler;
import org.netbeans.modules.netserver.SocketServer;


/**
 * @author ads
 *
 */
public class WebSocketServer extends SocketServer {
    
    public static final String UTF_8 = "UTF-8";                    // NOI18N
    private static final char NEW_LINE = '\n';
    
    protected static final Logger LOG = SocketServer.LOG;
    
    static final String VERSION = "Sec-WebSocket-Version";  // NOI18N
    static final String KEY = "Sec-WebSocket-Key";          // NOI18N
    static final String KEY1 = "Sec-WebSocket-Key1";        // NOI18N
    static final String KEY2 = "Sec-WebSocket-Key2";        // NOI18N

    public WebSocketServer( SocketAddress address ) throws IOException {
        this(address , true);
    }
    
    protected WebSocketServer( SocketAddress address , boolean onlyWebSocket) 
        throws IOException 
    {
        super(address);
        if ( onlyWebSocket ){
            setReadHandler(new WebSocketHandler());
        }
    }
    
    public void setWebSocketReadHandler( WebSocketReadHandler handler ){
        this.handler = handler ;
    }
    
    public WebSocketReadHandler getWebSocketReadHandler(){
        return handler;
    }
    
    public void sendMessage( SelectionKey key , String message){
        byte[] bytes = getHandler(key).createTextFrame( message);
        send(bytes , key); 
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.SocketServer#getWriteQueue(java.nio.channels.SelectionKey)
     */
    @Override
    protected Queue<ByteBuffer> getWriteQueue( SelectionKey key ) {
        return getContext(key).getQueue();
    }

    @Override
    protected SocketAddress getAddress() {
        return super.getAddress();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.SocketServer#initWriteQueue(java.nio.channels.SelectionKey)
     */
    @Override
    protected void initWriteQueue( SelectionKey key ) {
        if ( key.attachment() == null ){
            key.attach( new SocketContext() );
        }
    }
    
    protected SocketContext getContext( SelectionKey key ){
        return (SocketContext)key.attachment();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.SocketServer#getReadHandler()
     */
    @Override
    protected WebSocketHandler getReadHandler() {
        return (WebSocketHandler)super.getReadHandler();
    }
    
    protected void setHandler( WebSocketChanelHandler handler , SelectionKey key){
        getContext(key).setHandler(handler);
    }
    
    protected WebSocketChanelHandler getHandler(SelectionKey key ){
        return getContext(key).getHandler();
    }

    @Override
    public void close(SelectionKey key) throws IOException {
        super.close(key);
        getWebSocketReadHandler().closed(key);
    }
    
    static class SocketContext {
        
        public SocketContext() {
            writeQueue = new ConcurrentLinkedQueue<ByteBuffer>();
        }
        
        Queue<ByteBuffer> getQueue(){
            return writeQueue;
        }
        
        Map<String,String> getHeaders(){
            return headers;
        }
        
        void setHeaders( Map<String,String> headers ){
            this.headers = headers;
        }
        
        String getRequestString(){
            return httpRequest;
        }
        
        void setRequest( String request ){
            httpRequest = request;
        }
        
        void setHandler( WebSocketChanelHandler handler ){
            this.handler = handler;
        }
        
        WebSocketChanelHandler getHandler(){
            return handler;
        }
        
        private final Queue<ByteBuffer> writeQueue; 
        private volatile Map<String,String> headers;
        private volatile String httpRequest;
        private volatile WebSocketChanelHandler handler;
    }
    
    protected class WebSocketHandler implements ReadHandler {
        
        WebSocketHandler(){
            byteBuffer = ByteBuffer.allocate(BYTES);
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.web.common.websocket.ReadHandler#read(java.nio.channels.SelectionKey)
         */
        @Override
        public void read( SelectionKey key ) throws IOException {
            if ( getContext(key).getHeaders()!=null ){
                getContext(key).getHandler().read( byteBuffer );
            }
            else {
                handshake( key );
            }
        }
        
        protected void handshake( SelectionKey key ) throws IOException {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            try {
                if ( !readHttpRequest(socketChannel, key )){
                    close(key);
                    return;
                }
                initHandler(key);
                getWebSocketReadHandler().accepted(key);
            }
            catch(IOException e ){
                close(key);
            }
        }

        protected void initHandler( SelectionKey key ) throws IOException {
            Map<String, String> headers = getContext(key).getHeaders();
            String version = headers.get(VERSION);
            if ( version == null ){
                if ( headers.containsKey( KEY1 ) ){
                    handshakeVersion76( key );
                }
                else {
                    handshakeVersion75( key );
                }
            }
            else {
                handshakeVersion7( key );
            }
            WebSocketChanelHandler handler = getHandler(key);
            if ( handler== null){
                LOG.log(Level.WARNING , "Unexpected protocol version. " +
                		"Chanel handler is null."); 
                close(key);
                return;
            }
            handler.sendHandshake( );
        }
        
        /**
         * Handshake for protocol version since 07
         */
        private void handshakeVersion7( SelectionKey key ) {
            setHandler( new WebSocketHandler7(WebSocketServer.this, key ), key );
        }

        /**
         * Handshake for protocol version 75
         */
        private void handshakeVersion75( SelectionKey key ) {
            setHandler( new WebSocketHandler75(WebSocketServer.this, key ), key );            
        }

        /**
         * Handshake for protocol version 76
         */
        private void handshakeVersion76( SelectionKey key ) {
            setHandler( new WebSocketHandler76(WebSocketServer.this, key ), key );            
        }

        protected boolean readHttpRequest(SocketChannel socketChannel, 
                SelectionKey key) throws IOException
        {
                List<String> headers = new LinkedList<String>();
                byteBuffer.clear();
                StringBuilder builder = new StringBuilder();
                byte[] bytes = new byte[ BYTES ];
                read: while( true ){
                    int read = socketChannel.read( byteBuffer );
                    if ( read ==-1 ){
                        return false;
                    }
                    byteBuffer.flip();
                    int size = byteBuffer.limit();
                    byteBuffer.get( bytes , 0, size);
                    byteBuffer.clear();
                    String stringValue = new String( bytes , 0, size, 
                            Charset.forName(UTF_8) );
                    int index = stringValue.indexOf(NEW_LINE);
                    if ( index == -1 ){
                        builder.append( stringValue );
                    }
                    else {
                        builder.append( stringValue.subSequence(0, index));
                        String line = builder.toString().trim();
                        headers.add( line );
                        builder.setLength(0);
                        if ( line.isEmpty() ){
                            break;
                        }
                        do {
                            stringValue = stringValue.substring( index +1);
                            index = stringValue.indexOf(NEW_LINE );
                            if ( index != -1){
                                line = stringValue.substring( 0, index ).trim();
                                headers.add( line );
                                if ( line.isEmpty() ){
                                    break read;
                                }
                            }
                        }
                        while( index != -1 );
                        builder.append( stringValue);
                    }
                }
                setHeaders(key , headers);
                return true;
        }
        
        private void setHeaders(SelectionKey key , List<String> headerLines){
            if ( headerLines.size() >0 ){
                getContext(key).setRequest(headerLines.get(0));
            }
            Map<String,String> result = new HashMap<String, String>();
            for (String line : headerLines) {
                int index = line.indexOf(':');
                if ( index != -1 ){
                    result.put( line.substring( 0, index), line.substring(index+1).trim());
                }
            }
            getContext(key).setHeaders(result);
        }
        
        private ByteBuffer byteBuffer;
    }
    
    private volatile WebSocketReadHandler handler;
}

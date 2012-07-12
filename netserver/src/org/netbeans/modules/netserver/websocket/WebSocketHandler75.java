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
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.netbeans.modules.netserver.SocketFramework;


/**
 * @author ads
 *
 */
class WebSocketHandler75 extends AbstractWSHandler75 {
    
    private static final String WS_PROTOCOL = "WebSocket-Protocol";     // NOI18N
    
    public WebSocketHandler75( WebSocketServer webSocketServer, SelectionKey key ) {
        server = webSocketServer;
        this.key = key;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.WebSocketChanelHandler#sendHandshake()
     */
    @Override
    public void sendHandshake( ) {
        StringBuilder builder = new StringBuilder(HTTP_RESPONSE);
        builder.append(CRLF);
        builder.append(WS_UPGRADE);
        builder.append(CRLF);
        builder.append(CONN_UPGRADE);
        builder.append(CRLF);
        builder.append("WebSocket-Origin: ");                        // NOI18N
        String origin = getServer().getContext(key).getHeaders().get("Origin");      // NOI18N
        if ( origin != null ){
            builder.append( origin);
        }
        builder.append(CRLF);
        builder.append("WebSocket-Location: ws://");                 // NOI18N
        String host = getServer().getContext(key).getHeaders().get(HOST);                
        if ( host != null) {
            builder.append( host );
        }
        else {
            builder.append("127.0.0.1:");                            // NOI18N
            builder.append( ((InetSocketAddress)server.getAddress()).getPort());
        }
        String request = getServer().getContext(key).getRequestString();
        int index = request.indexOf(' ');
        String url = null;
        if ( index != -1 ){
            request = request.substring(index).trim();
            index = request.indexOf(' ');
            if ( index !=-1 ){
                url = request.substring( 0, index ).trim();
            }
        }
        else {
            url ="/";
        }
        builder.append( url );
        builder.append( CRLF );
        String protocol = getServer().getContext(key).getHeaders().get(WS_PROTOCOL);
        if ( protocol != null ){
            builder.append( WS_PROTOCOL );
            builder.append(": ");               // NOI18N
            builder.append( protocol );
        }
        builder.append( CRLF );
        builder.append( CRLF );
        getServer().send(builder.toString().getBytes( 
                Charset.forName(WebSocketServer.UTF_8)), key );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.WebSocketChanelHandler#read(java.nio.ByteBuffer)
     */
    @Override
    public void read( ByteBuffer byteBuffer ) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        byte[] bytes = new byte[SocketFramework.BYTES];
        List<List<Byte>> messages = new LinkedList<List<Byte>>();
        List<Byte> message = new LinkedList<Byte>();
        boolean newMessage = false;
        while (true) {
            byteBuffer.clear();
            if (socketChannel.read(byteBuffer) == -1) {
                close(key);
            }
            byteBuffer.flip();
            byteBuffer.get(bytes);
            if (bytes[0] == 0 && !newMessage) {
                newMessage = true;
                if (!message.isEmpty()) {
                    messages.add(new ArrayList<Byte>(message));
                }
                message.clear();
            }
            int i;
            for (i = 1; i < byteBuffer.limit(); i++) {
                if (bytes[i] == (byte) 255) {
                    messages.add(new ArrayList<Byte>(message));
                    message.clear();
                    newMessage = false;
                }
                else {
                    message.add(bytes[i]);
                }
            }
            if (message.isEmpty()) {
                break;
            }
        }
        for (List<Byte> list : messages) {
            bytes = new byte[list.size()];
            int i = 0;
            for (Byte byt : list) {
                bytes[i] = byt;
                i++;
            }
            getServer().getWebSocketReadHandler().read(key, bytes, null);
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.WebSocketChanelHandler#createTextFrame(java.lang.String)
     */
    @Override
    public byte[] createTextFrame( String message ) {
        byte[] data = message.getBytes( Charset.forName( WebSocketServer.UTF_8));
        byte[] result = new byte[ data.length +2 ];
        result[0] = 0;
        result[ data.length +1 ]=(byte)255;
        System.arraycopy(data, 0, result, 1, data.length);
        return result;
    }
    
    protected WebSocketServer getServer(){
        return server;
    }
    
    protected void close( SelectionKey key ){
        try {
            getServer().close(key);
        }
        catch( IOException e ){
            WebSocketServer.LOG.log( Level.WARNING , null , e);
        }
    }
    
    protected SelectionKey getKey(){
        return key;
    }
    
    private WebSocketServer server;
    private SelectionKey key;
    
}

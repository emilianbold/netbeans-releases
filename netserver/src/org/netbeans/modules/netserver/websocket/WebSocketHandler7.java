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
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

import javax.xml.bind.DatatypeConverter;


/**
 * @author ads
 *
 */
class WebSocketHandler7 extends AbstractWSHandler7 {
    
    private static final String SALT = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";     // NOI18N
    
    /**
     * FIN bit is set and opcode is text ( equals 1 )
     */
    private static final byte FIRST_BYTE_MESSAGE = Integer.valueOf("10000001",     // NOI18N
            2).byteValue();
    
    /**
     * FIN bit is set and opcode is close connection ( equals 8 )
     */
    private static final byte CLOSE_CONNECTION_BYTE = Integer.valueOf("10001000",     // NOI18N
            2).byteValue();
    
    /**
     * FIN bit is set and opcode is binary ( equals 2 )
     */
    private static final byte FIRST_BYTE_BINARY = Integer.valueOf("10000010",     // NOI18N
            2).byteValue();
    
    /*
     * Message max length which is marked in the message with 126 code in the 
     * "Extended payload length" section 
     */
    private static final int LENGTH_LEVEL  = 0x10000;                              
    
    public WebSocketHandler7( WebSocketServer webSocketServer, SelectionKey key ) {
        server = webSocketServer;
        this.key=key;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.WebSocketChanelHandler#sendHandshake()
     */
    @Override
    public void sendHandshake( ) {
        String acceptKey = createAcceptKey( key );
        if ( acceptKey == null ){
            close( key );
            return;
        }
        StringBuilder builder = new StringBuilder(HTTP_RESPONSE);
        builder.append(CRLF);
        builder.append(WS_UPGRADE);
        builder.append(CRLF);
        builder.append(CONN_UPGRADE);
        builder.append(CRLF);
        builder.append("Sec-WebSocket-Origin: ");           // NOI18N
        String origin = server.getContext(key).getHeaders().get("Sec-WebSocket-Origin");  // NOI18N
        if ( origin == null ){
            origin = server.getContext(key).getHeaders().get("Origin");  // NOI18N
        }
        if ( origin != null ){
            builder.append( origin);
        }
        builder.append(CRLF);
        builder.append("Sec-WebSocket-Accept: ");
        builder.append(acceptKey);
        builder.append( CRLF );
        builder.append( CRLF );
        server.send(builder.toString().getBytes(
                Charset.forName(WebSocketServer.UTF_8)), key );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.WebSocketChanelHandler#read(java.nio.ByteBuffer)
     */
    @Override
    public void read( ByteBuffer byteBuffer ) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        while (true) {
            byteBuffer.clear();
            byteBuffer.limit(1);
            int size = socketChannel.read(byteBuffer);
            if (size == -1) {
                close(key);
                return;
            }
            else if (size == 0) {
                return;
            }
            byteBuffer.flip();
            byte leadingByte = byteBuffer.get();
            if (leadingByte == CLOSE_CONNECTION_BYTE) {
                // connection close
                close(key);
                return;
            }
            else if (leadingByte == FIRST_BYTE_MESSAGE
                    || leadingByte == FIRST_BYTE_BINARY)
            {
                if (!readFinalFrame(key, byteBuffer, socketChannel, leadingByte))
                {
                    return;
                }
                else {
                    continue;
                }
            }
            else {
                // TODO : handle frame sequence, ping frame
            }

        }
    }

    @Override
    public byte[] createTextFrame( String message ) {
        byte[] data = message.getBytes( Charset.forName( WebSocketServer.UTF_8));
        int length = data.length;
        byte[] lengthBytes;
        if ( length< 126){
            lengthBytes =new byte[]{ (byte)length };
        }
        else if (length < LENGTH_LEVEL){
            lengthBytes = new byte[]{126, (byte)(length>>8), (byte)(length&0xFF)};
        }
        else {
            lengthBytes = new byte[9];
            lengthBytes[0] = 127;
            for( int i =8; i>=1; i-- ){
                lengthBytes[i]=(byte)(length & 0xFF);
                length = length >>8;
            }
        }
        /* Set masking bit to avoid clients that require masking
         * lengthBytes[0]=(byte)(lengthBytes[0]|0x80);
         */ 
        /* Reserve 4 bytes for mask 
         * ( without mask only data.length+lengthBytes.length +1 are required )
         * byte[] result = new byte[data.length+lengthBytes.length+5];
         */
        byte[] result = new byte[data.length+lengthBytes.length+1];
        result[0] = FIRST_BYTE_MESSAGE;
        System.arraycopy(lengthBytes, 0, result, 1, lengthBytes.length);
        /*
         *  Don't fill mask at all. XOR with 0 mask doesn't change the value
         *  System.arraycopy( data, 0 , result, lengthBytes.length+5, data.length);
         */
        System.arraycopy( data, 0 , result, lengthBytes.length+1, data.length);
        return result;
    }
    
    private boolean readFinalFrame( SelectionKey key, ByteBuffer byteBuffer,
            SocketChannel socketChannel, byte leadingByte) throws IOException
    {
        int frameType = leadingByte == FIRST_BYTE_MESSAGE? 1:2;
        byteBuffer.clear();
        byteBuffer.limit(1);
        int size ;
        do {
            size = socketChannel.read(byteBuffer);
            if (  size==-1 ){
                close( key);
                return false;
            }
        }
        while( size ==0 );
        byteBuffer.flip();
        byte masknLength = byteBuffer.get();
        if ( masknLength>=0 ){   // first bit is not set
            WebSocketServer.LOG.log(Level.WARNING, 
                    "Unexpected client data. Frame is not masked"); // NOI18N
            close( key );
            return false;
        }
        int length = masknLength&0x7F;
        if ( length <126 ){
            return readData(key, byteBuffer, socketChannel, frameType, length);
        }
        else if ( length ==126 ){
            byteBuffer.clear();
            byteBuffer.limit(2);
            do {
                size = socketChannel.read(byteBuffer);
                if (  size==-1 ){
                    close( key);
                    return false;
                }
            }
            while(size<2);
            byteBuffer.flip();
            length = byteBuffer.getShort()&0xFFFF;
            return readData(key, byteBuffer, socketChannel, frameType, length);
        }
        else if ( length ==127 ){
            byteBuffer.clear();
            byteBuffer.limit(8);
            do {
                size = socketChannel.read(byteBuffer);
                if (  size==-1 ){
                    close( key);
                    return false;
                }
            }
            while(size<8);
            byteBuffer.flip();
            long longLength = byteBuffer.getLong();
            return readData(key, byteBuffer, socketChannel, frameType, longLength );
        }
        return true;
    }

    private boolean readData( SelectionKey key, ByteBuffer byteBuffer,
            SocketChannel socketChannel, int frameType, int length )
            throws IOException
    {
        byteBuffer.clear();
        int frameSize = length +4;
        if ( frameSize <0 ){
            readData(key, byteBuffer, socketChannel, frameType, (long)length);
        }
        byte[] result = readData(key, byteBuffer, socketChannel, frameSize);
        if ( result == null ){
            return false;
        }
        server.getWebSocketReadHandler().read(key, mask( result), frameType);
        
        return true;
    }

    private byte[] readData( SelectionKey key, ByteBuffer byteBuffer,
            SocketChannel socketChannel, int size ) throws IOException
    {
        int redBytes =0;
        byte[] result = new byte[ size ];
        int fullBufferCount =0;
        if (size < byteBuffer.capacity()) {
            byteBuffer.limit(size);
        }
        while( redBytes <size ){
            int red = socketChannel.read( byteBuffer );
            if ( red == -1){
                close(key);
                return null;
            }
            if ( red ==0 ){
                continue;
            }
            redBytes += red;
            if (redBytes%byteBuffer.capacity() == 0){
                byteBuffer.flip();
                byteBuffer.get( result , fullBufferCount*byteBuffer.capacity(), 
                        byteBuffer.limit());
                fullBufferCount++;
                byteBuffer.clear();
                int resultRed = fullBufferCount*byteBuffer.capacity();
                if ( size- resultRed<=byteBuffer.capacity()){
                    byteBuffer.limit( size -resultRed);
                }
            }
        }
        byteBuffer.flip();
        int savedBytes = byteBuffer.capacity()*fullBufferCount;
        byteBuffer.get( result , savedBytes, size - savedBytes);
        return result;
    }
    
    private boolean readData( SelectionKey key, ByteBuffer byteBuffer,
            SocketChannel socketChannel, int frameType, long length )
            throws IOException
    {
        int shift = (int)(length>>32);
        if ( shift != 0 ){
            throw new RuntimeException("Data frame is too big. " +
            		"Cannot handle it. Implementation should be rewritten.");
        }
        else {
            readData(key, byteBuffer, socketChannel, frameType, (int)length);
        }
        return true;
    }
    
    private byte[] mask( byte[] maskedMessage ) {
        byte[] result = new byte[ maskedMessage.length -4 ];
        for( int i=4; i< maskedMessage.length;i++){
            byte unsignedMask = (byte)(maskedMessage[i%4]&0xFF);
            result[i-4] = (byte)(unsignedMask^maskedMessage[i]);
        }
        return result;
    }
    
    private String createAcceptKey(SelectionKey key ){
        String originalKey = server.getContext(key).getHeaders().get(WebSocketServer.KEY);
        if ( originalKey == null ){
            return null;
        }
        StringBuilder builder = new StringBuilder( originalKey );
        builder.append(SALT);
        try {
            return DatatypeConverter.printBase64Binary( MessageDigest.getInstance(
                    "SHA").digest(builder.toString().getBytes(  // NOI18N
                            Charset.forName(WebSocketServer.UTF_8))));
        }
        catch (NoSuchAlgorithmException e) {
            WebSocketServer.LOG.log(Level.WARNING, null , e);
            return null;
        } 
    }
    
    private void close( SelectionKey key ){
        try {
            server.close(key);
        }
        catch( IOException e ){
            WebSocketServer.LOG.log( Level.WARNING , null , e);
        }
    }

    private WebSocketServer server;
    private SelectionKey key;
    
}

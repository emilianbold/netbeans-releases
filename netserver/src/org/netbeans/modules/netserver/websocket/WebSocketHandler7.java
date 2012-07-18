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
    
    
    public WebSocketHandler7( WebSocketServer webSocketServer, SelectionKey key ) {
        server = webSocketServer;
        this.key=key;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.WebSocketChanelHandler#sendHandshake()
     */
    @Override
    public void sendHandshake( ) throws IOException {
        String acceptKey = createAcceptKey( getKey() );
        if ( acceptKey == null ){
            close( );
            return;
        }
        StringBuilder builder = new StringBuilder(Utils.HTTP_RESPONSE);
        builder.append(Utils.CRLF);
        builder.append(Utils.WS_UPGRADE);
        builder.append(Utils.CRLF);
        builder.append(Utils.CONN_UPGRADE);
        builder.append(Utils.CRLF);
        builder.append("Sec-WebSocket-Origin: ");           // NOI18N
        String origin = server.getContext(key).getHeaders().get("Sec-WebSocket-Origin");  // NOI18N
        if ( origin == null ){
            origin = server.getContext(key).getHeaders().get("Origin");  // NOI18N
        }
        if ( origin != null ){
            builder.append( origin);
        }
        builder.append(Utils.CRLF);
        builder.append(Utils.ACCEPT);
        builder.append(": ");
        builder.append(acceptKey);
        builder.append( Utils.CRLF );
        builder.append( Utils.CRLF );
        server.send(builder.toString().getBytes(
                Charset.forName(Utils.UTF_8)), key );
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
                close();
                return;
            }
            else if (size == 0) {
                return;
            }
            byteBuffer.flip();
            byte leadingByte = byteBuffer.get();
            if (leadingByte == CLOSE_CONNECTION_BYTE) {
                // connection close
                close();
                return;
            }
            else if (leadingByte == FIRST_BYTE_MESSAGE
                    || leadingByte == FIRST_BYTE_BINARY)
            {
                if (!readFinalFrame( byteBuffer, socketChannel, leadingByte))
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

    /**
     * TODO: Remove this method ( superclass method should be used )
     */
    @Override
    public byte[] createTextFrame( String message ) {
        byte[] data = message.getBytes( Charset.forName( Utils.UTF_8));
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
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler7#isClient()
     */
    @Override
    protected boolean isClient() {
        return false;
    }
    
    private boolean readFinalFrame( ByteBuffer byteBuffer,
            SocketChannel socketChannel, byte leadingByte) throws IOException
    {
        int frameType = leadingByte == FIRST_BYTE_MESSAGE? 1:2;
        byteBuffer.clear();
        byteBuffer.limit(1);
        int size ;
        do {
            size = socketChannel.read(byteBuffer);
            if (  size==-1 ){
                close( );
                return false;
            }
        }
        while( size ==0 );
        byteBuffer.flip();
        byte masknLength = byteBuffer.get();
        if ( masknLength>=0 ){   // first bit is not set
            WebSocketServer.LOG.log(Level.WARNING, 
                    "Unexpected client data. Frame is not masked"); // NOI18N
            close( );
            return false;
        }
        int length = masknLength&0x7F;
        if ( length <126 ){
            return readData(byteBuffer, socketChannel, frameType, length);
        }
        else if ( length ==126 ){
            byteBuffer.clear();
            byteBuffer.limit(2);
            do {
                size = socketChannel.read(byteBuffer);
                if (  size==-1 ){
                    close( );
                    return false;
                }
            }
            while(size<2);
            byteBuffer.flip();
            length = byteBuffer.getShort()&0xFFFF;
            return readData(byteBuffer, socketChannel, frameType, length);
        }
        else if ( length ==127 ){
            byteBuffer.clear();
            byteBuffer.limit(8);
            do {
                size = socketChannel.read(byteBuffer);
                if (  size==-1 ){
                    close( );
                    return false;
                }
            }
            while(size<8);
            byteBuffer.flip();
            long longLength = byteBuffer.getLong();
            return readData(byteBuffer, socketChannel, frameType, longLength );
        }
        return true;
    }

    private boolean readData( ByteBuffer byteBuffer,
            SocketChannel socketChannel, int frameType, int length )
            throws IOException
    {
        byteBuffer.clear();
        int frameSize = length +4;
        if ( frameSize <0 ){
            readData(byteBuffer, socketChannel, frameType, (long)length);
        }
        byte[] result = readData( byteBuffer, socketChannel, frameSize);
        if ( result == null ){
            return false;
        }
        server.getWebSocketReadHandler().read(getKey(), mask( result, true), frameType);
        
        return true;
    }

    private byte[] readData( ByteBuffer byteBuffer,
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
                close();
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
    
    private boolean readData(ByteBuffer byteBuffer,
            SocketChannel socketChannel, int frameType, long length )
            throws IOException
    {
        int shift = (int)(length>>32);
        if ( shift != 0 ){
            throw new RuntimeException("Data frame is too big. " +
            		"Cannot handle it. Implementation should be rewritten.");
        }
        else {
            readData(byteBuffer, socketChannel, frameType, (int)length);
        }
        return true;
    }
    
    private String createAcceptKey(SelectionKey key ){
        String originalKey = server.getContext(key).getHeaders().get(Utils.KEY);
        if ( originalKey == null ){
            return null;
        }
        StringBuilder builder = new StringBuilder( originalKey );
        builder.append(SALT);
        try {
            return DatatypeConverter.printBase64Binary( MessageDigest.getInstance(
                    "SHA").digest(builder.toString().getBytes(  // NOI18N
                            Charset.forName(Utils.UTF_8))));
        }
        catch (NoSuchAlgorithmException e) {
            WebSocketServer.LOG.log(Level.WARNING, null , e);
            return null;
        } 
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler7#getKey()
     */
    @Override
    protected SelectionKey getKey() {
        return key;
    }
    
    @Override
    protected void close( ) throws IOException {
        server.close(getKey());
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler7#readDelegate(byte[], int)
     */
    @Override
    protected void readDelegate( byte[] bytes, int dataType ) {
        server.getWebSocketReadHandler().read(getKey(), mask( bytes, true), dataType);        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler7#onHasMask(boolean)
     */
    @Override
    protected boolean verifyMask( boolean hasMask ) throws IOException {
        if ( !hasMask ){
            WebSocketServer.LOG.log(Level.WARNING, 
                    "Unexpected client data. Frame is not masked"); // NOI18N
            close();
            return false;
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler7#isStopped()
     */
    @Override
    protected boolean isStopped() {
        return server.isStopped();
    }

    private WebSocketServer server;
    private SelectionKey key;
    
}

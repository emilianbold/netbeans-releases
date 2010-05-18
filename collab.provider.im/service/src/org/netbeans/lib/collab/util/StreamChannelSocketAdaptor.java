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
 *
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

package org.netbeans.lib.collab.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

import java.net.Socket;
import java.net.SocketException;
import java.net.SocketAddress;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSession;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;

/**
 *
 *
 * @author Vijayakumar Palaniappan
 *
 */
public class StreamChannelSocketAdaptor extends Socket {
    
    private Socket _channelSocket = null;
    private Socket _wrapperSocket = null;
    private boolean _sslSock = false;
    private OutStream _outStream = new OutStream();
    private InStream _inStream = new InStream();
    private ByteChannel _byteChannel;
          
    /** Creates a new instance of StreamChannelSocketAdaptor */
    public StreamChannelSocketAdaptor(Socket channelSocket ) {
        this(channelSocket,null);
    }
    public StreamChannelSocketAdaptor(Socket channelSocket , ByteChannel byteChannel) {
        _channelSocket = channelSocket;
        _byteChannel = byteChannel;
        if(_byteChannel == null) {
            _byteChannel = channelSocket.getChannel();
        }
    }
    
    //Init this only once.
    public void initWrapperSocket(Socket sock) {
        _wrapperSocket = sock;
        //To avoid instanceof check each time. 
        _sslSock = _wrapperSocket instanceof SSLSocket;
    }
    
    public OutputStream getOutputStream() throws IOException {
        //System.out.println("Get the output stream");
        return _outStream;
	//return _channelSocket.getOutputStream();
    }
    
    public InputStream getInputStream() throws IOException {
        return _inStream;
        //return _channelSocket.getInputStream();
    }

    public void bind(SocketAddress socketaddress_1) throws IOException {
        _channelSocket.bind(socketaddress_1);
    }

    public synchronized void close() throws IOException {
        _channelSocket.close();
    }

    public void connect(SocketAddress socketaddress_1) throws IOException {
        _channelSocket.connect(socketaddress_1);
    }

    public void connect(SocketAddress socketaddress_1, int int_1) throws IOException {
        _channelSocket.connect(socketaddress_1,int_1);
    }

    public SocketChannel getChannel() {
        return _channelSocket.getChannel();
    }

    public InetAddress getInetAddress() {
        return _channelSocket.getInetAddress();
    }

    public boolean getKeepAlive() throws SocketException {
        return _channelSocket.getKeepAlive();
    }

    public InetAddress getLocalAddress() {
        return _channelSocket.getLocalAddress();
    }

    public int getLocalPort() {
        return _channelSocket.getLocalPort();
    }

    public SocketAddress getLocalSocketAddress() {
        return _channelSocket.getLocalSocketAddress();
    }

    public boolean getOOBInline() throws SocketException {
        return _channelSocket.getOOBInline();
    }

    public int getPort() {
        return _channelSocket.getPort();
    }

    public synchronized int getReceiveBufferSize() throws SocketException {
        return _channelSocket.getReceiveBufferSize();
    }

    public SocketAddress getRemoteSocketAddress() {
        return _channelSocket.getRemoteSocketAddress();
    }

    public boolean getReuseAddress() throws SocketException {
        return _channelSocket.getReuseAddress();
    }

    public synchronized int getSendBufferSize() throws SocketException {
        return _channelSocket.getSendBufferSize();
    }

    public int getSoLinger() throws SocketException {
        return _channelSocket.getSoLinger();
    }

    public synchronized int getSoTimeout() throws SocketException {
        return _channelSocket.getSoTimeout();
    }

    public boolean getTcpNoDelay() throws SocketException {
        return _channelSocket.getTcpNoDelay();
    }

    public int getTrafficClass() throws SocketException {
        return _channelSocket.getTrafficClass();
    }

    public boolean isBound() {
        return _channelSocket.isBound();
    }

    public boolean isClosed() {
        return _channelSocket.isClosed();
    }

    public boolean isConnected() {
        return _channelSocket.isConnected();
    }

    public boolean isInputShutdown() {
        return _channelSocket.isInputShutdown();
    }

    public boolean isOutputShutdown() {
        return _channelSocket.isOutputShutdown();
    }

    public void sendUrgentData(int int_1) throws IOException {
        _channelSocket.sendUrgentData(int_1);
    }

    public void setKeepAlive(boolean boolean_1) throws SocketException {
        _channelSocket.setKeepAlive(boolean_1);
    }

    public void setOOBInline(boolean boolean_1) throws SocketException {
        _channelSocket.setOOBInline(boolean_1);
    }

    public synchronized void setReceiveBufferSize(int int_1) 
                                                        throws SocketException {
        _channelSocket.setReceiveBufferSize(int_1);
    }

    public void setReuseAddress(boolean boolean_1) throws SocketException {
        _channelSocket.setReuseAddress(boolean_1);
    }

    public synchronized void setSendBufferSize(int int_1) 
                                                        throws SocketException {
        _channelSocket.setSendBufferSize(int_1);
    }

    public void setSoLinger(boolean boolean_1, int int_1) 
                                                        throws SocketException {
        _channelSocket.setSoLinger(boolean_1,int_1);
    }

    public synchronized void setSoTimeout(int int_1) throws SocketException {
        //System.out.println("Setting timeout " + int_1);
        _channelSocket.setSoTimeout(int_1);
    }

    public void setTcpNoDelay(boolean boolean_1) throws SocketException {
        _channelSocket.setTcpNoDelay(boolean_1);
    }

    public void setTrafficClass(int int_1) throws SocketException {
        _channelSocket.setTrafficClass(int_1);
    }

    public void shutdownInput() throws IOException {
        _channelSocket.shutdownInput();
    }

    public void shutdownOutput() throws IOException {
        _channelSocket.shutdownOutput();
    }
    
    private class OutStream extends OutputStream {
        public void write(int b) throws IOException {
            write(new byte[]{(byte)b},0,1);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            //System.out.println("Wrirting... " + len);
            int written = 0;
            if(len != 0){
                ByteBuffer byteBuffer = ByteBuffer.wrap(b,off,len);
                int waitTime = 10;
                int lastCount = 0;
                boolean done = false;
                while(!done){
                    int size = _byteChannel. write(byteBuffer);
                    
                    if (size < 0){
                        throw new IOException("error writing to socket : " + size);
                    }
                    
                    written += size;
                    
                    if (written >= len) {
                        break;
                    }
                    //non blocking channels
                    //try after sometime
                    if(lastCount != written) {
                        waitTime = 10;
                    }
                    
                    lastCount = written;
                    
                    try {
                        Thread.sleep(waitTime);
                        // keep increasing wait time in order to release some cpu
                        waitTime += waitTime;
                    } catch (Exception e) {}
                }
            }
        }

        public void close() throws IOException{
            //_channelSocket.getChannel().close();
            _byteChannel.close();
        }
    }

    private class InStream extends InputStream {
        public int read() throws IOException {
            return read(new byte[1],0,1);
        }

        public void close() throws IOException {
            //_channelSocket.getChannel().close();
            _byteChannel.close();
        }
        
        
        public int read(byte[] data, int offset, int len) throws IOException {
            //System.out.println("read(byte,i1,i2) " + offset + "  " + len);
            int read = _byteChannel.read(
                                        ByteBuffer.wrap(data,offset,len));
            boolean handle_bug_4836493 = false;
            if(_sslSock) {
                //This is to avoid a problem in jsse Bug - 4836493
                //If SSL Layer is trying to read, then block
                SSLSession session = ((SSLSocket)_wrapperSocket).getSession();
                handle_bug_4836493 = session.getValue("BUG_4836493_READ") != Boolean.TRUE;
                session.removeValue("BUG_4836493_READ");
            }
            if(read == 0 ) {
                if(handle_bug_4836493) {
                    while(read == 0) {
                        try {
                            Thread.sleep(_channelSocket.getSoTimeout());
                        } catch(InterruptedException e){
                        }
                        read = _channelSocket.getChannel().read(
                                                   ByteBuffer.wrap(data,offset,len));
                    }
                } else {
                    if( _channelSocket.getSoTimeout() != 0) {
                        throw new SocketTimeoutException();
                    }
                }
            }
            return read;
        }
    }
}

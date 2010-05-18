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

package org.netbeans.lib.collab.xmpp;

import org.netbeans.lib.collab.util.CertificateVerify;
import org.netbeans.lib.collab.CollaborationSessionListener;
import org.netbeans.lib.collab.SecureSessionListener;
import org.netbeans.lib.collab.CollaborationException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;
import java.security.cert.X509Certificate;
import java.net.Socket;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.jabberstudio.jso.io.src.ChannelStreamSource;
import org.jabberstudio.jso.io.StreamSource;
import java.nio.channels.SocketChannel;
import org.netbeans.lib.collab.util.StreamChannelSocketAdaptor;
import org.netbeans.lib.collab.util.BufferedByteChannel;
import org.netbeans.lib.collab.util.JavaxX509TrustManager;
import org.netbeans.lib.collab.util.SocketByteChannel;
import org.netbeans.lib.collab.util.SelectWorker;
import java.nio.channels.ByteChannel;

/**
 *
 * 
 * @author Vijayakumar Palaniappan
 * 
 */
public class SecureStreamSourceCreator extends SocketStreamSourceCreator {
    
    private java.net.Socket _baseSocket;
    SSLSocket _sslSocket;
    private boolean _certAcceptedByClient;
    private boolean _certProvidedToClient;   
    ByteChannel bytCh = null;
    
    /** Creates a new instance of SecureStreamSourceCreator */
    public SecureStreamSourceCreator(CollaborationSessionListener sListener) {
        super(sListener);
    }
    
    public SecureStreamSourceCreator(CollaborationSessionListener sListener, SelectWorker selWorker) {
        super(sListener,selWorker);
    }
    
    public StreamSource createStreamSource(String hostName, int port) 
	throws Exception {
        JavaxX509TrustManager trustManager = new JavaxX509TrustManager(
        new CertificateVerify(){
            public boolean doYouTrustCertificate(X509Certificate[] chain){
                CollaborationSessionListener sListener = getSessionListener();
                if(sListener
                instanceof SecureSessionListener){
                    _certProvidedToClient = true;
                    _certAcceptedByClient = 
                            ((SecureSessionListener)sListener).
                                                    onX509Certificate(chain);
                    return _certAcceptedByClient;
                }
                return false;
            }
        });
        
        SocketChannel socChannel = SocketChannel.open();
        _baseSocket = socChannel.socket();
       int snd_size = XMPPSessionProvider.getSocketSendbufferSize();
        int rcv_size = XMPPSessionProvider.getSocketReceivebufferSize();
        if(rcv_size > 0) {
            _baseSocket.setReceiveBufferSize(rcv_size);
        }
        if(snd_size > 0) {
            _baseSocket.setSendBufferSize(snd_size);
        }

        socChannel.connect(new InetSocketAddress(hostName,port));
        
        if(_selector != null) {
            bytCh = new BufferedByteChannel(_baseSocket.getChannel(), _selector);
        }  else {
            bytCh = _baseSocket.getChannel();
        }
        
        StreamChannelSocketAdaptor adapter = 
	    new StreamChannelSocketAdaptor(_baseSocket,bytCh);
        SSLSocketFactory factory = trustManager.getSocketFactory();
        _sslSocket = (SSLSocket)factory.createSocket(adapter, hostName,
						     port, true);
        _sslSocket.setEnabledCipherSuites(factory.getSupportedCipherSuites());
        try {
            _sslSocket.startHandshake();
        } catch(java.io.IOException e) {
            if(_certProvidedToClient && !_certAcceptedByClient) {
                throw new org.netbeans.lib.collab.CertificateRejectedException();
            }
            throw e;
        }

        _sslSocket.setSoTimeout(100);
        adapter.initWrapperSocket(_sslSocket);
        StreamSource streamSource =
	    new ChannelStreamSource(new SocketByteChannel(_sslSocket));
        _baseSocket.getChannel().configureBlocking(false);
        return streamSource;
    }
    
    public SocketChannel getSocketChannel() throws IOException {
        return (SocketChannel)_baseSocket.getChannel();
    }
    
    public ByteChannel getBufferedChannel() throws IOException {
        return bytCh;
    }       

    protected Socket getSocket() {
	return _sslSocket;
    }
   
    public boolean isTLSSupported(){
        // Legacy class - not supported.
        return false;
    }
    
    public void upgradeToTLS(StreamSource stream) throws IOException , 
            CollaborationException , UnsupportedOperationException{
        throw new UnsupportedOperationException("Not supported");
    }
}

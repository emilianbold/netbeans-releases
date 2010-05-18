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

import org.netbeans.lib.collab.CollaborationSessionListener;
import org.netbeans.lib.collab.SecureSessionListener;
import java.net.Socket;
import java.io.IOException;
import org.jabberstudio.jso.io.src.ChannelStreamSource;
import org.jabberstudio.jso.io.StreamSource;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;
import java.security.cert.X509Certificate;

import org.netbeans.lib.collab.util.*;
import java.util.*;


/**
 *
 *
 * @author Jacques Belissent
 * 
 */
public class Socks5StreamSourceCreator extends SocketStreamSourceCreator 
{    
    private String socksHost, username, password;
    private int socksPort;
    private SocketChannel sc = null;
    private boolean usessl = false;
    private boolean _certAcceptedByClient;
    private boolean _certProvidedToClient;
    
    /** Creates a new instance of Socks5StreamSourceCreator */
    public Socks5StreamSourceCreator(CollaborationSessionListener sListener,
				     java.net.URI uri, 
				     Map attributes) {
        super(sListener);
	socksHost = uri.getHost();
	socksPort = uri.getPort();
	if (socksPort < 0) socksPort = 1080;
	username = (String)attributes.get(ProxySessionProvider.AUTHNAME_ATTR);
	password = (String)attributes.get(ProxySessionProvider.PASSWORD_ATTR);
        String ssl = (String)attributes.get(
					ProxySessionProvider.USE_SSL_ATTR);
        if(ssl != null) {
            usessl = Boolean.valueOf(ssl).booleanValue();
        }
    }
    
    public StreamSource createStreamSource(String host, int port) 
	throws Exception 
    {
        SocketChannel channel = SocketChannel.open();
        Socket soc = channel.socket();
        StreamSource streamSource = null;
        int snd_size = XMPPSessionProvider.getSocketSendbufferSize();
        int rcv_size = XMPPSessionProvider.getSocketReceivebufferSize();
        if(rcv_size > 0) {
            soc.setReceiveBufferSize(rcv_size);
        }
        if(snd_size > 0) {
            soc.setSendBufferSize(snd_size);
        }
	sc = Socks5SocketChannelAdaptor.open(host, port,
					     channel,socksHost, socksPort,
					     username, password);
        if(usessl) {
            JavaxX509TrustManager trustManager = 
                new JavaxX509TrustManager(new CertificateVerify(){
                    public boolean doYouTrustCertificate(X509Certificate[] chain){
                        CollaborationSessionListener sListener = getSessionListener();
                        if (sListener instanceof SecureSessionListener) {
                            _certProvidedToClient = true;
                            _certAcceptedByClient = 
                                ((SecureSessionListener)sListener).
                                onX509Certificate(chain);
                            return _certAcceptedByClient;
                        }
                        return false;
                    }
                });
                
            StreamChannelSocketAdaptor adapter = 
                new StreamChannelSocketAdaptor(sc.socket());

            SSLSocketFactory factory = trustManager.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket)factory.createSocket(adapter, socksHost,
                                                         socksPort, true);
            sslSocket.setEnabledCipherSuites(factory.getSupportedCipherSuites());
            try {
                sslSocket.startHandshake();
            } catch(java.io.IOException e) {
                if (_certProvidedToClient && !_certAcceptedByClient) {
                    throw new org.netbeans.lib.collab.CertificateRejectedException();
                }
                throw e;
            }
             
            sslSocket.setSoTimeout(100);
            adapter.initWrapperSocket(sslSocket);
            streamSource = new ChannelStreamSource(new SocketByteChannel(sslSocket));
        } else {
            streamSource = new ChannelStreamSource(sc);
        }

        StreamSource delegated = streamSource;
        if (!usessl){
            delegated = createDelegateStreamSource(streamSource , host , port);
        }
       
        sc.configureBlocking(false);
        return delegated;
    }
    
    public SocketChannel getSocketChannel() throws IOException {
        return sc;
    }

    protected Socket getSocket() {
	return sc.socket();
    }
    
    public boolean isTLSSupported(){
        return !usessl;
    }
}

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

import java.net.*;
import java.io.*;
import java.util.*;

import java.nio.channels.SocketChannel;
import org.jabberstudio.jso.io.StreamSource;
import org.jabberstudio.jso.util.ByteCodec;
import org.jabberstudio.jso.io.src.ChannelStreamSource;
import org.jabberstudio.jso.io.src.SocketStreamSource;

import org.netbeans.lib.collab.CollaborationSessionListener;
import org.netbeans.lib.collab.SecureSessionListener;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;

import org.netbeans.lib.collab.util.*;


/**
 * Stream Source creator used by HTTPSessionProvider
 * 
 * 
 * @author Jacques Belissent
 * 
 */
public class HTTPStreamSourceCreator extends SocketStreamSourceCreator
{    
    private String proxyHost, username, password;
    private int proxyPort;
    private boolean usessl = false;
    private java.net.Socket _socket;
    private boolean _certAcceptedByClient;
    private boolean _certProvidedToClient;
    
    static ByteCodec.Base64Codec base64 = new ByteCodec.Base64Codec();

    /** Creates a new instance of HTTPStreamSourceCreator */
    public HTTPStreamSourceCreator(CollaborationSessionListener sListener,
                                       java.net.URI uri,
                                       Map attributes) {
        this(sListener, uri);
        username = (String)attributes.get(ProxySessionProvider.AUTHNAME_ATTR);
        password = (String)attributes.get(ProxySessionProvider.PASSWORD_ATTR);
        String ssl = (String)attributes.get(
					ProxySessionProvider.USE_SSL_ATTR);
        if(ssl != null) {
            usessl = Boolean.valueOf(ssl).booleanValue();
        }
    }
    
    /** Creates a new instance of HTTPStreamSourceCreator */
    public HTTPStreamSourceCreator(CollaborationSessionListener sListener,
                                   java.net.URI uri) 
    {
        super(sListener);
        proxyHost = uri.getHost();
        proxyPort = uri.getPort();
        if (proxyPort < 0) proxyPort = usessl ? 443 : 80;
    }
    
    public StreamSource createStreamSource(String host, int port) 
        throws Exception
    {
        StreamSource streamSource;

       int snd_size = XMPPSessionProvider.getSocketSendbufferSize();
       int rcv_size = XMPPSessionProvider.getSocketReceivebufferSize();
        
        if (!usessl) {
            streamSource = ChannelStreamSource.createSocket(
							proxyHost, proxyPort);
            _sockCh = (SocketChannel)((ChannelStreamSource)streamSource).getChannel();
            _socket = _sockCh.socket();
            if(rcv_size > 0) {
                _socket.setReceiveBufferSize(rcv_size);
            }
            if(snd_size > 0) {
                _socket.setSendBufferSize(snd_size);
            }
            _sockCh.configureBlocking(true);
            doConnect(host,port,_socket);

        } else {
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
            _sockCh = SocketChannel.open();
            
            if(snd_size > 0) {
                _sockCh.socket().setReceiveBufferSize(snd_size);
            }
            if(snd_size > 0) {
                _sockCh.socket().setSendBufferSize(snd_size);
            }
            _sockCh.connect(new InetSocketAddress(proxyHost,proxyPort));
            StreamChannelSocketAdaptor adapter = 
                new StreamChannelSocketAdaptor(_sockCh.socket());
            doConnect(host,port,adapter);

            SSLSocketFactory factory = trustManager.getSocketFactory();
            _socket = (SSLSocket)factory.createSocket(adapter, proxyHost,
                                                         proxyPort, true);
            ((SSLSocket)_socket).setEnabledCipherSuites(factory.getSupportedCipherSuites());
            try {
                ((SSLSocket)_socket).startHandshake();
            } catch(java.io.IOException e) {
                if (_certProvidedToClient && !_certAcceptedByClient) {
                    throw new org.netbeans.lib.collab.CertificateRejectedException();
                }
                throw e;
            }
             
            _socket.setSoTimeout(100);
            adapter.initWrapperSocket(_socket);
            streamSource = new ChannelStreamSource(new SocketByteChannel(_socket));
        }

       StreamSource delegated = streamSource;
       
       if (!usessl){
            delegated = createDelegateStreamSource(streamSource , host , port);
       }
        // ok good to go.
        _sockCh.configureBlocking(false);
        return delegated;
    }
    private  void doConnect(String host,int port, Socket socket)
                                                        throws Exception {
        StringBuffer request  = new StringBuffer("CONNECT ");
        request.append(host);
        request.append(":");
        request.append(port);
        request.append(" HTTP/1.0\r\n");
        if (username != null) {
            String sCreds = username + ":" + password;
            request.append("Proxy-Authorization: Basic ");
            request.append(base64.encode(sCreds.getBytes("US-ASCII")));
            request.append("\r\n");
        }
        request.append("\r\n");

        InputStream is  = socket.getInputStream();
        OutputStream os = socket.getOutputStream();

        os.write(request.toString().getBytes("US-ASCII"));
        os.flush();

        readHTTPHeader(is);
    }          
    /**
     *
     *
     */
    private static void readHTTPHeader(InputStream is)
        throws Exception {
        // The HTTP response generally looks like this:
        // HTTP/1.0 200 Connection established
        
        BufferedReader reader=new BufferedReader(
            new InputStreamReader(is,"US-ASCII"));
        
        String header=reader.readLine();
        
        // Skip the HTTP token
        int index=header.indexOf(" ");
        
        // Look at the first number of the response code to tell if this
        // is an success response.
        if (!header.startsWith("2",index+1))
            throw new IOException("Response from server: "+header);
        
        // Consume the rest of the header; it is terminated by a blank line
        while (header!=null) {
            //System.out.println(header);
            if (header.length()==0)
                break;
            
            header=reader.readLine();
        }
    }


    /**
     *
     *
     */
    private static void readContent(InputStream is)
        throws Exception
    {
        for (int i=is.read(); i!=-1; i=is.read()) {
            char c=(char)i;
            //System.out.print(c);
        }
    }
    
    protected Socket getSocket() {
        return _socket;
    }

    public boolean isTLSSupported(){
        return !usessl;
    }
}

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

import java.io.*;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.nio.channels.ByteChannel;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.jabberstudio.jso.Stream;
import org.jabberstudio.jso.io.src.ChannelStreamSource;
import org.jabberstudio.jso.io.StreamSource;
import org.jabberstudio.jso.io.src.SocketStreamSource;
import org.netbeans.lib.collab.CollaborationException;
import org.netbeans.lib.collab.CollaborationSessionListener;
import org.netbeans.lib.collab.SecureSessionListener;
import org.netbeans.lib.collab.util.*;

/**
 *
 *
 * @author Vijayakumar Palaniappan
 *
 */
public class SocketStreamSourceCreator implements StreamSourceCreator {

    /** Creates a new instance of StreamSourceCreator */
    //private ChannelStreamSource _css;
    private StreamSource _css;
    SocketChannel _sockCh;
    private CollaborationSessionListener _sessionListener;
    ByteChannel _byteCh;
    protected SelectWorker _selector;

    public SocketStreamSourceCreator(CollaborationSessionListener listener) {
        _sessionListener = listener;
    }

    public SocketStreamSourceCreator(CollaborationSessionListener listener,
			       SelectWorker selw) {
        _sessionListener = listener;
        _selector = selw;
    }

    public StreamSource createStreamSource(String hostName, int port)
                                                        throws Exception {
        // TODO : Modify code to return something which can be upgraded to tls
        // if required.
        _sockCh = SocketChannel.open();
        Socket soc = _sockCh.socket();
        int snd_size = XMPPSessionProvider.getSocketSendbufferSize();
        int rcv_size = XMPPSessionProvider.getSocketReceivebufferSize();
        if(rcv_size > 0) {
            soc.setReceiveBufferSize(rcv_size);
        }
        if(snd_size > 0) {
            soc.setSendBufferSize(snd_size);
        }
        _sockCh.connect(new InetSocketAddress(hostName, port));
	if (_selector == null) {
	    _byteCh = _sockCh;
	} else {
	    _byteCh = new BufferedByteChannel(_sockCh, _selector);
	}

	//_css = new ChannelStreamSource(_byteCh);
        _css = createDelegateStreamSource(new ChannelStreamSource(_byteCh) 
            , hostName , port);

        _sockCh.configureBlocking(false);
        return _css;
    }

    protected static boolean isDelegateStream(StreamSource stream){
        return stream instanceof DelegateStreamSource;
    }
    
    protected static void setDelegateStream(StreamSource stream , StreamSource newDelegateTo){
        if (isDelegateStream(stream)){
            ((DelegateStreamSource)stream).setDelegateStreamSource(newDelegateTo);
        }
    }
    
    protected StreamSource createDelegateStreamSource(StreamSource toDelegateTo ,
            String hostName, int port){
        DelegateStreamSource strm = new DelegateStreamSource(hostName , port);
        setDelegateStream(strm , toDelegateTo);
        return strm;
    }

    
    protected void setSocketChannel(SocketChannel sockCh) {
        _sockCh = sockCh;
    }

    public SocketChannel getSocketChannel() throws IOException {
        return _sockCh;
    }

    public ByteChannel getBufferedChannel() throws IOException {
        return (_byteCh != null) ? _byteCh : _sockCh;
    }

    public CollaborationSessionListener getSessionListener() {
        return _sessionListener;
    }

    protected Socket getSocket() {
	return _sockCh.socket();
    }
    
    public boolean isTLSSupported(){
        return true;
    }
    
    private boolean _certProvidedToClient = false;
    private boolean _certAcceptedByClient = false;

    protected boolean getCertProvidedToClient(){
        return _certProvidedToClient;
    }
    protected boolean getCertAcceptedByClient(){
        return _certAcceptedByClient;
    }
    protected void setCertProvidedToClient(boolean provided){
        _certProvidedToClient = provided;
    }
    protected void setCertAcceptedByClient(boolean accepted){
        _certAcceptedByClient = accepted;
    }

    // Not threaded.
    protected JavaxX509TrustManager getTrustManager() 
        throws NoSuchAlgorithmException, KeyStoreException,
            FileNotFoundException, KeyManagementException,
            IOException, CertificateException {
        
        return new JavaxX509TrustManager(
            new CertificateVerify(){
                public boolean doYouTrustCertificate(X509Certificate[] chain){
                    CollaborationSessionListener sListener = getSessionListener();
                    if(sListener instanceof SecureSessionListener){
                        setCertProvidedToClient(true);
                        boolean accepted = ((SecureSessionListener)sListener).
                                onX509Certificate(chain);
                        setCertAcceptedByClient(accepted);
                        return accepted;
                    }
                    return false;
                }
            });
    }

    public void upgradeToTLS(StreamSource stream) throws IOException , 
            CollaborationException , UnsupportedOperationException{
        if (isDelegateStream(stream)){
            XMPPSessionProvider.debug("upgradeToTLS (delegate) : " + stream);
            DelegateStreamSource delegate = (DelegateStreamSource)stream;

            Socket baseSocket = getSocket();
            getSocketChannel().configureBlocking(true);
            
            if (!getSocketChannel().isBlocking()){
                XMPPSessionProvider.debug("\n\n\nNOT BLOCKING !\n\n\n");
                //System.exit (-1);
                throw new CollaborationException("NOT Blocking");
            }

            JavaxX509TrustManager trustManager;
            try{
                trustManager = getTrustManager();
            }catch(Exception ex){
                throw new CollaborationException("Trustmanager creation exception" , ex);
            }
            StreamChannelSocketAdaptor adapter;
            adapter = new StreamChannelSocketAdaptor(baseSocket,getBufferedChannel());

            SSLSocketFactory factory = trustManager.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket)factory.createSocket(adapter, 
                    delegate.getHostName(), delegate.getPort() , true);
            sslSocket.setEnabledCipherSuites(factory.getSupportedCipherSuites());

            try {
                sslSocket.startHandshake();
            } catch(IOException e) {
                if(getCertProvidedToClient() && !getCertAcceptedByClient()) {
                    throw new org.netbeans.lib.collab.CertificateRejectedException();
                }
                throw e;
            } catch(Exception e1) {
                throw new CollaborationException("Exception in ssl handshake" , e1);
            }
            XMPPSessionProvider.debug("Suceeded !");
            sslSocket.setSoTimeout(100);
            
            adapter.initWrapperSocket(sslSocket);
            StreamSource newStream = 
                    new ChannelStreamSource(new SocketByteChannel(sslSocket));
            baseSocket.getChannel().configureBlocking(false);
            sslSocket.getChannel().configureBlocking(false);
            setDelegateStream(delegate, newStream);
            XMPPSessionProvider.debug("delegate done : " + (stream == _css));
        }
        else{
            XMPPSessionProvider.error("Unsupported stream : " + stream);
        }
    }
    /*
     * This class is for solving a specific problem and should not be used in a 
     * generic manner.
     * Theoretically , some of the methods of this class should be synchronized -
     * but the practical usecase of this method is within the first few stanza's of
     * the stream creation.
     * And at that time , only XMPPSession is accessing the StreamSource - and in a
     * non-MT manner : so I am avaoiding the expensive synchronized blocks.
     * If this assumption changes - need to redo this class.
     * Essentially a fragile class where I am trying to minimise the runtime
     * expense of doing a delegation to another stream source.
     *
     * The user of this class should be aware of what he is doing - I have not added
     * detailed docs on why this class is present.
     *
     * @author Mridul Muralidharan
     */
    private class DelegateStreamSource implements StreamSource {

        private StreamSource delegateSrc;
        private String hostName;
        private int port;

        /** Creates a new instance of DelegateStreamSource */
        public DelegateStreamSource(String hostName, int port) {
            this.hostName = hostName;
            this.port = port;
        }

        public String getHostName(){
            return hostName;
        }

        public int getPort(){
            return port;
        }

        public String getHostname(){
            return getDelegateStreamSource().getHostname();
        }

        public void connect(Stream s) throws IOException, Exception{
            getDelegateStreamSource().connect(s);
        }

        public void disconnect(Stream s) throws IOException, Exception{
            getDelegateStreamSource().disconnect(s);
        }

        public boolean ready() throws IOException{
            return getDelegateStreamSource().ready();
        }

        public int read(byte[] buffer, int offset, int length)
        throws IllegalArgumentException, IOException{

            return getDelegateStreamSource().read(buffer , offset , length);
        }

        public int write(byte[] buffer, int offset, int length)
        throws IllegalArgumentException, IOException{

            return getDelegateStreamSource().write(buffer, offset, length);
        }

        protected StreamSource getDelegateStreamSource(){
            return delegateSrc;
        }

        public void setDelegateStreamSource(StreamSource delegateSrc){
            this.delegateSrc = delegateSrc;
        }
    }
}

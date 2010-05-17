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
package org.netbeans.lib.collab.xmpp.httpbind.providers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import org.netbeans.lib.collab.CollaborationException;
import org.netbeans.lib.collab.CollaborationSessionListener;
import org.netbeans.lib.collab.SecureSessionListener;
import org.netbeans.lib.collab.util.CertificateVerify;
import org.netbeans.lib.collab.util.JavaxX509TrustManager;
import org.netbeans.lib.collab.xmpp.httpbind.ConnectionProvider;
import org.netbeans.lib.collab.xmpp.httpbind.HTTPSessionController;

/**
 *
 * @author Mridul Muralidharan
 */
public class HTTPSConnectionProvider extends ConnectionProviderImpl {
    private CollaborationSessionListener sessionListener = null;
    private SSLSocketFactory sslSocketFactory;
    private HostnameVerifier testVerifier = new TestHostnameVerifier();
    
    public static final int TRUSTED_CHAIN = 1;
    public static final int UNTRUSTED_CHAIN = 2;
    public static final int UNKNOWN_CHAIN = 4;
    
    private static final String SSL_CHAIN_MAP =
            "com.sun.im.service.xmpp.httpbind.providers.HTTPSConnectionProvider.certficatechains";
    
    public HttpURLConnection openConnection(URL destination)
    throws IOException{
        HttpsURLConnection connection = null;
        URLConnection _conn = destination.openConnection();
        connection = (HttpsURLConnection) _conn;
        connection.setSSLSocketFactory(getSSLSocketFactory());
        connection.setHostnameVerifier(testVerifier);
        setConnProperties(connection);
        return connection;
    }
    
    public ConnectionProvider createInstance(Map connParams)
    throws CollaborationException{
        try{
            HTTPSConnectionProvider conn = new HTTPSConnectionProvider();
            conn.setProperties(connParams);
            conn.initListener(connParams);
            conn.initProvider();
            return conn;
        }catch(Exception ex){
            throw new CollaborationException(ex);
        }
    }
    
    protected void initProvider() throws NoSuchAlgorithmException,
            KeyStoreException, FileNotFoundException,
            KeyManagementException, IOException, CertificateException{
        
        JavaxX509TrustManager trustManager = new JavaxX509TrustManager(
                new CertificateVerify(){
            public boolean doYouTrustCertificate(X509Certificate[] chain){
                if (HTTPSessionController.isDebugOn()){
                    HTTPSessionController.debug("Certificate chain : " + chain +
                            "listener : " + getSessionListener() + 
                            //", class : " + getSessionListener().getClass() + 
                        " , is valid listener : " + 
                            (getSessionListener() instanceof SecureSessionListener));
                }
                if(getSessionListener() instanceof SecureSessionListener){
                    int trust = getCertificateTrust(chain);
                    
                    // Remove this dependency later !
                    if (HTTPSessionController.isDebugOn()){
                        HTTPSessionController.debug("Trust value : " + trust);
                    }
                    
                    if (UNKNOWN_CHAIN == trust){
                        boolean callbackTrust =
                                ((SecureSessionListener)getSessionListener()).
                                onX509Certificate(chain);
                        trust = callbackTrust ?
                            TRUSTED_CHAIN : UNTRUSTED_CHAIN;
                        setCertificateTrust(chain , trust);
                    }
                    
                    return TRUSTED_CHAIN == trust;
                }
                return false;
            }
        } , "SSLv3");
        if (HTTPSessionController.isDebugOn()){
            HTTPSessionController.debug("trustManager : " + trustManager);
            HTTPSessionController.debug("SocketFactory : " +
                    trustManager.getSocketFactory());
        }
        setSSLSocketFactory(trustManager.getSocketFactory());
    }
    
    private void initListener(Map connParams){
        setSessionListener(
                (CollaborationSessionListener)
                connParams.get(SESSION_LISTENER));
    }
    
    protected CollaborationSessionListener getSessionListener(){
        return sessionListener;
    }
    
    protected void setSessionListener(
            CollaborationSessionListener sessionListener){
        this.sessionListener = sessionListener;
    }
    
    protected void setSSLSocketFactory(SSLSocketFactory sslSocketFactory){
        this.sslSocketFactory = sslSocketFactory;
    }
    
    protected SSLSocketFactory getSSLSocketFactory(){
        return sslSocketFactory;
    }
    
    
    private class CertificateChainHolder{
        private X509Certificate[] chain = null;
        private int hash = 0;
        public CertificateChainHolder(X509Certificate[] chain){
            this.chain = chain;
            hash = generateCheapHash();
        }
        
        public int hashCode(){
            return hash;
        }
        
        public boolean equals(Object obj){
            X509Certificate[] tchain = null;
            
            if (obj == this){
                return true;
            }
            
            if (null == obj ||
                    !(obj instanceof CertificateChainHolder)){
                return false;
            }
            
            tchain = ((CertificateChainHolder)obj).chain;
            if (tchain.length != chain.length){
                return false;
            }
            
            int count = 0;
            while (count < chain.length){
                if (chain[count] != tchain[count] &&
                        (!chain[count].equals(tchain[count]))){
                    return false;
                }
                count ++;
            }
            return true;
        }
        
        // This has been written in the assumption that the
        // hashCode is implemented in the X509Certificate impl
        private int generateCheapHash(){
            int count = 0;
            int retval = 0;
            
            while (count < chain.length){
                retval ^= chain[count].hashCode();
                count ++;
            }
            return retval;
        }
    }
    
    protected int getCertificateTrust(X509Certificate[] chain){
        Map props = getProperties();
        synchronized(props){
            // El Cheapo hash
            Map map = (Map)props.get(SSL_CHAIN_MAP);
            
            if (null != map){
                CertificateChainHolder holder = new
                        CertificateChainHolder(chain);
                Integer val = (Integer)map.get(holder);
                
                if (null != val){
                    return val.intValue();
                }
            }
        }
        
        return UNKNOWN_CHAIN;
    }
    
    protected void setCertificateTrust(X509Certificate[] chain , int trust){
        Map props = getProperties();
        synchronized(props){
            // El Cheapo hash
            Map map = (Map)props.get(SSL_CHAIN_MAP);
            
            if (null != map){
                CertificateChainHolder holder = new
                        CertificateChainHolder(chain);
                assert (!map.containsKey(holder));
                
                map.put(holder , new Integer(trust));
            } else{
                map = new HashMap();
                CertificateChainHolder holder = new
                        CertificateChainHolder(chain);
                map.put(holder , new Integer(trust));
                props.put(SSL_CHAIN_MAP , map);
            }
        }
        
        return ;
    }
    
    
    // When I was testing with http://nicp103.india.sun.com:8888/
    // The peerhost reported was the IP !
    // We should do something more intelligent here - a dns lookup
    // to verify cred is a bad idea IMHO.
    class TestHostnameVerifier implements HostnameVerifier{
        public boolean verify(String hostname, SSLSession session){
            
            if (HTTPSessionController.isDebugOn()){
                HTTPSessionController.debug("HostnameVerifier : host : " +
                    hostname + " , peerhost : " + session.getPeerHost());
            }
            
            int count = 0;
            String[] vals = session.getValueNames();
            
            while (count < vals.length){
                String val = vals[count];
                if (HTTPSessionController.isDebugOn()){
                    HTTPSessionController.debug("value : " + val + " , obj : " +
                            session.getValue(val));
                }
                count ++;
            }
            
            //return hostname.equalsIgnoreCase(session.getPeerHost());
            return true;
        }
    }
}

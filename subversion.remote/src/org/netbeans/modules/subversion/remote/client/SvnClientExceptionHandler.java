/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.remote.client;

import java.awt.Dialog;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JButton;
import org.netbeans.modules.proxy.Base64Encoder;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.SvnModuleConfig;
import org.netbeans.modules.subversion.remote.WorkingCopyAttributesCache;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.cli.CommandlineClient;
import org.netbeans.modules.subversion.remote.config.CertificateFile;
import org.netbeans.modules.subversion.remote.config.SvnConfigFiles;
import org.netbeans.modules.subversion.remote.ui.repository.Repository;
import org.netbeans.modules.subversion.remote.ui.repository.RepositoryConnection;
import org.netbeans.modules.subversion.remote.ui.wcadmin.UpgradeAction;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.KeyringSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.*;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NetworkSettings;
import org.openide.util.actions.SystemAction;

/**
 *
 * 
 */
public class SvnClientExceptionHandler {
    private final CommandlineClient adapter;
    private final SvnClient client;
    private final SvnClientDescriptor desc;    
    private final int handledExceptions;
    private static final String CHARSET_NAME = "ASCII7";                        // NOI18N
    
    private String methodName;
    
    private static class CertificateFailure {
        int mask;
        String error;
        String message;
        CertificateFailure(int mask, String error, String message) {
            this.mask = mask;
            this.error = error;
            this.message = message;
        }
    };
   
    private final CertificateFailure[] failures = new CertificateFailure[] {       
        new CertificateFailure (1, "certificate is not yet valid" ,                 NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_CertFailureNotYetValid")),       // NOI18N
        new CertificateFailure (2, "certificate has expired" ,                      NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_CertFailureHasExpired")),        // NOI18N
        new CertificateFailure (4, "certificate issued for a different hostname" ,  NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_CertFailureWrongHostname")),     // NOI18N
        new CertificateFailure (8, "issuer is not trusted" ,                        NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_CertFailureNotTrusted"))         // NOI18N
    };
    
    public final static int EX_UNKNOWN = 0;
    public final static int EX_ACTION_CANCELED_BY_USER = 2;
    public final static int EX_AUTHENTICATION = 4;
    public final static int EX_NO_CERTIFICATE = 8;
    public final static int EX_WRONG_URL = 16;
    public final static int EX_NO_HOST_CONNECTION = 32;
    public final static int EX_UNVERSIONED_RESOURCE = 64;
    public final static int EX_WRONG_URL_IN_REVISION = 128;
    public final static int EX_URL_NON_EXISTENT = 256;
    public final static int EX_HTTP_405 = 512;
    public final static int EX_IS_ALREADY_WC = 1024;
    public final static int EX_CLOSED_CONNECTION = 2048;
    public final static int EX_COMMIT_FAILED = 4096;
    public final static int EX_FILE_ALREADY_EXISTS = 8192;
    public final static int EX_IS_OUT_OF_DATE = 16384;            
    public final static int EX_NO_SVN_CLIENT = 32768;            
    public final static int EX_HTTP_FORBIDDEN = 65536;      
    public final static int EX_SSL_NEGOTIATION_FAILED = 131072;
          
  
    public final static int EX_HANDLED_EXCEPTIONS = EX_AUTHENTICATION | EX_NO_CERTIFICATE | EX_NO_HOST_CONNECTION | EX_SSL_NEGOTIATION_FAILED | EX_HTTP_FORBIDDEN;
    public final static int EX_DEFAULT_HANDLED_EXCEPTIONS = EX_HANDLED_EXCEPTIONS;
    
    private final SVNClientException exception;
    private final int exceptionMask;
            
    static final String ACTION_CANCELED_BY_USER = org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_ActionCanceledByUser");
    
    public SvnClientExceptionHandler(SVNClientException exception, CommandlineClient adapter, SvnClient client, SvnClientDescriptor desc, int handledExceptions) {
        this.exception = exception;                
        this.adapter = adapter;
        this.client = client;
        this.desc = desc;
        this.handledExceptions = handledExceptions;
        exceptionMask = getMask(exception.getMessage());
    }      

    public boolean handleException() throws SVNClientException {
        if(exceptionMask != EX_UNKNOWN) {
            if( (handledExceptions & exceptionMask & EX_NO_HOST_CONNECTION) == exceptionMask) {
                return handleRepositoryConnectError();
            } if( (handledExceptions & exceptionMask & EX_NO_CERTIFICATE) == exceptionMask) {                        
                return handleNoCertificateError();
            } if( (handledExceptions &  exceptionMask & EX_AUTHENTICATION) == exceptionMask) {
                return handleRepositoryConnectError();
            } if( (handledExceptions &  exceptionMask & EX_SSL_NEGOTIATION_FAILED) == exceptionMask) {
                return handleRepositoryConnectError();
            } if( (handledExceptions &  exceptionMask & EX_HTTP_FORBIDDEN) == exceptionMask) {
                return handleRepositoryConnectError();
            }
        }
        throw getException();
    }
       
    void setMethod (String methodName) {
        this.methodName = methodName;
    }

    private boolean handleRepositoryConnectError() throws SVNClientException {
        SVNUrl url = getRemoteHostUrl(); // try to get the repository url from the svnclientdescriptor
        if (url == null) {
            // god knows why this can happen
            return false;
        }

        if (Thread.interrupted()) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, "SvnClientExceptionHandler.handleRepositoryConnectError(): canceled"); //NOI18N
            }
            return false;
        }
        Repository repository = new Repository(adapter.getFileSystem(), Repository.FLAG_SHOW_PROXY, org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_Error_ConnectionParameters"));  // NOI18N
        repository.selectUrl(url, true);

        JButton retryButton = new JButton(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Action_Retry"));           // NOI18N
        String title = ((exceptionMask & EX_NO_HOST_CONNECTION) == exceptionMask) ?
                            org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_Error_CouldNotConnect") :
                            org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_Error_AuthFailed");
        Object option = repository.show(title, new HelpCtx(this.getClass()), new Object[] {retryButton, org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Action_Cancel")},    // NOI18N
                retryButton);

        boolean ret = (option == retryButton);
        if(ret) {
            RepositoryConnection rc = repository.getSelectedRC();
            String username = rc.getUsername();
            char[] password = rc.getPassword();

            adapter.setUsername(username);
            adapter.setPassword(password != null ? new String(password) : ""); //NOI18N
            SvnModuleConfig.getDefault(adapter.getFileSystem()).insertRecentUrl(rc);
        }
        return ret;
    }

    @NbBundle.Messages("CTL_Action_Cancel=Cancel")
    public boolean handleAuth (SVNUrl url) {
        Repository repository = new Repository(adapter.getFileSystem(), Repository.FLAG_SHOW_PROXY, org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_Error_ConnectionParameters"));  // NOI18N
        repository.selectUrl(url, true);

        JButton retryButton = new JButton(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Action_Retry"));           // NOI18N
        String title = org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_Error_AuthFailed");
        Object option = repository.show(
                title, 
                new HelpCtx("org.netbeans.modules.subversion.client.SvnClientExceptionHandler"), //NOI18N
                new Object[] { retryButton,
                    Bundle.CTL_Action_Cancel()
                }, retryButton);

        boolean ret = (option == retryButton);
        if(ret) {
            SvnModuleConfig.getDefault(adapter.getFileSystem()).insertRecentUrl(repository.getSelectedRC());
        }
        return ret;
    }

    private boolean handleNoCertificateError() throws SVNClientException {
        
        SVNUrl url = getSVNUrl(); // get the remote host url
        String realmString = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort(); // NOI18N
        String hostString = SvnUtils.ripUserFromHost(url.getHost());                                
        // copy the certificate if it already exists        
        VCSFileProxy certFile = CertificateFile.getSystemCertFile(adapter.getFileSystem(), realmString);
        try {
            if (SvnConfigFiles.COPY_CONFIG_FILES) {
                VCSFileProxy nbCertFile = CertificateFile.getNBCertFile(adapter.getFileSystem(), realmString);
                if( !nbCertFile.exists() &&  certFile.exists() ) {            
                    VCSFileProxySupport.copyFile(certFile, nbCertFile);
                    return true;
                }
            }
        } catch (IOException ex) {
            throw new SVNClientException(ex);
        }

        // otherwise try to retrieve the certificate from the server ...                                             
        SSLSocket socket;
        try {
            socket = getSSLSocket(hostString, url.getPort(), null, url);
        } catch (Exception e) {
            throw new SVNClientException(e);
        }
        if(socket == null) {
            return false;
        }

        X509Certificate cert = null;
        java.security.cert.Certificate[] serverCerts = null;
        try {
            serverCerts = socket.getSession().getPeerCertificates();
        } catch (SSLPeerUnverifiedException ex) {
            throw new SVNClientException(ex);
        }
        for (Certificate serverCert : serverCerts) {
            if (serverCert instanceof X509Certificate) {
                cert = (X509Certificate) serverCert;
                try {
                    cert.checkValidity();
                } catch (CertificateExpiredException ex) {
                    continue; // try to get the next one
                } catch (CertificateNotYetValidException ex) {
                    continue; // try to get the next one
                }
                break;
            }
        }

        AcceptCertificatePanel acceptCertificatePanel = new AcceptCertificatePanel();
        acceptCertificatePanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Error_CertFailed")); // NOI18N
        acceptCertificatePanel.certificatePane.setText(getCertMessage(cert, hostString));
        DialogDescriptor dialogDescriptor = new DialogDescriptor(acceptCertificatePanel, org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Error_CertFailed")); // NOI18N
        dialogDescriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.subversion.serverCertificateVerification"));
        JButton permanentlyButton = new JButton(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Cert_AcceptPermanently")); // NOI18N
        JButton temporarilyButton = new JButton(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Cert_AcceptTemp")); // NOI18N
        JButton rejectButton = new JButton(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Cert_Reject")); // NOI18N
        dialogDescriptor.setOptions(new Object[] {permanentlyButton, temporarilyButton, rejectButton}); 

        showDialog(dialogDescriptor);

        if(dialogDescriptor.getValue()!=permanentlyButton && dialogDescriptor.getValue()!=temporarilyButton) {                
            return false;
        }

        try {
            boolean temporarily = dialogDescriptor.getValue() == temporarilyButton;
            CertificateFile cf = new CertificateFile(adapter.getFileSystem(), cert, "https://" + hostString + ":" + url.getPort(), getFailuresMask(), temporarily); // NOI18N
            cf.store();
        } catch (CertificateEncodingException ex) {
            throw new SVNClientException(ex);
        } catch (IOException ex) {
            throw new SVNClientException(ex);
        }
            
        return true;                
    }

    private SVNUrl getSVNUrl() {
        String realmString = getRealmFromException(); 
        SVNUrl url = null; 
        if(realmString != null) {
            try {
                url = new SVNUrl(realmString);
            } catch (MalformedURLException e) {
                // something went wrong. 
                // ignore and try to fallback on the url from client
                Subversion.LOG.log(Level.INFO, e.getMessage(), e);                
            }    
        }                 
        if(url == null) {
            // fallback to the best we have
            url = client.getSvnUrl();
        }        
        return url;
    }

    private String getRealmFromException() {        
        String exceptionMessage = exception.getMessage().toLowerCase(Locale.ENGLISH);
        String[] errorMessages = new String[] {
            "host not found (",  //NOI18N
            "could not connect to server (",  //NOI18N
            "could not resolve hostname (",  //NOI18N
            "issuer is not trusted (", //NOI18N
            "authorization failed (" //NOI18N
        };        
        for(String errorMessage : errorMessages) {
            int idxL = exceptionMessage.indexOf(errorMessage);
            if(idxL < 0) {
                continue;
            }
            int idxR = exceptionMessage.indexOf(')', idxL + errorMessage.length()); //NOI18N
            if(idxR < 0) {
                continue;
            }
            return exceptionMessage.substring(idxL + errorMessage.length(), idxR);                                        
        }
        return null;
    }  
    
    private SSLSocket getSSLSocket(String host, int port, String[] protocols, SVNUrl url) throws Exception {
        TrustManager[] trust = new TrustManager[] {
            new X509TrustManager() {
            @Override
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
            @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
            }
        };
        
        URI uri = null;
        try {
            uri = new URI(url.toString());
        } catch (URISyntaxException ex) {
            Subversion.LOG.log(Level.INFO, null, ex);
        }
       
        String proxyHost = uri == null ? null : NetworkSettings.getProxyHost(uri);
        String proxyPort = uri == null ? null : NetworkSettings.getProxyPort(uri);

        // now this is the messy part ...
        Socket proxySocket = new Socket(java.net.Proxy.NO_PROXY);
        if(proxyHost == null || proxyHost.length() == 0) {                                           
            proxySocket.connect(new InetSocketAddress(host, port));
        } else {
            boolean directWorks = false;
            try {
                proxySocket.connect(new InetSocketAddress(host, port));
                directWorks = true;
            } catch (IOException e) {
                // do nothing
                if (Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.log(Level.FINE, null, e);
                }
            }
            if(!directWorks) {
                proxySocket = new Socket(java.net.Proxy.NO_PROXY); // reusing sockets seems to cause problems - see #138916
                proxySocket.connect(new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
                String username = NetworkSettings.getAuthenticationUsername(uri);
                String password = null;
                if (username != null) {
                    char[] pwd = KeyringSupport.read(NetworkSettings.getKeyForAuthenticationPassword(uri), null);
                    password = pwd == null ? "" : new String(pwd); //NOI18N
                }
                connectProxy(proxySocket, host, port, proxyHost, proxyPort, username, password);
            } 
        }
                        
        SSLContext context = SSLContext.getInstance("SSL");                     // NOI18N
        context.init(getKeyManagers(), trust, null);
        SSLSocketFactory factory = context.getSocketFactory();
        SSLSocket socket = (SSLSocket) factory.createSocket(proxySocket, host, port, true);
        if (protocols != null) {
            socket.setEnabledProtocols(protocols);
        }
        try {
            socket.startHandshake();
        } catch (SSLException ex) {
            if (protocols == null && isBadRecordMac(ex.getMessage())) {
                return getSSLSocket(host, port, new String[] {"SSLv3", "SSLv2Hello"}, url); //NOI18N
            } else {
                throw ex;
            }
        }
        return socket;
    }
        private KeyManager[] getKeyManagers() {        
        try {
            SVNUrl url = getRemoteHostUrl();
            RepositoryConnection rc = SvnModuleConfig.getDefault(adapter.getFileSystem()).getRepositoryConnection(url.toString());
            if(rc == null) {
                return null;
            }
            String certFile = rc.getCertFile();
            if(certFile.trim().isEmpty()) {
                return null;
            }               
            char[] certPasswordChars = rc.getCertPassword();
            
            KeyStore ks = KeyStore.getInstance("pkcs12");                                   // NOI18N            
            FileInputStream fis = new FileInputStream(certFile);
            try {
                ks.load(fis, certPasswordChars);
            } finally {
                fis.close();
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, certPasswordChars);
            return kmf.getKeyManagers();
            
        } catch(IOException ex) {
            Subversion.LOG.log(Level.SEVERE, null, ex);
        } catch (GeneralSecurityException ex) {
            Subversion.LOG.log(Level.SEVERE, null, ex);
        }                                       
        return null;
    }
        
    @org.netbeans.api.annotations.common.SuppressWarnings("Dm")
    private void connectProxy(Socket proxy, String host, int port, String proxyHost, String proxyPort, String userName, String password) throws IOException {
      StringBuilder sb = new StringBuilder("CONNECT ").append(host).append(":").append(port).append(" HTTP/1.0\r\n") //NOI18N
              .append("Connection: Keep-Alive\r\n");                    //NOI18N
      if (userName != null && password != null && userName.length() > 0) {
          Subversion.LOG.info("connectProxy: adding proxy authorization field"); //NOI18N
          sb.append("Proxy-Authorization: Basic ").append(Base64Encoder.encode((userName + ":" + password).getBytes("UTF-8"))).append("\r\n"); //NOI18N
      }
      String connectString = sb.append("\r\n").toString(); //NOI18N
      byte connectBytes[];
      try {
         connectBytes = connectString.getBytes(CHARSET_NAME);
      } catch (UnsupportedEncodingException ignored) {
         connectBytes = connectString.getBytes();
      }
      
      OutputStream out = proxy.getOutputStream();
      out.write(connectBytes);
      out.flush();

      byte reply[] = new byte[200];
      int replyLen = 0;
      int newlinesSeen = 0;
      boolean headerDone = false;
      InputStream in = proxy.getInputStream();
      
      while (newlinesSeen < 2) {
         byte b = (byte) in.read();
         if (b < 0) {
            throw new IOException("Unexpected EOF from proxy");                 // NOI18N
         }
         if (b == '\n') {
            headerDone = true;
            ++newlinesSeen;
         } else if (b != '\r') {
            newlinesSeen = 0;
            if (!headerDone && replyLen < reply.length) {
               reply[replyLen++] = b;
            }
         }
      }

      String ret;
      try {
        ret = new String(reply, 0, replyLen, CHARSET_NAME);
      } catch (UnsupportedEncodingException ignored) {
        ret = new String(reply, 0, replyLen);
      }
        if (!isOKresponse(ret.toLowerCase(Locale.ENGLISH))) {
            throw new IOException("Unable to connect through proxy "            // NOI18N
                                 + proxyHost + ":" + proxyPort                  // NOI18N
                                 + ".  Proxy returns \"" + ret + "\"");         // NOI18N
        }
    }    

    private boolean isOKresponse(String ret) {
        return ret.startsWith("http/1.1 200") || ret.startsWith("http/1.0 200");// NOI18N
    }    
        
    private SVNUrl getRemoteHostUrl() {
        SVNUrl url = desc != null ? desc.getSvnUrl() : null;
        if (url == null) {
            // huh ??? - try to fallback to the url given by the error msg.
            // unfortunatelly - this musn't be the repo url but only the the remote host url
            url = getSVNUrl();
        }
        return url;
    }

    private void showDialog(DialogDescriptor dialogDescriptor) {
        dialogDescriptor.setModal(true);
        dialogDescriptor.setValid(false);     

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.setVisible(true);
    }     

    private String getCertMessage(X509Certificate cert, String host) { 
        CertificateFailure[] certFailures = getCertFailures();
        Object[] param = new Object[7];
        param[0] = host;
        param[1] = cert.getSubjectDN().getName();
        param[2] = cert.getNotBefore();
        param[3] = cert.getNotAfter();
        param[4] = cert.getIssuerDN().getName();
        param[5] = getFingerprint(cert, "SHA1");      // NOI18N
        param[6] = getFingerprint(cert, "MD5");       // NOI18N

        String message = NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_BadCertificate", param); // NOI18N
        for (CertificateFailure certFailure : certFailures) {
            message = certFailure.message + message;
        }
        return message;
    }

    private CertificateFailure[] getCertFailures() {
        List<CertificateFailure> ret = new ArrayList<>();
        String exceptionMessage = getException().getMessage();
        for (CertificateFailure failure : failures) {
            if (exceptionMessage.indexOf(failure.error) > -1) {
                ret.add(failure);
            }
        }
        return ret.toArray(new CertificateFailure[ret.size()]);
    }
   
    private int getFailuresMask() {
        CertificateFailure[] certFailures = getCertFailures();
        if(certFailures.length == 0) {
            return 15; // something went wrong, 15 should work for everything
        }
        int mask = 0;
        for (CertificateFailure certFailure : certFailures) {
            mask |= certFailure.mask;
        }
        return mask;
    }
    
    private String getFingerprint(X509Certificate cert, String alg) {
        try {
            byte[] encoded = cert.getEncoded();            
            return Utils.getHash(alg, encoded);
        } catch (CertificateEncodingException ex) {
            Subversion.LOG.log(Level.INFO, ex.getMessage(), ex); // should not happen
        } catch (NoSuchAlgorithmException ex) {
            Subversion.LOG.log(Level.INFO, ex.getMessage(), ex); // should not happen
        }                       
        return ""; // NOI18N
    }
    
    private SVNClientException getException() {
        return exception;
    }

    private static int getMask(String msg) {
        if(msg == null || msg.trim().equals("")) {
            return EX_UNKNOWN;
        }
        msg = msg.toLowerCase(Locale.ENGLISH);
        if(isAuthentication(msg)) {         
            return EX_AUTHENTICATION;
        } else if (isCancelledAction(msg)) {
            return EX_ACTION_CANCELED_BY_USER;
        } else if (isNoCertificate(msg)) {
            return EX_NO_CERTIFICATE;
        } else if (isWrongUrl(msg)) {
            return EX_WRONG_URL;
        } else if (isNoHostConnection(msg)) {
            return EX_NO_HOST_CONNECTION;
        } else if(isUnversionedResource(msg)) {
            return EX_UNVERSIONED_RESOURCE;
        } else if(isWrongURLInRevision(msg)) {
            return EX_WRONG_URL_IN_REVISION;
        } else if(isHTTP405(msg)) { 
            return EX_HTTP_405;
        } else if(isAlreadyAWorkingCopy(msg)) {
            return EX_IS_ALREADY_WC;
        } else if(isClosedConnection(msg)) {
            return EX_CLOSED_CONNECTION;
        } else if(isCommitFailed(msg)) {
            return EX_COMMIT_FAILED;
        } else if(isNoCliSvnClient(msg)) {
            return EX_NO_SVN_CLIENT;
        } else if(isHTTP403(msg)) {
            return EX_HTTP_FORBIDDEN;
        } else if(isSSLNegotiation(msg)) {
            return EX_SSL_NEGOTIATION_FAILED;
        }
        return EX_UNKNOWN;
    }
    
    public static boolean isCancelledAction(String msg) {
        return msg.equals(ACTION_CANCELED_BY_USER);
    }

    static boolean isOperationCancelled(String message) {
        message = message.toLowerCase(Locale.ENGLISH);
        return message.indexOf("operation canceled") > -1 //NOI18N
                || message.contains("closedchannelexception"); //NOI18N - canceling a command while in svnkit/sqljet throws ClosedChannelException, no CanceledEx
    }

    public static boolean isAuthentication(String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        return msg.indexOf("authentication error from server: username not found") > - 1 || // NOI18N
               msg.indexOf("authorization failed") > - 1 ||                                 // NOI18N
               msg.indexOf("authentication failed") > - 1 ||                                // NOI18N
               msg.indexOf("authentication cancelled") > - 1 ||                             // NOI18N
               msg.indexOf("authentication error from server: password incorrect") > -1 ||  // NOI18N
               msg.indexOf("can't get password") > - 1 ||                                   // NOI18N
               msg.contains("user canceled dialog") ||                                      // NOI18N
               msg.contains("mkactivity request failed on") ||                              // NOI18N
               msg.contains("could not authenticate to server") ||                          // NOI18N
               msg.contains("unable to connect to a repository") && msg.contains("undefined tunnel scheme") || //NOI18N
               msg.indexOf("can't get username or password") > - 1;                         // NOI18N
    }

    public static boolean isNoCertificate(String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        return msg.indexOf("server certificate verification failed") > -1 // NOI18N
                || msg.contains("server ssl certificate untrusted") //NOI18N
                || msg.contains("server ssl certificate verification failed"); //NOI18N
    }
    
    public static boolean isWrongUrl(String msg) {
//      javahl: org.tigris.subversion.javahl.ClientException: Bad URL passed to RA layer
//      svn: URL 'file:///data/subversion/dilino' non-existent in revision 88
        msg = msg.toLowerCase(Locale.ENGLISH);
        return msg.indexOf("(not a valid url)") > - 1 ||                                      // NOI18N
               (msg.contains("svn:") && msg.contains("url") && msg.contains("non-existent in")) || //NOI18N
               (msg.indexOf("bad url passed to ra layer") > - 1 ); //NOI18N
    }

    private static boolean isNoHostConnection(String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        return msg.indexOf("host not found") > -1 ||                                        // NOI18N
               msg.indexOf("could not connect to server") > -1 ||                           // NOI18N
               msg.contains("cannot connect to") && msg.contains("there was a problem while connecting to") || // NOI18N
               msg.indexOf("could not resolve hostname") > -1;                              // NOI18N
    }
    
    public static boolean isUnversionedResource(String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        return msg.indexOf("(not a versioned resource)") > -1 ||                            // NOI18N
               msg.indexOf("is not a working copy") > -1 ||                                 //NOI18N
               msg.contains("some targets are not versioned") ||                            //NOI18N
               isNodeUnderVersionControl(msg) ||
               isNodeNotFound(msg);
    }
    
    public static boolean hasNoBaseRevision (String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        return msg.contains("has no base revision until it is committed"); //NOI18N
    }

    public static boolean isTooOldClientForWC(String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        return msg.indexOf("this client is too old") > -1;                                   // NOI18N
    }
    
    public static boolean isWrongURLInRevision(String msg) {        
        msg = msg.toLowerCase(Locale.ENGLISH);
        if (isNoSuchRevision(msg)) {
            return true;
        }
        int idx = msg.indexOf("unable to find repository location for");                    // NOI18N
        if(idx > -1 && msg.indexOf("in revision", idx + 23) > -1) {                         // NOI18N
            return true;
        }
        idx = msg.indexOf("url");                                                           // NOI18N
        return idx > -1 && (msg.indexOf("non-existent in that revision", idx + 3) > -1      //NOI18N
                || msg.indexOf("non-existent in revision", idx + 3) > -1);                  //NOI18N
    }    

    public static boolean isNoSuchRevision (String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        return msg.contains("no such revision"); //NOI18N
    }

    private static boolean isHTTP405(String msg) {
        return msg.indexOf("405") > -1;                                                     // NOI18N
    }

    public static boolean isHTTP403(String msg) {
        return msg.indexOf("403") > -1;                                                     // NOI18N
    }
    
    public static boolean isSSLNegotiation(String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        return msg.indexOf("ssl negotiation failed: ssl error: sslv3 alert handshake failure") > -1;                                                     // NOI18N
    }

    public static boolean isReportOf200(String msg) {  
        msg = msg.toLowerCase(Locale.ENGLISH);
        int idx = msg.indexOf("svn: report of");            // NOI18N
        if(idx < 0) {
            return false;
        }
        return msg.indexOf("200", idx + 13) > -1;           // NOI18N
    }
    
    public static boolean isSecureConnTruncated(String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        return msg.indexOf("could not read chunk size: secure connection truncated") > -1;  // NOI18N
    }        

    public static boolean isFileNotFoundInRevision(String msg) {

//      javahl:
//      Unable to find repository location for 'file:///data/subversion/JavaApplication31/nbproject/project.xml' in revision 87

//      cli:
//      svn: File not found: revision 87, path '/JavaApplication31/src/javaapplication31/Main.java'

        msg = msg.toLowerCase(Locale.ENGLISH);
        return msg.indexOf("file not found: revision") > -1 ||                                                  // NOI18N
              (msg.indexOf("unable to find repository location for") > -1 && msg.indexOf("in revision") > -1);  // NOI18N
    }
    
    public static boolean isPathNotFound(String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        return msg.indexOf("path not found") > -1;  // NOI18N
    }
        
    private static boolean isAlreadyAWorkingCopy(String msg) {   
        msg = msg.toLowerCase(Locale.ENGLISH);
        return msg.indexOf("is already a working copy for a different url") > -1;           // NOI18N
    }

    private static boolean isClosedConnection(String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        return msg.indexOf("could not read status line: an existing connection was forcibly closed by the remote host.") > -1; // NOI18N
    }

    private static boolean isCommitFailed(String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        return msg.indexOf("commit failed (details follow)") > -1;                          // NOI18N
    }

    public static boolean isFileAlreadyExists(String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        return msg.indexOf("file already exists") > -1 ||                                   // NOI18N
               (msg.indexOf("mkcol") > -1 && isHTTP405(msg));                               // NOI18N
    }
    
    private static boolean isOutOfDate(String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        return msg.indexOf("out of date") > -1 || msg.indexOf("out-of-date") > -1;                                             // NOI18N
    }
    
    public static boolean isNoCliSvnClient(String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        return (msg.indexOf("command line client adapter is not available") > -1) ||  //NOI18N
               (msg.indexOf(CommandlineClient.ERR_CLI_NOT_AVALABLE) > -1);
    }

    public static boolean isMissingOrLocked(String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        int idx = msg.indexOf("svn: working copy");                                         // NOI18N
        if(idx > -1) {
            return msg.indexOf("is missing or not locked", idx + 17) > -1;                  // NOI18N
        }
        idx = msg.indexOf("svn: directory");                                                // NOI18N
        if(idx > -1) {
            return msg.indexOf("is missing", idx + 13) > -1;                                // NOI18N
        }
        return false;
    }

    /**
     * Determines if the message is a result of an on-direcotory-called command.
     * @param msg error message
     * @return <code>true</code> if <code>msg</code> is a message returned from a command called on a directory, <code>false</code> otherwise.
     */
    public static boolean isTargetDirectory(String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        return (msg.indexOf("refers to a directory") > -1);                                         // NOI18N
    }

    /**
     * Is relocating to a wrong repository URL?
     * @param msg
     * @return
     */
    public static boolean isWrongUUID(String msg) {
        msg = msg.toLowerCase(Locale.ENGLISH);
        return (msg.contains("has uuid") && msg.contains("but the wc has")); //NOI18N
    }
    
    public static boolean isNotUnderVersionControl (String message) {
        message = message.toLowerCase(Locale.ENGLISH);
        return message.contains("is not under version control"); //NOI18N
    }

    public static boolean isNodeUnderVersionControl (String message) {
        message = message.toLowerCase(Locale.ENGLISH);
        return message.contains("is not under version control"); //NOI18N
    }
    
    public static boolean isNodeNotFound (String message) {
        message = message.toLowerCase(Locale.ENGLISH);
        return (message.contains(": the node") && message.contains("not found")); //NOI18N
    }
    
    public static boolean isPartOfNewerWC (String message) {
        message = message.toLowerCase(Locale.ENGLISH);
        return message.contains("this client is too old to work with the working copy"); //NOI18N
    }

    public static boolean isTooOldWorkingCopy (String message) {
        message = message.toLowerCase(Locale.ENGLISH);
        return message.contains("working copy") //NOI18N
                && message.contains("is too old") // NOI18N
                || message.contains("working copy needs to be upgraded"); //NOI18N
    }

    public static void notifyNullUrl(Context context) {
        Subversion.LOG.log(Level.FINE, "Unexpected null root URL for " + context); //NOI18N
    }

    public static void notifyException(Context context, Exception ex, boolean annotate, boolean isUI) {
        String message = ex.getMessage();
        if (isUI && isTooOldWorkingCopy(message)) {
            if (upgrade(context, message)) {
                return;
            }
        }
        if(isNoCliSvnClient(message)) {
            if(isUI) {
                if (context != null) {
                    notifyNoClient(context);
                }
            }
            return;
        }
        if(isCancelledAction(message)) {
            Subversion.LOG.log(Level.FINE, message, ex);
            return;
        }                   
        Subversion.LOG.log(Level.INFO, message, ex);
        if( annotate ) {
            String msg = getCustomizedMessage(ex);  
            if(msg == null) {
                if(ex instanceof SVNClientException) {
                    msg = parseExceptionMessage((SVNClientException) ex);
                } else {
                    msg = message;                        
                }                
            }        
            annotate(msg);
        }         
    }       

    public static boolean handleLogException(SVNUrl url, SVNRevision revision, SVNClientException e) {
        String protocol = url.getProtocol();
        if(  ( protocol.startsWith("https") && SvnClientExceptionHandler.isSecureConnTruncated(e.getMessage()) ) ||                    
             ( protocol.startsWith("http") && SvnClientExceptionHandler.isReportOf200(e.getMessage())              ) ||
             ( ( protocol.startsWith("file") || protocol.startsWith("svn+") ) && SvnClientExceptionHandler.isFileNotFoundInRevision(e.getMessage()) ) ) 
        {            
            Subversion.LOG.log(Level.INFO, e.getMessage(), e);    // keep track
            annotate(NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_ErrorFileNotFoundInRevision", new String[] {revision.toString()} )); // NOI18N                      
            return true;
        } 
        return false;
    }     
    
    private static void notifyNoClient(Context context) {
        MissingClient msc = new MissingClient(context);
        msc.show();
    }
    
    @Messages({
        "MSG_Error_TooOldWC=Working copy is too old for the currently used Subversion client.\n"
            + "You'll need to manually upgrade the working copy."
    })
    private static String getCustomizedMessage(Exception exception) {
        String exMsg = exception.getMessage();
        String msg = exMsg.toLowerCase(Locale.ENGLISH);
        if (isHTTP405(msg)) {
            msg = exMsg + "\n\n" + NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_Error405");                                // NOI18N
        } else if(isOutOfDate(msg) || isMissingOrLocked(msg)) {
            msg = exMsg + "\n\n" + org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_Error_OutOfDate") + "\n"; // NOI18N
        } else if(isWrongUUID(msg)) {
            msg = exMsg + "\n\n" + org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_Error_RelocateWrongUUID") + "\n"; // NOI18N
        } else if (isTooOldWorkingCopy(msg) && (msg.contains("svn upgrade") //NOI18N
                || msg.contains("working copy format of ") && msg.contains("is too old") //NOI18N
                || msg.contains("needs to be upgraded"))) { //NOI18N
            msg = Bundle.MSG_Error_TooOldWC() + "\n\n" + exMsg + "\n"; //NOI18N
        } else {
            msg = null;
        }
        return msg;
    }

    public static String parseExceptionMessage(SVNClientException ex) {
        String msg = ex.getMessage();
        msg = msg.replace("svn: warning: ", ""); //NOI18N
        msg = msg.replace("svn: ", ""); //NOI18N
        if (isTooOldClientForWC(msg)) {
            // add an additional message for old clients
            msg += NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_Error_OldClient");    // NOI18N
        }
        return msg;
    }

    public static void annotate(String msg) {        
        CommandReport report = new CommandReport(NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_SubversionCommandError"), msg);
        JButton ok = new JButton(NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_CommandReport_OK"));
        NotifyDescriptor descriptor = new NotifyDescriptor(
                report, 
                NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_CommandFailed_Title"), 
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.ERROR_MESSAGE,
                new Object [] { ok },
                ok);
        DialogDisplayer.getDefault().notify(descriptor);        
    }

    static void handleInvalidKeyException(InvalidKeyException ike) {
        Subversion.LOG.log(Level.INFO, ike.getMessage(), ike);
        String msg = NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_InvalidKeyException"); // NOI18N
        annotate(msg);
    }

    private boolean isBadRecordMac (String message) {
        message = message.toLowerCase(Locale.ENGLISH);
        return message.contains("received fatal alert")                 //NOI18N
                && message.contains("bad_record_mac");                  //NOI18N
    }

    private static boolean upgrade (Context context, final String exMessage) {
        boolean retval = false;
        final VCSFileProxy wc = findPathInTooOldWCMessage(context, exMessage);
        if (wc != null) {
            retval = Mutex.EVENT.readAccess(new Mutex.Action<Boolean>() {
                @Override
                public Boolean run () {
                    if (WorkingCopyAttributesCache.getInstance().logAskedToUpgrade(wc)) {
                        // displayed for the first time
                        SystemAction.get(UpgradeAction.class).upgrade(wc);
                        return true;
                    } else {
                        // do not bother user any more
                        return false;
                    }
                }
            });
        }
        return retval;
    }
    
    private static VCSFileProxy findPathInTooOldWCMessage (Context context, String message) {
        for (String s : new String[] { ".*Working copy \'([^\']+)\' is too old.*" }) { //NOI18N
            Pattern p = Pattern.compile(s, Pattern.DOTALL);
            Matcher m = p.matcher(message);
            if (m.matches()) {
                return VCSFileProxySupport.getResource(context.getFileSystem(), m.group(1));
            }
        }
        return null;
    }
}

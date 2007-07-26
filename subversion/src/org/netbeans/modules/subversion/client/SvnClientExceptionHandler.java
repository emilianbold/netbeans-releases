/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.client;

import java.awt.Dialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JButton;
import org.netbeans.modules.subversion.Diagnostics;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.config.CertificateFile;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.netbeans.modules.subversion.ui.repository.Repository;
import org.netbeans.modules.subversion.ui.repository.RepositoryConnection;
import org.netbeans.modules.subversion.util.FileUtils;
import org.netbeans.modules.subversion.util.ProxySettings;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class SvnClientExceptionHandler {
    
    private final ISVNClientAdapter adapter;
    private final SvnClient client;
    private final int handledExceptions;
    
    private static final String NEWLINE = System.getProperty("line.separator"); // NOI18N
    private static final String CHARSET_NAME = "ASCII7";                        // NOI18N
  
    private class CertificateFailure {
        int mask;
        String error;
        String message;
        CertificateFailure(int mask, String error, String message) {
            this.mask = mask;
            this.error = error;
            this.message = message;
        }
    };
   
    private CertificateFailure[] failures = new CertificateFailure[] {       
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
          
  
    public final static int EX_HANDLED_EXCEPTIONS = EX_AUTHENTICATION | EX_NO_CERTIFICATE | EX_NO_HOST_CONNECTION;
    public final static int EX_DEFAULT_HANDLED_EXCEPTIONS = EX_HANDLED_EXCEPTIONS;
    
    private final SVNClientException exception;
    private final int exceptionMask;
            
    static final String ACTION_CANCELED_BY_USER = org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_ActionCanceledByUser");
    
    public SvnClientExceptionHandler(SVNClientException exception, ISVNClientAdapter adapter, SvnClient client, int handledExceptions) {
        this.exception = exception;                
        this.adapter = adapter;
        this.client = client;
        this.handledExceptions = handledExceptions;
        exceptionMask = getMask(exception.getMessage());
    }      
    
    public boolean handleException() throws Exception {
        if(exceptionMask != EX_UNKNOWN) {
            if( (handledExceptions & exceptionMask & EX_NO_HOST_CONNECTION) == exceptionMask) {
                return handleRepositoryConnectError();
            } if( (handledExceptions & exceptionMask & EX_NO_CERTIFICATE) == exceptionMask) {                        
                return handleNoCertificateError();
            } if( (handledExceptions &  exceptionMask & EX_AUTHENTICATION) == exceptionMask) {
                return handleRepositoryConnectError();
            }
        }
        throw getException();
    }
         
    private boolean handleRepositoryConnectError() {        
        SVNUrl url = getSVNUrl();
        Repository repository = new Repository(Repository.FLAG_SHOW_PROXY, org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_Error_ConnectionParameters"));  // NOI18N
        repository.selectUrl(url, true);
        
        JButton retryButton = new JButton(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Action_Retry"));           // NOI18N        
        String title = ((exceptionMask & EX_NO_HOST_CONNECTION) == exceptionMask) ? 
                            org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_Error_CouldNotConnect") : 
                            org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_Error_AuthFailed");
        Object option = repository.show(title, new HelpCtx(this.getClass()), new Object[] {retryButton, org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Action_Cancel")});   // NOI18N
                

        boolean ret = (option == retryButton);
        if(ret) {
            RepositoryConnection rc = repository.getSelectedRC();
            String username = rc.getUsername();
            String password = rc.getPassword();

            adapter.setUsername(username);
            adapter.setPassword(password);                                        
            SvnModuleConfig.getDefault().insertRecentUrl(rc);
        }                 
        return ret;
    }

    private boolean handleNoCertificateError() throws Exception {
        
        SVNUrl url = getSVNUrl();
        String realmString = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort(); // NOI18N
        String hostString = SvnUtils.ripUserFromHost(url.getHost());                                
        
        // copy the certificate if it already exists        
        File certFile = CertificateFile.getSystemCertFile(realmString);
        File nbCertFile = CertificateFile.getNBCertFile(realmString);
        if( !nbCertFile.exists() &&  certFile.exists() ) {            
            FileUtils.copyFile(certFile, CertificateFile.getNBCertFile(realmString));            
            return true;
        }

        // otherwise try to retrieve the certificate from the server ...                                             
        SSLSocket socket;
        try {
            socket = getSSLSocket(hostString, url.getPort());
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
            return false;
        }
        if(socket == null) {
            return false;
        }

        X509Certificate cert = null;
        java.security.cert.Certificate[] serverCerts = null;
        try {
            serverCerts = socket.getSession().getPeerCertificates();
        } catch (SSLPeerUnverifiedException ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        }
        for (int i = 0; i < serverCerts.length; i++) {                        
            Diagnostics.println("Cert[" + i + "] type - " + serverCerts[i].getType());      // NOI18N
            if(serverCerts[i] instanceof X509Certificate) {                                
                cert = (X509Certificate) serverCerts[i];
                Diagnostics.println("Cert[" + i + "] - notAfter " + cert.getNotAfter());    // NOI18N
                Diagnostics.println("Cert[" + i + "] - notBefore " + cert.getNotBefore());  // NOI18N
                try {
                    cert.checkValidity();
                } catch (CertificateExpiredException ex) {
                    Diagnostics.println("Cert[" + i + "] - " + ex);                    // NOI18N                    
                    continue; // try to get the next one
                } catch (CertificateNotYetValidException ex) {
                    Diagnostics.println("Cert[" + i + "] - " + ex);                    // NOI18N
                    continue; // try to get the next one
                }
                break;
            }
        }

        AcceptCertificatePanel acceptCertificatePanel = new AcceptCertificatePanel();
        acceptCertificatePanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Error_CertFailed")); // NOI18N
        acceptCertificatePanel.certificatePane.setText(getCertMessage(cert, hostString));
        DialogDescriptor dialogDescriptor = new DialogDescriptor(acceptCertificatePanel, org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Error_CertFailed")); // NOI18N        
        JButton permanentlyButton = new JButton(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Cert_AcceptPermanently")); // NOI18N
        JButton temporarilyButton = new JButton(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Cert_AcceptTemp")); // NOI18N
        JButton rejectButton = new JButton(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Cert_Reject")); // NOI18N
        dialogDescriptor.setOptions(new Object[] {permanentlyButton, temporarilyButton, rejectButton}); 

        showDialog(dialogDescriptor);

        if(dialogDescriptor.getValue()!=permanentlyButton && dialogDescriptor.getValue()!=temporarilyButton) {                
            return false;
        }

        CertificateFile cf = null;
        try {
            boolean temporarily = dialogDescriptor.getValue() == temporarilyButton;
            cf = new CertificateFile(cert, "https://" + hostString + ":" + url.getPort(), getFailuresMask(), temporarily); // NOI18N
            cf.store();
        } catch (CertificateEncodingException ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
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
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }    
        }                 
        if(url == null) {
            // fallback to the best we have
            url = client.getSvnUrl();
        }        
        return url;
    }
    private String getRealmFromException() {        
        String exceptionMessage = exception.getMessage().toLowerCase();             
        String[] errorMessages = new String[] {"host not found (", "could not connect to server (", "could not resolve hostname (", "issuer is not trusted ("};        
        for(String errorMessage : errorMessages) {
            int idxL = exceptionMessage.indexOf(errorMessage);
            if(idxL < 0) {
                continue;
            }
            int idxR = exceptionMessage.indexOf(")", idxL + errorMessage.length());
            if(idxR < 0) {
                continue;
            }
            return exceptionMessage.substring(idxL + errorMessage.length(), idxR);                                        
        }
        return null;
    }  
    
    private SSLSocket getSSLSocket(String host, int port) throws Exception {
        TrustManager[] trust = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
            }
        };
       
        ProxySettings proxySettings = new ProxySettings();
        String proxyHost = proxySettings.getHttpsHost();
        int proxyPort = proxySettings.getHttpsPort();                                     
        if(proxyHost.equals("")) {
            proxyHost = proxySettings.getHttpHost();
            proxyPort = proxySettings.getHttpPort();
        }
        
        // now this is the messy part ...
        Socket proxySocket = new Socket(java.net.Proxy.NO_PROXY);
        if(proxySettings.isDirect()) {                                           
            proxySocket.connect(new InetSocketAddress(host, port));
        } else {
            boolean directWorks = false;
            try {
                proxySocket.connect(new InetSocketAddress(host, port));                
                directWorks = true;                
            } catch (Exception e) {
                // do nothing 
            }            
            if(!directWorks) {
                proxySocket.connect(new InetSocketAddress(proxyHost, proxyPort));           
                connectProxy(proxySocket, host, port, proxyHost, proxyPort);                       
            }            
        }
                        
        SSLContext context = SSLContext.getInstance("SSL");                     // NOI18N
                        
        context.init(getKeyManagers(host), trust, null);        
        SSLSocketFactory factory = context.getSocketFactory();                                         
        SSLSocket socket = (SSLSocket) factory.createSocket(proxySocket, host, port, true);
        socket.startHandshake();
        return socket;
    }
    
    private KeyManager[] getKeyManagers(String host) {        
        try {   
            String certFile = SvnConfigFiles.getInstance().getClientCertFile(host);
            if(certFile == null || certFile.trim().equals("")) {                            // NOI18N
                return null;
            }                                    
            String certPassword = SvnConfigFiles.getInstance().getClientCertPassword(host);
            char[] certPasswordChars = certPassword != null ? certPassword.toCharArray() : null;                        
            
            KeyStore ks = KeyStore.getInstance("pkcs12");                                   // NOI18N            
            ks.load(new FileInputStream(certFile), certPasswordChars);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, certPasswordChars);
            return kmf.getKeyManagers();
            
        } catch(Exception ex) {
            ErrorManager.getDefault().notify(ex);
            return null;
        }                                       
    }
    
    private void connectProxy(Socket proxy, String host, int port, String proxyHost, int proxyPort) throws IOException {
      
      String connectString = "CONNECT "+ host + ":" + port + " HTTP/1.0\r\n" + "Connection: Keep-Alive\r\n\r\n"; // NOI18N
        
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

      String ret = "";                                                          // NOI18N
      try {
        ret = new String(reply, 0, replyLen, CHARSET_NAME);
      } catch (UnsupportedEncodingException ignored) {
        ret = new String(reply, 0, replyLen);
      }
        if (!isOKresponse(ret.toLowerCase())) {
            throw new IOException("Unable to connect through proxy "            // NOI18N
                                 + proxyHost + ":" + proxyPort                  // NOI18N
                                 + ".  Proxy returns \"" + ret + "\"");         // NOI18N
        }
    }    
    
    private boolean isOKresponse(String ret) {
        return ret.startsWith("http/1.1 200") || ret.startsWith("http/1.0 200");// NOI18N
    }    
    
    private void showDialog(DialogDescriptor dialogDescriptor) {
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
        dialogDescriptor.setValid(false);     

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.setVisible(true);
    }     

    private String getCertMessage(X509Certificate cert, String host) { 
        CertificateFailure[] certFailures = getCertFailures();
        Object[] param = new Object[6];
        param[0] = host;
        param[1] = cert.getNotBefore();
        param[2] = cert.getNotAfter();
        param[3] = cert.getIssuerDN().getName();
        param[4] = getFingerprint(cert, "SHA1");      // NOI18N
        param[5] = getFingerprint(cert, "MD5");       // NOI18N

        String message = NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_BadCertificate", param); // NOI18N
        for (int i = 0; i < certFailures.length; i++) {
            message = certFailures[i].message + message;
        }
        return message;
    }

    private CertificateFailure[] getCertFailures() {
        List<CertificateFailure> ret = new ArrayList<CertificateFailure>();
        String exceptionMessage = getException().getMessage();
        for (int i = 0; i < failures.length; i++) {
            if(exceptionMessage.indexOf(failures[i].error) > -1) {
                ret.add(failures[i]);
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
        for (int i = 0; i < certFailures.length; i++) {
            mask |= certFailures[i].mask;
        }
        return mask;
    }
    
    private String getFingerprint(X509Certificate cert, String alg) {
        String str;
        try {
            str = new sun.misc.BASE64Encoder().encode(cert.getEncoded());            
            str = str.replace(NEWLINE, ""); // NOI18N
            MessageDigest md5 = MessageDigest.getInstance(alg);
            byte[] url = new sun.misc.BASE64Decoder().decodeBuffer(str);            
            md5.update(url);
            byte[] md5digest = md5.digest();            
            String ret = ""; // NOI18N
            for (int i = 0; i < md5digest.length; i++) {
                String hex = Integer.toHexString(md5digest[i] & 0x000000FF);
                if(hex.length()==1) {
                    hex = "0" + hex; // NOI18N
                }
                ret += hex + (i < md5digest.length - 1 ? ":"  : ""); // NOI18N
            }
            return ret;
        } catch (CertificateEncodingException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
        } catch (NoSuchAlgorithmException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
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
        msg = msg.toLowerCase();        
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
        } else if(isNoSvnClient(msg)) {               
            return EX_NO_SVN_CLIENT;
        } else if(isHTTP403(msg)) {
            return EX_HTTP_FORBIDDEN;
        }
        return EX_UNKNOWN;
    }
    
    private static boolean isCancelledAction(String msg) {
        return msg.equals(ACTION_CANCELED_BY_USER);
    }
    
    private static boolean isAuthentication(String msg) {        
        return msg.indexOf("authentication error from server: username not found") > - 1 || // NOI18N
               msg.indexOf("authorization failed") > - 1 ||                                 // NOI18N
               msg.indexOf("authentication error from server: password incorrect") > -1 ||  // NOI18N
               msg.indexOf("can't get password") > - 1;                                     // NOI18N
        // XXX we also have to check for authentication messages from proxy
    }

    private static boolean isNoCertificate(String msg) {
        return msg.indexOf("server certificate verification failed") > -1;                  // NOI18N
    }
    
    public static boolean isWrongUrl(String msg) {
        msg = msg.toLowerCase();
        return msg.indexOf("(not a valid url)") > - 1;                                      // NOI18N
    }

    private static boolean isNoHostConnection(String msg) {
        return msg.indexOf("host not found") > -1 ||                                        // NOI18N
               msg.indexOf("could not connect to server") > -1 ||                           // NOI18N
               msg.indexOf("could not resolve hostname") > -1;                              // NOI18N
    }
    
    public static boolean isUnversionedResource(String msg) {
        msg = msg.toLowerCase();
        return msg.indexOf("(not a versioned resource)") > -1 ||                            // NOI18N
               msg.indexOf("is not a working copy") > -1;                                   // NOI18N
    }
    
    public static boolean isWrongURLInRevision(String msg) {        
        msg = msg.toLowerCase();
        if (msg.indexOf("no such revision") > -1 ) {                                        // NOI18N
            return true;
        }
        int idx = msg.indexOf("unable to find repository location for");                    // NOI18N
        if(idx > -1 && msg.indexOf("in revision", idx + 23) > -1) {                         // NOI18N
            return true;
        }
        idx = msg.indexOf("url");                                                           // NOI18N
        return idx > -1 && msg.indexOf("non-existent in that revision", idx + 3) > -1;      // NOI18N        
    }    

    private static boolean isHTTP405(String msg) {
        return msg.indexOf("405") > -1;                                                     // NOI18N
    }

    public static boolean isHTTP403(String msg) {
        return msg.indexOf("403") > -1;                                                     // NOI18N
    }
    
    public static boolean isReportOf200(String msg) {  
        msg = msg.toLowerCase();
        int idx = msg.indexOf("svn: report of");            // NOI18N
        if(idx < 0) {
            return false;
        }
        return msg.indexOf("200", idx + 13) > -1;           // NOI18N
    }
    
    public static boolean isSecureConnTruncated(String msg) {
        msg = msg.toLowerCase();
        return msg.indexOf("could not read chunk size: secure connection truncated") > -1;  // NOI18N
    }        

    public static boolean isFileNotFoundInRevision(String msg) {
        msg = msg.toLowerCase();
        return msg.indexOf("file not found: revision") > -1;  // NOI18N
    }      
        
    private static boolean isAlreadyAWorkingCopy(String msg) {        
        return msg.indexOf("is already a working copy for a different url") > -1;           // NOI18N
    }

    private static boolean isClosedConnection(String msg) {
        return msg.indexOf("could not read status line: an existing connection was forcibly closed by the remote host.") > -1; // NOI18N
    }

    private static boolean isCommitFailed(String msg) {
        return msg.indexOf("commit failed (details follow)") > -1;                          // NOI18N
    }

    public static boolean isFileAlreadyExists(String msg) {
        msg = msg.toLowerCase();        
        return msg.indexOf("file already exists") > -1 ||                                   // NOI18N
               (msg.indexOf("mkcol") > -1 && isHTTP405(msg));                               // NOI18N
    }
    
    private static boolean isOutOfDate(String msg) {
        return msg.indexOf("out of date") > -1;                                             // NOI18N
    }
    
    private static boolean isNoSvnClient(String msg) {
        msg = msg.toLowerCase();
        return msg.equals("command line client adapter is not available");
    }

    public static boolean isMissingOrLocked(String msg) {  
        msg = msg.toLowerCase();       
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
    
    public static void notifyException(Exception ex, boolean annotate, boolean isUI) {
        if(isNoSvnClient(ex.getMessage())) {
            if(isUI) {
                notifyNoClient();
            }
            return;
        }
        if(isCancelledAction(ex.getMessage())) {
            cancelledAction();
            return;
        }                 
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        if( annotate ) {
            String msg = getCustomizedMessage(ex);
            if(msg == null) {
                if(ex instanceof SVNClientException) {
                    msg = parseExceptionMessage((SVNClientException) ex);    
                } else {
                    msg = ex.getMessage();                        
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
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);    // keep track
            annotate(NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_ErrorFileNotFoundInRevision", new String[] {revision.toString()} )); // NOI18N                      
            return true;
        } 
        return false;
    }     
    
    private static void notifyNoClient() {
        MissingSvnClient msc = new MissingSvnClient();
        msc.show();
    }
    
    private static String getCustomizedMessage(Exception exception) {
        String msg = null;
        if (isHTTP405(exception.getMessage())) {
            msg = exception.getMessage() + "\n\n" + NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_Error405");                                // NOI18N
        } else if(isOutOfDate(exception.getMessage()) || isMissingOrLocked(exception.getMessage())) {
            msg = exception.getMessage() + "\n\n" + org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_Error_OutOfDate") + "\n"; // NOI18N            
        } 
        return msg;
    }

    public static String parseExceptionMessage(SVNClientException ex) {
        String msg = ex.getMessage();
        int idx = msg.lastIndexOf("svn: "); // NOI18N
        if(idx > -1) {
            msg = msg.substring(idx + 5);
        }
        return msg;
    }

    private static void annotate(String msg) {        
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
    
    private static void cancelledAction() {
        JButton ok = new JButton(NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Action_OK")); // NOI18N
        NotifyDescriptor descriptor = new NotifyDescriptor(
                ACTION_CANCELED_BY_USER,
                NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_ActionCanceled_Title"), // NOI18N
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                new Object [] { ok },
                ok);
        DialogDisplayer.getDefault().notify(descriptor);
        return;
    }

    static void handleInvalidKeyException(InvalidKeyException ike) {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ike);
        String msg = NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_InvalidKeyException"); // NOI18N
        annotate(msg);
    }
        
}

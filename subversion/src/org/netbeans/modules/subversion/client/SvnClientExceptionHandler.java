/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.client;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javax.swing.JButton;
import org.netbeans.modules.proxy.ConnectivitySettings;
import org.netbeans.modules.subversion.config.CertificateFile;
import org.netbeans.modules.subversion.config.ProxyDescriptor;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.netbeans.modules.subversion.ui.repository.Repository;
import org.netbeans.modules.subversion.util.FileUtils;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
class SvnClientExceptionHandler extends ExceptionHandler {

    private final ISVNClientAdapter adapter;
    private final SvnClient client;
    private static final String NEWLINE = System.getProperty("line.separator"); // NOI18N
    private final String CHARSET_NAME = "ASCII7"; // NOI18N

    public SvnClientExceptionHandler(SVNClientException exception, ISVNClientAdapter adapter, SvnClient client) {
        super(exception);
        this.adapter = adapter;
        this.client = client;
    }  
    
    public boolean handleException() throws Exception {
        if(isAuthentication(getException())) {
            return handleAuthenticationError();
        } if(isNoCertificate(getException())) {
            return handleNoCertificateError();
        }

        throw getException();
    }

    private boolean handleAuthenticationError() {
        SVNUrl url = client.getSvnUrl();
        Repository repository = new Repository(url, false, false, org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_Error_ConnectionParameters")); // NOI18N
        CorrectAuthPanel corectPanel = new CorrectAuthPanel();
        corectPanel.panel.setLayout(new BorderLayout());
        corectPanel.panel.add(repository.getPanel(), BorderLayout.NORTH);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(corectPanel, org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_Error_AuthFailed")); // NOI18N

        JButton retryButton = new JButton(org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Action_Retry")); // NOI18N
        dialogDescriptor.setOptions(new Object[] {retryButton, org.openide.util.NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Action_Cancel")}); // NOI18N
        
        showDialog(dialogDescriptor);

        boolean ret = dialogDescriptor.getValue()==retryButton;
        if(ret) {
            adapter.setUsername(repository.getUserName());
            adapter.setPassword(repository.getPassword());

            repository.storeConfigValues();
        }        
        return ret;
    }

    // XXX refactor, move, clean up ...
    private boolean handleNoCertificateError() throws Exception {

        // copy the certificate if it already exists
        SVNUrl url = client.getSvnUrl();
        String hostString = SvnUtils.ripUserFromHost(url.getHost());
        String realmString = url.getProtocol() + "://" + hostString + ":" + url.getPort(); // NOI18N
        File certFile = CertificateFile.getSystemCertFile(realmString);
        if(certFile.exists()) {
            FileUtils.copyFile(certFile, CertificateFile.getNBCertFile(realmString));
            return true;
        }

        // otherwise try  to retrieve the certificate from the server ...
        TrustManager[] trust = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
            }
        };

        ProxyDescriptor proxyDescriptor = SvnConfigFiles.getInstance().getProxyDescriptor(hostString); 
        Socket proxy = null;
        if (proxyDescriptor != null && proxyDescriptor.getHost() != null ) { 
            ConnectivitySettings connectivitySettings = proxyDescriptor.toConnectivitySettings();
            try {
                proxy = new Socket(connectivitySettings.getProxyHost(), connectivitySettings.getProxyPort());
                connectProxy(proxy, hostString, url.getPort(), connectivitySettings.getProxyHost(), connectivitySettings.getProxyPort());
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
                return false;
            }            
        }

        SSLContext context = null;
        try {
            context = SSLContext.getInstance("SSL"); // NOI18N
            context.init(null, trust, null);
        } catch (NoSuchAlgorithmException ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        } catch (KeyManagementException ex) {
            ErrorManager.getDefault().notify(ex);
            return false;                    
        }

        SSLSocketFactory factory = context.getSocketFactory();                                         
        SSLSocket socket = null;
        try {
            if(proxy == null) {
                socket = (SSLSocket) (factory).createSocket(hostString, url.getPort());
            } else {
                socket = (SSLSocket) (factory).createSocket(proxy, hostString, url.getPort(), true);
            }
            socket.startHandshake();
        } catch (IOException ex) {
            throw ex;
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
            if(serverCerts[i] instanceof X509Certificate) {
                cert = (X509Certificate) serverCerts[i];
                break; 
            }
        }

        AcceptCertificatePanel acceptCertificatePanel = new AcceptCertificatePanel();
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
            cf = new CertificateFile(cert, url.getProtocol() + "://" + hostString + ":" + url.getPort(), 10, temporarily); // XXX how to get the value for failures  // NOI18N
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

    private void showDialog(DialogDescriptor dialogDescriptor) {
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
        dialogDescriptor.setValid(false);     

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.setVisible(true);
    }
    
    private void connectProxy(Socket tunnel, String host, int port, String proxyHost, int proxyPort) throws IOException {
      
      String connectString = "CONNECT "+ host + ":" + port + " HTTP/1.0\r\n" + "Connection: Keep-Alive\r\n\r\n"; // NOI18N
        
      byte connectBytes[];
      try {
         connectBytes = connectString.getBytes(CHARSET_NAME);
      } catch (UnsupportedEncodingException ignored) {
         connectBytes = connectString.getBytes();
      }
      
      OutputStream out = tunnel.getOutputStream();
      out.write(connectBytes);
      out.flush();

      byte reply[] = new byte[200];
      int replyLen = 0;
      int newlinesSeen = 0;
      boolean headerDone = false;
      InputStream in = tunnel.getInputStream();
      
      while (newlinesSeen < 2) {
         byte b = (byte) in.read();
         if (b < 0) {
            throw new IOException("Unexpected EOF from proxy"); // NOI18N
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

      String ret = ""; // NOI18N
      try {
        ret = new String(reply, 0, replyLen, CHARSET_NAME);
      } catch (UnsupportedEncodingException ignored) {
        ret = new String(reply, 0, replyLen);
      }      
      if(ret.toLowerCase().indexOf("200 connection established") == -1) { // NOI18N
         throw new IOException("Unable to connect through proxy " // NOI18N
                              + proxyHost + ":" + proxyPort // NOI18N
                              + ".  Proxy returns \"" + ret + "\""); // NOI18N
      }      
   }
    
   private String getCertMessage(X509Certificate cert, String host) {
       return NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_BadCertificate", new Object [] { // NOI18N
           host, cert.getNotBefore(), cert.getNotAfter(), cert.getIssuerDN().getName(),
           getFingerprint(cert, "SHA1"), getFingerprint(cert, "MD5") // NOI18N
       });
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
    
}

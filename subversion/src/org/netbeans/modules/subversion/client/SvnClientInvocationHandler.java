/*
 * SvnClientInvocationHandler.java
 *
 * Created on September 9, 2005, 12:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.subversion.client;

import java.awt.Dialog;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.util.*;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.settings.SVNCredentialFile;
import org.netbeans.modules.subversion.ui.repository.Repository;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.netbeans.modules.proxy.ConnectivitySettings;
import org.openide.ErrorManager;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 *
 * @author Tomas Stupka
 */
public class SvnClientInvocationHandler implements InvocationHandler {        

    private static Set remoteMethods = new HashSet();
    static {
        remoteMethods.add("checkout");  // NOI19N
        remoteMethods.add("commit"); // NOI19N
        remoteMethods.add("commitAcrossWC"); // NOI19N
        remoteMethods.add("getList"); // NOI19N
        remoteMethods.add("getDirEntry"); // NOI19N
        remoteMethods.add("copy"); // NOI19N
        remoteMethods.add("remove"); // NOI19N
        remoteMethods.add("doExport"); // NOI19N
        remoteMethods.add("doImport"); // NOI19N
        remoteMethods.add("mkdir"); // NOI19N
        remoteMethods.add("move"); // NOI19N
        remoteMethods.add("update"); // NOI19N
        remoteMethods.add("getLogMessages"); // NOI19N
        remoteMethods.add("getContent"); // NOI19N
        remoteMethods.add("setRevProperty"); // NOI19N
        remoteMethods.add("diff"); // NOI19N
        remoteMethods.add("annotate"); // NOI19N
        remoteMethods.add("getInfo"); // NOI19N
        remoteMethods.add("switchToUrl"); // NOI19N
        remoteMethods.add("merge"); // NOI19N
        remoteMethods.add("lock"); // NOI19N
        remoteMethods.add("unlock"); // NOI19N
    }

    private final ISVNClientAdapter adapter;
    
    /**
     *
     */
    public SvnClientInvocationHandler (ISVNClientAdapter adapter) {
        this.adapter = adapter;
    }
    
    /**
     * @see InvocationHandler#invoke(Object proxy, Method method, Object[] args)
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {               

        assert noRemoteCallinAWT(method, args) : "noRemoteCallinAWT(): " + method.getName();

        try {             
            return getAdapterMethod(method).invoke(adapter, args);                                             
        } catch (Exception e) {
            try {
                if(handleException((SvnClient) proxy, e) ) {
                    return getAdapterMethod(method).invoke(adapter, args); 
                } else {
                    // XXX some action canceled by user message ... wrap the exception ???
                    throw new SVNClientException("Action canceled by user"); // XXX wrap me
                }
            } catch (Exception ex) {
                throw ex;
            }
        }
    }

    private Method getAdapterMethod(Method proxyMethod) throws NoSuchMethodException  {
        Class[] parameters = proxyMethod.getParameterTypes();        
        return adapter.getClass().getMethod(proxyMethod.getName(), parameters);                
    }
    
    /**
     * @return false for methods that perform calls over network
     */
    private static boolean noRemoteCallinAWT(Method method, Object[] args) {

        if(!SwingUtilities.isEventDispatchThread()) {
            return true;
        }

        String name = method.getName();
        if (remoteMethods.contains(name)) {
            return false;
        } else if ("getStatus".equals(name)) { // NOI18N
            return args.length != 4 || (Boolean.TRUE.equals(args[3]) == false);
        }

        return true;
    }

    private boolean handleException(SvnClient client, Throwable t) throws Throwable {
        SVNClientException svnException = null;
        if( t instanceof InvocationTargetException ) {
            t = ((InvocationTargetException) t).getCause();            
        } 
        if( !(t instanceof SVNClientException) ) {
            throw t;
        }
        
        ExceptionInformation ei = new ExceptionInformation((SVNClientException) t);
        if(ei.isAuthentication()) {
            return handleAuthenticationError();
        } if(ei.isNoCertificate()) {
            return handleNoCertificateError(client);
        }

        // no handling for this exception -> throw it, so the caller may decide what to do...
        throw t;
    }

    private boolean handleAuthenticationError() {
        Repository repository = new Repository(false, false, "Correct the password, username and proxy settings for ths URL:"); 
        DialogDescriptor dialogDescriptor = new DialogDescriptor(repository.getPanel(), "Authentication failed"); 

        JButton retryButton = new JButton("Retry"); 
        dialogDescriptor.setOptions(new Object[] {retryButton, "Cancel"}); 
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
        dialogDescriptor.setValid(false);     
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.setVisible(true);
        boolean ret = dialogDescriptor.getValue()==retryButton;
        if(ret) {
            adapter.setUsername(repository.getUserName());
            adapter.setPassword(repository.getPassword());

            // XXX here should be handled the proxy setting - the whole thing is a mess, its a wonder it works at all ...
        }        
        return ret;
    }        
    
    // XXX refactor, move, clean up ...
    private boolean handleNoCertificateError(SvnClient client) throws Exception {        
        try {
            TrustManager[] trust = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };
                        
            SVNUrl url = Subversion.getInstance().getUrl(client);               
            ProxyDescriptor proxyDescriptor = Subversion.getInstance().getProxyDescriptor(client); // XXX don't like the way how the descriptor is get
            Socket proxy = null;
            if (proxyDescriptor != null && proxyDescriptor.needsProxy(url.getHost())) {
                ConnectivitySettings connectivitySettings = toConnectivitySettings(proxyDescriptor);
                proxy = new Socket(connectivitySettings.getProxyHost(), connectivitySettings.getProxyPort());
                connectProxy(proxy, url.getHost(), url.getPort(), connectivitySettings.getProxyHost(), connectivitySettings.getProxyPort());
            }
                                 
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, trust, null);
            
            SSLSocketFactory factory = (SSLSocketFactory) context.getSocketFactory();                                         
            SSLSocket socket = null;
            if(proxy == null) {
                socket = (SSLSocket) ((SSLSocketFactory) factory).createSocket(url.getHost(), url.getPort());                
            } else {
                socket = (SSLSocket) ((SSLSocketFactory) factory).createSocket(proxy, url.getHost(), url.getPort(), true);                
            }            
            socket.startHandshake();

            X509Certificate cert = null;
            java.security.cert.Certificate[] serverCerts = socket.getSession().getPeerCertificates();
            for (int i = 0; i < serverCerts.length; i++) {
                if(serverCerts[i] instanceof X509Certificate) {
                    cert = (X509Certificate) serverCerts[i];
                    break; 
                }
            }

            AcceptCertificatePanel acceptCertificatePanel = new AcceptCertificatePanel();
            acceptCertificatePanel.certificatePane.setText(getCertMessage(cert, url.getHost()));
            DialogDescriptor dialogDescriptor = new DialogDescriptor(acceptCertificatePanel, "Server certificate verification failed"); 

            JButton permanentlyButton = new JButton("Accept permanently"); 
            JButton temporarilyButton = new JButton("Accept temporarily"); 
            JButton rejectButton = new JButton("Reject"); 
            dialogDescriptor.setOptions(new Object[] {permanentlyButton, temporarilyButton, rejectButton}); 

            dialogDescriptor.setModal(true);
            dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
            dialogDescriptor.setValid(false);     

            Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
            dialog.setVisible(true);
                                                                    
            if(dialogDescriptor.getValue()!=permanentlyButton && dialogDescriptor.getValue()!=temporarilyButton) {                
                return false;
            }
            
            SVNCredentialFile.CertificateFile cf = new SVNCredentialFile.CertificateFile(url.getProtocol() + "://" + url.getHost() + ":" + url.getPort());
            String encodedCert = new sun.misc.BASE64Encoder().encode(cert.getEncoded());                
            encodedCert.replaceAll("\\n", ""); // XXX where does this come from ????!!!
            String[] str = encodedCert.split("\n"); // XXX whats wrong with replace ???
            encodedCert = "";
            for (int i = 0; i < str.length; i++) {
                encodedCert+=str[i];
            }
            cf.setCert(encodedCert);
            cf.setFailures("10"); // XXX what is this !!??            
            if(dialogDescriptor.getValue() == temporarilyButton) {
                cf.deleteOnExit();
            }
            cf.store();
            return true;
            
        } catch (Exception ex) {
            throw ex;
        }
    }

    // XXX move. maybe into client?
    private static ConnectivitySettings toConnectivitySettings(ProxyDescriptor pd) {
        ConnectivitySettings cs = new ConnectivitySettings();
        String pasword = pd.getPassword();
        int port = pd.getPort();
        switch (pd.getType()) {
            case ProxyDescriptor.TYPE_DIRECT:
                break;
            case ProxyDescriptor.TYPE_HTTP:
                if (port <= 0) {
                    ErrorManager.getDefault().log("Assuming default port 8080 for " + pd.getHost() + " HTTP proxy.");  // NOI18N
                    port = 8080;  // could be also 3127, 80, anyway user can specify exact value
                }
                cs.setProxy(ConnectivitySettings.CONNECTION_VIA_HTTPS, pd.getHost(), port, pd.getUserName(), pasword == null ? null : pasword.toCharArray());
                break;
            case ProxyDescriptor.TYPE_SOCKS:
                if (port <= 0) {
                    ErrorManager.getDefault().log("Assuming default port 1080 for " + pd.getHost() + " SOCKS proxy.");  // NOI18N
                    port = 1080;
                }
                cs.setProxy(ConnectivitySettings.CONNECTION_VIA_SOCKS, pd.getHost(), port, pd.getUserName(), pasword == null ? null : pasword.toCharArray());
                break;
            default:
                break;
        }
        return cs;
    }

    private final String CHARSET_NAME = "ASCII7";
    private void connectProxy(Socket tunnel, String host, int port, String proxyHost, int proxyPort) throws IOException {
      
      String connectString = "CONNECT "+ host + ":" + port + " HTTP/1.0\r\n" + "Connection: Keep-Alive\r\n\r\n";
        
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
            throw new IOException("Unexpected EOF from proxy");
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

      String ret = "";
      try {
        ret = new String(reply, 0, replyLen, CHARSET_NAME);
      } catch (UnsupportedEncodingException ignored) {
        ret = new String(reply, 0, replyLen);
      }      
      if(ret.toLowerCase().indexOf("200 connection established") == -1) {
         throw new IOException("Unable to connect through proxy "
                              + proxyHost + ":" + proxyPort
                              + ".  Proxy returns \"" + ret + "\"");
      }      
   }
    
   private String getCertMessage(X509Certificate cert, String host) {
        return       " - The certificate is not issued by a trusted authority. Use the fingerprint to validate the certificate manually!\n" +
                     " - The certificate has expired.\n" +
                     "Certificate information:\n" + 
                     " - Hostname: " + host + "\n" + 
                     " - Valid: from " + cert.getNotBefore() + " until " + cert.getNotAfter() + "\n" +
                     " - Issuer: " + cert.getIssuerDN().getName() + "\n" + 
                     " - Fingerprint : \n" + 
                     "    - SHA1: " + getFingerprint(cert, "SHA1") + " \n" +
                     "    - MD5:  " + getFingerprint(cert, "MD5") + " \n";
   } 

    private String getFingerprint(X509Certificate cert, String alg) {
        String str;
        try {
            str = new sun.misc.BASE64Encoder().encode(cert.getEncoded());            
            String[] strA = str.split("\n"); // XXX this againn:)!
            str = "";
            for (int i = 0; i < strA.length; i++) {
                str+=strA[i];
            }
            MessageDigest md5;           
            md5 = MessageDigest.getInstance(alg);
            byte[] url;
            url = new sun.misc.BASE64Decoder().decodeBuffer(str);            
            md5.update(url);
            byte[] md5digest = md5.digest();            
            String ret = "";
            for (int i = 0; i < md5digest.length; i++) {
                String hex = Integer.toHexString(md5digest[i] & 0x000000FF);
                if(hex.length()==1) {
                    hex = "0" + hex;
                }
                ret += hex + (i < md5digest.length - 1 ? ":"  : "");
            }
            return ret;
        } catch (CertificateEncodingException ex) {
            ex.printStackTrace(); // should not happen
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace(); // should not happen
        } catch (IOException ex) {
            ex.printStackTrace(); // should not happen
        }                       
        return "";
    }
     
}

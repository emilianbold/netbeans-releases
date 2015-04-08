/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.weblogic9;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.weblogic.common.api.WebLogicConfiguration;
import org.netbeans.modules.weblogic.common.spi.WebLogicTrustHandler;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Petr Hejl
 */
@ServiceProvider(service = WebLogicTrustHandler.class)
public class WLTrustHandler implements WebLogicTrustHandler {

    private static final Logger LOGGER = Logger.getLogger(WLTrustHandler.class.getName());

    private static final RequestProcessor TRUST_MANAGER_ACCESS = new RequestProcessor(WLTrustHandler.class);

    private static final int CHECK_TIMEOUT = 5000;

    private static final String TRUST_STORE_PATH = "J2EE/TrustStores/wlstruststore.jks"; // NOI18N

    private static final String TRUST_EXCEPTION_PROPERTY = "trustException"; // NOI18N

    private static final String TRUST_PASSWORD_KEY = "nb_weblogic_truststore"; // NOI18N

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public TrustManager getTrustManager(WebLogicConfiguration config) throws GeneralSecurityException {
        return new DelegatingTrustManager(config);
    }

    @Override
    public Map<String, String> getTrustProperties(WebLogicConfiguration config) {
        try {
            setup(config);
        } catch (GeneralSecurityException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }

        final InstanceProperties ip = InstanceProperties.getInstanceProperties(WLDeploymentFactory.getUrl(config));
        boolean trustException = Boolean.parseBoolean(ip.getProperty(TRUST_EXCEPTION_PROPERTY));
        if (!trustException) {
            return Collections.emptyMap();
        }
        FileObject fo = FileUtil.getConfigFile(TRUST_STORE_PATH);
        if (fo == null) {
            return Collections.emptyMap();
        }
        File file = FileUtil.toFile(fo);
        if (file == null) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new HashMap<String, String>();
        result.put("weblogic.security.TrustKeyStore", "CustomTrust"); // NOI18N
        result.put("weblogic.security.CustomTrustKeyStoreType", "JKS"); // NOI18N
        result.put("weblogic.security.CustomTrustKeyStoreFileName", file.getAbsolutePath()); // NOI18N
        result.put("weblogic.security.SSL.ignoreHostnameVerification", "true"); // NOI18N
        return result;
    }

    private void setup(WebLogicConfiguration config) throws GeneralSecurityException, IOException {
        SSLContext context = SSLContext.getInstance("TLS"); // NOI18N
        context.init(null, new TrustManager[]{getTrustManager(config)}, RANDOM);
        SSLSocket socket = (SSLSocket) context.getSocketFactory().createSocket();
        try {
            // we just trigger the trust manager here
            socket.connect(new InetSocketAddress(config.getHost(), config.getPort()), CHECK_TIMEOUT); // NOI18N
            socket.setSoTimeout(CHECK_TIMEOUT);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true); // NOI18N
            try {
                out.println("GET / HTTP/1.1\nHost:\n"); // NOI18N
            } finally {
                out.close();
            }
        } finally {
            socket.close();
        }
    }

    private static class DelegatingTrustManager implements X509TrustManager {

        private final WebLogicConfiguration config;

        /*
         * The default PKIX X509TrustManager9.  We'll delegate
         * decisions to it, and fall back to the logic in this class if the
         * default X509TrustManager doesn't trust it.
         */
        private X509TrustManager pkixTrustManager;

        DelegatingTrustManager(WebLogicConfiguration config) throws NoSuchAlgorithmException, KeyStoreException {
            this.config = config;
            // create a "default" JSSE X509TrustManager.

            TrustManagerFactory tmf
                    = TrustManagerFactory.getInstance("PKIX"); // NOI18N
            tmf.init((KeyStore) null);

            TrustManager[] tms = tmf.getTrustManagers();

            /*
             * Iterate over the returned trustmanagers, look
             * for an instance of X509TrustManager.  If found,
             * use that as our "default" trust manager.
             */
            for (int i = 0; i < tms.length; i++) {
                if (tms[i] instanceof X509TrustManager) {
                    pkixTrustManager = (X509TrustManager) tms[i];
                    break;
                }
            }
        }

        /*
         * Delegate to the default trust manager.
         */
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            throw new CertificateException("TrustManager does not trust any client");
        }

        @NbBundle.Messages("MSG_NotTrusted=The server certificate is not trusted. Add as an exception and proceed anyway?")
        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType)
                throws CertificateException {
            try {
                pkixTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException excep) {
                final String url = WLDeploymentFactory.getUrl(config);
                final InstanceProperties ip = InstanceProperties.getInstanceProperties(url);

                Future<Boolean> task = TRUST_MANAGER_ACCESS.submit(new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws GeneralSecurityException, IOException {
                        boolean trustException = Boolean.parseBoolean(ip.getProperty(TRUST_EXCEPTION_PROPERTY));
                        if (trustException) {
                            FileObject fo = FileUtil.getConfigFile(TRUST_STORE_PATH);
                            if (fo != null) {
                                try {
                                    X509TrustManager m = createTrustManager(fo);
                                    try {
                                        m.checkServerTrusted(chain, authType);
                                        return true;
                                    } catch (CertificateException ex) {
                                        LOGGER.log(Level.FINE, null, ex);
                                    }
                                } catch (GeneralSecurityException ex) {
                                    // proceed to dialog
                                    LOGGER.log(Level.INFO, null, ex);
                                } catch (IOException ex) {
                                    // proceed to dialog
                                    LOGGER.log(Level.INFO, null, ex);
                                }
                            }
                        }

                        NotifyDescriptor notDesc = new NotifyDescriptor.Confirmation(Bundle.MSG_NotTrusted(),
                                NotifyDescriptor.YES_NO_OPTION);
                        X509Certificate[] sorted = sortChain(chain);
                        Object result = DialogDisplayer.getDefault().notify(notDesc);
                        if (result == NotifyDescriptor.YES_OPTION) {
                            addToTrustStore(url, sorted[sorted.length - 1]);
                            ip.setProperty(TRUST_EXCEPTION_PROPERTY, Boolean.TRUE.toString());
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                try {
                    if (!task.get()) {
                        throw excep;
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    throw new CertificateException(ex);
                }
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return pkixTrustManager.getAcceptedIssuers();
        }
    }

    private static X509Certificate[] sortChain(X509Certificate[] certificates) {
        if ((certificates != null) && (certificates.length == 1)) {
            return certificates;
        } else {
            List<X509Certificate> certs = new ArrayList<X509Certificate>();
            certs.addAll(Arrays.asList(certificates));
            X509Certificate certChain = certs.get(0);
            certs.remove(certChain);
            LinkedList<X509Certificate> chainList = new LinkedList<X509Certificate>();
            chainList.add(certChain);
            Principal certIssuer = certChain.getIssuerDN();
            Principal certSubject = certChain.getSubjectDN();
            while (!certs.isEmpty()) {
                List<X509Certificate> tempcerts = new ArrayList<X509Certificate>();
                tempcerts.addAll(certs);
                for (X509Certificate cert : tempcerts) {
                    if (cert.getIssuerDN().equals(certSubject)) {
                        chainList.addFirst(cert);
                        certSubject = cert.getSubjectDN();
                        certs.remove(cert);
                        continue;
                    }

                    if (cert.getSubjectDN().equals(certIssuer)) {
                        chainList.addLast(cert);
                        certIssuer = cert.getIssuerDN();
                        certs.remove(cert);
                    }
                }
            }
            return chainList.toArray(new X509Certificate[chainList.size()]);
        }
    }

    private static synchronized void addToTrustStore(String url, X509Certificate cert) throws GeneralSecurityException, IOException {
        FileObject root = FileUtil.getConfigRoot();
        FileObject ts = root.getFileObject(TRUST_STORE_PATH);

        char[] password = Keyring.read(TRUST_PASSWORD_KEY);
        if (password == null) {
            password = new BigInteger(130, RANDOM).toString(32).toCharArray();
            Keyring.save(TRUST_PASSWORD_KEY, password, null);
        }

        KeyStore keystore = KeyStore.getInstance("JKS"); // NOI18N
        InputStream is = (ts == null) ? null : new BufferedInputStream(ts.getInputStream());
        try {
            keystore.load(is, password);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            // start from scratch
            keystore.load(null, null);
        } finally {
            if (is != null) {
                is.close();
            }
        }

        keystore.setCertificateEntry(url, cert); // NOI18N

        if (ts == null) {
            ts = FileUtil.createData(root, TRUST_STORE_PATH);
        }

        OutputStream out = new BufferedOutputStream(ts.getOutputStream());
        try {
            keystore.store(out, password);
        } finally {
            out.close();
        }
    }

    private static synchronized X509TrustManager createTrustManager(FileObject fo) throws GeneralSecurityException, IOException {
        KeyStore ts = KeyStore.getInstance("JKS"); // NOI18N
        InputStream in = new BufferedInputStream(fo.getInputStream());
        try {
            ts.load(in, null);
        } finally {
            in.close();
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);

        TrustManager[] tms = tmf.getTrustManagers();
        for (int i = 0; i < tms.length; i++) {
            if (tms[i] instanceof X509TrustManager) {
                return (X509TrustManager) tms[i];
            }
        }

        throw new NoSuchAlgorithmException("No X509TrustManager in TrustManagerFactory");
    }
}

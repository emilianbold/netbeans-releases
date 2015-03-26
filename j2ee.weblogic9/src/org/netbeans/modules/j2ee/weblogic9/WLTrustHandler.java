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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.security.Certificate;
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
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.weblogic.common.api.WebLogicConfiguration;
import org.netbeans.modules.weblogic.common.spi.WebLogicTrustHandler;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Petr Hejl
 */
@ServiceProvider(service = WebLogicTrustHandler.class)
public class WLTrustHandler implements WebLogicTrustHandler {

    private static final RequestProcessor TRUST_MANAGER_ACCESS = new RequestProcessor(WLTrustHandler.class);

    private static final int CHECK_TIMEOUT = 5000;

    @Override
    public void setup(WebLogicConfiguration config) throws GeneralSecurityException, IOException {
        SSLContext context = SSLContext.getInstance("TLS"); // NOI18N
        context.init(null, new TrustManager[]{getTrustManager(config)}, new SecureRandom());
        SSLSocket socket = (SSLSocket) context.getSocketFactory().createSocket();
        try {
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

    @Override
    public TrustManager getTrustManager(WebLogicConfiguration config) throws GeneralSecurityException {
        return new DelegatingTrustManager(config);
    }

    @Override
    public Map<String, String> getTrustProperties(WebLogicConfiguration config) {
        // hacky way for now
        String url = WLDeploymentFactory.URI_PREFIX + config.getHost()
            + ":" + config.getPort() + ":" + config.getServerHome().getAbsolutePath(); // NOI18N
        File domain = config.getDomainHome();
        if (domain != null) {
            url += ":" + domain.getAbsolutePath(); // NOI18N;
        }
        final InstanceProperties ip = InstanceProperties.getInstanceProperties(url);
        String path = ip.getProperty("trustStore");
        if (path == null) {
            return Collections.emptyMap();
        }
        File file = new File(path);
        if (!file.isFile()) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new HashMap<String, String>();
        result.put("weblogic.security.TrustKeyStore", "CustomTrust");
        result.put("weblogic.security.CustomTrustKeyStoreType", "JKS");
        result.put("weblogic.security.CustomTrustKeyStoreFileName", file.getAbsolutePath());
        result.put("weblogic.security.SSL.ignoreHostnameVerification", "true");
        return result;
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
                    //System.out.println("\tAccepted issuers count : " + pkixTrustManager.getAcceptedIssuers().length);
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
            throw new CertificateException("TrustManager do not trust any client");
        }

        /*
         * Delegate to the default trust manager.
         */
        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType)
                throws CertificateException {
            try {
                pkixTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException excep) {
                // hacky way for now
                String url = WLDeploymentFactory.URI_PREFIX + config.getHost()
                    + ":" + config.getPort() + ":" + config.getServerHome().getAbsolutePath(); // NOI18N
                File domain = config.getDomainHome();
                if (domain != null) {
                    url += ":" + domain.getAbsolutePath(); // NOI18N;
                }
                final InstanceProperties ip = InstanceProperties.getInstanceProperties(url);

                Future<Boolean> task = TRUST_MANAGER_ACCESS.submit(new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws GeneralSecurityException, IOException {
                        String path = ip.getProperty("trustStore");
                        if (path != null) {
                            File singleTrust = new File(path);
                            try {
                                X509TrustManager m = createTrustManager(singleTrust);
                                try {
                                    m.checkServerTrusted(chain, authType);
                                    return true;
                                } catch (CertificateException ex) {

                                }
                            } catch (Exception ex) {
                                // proceed to dialog
                            }
                        }

                        NotifyDescriptor notDesc = new NotifyDescriptor.Confirmation(
                                "The server certificate is not trusted. Proceed anyway?",
                                NotifyDescriptor.YES_NO_OPTION);
                        X509Certificate[] sorted = sortChain(chain);
                        System.out.println("XXX " + sorted[sorted.length - 1].getSubjectDN().getName());
                        Object result = DialogDisplayer.getDefault().notify(notDesc);
                        if (result == NotifyDescriptor.YES_OPTION) {
                            ip.setProperty("trustStore", createSingleTrustStore(sorted[sorted.length - 1]).getAbsolutePath());
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

        /*
         * Merely pass this through.
         */
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

    private static File createSingleTrustStore(X509Certificate cert) throws GeneralSecurityException, IOException {
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(null, null);
        keystore.setCertificateEntry("single", cert);
        File file = File.createTempFile("wlskeystore", null);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        try {
            keystore.store(out, "weblogic".toCharArray());
        } finally {
            out.close();
        }
        return file;
    }

    private static X509TrustManager createTrustManager(File file) throws GeneralSecurityException, IOException {
        KeyStore ts = KeyStore.getInstance("JKS");
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            ts.load(in, null);
        } finally {
            in.close();
        }

        // initialize a new TMF with the ts we just loaded
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);

        // acquire X509 trust manager from factory
        TrustManager tms[] = tmf.getTrustManagers();
        for (int i = 0; i < tms.length; i++) {
            if (tms[i] instanceof X509TrustManager) {
                return (X509TrustManager) tms[i];
            }
        }

        throw new NoSuchAlgorithmException("No X509TrustManager in TrustManagerFactory");
    }
}

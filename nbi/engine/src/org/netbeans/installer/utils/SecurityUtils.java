/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */
package org.netbeans.installer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.CodeSigner;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.netbeans.installer.utils.UiUtils.CertificateAcceptanceStatus;

/**
 *
 * @author Kirill Sorokin
 * @author Dmitry Lipin
 */
public final class SecurityUtils {

    private static KeyStore caStore;
    private static KeyStore permanentTrustedStore;
    private static KeyStore sessionTrustedStore;
    private static KeyStore deniedStore;
    private static String CACERTS_FILE_PATH = "lib/security/cacerts";//NOI18N
    private static final int BUFFER_SIZE = 4096;

    public static boolean isJarSignatureVeryfied(
            final File file,
            final String description) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        if (caStore == null) {
            caStore = KeyStore.getInstance(KeyStore.getDefaultType());
            final File cacertsFile = new File(SystemUtils.getCurrentJavaHome(), CACERTS_FILE_PATH);
            caStore.load(new FileInputStream(cacertsFile), null);

            permanentTrustedStore = KeyStore.getInstance(KeyStore.getDefaultType());
            permanentTrustedStore.load(null, null);

            sessionTrustedStore = KeyStore.getInstance(KeyStore.getDefaultType());
            sessionTrustedStore.load(null, null);

            deniedStore = KeyStore.getInstance(KeyStore.getDefaultType());
            deniedStore.load(null, null);
        }

        final JarFile jar = new JarFile(file);
        try {
            // first we should fetch all certificates that are present in the jar
            // file skipping duplicates
            Certificate[] certificates = null;
            CodeSigner[] codeSigners = null;
            for (JarEntry entry : Collections.list(jar.entries())) {
                readFully(jar.getInputStream(entry));

                certificates = entry.getCertificates();
                codeSigners = entry.getCodeSigners();

                if (certificates != null) {
                    break;
                }
            }

            // if there are no certificates -- we should pop up the dialog warning
            // that the jar is not signed and ask the user whether he wants to
            // accept this
            if (certificates == null) {
                // todo
            }

            // check the permanent and session trusted stores
            int chainStart = 0;
            int chainEnd = 0;
            int chainNum = 0;

            // iterate over the certificate chains that are present in the
            // certificate arrays
            while (chainEnd < certificates.length) {
                // determine the start and end of the current certificates chain
                int i = chainStart;
                while (i < certificates.length - 1) {
                    final boolean isIssuer = isIssuerOf(
                            (X509Certificate) certificates[i],
                            (X509Certificate) certificates[i + 1]);

                    if ((certificates[i] instanceof X509Certificate) && (certificates[i + 1] instanceof X509Certificate) && isIssuer) {
                        i++;
                    } else {
                        break;
                    }
                }
                chainEnd = i + 1;

                // if the denied certificates store contains the
                if (containsCertificate(deniedStore, certificates[chainStart])) {
                    return false;
                } else if (containsCertificate(permanentTrustedStore, certificates[chainStart]) ||
                        containsCertificate(sessionTrustedStore, certificates[chainStart])) {
                    return true;
                }

                chainStart = chainEnd;
                chainNum++;
            }

            // If we get here, no cert in chain has been stored in Session or Permanent store.
            // If they are not in Deny store either, we have to pop up security dialog box
            // for each signer's certificate one by one.
            boolean rootCANotValid = false;
            boolean timeNotValid = false;

            chainStart = 0;
            chainEnd = 0;
            chainNum = 0;
            while (chainEnd < certificates.length) {
                int i = chainStart;

                for (i = chainStart; i < certificates.length; i++) {
                    X509Certificate currentCert = null;
                    X509Certificate issuerCert = null;

                    if (certificates[i] instanceof X509Certificate) {
                        currentCert = (X509Certificate) certificates[i];
                    }
                    if ((i < certificates.length - 1) &&
                            (certificates[i + 1] instanceof X509Certificate)) {
                        issuerCert = (X509Certificate) certificates[i + 1];
                    } else {
                        issuerCert = currentCert;
                    }

                    // check if the certificate is valid and has not expired
                    try {
                        currentCert.checkValidity();
                    } catch (CertificateExpiredException e1) {
                        timeNotValid = true;
                    } catch (CertificateNotYetValidException e2) {
                        timeNotValid = true;
                    }

                    if (isIssuerOf(currentCert, issuerCert)) {
                        // check the current certificate's signature -- verify that
                        // this issuer did indeed sign the certificate.
                        try {
                            currentCert.verify(issuerCert.getPublicKey());
                        } catch (GeneralSecurityException e) {
                            return false;
                        }
                    } else {
                        break;
                    }
                }
                chainEnd = (i < certificates.length) ? (i + 1) : i;

                // we need to verify if the certificate chain is signed by a CA
                rootCANotValid = !verifyCertificate(caStore, certificates[chainEnd - 1]);

                Date timestamp = null;
                if (codeSigners[chainNum].getTimestamp() != null) {
                    timestamp = codeSigners[chainNum].getTimestamp().getTimestamp();
                }

                CertificateAcceptanceStatus status = UiUtils.showCertificateAcceptanceDialog(
                        certificates,
                        chainStart,
                        chainEnd,
                        rootCANotValid,
                        timeNotValid,
                        timestamp,
                        description);


                // If user Grant permission, just pass all security checks.
                // If user Deny first signer, pop up security box for second signer certs
                if (status == CertificateAcceptanceStatus.ACCEPT_PERMANENTLY) {
                    addCertificate(permanentTrustedStore, certificates[chainStart]);
                    return true;
                } else if (status == CertificateAcceptanceStatus.ACCEPT_FOR_THIS_SESSION) {
                    addCertificate(sessionTrustedStore, certificates[chainStart]);
                    return true;
                } else {
                    addCertificate(deniedStore, certificates[chainStart]);
                }

                chainStart = chainEnd;
                chainNum++;
            }

            return false;
        } finally {
            jar.close();
        }
    }

    private static void readFully(
            final InputStream stream) throws IOException {
        final byte[] buffer = new byte[BUFFER_SIZE];
        while (stream.read(buffer) != -1) {
            ; // do this!
        }
    }

    private static boolean isIssuerOf(
            final X509Certificate certificate1,
            final X509Certificate certificate2) {
        return certificate1.getIssuerDN().equals(certificate2.getSubjectDN());
    }

    private static boolean containsCertificate(
            final KeyStore store,
            final Certificate certificate) throws KeyStoreException {
        return store.getCertificateAlias(certificate) != null;
    }

    private static void addCertificate(
            final KeyStore store,
            final Certificate certificate) throws KeyStoreException {
        if (store.getCertificateAlias(certificate) == null) {
            store.setCertificateEntry(
                    "alias" + new Random().nextLong(),
                    certificate);
        }
    }

    private static boolean verifyCertificate(
            final KeyStore store,
            final Certificate certificate) throws KeyStoreException {
        for (String alias : Collections.list(store.aliases())) {
            try {
                certificate.verify(store.getCertificate(alias).getPublicKey());
                return true;
            } catch (GeneralSecurityException e) {
                // we must ignore this exception as it is VERY expected -- will
                // happen N-1 times at least
            }
        }

        return false;
    }
}

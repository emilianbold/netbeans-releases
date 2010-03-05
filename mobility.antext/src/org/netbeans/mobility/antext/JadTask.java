/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

/*
 * JadTask.java
 *
 * Created on 15. prosinec 2003, 9:12
 */
package org.netbeans.mobility.antext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Jad Task adds support for updating values of MIDlet-Jar-Size and MIDlet-Jar-URL attributes in jad file.
 *
 * <p>Attributes:<ol>
 * <li>JadFile - Required. Specifies source jad file location. If Output does not exist, this is destination jad file location too.
 * <li>JarFile - Required. Specifies jar file location.
 * <li>Output - Optional. If specified, this is destination jad file location.
 * <li>Url - Optional. If specified, value of MIDlet-Jar-URL attribute in jad file is updated with it.
 * <li>Encoding - Optional. Encoding of destination jad file. If not specified, "UTF-8" is used.
 * <li>Sign - Optional. Specifies if jar file should be signed. Default: false.
 * <li>KeyStore - Semi-required. Required when Sign is true. Specifies keystore file location.
 * <li>KeyStoreType - Optional. Specifies keystore type. Valid: Default, PKCS12. If not specified, type is recognized by extension of keystore file: ".p12" or ".pkcs12" extensions are recognized as PKCS12 type. Other extensions are recognized as Default type.
 * <li>KeyStorePassword - Semi-required. Required when Sign is true. Specifies keystore file password.
 * <li>Alias - Semi-required. Required when Sign is true. Specifies alias name.
 * <li>AliasPassword - Semi-required. Required when Sign is true. Specifies alias key password.
 * </ol>
 *
 * <p>Jad file is updated if and only if source jad file or jar file is newer than destination jad file.
 *
 * @author Adam Sotona, David Kaspar
 */
public class JadTask extends Task {

    private static final String LIBLET_NAME = "LIBlet-Name"; //NOI18N
    private static final String MIDLET_PREFIX = "MIDlet-"; //NOI18N
    private static final String LIBLET_PREFIX = "LIBlet-"; //NOI18N
    private static final String MIDLET_TEST_KEYS[] = new String[]{"MicroEdition-Configuration", "MicroEdition-Profile", "MIDlet-Name", "MIDlet-Vendor", "MIDlet-Version"}; // NO I18N
    private static final String LIBLET_TEST_KEYS[] = new String[]{"MicroEdition-Configuration", "MicroEdition-Profile", "LIBlet-Name", "LIBlet-Vendor", "LIBlet-Version"}; // NO I18N
    private static final String DEFAULT_ENCODING = "UTF-8"; // NO I18N
    private static final String JAR_URL_KEY = "Jar-URL"; // NO I18N
    private static final String JAR_SIZE_KEY = "Jar-Size"; // NO I18N
    private static final String JAR_RSA_SHA1 = "MIDlet-Jar-RSA-SHA1"; // NO I18N
    private static final String LIBLET_JAR_SHA1 = "LIBlet-Jar-SHA1"; // NO I18N
    private static final String CERTIFICATE = "MIDlet-Certificate-{0}-{1}"; // NO I18N
    private static final String SHA1withRSA = "SHA1withRSA"; // NO I18N
    private static final String SHA1 = "SHA-1"; //NOI18N
    private static final String MIDLET_1 = "MIDlet-1"; // NOI18N
    private static final String PKCS12 = "PKCS12"; // NOI18N
    /** Holds value of property jadFile. */
    private File jadFile;
    /** Holds value of property jarFile. */
    private File jarFile;
    /** Holds value of property output. */
    private File output;
    /** Holds value of property url. */
    private String url;
    /** Holds value of property encoding. */
    private String encoding;
    /** Holds value of property sign. */
    private boolean sign = false;
    /** Holds value of property keyStore. */
    private File keyStore = null;
    /** Holds value of property keyStoreType. */
    private String keyStoreType = null;
    /** Holds value of property keyStorePassword. */
    private String keyStorePassword = null;
    /** Holds value of property alias. */
    private String alias = null;
    /** Holds value of property aliasPassword. */
    private String aliasPassword = null;
    private final static String ERR_MissingAttr = "ERR_MissingAttr";

    /**
     * Do the work.
     * @throws BuildException if attribute is missing or there is a problem during jad file update.
     */
    public void execute() throws BuildException {
        if (jadFile == null) {
            throw new BuildException(Bundle.getMessage(ERR_MissingAttr, "jadFile")); // NO I18N
        }
        if (jarFile == null) {
            throw new BuildException(Bundle.getMessage(ERR_MissingAttr, "jarFile")); // NO I18N
        }
        final File jadFrom = jadFile;
        final File jadTo = (output != null) ? output : jadFile;
        if (!jadFrom.isFile()) {
            throw new BuildException(Bundle.getMessage("ERR_SourceIsMissing", jadFrom.toString())); // NO I18N
        }
        if (!jarFile.isFile()) {
            throw new BuildException(Bundle.getMessage("ERR_JarFileIsMissing", jarFile.toString())); // NO I18N
        }//        if (output != null  &&  jadFrom.lastModified() <= jadTo.lastModified ()  &&  jarFile.lastModified () <= jadTo.lastModified()) {
//            log (Bundle.getMessage ("MSG_JadIsUpToDate"), Project.MSG_VERBOSE); // No I18N
//            return;
//        }
        try {
            log(Bundle.getMessage("MSG_Updating", jadTo.getAbsolutePath()), Project.MSG_INFO); // NO I18N
            log(Bundle.getMessage("MSG_Loading", jadFrom.toString()), Project.MSG_VERBOSE); // NO I18N
            final HashMap<String, String> hash = new HashMap<String, String>();
            final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(jadFrom), (encoding != null) ? encoding : DEFAULT_ENCODING));
            try {
                for (;;) {
                    final String readLine = br.readLine();
                    if (readLine == null) {
                        break;
                    }
                    if ("".equals(readLine)) //NOI18N
                    {
                        continue;
                    }
                    if (readLine.startsWith("#")) //NOI18N
                    {
                        continue;
                    }
                    final int colon = readLine.indexOf(':');
                    if (colon < 0) {
                        log(Bundle.getMessage("WARN_InvalidLineFormat", readLine), Project.MSG_WARN); // NO I18N
                        continue;
                    }
                    hash.put(readLine.substring(0, colon), readLine.substring(colon + 1).trim());
                }
            } finally {
                br.close();
            }
            boolean isLiblet = hash.containsKey(LIBLET_NAME);
            String prefix = isLiblet ? LIBLET_PREFIX : MIDLET_PREFIX;
            if (!isLiblet && !hash.containsKey(MIDLET_1)) {
                log(Bundle.getMessage("WARN_MissingMIDlets"), Project.MSG_WARN); // NOI18N
            }

            String testKeys[] = isLiblet ? LIBLET_TEST_KEYS : MIDLET_TEST_KEYS;
            for (int i = 0; i < testKeys.length; i++) {
                if (!hash.containsKey(testKeys[i])) {
                    log(Bundle.getMessage("WARN_MissingAttribute", testKeys[i]), Project.MSG_WARN); // NO I18N
                }
            }
//            String version = (String)hash.get(MIDLET_VERSION);
//            if (version != null && Pattern.matches("^[0-9][0-9][0-9][0-9][0-9][0-9]$", version)) { //NOI18N
//                hash.put(MIDLET_VERSION, version.substring(0, 2) + '.' + version.substring(2, 4) + '.' + version.substring(4, 6));
//            }
            if (url == null) {
                if (!hash.containsKey(prefix + JAR_URL_KEY)) {
                    final String jarname = jarFile.getName();
                    log(Bundle.getMessage("WARN_MissingURL", prefix + JAR_URL_KEY, jarname), Project.MSG_WARN); // NO I18N
                    hash.put(prefix + JAR_URL_KEY, jarname);
                }
            } else {
                log(Bundle.getMessage("MSG_SettingAttribute", prefix + JAR_URL_KEY, url), Project.MSG_VERBOSE); // NO I18N
                hash.put(prefix + JAR_URL_KEY, url);
            }
            final String jarsize = String.valueOf(jarFile.length());
            log(Bundle.getMessage("MSG_JarSize", jarFile.getAbsolutePath(), jarsize), Project.MSG_INFO); // NO I18N
            log(Bundle.getMessage("MSG_SettingAttribute", prefix + JAR_SIZE_KEY, jarsize), Project.MSG_VERBOSE); // NO I18N
            hash.put(prefix + JAR_SIZE_KEY, jarsize);

            // signing
            if (sign && !isLiblet) {
                if (keyStore == null || !keyStore.exists() || !keyStore.isFile()) {
                    throw new BuildException(Bundle.getMessage(ERR_MissingAttr, keyStore == null ? "keystore" : keyStore.getAbsolutePath())); // NO I18N
                }
                if (keyStorePassword == null) {
                    throw new BuildException(Bundle.getMessage(ERR_MissingAttr, "keyStorePassword")); // NO I18N
                }
                if (alias == null) {
                    throw new BuildException(Bundle.getMessage(ERR_MissingAttr, "alias")); // NO I18N
                }
                if (aliasPassword == null) {
                    throw new BuildException(Bundle.getMessage(ERR_MissingAttr, "aliasPassword")); // NO I18N
                }
                log(Bundle.getMessage("MSG_Signing"), Project.MSG_INFO); // NO I18N

                KeyStore store;
                try {
                    if (keyStoreType == null) {
                        final String name = keyStore.getAbsolutePath().toLowerCase();
                        if (name.endsWith(".p12") || name.endsWith(".pkcs12")) // NOI18N
                        {
                            keyStoreType = PKCS12;
                        }
                    }
                    if (keyStoreType != null && PKCS12.equals(keyStoreType)) // NO I18N
                    {
                        store = KeyStore.getInstance("pkcs12", "SunJSSE"); // NO I18N
                    } else {
                        store = KeyStore.getInstance("JKS", "SUN"); // NO I18N
                    }
                } catch (NoSuchProviderException e) {
                    throw new BuildException(Bundle.getMessage("ERR_UnsupportedKeyStoreProvider", keyStoreType != null ? keyStoreType : "default"), e); // NO I18N
                } catch (KeyStoreException e) {
                    throw new BuildException(Bundle.getMessage("ERR_UnsupportedKeyStoreType", keyStoreType != null ? keyStoreType : "default"), e); // NO I18N
                }
                final InputStream stream = new FileInputStream(keyStore);
                try {
                    store.load(stream, keyStorePassword.toCharArray());
                } catch (IOException e) {
                    throw new BuildException(Bundle.getMessage("ERR_ErrorLoadingKeyStore", keyStore.getAbsolutePath()), e); // NO I18N
                } catch (NoSuchAlgorithmException e) {
                    throw new BuildException(Bundle.getMessage("ERR_ErrorLoadingKeyStore", keyStore.getAbsolutePath()), e); // NO I18N
                } catch (CertificateException e) {
                    throw new BuildException(Bundle.getMessage("ERR_ErrorLoadingKeyStore", keyStore.getAbsolutePath()), e); // NO I18N
                } finally {
                    stream.close();
                }

                hash.remove(JAR_RSA_SHA1);
                for (int a = 1;; a++) {
                    int b = 1;
                    for (;; b++) {
                        if (hash.remove(MessageFormat.format(CERTIFICATE, new Object[]{Integer.toString(a), Integer.toString(b)})) == null) {
                            break;
                        }
                    }
                    if (b <= 1) {
                        break;
                    }
                }

                Certificate[] certs;
                Key key;
                try {
                    certs = store.getCertificateChain(alias);
                    key = store.getKey(alias, aliasPassword.toCharArray());
                } catch (UnrecoverableKeyException e) {
                    e.printStackTrace();
                    certs = null;
                    key = null;
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    certs = null;
                    key = null;
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                    certs = null;
                    key = null;
                }
                if (certs == null || key == null) {
                    throw new BuildException(Bundle.getMessage("ERR_NoCertificateOrAlgorithm", alias)); // NO I18N
                }
                for (int a = 0; a < certs.length; a++) {
                    final Certificate cert = certs[a];
                    final String certAttr = MessageFormat.format(CERTIFICATE, new Object[]{"1", Integer.toString(a + 1)}); //NOI18N
                    log(Bundle.getMessage("MSG_AddingCertificateAttr", certAttr), Project.MSG_VERBOSE); // NO I18N
                    try {
                        hash.put(certAttr, Base64.encode(cert.getEncoded()));
                    } catch (CertificateEncodingException e) {
                        throw new BuildException(Bundle.getMessage("ERR_CertificateEncoding"), e); // NO I18N
                    }
                }

                Signature signature;
                try {
                    signature = Signature.getInstance(SHA1withRSA); // NO I18N
                } catch (NoSuchAlgorithmException e) {
                    throw new BuildException(Bundle.getMessage("ERR_NoSuchAlgorithmException", SHA1withRSA), e); // NO I18N
                }
                try {
                    if (!(key instanceof PrivateKey)) {
                        key = null;
                    }
                    signature.initSign((PrivateKey) key);
                } catch (InvalidKeyException e) {
                    key = null;
                }
                if (key == null) {
                    throw new BuildException(Bundle.getMessage("ERR_InvalidPrivateKey", alias)); // NO I18N
                }
                final byte[] mem = new byte[16384];
                final FileInputStream fis = new FileInputStream(jarFile);
                try {
                    for (;;) {
                        int i = fis.read(mem);
                        if (i < 0) {
                            break;
                        }
                        if (i >= 0) {
                            signature.update(mem, 0, i);
                        }
                    }
                    final byte signed[] = signature.sign();
                    log(Bundle.getMessage("MSG_AddingSignAttr", JAR_RSA_SHA1), Project.MSG_INFO); // NO I18N
                    hash.put(JAR_RSA_SHA1, Base64.encode(signed));
                } catch (SignatureException e) {
                    throw new BuildException(e);
                } finally {
                    fis.close();
                }
            }

            if (isLiblet) { //calculation of SHA1
                MessageDigest md;
                try {
                    md = MessageDigest.getInstance(SHA1);
                } catch (NoSuchAlgorithmException e) {
                    throw new BuildException(Bundle.getMessage("ERR_NoSuchAlgorithmException", SHA1), e); // NO I18N
                }
                final byte[] mem = new byte[16384];
                final FileInputStream fis = new FileInputStream(jarFile);
                try {
                    for (;;) {
                        int i = fis.read(mem);
                        if (i < 0) {
                            break;
                        }
                        if (i >= 0) {
                            md.update(mem, 0, i);
                        }
                    }
                    final byte signed[] = md.digest();
                    log(Bundle.getMessage("MSG_AddingSignAttr", LIBLET_JAR_SHA1), Project.MSG_INFO); // NO I18N
                    hash.put(LIBLET_JAR_SHA1, Base64.encode(signed));
                } finally {
                    fis.close();
                }
            }


            // saving
            log(Bundle.getMessage("MSG_Saving", jadTo.toString()), Project.MSG_VERBOSE); // NO I18N
            final PrintStream out = new PrintStream(new FileOutputStream(jadTo), false, encoding == null ? DEFAULT_ENCODING : encoding);
            final Object[] keys = hash.keySet().toArray();
            if (keys != null) {
                Arrays.sort(keys);
                for (int a = 0; a < keys.length; a++) {
                    out.println(keys[a] + ": " + hash.get(keys[a])); //NOI18N
                }
            }
            out.flush();
            out.close();
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        }
    }

    /** Setter for property jadFile.
     * @param jadFile New value of property jadFile.
     *
     */
    public void setJadFile(final File jadFile) {
        this.jadFile = jadFile;
    }

    /** Setter for property jarFile.
     * @param jarFile New value of property jarFile.
     *
     */
    public void setJarFile(final File jarFile) {
        this.jarFile = jarFile;
    }

    /** Setter for property output.
     * @param output New value of property output.
     *
     */
    public void setOutput(final File output) {
        this.output = output;
    }

    /** Setter for property url.
     * @param url New value of property url.
     *
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /** Setter for property encoding.
     * @param encoding New value of property encoding.
     *
     */
    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    /**
     * Setter for property sign.
     * @param sign New value of property sign.
     */
    public void setSign(final boolean sign) {
        this.sign = sign;
    }

    /**
     * Setter for property keyStore.
     * @param keyStore New value of property keyStore.
     */
    public void setKeyStore(final java.io.File keyStore) {
        this.keyStore = keyStore;
    }

    /**
     * Setter for property keyStoreType.
     * @param keyStoreType New value of property keyStoreType.
     */
    public void setKeyStoreType(final java.lang.String keyStoreType) {
        this.keyStoreType = keyStoreType.toUpperCase();
    }

    /**
     * Setter for property keyStorePassword.
     * @param keyStorePassword New value of property keyStorePassword.
     */
    public void setKeyStorePassword(final java.lang.String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * Setter for property alias.
     * @param alias New value of property alias.
     */
    public void setAlias(final java.lang.String alias) {
        this.alias = alias;
    }

    /**
     * Setter for property aliasPassword.
     * @param aliasPassword New value of property aliasPassword.
     */
    public void setAliasPassword(final java.lang.String aliasPassword) {
        this.aliasPassword = aliasPassword;
    }
}

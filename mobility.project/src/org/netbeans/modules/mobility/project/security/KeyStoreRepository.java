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

/*
 * KeyStoreRepository.java -- synopsis.
 *
 *
 *
 *
 */
package org.netbeans.modules.mobility.project.security;

import java.io.*;
import java.util.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.mobility.project.security.KeyStoreRepository.KeyStoreBean.KeyAliasBean;
import org.openide.util.Lookup;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import sun.security.tools.KeyTool;

/**
 * @author ps121858, David Kaspar
 */
public class KeyStoreRepository implements java.io.Externalizable, PropertyChangeListener {
    
    public static boolean rememberPasswords = true;
    
    public static final String PROP_KEYSTORE_ADDED = "keystore_added"; //NOI18N
    public static final String PROP_KEYSTORE_REMOVED = "keystore_removed"; //NOI18N
    public static final String PROP_OPENED = "opened";//NOI18N
    
    private static final long serialVersionUID = -4428411825913512121L;
    
    private static transient boolean defaultKeyStoreInitialized = false;
    private static KeyStoreRepository repo;
    private static KeyStoreBean defaultKeyStore;
    final private static String PASSWORD="password";
    
    
    private ArrayList<KeyStoreBean> keyStores = new ArrayList<KeyStoreBean>();
    final private transient Map<String,Object> passwords = Collections.synchronizedMap(new HashMap<String,Object>());
    
    public KeyStoreRepository() {
        if (defaultKeyStore != null)
            keyStores.add(defaultKeyStore);
    }
    
    public static boolean isDefaultKeystore(final KeyStoreBean keystore) {
        if (! defaultKeyStoreInitialized)
            getDefault();
        return keystore != null  &&  keystore == defaultKeyStore;
    }
    
    public synchronized static KeyStoreRepository getDefault() {
        if (repo == null) {
            defaultKeyStoreInitialized = true;
            final FileObject foRoot = Repository.getDefault().getDefaultFileSystem().getRoot();
            File file = FileUtil.toFile(foRoot);

            if (file != null)
                file = new File(file, "j2me" + File.separator + "builtin.ks");
            if (file != null) {
                KeyStoreBean bean = KeyStoreBean.create(file.getAbsolutePath(),
                                                        PASSWORD);

                if (!file.exists()) {
                    if (bean.openKeyStore(true)) {
                        try {
                            bean.addKeyToStore("trusted", "CN=trusted", PASSWORD,
                                               -1);
                            bean.addKeyToStore("untrusted", "CN=untrusted",
                                               PASSWORD, -1);
                            bean.addKeyToStore("minimal", "CN=minimal", PASSWORD,
                                               -1);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                            bean = null;
                        }
                    }
                }
                defaultKeyStore = bean;
                if (defaultKeyStore != null) {
                    defaultKeyStore.openKeyStore();
                    if (defaultKeyStore.isOpened()) {
                        for (final KeyAliasBean alias : defaultKeyStore.aliasses()) {
                            alias.setPassword(PASSWORD);
                            alias.open();
                        }
                    }
                }
            }
            repo = Lookup.getDefault().lookup(KeyStoreRepository.class);
        }
        return repo;
    }
    
    public static KeyStoreRepository createRepository() {
        return new KeyStoreRepository();
    }
    
    public Object getPassword(final String keyFile) {
        return passwords.get(keyFile);
    }
    
    public Object putPassword(final String keyFile,final Object password) {
        return passwords.put(keyFile,password);
    }
    
    public Object removePassword(final String keyFile) {
        return passwords.remove(keyFile);
    }
    
    public void readExternal(final ObjectInput in) throws ClassNotFoundException {
        keyStores = new ArrayList<KeyStoreBean>();
        if (defaultKeyStore != null)
            keyStores.add(defaultKeyStore);
        while (true) {
            try {
                final KeyStoreBean keyStoreBean = (KeyStoreBean) in.readObject();
                keyStores.add(keyStoreBean);
            } catch (java.io.IOException e) {
                break;
            }
        }
    }
    
    public void writeExternal(final ObjectOutput out) throws IOException {
        for (final KeyStoreBean keyStoreBean : keyStores) {
            if (keyStoreBean != defaultKeyStore)
                out.writeObject(keyStoreBean);
        }
    }
    
    public List<KeyStoreBean> getKeyStores() {
        return keyStores;
    }
    
    public void addKeyStore(final KeyStoreBean bean) {
        if (bean == null)
            return;
        if (getKeyStore(bean.getKeyStorePath(), false) != null)
            return;
        
        keyStores.add(bean);
        bean.addPropertyChangeListener(this);
        propertyChangeSupport.firePropertyChange(PROP_KEYSTORE_ADDED, null, null);
    }
    
    public void removeKeyStore(final KeyStoreBean bean) {
        bean.removePropertyChangeListener(this);
        keyStores.remove(bean);
        propertyChangeSupport.firePropertyChange(PROP_KEYSTORE_REMOVED, null, null);
    }
    
    public KeyStoreBean getKeyStore(final String path, final boolean create) {
        if (path == null) return null;
        final File file=new File(path);
        for ( final KeyStoreBean bean : keyStores ) {
            if (KeyStoreBean.equalFiles(bean.getKeyStoreFile(), file))
                return bean;
        }
        if (! create)
            return null;
        final KeyStoreBean bean = KeyStoreBean.create(path);
        addKeyStore(bean);
        return bean;
    }
    
    public static class KeyStoreBean implements Externalizable {
        
        private static final long serialVersionUID = -38422947758836052L;
        
        public static final String PROP_PASSWORD = PASSWORD; //NOI18N
        public static final String PROP_PATH2KEY_STORE = "pathToKeyStore"; //NOI18N
        public static final String PROP_TYPE = "type"; // NOI18N
        
        PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
        
        private File keyStoreFile;
        private String password;
        private String type;
        private static final String JKS = "JKS"; // NOI18N
        
        transient private boolean opened = false;
        transient private KeyStore store;
        
        transient private TreeSet<KeyAliasBean> aliasses = new TreeSet<KeyAliasBean>();
        
        public static KeyStoreBean create(String pathToKeyStore) {
            if (pathToKeyStore == null)
                return null;
            final KeyStoreBean bean = new KeyStoreBean();
            bean.keyStoreFile = new File(pathToKeyStore);
            pathToKeyStore = pathToKeyStore.toLowerCase();
            bean.type = (pathToKeyStore.endsWith(".pkcs12") || pathToKeyStore.endsWith(".p12")) ? "PKCS12" : JKS; // NOI18N
            return bean;
        }
        
        public static KeyStoreBean create(final String pathToKeyStore, final String password) {
            final KeyStoreBean bean = create(pathToKeyStore);
            bean.password = password;
            return bean;
        }

        public boolean isValid() {
            if (!keyStoreFile.exists() || !keyStoreFile.isFile()) return false;
            return true;
        }
        
        public String getType() {
            if (type == null) return JKS; // NOI18N
            return type;
        }
        
        /**
         * Set the keyatore type. Except for PKCS12 type all other types defaults to JKS
         *
         * @param type
         */
        public void setType(final String type) {
            String newType=type==null?JKS:type.toUpperCase();
            if (!"PKCS12".equals(newType))
                newType = JKS;
            if (!newType.equals(this.type)) {
                this.type=newType;
                propertyChangeSupport.firePropertyChange(PROP_TYPE, null, null);
            }
        }
        
        public File getKeyStoreFile() {
            return keyStoreFile;
        }
        
        public String getKeyStorePath() {
            return keyStoreFile.getAbsolutePath();
        }
        
        public void setKeyStoreFile(final File keyStoreFile) {
            this.keyStoreFile = keyStoreFile;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(final String password) {
            this.password = password;
        }
        
        public boolean isOpened() {
            return opened;
        }
        
        public KeyStore getStore() {
            return store;
        }
        
        public Set<KeyAliasBean> aliasses() {
            return Collections.unmodifiableSet(aliasses);
        }
        
        public KeyAliasBean getAlias(final String alias) {
            for (final KeyAliasBean keyAliasBean : aliasses ) {
                if (alias.equals(keyAliasBean.getAlias()))
                    return keyAliasBean;
            }
            return null;
        }
        
        public boolean openKeyStore() {
            return openKeyStore(false);
        }
        
        public synchronized boolean openKeyStore(final boolean create) {
            if (opened)
                return true;
            clearAliasses();
            if (getPassword() == null)
                return false;
            try {
                if ("PKCS12".equals(type)) { // NOI18N
                    store = KeyStore.getInstance("pkcs12", "SunJSSE"); // NOI18N
                } else {
                    store = KeyStore.getInstance(JKS, "SUN"); //NOI18N
                }
                if (keyStoreFile.exists()) {
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(keyStoreFile);
                        store.load(fis, password.toCharArray());
                    } catch (IOException ioEx) {
                        throw ioEx;
                    } finally {
                        if (fis != null) fis.close();
                    }
                } else {
                    store.load(null, password.toCharArray());
                    if (! create)
                        return false;
                    storeKeyStore();
                }
                
            } catch (NoSuchAlgorithmException e) {
                return false;
            } catch (CertificateException e) {
                return false;
            } catch (KeyStoreException e) {
                return false;
            } catch (NoSuchProviderException e) {
                return false;
            } catch (IOException e) {
                return false;
            }
            opened = true;
            loadAliasses(null);
            propertyChangeSupport.firePropertyChange(PROP_OPENED, false, true); //NOI18N
            return true;
        }
        
        public boolean storeKeyStore() throws IOException {
            if (isDefaultKeystore(this))
                return false;
            if (store == null) return false;
            FileOutputStream fos = null;
            try {
                if (!keyStoreFile.exists()) {
                    final File parent = keyStoreFile.getParentFile();
                    if (parent != null  &&  ! parent.exists())
                        parent.mkdirs();
                }
                fos = new FileOutputStream(keyStoreFile);
                store.store(fos, password.toCharArray());
            } catch (IOException ioEx) {
                throw ioEx;
            } catch (KeyStoreException e) {
                return false;
            } catch (NoSuchAlgorithmException e) {
                return false;
            } catch (CertificateException e) {
                return false;
            } finally {
                if (fos != null) try { fos.close(); } catch (IOException e) {}
            }
            return true;
        }
        
        public boolean closeKeyStore() {
            opened = false;
            store = null;
            propertyChangeSupport.firePropertyChange(PROP_OPENED, true, false); //NOI18N
            return true;
        }
        
        public void refresh() throws IOException {
            final TreeSet<KeyAliasBean> oldAliasses = aliasses;
            clearAliasses();
            if (getPassword() == null)
                return;
            
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(keyStoreFile);
                store.load(fis, password.toCharArray());
                loadAliasses(oldAliasses);
            } catch (IOException ioEx) {
                throw ioEx;
            } catch (NoSuchAlgorithmException e) {
            } catch (CertificateException e) {
            } finally {
                if (fis != null) fis.close();
            }
        }
        
        private void clearAliasses() {
            aliasses = new TreeSet<KeyAliasBean>();
        }
        
        private void loadAliasses(final TreeSet<KeyAliasBean> oldAliassesWithPasswords) {
            aliasses = new TreeSet<KeyAliasBean>();
            try {
                final Enumeration e = store.aliases();
                while (e.hasMoreElements()) {
                    final String alias = (String) e.nextElement();
                    if (store.isKeyEntry(alias)) {
                        final KeyAliasBean newAlias = new KeyAliasBean(store, alias);
                        if (oldAliassesWithPasswords != null) {
                            newAlias.getAlias();
                            for (final KeyAliasBean oldKeyAliasBean : oldAliassesWithPasswords) {
                                if (newAlias.equals(oldKeyAliasBean)) {
                                    newAlias.setPassword(oldKeyAliasBean.getPassword());
                                    if (oldKeyAliasBean.isOpened())
                                        newAlias.open();
                                    break;
                                }
                            }
                        }
                        aliasses.add(newAlias);
                    }
                }
            } catch (KeyStoreException e1) {
                aliasses = new TreeSet<KeyAliasBean>();
            }
        }
        
        public void writeExternal(final ObjectOutput out) throws IOException {
            out.writeUTF(getKeyStorePath());
            out.writeUTF(getType()); // NOI18N
        }
        
        public void readExternal(final ObjectInput in) throws IOException {
            keyStoreFile = new File(in.readUTF());
            type = in.readUTF();
        }
        
        public KeyAliasBean addKeyToStore(final String alias, final String dname, String keyPass, int validity) throws IOException {
            if (isDefaultKeystore(this))
                return null;
            if (alias == null)
                return null;
            if (keyPass == null  ||  "".equals(keyPass)) keyPass = getPassword();
            if (validity == -1) validity = 180;
            final String as[] = {
                "-genkey", "-alias", alias, "-keyalg", "RSA", "-dname", dname, "-keystore", getKeyStorePath(), "-storepass", //NOI18N
                getPassword(), "-keypass", keyPass, "-storetype", getType(),"-validity", String.valueOf(validity)};//NOI18N
            
            try {
                KeyTool.main(as);
            } catch (Exception se) { /*catch System.exit */
                return null;
            }
            refresh();
            return getAlias(alias);
        }
        
        public KeyAliasBean createInvalidKeyAliasBean(final String alias) {
            final KeyAliasBean bean = new KeyAliasBean(store, alias);
            bean.invalidate();
            return bean;
        }
        
        public boolean removeAliasFromStore(final KeyAliasBean alias) throws IOException {
            if (isDefaultKeystore(this))
                return false;
            if (! opened  ||  alias == null)
                return false;
            try {
                store.deleteEntry(alias.getAlias());
                storeKeyStore();
                loadAliasses(aliasses);
                return true;
            } catch (KeyStoreException e) {
                return false;
            }
        }
        
        public static boolean equalFiles(File f1, File f2) {
            f1 = FileUtil.normalizeFile(f1);
            f2 = FileUtil.normalizeFile(f2);
            return f1 != null  &&  f1.equals(f2);
        }
        
        public boolean equals(final Object o) {
            if (o instanceof KeyStoreBean) {
                final KeyStoreBean b2 = (KeyStoreBean) o;
                return equalFiles(getKeyStoreFile(), b2.getKeyStoreFile());
            } else if (o instanceof String) {
                return equalFiles(getKeyStoreFile(), new File((String) o));
            } else
                return false;
        }
        
        public int hashCode() {
            final File f = getKeyStoreFile();
            return f != null ? FileUtil.normalizeFile(f).hashCode() : super.hashCode();
        }
        
        public static class KeyAliasBean implements Comparable {
            
            final private KeyStore store;
            final private String alias;
            private String password;
            private boolean opened;
            private boolean valid;
            private String issuerName;
            private String subjectName;
            private String serialNumber;
            private Date notBefore;
            private Date notAfter;
            private String md5;
            private String sha;
            
            public KeyAliasBean(KeyStore store, String alias) {
                this.store = store;
                this.alias = alias;
                opened = false;
                valid = true;
            }
            
            public void invalidate() {
                valid = false;
            }
            
            public void setPassword(final String password) {
                this.password = password;
            }
            
            public boolean isOpened() {
                return opened;
            }
            
            public boolean isValid() {
                return valid;
            }
            
            public boolean open() {
                if (opened)
                    return true;
                if (! valid  ||  alias == null  ||  password == null)
                    return false;
                try {
                    store.getKey(alias, password.toCharArray());
                    final Certificate cert = store.getCertificate(alias);
                    if (cert instanceof X509Certificate) {
                        final X509Certificate certx509 = (X509Certificate) cert;
                        subjectName = certx509.getSubjectDN().getName();
                        issuerName = certx509.getIssuerDN().getName();
                        serialNumber = certx509.getSerialNumber().toString(16).toUpperCase();
                        notBefore = certx509.getNotBefore();
                        notAfter = certx509.getNotAfter();
                        try {
                            final byte[] encoded = certx509.getEncoded();
                            md5 = createFingerPrint(encoded, "MD5".toUpperCase()); // NOI18N
                            sha = createFingerPrint(encoded, "SHA".toUpperCase()); // NOI18N
                        } catch (CertificateEncodingException e) {
                        }
                    }
                } catch (KeyStoreException e) {
                    return false;
                } catch (NoSuchAlgorithmException e) {
                    return false;
                } catch (UnrecoverableKeyException e) {
                    return false;
                }
                opened = true;
                return true;
            }
            
            public static String createFingerPrint(final byte[] encoded, final String algorithm) {
                MessageDigest messagedigest;
                try {
                    messagedigest = MessageDigest.getInstance(algorithm);
                } catch (NoSuchAlgorithmException e) {
                    return null;
                }
                messagedigest.update(encoded);
                final byte abyte1[] = messagedigest.digest();
                final StringBuffer stringbuffer = new StringBuffer();
                for (int i = 0; i < abyte1.length; i++) {
                    if (i != 0)
                        stringbuffer.append(':');//NOI18N
                    final int j = abyte1[i] & 0xff;
                    final String s1 = Integer.toHexString(j);
                    if (s1.length() == 1)
                        stringbuffer.append('0');//NOI18N
                    stringbuffer.append(s1);
                }
                return stringbuffer.toString();
            }
            
            public String getAlias() {
                return alias;
            }
            
            public String getPassword() {
                return password;
            }
            
            public String getIssuerName() {
                return issuerName;
            }
            
            public String getSubjectName() {
                return subjectName;
            }
            
            public String getSerialNumber() {
                return serialNumber;
            }
            
            public Date getNotBefore() {
                return notBefore == null ? null : (Date)notBefore.clone();
            }
            
            public Date getNotAfter() {
                return notAfter == null ? null : (Date)notAfter.clone();
            }
            
            public String getMd5() {
                return md5;
            }
            
            public String getSha() {
                return sha;
            }
            
            public boolean equals(final Object o) {
                if (o instanceof KeyAliasBean) {
                    return getAlias().equals(((KeyAliasBean) o).getAlias());
                } else if (o instanceof String) {
                    return getAlias().equals(o);
                } else
                    return false;
            }
            
            public int hashCode() {
                return getAlias().hashCode();
            }

            public int compareTo(Object o) {
                return getAlias().compareTo(o instanceof String ? (String)o : ((KeyAliasBean) o).getAlias());
            }
            
        }
        
        /**
         * Adds a PropertyChangeListener to the listener list.
         *
         * @param l The listener to add.
         */
        public void addPropertyChangeListener(final PropertyChangeListener l) {
            propertyChangeSupport.addPropertyChangeListener(l);
        }
        
        /**
         * Removes a PropertyChangeListener from the listener list.
         *
         * @param l The listener to remove.
         */
        public void removePropertyChangeListener(final PropertyChangeListener l) {
            propertyChangeSupport.removePropertyChangeListener(l);
        }
        
    }
    
    /**
     * Adds a PropertyChangeListener to the listener list.
     *
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }
    
    /**
     * Removes a PropertyChangeListener from the listener list.
     *
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    /**
     * Utility field used by bound properties.
     */
    java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    
    public void propertyChange(final PropertyChangeEvent evt) {
        propertyChangeSupport.firePropertyChange(evt);
    }
    
}

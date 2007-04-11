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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.spi.autoupdate.KeyStoreProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Jiri Rechtacek
 */
public class Utilities {
    
    private static Lookup.Result<KeyStoreProvider> result;
    
    public static Collection<KeyStore> getKeyStore () {
        if (result == null) {            
            result = Lookup.getDefault ().lookup (
                    new Lookup.Template<KeyStoreProvider> (KeyStoreProvider.class));
            result.addLookupListener (new KeyStoreProviderListener ());
        }
        Collection<? extends KeyStoreProvider> c = result.allInstances ();
        if (c == null || c.isEmpty ()) {
            return Collections.emptyList ();
        }
        List<KeyStore> kss = new ArrayList<KeyStore> ();
        
        for (KeyStoreProvider provider : c) {
            KeyStore ks = provider.getKeyStore ();
            if (ks != null) {
                kss.add (ks);
            }
        }
        
        return kss;
    }
    
    static private class KeyStoreProviderListener implements LookupListener {
        private KeyStoreProviderListener () {
        }
        
        public void resultChanged (LookupEvent ev) {
            result = null;
        }
    }
    
    /** Returns the platform installatiion directory.
     * @return the File directory.
     */
    public static File getPlatformDir () {
        return new File (System.getProperty ("netbeans.home")); // NOI18N
    }
    
    /** Returns enumeration of Files that represent each possible install
     * directory.
     * @param includeUserDir whether to include also user dir
     * @return List<File>
     */
    public static List<File> clusters (boolean includeUserDir) {
        List<File> files = new ArrayList<File> ();
        
        if (includeUserDir) {
            File ud = new File (System.getProperty ("netbeans.user"));  // NOI18N
            files.add (ud);
        }
        
        
        String dirs = System.getProperty("netbeans.dirs"); // NOI18N
        if (dirs != null) {
            Enumeration en = new StringTokenizer (dirs, File.pathSeparator);
            while (en.hasMoreElements ()) {
                File f = new File ((String) en.nextElement ());
                files.add (f);
            }
        }
        
        
        File id = getPlatformDir ();
        files.add (id);
        
        return Collections.unmodifiableList (files);
    }
    
    private static final String ELEMENT_MODULES = "module_updates"; // NOI18N
    private static final String ELEMENT_MODULE = "module"; // NOI18N
    private static final String ATTR_CODENAMEBASE = "codenamebase"; // NOI18N
    private static final String ATTR_NAME = "name"; // NOI18N
    private static final String ATTR_SPEC_VERSION = "specification_version"; // NOI18N
    private static final String ATTR_SIZE = "size"; // NOI18N
    private static final String ATTR_NBM_NAME = "nbm_name"; // NOI18N
    
    /** Platform dependent file name separator */
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");    
    /** Relative name of update directory */
    private static final String UPDATE_DIR = "update"; // NOI18N    
    /** Relative name of directory where the .NBM files are downloaded */
    private static final String DOWNLOAD_DIR =UPDATE_DIR + FILE_SEPARATOR + "download"; // NOI18N    
    /** The name of the install_later file */
    public static final String LATER_FILE_NAME = "install_later.xml"; // NOI18N
    
    private static File getInstall_Later(File root) {
        File file = new File(root.getPath() + FILE_SEPARATOR + DOWNLOAD_DIR + FILE_SEPARATOR + LATER_FILE_NAME);
        return file;
    }

    public static void deleteInstall_Later() {
        List/*<File>*/ clusters = clusters(true);
        assert clusters != null : "Clusters cannot be empty."; // NOI18N
        Iterator iter =  clusters.iterator();
        while (iter.hasNext()) {
            File installLaterFile = getInstall_Later((File)iter.next());
            if (installLaterFile != null && installLaterFile.exists()) {
                installLaterFile.delete();                
            }
        }                                
    }
    
    public static void writeInstall_Later(Map<UpdateElementImpl,File> updates) {
        // loop for all clusters and write if needed
        List/*<File>*/ clusters = clusters(true);
        assert clusters != null : "Clusters cannot be empty."; // NOI18N
        Iterator iter =  clusters.iterator();
        while (iter.hasNext()) {
            writeToCluster((File)iter.next(), updates);
        }
    }
    
    private static void writeToCluster (File cluster, Map<UpdateElementImpl,File> updates) {
        Document document = XMLUtil.createDocument(ELEMENT_MODULES, null, null, null);                
        
        Element root = document.getDocumentElement();
        Element module = null;
        Iterator<UpdateElementImpl> it = updates.keySet().iterator();
        boolean empty = true;
        while ( it.hasNext() ) {
            UpdateElementImpl elementImpl = it.next();
            
            File c = updates.get(elementImpl);
            // pass this module to given cluster ?
            if (cluster.equals (c)) {
                module = document.createElement(ELEMENT_MODULE);
                module.setAttribute(ATTR_CODENAMEBASE, elementImpl.getCodeName());
                module.setAttribute(ATTR_NAME, elementImpl.getDisplayName());
                module.setAttribute(ATTR_SPEC_VERSION, elementImpl.getSpecificationVersion().toString());
                module.setAttribute(ATTR_SIZE, Long.toString(elementImpl.getDownloadSize()));
                module.setAttribute(ATTR_NBM_NAME, InstallSupportImpl.getDestination(cluster, elementImpl.getCodeName(), true).getName());

                root.appendChild( module );
                empty = false;
            }
        }
        
        if ( empty )
            return;

        document.getDocumentElement().normalize();
                
        File installLaterFile = getInstall_Later (cluster);
        installLaterFile.getParentFile ().mkdirs ();
        InputStream is = null;
        ByteArrayOutputStream  bos = new ByteArrayOutputStream ();        
        OutputStream fos = null;            
            try {
                try {
                    XMLUtil.write(document, bos, "UTF-8"); // NOI18N
                    if (bos != null) bos.close();
                    fos = new FileOutputStream(installLaterFile);
                    is = new ByteArrayInputStream(bos.toByteArray());
                    FileUtil.copy(is,fos);
                } finally {
                    if (is != null) is.close();
                    if (fos != null) fos.close();
                    if (bos != null) bos.close();
                }                
            } catch (java.io.FileNotFoundException fnfe) {
                Exceptions.printStackTrace(fnfe);
            } catch (java.io.IOException ioe) {
                Exceptions.printStackTrace(ioe);
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (Exception x) {
                        Exceptions.printStackTrace(x);
                    }
                }
            }
            
        }
    
}

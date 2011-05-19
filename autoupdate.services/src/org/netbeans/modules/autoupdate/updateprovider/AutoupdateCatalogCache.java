/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.autoupdate.services.AutoupdateSettings;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Jiri Rechtacek
 * @author Dmitry Lipin
 */
public final class AutoupdateCatalogCache {
    private File cacheDir;
    
    
    private static AutoupdateCatalogCache INSTANCE;
    
    private static final Logger err = Logger.getLogger (AutoupdateCatalogCache.class.getName());
    
    public static synchronized AutoupdateCatalogCache getDefault () {
        if (INSTANCE == null) {
            INSTANCE = new AutoupdateCatalogCache ();
            INSTANCE.initCacheDirectory ();
        }
        return INSTANCE;
    }
    
    private synchronized File getCatalogCache () {
        assert cacheDir != null && cacheDir.exists ();
        return cacheDir;
    }
    
    private void initCacheDirectory () {
        assert cacheDir == null : "Do initCacheDirectory only once!";
        String userDir = System.getProperty("netbeans.user"); // NOI18N
        if (userDir != null) {
            cacheDir = new File (new File (new File (userDir, "var"), "cache"), "catalogcache"); // NOI18N
        } else {
            File dir = FileUtil.toFile (FileUtil.getConfigRoot());
            assert dir != null : "Provide netbeans.user so we know where to store catalogcache!";
            cacheDir = new File(dir, "catalogcache"); // NOI18N
        }
        cacheDir.mkdirs();
        getLicenseDir().mkdirs();
        err.log (Level.FINE, "getCacheDirectory: {0}", cacheDir.getPath ());
        return;
    }
    
    public URL writeCatalogToCache (String codeName, URL original) throws IOException {
            URL url = null;
            File dir = getCatalogCache ();
            assert dir != null && dir.exists () : "Cache directory must exist.";
            File cache = new File (dir, codeName);

            copy(original, cache, false);

            try {
                url = cache.toURI ().toURL ();
            } catch (MalformedURLException ex) {
                assert false : ex;
            }
            synchronized (getLock(cache)) {
                assert cache.exists() : "Cache " + cache + " exists.";
                err.log(Level.FINER, "Cache file {0} was wrote from original URL {1}", new Object[]{cache, original});
                if (cache.exists() && cache.length() == 0) {
                    err.log(Level.INFO, "Written cache size is zero bytes");
                }
            }
            return url;        
    }
    
    public URL getCatalogURL(String codeName) {
        File dir = getCatalogCache();
        assert dir != null && dir.exists() : "Cache directory must exist.";
        File cache = new File(dir, codeName);
        synchronized (getLock(cache)) {
            if (cache != null && cache.exists()) {
                if (cache.length() == 0) {
                    err.log(Level.INFO, "Cache file {0} exists and of zero size", cache);
                    return null;
                }
                URL url = null;
                try {
                    url = cache.toURI().toURL();
                } catch (MalformedURLException ex) {
                    assert false : ex;
                }
                return url;
            } else {
                return null;
            }
        }
    }
    private File getLicenseDir() {
        return new File(getCatalogCache(), "licenses");
    }

    private File getLicenseFile(String name) {
        return new File(getLicenseDir(), name);
    }

    public String getLicense(String name) {
        return getLicense(name, null);
    }

    public String getLicense(String name, URL url) {
        File file = getLicenseFile(name);
        synchronized (name.intern()) {            
            if (!file.exists()) {
                if (url == null) {
                    return null;
                }
                try {
                    copy(url, file, true);
                } catch (IOException e) {
                    // if can`t get the license, treat it as empty but delete it on exit
                    err.log(Level.INFO, "Can`t store license from " + url + " to " + file, e);
                    try {
                        if (file.exists()) {
                            file.delete();
                        }
                        file.createNewFile();
                         //in case of error remove the license file and try to download it on the next start
                        file.deleteOnExit();

                    } catch (IOException ex) {
                        err.log(Level.INFO, "Can`t create empty license file", ex);
                    }
                }
            }
            return readLicenseFile(name);
        }
    }

    public void storeLicense(String name, String content) {
        File file = getLicenseFile(name);
        synchronized (name.intern()) {            
            if (file.exists() || content == null) {
                return;
            }
            writeToFile(content, file);
        }
    }
    
    private String readLicenseFile(String name) {
        File file = getLicenseFile(name);
        FileInputStream fr = null;
        synchronized (name.intern()) {
            try {
                fr = new FileInputStream(file);
                byte[] buffer = new byte[8192];
                int n = 0;
                StringBuilder sb = new StringBuilder();
                while ((n = fr.read(buffer)) != -1) {
                    sb.append(new String(buffer, 0, n, "utf-8"));//NOI18N
                }
                return sb.toString();
            } catch (IOException e) {
                err.log(Level.INFO, "Can`t read license from file " + file, e);
                return null;
            } finally {
                if (fr != null) {
                    try {
                        fr.close();
                    } catch (IOException e) {
                        err.log(Level.INFO, "Can`t read close input stream for " + file, e);
                    }
                }
            }
        }
    }
    private void writeToFile(String content, File file) {
        FileOutputStream fw = null;
        try {
            fw = new FileOutputStream(file);
            fw.write(content.getBytes("utf-8")); //NOI18N
        } catch (IOException e) {
            err.log(Level.INFO, "Can`t write to " + file, e);
        } finally {
            if (fw != null) {
                try {
                    fw.flush();
                    fw.close();
                } catch (IOException e) {
                    err.log(Level.INFO, "Can`t output stream for " + file, e);
                }
            }
        }
    }
    
    private void copy (final URL sourceUrl, final File cache, final boolean allowZeroSize) throws IOException {
        // -- create NetworkListener
        // -- request stream
        // -- report success or IOException
        // -- if success then do copy
        
        err.log(Level.FINE, "Processing URL: {0}", sourceUrl); // NOI18N
        
        String prefix = "";
        while (prefix.length () < 3) {
            prefix += cache.getName();
        }
        final File temp = File.createTempFile (prefix, null, cache.getParentFile ()); //NOI18N
        temp.deleteOnExit();        

        DownloadListener nwl = new DownloadListener(sourceUrl, temp, allowZeroSize);
        
        NetworkAccess.Task task = NetworkAccess.createNetworkAcessTask (sourceUrl, AutoupdateSettings.getOpenConnectionTimeout (), nwl);
        task.waitFinished ();
        nwl.notifyException ();
        synchronized(getLock(cache)) {
            updateCachedFile(cache, temp);
        }
    }

    public String getLock(File cache) {
        return cache.getAbsolutePath().intern();
    }
    public String getLock(URL cache) throws IOException {
        try {
            return getLock(new File(cache.toURI()));
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }
    
    private void updateCachedFile(File cache, File temp) {
        if (cache.exists() && !cache.delete()) {
            err.log(Level.INFO, "Cannot delete cache {0}", cache);
            try {
               Thread.sleep(200);
            } catch (InterruptedException ie) {
                assert false : ie;
            }
            cache.delete();
        }

        if (temp.length() == 0) {
            err.log(Level.INFO, "Temp cache size is zero bytes");
        }

        if (!temp.renameTo(cache)) {
            err.log(Level.INFO, "Cannot rename temp {0} to cache {1}", new Object[]{temp, cache});
            err.log(Level.INFO, "Trying to copy {0} to cache {1}", new Object[] {temp, cache});
            try {
                FileOutputStream os = new FileOutputStream(cache);
                FileInputStream is = new FileInputStream(temp);
                FileUtil.copy(is, os);
                os.close();
                is.close();
                temp.delete();
            } catch (IOException ex) {
                err.log(Level.INFO, "Cannot even copy: {0}", ex.getMessage());
                err.log(Level.FINE, null, ex);
            }
        }

        if (cache.exists() && cache.length() == 0) {
            err.log(Level.INFO, "Final cache size is zero bytes");
        }
    }
}

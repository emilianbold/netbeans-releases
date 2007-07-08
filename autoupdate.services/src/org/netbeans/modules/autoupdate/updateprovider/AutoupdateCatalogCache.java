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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.autoupdate.services.AutoupdateSettings;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateCatalogCache {
    private File cacheDir;
    private Exception storedException;
    
    private static AutoupdateCatalogCache INSTANCE;
    
    private Logger err = Logger.getLogger (this.getClass ().getName ());
    
    public static AutoupdateCatalogCache getDefault () {
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
            File dir = FileUtil.toFile (Repository.getDefault ().getDefaultFileSystem ().getRoot());
            cacheDir = new File(dir, "catalogcache"); // NOI18N
        }
        cacheDir.mkdirs();
        err.log (Level.FINE, "getCacheDirectory: " + cacheDir.getPath ());
        return;
    }
    
    public URL writeCatalogToCache (String codeName, URL original) throws IOException {
        synchronized(AutoupdateCatalogCache.class) {
            URL url = null;
            File dir = getCatalogCache ();
            assert dir != null && dir.exists () : "Cache directory must exist.";
            File cache = new File (dir, codeName);

            copy (original, cache);

            try {
                url = cache.toURI ().toURL ();
            } catch (MalformedURLException ex) {
                assert false : ex;
            }
            assert new File (dir, codeName).exists () : "Cache " + cache + " exists.";
            err.log (Level.FINER, "Cache file " + cache + " was wrote from original URL " + original);
            return url;
        }
    }
    
    public synchronized URL getCatalogURL (String codeName) {
        File dir = getCatalogCache ();
        assert dir != null && dir.exists () : "Cache directory must exist.";
        File cache = new File (dir, codeName);
        
        if (cache != null && cache.exists ()) {
            URL url = null;
            try {
                url = cache.toURI ().toURL ();
            } catch (MalformedURLException ex) {
                assert false : ex;
            }
            return url;
        } else {
            return null;
        }
    }
    
    private void copy (final URL sourceUrl, final File cache) throws IOException {
        // -- create NetworkListener
        // -- request stream
        // -- report success or IOException
        // -- if success then do copy
        
        err.log(Level.INFO, "Processing URL: " + sourceUrl); // NOI18N
        
        String prefix = "";
        while (prefix.length () < 3) {
            prefix += cache.getName();
        }
        final File temp = File.createTempFile (prefix, null, cache.getParentFile ()); //NOI18N
        temp.deleteOnExit();
        storeException (null);

        NetworkAccess.NetworkListener nwl = new NetworkAccess.NetworkListener () {

            public void streamOpened (InputStream stream) {
                err.log (Level.FINE, "Successfully read URI " + sourceUrl);
                doCopy (sourceUrl, stream, cache, temp);
            }

            public void accessCanceled () {
                err.log (Level.FINE, "Processing " + sourceUrl + " was cancelled.");
                storeException (new IOException ("Processing " + sourceUrl + " was cancelled."));
            }

            public void accessTimeOut () {
                err.log (Level.FINE, "Timeout when processing " + sourceUrl);
                storeException (new IOException ("Timeout when processing " + sourceUrl));
            }

            public void notifyException (Exception x){
                err.log (Level.INFO,
                            "Reading URL " + sourceUrl + " failed (" + x +
                            ")");
                storeException (x);
            }
            
        };
        
        NetworkAccess.Task task = NetworkAccess.createNetworkAcessTask (sourceUrl, AutoupdateSettings.getOpenConnectionTimeout (), nwl);
        task.waitFinished ();
        notifyException ();
    }
    
    private void notifyException () throws IOException {
        if (isExceptionStored ()) {
            throw new IOException (getStoredException ().getLocalizedMessage ());
        }
    }
    
    private boolean isExceptionStored () {
        return storedException != null;
    }
    
    private void storeException (Exception x) {
        storedException = x;
    }
    
    private Exception getStoredException () {
        return storedException;
    }
    
    private void doCopy (URL sourceUrl, InputStream is, File cache, File temp) {
        
        FileOutputStream os = null;
        int read = 0;
        
        try {
            os = new FileOutputStream (temp);
            while ((read = is.read ()) != -1) {
                os.write (read);
            }   
        } catch (IOException ioe) {
            err.log (Level.INFO, "Writing content of URL " + sourceUrl + " failed.", ioe);
        } finally {
            try {
                if (is != null) is.close ();
                if (os != null) os.flush ();
                if (os != null) os.close ();
                synchronized (this) {
                    cache.delete ();
                    if (! temp.renameTo (cache)) {
                        throw new IOException ("Cannot move temp " + temp + " to cache " + cache);
                    }
                }                    
            } catch (IOException ioe) {
                err.log (Level.INFO, "Closing streams failed.", ioe);
            }
        }
        
    }
    
}

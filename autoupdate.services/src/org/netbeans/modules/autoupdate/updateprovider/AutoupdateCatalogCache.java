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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;
import org.xml.sax.InputSource;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateCatalogCache {
    private static String GZIP_EXTENSION = ".gz"; // NOI18N
    
    private File cacheDir;
    private static AutoupdateCatalogCache INSTANCE;
    
    private Logger err = Logger.getLogger (this.getClass ().getName ());
    
    public static AutoupdateCatalogCache getDefault () {
        if (INSTANCE == null) {
            INSTANCE = new AutoupdateCatalogCache ();
            INSTANCE.initCacheDirectory ();
        }
        return INSTANCE;
    }
    
    public File getCatalogCache () {
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
        return url;
    }
    
    public URL getCatalogURL (String codeName) {
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
    
    private void copy (URL sourceUrl, File dest) throws IOException {
        err.log(Level.INFO, "Processing URL: " + sourceUrl); // NOI18N

        URL urlToGZip = null;
        Reader reader = null;
        try {
            
            String gzipFile = sourceUrl.getPath () + GZIP_EXTENSION;
            String query = sourceUrl.getQuery ();
            if (query != null && query.trim ().length () > 0) {
                gzipFile = gzipFile + '?' + query; // NOI18N
            }
            urlToGZip = new URL (sourceUrl.getProtocol (), sourceUrl.getHost (), sourceUrl.getPort (), gzipFile);
            
            reader = new BufferedReader (new InputStreamReader (new GZIPInputStream (urlToGZip.openStream ())));
            err.log(Level.FINE, "Successfully read URL " + urlToGZip); // NOI18N
            
        } catch (IOException ioe) {
            try {
                err.log(Level.FINE,
                        "Reading GZIP URL " + urlToGZip + " failed (" + ioe +
                        "). Try read XML directly " + sourceUrl);
                reader = new BufferedReader (new InputStreamReader (sourceUrl.openStream ()));
                err.log(Level.FINE, "Successfully read URI " + sourceUrl);
            } catch (IOException ex) {
                err.log(Level.FINE,
                        "Reading URL " + sourceUrl + " failed (" + ioe +
                        ")");
                throw ex;
            }
        }
        
        Writer writer = null;
        int read = 0;
        
        try {
            writer = new BufferedWriter (new FileWriter (dest));
            while ((read = reader.read ()) != -1) {
                writer.write (read);
            }
        } catch (IOException ioe) {
            err.log (Level.INFO, "Writing content of URL " + sourceUrl + " failed.", ioe);
        } finally {
            try {
                if (reader != null) reader.close ();
                if (writer != null) writer.flush ();
                if (writer != null) writer.close ();
            } catch (IOException ioe) {
                err.log (Level.INFO, "Closing streams failed.", ioe);
            }
        }
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.autoupdate.services.AutoupdateSettings;
import org.openide.filesystems.FileUtil;

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
            File dir = FileUtil.toFile (FileUtil.getConfigRoot());
            cacheDir = new File(dir, "catalogcache"); // NOI18N
        }
        cacheDir.mkdirs();
        err.log (Level.FINE, "getCacheDirectory: " + cacheDir.getPath ());
        return;
    }
    
    public URL writeCatalogToCache (String codeName, URL original) throws IOException {
        synchronized(codeName.intern ()) {
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
            if(cache.exists() && cache.length()==0) {
                err.log (Level.INFO, "Written cache size is zero bytes");
            }
            return url;
        }
    }
    
    public synchronized URL getCatalogURL (String codeName) {
        File dir = getCatalogCache ();
        assert dir != null && dir.exists () : "Cache directory must exist.";
        File cache = new File (dir, codeName);
        
        if (cache != null && cache.exists ()) {
            if(cache.length() == 0) {
                err.log(Level.INFO, "Cache file " + cache + " exists and of zero size");
                return null;
            }
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

            public void streamOpened (InputStream stream, int contentLength) {
                err.log (Level.FINE, "Successfully started reading URI " + sourceUrl);
                try {
                    doCopy (sourceUrl, stream, cache, temp, contentLength);
                } catch (IOException ex) {
                    storeException (ex);
                }
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
    
    private void doCopy (URL sourceUrl, InputStream is, File cache, File temp, int contentLength) throws IOException {
        
        OutputStream os = null;
        int read = 0;
        int totalRead = 0;
        
        try {
            os = new BufferedOutputStream(new FileOutputStream (temp));            
            byte [] bytes = new byte [1024];
            while ((read = is.read (bytes)) != -1) {
                os.write (bytes, 0, read);
                totalRead+=read;
            }
            is.close ();
            os.flush ();
            os.close ();
            os = null;
            if(contentLength!=-1 && contentLength!=totalRead) {
                err.log(Level.INFO, "Content length was reported as " + contentLength + " bytes, but read " + totalRead + " bytes from " + sourceUrl);
                throw new IOException("unexpected closed connection to " + sourceUrl);
            }
            if(totalRead==0) {
                err.log(Level.INFO, "Connection content length was " + contentLength + " bytes (read " + totalRead + "bytes), catalog size can`t be that size - likely server with catalog at " + sourceUrl + " is temporary down");
                throw new IOException("zero sized catalog reported at " + sourceUrl);
            }
            synchronized (this) {
                if (cache.exists () && ! cache.delete ()) {
                    err.log (Level.INFO, "Cannot delete cache " + cache);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ie) {
                        assert false : ie;
                    }
                    cache.delete();
                }
            }
            err.log (Level.INFO, "Read " + totalRead + " bytes from catalog at " + sourceUrl);
            
            if(temp.length()==0) {
                err.log (Level.INFO, "Temp cache size is zero bytes");
            }
            if (! temp.renameTo (cache)) {
                err.log (Level.INFO, "Cannot rename temp " + temp + " to cache " + cache);
            }
            if(cache.exists() && cache.length()==0) {
                err.log (Level.INFO, "Final cache size is zero bytes");
            }
        } catch (IOException ioe) {
            err.log (Level.INFO, "Writing content of URL " + sourceUrl + " failed.", ioe);
            throw ioe;
        } finally {
            try {
                if (is != null) is.close ();
                if (os != null)  {
                    os.flush ();
                    os.close ();
                }
            } catch (IOException ioe) {
                err.log (Level.INFO, "Closing streams failed.", ioe);
            }
        }
        
    }
    
}

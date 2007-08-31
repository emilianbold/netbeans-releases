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

package org.netbeans.core.startup.layers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.startup.StartLog;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Layered file system serving itself as either the user or installation layer.
 * Holds one layer of a writable system directory, and some number
 * of module layers.
 * @author Jesse Glick, Jaroslav Tulach
 */
public class ModuleLayeredFileSystem extends MultiFileSystem 
implements LookupListener {
    /** serial version UID */
    private static final long serialVersionUID = 782910986724201983L;
    
    private static final String LAYER_STAMP = "layer-stamp.txt";
    
    static final Logger err = Logger.getLogger("org.netbeans.core.projects"); // NOI18N

    /** lookup result we listen on */
    private static Lookup.Result<FileSystem> result = Lookup.getDefault().lookupResult(FileSystem.class);
    
    /** current list of URLs - r/o; or null if not yet set */
    private List<URL> urls;
    /** cache manager */
    private LayerCacheManager manager;
    /** writable layer */
    private final FileSystem writableLayer;
    /** cache layer */
    private FileSystem cacheLayer;
    /** other layers */
    private final FileSystem[] otherLayers;
    /** addLookup */
    private final boolean addLookup;

    /** Create layered filesystem based on a supplied writable layer.
     * @param userDir is this layer for modules from userdir or not?
     * @param writableLayer the writable layer to use, typically a LocalFileSystem
     * @param otherLayers some other layers to use, e.g. LocalFileSystem[]
     * @param cacheDir a directory in which to store a cache, or null for no caching
     */
    ModuleLayeredFileSystem (FileSystem writableLayer, boolean userDir, FileSystem[] otherLayers, File cacheDir) throws IOException {
        this(writableLayer, userDir, otherLayers, manager(cacheDir));
    }
    
    private ModuleLayeredFileSystem(FileSystem writableLayer, boolean addLookup, FileSystem[] otherLayers, LayerCacheManager mgr) throws IOException {
        this(writableLayer, addLookup, otherLayers, mgr, loadCache(mgr));
    }
    
    private ModuleLayeredFileSystem(FileSystem writableLayer, boolean addLookup, FileSystem[] otherLayers, LayerCacheManager mgr, FileSystem cacheLayer) throws IOException {
        super(appendLayers(writableLayer, addLookup, otherLayers, cacheLayer));
        this.manager = mgr;
        this.writableLayer = writableLayer;
        this.otherLayers = otherLayers;
        this.cacheLayer = cacheLayer;
        this.addLookup = addLookup;
        
        // Wish to permit e.g. a user-installed module to mask files from a
        // root-installed module, so propagate masks up this high.
        // SystemFileSystem leaves this off, so that the final file system
        // will not show them if there are some left over.
        setPropagateMasks (true);
        
        urls = null;

        if (addLookup) {
            result.addLookupListener(this);
            result.allItems();
        }
        
    }
    
    private static LayerCacheManager manager(File cacheDir) throws IOException {
        if (cacheDir != null) {
            if (!cacheDir.isDirectory()) {
                if (!cacheDir.mkdirs()) {
                    throw new IOException("Could not make dir: " + cacheDir); // NOI18N
                }
            }
            String defaultManager = "org.netbeans.core.startup.layers.BinaryCacheManager"; // NOI18N
            String managerName = System.getProperty("netbeans.cache.layers", defaultManager); // NOI18N
            if (managerName.equals("-")) { // NOI18N
                err.fine("Cache manager disabled");
                return LayerCacheManager.emptyManager();
            }
            try {
                Class<?> c = Class.forName(managerName);
                Constructor ctor = c.getConstructor(File.class);
                LayerCacheManager mgr = (LayerCacheManager)ctor.newInstance(cacheDir);
                err.fine("Using cache manager of type " + managerName + " in " + cacheDir);
                return mgr;
            } catch (Exception e) {
                throw (IOException) new IOException(e.toString()).initCause(e);
            }
        } else {
            err.fine("No cache manager");
            return LayerCacheManager.emptyManager();
        }
    }
    
    private static FileSystem loadCache(LayerCacheManager mgr) throws IOException {
        if (mgr.cacheExists()) {
            // XXX use Events to log!
            setStatusText(
                NbBundle.getMessage(ModuleLayeredFileSystem.class, "MSG_start_load_cache"));
            String msg = "Loading layers from " + mgr.getCacheDirectory(); // NOI18N
            StartLog.logStart(msg);
            FileSystem fs;
            try {
                fs = mgr.createLoadedFileSystem();
            } catch (IOException ioe) {
                err.log(Level.WARNING, null, ioe);
                mgr.cleanupCache();
                cleanStamp(mgr.getCacheDirectory());
                fs = mgr.createEmptyFileSystem();
            }
            setStatusText(
                NbBundle.getMessage(ModuleLayeredFileSystem.class, "MSG_end_load_cache"));
            StartLog.logEnd(msg);
            return fs;
        } else {
            return mgr.createEmptyFileSystem();
        }
    }
    
    private static FileSystem[] appendLayers(FileSystem fs1, boolean addLookup, FileSystem[] fs2s, FileSystem fs3) {
        List<FileSystem> l = new ArrayList<FileSystem>(fs2s.length + 2);
        l.add(fs1);
        if (addLookup) {
            Collection<? extends FileSystem> fromLookup = result.allInstances();
            l.addAll(fromLookup);
        }
        l.addAll(Arrays.asList(fs2s));
        l.add(fs3);
        return l.toArray(new FileSystem[l.size()]);
    }

    private static void cleanStamp(File cacheDir) throws IOException {
        File stampFile = new File(cacheDir, LAYER_STAMP);
        if (stampFile.exists() && ! stampFile.delete()) {
            throw new IOException("Could not delete: " + stampFile); // NOI18N
        }
    }
    
    /** Get all layers.
     * @return all filesystems making layers
     */
    public/*but just for debugging*/ final FileSystem[] getLayers () {
        return getDelegates ();
    }

    /** Get the writable layer.
     * @return the writable layer
     */
    final FileSystem getWritableLayer () {
        return writableLayer;
    }
    
    /** Get the installation layer.
     * You can take advantage of the specialized return type
     * if working within the core.
     */
    public static ModuleLayeredFileSystem getInstallationModuleLayer () {
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem();
        SystemFileSystem sfs = (SystemFileSystem)fs;            
        ModuleLayeredFileSystem home = sfs.getInstallationLayer ();
        if (home != null)
            return home;
        else
            return sfs.getUserLayer ();
    }    
    
    /** Get the user layer.
     * You can take advantage of the specialized return type
     * if working within the core.
     */
    public static ModuleLayeredFileSystem getUserModuleLayer () {
        SystemFileSystem sfs = (SystemFileSystem)
            Repository.getDefault().getDefaultFileSystem();
        return sfs.getUserLayer ();
    }

    /** Change the list of module layers URLs.
     * @param urls the urls describing module layers to use. List<URL>
     */
    public void setURLs (final List<URL> urls) throws Exception {
        if (urls.contains(null)) throw new NullPointerException("urls=" + urls); // NOI18N
        if (err.isLoggable(Level.FINE)) {
            err.fine("setURLs: " + urls);
        }
        if (this.urls != null && urls.equals(this.urls)) {
            err.fine("no-op");
            return;
        }
        
        StartLog.logStart("setURLs"); // NOI18N
        
        final File stampFile;
        final Stamp stamp;
        final File cacheDir = manager.getCacheDirectory();
        if (cacheDir != null) {
            stampFile = new File(cacheDir, LAYER_STAMP);
            stamp = new Stamp(manager.getClass().getName(), urls);
        } else {
            stampFile = null;
            stamp = null;
        }
        if (cacheDir != null && stampFile.isFile()) {
            err.fine("Stamp of new URLs: " + stamp.getHash());
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(stampFile), "UTF-8")); // NOI18N
            try {
                String line = r.readLine();
                long hash;
                try {
                    hash = Long.parseLong(line);
                } catch (NumberFormatException nfe) {
                    throw new IOException(nfe.toString());
                }
                err.fine("Stamp in the cache: " + hash);
                if (hash == stamp.getHash()) {
                    err.fine("Cache hit!");
                    this.urls = urls;
                    StartLog.logEnd("setURLs"); // NOI18N
                    return;
                }
            } finally {
                r.close();
            }
        }

        // #17656: don't hold synch lock while firing changes, it could be dangerous...
        runAtomicAction(new AtomicAction() {
            public void run() throws IOException {
                synchronized (ModuleLayeredFileSystem.this) {
                    if (cacheDir != null) {
                        setStatusText(
                            NbBundle.getMessage(ModuleLayeredFileSystem.class, "MSG_start_rewrite_cache"));
                        err.fine("Rewriting cache in " + cacheDir);
                    } // else if null -> we are using emptyManager, so do not print confusing messages
                        try {
                            if (manager.supportsLoad()) {
                                manager.store(cacheLayer, urls);
                            } else {
                                cacheLayer = manager.store(urls);
                                setDelegates(appendLayers(writableLayer, addLookup, otherLayers, cacheLayer));
                            }
                        } catch (IOException ioe) {
                            err.log(Level.WARNING, null, ioe);
                            err.fine("Abandoning cache manager");
                            manager.cleanupCache();
                            cleanStamp(cacheDir);
                            manager = LayerCacheManager.emptyManager();
                            // Try again with no-op manager.
                            try {
                                if (manager.supportsLoad()) {
                                    cacheLayer = manager.createEmptyFileSystem();
                                    manager.store(cacheLayer, urls);
                                } else {
                                    cacheLayer = manager.store(urls);
                                }
                                setDelegates(appendLayers(writableLayer, addLookup, otherLayers, cacheLayer));
                            } catch (IOException ioe2) {
                                // More serious - should not happen.
                                err.log(Level.WARNING, null, ioe2);
                            }
                            return;
                        }
                    if (stampFile != null) {
                        // Write out new stamp too.
                        Writer wr = new OutputStreamWriter(new FileOutputStream(stampFile), "UTF-8"); // NOI18N
                        try {
                            // Would be nice to write out as zero-padded hex.
                            // Unfortunately while Long.toHexString works fine,
                            // Long.parseLong cannot be asked to parse unsigned longs,
                            // so fails when the high bit is set.
                            wr.write(Long.toString(stamp.getHash()));
                            wr.write("\nLine above is identifying hash key, do not edit!\nBelow is metadata about layer cache, for debugging purposes.\n"); // NOI18N
                            wr.write(stamp.toString());
                        } finally {
                            wr.close();
                        }
                    }
                    if (cacheDir != null) {
                        setStatusText(
                            NbBundle.getMessage(ModuleLayeredFileSystem.class, "MSG_end_rewrite_cache"));
                        err.fine("Finished rewriting cache in " + cacheDir);
                    }
                }
            }
        });
        
        this.urls = urls;
        firePropertyChange ("layers", null, null); // NOI18N
        
        StartLog.logEnd("setURLs"); // NOI18N
    }
    
    /** Adds few URLs.
     */
    public void addURLs(Collection<URL> urls) throws Exception {
        if (urls.contains(null)) throw new NullPointerException("urls=" + urls); // NOI18N
        // Add to the front: #23609.
        ArrayList<URL> arr = new ArrayList<URL>(urls);
        if (this.urls != null) arr.addAll(this.urls);
        setURLs(arr);
    }
    
    /** Removes few URLs.
     */
    public void removeURLs(Collection<URL> urls) throws Exception {
        if (urls.contains(null)) throw new NullPointerException("urls=" + urls); // NOI18N
        ArrayList<URL> arr = new ArrayList<URL>();
        if (this.urls != null) arr.addAll(this.urls);
        arr.removeAll(urls);
        setURLs(arr);
    }
    
    /** Refresh layers */
    public void resultChanged(org.openide.util.LookupEvent ev) {
        setDelegates(appendLayers(writableLayer, addLookup, otherLayers, cacheLayer));
    }
    
    /** Represents a hash of a bunch of jar: URLs and the associated JAR timestamps.
     */
    private static final class Stamp implements Comparator<URL> {
        private final String managerName;
        private final List<URL> urls;
        private final long[] times;
        private final long hash;
        public Stamp(String name, List<URL> urls) throws IOException {
            managerName = name;
            this.urls = new ArrayList<URL>(urls);
            Collections.sort(this.urls, this);
            times = new long[this.urls.size()];
            long x = 17L ^ managerName.hashCode();
            Iterator it = this.urls.iterator();
            int i = 0;
            while (it.hasNext()) {
                URL u = (URL)it.next();
                String s = u.toExternalForm();
                x += 3199876987199633L;
                x ^= s.hashCode();
                URL u2;
                if (s.startsWith("jar:")) { // NOI18N
                    int bangSlash = s.lastIndexOf("!/"); // NOI18N
                    if (bangSlash != -1) {
                        // underlying URL inside jar:, generally file:
                        u2 = new URL(s.substring(4, bangSlash));
                    } else {
                        err.warning("Weird JAR URL: " + u);
                        u2 = u;
                    }
                } else {
                    // something else... plain file: URL?
                    u2 = u;
                }
                File extracted = new File(URI.create(u2.toExternalForm()));
                if (extracted != null) {
                    // the JAR file containing the layer entry:
                    x ^= (times[i++] = extracted.lastModified());
                } else {
                    // not a file: or jar:file: URL?
                    times[i++] = 0L;
                }
            }
            hash = x;
        }
        public long getHash() {
            return hash;
        }
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append(managerName);
            buf.append('\n');
            Iterator<URL> it = urls.iterator();
            int i = 0;
            while (it.hasNext()) {
                long t = times[i++];
                if (t == 0L) {
                    buf.append("<file not found>"); // NOI18N
                } else {
                    buf.append(new Date(t));
                }
                buf.append('\t').append(it.next()).append('\n');
            }
            return buf.toString();
        }
        public int compare(URL o1, URL o2) {
            return o1.toString().compareTo(o2.toString());
        }
    }
    private static void setStatusText (String msg) {
        org.netbeans.core.startup.Main.setStatusText(msg);
    }

}

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup.layers;

import java.beans.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.net.*;
import java.util.*;

import org.openide.ErrorManager;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.util.NbBundle;

import org.netbeans.core.startup.StartLog;
import org.openide.filesystems.Repository;
import org.openide.util.Utilities;

/** Layered file system serving itself as either the user or installation layer.
 * Holds one layer of a writable system directory, and some number
 * of module layers.
 * @author Jesse Glick, Jaroslav Tulach
 */
public class ModuleLayeredFileSystem extends MultiFileSystem {
    /** serial version UID */
    private static final long serialVersionUID = 782910986724201983L;
    
    private static final String LAYER_STAMP = "layer-stamp.txt";
    
    static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.core.projects"); // NOI18N
    
    /** current list of URLs - r/o; or null if not yet set */
    private List urls; // List<URL>
    /** cache manager */
    private LayerCacheManager manager;
    /** writable layer */
    private final FileSystem writableLayer;
    /** cache layer */
    private FileSystem cacheLayer;
    /** other layers */
    private final FileSystem[] otherLayers;

    /** Create layered filesystem based on a supplied writable layer.
     * @param writableLayer the writable layer to use, typically a LocalFileSystem
     * @param otherLayers some other layers to use, e.g. LocalFileSystem[]
     * @param cacheDir a directory in which to store a cache, or null for no caching
     */
    ModuleLayeredFileSystem (FileSystem writableLayer, FileSystem[] otherLayers, File cacheDir) throws IOException {
        this(writableLayer, otherLayers, manager(cacheDir));
    }
    
    private ModuleLayeredFileSystem(FileSystem writableLayer, FileSystem[] otherLayers, LayerCacheManager mgr) throws IOException {
        this(writableLayer, otherLayers, mgr, loadCache(mgr));
    }
    
    private ModuleLayeredFileSystem(FileSystem writableLayer, FileSystem[] otherLayers, LayerCacheManager mgr, FileSystem cacheLayer) throws IOException {
        super(appendLayers(writableLayer, otherLayers, cacheLayer));
        this.manager = mgr;
        this.writableLayer = writableLayer;
        this.otherLayers = otherLayers;
        this.cacheLayer = cacheLayer;
        
        // Wish to permit e.g. a user-installed module to mask files from a
        // root-installed module, so propagate masks up this high.
        // SystemFileSystem leaves this off, so that the final file system
        // will not show them if there are some left over.
        setPropagateMasks (true);
        
        urls = null;
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
                err.log("Cache manager disabled");
                return LayerCacheManager.emptyManager();
            }
            try {
                Class c = Class.forName(managerName);
                Constructor ctor = c.getConstructor(new Class[] {File.class});
                LayerCacheManager mgr = (LayerCacheManager)ctor.newInstance(new Object[] {cacheDir});
                err.log("Using cache manager of type " + managerName + " in " + cacheDir);
                return mgr;
            } catch (Exception e) {
                IOException ioe = new IOException(e.toString());
                err.annotate(ioe, e);
                throw ioe;
            }
        } else {
            err.log("No cache manager");
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
                err.notify(ErrorManager.INFORMATIONAL, ioe);
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
    
    private static FileSystem[] appendLayers(FileSystem fs1, FileSystem[] fs2s, FileSystem fs3) {
        List l = new ArrayList(fs2s.length + 2);
        l.add(fs1);
        l.addAll(Arrays.asList(fs2s));
        l.add(fs3);
        return (FileSystem[])l.toArray(new FileSystem[l.size()]);
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
    public void setURLs (final List urls) throws Exception {
        if (urls.contains(null)) throw new NullPointerException("urls=" + urls); // NOI18N
        if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
            err.log("setURLs: " + urls);
        }
        if (this.urls != null && urls.equals(this.urls)) {
            err.log("no-op");
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
            err.log("Stamp of new URLs: " + stamp.getHash());
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(stampFile), "UTF-8")); // NOI18N
            try {
                String line = r.readLine();
                long hash;
                try {
                    hash = Long.parseLong(line);
                } catch (NumberFormatException nfe) {
                    throw new IOException(nfe.toString());
                }
                err.log("Stamp in the cache: " + hash);
                if (hash == stamp.getHash()) {
                    err.log("Cache hit!");
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
                        err.log("Rewriting cache in " + cacheDir);
                    } // else if null -> we are using emptyManager, so do not print confusing messages
                        try {
                            if (manager.supportsLoad()) {
                                manager.store(cacheLayer, urls);
                            } else {
                                cacheLayer = manager.store(urls);
                                setDelegates(appendLayers(writableLayer, otherLayers, cacheLayer));
                            }
                        } catch (IOException ioe) {
                            err.notify(ErrorManager.INFORMATIONAL, ioe);
                            err.log("Abandoning cache manager");
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
                                setDelegates(appendLayers(writableLayer, otherLayers, cacheLayer));
                            } catch (IOException ioe2) {
                                // More serious - should not happen.
                                err.notify(ioe2);
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
                        err.log("Finished rewriting cache in " + cacheDir);
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
    public void addURLs(Collection urls) throws Exception {
        if (urls.contains(null)) throw new NullPointerException("urls=" + urls); // NOI18N
        // Add to the front: #23609.
        ArrayList arr = new ArrayList(urls);
        if (this.urls != null) arr.addAll(this.urls);
        setURLs(arr);
    }
    
    /** Removes few URLs.
     */
    public void removeURLs(Collection urls) throws Exception {
        if (urls.contains(null)) throw new NullPointerException("urls=" + urls); // NOI18N
        ArrayList arr = new ArrayList();
        if (this.urls != null) arr.addAll(this.urls);
        arr.removeAll(urls);
        setURLs(arr);
    }
    
    /** Represents a hash of a bunch of jar: URLs and the associated JAR timestamps.
     */
    private static final class Stamp implements Comparator {
        private final String managerName;
        private final List urls; // List<URL>
        private final long[] times;
        private final long hash;
        public Stamp(String name, List urls) throws IOException {
            managerName = name;
            this.urls = new ArrayList(urls);
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
                        err.log(ErrorManager.WARNING, "Weird JAR URL: " + u);
                        u2 = u;
                    }
                } else {
                    // something else... plain file: URL?
                    u2 = u;
                }
                File extracted = Utilities.toFile(u2);
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
            StringBuffer buf = new StringBuffer();
            buf.append(managerName);
            buf.append('\n');
            Iterator it = urls.iterator();
            int i = 0;
            while (it.hasNext()) {
                long t = times[i++];
                if (t == 0L) {
                    buf.append("<file not found>"); // NOI18N
                } else {
                    buf.append(new Date(t));
                }
                buf.append('\t');
                buf.append(it.next());
                buf.append('\n');
            }
            return buf.toString();
        }
        public int compare(Object o1, Object o2) {
            return ((URL)o1).toString().compareTo(((URL)o2).toString());
        }
    }
    private static void setStatusText (String msg) {
        org.netbeans.core.startup.Main.setStatusText (msg);
    }
    
}

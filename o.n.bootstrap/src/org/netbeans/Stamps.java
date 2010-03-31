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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 * Support for optimal checking of time stamps of certain files in
 * NetBeans directory structure. 
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 * @since 2.9
 */
public final class Stamps {
    private static final Logger LOG = Logger.getLogger(Stamps.class.getName());
    private static AtomicLong moduleJARs;
    private static File moduleNewestFile;
    
    private Worker worker = new Worker();

    private Stamps() {
    }
    
    /** This class can be executed from command line to perform various checks
     * on installed NetBeans, however outside of running NetBeans.
     * 
     */
    static void main(String... args) {
        if (args.length == 1 && "reset".equals(args[0])) { // NOI18N
            moduleJARs = null;
            stamp(false);
            return;
        }
        if (args.length == 1 && "init".equals(args[0])) { // NOI18N
            moduleJARs = null;
            stamp(true);
            return;
        }
        if (args.length == 1 && "clear".equals(args[0])) { // NOI18N
            moduleJARs = null;
            return;
        }
    }
    private static final Stamps MODULES_JARS = new Stamps();
    /** Creates instance of stamp that checks timestamp for all files that affect
     * module classloading and related caches.
     */
    public static Stamps getModulesJARs() {
        return MODULES_JARS;
    }
    
    /** Finds out the time of last modifications of files that influnce
     * this cache. Each cached file needs to be "younger".
     * @return time in ms since epoch
     */
    public long lastModified() {
        return moduleJARs();
    }
    
    /** Checks whether a cache exists
     * 
     * @param cache name of the cache
     * @return true if the cache exists and is not out of date
     */
    public boolean exists(String cache) {
        return file(cache, null) != null;
    }
    
    /** Opens the access to cache object as a stream.
     * @param name name of the cache
     * @return stream to read from the cache or null if the cache is not valid
     */
    public InputStream asStream(String cache) {
        ByteBuffer bb = asByteBuffer(cache, false, false);
        if (bb == null) {
            return null;
        }
        return new ByteArrayInputStream(bb.array());
    }
    
    /** Getter for mmapped buffer access to the cache.
     * @param cache the file to access
     * @return mmapped read only buffer
     */
    public MappedByteBuffer asMappedByteBuffer(String cache) {
        return (MappedByteBuffer)asByteBuffer(cache, true, true);
    }
        
    /** Returns the stamp for this caches. 
     * @return a date, each cache needs to be newer than this date
     */
   
    /** Opens the access to cache object as a stream.
     * @param name name of the cache
     * @return stream to read from the cache or null if the cache is not valid
     */
    public ByteBuffer asByteBuffer(String cache) {
        return asByteBuffer(cache, true, false);
    }
    final File file(String cache, int[] len) {
        String ud = getUserDir();
        if (ud == null) {
            LOG.log(Level.FINE, "No userdir when asking for {0}", cache); // NOI18N
            return null;
        }
        synchronized (this) {
            if (worker.isProcessing(cache)) {
                LOG.log(Level.FINE, "Worker processing when asking for {0}", cache); // NOI18N
                return null;
            }
        }
        
        File cacheFile = new File(new File(new File(ud, "var"), "cache"), cache.replace('/', File.separatorChar)); // NOI18N
        long last = cacheFile.lastModified();
        if (last <= 0) {
            LOG.log(Level.FINE, "Cache does not exist when asking for {0}", cache); // NOI18N
            return null;
        }

        if (moduleJARs() > last) {
            LOG.log(Level.FINE, "Timestamp does not pass when asking for {0}. Newest file {1}", new Object[] { cache, moduleNewestFile }); // NOI18N
            return null;
        }

        long longLen = cacheFile.length();
        if (longLen > Integer.MAX_VALUE) {
            LOG.warning("Cache file is too big: " + longLen + " bytes for " + cacheFile); // NOI18N
            return null;
        }
        if (len != null) {
            len[0] = (int)longLen;
        }
        
        LOG.log(Level.FINE, "Cache found: {0}", cache); // NOI18N
        return cacheFile;
    }
    
    private ByteBuffer asByteBuffer(String cache, boolean direct, boolean mmap) {
        int[] len = new int[1];
        File cacheFile = file(cache, len);
        if (cacheFile == null) {
            return null;
        }
        
        try {
            FileChannel fc = new FileInputStream(cacheFile).getChannel();
            ByteBuffer master;
            if (mmap) {
                master = fc.map(FileChannel.MapMode.READ_ONLY, 0, len[0]);
                master.order(ByteOrder.LITTLE_ENDIAN);
            } else {
                master = direct ? ByteBuffer.allocateDirect(len[0]) : ByteBuffer.allocate(len[0]);
                int red = fc.read(master);
                if (red != len[0]) {
                    LOG.warning("Read less than expected: " + red + " expected: " + len + " for " + cacheFile); // NOI18N
                    return null;
                }
                master.flip();
            }

            fc.close();
            
            return master;
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Cannot read cache " + cacheFile, ex); // NOI18N
            return null;
        }
    }
    
    /** Method for registering updates to caches.
     * @param updater the callback to start when flushing caches
     * @param file name of the file to store the cache into
     * @param append write from scratch or append?
     */
    public void scheduleSave(Updater updater, String cache, boolean append) {
        LOG.log(Level.FINE, "Scheduling save for {0} cache", cache);
        synchronized (worker) {
            worker.addStorage(new Store(updater, cache, append));
        }
    }
    
    /** Flushes all caches.
     * @param delay the delay to wait with starting the parsing, if zero, that also means
     *   we want to wait for the end of parsing
     */
    public void flush(int delay) {
        synchronized (worker) {
            worker.start(delay);
        }
    }

    /** Waits for the worker to finish */
    public void shutdown() {
        waitFor(true);
    }
    
    public void discardCaches() {
        discardCachesImpl(moduleJARs);
    }
    
    private static void discardCachesImpl(AtomicLong al) {
        String user = getUserDir();
        long now = System.currentTimeMillis();
        if (user != null) {
            File f = new File(user, ".lastModified");
            if (f.exists()) {
                f.setLastModified(now);
            } else {
                f.getParentFile().mkdirs();
                try {
                    f.createNewFile();
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Cannot create " + f, ex);
                }
            }
        }
        if (al != null) {
            al.set(now);
        }
    }
    
    final void waitFor(boolean noNotify) {
        Worker wait;
        synchronized (worker) {
            flush(0);
            wait = worker;
        }
        wait.waitFor(noNotify);
    }
    
    
    /** Computes and returns timestamp for all files that affect
     * module classloading and related caches.
     * @return
     */
    static long moduleJARs() {
        AtomicLong local = moduleJARs;
        if (local == null) {
            local = new AtomicLong();
            AtomicReference<File> newestFile = new AtomicReference<File>();
            stamp(true, local, newestFile);
            moduleJARs = local;
            moduleNewestFile = newestFile.get();
        }
        return local.longValue();
    }
    
    //
    // Implementation. As less dependecies on other NetBeans clases, as possible, please.
    // This will be called externally from a launcher.
    //

    private static String getUserDir() {
        String ud = System.getProperty("netbeans.user"); // NOI18N
        return "memory".equals(ud) ? null : ud;
    }

    private static AtomicLong stamp(boolean checkStampFile) {
        AtomicLong result = new AtomicLong();
        AtomicReference<File> newestFile = new AtomicReference<File>();
        stamp(checkStampFile, result, newestFile);
        return result;
    }

    private static void stamp(boolean checkStampFile, AtomicLong result, AtomicReference<File> newestFile) {
        StringBuilder sb = new StringBuilder();
        
        Set<File> processedDirs = new HashSet<File>();
        String home = System.getProperty ("netbeans.home"); // NOI18N
        if (home != null) {
            long stamp = stampForCluster (new File (home), result, newestFile, processedDirs, checkStampFile, true, null);
            sb.append(home).append('=').append(stamp).append('\n');
        }
        String nbdirs = System.getProperty("netbeans.dirs"); // NOI18N
        if (nbdirs != null) {
            StringTokenizer tok = new StringTokenizer(nbdirs, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                String t = tok.nextToken();
                long stamp = stampForCluster(new File(t), result, newestFile, processedDirs, checkStampFile, true, null);
                if (stamp != -1) {
                    sb.append(t).append('=').append(stamp).append('\n');
                }
            }
        }
        String user = getUserDir();
        if (user != null) {
            AtomicInteger crc = new AtomicInteger();
            stampForCluster (new File (user), result, newestFile, new HashSet<File> (), false, false, crc);
            sb.append(user).append('=').append(result.longValue()).append('\n');
            sb.append("crc=").append(crc.intValue()).append('\n');
            
            File userDir = new File(user);
            File checkSum = new File(new File(new File(new File(userDir, "var"), "cache"), "lastModified"), "all-checksum.txt");
            if (!compareAndUpdateFile(checkSum, sb.toString(), result)) {
                discardCachesImpl(result);
            }
        }
    }
    
    private static long stampForCluster(
        File cluster, AtomicLong result, AtomicReference<File> newestFile, Set<File> hashSet,
        boolean checkStampFile, boolean createStampFile, AtomicInteger crc
    ) {
        File stamp = new File(cluster, ".lastModified"); // NOI18N
        long time;
        if (checkStampFile && (time = stamp.lastModified()) > 0) {
            if (time > result.longValue()) {
                newestFile.set(stamp);
                result.set(time);
            }
            return time;
        }
        String user = getUserDir();
        if (user != null) {
            File userDir = new File(user);
            stamp = new File(new File(new File(new File(userDir, "var"), "cache"), "lastModified"), cluster.getName());
            if (checkStampFile && (time = stamp.lastModified()) > 0) {
                if (time > result.longValue()) {
                    newestFile.set(stamp);
                    result.set(time);
                }
                return time;
            }
        } else {
            createStampFile = false;
        }

        File configDir = new File(new File(cluster, "config"), "Modules"); // NOI18N
        File modulesDir = new File(cluster, "modules"); // NOI18N

        AtomicLong clusterResult = new AtomicLong();
        AtomicReference<File> newestInCluster = new AtomicReference<File>();
        if (highestStampForDir(configDir, newestInCluster, clusterResult, crc) && highestStampForDir(modulesDir, newestInCluster, clusterResult, crc)) {
            // ok
        } else {
            if (!cluster.isDirectory()) {
                // skip non-existing clusters`
                return -1;
            }
        }

        if (clusterResult.longValue() > result.longValue()) {
            newestFile.set(newestInCluster.get());
            result.set(clusterResult.longValue());
        }
        
        if (createStampFile) {
            try {
                stamp.getParentFile().mkdirs();
                stamp.createNewFile();
                stamp.setLastModified(clusterResult.longValue());
            } catch (IOException ex) {
                System.err.println("Cannot write timestamp to " + stamp); // NOI18N
            }
        }
        return clusterResult.longValue();
    }

    private static boolean highestStampForDir(File file, AtomicReference<File> newestFile, AtomicLong result, AtomicInteger crc) {
        if (file.getName().equals(".nbattrs")) { // NOI18N
            return true;
        }

        File[] children = file.listFiles();
        if (children == null) {
            if (crc != null) {
                crc.addAndGet(file.getName().length());
            }
            long time = file.lastModified();
            if (time > result.longValue()) {
                newestFile.set(file);
                result.set(time);
            }
            return false;
        }
        
        for (File f : children) {
            highestStampForDir(f, newestFile, result, crc);
        }
        return true;
    }
    
    private static boolean compareAndUpdateFile(File file, String content, AtomicLong result) {
        try {
            byte[] expected = content.getBytes("UTF-8"); // NOI18N
            byte[] read = new byte[expected.length];
            FileInputStream is = null;
            boolean areCachesOK;
            boolean writeFile;
            long lastMod;
            try {
                is = new FileInputStream(file);
                int len = is.read(read);
                areCachesOK = len == read.length && is.available() == 0 && Arrays.equals(expected, read);
                writeFile = !areCachesOK;
                lastMod = file.lastModified();
            } catch (FileNotFoundException notFoundEx) {
                // ok, running for the first time, no need to invalidate the cache
                areCachesOK = true;
                writeFile = true;
                lastMod = result.get();
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            if (writeFile) {
                file.getParentFile().mkdirs();
                FileOutputStream os = new FileOutputStream(file);
                os.write(expected);
                os.close();
                if (areCachesOK) {
                    file.setLastModified(lastMod);
                }
            } else {
                if (lastMod > result.get()) {
                    result.set(lastMod);
                }
            }
            return areCachesOK;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    private static void deleteCache(File cacheFile) throws IOException {
        int fileCounter = 0;
        if (cacheFile.exists()) {
            // all of this mess is here because Windows can't delete mmaped file.
            File tmpFile = new File(cacheFile.getParentFile(), cacheFile.getName() + "." + fileCounter++);
            tmpFile.delete(); // delete any leftover file from previous session
            boolean renamed = false;
            for (int i = 0; i < 5; i++) {
                renamed = cacheFile.renameTo(tmpFile); // try to rename it
                if (renamed) {
                    break;
                }
                LOG.fine("cannot rename (#" + i + "): " + cacheFile); // NOI18N
                // try harder
                System.gc();
                System.runFinalization();
                LOG.fine("after GC"); // NOI18N
            }
            if (!renamed) {
                // still delete on exit, so next start is ok
                cacheFile.deleteOnExit();
                throw new IOException("Could not delete: " + cacheFile); // NOI18N
            }
            if (!tmpFile.delete()) {
                tmpFile.deleteOnExit();
            } // delete now or later
        }
    }

    /** A callback interface to flush content of some cache at a suitable
     * point in time.
     */
    public static interface Updater {
        /** Callback method to allow storage of the cache to a stream.
         * If an excetion is thrown, cache is invalidated.
         * 
         * @param os the stream to write to
         * @throws IOException exception in case something goes wrong
         */
        public void flushCaches(DataOutputStream os) throws IOException;
        
        /** Callback method to notify the caller, that
         * caches are successfully written.
         */
        public void cacheReady();
    }
    
    /** Internal structure keeping info about storages.
     */
    private static final class Store extends OutputStream {
        final Updater updater;
        final String cache;
        final boolean append;
        
        OutputStream os;
        AtomicInteger delay;
        int count;
        
        public Store(Updater updater, String cache, boolean append) {
            this.updater = updater;
            this.cache = cache;
            this.append = append;
        }
        
        public boolean store(AtomicInteger delay) {
            assert os == null;
            
            String ud = getUserDir();
            if (ud == null) {
                LOG.warning("No 'netbeans.user' property to store: " + cache); // NOI18N
                return false;
            }
            File cacheFile = new File(new File(new File(ud, "var"), "cache"), cache); // NOI18N
            boolean delete = false;
            try {
                LOG.log(Level.FINE, "Cleaning cache {0}", cacheFile);
                
                if (!append) {
                    deleteCache(cacheFile);
                }
                cacheFile.getParentFile().mkdirs();

                LOG.log(Level.FINE, "Storing cache {0}", cacheFile);
                os = new FileOutputStream(cacheFile, append); //append new entries only
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(this, 1024 * 1024));
                
                this.delay = delay;
        
                updater.flushCaches(dos);
                dos.close();
                LOG.log(Level.FINE, "Done Storing cache {0}", cacheFile);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Error saving cache " + cacheFile, ex); // NOI18N
                delete = true;
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, "Error closing stream for " + cacheFile, ex); // NOI18N
                    }
                    os = null;
                }
                if (delete) {
                    cacheFile.delete();
                    cacheFile.deleteOnExit();
                } else {
                    cacheFile.setLastModified(moduleJARs());
                }
            }
            return !delete;
        }

        @Override
        public void close() throws IOException {
            os.close();
        }

        @Override
        public void flush() throws IOException {
            os.flush();
        }

        @Override
        public void write(int b) throws IOException {
            os.write(b);
            count(1);
        }

        @Override
        public void write(byte[] b) throws IOException {
            os.write(b);
            count(b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            os.write(b, off, len);
            count(len);
        }
        
        private void count(int add) {
            count += add;
            if (count > 64 * 1024) {
                int wait = delay.get();
                if (wait > 0) {
                    try {
                        Thread.sleep(wait);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                count = 0;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Store other = (Store) obj;
            if (!this.updater.equals(other.updater)) {
                return false;
            }
            if (!this.cache.equals(other.cache)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 19 * hash + (this.updater != null ? this.updater.hashCode() : 0);
            hash = 19 * hash + (this.cache != null ? this.cache.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return cache;
        }
    } // end of Store
    
    private final class Worker extends Thread {
        private final LinkedList<Store> storages;
        private final HashSet<String> processing;
        private AtomicInteger delay;
        private boolean noNotify;
        
        public Worker() {
            super("Flushing caches");
            storages = new LinkedList<Stamps.Store>();
            processing = new HashSet<String>();
            setPriority(MIN_PRIORITY);
        }
        
        public synchronized void start(int time) {
            if (delay == null) {
                delay = new AtomicInteger(time);
                super.start();
            }
        }
        
        public synchronized void addStorage(Store s) {
            processing.add(s.cache);
            for (Iterator<Stamps.Store> it = storages.iterator(); it.hasNext();) {
                Stamps.Store store = it.next();
                if (store.equals(s)) {
                    it.remove();
                }
            }
            storages.add(s);
        }
        
        @Override
        public void run() {
            int before = delay.get();
            for (int till = before; till >= 0; till -= 500) {
                try {
                    synchronized (this) {
                        wait(500);
                    }
                } catch (InterruptedException ex) {
                    LOG.log(Level.INFO, null, ex);
                }
                if (before != delay.get()) {
                    break;
                }
            }
            if (before > 512) {
                delay.compareAndSet(before, 512);
            }
            
            long time = System.currentTimeMillis();
            LOG.log(Level.FINE, "Storing caches {0}", storages);

            HashSet<Store> notify = new HashSet<Stamps.Store>();
            for (;;) {
                Store store;
                synchronized (this) {
                    store = this.storages.poll();
                    if (store == null) {
                        // ready for new round of work
                        worker = new Worker();
                        break;
                    }
                }
                if (store.store(delay)) {
                    notify.add(store);
                }
            }
            
            long much = System.currentTimeMillis() - time;
            LOG.log(Level.FINE, "Done storing caches {0}", notify);
            LOG.log(Level.FINE, "Took {0} ms", much);
            
            processing.clear();
            
            for (Stamps.Store store : notify) {
                if (!noNotify) {
                    store.updater.cacheReady();
                }
            }
            LOG.log(Level.FINE, "Notified ready {0}", notify);

        }


        final void waitFor(boolean noNotify) {
            try {
                this.noNotify = noNotify;
                delay.set(0);
                synchronized (this) {
                    notifyAll();
                }
                join();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private boolean isProcessing(String cache) {
            return processing.contains(cache);
        }
        
    }
}

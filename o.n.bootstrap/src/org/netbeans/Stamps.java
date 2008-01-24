/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import java.io.BufferedInputStream;
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
import java.nio.ByteBuffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicLong;
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
    
    static int delay = 0;

    
    private List<Store> storages;

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
    
    /** Opens the access to cache object as a stream.
     * @param name name of the cache
     * @return stream to read from the cache or null if the cache is not valid
     */
    public InputStream asStream(String cache) {
        ByteBuffer bb = asByteBuffer(cache, false);
        if (bb == null) {
            return null;
        }
        return new ByteArrayInputStream(bb.array());
    }
    
    /** Returns the stamp for this caches. 
     * @return a date, each cache needs to be newer than this date
     */
   
    /** Opens the access to cache object as a stream.
     * @param name name of the cache
     * @return stream to read from the cache or null if the cache is not valid
     */
    public ByteBuffer asByteBuffer(String cache) {
        return asByteBuffer(cache, true);
    }
    private ByteBuffer asByteBuffer(String cache, boolean direct) {
        String ud = System.getProperty("netbeans.user"); // NOI18N
        if (ud == null) {
            return null;
        }
        
        File cacheFile = new File(new File(new File(ud, "var"), "cache"), cache); // NOI18N
        long last = cacheFile.lastModified();
        if (last <= 0) {
            return null;
        }

        if (moduleJARs() >= last) {
            return null;
        }

        try {
            long longLen = cacheFile.length();
            if (longLen > Integer.MAX_VALUE) {
                LOG.warning("Cache file is too big: " + longLen + " bytes for " + cacheFile); // NOI18N
                return null;
            }
            int len = (int)longLen;
            ByteBuffer master = direct ? ByteBuffer.allocateDirect(len) : ByteBuffer.allocate(len);
            FileChannel fc = new FileInputStream(cacheFile).getChannel();
            int red = fc.read(master);
            if (red != len) {
                LOG.warning("Read less than expected: " + red + " expected: " + len + " for " + cacheFile); // NOI18N
                return null;
            }

            fc.close();
            master.flip();
            
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
    public synchronized void scheduleSave(Updater updater, String cache, boolean append) {
        if (storages == null) {
            storages = new ArrayList<Stamps.Store>();
        }
        LOG.log(Level.FINE, "Scheduling save for {0} cache", cache);
        storages.add(new Store(updater, cache, append));
    }
    
    /** Flushes all caches.
     */
    public void flush(boolean now) {
        List<Store> work;
        synchronized (this) {
            work = storages;
            storages = null;
        }
        
        delay = now ? 0 : 1024;
        
        if (work == null) {
            return;
        }
        
        for (Store store : work) {
            store.store();
        }

    }
    
    
    /** Computes and returns timestamp for all files that affect
     * module classloading and related caches.
     * @return
     */
    static long moduleJARs() {
        AtomicLong local = moduleJARs;
        if (local == null) {
            local = moduleJARs = stamp(true);
        }
        return local.longValue();
    }
    
    //
    // Implementation. As less dependecies on other NetBeans clases, as possible, please.
    // This will be called externally from a launcher.
    //
    

    private static AtomicLong stamp(boolean checkStampFile) {
        AtomicLong result = new AtomicLong();
        
        Set<File> processedDirs = new HashSet<File>();
        String home = System.getProperty ("netbeans.home"); // NOI18N
        if (home != null) {
            stampForCluster (new File (home), result, processedDirs, checkStampFile, true);
        }
        String nbdirs = System.getProperty("netbeans.dirs"); // NOI18N
        if (nbdirs != null) {
            StringTokenizer tok = new StringTokenizer(nbdirs, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                stampForCluster(new File(tok.nextToken()), result, processedDirs, checkStampFile, true);
            }
        }
        String user = System.getProperty ("netbeans.user"); // NOI18N
        if (user != null) {
            stampForCluster (new File (user), result, new HashSet<File> (), false, false);
        }
        
        return result;
    }
    
    private static void stampForCluster(
        File cluster, AtomicLong result, Set<File> hashSet, 
        boolean checkStampFile, boolean createStampFile
    ) {
        File stamp = new File(cluster, ".lastModified"); // NOI18N
        long time;
        if (checkStampFile && (time = stamp.lastModified()) > 0) {
            if (time > result.longValue()) {
                result.set(time);
            }
            return;
        }
        String user = System.getProperty ("netbeans.user"); // NOI18N
        if (user != null) {
            File userDir = new File(user);
            stamp = new File(new File(new File(new File(userDir, "var"), "cache"), "lastModified"), cluster.getName());
            if (checkStampFile && (time = stamp.lastModified()) > 0) {
                if (time > result.longValue()) {
                    result.set(time);
                }
                return;
            }
        } else {
            createStampFile = false;
        }

    
        File configDir = new File(new File(cluster, "config"), "Modules"); // NOI18N
        File modulesDir = new File(cluster, "modules"); // NOI18N
        
        highestStampForDir(configDir, result);
        highestStampForDir(modulesDir, result);
    
        if (createStampFile) {
            try {
                stamp.getParentFile().mkdirs();
                stamp.createNewFile();
                stamp.setLastModified(result.longValue());
            } catch (IOException ex) {
                System.err.println("Cannot write timestamp to " + stamp); // NOI18N
            }
        }
    }

    private static void highestStampForDir(File file, AtomicLong result) {
        File[] children = file.listFiles();
        if (children == null) {
            long time = file.lastModified();
            if (time > result.longValue()) {
                result.set(time);
            }
            return;
        }
        
        for (File f : children) {
            highestStampForDir(f, result);
        }

    }

    /** A callback interface to flush content of some cache at a suitable
     * point in time.
     */
    public static interface Updater {
        public void flushCaches(DataOutputStream os) throws IOException;
    }
    
    /** Internal structure keeping info about storages.
     */
    private static final class Store extends OutputStream {
        final Updater updater;
        final String cache;
        final boolean append;
        
        OutputStream os;
        int count;
        
        public Store(Updater updater, String cache, boolean append) {
            this.updater = updater;
            this.cache = cache;
            this.append = append;
        }
        
        public void store() {
            assert os == null;
            
            String ud = System.getProperty("netbeans.user"); // NOI18N
            if (ud == null) {
                LOG.warning("No 'netbeans.user' property to store: " + cache); // NOI18N
                return;
            }
            File cacheFile = new File(new File(new File(ud, "var"), "cache"), cache); // NOI18N
            cacheFile.getParentFile().mkdirs();

            boolean delete = false;
            try {
                os = new FileOutputStream(cacheFile, append); //append new entries only
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(this, 1024 * 1024));
        
                updater.flushCaches(dos);
                dos.close();
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
                }
            }
            
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
            if (count > 1024 * 1024) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                count = 0;
            }
        }
    } // end of Store
}

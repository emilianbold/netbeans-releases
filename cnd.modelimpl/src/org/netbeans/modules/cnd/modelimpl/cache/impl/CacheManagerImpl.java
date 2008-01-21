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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelimpl.cache.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.cache.FileCache;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ParserThreadManager;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 * cache manager implementation
 * @author Vladimir Voskresensky
 */
public final class CacheManagerImpl {
    // main cache directory
    // this directory can be changable:
    // i.e. we can copy all to tmp for fast access and then to userdir again
    private String cacheDir;
    
    // cache per project
    private WeakHashMap/*<String(project-key), ProjectCache>*/ projectCache = new WeakHashMap();
    // master index of cache
    private MasterIndex index = new MasterIndex();
    
    public CacheManagerImpl() {
        if (ParserThreadManager.instance().isStandalone()) {
            cacheDir = System.getProperty("java.io.tmpdir");// NOI18N
        } else {
            cacheDir = System.getProperty("netbeans.user");// NOI18N
        }
        cacheDir = cacheDir + File.separatorChar + "var" + File.separatorChar + // NOI18N
                "cache" + File.separatorChar + "cndcache"; // NOI18N
        new File(cacheDir).mkdirs();
        if (TraceFlags.TRACE_CACHE) System.err.println("CACHE: Cache dir:" + cacheDir); // NOI18N
        load();      
    }
    
    public APTFile findAPT(CsmFile file) {
        return getSyncCacheBridge((FileImpl)file).findAPT();
    }
    
    public APTFile findAPTLight(CsmFile file) {
        return getSyncCacheBridge((FileImpl)file).findAPTLight();
    }
    
    public FileCache findCacheWithAST(CsmFile file, APTPreprocHandler preprocHandler) {
        return getSyncCacheBridge((FileImpl)file).findCacheWithAST(preprocHandler);
    }    
    
    public void projectOpened(CsmProject project) {
        // load cache related to project?
    }

    public void projectClosed(CsmProject project) {
        if (projectCache == null || index == null) {
            return;
        }
        synchronized (loadLock) {
            // save/close cache related to project
            ProjectCache prjCache = null;
            synchronized (projectCache) {
                prjCache = (ProjectCache) projectCache.remove(project);
            }
            if (prjCache != null) {
                assert (index != null);
                String prjDir = index.getProjectDir(project);
                assert (prjDir != null);
                // close project cache
                prjCache.close(this.cacheDir + File.separatorChar + prjDir);
            }
        }
    }

    public void modelChanged(CsmChangeEvent e) {
        // nothings now
    } 
    
    public void invalidateFile(CsmFile file) {
        synchronized (fileLocks) {
            fileLocks.remove(file.getAbsolutePath());
        }
        ProjectCache prjCache = getProjectCache(file.getProject(), false);
        if (prjCache != null) {
            prjCache.invalidateFile(file.getAbsolutePath().toString());
        }
    }    

    public void invalidateAllFiles(String absPath) {
        synchronized (fileLocks) {
            fileLocks.remove(absPath);
        }       
        ProjectCache[] caches = getProjectCaches();
        for (int i = 0; i < caches.length; i++) {
            ProjectCache cur = caches[i];
            cur.invalidateFile(absPath);
        }
    }
    
    public void changeDir(String dir) {
        // here we should move cache from one place to another and 
        // it's better to be locked for this time
        this.cacheDir = dir;
    }
  
    ////////////////////////////////////////////////////////////////////////////
    // access to sync project cache
    
    private ProjectCache getProjectCache(CsmProject project, boolean create) {
        load();
        assert (project != null);
        ProjectCache prjCache = null;
        synchronized (projectCache) {
            // check if already in projects
            prjCache = (ProjectCache) projectCache.get(project);
            if (prjCache == null && create) {
                if (TraceFlags.TRACE_CACHE) {
                    System.err.println("CACHE: creating Project cache for " + project.getName()); // NOI18N
                }                
                // check if can load from disk
                // prjDir is non absolute
                String prjDir = (String) index.getProjectDir(project);
                if (prjDir == null) {
                    // new project => generate new dir and remember in master index
                    prjDir = index.putProject(project);
                }
                // create project cache and pass full cache path to load own cache
                prjCache = new ProjectCache(cacheDir + File.separatorChar + prjDir);
                projectCache.put(project, prjCache);
            }
        }
        assert (prjCache != null || !create);
        return prjCache;
    }

    private ProjectCache[] getProjectCaches() {
        load();
        ProjectCache[] copy = null;
        synchronized (projectCache) {
            // check if already in projects
            Collection values = projectCache.values();
            copy = (ProjectCache[]) values.toArray(new ProjectCache[values.size()]);
        }
        assert (copy != null);
        return copy;        
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // access to sync file cache 
    private FileCacheSyncBridge getSyncCacheBridge(FileImpl file) {
        return getProjectCache(file.getProject(), true).getSyncCacheBridge(file);
    } 
    
    ////////////////////////////////////////////////////////////////////////////
    // getting valid cache files 
    
    private File getValidCacheFile(CsmFile file, boolean save) {
        assert (file != null);
        assert (cacheDir !=null);
        String prjRelDir = index.getProjectDir(file.getProject());
        File out = null;
        if (prjRelDir != null) {
            String baseDir = cacheDir + File.separatorChar + prjRelDir; 
            CharSequence fileName = getProjectCache(file.getProject(), true).getValidCacheFileName((FileImpl) file, save);
            if (fileName != null) {
                out = new File(baseDir, fileName.toString());
            }
        } else {
            if (TraceFlags.TRACE_CACHE) {
                System.err.println("CACHE: no cache dir for project " + file.getProject().getName()); // NOI18N
            }            
        }
        return out;
    }

    ////////////////////////////////////////////////////////////////////////////
    // loading cache
    
    public FileCache loadValidCache(CsmFile file) {
        FileCache loaded = null;
        File cacheFile = getValidCacheFile(file,false);
        if (cacheFile != null) {
            loaded = loadFile(cacheFile);
            if (TraceFlags.TRACE_CACHE) {
                if (loaded == null) {
                    System.err.println("CACHE: Failed load cache " + cacheFile.getAbsolutePath() + // NOI18N
                            " for:" + file.getAbsolutePath()); // NOI18N
                }
            }
        } else {
            if (TraceFlags.TRACE_CACHE) {
                if (((FileImpl)file).getBuffer().isFileBased()) {
                    System.err.println("CACHE: not found valid cache for:" + file.getAbsolutePath()); // NOI18N
                } else {
                    System.err.println("CACHE: do not load cache for document based file-buffer:" + file.getAbsolutePath()); // NOI18N
                }
            }
        }
        return loaded;
    }
    
    private FileCache loadFile(File from) {
        assert (from != null);
        FileCache loaded = null;
        ObjectInputStream ois = null;
        Object fileLock = getCacheFileLock(from);
        assert (fileLock != null);
        synchronized (fileLock) {
            try {
                InputStream in = null;
                try {
                    in = new FileInputStream(from);
                    in = new BufferedInputStream(in);
                    ois = new ObjectInputStream(in);
                } finally {
                    if (in != null && ois == null) {
                        in.close();
                    }
                }
                if (ois != null) {
                    loaded = (FileCache) ois.readObject();
                }
            } catch (IOException io) {
                // null cache
            } catch (ClassNotFoundException e) {
		DiagnosticExceptoins.register(e);
                APTUtils.LOG.log(Level.SEVERE, "load cache file: {0}", new Object[] { e.getMessage() });// NOI18N
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
        return loaded;        
    }
        
    ////////////////////////////////////////////////////////////////////////////
    // saving cache

    public void saveCache(CsmFile file, FileCache cache) {
        FileCache loaded = null;
        File cacheFile = getValidCacheFile(file,true);
        FileCacheSyncBridge syncCache = getSyncCacheBridge((FileImpl) file);
        assert (cacheFile != null);
        if (save2File(cacheFile, cache)) {
            if (TraceFlags.TRACE_CACHE) System.err.println("CACHE: saved cache for:" + file.getAbsolutePath()); // NOI18N
            syncCache.updateStorage(cache);
        } else {
            if (TraceFlags.TRACE_CACHE) {
                if (((FileImpl)file).getBuffer().isFileBased()) {
                    System.err.println("CACHE: FAILED saving cache " + cacheFile.getAbsolutePath() + // NOI18N
                            " for:" + file.getAbsolutePath()); // NOI18N
                } else {
                    System.err.println("CACHE: do not save cache for document based file-buffer:" + file.getAbsolutePath()); // NOI18N
                }
            }            
            
        }
    }

    private boolean save2File(File to, FileCache cache) {
        assert (to != null);
        ObjectOutputStream oos = null;
        Object fileLock = getCacheFileLock(to);
        assert (fileLock != null);
        synchronized (fileLock) {
            try {
                OutputStream out = null;
                try {
                    out = new FileOutputStream(to);
                    out = new BufferedOutputStream(out);
                    oos = new ObjectOutputStream(out);
                } finally {
                    if (out != null && oos == null) {
                        out.close();
                    }
                }
                if (oos != null) {
                    oos.writeObject(cache);
                    return true;
                }
            } catch (IOException io) {
                // null cache
            } finally {
                if (oos != null) {
                    try {
                        oos.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }    
        return false;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // save/load file locks
    private static class FileLock {
        String path;
        private FileLock(String path) {
            this.path = path;
        }
        
        @Override
        public String toString() {
            return "read/write cache lock for " + path; // NOI18N
        }        
    };
    
    private Object getCacheFileLock(final File file) {
        Object fileLock = null;
        synchronized (fileLocks) {
            final String path = file.getAbsolutePath();
            fileLock = fileLocks.get(path);
            if (fileLock == null) {
                fileLock = new FileLock(path);
                fileLocks.put(path, fileLock);
            }
        }
        assert (fileLock != null);
        return fileLock;
    }
    
    private Map<CharSequence, Object> fileLocks = new HashMap();
    
    ///////////////////////////////////////////////////////////////////////////
    // save load main cache info
    
    private boolean loaded = false;
    
    // we need exclusive copy of string => use "new String(String)" constructor
    private final String loadLock = new String("CacheManagerImpl load lock"); // NOI18N
    
    private void load() {
        if (!loaded) {
            synchronized (loadLock) {
                index = new MasterIndex();
                loadIndex(cacheDir);
                fileLocks = new HashMap();
                projectCache = new WeakHashMap();
                loaded = true;
            }
        }
    }
    
    private void store() {
        if (TraceFlags.TRACE_CACHE) {
            System.err.println("CACHE: saving cache manager data in " + cacheDir); // NOI18N
        }
        synchronized (loadLock) {
            // store master index 
            storeIndex(cacheDir);
            // store all opened projects
            saveProjects();
        }
    }    
    
    // XXX what are the right events to call this?
    public void close() {
        synchronized (loadLock) {
            store();

            // clean all
            loaded = false;
            projectCache = new WeakHashMap();
            index = new MasterIndex();        
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // master index file support
    // this file contains references from project keys to project-directory name
    // in the main cache directory
    
    private void loadIndex(String baseDir) {
        File file = getMasterIndexFile(baseDir);
        boolean loaded = index.load(file);
        if (TraceFlags.TRACE_CACHE) {
            if (loaded) {
                System.err.println("CACHE: loaded master index:" + file.getAbsolutePath()); // NOI18N
                System.err.println("CACHE: index value" + index); // NOI18N
            } else {
                System.err.println("CACHE: master index not found:" + file.getAbsolutePath()); // NOI18N
            }
        }
    }
    
    private void storeIndex(String baseDir) {
        if (index == null) return;
        File file = getMasterIndexFile(baseDir);
        boolean saved = index.save(file);
        if (TraceFlags.TRACE_CACHE) {
            if (saved) {
                System.err.println("CACHE: saved master index:" + file.getAbsolutePath()); // NOI18N
                System.err.println("index value:" + index.toString()); // NOI18N
            } else {
                System.err.println("CACHE: errors on saving master index:" + file.getAbsolutePath()); // NOI18N
            }
        }
    }
    
    private File getMasterIndexFile(String baseDir) {
        String cache = baseDir + File.separatorChar + "index.dat"; // NOI18N
        File file = new File(cache);
        return file;        
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // save project index
    
    private void saveProjects() {  
        Map copy = new HashMap();
        synchronized (projectCache) {
            copy.putAll(projectCache);   
        }
        for (Iterator it = copy.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Entry) it.next();
            CsmProject project = (CsmProject) entry.getKey();
            String prjDir = (String) index.getProjectDir(project);
            if (prjDir != null) {
                // create project cache and pass full cache path to load own cache
                String prjCachePath = cacheDir + File.separatorChar + prjDir;   
                ProjectCache cache = (ProjectCache) entry.getValue();
                assert (cache != null);
                cache.store(prjCachePath);
            } else {
                System.err.println("SEVERE: CACHE (saveProjects): not found cache directory for project " + project.getName()); // NOI18N
            }
        }         
    }
}

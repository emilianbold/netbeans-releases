/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.modelimpl.cache.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/** 
 * cache manager entry for one project
 * 
 * @author Vladimir Voskresensky
 */
final class ProjectCache {
    // index for cache files of the project
    private ProjectIndex index;
    // accessor to cached info
    private Map cachedFiles/*<String(abs-path), FileCacheSyncBridge>*/;
    
    public ProjectCache(String projectDir) {
        new File(projectDir).mkdirs();
        load(projectDir);
    }        
    
    public FileCacheSyncBridge getSyncCacheBridge(FileImpl file) {
        synchronized (cachedFiles) {
            String absPath = file.getAbsolutePath();
            FileCacheSyncBridge cache = (FileCacheSyncBridge) cachedFiles.get(absPath);
            if (cache == null) {
                if (TraceFlags.TRACE_CACHE) {
                    System.out.println("CACHE: creating FileCacheSyncBridge cache for " + absPath);
                }                 
                cache = new FileCacheSyncBridge(file);
                cachedFiles.put(absPath, cache);
            }
            return cache;
        }
    }
    
    public void invalidateFile(String absPath) {
        synchronized (cachedFiles) {
            if (TraceFlags.TRACE_CACHE) {
                System.out.println("CACHE: remove FileCacheSyncBridge cache for " + absPath);
            }            
            // remove cache of invalid file
            FileCacheSyncBridge cache = (FileCacheSyncBridge) cachedFiles.remove(absPath);
            if (cache != null) {
                cache.invalidate();
            } else {
                if (TraceFlags.TRACE_CACHE) {
                    System.out.println("CACHE: (invalidateFile) not exists FileCacheSyncBridge cache for " + absPath);
                }                    
            }
        }
        ProjectIndex index = getIndex();
        if (index != null) {     
            synchronized (index) {
                index.invalidateFile(absPath);
            }
        }
    }        
    
    private ProjectIndex getIndex() {
        ProjectIndex index = null;
        synchronized (loadLock) {
            if (loaded) {
                index = this.index;
            }
        }
        return index;
    }
    ////////////////////////////////////////////////////////////////////////////
    // load/store/close support 
    
    public void store(String baseDir) {
        if (TraceFlags.TRACE_CACHE) {
            System.out.println("CACHE: saving project cache data in " + baseDir);
        }
        synchronized (loadLock) {
            storeIndex(baseDir);
        }
    }
    
    void close(String baseDir) {
        store(baseDir);
        index = null;
        cachedFiles = null;
        loaded = false;
    }
    
    private boolean loaded = false;
    
    // we need exclusive copy of string => use "new String(String)" constructor
    private final String loadLock = new String("ProjectCache load lock");  
    
    private void load(String baseDir) {
        if (!loaded) {
            synchronized (loadLock) {
                if (!loaded) {
                    index = new ProjectIndex();
                    loadIndex(baseDir);
                    cachedFiles = new HashMap();
                    loaded = true;
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // project index file support
    // this file contains references from files keys to file-cache names
    // in the project's cache directory
    
    private void loadIndex(String baseDir) {
        assert (index != null);
        File file = getProjectIndexFile(baseDir);
        boolean loaded = false;
        synchronized (index) {
            loaded = index.load(file);
        }
        if (TraceFlags.TRACE_CACHE) {
            if (loaded) {
                System.out.println("CACHE: loaded project index:" + file.getAbsolutePath());
                synchronized (index) {
                    System.out.println("CACHE: " + index);
                }
            } else {
                System.out.println("CACHE: project index not found:" + file.getAbsolutePath());
            }
        }
    }
    
    private void storeIndex(String baseDir) {
        if (index == null) return;
        File file = getProjectIndexFile(baseDir);
        boolean saved = false;
        synchronized (index) {
            saved = index.save(file);
        }
        if (TraceFlags.TRACE_CACHE) {
            if (saved) {
                System.out.println("CACHE: saved project index:" + file.getAbsolutePath());
                synchronized (index) {
                    System.out.println("project index:" + index.toString());
                }
            } else {
                System.out.println("CACHE: errors on saving project index:" + file.getAbsolutePath());
            }
        }
    }
    
    private File getProjectIndexFile(String baseDir) {
        String cache = baseDir + File.separatorChar + "project.dat";
        File file = new File(cache);
        return file;        
    }

    public String getValidCacheFileName(FileImpl file, boolean updateEntry) {
        ProjectIndex index = getIndex();
        if (index == null) {
            return null;
        }
        String out = null;
        // return cache only for file based buffers
        // and with correct modified date
        if (file.getBuffer().isFileBased()) {
            long lastModified = file.getBuffer().getFile().lastModified();
            ProjectIndex.Entry entry = null;
            synchronized (index) {
                entry = index.getFileEntry(file);
                if (entry == null) {
                    entry = index.putFile(file);
                }
                if (updateEntry) {
                    entry.setLastModified(lastModified);
                }
            }
            assert (entry != null);
            if (updateEntry) {
                out = entry.getCacheFileName();
            } else {
                if (entry.getLastModified() == lastModified) {
                    out = entry.getCacheFileName();
                } else {
                    if (TraceFlags.TRACE_CACHE) {
                        System.out.println("CACHE: file " + file.getAbsolutePath() + " was modified " + lastModified + " vs. entry=" + entry.getLastModified());
                    }
                }
            }
        }
        return TraceFlags.CACHE_SKIP_SAVE ? null : out;
    }
}
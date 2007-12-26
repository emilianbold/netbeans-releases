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
                    System.err.println("CACHE: creating FileCacheSyncBridge cache for " + absPath); // NOI18N
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
                System.err.println("CACHE: remove FileCacheSyncBridge cache for " + absPath); // NOI18N
            }            
            // remove cache of invalid file
            FileCacheSyncBridge cache = (FileCacheSyncBridge) cachedFiles.remove(absPath);
            if (cache != null) {
                cache.invalidate();
            } else {
                if (TraceFlags.TRACE_CACHE) {
                    System.err.println("CACHE: (invalidateFile) not exists FileCacheSyncBridge cache for " + absPath); // NOI18N
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
            System.err.println("CACHE: saving project cache data in " + baseDir); // NOI18N
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
    private final String loadLock = new String("ProjectCache load lock"); // NOI18N
    
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
                System.err.println("CACHE: loaded project index:" + file.getAbsolutePath()); // NOI18N
                synchronized (index) {
                    System.err.println("CACHE: " + index); // NOI18N
                }
            } else {
                System.err.println("CACHE: project index not found:" + file.getAbsolutePath()); // NOI18N
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
                System.err.println("CACHE: saved project index:" + file.getAbsolutePath()); // NOI18N
                synchronized (index) {
                    System.err.println("project index:" + index.toString()); // NOI18N
                }
            } else {
                System.err.println("CACHE: errors on saving project index:" + file.getAbsolutePath()); // NOI18N
            }
        }
    }
    
    private File getProjectIndexFile(String baseDir) {
        String cache = baseDir + File.separatorChar + "project.dat"; // NOI18N
        File file = new File(cache);
        return file;        
    }

    public CharSequence getValidCacheFileName(FileImpl file, boolean updateEntry) {
        ProjectIndex index = getIndex();
        if (index == null) {
            return null;
        }
        CharSequence out = null;
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
                        System.err.println("CACHE: file " + file.getAbsolutePath() + " was modified " + lastModified + " vs. entry=" + entry.getLastModified()); // NOI18N
                    }
                }
            }
        }
        return TraceFlags.CACHE_SKIP_SAVE ? null : out;
    }
}

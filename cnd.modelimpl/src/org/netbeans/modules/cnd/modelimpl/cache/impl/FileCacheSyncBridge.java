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

import antlr.TokenStream;
import antlr.collections.AST;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.support.APTBuilder;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.cache.FileCache;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 * synchronized bridge to cache file
 * @author Vladimir Voskresensky
 */
//final class FileCacheSyncBridge implements FileCache {
final class FileCacheSyncBridge {
    // cache
    private FileCacheWeakImpl storage;
    // flags indicated which data exists in cache on disk
    private boolean astMaybeOnDisk;
    private boolean aptMaybeOnDisk;
    private boolean aptLightMaybeOnDisk;
    
    private FileImpl fileImpl;
    // valid FALSE means we need to interrupt current activity
    private boolean validCache = true;

    public FileCacheSyncBridge(FileImpl file) {
        assert (file != null);
        this.fileImpl = file;
        this.storage = new FileCacheWeakImpl();
        if (fileImpl.getBuffer().isFileBased()) {
            // we think, everything is on disk
            astMaybeOnDisk = true;
            aptMaybeOnDisk = true;
            aptLightMaybeOnDisk = true;
        } else {
            astMaybeOnDisk = false;
            aptMaybeOnDisk = false;
            aptLightMaybeOnDisk = false;
        }
    }

    public APTFile findAPT() {   
        if (TraceFlags.TRACE_CACHE) {
            System.err.println("CACHE: findAPT for " + fileImpl.getAbsolutePath());
        }         
        APTFile apt = null;
        synchronized (aptLock) {
            apt = storage.getAPT();
            if (apt == null && validCache) {
                if (aptMaybeOnDisk) {
                    synchronized (loadLock) {
                        apt = storage.getAPT();
                        if (apt == null && validCache && aptMaybeOnDisk) {    
                            if (TraceFlags.TRACE_CACHE) {
                                System.err.println("CACHE: findAPT loading from disk for " + fileImpl.getAbsolutePath());
                            }                              
                            FileCache loaded = loadValidCache();
                            if (loaded != null) {
                                if (TraceFlags.TRACE_CACHE) {
                                    System.err.println("CACHE: findAPT loaded from disk for " + fileImpl.getAbsolutePath());
                                }                                 
                                apt = loaded.getAPT();
                            }
                        }
                    }
                    if (apt == null) {                   
                        aptMaybeOnDisk = false;
                    }
                }
                if (apt == null) {
                    // need to create full APT
                    apt = createAPTFull();                
                }                
            }
            // apt should be asked only once! Clear memory
            storage.setAPT(null);
        }
        return apt;
    }
    
    public APTFile findAPTLight() {
        if (TraceFlags.TRACE_CACHE) {
            System.err.println("CACHE: findAPTLight for " + fileImpl.getAbsolutePath());
        }        
        APTFile aptLight = null;
        synchronized (aptLightLock) {
            aptLight = storage.getAPTLight();
            if (aptLight == null && validCache) {
                if (aptLightMaybeOnDisk) {
                    synchronized (loadLock) {
                        aptLight = storage.getAPTLight();
                        if (aptLight == null && validCache && aptLightMaybeOnDisk) {   
                            if (TraceFlags.TRACE_CACHE) {
                                System.err.println("CACHE: findAPTLight loading from disk for " + fileImpl.getAbsolutePath());
                            }                              
                            FileCache loaded = loadValidCache();
                            if (loaded != null) {
                                if (TraceFlags.TRACE_CACHE) {
                                    System.err.println("CACHE: findAPTLight loaded from disk for " + fileImpl.getAbsolutePath());
                                }                                 
                                aptLight = loaded.getAPTLight();                 
                            }
                        }
                    }
                    if (aptLight == null) {    
                        aptLightMaybeOnDisk = false;
                    }                       
                }
                if (aptLight == null) {
                    // need to create APT light
                    aptLight = createAPTLight();                    
                }
            }            
        }
        if (TraceFlags.TRACE_CACHE) {
            if (aptLight == null) {
                System.err.println("CACHE: WARNING!!! No APT Light for " + fileImpl.getAbsolutePath());
            }
        }
        return aptLight;
    }
    
    public FileCache findCacheWithAST(APTPreprocState preprocState) {
        if (TraceFlags.TRACE_CACHE) {
            System.err.println("CACHE: findCacheWithAST for " + fileImpl.getAbsolutePath());
        }            
        AST ast = null;
        APTFile aptLight = null;
        APTFile aptFull = null;
        FileCache loaded = null;
        synchronized (astLock) {
            ast = storage.getAST(preprocState);
            if (ast == null) {
                if (TraceFlags.TRACE_CACHE) {
                    System.err.println("CACHE: findCacheWithAST AST not in memory for " + fileImpl.getAbsolutePath());
                }                 
                if (validCache && astMaybeOnDisk) {
                    synchronized (loadLock) {                       
                        ast = storage.getAST(preprocState);
                        if (ast == null && validCache && astMaybeOnDisk) {
                            if (TraceFlags.TRACE_CACHE) {
                                System.err.println("CACHE: findCacheWithAST loading AST for " + fileImpl.getAbsolutePath()); 
                            }                                   
                            loaded = loadValidCache();
                            if (loaded != null) {
                                ast = loaded.getAST(preprocState);                                   
                                if (TraceFlags.TRACE_CACHE) {
                                    System.err.println("CACHE: findCacheWithAST loaded AST from disk for " + fileImpl.getAbsolutePath());
                                }                                 
                            }
                        }
                    }
                } else {
                    if (TraceFlags.TRACE_CACHE) {
                        System.err.println("CACHE: findCacheWithAST not necessary load cache, because AST not in it for " + fileImpl.getAbsolutePath());
                    }                      
                }
                if (loaded == null) {
                    aptFull = storage.getAPT();
                    if (aptFull == null) {
                        if (TraceFlags.TRACE_CACHE) {
                            System.err.println("CACHE: findCacheWithAST (AST preparation) creating APT for " + fileImpl.getAbsolutePath());
                        }                    
                        aptFull = createAPT(true);
                        if (aptFull != null) {
                            aptLight = storage.getAPTLight();
                            assert (aptLight != null);
                        } else {
                            aptLight = null;
                        }
                    } else {
                        if (TraceFlags.TRACE_CACHE) {
                            System.err.println("CACHE: findCacheWithAST (AST preparation) got APT from memory for " + fileImpl.getAbsolutePath());
                        }
                        aptLight = storage.getAPTLight();
                    }
                }
            } else {
                aptLight = storage.getAPTLight();
            }
            // clear full APT and AST on requesting AST to save memory
            if (TraceFlags.TRACE_CACHE) {
                System.err.println("CACHE: findCacheWithAST clear APT and AST after request to AST");
            }
            storage.setAPT(null);
            storage.setAST(null, null);
            astMaybeOnDisk = loaded == null ? ast != null : loaded.getAST(null) != null;
        }

        if (TraceFlags.TRACE_CACHE) {
            if (aptLight == null && loaded == null) {                    
                System.err.println("CACHE: no APT light while getting AST for " + fileImpl.getAbsolutePath());
            }
        }        
        return loaded == null ? new FileCacheImpl(aptLight, aptFull, ast) : loaded;
    }
    
    void invalidate() {
        validCache = false;
    }
    
//    public APTFile getAPT() {
//        return findAPT();
//    }
//
//    public APTFile getAPTLight() {
//        return findAPTLight();
//    }
//    
//    public AST getAST(APTPreprocState preprocState) {
//        return findCacheWithAST(preprocState).getAST(preprocState);
//    }
    
    public void updateStorage(FileCache cache) {
        APTFile aptLight = cache.getAPTLight();
        if (aptLight != null) {
            this.storage.setAPTLight(aptLight);
            aptLightMaybeOnDisk = true;
        }
        if (cache.getAPT() != null) {
//            this.storage.setAPT(aptFull);
            aptMaybeOnDisk = true;            
        }
        if (cache.getAST(null) != null) {
//            this.storage.setAST(ast,null);
            astMaybeOnDisk = true;
        }        
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // APT creating
    
    private APTFile createAPTFull() {
        if (TraceFlags.TRACE_CACHE) {
            System.err.println("CACHE: creating full APT for " + fileImpl.getAbsolutePath());
        }
        return createAPT(true);
    }
    
    private APTFile createAPTLight() {
        if (TraceFlags.TRACE_CACHE) {
            System.err.println("CACHE: creating APT light for " + fileImpl.getAbsolutePath());
        }        
        return createAPT(false);
    }
    
    private Object createAPTLock = new Object() {
        public String toString() {
            return "APT creating lock for " + fileImpl.getAbsolutePath(); // NOI18N
        }
    };
    
    private APTFile createAPT(boolean full) {
        synchronized (createAPTLock) {
            // quick exit: check if already was added by another creator
            // during wait
            APTFile out = full ? storage.getAPT() : storage.getAPTLight();
            if (out == null) {
                String path = fileImpl.getAbsolutePath();
                // ok, create new apt
                // build token stream for file       
                InputStream stream = null;
                try {
                    stream = fileImpl.getBuffer().getInputStream();               

                    TokenStream ts = APTTokenStreamBuilder.buildTokenStream(path, stream);
                    // build apt from token stream
                    APTFile aptFull = APTBuilder.buildAPT(path, ts);
                    if (aptFull != null) {
                        storage.setAPT(aptFull);
                        APTFile aptLight = (APTFile) APTBuilder.buildAPTLight(aptFull);
                        assert (aptLight != null);
                        storage.setAPTLight(aptLight);
                        out = full ? aptFull : aptLight;
                    }
                } catch (IOException ex) {
                    APTUtils.LOG.log(Level.SEVERE, "create stream: {0}", new Object[] {ex.getMessage()});// NOI18N
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException ex) {
                            APTUtils.LOG.log(Level.SEVERE, "closing stream: {0}", new Object[] {ex.getMessage()});// NOI18N
                        }
                    }
                }
            }
            return out;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // load from cache files
    
    private Object loadLock = new Object();
    private FileCache loadValidCache() {
        FileCache loaded = CacheManager.getInstance().loadValidCache(fileImpl);
        if (loaded != null) {
            storage.setAPT(loaded.getAPT());
            aptMaybeOnDisk = (loaded.getAPT() != null);
            storage.setAPTLight(loaded.getAPTLight());
            aptLightMaybeOnDisk = (loaded.getAPTLight() != null);
            storage.setAST(loaded.getAST(null),null);
            astMaybeOnDisk = (loaded.getAST(null) != null);
        }
        return loaded;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // locks

    private Object aptLock = new Object() {
        public String toString() {
            return "APT lock for " + fileImpl.getAbsolutePath(); // NOI18N
        }
    };

    private Object aptLightLock = new Object() {
        public String toString() {
            return "APT Light lock for " + fileImpl.getAbsolutePath(); // NOI18N
        }
    };
    

    private Object astLock = new Object() {
        public String toString() {
            return "AST lock for " + fileImpl.getAbsolutePath(); // NOI18N
        }
    };    
}

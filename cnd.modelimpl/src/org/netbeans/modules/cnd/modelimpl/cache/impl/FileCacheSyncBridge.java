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
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.apt.support.APTBuilder;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.cache.FileCache;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;

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
    
    // only one of fileUID/fileImplOLD must be used (based on USE_REPOSITORY)
    private FileImpl fileImplOLD;
    private CsmUID<CsmFile> fileUID;
    
    private String absPath;
    
    // valid FALSE means we need to interrupt current activity
    private boolean validCache = true;

    public FileCacheSyncBridge(FileImpl file) {
        assert (file != null);
        this.storage = new FileCacheWeakImpl();
        if (file.getBuffer().isFileBased()) {
            // we think, everything is on disk
            astMaybeOnDisk = true;
            aptMaybeOnDisk = true;
            aptLightMaybeOnDisk = true;
        } else {
            astMaybeOnDisk = false;
            aptMaybeOnDisk = false;
            aptLightMaybeOnDisk = false;
        }
        _setFile(file);
        initLocks(_getAbsolutePath());
    }

    public APTFile findAPT() {   
        if (TraceFlags.TRACE_CACHE) {
            System.err.println("CACHE: findAPT for " + _getAbsolutePath());
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
                                System.err.println("CACHE: findAPT loading from disk for " + _getAbsolutePath());
                            }                              
                            FileCache loaded = loadValidCache();
                            if (loaded != null) {
                                if (TraceFlags.TRACE_CACHE) {
                                    System.err.println("CACHE: findAPT loaded from disk for " + _getAbsolutePath());
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
            System.err.println("CACHE: findAPTLight for " + _getAbsolutePath());
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
                                System.err.println("CACHE: findAPTLight loading from disk for " + _getAbsolutePath());
                            }                              
                            FileCache loaded = loadValidCache();
                            if (loaded != null) {
                                if (TraceFlags.TRACE_CACHE) {
                                    System.err.println("CACHE: findAPTLight loaded from disk for " + _getAbsolutePath());
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
                System.err.println("CACHE: WARNING!!! No APT Light for " + _getAbsolutePath());
            }
        }
        return aptLight;
    }
    
    public FileCache findCacheWithAST(APTPreprocState preprocState) {
        if (TraceFlags.TRACE_CACHE) {
            System.err.println("CACHE: findCacheWithAST for " + _getAbsolutePath());
        }            
        AST ast = null;
        APTFile aptLight = null;
        APTFile aptFull = null;
        FileCache loaded = null;
        synchronized (astLock) {
            ast = storage.getAST(preprocState);
            if (ast == null) {
                if (TraceFlags.TRACE_CACHE) {
                    System.err.println("CACHE: findCacheWithAST AST not in memory for " + _getAbsolutePath());
                }                 
                if (validCache && astMaybeOnDisk) {
                    synchronized (loadLock) {                       
                        ast = storage.getAST(preprocState);
                        if (ast == null && validCache && astMaybeOnDisk) {
                            if (TraceFlags.TRACE_CACHE) {
                                System.err.println("CACHE: findCacheWithAST loading AST for " + _getAbsolutePath()); 
                            }                                   
                            loaded = loadValidCache();
                            if (loaded != null) {
                                ast = loaded.getAST(preprocState);                                   
                                if (TraceFlags.TRACE_CACHE) {
                                    System.err.println("CACHE: findCacheWithAST loaded AST from disk for " + _getAbsolutePath());
                                }                                 
                            }
                        }
                    }
                } else {
                    if (TraceFlags.TRACE_CACHE) {
                        System.err.println("CACHE: findCacheWithAST not necessary load cache, because AST not in it for " + _getAbsolutePath());
                    }                      
                }
                if (loaded == null) {
                    aptFull = storage.getAPT();
                    if (aptFull == null) {
                        if (TraceFlags.TRACE_CACHE) {
                            System.err.println("CACHE: findCacheWithAST (AST preparation) creating APT for " + _getAbsolutePath());
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
                            System.err.println("CACHE: findCacheWithAST (AST preparation) got APT from memory for " + _getAbsolutePath());
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
                System.err.println("CACHE: no APT light while getting AST for " + _getAbsolutePath());
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
            System.err.println("CACHE: creating full APT for " + _getAbsolutePath());
        }
        return createAPT(true);
    }
    
    private APTFile createAPTLight() {
        if (TraceFlags.TRACE_CACHE) {
            System.err.println("CACHE: creating APT light for " + _getAbsolutePath());
        }        
        return createAPT(false);
    }
    
    private Object createAPTLock = new Object() {
        public String toString() {
            return "APT creating lock for " + _getAbsolutePath(); // NOI18N
        }
    };
    
    private APTFile createAPT(boolean full) {
        synchronized (createAPTLock) {
            // quick exit: check if already was added by another creator
            // during wait
            APTFile out = full ? storage.getAPT() : storage.getAPTLight();
            if (out == null) {
                String path = _getAbsolutePath();
                // ok, create new apt
                // build token stream for file       
                InputStream stream = null;
                try {
                    stream = _getFile().getBuffer().getInputStream();               

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
        FileCache loaded = CacheManager.getInstance().loadValidCache(_getFile());
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


    private String _getAbsolutePath() {
        return absPath;
    }
    
    private void _setFile(FileImpl file) {
        this.absPath = file.getAbsolutePath();
        if (TraceFlags.USE_REPOSITORY) {
            this.fileUID = UIDCsmConverter.fileToUID(file);
        } else {
            this.fileImplOLD = file;
        }
    }
    
    private FileImpl _getFile() {
        if (TraceFlags.USE_REPOSITORY) {
            FileImpl file = (FileImpl) UIDCsmConverter.UIDtoFile(fileUID);
            assert (file != null);
            return file;
        } else {
            return fileImplOLD;
        }
    } 
    ////////////////////////////////////////////////////////////////////////////
    // locks

    private void initLocks(String absPath) {
        aptLock = new String("APT lock for " + absPath); // NOI18N
        aptLightLock = new String("APT Light lock for " + absPath); // NOI18N
        astLock = new String("AST lock for " + absPath); // NOI18N
    }
    
    private String aptLock;
    private String aptLightLock;
    private String astLock;
    
}

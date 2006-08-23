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

package org.netbeans.modules.cnd.modelimpl.cache;

import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.modelimpl.csm.Diagnostic;
import java.io.*;
import java.util.*;

import antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmProject;

/**
 * Cache for ASTs
 * @author Vladimir Kvasihn
 */
public class CacheManager implements CsmModelListener {
    
    private static CacheManager instance = new CacheManager();
    private static boolean useCache = Boolean.getBoolean("parser.cache");
    private Map/*<CsmProject, Cache>*/ caches = null;
    private final ProjectCache dummyCache = new ProejctDummyCache();
    
    protected CacheManager() {
        init();
        CsmModel model = CsmModelAccessor.getModel();
        if( model != null ) {
            model.addModelListener(this);
        }
    }
    
    public static CacheManager instance() {
//        if( instance == null ) {
//            synchronized( CacheManager.class ) {
//                if( instance == null ) {
//                    instance = new CacheManager();
//                }
//            }
//        }
        return instance;
    }
    
    public boolean getUseCache() {
        return useCache;
    }
    
    public void setUseCache(boolean useCache) {
        if( this.useCache != useCache ) {
            synchronized( this ) {
                if( this.useCache != useCache ) {
                    this.useCache = useCache;
                    init();
                }
            }
        }
    }
    
//    private static CacheManager createInstance() {
//        if( useCache ) {
//            try {
//                return new CacheManagerImpl();
//            }
//            catch( Exception e ) {
//                e.printStackTrace(System.err);
//                System.err.println("Error initializing cache manager. Switching cache off");
//            }
//        }
//        return new DummyCacheManager();
//    }

    private void init() {
        if( useCache ) {
            caches = new HashMap/*<CsmProject, Cache>*/();
        }
        else {
            caches = null;
        }
    }
    
    public FileCache getCache(CsmProject project, File srcFile) {
        ProjectCache cache = getCache(project);
        return cache.getCache(srcFile);
    }
    
    public void storeCache(CsmProject project, File srcFile, AST ast, List/*<String>*/ includes) {
        ProjectCache cache = getCache(project);
        cache.storeCache(srcFile, ast, includes);
    }
    
    private ProjectCache getCache(CsmProject project) {
        if( useCache ) {
            ProjectCache cache = (ProjectCache) caches.get(project);
            if( cache == null ) {
                try {
                    if( project instanceof LibProjectImpl ) {
                        cache = new ProjectZipCache(project, "/");
                    }
                    else {
                        cache = new ProjectPlainCache(project, "/");
                    }
                }
                catch( IOException e ) {
                    System.err.println("Exception when creating cache for project " + project.getName());
                    e.printStackTrace(System.err);
                    System.err.println("switching cache OFF for this project");
                    cache = dummyCache;
                }
                caches.put(project, cache);
            }
            return cache;
        }
        return dummyCache;
    }

    public void projectClosed(CsmProject project) {
        synchronized( this ) {
            if( caches != null && caches.containsKey(project) ) {
                if( Diagnostic.DEBUG ) Diagnostic.trace("CacheManager: removing cache for project " + project.getName());
                caches.remove(project);
            }
            else {
                if( Diagnostic.DEBUG ) Diagnostic.trace("CacheManager: cache for project " + project.getName() + " not found: nothing to remove");
            }
        }
    }

    public void projectOpened(CsmProject project) {
        if( Diagnostic.DEBUG ) Diagnostic.trace("CacheManager: project opened");
    }

    public void modelChanged(CsmChangeEvent e) {
    }

    
}

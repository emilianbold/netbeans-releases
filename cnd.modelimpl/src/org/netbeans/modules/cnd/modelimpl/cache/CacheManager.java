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
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.apt.structure.APTFile;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.modelimpl.cache.impl.CacheManagerImpl;

/**
 * manager of cached files
 * @author Vladimir Voskresensky
 */
public final class CacheManager implements CsmModelListener {
    private static CacheManager singleton = new CacheManager();
    private CacheManagerImpl impl;
    private CacheManager() {
        impl = new CacheManagerImpl();      
        CsmModel model = CsmModelAccessor.getModel();
        if( model != null ) {
            model.addModelListener(this);
        }           
    }
    
    /**
     * instance of manager
     */
    public static CacheManager getInstance() {
        return singleton;
    }
    
    /**
     * get or create full APT
     * 
     */
    public APTFile findAPT(CsmFile file) {
        // delegate to impl
        return impl.findAPT(file);
    }
    
    /**
     * get or create APT light
     * 
     */
    public APTFile findAPTLight(CsmFile file) {
        // delegate to impl
        return impl.findAPTLight(file);
    }
    
    /**
     * get or create AST
     */
    public FileCache findCacheWithAST(CsmFile file, APTPreprocState preprocState) {
        // delegate to impl
        return impl.findCacheWithAST(file, preprocState);
    }    
    
    /**
     * invalidate information related to file
     */
    public void invalidate(CsmFile file) {
        // delegate to impl
        impl.invalidateFile(file);
    }

    /**
     * invalidate information related to file in all cached projects
     */    
    public void invalidate(String absPath) {
        // delegate to impl
        impl.invalidateAllFiles(absPath);
    }
    
    /**
     * load cache for file (check if cache is valid)
     */
    public FileCache loadValidCache(CsmFile file) {
        return impl.loadValidCache(file);
    }

    /**
     * save cache
     */
    public void saveCache(CsmFile file, FileCache cache) {
        impl.saveCache(file, cache);
    }
    
    /**
     * handle opened project event
     */
    public void projectOpened(CsmProject project) {
        impl.projectOpened(project);
    }

    /**
     * handle closed project event
     */    
    public void projectClosed(CsmProject project) {
        impl.projectClosed(project);
    }

    /**
     * handle change model event
     */    
    public void modelChanged(CsmChangeEvent e) {
        impl.modelChanged(e);
    }
    
    public void close() {
        impl.close();
    }
}

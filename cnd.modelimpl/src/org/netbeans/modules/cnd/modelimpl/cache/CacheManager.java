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

package org.netbeans.modules.cnd.modelimpl.cache;

import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.modelimpl.cache.impl.CacheManagerImpl;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 * manager of cached files
 * @author Vladimir Voskresensky
 */
public final class CacheManager implements CsmModelListener {
    private static CacheManager singleton = new CacheManager();
    private CacheManagerImpl impl;
    private CacheManager() {
        impl = new CacheManagerImpl();
	CsmListeners.getDefault().addModelListener(this);
    }
    
    /**
     * instance of manager
     */
    public static CacheManager getInstance() {
        if (!TraceFlags.USE_AST_CACHE) {
            assert false : "the flag TraceFlags.USE_AST_CACHE is turned OFF, it's impossible to use CacheManager";
        }
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
    public FileCache findCacheWithAST(CsmFile file, APTPreprocHandler preprocHandler) {
        // delegate to impl
        return impl.findCacheWithAST(file, preprocHandler);
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

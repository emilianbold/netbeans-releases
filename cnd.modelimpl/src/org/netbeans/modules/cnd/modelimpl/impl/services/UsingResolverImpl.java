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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmUsingResolver;

/**
 * implementation of using directives and using declarations resolver
 * @author Vladimir Voskresensky
 */
public class UsingResolverImpl extends CsmUsingResolver implements CsmProgressListener {
    
    public UsingResolverImpl() {
        if (cache) {
            CsmModelAccessor.getModel().addProgressListener(this);
        }
    }
    
    public Collection<CsmDeclaration> findUsedDeclarations(CsmFile file, int offset, CsmProject onlyInProject) {
        return getCollector(file, offset, onlyInProject).getUsedDeclarations();
    }
    
    public Collection<CsmNamespace> findVisibleNamespaces(CsmFile file, int offset, CsmProject onlyInProject) {
        return getCollector(file, offset, onlyInProject).getVisibleNamespaces();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // try to cache a little the last request
    
    private Object lock = new Object();
    private Reference<SearchInfo> lastSearch = new SoftReference<SearchInfo>(null);
    
    private final boolean cache = false; // DO NOT CACHE YET
    private FileElementsCollector getCollector(CsmFile file, int offset, CsmProject onlyInProject) {
        if (!cache) {
            return new FileElementsCollector(file, offset, onlyInProject);
        } else {
            synchronized (lock) {
                SearchInfo search = lastSearch.get();
                if (search == null || !search.valid(file, offset, onlyInProject)) {
                    FileElementsCollector collector = new FileElementsCollector(file, offset, onlyInProject);
                    search = new SearchInfo(file, offset, onlyInProject, collector);
                    lastSearch = new SoftReference(search);
                }
                assert search != null;
                assert search.collector != null;
                return search.collector;
            }
        }
    }
    
    private static final class SearchInfo {
        public final CsmFile file;
        public final int offset;
        public final FileElementsCollector collector;
        public final CsmProject onlyInProject;
        public SearchInfo(CsmFile file, int offset, CsmProject onlyInProject, FileElementsCollector collector) {
            this.file = file;
            this.offset = offset;
            this.collector = collector;
            this.onlyInProject = onlyInProject;
        }
        
        private boolean valid(CsmFile file, int offset, CsmProject onlyInProject) {
            return this.file == file && this.offset == offset && this.onlyInProject == onlyInProject;
        }
    }
    
    public void projectParsingStarted(CsmProject project) {
    }
    
    public void projectFilesCounted(CsmProject project, int filesCount) {
    }
    
    public void projectParsingFinished(CsmProject project) {
    }
    
    public void projectParsingCancelled(CsmProject project) {
    }
    
    public void fileInvalidated(CsmFile file) {
    }
    
    public void fileParsingStarted(CsmFile file) {
    }
    
    public void fileParsingFinished(CsmFile file) {
        cleanCache();
    }
    
    public void projectLoaded(CsmProject project) {
    }
    
    public void parserIdle() {
    }
    
    private void cleanCache() {
        synchronized (lock) {
            lastSearch.clear();
        }
    }
}

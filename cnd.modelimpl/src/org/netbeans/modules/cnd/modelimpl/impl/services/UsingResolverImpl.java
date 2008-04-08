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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
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
            CsmListeners.getDefault().addProgressListener(this);
        }
    }
    
    public Collection<CsmDeclaration> findUsedDeclarations(CsmFile file, int offset, CsmProject onlyInProject) {
        return getCollector(file, offset, onlyInProject).getUsedDeclarations();
    }
    
    public Collection<CsmNamespace> findVisibleNamespaces(CsmFile file, int offset, CsmProject onlyInProject) {
        return getCollector(file, offset, onlyInProject).getVisibleNamespaces();
    }
    
    public Collection<CsmNamespaceAlias> findNamespaceAliases(CsmFile file, int offset, CsmProject onlyInProject) {
        return getCollector(file, offset, onlyInProject).getNamespaceAliases();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // try to cache a little the last request
    
    private Object lock = new Object();
    private Reference<SearchInfo> lastSearch = new SoftReference<SearchInfo>(null);
    
    private final boolean cache = true;
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
            return this.file.equals(file) && this.offset == offset && this.onlyInProject == onlyInProject;
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

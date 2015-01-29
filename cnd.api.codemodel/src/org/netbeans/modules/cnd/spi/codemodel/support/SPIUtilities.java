/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.spi.codemodel.support;

import java.net.URI;
import java.util.ArrayList;
import org.netbeans.modules.cnd.spi.codemodel.providers.CMCompilationDataBase;
import org.netbeans.modules.cnd.spi.codemodel.providers.CMIndexProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.api.codemodel.CMIndex;
import org.netbeans.modules.cnd.api.codemodel.CMTranslationUnit;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitQuery;
import org.netbeans.modules.cnd.spi.codemodel.CMIndexImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMTranslationUnitImplementation;
import org.netbeans.modules.cnd.spi.codemodel.impl.APIAccessor;
import org.netbeans.modules.cnd.spi.codemodel.providers.CMParsingProviderImplementation;
import org.netbeans.modules.cnd.spi.codemodel.providers.CMUnsavedFileImplementation;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class SPIUtilities {
    public static CMIndex createIndex(CMCompilationDataBase db, CMVisitQuery.IndexCallback callback, CMVisitQuery.VisitOptions options) {
        Collection<? extends CMIndexProvider> factories = Lookups.forPath(CMIndexProvider.PATH).lookupAll(CMIndexProvider.class);
        for (CMIndexProvider f : factories) {
            if (f.canCreate(db)) {
                CMIndexImplementation idx = f.createAndIndex(db, callback, options);
                assert idx != null;
                return CMFactory.CoreAPI.createIndex(idx);
            }
        }
        System.err.println("No CMIndexProvider factory for " + db);
        return null;
    }

    public static CMIndex parse(CMCompilationDataBase db) {
        return parse(db.getEntries());
    }
    
    public static CMIndex parse(Collection<CMCompilationDataBase.Entry> entries) {
        Collection<? extends CMIndexProvider> factories = Lookups.forPath(CMIndexProvider.PATH).lookupAll(CMIndexProvider.class);
        //Collection<? extends CMIndexProvider> factories = Lookup.getDefault().lookupAll(CMIndexProvider.class);
        for (CMIndexProvider f : factories) {
            if (f.canCreate(entries)) {
                CMIndexImplementation idx = f.create(entries);
                assert idx != null;
                return CMFactory.CoreAPI.createIndex(idx);
            }
        }
        System.err.println("No CMIndexProvider factory for " + entries);
        return null;
    }

    public static CMIndex createIndex(CMCompilationDataBase db, CMVisitQuery.TokenVisitor callback) {
        Collection<? extends CMIndexProvider> factories = Lookups.forPath(CMIndexProvider.PATH).lookupAll(CMIndexProvider.class);
        for (CMIndexProvider f : factories) {
            if (f.canCreate(db)) {
                CMIndexImplementation idx = f.createAndTokenize(db, callback);
                assert idx != null;
                return CMFactory.CoreAPI.createIndex(idx);
            }
        }
        System.err.println("No CMIndexProvider factory for " + db);
        return null;
    }    
    
    public static void reindexFile(Collection<CMIndex> indices, CMCompilationDataBase.Entry entry, CMVisitQuery.IndexCallback callback, CMVisitQuery.VisitOptions options) {
        // does not work for now
        if (true) {
            //delete all data from database
            for (CMParsingProviderImplementation q : queries.allInstances()) {
                q.onReparse(entry.getFile());
            }        
            //re-index?
            //re-parse?
            for (CMIndex index : indices) {
                CMIndexImplementation indexImpl = APIAccessor.get().getIndexImpl(index);
                indexImpl.reindex(entry, callback, options);
            }
        }
    }

    public static Collection<CMTranslationUnit> reparseFile(Collection<CMIndex> indices, CMCompilationDataBase.Entry entry, Collection<CMUnsavedFileImplementation> unsavedFiles) {
        Collection<CMTranslationUnit> res = new ArrayList<>(indices.size());
        for (CMIndex index : indices) {
            CMIndexImplementation indexImpl = APIAccessor.get().getIndexImpl(index);
            CMTranslationUnitImplementation tu = indexImpl.createTranslationUnit(entry, unsavedFiles);
            if (tu != null) {
                res.add(APIAccessor.get().createTranslationUnit(tu));
            }
        }
        return res;
    }
    
    public static int reparseTranslationUnit(CMTranslationUnit tu, Collection<CMUnsavedFileImplementation> unsavedFiles) {
        CMTranslationUnitImplementation tuImpl = APIAccessor.get().getTUImpl(tu);
        return tuImpl.reparse(unsavedFiles);
    }
    
    public static void disposeTranslationUnits(Collection<CMTranslationUnit> units) {
        for (CMTranslationUnit tu : units) {
            APIAccessor.get().getTUImpl(tu).dispose();
        }
    }
    
    public static void disposeTranslationUnit(CMTranslationUnit unit) {
        APIAccessor.get().getTUImpl(unit).dispose();
    }

    public static void registerIndex(Object key, CMIndex index) {
        Indices.getInstance().registerIndex(key, index);
    }

    public static void unregisterIndex(Object key) {
        Indices.getInstance().unregisterIndex(key);
    }

    public static CMIndex getIndex(Object key) {
        return Indices.getInstance().getIndex(key);
    }

    public static CMIndex getIndexByImpl(CMIndexImplementation impl) {
        return Indices.getInstance().getIndexByImpl(impl);
    }

    public static Collection<CMIndex> getIndices() {
        return Indices.getInstance().getIndices();
    }

    public static Collection<CMIndex> getIndices(URI file) {
        // TODO: optimize
        Set<CMIndex> res = new HashSet<>();
        for (CMIndex index : Indices.getInstance().getIndices()) {
            for (CMTranslationUnit tu : index.getTranslationUnits()) {
                if (tu.getFile(file) != null) {
                   res.add(index);
                }
            }
        }
        return res;
    }
    
    private static final Lookup.Result<CMParsingProviderImplementation> queries;
    
    static {
        queries = Lookups.forPath(CMParsingProviderImplementation.PATH).lookupResult(CMParsingProviderImplementation.class);
    }    
    
    public static Collection<URI> getTranslationUnitsURI(URI file) {        
        for (CMParsingProviderImplementation q : queries.allInstances()) {
            Collection<URI> res = q.findTranslationUnitFor(file);
            if (res != null) {
                return res;
            }
        }        
        return Collections.emptyList();
    }    

    public static Collection<CMTranslationUnit> getTranslationUnits(URI file) {
        // TODO: optimize
        Set<CMTranslationUnit> res = new HashSet<>();
        for (CMIndex index : Indices.getInstance().getIndices()) {
            for (CMTranslationUnit tu : index.getTranslationUnits()) {
                if (tu.getFile(file) != null) {
                   res.add(tu);
                }
            }
        }
        return res;
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.gotodeclaration.symbol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.NameAcceptor;
import org.netbeans.modules.cnd.api.model.services.CsmVisibilityQuery;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.spi.jumpto.support.NameMatcher;
import org.netbeans.spi.jumpto.support.NameMatcherFactory;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.util.NbBundle;

/**
 * SymbolProvider for C/C++ implementation
 * @author Vladimir Kvashin
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.jumpto.symbol.SymbolProvider.class)
public class CppSymbolProvider implements SymbolProvider {

    private static class Cache {
        public final String text;
        public final SearchType searchType;
        private final Map<Project, List<CppSymbolDescriptor>> data;
        public Cache(SearchType searchType, String text) {
            this.text = text;
            this.searchType = searchType;
            this.data = new HashMap<Project, List<CppSymbolDescriptor>>();
        }
    }

    private volatile Cache cache;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private static final boolean TRACE = Boolean.getBoolean("cnd.gotosymbol.trace");
    private static final boolean USE_CACHE = CndUtils.getBoolean("cnd.gotosymbol.cache", true);

    public CppSymbolProvider() {
        if (TRACE) { trace("ctor"); } // NOI18N
    }

    @Override
    public void cancel() {
        if (TRACE) { trace("cancel"); } // NOI18N
        cancelled.set(true);
        cache = null;
    }

    @Override
    public void cleanup() {
        if (TRACE) { trace("cleanup"); } // NOI18N
        cancelled.set(false);
        cache = null;
    }

    public static CsmSelect.NameAcceptor createNameAcceptor(final String text, final SearchType searchType) {
        final NameMatcher nameMatcher = NameMatcherFactory.createNameMatcher(text, searchType);
        return new NameAcceptorImpl(nameMatcher);
    }

    // synchronized is just in case here - it shouldn't be called async
    @Override
    public synchronized void computeSymbolNames(Context context, Result result) {
        if (TRACE) { trace("computeSymbolNames %s", toString(context)); } // NOI18N
        cancelled.set(false);
        CsmSelect.NameAcceptor nameAcceptor = createNameAcceptor(context.getText(), context.getSearchType());
        if (nameAcceptor == null) {
            if (CndUtils.isDebugMode()) {
                Logger log = Logger.getLogger("org.netbeans.modules.cnd.gotodeclaration"); // NOI18N
                log.log(Level.SEVERE, "Can not create matcher for ''{0}'' search type {1}", new Object[]{context.getText(), context.getSearchType()}); //NOI18N
            }
            return;
        }

        List<CppSymbolDescriptor> symbols = new ArrayList<CppSymbolDescriptor>();

        boolean filled = false;
        Cache aCache = cache;
        if (aCache != null && context.getSearchType() == aCache.searchType && context.getText().startsWith(aCache.text)) {
            List<CppSymbolDescriptor> cached = aCache.data.get(context.getProject());
            if (cached != null) {
                filled = true;
                long time = System.currentTimeMillis();
                for (CppSymbolDescriptor desc : cached) {
                    if (nameAcceptor.accept(desc.getRawName())) {
                        symbols.add(desc);
                    }
                }
                if (TRACE) { trace("Narrowing %d symbols took %d ms", symbols.size(), System.currentTimeMillis() - time); } //NOI18N
            }
        }
        if (!filled) {
            long time = System.currentTimeMillis();
            CsmCacheManager.enter();
            try {
                collect(context, nameAcceptor, symbols);
            } finally {
                CsmCacheManager.leave();
            }
            if (TRACE) { trace("Collecting %d symbols took %d ms", symbols.size(), System.currentTimeMillis() - time); } //NOI18N
        }
        
        if (cancelled.get()) {
            cache = null;
        } else {
            if (USE_CACHE) {
                aCache = new Cache(context.getSearchType(), context.getText());
                aCache.data.put(context.getProject(), symbols);
                cache = aCache;
            }
            result.addResult(symbols);
        }
        cancelled.set(false);
    }

    private void collect(Context context, NameAcceptor nameAcceptor, List<CppSymbolDescriptor> symbols) {
        if (context.getProject() == null) {
            Set<CsmProject> libs = new HashSet<CsmProject>();
            for (CsmProject csmProject : CsmModelAccessor.getModel().projects()) {
                if (cancelled.get()) {
                    break;
                }
                collectSymbols(csmProject, nameAcceptor, symbols);
                collectLibs(csmProject, libs);
            }
            for (CsmProject csmProject : libs) {
                if (cancelled.get()) {
                    break;
                }
                collectSymbols(csmProject, nameAcceptor, symbols);
            }
        } else {
            NativeProject nativeProject = context.getProject().getLookup().lookup(NativeProject.class);
            if (nativeProject != null) {
                CsmProject csmProject = CsmModelAccessor.getModel().getProject(nativeProject);
                if (csmProject != null) {
                    collectSymbols(csmProject, nameAcceptor, symbols);
                }
            }
        }
    }

    private void collectLibs(CsmProject project, Collection<CsmProject> libs) {
        for( CsmProject lib : project.getLibraries()) {
            if (! libs.contains(lib)) {
                libs.add(lib);
                collectLibs(lib, libs);
            }
        }
    }

    private void collectSymbols(CsmProject csmProject, CsmSelect.NameAcceptor nameAcceptor, List<CppSymbolDescriptor> symbols) {

        // process project namespaces
        collectSymbols(csmProject.getGlobalNamespace(), nameAcceptor, symbols);

        CsmSelect.CsmFilter nameFilter = CsmSelect.getFilterBuilder().createNameFilter(nameAcceptor);

        // process project files
        for(CsmFile csmFile : csmProject.getAllFiles()) {
            // macros
            Iterator<CsmMacro> macros = CsmSelect.getMacros(csmFile, nameFilter);
            while (macros.hasNext() && !cancelled.get()) {
                CsmMacro macro = macros.next();
                if (nameAcceptor.accept(macro.getName())) {
                    if(CsmVisibilityQuery.isVisible(macro)) {
                        symbols.add(new CppSymbolDescriptor(macro));
                    }
                }
            }
            if (cancelled.get()) {
                break;
            }
            // static functions
            Iterator<CsmFunction> funcs = CsmSelect.getStaticFunctions(csmFile, nameFilter);
            while (funcs.hasNext() && !cancelled.get()) {
                CsmFunction func = funcs.next();
                if (nameAcceptor.accept(func.getName())) {
                    if (CsmKindUtilities.isFunctionDefinition(func)) { // which is unlikely, but just in case
                        if(CsmVisibilityQuery.isVisible(func)) {
                            symbols.add(new CppSymbolDescriptor(func));
                        }
                    } else {
                        // static functions definitions are not returned by Select;
                        // neither do they reside in namespace
                        CsmFunctionDefinition definition = func.getDefinition();
                        if (definition != null ) {
                            if(CsmVisibilityQuery.isVisible(definition)) {
                                symbols.add(new CppSymbolDescriptor(definition));
                            }
                        }
                    }
                }
            }
            if (cancelled.get()) {
                break;
            }
            CsmSelect.CsmFilter definitions = CsmSelect.getFilterBuilder().createCompoundFilter(nameFilter,  CsmSelect.getFilterBuilder().createKindFilter(Kind.FUNCTION_DEFINITION));
            Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(csmFile, definitions);
            while (declarations.hasNext() && !cancelled.get()) {
                CsmOffsetableDeclaration decl = declarations.next();
                if (nameAcceptor.accept(decl.getName())) {
                    if (CsmKindUtilities.isFunctionDefinition(decl) && ((CsmFunction)decl).isStatic()) {
                        CsmFunction func = (CsmFunction) decl;
                        if (func.equals(func.getDeclaration()) && CsmKindUtilities.isFile(func.getScope())) {
                            if(CsmVisibilityQuery.isVisible(func)) {
                                symbols.add(new CppSymbolDescriptor(func));
                            }
                        }
                    }
                }
            }
            if (cancelled.get()) {
                break;
            }
            // static variables
            Iterator<CsmVariable> vars = CsmSelect.getStaticVariables(csmFile, nameFilter);
            while (vars.hasNext() && !cancelled.get()) {
                CsmVariable var = vars.next();
                if (nameAcceptor.accept(var.getName())) {
                    if(CsmVisibilityQuery.isVisible(var)) {
                        symbols.add(new CppSymbolDescriptor(var));
                    }
                }
            }
            if (cancelled.get()) {
                break;
            }
        }

    }

    private void collectSymbols(CsmNamespace namespace, CsmSelect.NameAcceptor nameAcceptor, List<CppSymbolDescriptor> symbols) {

        // we can filter out "simple" (non-class) namespace elements via CsmSelect;
        // later we have to instantiate classes and enums to check their *members* as well

        CsmSelect.CsmFilter nameFilter = CsmSelect.getFilterBuilder().createNameFilter(nameAcceptor);

        CsmSelect.CsmFilter simpleKindFilter = CsmSelect.getFilterBuilder().createKindFilter(
                CsmDeclaration.Kind.FUNCTION, CsmDeclaration.Kind.FUNCTION_DEFINITION, 
                CsmDeclaration.Kind.FUNCTION_FRIEND, CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION,
                CsmDeclaration.Kind.VARIABLE, CsmDeclaration.Kind.TYPEDEF);

        CsmSelect.CsmFilter simpleNameAndKindFilter = CsmSelect.getFilterBuilder().createCompoundFilter(nameFilter, simpleKindFilter);
        
        Iterator<? extends CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(namespace, simpleNameAndKindFilter);
        while (declarations.hasNext()) {
            if (cancelled.get()) {
                break;
            }
            CsmOffsetableDeclaration decl = declarations.next();
            if (nameAcceptor.accept(decl.getName())) {
                if (CsmKindUtilities.isFunction(decl)) {
                    // do not add declarations if their definitions exist
                    if (CsmKindUtilities.isFunctionDefinition(decl)) {
                        if(CsmVisibilityQuery.isVisible(decl)) {
                            symbols.add(new CppSymbolDescriptor(decl));
                        }
                    } else {
                        CsmFunctionDefinition definition = ((CsmFunction) decl).getDefinition();
                        if (definition == null || definition == decl) {
                            if(CsmVisibilityQuery.isVisible(decl)) {
                                symbols.add(new CppSymbolDescriptor(decl));
                            }
                        }
                    }
                } else {
                    if(CsmVisibilityQuery.isVisible(decl)) {
                        symbols.add(new CppSymbolDescriptor(decl));
                    }
                }
            }
        }

        // instantiate classes and enums to check them and their members as well
        CsmSelect.CsmFilter kindFilter = CsmSelect.getFilterBuilder().createKindFilter(
                CsmDeclaration.Kind.CLASS, CsmDeclaration.Kind.ENUM, CsmDeclaration.Kind.STRUCT);
        CsmSelect.CsmFilter classesOrMembers = CsmSelect.getFilterBuilder().createOrFilter(kindFilter, nameFilter);
        declarations = CsmSelect.getDeclarations(namespace, kindFilter);
        while (declarations.hasNext()) {
            if (cancelled.get()) {
                break;
            }
            addDeclarationIfNeed(declarations.next(), nameAcceptor, symbols, classesOrMembers, nameFilter);
        }

        // process nested namespaces
        for (CsmNamespace child : namespace.getNestedNamespaces()) {
            if (cancelled.get()) {
                break;
            }
            collectSymbols(child, nameAcceptor, symbols);
        }
    }

    /**
     * Is called for classes, enums and their members.
     * Checks name, if it suites, adds result to symbols collection.
     * Does the same recursively (with members/enumerators)
     */
    private void addDeclarationIfNeed(CsmOffsetableDeclaration decl, CsmSelect.NameAcceptor nameAcceptor, List<CppSymbolDescriptor> symbols,
            CsmSelect.CsmFilter classesOrMembers, CsmSelect.CsmFilter nameFilter) {
        if (nameAcceptor.accept(decl.getName())) {
            if(CsmVisibilityQuery.isVisible(decl)) {
                symbols.add(new CppSymbolDescriptor(decl));
            }
        }
        if (CsmKindUtilities.isClass(decl)) {
            CsmClass cls = (CsmClass) decl;
            final Iterator<CsmMember> classMembers = CsmSelect.getClassMembers(cls, classesOrMembers);
            while(classMembers.hasNext()) {
                addDeclarationIfNeed(classMembers.next(), nameAcceptor, symbols, classesOrMembers, nameFilter);
            }
        } else if (CsmKindUtilities.isEnum(decl)) {
            CsmEnum en = (CsmEnum) decl;
            Iterator<CsmEnumerator> enumerators = CsmSelect.getEnumerators(en, nameFilter);
            while(enumerators.hasNext()) {
                CsmEnumerator enumerator = enumerators.next();
                if (nameAcceptor.accept(enumerator.getName())) {
                    if(CsmVisibilityQuery.isVisible(enumerator)) {
                        symbols.add(new CppSymbolDescriptor(enumerator));
                    }
                }
            }
        }
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "CPP_Provider_Display_Name");
    }

    @Override
    public String name() {
        return "C/C++"; //NOI18N
    }

    private String toString(Context context) {
        return String.format("Context: prj=%s type=%s text=%s", context.getProject(), context.getSearchType(), context.getText()); //NOI18N
    }

    private void trace(String format, Object... args) {
        if (TRACE) {
            format = String.format("%s @%x %s\n", getClass().getSimpleName(), hashCode(), format); //NOI18N
            System.err.printf(format, args);
        }
    }

    private static final class NameAcceptorImpl implements NameAcceptor {

        private final NameMatcher nameMatcher;

        public NameAcceptorImpl(NameMatcher nameMatcher) {
            this.nameMatcher = nameMatcher;
        }

        @Override
        public boolean accept(CharSequence name) {
            return nameMatcher.accept(name.toString());
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 17 * hash + this.nameMatcher.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NameAcceptorImpl other = (NameAcceptorImpl) obj;
            return this.nameMatcher.equals(other.nameMatcher);
        }
    }

}

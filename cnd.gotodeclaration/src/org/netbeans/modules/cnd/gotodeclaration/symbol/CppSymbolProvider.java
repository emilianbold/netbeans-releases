/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.gotodeclaration.util.NameMatcher;
import org.netbeans.modules.cnd.gotodeclaration.util.NameMatcherFactory;
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
        private final Map<CsmProject, List<CppSymbolDescriptor>> data;
        public Cache(SearchType searchType, String text) {
            this.text = text;
            this.searchType = searchType;
            this.data = new HashMap<CsmProject, List<CppSymbolDescriptor>>();
        }
    }

    Cache cache;
    private boolean cancelled;
    private static final boolean TRACE = Boolean.getBoolean("cnd.gotosymbol.trace");

    public CppSymbolProvider() {
        if (TRACE) { trace("ctor"); } // NOI18N
    }

    public void cancel() {
        if (TRACE) { trace("cancel"); } // NOI18N
        cancelled = true;
        cache = null;
    }

    public void cleanup() {
        if (TRACE) { trace("cleanup"); } // NOI18N
        cancelled = false;
        cache = null;
    }

    CsmSelect.NameAcceptor createNameAcceptor(final Context context) {
        final NameMatcher nameMatcher = NameMatcherFactory.createNameMatcher(context.getText(), context.getSearchType());
        return new CsmSelect.NameAcceptor() {
            public boolean accept(CharSequence name) {
                return nameMatcher.accept(name.toString());
            }
        };
    }

    public void computeSymbolNames(Context context, Result result) {
        if (TRACE) { trace("computeSymbolNames %s", toString(context)); } // NOI18N
        cancelled = false;
        CsmSelect.NameAcceptor nameAcceptor = createNameAcceptor(context);

        if (context.getProject() == null) {
            List<CppSymbolDescriptor> symbols = new ArrayList<CppSymbolDescriptor>();
            Set<CsmProject> libs = new HashSet<CsmProject>();
            for (CsmProject csmProject : CsmModelAccessor.getModel().projects()) {
                if (cancelled) {
                    break;
                }
                collectSymbols(csmProject, nameAcceptor, symbols);
                collectLibs(csmProject, libs);                
            }
            for(CsmProject csmProject : libs) {
                if (cancelled) {
                    break;
                }
                collectSymbols(csmProject, nameAcceptor, symbols);
            }
            result.addResult(symbols);
        } else {
            NativeProject nativeProject = context.getProject().getLookup().lookup(NativeProject.class);
            if (nativeProject != null) {
                CsmProject csmProject = CsmModelAccessor.getModel().getProject(nativeProject);
                if (csmProject != null) {
                    List<CppSymbolDescriptor> symbols = new ArrayList<CppSymbolDescriptor>();
                    collectSymbols(csmProject, nameAcceptor, symbols);
                    result.addResult(symbols);
                }
            }
        }
        cancelled = false;
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

        CsmSelect.CsmFilter filter = CsmSelect.getDefault().getFilterBuilder().createNameFilter(nameAcceptor);
        // process project namespaces
        collectSymbols(csmProject.getGlobalNamespace(), filter, symbols);

        // process project files
        for(CsmFile csmFile : csmProject.getAllFiles()) {
            // macros
            Iterator<CsmMacro> macros = CsmSelect.getDefault().getMacros(csmFile, filter);
            while (macros.hasNext() && !cancelled) {
                symbols.add(new CppSymbolDescriptor(macros.next()));
            }
            if (cancelled) {
                break;
            }
            // static functions
            Iterator<CsmFunction> funcs = CsmSelect.getDefault().getStaticFunctions(csmFile, filter);
            while (funcs.hasNext() && !cancelled) {
                symbols.add(new CppSymbolDescriptor(funcs.next()));
            }
            if (cancelled) {
                break;
            }
            // static variables
            Iterator<CsmFunction> vars = CsmSelect.getDefault().getStaticFunctions(csmFile, filter);
            while (vars.hasNext() && !cancelled) {
                symbols.add(new CppSymbolDescriptor(vars.next()));
            }
            if (cancelled) {
                break;
            }
        }

    }

    private void collectSymbols(CsmNamespace namespace, CsmSelect.CsmFilter filter, List<CppSymbolDescriptor> symbols) {
        Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDefault().getDeclarations(namespace, filter);
        while (declarations.hasNext() && ! cancelled) {
            symbols.add(new CppSymbolDescriptor(declarations.next()));
        }
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "CPP_Provider_Display_Name");
    }

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

}

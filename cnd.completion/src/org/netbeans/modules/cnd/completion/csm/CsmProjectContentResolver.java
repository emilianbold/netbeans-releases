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

package org.netbeans.modules.cnd.completion.csm;

import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.editor.StringMap;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;

/**
 * help class to resolve content of the project
 * if file was passed => additionally collect file specific info
 * @author vv159170
 */
public final class CsmProjectContentResolver {
    
    private boolean caseSensitive = false;
    private boolean naturalSort = false;
    private boolean sort = false;
    private CsmFile file;
    private CsmProject project;
    
    public CsmProjectContentResolver() {
        this(false);
    }
    
    public CsmProjectContentResolver(boolean caseSensitive) {
        this(caseSensitive, false, false);
    }
    
    public CsmProjectContentResolver(boolean caseSensitive, boolean naturalSort) {
        this(caseSensitive, true, naturalSort);
    }
    
    public CsmProjectContentResolver(boolean caseSensitive, boolean needSort, boolean naturalSort) {
        this.caseSensitive = caseSensitive;
        this.naturalSort = naturalSort;
        this.sort = needSort;
    }
    
    /**
     * Creates a new instance of CsmProjectContentResolver
     * could be used for getting info only from model
     */
    public CsmProjectContentResolver(CsmProject project) {
        this(project, false);
    }
    
    /**
     * Creates a new instance of CsmProjectContentResolver
     * could be used for getting info only from project
     */
    public CsmProjectContentResolver(CsmProject project, boolean caseSensitive) {
        this(project, caseSensitive, false, false);
    }
    
    /**
     * Creates a new instance of CsmProjectContentResolver
     * could be used for getting info only from project
     */
    public CsmProjectContentResolver(CsmProject project, boolean caseSensitive, boolean needSort, boolean naturalSort) {
        this((CsmFile)null, caseSensitive, needSort, naturalSort);
        this.project=project;
    }
    
    /**
     * Creates a new instance of CsmProjectContentResolver
     * could be used for getting info from file and it's project
     */
    public CsmProjectContentResolver(CsmFile file) {
        this(file, false, false, false);
    }
    
    /**
     * Creates a new instance of CsmProjectContentResolver
     * could be used for getting info from file and it's project
     */
    public CsmProjectContentResolver(CsmFile file, boolean caseSensitive) {
        this(file, caseSensitive, false, false);
    }
    
    /**
     * Creates a new instance of CsmProjectContentResolver
     * could be used for getting info from file and it's project
     */
    public CsmProjectContentResolver(CsmFile file, boolean caseSensitive, boolean needSort, boolean naturalSort) {
        this.caseSensitive = caseSensitive;
        this.naturalSort = naturalSort;
        this.file = file;
        this.project = file != null ? file.getProject() : null;
        this.sort = needSort;
    }

    private List<CsmEnumerator> getEnumeratorsFromEnumsAndTypedefs(List enumsAndTypedefs, boolean match, String strPrefix, boolean sort) {
        List<CsmEnumerator> res = new ArrayList<CsmEnumerator>();
        if (enumsAndTypedefs != null) {
            for (Iterator it = enumsAndTypedefs.iterator(); it.hasNext();) {
                CsmObject ob = (CsmObject) it.next();
                CsmEnum elemEnum = null;
                if (CsmKindUtilities.isEnum(ob)) {
                    elemEnum = (CsmEnum) ob;
                } else {
                    // for typedef check whether it defines unnamed enum
                    assert CsmKindUtilities.isTypedef(ob);
                    CsmTypedef td = (CsmTypedef) ob;
                    if (td.isTypeUnnamed() && td.getType() != null) {
                        CsmType type = td.getType();
                        if (CsmKindUtilities.isEnum(type.getClassifier())) {
                            elemEnum = (CsmEnum) type.getClassifier();
                        }
                    }
                }
                if (elemEnum != null) {
                    for (Iterator enmtrIter = elemEnum.getEnumerators().iterator(); enmtrIter.hasNext();) {
                        CsmEnumerator elem = (CsmEnumerator) enmtrIter.next();
                        if (matchName(elem.getName().toString(), strPrefix, match)) {
                            res.add(elem);
                        }
                    }
                }
            }
            if (sort && res != null) {
                CsmSortUtilities.sortMembers(res, isCaseSensitive());
            }
        }
        return res;
    }
    
    private CsmProject getProject() {
        return this.project;
    }
    
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
    
    public boolean isNaturalSort() {
        return naturalSort;
    }
    
    public void setNaturalSort(boolean naturalSort) {
        this.naturalSort = naturalSort;
    }
    
    public boolean isSortNeeded() {
        return sort;
    }
    
    public void setSortNeeded(boolean sort) {
        this.sort = sort;
    }
    
    public CsmFile getFile() {
        return file;
    }
    
    public void setFile(CsmFile file) {
        this.file = file;
    }
    
    /** ================= help methods =======================================*/
    
    public List getGlobalVariables(String strPrefix, boolean match) {
        if (project == null) {
            return Collections.EMPTY_LIST;
        }
        CsmNamespace globNS = project.getGlobalNamespace();
        // add global variables
        List res = getNamespaceVariables(globNS, strPrefix, match, false, false);
        if (res != null && sort) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List getGlobalFunctions(String strPrefix, boolean match) {
        if (project == null) {
            return Collections.EMPTY_LIST;
        }
        CsmNamespace globNS = project.getGlobalNamespace();
        List res = getNamespaceFunctions(globNS, strPrefix, match, false, false);
        if (res != null && sort) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List getGlobalNamespaces(String strPrefix, boolean match) {
        if (project == null) {
            return Collections.EMPTY_LIST;
        }
        CsmNamespace globNS = project.getGlobalNamespace();
        List res = getNestedNamespaces(globNS, strPrefix, match);
        if (res != null && sort) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // help methods to resolve macros
    
    public List<CsmMacro> getFileLocalMacros(CsmContext context, String strPrefix, boolean match) {
        List<CsmMacro> res = CsmContextUtilities.findFileLocalMacros(context, strPrefix, match, isCaseSensitive());
        if (res != null && isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List<CsmMacro> getFileIncludedProjectMacros(CsmContext context, String strPrefix, boolean match) {
        List<CsmMacro> res = CsmContextUtilities.findFileIncludedProjectMacros(context, strPrefix, match, isCaseSensitive());
        if (res != null && isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List<CsmMacro> getFileIncludeLibMacros(CsmContext context, String strPrefix, boolean match) {
        List<CsmMacro> res = CsmContextUtilities.findFileIncludedLibMacros(context, strPrefix, match, isCaseSensitive());
        if (res != null && isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List<CsmMacro> getProjectMacros(CsmContext context, String strPrefix, boolean match) {
        List<CsmMacro> res = CsmContextUtilities.findProjectMacros(context, strPrefix, match, isCaseSensitive());
        if (res != null && isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List<CsmMacro> getLibMacros(CsmContext context, String strPrefix, boolean match) {
        List<CsmMacro> res = CsmContextUtilities.findLibMacros(context, strPrefix, match, isCaseSensitive());
        if (res != null && isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    ///////////////////////////////////////////////////////////////////////////////
    // help methods to resolve current project libraries content
    
    @SuppressWarnings("unchecked")
    public Collection<CsmVariable> getLibVariables(String strPrefix, boolean match) {
        return getLibElements(NS_VARIABLE_FILTER, strPrefix, match, this.isSortNeeded(), false);
    }
    
    @SuppressWarnings("unchecked")
    public Collection<CsmFunction> getLibFunctions(String strPrefix, boolean match) {
        return getLibElements(NS_FUNCTION_FILTER, strPrefix, match, this.isSortNeeded(), false);
    }
    
    @SuppressWarnings("unchecked")
    public Collection<CsmClassifier> getLibClassesEnums(String strPrefix, boolean match) {
        return getLibElements(NS_CLASS_ENUM_FILTER, strPrefix, match, this.isSortNeeded(), false);
    }
    
    @SuppressWarnings("unchecked")
    public Collection<CsmEnumerator> getLibEnumerators(String strPrefix, boolean match, boolean sort) {
        return getLibElements(NS_ENUMERATOR_FILTER, strPrefix, match, this.isSortNeeded(), false);
    }
    
    @SuppressWarnings("unchecked")
    public Collection<CsmNamespace> getLibNamespaces(String strPrefix, boolean match) {
        return getLibElements(NS_NAMESPACES_FILTER, strPrefix, match, this.isSortNeeded(), false);
    }
    
    @SuppressWarnings("unchecked")
    private Collection getLibElements(NsContentResultsFilter filter, String strPrefix, boolean match, boolean sort, boolean searchNested) {
        if (project == null) {
            return Collections.EMPTY_LIST;
        }
        Set handledLibs = new HashSet();
        Map<String, CsmObject> res = new HashMap<String, CsmObject>();
        // add libararies elements
        for (Iterator it = project.getLibraries().iterator(); it.hasNext();) {
            CsmProject lib = (CsmProject) it.next();
            if (!handledLibs.contains(lib)) {
                handledLibs.add(lib);
                CsmProjectContentResolver libResolver = new CsmProjectContentResolver(lib, isCaseSensitive(), isSortNeeded(), isNaturalSort());
                // TODO: now only direct lib is handled and not libraries of libraries of ...
                res = mergeByFQN(res, filter.getResults(libResolver, lib.getGlobalNamespace(), strPrefix, match, searchNested));
            }
        }
        Collection<CsmObject> out = res.values();
        if (res != null && sort) {
            List<CsmObject> sorted = new ArrayList<CsmObject>(out);
            CsmSortUtilities.sortMembers(sorted, isNaturalSort(), isCaseSensitive());
            out = sorted;
        }
        return out;
    }
    
    /**
     * namespace's content filter
     */
    private interface NsContentResultsFilter {
        /**
         * @param resolver resolver
         * @param ns namespace to analyze using resolver
         * @param strPrefix prefix to search elements starting from
         * @param match flag indicating that results must match prefix
         * @param sort flag indicating that results must be sorted
         * @searchNested flag indicating that results must be searched in nested namespaces as well
         * @returns specific results of analyzed namespace
         */
        public Collection getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean searchNested);
    }
    
    private static final NsContentResultsFilter NS_VARIABLE_FILTER = new NsContentResultsFilter() {
        public Collection getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
            return resolver.getNamespaceVariables(ns, strPrefix, match, searchNested);
        }
    };
    
    private static final NsContentResultsFilter NS_FUNCTION_FILTER = new NsContentResultsFilter() {
        public Collection getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
            return resolver.getNamespaceFunctions(ns, strPrefix, match, searchNested);
        }
    };
    
    private static final NsContentResultsFilter NS_CLASS_ENUM_FILTER = new NsContentResultsFilter() {
        public Collection getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
            return resolver.getNamespaceClassesEnums(ns, strPrefix, match, searchNested);
        }
    };
    
    private static final NsContentResultsFilter NS_ENUMERATOR_FILTER = new NsContentResultsFilter() {
        public Collection getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
            return resolver.getNamespaceEnumerators(ns, strPrefix, match, searchNested);
        }
    };
    
    private static final NsContentResultsFilter NS_NAMESPACES_FILTER = new NsContentResultsFilter() {
        public Collection getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
            return resolver.getGlobalNamespaces(strPrefix, match);
        }
    };
    
    /////////////////////////////////////////////////////////////////////////////////////////
    
    public List<CsmDeclaration> findFunctionLocalDeclarations(CsmContext context, String strPrefix, boolean match) {
        List<CsmDeclaration> res = CsmContextUtilities.findFunctionLocalDeclarations(context, strPrefix, match, isCaseSensitive());
        if (isSortNeeded() && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List<CsmEnumerator> getFileLocalEnumerators(CsmContext context, String strPrefix, boolean match) {
        List<CsmEnumerator> res = CsmContextUtilities.findFileLocalEnumerators(context, strPrefix, match, isCaseSensitive());
        if (isSortNeeded() && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List<CsmVariable> getFileLocalVariables(CsmContext context, String strPrefix, boolean match, boolean needFileLocalOrDeclFromUnnamedNS) {
        List<CsmVariable> out = new ArrayList<CsmVariable>();
        if (!context.isEmpty()) {
            for (Iterator it = context.iterator(); it.hasNext();) {
                CsmContext.CsmContextEntry elem = (CsmContext.CsmContextEntry) it.next();
                if (CsmKindUtilities.isFile(elem.getScope())) {
                    CsmFile currentFile = (CsmFile) elem.getScope();
                    fillFileLocalVariables(strPrefix, match, currentFile, needFileLocalOrDeclFromUnnamedNS, false, out);
                    if (!needFileLocalOrDeclFromUnnamedNS) {
                        fillFileLocalIncludeVariables(strPrefix, match, currentFile, out);
                    }
                    break;
                }
            }
        }
        return out;
    }
    
    public List<CsmFunction> getFileLocalFunctions(CsmContext context, String strPrefix, boolean match, boolean needDeclFromUnnamedNS) {
        List<CsmFunction> out = new ArrayList<CsmFunction>();
        if (!context.isEmpty()) {
            for (Iterator it = context.iterator(); it.hasNext();) {
                CsmContext.CsmContextEntry elem = (CsmContext.CsmContextEntry) it.next();
                if (CsmKindUtilities.isFile(elem.getScope())) {
                    CsmFile currentFile = (CsmFile) elem.getScope();
                    fillFileLocalFunctions(strPrefix, match, currentFile, needDeclFromUnnamedNS, false, out);
                    break;
                }
            }
        }
        return out;
    }
    
    private void fillFileLocalFunctions(String strPrefix, boolean match, 
            CsmFile file, boolean needDeclFromUnnamedNS, boolean fromUnnamedNamespace, 
            Collection<CsmFunction> out) {
        CsmDeclaration.Kind[] kinds;
        if (needDeclFromUnnamedNS||fromUnnamedNamespace) {
            kinds = new CsmDeclaration.Kind[] {
                        CsmDeclaration.Kind.FUNCTION,
                        CsmDeclaration.Kind.FUNCTION_DEFINITION,
                        CsmDeclaration.Kind.NAMESPACE_DEFINITION};
        } else {
            kinds = new CsmDeclaration.Kind[] {
                        CsmDeclaration.Kind.FUNCTION,
                        CsmDeclaration.Kind.FUNCTION_DEFINITION};
        }
        CsmFilter filter = CsmContextUtilities.createFilter(kinds,
                                            strPrefix, match, caseSensitive, fromUnnamedNamespace||needDeclFromUnnamedNS);
        Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDefault().getDeclarations(file, filter);
        while(it.hasNext()) {
            CsmOffsetableDeclaration decl = it.next();
            if (CsmKindUtilities.isFunction(decl)) {
                CsmFunction fun = (CsmFunction) decl;
                if (fromUnnamedNamespace || CsmBaseUtilities.isFileLocalFunction(fun)) {
                    if (decl.getName().length() != 0 && matchName(decl.getName().toString(), strPrefix, match)) {
                        out.add(fun);
                    }
                }
            } else if (needDeclFromUnnamedNS && CsmKindUtilities.isNamespaceDefinition(decl)) {
                if (((CsmNamespaceDefinition)decl).getName().length() == 0) {
                    // add all declarations from unnamed namespace as well
                    fillFileLocalFunctions(strPrefix, match, (CsmNamespaceDefinition)decl, needDeclFromUnnamedNS, true, out);
                }
            }
        }        
    }

    private void fillFileLocalFunctions(String strPrefix, boolean match, 
            CsmNamespaceDefinition ns, boolean needDeclFromUnnamedNS, boolean fromUnnamedNamespace, 
            Collection<CsmFunction> out) {
        CsmDeclaration.Kind[] kinds;
        if (fromUnnamedNamespace||needDeclFromUnnamedNS) {
            kinds = new CsmDeclaration.Kind[] {
                        CsmDeclaration.Kind.FUNCTION,
                        CsmDeclaration.Kind.FUNCTION_DEFINITION,
                        CsmDeclaration.Kind.NAMESPACE_DEFINITION};
        } else {
            kinds = new CsmDeclaration.Kind[] {
                        CsmDeclaration.Kind.FUNCTION,
                        CsmDeclaration.Kind.FUNCTION_DEFINITION};
        }
        CsmFilter filter = CsmContextUtilities.createFilter(kinds,
                           strPrefix, match, caseSensitive, fromUnnamedNamespace||needDeclFromUnnamedNS);
        Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDefault().getDeclarations(ns, filter);
        while(it.hasNext()) {
            CsmOffsetableDeclaration decl = it.next();
            if (CsmKindUtilities.isFunction(decl)) {
                CsmFunction fun = (CsmFunction) decl;
                if (fromUnnamedNamespace || CsmBaseUtilities.isFileLocalFunction(fun)) {
                    if (decl.getName().length() != 0 && matchName(decl.getName().toString(), strPrefix, match)) {
                        out.add(fun);
                    }
                }
            } else if (needDeclFromUnnamedNS && CsmKindUtilities.isNamespaceDefinition(decl)) {
                if (((CsmNamespaceDefinition)decl).getName().length() == 0) {
                    // add all declarations from unnamed namespace as well
                    fillFileLocalFunctions(strPrefix, match, (CsmNamespaceDefinition)decl, needDeclFromUnnamedNS, true, out);
                }
            }
        }        
    }
    
    private void fillFileLocalVariables(String strPrefix, boolean match, 
            CsmFile file, boolean needDeclFromUnnamedNS, boolean fromUnnamedNamespace, 
            Collection<CsmVariable> out) {
        CsmDeclaration.Kind[] kinds;
        if (fromUnnamedNamespace||needDeclFromUnnamedNS) {
            kinds = new CsmDeclaration.Kind[] {
                        CsmDeclaration.Kind.VARIABLE,
                        CsmDeclaration.Kind.VARIABLE_DEFINITION,
                        CsmDeclaration.Kind.NAMESPACE_DEFINITION};
        } else {
            kinds = new CsmDeclaration.Kind[] {
                        CsmDeclaration.Kind.VARIABLE,
                        CsmDeclaration.Kind.VARIABLE_DEFINITION};
        }
        CsmFilter filter = CsmContextUtilities.createFilter(kinds,
                           strPrefix, match, caseSensitive, true);
        Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDefault().getDeclarations(file, filter);
        fillFileLocalVariables(strPrefix, match, it, needDeclFromUnnamedNS, fromUnnamedNamespace, out);
    }
    
    private void fillFileLocalVariables(String strPrefix, boolean match, 
            CsmNamespaceDefinition ns, boolean needDeclFromUnnamedNS, boolean fromUnnamedNamespace, 
            Collection<CsmVariable> out) {
        CsmDeclaration.Kind[] kinds;
        if (fromUnnamedNamespace||needDeclFromUnnamedNS) {
            kinds = new CsmDeclaration.Kind[] {
                        CsmDeclaration.Kind.VARIABLE,
                        CsmDeclaration.Kind.VARIABLE_DEFINITION,
                        CsmDeclaration.Kind.NAMESPACE_DEFINITION};
        } else {
            kinds = new CsmDeclaration.Kind[] {
                        CsmDeclaration.Kind.VARIABLE,
                        CsmDeclaration.Kind.VARIABLE_DEFINITION};
        }
        CsmFilter filter = CsmContextUtilities.createFilter(kinds,
                           strPrefix, match, caseSensitive, true);
        Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDefault().getDeclarations(ns, filter);
        fillFileLocalVariables(strPrefix, match, it, needDeclFromUnnamedNS, fromUnnamedNamespace, out);
    }

    @SuppressWarnings("unchecked")
    private void fillFileLocalVariables(String strPrefix, boolean match, 
            Iterator<CsmOffsetableDeclaration> it, boolean needDeclFromUnnamedNS, boolean fromUnnamedNamespace, 
            Collection<CsmVariable> out) {
        while(it.hasNext()) {
            CsmOffsetableDeclaration decl = it.next();
            if (CsmKindUtilities.isVariable(decl)) {
                CharSequence varName = decl.getName();
                if (fromUnnamedNamespace || CsmKindUtilities.isFileLocalVariable(decl)) {
                    if (varName.length() != 0) {
                        if(matchName(varName.toString(), strPrefix, match)) {
                            out.add((CsmVariable) decl);
                        }
                    } else {
                        CsmVariable var = (CsmVariable) decl;
                        CsmType type = var.getType();
                        if (type != null) {
                            CsmClassifier clsfr = type.getClassifier();
                            if (clsfr != null) {
                                if (CsmKindUtilities.isUnion(clsfr)) {
                                    CsmClass cls = (CsmClass) clsfr;
                                    Collection filtered = CsmSortUtilities.filterList(cls.getMembers(), strPrefix, match, caseSensitive);
                                    out.addAll(filtered);
                                }
                            }
                        }
                    }
                }
            } else if (needDeclFromUnnamedNS && CsmKindUtilities.isNamespaceDefinition(decl)) {
                if (((CsmNamespaceDefinition)decl).getName().length() == 0) {
                    // add all declarations from unnamed namespace as well
                    fillFileLocalVariables(strPrefix, match, (CsmNamespaceDefinition)decl, needDeclFromUnnamedNS, true, out);
                }
            }
        }        
    }

    private void fillFileLocalIncludeVariables(String strPrefix, boolean match, 
            CsmFile file, Collection<CsmVariable> out) {
        CsmDeclaration.Kind[] kinds = new CsmDeclaration.Kind[] {
                        CsmDeclaration.Kind.VARIABLE,
                        CsmDeclaration.Kind.VARIABLE_DEFINITION};
        CsmFilter filter = CsmContextUtilities.createFilter(kinds,
                           strPrefix, match, caseSensitive, false);
        fillFileLocalIncludeVariables(filter, file, out, new HashSet<CsmFile>(), true);
    }
    
    private void fillFileLocalIncludeVariables(CsmFilter filter, CsmFile file,
            Collection<CsmVariable> out, Set<CsmFile> antiLoop, boolean first) {
        if (antiLoop.contains(file)) {
            return;
        }
        antiLoop.add(file);
        for(CsmInclude incl : file.getIncludes()){
            CsmFile f = incl.getIncludeFile();
            if (f != null) {
                fillFileLocalIncludeVariables(filter, f, out, antiLoop, false);
            }
        }
        if (!first) {
            Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDefault().getDeclarations(file, filter);
            while(it.hasNext()) {
                CsmOffsetableDeclaration decl = it.next();
                 if (CsmKindUtilities.isFileLocalVariable(decl)) {
                     out.add((CsmVariable) decl);
                }
            }
        }
    }
    
    
//    public List getLocalDeclarations(CsmContext context, String strPrefix, boolean match) {
//        List res = CsmContextUtilities.findLocalDeclarations(context, strPrefix, match, isCaseSensitive());
//        if (res != null) {
//            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
//        }
//        return res;
//    }
    
    public List<CsmVariable> getNamespaceVariables(CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
        return getNamespaceVariables(ns, strPrefix, match, isSortNeeded(), searchNested);
    }
    
    @SuppressWarnings("unchecked")
    private List<CsmVariable> getNamespaceVariables(CsmNamespace ns, String strPrefix, boolean match, boolean sort, boolean searchNested) {
        List<CsmVariable> res = getNamespaceMembers(ns, CsmDeclaration.Kind.VARIABLE, strPrefix, match, searchNested, false);
        res = filterVariables(res);
        if (sort && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List<CsmFunction> getNamespaceFunctions(CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
        return getNamespaceFunctions(ns, strPrefix, match, isSortNeeded(), searchNested);
    }
    
    @SuppressWarnings("unchecked")
    private List<CsmFunction> getNamespaceFunctions(CsmNamespace ns, String strPrefix, boolean match, boolean sort, boolean searchNested) {
        CsmDeclaration.Kind memberKinds[] = {
            CsmDeclaration.Kind.FUNCTION,
            CsmDeclaration.Kind.FUNCTION_DEFINITION
        };
        List res = getNamespaceMembers(ns, memberKinds, strPrefix, match, searchNested, false);
        res = filterFunctionDefinitions(res);
        if (sort && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List<CsmNamespace> getNestedNamespaces(CsmNamespace ns, String strPrefix, boolean match) {
        List<CsmNamespace> res = new ArrayList<CsmNamespace>();
        // handle all nested namespaces
        for (Iterator it = ns.getNestedNamespaces().iterator(); it.hasNext();) {
            CsmNamespace nestedNs = (CsmNamespace) it.next();
            // TODO: consider when we add nested namespaces
            if (nestedNs.getName().length() != 0 && matchName(nestedNs.getName().toString(), strPrefix, match)) {
                res.add(nestedNs);
            }
        }  
        if (sort && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;        
    }
    
    @SuppressWarnings("unchecked")
    public List<CsmClassifier> getNamespaceClassesEnums(CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
        CsmDeclaration.Kind classKinds[] =	{
            CsmDeclaration.Kind.CLASS,
            CsmDeclaration.Kind.STRUCT,
            CsmDeclaration.Kind.UNION,
            CsmDeclaration.Kind.ENUM,
            CsmDeclaration.Kind.TYPEDEF
        };
        List<CsmClassifier> res = getNamespaceMembers(ns, classKinds, strPrefix, match, searchNested, false);
        if (!ns.getProject().isArtificial() && !ns.isGlobal()){
            for(CsmProject lib : ns.getProject().getLibraries()){
                CsmNamespace n = lib.findNamespace(ns.getQualifiedName());
                if (n != null) {
                    res.addAll(getNamespaceMembers(n, classKinds, strPrefix, match, searchNested, false));
                }
            }
        }
        if (isSortNeeded() && res != null) {
            CsmSortUtilities.sortClasses(res, isCaseSensitive());
        }
        return res;
    }

    public List<CsmEnumerator> getNamespaceEnumerators(CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
        // get all enums and check theirs enumerators
        // also get all typedefs and check whether they define
        // unnamed enum
        CsmDeclaration.Kind classKinds[] =	{
            CsmDeclaration.Kind.ENUM,
            CsmDeclaration.Kind.TYPEDEF
        };        
        List enumsAndTypedefs = getNamespaceMembers(ns, classKinds, "", false, searchNested, true);
        List<CsmEnumerator> res = getEnumeratorsFromEnumsAndTypedefs(enumsAndTypedefs, match, strPrefix, sort);
        return res;
    }

    @SuppressWarnings("unchecked")
    public List<CsmClassifier> getNestedClassifiers(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean match, boolean inspectParentClasses) {
        CsmDeclaration.Kind memberKinds[] = {
            CsmDeclaration.Kind.TYPEDEF,
            CsmDeclaration.Kind.UNION,
            CsmDeclaration.Kind.STRUCT,
            CsmDeclaration.Kind.CLASS,
            CsmDeclaration.Kind.ENUM
        };
        List res = getClassMembers(clazz, contextDeclaration, memberKinds, strPrefix, false, match, inspectParentClasses, true, false);
        if (res != null && this.isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    @SuppressWarnings("unchecked")
    public List<CsmMethod> getMethods(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean staticOnly, boolean match, boolean inspectParentClasses,boolean scopeAccessedClassifier) {
        CsmDeclaration.Kind memberKinds[] = {
            CsmDeclaration.Kind.FUNCTION,
            CsmDeclaration.Kind.FUNCTION_DEFINITION
        };
        List res = getClassMembers(clazz, contextDeclaration, memberKinds, strPrefix, staticOnly, match, inspectParentClasses, scopeAccessedClassifier, false);
        if (res != null && this.isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List/*<CsmField>*/ getFields(CsmClass clazz, boolean staticOnly) {
        return getFields(clazz, clazz, "", staticOnly, false, true,false);
    }
    
    @SuppressWarnings("unchecked")
    public List<CsmField> getFields(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean staticOnly, boolean match, boolean inspectParentClasses,boolean scopeAccessedClassifier) {
        List<CsmField> res = getClassMembers(clazz, contextDeclaration, CsmDeclaration.Kind.VARIABLE, strPrefix, staticOnly, match, inspectParentClasses,scopeAccessedClassifier);
        if (isSortNeeded() && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List<CsmEnumerator> getEnumerators(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean match, boolean inspectParentClasses,boolean scopeAccessedClassifier) {
        // get all enums and check theirs enumerators
        // also get all typedefs and check whether they define
        // unnamed enum
        CsmDeclaration.Kind classKinds[] =	{
            CsmDeclaration.Kind.ENUM,
            CsmDeclaration.Kind.TYPEDEF
        };        
        List enumsAndTypedefs = getClassMembers(clazz, contextDeclaration, classKinds, "", false, false, inspectParentClasses,scopeAccessedClassifier, true);
        List<CsmEnumerator> res = getEnumeratorsFromEnumsAndTypedefs(enumsAndTypedefs, match, strPrefix, sort);
        return res;
    }
    
    public List/*<CsmMember>*/ getFieldsAndMethods(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean staticOnly, boolean match, boolean inspectParentClasses, boolean scopeAccessedClassifier) {
        CsmDeclaration.Kind memberKinds[] = {
            CsmDeclaration.Kind.VARIABLE,
            CsmDeclaration.Kind.FUNCTION,
            CsmDeclaration.Kind.FUNCTION_DEFINITION
        };
        List res = getClassMembers(clazz, contextDeclaration, memberKinds, strPrefix, staticOnly, match, inspectParentClasses, scopeAccessedClassifier, false);
        if (isSortNeeded() && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    private List/*<CsmMember>*/ getClassMembers(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, CsmDeclaration.Kind kind, String strPrefix, boolean staticOnly, boolean match, boolean inspectParentClasses,boolean scopeAccessedClassifier) {
        return getClassMembers(clazz, contextDeclaration, new CsmDeclaration.Kind [] {kind}, strPrefix, staticOnly, match, inspectParentClasses,scopeAccessedClassifier, false);
    }
    
    // =============== help methods to get/check content of containers =========
    
    private static final int INIT_INHERITANCE_LEVEL = 0;
    private static final int NO_INHERITANCE = 1;
    private static final int EXACT_CLASS = 2;
    private static final int CHILD_INHERITANCE = 3;
    
    private List<CsmMember> getClassMembers(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, 
            CsmDeclaration.Kind kinds[], String strPrefix, boolean staticOnly, boolean match, 
            boolean inspectParentClasses, boolean scopeAccessedClassifier, boolean returnUnnamedMembers) {
        assert (clazz != null);
        CsmVisibility minVisibility;
        if (contextDeclaration == null) {
            // we are in global context and are interested in all static members
            minVisibility = CsmInheritanceUtilities.MAX_VISIBILITY;
        } else if (scopeAccessedClassifier) {
            minVisibility = CsmInheritanceUtilities.getContextVisibility(clazz, contextDeclaration, CsmVisibility.PUBLIC, inspectParentClasses);
            if (minVisibility == CsmVisibility.NONE) {
                staticOnly = true;
                minVisibility = CsmInheritanceUtilities.getContextVisibility(clazz, contextDeclaration);
            }
        } else {
            minVisibility = CsmInheritanceUtilities.getContextVisibility(clazz, contextDeclaration);
        }
        
        Map<String, CsmMember> set = getClassMembers(clazz, contextDeclaration, kinds, strPrefix, staticOnly, match,
                new HashSet<CsmClass>(), minVisibility, INIT_INHERITANCE_LEVEL, inspectParentClasses, returnUnnamedMembers);
        List<CsmMember> res;
        if (set != null && set.size() > 0) {
            res = new ArrayList<CsmMember>(set.values());
        } else {
             res = new ArrayList<CsmMember>();
        }
        return res;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, CsmMember> getClassMembers(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, CsmDeclaration.Kind kinds[],
            String strPrefix, boolean staticOnly, boolean match,
            Set<CsmClass> handledClasses, CsmVisibility minVisibility, int inheritanceLevel, boolean inspectParentClasses, 
            boolean returnUnnamedMembers) {
        assert(clazz != null);
        
        if (handledClasses.contains(clazz)) {
            return Collections.<String, CsmMember>emptyMap();
        }       
        
        if (minVisibility == CsmVisibility.NONE) {
            return Collections.<String, CsmMember>emptyMap();
        }

        if (inheritanceLevel == INIT_INHERITANCE_LEVEL) {
            inheritanceLevel = NO_INHERITANCE;
            CsmClass contextClass = CsmBaseUtilities.getContextClass(contextDeclaration);
            if (clazz.equals(contextClass)) {
                inheritanceLevel = EXACT_CLASS;
            } else if (contextClass != null) {
                // check how clazz is visible in context class
                if (CsmInheritanceUtilities.isAssignableFrom(contextClass, clazz)) {
                    inheritanceLevel = CHILD_INHERITANCE;
                }
                // TODO: think about opposite usage C extends B extends A; C is used in A context
                // what is by spec? Are there additional visibility for A about C?
            }            
        } else if (contextDeclaration != null) {
            // min visibility can be changed by context declaration properties
            minVisibility = CsmInheritanceUtilities.getContextVisibility(clazz, contextDeclaration, minVisibility, inheritanceLevel == CHILD_INHERITANCE);
        }
        
        handledClasses.add(clazz);
        Map<String, CsmMember> res = new StringMap();
        Iterator it = clazz.getMembers().iterator();
        while (it.hasNext()) {
            CsmMember member = (CsmMember) it.next();
            if (isKindOf(member.getKind(), kinds) &&
                    (!staticOnly || member.isStatic()) &&
                    matchVisibility(member, minVisibility)) {
                CharSequence memberName = member.getName();
                if ((matchName(memberName.toString(), strPrefix, match)) ||
                        (memberName.length() == 0 && returnUnnamedMembers)) {
                    if (CsmKindUtilities.isFunction(member)) {
                        res.put(((CsmFunction) member).getSignature().toString(), member);
                    } else {
                        res.put(member.getQualifiedName().toString(), member);
                    }
                }
            }
        }
        
        // inspect unnamed unions, structs and classes
        CsmDeclaration.Kind memberKinds[] = {
            CsmDeclaration.Kind.UNION,
            CsmDeclaration.Kind.STRUCT,
            CsmDeclaration.Kind.CLASS,
        };
        it = clazz.getMembers().iterator();
        while (it.hasNext()) {
            CsmMember member = (CsmMember) it.next();
            if (isKindOf(member.getKind(), memberKinds) &&
                    matchVisibility(member, minVisibility)) {
                CharSequence memberName = member.getName();
                if (memberName.length() == 0) {
                    Map<String, CsmMember> set = getClassMembers((CsmClass) member, contextDeclaration, kinds, strPrefix, staticOnly, match,
                        new HashSet<CsmClass>(), CsmVisibility.PUBLIC, INIT_INHERITANCE_LEVEL, inspectParentClasses, returnUnnamedMembers);
                    res.putAll(set);
                }
            }
        }
        
        if (inspectParentClasses) {
            // handle base classes in context of original class/function
            for (it = clazz.getBaseClasses().iterator(); it.hasNext();) {
                CsmInheritance inherit = (CsmInheritance) it.next();
                CsmClass baseClass = inherit.getCsmClass();
                if (baseClass != null) {
                    CsmVisibility nextMinVisibility;
                    int nextInheritanceLevel = inheritanceLevel;
                    if (inheritanceLevel == NO_INHERITANCE) {
                        nextMinVisibility = CsmInheritanceUtilities.mergeExtInheritedVisibility(minVisibility, inherit.getVisibility());
                        nextInheritanceLevel = NO_INHERITANCE;
                    } else if (inheritanceLevel == EXACT_CLASS) {
                        // create merged visibility based on direct inheritance
                        nextMinVisibility = CsmInheritanceUtilities.mergeInheritedVisibility(minVisibility, inherit.getVisibility());
                        nextInheritanceLevel = CHILD_INHERITANCE;
                    } else {
                        assert (inheritanceLevel == CHILD_INHERITANCE);
                        // create merged visibility based on child inheritance
                        nextMinVisibility = CsmInheritanceUtilities.mergeChildInheritanceVisibility(minVisibility, inherit.getVisibility());
                        nextInheritanceLevel = CHILD_INHERITANCE;
                    }
                    
                    Map<String, CsmMember> baseRes = getClassMembers(baseClass, contextDeclaration, kinds, strPrefix, staticOnly, match,
                            handledClasses, nextMinVisibility, nextInheritanceLevel, inspectParentClasses, returnUnnamedMembers);
                    if (baseRes != null && baseRes.size() > 0) {
                        baseRes.putAll(res);
                        res = baseRes;
                    }
                }
            }
        }
        return res;
    }
    
    private List/*<CsmDeclaration>*/ getNamespaceMembers(CsmNamespace ns, CsmDeclaration.Kind kind, String strPrefix, boolean match, boolean searchNested, boolean returnUnnamedMembers) {
        return getNamespaceMembers(ns, new CsmDeclaration.Kind[] {kind}, strPrefix, match, searchNested, returnUnnamedMembers);
    }
    
    private List/*<CsmDeclaration>*/ getNamespaceMembers(CsmNamespace ns, CsmDeclaration.Kind kinds[], String strPrefix, boolean match, boolean searchNested, boolean returnUnnamedMembers) {
        List res = getNamespaceMembers(ns, kinds, strPrefix, match, new HashSet(), searchNested, returnUnnamedMembers);
        return res;
    }
    
    @SuppressWarnings("unchecked")
    private List/*<CsmDeclaration>*/ getNamespaceMembers(CsmNamespace ns, CsmDeclaration.Kind kinds[], String strPrefix, boolean match, Set handledNS, boolean searchNested, boolean returnUnnamedMembers) {
        if (handledNS.contains(ns)) {
            return Collections.EMPTY_LIST;
        }
        
        handledNS.add(ns);
        List res = new ArrayList();
        Iterator it;
        //it = ns.getDeclarations().iterator();
        //filterDeclarations(it, res, kinds, strPrefix, match, returnUnnamedMembers);
        filterDeclarations(ns, res, kinds, strPrefix, match, returnUnnamedMembers);
        // handle all nested namespaces
        if (searchNested) {
            for (it = ns.getNestedNamespaces().iterator(); it.hasNext();) {
                CsmNamespace nestedNs = (CsmNamespace) it.next();
                // TODO: consider when we add nested namespaces
//            if (nestedNs.getName().length() != 0) {
//                if (need namespaces &&
//                        matchName(nestedNs.getName(), strPrefix, match)) {
//                    res.add(nestedNs);
//                }
//            }
                res.addAll(getNamespaceMembers(nestedNs, kinds, strPrefix, match, handledNS, true, returnUnnamedMembers));
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    /*package*/ void filterDeclarations(final CsmNamespace ns, final Collection out, final CsmDeclaration.Kind[] kinds, final String strPrefix, final boolean match, final boolean returnUnnamedMembers) {
        CsmFilter filter = CsmContextUtilities.createFilter(kinds, strPrefix, match, caseSensitive, returnUnnamedMembers);
        Iterator it = CsmSelect.getDefault().getDeclarations(ns, filter);
        while (it.hasNext()) {
            CsmDeclaration decl = (CsmDeclaration) it.next();
            if (isKindOf(decl.getKind(), kinds)) {
                String name = decl.getName().toString();
                if (matchName(name, strPrefix, match) || (name.length() == 0 && returnUnnamedMembers)) {
                    out.add(decl);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    /*package*/ void filterDeclarations(final Iterator in, final Collection out, final CsmDeclaration.Kind kinds[], final String strPrefix, final boolean match, final boolean returnUnnamedMembers) {
        while (in.hasNext()) {
            CsmDeclaration decl = (CsmDeclaration) in.next();
            if (isKindOf(decl.getKind(), kinds)) {
                String name = decl.getName().toString();
                if (matchName(name, strPrefix, match) || (name.length() == 0 && returnUnnamedMembers)) {
                    out.add(decl);
                }
            }
        }
    }
    
    private static boolean isKindOf(CsmDeclaration.Kind kind, CsmDeclaration.Kind kinds[]) {
        for (int i = 0; i < kinds.length; i++) {
            if (kind == kinds[i]) {
                return true;
            }
        }
        return false;
    }
    
    private boolean matchName(String name, String strPrefix) {
        return CsmSortUtilities.matchName(name, strPrefix, false, caseSensitive);
    }
    
    private boolean matchName(String name, String strPrefix, boolean match) {
        return CsmSortUtilities.matchName(name, strPrefix, match, caseSensitive);
    }
    
    private static boolean matchName(String name, String strPrefix, boolean match, boolean caseSensitive) {
        return CsmSortUtilities.matchName(name, strPrefix, match, caseSensitive);
    }
    
    public boolean matchVisibility(CsmMember member, CsmVisibility minVisibility) {
        return CsmInheritanceUtilities.matchVisibility(member, minVisibility);
    }
    
    private Map<String, CsmObject> mergeByFQN(Map<String, CsmObject> orig, Collection<CsmObject> newList) {
        assert orig != null;
        if (newList != null && newList.size() > 0) {
            for (CsmObject object : newList) {
                assert CsmKindUtilities.isQualified(object);
                String fqn = ((CsmQualifiedNamedElement)object).getQualifiedName().toString();
                orig.put(fqn, object);
            }
        }
        return orig;
    }
    
    private <T> Collection<T> merge(Collection<T> orig, Collection<T> newList) {
        return CsmUtilities.merge(orig, newList);
    }
    
    private List<CsmFunction> filterFunctionDefinitions(List funs) {
        List<CsmFunction> out = new ArrayList<CsmFunction>();
        if (funs != null && funs.size() > 0) {
            for (Iterator it = funs.iterator(); it.hasNext();) {
                CsmObject fun = (CsmObject) it.next();
                if (!CsmKindUtilities.isFunctionDefinition(fun) ||
                        ((CsmFunctionDefinition)fun).getDeclaration() == fun ) {
                    out.add((CsmFunction) fun);
                }
            }
        }
        return out;
    }

    private List<CsmVariable> filterVariables(List<CsmVariable> res) {
        Map<String,CsmVariable> out = new HashMap<String, CsmVariable>(res.size());
        for (CsmVariable var : res) {
            String fqn = var.getQualifiedName().toString();
            CsmVariable old = out.get(fqn);
            // replace extern variable by normal one if needed
            if (old == null || !CsmKindUtilities.isExternVariable(var)) {
                out.put(fqn, var);
            }
        }
        return new ArrayList<CsmVariable>(out.values());
    } 
}

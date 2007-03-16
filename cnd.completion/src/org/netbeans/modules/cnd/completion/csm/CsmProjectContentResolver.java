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
import org.netbeans.modules.cnd.api.model.util.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.editor.StringMap;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
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
        boolean sort = isSortNeeded();
        if (project == null) {
            return null;
        }
        CsmNamespace globNS = project.getGlobalNamespace();
        // add global variables
        List res = getNamespaceVariables(globNS, strPrefix, match, false);
        if (res != null && sort) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List getGlobalFunctions(String strPrefix, boolean match) {
        boolean sort = this.isSortNeeded();
        if (project == null) {
            return null;
        }
        CsmNamespace globNS = project.getGlobalNamespace();
        List res = getNamespaceFunctions(globNS, strPrefix, match, false);
        if (res != null && sort) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // help methods to resolve macros
    
    public List getFileMacros(CsmContext context, String strPrefix, boolean match) {
        List res = CsmContextUtilities.findFileMacros(context, strPrefix, match, isCaseSensitive());
        if (res != null && isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List getFileProjectMacros(CsmContext context, String strPrefix, boolean match) {
        List res = CsmContextUtilities.findFileProjectMacros(context, strPrefix, match, isCaseSensitive());
        if (res != null && isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List getFileLibMacros(CsmContext context, String strPrefix, boolean match) {
        List res = CsmContextUtilities.findFileLibMacros(context, strPrefix, match, isCaseSensitive());
        if (res != null && isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List getProjectMacros(CsmContext context, String strPrefix, boolean match) {
        List res = CsmContextUtilities.findProjectMacros(context, strPrefix, match, isCaseSensitive());
        if (res != null && isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List getLibMacros(CsmContext context, String strPrefix, boolean match) {
        List res = CsmContextUtilities.findLibMacros(context, strPrefix, match, isCaseSensitive());
        if (res != null && isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    ///////////////////////////////////////////////////////////////////////////////
    // help methods to resolve current project libraries content
    
    public List getLibVariables(String strPrefix, boolean match) {
        return getLibElements(NS_VARIABLE_FILTER, strPrefix, match, this.isSortNeeded());
    }
    
    public List getLibFunctions(String strPrefix, boolean match) {
        return getLibElements(NS_FUNCTION_FILTER, strPrefix, match, this.isSortNeeded());
    }
    
    public List getLibClassesEnums(String strPrefix, boolean match) {
        return getLibElements(NS_CLASS_ENUM_FILTER, strPrefix, match, this.isSortNeeded());
    }
    
    public List getLibEnumerators(String strPrefix, boolean match, boolean sort) {
        return getLibElements(NS_ENUMERATOR_FILTER, strPrefix, match, this.isSortNeeded());
    }
    
    private List getLibElements(NsContentResultsFilter filter, String strPrefix, boolean match, boolean sort) {
        if (project == null) {
            return null;
        }
        Set handledLibs = new HashSet();
        List res = null;
        // add libararies elements
        for (Iterator it = project.getLibraries().iterator(); it.hasNext();) {
            CsmProject lib = (CsmProject) it.next();
            if (!handledLibs.contains(lib)) {
                handledLibs.add(lib);
                CsmProjectContentResolver libResolver = new CsmProjectContentResolver(lib, isCaseSensitive(), isSortNeeded(), isNaturalSort());
                // TODO: now only direct lib is handled and not libraries of libraries of ...
                res = merge(res, filter.getResults(libResolver, lib.getGlobalNamespace(), strPrefix, match));
            }
        }
        if (res != null && sort) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
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
         * @returns specific results of analyzed namespace
         */
        public List getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match);
    }
    
    private static final NsContentResultsFilter NS_VARIABLE_FILTER = new NsContentResultsFilter() {
        public List getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match) {
            return resolver.getNamespaceVariables(ns, strPrefix, match);
        }
    };
    
    private static final NsContentResultsFilter NS_FUNCTION_FILTER = new NsContentResultsFilter() {
        public List getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match) {
            return resolver.getNamespaceFunctions(ns, strPrefix, match);
        }
    };
    
    private static final NsContentResultsFilter NS_CLASS_ENUM_FILTER = new NsContentResultsFilter() {
        public List getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match) {
            return resolver.getNamespaceClassesEnums(ns, strPrefix, match);
        }
    };
    
    private static final NsContentResultsFilter NS_ENUMERATOR_FILTER = new NsContentResultsFilter() {
        public List getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match) {
            return resolver.getNamespaceEnumerators(ns, strPrefix, match);
        }
    };
    
    /////////////////////////////////////////////////////////////////////////////////////////
    
    public List getFunctionVariables(CsmContext context, String strPrefix, boolean match) {
        List res = CsmContextUtilities.findFunctionLocalVariables(context, strPrefix, match, isCaseSensitive());
        if (isSortNeeded() && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List getFileLocalEnumerators(CsmContext context, String strPrefix, boolean match) {
        List res = CsmContextUtilities.findFileLocalEnumerators(context, strPrefix, match, isCaseSensitive());
        if (isSortNeeded() && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List getFileLocalVariables(CsmContext context, String strPrefix, boolean match) {
        List res = CsmContextUtilities.findFileLocalVariables(context, strPrefix, match, isCaseSensitive());
        if (isSortNeeded() && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
//    public List getLocalDeclarations(CsmContext context, String strPrefix, boolean match) {
//        List res = CsmContextUtilities.findLocalDeclarations(context, strPrefix, match, isCaseSensitive());
//        if (res != null) {
//            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
//        }
//        return res;
//    }
    
    public List getNamespaceVariables(CsmNamespace ns, String strPrefix, boolean match) {
        return getNamespaceVariables(ns, strPrefix, match, isSortNeeded());
    }
    
    private List getNamespaceVariables(CsmNamespace ns, String strPrefix, boolean match, boolean sort) {
        List res = getNamespaceMembers(ns, CsmDeclaration.Kind.VARIABLE, strPrefix, match);
        
        if (sort && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List getNamespaceFunctions(CsmNamespace ns, String strPrefix, boolean match) {
        return getNamespaceFunctions(ns, strPrefix, match, isSortNeeded());
    }
    
    private List getNamespaceFunctions(CsmNamespace ns, String strPrefix, boolean match, boolean sort) {
        CsmDeclaration.Kind memberKinds[] = {
            CsmDeclaration.Kind.FUNCTION,
            CsmDeclaration.Kind.FUNCTION_DEFINITION
        };
        List res = getNamespaceMembers(ns, memberKinds, strPrefix, match);
        res = filterFunctionDefinitions(res);
        if (sort && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List/*<CsmClass>*/ getNamespaceClassesEnums(CsmNamespace ns, String strPrefix, boolean match) {
        CsmDeclaration.Kind classKinds[] =	{
            CsmDeclaration.Kind.CLASS,
            CsmDeclaration.Kind.STRUCT,
            CsmDeclaration.Kind.UNION,
            CsmDeclaration.Kind.ENUM,
            CsmDeclaration.Kind.TYPEDEF
        };
        List res = getNamespaceMembers(ns, classKinds, strPrefix, match);
        if (isSortNeeded() && res != null) {
            CsmSortUtilities.sortClasses(res, isCaseSensitive());
        }
        return res;
    }
    
    public List/*<CsmEnumerator>*/ getNamespaceEnumerators(CsmNamespace ns, String strPrefix, boolean match) {
        boolean sort = isSortNeeded();
        // get all enums and check theirs enumerators
        List enums = getNamespaceMembers(ns, CsmDeclaration.Kind.ENUM, "", false);
        List res = null;
        if (enums != null) {
            for (Iterator it = enums.iterator(); it.hasNext();) {
                CsmEnum elemEnum = (CsmEnum) it.next();
                for (Iterator enmtrIter = elemEnum.getEnumerators().iterator(); enmtrIter.hasNext();) {
                    CsmEnumerator elem = (CsmEnumerator) enmtrIter.next();
                    if (matchName(elem.getName(), strPrefix, match)) {
                        if (res == null) {
                            res = new ArrayList();
                        }
                        res.add(elem);
                    }
                }
            }
            if (sort && res != null) {
                CsmSortUtilities.sortMembers(res, isCaseSensitive());
            }
        }
        return res;
    }
    
    public List/*<CsmField>*/ getMethods(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean staticOnly, boolean match, boolean inspectParentClasses) {
        CsmDeclaration.Kind memberKinds[] = {
            CsmDeclaration.Kind.FUNCTION,
            CsmDeclaration.Kind.FUNCTION_DEFINITION
        };
        List res = getClassMembers(clazz, contextDeclaration, memberKinds, strPrefix, staticOnly, match, inspectParentClasses);
        if (res != null && this.isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List/*<CsmField>*/ getFields(CsmClass clazz, boolean staticOnly) {
        return getFields(clazz, clazz, "", staticOnly, false, true);
    }
    
    public List/*<CsmField>*/ getFields(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean staticOnly, boolean match, boolean inspectParentClasses) {
        List res = getClassMembers(clazz, contextDeclaration, CsmDeclaration.Kind.VARIABLE, strPrefix, staticOnly, match, inspectParentClasses);
        if (isSortNeeded() && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List/*<CsmMember>*/ getFieldsAndMethods(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean staticOnly, boolean match, boolean inspectParentClasses) {
        CsmDeclaration.Kind memberKinds[] = {
            CsmDeclaration.Kind.VARIABLE,
            CsmDeclaration.Kind.FUNCTION,
            CsmDeclaration.Kind.FUNCTION_DEFINITION
        };
        List res = getClassMembers(clazz, contextDeclaration, memberKinds, strPrefix, staticOnly, match, inspectParentClasses);
        if (isSortNeeded() && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    private List/*<CsmMember>*/ getClassMembers(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, CsmDeclaration.Kind kind, String strPrefix, boolean staticOnly, boolean match, boolean inspectParentClasses) {
        return getClassMembers(clazz, contextDeclaration, new CsmDeclaration.Kind [] {kind}, strPrefix, staticOnly, match, inspectParentClasses);
    }
    
    // =============== help methods to get/check content of containers =========
    
    private static final int NO_INHERITANCE = 0;
    private static final int EXACT_CLASS = 1;
    private static final int CHILD_INHERITANCE = 2;
    
    private List/*<CsmMember>*/ getClassMembers(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, CsmDeclaration.Kind kinds[], String strPrefix, boolean staticOnly, boolean match, boolean inspectParentClasses) {
        assert (clazz != null);
        CsmVisibility minVisibility = CsmInheritanceUtilities.getContextVisibility(clazz, contextDeclaration);
        
        int inheritanceLevel = NO_INHERITANCE;
        CsmClass contextClass = CsmBaseUtilities.getContextClass(contextDeclaration);
        if (contextClass == clazz) {
            inheritanceLevel = EXACT_CLASS;
        } else if (contextClass != null) {
            // check how clazz is visible in context class
            if (CsmInheritanceUtilities.isAssignableFrom(contextClass, clazz)) {
                inheritanceLevel = CHILD_INHERITANCE;
            }
            // TODO: think about opposite usage C extends B extends A; C is used in A context
            // what is by spec? Are there additional visibility for A about C?
        }
        
        Map set = getClassMembers(clazz, kinds, strPrefix, staticOnly, match,
                new HashSet(), minVisibility, inheritanceLevel, inspectParentClasses);
        List res = null;
        if (set != null && set.size() > 0) {
            res = new ArrayList(set.values());
        }
        return res;
    }
    
    private Map/*<String, CsmMember>*/ getClassMembers(CsmClass clazz, CsmDeclaration.Kind kinds[],
            String strPrefix, boolean staticOnly, boolean match,
            Set handledClasses, CsmVisibility minVisibility, int inheritanceLevel, boolean inspectParentClasses) {
        assert(clazz != null);
        
        if (handledClasses.contains(clazz)) {
            return Collections.EMPTY_MAP;
        }
        
        if (minVisibility == CsmVisibility.NONE) {
            return Collections.EMPTY_MAP;
        }
        
        handledClasses.add(clazz);
        Map res = new StringMap();
        Iterator it = clazz.getMembers().iterator();
        while (it.hasNext()) {
            CsmMember member = (CsmMember) it.next();
            if (isKindOf(member.getKind(), kinds) &&
                    (!staticOnly || member.isStatic()) &&
                    matchVisibility(member, minVisibility) &&
                    matchName(member.getName(), strPrefix, match)) {
                if (CsmKindUtilities.isFunction(member)) {
                    res.put(((CsmFunction)member).getSignature(), member);
                } else {
                    res.put(member.getQualifiedName(), member);
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
                    
                    Map baseRes = getClassMembers(baseClass, kinds, strPrefix, staticOnly, match,
                            handledClasses, nextMinVisibility, nextInheritanceLevel, inspectParentClasses);
                    if (baseRes != null && baseRes.size() > 0) {
                        baseRes.putAll(res);
                        res = baseRes;
                    }
                }
            }
        }
        return res;
    }
    
    private List/*<CsmDeclaration>*/ getNamespaceMembers(CsmNamespace ns, CsmDeclaration.Kind kind, String strPrefix, boolean match) {
        return getNamespaceMembers(ns, new CsmDeclaration.Kind[] {kind}, strPrefix, match);
    }
    
    private List/*<CsmDeclaration>*/ getNamespaceMembers(CsmNamespace ns, CsmDeclaration.Kind kinds[], String strPrefix, boolean match) {
        List res = getNamespaceMembers(ns, kinds, strPrefix, match, new HashSet());
        return res;
    }
    
    private List/*<CsmDeclaration>*/ getNamespaceMembers(CsmNamespace ns, CsmDeclaration.Kind kinds[], String strPrefix, boolean match, Set handledNS) {
        if (handledNS.contains(ns)) {
            return Collections.EMPTY_LIST;
        }
        
        handledNS.add(ns);
        List res = new ArrayList();
        Iterator it = ns.getDeclarations().iterator();
        while (it.hasNext()) {
            CsmDeclaration decl = (CsmDeclaration) it.next();
            if (isKindOf(decl.getKind(), kinds) &&
                    matchName(decl.getName(), strPrefix, match)) {
                res.add(decl);
            }
        }
        // handle all nested namespaces
        for (it = ns.getNestedNamespaces().iterator(); it.hasNext();) {
            CsmNamespace nestedNs = (CsmNamespace) it.next();
            res.addAll(getNamespaceMembers(nestedNs, kinds, strPrefix, match, handledNS));
        }
        return res;
    }
    
    private boolean isKindOf(CsmDeclaration.Kind kind, CsmDeclaration.Kind kinds[]) {
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
    
    private boolean matchName(String name, String strPrefix, boolean match, boolean caseSensitive) {
        return CsmSortUtilities.matchName(name, strPrefix, match, caseSensitive);
    }
    
    public boolean matchVisibility(CsmMember member, CsmVisibility minVisibility) {
        return CsmInheritanceUtilities.matchVisibility(member, minVisibility);
    }
    
    private List merge(List orig, List newList) {
        return CsmUtilities.merge(orig, newList);
    }
    
    private List filterFunctionDefinitions(List funs) {
        List out = funs;
        if (funs != null && funs.size() > 1) {
            out = new ArrayList();
            for (Iterator it = funs.iterator(); it.hasNext();) {
                CsmObject fun = (CsmObject) it.next();
                if (!CsmKindUtilities.isFunctionDefinition(fun) ||
                        ((CsmFunctionDefinition)fun).getDeclaration() == fun ) {
                    out.add(fun);
                }
            }
        }
        return out;
    }
    
    
}

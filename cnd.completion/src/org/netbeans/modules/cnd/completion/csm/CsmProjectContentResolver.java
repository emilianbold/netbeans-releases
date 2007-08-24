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
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmFriendResolver;
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
        boolean sort = this.isSortNeeded();
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
        boolean sort = this.isSortNeeded();
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
    
    public List getFileLocalMacros(CsmContext context, String strPrefix, boolean match) {
        List res = CsmContextUtilities.findFileLocalMacros(context, strPrefix, match, isCaseSensitive());
        if (res != null && isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List getFileIncludedProjectMacros(CsmContext context, String strPrefix, boolean match) {
        List res = CsmContextUtilities.findFileIncludedProjectMacros(context, strPrefix, match, isCaseSensitive());
        if (res != null && isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List getFileIncludeLibMacros(CsmContext context, String strPrefix, boolean match) {
        List res = CsmContextUtilities.findFileIncludedLibMacros(context, strPrefix, match, isCaseSensitive());
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
        return getLibElements(NS_VARIABLE_FILTER, strPrefix, match, this.isSortNeeded(), false);
    }
    
    public List getLibFunctions(String strPrefix, boolean match) {
        return getLibElements(NS_FUNCTION_FILTER, strPrefix, match, this.isSortNeeded(), false);
    }
    
    public List getLibClassesEnums(String strPrefix, boolean match) {
        return getLibElements(NS_CLASS_ENUM_FILTER, strPrefix, match, this.isSortNeeded(), false);
    }
    
    public List getLibEnumerators(String strPrefix, boolean match, boolean sort) {
        return getLibElements(NS_ENUMERATOR_FILTER, strPrefix, match, this.isSortNeeded(), false);
    }
    
    public List getLibNamespaces(String strPrefix, boolean match) {
        if (project == null) {
            return Collections.EMPTY_LIST;
        }
        Set handledLibs = new HashSet();
        List res = new ArrayList();
        // add libararies elements
        for (Iterator it = project.getLibraries().iterator(); it.hasNext();) {
            CsmProject lib = (CsmProject) it.next();
            if (!handledLibs.contains(lib)) {
                handledLibs.add(lib);
                CsmProjectContentResolver libResolver = new CsmProjectContentResolver(lib, isCaseSensitive(), isSortNeeded(), isNaturalSort());
                // TODO: now only direct lib is handled and not libraries of libraries of ...
                res = merge(res, libResolver.getGlobalNamespaces(strPrefix, match));
            }
        }
        if (res != null && sort) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    private List getLibElements(NsContentResultsFilter filter, String strPrefix, boolean match, boolean sort, boolean searchNested) {
        if (project == null) {
            return Collections.EMPTY_LIST;
        }
        Set handledLibs = new HashSet();
        List res = new ArrayList();
        // add libararies elements
        for (Iterator it = project.getLibraries().iterator(); it.hasNext();) {
            CsmProject lib = (CsmProject) it.next();
            if (!handledLibs.contains(lib)) {
                handledLibs.add(lib);
                CsmProjectContentResolver libResolver = new CsmProjectContentResolver(lib, isCaseSensitive(), isSortNeeded(), isNaturalSort());
                // TODO: now only direct lib is handled and not libraries of libraries of ...
                res = merge(res, filter.getResults(libResolver, lib.getGlobalNamespace(), strPrefix, match, searchNested));
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
         * @searchNested flag indicating that results must be searched in nested namespaces as well
         * @returns specific results of analyzed namespace
         */
        public List getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean searchNested);
    }
    
    private static final NsContentResultsFilter NS_VARIABLE_FILTER = new NsContentResultsFilter() {
        public List getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
            return resolver.getNamespaceVariables(ns, strPrefix, match, searchNested);
        }
    };
    
    private static final NsContentResultsFilter NS_FUNCTION_FILTER = new NsContentResultsFilter() {
        public List getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
            return resolver.getNamespaceFunctions(ns, strPrefix, match, searchNested);
        }
    };
    
    private static final NsContentResultsFilter NS_CLASS_ENUM_FILTER = new NsContentResultsFilter() {
        public List getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
            return resolver.getNamespaceClassesEnums(ns, strPrefix, match, searchNested);
        }
    };
    
    private static final NsContentResultsFilter NS_ENUMERATOR_FILTER = new NsContentResultsFilter() {
        public List getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
            return resolver.getNamespaceEnumerators(ns, strPrefix, match, searchNested);
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
    
    public List getNamespaceVariables(CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
        return getNamespaceVariables(ns, strPrefix, match, isSortNeeded(), searchNested);
    }
    
    private List getNamespaceVariables(CsmNamespace ns, String strPrefix, boolean match, boolean sort, boolean searchNested) {
        List res = getNamespaceMembers(ns, CsmDeclaration.Kind.VARIABLE, strPrefix, match, searchNested);
        res = filterVariables(res);
        if (sort && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List getNamespaceFunctions(CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
        return getNamespaceFunctions(ns, strPrefix, match, isSortNeeded(), searchNested);
    }
    
    private List getNamespaceFunctions(CsmNamespace ns, String strPrefix, boolean match, boolean sort, boolean searchNested) {
        CsmDeclaration.Kind memberKinds[] = {
            CsmDeclaration.Kind.FUNCTION,
            CsmDeclaration.Kind.FUNCTION_DEFINITION
        };
        List res = getNamespaceMembers(ns, memberKinds, strPrefix, match, searchNested);
        res = filterFunctionDefinitions(res);
        if (sort && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List/*<CsmNamespace>*/ getNestedNamespaces(CsmNamespace ns, String strPrefix, boolean match) {
        List res = new ArrayList();
        // handle all nested namespaces
        for (Iterator it = ns.getNestedNamespaces().iterator(); it.hasNext();) {
            CsmNamespace nestedNs = (CsmNamespace) it.next();
            // TODO: consider when we add nested namespaces
            if (nestedNs.getName().length() != 0 && matchName(nestedNs.getName(), strPrefix, match)) {
                res.add(nestedNs);
            }
        }  
        if (sort && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;        
    }
    
    public List/*<CsmClass>*/ getNamespaceClassesEnums(CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
        CsmDeclaration.Kind classKinds[] =	{
            CsmDeclaration.Kind.CLASS,
            CsmDeclaration.Kind.STRUCT,
            CsmDeclaration.Kind.UNION,
            CsmDeclaration.Kind.ENUM,
            CsmDeclaration.Kind.TYPEDEF
        };
        List res = getNamespaceMembers(ns, classKinds, strPrefix, match, searchNested);
        if (isSortNeeded() && res != null) {
            CsmSortUtilities.sortClasses(res, isCaseSensitive());
        }
        return res;
    }

    public List/*<CsmEnumerator>*/ getNamespaceEnumerators(CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
        boolean sort = isSortNeeded();
        // get all enums and check theirs enumerators
        List enums = getNamespaceMembers(ns, CsmDeclaration.Kind.ENUM, "", false, searchNested);
        List res = new ArrayList();
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

    public List/*<CsmField>*/ getNestedClassifiers(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean match, boolean inspectParentClasses) {
        CsmDeclaration.Kind memberKinds[] = {
            CsmDeclaration.Kind.TYPEDEF,
            CsmDeclaration.Kind.UNION,
            CsmDeclaration.Kind.STRUCT,
            CsmDeclaration.Kind.CLASS,
            CsmDeclaration.Kind.ENUM
        };
        List res = getClassMembers(clazz, contextDeclaration, memberKinds, strPrefix, false, match, inspectParentClasses,true);
        if (res != null && this.isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List/*<CsmField>*/ getMethods(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean staticOnly, boolean match, boolean inspectParentClasses,boolean scopeAccessedClassifier) {
        CsmDeclaration.Kind memberKinds[] = {
            CsmDeclaration.Kind.FUNCTION,
            CsmDeclaration.Kind.FUNCTION_DEFINITION
        };
        List res = getClassMembers(clazz, contextDeclaration, memberKinds, strPrefix, staticOnly, match, inspectParentClasses,scopeAccessedClassifier);
        if (res != null && this.isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List/*<CsmField>*/ getFields(CsmClass clazz, boolean staticOnly) {
        return getFields(clazz, clazz, "", staticOnly, false, true,false);
    }
    
    public List/*<CsmField>*/ getFields(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean staticOnly, boolean match, boolean inspectParentClasses,boolean scopeAccessedClassifier) {
        List res = getClassMembers(clazz, contextDeclaration, CsmDeclaration.Kind.VARIABLE, strPrefix, staticOnly, match, inspectParentClasses,scopeAccessedClassifier);
        if (isSortNeeded() && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List/*<CsmEnumerator>*/ getEnumerators(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean match, boolean inspectParentClasses,boolean scopeAccessedClassifier) {
        boolean sort = isSortNeeded();
        // get all enums and check theirs enumerators
        List enums = getClassMembers(clazz, contextDeclaration, CsmDeclaration.Kind.ENUM, "", false, false, inspectParentClasses,scopeAccessedClassifier);
        List res = new ArrayList();
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
    
    public List/*<CsmMember>*/ getFieldsAndMethods(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean staticOnly, boolean match, boolean inspectParentClasses, boolean scopeAccessedClassifier) {
        CsmDeclaration.Kind memberKinds[] = {
            CsmDeclaration.Kind.VARIABLE,
            CsmDeclaration.Kind.FUNCTION,
            CsmDeclaration.Kind.FUNCTION_DEFINITION
        };
        List res = getClassMembers(clazz, contextDeclaration, memberKinds, strPrefix, staticOnly, match, inspectParentClasses,scopeAccessedClassifier);
        if (isSortNeeded() && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    private List/*<CsmMember>*/ getClassMembers(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, CsmDeclaration.Kind kind, String strPrefix, boolean staticOnly, boolean match, boolean inspectParentClasses,boolean scopeAccessedClassifier) {
        return getClassMembers(clazz, contextDeclaration, new CsmDeclaration.Kind [] {kind}, strPrefix, staticOnly, match, inspectParentClasses,scopeAccessedClassifier);
    }
    
    // =============== help methods to get/check content of containers =========
    
    private static final int INIT_INHERITANCE_LEVEL = 0;
    private static final int NO_INHERITANCE = 1;
    private static final int EXACT_CLASS = 2;
    private static final int CHILD_INHERITANCE = 3;
    
    private List/*<CsmMember>*/ getClassMembers(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, CsmDeclaration.Kind kinds[], String strPrefix, boolean staticOnly, boolean match, boolean inspectParentClasses,boolean scopeAccessedClassifier) {
        assert (clazz != null);
        CsmVisibility minVisibility;
        if (contextDeclaration == null) {
            // we are in global context and are interested in all static members
            minVisibility = CsmInheritanceUtilities.MAX_VISIBILITY;
        } else if (scopeAccessedClassifier) {
            minVisibility = CsmInheritanceUtilities.getContextVisibility(clazz, contextDeclaration, CsmVisibility.PUBLIC, inspectParentClasses);
        } else {
            minVisibility = CsmInheritanceUtilities.getContextVisibility(clazz, contextDeclaration);
        }
        
        Map set = getClassMembers(clazz, contextDeclaration, kinds, strPrefix, staticOnly, match,
                new HashSet(), minVisibility, INIT_INHERITANCE_LEVEL, inspectParentClasses);
        List res = new ArrayList();
        if (set != null && set.size() > 0) {
            res = new ArrayList(set.values());
        }
        return res;
    }
    
    private Map/*<String, CsmMember>*/ getClassMembers(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, CsmDeclaration.Kind kinds[],
            String strPrefix, boolean staticOnly, boolean match,
            Set handledClasses, CsmVisibility minVisibility, int inheritanceLevel, boolean inspectParentClasses) {
        assert(clazz != null);
        
        if (handledClasses.contains(clazz)) {
            return Collections.EMPTY_MAP;
        }       
        
        if (minVisibility == CsmVisibility.NONE) {
            return Collections.EMPTY_MAP;
        }

        if (inheritanceLevel == INIT_INHERITANCE_LEVEL) {
            inheritanceLevel = NO_INHERITANCE;
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
        } else if (contextDeclaration != null) {
            // min visibility can be changed by context declaration properties
            minVisibility = CsmInheritanceUtilities.getContextVisibility(clazz, contextDeclaration, minVisibility, inheritanceLevel == CHILD_INHERITANCE);
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
                    
                    Map baseRes = getClassMembers(baseClass, contextDeclaration, kinds, strPrefix, staticOnly, match,
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
    
    private List/*<CsmDeclaration>*/ getNamespaceMembers(CsmNamespace ns, CsmDeclaration.Kind kind, String strPrefix, boolean match, boolean searchNested) {
        return getNamespaceMembers(ns, new CsmDeclaration.Kind[] {kind}, strPrefix, match, searchNested);
    }
    
    private List/*<CsmDeclaration>*/ getNamespaceMembers(CsmNamespace ns, CsmDeclaration.Kind kinds[], String strPrefix, boolean match, boolean searchNested) {
        List res = getNamespaceMembers(ns, kinds, strPrefix, match, new HashSet(), searchNested);
        return res;
    }
    
    private List/*<CsmDeclaration>*/ getNamespaceMembers(CsmNamespace ns, CsmDeclaration.Kind kinds[], String strPrefix, boolean match, Set handledNS, boolean searchNested) {
        if (handledNS.contains(ns)) {
            return Collections.EMPTY_LIST;
        }
        
        handledNS.add(ns);
        List res = new ArrayList();
        Iterator it = ns.getDeclarations().iterator();
        filterDeclarations(it, res, kinds, strPrefix, match);
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
                res.addAll(getNamespaceMembers(nestedNs, kinds, strPrefix, match, handledNS, true));
            }
        }
        return res;
    }

    /*package*/ void filterDeclarations(final Iterator in, final Collection out, final CsmDeclaration.Kind kinds[], final String strPrefix, final boolean match) {
        while (in.hasNext()) {
            CsmDeclaration decl = (CsmDeclaration) in.next();
            if (isKindOf(decl.getKind(), kinds) &&
                    matchName(decl.getName(), strPrefix, match)) {
                out.add(decl);
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
    
    private List merge(List orig, List newList) {
        return (List)CsmUtilities.merge(orig, newList);
    }
    
    private List filterFunctionDefinitions(List funs) {
        List out = new ArrayList();
        if (funs != null && funs.size() > 0) {
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

    private List filterVariables(List<CsmVariable> res) {
        Map<String,CsmVariable> out = new HashMap<String, CsmVariable>(res.size());
        for (CsmVariable var : res) {
            String fqn = var.getQualifiedName();
            CsmVariable old = out.get(fqn);
            // replace extern variable by normal one if needed
            if (old == null || !CsmKindUtilities.isExternVariable(var)) {
                out.put(fqn, var);
            }
        }
        return new ArrayList(out.values());
    } 
}

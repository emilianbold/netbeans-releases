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
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.editor.StringMap;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;

/**
 * help class to resolve content of the project
 * if file was passed => additionally collect file specific info
 * @author vv159170
 */
public class CsmProjectContentResolver {
    
    private boolean caseSensitive = false;
    private boolean naturalSort = false;
    private CsmFile file;
    private CsmProject project;

    public CsmProjectContentResolver () {
        this(false);
    }
    
    public CsmProjectContentResolver (boolean caseSensitive) {
        this(caseSensitive, false);
    }

    public CsmProjectContentResolver (boolean caseSensitive, boolean naturalSort) {
        this.caseSensitive = caseSensitive;
        this.naturalSort = naturalSort;
    }
    
    /** 
     * Creates a new instance of CsmProjectContentResolver 
     * could be used for getting info only from model
     */
    public CsmProjectContentResolver (CsmProject project) {
        this(project, false);
    }
    
    /** 
     * Creates a new instance of CsmProjectContentResolver 
     * could be used for getting info only from project
     */
    public CsmProjectContentResolver (CsmProject project, boolean caseSensitive) {
        this(project, caseSensitive, true);
    }

    /** 
     * Creates a new instance of CsmProjectContentResolver 
     * could be used for getting info only from project
     */    
    public CsmProjectContentResolver (CsmProject project, boolean caseSensitive, boolean naturalSort) {
        this((CsmFile)null, caseSensitive, naturalSort);
        this.project=project;
    }
    
    /** 
     * Creates a new instance of CsmProjectContentResolver 
     * could be used for getting info from file and it's project
     */    
    public CsmProjectContentResolver (CsmFile file) {
        this(file, false, true);
    }    
    
    /** 
     * Creates a new instance of CsmProjectContentResolver 
     * could be used for getting info from file and it's project
     */    
    public CsmProjectContentResolver (CsmFile file, boolean caseSensitive) {
        this(file, caseSensitive, true);
    } 
    
    /** 
     * Creates a new instance of CsmProjectContentResolver 
     * could be used for getting info from file and it's project
     */    
    public CsmProjectContentResolver (CsmFile file, boolean caseSensitive, boolean naturalSort) {
        this.caseSensitive = caseSensitive;
        this.naturalSort = naturalSort;
        this.file = file;
        this.project = file != null ? file.getProject () : null;
    } 
    
    protected CsmProject getProject() {
        return this.project;
    }

    public boolean isCaseSensitive () {
        return caseSensitive;
    }

    public void setCaseSensitive (boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isNaturalSort () {
        return naturalSort;
    }

    public void setNaturalSort (boolean naturalSort) {
        this.naturalSort = naturalSort;
    }

    public CsmFile getFile () {
        return file;
    }

    public void setFile (CsmFile file) {
        this.file = file;
    }
    
    /** ================= help methods =======================================*/

    public List getGlobalVariables(String strPrefix, boolean match, boolean sort) {
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

    public List getGlobalFunctions(String strPrefix, boolean match, boolean sort) {
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

    ///////////////////////////////////////////////////////////////////////////////
    // help methods to resolve current project libraries content
    
    public List getLibVariables(String strPrefix, boolean match, boolean sort) {
        return getLibElements(NS_VARIABLE_FILTER, strPrefix, match, sort);
    } 
    
    public List getLibFunctions(String strPrefix, boolean match, boolean sort) {
        return getLibElements(NS_FUNCTION_FILTER, strPrefix, match, sort);        
    }       

    public List getLibClassesEnums(String strPrefix, boolean match, boolean sort) {
        return getLibElements(NS_CLASS_ENUM_FILTER, strPrefix, match, sort);         
    }   
    
    public List getLibEnumerators(String strPrefix, boolean match, boolean sort) {
        return getLibElements(NS_ENUMERATOR_FILTER, strPrefix, match, sort);
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
                CsmProjectContentResolver libResolver = new CsmProjectContentResolver(lib, isCaseSensitive(), isNaturalSort());
                // TODO: now only direct lib is handled and not libraries of libraries of ...
                res = merge(res, filter.getResults(libResolver, lib.getGlobalNamespace(), strPrefix, match, false));
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
        public List getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean sort);
    }
    
    private static final NsContentResultsFilter NS_VARIABLE_FILTER = new NsContentResultsFilter() {
        public List getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean sort) {
            return resolver.getNamespaceVariables(ns, strPrefix, match, sort);
        }
    };
    
    private static final NsContentResultsFilter NS_FUNCTION_FILTER = new NsContentResultsFilter() {
        public List getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean sort) {
            return resolver.getNamespaceFunctions(ns, strPrefix, match, sort);
        }
    };
    
    private static final NsContentResultsFilter NS_CLASS_ENUM_FILTER = new NsContentResultsFilter() {
        public List getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean sort) {
            return resolver.getNamespaceClassesEnums(ns, strPrefix, match, sort);
        }
    };
    
    private static final NsContentResultsFilter NS_ENUMERATOR_FILTER = new NsContentResultsFilter() {
        public List getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean sort) {
            return resolver.getNamespaceEnumerators(ns, strPrefix, match, sort);
        }
    };
    
    /////////////////////////////////////////////////////////////////////////////////////////
    
    public List getFunctionVariables(CsmContext context, String strPrefix, boolean match) {
        List res = CsmContextUtilities.findFunctionLocalVariables(context, strPrefix, match, isCaseSensitive());
        if (res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }
    
    public List getFileLocalVariables(CsmContext context, String strPrefix, boolean match) {
        List res = CsmContextUtilities.findFileLocalVariables(context, strPrefix, match, isCaseSensitive());
        if (res != null) {
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
        return getNamespaceVariables(ns, strPrefix, match, true);
    }
    
    public List getNamespaceVariables(CsmNamespace ns, String strPrefix, boolean match, boolean sort) {
	List res = getNamespaceMembers(ns, CsmDeclaration.Kind.VARIABLE, strPrefix, match);
        
        if (sort && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    public List getNamespaceFunctions(CsmNamespace ns, String strPrefix, boolean match) {
        return getNamespaceFunctions(ns, strPrefix, match, true);
    }    
    
    public List getNamespaceFunctions(CsmNamespace ns, String strPrefix, boolean match, boolean sort) {
	CsmDeclaration.Kind memberKinds[] = {
						CsmDeclaration.Kind.FUNCTION,
                                                CsmDeclaration.Kind.FUNCTION_DEFINITION
						};	
	List res = getNamespaceMembers(ns, memberKinds, strPrefix, match);
        if (sort && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());  
        }
        return res;
    }

    public List/*<CsmClass>*/ getNamespaceClassesEnums(CsmNamespace ns, String strPrefix, boolean match) {
        return getNamespaceClassesEnums(ns, strPrefix, match, true);
    }
    
    public List/*<CsmClass>*/ getNamespaceClassesEnums(CsmNamespace ns, String strPrefix, boolean match, boolean sort) {
	CsmDeclaration.Kind classKinds[] =	{
					CsmDeclaration.Kind.CLASS,
					CsmDeclaration.Kind.STRUCT,
					CsmDeclaration.Kind.UNION,
                                        CsmDeclaration.Kind.ENUM
					};
	List res = getNamespaceMembers(ns, classKinds, strPrefix, match);
        if (sort && res != null) {
            CsmSortUtilities.sortClasses(res, isCaseSensitive());
        }
        return res;
    }

    public List/*<CsmEnumerator>*/ getNamespaceEnumerators(CsmNamespace ns, String strPrefix, boolean match) {
        return getNamespaceEnumerators(ns, strPrefix, match, true);
    }
    
    public List/*<CsmEnumerator>*/ getNamespaceEnumerators(CsmNamespace ns, String strPrefix, boolean match, boolean sort) {
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
    
    public List/*<CsmField>*/ getMethods(CsmClass clazz, CsmVisibility minVisibility, String strPrefix, boolean staticOnly, boolean match) {
	CsmDeclaration.Kind memberKinds[] = {
						CsmDeclaration.Kind.FUNCTION,
                                                CsmDeclaration.Kind.FUNCTION_DEFINITION
						};	
        List res = getClassMembers(clazz, minVisibility, memberKinds, strPrefix, staticOnly, match);
        if (res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }   

    public List/*<CsmField>*/ getFields(CsmClass clazz, boolean staticOnly) {
        return getFields(clazz, CsmInheritanceUtilities.MAX_VISIBILITY, "", staticOnly, false);
    }  
    
    public List/*<CsmField>*/ getFields(CsmClass clazz, CsmVisibility minVisibility, String strPrefix, boolean staticOnly, boolean match) {
	List res = getClassMembers(clazz, minVisibility, CsmDeclaration.Kind.VARIABLE, strPrefix, staticOnly, match);
        if (res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }    

    public List/*<CsmMember>*/ getFieldsAndMethods(CsmClass clazz, CsmVisibility minVisibility, String strPrefix, boolean staticOnly, boolean match) { 
	CsmDeclaration.Kind memberKinds[] = {
						CsmDeclaration.Kind.VARIABLE,
						CsmDeclaration.Kind.FUNCTION,
                                                CsmDeclaration.Kind.FUNCTION_DEFINITION
						};					    
	List res = getClassMembers(clazz, minVisibility, memberKinds, strPrefix, staticOnly, match);
        if (res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    protected List/*<CsmMember>*/ getClassMembers(CsmClass clazz, CsmVisibility minVisibility, CsmDeclaration.Kind kind, String strPrefix, boolean staticOnly, boolean match) {
	return getClassMembers(clazz, minVisibility, new CsmDeclaration.Kind [] {kind}, strPrefix, staticOnly, match);
    }

    // =============== help methods to get/check content of containers =========

    protected List/*<CsmMember>*/ getClassMembers(CsmClass clazz, CsmVisibility minVisibility, CsmDeclaration.Kind kinds[], String strPrefix, boolean staticOnly, boolean match) {
	Map set = getClassMembers(clazz, minVisibility, kinds, strPrefix, staticOnly, match, new HashSet(), 0);
        List res = null;
        if (set != null && set.size() > 0) {
            res = new ArrayList(set.values());
        }
        return res;
    }

    protected Map/*<String, CsmMember>*/ getClassMembers(CsmClass clazz, CsmVisibility minVisibility, CsmDeclaration.Kind kinds[], String strPrefix, boolean staticOnly, boolean match, Set handledClasses, int level) {
        if (handledClasses.contains(clazz) || minVisibility == CsmVisibility.NONE) {
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
                if (CsmKindUtilities.isFunctionDeclaration(member)) {
                    res.put(((CsmFunction)member).getSignature(), member);
                } else {
                    res.put(member.getQualifiedName(), member);
                }
	    }
	}
        // handle base classes
        for (it = clazz.getBaseClasses().iterator(); it.hasNext();) {
            CsmInheritance inherit = (CsmInheritance) it.next();
            CsmVisibility mergedVisibility;
            if (level == 0) {
                // create merged visibility based on child inheritance
                mergedVisibility = CsmInheritanceUtilities.mergeInheritedVisibility(minVisibility, inherit.getVisibility());
            } else {
                // create merged visibility based on direct inheritance
                mergedVisibility = CsmInheritanceUtilities.mergeChildInheritanceVisibility(minVisibility, inherit.getVisibility());
            }
            Map baseRes = getClassMembers(inherit.getCsmClass(), mergedVisibility, kinds, strPrefix, staticOnly, match, handledClasses, ++level);
            if (baseRes != null && baseRes.size() > 0) {
                baseRes.putAll(res);
                res = baseRes;
            }
        }
	return res;		
    }
        
    protected List/*<CsmDeclaration>*/ getNamespaceMembers(CsmNamespace ns, CsmDeclaration.Kind kind, String strPrefix, boolean match) {
	return getNamespaceMembers(ns, new CsmDeclaration.Kind[] {kind}, strPrefix, match);
    }
    
    protected List/*<CsmDeclaration>*/ getNamespaceMembers(CsmNamespace ns, CsmDeclaration.Kind kinds[], String strPrefix, boolean match) {
	List res = getNamespaceMembers(ns, kinds, strPrefix, match, new HashSet());
        return res;
    }

    protected List/*<CsmDeclaration>*/ getNamespaceMembers(CsmNamespace ns, CsmDeclaration.Kind kinds[], String strPrefix, boolean match, Set handledNS) {
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
        
    protected boolean matchName(String name, String strPrefix) {
	return CsmSortUtilities.matchName(name, strPrefix, false, caseSensitive); 
    }
    
    protected boolean matchName(String name, String strPrefix, boolean match) {
	return CsmSortUtilities.matchName(name, strPrefix, match, caseSensitive); 
    }

    protected boolean matchName(String name, String strPrefix, boolean match, boolean caseSensitive) {
	return CsmSortUtilities.matchName(name, strPrefix, match, caseSensitive); 
    }
    
    public boolean matchVisibility(CsmMember member, CsmVisibility minVisibility) {
        return CsmInheritanceUtilities.matchVisibility(member, minVisibility);
    }    
    
    private List merge(List orig, List newList) {
        return CsmUtilities.merge(orig, newList);
    }
    
}

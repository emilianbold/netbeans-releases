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
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author vv159170
 */
public class CsmContextUtilities {
    private static final boolean DEBUG = Boolean.getBoolean("csm.utilities.trace");

    /** Creates a new instance of CsmScopeUtilities */
    private CsmContextUtilities() {
    }

    public static List/*<CsmDeclaration*/ findGlobalVariables(CsmProject prj) {
        CsmProjectContentResolver resolver = new CsmProjectContentResolver(prj);
        return resolver.getGlobalVariables("", false, false);
    }
    
    public static List/*<CsmDeclaration*/ findLocalDeclarations(CsmContext context, String strPrefix, boolean match, boolean caseSensitive) {
        return findLocalDeclarations(context, strPrefix, match, caseSensitive, true/*include file locals*/, false/*include function locals*/);
    }

    public static List/*<CsmDeclaration*/ findFileLocalVariables(CsmContext context) {
        return findFileLocalVariables(context, "", false, false);
    }

    public static List/*<CsmDeclaration*/ findFileLocalVariables(CsmContext context, String strPrefix, boolean match, boolean caseSensitive) {
        return findLocalDeclarations(context, strPrefix, match, caseSensitive, true/*include file locals*/, false/*exclude function locals*/);
    }
    
    public static List/*<CsmDeclaration*/ findFunctionLocalVariables(CsmContext context) {
        return findFunctionLocalVariables(context, "", false, false);
    }

    public static List/*<CsmDeclaration*/ findFunctionLocalVariables(CsmContext context, String strPrefix, boolean match, boolean caseSensitive) {
        return findLocalDeclarations(context, strPrefix, match, caseSensitive, false/*do not include file locals*/, true/*include function locals*/);
    }
    
    protected static List/*<CsmDeclaration*/ findLocalDeclarations(CsmContext context, String strPrefix, boolean match, boolean caseSensitive, boolean includeFileLocal, boolean includeFunctionVars) {
        List res = new ArrayList();
        boolean incAny = includeFileLocal || includeFunctionVars;
        assert (incAny) : "at least one must be true";
        boolean incAll = includeFileLocal && includeFunctionVars;
        for (Iterator it = context.iterator(); it.hasNext() && incAny;) {
            CsmContext.CsmContextEntry entry = (CsmContext.CsmContextEntry) it.next();
            boolean include = incAll;
            if (!include) {
                // check if something changed
                if (!includeFileLocal) {
                    assert (includeFunctionVars);
                    // if it wasn't necessary to include all file local variables, but now 
                    // we jump in function definition => mark that from now include all
                    if (CsmKindUtilities.isFunctionDefinition(entry.getScope())) {
                        incAll = include = true;
                    }
                } else if (!includeFunctionVars) {
                    assert (includeFileLocal);
                    // we have sorted context entries => if we reached function =>
                    // skip function and all others
                    if (CsmKindUtilities.isFunctionDefinition(entry.getScope())) {
                        incAll = incAny = include = false;
                    } else {
                        include = true;
                    }
                }
            }
            if (include) {
                res = addEntryDeclarations(entry, res, context, strPrefix, match, caseSensitive);
            }
        }
        return res;
    }

    private static List/*<CsmDeclaration>*/ addEntryDeclarations(CsmContext.CsmContextEntry entry, List/*<CsmDeclaration>*/ decls, CsmContext fullContext,
                                                                String strPrefix, boolean match, boolean caseSensitive) {
        List/*<CsmDeclaration>*/ newList = findEntryDeclarations(entry, fullContext, strPrefix, match, caseSensitive);
        return mergeDeclarations(decls, newList);
    }
    
    private static List/*<CsmDeclaration>*/ findEntryDeclarations(CsmContext.CsmContextEntry entry, CsmContext fullContext,
                                                                String strPrefix, boolean match, boolean caseSensitive) {
        assert (entry != null) : "can't work on null entries";
        CsmScope scope = entry.getScope();
        int offsetInScope = entry.getOffset();
        List resList = new ArrayList();
        for (Iterator it = scope.getScopeElements().iterator(); it.hasNext();) {
            CsmScopeElement scpElem = (CsmScopeElement) it.next();
            if (canBreak(offsetInScope, scpElem, fullContext)) {
                break;
            }
            List/*<CsmDeclaration>*/ declList = extractDeclarations(scpElem, strPrefix, match, caseSensitive);
            resList.addAll(declList);
        }
        return resList;
    }
    
    private static boolean canBreak(int offsetInScope, CsmScopeElement elem, CsmContext fullContext) {
        // break if element already is in context
        // or element is after offset
        if (offsetInScope == CsmContext.CsmContextEntry.WHOLE_SCOPE) {
            return isInContext(fullContext, elem);
        } else if (CsmKindUtilities.isOffsetable(elem)) {
            return ((CsmOffsetable)elem).getStartOffset() > offsetInScope || isInContext(fullContext, elem);
        }
        return isInContext(fullContext, elem);
    }
    
    private static List/*<CsmDeclaration>*/ mergeDeclarations(List/*<CsmDeclaration>*/ decls, List/*<CsmDeclaration>*/ newList) {
        // XXX: now just add all
        if (newList != null && newList.size() > 0) {
            decls.addAll(newList);
        }
        return decls;
    }

//    public static void updateContextObject(CsmObject obj, CsmContext context) {
//        if (context != null && obj != null) {
//            context.setLastObject(obj);
//        }
//    }
    
    public static void updateContextObject(CsmObject obj, int offset, CsmContext context) {
        if (context != null && obj != null) {
            context.setLastObject(obj);
        }
    }
    
//    public static void updateContext(CsmObject obj, CsmContext context) {
//        if (context != null && CsmKindUtilities.isScope(obj)) {
//            context.add((CsmScope)obj);
//        } else if (CsmKindUtilities.isOffsetable(obj)) {
//            updateContextObject(obj, context);
//        }
//    }
    
    public static void updateContext(CsmObject obj, int offset, CsmContext context) {
        if (context != null) {
            if (CsmKindUtilities.isScope(obj)) {
                context.add((CsmScope)obj, offset);
            } else if (CsmKindUtilities.isOffsetable(obj)) {
                updateContextObject(obj, offset, context);
            }
        }
    }
    
    private static boolean isInContext(CsmContext context, CsmObject obj) {
        // XXX: in fact better to start from end
        for (Iterator it = context.iterator(); it.hasNext();) {
            CsmContext.CsmContextEntry elem = (CsmContext.CsmContextEntry) it.next();
            if (obj == elem.getScope()) {
                return true;
            }
        }
        return false;
    }

    private static List/*<CsmDeclaration>*/ extractDeclarations(CsmScopeElement scpElem,
                                                        String strPrefix, boolean match, boolean caseSensitive) {
        List list = new ArrayList();
        if (CsmKindUtilities.isDeclaration(scpElem)) {
            if (CsmSortUtilities.matchName(((CsmDeclaration)scpElem).getName(), strPrefix, match, caseSensitive)) {
                boolean add = true;
                // special check for "var args" parameters
                if (CsmKindUtilities.isParamVariable(scpElem)) {
                    add = !((CsmParameter)scpElem).isVarArgs();
                }
                if (add) {
                    list.add(scpElem);
                }
            }
        } else if (CsmKindUtilities.isStatement(scpElem)) {
            CsmStatement.Kind kind = ((CsmStatement)scpElem).getKind();
            if (kind == CsmStatement.Kind.DECLARATION) {
                List/*<CsmObject>*/ listByName = CsmSortUtilities.filterList(((CsmDeclarationStatement)scpElem).getDeclarators(), strPrefix, match, caseSensitive);
                list.addAll(listByName);
            }
        }
        return list;
    }

    public static CsmClass getClass(CsmContext context) {
        CsmClass clazz = null;
        for (Iterator it = context.iterator(); it.hasNext();) {
            CsmContext.CsmContextEntry elem = (CsmContext.CsmContextEntry) it.next();
            if (CsmKindUtilities.isClass(elem.getScope())) {
                clazz = (CsmClass)elem.getScope();
                break;
            }
        }        
        if (clazz == null) {
            // check if we in one of class's method
            CsmFunctionDefinition funDef = getFunctionDefinition(context);
            clazz = funDef == null ? null : CsmBaseUtilities.getFunctionClass(funDef);
        }
        return clazz;
    }    
    
    public static CsmFunction getFunction(CsmContext context) {
        CsmFunction fun = null;
        for (Iterator it = context.iterator(); it.hasNext();) {
            CsmContext.CsmContextEntry elem = (CsmContext.CsmContextEntry) it.next();
            if (CsmKindUtilities.isFunction(elem.getScope())) {
                fun = (CsmFunction)elem.getScope();
                break;
            }
        }        
        return fun;
    }    
    
    public static CsmFunctionDefinition getFunctionDefinition(CsmContext context) {
        CsmFunctionDefinition fun = null;
        for (Iterator it = context.iterator(); it.hasNext();) {
            CsmContext.CsmContextEntry elem = (CsmContext.CsmContextEntry) it.next();
            if (CsmKindUtilities.isFunctionDefinition(elem.getScope())) {
                fun = (CsmFunctionDefinition)elem.getScope();
                break;
            }
        }        
        return fun;
    }   
    
    public static boolean isInFunctionBody(CsmContext context, int offset) {
        CsmFunctionDefinition funDef = getFunctionDefinition(context);
        return (funDef == null) ? false : CsmOffsetUtilities.isInObject(funDef.getBody(), offset);
    }   
}

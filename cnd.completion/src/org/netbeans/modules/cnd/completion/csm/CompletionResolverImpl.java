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
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;


/**
 *
 * @author Vladimir Voskresensky
 */
public class CompletionResolverImpl implements CompletionResolver {
    
    private static final boolean DEBUG_SUMMARY = Boolean.getBoolean("csm.utilities.trace.summary");
    private static final boolean TRACE = Boolean.getBoolean("csm.utilities.trace");
    private static final boolean DEBUG = TRACE | DEBUG_SUMMARY;

    // flags indicating what we plan to resolve using this resolver
    private static final int RESOLVE_NONE                   = 0;
    
    private static final int RESOLVE_CONTEXT                = 1 << 0;

    private static final int RESOLVE_CLASSES                = 1 << 1;
    
    private static final int RESOLVE_GLOB_VARIABLES         = 1 << 2;

    private static final int RESOLVE_GLOB_FUNCTIONS         = 1 << 3;

    private static final int RESOLVE_CLASS_FIELDS           = 1 << 4;

    private static final int RESOLVE_CLASS_METHODS          = 1 << 5;
    
    private static final int RESOLVE_LOCAL_VARIABLES        = 1 << 6;

    private static final int RESOLVE_FILE_LOCAL_VARIABLES   = 1 << 7;

    private static final int RESOLVE_LIB_CLASSES            = 1 << 8;
    
    private static final int RESOLVE_LIB_VARIABLES          = 1 << 9;

    private static final int RESOLVE_LIB_FUNCTIONS          = 1 << 10;
    
    private static final int RESOLVE_LIB_ENUMERATORS       = 1 << 11;
    
    private static final int RESOLVE_GLOB_ENUMERATORS       = 1 << 12;
    
//    private static final int RESOLVE_CLASS_ENUMERATORS       = 1 << 13;
    
    private int resolveTypes = RESOLVE_NONE;
    
    private CsmFile file;
    private CsmContext context;
    
    List/*<CsmObject>*/ result = new ArrayList();
    CsmProjectContentResolver contResolver = null;
    
    private boolean caseSensitive = false;
    private boolean naturalSort = false;

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public boolean isNaturalSort() {
        return naturalSort;
    }
    
    /** Creates a new instance of CompletionResolver */
    public CompletionResolverImpl(CsmFile file) {
        this(file, false, true);
    }

    public CompletionResolverImpl(CsmFile file, boolean caseSensitive, boolean naturalSort) {
        this(file, RESOLVE_CONTEXT, caseSensitive, naturalSort);
    }
    
    public CompletionResolverImpl(CsmFile file, int resolveTypes, boolean caseSensitive, boolean naturalSort) {
	this.file = file;
        this.resolveTypes = resolveTypes;
        this.caseSensitive = caseSensitive;
        this.naturalSort = naturalSort;
    }

    public boolean refresh() {
        result = new ArrayList();
        // update if file attached to invalid project
        if ((file != null) && (file.getProject() != null) && !file.getProject().isValid()) {
            file = CsmUtilities.getCsmFile(CsmUtilities.getFileObject(file));
        }        
        context = null;
        // should be called last, because uses setting set above
        this.contResolver = null;
        if (file == null) {
            return false;
        }
        this.contResolver = createContentResolver(file.getProject());
        return true;
    }
    
    public boolean update(boolean caseSensitive, boolean naturalSort) {
        this.caseSensitive = caseSensitive;
        this.naturalSort = naturalSort;
        return refresh();
    }
    
    public boolean resolve(int offset, String strPrefix, boolean match) {
	if (file == null) {
	    return false;
	}
        context  = CsmOffsetResolver.findContext(file, offset);
        if (DEBUG) System.out.println("context for offset " + offset + " :\n" + context);
        resolveContext(context, offset, strPrefix, match);
	return file != null;
    }
    
    public List/*<CsmObject>*/ getResult() {
	return result;
    }
    
    private void resolveContext(CsmContext context, int offset, String strPrefix, boolean match) {
        List classes        = null;
        List localVars      = null;
        List fileLocalVars  = null;
        List classFields    = null;
        List classMethods   = null;
        List globVars       = null;
        List globEnmtrs     = null;
        List globFuns       = null;
        List libClasses     = null;
        List libVars        = null;
        List libEnmtrs      = null;
        List libFuns        = null;
        
        CsmProject prj = file != null ? file.getProject() : null;
        if (prj == null) {
            return;
        }
        if (needClasses(context, offset)) {
            // list of classes
            classes = getClassesEnums(prj, strPrefix, match);
        }
        
        if (needLocalVars(context, offset)) {
            CsmFunctionDefinition funDef = CsmContextUtilities.getFunctionDefinition(context);
            if (funDef != null) {
                boolean staticContext = CsmBaseUtilities.isStaticContext(funDef);
                // get local variables from context
                
                // function variables
                if (needFunctionVars(context, offset)) {
                    localVars = contResolver.getFunctionVariables(context, strPrefix, match);
                }
                
                // file local variables
                if (needFileLocalVars(context, offset)) {
                    fileLocalVars = contResolver.getFileLocalVariables(context, strPrefix, match);
                }
                
                CsmClass clazz = CsmBaseUtilities.getFunctionClass(funDef);
                if (clazz != null) {
                    // get class variables visible in this method
                    classFields = contResolver.getFields(clazz, CsmInheritanceUtilities.MAX_VISIBILITY, strPrefix, staticContext, match);

                    // get class methods visible in this method
                    classMethods = contResolver.getMethods(clazz, CsmInheritanceUtilities.MAX_VISIBILITY, strPrefix, staticContext, match);
                }
            }
        }
        if (needGlobalVariables(context, offset)) {
            globVars = getGlobalVariables(prj, strPrefix, match);
        }
        if (needGlobalEnumerators(context, offset)) {
            globEnmtrs = getGlobalEnumerators(prj, strPrefix, match);
        }        
        if (needGlobalFunctions(context, offset)) {
            globFuns = getGlobalFunctions(prj, strPrefix, match);
        }
        if (needLibClasses(context, offset)) {
            libClasses = getLibClassesEnums(prj, strPrefix, match);
        }
        if (needLibVariables(context, offset)) {
            libVars = getLibVariables(prj, strPrefix, match);
        }
        if (needLibEnumerators(context, offset)) {
            libEnmtrs = getLibEnumerators(prj, strPrefix, match);
        }        
        if (needLibFunctions(context, offset)) {
            libFuns = getLibFunctions(prj, strPrefix, match);
        }
        result = buildResult(context, localVars, classFields, classMethods, 
                classes, fileLocalVars, globVars, globEnmtrs, globFuns,
                libClasses, libVars, libEnmtrs, libFuns);
    }
    
    private static List buildResult(CsmContext context, 
            List localVars, List classFields, List classMethods, 
            List classes, List fileLocalVars, 
            List globVars, List globEnmtrs, List globFuns,
            List libClasses, List libVars, List libEnmtrs, List libFuns) {
        List result = new ArrayList();
        // add local vars
        if (DEBUG) { trace(localVars, "Local variables");}
        result = merge(result, localVars);
        // add class fields
        if (DEBUG) { trace(classFields, "Class fields");}
        result = merge(result, classFields);
        // add class methods
        if (DEBUG) { trace(classMethods, "Class methods");}
        result = merge(result, classMethods);
        // add classes 
        if (DEBUG) { trace(classes, "Classes");}
        result = merge(result, classes);
        // add file local variables 
        if (DEBUG) { trace(fileLocalVars, "File Local Variables");}
        result = merge(result, fileLocalVars);
        // add global variables 
        if (DEBUG) { trace(globVars, "Global variables");}
        result = merge(result, globVars);
        // add global enumerators 
        if (DEBUG) { trace(globEnmtrs, "Global enumerators");}
        result = merge(result, globEnmtrs);        
        // add global functions 
        if (DEBUG) { trace(globFuns, "Global functions");}
        result = merge(result, globFuns);
        // add libraries classes
        if (DEBUG) { trace(libClasses, "Library classes");}
        result = merge(result, libClasses);
        // add libraries variables 
        if (DEBUG) { trace(libVars, "Library variables");}
        result = merge(result, libVars);
        // add libraries enumerators 
        if (DEBUG) { trace(libEnmtrs, "Library enumerators");}
        result = merge(result, libEnmtrs);        
        // add libraries functions 
        if (DEBUG) { trace(libFuns, "Library functions");}
        result = merge(result, libFuns);
        if (DEBUG) { trace(result, "Final result");}
        return result;
    }
    
    protected CsmProjectContentResolver createContentResolver(CsmProject prj) {
        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(prj, isCaseSensitive(), isNaturalSort());
        return contResolver;
    }

    protected CsmProjectContentResolver createLibraryResolver(CsmProject lib) {
        CsmProjectContentResolver libResolver = new CsmProjectContentResolver(lib, isCaseSensitive(), isNaturalSort());
        return libResolver;
    }

    private static List merge(List orig, List newList) {
        return CsmUtilities.merge(orig, newList);
    }

    private List getClassesEnums(CsmProject prj, String strPrefix, boolean match) {
        if (prj == null) {
            return null;
        }
        CsmNamespace globNS = prj.getGlobalNamespace();
        List res = contResolver.getNamespaceClassesEnums(globNS, strPrefix, match, true);
        return res;
    }
    
    private List getGlobalVariables(CsmProject prj, String strPrefix, boolean match) {
        List res = contResolver.getGlobalVariables(strPrefix, match, true);
        return res;
    }    
    
    private List getGlobalEnumerators(CsmProject prj, String strPrefix, boolean match) {
        if (prj == null) {
            return null;
        }
        CsmNamespace globNS = prj.getGlobalNamespace();
        List res = contResolver.getNamespaceEnumerators(globNS, strPrefix, match, true);
        return res;
    }    
    
    private List getGlobalFunctions(CsmProject prj, String strPrefix, boolean match) {
        List res = contResolver.getGlobalFunctions(strPrefix, match, true);
        return res;
    }      

    private List getLibClassesEnums(CsmProject prj, String strPrefix, boolean match) {
        List res = contResolver.getLibClassesEnums(strPrefix, match, true);
        return res;
    }

    private List getLibVariables(CsmProject prj, String strPrefix, boolean match) {
        List res = contResolver.getLibVariables(strPrefix, match, true);
        return res;
    }

    private List getLibEnumerators(CsmProject prj, String strPrefix, boolean match) {
        List res = contResolver.getLibEnumerators(strPrefix, match, true);
        return res;
    }
    
    private List getLibFunctions(CsmProject prj, String strPrefix, boolean match) {
        List res = contResolver.getLibFunctions(strPrefix, match, true);
        return res;
    }
    
    private boolean needClasses(CsmContext context, int offset) {
        if ((resolveTypes & RESOLVE_CLASSES) == RESOLVE_CLASSES) {
            return true;
        } 
        if ((resolveTypes & RESOLVE_CONTEXT) == RESOLVE_CONTEXT) {
            assert (context != null);
            if (CsmContextUtilities.isInFunctionBody(context, offset)) {
                // for speed up remember result
                resolveTypes |= RESOLVE_CLASSES;
                
                // some other info to remember in this context
                resolveTypes |= RESOLVE_LOCAL_VARIABLES;
                resolveTypes |= RESOLVE_GLOB_VARIABLES;
                resolveTypes |= RESOLVE_GLOB_ENUMERATORS;
                resolveTypes |= RESOLVE_GLOB_FUNCTIONS;
                resolveTypes |= RESOLVE_LIB_CLASSES;
                resolveTypes |= RESOLVE_LIB_VARIABLES;
                resolveTypes |= RESOLVE_LIB_ENUMERATORS;
                resolveTypes |= RESOLVE_LIB_FUNCTIONS;
                return true;
            } else if (CsmContextUtilities.getClass(context) != null) {
                // for speed up remember result
                resolveTypes |= RESOLVE_CLASSES;

                return true;
            } else {
                // resolve classes always 

                // for speed up remember result
                resolveTypes |= RESOLVE_CLASSES;

                return true;                
            }
        }
        return false;
    }
    
    private boolean needFileLocalVars(CsmContext context, int offset) {
        if ((resolveTypes & RESOLVE_FILE_LOCAL_VARIABLES) == RESOLVE_LOCAL_VARIABLES) {
            return true;
        }         
        if ((resolveTypes & RESOLVE_CONTEXT) == RESOLVE_CONTEXT) {
            assert (context != null);
            if (CsmContextUtilities.isInFunctionBody(context, offset)) {
                // for speed up remember some results
                resolveTypes |= RESOLVE_FILE_LOCAL_VARIABLES;
                
                // some other info to remember in this context
                resolveTypes |= RESOLVE_LOCAL_VARIABLES;
                resolveTypes |= RESOLVE_GLOB_VARIABLES;
                resolveTypes |= RESOLVE_GLOB_ENUMERATORS;
                resolveTypes |= RESOLVE_GLOB_FUNCTIONS;
                resolveTypes |= RESOLVE_LIB_CLASSES;
                resolveTypes |= RESOLVE_LIB_VARIABLES;
                resolveTypes |= RESOLVE_LIB_ENUMERATORS;
                resolveTypes |= RESOLVE_LIB_FUNCTIONS;
                return true;                
            }
        }
        return false;
    }
    
    private boolean needLocalVars(CsmContext context, int offset) {
        if ((resolveTypes & RESOLVE_LOCAL_VARIABLES) == RESOLVE_LOCAL_VARIABLES) {
            return true;
        }         
        if ((resolveTypes & RESOLVE_CONTEXT) == RESOLVE_CONTEXT) {
            assert (context != null);
            if (CsmContextUtilities.isInFunctionBody(context, offset)) {
                // for speed up remember some results
                resolveTypes |= RESOLVE_LOCAL_VARIABLES;
                
                // some other info to remember in this context
                resolveTypes |= RESOLVE_FILE_LOCAL_VARIABLES;
                resolveTypes |= RESOLVE_GLOB_VARIABLES;
                resolveTypes |= RESOLVE_GLOB_ENUMERATORS;
                resolveTypes |= RESOLVE_GLOB_FUNCTIONS;
                resolveTypes |= RESOLVE_LIB_CLASSES;
                resolveTypes |= RESOLVE_LIB_VARIABLES;
                resolveTypes |= RESOLVE_LIB_ENUMERATORS;
                resolveTypes |= RESOLVE_LIB_FUNCTIONS;
                return true;                
            }
        }
        return false;
    }
    
    private boolean needGlobalVariables(CsmContext context, int offset) {
        if ((resolveTypes & RESOLVE_GLOB_VARIABLES) == RESOLVE_GLOB_VARIABLES) {
            return true;
        } 
        return false;
    }

    private boolean needGlobalEnumerators(CsmContext context, int offset) {
        if ((resolveTypes & RESOLVE_GLOB_ENUMERATORS) == RESOLVE_GLOB_ENUMERATORS) {
            return true;
        } 
        return false;
    }
    
    private boolean needGlobalFunctions(CsmContext context, int offset) {
        if ((resolveTypes & RESOLVE_GLOB_FUNCTIONS) == RESOLVE_GLOB_FUNCTIONS) {
            return true;
        } 
        return false;
    }
    
    private boolean needFunctionVars(CsmContext context, int offset) {
        return needLocalVars(context, offset);
    }

    private boolean needLibClasses(CsmContext context, int offset) {
        if ((resolveTypes & RESOLVE_LIB_CLASSES) == RESOLVE_LIB_CLASSES) {
            return true;
        } 
        return false;
    }

    private boolean needLibVariables(CsmContext context, int offset) {
        if ((resolveTypes & RESOLVE_LIB_VARIABLES) == RESOLVE_LIB_VARIABLES) {
            return true;
        } 
        return false;
    }

    private boolean needLibEnumerators(CsmContext context, int offset) {
        if ((resolveTypes & RESOLVE_LIB_ENUMERATORS) == RESOLVE_LIB_ENUMERATORS) {
            return true;
        }
        return false;
    }
        
    private boolean needLibFunctions(CsmContext context, int offset) {
        if ((resolveTypes & RESOLVE_LIB_FUNCTIONS) == RESOLVE_LIB_FUNCTIONS) {
            return true;
        } 
        return false;
    }
    
    // ====================== Debug support ===================================
    
    private static void trace(List/*<CsmObject*/ list, String msg) {
        if (list == null) {
            return;
        }
        System.out.println(msg + " [size - " + list.size() +"]:");
        if (TRACE) {
            for (int i = 0; i < list.size(); i++) {
                CsmObject elem = (CsmObject) list.get(i);
                System.out.println("["+i+"]"+CsmUtilities.getCsmName(elem));
            }        
        }
    }    
}

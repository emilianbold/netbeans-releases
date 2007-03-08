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
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;


/**
 *
 * @author Vladimir Voskresensky
 */
public class CompletionResolverImpl implements CompletionResolver {
    
    private static final boolean DEBUG_SUMMARY = Boolean.getBoolean("csm.utilities.trace.summary");
    private static final boolean TRACE = Boolean.getBoolean("csm.utilities.trace");
    private static final boolean DEBUG = TRACE | DEBUG_SUMMARY;
    
    //    public static final int RESOLVE_CLASS_ENUMERATORS       = 1 << 13;
    
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
    
    public void setResolveTypes(int resolveTypes) {
        this.resolveTypes = resolveTypes;
    }
    
    public boolean refresh() {
        result = new ArrayList();
        // update if file attached to invalid project
        if ((file != null) && (file.getProject() != null) && !file.getProject().isValid()) {
            file = CsmUtilities.getCsmFile(CsmUtilities.getFileObject(file), true);
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
        if (DEBUG) System.out.println("context for offset " + offset + " :\n" + context); //NOI18N
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
        List fileLocalEnumerators = null;
        List fileLocalMacros = null;
        List fileProjectMacros = null;
        List fileLibMacros  = null;
        List globProjectMacros = null;
        List globLibMacros  = null;
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
        //long timeStart = System.nanoTime();
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
                    classFields = contResolver.getFields(clazz, funDef, strPrefix, staticContext, match, true);
                    
                    // get class methods visible in this method
                    classMethods = contResolver.getMethods(clazz, funDef, strPrefix, staticContext, match, true);
                }
            }
            fileLocalEnumerators = contResolver.getFileLocalEnumerators(context, strPrefix, match);
        } else if (needClassMethods(context, offset)) {
            CsmFunctionDefinition funDef = CsmContextUtilities.getFunctionDefinition(context);
            if (funDef == null || !CsmContextUtilities.isInFunctionBody(context, offset)) {
                funDef = null;
            }            
            CsmClass clazz = funDef == null ? null : CsmBaseUtilities.getFunctionClass(funDef);
            clazz = clazz != null ? clazz : CsmContextUtilities.getClass(context, false);
            if (clazz != null) {
                boolean staticContext = false;
                // get class methods visible in this method
                classMethods = contResolver.getMethods(clazz, funDef, strPrefix, staticContext, match, true);
            }            
        }
        if (needFileMacros(context, offset)) {
            fileLocalMacros = contResolver.getFileMacros(context, strPrefix, match);
        }
        //long timeGlobMacroStart = System.nanoTime();
        if (needGlobalMacros(context, offset)) {
            globProjectMacros = contResolver.getProjectMacros(context, strPrefix, match);
            globLibMacros = contResolver.getLibMacros(context, strPrefix, match);
        } else if (needFileMacros(context, offset)){
            fileProjectMacros = contResolver.getFileProjectMacros(context, strPrefix, match);
            fileLibMacros = contResolver.getFileLibMacros(context, strPrefix, match);
        }
        //long timeGlobMacroEnd = System.nanoTime();
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
                classes, fileLocalVars, fileLocalEnumerators,
                fileLocalMacros, fileProjectMacros, fileLibMacros, globProjectMacros, globLibMacros,
                globVars, globEnmtrs, globFuns,
                libClasses, libVars, libEnmtrs, libFuns);
        //long timeEnd = System.nanoTime();
        //System.out.println("get gesolve list time "+(timeEnd -timeStart)+" objects "+result.size()); //NOI18N
        //System.out.println("get global macro time "+(timeGlobMacroEnd -timeGlobMacroStart)+" objects "+ //NOI18N
        //        (globProjectMacros.size()+globLibMacros.size()));
    }
    
    private static List buildResult(CsmContext context,
            List localVars, List classFields, List classMethods,
            List classes, List fileLocalVars, List fileLocalEnumerators,
            List fileLocalMacros, List fileProjectMacros, List fileLibMacros, List globProjectMacros, List globLibMacros,
            List globVars, List globEnmtrs, List globFuns,
            List libClasses, List libVars, List libEnmtrs, List libFuns) {
        List result = new ArrayList();
        // add local vars
        if (DEBUG) { trace(localVars, "Local variables");} //NOI18N
        result = merge(result, localVars);
        // add class fields
        if (DEBUG) { trace(classFields, "Class fields");} //NOI18N
        result = merge(result, classFields);
        // add class methods
        if (DEBUG) { trace(classMethods, "Class methods");} //NOI18N
        result = merge(result, classMethods);
        // add classes
        if (DEBUG) { trace(classes, "Classes");} //NOI18N
        result = merge(result, classes);
        // add file local variables
        if (DEBUG) { trace(fileLocalVars, "File Local Variables");} //NOI18N
        result = merge(result, fileLocalVars);
        if (DEBUG) { trace(fileLocalEnumerators, "File Local Enumerators");} //NOI18N
        result = merge(result, fileLocalEnumerators);
        if (DEBUG) { trace(fileLocalEnumerators, "File Local Macros");} //NOI18N
        result = merge(result, fileLocalMacros);
        // add global variables
        if (DEBUG) { trace(globVars, "Global variables");} //NOI18N
        result = merge(result, globVars);
        // add global enumerators
        if (DEBUG) { trace(globEnmtrs, "Global enumerators");} //NOI18N
        result = merge(result, globEnmtrs);
        // add global functions
        if (DEBUG) { trace(globFuns, "Global functions");} //NOI18N
        result = merge(result, globFuns);
        if (DEBUG) { trace(fileProjectMacros, "File Project Macros");} //NOI18N
        result = merge(result, fileProjectMacros);
        // add libraries classes
        if (DEBUG) { trace(libClasses, "Library classes");} //NOI18N
        result = merge(result, libClasses);
        // add libraries variables
        if (DEBUG) { trace(libVars, "Library variables");} //NOI18N
        result = merge(result, libVars);
        // add libraries enumerators
        if (DEBUG) { trace(libEnmtrs, "Library enumerators");} //NOI18N
        result = merge(result, libEnmtrs);
        // add libraries functions
        if (DEBUG) { trace(libFuns, "Library functions");} //NOI18N
        result = merge(result, libFuns);
        if (DEBUG) { trace(fileLibMacros, "File Library Macros");} //NOI18N
        result = merge(result, fileLibMacros);
        if (DEBUG) { trace(globProjectMacros, "Project Macros");} //NOI18N
        result = merge(result, globProjectMacros);
        if (DEBUG) { trace(globLibMacros, "Library Macros");} //NOI18N
        result = merge(result, globLibMacros);
        if (DEBUG) { trace(result, "Final result");} //NOI18N
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
            resolveTypes |= RESOLVE_FILE_MACROS;
            resolveTypes |= RESOLVE_GLOB_MACROS;
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
            } else if (CsmContextUtilities.getClass(context, true) != null) {
                // for speed up remember result
                resolveTypes |= RESOLVE_CLASSES;
                
                return true;
            } else {
                // resolve classes always
                
                // for speed up remember result
                resolveTypes |= RESOLVE_CLASSES;
                
                // resolve global context as well
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
    
    private boolean needFileMacros(CsmContext context, int offset) {
        if ((resolveTypes & RESOLVE_FILE_MACROS) == RESOLVE_FILE_MACROS) {
            return true;
        }
        return false;
    }
    
    private boolean needGlobalMacros(CsmContext context, int offset) {
        if ((resolveTypes & RESOLVE_GLOB_MACROS) == RESOLVE_GLOB_MACROS) {
            return true;
        }
        return false;
    }
    

    private boolean needClassMethods(CsmContext context, int offset) {
        if ((resolveTypes & RESOLVE_CLASS_METHODS) == RESOLVE_CLASS_METHODS) {
            return true;
        }
        return false;
    }
    
    // ====================== Debug support ===================================
    
    private static void trace(List/*<CsmObject*/ list, String msg) {
        if (list == null) {
            return;
        }
        System.out.println(msg + " [size - " + list.size() +"]:"); //NOI18N
        if (TRACE) {
            for (int i = 0; i < list.size(); i++) {
                CsmObject elem = (CsmObject) list.get(i);
                System.out.println("["+i+"]"+CsmUtilities.getCsmName(elem)); //NOI18N
            }
        }
    }

    
}

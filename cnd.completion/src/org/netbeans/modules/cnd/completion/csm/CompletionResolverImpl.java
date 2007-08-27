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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.services.CsmUsingResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.completion.csm.CompletionResolver.Result;
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
    private int hideTypes = ~RESOLVE_NONE;
    
    private CsmFile file;
    private CsmContext context;
    
    Result result = EMPTY_RESULT;
    CsmProjectContentResolver contResolver = null;
    
    private boolean caseSensitive = false;
    private boolean naturalSort = false;
    private boolean sort = false;


    public boolean isSortNeeded() {
        return sort;
    }

    public void setSortNeeded(boolean sort) {
        this.sort = sort;
    }
    
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    public boolean isNaturalSort() {
        return naturalSort;
    }
    
    /** Creates a new instance of CompletionResolver */
    public CompletionResolverImpl(CsmFile file) {
        this(file, false, false, false);
    }
    
    public CompletionResolverImpl(CsmFile file, boolean caseSensitive, boolean sort, boolean naturalSort) {
        this(file, RESOLVE_CONTEXT, caseSensitive, sort, naturalSort);
    }
    
    public CompletionResolverImpl(CsmFile file, int resolveTypes, boolean caseSensitive, boolean sort, boolean naturalSort) {
        this.file = file;
        this.resolveTypes = resolveTypes;
        this.caseSensitive = caseSensitive;
        this.naturalSort = naturalSort;
        this.sort = sort;
    }
    
    public void setResolveTypes(int resolveTypes) {
        this.resolveTypes = resolveTypes;
    }
    
    public boolean refresh() {
        result = EMPTY_RESULT;
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
        initMacroResolving(context, strPrefix);
        this.hideTypes = initHideMask(context, this.resolveTypes, strPrefix);
        resolveContext(context, offset, strPrefix, match);
        return file != null;
    }
    
    public Result getResult() {
        return this.result;
    }
    
    public static final boolean STAT_COMPLETION = Boolean.getBoolean("cnd.completion.stat");
    public static final boolean TIMING_COMPLETION = Boolean.getBoolean("cnd.completion.timing") || STAT_COMPLETION;
    
    private void resolveContext(CsmContext context, int offset, String strPrefix, boolean match) {
        long time = 0;
        if (TIMING_COMPLETION) {
            time = System.currentTimeMillis();
            System.err.println("Started resolving context");
        }
        Collection localVars      = null;
        
        Collection classFields    = null;
        Collection classEnumerators = null;
        Collection classMethods   = null;
        Collection classesEnumsTypedefs = null;
        
        Collection fileLocalVars  = null;
        Collection fileLocalEnumerators = null;
        Collection fileLocalMacros = null;

        Collection fileProjectMacros = null;

        Collection globVars       = null;
        Collection globEnumerators = null;
        Collection globProjectMacros = null;
        
        Collection globFuns       = null;
        Collection globProjectNSs = null; // NOT YET IMPL
        
        Collection libClasses     = null;
        Collection fileLibMacros  = null;
        Collection globLibMacros  = null;
        Collection libVars        = null;
        Collection libEnumerators = null;
        Collection libFuns        = null;
        Collection libNSs         = null; // NOT YET IMPL
        
        CsmProject prj = file != null ? file.getProject() : null;
        if (prj == null) {
            return;
        }
                
        //long timeStart = System.nanoTime();
        if (needClasses(context, offset)) {
            // list of classesEnumsTypedefs
            classesEnumsTypedefs = getClassesEnums(prj, strPrefix, match, offset);
        }
        
        if (needLocalVars(context, offset)) {
            fileLocalEnumerators = contResolver.getFileLocalEnumerators(context, strPrefix, match);            
            CsmFunction fun = CsmContextUtilities.getFunction(context);
            boolean staticContext = fun == null ? true : CsmBaseUtilities.isStaticContext(fun);
            // get local variables from context

            // function variables
            if (needFunctionVars(context, offset)) {
                List<CsmDeclaration> decls = contResolver.findFunctionLocalDeclarations(context, strPrefix, match);
                // separate local classes/structs/enums/unions and variables
                localVars = new ArrayList<CsmDeclaration>(decls.size());
                for (CsmDeclaration elem : decls) {
                    if (CsmKindUtilities.isVariable(elem)) {
                        localVars.add(elem);
                    } if (needClasses(context, offset) && CsmKindUtilities.isClassifier(elem)) {
                        if (classesEnumsTypedefs == null) {
                            classesEnumsTypedefs = new ArrayList<CsmDeclaration>();
                        }
                        classesEnumsTypedefs.add(elem);
                    } if (CsmKindUtilities.isEnumerator(elem)) {
                        if (fileLocalEnumerators == null) {
                            fileLocalEnumerators = new ArrayList<CsmDeclaration>();
                        }
                        fileLocalEnumerators.add(elem);
                    }
                }                   
            }

            // file local variables
            if (needFileLocalVars(context, offset)) {
                fileLocalVars = contResolver.getFileLocalVariables(context, strPrefix, match);
            }

            if (needClassElements(context, offset)) {
                CsmClass clazz = CsmBaseUtilities.getFunctionClass(fun);
                if (clazz != null) {
                    // get class variables visible in this method
                    classFields = contResolver.getFields(clazz, fun, strPrefix, staticContext, match, true,false);

                    // get class enumerators visible in this method
                    classEnumerators = contResolver.getEnumerators(clazz, fun, strPrefix, match, true,false);

                    // get class methods visible in this method
                    classMethods = contResolver.getMethods(clazz, fun, strPrefix, staticContext, match, true,false);
                }
            }
        } else if (needClassElements(context, offset)) {
            CsmFunction fun = CsmContextUtilities.getFunction(context);
            CsmClass clazz = fun == null ? null : CsmBaseUtilities.getFunctionClass(fun);
            clazz = clazz != null ? clazz : CsmContextUtilities.getClass(context, false);
            if (clazz != null) {
                boolean staticContext = false;
                // get class methods visible in this method
                CsmOffsetableDeclaration contextDeclaration = fun != null ? fun : CsmContextUtilities.getClass(context, false);
                if (needClassMethods(context, offset)) {
                    if (clazz != null) {
                        classMethods = contResolver.getMethods(clazz, contextDeclaration, strPrefix, staticContext, match, true,false);
                    }
                }
                if (needClassFields(context, offset)) {
                    // get class variables visible in this context
                    classFields = contResolver.getFields(clazz, contextDeclaration, strPrefix, staticContext, match, true,false);
                }
                if (needClassEnumerators(context, offset)) {
                    // get class enumerators visible in this context
                    classEnumerators = contResolver.getEnumerators(clazz, contextDeclaration, strPrefix, match, true,false);
                }
                if (needNestedClassifiers(context, offset)) {
                    // get class nested classifiers visible in this context
                    List<CsmClassifier> innerCls = contResolver.getNestedClassifiers(clazz, contextDeclaration, strPrefix, match, true);
                    classesEnumsTypedefs.addAll(innerCls);
                }
            }
        }
        if (needFileLocalMacros(context, offset)) {
            fileLocalMacros = contResolver.getFileLocalMacros(context, strPrefix, match);
        }
        if (needFileIncludedMacros(context, offset)) {
            fileProjectMacros = contResolver.getFileIncludedProjectMacros(context, strPrefix, match);
        }
        if (needFileIncludedLibMacros(context, offset)) {
            fileLibMacros = contResolver.getFileIncludeLibMacros(context, strPrefix, match);
        }
        if (needGlobalMacros(context, offset)) {
            globProjectMacros = contResolver.getProjectMacros(context, strPrefix, match);
        }
        if (needGlobalLibMacros(context, offset)) {
            globLibMacros = contResolver.getLibMacros(context, strPrefix, match);
        } 
        
        if (needGlobalVariables(context, offset)) {
            globVars = getGlobalVariables(prj, strPrefix, match, offset);
        }
        if (needGlobalEnumerators(context, offset)) {
            globEnumerators = getGlobalEnumerators(prj, strPrefix, match, offset);
        }
        if (needGlobalFunctions(context, offset)) {
            globFuns = getGlobalFunctions(prj, strPrefix, match, offset);
        }
        if (needGlobalNamespaces(context, offset)) {
            globProjectNSs = getGlobalNamespaces(prj, strPrefix, match, offset);
        }        
        if (needLibClasses(context, offset)) {
            libClasses = getLibClassesEnums(prj, strPrefix, match);
        }
        if (needLibVariables(context, offset)) {
            libVars = getLibVariables(prj, strPrefix, match);
        }
        if (needLibEnumerators(context, offset)) {
            libEnumerators = getLibEnumerators(prj, strPrefix, match);
        }
        if (needLibFunctions(context, offset)) {
            libFuns = getLibFunctions(prj, strPrefix, match);
        }
        if (needLibNamespaces(context, offset)) {
            libNSs = getLibNamespaces(prj, strPrefix, match);
        }        
        this.result = buildResult(context, 
                localVars, 
                classFields, classEnumerators, classMethods, classesEnumsTypedefs, 
                fileLocalVars, fileLocalEnumerators, 
                fileLocalMacros, fileProjectMacros, 
                globVars, globEnumerators, globProjectMacros, globFuns, globProjectNSs, 
                libClasses, 
                fileLibMacros, globLibMacros, 
                libVars, libEnumerators, libFuns, libNSs);
        //long timeEnd = System.nanoTime();
        //System.out.println("get gesolve list time "+(timeEnd -timeStart)+" objects "+result.size()); //NOI18N
        //System.out.println("get global macro time "+(timeGlobMacroEnd -timeGlobMacroStart)+" objects "+ //NOI18N
        //        (globProjectMacros.size()+globLibMacros.size()));
        if (TIMING_COMPLETION) {
            time = System.currentTimeMillis() - time;
            System.err.println("Resolving context took " + time + "ms");
        }        
    }

    private static int initHideMask(final CsmContext context, final int resolveTypes, final String strPrefix) {
        int hideTypes = ~RESOLVE_NONE;
        // do not provide libraries data and global data when just resolve context with empty prefix
        if ((resolveTypes & RESOLVE_CONTEXT) == RESOLVE_CONTEXT && strPrefix.length() == 0) {
            hideTypes &= ~RESOLVE_FILE_LIB_MACROS;
            hideTypes &= ~RESOLVE_LIB_MACROS;
            hideTypes &= ~RESOLVE_LIB_CLASSES;
            hideTypes &= ~RESOLVE_LIB_ENUMERATORS;
            hideTypes &= ~RESOLVE_LIB_FUNCTIONS;
            hideTypes &= ~RESOLVE_LIB_NAMESPACES;
            hideTypes &= ~RESOLVE_LIB_VARIABLES;
            // if not in exact file scope do not provide project globals
            if (!CsmKindUtilities.isFile(context.getLastScope())) {
                hideTypes &= ~RESOLVE_GLOB_MACROS;
//                hideTypes &= ~RESOLVE_FILE_PRJ_MACROS;
//                hideTypes &= ~RESOLVE_GLOB_VARIABLES;
//                hideTypes &= ~RESOLVE_GLOB_FUNCTIONS;
//                hideTypes &= ~RESOLVE_GLOB_ENUMERATORS;
//                hideTypes &= ~RESOLVE_CLASSES;
            }
        }
        return hideTypes;
    }
    
    private static Result buildResult(CsmContext context,
            Collection localVars, 
            Collection classFields, Collection classEnumerators, Collection classMethods, Collection classesEnumsTypedefs, 
            Collection fileLocalVars, Collection fileLocalEnumerators, Collection fileLocalMacros, Collection fileProjectMacros, 
            Collection globVars, Collection globEnumerators, 
            Collection globProjectMacros, Collection globFuns, Collection globProjectNSs,             
            Collection libClasses,             
            Collection fileLibMacros, Collection globLibMacros,
            Collection libVars, Collection libEnumerators, Collection libFuns, Collection libNSs) {
        // local vars
        int fullSize = 0;
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(localVars, "Local variables");} //NOI18N
        // add class fields
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(classFields, "Class fields");} //NOI18N
        // add class enumerators
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(classEnumerators, "Class enumerators");} //NOI18N
        // add class methods
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(classMethods, "Class methods");} //NOI18N
        // add classesEnumsTypedefs
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(classesEnumsTypedefs, "Classes/Enums/Typedefs");} //NOI18N
        // add file local variables
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(fileLocalVars, "File Local Variables");} //NOI18N
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(fileLocalEnumerators, "File Local Enumerators");} //NOI18N
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(fileLocalMacros, "File Local Macros");} //NOI18N
        // remove local macros from project included macros
        remove(fileProjectMacros, fileLocalMacros);
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(fileProjectMacros, "File Included Project Macros");} //NOI18N
        // add global variables
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(globVars, "Global variables");} //NOI18N
        // add global enumerators, but remove file local ones
        remove(globEnumerators, fileLocalEnumerators);
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(globEnumerators, "Global enumerators");} //NOI18N
        // global macros
        // remove project included macros from all other macros
        remove(globProjectMacros, fileProjectMacros);
        // remove local macros from project macros
        remove(globProjectMacros, fileLocalMacros);
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(globProjectMacros, "Global Project Macros");} //NOI18N
        // add global functions
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(globFuns, "Global Project functions");} //NOI18N
        // add namespaces
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(globProjectNSs, "Global Project Namespaces");} //NOI18N        
        // add libraries classesEnumsTypedefs
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(libClasses, "Library classes");} //NOI18N
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(fileLibMacros, "File Included Library Macros");} //NOI18N
        // remove file included lib macros from all other lib macros
        remove(globLibMacros, fileLibMacros);
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(globLibMacros, "Global Library Macros");} //NOI18N
        // add libraries variables
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(libVars, "Global Library variables");} //NOI18N
        // add libraries enumerators
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(libEnumerators, "Global Library enumerators");} //NOI18N
        // add libraries functions
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(libFuns, "Global Library functions");} //NOI18N
        // add libraries functions
        if (DEBUG || STAT_COMPLETION) { fullSize += trace(libNSs, "Global Library Namespaces");} //NOI18N
        // all elements info
        if (DEBUG || STAT_COMPLETION) { trace(null, "There are " + fullSize + " resovled elements");} //NOI18N
        Result out = new ResultImpl(
                    localVars,
                    classFields, classEnumerators, classMethods, classesEnumsTypedefs, 
                    fileLocalVars, fileLocalEnumerators, 
                    fileLocalMacros, fileProjectMacros,
                    globVars, globEnumerators, globProjectMacros, globFuns, globProjectNSs,
                    libClasses,
                    fileLibMacros, globLibMacros,
                    libVars, libEnumerators, libFuns, libNSs 
                            );
        return out;
    }
    
    private static Collection remove(Collection dest, Collection removeItems) {
        CsmUtilities.removeAll(dest, removeItems);
        return dest;
    }
    
    protected CsmProjectContentResolver createContentResolver(CsmProject prj) {
        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(prj, isCaseSensitive(), isSortNeeded(), isNaturalSort());
        return contResolver;
    }
    
    protected CsmProjectContentResolver createLibraryResolver(CsmProject lib) {
        CsmProjectContentResolver libResolver = new CsmProjectContentResolver(lib, isCaseSensitive(), isSortNeeded(), isNaturalSort());
        return libResolver;
    }
    
    private static Collection merge(Collection orig, Collection newList) {
        return CsmUtilities.merge(orig, newList);
    }
    
    private Collection getClassesEnums(CsmProject prj, String strPrefix, boolean match, int offset) {
        if (prj == null) {
            return null;
        }
        // try to get elements from visible namespaces
        Collection<CsmNamespace> namespaces = getNamespacesToSearch(this.file, offset, strPrefix.length() == 0);
        LinkedHashSet out = new LinkedHashSet(1024);
        for (CsmNamespace ns : namespaces) {
            List res = contResolver.getNamespaceClassesEnums(ns, strPrefix, match, false);
            out.addAll(res);
        }
        CsmDeclaration.Kind kinds[] =	{
            CsmDeclaration.Kind.CLASS,
            CsmDeclaration.Kind.STRUCT,
            CsmDeclaration.Kind.UNION,
            CsmDeclaration.Kind.ENUM,
            CsmDeclaration.Kind.TYPEDEF
        };        
        Collection usedDecls = getUsedDeclarations(this.file, offset, strPrefix, match, kinds);
        out.addAll(usedDecls);
        return out;
    }
    
    private Collection getGlobalVariables(CsmProject prj, String strPrefix, boolean match, int offset) {    
        Collection<CsmNamespace> namespaces = getNamespacesToSearch(this.file, offset, strPrefix.length() == 0);
        LinkedHashSet out = new LinkedHashSet(1024);
        for (CsmNamespace ns : namespaces) {
            List res = contResolver.getNamespaceVariables(ns, strPrefix, match, false);
            out.addAll(res);
        } 
        CsmDeclaration.Kind kinds[] =	{
            CsmDeclaration.Kind.VARIABLE
        };        
        Collection usedDecls = getUsedDeclarations(this.file, offset, strPrefix, match, kinds);
        out.addAll(usedDecls);        
        return out;
    }
    
    private Collection getGlobalEnumerators(CsmProject prj, String strPrefix, boolean match, int offset) {
        Collection<CsmNamespace> namespaces = getNamespacesToSearch(this.file, offset, strPrefix.length() == 0);
        LinkedHashSet out = new LinkedHashSet(1024);
        for (CsmNamespace ns : namespaces) {
            List res = contResolver.getNamespaceEnumerators(ns, strPrefix, match, false);
            out.addAll(res);
        }        
        return out;        
    }
    
    private Collection getGlobalFunctions(CsmProject prj, String strPrefix, boolean match, int offset) {
        Collection<CsmNamespace> namespaces = getNamespacesToSearch(this.file, offset, strPrefix.length() == 0);
        LinkedHashSet out = new LinkedHashSet(1024);
        for (CsmNamespace ns : namespaces) {
            List res = contResolver.getNamespaceFunctions(ns, strPrefix, match, false);
            out.addAll(res);
        }   
        CsmDeclaration.Kind kinds[] =	{
            CsmDeclaration.Kind.FUNCTION,
            CsmDeclaration.Kind.FUNCTION_DEFINITION
        };        
        Collection usedDecls = getUsedDeclarations(this.file, offset, strPrefix, match, kinds);
        out.addAll(usedDecls);           
        return out;
    }
    
    private Collection getGlobalNamespaces(CsmProject prj, String strPrefix, boolean match, int offset) {
        Collection<CsmNamespace> namespaces = getNamespacesToSearch(this.file, offset, strPrefix.length() == 0);
        LinkedHashSet out = new LinkedHashSet(1024);
        for (CsmNamespace ns : namespaces) {
            List res = contResolver.getNestedNamespaces(ns, strPrefix, match);
            out.addAll(res);
        }        
        return out;
    }
    
    private Collection getLibClassesEnums(CsmProject prj, String strPrefix, boolean match) {
        Collection res = contResolver.getLibClassesEnums(strPrefix, match);
        return res;
    }
    
    private Collection getLibVariables(CsmProject prj, String strPrefix, boolean match) {
        Collection res = contResolver.getLibVariables(strPrefix, match);
        return res;
    }
    
    private Collection getLibEnumerators(CsmProject prj, String strPrefix, boolean match) {
        Collection res = contResolver.getLibEnumerators(strPrefix, match, true);
        return res;
    }
    
    private Collection getLibFunctions(CsmProject prj, String strPrefix, boolean match) {
        Collection res = contResolver.getLibFunctions(strPrefix, match);
        return res;
    }
    
    private Collection getLibNamespaces(CsmProject prj, String strPrefix, boolean match) {
        Collection res = contResolver.getLibNamespaces(strPrefix, match);
        return res;
    }
    
    private boolean needClasses(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_CLASSES) == RESOLVE_CLASSES) {
            return true;
        }
        boolean need = false;
        if ((resolveTypes & RESOLVE_CONTEXT) == RESOLVE_CONTEXT) {
            assert (context != null);
            if (CsmContextUtilities.isInFunction(context, offset)) {
                // for speed up remember result
                resolveTypes |= RESOLVE_CLASSES;
                updateResolveTypesInFunction(offset, context);
                need = true;
            } else if (CsmContextUtilities.getClass(context, false) != null) {
                // for speed up remember result
                resolveTypes |= RESOLVE_CLASSES;
                resolveTypes |= RESOLVE_CLASS_FIELDS;
                resolveTypes |= RESOLVE_CLASS_ENUMERATORS;
                resolveTypes |= RESOLVE_CLASS_NESTED_CLASSIFIERS;
                need = true;
            } else {
                // resolve classes always
                
                // for speed up remember result
                resolveTypes |= RESOLVE_CLASSES;
                
                // resolve global context as well
                resolveTypes |= RESOLVE_GLOB_VARIABLES;
                resolveTypes |= RESOLVE_GLOB_ENUMERATORS;
                resolveTypes |= RESOLVE_GLOB_FUNCTIONS;
                resolveTypes |= RESOLVE_GLOB_NAMESPACES;
                resolveTypes |= RESOLVE_LIB_CLASSES;
                resolveTypes |= RESOLVE_LIB_VARIABLES;
                resolveTypes |= RESOLVE_LIB_ENUMERATORS;
                resolveTypes |= RESOLVE_LIB_FUNCTIONS;     
                resolveTypes |= RESOLVE_LIB_NAMESPACES;
                need = true;
            }
        }
        if (need && (hideTypes & resolveTypes & RESOLVE_CLASSES) == RESOLVE_CLASSES) {
            return true;
        }
        return false;
    }

    private void updateResolveTypesInFunction(final int offset, final CsmContext context) {
        
        if (!CsmContextUtilities.isInType(context, offset)) {
            // some other info to remember in this context
            resolveTypes |= RESOLVE_LOCAL_VARIABLES;
            resolveTypes |= RESOLVE_CLASS_METHODS;
            resolveTypes |= RESOLVE_CLASS_FIELDS;
            resolveTypes |= RESOLVE_CLASS_ENUMERATORS;
            resolveTypes |= RESOLVE_CLASS_NESTED_CLASSIFIERS;
        }
        if (CsmContextUtilities.isInFunctionBody(context, offset)) {
            // some other info to remember in this context
            resolveTypes |= RESOLVE_GLOB_VARIABLES;
            resolveTypes |= RESOLVE_GLOB_ENUMERATORS;
            resolveTypes |= RESOLVE_LIB_VARIABLES;
            resolveTypes |= RESOLVE_LIB_ENUMERATORS;
            resolveTypes |= RESOLVE_GLOB_FUNCTIONS;
            resolveTypes |= RESOLVE_GLOB_NAMESPACES;
            resolveTypes |= RESOLVE_LIB_CLASSES;
            resolveTypes |= RESOLVE_LIB_FUNCTIONS;
            resolveTypes |= RESOLVE_LIB_NAMESPACES;
        }
    }
    
    private boolean needFileLocalVars(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_FILE_LOCAL_VARIABLES) == RESOLVE_LOCAL_VARIABLES) {
            return true;
        }
        boolean need = false;
        if ((resolveTypes & RESOLVE_CONTEXT) == RESOLVE_CONTEXT) {
            assert (context != null);
            if (CsmContextUtilities.isInFunction(context, offset)) {
                // for speed up remember some results
                resolveTypes |= RESOLVE_FILE_LOCAL_VARIABLES;
                
                updateResolveTypesInFunction(offset, context);
                
                need = true;
            }
        }
        if (need && (hideTypes & resolveTypes & RESOLVE_FILE_LOCAL_VARIABLES) == RESOLVE_FILE_LOCAL_VARIABLES) {
            return true;
        }        
        return false;
    }
    
    private boolean needLocalVars(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LOCAL_VARIABLES) == RESOLVE_LOCAL_VARIABLES) {
            return true;
        }
        boolean need = false;
        if ((resolveTypes & RESOLVE_CONTEXT) == RESOLVE_CONTEXT) {
            assert (context != null);
            if (CsmContextUtilities.isInFunction(context, offset)) {
                // for speed up remember some results
                resolveTypes |= RESOLVE_LOCAL_VARIABLES;
                
                updateResolveTypesInFunction(offset, context);
                
                need = true;
            }
        }
        if (need && (hideTypes & resolveTypes & RESOLVE_LOCAL_VARIABLES) == RESOLVE_LOCAL_VARIABLES) {
            return true;
        }          
        return false;
    }
    
    private boolean needGlobalVariables(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_GLOB_VARIABLES) == RESOLVE_GLOB_VARIABLES) {
            return true;
        }
        return false;
    }
    
    private boolean needGlobalEnumerators(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_GLOB_ENUMERATORS) == RESOLVE_GLOB_ENUMERATORS) {
            return true;
        }
        return false;
    }
    
    private boolean needGlobalFunctions(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_GLOB_FUNCTIONS) == RESOLVE_GLOB_FUNCTIONS) {
            return true;
        }
        return false;
    }
    
    private boolean needGlobalNamespaces(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_GLOB_NAMESPACES) == RESOLVE_GLOB_NAMESPACES) {
            return true;
        }
        return false;
    }
    
    private boolean needFunctionVars(CsmContext context, int offset) {
        return needLocalVars(context, offset);
    }
    
    private boolean needLibClasses(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_CLASSES) == RESOLVE_LIB_CLASSES) {
            return true;
        }
        return false;
    }
    
    private boolean needLibVariables(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_VARIABLES) == RESOLVE_LIB_VARIABLES) {
            return true;
        }
        return false;
    }
    
    private boolean needLibEnumerators(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_ENUMERATORS) == RESOLVE_LIB_ENUMERATORS) {
            return true;
        }
        return false;
    }
    
    private boolean needLibFunctions(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_FUNCTIONS) == RESOLVE_LIB_FUNCTIONS) {
            return true;
        }
        return false;
    }
    
    private boolean needLibNamespaces(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_NAMESPACES) == RESOLVE_LIB_NAMESPACES) {
            return true;
        }
        return false;
    }
    
    private boolean needFileLocalMacros(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_FILE_LOCAL_MACROS) == RESOLVE_FILE_LOCAL_MACROS) {
            return true;
        }
        return false;
    }
    
    private boolean needFileIncludedMacros(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_FILE_PRJ_MACROS) == RESOLVE_FILE_PRJ_MACROS) {
            return true;
        }
        return false;
    }
    
    private boolean needFileIncludedLibMacros(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_FILE_LIB_MACROS) == RESOLVE_FILE_LIB_MACROS) {
            return true;
        }
        return false;
    }
    
    private boolean needGlobalMacros(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_GLOB_MACROS) == RESOLVE_GLOB_MACROS) {
            return true;
        }
        return false;
    }
    
    private boolean needGlobalLibMacros(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_MACROS) == RESOLVE_LIB_MACROS) {
            return true;
        }
        return false;
    }
    
    private boolean needClassMethods(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_CLASS_METHODS) == RESOLVE_CLASS_METHODS) {
            return true;
        }
        return false;
    }
    
    private boolean needClassFields(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_CLASS_FIELDS) == RESOLVE_CLASS_FIELDS) {
            return true;
        }
        return false;
    }
    
    private boolean needClassEnumerators(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_CLASS_ENUMERATORS) == RESOLVE_CLASS_ENUMERATORS) {
            return true;
        }
        return false;
    }
    
    private boolean needNestedClassifiers(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_CLASS_NESTED_CLASSIFIERS) == RESOLVE_CLASS_NESTED_CLASSIFIERS) {
            return true;
        }
        return false;
    }
    
    private boolean needClassElements(CsmContext context, int offset) {
        if (((hideTypes & resolveTypes & RESOLVE_CLASS_METHODS) == RESOLVE_CLASS_METHODS) ||
            ((hideTypes & resolveTypes & RESOLVE_CLASS_FIELDS) == RESOLVE_CLASS_FIELDS) ||
            ((hideTypes & resolveTypes & RESOLVE_CLASS_NESTED_CLASSIFIERS) == RESOLVE_CLASS_NESTED_CLASSIFIERS) ||
            ((hideTypes & resolveTypes & RESOLVE_CLASS_ENUMERATORS) == RESOLVE_CLASS_ENUMERATORS)) {
            return true;
        }
        return false;
    }
    

    // ====================== Debug support ===================================
    
    private static int trace(Collection/*<CsmObject*/ list, String msg) {
        System.err.println("\t" + msg + " [size - " + (list == null ? "null" : list.size()) +"]"); //NOI18N
        if (list == null) {
            return 0;
        }
        if (TRACE) {
            int i = 0;
            for (Object obj : list) {
                CsmObject elem = (CsmObject) obj;
                System.err.println("\t\t["+i+"]"+CsmUtilities.getCsmName(elem)); //NOI18N
                i++;
            }
        }
        return list.size();
    }

    private static final class ResultImpl implements Result {
        private Collection localVars      = null;
        
        private Collection classFields    = null;
        private Collection classEnumerators = null;
        private Collection classMethods   = null;
        private Collection classesEnumsTypedefs = null;
        
        private Collection fileLocalVars  = null;
        private Collection fileLocalEnumerators = null;
        private Collection fileLocalMacros = null;

        private Collection fileProjectMacros = null;

        private Collection globVars       = null;
        private Collection globEnumerators = null;
        private Collection globProjectMacros = null;
        
        private Collection globFuns       = null;
        private Collection globProjectNSs = null; 
        
        private Collection libClasses     = null;
        private Collection fileLibMacros  = null;
        private Collection globLibMacros  = null;
        private Collection libVars        = null;
        private Collection libEnumerators = null;
        private Collection libFuns        = null;
        private Collection libNSs         = null;    

        private ResultImpl(
                    Collection localVars,

                    Collection classFields,
                    Collection classEnumerators,
                    Collection classMethods,
                    Collection classesEnumsTypedefs,

                    Collection fileLocalVars,
                    Collection fileLocalEnumerators,
                    Collection fileLocalMacros,

                    Collection fileProjectMacros,

                    Collection globVars,
                    Collection globEnumerators,
                    Collection globProjectMacros,

                    Collection globFuns,
                    Collection globProjectNSs,

                    Collection libClasses,
                    Collection fileLibMacros,
                    Collection globLibMacros,
                    Collection libVars,
                    Collection libEnumerators,
                    Collection libFuns,
                    Collection libNSs 
                            ) {
            this.localVars = localVars;
            
            this.classFields = classFields;
            this.classEnumerators = classEnumerators;
            this.classMethods = classMethods;
            this.classesEnumsTypedefs = classesEnumsTypedefs;
            
            this.fileLocalVars = fileLocalVars;
            this.fileLocalEnumerators = fileLocalEnumerators;
            this.fileLocalMacros = fileLocalMacros;
            
            this.fileProjectMacros = fileProjectMacros;
            
            this.globVars = globVars;
            this.globEnumerators = globEnumerators;
            this.globProjectMacros = globProjectMacros;
            
            this.globFuns = globFuns;
            this.globProjectNSs = globProjectNSs;
            
            this.libClasses = libClasses;
            this.fileLibMacros = fileLibMacros;
            this.globLibMacros = globLibMacros;
            this.libVars = libVars;
            this.libEnumerators = libEnumerators;
            this.libFuns = libFuns;
            this.libNSs = libNSs;
        }
        
        public Collection getLocalVariables() {
            return maskNull(localVars);
        }

        public Collection getClassFields() {
            return maskNull(classFields);
        }

        public Collection getClassEnumerators() {
            return maskNull(classEnumerators);
        }

        public Collection getClassMethods() {
            return maskNull(classMethods);
        }

        public Collection getProjectClassesifiersEnums() {
            return maskNull(classesEnumsTypedefs);
        }

        public Collection getFileLocalVars() {
            return maskNull(fileLocalVars);
        }

        public Collection getFileLocalEnumerators() {
            return maskNull(fileLocalEnumerators);
        }

        public Collection getFileLocalMacros() {
            return maskNull(fileLocalMacros);
        }

        public Collection getInFileIncludedProjectMacros() {
            return maskNull(fileProjectMacros);
        }

        public Collection getGlobalVariables() {
            return maskNull(globVars);
        }

        public Collection getGlobalEnumerators() {
            return maskNull(globEnumerators);
        }

        public Collection getGlobalProjectMacros() {
            return maskNull(globProjectMacros);
        }

        public Collection getGlobalProjectFunctions() {
            return maskNull(globFuns);
        }

        public Collection getGlobalProjectNamespaces() {
            return maskNull(globProjectNSs);
        }

        public Collection getLibClassifiersEnums() {
            return maskNull(libClasses);
        }

        public Collection getInFileIncludedLibMacros() {
            return maskNull(fileLibMacros);
        }

        public Collection getLibMacros() {
            return maskNull(globLibMacros);
        }

        public Collection getLibVariables() {
            return maskNull(libVars);
        }

        public Collection getLibEnumerators() {
            return maskNull(libEnumerators);
        }

        public Collection getLibFunctions() {
            return maskNull(libFuns);
        }

        public Collection getLibNamespaces() {
            return maskNull(libNSs);
        }
        
        public Collection addResulItemsToCol(Collection orig) {
            assert orig != null;
            return appendResult(orig, this);
        }

        int size = -1;
        public int size() {
            if (size == -1) {
                size = 0;
                // init size value
                size += getLocalVariables().size();

                size += getClassFields().size();

                size += getClassEnumerators().size();

                size += getClassMethods().size();

                size += getProjectClassesifiersEnums().size();

                size += getFileLocalVars().size();

                size += getFileLocalEnumerators().size();

                size += getFileLocalMacros().size();

                size += getInFileIncludedProjectMacros().size();

                size += getGlobalVariables().size();

                size += getGlobalEnumerators().size();

                size += getGlobalProjectMacros().size();

                size += getGlobalProjectFunctions().size();

                size += getGlobalProjectNamespaces().size();

                size += getLibClassifiersEnums().size();

                size += getInFileIncludedLibMacros().size();

                size += getLibMacros().size();

                size += getLibVariables().size();

                size += getLibEnumerators().size();

                size += getLibFunctions().size();

                size += getLibNamespaces().size();
            }
            return size;
        }
    }
    
    private static final Result EMPTY_RESULT = new EmptyResultImpl();
    private static final class EmptyResultImpl implements Result {
        public Collection getLocalVariables() {
            return Collections.EMPTY_LIST;
        }

        public Collection getClassFields() {
            return Collections.EMPTY_LIST;
        }

        public Collection getClassEnumerators() {
            return Collections.EMPTY_LIST;
        }

        public Collection getClassMethods() {
            return Collections.EMPTY_LIST;
        }

        public Collection getProjectClassesifiersEnums() {
            return Collections.EMPTY_LIST;
        }

        public Collection getFileLocalVars() {
            return Collections.EMPTY_LIST;
        }

        public Collection getFileLocalEnumerators() {
            return Collections.EMPTY_LIST;
        }

        public Collection getFileLocalMacros() {
            return Collections.EMPTY_LIST;
        }

        public Collection getInFileIncludedProjectMacros() {
            return Collections.EMPTY_LIST;
        }

        public Collection getGlobalVariables() {
            return Collections.EMPTY_LIST;
        }

        public Collection getGlobalEnumerators() {
            return Collections.EMPTY_LIST;
        }

        public Collection getGlobalProjectMacros() {
            return Collections.EMPTY_LIST;
        }

        public Collection getGlobalProjectFunctions() {
            return Collections.EMPTY_LIST;
        }

        public Collection getGlobalProjectNamespaces() {
            return Collections.EMPTY_LIST;
        }

        public Collection getLibClassifiersEnums() {
            return Collections.EMPTY_LIST;
        }

        public Collection getInFileIncludedLibMacros() {
            return Collections.EMPTY_LIST;
        }

        public Collection getLibMacros() {
            return Collections.EMPTY_LIST;
        }

        public Collection getLibVariables() {
            return Collections.EMPTY_LIST;
        }

        public Collection getLibEnumerators() {
            return Collections.EMPTY_LIST;
        }

        public Collection getLibFunctions() {
            return Collections.EMPTY_LIST;
        }

        public Collection getLibNamespaces() {
            return Collections.EMPTY_LIST;
        }

        public Collection addResulItemsToCol(Collection orig) {
            return orig;
        }

        public int size() {
            return 0;
        }
        
    }
    
    private static Collection maskNull(Collection list) {
        return list != null ? list : Collections.EMPTY_LIST;
    }
    
    private static Collection appendResult(Collection dest, ResultImpl result) {
        assert dest != null;
        // local vars
        merge(dest, result.localVars);
        // add class fields
        merge(dest, result.classFields);
        // add class enumerators
        merge(dest, result.classEnumerators);
        // add class methods
        merge(dest, result.classMethods);
        // add classesEnumsTypedefs
        merge(dest, result.classesEnumsTypedefs);
        // add file local variables
        merge(dest, result.fileLocalVars);
        merge(dest, result.fileLocalEnumerators);
        merge(dest, result.fileLocalMacros);
        merge(dest, result.fileProjectMacros);
        // add global variables
        merge(dest, result.globVars);
        // add global enumerators
        merge(dest, result.globEnumerators);
        // global macros
        merge(dest, result.globProjectMacros);
        // add global functions
        merge(dest, result.globFuns);
        // add namespaces
        merge(dest, result.globProjectNSs);
        // add libraries classesEnumsTypedefs
        merge(dest, result.libClasses);
        merge(dest, result.fileLibMacros);
        merge(dest, result.globLibMacros);
        // add libraries variables
        merge(dest, result.libVars);
        // add libraries enumerators
        merge(dest, result.libEnumerators);
        // add libraries functions
        merge(dest, result.libFuns);
        // add libraries functions
        merge(dest, result.libNSs);

        return dest;
    }    

    private void initMacroResolving(final CsmContext context, final String strPrefix) {
        if ((resolveTypes & RESOLVE_CONTEXT) == RESOLVE_CONTEXT) {
            if (strPrefix.length() == 0) {
                resolveTypes |= RESOLVE_FILE_LOCAL_MACROS | RESOLVE_FILE_PRJ_MACROS;
            } else {
                resolveTypes |= RESOLVE_FILE_LOCAL_MACROS | RESOLVE_GLOB_MACROS | RESOLVE_LIB_MACROS;
            }
        }
    }
    
    private Collection<CsmDeclaration> getUsedDeclarations(CsmFile file, int offset, String prefix, boolean match, CsmDeclaration.Kind[] kinds) {
        CsmProject prj = file.getProject();
        CsmProject inProject = prefix.length() == 0 ? prj : null;
        Collection<CsmDeclaration> usedDecls = CsmUsingResolver.getDefault().findUsedDeclarations(file, offset, inProject);
        Collection<CsmDeclaration> out = filterDeclarations(usedDecls, prefix, match, kinds);
        return out;
    }
    
    private Collection<CsmDeclaration> filterDeclarations(Collection<CsmDeclaration> orig, String prefix, boolean match,  CsmDeclaration.Kind[] kinds) {
        LinkedHashSet<CsmDeclaration> out = new LinkedHashSet<CsmDeclaration>(orig.size());
        contResolver.filterDeclarations(orig.iterator(), out, kinds, prefix, match);
        return out;
    }
        
    private Collection<CsmNamespace> getNamespacesToSearch(CsmFile file, int offset, boolean onlyInProject) {
        CsmProject prj = file.getProject();
        CsmProject inProject = onlyInProject ? prj : null;
        Collection<CsmNamespace> namespaces = CsmUsingResolver.getDefault().findVisibleNamespaces(file, offset, inProject);
        CsmNamespace globNS = prj.getGlobalNamespace();
        namespaces.add(globNS);
        namespaces = filterNamespaces(namespaces, inProject);
        return namespaces;
    }
    
    private Collection<CsmNamespace> filterNamespaces(Collection<CsmNamespace> orig, CsmProject prj) {
        if (prj == null) {
            return orig;
        }
        LinkedHashSet out = new LinkedHashSet(orig.size());
        for (CsmNamespace ns : orig) {
            if (ns.getProject() == prj) {
                out.add(ns);
            }
        }
        return out;
    }    
}

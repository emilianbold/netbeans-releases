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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JEditorPane;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.impl.services.ReferenceRepositoryImpl;
import org.netbeans.modules.cnd.modelimpl.trace.XRefResultSet.ContextEntry;
import org.netbeans.modules.cnd.modelimpl.trace.XRefResultSet.DeclarationScope;
import org.netbeans.modules.cnd.modelimpl.trace.XRefResultSet.IncludeLevel;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author Vladimir Voskresensky
 */
public class TraceXRef extends TraceModel {

    private String refFile = "";
    private String declarationName = "";
    private int line = 0;
    private int column = 0;
    public TraceXRef() {
    }
    
    public static void main(String[] args) {
        setUp();
        TraceXRef trace = new TraceXRef();
        trace.test(args);
    }    
    
    private void test(String[] args) {
        try {
            processArguments(args);
            doTest();
            long time = 0;
            if (super.isShowTime()) {
                time = System.currentTimeMillis();
            }
            CsmObject object = null;
            if (declarationName.length() > 0) {
                System.err.println("looking for object with name: " + declarationName);
                object = super.getProject().findDeclaration(declarationName);
                if (object == null) {
                    System.err.println("No object with name " + declarationName + " in model");
                }
            } else if ((refFile.length() > 0) && (line > 0) && (column > 0)) {
                System.out.println("looking for object on position: line=" + line + " column="+column); // NOI18N
                System.out.println("in file:" + refFile); // NOI18N
                CsmFile file = getCsmFile(refFile);
                if (!(file instanceof FileImpl)) {
                    System.err.println("No CsmFile was found with name: " + refFile);
                } else {
                    FileImpl implFile = (FileImpl)file;
                    int offset = implFile.getOffset(line, column);
                    if (offset < 0) {
                        System.err.println("incorrect offset for position line="+line+" col="+column);
                    } else {
                        CsmReference ref = CsmReferenceResolver.getDefault().findReference(implFile, offset);
                        if (ref == null) {
                            System.err.println("no any references were found on position line="+line+" col="+column);
                        } else {
                            object = ref.getReferencedObject();
                        }
                    }
                }
            } else {
                System.err.println("To run xref tests start script with parameter:");
                System.err.println("should be --xref#file_path#1_based_line#1_based_column or --xref#name");
            }
            if (object == null) {
                System.out.println("Nothing to search"); // NOI18N
            } else {
                System.out.println("TARGET OBJECT IS\n  " + CsmTracer.toString(object)); // NOI18N
                if (CsmKindUtilities.isNamedElement(object)) {
                    System.out.println("NAME IS: " + ((CsmNamedElement)object).getName()); // NOI18N
                }
                if (CsmKindUtilities.isDeclaration(object)) {
                    System.out.println("UNIQUE NAME IS: " + ((CsmDeclaration)object).getUniqueName()); // NOI18N
                }
                
                ReferenceRepositoryImpl xRefRepository = new ReferenceRepositoryImpl();
                CsmObject[] decDef = CsmBaseUtilities.getDefinitionDeclaration(object, true);
                CsmObject decl = decDef[0];
                CsmObject def = decDef[1];                
                Collection<CsmReference> refs = xRefRepository.getReferences(decl, getProject(), CsmReferenceKind.ALL);
                if (super.isShowTime()) {
                    time = System.currentTimeMillis() - time;
                }            
                traceRefs(refs, decl, def, System.out);
                if (super.isShowTime()) {
                    System.out.println("search took " + time + "ms"); // NOI18N
                }       
            }
        }
        finally {
            super.shutdown();
            if (TraceFlags.USE_AST_CACHE) {
                CacheManager.getInstance().close();
            } else {
                APTDriver.getInstance().close();
            }            
        }        
    }
   
    @SuppressWarnings("deprecation")
    private static void setUp() {
        // this is the only way to init extension-based recognizer
        FileUtil.setMIMEType("cc", "text/x-c++"); // NOI18N
        FileUtil.setMIMEType("h", "text/x-c++"); // NOI18N
        FileUtil.setMIMEType("c", "text/x-c"); // NOI18N
        
        JEditorPane.registerEditorKitForContentType("text/x-c++", "org.netbeans.modules.cnd.editor.cplusplus.CCKit"); // NOI18N
        
        JEditorPane.registerEditorKitForContentType("text/x-c", "org.netbeans.modules.cnd.editor.cplusplus.CKit"); // NOI18N
    }
    
    private CsmFile getCsmFile(String path) {
        return super.getProject().findFile(new File(path).getAbsolutePath());
    }
    
    @Override
    protected boolean processFlag(String flag) {
        String xRef = "xref"; // NOI18N
        if (flag.startsWith(xRef)) {
            String[] split = flag.split("#"); // NOI18N
            boolean error = false;
            if (split.length == 2) {
                declarationName = split[1];
                error = (declarationName == null) || (declarationName.length() == 0);
            } else if (split.length == 4) {
                refFile = split[1];
                try {
                    line = Integer.parseInt(split[2]);
                    column = Integer.parseInt(split[3]);
                } catch (NumberFormatException ex) {
                    DiagnosticExceptoins.register(ex);
                    line = 0;
                    column = 0;
                }
                error = (refFile == null) || (refFile.length() == 0) || line <= 0 || column <= 0;
            }
            if (error) {
                declarationName ="";
                refFile ="";
                System.err.println("unexpected parameter " + flag);
                System.err.println("should be --xref#file_path#1_based_line#1_based_column or --xref#name");
            }
	    return true;
        }
        return false;
    }
    
    public static void traceProjectRefsStatistics(NativeProject prj, PrintWriter printOut, CsmProgressListener callback) {
        CsmProject csmPrj = CsmModelAccessor.getModel().getProject(prj);
        XRefResultSet bag = new XRefResultSet();
        Collection<CsmFile> allFiles = csmPrj.getAllFiles();
        if (callback != null) {
            callback.projectFilesCounted(csmPrj, allFiles.size());
        }
        for (CsmFile file : allFiles) {
            if (callback != null) {
                callback.fileParsingStarted(file);
            }
            analyzeFile(file, bag, printOut);
        }
        if (callback != null) {
            callback.projectParsingFinished(csmPrj);
        }
        traceStatistics(bag, printOut);
    }
    
    public static void traceRefs(Collection<CsmReference> out, CsmObject target, PrintStream streamOut) {
        assert target != null;
        CsmObject[] decDef = CsmBaseUtilities.getDefinitionDeclaration(target, true);
        CsmObject decl = decDef[0];
        CsmObject def = decDef[1];        
        assert decl != null;
        traceRefs(out, decl, def, streamOut);
    }
    
    public static void traceRefs(Collection<CsmReference> out, CsmObject targetDecl, CsmObject targetDef, PrintStream streamOut) {
        if (out.size() == 0) {
            streamOut.println("REFERENCES ARE NOT FOUND"); // NOI18N
        } else {
            streamOut.println("REFERENCES ARE:" ); // NOI18N
            out = sortRefs(out);
            for (CsmReference ref : out) {
                streamOut.println(toString(ref, targetDecl, targetDef));
            }
        }        
    }
    
    public static String toString(CsmReference ref, CsmObject targetDecl, CsmObject targetDef) {
        String out = CsmTracer.getOffsetString(ref, true);
        CsmReferenceKind kind = CsmReferenceResolver.getDefault().getReferenceKind(ref, targetDecl, targetDef);
        String postfix;
        if (kind == CsmReferenceKind.DECLARATION) {
            postfix = " (DECLARATION)"; // NOI18N
        } else if (kind == CsmReferenceKind.DEFINITION) {
            postfix = " (DEFINITION)"; // NOI18N
        } else if (CsmReferenceKind.ANY_USAGE.contains(kind)) {
            postfix = "";
        } else {
            System.err.println("unknown reference kind " + kind + " for " + ref);           
            postfix = "";
        }
        return out + postfix;
    }
        
    public static Collection<CsmReference> sortRefs(Collection<CsmReference> refs) {
        List<CsmReference> out = new ArrayList(refs); 
        Collections.sort(out, FILE_NAME_START_OFFSET_COMPARATOR);
        return out;
    }    
    
    public static final Comparator<CsmOffsetable> FILE_NAME_START_OFFSET_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            if (o1 == o2) {
                return 0;
            }
            CsmOffsetable i1 = (CsmOffsetable)o1;
            CsmOffsetable i2 = (CsmOffsetable)o2; 
            CharSequence path1 = i1.getContainingFile().getAbsolutePath();
            CharSequence path2 = i2.getContainingFile().getAbsolutePath();
            int res = CharSequenceKey.Comparator.compare(path1,path2);
            if (res == 0) {
                int ofs1 = i1.getStartOffset();
                int ofs2 = i2.getStartOffset();
                res = ofs1 - ofs2;
            }
            return res;
        }   
        
        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return 11; // any dummy value
        }          
    };  

    private static void analyzeFile(CsmFile file, XRefResultSet bag, PrintWriter out) {
        long time = System.currentTimeMillis();
        visitDeclarations(file.getDeclarations(), bag, out);
        time = System.currentTimeMillis() - time;
        out.println(file.getAbsolutePath() + " took " + time + "ms");
    }
    
    private static void visitDeclarations(Collection<? extends CsmOffsetableDeclaration> decls, XRefResultSet bag, PrintWriter printOut) {
        for (CsmOffsetableDeclaration decl : decls) {
            if (CsmKindUtilities.isFunctionDefinition(decl)) {
                handleFunctionDefinition((CsmFunctionDefinition)decl, bag, printOut);
            } else if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                visitDeclarations(((CsmNamespaceDefinition)decl).getDeclarations(), bag, printOut);
            } else if (CsmKindUtilities.isClass(decl)) {
                visitDeclarations(((CsmClass)decl).getMembers(), bag, printOut);
            }
        }
    }
    
    private static void handleFunctionDefinition(final CsmFunctionDefinition fun, final XRefResultSet bag, final PrintWriter printOut) {
        final CsmScope scope = fun.getBody();
        if (scope != null) {
            final XRefResultSet.ContextScope funScope = classifyFunctionScope(fun, printOut);
            final ObjectContext<CsmFunctionDefinition> funContext = createContextObject(fun, printOut);
            final Set<CsmObject> objectsUsedInScope = new HashSet<CsmObject>();
            bag.incrementScopeCounter(funScope);
            CsmFileReferences.getDefault().accept(
                    scope, 
                    new CsmFileReferences.Visitor() {
                        public void visit(CsmReference ref) {
                            XRefResultSet.ContextEntry entry = createEntry(objectsUsedInScope, ref, funContext, printOut);
                            if (entry != null) {
                                bag.addEntry(funScope, entry);
                            }
                        }
                    });
        } else {
            printOut.println("function definition without body " + fun);
        }
    }
    
    private static XRefResultSet.ContextEntry createEntry(Set<CsmObject> objectsUsedInScope, CsmReference ref, ObjectContext<CsmFunctionDefinition> fun, PrintWriter printOut) {
        XRefResultSet.ContextEntry entry;
        CsmObject target = ref.getReferencedObject();
        if (target == null) {
            entry = XRefResultSet.ContextEntry.UNRESOLVED;
        } else {
            CsmReferenceKind kind = CsmReferenceResolver.getDefault().getReferenceKind(ref);
            if (kind == CsmReferenceKind.DIRECT_USAGE) { 
                XRefResultSet.DeclarationKind declaration = classifyDeclaration(target, printOut);
                XRefResultSet.DeclarationScope declarationScope = classifyDeclarationScopeForFunction(declaration, target, fun, printOut);
                XRefResultSet.IncludeLevel declarationIncludeLevel = classifyIncludeLevel(target, fun.objFile, printOut);
                XRefResultSet.UsageStatistics usageStat = XRefResultSet.UsageStatistics.FIRST_USAGE;
                if (objectsUsedInScope.contains(target)) {
                    usageStat = XRefResultSet.UsageStatistics.NEXT_USAGE;
                } else {
                    objectsUsedInScope.add(target);
                }
                entry = new XRefResultSet.ContextEntry(declaration, declarationScope, declarationIncludeLevel, usageStat);
            } else {
                entry = null;
            }
        }
        return entry;
    }
    
    private static XRefResultSet.ContextScope classifyFunctionScope(CsmFunction fun, PrintWriter printOut) {
        assert fun != null;
        XRefResultSet.ContextScope out = XRefResultSet.ContextScope.UNRESOLVED;
        CsmScope outScope = fun.getScope();
        if (outScope == null) {
            printOut.println("ERROR: no scope for function " + fun);
            return out;
        }
        if (CsmKindUtilities.isConstructor(fun)) {
            out = CsmBaseUtilities.isInlineFunction(fun) ? 
                            XRefResultSet.ContextScope.INLINED_CONSTRUCTOR : 
                            XRefResultSet.ContextScope.CONSTRUCTOR;
        } else if (CsmKindUtilities.isMethod(fun)) {
            out = CsmBaseUtilities.isInlineFunction(fun) ? 
                XRefResultSet.ContextScope.INLINED_METHOD : 
                XRefResultSet.ContextScope.METHOD;
        } else {
            if (CsmKindUtilities.isFile(outScope)) {
                out = XRefResultSet.ContextScope.FILE_LOCAL_FUNCTION;
            } else {
                CsmNamespace ns = CsmBaseUtilities.getFunctionNamespace(fun);
                if (ns != null) {
                    out = ns.isGlobal() ? 
                            XRefResultSet.ContextScope.GLOBAL_FUNCTION :
                            XRefResultSet.ContextScope.NAMESPACE_FUNCTION;
                }
            }
        }
        if (out == XRefResultSet.ContextScope.UNRESOLVED) {
            printOut.println("ERROR: non classified function " + fun);            
        }
        return out;
    }   
 
    private static XRefResultSet.DeclarationKind classifyDeclaration(CsmObject obj, PrintWriter printOut) {
        XRefResultSet.DeclarationKind out = XRefResultSet.DeclarationKind.UNRESOLVED;
        if (CsmKindUtilities.isClassifier(obj)) {
            out = XRefResultSet.DeclarationKind.CLASSIFIER;
        } else if (CsmKindUtilities.isEnumerator(obj)) {
            out = XRefResultSet.DeclarationKind.ENUMERATOR;
        } else if (CsmKindUtilities.isParamVariable(obj)) {
            out = XRefResultSet.DeclarationKind.PARAMETER;
        } else if (CsmKindUtilities.isVariable(obj)) {
            out = XRefResultSet.DeclarationKind.VARIABLE;
        } else if (CsmKindUtilities.isFunction(obj)) {
            out = XRefResultSet.DeclarationKind.FUNCTION;
        } else if (CsmKindUtilities.isNamespace(obj)) {
            out = XRefResultSet.DeclarationKind.NAMESPACE;
        } else if (CsmKindUtilities.isMacro(obj)) {
            out = XRefResultSet.DeclarationKind.MACRO;
        } else if (CsmKindUtilities.isClassForwardDeclaration(obj)) {
            out = XRefResultSet.DeclarationKind.CLASS_FORWARD;
        } else if (obj != null) {
            printOut.println("ERROR: non classified declaration " + obj);            
        }
        return out;
    }
    
    private static XRefResultSet.IncludeLevel classifyIncludeLevel(CsmObject obj, CsmFile file, PrintWriter printOut) {
        XRefResultSet.IncludeLevel out = XRefResultSet.IncludeLevel.UNRESOLVED;
        CsmInclude incl = null;
        CsmProject objPrj = null;
        if (CsmKindUtilities.isOffsetable(obj)) {
            CsmFile objFile = ((CsmOffsetable)obj).getContainingFile();
            if (file.equals(objFile)) {
                out = XRefResultSet.IncludeLevel.THIS_FILE;
            } else {
                objPrj = objFile.getProject();
                incl = findFirstLevelInclude(file, objFile);
            }
        } else if (CsmKindUtilities.isNamespace(obj)) {
            CsmNamespace ns = (CsmNamespace)obj;
            objPrj = ns.getProject();
            // check all namespace definitions
            for (CsmNamespaceDefinition nsDef : ns.getDefinitions()) {
                CsmFile defFile = nsDef.getContainingFile();
                if (file.equals(defFile)) {
                    out = XRefResultSet.IncludeLevel.THIS_FILE;
                    break;
                }
            }
            if (out != XRefResultSet.IncludeLevel.THIS_FILE) {
                for (CsmNamespaceDefinition nsDef : ns.getDefinitions()) {
                    CsmFile defFile = nsDef.getContainingFile();
                    CsmInclude curIncl = findFirstLevelInclude(file, defFile);
                    if (curIncl != null) {
                        incl = curIncl;
                        break;
                    }
                }            
            }
        } else {
            printOut.println("ERROR: non classified declaration " + obj); 
        }
        if (out != XRefResultSet.IncludeLevel.THIS_FILE) {
            if (incl != null) {
                out = incl.isSystem() ? XRefResultSet.IncludeLevel.LIBRARY_DIRECT : XRefResultSet.IncludeLevel.PROJECT_DIRECT;
            } else {
                out = file.getProject().equals(objPrj) ? XRefResultSet.IncludeLevel.PROJECT_DEEP : XRefResultSet.IncludeLevel.LIBRARY_DEEP;
            }
        }
        return out;
    }   
    
    private static XRefResultSet.DeclarationScope classifyDeclarationScopeForFunction(XRefResultSet.DeclarationKind kind, CsmObject obj, 
            ObjectContext<CsmFunctionDefinition> csmFunction, PrintWriter printOut) {
        XRefResultSet.DeclarationScope out = XRefResultSet.DeclarationScope.UNRESOLVED;
        ObjectContext<CsmObject> objContext = createContextObject(obj, printOut);
        switch (kind) {
            case NAMESPACE:
            {
                out = checkNamespaceContainers(objContext, csmFunction);
                break;
            }
            case CLASSIFIER:
            {
                if (objContext.objClass != null) {
                    out = checkClassContainers(objContext, csmFunction);
                } else if (objContext.objNs != null) {
                    out = checkNamespaceContainers(objContext, csmFunction);
                } else if (printOut != null) {
                    printOut.println("unknown classifier " + objContext.csmObject + " in context of " + csmFunction.csmObject); // NOI18N
                }
                break;
            }
            case FUNCTION:
            {
                out = checkFileClassNamespaceContainers(objContext, csmFunction, printOut);
                break;
            }
            case MACRO:
            {
                out = checkFileContainer(objContext, csmFunction);           
                break;
            }
            case PARAMETER:
            {
                out = XRefResultSet.DeclarationScope.FUNCTION_THIS;
                break;
            }
            case ENUMERATOR:
            case VARIABLE:
            {
                int stOffset = ((CsmOffsetable)obj).getStartOffset();
                if (csmFunction.csmObject.getStartOffset() < stOffset &&
                        stOffset < csmFunction.csmObject.getEndOffset()) {
                    out = XRefResultSet.DeclarationScope.FUNCTION_THIS;
                } else {
                    out = checkFileClassNamespaceContainers(objContext, csmFunction, printOut);
                }
                break;
            }
            case UNRESOLVED:
                break;
            case CLASS_FORWARD:
            default:
                printOut.println("unhandled kind " + kind + " for object " + objContext.csmObject);
        }
        return out;
    }
    
    private static XRefResultSet.DeclarationScope checkFileContainer(
                                        ObjectContext<CsmObject> objContext,
                                        ObjectContext<CsmFunctionDefinition> csmFunction) {
        XRefResultSet.DeclarationScope out = XRefResultSet.DeclarationScope.UNRESOLVED;
        if (csmFunction.objFile.equals(objContext.objFile)) {
            out = XRefResultSet.DeclarationScope.FILE_THIS;
        } else if (csmFunction.objPrj.equals(objContext.objPrj)) {
            out = XRefResultSet.DeclarationScope.PROJECT_FILE;
        } else {
            out = XRefResultSet.DeclarationScope.LIBRARY_FILE;
        }        
        return out;
    }
    
    private static XRefResultSet.DeclarationScope checkNamespaceContainers(
                                        ObjectContext<CsmObject> objContext,
                                        ObjectContext<CsmFunctionDefinition> csmFunction) {
        XRefResultSet.DeclarationScope out = XRefResultSet.DeclarationScope.UNRESOLVED;
        if (objContext.objNs != null) {
            boolean isNested = false;
            if (!objContext.objNs.isGlobal() && (csmFunction.objNs != null) &&
                    !csmFunction.objNs.isGlobal()) {
                CsmNamespace ns = csmFunction.objNs;
                if (ns.equals(objContext.objNs)) {
                    out = XRefResultSet.DeclarationScope.NAMESPACE_THIS;
                    isNested = true;
                } else {
                    while (ns != null && !ns.isGlobal()) {
                        if (ns.equals(objContext.objNs)) {
                            out = XRefResultSet.DeclarationScope.NAMESPACE_PARENT;
                            isNested = true;
                            break;
                        }
                        ns = ns.getParent();
                    }
                }
            }
            if (!isNested) {
                if (objContext.objNs.isGlobal()) {
                    out = csmFunction.objPrj.equals(objContext.objPrj) ? XRefResultSet.DeclarationScope.PROJECT_GLOBAL : XRefResultSet.DeclarationScope.LIBRARY_GLOBAL;
                } else {
                    out = csmFunction.objPrj.equals(objContext.objPrj) ? XRefResultSet.DeclarationScope.PROJECT_NAMESPACE : XRefResultSet.DeclarationScope.LIBRARY_NAMESPACE;
                }
            }
        }
        return out;
    }
    
    private static XRefResultSet.DeclarationScope checkClassContainers(
                                        ObjectContext<CsmObject> objContext,
                                        ObjectContext<CsmFunctionDefinition> csmFunction) {
        XRefResultSet.DeclarationScope out = XRefResultSet.DeclarationScope.UNRESOLVED;
        if (objContext.objClass != null) {
            boolean isInherited = false;
            if (csmFunction.objClass != null) {
                // check inheritance 
                if (csmFunction.objClass.equals(objContext.objClass)) {
                    out = XRefResultSet.DeclarationScope.CLASSIFIER_THIS;
                    isInherited = true;
                } else if (CsmInheritanceUtilities.isAssignableFrom(objContext.objClass, csmFunction.objClass)) {
                    out = XRefResultSet.DeclarationScope.CLASSIFIER_PARENT;
                    isInherited = true;
                }
            }
            if (!isInherited) {
                if (csmFunction.objPrj.equals(objContext.objPrj)) {
                    out = XRefResultSet.DeclarationScope.PROJECT_CLASSIFIER;
                } else {
                    out = XRefResultSet.DeclarationScope.LIBRARY_CLASSIFIER;
                }
            }
        }   
        return out;
    }
    
    private static XRefResultSet.DeclarationScope checkFileClassNamespaceContainers(
                                        ObjectContext<CsmObject> objContext,
                                        ObjectContext<CsmFunctionDefinition> csmFunction,
                                        PrintWriter printOut) {
        XRefResultSet.DeclarationScope out = XRefResultSet.DeclarationScope.UNRESOLVED;
        if (CsmKindUtilities.isFile(objContext.objScope)) {
            out = checkFileContainer(objContext, csmFunction);
        } else if (objContext.objClass != null) {
            out = checkClassContainers(objContext, csmFunction);
        } else if (objContext.objNs != null) {
            out = checkNamespaceContainers(objContext, csmFunction);
        } else if (printOut != null) {
            printOut.println("unknown scope of " + objContext.csmObject + " in context of " + csmFunction.csmObject); // NOI18N
        }
        return out;
    }

    private static CsmInclude findFirstLevelInclude(CsmFile startFile, CsmFile searchFile) {
        assert startFile != null : "start file must be not null";
        assert searchFile != null : "search file must be not null";
        for (CsmInclude incl : startFile.getIncludes()) {
            CsmFile included = incl.getIncludeFile();
            if (searchFile.equals(included)) {
                return incl;
            } else if (included != null && included.getDeclarations().isEmpty()) {
                // this is a fake include only file
                return findFirstLevelInclude(included, searchFile);
            }
        }
        return null;
    }

    private static void traceStatistics(XRefResultSet bag, PrintWriter printOut) {
        printOut.println("Number of analyzed contexts " + bag.getNumberOfAllContexts());
        String contextFmt = "%20s\t|%6s\t| %2s |\n";
        String msg = String.format(contextFmt, "Name", "Num", "%");
        printOut.println(msg);
        Collection<XRefResultSet.ContextScope> sortedContextScopes = XRefResultSet.sortedContextScopes(bag, false);
        for (XRefResultSet.ContextScope scope : sortedContextScopes) {
            Collection<XRefResultSet.ContextEntry> entries = bag.getEntries(scope);
            if (scope == XRefResultSet.ContextScope.UNRESOLVED) {
                if (entries.isEmpty()) {
                    continue;
                }
            }
            msg = String.format(contextFmt, scope, bag.getNumberOfContexts(scope, false), bag.getNumberOfContexts(scope, true));
            printOut.print(msg);
        }
        printOut.println("\nAnalyzed entries per scopes ");
        boolean printTitle = true;
        sortedContextScopes = XRefResultSet.sortedContextScopes(bag, true);
        for (XRefResultSet.ContextScope scope : sortedContextScopes) {
            Collection<XRefResultSet.ContextEntry> entries = bag.getEntries(scope);
            traceEntriesStatistics(scope, entries, printTitle, printOut);
            printTitle = false;
        }
        printOut.println("\nNumbers for \"first\" items approach");
        printTitle = true;
        for (XRefResultSet.ContextScope scope : sortedContextScopes) {
            Collection<XRefResultSet.ContextEntry> entries = bag.getEntries(scope);
            traceFirstItemsStatistics(scope, entries, printTitle, printOut);
            printTitle = false;
        }         
        printOut.println("\nDetails about file inclusion level");
        printTitle = true;
        for (XRefResultSet.ContextScope scope : sortedContextScopes) {
            Collection<XRefResultSet.ContextEntry> entries = bag.getEntries(scope);
            traceFileBasedEntriesStatistics(scope, entries, printTitle, printOut);
            printTitle = false;
        }
        printOut.println("\nDetails about scope of referenced declarations");
        printTitle = true;
        for (XRefResultSet.ContextScope scope : sortedContextScopes) {
            Collection<XRefResultSet.ContextEntry> entries = bag.getEntries(scope);
            traceUsedDeclarationScopeEntriesStatistics(scope, entries, printTitle, printOut);
            printTitle = false;
        }     
    }
    
    private static void traceFirstItemsStatistics(XRefResultSet.ContextScope scope, 
                                                    Collection<XRefResultSet.ContextEntry> entries, 
                                                    boolean printTitle, PrintWriter printOut) {
        String entryFmtFileInfo = "%20s\t|%10s\t|%20s\t|%20s\t|%20s\t|%20s\t|%20s\n";
        if (printTitle) {
            String title = String.format(entryFmtFileInfo, "scope name", "All", "local+cls+ns", "file+#incl-1", "local+cls+ns+#incl-1",
                    "was usages", "context+used");
            printOut.print(title);
        }
        if (scope == XRefResultSet.ContextScope.UNRESOLVED) {
            if (entries.isEmpty()) {
                return;
            }
        }

        EnumSet<IncludeLevel> nearestIncludes = EnumSet.of(XRefResultSet.IncludeLevel.THIS_FILE, XRefResultSet.IncludeLevel.PROJECT_DIRECT, XRefResultSet.IncludeLevel.LIBRARY_DIRECT);
        EnumSet<DeclarationScope> nearestScopes = EnumSet.of(
                XRefResultSet.DeclarationScope.FUNCTION_THIS, 
                XRefResultSet.DeclarationScope.CLASSIFIER_THIS, 
                XRefResultSet.DeclarationScope.CLASSIFIER_PARENT, 
                XRefResultSet.DeclarationScope.FILE_THIS, 
                XRefResultSet.DeclarationScope.NAMESPACE_THIS, 
                XRefResultSet.DeclarationScope.NAMESPACE_PARENT);
        EnumSet<DeclarationScope> nonScopes = EnumSet.noneOf(XRefResultSet.DeclarationScope.class);
        EnumSet<IncludeLevel> nonIncludes = EnumSet.noneOf(XRefResultSet.IncludeLevel.class);
        EnumSet<XRefResultSet.UsageStatistics> nonUsages = EnumSet.noneOf(XRefResultSet.UsageStatistics.class);
        EnumSet<XRefResultSet.UsageStatistics> wasUsages = EnumSet.of(XRefResultSet.UsageStatistics.SECOND_USAGE, XRefResultSet.UsageStatistics.NEXT_USAGE);
        String msg = String.format(entryFmtFileInfo, scope,
                entries.size(),
                getDeclScopeAndIncludeLevelInfo(entries, nearestScopes, nonIncludes, nonUsages),
                getDeclScopeAndIncludeLevelInfo(entries, nonScopes, nearestIncludes, nonUsages),
                getDeclScopeAndIncludeLevelInfo(entries, nearestScopes, nearestIncludes, nonUsages),
                getDeclScopeAndIncludeLevelInfo(entries, nonScopes, nonIncludes, wasUsages),
                getDeclScopeAndIncludeLevelInfo(entries, nearestScopes, nearestIncludes, wasUsages)
                );
        printOut.print(msg);        
    }
    
    private static void traceFileBasedEntriesStatistics(XRefResultSet.ContextScope scope, 
                                                    Collection<XRefResultSet.ContextEntry> entries, 
                                                    boolean printTitle, PrintWriter printOut) {
        String entryFmtFileInfo = "%20s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\n";
        if (printTitle) {
            String title = String.format(entryFmtFileInfo, "scope name", "this file", "direct \"\"", "direct <>", "project", "library", "unresolved", "All");
            printOut.print(title);
        }
        if (scope == XRefResultSet.ContextScope.UNRESOLVED) {
            if (entries.isEmpty()) {
                return;
            }
        }
        String msg = String.format(entryFmtFileInfo, scope,
                getIncludeLevelInfo(entries, XRefResultSet.IncludeLevel.THIS_FILE),
                getIncludeLevelInfo(entries, XRefResultSet.IncludeLevel.PROJECT_DIRECT),
                getIncludeLevelInfo(entries, XRefResultSet.IncludeLevel.LIBRARY_DIRECT),
                getIncludeLevelInfo(entries, XRefResultSet.IncludeLevel.PROJECT_DEEP),
                getIncludeLevelInfo(entries, XRefResultSet.IncludeLevel.LIBRARY_DEEP),
                getIncludeLevelInfo(entries, XRefResultSet.IncludeLevel.UNRESOLVED),
                entries.size());
        printOut.print(msg);
    }
    
    private static void traceUsedDeclarationScopeEntriesStatistics(XRefResultSet.ContextScope scope, 
                                                    Collection<XRefResultSet.ContextEntry> entries, 
                                                    boolean printTitle, PrintWriter printOut) {
        String entryDeclScopeInfo = "%20s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s|\n";
        if (printTitle) {
            String title = String.format(entryDeclScopeInfo, 
                    "scope name", 
//                    "All this", "All parent", "This+Parent",
                    "this fun", 
                    "this class", "parent class", "prj class", "lib class",
                    "this ns", "parent ns", "prj ns", "lib ns", 
                    "this file", "prj file", "lib file",
                    "project", "library",
                    "unresolved", "All");
            printOut.print(title);
        }
        if (scope == XRefResultSet.ContextScope.UNRESOLVED) {
            if (entries.isEmpty()) {
                return;
            }
        }
        String msg = String.format(entryDeclScopeInfo, scope,
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.FUNCTION_THIS),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.CLASSIFIER_THIS),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.CLASSIFIER_PARENT),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.PROJECT_CLASSIFIER),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.LIBRARY_CLASSIFIER),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.NAMESPACE_THIS),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.NAMESPACE_PARENT),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.PROJECT_NAMESPACE),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.LIBRARY_NAMESPACE),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.FILE_THIS),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.PROJECT_FILE),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.LIBRARY_FILE),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.PROJECT_GLOBAL),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.LIBRARY_GLOBAL),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.UNRESOLVED),
                entries.size());
        printOut.print(msg);
    }    
    
    
    private static String getDeclScopeAndIncludeLevelInfo(Collection<ContextEntry> entries,
            EnumSet<XRefResultSet.DeclarationScope> declScopes,
            EnumSet<XRefResultSet.IncludeLevel> levels,EnumSet<XRefResultSet.UsageStatistics> usages) {
        int num = 0;
        
        for (XRefResultSet.ContextEntry contextEntry : entries) {
            if (declScopes.contains(contextEntry.declarationScope) ||
                levels.contains(contextEntry.declarationIncludeLevel) ||
                usages.contains(contextEntry.usageStatistics)) {
                num++;
            }
        }
        return toRelString(num, entries.size());        
    }
    
    private static String getIncludeLevelInfo(Collection<XRefResultSet.ContextEntry> entries, XRefResultSet.IncludeLevel level) {
        int num = 0;
        for (XRefResultSet.ContextEntry contextEntry : entries) {
            if (contextEntry.declarationIncludeLevel == level) {
                num++;
            }
        }
        return toRelString(num, entries.size());
    }
    
    private static String getDeclarationScopeInfo(Collection<XRefResultSet.ContextEntry> entries, XRefResultSet.DeclarationScope declScope) {
        int num = 0;
        for (XRefResultSet.ContextEntry contextEntry : entries) {
            if (contextEntry.declarationScope == declScope) {
                num++;
            }
        }
        return toRelString(num, entries.size());
    }
    
    private static String getDeclarationKindInfo(Collection<XRefResultSet.ContextEntry> entries, XRefResultSet.DeclarationKind declKind) {
        int num = 0;
        for (XRefResultSet.ContextEntry contextEntry : entries) {
            if (contextEntry.declaration == declKind) {
                num++;
            }
        }
        return toRelString(num, entries.size());
    }
    
    private static String toRelString(int num, int size) {
        assert (size != 0) || (num == 0);
        int rel = (num == 0) ? 0 : (num *100) / size;
        return rel + "%(" + num + ")";        
    }
    
    private static void traceEntriesStatistics(XRefResultSet.ContextScope scope, 
                                                Collection<XRefResultSet.ContextEntry> entries, 
                                                boolean printTitle, PrintWriter printOut) {
        String entryFmt = "%20s\t|%10s\t|%10s\t|%10s|\n";
        if (printTitle) {
            String title = String.format(entryFmt, "Entries for scope", "Num", "Resolved", "Unresolved");
            printOut.print(title);
        }
        if (scope == XRefResultSet.ContextScope.UNRESOLVED) {
            if (entries.isEmpty()) {
                return;
            }
        }        
        int unresolved = 0;
        for (XRefResultSet.ContextEntry contextEntry : entries) {
            if (contextEntry.declaration == XRefResultSet.DeclarationKind.UNRESOLVED) {
                unresolved++;
            }
        }
        String msg = String.format(entryFmt, scope, entries.size(), (entries.size() - unresolved), unresolved);
        printOut.print(msg);
    }
    
    private static <T extends CsmObject> ObjectContext<T> createContextObject(T obj, PrintWriter printOut) {
        T     csmObject = obj;
        CsmClass      objClass = null;
        CsmFile       objFile = null;
        CsmProject    objPrj = null;
        CsmNamespace  objNs = null;
        CsmScope      objScope = null;
        // init project and file
        if (CsmKindUtilities.isOffsetable(obj)) {
            objFile = ((CsmOffsetable)obj).getContainingFile();
            assert objFile != null;
            objPrj = objFile.getProject();
        } else if (CsmKindUtilities.isNamespace(obj)) {
            objPrj = ((CsmNamespace)obj).getProject();
        } else {
            printOut.println("not handled object " + obj);
        }
        // init namespace
        objNs = CsmBaseUtilities.getObjectNamespace(obj);
        // init class
        objClass = CsmBaseUtilities.getObjectClass(obj);
        // init scope
        if (CsmKindUtilities.isEnumerator(obj)) {
            objScope = ((CsmEnumerator)obj).getEnumeration().getScope();
        } else if (CsmKindUtilities.isScopeElement(obj)) {
            objScope = ((CsmScopeElement)obj).getScope();
        }
        return new ObjectContext<T>(csmObject, objClass, objFile, objPrj, objNs, objScope);
    }
    
    private static final class ObjectContext<T extends CsmObject> {
        private final T     csmObject;
        private final CsmClass      objClass;
        private final CsmFile       objFile;
        private final CsmProject    objPrj;
        private final CsmNamespace  objNs;
        private final CsmScope      objScope;

        public ObjectContext(T csmObject, CsmClass objClass, CsmFile objFile, CsmProject objPrj, CsmNamespace objNs, CsmScope objScope) {
            this.csmObject = csmObject;
            this.objClass = objClass;
            this.objFile = objFile;
            this.objPrj = objPrj;
            this.objNs = objNs;
            this.objScope = objScope;
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("Object: ").append(csmObject);//NOI18N
            buf.append("\nFile: ").append(objFile);//NOI18N
            buf.append("\nClass: ").append(objClass);//NOI18N
            buf.append("\nNS: ").append(objNs);//NOI18N
            buf.append("\nProject: ").append(objPrj);//NOI18N
            buf.append("\nScope: ").append(objScope);//NOI18N
            return buf.toString();
        }
    }
}

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
import java.util.List;
import javax.swing.JEditorPane;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmField;
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
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.impl.services.ReferenceRepositoryImpl;
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
                CsmObject[] decDef = getDefinitionDeclaration(object);
                CsmObject decl = decDef[0];
                CsmObject def = decDef[1];                
                Collection<CsmReference> refs = xRefRepository.getReferences(decl, getProject(), true);
                if (super.isShowTime()) {
                    time = System.currentTimeMillis() - time;
                }            
                traceRefs(refs, decl, def, System.out);
                if (super.isShowTime()) {
                    System.out.println("search took " + time + "ms"); // NOI18N
                }       
                ReferenceRepositoryImpl.getDefinitionDeclaration(object);
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

    /**
     * 
     * @param target
     * @return new CsmObject[] { declaration, definion }
     */    
    public static CsmObject[] getDefinitionDeclaration(CsmObject target) {
        CsmObject[] decDef = ReferenceRepositoryImpl.getDefinitionDeclaration(target);
        return decDef;
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
    
    public static void traceProjectRefsStatistics(NativeProject prj, PrintWriter out, CsmProgressListener callback) {
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
            analyzeFile(file, bag, out);
        }
    }
    
    public static void traceRefs(Collection<CsmReference> out, CsmObject target, PrintStream streamOut) {
        assert target != null;
        CsmObject[] decDef = ReferenceRepositoryImpl.getDefinitionDeclaration(target);
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
        ReferenceRepositoryImpl.ReferenceKind kind = ReferenceRepositoryImpl.getReferenceKind(ref, targetDecl, targetDef);
        String postfix;
        if (kind == ReferenceRepositoryImpl.ReferenceKind.DECLARATION) {
            postfix = " (DECLARATION)"; // NOI18N
        } else if (kind == ReferenceRepositoryImpl.ReferenceKind.DEFINITION) {
            postfix = " (DEFINITION)"; // NOI18N
        } else {
            assert kind == ReferenceRepositoryImpl.ReferenceKind.USAGE : "unknown reference kind" + kind;
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
            bag.incrementScopeCounter(funScope);
            CsmFileReferences.getDefault().accept(
                    scope, 
                    new CsmFileReferences.Visitor() {
                        public void visit(CsmReference ref) {
                            XRefResultSet.ContextEntry entry = createEntry(ref, funContext, printOut);
                            if (entry != null) {
                                bag.addEntry(funScope, entry);
                            }
                        }
                    });
        } else {
            printOut.println("function definition without body " + fun);
        }
    }
    
    private static XRefResultSet.ContextEntry createEntry(CsmReference ref, ObjectContext<CsmFunctionDefinition> fun, PrintWriter printOut) {
        XRefResultSet.ContextEntry entry;
        CsmObject target = ref.getReferencedObject();
        if (target == null) {
            entry = XRefResultSet.ContextEntry.UNRESOLVED;
        } else {
            if (ReferenceRepositoryImpl.getReferenceKind(ref) == ReferenceRepositoryImpl.ReferenceKind.USAGE) { 
                XRefResultSet.DeclarationKind declaration = classifyDeclaration(target, printOut);
                XRefResultSet.DeclarationScope declarationScope = classifyDeclarationScopeForFunction(declaration, target, fun, printOut);
                XRefResultSet.IncludeLevel declarationIncludeLevel = classifyIncludeLevel(target, fun.objFile, printOut);
                entry = new XRefResultSet.ContextEntry(declaration, declarationScope, declarationIncludeLevel);
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
                out = file.getProject().equals(objPrj) ? XRefResultSet.IncludeLevel.PROJECT_DEEP : XRefResultSet.IncludeLevel.PROJECT_DIRECT;
            }
        }
        return out;
    }
    
//    public enum DeclarationScope {
//        UNRESOLVED,
//        PROJECT,
//        LIBRARY,
//        PROJECT_NAMESPACE,
//        LIBRARY_NAMESPACE,
//        FILE,
//        FUNCTION,
//        NAMESPACE_THIS,
//        NAMESPACE,
//        CLASSIFIER_THIS,
//        CLASSIFIER_PARENT
//    }
    
    private static XRefResultSet.DeclarationScope classifyDeclarationScopeForFunction(XRefResultSet.DeclarationKind kind, CsmObject obj, 
            ObjectContext<CsmFunctionDefinition> csmFunction, PrintWriter printOut) {
        XRefResultSet.DeclarationScope out = XRefResultSet.DeclarationScope.UNRESOLVED;
        switch (kind) {
            case NAMESPACE:
            {
                break;
            }
            case CLASSIFIER:
            case CLASS_FORWARD:
            case ENUMERATOR:
            case FUNCTION:
            {
                break;
            }
            case MACRO:
            {
                break;
            }
            case PARAMETER:
            {
                out = XRefResultSet.DeclarationScope.FUNCTION_THIS;
                break;
            }
            case VARIABLE:
            {
                if (CsmKindUtilities.isField(obj)) {
                    CsmClass objClass = ((CsmField)obj).getContainingClass();
                    if (csmFunction.objPrj.equals(objClass.getContainingFile().getProject())) {
                        out = XRefResultSet.DeclarationScope.PROJECT_CLASSIFIER;
                    } else {
                        out = XRefResultSet.DeclarationScope.LIBRARY_CLASSIFIER;
                    }
                } else if (CsmKindUtilities.isGlobalVariable(obj)) {
                    CsmScope scope = ((CsmVariable)obj).getScope();
                    if (csmFunction.objFile.equals(scope)) {
                        out = XRefResultSet.DeclarationScope.FILE_THIS;
                    } else if (CsmKindUtilities.isNamespace(scope)) {
                        CsmNamespace ns = (CsmNamespace)scope;
                        if (ns.isGlobal()) {
                            out = csmFunction.objPrj.equals(((CsmNamespace)scope).getProject()) ?
                                    XRefResultSet.DeclarationScope.PROJECT_GLOBAL :
                                    XRefResultSet.DeclarationScope.LIBRARY_GLOBAL;                            
                        } else {
                            out = csmFunction.objPrj.equals(((CsmNamespace)scope).getProject()) ?
                                    XRefResultSet.DeclarationScope.PROJECT_NAMESPACE :
                                    XRefResultSet.DeclarationScope.LIBRARY_NAMESPACE;                            
                        }
                    }
                } else {
                    out = XRefResultSet.DeclarationScope.FUNCTION_THIS;
                }
                break;
            }
            case UNRESOLVED:
            default:
                printOut.println("unhandled kind " + kind);
        }
        return out;
    }
    
    private static CsmInclude findFirstLevelInclude(CsmFile startFile, CsmFile searchFile) {
        assert startFile != null : "start file must be not null";
        assert searchFile != null : "search file must be not null";
        for (CsmInclude incl : startFile.getIncludes()) {
            if (searchFile.equals(incl.getIncludeFile())) {
                return incl;
            }
        }
        return null;
    }
    
    private static <T extends CsmObject> ObjectContext<T> createContextObject(T obj, PrintWriter printOut) {
        T     csmObject = obj;
        CsmClass      objClass = null;
        CsmFile       objFile = null;
        CsmProject    objPrj = null;
        CsmNamespace  objNs = null;
        if (CsmKindUtilities.isOffsetable(obj)) {
            objFile = ((CsmOffsetable)obj).getContainingFile();
            assert objFile != null;
            objPrj = objFile.getProject();
        } else if (CsmKindUtilities.isNamespace(obj)) {
            CsmNamespace ns = (CsmNamespace)obj;
            objPrj = ns.getProject();
            objNs = ns;
        } else {
            printOut.println("not handled object " + obj);
        }
        return new ObjectContext<T>(csmObject, objClass, objFile, objPrj, objNs);
    }
    
    private static final class ObjectContext<T extends CsmObject> {
        private final T     csmObject;
        private final CsmClass      objClass;
        private final CsmFile       objFile;
        private final CsmProject    objPrj;
        private final CsmNamespace  objNs;

        public ObjectContext(T csmObject, CsmClass objClass, CsmFile objFile, CsmProject objPrj, CsmNamespace objNs) {
            this.csmObject = csmObject;
            this.objClass = objClass;
            this.objFile = objFile;
            this.objPrj = objPrj;
            this.objNs = objNs;
        }
    }
}

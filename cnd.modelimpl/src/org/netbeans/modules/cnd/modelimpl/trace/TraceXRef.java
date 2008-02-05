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
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
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
import org.openide.util.Exceptions;


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
    
    public static void traceProjectRefsStatistics(NativeProject prj, PrintWriter out) {
        try {
            CsmProject csmPrj = CsmModelAccessor.getModel().getProject(prj);
            out.println("analyzing project " + prj.getProjectDisplayName() + "...");
            Thread.sleep(10000);
            out.println("finished");
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
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

    private static boolean isGlobalNamespace(CsmScope scope) {
        if (CsmKindUtilities.isNamespace(scope)) {
            return ((CsmNamespace)scope).isGlobal();
        }
        return false;
    }
    
    private static boolean isInlineFunction(CsmFunction fun) {
        if (fun.isInline()) {
            return true;
        }
        CsmScope outScope = fun.getScope();
        if (outScope == null || isGlobalNamespace(outScope)) {
            return false;
        } else {
            CsmFunction decl = (CsmFunction) CsmBaseUtilities.getFunctionDeclaration(fun);
            if (decl == null || !CsmKindUtilities.isMethod(fun)) {
                return false;
            } else {
                return outScope.equals(((CsmMethod)decl).getContainingClass());
            }
        }
    }
    
    private static XRefResultSet.ContextScope classifyFunctionScope(CsmFunction fun) {
        assert fun != null;
        XRefResultSet.ContextScope out = XRefResultSet.ContextScope.UNRESOLVED;
        CsmScope outScope = fun.getScope();
        if (outScope == null) {
            System.err.println("ERROR: no scope for function " + fun);
            return out;
        }
        if (CsmKindUtilities.isConstructor(fun)) {
            out = isInlineFunction(fun) ? 
                            XRefResultSet.ContextScope.INLINED_CONSTRUCTOR : 
                            XRefResultSet.ContextScope.CONSTRUCTOR;
        } else if (CsmKindUtilities.isMethod(fun)) {
            out = isInlineFunction(fun) ? 
                XRefResultSet.ContextScope.INLINED_METHOD : 
                XRefResultSet.ContextScope.METHOD;
        } else {
            if (CsmKindUtilities.isFile(outScope)) {
                out = XRefResultSet.ContextScope.FILE_LOCAL_FUNCTION;
            } else {
                CsmNamespace ns = getFunctionNamespace(fun);
                if (ns != null) {
                    out = ns.isGlobal() ? 
                            XRefResultSet.ContextScope.GLOBAL_FUNCTION :
                            XRefResultSet.ContextScope.NAMESPACE_FUNCTION;
                }
            }
        }
        if (out == XRefResultSet.ContextScope.UNRESOLVED) {
            System.err.println("ERROR: non classified function " + fun);            
        }
        return out;
    }
    
    private static CsmNamespace getFunctionNamespace(CsmFunction fun) {
        if (CsmKindUtilities.isFunctionDefinition(fun)) {
            CsmFunction decl = ((CsmFunctionDefinition) fun).getDeclaration();
            fun = decl != null ? decl : fun;
        }
        if (fun != null) {
            CsmScope scope = fun.getScope();
            if (CsmKindUtilities.isNamespace(scope)) {
                CsmNamespace ns = (CsmNamespace) scope;
                return ns;
            } else if (CsmKindUtilities.isClass(scope)) {
                return getClassNamespace((CsmClass) scope);
            }
        }
        return null;
    }

    private static CsmNamespace getClassNamespace(CsmClass cls) {
        CsmScope scope = cls.getScope();
        while (scope != null) {
            if (CsmKindUtilities.isNamespace(scope)) {
                return (CsmNamespace) scope;
            }
            if (CsmKindUtilities.isScopeElement(scope)) {
                scope = ((CsmScopeElement) scope).getScope();
            } else {
                break;
            }
        }
        return null;
    }    
}

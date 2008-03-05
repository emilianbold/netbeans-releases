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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.source.usages;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.TransTypes;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Name;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.OutputFileManager;
import org.netbeans.modules.java.source.usages.ClassIndexImpl.UsageType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class SourceAnalyser {    
    
    private final Index index;
    private final Map<Pair<String, String>, List<String>> references;
    private final Set<Pair<String,String>> toDelete;
    
    /** Creates a new instance of SourceAnalyser */
    public SourceAnalyser (final Index index) {
        assert index != null;
        this.index = index;
        this.references = new HashMap<Pair<String,String>, List<String>> ();
        this.toDelete = new HashSet<Pair<String,String>> ();
    }
    
    
    public void store () throws IOException {
        if (this.references.size() > 0 || this.toDelete.size() > 0) {
            this.index.store(this.references, toDelete);
            this.references.clear();
            this.toDelete.clear();
        }
    }
    
    public boolean isValid () throws IOException {
        return this.index.isValid(true);
    }

    public void analyse (final Iterable<? extends CompilationUnitTree> data, JavacTaskImpl jt, JavaFileManager manager, javax.tools.JavaFileObject sibling, Set<? super ElementHandle<TypeElement>> newTypes, /*out*/boolean[] mainMethod) throws IOException {
        final Map<Pair<String, String>,Map<String,Set<ClassIndexImpl.UsageType>>> usages = new HashMap<Pair<String,String>,Map<String,Set<ClassIndexImpl.UsageType>>>();
        for (CompilationUnitTree cu : data) {
            UsagesVisitor uv = new UsagesVisitor (jt, cu, manager, sibling, newTypes);
            uv.scan(cu,usages);
            mainMethod[0] |= uv.mainMethod;
            if (uv.rsList != null && uv.rsList.size()>0) {
                final int index = uv.sourceName.lastIndexOf('.');              //NOI18N
                final String pkg = index == -1 ? "" : uv.sourceName.substring(0,index);    //NOI18N
                final String rsName = (index == -1 ? uv.sourceName : uv.sourceName.substring(index+1)) + '.' + FileObjects.RS;    //NOI18N
                javax.tools.FileObject fo = manager.getFileForOutput(StandardLocation.CLASS_OUTPUT, pkg, rsName, sibling);
                assert fo != null;
                try {
                    BufferedReader in = new BufferedReader ( new InputStreamReader (fo.openInputStream(), "UTF-8"));
                    try {
                        String line;
                        while ((line = in.readLine())!=null) {
                            uv.rsList.add (line);
                        }
                    } finally {
                        in.close();
                    }
                } catch (FileNotFoundException e) {
                    //The manager.getFileForInput() should be used which returns null when file doesn't exist.
                    //but the javac API doesn't allow to specify siblink  which will not work if there are two roots
                    //with the same class name in the same wrong package.
                    //workarond: use manager.getFileForOutput() which may return non existing javac FileObject and
                    //cahch FileNotFoundException when it doens't exist, there is nothing to add into rsList
                }
                PrintWriter rsOut = new PrintWriter( new OutputStreamWriter (fo.openOutputStream(), "UTF-8"));
                try {
                    for (String sig : uv.rsList) {
                        rsOut.println(sig);
                    }
                } finally {
                    rsOut.close();
                }
            }
        }
        for (Map.Entry<Pair<String,String>,Map<String,Set<ClassIndexImpl.UsageType>>> oe : usages.entrySet()) {
            Pair<String,String> key = oe.getKey();
            Map<String,Set<ClassIndexImpl.UsageType>> oeValue = oe.getValue();            
            List<String> ru = getClassReferences (key);
            for (Map.Entry<String,Set<ClassIndexImpl.UsageType>> ue : oeValue.entrySet()) {
                ru.add (DocumentUtil.encodeUsage(ue.getKey(),ue.getValue()));
            }
        }
    }
    
    void analyseUnitAndStore (final CompilationUnitTree cu, final JavacTaskImpl jt, final JavaFileManager manager) throws IOException {
        try {
            final Map<Pair<String,String>,Map<String,Set<ClassIndexImpl.UsageType>>> usages = new HashMap<Pair<String,String>,Map<String,Set<ClassIndexImpl.UsageType>>> ();
            final List<Pair<String,String>> topLevels = new ArrayList<Pair<String,String>>();
            UsagesVisitor uv = new UsagesVisitor (jt, cu, manager, cu.getSourceFile(), topLevels);
            uv.scan(cu,usages);
            for (Map.Entry<Pair<String,String>,Map<String,Set<ClassIndexImpl.UsageType>>> oe : usages.entrySet()) {            
                Pair<String,String> key = oe.getKey();
                Map<String,Set<ClassIndexImpl.UsageType>> oeValue = oe.getValue();
                List<String> ru = getClassReferences (key);
                for (Map.Entry<String,Set<ClassIndexImpl.UsageType>> ue : oeValue.entrySet()) {
                    ru.add (DocumentUtil.encodeUsage(ue.getKey(),ue.getValue()));
                }            
            }
            this.index.store(this.references, topLevels);
        } catch (OutputFileManager.InvalidSourcePath e) {
            //Deleted project, ignore
        } finally {
            this.references.clear();
        }
    }
    
    public void delete (final Pair<String,String>name) throws IOException {
        if (!this.index.isValid(false)) {
            return;
        }
        this.toDelete.add(name);
    }
    
    public void delete (final String binaryName, final String sourceName) throws IOException {
        this.delete(Pair.<String,String>of(binaryName, sourceName));
    }
    
    
    private List<String> getClassReferences (final Pair<String,String> name) {
        assert name != null;
        List<String> result = this.references.get (name);
        if (result == null) {
            result = new LinkedList<String>();
            this.references.put(name,result);
        }        
        return result;
    }    
    
    
    private static void dumpUsages(final Map<Pair<String,String>, Map<String,Set<ClassIndexImpl.UsageType>>> usages) throws IOException {
        assert usages != null;
        for (Map.Entry<Pair<String,String>,Map<String,Set<ClassIndexImpl.UsageType>>> oe : usages.entrySet()) {
            System.out.println("Usages in class: " + oe.getKey());      // NOI18N
            for (Map.Entry<String,Set<ClassIndexImpl.UsageType>> ue : oe.getValue().entrySet()) {
                System.out.println("\t"+ue.getKey()+"\t: "+ue.getValue().toString());   // NOI18N
            }
        }
    }

    static class UsagesVisitor extends TreeScanner<Void,Map<Pair<String,String>,Map<String, Set<ClassIndexImpl.UsageType>>>> {
        
        enum State {EXTENDS, IMPLEMENTS, GT, OTHER};
        
        private final Stack<Pair<String,String>> activeClass;
        private JavaFileManager manager;
        private final JavacTaskImpl jt;
        private final Name errorName;
        private final CompilationUnitTree cu;        
        private final Types types;
        private final TransTypes trans;
        private final javax.tools.JavaFileObject sibling;
        private final String sourceName;
        private final boolean signatureFiles;
        private final List<? super Pair<String,String>> topLevels;
        private final Set<? super ElementHandle<TypeElement>> newTypes;
        private final Set<String> imports;
        private final Set<String> staticImports;
        private boolean isStaticImport;
        private State state;        
        private Element enclosingElement = null;
        private Set<String> rsList;        
        private boolean mainMethod;
        
        
        
        public UsagesVisitor (JavacTaskImpl jt, CompilationUnitTree cu, JavaFileManager manager, javax.tools.JavaFileObject sibling, Set<? super ElementHandle<TypeElement>> newTypes) {
            assert jt != null;
            assert cu != null;
            assert manager != null;
            assert sibling != null;
            this.activeClass = new Stack<Pair<String,String>> ();
            this.imports = new HashSet<String> ();
            this.staticImports = new HashSet<String> ();
            this.jt = jt;
            this.errorName = Name.Table.instance(jt.getContext()).error;
            this.state = State.OTHER;
            this.types = com.sun.tools.javac.code.Types.instance(jt.getContext());
            this.trans = TransTypes.instance(jt.getContext());
            this.cu = cu;
            this.signatureFiles = true;
            this.manager = manager;
            this.sibling = sibling;
            this.sourceName = this.manager.inferBinaryName(StandardLocation.SOURCE_PATH, this.sibling);            
            this.topLevels = null;
            this.newTypes = newTypes;            
        }
                
        protected UsagesVisitor (JavacTaskImpl jt, CompilationUnitTree cu, JavaFileManager manager, javax.tools.JavaFileObject sibling, List<? super Pair<String,String>> topLevels) {
            assert jt != null;
            assert cu != null;
            assert manager != null;
            assert sibling != null;
            
            this.activeClass = new Stack<Pair<String,String>> ();
            this.imports = new HashSet<String> ();
            this.staticImports = new HashSet<String>();
            this.jt = jt;
            this.errorName = Name.Table.instance(jt.getContext()).error;
            this.state = State.OTHER;
            this.types = com.sun.tools.javac.code.Types.instance(jt.getContext());
            this.trans = TransTypes.instance(jt.getContext());
            this.cu = cu;
            this.signatureFiles = false;
            this.manager = manager;
            this.sibling = sibling;
            this.sourceName = this.manager.inferBinaryName(StandardLocation.SOURCE_PATH, this.sibling);
            this.topLevels = topLevels;
            this.newTypes = null;
        }
        
        final Types getTypes() {
            return types;
        }
        
        final TransTypes getTransTypes () {
            return trans;
        }
        
        public @Override Void scan(Tree node, Map<Pair<String,String>, Map<String, Set<ClassIndexImpl.UsageType>>> p) {
            if (node == null) {
                return null;
            }
            super.scan (node,p);
            return null;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree node, Map<Pair<String,String>, Map<String, Set<UsageType>>> p) {
            super.visitCompilationUnit(node, p);
            if (!imports.isEmpty()) {
                //Empty file
                String className = getResourceName(node);
                if (className != null) {
                    final String classNameType = className + DocumentUtil.encodeKind(ElementKind.CLASS);                            
                    final Pair<String,String> name = Pair.<String,String>of(classNameType, null);
                    for (String s : imports) {
                        addUsage(name, s, p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                    }
                    imports.clear();
                    for (String s : staticImports) {
                        addUsage(name, s, p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                        addUsage(name, s, p, ClassIndexImpl.UsageType.METHOD_REFERENCE);
                        addUsage(name, s, p, ClassIndexImpl.UsageType.FIELD_REFERENCE);
                    }
                    staticImports.clear();
                }
            }
            return null;
        }

        public @Override Void visitMemberSelect(final MemberSelectTree node,  final Map<Pair<String,String>,Map<String, Set<ClassIndexImpl.UsageType>>> p) {
            handleVisitIdentSelect (((JCTree.JCFieldAccess)node).sym, p);
            State oldState = this.state;
            this.state = State.OTHER;
            Void ret = super.visitMemberSelect (node, p);
            this.state = oldState;
            return ret;
        }

        public @Override Void visitIdentifier(final IdentifierTree node, final Map<Pair<String,String>,Map<String, Set<ClassIndexImpl.UsageType>>> p) {
            handleVisitIdentSelect (((JCTree.JCIdent)node).sym, p);
            return super.visitIdentifier(node, p);
        }
        
        public @Override Void visitImport (final ImportTree node, final Map<Pair<String,String>,Map<String, Set<ClassIndexImpl.UsageType>>> p) {
            this.isStaticImport = node.isStatic();
            final Void ret = super.visitImport(node, p);
            this.isStaticImport = false;
            return ret;
        }
        
        private void handleVisitIdentSelect (Symbol sym, final Map<Pair<String,String>,Map<String, Set<ClassIndexImpl.UsageType>>> p) {
            if (!activeClass.empty()) {               
                if (sym != null) {
                    if (sym.kind == Kinds.ERR) {
                        final Symbol owner = sym.getEnclosingElement();
                        if (owner.getKind().isClass() || owner.getKind().isInterface()) {
                            final String className = encodeClassName(owner);
                            if (className != null) {
                                addUsage(activeClass.peek(), className, p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                            }
                        }
                    }
                    if (sym.getKind().isClass() || sym.getKind().isInterface()) {
                        final String className = encodeClassName(sym);
                        if (className != null) {
                            switch (this.state) {
                                case EXTENDS:
                                    addUsage(activeClass.peek(),className, p, ClassIndexImpl.UsageType.SUPER_CLASS);
                                    break;
                                case IMPLEMENTS:
                                    addUsage (activeClass.peek(),className,p, ClassIndexImpl.UsageType.SUPER_INTERFACE);
                                    break;
                                case OTHER:
                                case GT:
                                    addUsage (activeClass.peek(),className,p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                                    break;
                            }
                        }
                    }
                    else if (sym.getKind().isField()) {
                        final Symbol owner = sym.getEnclosingElement();
                        final String className = encodeClassName(owner);
                        if (className != null) {
                            addUsage (activeClass.peek(),className,p,ClassIndexImpl.UsageType.FIELD_REFERENCE);
                        }                        
                    }
                    else if (sym.getKind() == ElementKind.CONSTRUCTOR || sym.getKind() == ElementKind.METHOD) {
                        final Symbol owner = sym.getEnclosingElement();
                        final String className = encodeClassName(owner);
                        if (className != null) {
                            addUsage (activeClass.peek(),className,p,ClassIndexImpl.UsageType.METHOD_REFERENCE);
                        }                        
                    }
                }
            }
            else if (sym != null && (sym.getKind().isClass() || sym.getKind().isInterface())) {
                final String className = encodeClassName(sym);
                if (className != null) {
                    if (this.isStaticImport) {
                        this.staticImports.add(className);                        
                    }
                    else {
                        this.imports.add(className);
                    }
                }
            }
        }
        
        public @Override Void visitParameterizedType(ParameterizedTypeTree node, final Map<Pair<String,String>,Map<String, Set<ClassIndexImpl.UsageType>>> p) {
            scan(node.getType(), p);
            State currState = this.state;
            this.state = State.GT;
            scan(node.getTypeArguments(), p);
            this.state = currState;
            return null;
        }
        
        void dump(TypeElement clazz, String className, Element enclosingElement) {
            PrintWriter output = null;
            if (this.rsList != null) {
                this.rsList.add (className);
            }
            try {
                JavaFileObject jfo = manager.getJavaFileForOutput(StandardLocation.CLASS_OUTPUT, className, JavaFileObject.Kind.CLASS, sibling);
                
                output = new PrintWriter(new OutputStreamWriter(jfo.openOutputStream(), "UTF-8"));
                
                SymbolDumper.dump(output, types, trans, clazz, enclosingElement);
                
                output.close();
                
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            } finally {
                if (output != null) {
                    output.close();
                }
            }
        }
        
        protected boolean shouldGenerate (final String binaryName, ClassSymbol sym) {
            if (!signatureFiles || binaryName == null) {
                return false;
            }
            if  (sym.getQualifiedName().isEmpty()) {
                return true;
            }        
            Symbol enclosing = sym.getEnclosingElement();
            while (enclosing != null && enclosing.getKind() != ElementKind.PACKAGE) {
                if (!enclosing.getKind().isClass() && !enclosing.getKind().isInterface()) {
                    return true;
                }
                enclosing = enclosing.getEnclosingElement();
            }
            return false;
    }
        
        public @Override Void visitClass (final ClassTree node, final Map<Pair<String,String>,Map<String, Set<ClassIndexImpl.UsageType>>> p) {
            final ClassSymbol sym = ((JCTree.JCClassDecl)node).sym;            
            boolean errorInDecl = false;
            boolean errorIgnorSubtree = true;
            String className = null;
            if (sym != null) {
                errorInDecl = hasErrorName(sym);               
                if (errorInDecl) {
                    if (activeClass.size()>0) {
                        activeClass.push (activeClass.get(0));
                        errorIgnorSubtree = false;
                    }
                    else {
                        className = getResourceName (this.cu);                   
                        if (className != null) {
                            final String classNameType = className + DocumentUtil.encodeKind(ElementKind.CLASS);
                            final Pair<String,String> name = Pair.<String,String>of(classNameType, null);
                            if (activeClass.isEmpty()) {
                                if (topLevels != null) {
                                    topLevels.add (Pair.<String,String>of(className, null));
                                }
                                for (String s : imports) {
                                    addUsage(name, s, p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                                }
                                imports.clear();
                                for (String s : staticImports) {
                                    addUsage(name, s, p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                                    addUsage(name, s, p, ClassIndexImpl.UsageType.METHOD_REFERENCE);
                                    addUsage(name, s, p, ClassIndexImpl.UsageType.FIELD_REFERENCE);
                                }
                                staticImports.clear();
                            }
                            activeClass.push (name);
                            errorIgnorSubtree = false;
                            addUsage (name,className, p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                            if (newTypes !=null) {
                                newTypes.add ((ElementHandle<TypeElement>)ElementHandleAccessor.INSTANCE.create(ElementKind.CLASS,className));
                            }
                        }
                        else {
                            Logger.getLogger(SourceAnalyser.class.getName()).warning(String.format("Cannot resolve %s, ignoring whole subtree.\n",sym.toString()));    //NOI18N
                        }
                    }
                }
                else {
                    final StringBuilder classNameBuilder = new StringBuilder ();
                    ClassFileUtil.encodeClassName(sym, classNameBuilder, '.');  //NOI18N
                    className = classNameBuilder.toString();
                    ElementKind kind = sym.getKind();
                    classNameBuilder.append(DocumentUtil.encodeKind(kind));
                    final String classNameType = classNameBuilder.toString();
                    String resourceName = null;
                    if (activeClass.isEmpty()) {
                        if (!className.equals(sourceName)) {
                            if (signatureFiles) {
                                rsList = new HashSet<String>();
                            }
                            try {
                                FileObject fo = URLMapper.findFileObject(this.sibling.toUri().toURL());
                                if (fo != null) {
                                    ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                                    if (cp != null) {
                                        resourceName = cp.getResourceName(fo, '/', true);                                    
                                    }
                                }
                            } catch (MalformedURLException e) {
                                Exceptions.printStackTrace(e);
                            }
                        }
                    }
                    else {
                        resourceName = activeClass.peek().second;
                    }
                    final Pair<String,String> name = Pair.<String,String>of(classNameType, resourceName);
                    if (activeClass.isEmpty()) {
                        if (topLevels != null) {
                            topLevels.add (Pair.<String,String>of(className, resourceName));
                        }
                        for (String s : imports) {
                            addUsage(name, s, p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                        }
                        imports.clear();
                        for (String s : staticImports) {
                            addUsage(name, s, p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                            addUsage(name, s, p, ClassIndexImpl.UsageType.METHOD_REFERENCE);
                            addUsage(name, s, p, ClassIndexImpl.UsageType.FIELD_REFERENCE);
                        }
                        staticImports.clear();
                    }
                    activeClass.push (name);                    
                    errorIgnorSubtree = false;
                    addUsage (name,className, p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                    if (newTypes !=null) {
                        newTypes.add ((ElementHandle<TypeElement>)ElementHandleAccessor.INSTANCE.create(kind, className));
                    }
                }                
            }            
            if (!errorIgnorSubtree) {
                Element old = enclosingElement;
                try {
                    enclosingElement = sym;
                    scan(node.getModifiers(), p);
                    scan(node.getTypeParameters(), p);
                    state = errorInDecl ? State.OTHER : State.EXTENDS;
                    scan(node.getExtendsClause(), p);
                    state = errorInDecl ? State.OTHER : State.IMPLEMENTS;
                    scan(node.getImplementsClause(), p);
                    state = State.OTHER;
                    scan(node.getMembers(), p);
                    activeClass.pop();
                } finally {
                    enclosingElement = old;
                }
            }
            if (!errorInDecl && shouldGenerate(className, sym)) {
                dump(sym, className, enclosingElement);
            }
            return null;
        }
        
        public @Override Void visitNewClass(NewClassTree node, Map<Pair<String,String>,Map<String, Set<ClassIndexImpl.UsageType>>> p) {
            final Symbol sym = ((JCTree.JCNewClass)node).constructor;                        
            if (sym != null) {
                final Symbol owner = sym.getEnclosingElement();
                if (owner != null && owner.getKind().isClass()) {
                    final String className = encodeClassName(owner);
                    if (className != null) {                        
                        addUsage(activeClass.peek(),className,p,ClassIndexImpl.UsageType.METHOD_REFERENCE);
                    }
                }                
            }
            return super.visitNewClass (node,p);
        }       
        
        public @Override Void visitErroneous(final  ErroneousTree tree, Map<Pair<String,String>,Map<String, Set<ClassIndexImpl.UsageType>>> p) {
            List<? extends Tree> trees = tree.getErrorTrees();
            for (Tree t : trees) {
                this.scan(t,p);
            }
            return null;
        }

        @Override
        public Void visitMethod(MethodTree node, Map<Pair<String,String>, Map<String, Set<ClassIndexImpl.UsageType>>> p) {
            Element old = enclosingElement;
            try {
                enclosingElement = ((JCMethodDecl) node).sym;
                if (enclosingElement != null && enclosingElement.getKind() == ElementKind.METHOD) {
                    mainMethod |= SourceUtils.isMainMethod((ExecutableElement) enclosingElement);
                }
                return super.visitMethod(node, p);
            } finally {
                enclosingElement = old;
            }
        }
        
        private void addUsage (final Pair<String,String>owner, final String className, final Map<Pair<String,String>,Map<String,Set<ClassIndexImpl.UsageType>>> map, final ClassIndexImpl.UsageType type) {
            assert className != null;
            assert map != null;
            assert type != null;
            Map<String,Set<ClassIndexImpl.UsageType>> tUsages = map.get(owner);
            if (tUsages == null) {
                tUsages = new HashMap<String,Set<ClassIndexImpl.UsageType>> ();
                map.put(owner,tUsages);
            }
            Set<ClassIndexImpl.UsageType> usageType = tUsages.get (className);
            if (usageType == null) {
                usageType = EnumSet.noneOf(ClassIndexImpl.UsageType.class);
                tUsages.put (className, usageType);
            }
            usageType.add (type);        
        }                
        
        private boolean hasErrorName (Symbol cs) {
            while (cs != null) {
                if (cs.name == errorName) {
                    return true;
                }
                cs = cs.getEnclosingElement();
            }
            return false;
        }        
        
        private static String encodeClassName (final Symbol sym) {
            assert sym instanceof Symbol.ClassSymbol;
            TypeElement toEncode = null;
            final TypeMirror  type = ((Symbol.ClassSymbol)sym).asType();
            if (sym.getEnclosingElement().getKind() == ElementKind.TYPE_PARAMETER) {                
                if (type.getKind() == TypeKind.ARRAY) {
                    TypeMirror ctype = ((ArrayType) type).getComponentType();
                    if (ctype.getKind() == TypeKind.DECLARED) {
                        toEncode = (TypeElement)((DeclaredType)ctype).asElement();
                    }
                }
            }
            else {
                toEncode = (TypeElement) sym;
            }
            return toEncode == null ? null : ClassFileUtil.encodeClassName(toEncode);
        }
        
        private static String getResourceName (final CompilationUnitTree cu) {
            if (cu instanceof JCTree.JCCompilationUnit) {
                JavaFileObject jfo = ((JCTree.JCCompilationUnit)cu).sourcefile;
                if (jfo != null) {
                    URI uri = jfo.toUri();
                    if (uri != null && uri.isAbsolute()) {
                        try {
                            FileObject fo = URLMapper.findFileObject(uri.toURL());
                            if (fo != null) {
                                ClassPath cp = ClassPath.getClassPath(fo,ClassPath.SOURCE);
                                if (cp != null) {
                                    return cp.getResourceName(fo,'.',false);
                                }
                            }
                        } catch (MalformedURLException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                }
            }
            return null;
        }
    }        
    
}

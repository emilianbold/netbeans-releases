/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.java.source.usages;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Name;
import java.io.BufferedReader;
import java.io.IOException;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class SourceAnalyser {    
    
    private final Index index;
    private final Map<String, List<String>> references;
    private final Set<String> toDelete;
    
    /** Creates a new instance of SourceAnalyser */
    public SourceAnalyser (final Index index) {
        assert index != null;
        this.index = index;
        this.references = new HashMap<String, List<String>> ();
        this.toDelete = new HashSet<String> ();
    }
    
    
    public void store () throws IOException {
        if (this.references.size() > 0 || this.toDelete.size() > 0) {
            this.index.store(this.references, toDelete);
            this.references.clear();
            this.toDelete.clear();
        }
    }

    public void analyse (final Iterable<? extends CompilationUnitTree> data, JavacTaskImpl jt, JavaFileManager manager, javax.tools.JavaFileObject sibling) throws IOException {
        final Map<String,Map<String,Set<ClassIndexImpl.UsageType>>> usages = new HashMap<String,Map<String,Set<ClassIndexImpl.UsageType>>> ();
        for (CompilationUnitTree cu : data) {
            UsagesVisitor uv = new UsagesVisitor (jt, cu, manager, sibling);
            uv.scan(cu,usages);
            if (uv.rsList != null && uv.rsList.size()>0) {
                final int index = uv.sourceName.lastIndexOf('.');              //NOI18N
                final String pkg = index == -1 ? "" : uv.sourceName.substring(0,index);    //NOI18N
                final String rsName = (index == -1 ? uv.sourceName : uv.sourceName.substring(index+1)) + '.' + FileObjects.RS;    //NOI18N
                javax.tools.FileObject fo = manager.getFileForOutput(StandardLocation.CLASS_OUTPUT, pkg, rsName, sibling);
                assert fo != null;
                BufferedReader in = new BufferedReader (fo.openReader(false));
                try {
                    String line;
                    while ((line = in.readLine())!=null) {
                        uv.rsList.add (line);
                    }
                } finally {
                    in.close();
                }
                PrintWriter rsOut = new PrintWriter(fo.openWriter());
                try {
                    for (String sig : uv.rsList) {
                        rsOut.println(sig);
                    }
                } finally {
                    rsOut.close();
                }
            }
        }
        for (Map.Entry<String,Map<String,Set<ClassIndexImpl.UsageType>>> oe : usages.entrySet()) {            
            List<String> ru = getClassReferences (oe.getKey());
            Map<String,Set<ClassIndexImpl.UsageType>> oeValue = oe.getValue();
            for (Map.Entry<String,Set<ClassIndexImpl.UsageType>> ue : oeValue.entrySet()) {
                ru.add (DocumentUtil.encodeUsage(ue.getKey(),ue.getValue()));
            }
        }
    }
    
    void analyseUnitAndStore (final CompilationUnitTree cu, final JavacTaskImpl jt) throws IOException {
        try {
            final Map<String,Map<String,Set<ClassIndexImpl.UsageType>>> usages = new HashMap<String,Map<String,Set<ClassIndexImpl.UsageType>>> ();
            List<String> topLevels = new ArrayList<String>();
            UsagesVisitor uv = new UsagesVisitor (jt, cu, topLevels);
            uv.scan(cu,usages);
            for (Map.Entry<String,Map<String,Set<ClassIndexImpl.UsageType>>> oe : usages.entrySet()) {            
                String className = oe.getKey();
                List<String> ru = getClassReferences (className);
                Map<String,Set<ClassIndexImpl.UsageType>> oeValue = oe.getValue();
                for (Map.Entry<String,Set<ClassIndexImpl.UsageType>> ue : oeValue.entrySet()) {
                    ru.add (DocumentUtil.encodeUsage(ue.getKey(),ue.getValue()));
                }            
            }
            this.index.store(this.references, topLevels);
        } finally {
            this.references.clear();
        }
    }
    
    public void delete (final String className) throws IOException {
        if (!this.index.isValid()) {
            return;
        }
        this.toDelete.add(className);
    }
    
    
    private List<String> getClassReferences (final String className) {
        assert className != null;
        List<String> result = this.references.get (className);
        if (result == null) {
            result = new LinkedList<String>();
            this.references.put(className,result);
        }
        return result;
    }    
    
    
    private static void dumpUsages(final Map<String,Map<String,Set<ClassIndexImpl.UsageType>>> usages) throws IOException {
        assert usages != null;
        for (Map.Entry<String,Map<String,Set<ClassIndexImpl.UsageType>>> oe : usages.entrySet()) {
            System.out.println("Usages in class: " + oe.getKey());      // NOI18N
            for (Map.Entry<String,Set<ClassIndexImpl.UsageType>> ue : oe.getValue().entrySet()) {
                System.out.println("\t"+ue.getKey()+"\t: "+ue.getValue().toString());   // NOI18N
            }
        }
    }

    static class UsagesVisitor extends TreeScanner<Void,Map<String,Map<String, Set<ClassIndexImpl.UsageType>>>> {
        
        enum State {EXTENDS, IMPLEMENTS, GT, OTHER};
        
        private final Stack<String> activeClass;
        private JavaFileManager manager;
        private final JavacTaskImpl jt;
        private final Name errorName;
        private final CompilationUnitTree cu;        
        private final Types types;
        private final javax.tools.JavaFileObject sibling;
        private final String sourceName;
        private final boolean signatureFiles;
        private State state;        
        private Element enclosingElement = null;
        private Set<String> rsList;
        private List<? super String> topLevels;
        
        
        public UsagesVisitor (JavacTaskImpl jt, CompilationUnitTree cu, JavaFileManager manager, javax.tools.JavaFileObject sibling) {
            assert jt != null;
            assert cu != null;
            assert manager != null;
            assert sibling != null;
            this.activeClass = new Stack<String> ();
            this.jt = jt;
            this.errorName = Name.Table.instance(jt.getContext()).error;
            this.state = State.OTHER;
            this.types = com.sun.tools.javac.code.Types.instance(jt.getContext());
            this.cu = cu;
            this.signatureFiles = true;
            this.manager = manager;
            this.sibling = sibling;
            this.sourceName = this.manager.inferBinaryName(StandardLocation.SOURCE_PATH, this.sibling);            
        }
                
        protected UsagesVisitor (JavacTaskImpl jt, CompilationUnitTree cu, List<? super String> topLevels) {
            assert jt != null;
            assert cu != null;           
            
            this.activeClass = new Stack<String> ();
            this.jt = jt;
            this.errorName = Name.Table.instance(jt.getContext()).error;
            this.state = State.OTHER;
            this.types = com.sun.tools.javac.code.Types.instance(jt.getContext());
            this.cu = cu;
            this.signatureFiles = false;
            this.manager = null;
            this.sibling = null;
            this.sourceName = "";   //NOI18N
            this.topLevels = topLevels;
        }
        
        final Types getTypes() {
            return types;
        }
        
        public @Override Void scan(Tree node, Map<String,Map<String, Set<ClassIndexImpl.UsageType>>> p) {
            if (node == null) {
                return null;
            }
            super.scan (node,p);
            return null;
        }        
        
        public @Override Void visitMemberSelect(final MemberSelectTree node,  final Map<String,Map<String, Set<ClassIndexImpl.UsageType>>> p) {
            handleVisitIdentSelect (((JCTree.JCFieldAccess)node).sym, p);
            State oldState = this.state;
            this.state = State.OTHER;
            Void ret = super.visitMemberSelect (node, p);
            this.state = oldState;
            return ret;
        }

        public @Override Void visitIdentifier(final IdentifierTree node, final Map<String,Map<String, Set<ClassIndexImpl.UsageType>>> p) {
            handleVisitIdentSelect (((JCTree.JCIdent)node).sym, p);
            return super.visitIdentifier(node, p);
        }
        
        private void handleVisitIdentSelect (Symbol sym, final Map<String,Map<String, Set<ClassIndexImpl.UsageType>>> p) {
            if (!activeClass.empty()) {               
                if (sym != null) {
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
        }
        
        public @Override Void visitParameterizedType(ParameterizedTypeTree node, final Map<String,Map<String, Set<ClassIndexImpl.UsageType>>> p) {
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
                
                SymbolDumper.dump(output, types, clazz, enclosingElement);
                
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
        
        public @Override Void visitClass (final ClassTree node, final Map<String,Map<String, Set<ClassIndexImpl.UsageType>>> p) {
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
                        if (this.cu instanceof JCTree.JCCompilationUnit) {
                            JavaFileObject jfo = ((JCTree.JCCompilationUnit)this.cu).sourcefile;
                            if (jfo != null) {
                                URI uri = jfo.toUri();
                                if (uri != null && uri.isAbsolute()) {
                                    try {
                                        FileObject fo = URLMapper.findFileObject(uri.toURL());
                                        if (fo != null) {
                                            ClassPath cp = ClassPath.getClassPath(fo,ClassPath.SOURCE);
                                            if (cp != null) {
                                                className = cp.getResourceName(fo,'.',false);
                                            }
                                        }
                                    } catch (MalformedURLException e) {
                                        Exceptions.printStackTrace(e);
                                    }
                                }
                            }
                        }                   
                        if (className != null) {
                            final String classNameType = className + DocumentUtil.encodeKind(ElementKind.CLASS);
                            if (topLevels != null && activeClass.isEmpty()) {
                                topLevels.add (className);
                            }
                            activeClass.push (classNameType);
                            errorIgnorSubtree = false;
                            addUsage (classNameType,className, p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                        }
                        else {
                            Logger.getLogger("global").warning(String.format("Cannot resolve %s, ignoring whole subtree.\n",sym.toString()));    //NOI18N
                        }
                    }
                }
                else {
                    
                    final StringBuilder classNameBuilder = new StringBuilder ();
                    ClassFileUtil.encodeClassName(sym, classNameBuilder, '.');  //NOI18N
                    className = classNameBuilder.toString();
                    classNameBuilder.append(DocumentUtil.encodeKind(sym.getKind()));
                    final String classNameType = classNameBuilder.toString();                                        
                    if (signatureFiles && activeClass.isEmpty() && !className.equals(sourceName)) {
                        rsList = new HashSet<String>();
                    }
                    if (topLevels != null && activeClass.isEmpty()) {
                        topLevels.add (className);
                    }
                    activeClass.push (classNameType);                    
                    errorIgnorSubtree = false;
                    addUsage (classNameType,className, p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
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
        
        public @Override Void visitNewClass(NewClassTree node, Map<String,Map<String, Set<ClassIndexImpl.UsageType>>> p) {
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
        
        public @Override Void visitErroneous(final  ErroneousTree tree, Map<String,Map<String, Set<ClassIndexImpl.UsageType>>> p) {
            List<? extends Tree> trees = tree.getErrorTrees();
            for (Tree t : trees) {
                this.scan(t,p);
            }
            return null;
        }

        public Void visitMethod(MethodTree node, Map<String, Map<String, Set<ClassIndexImpl.UsageType>>> p) {
            Element old = enclosingElement;
            try {
                enclosingElement = ((JCMethodDecl) node).sym;
                return super.visitMethod(node, p);
            } finally {
                enclosingElement = old;
            }
        }
        
        private void addUsage (final String ownerName, final String className, final Map<String,Map<String,Set<ClassIndexImpl.UsageType>>> map, final ClassIndexImpl.UsageType type) {
            assert className != null;
            assert map != null;
            assert type != null;
            Map<String,Set<ClassIndexImpl.UsageType>> tUsages = map.get(ownerName);
            if (tUsages == null) {
                tUsages = new HashMap<String,Set<ClassIndexImpl.UsageType>> ();
                map.put(ownerName,tUsages);
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
    }        
    
}

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
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.TransTypes;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.OutputFileManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class SourceAnalyser {    
    
    private final Index index;
    private final Map<Pair<String, String>, Object[]> references;
    private final Set<Pair<String,String>> toDelete;
    private static final boolean fullIndex = Boolean.getBoolean(SourceAnalyser.class.getName()+".fullIndex");   //NOI18N
    
    /** Creates a new instance of SourceAnalyser */
    public SourceAnalyser (final Index index) {
        assert index != null;
        this.index = index;
        this.references = new HashMap<Pair<String,String>, Object[]> ();
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
    
    public void analyse (final Iterable<? extends CompilationUnitTree> data, JavacTaskImpl jt, JavaFileManager manager,
        final CompileTuple tuple,
        Set<? super ElementHandle<TypeElement>> newTypes, /*out*/boolean[] mainMethod) throws IOException {
        final Map<Pair<String, String>,Data> usages = new HashMap<Pair<String,String>,Data>();
        for (CompilationUnitTree cu : data) {
            UsagesVisitor uv = new UsagesVisitor (jt, cu, manager, tuple.jfo, newTypes,
                    tuple);
            uv.scan(cu,usages);
            mainMethod[0] |= uv.mainMethod;
            if (uv.sourceName != null && uv.rsList != null && uv.rsList.size()>0) {
                final int index = uv.sourceName.lastIndexOf('.');              //NOI18N
                final String pkg = index == -1 ? "" : uv.sourceName.substring(0,index);    //NOI18N
                final String simpleName = index == -1 ? uv.sourceName : uv.sourceName.substring(index+1);
                String ext;
                if (tuple.virtual) {
                    ext = FileObjects.getExtension(tuple.indexable.getURL().getPath()) +'.'+ FileObjects.RX;    //NOI18N
                }
                else {
                    ext = FileObjects.RS;
                }
                final String rsName = simpleName + '.' + ext;   //NOI18N
                javax.tools.FileObject fo = manager.getFileForOutput(StandardLocation.CLASS_OUTPUT, pkg, rsName, tuple.jfo);
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
        //Ideally not even usegas will be calculated but it will propagate the storeIndex
        //through the UsagesVisitor
        if (tuple.index) {
            for (Map.Entry<Pair<String,String>,Data> oe : usages.entrySet()) {
                final Pair<String,String> key = oe.getKey();
                final Data value = oe.getValue();            
                addClassReferences (key,value);
            }
        }
    }
    
    void analyseUnitAndStore (final CompilationUnitTree cu, final JavacTaskImpl jt, final JavaFileManager manager) throws IOException {
        try {
            final Map<Pair<String,String>,Data> usages = new HashMap<Pair<String,String>,Data> ();
            final List<Pair<String,String>> topLevels = new ArrayList<Pair<String,String>>();
            UsagesVisitor uv = new UsagesVisitor (jt, cu, manager, cu.getSourceFile(), topLevels);
            uv.scan(cu,usages);
            for (Map.Entry<Pair<String,String>,Data> oe : usages.entrySet()) {            
                final Pair<String,String> key = oe.getKey();
                final Data data = oe.getValue();                
                addClassReferences (key,data);                            
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
    
    
    private void addClassReferences (final Pair<String,String> name, final Data data) {
        assert name != null;
        assert data != null;
        final Object[] result = new Object[3];
        final Map<String,Set<ClassIndexImpl.UsageType>> usages = data.usages;
        final Set<CharSequence> fids = data.featuresIdents;
        final Set<CharSequence> ids = data.idents;
        final List<String> ru = new LinkedList<String>();
        for (Map.Entry<String,Set<ClassIndexImpl.UsageType>> ue : usages.entrySet()) {
            ru.add (DocumentUtil.encodeUsage(ue.getKey(),ue.getValue()));
        }
        final StringBuilder fidents = new StringBuilder();
        for (CharSequence id : fids) {
            fidents.append(id);
            fidents.append(' '); //NOI18N
        }
        final StringBuilder idents = new StringBuilder();
        for (CharSequence id : ids) {
            idents.append(id);
            idents.append(' '); //NOI18N
        }
        result[0] = ru;
        result[1] = fidents.toString();
        result[2] = idents.toString();
        this.references.put(name,result);
    }    
    
    
    private static void dumpUsages(final Map<Pair<String,String>, Data> usages) throws IOException {
        assert usages != null;
        for (Map.Entry<Pair<String,String>,Data> oe : usages.entrySet()) {
            System.out.println("Usages in class: " + oe.getKey());      // NOI18N
            for (Map.Entry<String,Set<ClassIndexImpl.UsageType>> ue : oe.getValue().usages.entrySet()) {
                System.out.println("\t"+ue.getKey()+"\t: "+ue.getValue().toString());   // NOI18N
            }
            System.out.println("Feature idents in class: " + oe.getKey());      // NOI18N
            for (CharSequence s : oe.getValue().featuresIdents) {
                System.out.println("\t"+s);   // NOI18N
            }
            System.out.println("All idents in class: " + oe.getKey());      // NOI18N
            for (CharSequence s : oe.getValue().idents) {
                System.out.println("\t"+s);   // NOI18N
            }
        }
    }
    
    static class Data {
        final Map<String, Set<ClassIndexImpl.UsageType>> usages = new HashMap<String, Set<ClassIndexImpl.UsageType>>();
        final Set<CharSequence> featuresIdents = new HashSet<CharSequence>();
        final Set<CharSequence> idents = new HashSet<CharSequence>();
    }

    static class UsagesVisitor extends TreeScanner<Void,Map<Pair<String,String>,Data>> {
        
        enum State {EXTENDS, IMPLEMENTS, GT, OTHER};
        
        private final Stack<Pair<String,String>> activeClass;
        private JavaFileManager manager;
        private final JavacTaskImpl jt;
        private final Name errorName;
        private final CompilationUnitTree cu;        
        private final Types types;
        private final TransTypes trans;
        private final URL siblingUrl;
        private final String sourceName;
        private final boolean signatureFiles;
        private final List<? super Pair<String,String>> topLevels;
        private final Set<? super ElementHandle<TypeElement>> newTypes;
        private final Set<String> imports;
        private final Set<String> staticImports;
        private final Set<CharSequence> importIdents;
        private final boolean virtual;
        private final boolean storeIndex;
        private boolean isStaticImport;
        private State state;        
        private Element enclosingElement = null;
        private Set<String> rsList;         //List of references from source in case when the source has more top levels or is wrongly packaged
        private boolean crossedTopLevel;    //True when the visitor already reached the correctly packaged top level
        private boolean mainMethod;
        
        
        
        public UsagesVisitor (JavacTaskImpl jt, CompilationUnitTree cu, JavaFileManager manager, javax.tools.JavaFileObject sibling, Set<? super ElementHandle<TypeElement>> newTypes,
                final CompileTuple tuple) throws MalformedURLException {
            assert jt != null;
            assert cu != null;
            assert manager != null;
            assert sibling != null;
            this.activeClass = new Stack<Pair<String,String>> ();
            this.imports = new HashSet<String> ();
            this.staticImports = new HashSet<String> ();
            this.importIdents = new HashSet<CharSequence>();
            this.jt = jt;
            this.errorName = Names.instance(jt.getContext()).error;
            this.state = State.OTHER;
            this.types = com.sun.tools.javac.code.Types.instance(jt.getContext());
            this.trans = TransTypes.instance(jt.getContext());
            this.cu = cu;
            this.signatureFiles = true;
            this.manager = manager;
            this.virtual = tuple.virtual;
            this.storeIndex = tuple.index;
            this.siblingUrl = virtual ? tuple.indexable.getURL() : sibling.toUri().toURL();
            this.sourceName = this.manager.inferBinaryName(StandardLocation.SOURCE_PATH, sibling);            
            this.topLevels = null;
            this.newTypes = newTypes;            
        }
                
        protected UsagesVisitor (JavacTaskImpl jt, CompilationUnitTree cu, JavaFileManager manager, javax.tools.JavaFileObject sibling, List<? super Pair<String,String>> topLevels) throws MalformedURLException {
            assert jt != null;
            assert cu != null;
            assert manager != null;
            assert sibling != null;
            
            this.activeClass = new Stack<Pair<String,String>> ();
            this.imports = new HashSet<String> ();
            this.staticImports = new HashSet<String>();
            this.importIdents = new HashSet<CharSequence>();
            this.jt = jt;
            this.errorName = Names.instance(jt.getContext()).error;
            this.state = State.OTHER;
            this.types = com.sun.tools.javac.code.Types.instance(jt.getContext());
            this.trans = TransTypes.instance(jt.getContext());
            this.cu = cu;
            this.signatureFiles = false;
            this.manager = manager;
            this.siblingUrl = sibling.toUri().toURL();
            this.sourceName = this.manager.inferBinaryName(StandardLocation.SOURCE_PATH, sibling);
            this.topLevels = topLevels;
            this.newTypes = null;
            this.virtual = false;
            this.storeIndex = true;
        }
        
        final Types getTypes() {
            return types;
        }
        
        final TransTypes getTransTypes () {
            return trans;
        }
        
        public @Override Void scan(Tree node, Map<Pair<String,String>, Data> p) {
            if (node == null) {
                return null;
            }
            super.scan (node,p);
            return null;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree node, Map<Pair<String,String>, Data> p) {
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
                    for (CharSequence s : importIdents) {
                        addIdent(name, s, p, false);
                    }
                    importIdents.clear();
                }
            }
            return null;
        }

        public @Override Void visitMemberSelect(final MemberSelectTree node,  final Map<Pair<String,String>, Data> p) {
            handleVisitIdentSelect (((JCTree.JCFieldAccess)node).sym, node.getIdentifier(), p);
            State oldState = this.state;
            this.state = State.OTHER;
            Void ret = super.visitMemberSelect (node, p);
            this.state = oldState;
            return ret;
        }

        public @Override Void visitIdentifier(final IdentifierTree node, final Map<Pair<String,String>, Data> p) {
            handleVisitIdentSelect (((JCTree.JCIdent)node).sym, node.getName(), p);
            return super.visitIdentifier(node, p);
        }
        
        public @Override Void visitImport (final ImportTree node, final Map<Pair<String,String>, Data> p) {
            this.isStaticImport = node.isStatic();
            final Void ret = super.visitImport(node, p);
            this.isStaticImport = false;
            return ret;
        }
                                        
        private void handleVisitIdentSelect (final Symbol sym, final CharSequence name, final Map<Pair<String,String>, Data> p) {
            if (!activeClass.empty()) {
                addIdent(activeClass.peek(), name, p, false);
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
                        if (owner.getKind().isClass() || owner.getKind().isInterface()) {
                            final String className = encodeClassName(owner);
                            if (className != null) {
                                addUsage (activeClass.peek(),className,p,ClassIndexImpl.UsageType.FIELD_REFERENCE);
                            }
                        }
                    }
                    else if (sym.getKind() == ElementKind.CONSTRUCTOR || sym.getKind() == ElementKind.METHOD) {
                        final Symbol owner = sym.getEnclosingElement();
                        if (owner.getKind().isClass() || owner.getKind().isInterface()) {
                            final String className = encodeClassName(owner);
                            if (className != null) {
                                addUsage (activeClass.peek(),className,p,ClassIndexImpl.UsageType.METHOD_REFERENCE);
                            }
                        }
                    }
                }
            }
            else {
                importIdents.add(name);
                if (sym != null && (sym.getKind().isClass() || sym.getKind().isInterface())) {            
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
        }
        
        public @Override Void visitParameterizedType(ParameterizedTypeTree node, final Map<Pair<String,String>, Data> p) {
            scan(node.getType(), p);
            State currState = this.state;
            this.state = State.GT;
            scan(node.getTypeArguments(), p);
            this.state = currState;
            return null;
        }
        
        public @Override Void visitClass (final ClassTree node, final Map<Pair<String,String>,Data> p) {
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
                                for (CharSequence s : importIdents) {
                                    addIdent(name, s, p, false);
                                }
                                importIdents.clear();
                            }
                            activeClass.push (name);
                            errorIgnorSubtree = false;
                            addUsage (name,className, p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                            addIdent(name, className, p, true);
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
                        if (virtual || !className.equals(sourceName)) {
                            if (signatureFiles && rsList == null) {
                                rsList = new HashSet<String>();
                                if (crossedTopLevel) {
                                    rsList.add(sourceName);
                                }
                            }
                            final FileObject fo = URLMapper.findFileObject(this.siblingUrl);
                            if (fo != null) {
                                final ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                                if (cp != null) {
                                    resourceName = cp.getResourceName(fo, '/', true);   //NOI18N
                                }
                            }
                        }
                        else {
                            crossedTopLevel = true;
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
                        for (CharSequence s : importIdents) {
                            addIdent(name, s, p, false);
                        }
                        importIdents.clear();
                    }
                    activeClass.push (name);                    
                    errorIgnorSubtree = false;
                    addUsage (name,className, p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                    addIdent(name, node.getSimpleName(), p, true);
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
            if (!errorInDecl) {
                if (this.rsList != null)
                    this.rsList.add (className);
            }
            return null;
        }
        
        public @Override Void visitNewClass(NewClassTree node, Map<Pair<String,String>,Data> p) {
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
        
        public @Override Void visitErroneous(final  ErroneousTree tree, Map<Pair<String,String>,Data> p) {
            List<? extends Tree> trees = tree.getErrorTrees();
            for (Tree t : trees) {
                this.scan(t,p);
            }
            return null;
        }

        @Override
        public Void visitMethod(MethodTree node, Map<Pair<String,String>, Data> p) {
            Element old = enclosingElement;
            try {
                enclosingElement = ((JCMethodDecl) node).sym;
                if (enclosingElement != null && enclosingElement.getKind() == ElementKind.METHOD) {
                    mainMethod |= SourceUtils.isMainMethod((ExecutableElement) enclosingElement);
                }
                addIdent(activeClass.peek(), node.getName(), p, true);
                return super.visitMethod(node, p);
            } finally {
                enclosingElement = old;
            }
        }

        @Override
        public Void visitVariable(VariableTree node, Map<Pair<String, String>, Data> p) {
            
            Symbol s = ((JCTree.JCVariableDecl)node).sym;
            if (s != null && s.owner != null && (s.owner.getKind().isClass() || s.owner.getKind().isInterface())) {
                addIdent(activeClass.peek(), node.getName(), p, true);
            }
            return super.visitVariable(node, p);
        }
                
        
        private void addUsage (final Pair<String,String>owner, final String className, final Map<Pair<String,String>,Data> map, final ClassIndexImpl.UsageType type) {
            assert className != null;
            assert map != null;
            assert type != null;
            final Data data = getData(owner, map);
            Set<ClassIndexImpl.UsageType> usageType = data.usages.get (className);
            if (usageType == null) {
                usageType = EnumSet.noneOf(ClassIndexImpl.UsageType.class);
                data.usages.put (className, usageType);
            }
            usageType.add (type);        
        }
        
        private void addIdent (final Pair<String,String>owner, final CharSequence ident, final Map<Pair<String,String>,Data> map, final boolean feature) {
            assert owner != null;
            assert ident != null;
            assert map != null;
            if (feature || fullIndex) {
                final Data data = getData(owner, map);
                if (fullIndex) {
                    data.idents.add(ident);
                }
                if (feature) {                
                    data.featuresIdents.add(ident);
                }
            }            
            
        }
        
        private Data getData (final Pair<String,String>owner, final Map<Pair<String,String>, Data> map) {
            Data data = map.get(owner);
            if (data == null) {
                data = new Data ();
                map.put(owner,data);
            }
            return data;
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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.usages;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
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
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.OutputFileManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * Factory for creating java source analyzer.
 * @author Tomas Zezula
 */
public final class SourceAnalyzerFactory {
    
    private static final boolean fullIndex = Boolean.getBoolean("org.netbeans.modules.java.source.usages.SourceAnalyser.fullIndex");   //NOI18N
    private static final Logger LOG = Logger.getLogger(SourceAnalyzerFactory.class.getName());
    
    private SourceAnalyzerFactory() {}
    
    /**
     * Creates a {@link ClassIndexImpl.Writer} backed {@link StorableAnalyzer}.
     * The created analyzer stores and deletes data from given {@link ClassIndexImpl.Writer}
     * @param {@link ClassIndexImpl.Writer} the backing store
     * @return the java source file analyzer
     */
    public static StorableAnalyzer createStorableAnalyzer(@NonNull final ClassIndexImpl.Writer writer) {
        return new StorableAnalyzer(writer);
    }
    
    /**
     * Creates a {@link SimpleAnalyzer} which can be used to analyze single compilation unit.
     * @return the java source file analyzer
     */
    public static SimpleAnalyzer createSimpleAnalyzer() {
        return new SimpleAnalyzer();
    }
    
    /**
     * Java source file analyzer which operates on backing store.
     * The returned analyzer can be used several times for delete
     * and analyze. The changes are propagated into backing store
     * by {@link StorableAnalyzer#store()} method.
     */
    public static final class StorableAnalyzer extends BaseAnalyzer {
        private final ClassIndexImpl.Writer writer;
        
        private StorableAnalyzer(@NonNull final ClassIndexImpl.Writer writer) {
            Parameters.notNull("writer", writer);   //NOI18N
            this.writer = writer;
        }
        
        /**
         * Analyzes given compilation units.
         * @param data the java compilation units to be analyzed
         * @param jt the {@link JavacTaskImpl} providing the context
         * @param manager the {@link JavaFileManager} used to infer binary names
         * @param tuple the {@link JavaCustomIndexer.CompileTuple} providing file info
         * @param newTypes holder for new types
         * @param mainMethod holder for main class flag, set to true if class has a main method
         * @throws IOException in case of IO error
         */
        public void analyse (final Iterable<? extends CompilationUnitTree> data, JavacTaskImpl jt, JavaFileManager manager,
            final JavaCustomIndexer.CompileTuple tuple,
            Set<? super ElementHandle<TypeElement>> newTypes, /*out*/boolean[] mainMethod) throws IOException {
            final Map<Pair<String, String>,Data> usages = new HashMap<Pair<String,String>,Data>();
            for (CompilationUnitTree cu : data) {
                try {
                    UsagesVisitor uv = new UsagesVisitor (jt, cu, manager, tuple.jfo, newTypes, tuple);
                    uv.scan(cu,usages);
                    mainMethod[0] |= uv.mainMethod;
                    if (uv.rsList != null && uv.rsList.size()>0) {
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
                } catch (IllegalArgumentException iae) {
                    Exceptions.printStackTrace(iae);
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
        
        /**
         * Deletes given class from backing store.
         * @param name the name of the class encoded by tuple {fqn, relative_source_path_or_null}
         * @throws IOException in case of IO error
         */
        public void delete (final Pair<String,String>name) throws IOException {
            this.toDelete.add(name);
        }
        
        /**
         * Propagates results of analyzes into backing store.
         * @throws IOException in case of IO error.
         */
        public void store () throws IOException {
            if (this.references.size() > 0 || this.toDelete.size() > 0) {
                try {
                    this.writer.deleteAndFlush(this.references, toDelete);
                } finally {
                    this.references.clear();
                    this.toDelete.clear();
                }
            }
        }
    }
    
    /**
     * Java source file analyzer for analyzing single compilation unit.
     */
    public static final class SimpleAnalyzer extends BaseAnalyzer {
        private boolean used;
        
        /**
         * Analyzes given compilation unit and returns the result of analyzes.
         * @param cu the java compilation unit to be analyzed
         * @param jt the {@link JavacTaskImpl} providing the context
         * @param manager the {@link JavaFileManager} used to infer binary names
         * @return the result of analyzes encoded as list of tuples {{fqn,relative_source_path_or_null},usages_data}
         * @throws IOException in case of IO error
         */
        @CheckForNull
        public List<Pair<Pair<String, String>, Object[]>> analyseUnit (final CompilationUnitTree cu, final JavacTaskImpl jt, final JavaFileManager manager) throws IOException {
            if (used) {
                throw new IllegalStateException("Trying to reuse SimpleAnalyzer");  //NOI18N
            }
            used = true;
            try {
                final Map<Pair<String,String>,Data> usages = new HashMap<Pair<String,String>,Data> ();
                final Set<Pair<String,String>> topLevels = new HashSet<Pair<String,String>>();
                final UsagesVisitor uv = new UsagesVisitor (jt, cu, manager, cu.getSourceFile(), topLevels);
                uv.scan(cu,usages);
                for (Map.Entry<Pair<String,String>,Data> oe : usages.entrySet()) {
                    final Pair<String,String> key = oe.getKey();
                    final Data data = oe.getValue();
                    addClassReferences (key,data);
                }
                //this.writer.deleteEnclosedAndStore(this.references, topLevels);
                return this.references;
            } catch (IllegalArgumentException iae) {
                Exceptions.printStackTrace(iae);
                return null;
            }catch (OutputFileManager.InvalidSourcePath e) {
                return null;
            }
        }
    }
    
    private static class BaseAnalyzer {
        protected final List<Pair<Pair<String, String>, Object[]>> references = new ArrayList<Pair<Pair<String, String>, Object[]>>();
        protected final Set<Pair<String,String>> toDelete = new HashSet<Pair<String,String>> ();
        
        protected final void addClassReferences (final Pair<String,String> name, final Data data) {
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
            this.references.add(Pair.<Pair<String,String>,Object[]>of(name,result));
        }
    }

    private static class Data {
        final Map<String, Set<ClassIndexImpl.UsageType>> usages = new HashMap<String, Set<ClassIndexImpl.UsageType>>();
        final Set<CharSequence> featuresIdents = new HashSet<CharSequence>();
        final Set<CharSequence> idents = new HashSet<CharSequence>();
    }
    
    private static class UsagesVisitor extends TreeScanner<Void,Map<Pair<String,String>,Data>> {

        enum State {EXTENDS, IMPLEMENTS, GT, OTHER, IMPORT, PACKAGE_ANN};

        private final Stack<Pair<String,String>> activeClass;
        private final Name errorName;
        private final Name pkgImportName;
        private final CompilationUnitTree cu;
        private final URL siblingUrl;
        private final String sourceName;
        private final boolean signatureFiles;
        private final Set<? super Pair<String,String>> topLevels;
        private final Set<? super ElementHandle<TypeElement>> newTypes;
        private final Set<Symbol> imports;
        private final Set<Symbol> staticImports;
        private final Set<Symbol> unusedPkgImports;
        private final Set<Pair<Symbol,ClassIndexImpl.UsageType>> packageAnnotations;
        private final Set<CharSequence> importIdents;
        private final Set<CharSequence> packageAnnotationIdents;
        private final boolean virtual;
        private boolean isStaticImport;
        private boolean isPkgImport;
        private State state;
        private Element enclosingElement = null;
        private Set<String> rsList;         //List of references from source in case when the source has more top levels or is wrongly packaged
        private boolean crossedTopLevel;    //True when the visitor already reached the correctly packaged top level
        private boolean mainMethod;



        public UsagesVisitor (
                JavacTaskImpl jt,
                CompilationUnitTree cu,
                JavaFileManager manager,
                javax.tools.JavaFileObject sibling,
                Set<? super ElementHandle<TypeElement>> newTypes,
                final JavaCustomIndexer.CompileTuple tuple) throws MalformedURLException, IllegalArgumentException {

            assert jt != null;
            assert cu != null;
            assert manager != null;
            assert sibling != null;

            this.activeClass = new Stack<Pair<String,String>> ();
            this.imports = new HashSet<Symbol> ();
            this.staticImports = new HashSet<Symbol> ();
            this.unusedPkgImports = new HashSet<Symbol>();
            this.importIdents = new HashSet<CharSequence>();
            this.packageAnnotationIdents = new HashSet<CharSequence>();
            this.packageAnnotations = new HashSet<Pair<Symbol, ClassIndexImpl.UsageType>>();
            final Names names = Names.instance(jt.getContext());
            this.errorName = names.error;
            this.pkgImportName = names.asterisk;
            this.state = State.OTHER;
            this.cu = cu;
            this.signatureFiles = true;
            this.virtual = tuple.virtual;
            this.siblingUrl = virtual ? tuple.indexable.getURL() : sibling.toUri().toURL();
            this.sourceName = inferBinaryName(manager, sibling);
            this.topLevels = null;
            this.newTypes = newTypes;
        }

        protected UsagesVisitor (
                JavacTaskImpl jt,
                CompilationUnitTree cu,
                JavaFileManager manager,
                javax.tools.JavaFileObject sibling,
                Set<? super Pair<String,String>> topLevels) throws MalformedURLException, IllegalArgumentException {

            assert jt != null;
            assert cu != null;
            assert manager != null;
            assert sibling != null;

            this.activeClass = new Stack<Pair<String,String>> ();
            this.imports = new HashSet<Symbol> ();
            this.staticImports = new HashSet<Symbol>();
            this.unusedPkgImports = new HashSet<Symbol>();
            this.importIdents = new HashSet<CharSequence>();
            this.packageAnnotationIdents = new HashSet<CharSequence>();
            this.packageAnnotations = new HashSet<Pair<Symbol, ClassIndexImpl.UsageType>>();
            final Names names = Names.instance(jt.getContext());
            this.errorName = names.error;
            this.pkgImportName = names.asterisk;
            this.state = State.OTHER;
            this.cu = cu;
            this.signatureFiles = false;
            this.siblingUrl = sibling.toUri().toURL();
            this.sourceName = inferBinaryName(manager, sibling);
            this.topLevels = topLevels;
            this.newTypes = null;
            this.virtual = false;
        }


        @Override
        @CheckForNull
        public Void scan(@NonNull final Tree node, @NonNull final Map<Pair<String,String>, Data> p) {
            if (node == null) {
                return null;
            }
            super.scan (node,p);
            return null;
        }

        @Override
        @CheckForNull
        public Void visitCompilationUnit(@NonNull final CompilationUnitTree node, @NonNull final Map<Pair<String,String>, Data> p) {
            State oldState = state;
            try {
                state = State.PACKAGE_ANN;
                scan(node.getPackageAnnotations(),p);
                scan(node.getPackageName(),p);
                state = State.IMPORT;
                scan(node.getImports(),p);
            } finally {
                state=oldState;
            }
            scan(node.getTypeDecls(),p);

            String className = null;
            if (!imports.isEmpty() ||
                !staticImports.isEmpty() ||
                !unusedPkgImports.isEmpty()) {
                //Empty file
                className = getResourceName(node);
                final Pair<String,String> name;
                if (className != null) {
                    final String classNameType = className + DocumentUtil.encodeKind(ElementKind.CLASS);
                    name = Pair.<String,String>of(classNameType, null);
                } else {
                    name = null;
                }
                addAndClearImports(name,p);
                addAndClearUnusedPkgImports(name, p);
            }

            if (!packageAnnotations.isEmpty()) {
                if (className == null) {
                    className = getResourceName(node);
                }
                if (className != null) {
                    final String classNameType = className + DocumentUtil.encodeKind(ElementKind.CLASS);
                    final Pair<String,String> name = Pair.<String,String>of(classNameType, null);
                    for (Pair<Symbol,ClassIndexImpl.UsageType> usage : packageAnnotations) {
                        addUsage(usage.first, name, p, usage.second);
                    }
                    for (CharSequence ident : packageAnnotationIdents) {
                        addIdent(name, ident, p, false);
                    }
                }
                packageAnnotations.clear();
                packageAnnotationIdents.clear();
            }

            return null;
        }

        @Override
        @CheckForNull
        public Void visitMemberSelect(@NonNull final MemberSelectTree node,  @NonNull final Map<Pair<String,String>, Data> p) {
            handleVisitIdentSelect (((JCTree.JCFieldAccess)node).sym, node.getIdentifier(), p);
            State oldState = this.state;
            this.state = (this.state == State.IMPORT || state == State.PACKAGE_ANN) ? state : State.OTHER;
            Void ret = super.visitMemberSelect (node, p);
            this.state = oldState;
            return ret;
        }

        @Override
        @CheckForNull
        public Void visitIdentifier(@NonNull final IdentifierTree node, @NonNull final Map<Pair<String,String>, Data> p) {
            handleVisitIdentSelect (((JCTree.JCIdent)node).sym, node.getName(), p);
            return super.visitIdentifier(node, p);
        }

        @Override
        @CheckForNull
        public Void visitImport (@NonNull final ImportTree node, @NonNull final Map<Pair<String,String>, Data> p) {
            this.isStaticImport = node.isStatic();
            final Tree qit = node.getQualifiedIdentifier();
            isPkgImport = qit.getKind() == Tree.Kind.MEMBER_SELECT && pkgImportName == (((MemberSelectTree)qit).getIdentifier());
            final Void ret = super.visitImport(node, p);
            isStaticImport = isPkgImport = false;
            return ret;
        }

        private void handleVisitIdentSelect (
                @NullAllowed final Symbol sym,
                @NonNull final CharSequence name,
                @NonNull final Map<Pair<String,String>, Data> p) {
            if (!activeClass.empty()) {
                addIdent(activeClass.peek(), name, p, false);
                if (sym != null) {
                    if (sym.kind == Kinds.ERR) {
                        final Symbol owner = sym.getEnclosingElement();
                        if (owner.getKind().isClass() || owner.getKind().isInterface()) {
                            addUsage(owner, activeClass.peek(), p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                        }
                    }
                    if (sym.getKind().isClass() || sym.getKind().isInterface()) {
                        switch (this.state) {
                            case EXTENDS:
                                addUsage(sym, activeClass.peek(), p, ClassIndexImpl.UsageType.SUPER_CLASS);
                                break;
                            case IMPLEMENTS:
                                addUsage (sym, activeClass.peek(), p, ClassIndexImpl.UsageType.SUPER_INTERFACE);
                                break;
                            case OTHER:
                            case GT:
                                addUsage (sym, activeClass.peek(), p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                                break;
                        }
                    } else if (sym.getKind().isField()) {
                        final Symbol owner = sym.getEnclosingElement();
                        if (owner.getKind().isClass() || owner.getKind().isInterface()) {
                            addUsage (owner, activeClass.peek(),p,ClassIndexImpl.UsageType.FIELD_REFERENCE);
                        }
                        recordTypeUsage(sym.asType(), p);
                    } else if (sym.getKind() == ElementKind.CONSTRUCTOR || sym.getKind() == ElementKind.METHOD) {
                        final Symbol owner = sym.getEnclosingElement();
                        if (owner.getKind().isClass() || owner.getKind().isInterface()) {
                            addUsage (owner, activeClass.peek(), p, ClassIndexImpl.UsageType.METHOD_REFERENCE);
                        }
                        recordTypeUsage(((MethodSymbol) sym).getReturnType(), p);
                    }
                }
            } else {
                if (state == State.IMPORT) {
                    importIdents.add(name);
                    if (sym != null && (sym.getKind().isClass() || sym.getKind().isInterface())) {
                        if (this.isStaticImport) {
                            this.staticImports.add(sym);
                        }
                        else {
                            this.imports.add(sym);
                        }
                    } else if (isPkgImport && sym != null && sym.getKind() == ElementKind.PACKAGE) {
                        unusedPkgImports.add(sym);
                        isPkgImport = false;
                    }
                } else if (state == State.PACKAGE_ANN) {
                    packageAnnotationIdents.add(name);
                    if (sym != null) {
                        if (sym.kind == Kinds.ERR) {
                            final Symbol owner = sym.getEnclosingElement();
                            if (owner.getKind().isClass() || owner.getKind().isInterface()) {
                                packageAnnotations.add (Pair.of(owner,ClassIndexImpl.UsageType.TYPE_REFERENCE));
                            }
                        }
                        if (sym.getKind().isClass() || sym.getKind().isInterface()) {
                            packageAnnotations.add (Pair.of(sym,ClassIndexImpl.UsageType.TYPE_REFERENCE));
                        }
                        else if (sym.getKind().isField()) {
                            final Symbol owner = sym.getEnclosingElement();
                            if (owner.getKind().isClass() || owner.getKind().isInterface()) {
                                packageAnnotations.add (Pair.of(owner,ClassIndexImpl.UsageType.FIELD_REFERENCE));
                            }
                        }
                        else if (sym.getKind() == ElementKind.CONSTRUCTOR || sym.getKind() == ElementKind.METHOD) {
                            final Symbol owner = sym.getEnclosingElement();
                            if (owner.getKind().isClass() || owner.getKind().isInterface()) {
                                packageAnnotations.add(Pair.of(owner,ClassIndexImpl.UsageType.METHOD_REFERENCE));
                            }
                        }
                    }
                }
            }
        }

        @Override
        @CheckForNull
        public  Void visitParameterizedType(@NonNull final ParameterizedTypeTree node, @NonNull final Map<Pair<String,String>, Data> p) {
            scan(node.getType(), p);
            State currState = this.state;
            this.state = State.GT;
            scan(node.getTypeArguments(), p);
            this.state = currState;
            return null;
        }

        @Override
        @CheckForNull
        public Void visitClass (@NonNull final ClassTree node, @NonNull final Map<Pair<String,String>,Data> p) {
            final Symbol.ClassSymbol sym = ((JCTree.JCClassDecl)node).sym;
            boolean errorInDecl = false;
            boolean errorIgnorSubtree = true;
            boolean topLevel = false;
            String className = null;
            Pair<String,String> name = null;
            String simpleName = null;

            if (sym != null) {
                errorInDecl = hasErrorName(sym);
                if (errorInDecl) {
                    if (!activeClass.isEmpty()) {
                        name = activeClass.get(0);
                    } else {
                        topLevel = true;
                        className = getResourceName (this.cu);
                        if (className != null && !className.isEmpty()) {
                            final String classNameType = className + DocumentUtil.encodeKind(ElementKind.CLASS);
                            name = Pair.<String,String>of(classNameType, null);
                            simpleName = className.substring(className.lastIndexOf('.') + 1);
                        } else {
                            LOG.log(
                                Level.WARNING,
                                "Cannot resolve {0} (class name: {1}), ignoring whole subtree.",  //NOI18N
                                new Object[]{
                                    sym,
                                    className
                                });
                        }
                    }
                } else {
                    final StringBuilder classNameBuilder = new StringBuilder ();
                    ClassFileUtil.encodeClassName(sym, classNameBuilder, '.');  //NOI18N
                    className = classNameBuilder.toString();
                    if (!className.isEmpty()) {
                        ElementKind kind = sym.getKind();
                        classNameBuilder.append(DocumentUtil.encodeKind(kind));
                        final String classNameType = classNameBuilder.toString();
                        String resourceName = null;
                        topLevel = activeClass.isEmpty();
                        if (topLevel) {
                            if (virtual || !className.equals(sourceName)) {
                                if (signatureFiles && rsList == null) {
                                    rsList = new HashSet<String>();
                                    if (crossedTopLevel) {
                                        rsList.add(sourceName);
                                    }
                                }
                                final StringBuilder rnBuilder = new StringBuilder(FileObjects.convertPackage2Folder(sourceName));
                                rnBuilder.append('.');  //NOI18N
                                rnBuilder.append(FileObjects.getExtension(siblingUrl.getPath()));
                                resourceName =  rnBuilder.toString();
                            } else {
                                crossedTopLevel = true;
                            }
                        } else {
                            resourceName = activeClass.peek().second;
                        }
                        name = Pair.<String,String>of(classNameType, resourceName);
                        simpleName = sym.getSimpleName().toString();
                    } else {
                        LOG.log(
                            Level.WARNING,
                            "Invalid symbol {0} (source: {1}), ignoring whole subtree.",  //NOI18N
                            new Object[] {
                                sym,
                                siblingUrl
                        });
                    }
                }
            }
            if (name != null) {
                activeClass.push(name);
                errorIgnorSubtree = false;
                if (className != null) {
                    if (topLevel) {
                        if (topLevels != null) {
                            topLevels.add (Pair.<String,String>of(className, name.second));
                        }
                        addAndClearImports(name, p);
                    }
                    addUsage (className, name, p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                    // index only simple name, not FQN for classes
                    addIdent(name, simpleName, p, true);
                    if (newTypes !=null) {
                        newTypes.add ((ElementHandle<TypeElement>)ElementHandle.createTypeElementHandle(ElementKind.CLASS,className));
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
            if (!errorInDecl && this.rsList != null) {
                    this.rsList.add (className);
            }
            if (topLevel) {
                addAndClearUnusedPkgImports(name, p);
            }
            return null;
        }

        @Override
        @CheckForNull
        public Void visitNewClass(@NonNull final NewClassTree node, @NonNull final Map<Pair<String,String>,Data> p) {
            final Symbol sym = ((JCTree.JCNewClass)node).constructor;
            if (sym != null) {
                final Symbol owner = sym.getEnclosingElement();
                if (owner != null && owner.getKind().isClass()) {
                    addUsage(
                        owner,
                        activeClass.peek(),
                        p,
                        ClassIndexImpl.UsageType.METHOD_REFERENCE);
                }
            }
            return super.visitNewClass (node,p);
        }

        @Override
        @CheckForNull
        public Void visitErroneous(@NonNull final ErroneousTree tree, @NonNull final Map<Pair<String,String>,Data> p) {
            List<? extends Tree> trees = tree.getErrorTrees();
            for (Tree t : trees) {
                this.scan(t,p);
            }
            return null;
        }

        @Override
        @CheckForNull
        public Void visitMethod(@NonNull final MethodTree node, @NonNull final Map<Pair<String,String>, Data> p) {
            Element old = enclosingElement;
            try {
                enclosingElement = ((JCTree.JCMethodDecl) node).sym;
                if (enclosingElement != null && enclosingElement.getKind() == ElementKind.METHOD) {
                    mainMethod |= SourceUtils.isMainMethod((ExecutableElement) enclosingElement);
                    // do not add idents for constructors, they always match their class' name, which is added as an ident separately
                    addIdent(activeClass.peek(), node.getName(), p, true);
                }
                return super.visitMethod(node, p);
            } finally {
                enclosingElement = old;
            }
        }

        @Override
        @CheckForNull
        public Void visitVariable(@NonNull final VariableTree node, @NonNull final Map<Pair<String, String>, Data> p) {
            Symbol s = ((JCTree.JCVariableDecl)node).sym;
            if (s != null && s.owner != null && (s.owner.getKind().isClass() || s.owner.getKind().isInterface())) {
                addIdent(activeClass.peek(), node.getName(), p, true);
            }
            return super.visitVariable(node, p);
        }

        private void addAndClearImports(
                @NullAllowed final Pair<String,String> nameOfCU,
                @NonNull final Map<Pair<String,String>, Data> data) {
            if (nameOfCU != null) {
                for (Symbol s : imports) {
                    addUsage(s, nameOfCU, data, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                }
                for (Symbol s : staticImports) {
                    addUsage(
                        s,
                        nameOfCU,
                        data,
                        ClassIndexImpl.UsageType.TYPE_REFERENCE,
                        ClassIndexImpl.UsageType.METHOD_REFERENCE,
                        ClassIndexImpl.UsageType.FIELD_REFERENCE);
                }
                for (CharSequence s : importIdents) {
                    addIdent(nameOfCU, s, data, false);
                }
            }
            imports.clear();
            staticImports.clear();
            importIdents.clear();
        }

        private void addAndClearUnusedPkgImports(
                @NullAllowed final Pair<String,String> nameOfCU,
                @NonNull final Map<Pair<String,String>, Data> data) {
            if (nameOfCU != null) {
                for (Symbol s : unusedPkgImports) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(s.getQualifiedName());
                    sb.append(".package-info"); //NOI18N
                    addUsage(sb.toString(), nameOfCU, data, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                }
            }
            unusedPkgImports.clear();
        }

        /**
         * Infers a JavaFileObject
         * @param jfm the file manager
         * @param jfo the file to be inferred
         * @return a inferred name
         * @throws IllegalArgumentException when file cannot be inferred.
         */
        @NonNull
        private String inferBinaryName(
                @NonNull final JavaFileManager jfm,
                @NonNull final javax.tools.JavaFileObject jfo) throws IllegalArgumentException {
            String result = jfm.inferBinaryName(StandardLocation.SOURCE_PATH, jfo);
            if (result != null) {
                return result;
            }
            FileObject fo = null;
            ClassPath scp = null;
            try {
                fo = URLMapper.findFileObject(jfo.toUri().toURL());
                if (fo != null) {
                    scp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                    if (scp != null) {
                        result=scp.getResourceName(fo, '.', false); //NOI18N
                        if (result != null) {
                            return result;
                        }
                    }
                }
            } catch (MalformedURLException e) {
                //pass - throws IAE
            }
            throw new IllegalArgumentException(String.format("File: %s Type: %s FileObject: %s Sourcepath: %s",     //NOI18N
                    jfo.toUri().toString(),
                    jfo.getClass().getName(),
                    fo == null ? "<null>" : FileUtil.getFileDisplayName(fo),  //NOI18N
                    scp == null? "<null>" : scp.toString()));   //NOI18N
        }


        private void addUsage (
                @NullAllowed final Symbol sym,
                @NonNull final Pair<String,String>owner,
                @NonNull final Map<Pair<String,String>,Data> map,
                @NonNull final ClassIndexImpl.UsageType... types) {
            assert map != null;
            assert types != null;
            if (sym != null) {
                final String className = encodeClassName(sym);
                addUsage(className, owner, map, types);
                final Symbol encElm = sym.getEnclosingElement();
                if (encElm.getKind() == ElementKind.PACKAGE) {
                    unusedPkgImports.remove(encElm);
                }
            }
        }

        private void addUsage(
            @NullAllowed final String className,
            @NonNull final Pair<String,String>owner,
            @NonNull final Map<Pair<String,String>,Data> map,
            @NonNull final ClassIndexImpl.UsageType... types) {
            if (className != null) {
                final Data data = getData(owner, map);
                Set<ClassIndexImpl.UsageType> usageType = data.usages.get (className);
                if (usageType == null) {
                    usageType = EnumSet.noneOf(ClassIndexImpl.UsageType.class);
                    data.usages.put (className, usageType);
                }
                for (ClassIndexImpl.UsageType type : types) {
                    usageType.add (type);
                }
            }
        }

        private void addIdent (
                @NonNull final Pair<String,String>owner,
                @NonNull final CharSequence ident,
                @NonNull final Map<Pair<String,String>,Data> map,
                final boolean feature) {
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

        @NonNull
        private Data getData (@NonNull final Pair<String,String>owner, @NonNull final Map<Pair<String,String>, Data> map) {
            Data data = map.get(owner);
            if (data == null) {
                assert owner.first.charAt(owner.first.length()-2) != '.';   //NOI18N
                data = new Data ();
                map.put(owner,data);
            }
            return data;
        }

        private boolean hasErrorName (@NullAllowed Symbol cs) {
            while (cs != null) {
                if (cs.name == errorName) {
                    return true;
                }
                cs = cs.getEnclosingElement();
            }
            return false;
        }

        @CheckForNull
        private static String encodeClassName (@NonNull final Symbol sym) {
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

        @CheckForNull
        private static String getResourceName (@NullAllowed final CompilationUnitTree cu) {
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
        
        private void recordTypeUsage(final TypeMirror type, final Map<Pair<String, String>, Data> p) {
            List<TypeMirror> types = new LinkedList<TypeMirror>();
            types.add(type);
            while (!types.isEmpty()) {
                TypeMirror currentType = types.remove(0);
                if (currentType == null) continue;
                switch (currentType.getKind()) {
                    case DECLARED:
                        final Symbol typeSym = ((Type) currentType).tsym;
                        if (typeSym != null && (typeSym.getKind().isClass() || typeSym.getKind().isInterface())) {
                            addUsage (typeSym, activeClass.peek(), p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                        }
                        types.addAll(((DeclaredType) currentType).getTypeArguments());
                        break;
                    case ARRAY:
                        types.add(((ArrayType) currentType).getComponentType());
                        break;
                }
            }
        }

    }
    
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.impl;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.parser.EndPosParser;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.Lexer;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.Token;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.CancelService;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Names;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result2;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.modules.java.hints.jackpot.spi.ClassPathBasedHintProvider;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription;
import org.netbeans.modules.java.hints.jackpot.spi.HintProvider;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.builder.TreeFactory;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.pretty.ImportAnalysis2;
import org.netbeans.modules.java.source.transform.ImmutableTreeTranslator;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbCollections;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Lahoda
 */
public class Utilities {

    private Utilities() {}

    public static <E> Iterable<E> checkedIterableByFilter(final Iterable raw, final Class<E> type, final boolean strict) {
        return new Iterable<E>() {
            public Iterator<E> iterator() {
                return NbCollections.checkedIteratorByFilter(raw.iterator(), type, strict);
            }
        };
    }
    
//    public static AnnotationTree constructConstraint(WorkingCopy wc, String name, TypeMirror tm) {
//        TreeMaker make = wc.getTreeMaker();
//        ExpressionTree variable = prepareAssignment(make, "variable", make.Literal(name));
//        ExpressionTree type     = prepareAssignment(make, "type", make.MemberSelect((ExpressionTree) make.Type(wc.getTypes().erasure(tm)), "class"));
//        TypeElement constraint  = wc.getElements().getTypeElement(Annotations.CONSTRAINT.toFQN());
//
//        return make.Annotation(make.QualIdent(constraint), Arrays.asList(variable, type));
//    }

    public static ExpressionTree prepareAssignment(TreeMaker make, String name, ExpressionTree value) {
        return make.Assignment(make.Identifier(name), value);
    }

    public static ExpressionTree findValue(AnnotationTree m, String name) {
        for (ExpressionTree et : m.getArguments()) {
            if (et.getKind() == Kind.ASSIGNMENT) {
                AssignmentTree at = (AssignmentTree) et;
                String varName = ((IdentifierTree) at.getVariable()).getName().toString();

                if (varName.equals(name)) {
                    return at.getExpression();
                }
            }

            if (et instanceof LiteralTree/*XXX*/ && "value".equals(name)) {
                return et;
            }
        }

        return null;
    }

    public static List<AnnotationTree> findArrayValue(AnnotationTree at, String name) {
        ExpressionTree fixesArray = findValue(at, name);
        List<AnnotationTree> fixes = new LinkedList<AnnotationTree>();

        if (fixesArray != null && fixesArray.getKind() == Kind.NEW_ARRAY) {
            NewArrayTree trees = (NewArrayTree) fixesArray;

            for (ExpressionTree fix : trees.getInitializers()) {
                if (fix.getKind() == Kind.ANNOTATION) {
                    fixes.add((AnnotationTree) fix);
                }
            }
        }

        if (fixesArray != null && fixesArray.getKind() == Kind.ANNOTATION) {
            fixes.add((AnnotationTree) fixesArray);
        }
        
        return fixes;
    }

    public static boolean isPureMemberSelect(Tree mst, boolean allowVariables) {
        switch (mst.getKind()) {
            case IDENTIFIER: return allowVariables || ((IdentifierTree) mst).getName().charAt(0) != '$';
            case MEMBER_SELECT: return isPureMemberSelect(((MemberSelectTree) mst).getExpression(), allowVariables);
            default: return false;
        }
    }

//    public static Map<String, Collection<HintDescription>> sortOutHints(Iterable<? extends HintDescription> hints, Map<String, Collection<HintDescription>> output) {
//        for (HintDescription d : hints) {
//            Collection<HintDescription> h = output.get(d.getDisplayName());
//
//            if (h == null) {
//                output.put(d.getDisplayName(), h = new LinkedList<HintDescription>());
//            }
//
//            h.add(d);
//        }
//
//        return output;
//    }
//
//    public static List<HintDescription> listAllHints(Set<ClassPath> cps) {
//        List<HintDescription> result = new LinkedList<HintDescription>();
//
//        for (HintProvider p : Lookup.getDefault().lookupAll(HintProvider.class)) {
//            for (HintDescription hd : p.computeHints()) {
//                if (hd.getTriggerPattern() == null) continue; //TODO: only pattern based hints are currently supported
//                result.add(hd);
//            }
//        }
//
//        Set<FileObject> roots = new HashSet<FileObject>();
//
//        for (ClassPath cp : cps) {
//            for (FileObject r : cp.getRoots()) {
//                Result2 src;
//
//                try {
//                    src = SourceForBinaryQuery.findSourceRoots2(r.getURL());
//                } catch (FileStateInvalidException ex) {
//                    Logger.getLogger(Utilities.class.getName()).log(Level.FINE, null, ex);
//                    src = null;
//                }
//
//                if (src != null && src.preferSources()) {
//                    roots.addAll(Arrays.asList(src.getRoots()));
//                } else {
//                    roots.add(r);
//                }
//            }
//        }
//
//        ClassPath cp = ClassPathSupport.createClassPath(roots.toArray(new FileObject[0]));
//
//        for (ClassPathBasedHintProvider p : Lookup.getDefault().lookupAll(ClassPathBasedHintProvider.class)) {
//            result.addAll(p.computeHints(cp));
//        }
//
//        return result;
//    }
    
    public static Tree parseAndAttribute(CompilationInfo info, String pattern, Scope scope) {
        return parseAndAttribute(info, JavaSourceAccessor.getINSTANCE().getJavacTask(info), pattern, scope);
    }

    public static Tree parseAndAttribute(JavacTaskImpl jti, String pattern) {
        return parseAndAttribute(null, jti, pattern, null);
    }

    private static Tree parseAndAttribute(CompilationInfo info, JavacTaskImpl jti, String pattern, Scope scope) {
        Context c = jti.getContext();
        TreeFactory make = TreeFactory.instance(c);
        Tree patternTree = !isStatement(pattern) ? parseExpression(c, pattern, true, new SourcePositions[1]) : null;
        boolean expression = true;
        boolean classMember = false;

        if (patternTree == null || isErrorTree(patternTree)) {
            Log log = Log.instance(c);
            DiagnosticListener<? super JavaFileObject> old = log.getDiagnosticListener();
            DiagnosticCollector<JavaFileObject> dc = new DiagnosticCollector<JavaFileObject>();

            log.setDiagnosticListener(dc);

            try {
                Tree currentPatternTree = parseStatement(c, "{" + pattern + "}", new SourcePositions[1]);

                assert currentPatternTree.getKind() == Kind.BLOCK : currentPatternTree.getKind();

                List<? extends StatementTree> statements = ((BlockTree) currentPatternTree).getStatements();

                if (statements.size() == 1) {
                    currentPatternTree = statements.get(0);
                } else {
                    List<StatementTree> newStatements = new LinkedList<StatementTree>();

                    newStatements.add(make.ExpressionStatement(make.Identifier("$$1$")));
                    newStatements.addAll(statements);
                    newStatements.add(make.ExpressionStatement(make.Identifier("$$2$")));

                    currentPatternTree = make.Block(newStatements, false);
                }

                currentPatternTree = fixTree(c, currentPatternTree);

                boolean hasErrors = false;

                for (Diagnostic d : dc.getDiagnostics()) {
                    if (d.getKind() == Diagnostic.Kind.ERROR && !"compiler.err.not.stmt".contentEquals(d.getCode())) {
                        hasErrors = true;
                    }
                }

                if (hasErrors || containsError(currentPatternTree)) {
                    //maybe a class member?
                    Tree classPatternTree = parseExpression(c, "new Object() {" + pattern + "}", false, new SourcePositions[1]);

                    classPatternTree = fixTree(c, classPatternTree);

                    if (!containsError(classPatternTree)) {
                        patternTree = classPatternTree;
                        classMember = true;
                    } else {
                        patternTree = currentPatternTree;
                    }
                } else {
                    patternTree = currentPatternTree;
                }

                expression = false;
            } finally {
                log.setDiagnosticListener(old);
            }
        } else {
            patternTree = fixTree(c, patternTree);
        }

        int syntheticOffset = 0;

        if (scope != null) {
            assert info != null;

            TypeMirror type = info.getTreeUtilities().attributeTree(patternTree, scope);

            if (isError(type) && expression) {
                //maybe type?
                if (Utilities.isPureMemberSelect(patternTree, false) && info.getElements().getTypeElement(pattern) != null) {
                    Tree var = info.getTreeUtilities().parseExpression(pattern + ".class;", new SourcePositions[1]);

                    type = info.getTreeUtilities().attributeTree(var, scope);

                    Tree typeTree = ((MemberSelectTree) var).getExpression();

                    if (!isError(info.getTrees().getElement(new TreePath(new TreePath(info.getCompilationUnit()), typeTree)))) {
                        patternTree = typeTree;
                    }
                }
            }

            syntheticOffset = 1;
        }

        if (classMember) {
            List<? extends Tree> members = ((NewClassTree) patternTree).getClassBody().getMembers();

            if (members.size() > 1 + syntheticOffset) {
                ModifiersTree mt = make.Modifiers(EnumSet.noneOf(Modifier.class));
                List<Tree> newMembers = new LinkedList<Tree>();

                newMembers.add(make.ExpressionStatement(make.Identifier("$$1$")));
                newMembers.addAll(members.subList(syntheticOffset, members.size()));

                patternTree = make.Class(mt, "$", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), newMembers);
            } else {
                patternTree = members.get(0 + syntheticOffset);
            }
        }

        return patternTree;
    }

    private static boolean isError(Element el) {
        return (el == null || (el.getKind() == ElementKind.CLASS) && isError(((TypeElement) el).asType()));
    }

    private static boolean isError(TypeMirror type) {
        return type == null || type.getKind() == TypeKind.ERROR;
    }

    private static boolean isStatement(String pattern) {
        return pattern.trim().endsWith(";");
    }

    private static boolean isErrorTree(Tree t) {
        return t.getKind() == Kind.ERRONEOUS || (t.getKind() == Kind.IDENTIFIER && ((IdentifierTree) t).getName().contentEquals("<error>")); //TODO: <error>...
    }
    
    private static boolean containsError(Tree t) {
        return new TreeScanner<Boolean, Void>() {
            @Override
            public Boolean scan(Tree node, Void p) {
                if (node != null && isErrorTree(node)) {
                    return true;
                }
                return super.scan(node, p) ==Boolean.TRUE;
            }
            @Override
            public Boolean reduce(Boolean r1, Boolean r2) {
                return r1 == Boolean.TRUE || r2 == Boolean.TRUE;
            }
        }.scan(t, null);
    }

    private static Tree fixTree(Context c, Tree patternTree) {
        TreeFactory make = TreeFactory.instance(c);
        FixTree fixTree = new FixTree();

        //TODO: workaround, ImmutableTreeTranslator needs a CompilationUnitTree (rewriteChildren(BlockTree))
        //but sometimes no CompilationUnitTree (e.g. during BatchApply):
        CompilationUnitTree cut = make.CompilationUnit(null, Collections.<ImportTree>emptyList(), Collections.<Tree>emptyList(), null);
        ImportAnalysis2 ia = new ImportAnalysis2(c);

        ia.setImports(Collections.<ImportTree>emptyList());
        fixTree.attach(c, ia, cut, null);

        return fixTree.translate(patternTree);
    }

    private static final class FixTree extends ImmutableTreeTranslator {

        @Override
        public Tree translate(Tree tree) {
            if (tree != null && tree.getKind() == Kind.EXPRESSION_STATEMENT) {
                ExpressionStatementTree et = (ExpressionStatementTree) tree;

                if (et.getExpression().getKind() == Kind.ERRONEOUS) {
                    ErroneousTree err = (ErroneousTree) et.getExpression();

                    if (err.getErrorTrees().size() == 1 && err.getErrorTrees().get(0).getKind() == Kind.IDENTIFIER) {
                        IdentifierTree idTree = (IdentifierTree) err.getErrorTrees().get(0);
                        CharSequence id = idTree.getName().toString();

                        if (id.length() > 0 && id.charAt(0) == '$') {
                            return make.ExpressionStatement(idTree);
                        }
                    }
                }
            }
            return super.translate(tree);
        }

    }

    private static JCStatement parseStatement(Context context, CharSequence stmt, SourcePositions[] pos) {
        if (stmt == null || (pos != null && pos.length != 1))
            throw new IllegalArgumentException();
        JavaCompiler compiler = JavaCompiler.instance(context);
        JavaFileObject prev = compiler.log.useSource(new DummyJFO());
        try {
            CharBuffer buf = CharBuffer.wrap((stmt+"\u0000").toCharArray(), 0, stmt.length());
            ParserFactory factory = ParserFactory.instance(context);
            Scanner.Factory scannerFactory = Scanner.Factory.instance(context);
            Names names = Names.instance(context);
            Parser parser = new JackpotJavacParser(factory, scannerFactory.newScanner(buf), false, false, CancelService.instance(context), names);
            if (parser instanceof JavacParser) {
//                if (pos != null)
//                    pos[0] = new ParserSourcePositions((JavacParser)parser);
                return parser.parseStatement();
            }
            return null;
        } finally {
            compiler.log.useSource(prev);
        }
    }

    private static JCExpression parseExpression(Context context, CharSequence expr, boolean onlyFullInput, SourcePositions[] pos) {
        if (expr == null || (pos != null && pos.length != 1))
            throw new IllegalArgumentException();
        JavaCompiler compiler = JavaCompiler.instance(context);
        JavaFileObject prev = compiler.log.useSource(new DummyJFO());
        try {
            CharBuffer buf = CharBuffer.wrap((expr+"\u0000").toCharArray(), 0, expr.length());
            ParserFactory factory = ParserFactory.instance(context);
            Scanner.Factory scannerFactory = Scanner.Factory.instance(context);
            Names names = Names.instance(context);
            Scanner scanner = scannerFactory.newScanner(buf);
            Parser parser = new JackpotJavacParser(factory, scanner, false, false, CancelService.instance(context), names);
            if (parser instanceof JavacParser) {
//                if (pos != null)
//                    pos[0] = new ParserSourcePositions((JavacParser)parser);
                JCExpression result = parser.parseExpression();

                if (!onlyFullInput || scanner.token() == Token.EOF) {
                    return result;
                }
            }
            return null;
        } finally {
            compiler.log.useSource(prev);
        }
    }

    public static @CheckForNull CharSequence getWildcardTreeName(@NonNull Tree t) {
        if (t.getKind() == Kind.EXPRESSION_STATEMENT && ((ExpressionStatementTree) t).getExpression().getKind() == Kind.IDENTIFIER) {
            IdentifierTree identTree = (IdentifierTree) ((ExpressionStatementTree) t).getExpression();
            
            return identTree.getName().toString();
        }

        if (t.getKind() == Kind.IDENTIFIER) {
            IdentifierTree identTree = (IdentifierTree) t;
            String name = identTree.getName().toString();

            if (name.startsWith("$")) {
                return name;
            }
        }

        return null;
    }

    public static boolean isMultistatementWildcard(@NonNull CharSequence name) {
        return name.charAt(name.length() - 1) == '$';
    }

    public static boolean isMultistatementWildcardTree(Tree tree) {
        CharSequence name = Utilities.getWildcardTreeName(tree);

        return name != null && Utilities.isMultistatementWildcard(name);
    }

    private static long inc;

    public static Scope constructScope(CompilationInfo info, Map<String, TypeMirror> constraints) {
        return constructScope(info, constraints, Collections.<String>emptyList());
    }

    public static Scope constructScope(CompilationInfo info, Map<String, TypeMirror> constraints, Iterable<? extends String> auxiliaryImports) {
        return Lookup.getDefault().lookup(SPI.class).constructScope(info, constraints, auxiliaryImports);
    }

    public interface SPI {
        public Scope constructScope(CompilationInfo info, Map<String, TypeMirror> constraints, Iterable<? extends String> auxiliaryImports);
    }

    @ServiceProvider(service=SPI.class, position=1000)
    public static final class SPIImpl implements SPI {
        public Scope constructScope(CompilationInfo info, Map<String, TypeMirror> constraints, Iterable<? extends String> auxiliaryImports) {
        StringBuilder clazz = new StringBuilder();

        clazz.append("package $;");

        for (String i : auxiliaryImports) {
            clazz.append(i);
        }

        long count = inc++;

        clazz.append("public class $" + count + "{");

        for (Entry<String, TypeMirror> e : constraints.entrySet()) {
            if (e.getValue() != null) {
                clazz.append("private ");
                clazz.append(e.getValue().toString()); //XXX
                clazz.append(" ");
                clazz.append(e.getKey());
                clazz.append(";\n");
            }
        }

        clazz.append("private void test() {\n");
        clazz.append("}\n");
        clazz.append("}\n");

        JavacTaskImpl jti = JavaSourceAccessor.getINSTANCE().getJavacTask(info);
        Context context = jti.getContext();
        JavaCompiler jc = JavaCompiler.instance(context);

        Log.instance(context).nerrors = 0;

        //TODO: remove impl. dep. on java.source
        JavaFileObject jfo = FileObjects.memoryFileObject("$", "$", new File("/tmp/t" + count + ".java").toURI(), System.currentTimeMillis(), clazz.toString());
        boolean oldSkipAPs = jc.skipAnnotationProcessing;

        try {
            jc.skipAnnotationProcessing = true;
            
            Iterable<? extends CompilationUnitTree> parsed = jti.parse(jfo);
            CompilationUnitTree cut = parsed.iterator().next();

            jti.analyze(jti.enter(parsed));

            return new ScannerImpl().scan(cut, info);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } finally {
            jc.skipAnnotationProcessing = oldSkipAPs;
        }
    }
    }

    private static final class ScannerImpl extends TreePathScanner<Scope, CompilationInfo> {

        @Override
        public Scope visitBlock(BlockTree node, CompilationInfo p) {
            return p.getTrees().getScope(getCurrentPath());
        }

        @Override
        public Scope visitMethod(MethodTree node, CompilationInfo p) {
            if (node.getReturnType() == null) {
                return null;
            }
            return super.visitMethod(node, p);
        }

        @Override
        public Scope reduce(Scope r1, Scope r2) {
            return r1 != null ? r1 : r2;
        }

    }

//    private static Scope constructScope2(CompilationInfo info, Map<String, TypeMirror> constraints) {
//        JavacScope s = (JavacScope) info.getTrees().getScope(new TreePath(info.getCompilationUnit()));
//        Env<AttrContext> env = s.getEnv();
//
//        env = env.dup(env.tree);
//
//        env.info.
//    }

    public static String toHumanReadableTime(double d) {
        StringBuilder result = new StringBuilder();
        long inSeconds = (long) (d / 1000);
        int seconds = (int) (inSeconds % 60);
        long inMinutes = inSeconds / 60;
        int minutes = (int) (inMinutes % 60);
        long inHours = inMinutes / 60;

        if (inHours > 0) {
            result.append(inHours);
            result.append("h");
        }

        if (minutes > 0) {
            result.append(minutes);
            result.append("m");
        }
        
        result.append(seconds);
        result.append("s");

        return result.toString();
    }

    public static ClasspathInfo createUniversalCPInfo() {
        JavaPlatform select = JavaPlatform.getDefault();

        for (JavaPlatform p : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            if (p.getSpecification().getVersion().compareTo(select.getSpecification().getVersion()) > 0) {
                select = p;
            }
        }

        return ClasspathInfo.create(select.getBootstrapLibraries(), ClassPath.EMPTY, ClassPath.EMPTY);
    }

    @SuppressWarnings("deprecation")
    public static void waitScanFinished() throws InterruptedException {
        SourceUtils.waitScanFinished();
    }

    public static Set<? extends String> findSuppressedWarnings(CompilationInfo info, TreePath path) {
        //TODO: cache?
        Set<String> keys = new HashSet<String>();

        while (path != null) {
            Tree leaf = path.getLeaf();

            switch (leaf.getKind()) {
                case METHOD:
                    handleSuppressWarnings(info, path, ((MethodTree) leaf).getModifiers(), keys);
                    break;
                case CLASS:
                    handleSuppressWarnings(info, path, ((ClassTree) leaf).getModifiers(), keys);
                    break;
                case VARIABLE:
                    handleSuppressWarnings(info, path, ((VariableTree) leaf).getModifiers(), keys);
                    break;
            }

            path = path.getParentPath();
        }

        return Collections.unmodifiableSet(keys);
    }

    private static void handleSuppressWarnings(CompilationInfo info, TreePath path, ModifiersTree modifiers, final Set<String> keys) {
        Element el = info.getTrees().getElement(path);

        if (el == null) {
            return ;
        }

        for (AnnotationMirror am : el.getAnnotationMirrors()) {
            Name fqn = ((TypeElement) am.getAnnotationType().asElement()).getQualifiedName();
            
            if (!fqn.contentEquals("java.lang.SuppressWarnings")) {
                continue;
            }

            for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : am.getElementValues().entrySet()) {
                if (!e.getKey().getSimpleName().contentEquals("value"))
                    continue;

                e.getValue().accept(new AnnotationValueVisitor<Void, Void>() {
                    public Void visit(AnnotationValue av, Void p) {
                        av.accept(this, p);
                        return null;
                    }
                    public Void visit(AnnotationValue av) {
                        av.accept(this, null);
                        return null;
                    }
                    public Void visitBoolean(boolean b, Void p) {
                        return null;
                    }
                    public Void visitByte(byte b, Void p) {
                        return null;
                    }
                    public Void visitChar(char c, Void p) {
                        return null;
                    }
                    public Void visitDouble(double d, Void p) {
                        return null;
                    }
                    public Void visitFloat(float f, Void p) {
                        return null;
                    }
                    public Void visitInt(int i, Void p) {
                        return null;
                    }
                    public Void visitLong(long i, Void p) {
                        return null;
                    }
                    public Void visitShort(short s, Void p) {
                        return null;
                    }
                    public Void visitString(String s, Void p) {
                        keys.add(s);
                        return null;
                    }
                    public Void visitType(TypeMirror t, Void p) {
                        return null;
                    }
                    public Void visitEnumConstant(VariableElement c, Void p) {
                        return null;
                    }
                    public Void visitAnnotation(AnnotationMirror a, Void p) {
                        return null;
                    }
                    public Void visitArray(List<? extends AnnotationValue> vals, Void p) {
                        for (AnnotationValue av : vals) {
                            av.accept(this, p);
                        }
                        return null;
                    }
                    public Void visitUnknown(AnnotationValue av, Void p) {
                        return null;
                    }
                }, null);
            }
        }
    }

    public static Tree generalizePattern(CompilationInfo info, TreePath original) {
        return generalizePattern(JavaSourceAccessor.getINSTANCE().getJavacTask(info), original);
    }

    public static Tree generalizePattern(CompilationTask task, TreePath original) {
        JavacTaskImpl jti = (JavacTaskImpl) task;
        com.sun.tools.javac.util.Context c = jti.getContext();
        TreeFactory make = TreeFactory.instance(c);
        Trees javacTrees = Trees.instance(task);
        GeneralizePattern gp = new GeneralizePattern(javacTrees, make);

        gp.scan(original, null);

        GeneralizePatternITT itt = new GeneralizePatternITT(gp.tree2Variable);

        //TODO: workaround, ImmutableTreeTranslator needs a CompilationUnitTree (rewriteChildren(BlockTree))
        //but sometimes no CompilationUnitTree (e.g. during BatchApply):
        CompilationUnitTree cut = TreeFactory.instance(c).CompilationUnit(null, Collections.<ImportTree>emptyList(), Collections.<Tree>emptyList(), null);

        itt.attach(c, new NoImports(c), cut, null);

        return itt.translate(original.getLeaf());
    }

    public static Tree generalizePattern(CompilationInfo info, TreePath original, int firstStatement, int lastStatement) {
        JavacTaskImpl jti = JavaSourceAccessor.getINSTANCE().getJavacTask(info);
        com.sun.tools.javac.util.Context c = jti.getContext();
        TreeFactory make = TreeFactory.instance(c);
        Tree translated = Utilities.generalizePattern(jti, original);

        assert translated.getKind() == Kind.BLOCK;

        List<StatementTree> newStatements = new LinkedList<StatementTree>();
        BlockTree block = (BlockTree) translated;

        if (firstStatement != lastStatement) {
            newStatements.add(make.ExpressionStatement(make.Identifier("$s0$")));
            newStatements.addAll(block.getStatements().subList(firstStatement, lastStatement + 1));
            newStatements.add(make.ExpressionStatement(make.Identifier("$s1$")));

            translated = make.Block(newStatements, block.isStatic());
        } else {
            translated = block.getStatements().get(firstStatement);
        }

        return translated;
    }

    private static final class GeneralizePattern extends TreePathScanner<Void, Void> {

        public final Map<Tree, Tree> tree2Variable = new HashMap<Tree, Tree>();
        private final Map<Element, String> element2Variable = new HashMap<Element, String>();
        private final Trees javacTrees;
        private final TreeFactory make;

        private int currentVariableIndex = 0;

        public GeneralizePattern(Trees javacTrees, TreeFactory make) {
            this.javacTrees = javacTrees;
            this.make = make;
        }

        private @NonNull String getVariable(@NonNull Element el) {
            String var = element2Variable.get(el);

            if (var == null) {
                element2Variable.put(el, var = "$" + currentVariableIndex++);
            }

            return var;
        }

        private boolean shouldBeGeneralized(@NonNull Element el) {
            if (el.getModifiers().contains(Modifier.PRIVATE)) {
                return true;
            }

            switch (el.getKind()) {
                case LOCAL_VARIABLE:
                case EXCEPTION_PARAMETER:
                case PARAMETER:
                    return true;
            }

            return false;
        }

        @Override
        public Void visitIdentifier(IdentifierTree node, Void p) {
            Element e = javacTrees.getElement(getCurrentPath());

            if (e != null && shouldBeGeneralized(e)) {
                tree2Variable.put(node, make.Identifier(getVariable(e)));
            }

            return super.visitIdentifier(node, p);
        }

        @Override
        public Void visitVariable(VariableTree node, Void p) {
            Element e = javacTrees.getElement(getCurrentPath());

            if (e != null && shouldBeGeneralized(e)) {
                VariableTree nue = make.Variable(node.getModifiers(), getVariable(e), node.getType(), node.getInitializer());

                tree2Variable.put(node, nue);
            }

            return super.visitVariable(node, p);
        }

        @Override
        public Void visitNewClass(NewClassTree node, Void p) {
            //XXX:
            List<? extends ExpressionTree> arguments = node.getArguments();

            if (!arguments.isEmpty() && arguments.get(0).getKind() == Kind.OTHER) {
                tree2Variable.put(node, make.Identifier("$" + currentVariableIndex++));
                return null;
            }

            return super.visitNewClass(node, p);
        }

    }

    private static final class GeneralizePatternITT extends ImmutableTreeTranslator {

        private final Map<Tree, Tree> tree2Variable;

        public GeneralizePatternITT(Map<Tree, Tree> tree2Variable) {
            this.tree2Variable = tree2Variable;
        }

        @Override
        public Tree translate(Tree tree) {
            Tree var = tree2Variable.remove(tree);

            if (var != null) {
                return super.translate(var);
            }

            return super.translate(tree);
        }

    }

    private static final class NoImports extends ImportAnalysis2 {

        public NoImports(Context env) {
            super(env);
        }

        @Override
        public void classEntered(ClassTree clazz) {}

        @Override
        public void classLeft() {}

        @Override
        public ExpressionTree resolveImport(MemberSelectTree orig, Element element) {
            return orig;
        }

        @Override
        public void setCompilationUnit(CompilationUnitTree cut) {}

        private List<? extends ImportTree> imports;

        @Override
        public void setImports(List<? extends ImportTree> importsToAdd) {
            this.imports = importsToAdd;
        }

        @Override
        public List<? extends ImportTree> getImports() {
            return this.imports;
        }

        @Override
        public void setPackage(ExpressionTree packageNameTree) {}

    }

    public static long patternValue(Tree pattern) {
        class VisitorImpl extends TreeScanner<Void, Void> {
            private int value;
            @Override
            public Void scan(Tree node, Void p) {
                if (node != null) value++;
                return super.scan(node, p);
            }
            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                if (node.getName().toString().startsWith("$")) value--;
                
                return super.visitIdentifier(node, p);
            }
        }

        VisitorImpl vi = new VisitorImpl();

        vi.scan(pattern, null);

        return vi.value;
    }

    public static boolean containsMultistatementTrees(List<? extends Tree> statements) {
        for (Tree t : statements) {
            if (Utilities.isMultistatementWildcardTree(t)) {
                return true;
            }
        }

        return false;
    }

    private static class JackpotJavacParser extends EndPosParser {

        public JackpotJavacParser(ParserFactory fac,
                         Lexer S,
                         boolean keepDocComments,
                         boolean keepLineMap,
                         CancelService cancelService,
                         Names names) {
            super(fac, S, keepDocComments, keepLineMap, cancelService);

            newAnonScope(names.empty, -1);
        }

        @Override
        protected JCModifiers modifiersOpt(JCModifiers partial) {
            if (S.token() == Token.IDENTIFIER) {
                String ident = S.stringVal();

                if (Utilities.isMultistatementWildcard(ident)) {
                    com.sun.tools.javac.util.Name name = S.name();

                    S.nextToken();

                    return new ModifiersWildcard(name, F.Ident(name));
                }
            }

            return super.modifiersOpt(partial);
        }


    }

    private static final class DummyJFO extends SimpleJavaFileObject {
        private DummyJFO() {
            super(URI.create("dummy.java"), JavaFileObject.Kind.SOURCE);
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return "";
        }
    };
}

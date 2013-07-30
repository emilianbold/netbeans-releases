/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
package org.netbeans.modules.java.hints.introduce;

import org.netbeans.api.java.source.matching.Pattern;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.api.java.source.support.SelectionAwareJavaSourceTaskFactory;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.introduce.Flow.FlowResult;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.api.java.source.matching.Occurrence;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.hints.errors.CreateElementUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Union2;

/**
 *
 * @author Jan Lahoda
 */
public class IntroduceHint implements CancellableTask<CompilationInfo> {

    private AtomicBoolean cancel = new AtomicBoolean();

    public IntroduceHint() {
    }

    private static final Set<TypeKind> NOT_ACCEPTED_TYPES = EnumSet.of(TypeKind.ERROR, TypeKind.NONE, TypeKind.OTHER, TypeKind.VOID, TypeKind.EXECUTABLE);
    private static final Set<JavaTokenId> WHITESPACES = EnumSet.of(JavaTokenId.WHITESPACE, JavaTokenId.BLOCK_COMMENT, JavaTokenId.LINE_COMMENT, JavaTokenId.JAVADOC_COMMENT);

    static int[] ignoreWhitespaces(CompilationInfo ci, int start, int end) {
        TokenSequence<JavaTokenId> ts = ci.getTokenHierarchy().tokenSequence(JavaTokenId.language());

        if (ts == null) {
            return new int[] {start, end};
        }

        ts.move(start);

        if (ts.moveNext()) {
            boolean wasMoveNext = true;

            while (WHITESPACES.contains(ts.token().id()) && (wasMoveNext = ts.moveNext()))
                ;

            if (wasMoveNext && ts.offset() > start)
                start = ts.offset();
        }

        ts.move(end);

        while (ts.movePrevious() && WHITESPACES.contains(ts.token().id()) && ts.offset() < end)
            end = ts.offset();

        return new int[] {start, end};
    }

    static TreePath validateSelection(CompilationInfo ci, int start, int end) {
        return validateSelection(ci, start, end, NOT_ACCEPTED_TYPES);
    }

    public static TreePath validateSelection(CompilationInfo ci, int start, int end, Set<TypeKind> ignoredTypes) {
        int[] span = ignoreWhitespaces(ci, Math.min(start, end), Math.max(start, end));

        start = span[0];
        end   = span[1];
        
        TreePath tp = ci.getTreeUtilities().pathFor((start + end) / 2 + 1);

        for ( ; tp != null; tp = tp.getParentPath()) {
            Tree leaf = tp.getLeaf();

            if (   !ExpressionTree.class.isAssignableFrom(leaf.getKind().asInterface())
                && (leaf.getKind() != Kind.VARIABLE || ((VariableTree) leaf).getInitializer() == null))
               continue;

            long treeStart = ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), leaf);
            long treeEnd   = ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), leaf);

            if (treeStart != start || treeEnd != end) {
                continue;
            }

            TypeMirror type = ci.getTrees().getTypeMirror(tp);

            if (type != null && type.getKind() == TypeKind.ERROR) {
                type = ci.getTrees().getOriginalType((ErrorType) type);
            }

            if (type == null || ignoredTypes.contains(type.getKind()))
                continue;

            if(tp.getLeaf().getKind() == Kind.ASSIGNMENT)
                continue;

            if (tp.getLeaf().getKind() == Kind.ANNOTATION)
                continue;

            if (!isInsideClass(tp))
                return null;

            TreePath candidate = tp;

            tp = tp.getParentPath();

            while (tp != null) {
                switch (tp.getLeaf().getKind()) {
                    case VARIABLE:
                        VariableTree vt = (VariableTree) tp.getLeaf();
                        if (vt.getInitializer() == leaf) {
                            return candidate;
                        } else {
                            return null;
                        }
                    case NEW_CLASS:
                        NewClassTree nct = (NewClassTree) tp.getLeaf();
                        
                        if (nct.getIdentifier().equals(candidate.getLeaf())) { //avoid disabling hint ie inside of anonymous class higher in treepath
                            for (Tree p : nct.getArguments()) {
                                if (p == leaf) {
                                    return candidate;
                                }
                            }

                            return null;
                        }
                }

                leaf = tp.getLeaf();
                tp = tp.getParentPath();
            }

            return candidate;
        }

        return null;
    }

    public static TreePathHandle validateSelectionForIntroduceMethod(CompilationInfo ci, int start, int end, int[] statementsSpan) {
        int[] span = ignoreWhitespaces(ci, Math.min(start, end), Math.max(start, end));

        start = span[0];
        end   = span[1];

        if (start >= end)
            return null;

        TreePath tp = ci.getTreeUtilities().pathFor((start + end) / 2 + 1);

        for ( ; tp != null; tp = tp.getParentPath()) {
            Tree leaf = tp.getLeaf();

            if (!StatementTree.class.isAssignableFrom(leaf.getKind().asInterface()))
               continue;

            long treeStart = ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), leaf);
            long treeEnd   = ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), leaf);

            if (treeStart != start || treeEnd != end) {
                continue;
            }

            List<? extends StatementTree> statements = getStatements(tp);
            statementsSpan[0] = statements.indexOf(tp.getLeaf());
            statementsSpan[1] = statementsSpan[0];

            return TreePathHandle.create(tp, ci);
        }

        TreePath tpStart = ci.getTreeUtilities().pathFor(start);
        TreePath tpEnd = ci.getTreeUtilities().pathFor(end);

        if (tpStart.getLeaf() != tpEnd.getLeaf() || (tpStart.getLeaf().getKind() != Kind.BLOCK && tpStart.getLeaf().getKind() != Kind.CASE)) {
                    //??? not in the same block:
            return null;
        }

        int from = -1;
        int to   = -1;

        List<? extends StatementTree> statements =   tpStart.getLeaf().getKind() == Kind.BLOCK
                                                   ? ((BlockTree) tpStart.getLeaf()).getStatements()
                                                   : ((CaseTree) tpStart.getLeaf()).getStatements();

        int index = 0;

        for (StatementTree s : statements) {
            long sStart = ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), s);

            if (sStart == start && from == (-1)) {
                from = index;
            }

            if (end < sStart && to == (-1)) {
                to = index - 1;
            }

            index++;
        }

        if (from == (-1)) {
            return null;
        }

        if (to == (-1))
            to = statements.size() - 1;

        if (to < from) {
            return null;
        }

        statementsSpan[0] = from;
        statementsSpan[1] = to;

        return TreePathHandle.create(new TreePath(tpStart, statements.get(from)), ci);
    }

    public void run(CompilationInfo info) {
        cancel.set(false);

        FileObject file = info.getFileObject();
        int[] selection = SelectionAwareJavaSourceTaskFactory.getLastSelection(file);

        if (selection == null) {
            //nothing to do....
            HintsController.setErrors(info.getFileObject(), IntroduceHint.class.getName(), Collections.<ErrorDescription>emptyList());
        } else {
            HintsController.setErrors(info.getFileObject(), IntroduceHint.class.getName(), computeError(info, selection[0], selection[1], null, new EnumMap<IntroduceKind, String>(IntroduceKind.class), cancel));

            Document doc = info.getSnapshot().getSource().getDocument(false);

            if (doc != null) {
                PositionRefresherHelperImpl.setVersion(doc, selection[0], selection[1]);
            }
        }
    }

    public void cancel() {
        cancel.set(true);
    }

    private static boolean isConstructor(CompilationInfo info, TreePath path) {
        Element e = info.getTrees().getElement(path);

        return e != null && e.getKind() == ElementKind.CONSTRUCTOR;
    }

    private static boolean isInAnnotationType(CompilationInfo info, TreePath path) {
        Element e = info.getTrees().getElement(path);
        if (e != null) {
            e = e.getEnclosingElement();
            return e != null && e.getKind() == ElementKind.ANNOTATION_TYPE;
        }
        return false;
    }

    private static List<TreePath> findConstructors(CompilationInfo info, TreePath method) {
        List<TreePath> result = new LinkedList<TreePath>();
        TreePath parent = method.getParentPath();

        if (TreeUtilities.CLASS_TREE_KINDS.contains(parent.getLeaf().getKind())) {
            for (Tree t : ((ClassTree) parent.getLeaf()).getMembers()) {
                TreePath tp = new TreePath(parent, t);

                if (isConstructor(info, tp)) {
                    result.add(tp);
                }
            }
        }

        return result;
    }

    private static boolean isInsideClass(TreePath tp) {
        while (tp != null) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind()))
                return true;

            tp = tp.getParentPath();
        }

        return false;
    }

    public static List<ErrorDescription> computeError(CompilationInfo info, int start, int end, Map<IntroduceKind, Fix> fixesMap, Map<IntroduceKind, String> errorMessage, AtomicBoolean cancel) {
        List<ErrorDescription> hints = new LinkedList<ErrorDescription>();
        List<Fix> fixes = new LinkedList<Fix>();
        TreePath resolved = validateSelection(info, start, end);

        if (resolved != null) {
            TreePathHandle h = TreePathHandle.create(resolved, info);
            TreePath method   = findMethod(resolved);
            boolean variableRewrite = resolved.getLeaf().getKind() == Kind.VARIABLE;
            TreePath value = !variableRewrite ? resolved : new TreePath(resolved, ((VariableTree) resolved.getLeaf()).getInitializer());
            boolean isConstant = checkConstantExpression(info, value);
            TreePath constantTarget = isConstant ? findAcceptableConstantTarget(info, resolved) : null;
            isConstant &= constantTarget != null;
            boolean isVariable = findStatement(resolved) != null && method != null && !variableRewrite;
            Set<TreePath> duplicatesForVariable = isVariable ? SourceUtils.computeDuplicates(info, resolved, method, cancel) : null;
            Set<TreePath> duplicatesForConstant = /*isConstant ? */SourceUtils.computeDuplicates(info, resolved, new TreePath(info.getCompilationUnit()), cancel);// : null;
            Scope scope = info.getTrees().getScope(resolved);
            boolean statik = scope != null ? info.getTreeUtilities().isStaticContext(scope) : false;
            String guessedName = Utilities.getName(resolved.getLeaf());
            if (guessedName == null) guessedName = "name";
            Scope s = info.getTrees().getScope(resolved);
            CodeStyle cs = CodeStyle.getDefault(info.getFileObject());
            Fix variable = isVariable ? new IntroduceFix(h, info.getJavaSource(), variableRewrite ? guessedName : Utilities.makeNameUnique(info, s, guessedName, cs.getLocalVarNamePrefix(), cs.getLocalVarNameSuffix()), duplicatesForVariable.size() + 1, IntroduceKind.CREATE_VARIABLE, end) : null;
            Fix constant = isConstant ? new IntroduceFix(h, info.getJavaSource(), variableRewrite ? guessedName : Utilities.makeNameUnique(info, info.getTrees().getScope(constantTarget), Utilities.toConstantName(guessedName), cs.getStaticFieldNamePrefix(), cs.getStaticFieldNameSuffix()), duplicatesForConstant.size() + 1, IntroduceKind.CREATE_CONSTANT, end) : null;
            Fix parameter = isVariable ? new IntroduceParameterFix(h) : null;
            Fix field = null;
            Fix methodFix = null;

            if (method != null && !isInAnnotationType(info, method)) {
                int[] initilizeIn = computeInitializeIn(info, resolved, duplicatesForConstant);

                if (statik) {
                    initilizeIn[0] &= ~IntroduceFieldPanel.INIT_CONSTRUCTORS;
                    initilizeIn[1] &= ~IntroduceFieldPanel.INIT_CONSTRUCTORS;
                }

                boolean allowFinalInCurrentMethod = false;

                if (isConstructor(info, method)) {
                    //how many constructors do we have in the target class?:
                    allowFinalInCurrentMethod = findConstructors(info, method).size() == 1;
                }

                if (resolved.getLeaf().getKind() == Kind.VARIABLE) {
                    //the variable name would incorrectly clash with itself:
                    guessedName = Utilities.guessName(info, resolved, resolved.getParentPath(), cs.getFieldNamePrefix(), cs.getFieldNameSuffix());
                } else if (!variableRewrite) {
                    TreePath pathToClass = resolved;

                    while (pathToClass != null && !TreeUtilities.CLASS_TREE_KINDS.contains(pathToClass.getLeaf().getKind())) {
                        pathToClass = pathToClass.getParentPath();
                    }
                    
                    if (pathToClass != null) { //XXX: should actually produce two different names: one when replacing duplicates, one when not replacing them
                        guessedName = Utilities.makeNameUnique(info,
                                                               info.getTrees().getScope(pathToClass),
                                                               guessedName,
                                                               statik ? cs.getStaticFieldNamePrefix() : cs.getFieldNamePrefix(),
                                                               statik ? cs.getStaticFieldNameSuffix() : cs.getFieldNameSuffix());
                    }
                }

                field = new IntroduceFieldFix(h, info.getJavaSource(), guessedName, duplicatesForConstant.size() + 1, initilizeIn, statik, allowFinalInCurrentMethod, end);

                if (!variableRewrite) {
                    //introduce method based on expression:
                    Element methodEl = info.getTrees().getElement(method);
                    Map<TypeMirror, TreePathHandle> typeVar2Def = new HashMap<TypeMirror, TreePathHandle>();
                    List<TreePathHandle> typeVars = new LinkedList<TreePathHandle>();

                    prepareTypeVars(method, info, typeVar2Def, typeVars);

                    ScanStatement scanner = new ScanStatement(info, resolved.getLeaf(), resolved.getLeaf(), typeVar2Def, Collections.<Tree, Iterable<? extends TreePath>>emptyMap(), cancel);

                    if (methodEl != null && (methodEl.getKind() == ElementKind.METHOD || methodEl.getKind() == ElementKind.CONSTRUCTOR)) {
                        ExecutableElement ee = (ExecutableElement) methodEl;

                        scanner.localVariables.addAll(ee.getParameters());
                    }

                    scanner.scan(method, null);

                    List<TreePathHandle> params = new LinkedList<TreePathHandle>();

                    boolean error186980 = false;
                    for (VariableElement ve : scanner.usedLocalVariables.keySet()) {
                        TreePath path = info.getTrees().getPath(ve);
                        if (path == null) {
                            error186980 = true;
                            Logger.getLogger(IntroduceHint.class.getName()).warning("Cannot get TreePath for local variable " + ve + "\nfile=" + info.getFileObject().getPath());
                        } else {
                            params.add(TreePathHandle.create(path, info));
                        }
                    }

                    if (!error186980) {
                        Set<TypeMirror> exceptions = new HashSet<TypeMirror>(info.getTreeUtilities().getUncaughtExceptions(resolved));

                        Set<TypeMirrorHandle> exceptionHandles = new HashSet<TypeMirrorHandle>();

                        for (TypeMirror tm : exceptions) {
                            exceptionHandles.add(TypeMirrorHandle.create(tm));
                        }

                        Pattern p = Pattern.createPatternWithRemappableVariables(resolved, scanner.usedLocalVariables.keySet(), true);
                        int duplicatesCount = Matcher.create(info).setCancel(cancel).match(p).size();

                        typeVars.retainAll(scanner.usedTypeVariables);

                        methodFix = new IntroduceExpressionBasedMethodFix(info.getJavaSource(), h, params, exceptionHandles, duplicatesCount, typeVars, end);
                    }
                }
            }

            if (fixesMap != null) {
                fixesMap.put(IntroduceKind.CREATE_VARIABLE, variable);
                fixesMap.put(IntroduceKind.CREATE_CONSTANT, constant);
                fixesMap.put(IntroduceKind.CREATE_FIELD, field);
                fixesMap.put(IntroduceKind.CREATE_METHOD, methodFix);
                fixesMap.put(IntroduceKind.CREATE_PARAMETER, parameter);
            }


            if (variable != null) {
                fixes.add(variable);
            }

            if (constant != null) {
                fixes.add(constant);
            }

            if (field != null) {
                fixes.add(field);
            }

            if (methodFix != null) {
                fixes.add(methodFix);
            }
            if (parameter != null) {
                fixes.add(parameter);
            } 
        }

        Fix introduceMethod = computeIntroduceMethod(info, start, end, fixesMap, errorMessage, cancel);

        if (introduceMethod != null) {
            fixes.add(introduceMethod);
            if (fixesMap != null) {
                fixesMap.put(IntroduceKind.CREATE_METHOD, introduceMethod);
            }
        }

        if (!fixes.isEmpty()) {
            int pos = CaretAwareJavaSourceTaskFactory.getLastPosition(info.getFileObject());
            String displayName = NbBundle.getMessage(IntroduceHint.class, "HINT_Introduce");

            hints.add(ErrorDescriptionFactory.createErrorDescription(Severity.HINT, displayName, fixes, info.getFileObject(), pos, pos));
        }

        return hints;
    }

    static Fix computeIntroduceMethod(CompilationInfo info, int start, int end, Map<IntroduceKind, Fix> fixesMap, Map<IntroduceKind, String> errorMessage, AtomicBoolean cancel) {
        int[] statements = new int[2];

        TreePathHandle h = validateSelectionForIntroduceMethod(info, start, end, statements);

        if (h == null) {
            errorMessage.put(IntroduceKind.CREATE_METHOD, "ERR_Invalid_Selection"); // NOI18N
            return null;
        }

        TreePath block = h.resolve(info);
        TreePath method = findMethod(block);

        if (method == null) {
            errorMessage.put(IntroduceKind.CREATE_METHOD, "ERR_Invalid_Selection"); // NOI18N
            return null;
        }

        if (method.getLeaf().getKind() == Kind.METHOD && ((MethodTree) method.getLeaf()).getParameters().contains(block.getLeaf())) {
            errorMessage.put(IntroduceKind.CREATE_METHOD, "ERR_Invalid_Selection"); // NOI18N
            return null;
        }

        Map<TypeMirror, TreePathHandle> typeVar2Def = new HashMap<TypeMirror, TreePathHandle>();
        List<TreePathHandle> typeVars = new LinkedList<TreePathHandle>();

        prepareTypeVars(method, info, typeVar2Def, typeVars);
        
        Element methodEl = info.getTrees().getElement(method);
        List<? extends StatementTree> parentStatements = getStatements(block);
        List<? extends StatementTree> statementsToWrap = parentStatements.subList(statements[0], statements[1] + 1);
        FlowResult flow = Flow.assignmentsForUse(info, method, cancel);

        if (flow == null || cancel.get()) {
            return null;
        }

        Map<Tree, Iterable<? extends TreePath>> assignmentsForUse = flow.getAssignmentsForUse();
        ScanStatement scanner = new ScanStatement(info, statementsToWrap.get(0), statementsToWrap.get(statementsToWrap.size() - 1), typeVar2Def, assignmentsForUse, cancel);
        Set<TypeMirror> exceptions = new HashSet<TypeMirror>();
        int index = 0;
        TypeMirror methodReturnType = info.getTypes().getNoType(TypeKind.VOID);

        if (methodEl != null && (methodEl.getKind() == ElementKind.METHOD || methodEl.getKind() == ElementKind.CONSTRUCTOR)) {
            ExecutableElement ee = (ExecutableElement) methodEl;

            scanner.localVariables.addAll(ee.getParameters());
            methodReturnType = ee.getReturnType();
        }

        scanner.scan(method, null);

        List<TreePath> pathsOfStatementsToWrap = new LinkedList<TreePath>();

        for (StatementTree s : parentStatements) {
            TreePath path = new TreePath(block, s);

            if (index >= statements[0] && index <= statements[1]) {
                exceptions.addAll(info.getTreeUtilities().getUncaughtExceptions(path));
                pathsOfStatementsToWrap.add(path);
            }

            index++;
        }

        boolean exitsFromAllBranches = Utilities.exitsFromAllBranchers(info, new TreePath(block, statementsToWrap.get(statementsToWrap.size() - 1)));

        String exitsError = scanner.verifyExits(exitsFromAllBranches);

        if (exitsError != null) {
            errorMessage.put(IntroduceKind.CREATE_METHOD, exitsError);
            return null;
        }
        
        Map<VariableElement, Boolean> mergedVariableUse = new LinkedHashMap<VariableElement, Boolean>(scanner.usedLocalVariables);

        for (Entry<VariableElement, Boolean> e : scanner.usedAfterSelection.entrySet()) {
            if (cancel.get()) return null;

            Boolean usedLocal = mergedVariableUse.get(e.getKey());

            if (usedLocal == null && Flow.definitellyAssigned(info, e.getKey(), pathsOfStatementsToWrap, cancel)) {
                mergedVariableUse.put(e.getKey(), true);
            } else {
                mergedVariableUse.put(e.getKey(), !(usedLocal == Boolean.FALSE) && e.getValue());
            }
        }

        if (cancel.get()) return null;

        Set<VariableElement> additionalLocalVariables = new LinkedHashSet<VariableElement>();
        Set<VariableElement> paramsVariables = new LinkedHashSet<VariableElement>();

        for (Entry<VariableElement, Boolean> e : mergedVariableUse.entrySet()) {
            if (e.getValue() == null || e.getValue()) {
                additionalLocalVariables.add(e.getKey());
            } else {
                paramsVariables.add(e.getKey());
                additionalLocalVariables.remove(e.getKey());
            }
        }

        List<TreePathHandle> params = new LinkedList<TreePathHandle>();

        for (VariableElement ve : paramsVariables) {
            params.add(TreePathHandle.create(info.getTrees().getPath(ve), info));
        }

        additionalLocalVariables.removeAll(paramsVariables);//needed?
        additionalLocalVariables.removeAll(scanner.selectionLocalVariables);

        List<TypeMirrorHandle> additionaLocalTypes = new LinkedList<TypeMirrorHandle>();
        List<String> additionaLocalNames = new LinkedList<String>();

        for (VariableElement ve : additionalLocalVariables) {
            additionaLocalTypes.add(TypeMirrorHandle.create(ve.asType()));
            additionaLocalNames.add(ve.getSimpleName().toString());
        }

        List<TreePathHandle> exits = null;
        Tree lastStatement = statementsToWrap.get(statementsToWrap.size() - 1);
        
        if (parentStatements.get(parentStatements.size() - 1) == lastStatement) {
            TreePath search = block.getParentPath();
            Tree last = block.getLeaf();
            
            OUTTER: while (search != null) {
                switch (search.getLeaf().getKind()) {
                    case BLOCK:
                        List<? extends StatementTree> thisBlockStatements = ((BlockTree) search.getLeaf()).getStatements();
                        if (thisBlockStatements.get(thisBlockStatements.size() - 1) == last) break;
                        else break OUTTER;
                    case IF: break;
                    case METHOD:
                        Tree returnType = ((MethodTree) search.getLeaf()).getReturnType();
                        if (returnType == null || (returnType.getKind() == Kind.PRIMITIVE_TYPE && ((PrimitiveTypeTree) returnType).getPrimitiveTypeKind() == TypeKind.VOID)) {
                            exits = Collections.emptyList();
                        }
                        break OUTTER;
                    default:
                        break OUTTER;
                }
                
                last = search.getLeaf();
                search = search.getParentPath();
            }
        }
        
        if (exits == null) {
            exits = new LinkedList<TreePathHandle>();

            for (TreePath tp : scanner.selectionExits) {
                if(isInsideSameClass(tp, method))
                    exits.add(TreePathHandle.create(tp, info));
            }
        }

        TypeMirror returnType;
        TreePathHandle returnAssignTo;
        boolean declareVariableForReturnValue;

        Pattern p = Pattern.createPatternWithRemappableVariables(pathsOfStatementsToWrap, scanner.usedLocalVariables.keySet(), true);
        int duplicatesCount = Matcher.create(info).setCancel(cancel).match(p).size();

        if (!scanner.usedAfterSelection.isEmpty()) {
            VariableElement result = scanner.usedAfterSelection.keySet().iterator().next();

            returnType = result.asType();
            returnAssignTo = TreePathHandle.create(info.getTrees().getPath(result), info);
            declareVariableForReturnValue = scanner.selectionLocalVariables.contains(result);
        } else {
            if (!exits.isEmpty() && !exitsFromAllBranches) {
                returnType = info.getTypes().getPrimitiveType(TypeKind.BOOLEAN);
                returnAssignTo = null;
                declareVariableForReturnValue = false;
            } else {
                if (exitsFromAllBranches && scanner.hasReturns) {
                    returnType = methodReturnType;
                    returnAssignTo = null;
                    declareVariableForReturnValue = false;
                } else {
                    returnType = info.getTypes().getNoType(TypeKind.VOID);
                    returnAssignTo = null;
                    declareVariableForReturnValue = false;
                }
            }
        }

        Set<TypeMirrorHandle> exceptionHandles = new HashSet<TypeMirrorHandle>();

        for (TypeMirror tm : exceptions) {
            exceptionHandles.add(TypeMirrorHandle.create(tm));
        }

        typeVars.retainAll(scanner.usedTypeVariables);

        return new IntroduceMethodFix(info.getJavaSource(), h, params, additionaLocalTypes, additionaLocalNames, TypeMirrorHandle.create(returnType), returnAssignTo, declareVariableForReturnValue, exceptionHandles, exits, exitsFromAllBranches, statements[0], statements[1], duplicatesCount, typeVars, end);
    }

    private static boolean isInsideSameClass(TreePath one, TreePath two) {
        ClassTree oneClass = null;
        ClassTree twoClass = null;

        while (one.getLeaf().getKind() != Kind.COMPILATION_UNIT && one.getLeaf().getKind() != null) {
            Tree t = one.getLeaf();
            if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
                oneClass = (ClassTree) t;
                break;
            }
            one = one.getParentPath();
        }

        while (two.getLeaf().getKind() != Kind.COMPILATION_UNIT && two.getLeaf().getKind() != null) {
            Tree t = two.getLeaf();
            if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
                twoClass = (ClassTree) t;
                break;
            }
            two = two.getParentPath();
        }

        if (oneClass != null && oneClass.equals(twoClass))
            return true;
        
        return false;
    }

    static boolean checkConstantExpression(final CompilationInfo info, TreePath path) {
        class NotConstant extends Error {}
        try {
        new TreePathScanner<Void, Void>() {
            private final Set<Element> definedIn = new HashSet<Element>();
            @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                Element el = info.getTrees().getElement(getCurrentPath());
                if (el == null) throw new NotConstant();
                if (definedIn.contains(el)) return null;
                if (el.getKind().isClass() || el.getKind().isInterface()) return null;
                if (!el.getModifiers().contains(Modifier.STATIC)) throw new NotConstant();
                if (el.getKind() == ElementKind.FIELD && !el.getModifiers().contains(Modifier.FINAL)) throw new NotConstant();
                return super.visitIdentifier(node, p);
            }
            @Override public Void visitVariable(VariableTree node, Void p) {
                definedIn.add(info.getTrees().getElement(getCurrentPath()));
                return super.visitVariable(node, p);
            }
            @Override public Void visitMethod(MethodTree node, Void p) {
                definedIn.add(info.getTrees().getElement(getCurrentPath()));
                return super.visitMethod(node, p);
            }
        }.scan(path, null);
        } catch (NotConstant n) {
            return false;
        }
        return true;
    }

    private static final Set<ElementKind> LOCAL_VARIABLES = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER, ElementKind.RESOURCE_VARIABLE);

    private static TreePath findStatement(TreePath statementPath) {
        while (    statementPath != null
                && (   !StatementTree.class.isAssignableFrom(statementPath.getLeaf().getKind().asInterface())
                || (   statementPath.getParentPath() != null
                && statementPath.getParentPath().getLeaf().getKind() != Kind.BLOCK
                && statementPath.getParentPath().getLeaf().getKind() != Kind.CASE))) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(statementPath.getLeaf().getKind()))
                return null;

            statementPath = statementPath.getParentPath();
        }

        return statementPath;
    }

    private static TreePath findMethod(TreePath path) {
        while (path != null) {
            if (path.getLeaf().getKind() == Kind.METHOD) {
                return path;
            }

            if (   path.getLeaf().getKind() == Kind.BLOCK
                && path.getParentPath() != null
                && TreeUtilities.CLASS_TREE_KINDS.contains(path.getParentPath().getLeaf().getKind())) {
                //initializer:
                return path;
            }

            path = path.getParentPath();
        }

        return null;
    }

    private static TreePath findClass(TreePath path) {
        while (path != null) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind())) {
                return path;
            }

            path = path.getParentPath();
        }

        return null;
    }

    private static boolean isParentOf(TreePath parent, TreePath path) {
        Tree parentLeaf = parent.getLeaf();

        while (path != null && path.getLeaf() != parentLeaf) {
            path = path.getParentPath();
        }

        return path != null;
    }

    private static boolean isParentOf(TreePath parent, List<? extends TreePath> candidates) {
        for (TreePath tp : candidates) {
            if (!isParentOf(parent, tp))
                return false;
        }

        return true;
    }

    private static TreePath findAddPosition(CompilationInfo info, TreePath original, Set<? extends TreePath> candidates, int[] outPosition) {
        //find least common block holding all the candidates:
        TreePath statement = original;

        for (TreePath p : candidates) {
            Tree leaf = p.getLeaf();
            int  leafStart = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), leaf);
            int  stPathStart = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), statement.getLeaf());

            if (leafStart < stPathStart) {
                statement = p;
            }
        }

        List<TreePath> allCandidates = new LinkedList<TreePath>();

        allCandidates.add(original);
        allCandidates.addAll(candidates);

        statement = findStatement(statement);

        if (statement == null) {
            //XXX: well....
            return null;
        }

        while (statement.getParentPath() != null && !isParentOf(statement.getParentPath(), allCandidates)) {
            statement = statement.getParentPath();
        }

        //#126269: the common parent may not be block:
        while (statement.getParentPath() != null && statement.getParentPath().getLeaf().getKind() != Kind.BLOCK && statement.getParentPath().getLeaf().getKind() != Kind.CASE) {
            statement = statement.getParentPath();
        }

        if (statement.getParentPath() == null)
            return null;//XXX: log

        StatementTree statementTree = (StatementTree) statement.getLeaf();

        int index = getStatements(statement).indexOf(statementTree);

        if (index == (-1)) {
            //really strange...
            return null;
        }

        outPosition[0] = index;

        return statement;
    }

    private static int[] computeInitializeIn(final CompilationInfo info, TreePath firstOccurrence, Set<TreePath> occurrences) {
        int[] result = new int[] {7, 7};
        boolean inOneMethod = true;
        Tree currentMethod = findMethod(firstOccurrence).getLeaf();

        for (TreePath occurrence : occurrences) {
            TreePath method = findMethod(occurrence);

            if (method == null || currentMethod != method.getLeaf()) {
                inOneMethod = false;
                break;
            }
        }

        class Result extends RuntimeException {
            @Override
            public synchronized Throwable fillInStackTrace() {
                return null;
            }

        }
        class ReferencesLocalVariable extends TreePathScanner<Void, Void> {
            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                Element e = info.getTrees().getElement(getCurrentPath());

                if (e != null && LOCAL_VARIABLES.contains(e.getKind())) {
                    throw new Result();
                }

                return null;
            }
        }

        boolean referencesLocalvariables = false;

        try {
            new ReferencesLocalVariable().scan(firstOccurrence, null);
        } catch (Result r) {
            referencesLocalvariables = true;
        }

        if (!inOneMethod) {
            result[1] = IntroduceFieldPanel.INIT_FIELD | IntroduceFieldPanel.INIT_CONSTRUCTORS;
        }

        if (referencesLocalvariables) {
            result[0] = IntroduceFieldPanel.INIT_METHOD;
            result[1] = IntroduceFieldPanel.INIT_METHOD;
        }

        return result;
    }

    private static List<ExpressionTree> realArguments(final TreeMaker make, List<VariableElement> parameters) {
        List<ExpressionTree> realArguments = new LinkedList<ExpressionTree>();

        for (VariableElement p : parameters) {
            realArguments.add(make.Identifier(p.getSimpleName()));
        }

        return realArguments;
    }

    private static List<ExpressionTree> realArgumentsForTrees(final TreeMaker make, List<Union2<VariableElement, TreePath>> parameters) {
        List<ExpressionTree> realArguments = new LinkedList<ExpressionTree>();

        for (Union2<VariableElement, TreePath> p : parameters) {
            if (p.hasFirst()) {
                realArguments.add(make.Identifier(p.first().getSimpleName()));
            } else {
                realArguments.add((ExpressionTree) p.second().getLeaf());
            }
        }

        return realArguments;
    }

    private static List<VariableTree> createVariables(WorkingCopy copy, List<VariableElement> parameters) {
        final TreeMaker make = copy.getTreeMaker();
        List<VariableTree> formalArguments = new LinkedList<VariableTree>();

        for (VariableElement p : parameters) {
            TypeMirror tm = p.asType();
            Tree type = make.Type(tm);
            Name formalArgName = p.getSimpleName();
            Set<Modifier> formalArgMods = EnumSet.noneOf(Modifier.class);

            if (p.getModifiers().contains(Modifier.FINAL)) {
                formalArgMods.add(Modifier.FINAL);
            }

            formalArguments.add(make.Variable(make.Modifiers(formalArgMods), formalArgName, type, null));
        }

        return formalArguments;
    }

    private static List<ExpressionTree> typeHandleToTree(WorkingCopy copy, Set<TypeMirrorHandle> thrownTypes) {
        final TreeMaker make = copy.getTreeMaker();
        List<ExpressionTree> thrown = new LinkedList<ExpressionTree>();

        for (TypeMirrorHandle h : thrownTypes) {
            TypeMirror t = h.resolve(copy);

            if (t == null) {
                return null;
            }

            thrown.add((ExpressionTree) make.Type(t));
        }

        return thrown;
    }

    static final OffsetsBag introduceBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(IntroduceHint.class);

        if (bag == null) {
            doc.putProperty(IntroduceHint.class, bag = new OffsetsBag(doc));
        }

        return bag;
    }

    private static List<VariableElement> resolveVariables(CompilationInfo info, Collection<? extends TreePathHandle> handles) {
        List<VariableElement> vars = new LinkedList<VariableElement>();

        for (TreePathHandle tph : handles) {
            vars.add((VariableElement) tph.resolveElement(info));
        }

        return vars;
    }

    private static void prepareTypeVars(TreePath method, CompilationInfo info, Map<TypeMirror, TreePathHandle> typeVar2Def, List<TreePathHandle> typeVars) throws IllegalArgumentException {
        if (method.getLeaf().getKind() == Kind.METHOD) {
            MethodTree mt = (MethodTree) method.getLeaf();

            for (TypeParameterTree tv : mt.getTypeParameters()) {
                TreePath def = new TreePath(method, tv);
                TypeMirror type = info.getTrees().getTypeMirror(def);

                if (type != null && type.getKind() == TypeKind.TYPEVAR) {
                    TreePathHandle tph = TreePathHandle.create(def, info);

                    typeVar2Def.put(type, tph);
                    typeVars.add(tph);
                }
            }
        }
    }

    private static TreePath findAcceptableConstantTarget(CompilationInfo info, TreePath from) {
        boolean compileTimeConstant = info.getTreeUtilities().isCompileTimeConstantExpression(from);

        while (from != null) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(from.getLeaf().getKind())) {
                if (from.getParentPath().getLeaf().getKind() == Kind.COMPILATION_UNIT) return from;
                if (compileTimeConstant || ((ClassTree) from.getLeaf()).getModifiers().getFlags().contains(Modifier.STATIC)) {
                    /*TODO: should use TreeUtilities.isStaticContext?*/
                    return from;
                }
            }

            from = from.getParentPath();
        }

        return null;
    }

    private static final class ScanStatement extends TreePathScanner<Void, Void> {
        private static final int PHASE_BEFORE_SELECTION = 1;
        private static final int PHASE_INSIDE_SELECTION = 2;
        private static final int PHASE_AFTER_SELECTION = 3;

        private CompilationInfo info;
        private int phase = PHASE_BEFORE_SELECTION;
        private Tree firstInSelection;
        private Tree lastInSelection;
        private Set<VariableElement> localVariables = new HashSet<VariableElement>();
        private Map<VariableElement, Boolean> usedLocalVariables = new LinkedHashMap<VariableElement, Boolean>(); /*true if all uses have been definitelly assigned inside selection*/
        private Set<VariableElement> selectionLocalVariables = new HashSet<VariableElement>();
        private Map<VariableElement, Boolean> usedAfterSelection = new LinkedHashMap<VariableElement, Boolean>(); /*true if all uses have been definitelly assigned inside selection*/
        private Set<TreePath> selectionExits = new HashSet<TreePath>();
        private Set<Tree> treesSeensInSelection = new HashSet<Tree>();
        private final Map<TypeMirror, TreePathHandle> typeVar2Def;
        private final Map<Tree, Iterable<? extends TreePath>> assignmentsForUse;
        private Set<TreePathHandle> usedTypeVariables = new HashSet<TreePathHandle>();
        private boolean hasReturns = false;
        private boolean hasBreaks = false;
        private boolean hasContinues = false;
        private boolean secondPass = false;
        private boolean stopSecondPass = false;
        private final AtomicBoolean cancel;

        public ScanStatement(CompilationInfo info, Tree firstInSelection, Tree lastInSelection, Map<TypeMirror, TreePathHandle> typeVar2Def, Map<Tree, Iterable<? extends TreePath>> assignmentsForUse, AtomicBoolean cancel) {
            this.info = info;
            this.firstInSelection = firstInSelection;
            this.lastInSelection = lastInSelection;
            this.typeVar2Def = typeVar2Def;
            this.assignmentsForUse = assignmentsForUse;
            this.cancel = cancel;
        }

        @Override
        public Void scan(Tree tree, Void p) {
            if (stopSecondPass)
                return null;

            if (phase != PHASE_AFTER_SELECTION) {
                if (tree == firstInSelection) {
                    phase = PHASE_INSIDE_SELECTION;
                }

                if (phase == PHASE_INSIDE_SELECTION) {
                    treesSeensInSelection.add(tree);
                }
            }

            if (secondPass && tree == firstInSelection) {
                stopSecondPass = true;
                return null;
            }

            super.scan(tree, p);

            if (tree == lastInSelection) {
                phase = PHASE_AFTER_SELECTION;
            }

            return null;
        }

        @Override
        public Void visitVariable(VariableTree node, Void p) {
            Element e = info.getTrees().getElement(getCurrentPath());

            if (e != null && LOCAL_VARIABLES.contains(e.getKind())) {
                switch (phase) {
                    case PHASE_BEFORE_SELECTION:
                        localVariables.add((VariableElement) e);
                        break;
                    case PHASE_INSIDE_SELECTION:
                        selectionLocalVariables.add((VariableElement) e);
                        break;
                }
            }

            return super.visitVariable(node, p);
        }

        @Override
        public Void visitIdentifier(IdentifierTree node, Void p) {
            Element e = info.getTrees().getElement(getCurrentPath());

            if (e != null) {
                if (LOCAL_VARIABLES.contains(e.getKind())) {
                    switch (phase) {
                        case PHASE_INSIDE_SELECTION:
                            if (localVariables.contains(e) && usedLocalVariables.get(e) == null) {
                                Iterable<? extends TreePath> writes = assignmentsForUse.get(getCurrentPath().getLeaf());
                                Boolean definitellyAssignedInSelection = true;

                                if (writes != null) {
                                    for (TreePath w : writes) {
                                        if (w == null || !treesSeensInSelection.contains(w.getLeaf())) {
                                            definitellyAssignedInSelection = false;
                                            break;
                                        }
                                    }
                                } else if (getCurrentPath().getParentPath().getLeaf().getKind() == Kind.ASSIGNMENT) {
                                    definitellyAssignedInSelection = null;
                                } else {
                                    definitellyAssignedInSelection = false;
                                }

                                usedLocalVariables.put((VariableElement) e, definitellyAssignedInSelection);
                            }
                            break;
                        case PHASE_AFTER_SELECTION:
                            Iterable<? extends TreePath> writes = assignmentsForUse.get(getCurrentPath().getLeaf());
                            boolean assignedInSelection = false;
                            boolean definitellyAssignedInSelection = true;

                            if (writes != null) {
                                for (TreePath w : writes) {
                                    if (w != null && treesSeensInSelection.contains(w.getLeaf())) {
                                        assignedInSelection = true;
                                    }
                                    if (w == null || !treesSeensInSelection.contains(w.getLeaf())) {
                                        definitellyAssignedInSelection = false;
                                    }
                                }
                            }

                            if (assignedInSelection) {
                                usedAfterSelection.put((VariableElement) e, definitellyAssignedInSelection);
                            }
                            break;
                    }
                }
            }

            if (phase == PHASE_INSIDE_SELECTION) {
                TypeMirror type = info.getTrees().getTypeMirror(getCurrentPath());

                if (type != null) {
                    TreePathHandle def = typeVar2Def.get(type);

                    usedTypeVariables.add(def);
                }
            }

            return super.visitIdentifier(node, p);
        }

        @Override
        public Void visitReturn(ReturnTree node, Void p) {
            if (phase == PHASE_INSIDE_SELECTION) {
                selectionExits.add(getCurrentPath());
                hasReturns = true;
            }
            return super.visitReturn(node, p);
        }

        @Override
        public Void visitBreak(BreakTree node, Void p) {
            if (phase == PHASE_INSIDE_SELECTION && !treesSeensInSelection.contains(info.getTreeUtilities().getBreakContinueTarget(getCurrentPath()))) {
                selectionExits.add(getCurrentPath());
                hasBreaks = true;
            }
            return super.visitBreak(node, p);
        }

        @Override
        public Void visitContinue(ContinueTree node, Void p) {
            if (phase == PHASE_INSIDE_SELECTION && !treesSeensInSelection.contains(info.getTreeUtilities().getBreakContinueTarget(getCurrentPath()))) {
                selectionExits.add(getCurrentPath());
                hasContinues = true;
            }
            return super.visitContinue(node, p);
        }

        @Override
        public Void visitWhileLoop(WhileLoopTree node, Void p) {
            super.visitWhileLoop(node, p);

            if (phase == PHASE_AFTER_SELECTION) {
                //#109663&#112552:
                //the selection was inside the while-loop, the variables inside the
                //condition&statement of the while loop need to be considered to be used again after the loop:
                if (!secondPass) {
                    secondPass = true;
                    scan(node.getCondition(), p);
                    scan(node.getStatement(), p);
                    secondPass = false;
                    stopSecondPass = false;
                }
            }

            return null;
        }

        @Override
        public Void visitForLoop(ForLoopTree node, Void p) {
            super.visitForLoop(node, p);

            if (phase == PHASE_AFTER_SELECTION) {
                //#109663&#112552:
                //the selection was inside the for-loop, the variables inside the
                //condition, update and statement parts of the for loop need to be considered to be used again after the loop:
                if (!secondPass) {
                    secondPass = true;
                    scan(node.getCondition(), p);
                    scan(node.getUpdate(), p);
                    scan(node.getStatement(), p);
                    secondPass = false;
                    stopSecondPass = false;
                }
            }

            return null;
        }

        @Override
        public Void visitDoWhileLoop(DoWhileLoopTree node, Void p) {
            super.visitDoWhileLoop(node, p);

            if (phase == PHASE_AFTER_SELECTION) {
                //#109663&#112552:
                //the selection was inside the do-while, the variables inside the
                //statement part of the do-while loop need to be considered to be used again after the loop:
                if (!secondPass) {
                    secondPass = true;
                    scan(node.getStatement(), p);
                    secondPass = false;
                    stopSecondPass = false;
                }
            }

            return null;
        }

        private String verifyExits(boolean exitsFromAllBranches) {
            int i = 0;

            i += hasReturns ? 1 : 0;
            i += hasBreaks ? 1 : 0;
            i += hasContinues ? 1 : 0;

            if (i > 1) {
                return "ERR_Too_Many_Different_Exits"; // NOI18N
            }

            if ((exitsFromAllBranches ? 0 : i) + usedAfterSelection.size() > 1) {
                return "ERR_Too_Many_Return_Values"; // NOI18N
            }

            StatementTree breakOrContinueTarget = null;
            boolean returnValueComputed = false;
            TreePath returnValue = null;

            for (TreePath tp : selectionExits) {
                if (tp.getLeaf().getKind() == Kind.RETURN) {
                    if (!exitsFromAllBranches) {
                        ReturnTree rt = (ReturnTree) tp.getLeaf();
                        TreePath currentReturnValue = rt.getExpression() != null ? new TreePath(tp, rt.getExpression()) : null;

                        if (!returnValueComputed) {
                            returnValue = currentReturnValue;
                            returnValueComputed = true;
                        } else {
                            if (returnValue != null && currentReturnValue != null) {
                                Set<TreePath> candidates = SourceUtils.computeDuplicates(info, returnValue, currentReturnValue, cancel);

                                if (candidates.size() != 1 || candidates.iterator().next().getLeaf() != rt.getExpression()) {
                                    return "ERR_Different_Return_Values"; // NOI18N
                                }
                            } else {
                                if (returnValue != currentReturnValue) {
                                    return "ERR_Different_Return_Values"; // NOI18N
                                }
                            }
                        }
                    }
                } else {
                    StatementTree target = info.getTreeUtilities().getBreakContinueTarget(tp);

                    if (breakOrContinueTarget == null) {
                        breakOrContinueTarget = target;
                    }

                    if (breakOrContinueTarget != target)
                        return "ERR_Break_Mismatch"; // NOI18N
                }
            }

            return null;
        }
    }


    private static void removeFromParent(WorkingCopy parameter, TreePath what) throws IllegalAccessException {
        final TreeMaker make = parameter.getTreeMaker();
        Tree parentTree = what.getParentPath().getLeaf();
        Tree original = what.getLeaf();
        Tree newParent;

        switch (parentTree.getKind()) {
            case BLOCK:
                newParent = make.removeBlockStatement((BlockTree) parentTree, (StatementTree) original);
                break;
            case CASE:
                newParent = make.removeCaseStatement((CaseTree) parentTree, (StatementTree) original);
                break;
            default:
                throw new IllegalAccessException(parentTree.getKind().toString());
        }

        parameter.rewrite(parentTree, newParent);
    }

    //XXX: duplicate from CopyFinder:
    public static List<? extends StatementTree> getStatements(TreePath firstLeaf) {
        switch (firstLeaf.getParentPath().getLeaf().getKind()) {
            case BLOCK:
                return ((BlockTree) firstLeaf.getParentPath().getLeaf()).getStatements();
            case CASE:
                return ((CaseTree) firstLeaf.getParentPath().getLeaf()).getStatements();
            default:
                return Collections.singletonList((StatementTree) firstLeaf.getLeaf());
        }
    }

    private static Map<Tree, TreePath> createTree2TreePathMap(TreePath pathToClass) {
        Map<Tree, TreePath> classNormalization = new IdentityHashMap<Tree, TreePath>();
        TreePath temp = pathToClass;

        while (temp != null) {
            classNormalization.put(temp.getLeaf(), temp);
            temp = temp.getParentPath();
        }

        return classNormalization;
    }

    private static TreePath findTargetClassWithDuplicates(TreePath pathToClass, Collection<TreePath> duplicates) {
        TreePath targetClassWithDuplicates = pathToClass;
        Map<Tree, TreePath> classNormalization = createTree2TreePathMap(pathToClass);

        for (TreePath p : duplicates) {
            while (p != null) {
                if (classNormalization.containsKey(p.getLeaf())) {
                    classNormalization = createTree2TreePathMap(targetClassWithDuplicates = p);
                    break;
                }
                p = p.getParentPath();
            }
        }

        assert targetClassWithDuplicates != null;

        while (targetClassWithDuplicates != null && !TreeUtilities.CLASS_TREE_KINDS.contains(targetClassWithDuplicates.getLeaf().getKind())) {
            targetClassWithDuplicates = targetClassWithDuplicates.getParentPath();
        }

        if (targetClassWithDuplicates == null) {
            //strange...
            targetClassWithDuplicates = pathToClass;
        }
        
        return targetClassWithDuplicates;
    }
    
    private static ClassTree insertField(final WorkingCopy parameter, ClassTree clazz, VariableTree fieldToAdd, Set<Tree> allNewUses, int offset) {
        ClassTree nueClass = INSERT_CLASS_MEMBER.insertClassMember(parameter, clazz, fieldToAdd, offset);

        class Contains extends TreeScanner<Boolean, Set<Tree>> {
            @Override public Boolean reduce(Boolean r1, Boolean r2) {
                return r1 == Boolean.TRUE || r2 == Boolean.TRUE;
            }
            @Override public Boolean scan(Tree tree, Set<Tree> searchFor) {
                if (tree != null && searchFor.contains(tree)) return true;
                return super.scan(tree, searchFor);
            }
        }

        int i = 0;
        int insertLocation = -1;
        boolean newFieldStatic = fieldToAdd.getModifiers().getFlags().contains(Modifier.STATIC);

        for (Tree member : nueClass.getMembers()) {
            i++;
            if (member.getKind() == Kind.VARIABLE) {
                VariableTree field = (VariableTree) member;

                if (   (field.getModifiers().getFlags().contains(Modifier.STATIC) ^ newFieldStatic)
                    || new Contains().scan(field.getInitializer(), allNewUses) != Boolean.TRUE) {
                    continue;
                }
            } else if (member.getKind() == Kind.BLOCK) {
                BlockTree block = (BlockTree) member;

                if (   (block.isStatic() ^ newFieldStatic)
                    || new Contains().scan(block, allNewUses) != Boolean.TRUE) {
                    continue;
                }
            } else if (member == fieldToAdd) {
                break;
            } else {
                continue;
            }

            insertLocation = i - 1;
            break;
        }
        
        TreePath clazzPath = TreePath.getPath(parameter.getCompilationUnit(), clazz); //TODO: efficiency
        final Set<Element> used = Collections.newSetFromMap(new IdentityHashMap<Element, Boolean>());
        final boolean statik = fieldToAdd.getModifiers().getFlags().contains(Modifier.STATIC);
        
        new TreePathScanner<Void, Void>() {
            @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                handleCurrentPath();
                return super.visitIdentifier(node, p); //To change body of generated methods, choose Tools | Templates.
            }
            @Override public Void visitMemberSelect(MemberSelectTree node, Void p) {
                handleCurrentPath();
                return super.visitMemberSelect(node, p); //To change body of generated methods, choose Tools | Templates.
            }
            private void handleCurrentPath() {
                Element el = parameter.getTrees().getElement(getCurrentPath());
                
                if (el != null && el.getKind().isField() && el.getModifiers().contains(Modifier.STATIC) == statik) {
                    used.add(el);
                }
            }
        }.scan(new TreePath(clazzPath, fieldToAdd), null);
        
        List<? extends Tree> nueMembers = new ArrayList<Tree>(nueClass.getMembers());
        
        Collections.reverse(nueMembers);
        
        i = nueMembers.size() - 1;
        for (Tree member : nueMembers) {
            Element el = parameter.getTrees().getElement(new TreePath(clazzPath, member));
            
            if (el != null && used.contains(el)) {
                insertLocation = i;
                break;
            }
            
            i--;
            
            if (member == fieldToAdd || i < insertLocation)
                break;
        }

        if (insertLocation != (-1))
            nueClass = parameter.getTreeMaker().insertClassMember(clazz, insertLocation, fieldToAdd);

        return nueClass;
    }
    
    private static TypeMirror resolveType(CompilationInfo info, TreePath path) {
        TypeMirror tm = info.getTrees().getTypeMirror(path);
        
        if (tm != null && tm.getKind() == TypeKind.NULL) {
            List<? extends TypeMirror> targetType = CreateElementUtilities.resolveType(new HashSet<ElementKind>(), info, path.getParentPath(), path.getLeaf(), (int) info.getTrees().getSourcePositions().getStartPosition(path.getCompilationUnit(), path.getLeaf()), new TypeMirror[1], new int[1]);
            
            if (targetType != null && !targetType.isEmpty()) {
                tm = targetType.get(0);
            } else {
                TypeElement object = info.getElements().getTypeElement("java.lang.Object");
                tm = object != null ? object.asType() : null;
            }
        }
        
        return tm;
    }

    private static final class IntroduceFix implements Fix {

        private final String guessedName;
        private final TreePathHandle handle;
        private final JavaSource js;
        private final int numDuplicates;
        private final IntroduceKind kind;
        private final int offset;

        public IntroduceFix(TreePathHandle handle, JavaSource js, String guessedName, int numDuplicates, IntroduceKind kind, int offset) {
            this.handle = handle;
            this.js = js;
            this.guessedName = guessedName;
            this.numDuplicates = numDuplicates;
            this.kind = kind;
            this.offset = offset;
        }

        @Override
        public String toString() {
            return "[IntroduceFix:" + guessedName + ":" + numDuplicates + ":" + kind + "]"; // NOI18N
        }

        public String getKeyExt() {
            switch (kind) {
                case CREATE_CONSTANT:
                    return "IntroduceConstant"; //NOI18N
                case CREATE_VARIABLE:
                    return "IntroduceVariable"; //NOI18N
                default:
                    throw new IllegalStateException();
            }
        }

        public String getText() {
            return NbBundle.getMessage(IntroduceHint.class, "FIX_" + getKeyExt()); //NOI18N
        }

        public ChangeInfo implement() throws IOException, BadLocationException {
            JButton btnOk = new JButton( NbBundle.getMessage( IntroduceHint.class, "LBL_Ok" ) );
            JButton btnCancel = new JButton( NbBundle.getMessage( IntroduceHint.class, "LBL_Cancel" ) );
            IntroduceVariablePanel panel = new IntroduceVariablePanel(numDuplicates, guessedName, kind == IntroduceKind.CREATE_CONSTANT, handle.getKind() == Kind.VARIABLE, btnOk);
            String caption = NbBundle.getMessage(IntroduceHint.class, "CAP_" + getKeyExt()); //NOI18N
            DialogDescriptor dd = new DialogDescriptor(panel, caption, true, new Object[] {btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null, null);
            if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
                return null;//cancel
            }
            final String name = panel.getVariableName();
            final boolean replaceAll = panel.isReplaceAll();
            final boolean declareFinal = panel.isDeclareFinal();
            final Set<Modifier> access = kind == IntroduceKind.CREATE_CONSTANT ? panel.getAccess() : null;
            js.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);

                    TreePath resolved = handle.resolve(parameter);
                    
                    if (resolved == null) {
                        return ; //TODO...
                    }

                    TypeMirror tm = resolveType(parameter, resolved);

                    if (tm == null) {
                        return ; //TODO...
                    }

                    tm = Utilities.convertIfAnonymous(Utilities.resolveCapturedType(parameter, tm));

                    Tree original = resolved.getLeaf();
                    boolean variableRewrite = original.getKind() == Kind.VARIABLE;
                    ExpressionTree expression = !variableRewrite ? (ExpressionTree) resolved.getLeaf() : ((VariableTree) original).getInitializer();
                    ModifiersTree mods;
                    final TreeMaker make = parameter.getTreeMaker();
                    
                    boolean expressionStatement = resolved.getParentPath().getLeaf().getKind() == Tree.Kind.EXPRESSION_STATEMENT;

                    switch (kind) {
                        case CREATE_CONSTANT:
                            //find first class:
                            TreePath pathToClass = findAcceptableConstantTarget(parameter, resolved);

                            if (pathToClass == null) {
                                return ; //TODO...
                            }

                            Collection<TreePath> duplicates;

                            if (replaceAll) {
                                duplicates = SourceUtils.computeDuplicates(parameter, resolved, new TreePath(parameter.getCompilationUnit()), new AtomicBoolean());
                            } else {
                                duplicates = Collections.emptyList();
                            }

                            pathToClass = findTargetClassWithDuplicates(pathToClass, duplicates);

                            Set<Modifier> localAccess = EnumSet.of(Modifier.FINAL, Modifier.STATIC);

                            localAccess.addAll(access);

                            mods = make.Modifiers(localAccess);

                            VariableTree constant;

                            if (!variableRewrite) {
                                constant = make.Variable(mods, name, make.Type(tm), expression);
                                if (expressionStatement) {
                                    removeFromParent(parameter, resolved.getParentPath());
                                }
                            } else {
                                VariableTree originalVar = (VariableTree) original;
                                constant = make.Variable(mods, originalVar.getName(), originalVar.getType(), originalVar.getInitializer());
                                removeFromParent(parameter, resolved);
                                expressionStatement = true;
                            }
                            
                            Set<Tree> allNewUses = Collections.newSetFromMap(new IdentityHashMap<Tree, Boolean>());
                            
                            allNewUses.add(resolved.getLeaf());
                            
                            if (replaceAll) {
                                for (TreePath p : duplicates) {
                                    if (variableRewrite) {
                                        removeFromParent(parameter, p);
                                    } else {
                                        parameter.rewrite(p.getLeaf(), make.Identifier(name));
                                        allNewUses.add(p.getLeaf());
                                    }
                                }
                            }
                            
                            parameter.rewrite(pathToClass.getLeaf(), insertField(parameter, (ClassTree)pathToClass.getLeaf(), constant, allNewUses, offset));
                            break;
                        case CREATE_VARIABLE:
                            TreePath method        = findMethod(resolved);

                            if (method == null) {
                                return ; //TODO...
                            }

                            TreePath  statement;
                            int       index;

                            if (replaceAll) {
                                Set<TreePath> candidates = SourceUtils.computeDuplicates(parameter, resolved, method, new AtomicBoolean());
                                for (TreePath p : candidates) {
                                    Tree leaf = p.getLeaf();

                                    parameter.rewrite(leaf, make.Identifier(name));
                                }

                                int[] out = new int[1];
                                statement = findAddPosition(parameter, resolved, candidates, out);

                                if (statement == null) {
                                    return;
                                }

                                index = out[0];
                            } else {
                                int[] out = new int[1];
                                statement = findAddPosition(parameter, resolved, Collections.<TreePath>emptySet(), out);

                                if (statement == null) {
                                    return;
                                }

                                index = out[0];
                            }

                            List<StatementTree> nueStatements = new LinkedList<StatementTree>(getStatements(statement));
                            mods = make.Modifiers(declareFinal ? EnumSet.of(Modifier.FINAL) : EnumSet.noneOf(Modifier.class));
                            VariableTree newVariable = make.Variable(mods, name, make.Type(tm), expression);

                            nueStatements.add(index, newVariable);
                            
                            GeneratorUtilities.get(parameter).copyComments(resolved.getParentPath().getLeaf(), newVariable, true);
                            GeneratorUtilities.get(parameter).copyComments(resolved.getParentPath().getLeaf(), newVariable, false);

                            if (expressionStatement)
                                nueStatements.remove(resolved.getParentPath().getLeaf());

                            doReplaceInBlockCatchSingleStatement(parameter, new HashMap<Tree, Tree>(), statement, nueStatements);
                            break;
                    }

                    if (!expressionStatement) {
                        Tree origParent = resolved.getParentPath().getLeaf();
                        Tree newParent = parameter.getTreeUtilities().translate(origParent, Collections.singletonMap(resolved.getLeaf(), make.Identifier(name)));
                        parameter.rewrite(origParent, newParent);

                    }
                }
            }).commit();
            return null;
        }
    }

    private static final class IntroduceFieldFix implements Fix {

        private final String guessedName;
        private final TreePathHandle handle;
        private final JavaSource js;
        private final int numDuplicates;
        private final int[] initilizeIn;
        private final boolean statik;
        private final boolean allowFinalInCurrentMethod;
        private final int offset;

        public IntroduceFieldFix(TreePathHandle handle, JavaSource js, String guessedName, int numDuplicates, int[] initilizeIn, boolean statik, boolean allowFinalInCurrentMethod, int offset) {
            this.handle = handle;
            this.js = js;
            this.guessedName = guessedName;
            this.numDuplicates = numDuplicates;
            this.initilizeIn = initilizeIn;
            this.statik = statik;
            this.allowFinalInCurrentMethod = allowFinalInCurrentMethod;
            this.offset = offset;
        }

        public String getText() {
            return NbBundle.getMessage(IntroduceHint.class, "FIX_IntroduceField");
        }

        @Override
        public String toString() {
            return "[IntroduceField:" + guessedName + ":" + numDuplicates + ":" + statik + ":" + allowFinalInCurrentMethod + ":" + Arrays.toString(initilizeIn) + "]"; // NOI18N
        }

        public ChangeInfo implement() throws IOException, BadLocationException {
            JButton btnOk = new JButton( NbBundle.getMessage( IntroduceHint.class, "LBL_Ok" ) );
            btnOk.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(IntroduceHint.class, "AD_IntrHint_OK"));
            JButton btnCancel = new JButton( NbBundle.getMessage( IntroduceHint.class, "LBL_Cancel" ) );
            btnCancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(IntroduceHint.class, "AD_IntrHint_Cancel"));
            IntroduceFieldPanel panel = new IntroduceFieldPanel(guessedName, initilizeIn, numDuplicates, allowFinalInCurrentMethod, handle.getKind() == Kind.VARIABLE, btnOk);
            String caption = NbBundle.getMessage(IntroduceHint.class, "CAP_IntroduceField");
            DialogDescriptor dd = new DialogDescriptor(panel, caption, true, new Object[] {btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null, null);
            if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
                return null;//cancel
            }
            final String name = panel.getFieldName();
            final boolean replaceAll = panel.isReplaceAll();
            final boolean declareFinal = panel.isDeclareFinal();
            final Set<Modifier> access = panel.getAccess();
            final int initializeIn = panel.getInitializeIn();
            js.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);

                    TreePath resolved = handle.resolve(parameter);
                    TypeMirror tm = resolveType(parameter, resolved);

                    if (resolved == null || tm == null) {
                        return ; //TODO...
                    }

                    tm = Utilities.convertIfAnonymous(Utilities.resolveCapturedType(parameter, tm));

                    TreePath pathToClass = resolved;

                    while (pathToClass != null && !TreeUtilities.CLASS_TREE_KINDS.contains(pathToClass.getLeaf().getKind())) {
                        pathToClass = pathToClass.getParentPath();
                    }

                    if (pathToClass == null) {
                        return ; //TODO...
                    }

                    Tree original = resolved.getLeaf();
                    boolean variableRewrite = original.getKind() == Kind.VARIABLE;
                    ExpressionTree expression = !variableRewrite ? (ExpressionTree) resolved.getLeaf() : ((VariableTree) original).getInitializer();

                    Set<Modifier> mods = declareFinal ? EnumSet.of(Modifier.FINAL) : EnumSet.noneOf(Modifier.class);

                    if (statik) {
                        mods.add(Modifier.STATIC);
                    }

                    mods.addAll(access);
                    final TreeMaker make = parameter.getTreeMaker();

                    boolean isAnyOccurenceStatic = false;
                    Set<Tree> allNewUses = Collections.newSetFromMap(new IdentityHashMap<Tree, Boolean>());

                    allNewUses.add(resolved.getLeaf());
                    
                    Collection<TreePath> duplicates = new ArrayList<TreePath>();

                    if (replaceAll) {
                        for (TreePath p : SourceUtils.computeDuplicates(parameter, resolved, new TreePath(parameter.getCompilationUnit()), new AtomicBoolean())) {
                            if (variableRewrite) {
                                removeFromParent(parameter, p);
                            } else {
                                parameter.rewrite(p.getLeaf(), make.Identifier(name));
                                allNewUses.add(p.getLeaf());
                            }
                            Scope occurenceScope = parameter.getTrees().getScope(p);
                            if(parameter.getTreeUtilities().isStaticContext(occurenceScope))
                                isAnyOccurenceStatic = true;
                            duplicates.add(p);
                        }
                    }

                    if(!statik && isAnyOccurenceStatic) {
                        mods.add(Modifier.STATIC);
                    }

                    pathToClass = findTargetClassWithDuplicates(pathToClass, duplicates);

                    ModifiersTree modsTree = make.Modifiers(mods);
                    Tree parentTree = resolved.getParentPath().getLeaf();
                    VariableTree field;
                    TreePath toRemoveFromParent;
                    boolean expressionStatementRewrite = parentTree.getKind() == Kind.EXPRESSION_STATEMENT;

                    if (!variableRewrite) {
                        field = make.Variable(modsTree, name, make.Type(tm), initializeIn == IntroduceFieldPanel.INIT_FIELD ? expression : null);

                        if (!expressionStatementRewrite) {
                            Tree nueParent = parameter.getTreeUtilities().translate(parentTree, Collections.singletonMap(resolved.getLeaf(), make.Identifier(name)));
                            parameter.rewrite(parentTree, nueParent);
                            toRemoveFromParent = null;
                        } else {
                            toRemoveFromParent = resolved.getParentPath();
                        }
                    } else {
                        VariableTree originalVar = (VariableTree) original;

                        field = make.Variable(modsTree, name, originalVar.getType(), initializeIn == IntroduceFieldPanel.INIT_FIELD ? expression : null);

                        toRemoveFromParent = resolved;
                    }
                    
                    ClassTree nueClass = insertField(parameter, (ClassTree)pathToClass.getLeaf(), field, allNewUses, offset);

                    TreePath method        = findMethod(resolved);

                    if (method == null) {
                        return ; //TODO...
                    }

                    if (initializeIn == IntroduceFieldPanel.INIT_METHOD) {
                        TreePath statementPath = resolved;

                        statementPath = findStatement(statementPath);

                        if (statementPath == null) {
                            //XXX: well....
                            return ;
                        }

                        ExpressionStatementTree assignment = make.ExpressionStatement(make.Assignment(make.Identifier(name), expression));

                        if (!variableRewrite && !expressionStatementRewrite) {
                            BlockTree statements = (BlockTree) statementPath.getParentPath().getLeaf();
                            StatementTree statement = (StatementTree) statementPath.getLeaf();

                            int index = statements.getStatements().indexOf(statement);

                            if (index == (-1)) {
                                //really strange...
                                return ;
                            }

                            List<StatementTree> nueStatements = new LinkedList<StatementTree>(statements.getStatements());

                            if (expression.getKind() == Kind.NEW_ARRAY) {
                                List<? extends ExpressionTree> initializers = ((NewArrayTree) expression).getInitializers();
                                expression = make.NewArray(make.Type(((ArrayType)tm).getComponentType()), Collections.<ExpressionTree>emptyList(), initializers);
                            }

                            nueStatements.add(index, assignment);

                            BlockTree nueBlock = make.Block(nueStatements, false);

                            parameter.rewrite(statements, nueBlock);
                        } else {
                            parameter.rewrite(toRemoveFromParent.getLeaf(), assignment);
                            toRemoveFromParent = null;
                        }
                    }

                    if (initializeIn == IntroduceFieldPanel.INIT_CONSTRUCTORS) {
                        for (TreePath constructor : findConstructors(parameter, method)) {
                            //check for syntetic constructor:
                            if (parameter.getTreeUtilities().isSynthetic(constructor)) {
                                List<StatementTree> nueStatements = new LinkedList<StatementTree>();
                                ExpressionTree reference = make.Identifier(name);
                                Element clazz = parameter.getTrees().getElement(pathToClass);
                                ModifiersTree constrMods = clazz.getKind() != ElementKind.ENUM?make.Modifiers(EnumSet.of(Modifier.PUBLIC)):make.Modifiers(Collections.EMPTY_SET);

                                nueStatements.add(make.ExpressionStatement(make.Assignment(reference, expression)));

                                BlockTree nueBlock = make.Block(nueStatements, false);
                                MethodTree nueConstr = make.Method(constrMods, "<init>", null, Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), nueBlock, null); //NOI18N

                                nueClass = INSERT_CLASS_MEMBER.insertClassMember(parameter, nueClass, nueConstr, offset);

                                nueClass = make.removeClassMember(nueClass, constructor.getLeaf());
                                break;
                            }

                            boolean hasParameterOfTheSameName = false;
                            MethodTree constr = ((MethodTree) constructor.getLeaf());

                            for (VariableTree p : constr.getParameters()) {
                                if (name.equals(p.getName().toString())) {
                                    hasParameterOfTheSameName = true;
                                    break;
                                }
                            }

                            ExpressionTree reference = hasParameterOfTheSameName ? make.MemberSelect(make.Identifier("this"), name) : make.Identifier(name); // NOI18N
                            ExpressionStatementTree assignment = make.ExpressionStatement(make.Assignment(reference, expression));
                            
                            if ((!variableRewrite && !expressionStatementRewrite) || method.getLeaf() != constr) {
                                BlockTree origBody = constr.getBody();
                                List<StatementTree> nueStatements = new LinkedList<StatementTree>();

                                List<? extends StatementTree> origStatements = origBody.getStatements();
                                StatementTree canBeSuper = origStatements.get(0);
                                if (!parameter.getTreeUtilities().isSynthetic(TreePath.getPath(constructor, canBeSuper))) {
                                    nueStatements.add(canBeSuper);
                                }
                                nueStatements.add(assignment);
                                nueStatements.addAll(origStatements.subList(1, origStatements.size()));

                                BlockTree nueBlock = make.Block(nueStatements, false);

                                parameter.rewrite(origBody, nueBlock);
                            } else {
                                parameter.rewrite(toRemoveFromParent.getLeaf(), assignment);
                                toRemoveFromParent = null;
                            }
                        }
                    }

                    if (toRemoveFromParent != null) {
                        removeFromParent(parameter, toRemoveFromParent);
                    }

                    parameter.rewrite(pathToClass.getLeaf(), nueClass);
                }
            }).commit();
            return null;
        }
    }

    static final AttributeSet DUPE = AttributesUtilities.createImmutable(StyleConstants.Background, Color.GRAY);
    
    private static final class IntroduceMethodFix implements Fix {

        private final JavaSource js;

        private final TreePathHandle parentBlock;
        private final List<TreePathHandle> parameters;
        private final List<TypeMirrorHandle> additionalLocalTypes;
        private final List<String> additionalLocalNames;
        private final TypeMirrorHandle returnType;
        private final TreePathHandle returnAssignTo;
        private final boolean declareVariableForReturnValue;
        private final Set<TypeMirrorHandle> thrownTypes;
        private final List<TreePathHandle> exists;
        private final boolean exitsFromAllBranches;
        private final int from;
        private final int to;
        private final int duplicatesCount;
        private final List<TreePathHandle> typeVars;
        private final int offset;

        public IntroduceMethodFix(JavaSource js, TreePathHandle parentBlock, List<TreePathHandle> parameters, List<TypeMirrorHandle> additionalLocalTypes, List<String> additionalLocalNames, TypeMirrorHandle returnType, TreePathHandle returnAssignTo, boolean declareVariableForReturnValue, Set<TypeMirrorHandle> thrownTypes, List<TreePathHandle> exists, boolean exitsFromAllBranches, int from, int to, int duplicatesCount, List<TreePathHandle> typeVars, int offset) {
            this.js = js;
            this.parentBlock = parentBlock;
            this.parameters = parameters;
            this.additionalLocalTypes = additionalLocalTypes;
            this.additionalLocalNames = additionalLocalNames;
            this.returnType = returnType;
            this.returnAssignTo = returnAssignTo;
            this.declareVariableForReturnValue = declareVariableForReturnValue;
            this.thrownTypes = thrownTypes;
            this.exists = exists;
            this.exitsFromAllBranches = exitsFromAllBranches;
            this.from = from;
            this.to = to;
            this.duplicatesCount = duplicatesCount;
            this.typeVars = typeVars;
            this.offset = offset;
        }

        public String getText() {
            return NbBundle.getMessage(IntroduceHint.class, "FIX_IntroduceMethod");
        }

        public String toDebugString(CompilationInfo info) {
            return "[IntroduceMethod:" + from + ":" + to + "]"; // NOI18N
        }

        public ChangeInfo implement() throws Exception {
            JButton btnOk = new JButton( NbBundle.getMessage( IntroduceHint.class, "LBL_Ok" ) );
            JButton btnCancel = new JButton( NbBundle.getMessage( IntroduceHint.class, "LBL_Cancel" ) );
            IntroduceMethodPanel panel = new IntroduceMethodPanel("", duplicatesCount); //NOI18N
            panel.setOkButton( btnOk );
            String caption = NbBundle.getMessage(IntroduceHint.class, "CAP_IntroduceMethod");
            DialogDescriptor dd = new DialogDescriptor(panel, caption, true, new Object[] {btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null, null);
            if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
                return null;//cancel
            }
            final String name = panel.getMethodName();
            final Set<Modifier> access = panel.getAccess();
            final boolean replaceOther = panel.getReplaceOther();

            js.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy copy) throws Exception {
                    copy.toPhase(Phase.RESOLVED);

                    TreePath firstStatement = parentBlock.resolve(copy);
                    TypeMirror returnType = IntroduceMethodFix.this.returnType.resolve(copy);

                    if (firstStatement == null || returnType == null) {
                        return ; //TODO...
                    }

                    GeneratorUtilities.get(copy).importComments(firstStatement.getParentPath().getLeaf(), copy.getCompilationUnit());
                    
                    Scope s = copy.getTrees().getScope(firstStatement);
                    boolean isStatic = copy.getTreeUtilities().isStaticContext(s);
                    List<? extends StatementTree> statements = getStatements(firstStatement);
                    List<StatementTree> nueStatements = new LinkedList<StatementTree>();

                    nueStatements.addAll(statements.subList(0, from));

                    final TreeMaker make = copy.getTreeMaker();
                    List<VariableElement> parameters = resolveVariables(copy, IntroduceMethodFix.this.parameters);
                    List<ExpressionTree> realArguments = realArguments(make, parameters);

                    List<StatementTree> methodStatements = new LinkedList<StatementTree>();

                    Iterator<TypeMirrorHandle> additionalType = additionalLocalTypes.iterator();
                    Iterator<String> additionalName = additionalLocalNames.iterator();

                    while (additionalType.hasNext() && additionalName.hasNext()) {
                        TypeMirror tm = additionalType.next().resolve(copy);

                        if (tm == null) {
                            //XXX:
                            return ;
                        }

                        Tree type = make.Type(tm);

                        methodStatements.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), additionalName.next(), type, null));
                    }

                    if (from == to && statements.get(from).getKind() == Kind.BLOCK) {
                        methodStatements.addAll(((BlockTree) statements.get(from)).getStatements());
                    } else {
                        methodStatements.addAll(statements.subList(from, to + 1));
                    }

                    Tree returnTypeTree = make.Type(returnType);
                    ExpressionTree invocation = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier(name), realArguments);
                    boolean alreadyInvoked = false;

                    Callable<ReturnTree> ret = null;
                    final VariableElement returnAssignTo;

                    if (IntroduceMethodFix.this.returnAssignTo != null) {
                        returnAssignTo = (VariableElement) IntroduceMethodFix.this.returnAssignTo.resolveElement(copy);

                        if (returnAssignTo == null) {
                            return; //TODO...
                        }
                    } else {
                        returnAssignTo = null;
                    }

                    if (returnAssignTo != null) {
                        ret = new Callable<ReturnTree>() {
                            @Override public ReturnTree call() throws Exception {
                                return make.Return(make.Identifier(returnAssignTo.getSimpleName()));
                            }
                        };
                        if (declareVariableForReturnValue) {
                            nueStatements.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), returnAssignTo.getSimpleName(), returnTypeTree, invocation));
                            alreadyInvoked = true;
                        } else {
                            invocation = make.Assignment(make.Identifier(returnAssignTo.getSimpleName()), invocation);
                        }
                    }

                    if (!exists.isEmpty()) {
                        TreePath handle = null;

                        handle = exists.iterator().next().resolve(copy);

                        if (handle == null) {
                            return ; //TODO...
                        }

                        assert handle != null;

                        if (exitsFromAllBranches && handle.getLeaf().getKind() == Kind.RETURN && returnAssignTo == null && returnType.getKind() != TypeKind.VOID) {
                            nueStatements.add(make.Return(invocation));
                        } else {
                            if (ret == null) {
                                ret = new Callable<ReturnTree>() {
                                    @Override public ReturnTree call() throws Exception {
                                        if (exitsFromAllBranches) {
                                            return make.Return(null);
                                        } else {
                                            return make.Return(make.Literal(true));
                                        }
                                    }
                                };
                            }

                            for (TreePathHandle h : exists) {
                                TreePath resolved = h.resolve(copy);

                                if (resolved == null) {
                                    return ; //TODO...
                                }

                                ReturnTree r = ret.call();

                                GeneratorUtilities.get(copy).copyComments(resolved.getLeaf(), r, false);
                                GeneratorUtilities.get(copy).copyComments(resolved.getLeaf(), r, true);
                                
                                copy.rewrite(resolved.getLeaf(), r);
                            }

                            StatementTree branch = null;

                            switch (handle.getLeaf().getKind()) {
                                case BREAK:
                                    branch = make.Break(((BreakTree) handle.getLeaf()).getLabel());
                                    break;
                                case CONTINUE:
                                    branch = make.Continue(((ContinueTree) handle.getLeaf()).getLabel());
                                    break;
                                case RETURN:
                                    branch = make.Return(((ReturnTree) handle.getLeaf()).getExpression());
                                    break;
                            }

                            if (returnAssignTo != null || exitsFromAllBranches) {
                                nueStatements.add(make.ExpressionStatement(invocation));
                                nueStatements.add(branch);
                            } else {
                                nueStatements.add(make.If(make.Parenthesized(invocation), branch, null));
                                methodStatements.add(make.Return(make.Literal(false)));
                            }
                        }

                        alreadyInvoked = true;
                    } else {
                        if (ret != null) {
                            methodStatements.add(ret.call());
                        }
                    }

                    if (!alreadyInvoked)
                        nueStatements.add(make.ExpressionStatement(invocation));

                    nueStatements.addAll(statements.subList(to + 1, statements.size()));
                    
                    Map<Tree, Tree> rewritten = new IdentityHashMap<Tree, Tree>();

                    doReplaceInBlockCatchSingleStatement(copy, rewritten, firstStatement, nueStatements);
                    
                    if (replaceOther) {
                        //handle duplicates
                        Document doc = copy.getDocument();
                        List<TreePath> statementsPaths = new LinkedList<TreePath>();

                        for (StatementTree t : statements.subList(from, to + 1)) {
                            statementsPaths.add(new TreePath(firstStatement.getParentPath(), t));
                        }

                        Pattern p = Pattern.createPatternWithRemappableVariables(statementsPaths, parameters, true);

                        for (Occurrence desc : Matcher.create(copy).setCancel(new AtomicBoolean()).match(p)) {
                            TreePath firstLeaf = desc.getOccurrenceRoot();
                            List<? extends StatementTree> parentStatements = getStatements(new TreePath(new TreePath(firstLeaf.getParentPath().getParentPath(), resolveRewritten(rewritten, firstLeaf.getParentPath().getLeaf())), firstLeaf.getLeaf()));
                            int dupeStart = parentStatements.indexOf(firstLeaf.getLeaf());
                            int startOff = (int) copy.getTrees().getSourcePositions().getStartPosition(copy.getCompilationUnit(), parentStatements.get(dupeStart));
                            int endOff = (int) copy.getTrees().getSourcePositions().getEndPosition(copy.getCompilationUnit(), parentStatements.get(dupeStart + statementsPaths.size() - 1));

                            if (!shouldReplaceDuplicate(doc, startOff, endOff)) continue;

                            List<StatementTree> newStatements = new LinkedList<StatementTree>();

                            newStatements.addAll(parentStatements.subList(0, dupeStart));

                            //XXX:
                            List<Union2<VariableElement, TreePath>> dupeParameters = new LinkedList<Union2<VariableElement, TreePath>>();

                            for (VariableElement ve : parameters) {
                                if (desc.getVariablesRemapToTrees().containsKey(ve)) {
                                    dupeParameters.add(Union2.<VariableElement, TreePath>createSecond(desc.getVariablesRemapToTrees().get(ve)));
                                } else {
                                    dupeParameters.add(Union2.<VariableElement, TreePath>createFirst(ve));
                                }
                            }

                            List<ExpressionTree> dupeRealArguments = realArgumentsForTrees(make, dupeParameters);
                            ExpressionTree dupeInvocation = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier(name), dupeRealArguments);

                            if (returnAssignTo != null) {
                                TreePath remappedTree = desc.getVariablesRemapToTrees().containsKey(returnAssignTo) ? desc.getVariablesRemapToTrees().get(returnAssignTo) : null;
                                VariableElement remappedElement = desc.getVariablesRemapToElement().containsKey(returnAssignTo) ? (VariableElement) desc.getVariablesRemapToElement().get(returnAssignTo) : null;
//                                VariableElement dupeReturnAssignTo = mdd.variablesRemapToTrees.containsKey(returnAssignTo) ? (VariableElement) mdd.variablesRemapToTrees.get(returnAssignTo) : returnAssignTo;
                                if (declareVariableForReturnValue) {
                                    assert remappedElement != null || remappedTree == null;
                                    Name name = remappedElement != null ? remappedElement.getSimpleName() : returnAssignTo.getSimpleName();
                                    newStatements.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, returnTypeTree/*???: more specific type?*/, invocation));
                                    dupeInvocation = null;
                                } else {
                                    ExpressionTree sel = remappedTree != null ? (ExpressionTree) remappedTree.getLeaf()
                                                                              : remappedElement != null ? make.Identifier(remappedElement.getSimpleName())
                                                                                                        : make.Identifier(returnAssignTo.getSimpleName());
                                    dupeInvocation = make.Assignment(sel, dupeInvocation);
                                }
                            }

                            if (dupeInvocation != null)
                                newStatements.add(make.ExpressionStatement(dupeInvocation));

                            newStatements.addAll(parentStatements.subList(dupeStart + statementsPaths.size(), parentStatements.size()));

                            doReplaceInBlockCatchSingleStatement(copy, rewritten, firstLeaf, newStatements);
                        }

                        introduceBag(doc).clear();
                        //handle duplicates end
                    }

                    Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);

                    if (isStatic) {
                        modifiers.add(Modifier.STATIC);
                    }

                    modifiers.addAll(access);

                    ModifiersTree mods = make.Modifiers(modifiers);
                    List<VariableTree> formalArguments = createVariables(copy, parameters);

                    if (formalArguments == null) {
                        return ; //XXX
                    }

                    List<ExpressionTree> thrown = typeHandleToTree(copy, thrownTypes);

                    if (thrownTypes == null) {
                        return; //XXX
                    }

                    List<TypeParameterTree> typeVars = new LinkedList<TypeParameterTree>();

                    for (TreePathHandle tph : IntroduceMethodFix.this.typeVars) {
                        typeVars.add((TypeParameterTree) tph.resolve(copy).getLeaf());
                    }

                    MethodTree method = make.Method(mods, name, returnTypeTree, typeVars, formalArguments, thrown, make.Block(methodStatements, false), null);

                    TreePath pathToClass = findClass(firstStatement);

                    assert pathToClass != null;
                    
                    Tree parent = findMethod(firstStatement).getLeaf();
                    ClassTree nueClass = INSERT_CLASS_MEMBER.insertClassMember(copy, (ClassTree)pathToClass.getLeaf(), method, offset);

                    copy.rewrite(pathToClass.getLeaf(), nueClass);
                }
            }).commit();

            return null;
        }

    }

    private static Tree resolveRewritten(Map<Tree, Tree> rewritten, Tree t) {
        while (rewritten.containsKey(t)) {
            t = rewritten.get(t);
        }
        
        return t;
    }
    private static void doReplaceInBlockCatchSingleStatement(WorkingCopy copy, Map<Tree, Tree> rewritten, TreePath firstLeaf, List<? extends StatementTree> newStatements) {
        TreeMaker make = copy.getTreeMaker();
        Tree toReplace = resolveRewritten(rewritten, firstLeaf.getParentPath().getLeaf());
        Tree nueTree;

        switch (toReplace.getKind()) {
            case METHOD:
                toReplace = ((MethodTree) toReplace).getBody();
                //intentional fall-through
            case BLOCK:
                nueTree = make.Block(newStatements, ((BlockTree) toReplace).isStatic());
                break;
            case CASE:
                nueTree = make.Case(((CaseTree) toReplace).getExpression(), newStatements);
                break;
            default:
                assert getStatements(firstLeaf).size() == 1 : getStatements(firstLeaf).toString();
                assert newStatements.size() == 1 : newStatements.toString();
                toReplace = firstLeaf.getLeaf();
                nueTree = newStatements.get(0);
                break;
        }

        copy.rewrite(toReplace, nueTree);
        rewritten.put(toReplace, nueTree);
    }

    private static final class IntroduceExpressionBasedMethodFix implements Fix {

        private final JavaSource js;

        private final TreePathHandle expression;
        private final List<TreePathHandle> parameters;
        private final Set<TypeMirrorHandle> thrownTypes;
        private final int duplicatesCount;
        private final List<TreePathHandle> typeVars;
        private final int offset;

        public IntroduceExpressionBasedMethodFix(JavaSource js, TreePathHandle expression, List<TreePathHandle> parameters, Set<TypeMirrorHandle> thrownTypes, int duplicatesCount, List<TreePathHandle> typeVars, int offset) {
            this.js = js;
            this.expression = expression;
            this.parameters = parameters;
            this.thrownTypes = thrownTypes;
            this.duplicatesCount = duplicatesCount;
            this.typeVars = typeVars;
            this.offset = offset;
        }

        public String getText() {
            return NbBundle.getMessage(IntroduceHint.class, "FIX_IntroduceMethod");
        }

        public String toString() {
            return "[IntroduceExpressionBasedMethodFix]"; // NOI18N
        }

        public ChangeInfo implement() throws Exception {
            JButton btnOk = new JButton( NbBundle.getMessage( IntroduceHint.class, "LBL_Ok" ) );
            JButton btnCancel = new JButton( NbBundle.getMessage( IntroduceHint.class, "LBL_Cancel" ) );
            IntroduceMethodPanel panel = new IntroduceMethodPanel("", duplicatesCount); //NOI18N
            panel.setOkButton( btnOk );
            String caption = NbBundle.getMessage(IntroduceHint.class, "CAP_IntroduceMethod");
            DialogDescriptor dd = new DialogDescriptor(panel, caption, true, new Object[] {btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null, null);
            if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
                return null;//cancel
            }
            final String name = panel.getMethodName();
            final Set<Modifier> access = panel.getAccess();
            final boolean replaceOther = panel.getReplaceOther();

            js.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy copy) throws Exception {
                    copy.toPhase(Phase.RESOLVED);

                    TreePath expression = IntroduceExpressionBasedMethodFix.this.expression.resolve(copy);
                    TypeMirror returnType = expression != null ? resolveType(copy, expression) : null;

                    if (expression == null || returnType == null) {
                        return ; //TODO...
                    }

                    returnType = Utilities.convertIfAnonymous(Utilities.resolveCapturedType(copy, returnType));

                    final TreeMaker make = copy.getTreeMaker();
                    Tree returnTypeTree = make.Type(returnType);
                    List<VariableElement> parameters = resolveVariables(copy, IntroduceExpressionBasedMethodFix.this.parameters);
                    List<ExpressionTree> realArguments = realArguments(make, parameters);

                    ExpressionTree invocation = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier(name), realArguments);

                    Scope s = copy.getTrees().getScope(expression);
                    boolean isStatic = copy.getTreeUtilities().isStaticContext(s);

                    Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);

                    if (isStatic) {
                        modifiers.add(Modifier.STATIC);
                    }

                    modifiers.addAll(access);

                    ModifiersTree mods = make.Modifiers(modifiers);
                    List<VariableTree> formalArguments = createVariables(copy, parameters);

                    if (formalArguments == null) {
                        return ; //XXX
                    }

                    List<ExpressionTree> thrown = typeHandleToTree(copy, thrownTypes);

                    if (thrownTypes == null) {
                        return ; //XXX
                    }

                    List<StatementTree> methodStatements = new LinkedList<StatementTree>();

                    methodStatements.add(make.Return((ExpressionTree) expression.getLeaf()));

                    List<TypeParameterTree> typeVars = new LinkedList<TypeParameterTree>();

                    for (TreePathHandle tph : IntroduceExpressionBasedMethodFix.this.typeVars) {
                        typeVars.add((TypeParameterTree) tph.resolve(copy).getLeaf());
                    }

                    MethodTree method = make.Method(mods, name, returnTypeTree, typeVars, formalArguments, thrown, make.Block(methodStatements, false), null);
                    TreePath pathToClass = findClass(expression);

                    assert pathToClass != null;

                    Tree parent = findMethod(expression).getLeaf();
                    ClassTree nueClass = INSERT_CLASS_MEMBER.insertClassMember(copy, (ClassTree)pathToClass.getLeaf(), method, offset);
                    
                    copy.rewrite(pathToClass.getLeaf(), nueClass);

                    Tree parentTree = expression.getParentPath().getLeaf();
                    Tree nueParent = copy.getTreeUtilities().translate(parentTree, Collections.singletonMap(expression.getLeaf(), invocation));
                    copy.rewrite(parentTree, nueParent);

                    if (replaceOther) {
                        //handle duplicates
                        Document doc = copy.getDocument();
                        Pattern p = Pattern.createPatternWithRemappableVariables(expression, parameters, true);

                        for (Occurrence desc : Matcher.create(copy).setCancel(new AtomicBoolean()).match(p)) {
                            TreePath firstLeaf = desc.getOccurrenceRoot();
                            int startOff = (int) copy.getTrees().getSourcePositions().getStartPosition(copy.getCompilationUnit(), firstLeaf.getLeaf());
                            int endOff = (int) copy.getTrees().getSourcePositions().getEndPosition(copy.getCompilationUnit(), firstLeaf.getLeaf());

                            if (!shouldReplaceDuplicate(doc, startOff, endOff)) continue;

                            //XXX:
                            List<Union2<VariableElement, TreePath>> dupeParameters = new LinkedList<Union2<VariableElement, TreePath>>();

                            for (VariableElement ve : parameters) {
                                if (desc.getVariablesRemapToTrees().containsKey(ve)) {
                                    dupeParameters.add(Union2.<VariableElement, TreePath>createSecond(desc.getVariablesRemapToTrees().get(ve)));
                                } else {
                                    dupeParameters.add(Union2.<VariableElement, TreePath>createFirst(ve));
                                }
                            }

                            List<ExpressionTree> dupeRealArguments = realArgumentsForTrees(make, dupeParameters);
                            ExpressionTree dupeInvocation = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier(name), dupeRealArguments);

                            copy.rewrite(firstLeaf.getLeaf(), dupeInvocation);
                        }

                        introduceBag(doc).clear();
                        //handle duplicates end
                    }
                }
            }).commit();

            return null;
        }

    }
    
    private static boolean shouldReplaceDuplicate(final Document doc, final int startOff, final int endOff) {
        introduceBag(doc).clear();
        introduceBag(doc).addHighlight(startOff, endOff, DUPE);

        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                JTextComponent c = EditorRegistry.lastFocusedComponent();

                if (c != null && c.getDocument() == doc) {
                    try {
                        Rectangle start = c.modelToView(startOff);
                        Rectangle end = c.modelToView(endOff);
                        int sx = Math.min(start.x, end.x);
                        int dx = Math.max(start.x + start.width, end.x + end.width);
                        int sy = Math.min(start.y, end.y);
                        int dy = Math.max(start.y + start.height, end.y + end.height);

                        c.scrollRectToVisible(new Rectangle(sx, sy, dx - sx, dy - sy));
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });

        String title = NbBundle.getMessage(IntroduceHint.class, "TTL_DuplicateMethodPiece");
        String message = NbBundle.getMessage(IntroduceHint.class, "MSG_DuplicateMethodPiece");

        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.YES_NO_OPTION);

        return DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION;
    }

    public static final class HLFImpl implements HighlightsLayerFactory {

        public HighlightsLayer[] createLayers(Context context) {
            return new HighlightsLayer[] {
                HighlightsLayer.create(IntroduceHint.class.getName(), ZOrder.TOP_RACK.forPosition(500), true, introduceBag(context.getDocument())),
            };
        }

    }
    
    static class InsertClassMember {
        public ClassTree insertClassMember(WorkingCopy wc, ClassTree clazz, Tree member, int offset) throws IllegalStateException {
            return GeneratorUtils.insertClassMember(wc, clazz, member, offset);
        }
    }
    
    static InsertClassMember INSERT_CLASS_MEMBER = new InsertClassMember();//just for tests, for achieve compatibility with original behaviour
}

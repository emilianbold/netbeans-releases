/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.java.api.IntroduceParameterRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author  Jan Becicka
 */
public class IntroduceParameterPlugin extends JavaRefactoringPlugin {

    private IntroduceParameterRefactoring refactoring;
    private TreePathHandle treePathHandle;

    /**
     * Creates a new instance of introduce parameter refactoring plugin.
     *
     * @param method  refactored object, i.e. method or constructor
     */
    public IntroduceParameterPlugin(IntroduceParameterRefactoring refactoring) {
        this.refactoring = refactoring;
        this.treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
    }

    @Override
    public Problem checkParameters() {
        //TODO:
        return null;
    }

    @Override
    public Problem fastCheckParameters(CompilationController javac) throws IOException {

        return null;
    }

    private static String getString(String key) {
        return NbBundle.getMessage(IntroduceParameterPlugin.class, key);
    }
    private Set<ElementHandle<ExecutableElement>> allMethods;

    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        final Set<FileObject> set = new HashSet<FileObject>();
        JavaSource source = JavaSource.create(cpInfo, refactoring.getRefactoringSource().lookup(TreePathHandle.class).getFileObject());

        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }

                public void run(CompilationController info) throws Exception {
                    final ClassIndex idx = info.getClasspathInfo().getClassIndex();
                    info.toPhase(JavaSource.Phase.RESOLVED);
                    final ElementUtilities elmUtils = info.getElementUtilities();

                    //add all references of overriding methods
                    Element el = getMethodElement(treePathHandle, info);
                    ElementHandle<TypeElement> enclosingType = ElementHandle.create(elmUtils.enclosingTypeElement(el));
                    allMethods = new HashSet<ElementHandle<ExecutableElement>>();
                    allMethods.add(ElementHandle.create((ExecutableElement) el));
                    for (ExecutableElement e : RetoucheUtils.getOverridingMethods((ExecutableElement) el, info)) {
                        set.add(SourceUtils.getFile(e, info.getClasspathInfo()));
                        ElementHandle<TypeElement> encl = ElementHandle.create(elmUtils.enclosingTypeElement(e));
                        set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                        allMethods.add(ElementHandle.create(e));
                    }
                    //add all references of overriden methods
                    for (ExecutableElement e : RetoucheUtils.getOverridenMethods((ExecutableElement) el, info)) {
                        set.add(SourceUtils.getFile(e, info.getClasspathInfo()));
                        ElementHandle<TypeElement> encl = ElementHandle.create(elmUtils.enclosingTypeElement(e));
                        set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                        allMethods.add(ElementHandle.create(e));
                    }
                    set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                    set.add(SourceUtils.getFile(el, info.getClasspathInfo()));
                }
            }, true);
        } catch (IOException ioe) {
            throw (RuntimeException) new RuntimeException().initCause(ioe);
        }
        return set;
    }
    private ChangeParametersRefactoring.ParameterInfo[] paramTable;

    public Problem prepare(RefactoringElementsBag elements) {
        if (refactoring.isCompatible()) {
            fireProgressListenerStart(ProgressEvent.START, 1);
            CancellableTask<WorkingCopy> t = new CancellableTask<WorkingCopy>() {

                @Override
                public void cancel() {
                }

                @Override
                public void run(WorkingCopy parameter) throws Exception {
                    parameter.toPhase(JavaSource.Phase.RESOLVED);
                    
                        TreePath resolved = treePathHandle.resolve(parameter);

                        if (resolved == null) {
                            return; //TODO...
                        }

                        TypeMirror tm = parameter.getTrees().getTypeMirror(resolved);

                        if (tm == null) {
                            return; //TODO...
                        }

                        //tm = Utilities.convertIfAnonymous(Utilities.resolveCapturedType(parameter, tm));

                        Tree original = resolved.getLeaf();
                        boolean variableRewrite = original.getKind() == Kind.VARIABLE;
                        ExpressionTree expression = !variableRewrite ? (ExpressionTree) resolved.getLeaf() : ((VariableTree) original).getInitializer();
                        final TreeMaker make = parameter.getTreeMaker();

                        boolean expressionStatement = resolved.getParentPath().getLeaf().getKind() == Tree.Kind.EXPRESSION_STATEMENT;

                        TreePath meth = findMethod(resolved);

                        if (meth == null) {
                            return; //TODO...
                        }

                        BlockTree sttmts;
                        int index2;

                        if (refactoring.isReplaceAll()) {
                            Set<TreePath> candidates = new HashSet<TreePath>();//CopyFinder.computeDuplicates(parameter, resolved, meth, new AtomicBoolean(), null).keySet();
                            for (TreePath p : candidates) {
                                Tree leaf = p.getLeaf();

                                parameter.rewrite(leaf, make.Identifier(refactoring.getParameterName()));
                            }

                            int[] out = new int[1];
                            sttmts = findAddPosition(parameter, resolved, candidates, out);

                            if (sttmts == null) {
                                return;
                            }

                            index2 = out[0];
                        } else {
                            int[] out = new int[1];
                            sttmts = findAddPosition(parameter, resolved, Collections.<TreePath>emptySet(), out);

                            if (sttmts == null) {
                                return;
                            }

                            index2 = out[0];
                        }

                        List<StatementTree> nueStatements2 = new LinkedList<StatementTree>(sttmts.getStatements());

                        ExecutableElement currentMethod = (ExecutableElement) parameter.getTrees().getElement(meth);
                        TreeMaker treeMaker = parameter.getTreeMaker();
                        ReturnTree ret = treeMaker.Return(treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(), treeMaker.Identifier(currentMethod), toArgs(((MethodTree) meth.getLeaf()).getParameters(), treeMaker, expression)));
                        MethodTree newm = treeMaker.Method(currentMethod, treeMaker.Block(Collections.<StatementTree>singletonList(ret), false));
                        
                        ClassTree clazz = (ClassTree) meth.getParentPath().getLeaf();
                        parameter.rewrite(clazz, treeMaker.addClassMember(clazz, newm));

                        if (expressionStatement) {
                            nueStatements2.remove(resolved.getParentPath().getLeaf());
                        }

                        BlockTree nueBlock2 = make.Block(nueStatements2, false);

                        parameter.rewrite(sttmts, nueBlock2);
                        
                        VariableTree var = treeMaker.Variable(treeMaker.Modifiers(refactoring.isFinal() ? EnumSet.of(Modifier.FINAL) : EnumSet.noneOf(Modifier.class)), refactoring.getParameterName(), make.Identifier(tm.toString()) , null);

                        parameter.rewrite(meth.getLeaf(),treeMaker.addMethodParameter((MethodTree) meth.getLeaf(), var));
                        
                        Tree origParent = resolved.getParentPath().getLeaf();
                        Tree newParent = parameter.getTreeUtilities().translate(origParent, Collections.singletonMap(resolved.getLeaf(), make.Identifier(refactoring.getParameterName())));
                        parameter.rewrite(origParent, newParent);
                        
                }
            };
            createAndAddElements(Collections.singleton(treePathHandle.getFileObject()), t, elements, refactoring);
            
        } else {
            Set<FileObject> a = getRelevantFiles();
            fireProgressListenerStart(ProgressEvent.START, a.size());
            if (!a.isEmpty()) {
                CancellableTask<WorkingCopy> t = new CancellableTask<WorkingCopy>() {

                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void run(WorkingCopy parameter) throws Exception {
                        parameter.toPhase(JavaSource.Phase.RESOLVED);
                        TreePath resolved = treePathHandle.resolve(parameter);

                        if (resolved == null) {
                            return; //TODO...
                        }

                        TypeMirror tm = parameter.getTrees().getTypeMirror(resolved);

                        if (tm == null) {
                            return; //TODO...
                        }

                        //tm = Utilities.convertIfAnonymous(Utilities.resolveCapturedType(parameter, tm));

                        Tree original = resolved.getLeaf();
                        boolean variableRewrite = original.getKind() == Kind.VARIABLE;
                        ExpressionTree expression = !variableRewrite ? (ExpressionTree) resolved.getLeaf() : ((VariableTree) original).getInitializer();
                        final TreeMaker make = parameter.getTreeMaker();

                        boolean expressionStatement = resolved.getParentPath().getLeaf().getKind() == Tree.Kind.EXPRESSION_STATEMENT;

                        TreePath meth = findMethod(resolved);

                        if (meth == null) {
                            return; //TODO...
                        }

                        BlockTree sttmts;
                        int index2;

                        if (refactoring.isReplaceAll()) {
                            Set<TreePath> candidates = new HashSet<TreePath>();//CopyFinder.computeDuplicates(parameter, resolved, meth, new AtomicBoolean(), null).keySet();
                            for (TreePath p : candidates) {
                                Tree leaf = p.getLeaf();

                                parameter.rewrite(leaf, make.Identifier(refactoring.getParameterName()));
                            }

                            int[] out = new int[1];
                            sttmts = findAddPosition(parameter, resolved, candidates, out);

                            if (sttmts == null) {
                                return;
                            }

                            index2 = out[0];
                        } else {
                            int[] out = new int[1];
                            sttmts = findAddPosition(parameter, resolved, Collections.<TreePath>emptySet(), out);

                            if (sttmts == null) {
                                return;
                            }

                            index2 = out[0];
                        }

                        List<StatementTree> nueStatements2 = new LinkedList<StatementTree>(sttmts.getStatements());

                        ExecutableElement currentMethod = (ExecutableElement) parameter.getTrees().getElement(meth);

                        int originalIndex = 0;
                        List<? extends VariableElement> pars = currentMethod.getParameters();
                        paramTable = new ChangeParametersRefactoring.ParameterInfo[pars.size() + 1];
                        for (VariableElement par : pars) {
                            TypeMirror desc = par.asType();
                            String typeRepresentation;
                            if (currentMethod.isVarArgs() && originalIndex == pars.size() - 1) {
                                typeRepresentation = ((ArrayType) desc).getComponentType().toString() + " ..."; // NOI18N
                            } else {
                                typeRepresentation = desc.toString();
                            }
                            paramTable[originalIndex] = new ChangeParametersRefactoring.ParameterInfo(originalIndex, par.toString(), typeRepresentation, null);
                            originalIndex++;
                        }



                        paramTable[originalIndex] = new ChangeParametersRefactoring.ParameterInfo(-1, refactoring.getParameterName(), tm.toString(), expression.toString());


                        if (expressionStatement) {
                            nueStatements2.remove(resolved.getParentPath().getLeaf());
                        }

                        BlockTree nueBlock2 = make.Block(nueStatements2, false);

                        parameter.rewrite(sttmts, nueBlock2);

                        Tree origParent = resolved.getParentPath().getLeaf();
                        Tree newParent = parameter.getTreeUtilities().translate(origParent, Collections.singletonMap(resolved.getLeaf(), make.Identifier(refactoring.getParameterName())));
                        parameter.rewrite(origParent, newParent);

                    }
                };

                createAndAddElements(Collections.singleton(treePathHandle.getFileObject()), t, elements, refactoring);

                ChangeParametersRefactoring chgRef = new ChangeParametersRefactoring(treePathHandle);
                chgRef.setModifiers(null);
                chgRef.setParameterInfo(paramTable);
                TransformTask transform = new TransformTask(new ChangeParamsTransformer(chgRef, allMethods), treePathHandle);
                Problem p = createAndAddElements(a, transform, elements, refactoring);
                if (p != null) {
                    fireProgressListenerStop();
                    return p;
                }

            }
            fireProgressListenerStop();
        }
        return null;
    }

    private static List<? extends ExpressionTree> toArgs(List<? extends VariableTree> pars, TreeMaker make, ExpressionTree exp) {
        List<ExpressionTree> args = new LinkedList<ExpressionTree>();
        
        for(VariableTree par:pars) {
            args.add(make.Identifier(par.getName()));
        }
        args.add(exp);
        return args;
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
            if (!isParentOf(parent, tp)) {
                return false;
            }
        }

        return true;
    }

    private static TreePath findStatement(TreePath statementPath) {
        while (statementPath != null
                && (!StatementTree.class.isAssignableFrom(statementPath.getLeaf().getKind().asInterface())
                || (statementPath.getParentPath() != null
                && statementPath.getParentPath().getLeaf().getKind() != Kind.BLOCK))) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(statementPath.getLeaf().getKind())) {
                return null;
            }

            statementPath = statementPath.getParentPath();
        }

        return statementPath;
    }

    private static TreePath findMethod(TreePath path) {
        while (path != null) {
            if (path.getLeaf().getKind() == Kind.METHOD) {
                return path;
            }

            if (path.getLeaf().getKind() == Kind.BLOCK
                    && path.getParentPath() != null
                    && TreeUtilities.CLASS_TREE_KINDS.contains(path.getParentPath().getLeaf().getKind())) {
                //initializer:
                return path;
            }

            path = path.getParentPath();
        }

        return null;
    }

    private static BlockTree findAddPosition(CompilationInfo info, TreePath original, Set<? extends TreePath> candidates, int[] outPosition) {
        //find least common block holding all the candidates:
        TreePath statement = original;

        for (TreePath p : candidates) {
            Tree leaf = p.getLeaf();
            int leafStart = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), leaf);
            int stPathStart = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), statement.getLeaf());

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
        while (statement.getParentPath() != null && statement.getParentPath().getLeaf().getKind() != Kind.BLOCK) {
            statement = statement.getParentPath();
        }

        if (statement.getParentPath() == null) {
            return null;//XXX: log
        }
        BlockTree statements = (BlockTree) statement.getParentPath().getLeaf();
        StatementTree statementTree = (StatementTree) statement.getLeaf();

        int index = statements.getStatements().indexOf(statementTree);

        if (index == (-1)) {
            //really strange...
            return null;
        }

        outPosition[0] = index;

        return statements;
    }

    protected JavaSource getJavaSource(JavaRefactoringPlugin.Phase p) {
        switch (p) {
            case CHECKPARAMETERS:
            case FASTCHECKPARAMETERS:
            case PRECHECK:
                ClasspathInfo cpInfo = getClasspathInfo(refactoring);
                return JavaSource.create(cpInfo, treePathHandle.getFileObject());
        }
        return null;
    }

    /**
     * Returns list of problems. For the change method signature, there are two
     * possible warnings - if the method is overriden or if it overrides
     * another method.
     *
     * @return  overrides or overriden problem or both
     */
    @Override
    public Problem preCheck(CompilationController info) throws IOException {
        fireProgressListenerStart(refactoring.PRE_CHECK, 4);
        Problem preCheckProblem = null;
        info.toPhase(JavaSource.Phase.RESOLVED);
        preCheckProblem = isElementAvail(treePathHandle, info);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }
        
        TreePath tp = treePathHandle.resolve(info);
        TreePath method = getMethod(tp);
        if (method==null) {
            preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(IntroduceParameterPlugin.class, "ERR_ChangeParamsWrongType"));
            return preCheckProblem;
        }
        
        Element el = info.getTrees().getElement(method);
        if (el==null && !(el.getKind() == ElementKind.METHOD || el.getKind() == ElementKind.CONSTRUCTOR)) {
            preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(IntroduceParameterPlugin.class, "ERR_ChangeParamsWrongType"));
            return preCheckProblem;
        }

        preCheckProblem = JavaPluginUtils.isSourceElement(el, info);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }

        if (info.getElementUtilities().enclosingTypeElement(el).getKind() == ElementKind.ANNOTATION_TYPE) {
            preCheckProblem = new Problem(true, NbBundle.getMessage(IntroduceParameterPlugin.class, "ERR_MethodsInAnnotationsNotSupported"));
            return preCheckProblem;
        }

        for (ExecutableElement e : RetoucheUtils.getOverridenMethods((ExecutableElement) el, info)) {
            if (RetoucheUtils.isFromLibrary(e, info.getClasspathInfo())) { //NOI18N
                preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(IntroduceParameterPlugin.class, "ERR_CannnotRefactorLibrary", el));
            }
        }

        fireProgressListenerStop();
        return preCheckProblem;
    }
    
    private TreePath getMethod(TreePath treePath) {
        while (treePath!=null && treePath.getLeaf().getKind()!=Tree.Kind.METHOD) {
            treePath = treePath.getParentPath();
        }
        return treePath;
    }
    
    private ExecutableElement getMethodElement(TreePathHandle handle, CompilationInfo info) {
        return (ExecutableElement) info.getTrees().getElement(getMethod(handle.resolve(info)));
    }
}

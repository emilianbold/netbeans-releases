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

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.support.CancellableTreeScanner;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.InlineRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Refactoring used to replace all references of an element with its body
 * or expression.
 * @author Ralph Ruijs
 */
public class InlineRefactoringPlugin extends JavaRefactoringPlugin {

    private final InlineRefactoring refactoring;
    private TreePathHandle treePathHandle;

    public InlineRefactoringPlugin(InlineRefactoring refactoring) {
        this.refactoring = refactoring;
        this.treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        if (treePathHandle == null) {
            return null;
        }
        switch (p) {
            case PRECHECK:
            case FASTCHECKPARAMETERS:
                return JavaSource.forFileObject(treePathHandle.getFileObject());
            case CHECKPARAMETERS:
                ClasspathInfo classpathInfo = getClasspathInfo(refactoring);
                JavaSource source = JavaSource.create(classpathInfo, treePathHandle.getFileObject());
                return source;
        }
        throw new IllegalStateException();
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        if (treePathHandle == null) {
            return null;
        }
        RefactoringVisitor visitor;
        switch (refactoring.getType()) {
            case METHOD:
                visitor = new InlineMethodTransformer();
                break;
            default:
                visitor = new InlineVariableTransformer();
        }

        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStart(ProgressEvent.START, a.size());
        TransformTask transform = new TransformTask(visitor, treePathHandle);
        Problem problem = createAndAddElements(a, transform, refactoringElements, refactoring);
        fireProgressListenerStop();
        if (visitor instanceof InlineMethodTransformer) {
            InlineMethodTransformer imt = (InlineMethodTransformer) visitor;
            problem = problem != null ? problem : imt.getProblem();
        }
        return problem;
    }
    private Set<ElementHandle<ExecutableElement>> allMethods;

    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        final Set<FileObject> set = new HashSet<FileObject>();
        JavaSource source = JavaSource.create(cpInfo, treePathHandle.getFileObject());

        try {
            source.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController info) throws Exception {
                    final ClassIndex idx = info.getClasspathInfo().getClassIndex();
                    info.toPhase(JavaSource.Phase.RESOLVED);
                    Element el = treePathHandle.resolveElement(info);
                    ElementHandle<TypeElement> enclosingType;
                    if (el instanceof TypeElement) {
                        enclosingType = ElementHandle.create((TypeElement) el);
                    } else {
                        enclosingType = ElementHandle.create(info.getElementUtilities().enclosingTypeElement(el));
                    }
                    set.add(SourceUtils.getFile(enclosingType, info.getClasspathInfo()));
                    if (el.getModifiers().contains(Modifier.PRIVATE)) {
                        if (el.getKind() == ElementKind.METHOD) {
                            //add all references of overriding methods
                            allMethods = new HashSet<ElementHandle<ExecutableElement>>();
                            allMethods.add(ElementHandle.create((ExecutableElement) el));
                        }
                    } else {
                        if (el.getKind().isField()) {
                            set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                        } else if (el instanceof TypeElement) {
                            set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                        } else if (el.getKind() == ElementKind.METHOD) {
                            //add all references of overriding methods
                            allMethods = new HashSet<ElementHandle<ExecutableElement>>();
                            allMethods.add(ElementHandle.create((ExecutableElement) el));
                            for (ExecutableElement e : RetoucheUtils.getOverridingMethods((ExecutableElement) el, info)) {
                                addMethods(e, set, info, idx);
                            }
                            //add all references of overriden methods
                            for (ExecutableElement ov : RetoucheUtils.getOverridenMethods((ExecutableElement) el, info)) {
                                addMethods(ov, set, info, idx);
                                for (ExecutableElement e : RetoucheUtils.getOverridingMethods(ov, info)) {
                                    addMethods(e, set, info, idx);
                                }
                            }
                            set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE))); //?????
                        }
                    }
                }
            }, true);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return set;
    }

    private void addMethods(ExecutableElement e, Set set, CompilationInfo info, ClassIndex idx) {
        ElementHandle<TypeElement> encl = ElementHandle.create(info.getElementUtilities().enclosingTypeElement(e));
        set.add(SourceUtils.getFile(ElementHandle.create(e), info.getClasspathInfo()));
        set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        allMethods.add(ElementHandle.create(e));
    }

    @Override
    protected Problem preCheck(CompilationController javac) throws IOException {
        Problem preCheckProblem = null;
        javac.toPhase(JavaSource.Phase.RESOLVED);
        Element element = treePathHandle.resolveElement(javac);
        preCheckProblem = isElementAvail(treePathHandle, javac);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }

        preCheckProblem = JavaPluginUtils.isSourceElement(element, javac);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }

        switch (element.getKind()) {
            case FIELD:
            case LOCAL_VARIABLE:
                Tree tree = javac.getTrees().getTree(element);
                if (!tree.getKind().equals(Tree.Kind.VARIABLE)) {
                    preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(InlineRefactoringPlugin.class, "ERR_InlineWrongType", element.getKind().toString())); //NOI18N
                }
                VariableTree variableTree = (VariableTree) tree;

                // Inline a Variable needs the following preconditions:
                // - Not in Iterator
                // - Assigned to exactly once
                // - Not assigned to null
                // - Used at least once
                // - Not compound array initialization
                // ----------------------
                // Need to be initialized
                if (variableTree.getInitializer() == null) {
                    preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(InlineRefactoringPlugin.class, "ERR_InlineNoVarInitializer")); //NOI18N
                    return preCheckProblem;
                }
                // Not assigned to null
                if (variableTree.getInitializer().getKind() == Tree.Kind.NULL_LITERAL) {
                    preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(InlineRefactoringPlugin.class, "ERR_InlineNullVarInitializer")); //NOI18N
                    return preCheckProblem;
                }
                // Assigned to exactly once
                InlineUsageVisitor visitor = new InlineUsageVisitor(javac);
                TreePath elementPath = javac.getTrees().getPath(element);
                TreePath blockPath = elementPath.getParentPath();

                visitor.scan(blockPath.getLeaf(), element);
                if (visitor.assignmentCount > 0) {
                    preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(InlineRefactoringPlugin.class, "ERR_InlineAssignedOnce")); //NOI18N
                    return preCheckProblem;
                }
                // Used at least once
                if (visitor.usageCount <= 1) {
                    preCheckProblem = createProblem(preCheckProblem, false, NbBundle.getMessage(InlineRefactoringPlugin.class, "WRN_InlineNotUsed"));
                }
                // Possible change
                ExpressionTree initializer = variableTree.getInitializer();
                int skipFirstMethodInvocation = 0;
                if (initializer.getKind().equals(Tree.Kind.METHOD_INVOCATION)) {
                    skipFirstMethodInvocation++;
                }
                TreeScanner<Boolean, Boolean> scanner = new UnsafeTreeScanner(skipFirstMethodInvocation);
                Boolean isChanged = scanner.scan(initializer, false);
                if (isChanged != null && isChanged) {
                    preCheckProblem = createProblem(preCheckProblem, false, NbBundle.getMessage(InlineRefactoringPlugin.class, "WRN_InlineChange")); //NOI18N
                }
                // Not in Iterator
                TreePath treePath = treePathHandle.resolve(javac);
                treePath = treePath.getParentPath();
                Tree loop = treePath.getLeaf();
                if (loop.getKind() == Tree.Kind.ENHANCED_FOR_LOOP || loop.getKind() == Tree.Kind.FOR_LOOP) {
                    preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(InlineRefactoringPlugin.class, "ERR_InlineNotInIterator")); //NOI18N
                    return preCheckProblem;
                }
                // Not compound array initialization
                if (variableTree.getInitializer().getKind() == Tree.Kind.NEW_ARRAY) {
                    NewArrayTree newArrayTree = (NewArrayTree) variableTree.getInitializer();
                    if (newArrayTree.getType() == null || newArrayTree.getDimensions() == null) {
                        preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(InlineRefactoringPlugin.class, "ERR_InlineNotCompoundArrayInit")); //NOI18N
                        return preCheckProblem;
                    }
                }
                break;
            case METHOD:
                // Used at least once
                InlineUsageVisitor visitorMethod = new InlineUsageVisitor(javac);
                visitorMethod.scan(javac.getCompilationUnit(), element);
                if (visitorMethod.usageCount <= 1) {
                    preCheckProblem = createProblem(preCheckProblem, false, NbBundle.getMessage(InlineRefactoringPlugin.class, "WRN_InlineNotUsed")); //NOI18N
                }
                
                // Method can not be polymorphic
                Collection<ExecutableElement> overridenMethods = RetoucheUtils.getOverridenMethods((ExecutableElement) element, javac);
                Collection<ExecutableElement> overridingMethods = RetoucheUtils.getOverridingMethods((ExecutableElement) element, javac);
                if (overridenMethods.size() > 0 || overridingMethods.size() > 0) {
                    preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(InlineRefactoringPlugin.class, "ERR_InlineMethodPolymorphic")); //NOI18N
                    return preCheckProblem;
                }

                TreePath methodPath = javac.getTrees().getPath(element);
                MethodTree methodTree = (MethodTree) methodPath.getLeaf();
                Tree returnType = methodTree.getReturnType();
                boolean hasReturn = true;
                if (returnType.getKind().equals(Tree.Kind.PRIMITIVE_TYPE)) {
                    if (((PrimitiveTypeTree) returnType).getPrimitiveTypeKind().equals(TypeKind.VOID)) {
                        hasReturn = false;
                    }
                }
                InlineMethodVisitor methodVisitor = new InlineMethodVisitor(javac, element.getModifiers());
                methodVisitor.scan(methodPath.getLeaf(), element);
                if (hasReturn) {
                    // Method with returntype must have a return statement at the end
                    if (methodVisitor.nrOfReturnStatements > 1) {
                        preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(InlineRefactoringPlugin.class, "ERR_InlineMethodMultipleReturn")); //NOI18N
                        return preCheckProblem;
                    }
                    StatementTree lastStatement = methodTree.getBody().getStatements().get(methodTree.getBody().getStatements().size() - 1);
                    if (!lastStatement.getKind().equals(Tree.Kind.RETURN)) {
                        preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(InlineRefactoringPlugin.class, "ERR_InlineMethodNoLastReturn")); //NOI18N
                        return preCheckProblem;
                    }
                } else {
                    // Method with returntype void cannot have a return statement
                    if (methodVisitor.nrOfReturnStatements > 0) {
                        preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(InlineRefactoringPlugin.class, "ERR_InlineMethodVoidReturn")); //NOI18N
                        return preCheckProblem;
                    }
                }
                // Method can not be recursive
                if (methodVisitor.isRecursive) {
                    preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(InlineRefactoringPlugin.class, "ERR_InlineMethodRecursion")); //NOI18N
                    return preCheckProblem;
                }
                // Used accessors must not be local in public method
                if (methodVisitor.qualIdentProblem) {
                    preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(InlineRefactoringPlugin.class, "ERR_InlineMethodLocalAccessors")); //NOI18N
                    return preCheckProblem;
                }
                // Used accessors in the method must have the right access specification
                if (methodVisitor.accessorRightProblem) {
                    preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(InlineRefactoringPlugin.class, "ERR_InlineMethodNoAccessors")); //NOI18N
                    return preCheckProblem;
                }

                break;
            default:
                preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(InlineRefactoringPlugin.class, "ERR_InlineWrongType", element.getKind().toString())); //NOI18N
        }
        return preCheckProblem;
    }

    private class InlineMethodVisitor extends CancellableTreeScanner<Tree, Element> {

        public int nrOfReturnStatements = 0;
        public boolean isRecursive = false;
        private boolean accessorRightProblem = false;
        private boolean qualIdentProblem = false;
        private final CompilationController workingCopy;
        private final Modifier access;

        public InlineMethodVisitor(CompilationController workingCopy, Set<Modifier> modifiers) {
            this.workingCopy = workingCopy;
            this.access = getAccessSpecifier(modifiers);
        }

        @Override
        public Tree visitReturn(ReturnTree node, Element p) {
            nrOfReturnStatements++;
            return super.visitReturn(node, p);
        }

        @Override
        public Tree visitMethodInvocation(MethodInvocationTree node, Element p) {
            if (p.equals(asElement(node))) {
                isRecursive = true;
            } else {
                Element asElement = asElement(node);
                if (asElement.getKind().equals(ElementKind.FIELD)
                        || asElement.getKind().equals(ElementKind.METHOD)
                        || asElement.getKind().equals(ElementKind.CLASS)) {
                    Modifier mod = getAccessSpecifier(asElement(node).getModifiers());
                    accessorRightProblem = hasAccessorRightProblem(mod);
                    qualIdentProblem = hasQualIdentProblem(p, node);
                }
            }
            return super.visitMethodInvocation(node, p);
        }

        private boolean hasQualIdentProblem(Element p, Tree node) throws IllegalArgumentException {
            boolean result = qualIdentProblem;
            Element asElement = asElement(node);
            ElementUtilities elementUtilities = workingCopy.getElementUtilities();
            TypeElement bodyEnclosingTypeElement = elementUtilities.enclosingTypeElement(p);
            TypeElement invocationEnclosingTypeElement = elementUtilities.enclosingTypeElement(asElement);
            if (bodyEnclosingTypeElement.equals(invocationEnclosingTypeElement)
                    && (access == null || !access.equals(Modifier.PRIVATE))) {
                result = true;
            }
            return result;
        }

        private boolean hasAccessorRightProblem(Modifier mod) {
            boolean hasProblem = accessorRightProblem;
            if (access != null) {
                switch (access) {
                    case PUBLIC:
                        if (mod == null || Modifier.PROTECTED.equals(mod) || Modifier.PRIVATE.equals(mod)) {
                            hasProblem = true;
                        }
                        break;
                    case PROTECTED:
                        if (mod == null || Modifier.PRIVATE.equals(mod)) {
                            hasProblem = true;
                        }
                        break;
                    case PRIVATE:
                    default:
                        break;
                }
            } else {
                if (Modifier.PRIVATE.equals(mod)) {
                    hasProblem = true;
                }
            }
            return hasProblem;
        }

        private Modifier getAccessSpecifier(Set<Modifier> modifiers) {
            Modifier mod = null;
            for (Modifier modifier : modifiers) {
                switch (modifier) {
                    case PUBLIC:
                    case PRIVATE:
                    case PROTECTED:
                        mod = modifier;
                }
            }
            return mod;
        }

        @Override
        public Tree visitIdentifier(IdentifierTree node, Element p) {
            Element asElement = asElement(node);
            if (asElement.getKind().equals(ElementKind.FIELD)
                    || asElement.getKind().equals(ElementKind.METHOD)
                    || asElement.getKind().equals(ElementKind.CLASS)) {
                Modifier mod = getAccessSpecifier(asElement(node).getModifiers());
                accessorRightProblem = hasAccessorRightProblem(mod);
                qualIdentProblem = hasQualIdentProblem(p, node);
            }
            return super.visitIdentifier(node, p);
        }

        @Override
        public Tree visitNewClass(NewClassTree node, Element p) {
            Element asElement = asElement(node);
            if (asElement.getKind().equals(ElementKind.FIELD)
                    || asElement.getKind().equals(ElementKind.METHOD)
                    || asElement.getKind().equals(ElementKind.CLASS)) {
                Modifier mod = getAccessSpecifier(asElement(node).getModifiers());
                accessorRightProblem = hasAccessorRightProblem(mod);
                qualIdentProblem = hasQualIdentProblem(p, node);
            }
            return super.visitNewClass(node, p);
        }

        @Override
        public Tree visitMemberSelect(MemberSelectTree node, Element p) {
            Element asElement = asElement(node);
            if (asElement.getKind().equals(ElementKind.FIELD)
                    || asElement.getKind().equals(ElementKind.METHOD)
                    || asElement.getKind().equals(ElementKind.CLASS)) {
                Modifier mod = getAccessSpecifier(asElement(node).getModifiers());
                accessorRightProblem = hasAccessorRightProblem(mod);
                qualIdentProblem = hasQualIdentProblem(p, node);
            }
            return super.visitMemberSelect(node, p);
        }

        private Element asElement(Tree tree) {
            Trees treeUtil = workingCopy.getTrees();
            TreePath treePath = treeUtil.getPath(workingCopy.getCompilationUnit(), tree);
            Element element = treeUtil.getElement(treePath);
            return element;
        }
    }

    private class InlineUsageVisitor extends CancellableTreeScanner<Tree, Element> {

        public int assignmentCount = 0;
        public int usageCount = 0;
        private final CompilationController workingCopy;

        public InlineUsageVisitor(CompilationController workingCopy) {
            this.workingCopy = workingCopy;
        }

        @Override
        public Tree visitVariable(VariableTree node, Element p) {
            if (p.equals(asElement(node))) {
                usageCount++;
            }
            return super.visitVariable(node, p);
        }

        @Override
        public Tree visitMemberSelect(MemberSelectTree node, Element p) {
            if (p.equals(asElement(node))) {
                usageCount++;
            }
            return super.visitMemberSelect(node, p);
        }

        @Override
        public Tree visitIdentifier(IdentifierTree node, Element p) {
            if (p.equals(asElement(node))) {
                usageCount++;
            }
            return super.visitIdentifier(node, p);
        }

        @Override
        public Tree visitMethod(MethodTree node, Element p) {
            if (p.equals(asElement(node))) {
                usageCount++;
            }
            return super.visitMethod(node, p);
        }

        @Override
        public Tree visitMethodInvocation(MethodInvocationTree node, Element p) {
            if (p.equals(asElement(node))) {
                usageCount++;
            }
            return super.visitMethodInvocation(node, p);
        }

        @Override
        public Tree visitAssignment(AssignmentTree node, Element p) {
            if (p.equals(asElement(node.getVariable()))) {
                assignmentCount++;
            }
            return super.visitAssignment(node, p);
        }

        @Override
        public Tree visitCompoundAssignment(CompoundAssignmentTree node, Element p) {
            if (p.equals(asElement(node.getVariable()))) {
                assignmentCount++;
            }
            return super.visitCompoundAssignment(node, p);
        }

        @Override
        public Tree visitUnary(UnaryTree node, Element p) {
            if (p.equals(asElement(node.getExpression()))) {
                assignmentCount++;
            }
            return super.visitUnary(node, p);
        }

        private Element asElement(Tree tree) {
            Trees treeUtil = workingCopy.getTrees();
            TreePath treePath = treeUtil.getPath(workingCopy.getCompilationUnit(), tree);
            Element element = treeUtil.getElement(treePath);
            return element;
        }
    }

    private static class UnsafeTreeScanner extends TreeScanner<Boolean, Boolean> {

        private int skipFirstMethodInvocation;

        public UnsafeTreeScanner(int skipFirstMethodInvocation) {
            super();
            this.skipFirstMethodInvocation = skipFirstMethodInvocation;
        }

        @Override
        public Boolean visitMethodInvocation(MethodInvocationTree node, Boolean p) {
            if (skipFirstMethodInvocation > 0) {
                skipFirstMethodInvocation--;
                return super.visitMethodInvocation(node, p);
            } else {
                return true;
            }
        }

        @Override
        public Boolean visitNewClass(NewClassTree node, Boolean p) {
            return true;
        }

        @Override
        public Boolean visitNewArray(NewArrayTree node, Boolean p) {
            return true;
        }

        @Override
        public Boolean visitAssignment(AssignmentTree node, Boolean p) {
            return true;
        }

        @Override
        public Boolean visitUnary(UnaryTree node, Boolean p) {
            return true;
        }

        @Override
        public Boolean reduce(Boolean left, Boolean right) {
            return (left == null ? false : left) || (right == null ? false : right);
        }
    }
}

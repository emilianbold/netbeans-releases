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

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.Pair;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaMoveMembersProperties;
import org.netbeans.modules.refactoring.java.api.JavaMoveMembersProperties.Visibility;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * 
 * @author Ralph Ruijs
 */
public class MoveMembersTransformer extends RefactoringVisitor {

    private static final Set<Modifier> ALL_ACCESS_MODIFIERS = EnumSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC);
    private Problem problem;
    private Collection<? extends TreePathHandle> allElements;
    private final Visibility visibility;
    private final HashMap<TreePathHandle, Boolean> usageOutsideOfPackage;
    private final TreePathHandle targetHandle;
    private final boolean delegate;

    public MoveMembersTransformer(MoveRefactoring refactoring) {
        allElements = refactoring.getRefactoringSource().lookupAll(TreePathHandle.class);
        JavaMoveMembersProperties properties = refactoring.getContext().lookup(JavaMoveMembersProperties.class);
        properties = properties == null ? new JavaMoveMembersProperties() : properties;
        visibility = properties.getVisibility();
        usageOutsideOfPackage = new HashMap<TreePathHandle, Boolean>();
        for (TreePathHandle treePathHandle : allElements) {
            usageOutsideOfPackage.put(treePathHandle, Boolean.FALSE);
        }
        targetHandle = refactoring.getTarget().lookup(TreePathHandle.class);
        delegate = properties.isDelegate();
    }

    public Problem getProblem() {
        return problem;
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element target) {
        if (changeIfMatch(getCurrentPath(), node, target)) {
            return node;
        }
        return super.visitMemberSelect(node, target);
    }

    @Override
    public Tree visitIdentifier(IdentifierTree node, Element target) {
        if (changeIfMatch(getCurrentPath(), node, target)) {
            return node;
        }
        return super.visitIdentifier(node, target);
    }

    @Override
    public Tree visitMethodInvocation(MethodInvocationTree node, Element target) {
        if (changeIfMatch(getCurrentPath(), node, target)) {
            return node;
        }
        return super.visitMethodInvocation(node, target);
    }

    private boolean changeIfMatch(TreePath currentPath, Tree node, final Element target) throws IllegalArgumentException {
        Element el = workingCopy.getTrees().getElement(currentPath);
        if (el == null) {
            return false;
        }
        TreePathHandle elementBeingMoved = isElementBeingMoved(el);
        if (elementBeingMoved != null) {

            final FileObject folder = targetHandle.getFileObject().getParent();
            final CompilationUnitTree compilationUnit = currentPath.getCompilationUnit();
            checkForUsagesOutsideOfPackage(folder, compilationUnit, elementBeingMoved);

            if (node instanceof MethodInvocationTree) {
                if(!delegate) {
                    changeMethodInvocation((ExecutableElement) el, (MethodInvocationTree) node, currentPath, target);
                }
            } else if (node instanceof IdentifierTree) {
                changeIdentifier(el, (IdentifierTree) node, currentPath, target);
            } else if (node instanceof MemberSelectTree) {
                changeMemberSelect(el, (MemberSelectTree) node, currentPath, target);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Tree visitClass(ClassTree node, Element target) {
        insertIfMatch(getCurrentPath(), node, target);
        return super.visitClass(node, target);
    }

    @Override
    public Tree visitVariable(VariableTree node, Element target) {
        if(removeIfMatch(getCurrentPath(), target)) {;
            return node;
        } else {
            return super.visitVariable(node, target);
        }
    }

    @Override
    public Tree visitMethod(MethodTree node, Element target) {
        if(removeIfMatch(getCurrentPath(), target)) {
            return node;
        } else {
            return super.visitMethod(node, target);
        }
    }

    private void changeMemberSelect(Element el, final MemberSelectTree node, TreePath currentPath, final Element target) {
        if (el.getModifiers().contains(Modifier.STATIC)) {
            ExpressionTree expression = node.getExpression();
            TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, currentPath, true, true, true, true, true);
            Element enclosingElement = workingCopy.getTrees().getElement(enclosingClassPath);
            if (enclosingElement.equals(target)) {
                IdentifierTree newIdt = make.Identifier(node.getIdentifier());
                rewrite(node, newIdt);
            } else {
                ExpressionTree newIdent = make.QualIdent(target);
                rewrite(expression, newIdent);
            }
        } else {
            TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, currentPath, true, true, true, true, false);
            Element enclosingElement = workingCopy.getTrees().getElement(enclosingClassPath);
            if (enclosingElement.equals(target)
                    && node.getKind() == Tree.Kind.MEMBER_SELECT) {
                IdentifierTree newIdt = make.Identifier(((MemberSelectTree) node).getIdentifier());
                rewrite(node, newIdt);
            } else {
                Scope scope = workingCopy.getTrees().getScope(currentPath);
                Iterable<? extends Element> vars = workingCopy.getElementUtilities().getLocalMembersAndVars(scope, new ElementUtilities.ElementAcceptor() {

                    @Override
                    public boolean accept(Element e, TypeMirror type) { // Type will always be null
                        return workingCopy.getTypes().isSameType(e.asType(), target.asType());
                    }
                });
                if (!vars.iterator().hasNext()) {
                    problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveMembersTransformer.class, "WRN_NoAccessor"))); //NOI18N
                } else {
                    Element localVar = vars.iterator().next();
                    MemberSelectTree selectTree = (MemberSelectTree) node;

                    Tree it = selectTree.getExpression();
                    Tree newIt = make.Identifier(localVar);
                    if (it != null && newIt != null) {
                        rewrite(it, newIt);
                    }
                }
            }
        }
    }

    private void changeIdentifier(Element el, final IdentifierTree node, TreePath currentPath, final Element target) {
        if (el.getModifiers().contains(Modifier.STATIC)) {
            IdentifierTree it = (IdentifierTree) node;
            TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, currentPath, true, true, true, true, true);
            Element enclosingElement = workingCopy.getTrees().getElement(enclosingClassPath);
            if (!enclosingElement.equals(target)) {
                Tree newIdent = make.setLabel(node, target.getSimpleName().toString() + "." + it.getName().toString()); //NOI18N
                rewrite(it, newIdent);
            }
        } else {
            Scope scope = workingCopy.getTrees().getScope(currentPath);

            // TODO Maybe move to configuration
            Iterable<? extends Element> vars = workingCopy.getElementUtilities().getLocalMembersAndVars(scope, new ElementUtilities.ElementAcceptor() {

                @Override
                public boolean accept(Element e, TypeMirror type) { // Type will always be null
                    return workingCopy.getTypes().isSameType(e.asType(), target.asType());
                }
            });
            if (!vars.iterator().hasNext()) {
                problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveMembersTransformer.class, "WRN_NoAccessor"))); //NOI18N
            } else {
                Tree it;
                Tree newIt;
                Element localVar = vars.iterator().next();
                IdentifierTree variableTree = (IdentifierTree) node;
                it = node;
                newIt = make.setLabel(node, localVar.getSimpleName().toString() + "." + variableTree.getName().toString()); //NOI18N
                if (it != null && newIt != null) {
                    rewrite(it, newIt);
                }
            }
        }
    }

    /**
     * Changing a method invocation to refer to the new location.
     *
     * Steps: 1. Check if we need to remove a parameter from the invocation.
     * 2. Check if it is a Static method.
     * 2.1 Change methodSelect
     * 2.2 Translate method arguments
     *
     * 3. Find Parameter or local var to use
     * 3.1 Create problem if no accessor
     * 4. Check if it needs an argument for local accessors
     * 4.1 Check if it van be the memberselect
     *
     * 5. Create a new method invocation
     *
     * @param el
     * @param node
     * @param currentPath
     * @param target
     */
    private void changeMethodInvocation(final ExecutableElement el, final MethodInvocationTree node, final TreePath currentPath, final Element target) {
        rewrite(node, createMethodInvocationTree(el, node, currentPath, target));
    }
    
    private MethodInvocationTree createMethodInvocationTree(final ExecutableElement el, final MethodInvocationTree node, final TreePath currentPath, final Element target) {
        TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, currentPath, true, true, true, true, true);
        Element enclosingElement = workingCopy.getTrees().getElement(enclosingClassPath);

        final List<? extends ExpressionTree> typeArguments = (List<? extends ExpressionTree>) node.getTypeArguments();
        final LinkedList<ExpressionTree> arguments = new LinkedList(node.getArguments());
        final ExpressionTree newMethodSelect;
        
        if (el.getModifiers().contains(Modifier.STATIC)) {
            if (node.getMethodSelect().getKind() == Tree.Kind.MEMBER_SELECT) {
                if (enclosingElement.equals(target)) {
                    newMethodSelect = make.Identifier(((MemberSelectTree) node.getMethodSelect()).getIdentifier());
                } else {
                    newMethodSelect = make.MemberSelect(make.QualIdent(target), ((MemberSelectTree) node.getMethodSelect()).getIdentifier().toString());
                }
            } else { // if (methodSelect.getKind() == Tree.Kind.IDENTIFIER) {
                if (!enclosingElement.equals(target)) {
                    newMethodSelect = make.MemberSelect(make.QualIdent(target), el);
                } else {
                    newMethodSelect = node.getMethodSelect();
                }
            }
        } else {
            final ExpressionTree selectExpression;
            int removedIndex = -1;
            List<? extends VariableElement> parameters = el.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                VariableElement variableElement = parameters.get(i);
                if (workingCopy.getTypes().isSameType(variableElement.asType(), target.asType())) {
                    removedIndex = i;
                    break;
                }
            }
            if (removedIndex != -1) {
                selectExpression = node.getArguments().get(removedIndex);
            } else {
                Scope scope = workingCopy.getTrees().getScope(currentPath);
                Iterable<? extends Element> vars = workingCopy.getElementUtilities().getLocalMembersAndVars(scope, new ElementUtilities.ElementAcceptor() {

                    @Override
                    public boolean accept(Element e, TypeMirror type) { // Type will always be null
                        return workingCopy.getTypes().isSameType(e.asType(), target.asType());
                    }
                });
                if (!vars.iterator().hasNext()) {
                    problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveMembersTransformer.class, "WRN_NoAccessor"))); //NOI18N
                    selectExpression = null;
                } else {
                    Element localVar = vars.iterator().next();
                    selectExpression = make.Identifier(localVar);
                }
            }

            if(selectExpression == null) {
                newMethodSelect = node.getMethodSelect();
            } else {
                if (node.getMethodSelect().getKind() == Tree.Kind.MEMBER_SELECT) {
                    MemberSelectTree selectTree = (MemberSelectTree) node.getMethodSelect();
                    if (enclosingElement.equals(target)) {
                        newMethodSelect = make.Identifier(((MemberSelectTree) node.getMethodSelect()).getIdentifier());
                    } else {
                        newMethodSelect = make.MemberSelect(selectExpression, selectTree.getIdentifier());
                    }
                } else { // if (node.getMethodSelect().getKind() == Tree.Kind.IDENTIFIER) {
                    IdentifierTree variableTree = (IdentifierTree) node.getMethodSelect();
                    newMethodSelect = make.MemberSelect(selectExpression, variableTree.getName().toString());
                }
            }

            if (removedIndex != -1) {
                arguments.remove(removedIndex);
            }
            TreeScanner<Boolean, Void> needsArgumentScanner = new TreeScanner<Boolean, Void>() {
                // TODO: fix needsArgumentScanner

                @Override
                public Boolean visitIdentifier(IdentifierTree node, Void p) {

                    return super.visitIdentifier(node, p);
                }

                @Override
                public Boolean visitMethodInvocation(MethodInvocationTree node, Void p) {
                    return super.visitMethodInvocation(node, p);
                }

                @Override
                public Boolean visitMemberSelect(MemberSelectTree node, Void p) {
                    String isThis = node.getExpression().toString();
                    if (isThis.equals("this") || isThis.endsWith(".this")) {
                        return true;
                    }
                    return super.visitMemberSelect(node, p);
                }

                @Override
                public Boolean reduce(Boolean r1, Boolean r2) {
                    return (r1 == Boolean.TRUE || r2 == Boolean.TRUE);
                }
            };
            Boolean needsArgument = needsArgumentScanner.scan(workingCopy.getTrees().getTree(el).getBody(), null);
            if (needsArgument == Boolean.TRUE) {
                ExpressionTree newArgument;
                if (enclosingElement.equals(target) && node.getMethodSelect().getKind() == Tree.Kind.MEMBER_SELECT) {
                    newArgument = ((MemberSelectTree) node.getMethodSelect()).getExpression();
                } else {
                    newArgument = workingCopy.getTreeUtilities().parseExpression("this", new SourcePositions[1]); //NOI18N
                }
                if (el.isVarArgs()) {
                    arguments.add(arguments.size() - 1, newArgument);
                } else {
                    arguments.add(newArgument);
                }
            }
        }
        
        List<ExpressionTree> newArguments = new ArrayList<ExpressionTree>(arguments.size());
        for (ExpressionTree expressionTree : arguments) {
            ExpressionTree expression = fixReferences(expressionTree, target, currentPath);
            newArguments.add(expression);
        }
        return make.MethodInvocation(typeArguments, newMethodSelect, newArguments);
    }

    private void checkForUsagesOutsideOfPackage(final FileObject folder, final CompilationUnitTree compilationUnit, TreePathHandle elementBeingMoved) {
        if (!RefactoringUtils.getPackageName(folder).equals(
                RefactoringUtils.getPackageName(compilationUnit))) {
            usageOutsideOfPackage.put(elementBeingMoved, Boolean.TRUE);
        }
    }

    private void insertIfMatch(TreePath currentPath, ClassTree node, Element target) throws IllegalArgumentException {
        Element el = workingCopy.getTrees().getElement(currentPath);
        if (el == null) {
            return;
        }
        if (el.equals(target)) {
            ClassTree newClassTree = node;
            for (TreePathHandle tph : allElements) {

                final TreePath resolvedPath = tph.resolve(workingCopy);
                Tree member = resolvedPath.getLeaf();
                Tree newMember = null;

                // Make a new Method tree
                if (member.getKind() == Tree.Kind.METHOD) {

                    // Change Modifiers
                    final MethodTree methodTree = (MethodTree) member;
                    ExecutableElement method = (ExecutableElement) workingCopy.getTrees().getElement(resolvedPath);
                    ModifiersTree modifiers = changeModifiers(methodTree.getModifiers(), usageOutsideOfPackage.get(tph) == Boolean.TRUE);

                    // Find and remove a usable parameter
                    final List<? extends VariableTree> parameters = methodTree.getParameters();
                    LinkedList<VariableTree> newParameters;
                    VariableTree removedParameter = null;
                    if (!method.getModifiers().contains(Modifier.STATIC)) {
                        newParameters = new LinkedList<VariableTree>();
                        for (int i = 0; i < parameters.size(); i++) {
                            VariableTree variableTree = parameters.get(i);
                            TypeMirror type = workingCopy.getTrees().getTypeMirror(TreePath.getPath(resolvedPath, variableTree));
                            if (removedParameter != null || !workingCopy.getTypes().isSameType(type, target.asType())) {
                                newParameters.add(variableTree);
                            } else {
                                removedParameter = variableTree;
                            }
                        }
                    } else {
                        newParameters = new LinkedList<VariableTree>(parameters);
                    }
                    // Scan the body and fix references
                    BlockTree body = methodTree.getBody();
                    TreePath bodyPath = new TreePath(resolvedPath, body);

                    // Remove the parameter and change it to the keyword this
                    final Map<ExpressionTree, ExpressionTree> original2Translated = new HashMap<ExpressionTree, ExpressionTree>();
                    final Trees trees = workingCopy.getTrees();
                    // Add parameter and change local accessors
                    TreePath sourceClass = JavaRefactoringUtils.findEnclosingClass(workingCopy, resolvedPath, true, true, true, true, true);
                    TypeMirror sourceType = workingCopy.getTrees().getTypeMirror(sourceClass);
                    final String parameterName = getParameterName(sourceType, methodTree, workingCopy.getTrees().getScope(bodyPath), workingCopy);
                    TreeScanner<Void, TypeMirror> idScan = new TreeScanner<Void, TypeMirror>() {

                        @Override
                        public Void visitMemberSelect(MemberSelectTree node, TypeMirror source) {
                            String isThis = node.getExpression().toString();
                            if (isThis.equals("this") || isThis.endsWith(".this")) {
                                TreePath currentPath = new TreePath(resolvedPath, node);
                                Element el = trees.getElement(currentPath);
                                if(isElementBeingMoved(el) != null) {
                                    return null;
                                }
                            }
                            return super.visitMemberSelect(node, source);
                        }

                        @Override
                        public Void visitIdentifier(IdentifierTree node, TypeMirror source) {
                            TreePath currentPath = new TreePath(resolvedPath, node);
                            Element el = trees.getElement(currentPath);
                            
                            if(isElementBeingMoved(el) == null) {
                                String isThis = node.toString();
                                // TODO: Check for super keyword. if super is used, but it is not overloaded, there is no problem. else warning.
                                if (isThis.equals("this") || isThis.endsWith(".this")) {
                                    ExpressionTree newLabel = make.setLabel(node, parameterName);
                                    original2Translated.put(node, newLabel);
                                } else {
                                    if (el.getKind() == ElementKind.METHOD || el.getKind() == ElementKind.FIELD || el.getKind() == ElementKind.ENUM) {
                                        TypeElement elType = workingCopy.getElementUtilities().enclosingTypeElement(el);
                                        if (elType != null && workingCopy.getTypes().isSubtype(source, elType.asType())) {
                                            MemberSelectTree memberSelect = make.MemberSelect(workingCopy.getTreeUtilities().parseExpression(parameterName, new SourcePositions[1]), el);
                                            original2Translated.put(node, memberSelect);
                                        }
                                    }
                                }
                            }
                            return super.visitIdentifier(node, source);
                        }
                    };
                    idScan.scan(body, sourceType);
                    boolean addParameter = !original2Translated.isEmpty();

                    if (removedParameter != null) {
                        TreeScanner<Void, Pair<Element, ExpressionTree>> idScan2 = new TreeScanner<Void, Pair<Element, ExpressionTree>>() {

                            @Override
                            public Void visitIdentifier(IdentifierTree node, Pair<Element, ExpressionTree> p) {
                                TreePath currentPath = new TreePath(resolvedPath, node);
                                Element el = trees.getElement(currentPath);
                                if (p.first.equals(el)) {
                                    original2Translated.put(node, p.second);
                                }
                                return super.visitIdentifier(node, p);
                            }
                        };
                        TreePath path = new TreePath(resolvedPath, removedParameter);
                        Element element = trees.getElement(path);
                        final Pair<Element, ExpressionTree> pair = Pair.of(element, workingCopy.getTreeUtilities().parseExpression("this", new SourcePositions[1])); // NOI18N
                        idScan2.scan(body, pair);
                    }

                    body = (BlockTree) workingCopy.getTreeUtilities().translate(body, original2Translated);

                    if (addParameter) {
                        VariableTree vt = make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), parameterName, make.QualIdent(sourceType.toString()), null);
                        if (method.isVarArgs()) {
                            newParameters.add(newParameters.size() - 1, vt);
                        } else {
                            newParameters.add(vt);
                        }
                    }

                    newMember = make.Method(modifiers, methodTree.getName(), methodTree.getReturnType(), methodTree.getTypeParameters(), newParameters, methodTree.getThrows(), body, (ExpressionTree) methodTree.getDefaultValue());

                    // Make a new Variable (Field) tree
                } else if (member.getKind() == Tree.Kind.VARIABLE) {
                    VariableTree field = (VariableTree) member;
                    ModifiersTree modifiers = changeModifiers(field.getModifiers(), usageOutsideOfPackage.get(tph) == Boolean.TRUE);

                    // Scan the initializer and fix references
                    ExpressionTree initializer = field.getInitializer();
                    initializer = fixReferences(initializer, target, resolvedPath);

                    newMember = make.Variable(modifiers, field.getName(), field.getType(), initializer);
                }

                // Insert the member and copy its comments
                if (newMember != null) {
                    GeneratorUtilities.get(workingCopy).importComments(member, resolvedPath.getCompilationUnit());
                    GeneratorUtilities.get(workingCopy).copyComments(member, newMember, true);
                    GeneratorUtilities.get(workingCopy).copyComments(member, newMember, false);
                    newClassTree = GeneratorUtilities.get(workingCopy).insertClassMember(newClassTree, newMember);
                }
            }
            rewrite(node, newClassTree);
        }
    }

    private boolean removeIfMatch(TreePath currentPath, Element target) throws IllegalArgumentException {
        Element el = workingCopy.getTrees().getElement(currentPath);
        if (el == null) {
            return false;
        }
        if (isElementBeingMoved(el) != null) {
            TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, currentPath, true, true, true, true, true);
            ClassTree classTree = (ClassTree) enclosingClassPath.getLeaf();
            ClassTree newClassTree = classTree;
            for (TreePathHandle tph : allElements) {
                TreePath resolvedPath = tph.resolve(workingCopy);
                Tree member = resolvedPath.getLeaf();
                if (delegate && member.getKind() == Tree.Kind.METHOD) {
                    int index = newClassTree.getMembers().indexOf(member);
                    newClassTree = make.removeClassMember(newClassTree, member);
                    ExecutableElement element = (ExecutableElement) workingCopy.getTrees().getElement(resolvedPath);
                    List<ExpressionTree> paramList = new ArrayList<ExpressionTree>();

                    for (VariableElement variableElement : element.getParameters()) {
                        IdentifierTree vt = make.Identifier(variableElement.getSimpleName().toString());
                        paramList.add(vt);
                    }

                    MethodInvocationTree methodInvocation = make.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                            make.Identifier(element),
                            paramList);
                    methodInvocation = createMethodInvocationTree(element, methodInvocation, currentPath, target);

                    TypeMirror methodReturnType = element.getReturnType();

                    final StatementTree statement;
                    final Types types = workingCopy.getTypes();
                    if (!types.isSameType(methodReturnType, types.getNoType(TypeKind.VOID))) {
                        statement = make.Return(methodInvocation);
                    } else {
                        statement = make.ExpressionStatement(methodInvocation);
                    }
                    
                    
                    
                    MethodTree method = make.Method(element, make.Block(Collections.singletonList(statement), false));
                    newClassTree = make.insertClassMember(newClassTree, index, method);
                    
                } else {
                    newClassTree = make.removeClassMember(newClassTree, member);
                }
            }
            rewrite(classTree, newClassTree);
            return true;
        }
        return false;
    }

    private TreePathHandle isElementBeingMoved(Element el) {
        for (TreePathHandle mh : allElements) {
            Element element = mh.resolveElement(workingCopy);
            if (element == null) {
                Logger.getLogger("org.netbeans.modules.refactoring.java").log(Level.INFO, "MoveMembersTransformer cannot resolve {0}", mh); //NOI18N
                continue;
            }
            if (element.equals(el)) {
                return mh;
            }
        }
        return null;
    }

    private ModifiersTree changeModifiers(ModifiersTree modifiersTree, boolean usageOutsideOfPackage) {
        final Set<Modifier> flags = modifiersTree.getFlags();
        Set<Modifier> newModifiers = flags.isEmpty() ? EnumSet.noneOf(Modifier.class) : EnumSet.copyOf(flags);
        switch (visibility) {
            case ESCALATE:
                if (usageOutsideOfPackage) {
                    if (flags.contains(Modifier.PRIVATE)) {
                        newModifiers.removeAll(ALL_ACCESS_MODIFIERS);
                        newModifiers.add(Modifier.PUBLIC);
                    }
                } else {
                    if (flags.contains(Modifier.PRIVATE)) {
                        newModifiers.removeAll(ALL_ACCESS_MODIFIERS);
                    }
                }
                break;
            case ASIS:
            default:
                break;
            case PUBLIC:
                newModifiers.removeAll(ALL_ACCESS_MODIFIERS);
                newModifiers.add(Modifier.PUBLIC);
                break;
            case PROTECTED:
                newModifiers.removeAll(ALL_ACCESS_MODIFIERS);
                newModifiers.add(Modifier.PROTECTED);
                break;
            case DEFAULT:
                newModifiers.removeAll(ALL_ACCESS_MODIFIERS);
                break;
            case PRIVATE:
                newModifiers.removeAll(ALL_ACCESS_MODIFIERS);
                newModifiers.add(Modifier.PRIVATE);
                break;
        }
        ModifiersTree modifiers = make.Modifiers(newModifiers);
        return modifiers;
    }

    private <T extends Tree> T fixReferences(T body, Element target, final TreePath resolvedPath) {
                
        TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, resolvedPath, true, true, true, true, true);
        final TypeElement enclosingClass = (TypeElement) workingCopy.getTrees().getElement(enclosingClassPath);

        final Map<Tree, Tree> original2Translated = new HashMap<Tree, Tree>();
        
        // TODO What about non static stuff.
        TreeScanner<Void, Void> idScan = new TreeScanner<Void, Void>() {

            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                // TODO Check if this is necesary, probably it is no problem to change them all, if it is not moved.

                TreePath currentPath = new TreePath(resolvedPath, node);
                if (currentPath.getParentPath().getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
                    return super.visitIdentifier(node, p); // Already checked by visitMemberSelect
                }
                Element element = workingCopy.getTrees().getElement(currentPath);
                if (isElementBeingMoved(element) == null && element.getModifiers().contains(Modifier.STATIC)) {
                    Tree newTree = make.QualIdent(element);
                    original2Translated.put(node, newTree);
                }
                return super.visitIdentifier(node, p);
            }

            @Override
            public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
                TreePath currentPath = new TreePath(resolvedPath, node);
                if (currentPath.getParentPath().getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
                    return super.visitMethodInvocation(node, p); // Already checked by visitMemberSelect
                }
                Element element = workingCopy.getTrees().getElement(currentPath);
                ExpressionTree methodSelect = node.getMethodSelect();
                if(isElementBeingMoved(element) == null) {
                    if (element.getModifiers().contains(Modifier.STATIC)) {
                        Tree newTree = make.QualIdent(element);
                        original2Translated.put(methodSelect, newTree);
                    } else {
                        problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveMembersTransformer.class, "WRN_InitNoAccess")));
                    }
                }
                return super.visitMethodInvocation(node, p);
            }

            @Override
            public Void visitMemberSelect(MemberSelectTree node, Void p) {
                Element element = workingCopy.getTrees().getElement(new TreePath(resolvedPath, node));
                if (isElementBeingMoved(element) == null && element.getModifiers().contains(Modifier.STATIC)) {
                    Tree newTree = make.QualIdent(element);
                    original2Translated.put(node, newTree);
                }
                return super.visitMemberSelect(node, p);
            }
        };
        idScan.scan(body, null);

        return (T) workingCopy.getTreeUtilities().translate(body, original2Translated);
    }

    private static String getParameterName(TypeMirror type, MethodTree method, Scope scope, CompilationController info) {
        String name = JavaPluginUtils.getName(type);
        if (name == null) {
            name = JavaPluginUtils.DEFAULT_NAME;
        }

        return JavaPluginUtils.makeNameUnique(info, scope, name, method);
    }
}

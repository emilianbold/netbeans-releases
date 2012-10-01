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

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.javadoc.Doc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Tag;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring.ParameterInfo;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.netbeans.modules.refactoring.java.ui.ChangeParametersPanel.Javadoc;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * <b>!!! Do not use {@link Element} parameter of visitXXX methods. Use {@link #allMethods} instead!!!</b>
 *
 * @author Jan Becicka
 */
public class ChangeParamsTransformer extends RefactoringVisitor {

    private static final Set<Modifier> ALL_ACCESS_MODIFIERS = EnumSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC);
    private static final int NOPOS = -2;
    private Set<ElementHandle<ExecutableElement>> allMethods;
    /** refactored element is a synthetic default constructor */
    private boolean synthConstructor;
    /**
     * refactored element is a constructor; {@code null} if it is has not been initialized yet
     * @see #init()
     */
    private Boolean constructorRefactoring;
    private final ParameterInfo[] paramInfos;
    private Collection<? extends Modifier> newModifiers;
    private String returnType;
    private boolean compatible;
    private final Javadoc javaDoc;
    private final TreePathHandle refactoringSource;
    private MethodTree origMethod;
    private final String newName;
    private boolean fromIntroduce = false;
    
    public ChangeParamsTransformer(ParameterInfo[] paramInfo,
            Collection<? extends Modifier> newModifiers,
            String returnType,
            String newName,
            boolean compatible,
            Javadoc javaDoc,
            Set<ElementHandle<ExecutableElement>> am,
            TreePathHandle refactoringSource,
            boolean fromIntroduce) {
        this(paramInfo, newModifiers, returnType, newName, compatible, javaDoc, am, refactoringSource);
        this.fromIntroduce = fromIntroduce;
    }

    public ChangeParamsTransformer(ParameterInfo[] paramInfo,
            Collection<? extends Modifier> newModifiers,
            String returnType,
            String newName,
            boolean compatible,
            Javadoc javaDoc,
            Set<ElementHandle<ExecutableElement>> am,
            TreePathHandle refactoringSource) {
        this.paramInfos = paramInfo;
        this.newModifiers = newModifiers;
        this.returnType = returnType;
        this.newName = newName;
        this.compatible = compatible;
        this.javaDoc = javaDoc;
        this.allMethods = am;
        this.refactoringSource = refactoringSource;
    }
    
    private Problem problem;
    private LinkedList<ClassTree> problemClasses = new LinkedList<ClassTree>();

    public Problem getProblem() {
        return problem;
    }

    @Override
    public void setWorkingCopy(WorkingCopy workingCopy) throws ToPhaseException {
        super.setWorkingCopy(workingCopy);
        if(origMethod == null
                && workingCopy.getFileObject().equals(refactoringSource.getFileObject())) {
            TreePath resolvedPath = refactoringSource.resolve(workingCopy);
            TreePath meth = JavaPluginUtils.findMethod(resolvedPath);
            origMethod = (MethodTree) meth.getLeaf();
        }
    }

    private void checkNewModifier(TreePath tree, Element p) throws MissingResourceException {
        ClassTree classTree = (ClassTree) JavaRefactoringUtils.findEnclosingClass(workingCopy, tree, true, true, true, true, false).getLeaf();
        if(!problemClasses.contains(classTree) && !newModifiers.contains(Modifier.PUBLIC)) { // Only give one warning for every file
            Element el = workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), classTree));
            TypeElement enclosingTypeElement1 = workingCopy.getElementUtilities().outermostTypeElement(el);
            TypeElement enclosingTypeElement2 = workingCopy.getElementUtilities().outermostTypeElement(p);
            if(!workingCopy.getTypes().isSameType(enclosingTypeElement1.asType(), enclosingTypeElement2.asType())) {
                if(newModifiers.contains(Modifier.PRIVATE)) {
                    problem = MoveTransformer.createProblem(problem, false, NbBundle.getMessage(ChangeParamsTransformer.class, "ERR_StrongAccMod", Modifier.PRIVATE, enclosingTypeElement1)); //NOI18N
                    problemClasses.add(classTree);
                } else {
                    PackageElement package1 = workingCopy.getElements().getPackageOf(el);
                    PackageElement package2 = workingCopy.getElements().getPackageOf(p);
                    if(!package1.getQualifiedName().equals(package2.getQualifiedName())) {
                        if(newModifiers.contains(Modifier.PROTECTED)) {
                            if(!workingCopy.getTypes().isSubtype(enclosingTypeElement1.asType(), enclosingTypeElement2.asType())) {
                                problem = MoveTransformer.createProblem(problem, false, NbBundle.getMessage(ChangeParamsTransformer.class, "ERR_StrongAccMod", Modifier.PROTECTED, enclosingTypeElement1)); //NOI18N
                                problemClasses.add(classTree);
                            }
                        } else {
                            problem = MoveTransformer.createProblem(problem, false, NbBundle.getMessage(ChangeParamsTransformer.class, "ERR_StrongAccMod", "<default>", enclosingTypeElement1)); //NOI18N
                            problemClasses.add(classTree);
                        }
                    }
                }
            }
        }
    }

    private void init() {
        if (constructorRefactoring == null) {
            ElementHandle<ExecutableElement> handle = allMethods.iterator().next();
            constructorRefactoring = handle.getKind() == ElementKind.CONSTRUCTOR;
            Element el;
            synthConstructor = constructorRefactoring
                    && (el = handle.resolve(workingCopy)) != null
                    && workingCopy.getElementUtilities().isSynthetic(el);
        }
    }

    @Override
    public Tree visitCompilationUnit(CompilationUnitTree node, Element p) {
        init();
        return super.visitCompilationUnit(node, p);
    }

    @Override
    public Tree visitClass(ClassTree node, Element p) {
        if(compatible) {
            List<? extends Tree> members = node.getMembers();
            for (int i = 0; i < members.size(); i++) {
                Tree tree = members.get(i);
                if (tree.getKind().equals(Kind.METHOD)) {
                    ExecutableElement element = (ExecutableElement) workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), tree));
                    if (p.equals(element)) {
                        List<ExpressionTree> paramList = getNewCompatibleArguments();
                        MethodInvocationTree methodInvocation = make.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                                constructorRefactoring? make.Identifier("this") : make.Identifier(element),
                                paramList);
                        TypeMirror methodReturnType = element.getReturnType();
                        boolean hasReturn = true;
                        Types types = workingCopy.getTypes();
                        if (types.isSameType(methodReturnType, types.getNoType(TypeKind.VOID))) {
                            hasReturn = false;
                        }
                        StatementTree statement = null;
                        if (hasReturn) {
                            statement = make.Return(methodInvocation);
                        } else {
                            statement = make.ExpressionStatement(methodInvocation);
                        }
                        final GeneratorUtilities genutils = GeneratorUtilities.get(workingCopy);
                        tree = genutils.importComments(tree, workingCopy.getCompilationUnit());
                        
                        BlockTree body = make.Block(Collections.singletonList(statement), false);
                        final BlockTree oldBody = ((MethodTree)tree).getBody();
                        genutils.copyComments(oldBody, body, true);
                        genutils.copyComments(oldBody, body, false);
                        MethodTree newMethod;
                        if (!fromIntroduce) {
                            List<? extends TypeParameterElement> typeParameters = element.getTypeParameters();
                            List<TypeParameterTree> newTypeParams = new ArrayList<TypeParameterTree>(typeParameters.size());
                            transformTypeParameters(typeParameters, make, genutils, newTypeParams);

                            final List<? extends VariableElement> parameters = element.getParameters();
                            List<VariableTree> newParameters = new ArrayList<VariableTree>(parameters.size());
                            for (VariableElement variableElement : parameters) {
                                newParameters.add(make.Variable(variableElement, null));
                            }

                            final List<? extends TypeMirror> thrownTypes = element.getThrownTypes();
                            List<ExpressionTree> newThrownTypes = new ArrayList<ExpressionTree>(thrownTypes.size());
                            for (TypeMirror typeMirror : thrownTypes) {
                                newThrownTypes.add((ExpressionTree) make.Type(typeMirror));
                            }

                            newMethod = make.Method(
                                    make.Modifiers(element.getModifiers()),
                                    newName == null ? element.getSimpleName() : newName,
                                    make.Type(methodReturnType),
                                    newTypeParams,
                                    newParameters,
                                    newThrownTypes,
                                    body,
                                    null,
                                    element.isVarArgs());
                        } else {
                            newMethod = make.Method(element, body);
                        }
                        genutils.copyComments(tree, newMethod, true);
                        genutils.copyComments(tree, newMethod, false);

                        ClassTree addMember = make.insertClassMember(node, i, newMethod);
                        rewrite(node, addMember);
                    }
                }
            }
        }
        return super.visitClass(node, p);
    }
    
    @Override
    public Tree visitNewClass(NewClassTree tree, Element p) {
        if (constructorRefactoring && !compatible && !workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            ExecutableElement constructor = (ExecutableElement) p;
            final Trees trees = workingCopy.getTrees();
            Element el = trees.getElement(getCurrentPath());
            el = resolveAnonymousClassConstructor(el, tree, trees);
            if (el!=null) {
                if (isMethodMatch(el, p)) {
                    List<ExpressionTree> arguments = getNewArguments(tree.getArguments(), false, constructor);
                    NewClassTree nju = make.NewClass(tree.getEnclosingExpression(),
                            (List<ExpressionTree>)tree.getTypeArguments(),
                            tree.getIdentifier(),
                            arguments,
                            tree.getClassBody());
                    rewrite(tree, nju);
                }
            }
        }
        return super.visitNewClass(tree, p);
    }

    /**
     * special treatment for anonymous classes to resolve the proper constructor
     * of extended class instead of the synthetic one.
     * @see <a href="https://netbeans.org/bugzilla/show_bug.cgi?id=168775">#168775</a>
     */
    private Element resolveAnonymousClassConstructor(Element el, NewClassTree tree, final Trees trees) {
        if (el != null && tree.getClassBody() != null) {
            Tree t = trees.getTree(el);
            if (t != null && t.getKind() == Tree.Kind.METHOD) {
                MethodTree constructorTree = (MethodTree) t;
                Tree superCall = constructorTree.getBody().getStatements().get(0);
                TreePath superCallPath = trees.getPath(
                        getCurrentPath().getCompilationUnit(),
                        ((ExpressionStatementTree) superCall).getExpression());
                el = trees.getElement(superCallPath);
            }
        }
        return el;
    }
    
    private List<ExpressionTree> getNewCompatibleArguments() {
        List<ExpressionTree> arguments = new ArrayList();
        ParameterInfo[] pi = paramInfos;
        for (int i = 0; i < pi.length; i++) {
            int originalIndex = pi[i].getOriginalIndex();
            ExpressionTree vt;
            String value;
            if (originalIndex < 0) {
                value = pi[i].getDefaultValue();
            } else {
                value = pi[i].getName();
            }
            SourcePositions pos[] = new SourcePositions[1];
            vt = workingCopy.getTreeUtilities().parseExpression(value, pos);
            arguments.add(vt);
        }
        return arguments;
    }
    
    private List<ExpressionTree> getNewArguments(List<? extends ExpressionTree> currentArguments, boolean passThrough, ExecutableElement method) {
        List<ExpressionTree> arguments = new ArrayList();
        ParameterInfo[] pi = paramInfos;
        for (int i = 0; i < pi.length; i++) {
            int originalIndex = pi[i].getOriginalIndex();
            ExpressionTree vt;
            if (originalIndex < 0) {
                SourcePositions pos[] = new SourcePositions[1];
                if(passThrough) {
                    String value = pi[i].getName();
                    vt = workingCopy.getTreeUtilities().parseExpression(value, pos);
                } else {
                    String value = pi[i].getDefaultValue();
                    if (i == pi.length - 1 && pi[i].getType().endsWith("...")) { // NOI18N
                        // last param is vararg, so split the default value for the remaining arguments
                        MethodInvocationTree parsedExpression = (MethodInvocationTree) workingCopy.getTreeUtilities().parseExpression("method("+value+")", pos); //NOI18N
                        for (ExpressionTree expressionTree : parsedExpression.getArguments()) {
                            arguments.add(translateExpression(expressionTree, currentArguments, method));
                        }
                        break;
                    } else {
                        vt = translateExpression(workingCopy.getTreeUtilities().parseExpression(value, pos), currentArguments, method);
                    }
                }
            } else {
                if (i == pi.length - 1 && pi[i].getType().endsWith("...") && method.isVarArgs() && method.getParameters().size()-1 == originalIndex) { // NOI18N
                    // last param is vararg, so copy all remaining arguments
                    for (int j = originalIndex; j < currentArguments.size(); j++) {
                        arguments.add(currentArguments.get(j));
                    }
                    break;
                } else {
                    vt = currentArguments.get(originalIndex);
                }
            }
            arguments.add(vt);
        }
        return arguments;
    }

    @Override
    public Tree visitMethodInvocation(MethodInvocationTree tree, Element p) {
        if ((constructorRefactoring || !workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) && !compatible) {
            ExecutableElement method = (ExecutableElement) p;
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if (el!=null) {
                if (isMethodMatch(el, method)) {
                    if(newModifiers != null) {
                        checkNewModifier(getCurrentPath(), method);
                    }
                    TreePath enclosingMethod = JavaPluginUtils.findMethod(getCurrentPath());
                    Element enclosingElement = workingCopy.getTrees().getElement(enclosingMethod);
                    boolean passThrough = false;
                    if(isMethodMatch(enclosingElement, method)) {
                        passThrough = true;
                    }
                    List<ExpressionTree> arguments = getNewArguments(tree.getArguments(), passThrough, method);
                    
                    MethodInvocationTree nju = make.MethodInvocation(
                            (List<ExpressionTree>)tree.getTypeArguments(),
                            newName != null ? make.setLabel(tree.getMethodSelect(), newName) : tree.getMethodSelect(),
                            arguments);
                    
                    if (constructorRefactoring && workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
                        rewriteSyntheticConstructor(nju);
                    } else {
                        // rewrite existing super(); statement
                        rewrite(tree, nju);
                    }
                }
            }
        }
        return super.visitMethodInvocation(tree, p);
    }

    /** workaround to rewrite synthetic super(); statement */
    private void rewriteSyntheticConstructor(MethodInvocationTree nju) {
        TreePath constructorPath = getCurrentPath();
        while (constructorPath != null && constructorPath.getLeaf().getKind() != Tree.Kind.METHOD) {
            constructorPath = constructorPath.getParentPath();
        }
        if (constructorPath != null) {
            MethodTree constrTree = (MethodTree) constructorPath.getLeaf();
            BlockTree body = constrTree.getBody();
            body = make.removeBlockStatement(body, 0);
            body = make.insertBlockStatement(body, 0, make.ExpressionStatement(nju));
            if (workingCopy.getTreeUtilities().isSynthetic(constructorPath)) {
                // in case of synthetic default constructor declaration the whole constructor has to be rewritten
                MethodTree njuConstructor = make.Method(
                        make.Modifiers(constrTree.getModifiers().getFlags(),
                        constrTree.getModifiers().getAnnotations()),
                        constrTree.getName(),
                        constrTree.getReturnType(),
                        constrTree.getTypeParameters(),
                        constrTree.getParameters(),
                        constrTree.getThrows(),
                        body,
                        (ExpressionTree) constrTree.getDefaultValue());
                rewrite(constrTree, njuConstructor);
            } else {
                // declared default constructor => body rewrite is sufficient
                rewrite(constrTree.getBody(), body);
            }
        }
    }
    
    @Override
    public Tree visitMethod(MethodTree tree, Element p) {
        if (constructorRefactoring && isSyntheticConstructorOfAnnonymousClass(workingCopy.getTrees().getElement(getCurrentPath()))) {
            return tree;
        }
        renameDeclIfMatch(getCurrentPath(), tree, p);
        return super.visitMethod(tree, p);
    }

    private void renameDeclIfMatch(TreePath path, Tree tree, Element elementToFind) {
        if (!synthConstructor && workingCopy.getTreeUtilities().isSynthetic(path)) {
            return;
        }
        final GeneratorUtilities genutils = GeneratorUtilities.get(workingCopy);
        final MethodTree current;
        if(!compatible) { // Do not import comments twice.
            current = genutils.importComments((MethodTree)tree, workingCopy.getCompilationUnit());
        } else {
            current = (MethodTree) tree;
        }
        Element el = workingCopy.getTrees().getElement(path);
        if (isMethodMatch(el, elementToFind)) {
            
            List<? extends VariableTree> currentParameters = current.getParameters();
            List<VariableTree> newParameters = new ArrayList<VariableTree>(paramInfos.length);
            
            ParameterInfo[] p = paramInfos;
            for (int i=0; i<p.length; i++) {
                int originalIndex = p[i].getOriginalIndex();
                VariableTree vt;
                if (originalIndex <0) {
                    boolean isVarArgs = i == p.length -1 && p[i].getType().endsWith("..."); // NOI18N
                    vt = make.Variable(make.Modifiers(Collections.<Modifier>emptySet()),
                            p[i].getName(),
                            make.Identifier(isVarArgs? p[i].getType().replace("...", "") : p[i].getType()), // NOI18N
                            null);
                } else {
                    VariableTree originalVt = currentParameters.get(originalIndex);
                    boolean isVarArgs = i == p.length -1 && p[i].getType().endsWith("..."); // NOI18N
                    String newType = isVarArgs? p[i].getType().replace("...", "") : p[i].getType();
                    
                    final Tree typeTree;
                    if (origMethod != null) {
                        if (p[i].getType().equals(origMethod.getParameters().get(originalIndex).getType().toString())) { // Type has not changed
                            typeTree = originalVt.getType();
                        } else {
                            typeTree = make.Identifier(newType); // NOI18N
                        }
                    } else {
                        typeTree = make.Identifier(newType); // NOI18N
                    }
                    vt = make.Variable(originalVt.getModifiers(),
                            fromIntroduce? originalVt.getName() : p[i].getName(),
                            typeTree,
                            originalVt.getInitializer());
                }
                newParameters.add(vt);
            }

            // apply new access modifiers if necessary
            Set<Modifier> modifiers = new HashSet<Modifier>(current.getModifiers().getFlags());
            if (newModifiers!=null && !el.getEnclosingElement().getKind().isInterface()) {
                modifiers.removeAll(ALL_ACCESS_MODIFIERS);
                modifiers.addAll(newModifiers);
            }
            
            // apply new return type if necessary
            boolean applyNewReturnType = false;
            if(this.returnType != null) {
                ExecutableElement exEl = (ExecutableElement) el;
                String oldReturnType = exEl.getReturnType().toString();
                if(!this.returnType.equals(oldReturnType)) {
                    applyNewReturnType = true;
                }
            }

            //Compute new imports
            for (VariableTree vt : newParameters) {
                Set<ElementHandle<TypeElement>> declaredTypes = workingCopy.getClasspathInfo().getClassIndex().getDeclaredTypes(vt.getType().toString(), NameKind.SIMPLE_NAME, EnumSet.allOf(ClassIndex.SearchScope.class));
                Set<ElementHandle<TypeElement>> declaredTypesMirr = new HashSet<ElementHandle<TypeElement>>(declaredTypes);
                TypeElement type = null;

                //remove private types
                //TODO: and possibly package private?
                for (ElementHandle<TypeElement> typeName : declaredTypes) {
                    TypeElement te = workingCopy.getElements().getTypeElement(typeName.getQualifiedName());

                    if (te == null) {
                        Logger.getLogger(ChangeParamsTransformer.class.getName()).log(Level.INFO, "Cannot resolve type element \"{0}\".", typeName);
                        continue;
                    }
                    if (te.getModifiers().contains(Modifier.PRIVATE)) {
                        declaredTypesMirr.remove(typeName);
                    }

                }

                if (declaredTypesMirr.size() == 1) { //creates import if there is just one proposed type
                    ElementHandle<TypeElement> typeName = declaredTypesMirr.iterator().next();
                    TypeElement te = workingCopy.getElements().getTypeElement(typeName.getQualifiedName());

                    if (te == null) {
                        Logger.getLogger(ChangeParamsTransformer.class.getName()).log(Level.INFO, "Cannot resolve type element \"{0}\".", typeName);
                        continue;
                    }
                    type = te;
                }

                if (type != null) {
                    PackageElement packageOf = workingCopy.getElements().getPackageOf(type);
                    if (packageOf.getQualifiedName().toString().equals("java.lang")) {
                        continue;
                    }
                    try {
                        SourceUtils.resolveImport(workingCopy, path, type.getQualifiedName().toString());
                    } catch (NullPointerException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            final BlockTree body = translateBody(current.getBody(), current.getParameters(), (ExecutableElement)el);

            MethodTree nju = make.Method(
                    make.Modifiers(modifiers, current.getModifiers().getAnnotations()),
                    newName != null ? newName : current.getName(),
                    applyNewReturnType? make.Type(this.returnType) : current.getReturnType(),
                    current.getTypeParameters(),
                    newParameters,
                    current.getThrows(),
                    fromIntroduce? current.getBody() : body,
                    (ExpressionTree) current.getDefaultValue(),
                    p.length > 0 && p[p.length-1].getType().endsWith("...")); //NOI18N

            genutils.copyComments(current, nju, true);
            genutils.copyComments(current, nju, false);
            
            if(synthConstructor) {
                Comment comment = null;
                switch (javaDoc) {
                    case UPDATE:
                        comment = ChangeParamsJavaDocTransformer.updateJavadoc((ExecutableElement) el, paramInfos, workingCopy);
                        List<Comment> comments = workingCopy.getTreeUtilities().getComments(nju, true);
                        if (comments.isEmpty()) {
                            comment = null;
                        } else {
                            if (comments.get(0).isDocComment()) {
                                make.removeComment(nju, 0, true);
                            } else {
                                comment = null;
                            }
                        }
                        break;
                    case GENERATE:
                        String returnTypeString;
                        Tree returnType = nju.getReturnType();
                        if (this.returnType == null) {
                            boolean hasReturn = false;
                            if (returnType != null && returnType.getKind().equals(Tree.Kind.PRIMITIVE_TYPE)) {
                                if (!((PrimitiveTypeTree) returnType).getPrimitiveTypeKind().equals(TypeKind.VOID)) {
                                    hasReturn = true;
                                }
                            }
                            if (hasReturn) {
                                returnTypeString = returnType.toString();
                            } else {
                                returnTypeString = null;
                            }
                        } else {
                            if(this.returnType.equals("void")) {
                                returnTypeString = null;
                            } else {
                                returnTypeString = this.returnType;
                            }
                        }
                        comment = ChangeParamsJavaDocTransformer.generateJavadoc(newParameters, returnTypeString, current);
                        break;
                }
                if (comment != null) {
                    make.addComment(nju, comment, true);
                }
            }

            rewrite(tree, nju);
        }
    }

    private boolean isMethodMatch(Element method, Element p) {
        if(compatible) {
            return (method.getKind() == ElementKind.METHOD || method.getKind() == ElementKind.CONSTRUCTOR) && method == p;
        } else if ((method.getKind() == ElementKind.METHOD || method.getKind() == ElementKind.CONSTRUCTOR) && allMethods !=null) {
            for (ElementHandle<ExecutableElement> mh: allMethods) {
                ExecutableElement baseMethod =  mh.resolve(workingCopy);
                if (baseMethod==null) {
                    Logger.getLogger("org.netbeans.modules.refactoring.java").info("ChangeParamsTransformer cannot resolve " + mh);
                    continue;
                }
                if (baseMethod.equals(method) || workingCopy.getElements().overrides((ExecutableElement)method, baseMethod, workingCopy.getElementUtilities().enclosingTypeElement(baseMethod))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSyntheticConstructorOfAnnonymousClass(Element el) {
        if (el != null && el.getKind() == ElementKind.CONSTRUCTOR
                && workingCopy.getElementUtilities().isSynthetic(el)) {
            Element enclosingElement = el.getEnclosingElement();
            return enclosingElement != null && enclosingElement.getKind().isClass()
                    && ((TypeElement) enclosingElement).getNestingKind() == NestingKind.ANONYMOUS;
        }
        return false;
    }
    
    private BlockTree translateBody(BlockTree blockTree,  final List<? extends VariableTree> parameters, ExecutableElement p) {
        final Map<ExpressionTree, ExpressionTree> original2Translated = new HashMap<ExpressionTree, ExpressionTree>();
        boolean changed = false;
        do {
            original2Translated.clear();
            TreeScanner<Void, Void> idScan = new TreeScanner<Void, Void>() {
                @Override
                public Void visitIdentifier(IdentifierTree node, Void p) {
                    String name = node.getName().toString();
                    if (getCurrentPath().getParentPath().getLeaf().getKind() != Kind.MEMBER_SELECT){
                        for (int i = 0; i < paramInfos.length; i++) {
                            ParameterInfo parameterInfo = paramInfos[i];
                            if(parameterInfo.getOriginalIndex() >= 0 &&
                                    parameters.get(parameterInfo.getOriginalIndex()).getName().contentEquals(name)) {
                                original2Translated.put(node, make.Identifier(parameterInfo.getName()));
                            }
                        }
                    }
                    return super.visitIdentifier(node, p);
                }
            };
            idScan.scan(blockTree, null);
            blockTree = (BlockTree) workingCopy.getTreeUtilities().translate(blockTree, original2Translated);
            
            original2Translated.clear();
            TreeScanner<Boolean, ExecutableElement> methodScanner = new TreeScanner<Boolean, ExecutableElement>() {
                @Override
                public Boolean visitMethodInvocation(MethodInvocationTree node, ExecutableElement p) {
                    boolean changed = false;
                    final TreePath path = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), node);
                    if(path != null) {
                        Element el = workingCopy.getTrees().getElement(path);
                        if (el!=null) {
                            if (isMethodMatch(el, p)) {
                                List<ExpressionTree> arguments = getNewArguments(node.getArguments(), false, p);
                                MethodInvocationTree nju = make.MethodInvocation(
                                        (List<ExpressionTree>)node.getTypeArguments(),
                                        newName != null ? make.setLabel(node.getMethodSelect(), newName) : node.getMethodSelect(),
                                        arguments);
                                original2Translated.put(node, nju);
                                changed = true;
                            }
                        }
                    }
                    return super.visitMethodInvocation(node, p) || changed;
                }
                
                @Override
                public Boolean reduce(Boolean r1, Boolean r2) {
                    return r1 == Boolean.TRUE || r2 == Boolean.TRUE;
                }
            };
            changed = methodScanner.scan(blockTree, p) == Boolean.TRUE;
            if(changed) {
                blockTree = (BlockTree) workingCopy.getTreeUtilities().translate(blockTree, original2Translated);
            }
        } while(changed);

        return blockTree;
    }

    private ExpressionTree translateExpression(ExpressionTree expressionTree, final List<? extends ExpressionTree> currentArguments, ExecutableElement p) {
        final Map<ExpressionTree, ExpressionTree> original2Translated = new HashMap<ExpressionTree, ExpressionTree>();
        boolean changed = false;
        do {
            original2Translated.clear();
            TreeScanner<Void, Void> idScan = new TreeScanner<Void, Void>() {
                @Override
                public Void visitIdentifier(IdentifierTree node, Void p) {
                    String name = node.getName().toString();
                    if (getCurrentPath().getParentPath().getLeaf().getKind() != Kind.MEMBER_SELECT){
                        for (int i = 0; i < paramInfos.length; i++) {
                            ParameterInfo parameterInfo = paramInfos[i];
                            if(parameterInfo.getOriginalIndex() >= 0 && parameterInfo.getName().equals(name)) {
                                original2Translated.put(node, currentArguments.get(parameterInfo.getOriginalIndex()));
                            }
                        }
                    }
                    return super.visitIdentifier(node, p);
                }
            };
            idScan.scan(expressionTree, null);
            expressionTree = (ExpressionTree) workingCopy.getTreeUtilities().translate(expressionTree, original2Translated);
            
            original2Translated.clear();
            TreeScanner<Boolean, ExecutableElement> methodScanner = new TreeScanner<Boolean, ExecutableElement>() {
                @Override
                public Boolean visitMethodInvocation(MethodInvocationTree node, ExecutableElement p) {
                    boolean changed = false;
                    final TreePath path = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), node);
                    if(path != null) {
                        Element el = workingCopy.getTrees().getElement(path);
                        if (el!=null) {
                            if (isMethodMatch(el, p)) {
                                List<ExpressionTree> arguments = getNewArguments(node.getArguments(), false, p);
                                MethodInvocationTree nju = make.MethodInvocation(
                                        (List<ExpressionTree>)node.getTypeArguments(),
                                        newName != null ? make.setLabel(node.getMethodSelect(), newName) : node.getMethodSelect(),
                                        arguments);
                                original2Translated.put(node, nju);
                                changed = true;
                            }
                        }
                    }
                    return super.visitMethodInvocation(node, p) || changed;
                }
                
                @Override
                public Boolean reduce(Boolean r1, Boolean r2) {
                    return r1 == Boolean.TRUE || r2 == Boolean.TRUE;
                }
            };
            changed = methodScanner.scan(expressionTree, p) == Boolean.TRUE;
            if(changed) {
                expressionTree = (ExpressionTree) workingCopy.getTreeUtilities().translate(expressionTree, original2Translated);
            }
        } while(changed);

        return expressionTree;
    }
    
    private void transformTypeParameters(List<? extends TypeParameterElement> source, TreeMaker make, GeneratorUtilities genUtils, List<TypeParameterTree> newTypeParams) {
        for (TypeParameterElement typeParam : source) {
            List<? extends TypeMirror> bounds = typeParam.getBounds();
            List<ExpressionTree> newBounds = new ArrayList<ExpressionTree>(bounds.size());
            for (TypeMirror typeMirror : bounds) {
                TypeMirror typeObject = workingCopy.getElements().getTypeElement("java.lang.Object").asType();
                if (!workingCopy.getTypes().isSameType(typeMirror, typeObject)) {
                    ExpressionTree type = (ExpressionTree) make.Type(typeMirror);
                    newBounds.add(type);
                }
            }
            TypeParameterTree typeParameterTree = make.TypeParameter(typeParam.getSimpleName(), newBounds);
            if (!typeParameterTree.getBounds().isEmpty()) {
                typeParameterTree = (TypeParameterTree) genUtils.importFQNs(typeParameterTree);
            }
            newTypeParams.add(typeParameterTree);
        }
    }
}

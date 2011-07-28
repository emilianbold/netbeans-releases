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
import com.sun.source.util.Trees;
import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring.ParameterInfo;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
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
    private final ChangeParametersRefactoring refactoring;

    public ChangeParamsTransformer(ChangeParametersRefactoring refactoring, Set<ElementHandle<ExecutableElement>> am) {
        this.refactoring = refactoring;
        this.paramInfos = refactoring.getParameterInfo();
        this.newModifiers = refactoring.getModifiers();
        this.returnType = refactoring.getReturnType();
        this.allMethods = am;
    }
    
    private Problem problem;
    private LinkedList<ClassTree> problemClasses = new LinkedList<ClassTree>();

    public Problem getProblem() {
        return problem;
    }

    private void checkNewModifier(TreePath tree, Element p) throws MissingResourceException {
        ClassTree classTree = (ClassTree) RetoucheUtils.findEnclosingClass(workingCopy, tree, true, true, true, true, false).getLeaf();
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
    public Tree visitNewClass(NewClassTree tree, Element p) {
        if (constructorRefactoring && !workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            final Trees trees = workingCopy.getTrees();
            Element el = trees.getElement(getCurrentPath());
            el = resolveAnonymousClassConstructor(el, tree, trees);
            if (el!=null) {
                if (isMethodMatch(el)) {
                    List<ExpressionTree> arguments = getNewArguments(tree.getArguments());
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
    
    private List<ExpressionTree> getNewArguments(List<? extends ExpressionTree> currentArguments) {
        List<ExpressionTree> arguments = new ArrayList();
        ParameterInfo[] pi = paramInfos;
        for (int i = 0; i < pi.length; i++) {
            int originalIndex = pi[i].getOriginalIndex();
            ExpressionTree vt;
            if (originalIndex < 0) {
                String value = pi[i].getDefaultValue();
                SourcePositions pos[] = new SourcePositions[1];
                vt = workingCopy.getTreeUtilities().parseExpression(value, pos);
            } else {
                if (i == pi.length - 1 && pi[i].getType().endsWith("...")) { // NOI18N
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
        if (constructorRefactoring || !workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if (el!=null) {
                if (isMethodMatch(el)) {
                    checkNewModifier(getCurrentPath(), p);
                    List<ExpressionTree> arguments = getNewArguments(tree.getArguments());
                    
                    MethodInvocationTree nju = make.MethodInvocation(
                            (List<ExpressionTree>)tree.getTypeArguments(),
                            tree.getMethodSelect(),
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
        if (!synthConstructor && workingCopy.getTreeUtilities().isSynthetic(path))
            return;
        MethodTree current = (MethodTree) tree;
        Element el = workingCopy.getTrees().getElement(path);
        if (isMethodMatch(el)) {
            
            List<? extends VariableTree> currentParameters = current.getParameters();
            List<VariableTree> newParameters = new ArrayList<VariableTree>();
            
            ParameterInfo[] p = paramInfos;
            for (int i=0; i<p.length; i++) {
                int originalIndex = p[i].getOriginalIndex();
                VariableTree vt;
                if (originalIndex <0) {
                    vt = make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), p[i].getName(),make.Identifier(p[i].getType()), null);
                } else {
                    VariableTree originalVt = currentParameters.get(p[i].getOriginalIndex());
                    vt = make.Variable(originalVt.getModifiers(),
                            originalVt.getName(),
                            make.Identifier(p[i].getType()),
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
                        Logger.getLogger(ChangeParamsTransformer.class.getName()).log(Level.INFO, "Cannot resolve type element \"" + typeName + "\".");
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
                        Logger.getLogger(ChangeParamsTransformer.class.getName()).log(Level.INFO, "Cannot resolve type element \"" + typeName + "\".");
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


            MethodTree nju = make.Method(
                    make.Modifiers(modifiers, current.getModifiers().getAnnotations()),
                    current.getName(),
                    applyNewReturnType? make.Type(this.returnType) : current.getReturnType(),
                    current.getTypeParameters(),
                    newParameters,
                    current.getThrows(),
                    current.getBody(),
                    (ExpressionTree) current.getDefaultValue());

            Javadoc javadoc = refactoring.getContext().lookup(Javadoc.class);
            if (javadoc != null) {
                Comment comment = null;
                switch (javadoc) {
                    case UPDATE:
                        ArrayList removed = new ArrayList(currentParameters);
                        removed.removeAll(newParameters);
                        comment = updateJavadoc((ExecutableElement) el, removed, paramInfos);
                        GeneratorUtilities.get(workingCopy).copyComments(current, nju, true);
                        List<Comment> comments = workingCopy.getTreeUtilities().getComments(nju, true);
                        if(comments.isEmpty()) {
                            comment = null;
                        } else {
                            if(comments.get(0).isDocComment()) {
                                make.removeComment(nju, 0, true);
                            } else {
                                comment = null;
                            }
                        }
                        break;
                    case GENERATE:
                        comment = generateJavadoc(newParameters, current);
                        break;
                }
                if(comment != null) {
                    make.addComment(nju, comment, true);
                }
            }
            
            rewrite(tree, nju);
            return;
        }
    }

    private Comment updateJavadoc(ExecutableElement method, List<? extends VariableTree> removed, ParameterInfo[] parameters) {
        Doc javadoc = workingCopy.getElementUtilities().javaDocFor(method);
        List<Tag> paramTags = new LinkedList<Tag>();
        List<Tag> otherTags = new LinkedList<Tag>(Arrays.asList(javadoc.tags()));
        List<Tag> returnTags = new LinkedList<Tag>(Arrays.asList(javadoc.tags("@return"))); // NOI18N
        List<Tag> throwsTags = new LinkedList<Tag>(Arrays.asList(javadoc.tags("@throws"))); // NOI18N
        List<Tag> oldParamTags = new LinkedList<Tag>(Arrays.asList(javadoc.tags("@param"))); // NOI18N

        otherTags.removeAll(returnTags);
        otherTags.removeAll(throwsTags);
        otherTags.removeAll(oldParamTags);
        
        params: for (ParameterInfo parameter : parameters) {
            if(parameter.getOriginalIndex() == -1) {
                Tag newTag = new ParamTagImpl(parameter.getName(), "the value of " + parameter.getName(), javadoc); // NOI18N
                paramTags.add(newTag);
            } else {
                for (Tag tag : oldParamTags) {
                    ParamTag paramTag = (ParamTag) tag;
                    if (parameter.getName().toString().equals(paramTag.parameterName())) {
                        paramTags.add(tag);
                        continue params;
                    }
                }
            }
        }
        
        StringBuilder text = new StringBuilder(javadoc.commentText()).append("\n\n"); // NOI18N
        text.append(tagsToString(paramTags));
        text.append(tagsToString(returnTags));
        text.append(tagsToString(throwsTags));
        text.append(tagsToString(otherTags));
        
        Comment comment = Comment.create(Comment.Style.JAVADOC, NOPOS, NOPOS, NOPOS, text.toString());
        return comment;
    }
        
    private Comment generateJavadoc(List<VariableTree> newParameters, MethodTree current) {
        Tree returnType = current.getReturnType();
        StringBuilder builder = new StringBuilder("\n"); // NOI18N
        for (VariableTree variableTree : newParameters) {
            builder.append(String.format("@param %s the value of %s", variableTree.getName(), variableTree.getName())); // NOI18N
            builder.append("\n"); // NOI18N
        }
        boolean hasReturn = false;
        if (returnType != null && returnType.getKind().equals(Tree.Kind.PRIMITIVE_TYPE)) {
            if (!((PrimitiveTypeTree) returnType).getPrimitiveTypeKind().equals(TypeKind.VOID)) {
                hasReturn = true;
            }
        }
        if(hasReturn) {
            builder.append("@return the ").append(returnType).append("\n"); // NOI18N
        }
        for (ExpressionTree expressionTree : current.getThrows()) {
            builder.append("@throws ").append(expressionTree).append("\n"); // NOI18N
        }
        Comment comment = Comment.create(
                Comment.Style.JAVADOC, NOPOS, NOPOS, NOPOS,
                builder.toString());
        return comment;
    }

    private void removeFromDoc(ParamTag t) {
        try {
            Position[] tagBounds = JavadocUtilities.findTagBounds(workingCopy, workingCopy.getDocument(), t);
            int length = tagBounds[1].getOffset() - tagBounds[0].getOffset();
            workingCopy.rewriteInComment(tagBounds[0].getOffset(), length, "");
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }


    private boolean isMethodMatch(Element method) {
        if ((method.getKind() == ElementKind.METHOD || method.getKind() == ElementKind.CONSTRUCTOR) && allMethods !=null) {
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

    private String tagsToString(List<Tag> tags) {
        StringBuilder sb = new StringBuilder();
        for (Tag tag : tags) {
            sb.append(tag.name()).append(" ").append(tag.text()).append("\n"); // NOI18N
        }
        return sb.toString();
    }

    private static class ParamTagImpl implements ParamTag {
        private final String name;
        private final String comment;
        private final Doc holder;

        public ParamTagImpl(String name, String comment, Doc holder) {
            this.name = name;
            this.comment = comment;
            this.holder = holder;
        }

        @Override
        public String parameterName() {
            return name;
        }

        @Override
        public String parameterComment() {
            return comment;
        }

        @Override
        public boolean isTypeParameter() {
            return false; // Not important for the javadoc update
        }

        @Override
        public String name() {
            return "@param"; // NOI18N
        }

        @Override
        public Doc holder() {
            return holder;
        }

        @Override
        public String kind() {
            return name();
        }

        @Override
        public String text() {
            return parameterName() + " " + parameterComment(); // NOI18N
        }

        @Override
        public Tag[] inlineTags() {
            return null; // Not important for the javadoc update
        }

        @Override
        public Tag[] firstSentenceTags() {
            return null; // Not important for the javadoc update
        }

        @Override
        public SourcePosition position() {
            return null; // Not important for the javadoc update
        }
    }
}

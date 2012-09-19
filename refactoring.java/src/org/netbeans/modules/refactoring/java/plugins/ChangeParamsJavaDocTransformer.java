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
 * <b>!!! Do not use {@link Element} parameter of visitXXX methods. Use {@link #allMethods}
 * instead!!!</b>
 *
 * @author Jan Becicka
 */
public class ChangeParamsJavaDocTransformer extends RefactoringVisitor {

    private static final int NOPOS = -2;
    private Set<ElementHandle<ExecutableElement>> allMethods;
    /**
     * refactored element is a synthetic default constructor
     */
    private boolean synthConstructor;
    /**
     * refactored element is a constructor; {@code null} if it is has not been
     * initialized yet
     *
     * @see #init()
     */
    private Boolean constructorRefactoring;
    private final ParameterInfo[] paramInfos;
    private String returnType;
    private boolean compatible;
    private final Javadoc javaDoc;
    private final TreePathHandle refactoringSource;
    private MethodTree origMethod;

    public ChangeParamsJavaDocTransformer(ParameterInfo[] paramInfo,
            String returnType,
            boolean compatible,
            Javadoc javaDoc,
            Set<ElementHandle<ExecutableElement>> am,
            TreePathHandle refactoringSource) {
        this.paramInfos = paramInfo;
        this.returnType = returnType;
        this.compatible = compatible;
        this.javaDoc = javaDoc;
        this.allMethods = am;
        this.refactoringSource = refactoringSource;
    }
    private Problem problem;

    public Problem getProblem() {
        return problem;
    }

    @Override
    public void setWorkingCopy(WorkingCopy workingCopy) throws ToPhaseException {
        super.setWorkingCopy(workingCopy);
        if (origMethod == null
                && workingCopy.getFileObject().equals(refactoringSource.getFileObject())) {
            TreePath resolvedPath = refactoringSource.resolve(workingCopy);
            TreePath meth = JavaPluginUtils.findMethod(resolvedPath);
            origMethod = (MethodTree) meth.getLeaf();
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

    private List<ExpressionTree> getNewArguments(List<? extends ExpressionTree> currentArguments, boolean passThrough, ExecutableElement method) {
        List<ExpressionTree> arguments = new ArrayList();
        ParameterInfo[] pi = paramInfos;
        for (int i = 0; i < pi.length; i++) {
            int originalIndex = pi[i].getOriginalIndex();
            ExpressionTree vt;
            if (originalIndex < 0) {
                SourcePositions pos[] = new SourcePositions[1];
                if (passThrough) {
                    String value = pi[i].getName();
                    vt = workingCopy.getTreeUtilities().parseExpression(value, pos);
                } else {
                    String value = pi[i].getDefaultValue();
                    if (i == pi.length - 1 && pi[i].getType().endsWith("...")) { // NOI18N
                        // last param is vararg, so split the default value for the remaining arguments
                        MethodInvocationTree parsedExpression = (MethodInvocationTree) workingCopy.getTreeUtilities().parseExpression("method(" + value + ")", pos); //NOI18N
                        for (ExpressionTree expressionTree : parsedExpression.getArguments()) {
                            arguments.add(translateExpression(expressionTree, currentArguments, method));
                        }
                        break;
                    } else {
                        vt = translateExpression(workingCopy.getTreeUtilities().parseExpression(value, pos), currentArguments, method);
                    }
                }
            } else {
                if (i == pi.length - 1 && pi[i].getType().endsWith("...") && method.isVarArgs() && method.getParameters().size() - 1 == originalIndex) { // NOI18N
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
    public Tree visitMethod(MethodTree tree, Element p) {
        if (constructorRefactoring && isSyntheticConstructorOfAnnonymousClass(workingCopy.getTrees().getElement(getCurrentPath()))) {
            return tree;
        }
        renameDeclIfMatch(getCurrentPath(), tree, p);
        return super.visitMethod(tree, p);
    }

    private void renameDeclIfMatch(TreePath path, Tree tree, Element elementToFind) {
        if (workingCopy.getTreeUtilities().isSynthetic(path)) {
            return;
        }
        if (javaDoc != null) {
        final GeneratorUtilities genutils = GeneratorUtilities.get(workingCopy);
        Element el = workingCopy.getTrees().getElement(path);
        if (isMethodMatch(el, elementToFind)) {
            final MethodTree current = genutils.importComments((MethodTree) tree, workingCopy.getCompilationUnit());
            List<? extends VariableTree> currentParameters = current.getParameters();
            List<VariableTree> newParameters = new ArrayList<VariableTree>(paramInfos.length);

            ParameterInfo[] p = paramInfos;
            for (int i = 0; i < p.length; i++) {
                int originalIndex = p[i].getOriginalIndex();
                VariableTree vt;
                if (originalIndex < 0) {
                    boolean isVarArgs = i == p.length - 1 && p[i].getType().endsWith("..."); // NOI18N
                    vt = make.Variable(make.Modifiers(Collections.<Modifier>emptySet()),
                            p[i].getName(),
                            make.Identifier(isVarArgs ? p[i].getType().replace("...", "") : p[i].getType()), // NOI18N
                            null);
                } else {
                    VariableTree originalVt = currentParameters.get(originalIndex);
                    boolean isVarArgs = i == p.length - 1 && p[i].getType().endsWith("..."); // NOI18N
                    String newType = isVarArgs ? p[i].getType().replace("...", "") : p[i].getType();

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
                            originalVt.getName(),
                            typeTree,
                            originalVt.getInitializer());
                }
                newParameters.add(vt);
            }

            
                MethodTree copy = make.Method(current.getModifiers(),
                        current.getName(),
                        current.getReturnType(),
                        current.getTypeParameters(),
                        current.getParameters(),
                        current.getThrows(),
                        current.getBody(),
                        (ExpressionTree) current.getDefaultValue()
                );
                genutils.copyComments(current, copy, true);
                genutils.copyComments(current, copy, false);
                Comment comment = null;
                switch (javaDoc) {
                    case UPDATE:
                        comment = updateJavadoc((ExecutableElement) el, paramInfos, workingCopy);
                        List<Comment> comments = workingCopy.getTreeUtilities().getComments(copy, true);
                        if (comments.isEmpty()) {
                            comment = null;
                        } else {
                            if (comments.get(0).isDocComment()) {
                                make.removeComment(copy, 0, true);
                            } else {
                                comment = null;
                            }
                        }
                        break;
                    case GENERATE:
                        String returnTypeString;
                        Tree returnType = copy.getReturnType();
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
                        comment = generateJavadoc(newParameters, returnTypeString, current);
                        break;
                }
                if (comment != null) {
                    make.addComment(copy, comment, true);
                }
                rewrite(current, copy);
            }
        }
    }

    static Comment updateJavadoc(ExecutableElement method, ParameterInfo[] parameters, WorkingCopy workingCopy) {
        Doc javadoc = workingCopy.getElementUtilities().javaDocFor(method);
        List<? extends VariableElement> origParams = method.getParameters();
        List<Tag> paramTags = new LinkedList<Tag>();
        List<Tag> otherTags = new LinkedList<Tag>(Arrays.asList(javadoc.tags()));
        List<Tag> returnTags = new LinkedList<Tag>(Arrays.asList(javadoc.tags("@return"))); // NOI18N
        List<Tag> throwsTags = new LinkedList<Tag>(Arrays.asList(javadoc.tags("@throws"))); // NOI18N
        List<Tag> oldParamTags = new LinkedList<Tag>(Arrays.asList(javadoc.tags("@param"))); // NOI18N

        otherTags.removeAll(returnTags);
        otherTags.removeAll(throwsTags);
        otherTags.removeAll(oldParamTags);

        params:
        for (ParameterInfo parameter : parameters) {
            if (parameter.getOriginalIndex() == -1) {
                Tag newTag = new ParamTagImpl(parameter.getName(), "the value of " + parameter.getName(), javadoc); // NOI18N
                paramTags.add(newTag);
            } else {
                VariableElement origVar = origParams.get(parameter.getOriginalIndex());
                for (Tag tag : oldParamTags) {
                    ParamTag paramTag = (ParamTag) tag;
                    if (origVar.getSimpleName().toString().equals(paramTag.parameterName())) {
                        paramTags.add(updateTag(paramTag, paramTag.parameterName(), parameter.getName()));
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

    static Comment generateJavadoc(List<VariableTree> newParameters, String returnType, MethodTree current) {
        StringBuilder builder = new StringBuilder("\n"); // NOI18N
        for (VariableTree variableTree : newParameters) {
            builder.append(String.format("@param %s the value of %s", variableTree.getName(), variableTree.getName())); // NOI18N
            builder.append("\n"); // NOI18N
        }
        if (returnType != null) {
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

    private boolean isMethodMatch(Element method, Element p) {
        if (compatible) {
            return (method.getKind() == ElementKind.METHOD || method.getKind() == ElementKind.CONSTRUCTOR) && method == p;
        } else if ((method.getKind() == ElementKind.METHOD || method.getKind() == ElementKind.CONSTRUCTOR) && allMethods != null) {
            for (ElementHandle<ExecutableElement> mh : allMethods) {
                ExecutableElement baseMethod = mh.resolve(workingCopy);
                if (baseMethod == null) {
                    Logger.getLogger("org.netbeans.modules.refactoring.java").info("ChangeParamsTransformer cannot resolve " + mh);
                    continue;
                }
                if (baseMethod.equals(method) || workingCopy.getElements().overrides((ExecutableElement) method, baseMethod, workingCopy.getElementUtilities().enclosingTypeElement(baseMethod))) {
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

    static String tagsToString(List<Tag> tags) {
        StringBuilder sb = new StringBuilder();
        for (Tag tag : tags) {
            sb.append(tag.name()).append(" ").append(tag.text()).append("\n"); // NOI18N
        }
        return sb.toString();
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
                    if (getCurrentPath().getParentPath().getLeaf().getKind() != Kind.MEMBER_SELECT) {
                        for (int i = 0; i < paramInfos.length; i++) {
                            ParameterInfo parameterInfo = paramInfos[i];
                            if (parameterInfo.getOriginalIndex() >= 0 && parameterInfo.getName().equals(name)) {
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
                    if (path != null) {
                        Element el = workingCopy.getTrees().getElement(path);
                        if (el != null) {
                            if (isMethodMatch(el, p)) {
                                List<ExpressionTree> arguments = getNewArguments(node.getArguments(), false, p);
                                MethodInvocationTree nju = make.MethodInvocation(
                                        (List<ExpressionTree>) node.getTypeArguments(),
                                        node.getMethodSelect(),
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
            if (changed) {
                expressionTree = (ExpressionTree) workingCopy.getTreeUtilities().translate(expressionTree, original2Translated);
            }
        } while (changed);

        return expressionTree;
    }
    
    private static ParamTag updateTag(ParamTag tag, String oldName, String newName) {
        if(oldName.contentEquals(newName)) {
            return tag;
        } else {
            String comment = tag.parameterComment().replaceAll("\\b" + oldName + "\\b", newName);
            return new ParamTagImpl(newName, comment, tag.holder());
        }
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

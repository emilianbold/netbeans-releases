/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.suggestions;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LambdaExpressionTree.BodyKind;
import static com.sun.source.tree.LambdaExpressionTree.BodyKind.STATEMENT;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
public class Lambda {
    
    @Hint(displayName="#DN_lambda2Class", description="#DESC_lambda2Class", category="suggestions", hintKind=Hint.Kind.ACTION)
    @Messages({
        "DN_lambda2Class=Convert lambda expression to anonymous innerclass",
        "DESC_lambda2Class=Converts lambda expression to anonymous innerclass",
        "ERR_lambda2Class="
    })
    @TriggerTreeKind(Kind.LAMBDA_EXPRESSION)
    public static ErrorDescription lambda2Class(HintContext ctx) {
        TypeMirror samType = ctx.getInfo().getTrees().getTypeMirror(ctx.getPath());
        
        if (samType == null || samType.getKind() != TypeKind.DECLARED) return null;
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_lambda2Class(), new Lambda2Anonymous(ctx.getInfo(), ctx.getPath()).toEditorFix());
    }
    
    @Hint(displayName="#DN_expression2Return", description="#DESC_expression2Return", category="suggestions", hintKind=Hint.Kind.ACTION)
    @Messages({
        "DN_expression2Return=Convert lambda body to use a block rather than an expression",
        "DESC_expression2Return=Converts lambda body to use a block rather than an expression",
        "ERR_expression2Return=",
        "FIX_expression2Return=Use block as the lambda's body"
    })
    @TriggerPattern("($args$) -> $lambdaExpression")
    public static ErrorDescription expression2Return(HintContext ctx) {
        if (((LambdaExpressionTree) ctx.getPath().getLeaf()).getBodyKind() != BodyKind.EXPRESSION) return null;
        
        TypeMirror lambdaExpressionType = ctx.getInfo().getTrees().getTypeMirror(ctx.getVariables().get("$lambdaExpression"));
        String target =   lambdaExpressionType == null || lambdaExpressionType.getKind() != TypeKind.VOID
                        ? "($args$) -> { return $lambdaExpression; }"
                        : "($args$) -> { $lambdaExpression; }";
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_expression2Return(), JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_expression2Return(), ctx.getPath(), target));
    }
    
    @Hint(displayName="#DN_memberReference2Lambda", description="#DESC_memberReference2Lambda", category="suggestions", hintKind=Hint.Kind.ACTION)
    @Messages({
        "DN_memberReference2Lambda=Convert member reference to lambda expression",
        "DESC_memberReference2Lambda=Converts member references to a lambda expression",
        "ERR_memberReference2Lambda=",
        "FIX_memberReference2Lambda=Convert to use lambda expression"
    })
    @TriggerTreeKind(Kind.MEMBER_REFERENCE)
    public static ErrorDescription reference2Lambda(HintContext ctx) {
        Element refered = ctx.getInfo().getTrees().getElement(ctx.getPath());
        
        if (refered == null || refered.getKind() != ElementKind.METHOD) return null;//XXX: constructors!
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_memberReference2Lambda(), new MemberReference2Lambda(ctx.getInfo(), ctx.getPath()).toEditorFix());
    }
    
    @Hint(displayName="#DN_addExplicitLambdaParameters", description="#DESC_addExplicitLambdaParameters", category="suggestions", hintKind=Hint.Kind.ACTION)
    @Messages({
        "DN_addExplicitLambdaParameters=Convert lambda to use explicit parameter types",
        "DESC_addExplicitLambdaParameters=Converts lambda to use explicit parameter types",
        "ERR_addExplicitLambdaParameters=",
        "FIX_addExplicitLambdaParameters=Convert lambda to use explicit parameter types"
    })
    @TriggerTreeKind(Kind.LAMBDA_EXPRESSION)
    public static ErrorDescription explicitParameterTypes(HintContext ctx) {
        LambdaExpressionTree let = (LambdaExpressionTree) ctx.getPath().getLeaf();
        boolean hasSyntheticParameterName = false;
        
        for (VariableTree var : let.getParameters()) {
            hasSyntheticParameterName |= ctx.getInfo().getTreeUtilities().isSynthetic(TreePath.getPath(ctx.getPath(), var.getType()));
        }
        
        if (!hasSyntheticParameterName) return null;
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_addExplicitLambdaParameters(), new AddExplicitLambdaParameterTypes(ctx.getInfo(), ctx.getPath()).toEditorFix());
    }
    
    private static ExecutableElement findAbstractMethod(CompilationInfo info, TypeMirror type) {
        if (type.getKind() != TypeKind.DECLARED) return null;
        
        TypeElement clazz = (TypeElement) ((DeclaredType) type).asElement();
        
        if (!clazz.getKind().isInterface()) return null;
        
        for (ExecutableElement ee : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
            if (ee.getModifiers().contains(Modifier.ABSTRACT)) return ee;
        }
        
        for (TypeMirror tm : info.getTypes().directSupertypes(type)) {
            ExecutableElement ee = findAbstractMethod(info, tm);
            
            if (ee != null) return ee;
        }
        
        return null;
    }
    
    private static final class Lambda2Anonymous extends JavaFix {

        public Lambda2Anonymous(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        @Messages("FIX_lambda2Class=Convert lambda expression to anonymous innerclass")
        protected String getText() {
            return Bundle.FIX_lambda2Class();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            final WorkingCopy copy = ctx.getWorkingCopy();
            LambdaExpressionTree lambda = (LambdaExpressionTree) ctx.getPath().getLeaf();
            TypeMirror samType = copy.getTrees().getTypeMirror(ctx.getPath());
            
            if (samType == null || samType.getKind() != TypeKind.DECLARED) {
                //XXX
                return ;
            }
            
            ExecutableType descriptorType = copy.getTypeUtilities().getDescriptorType((DeclaredType) samType);
            ExecutableElement abstractMethod = findAbstractMethod(copy, samType);
            TypeElement samTypeElement = (TypeElement) ((DeclaredType) samType).asElement();
            List<VariableTree> methodParams = new ArrayList<>();
            Iterator<? extends TypeMirror> resolvedParamTypes = descriptorType.getParameterTypes().iterator();
            Iterator<? extends VariableTree> actualParams = lambda.getParameters().iterator();
            final TreeMaker make = copy.getTreeMaker();
            
            while (resolvedParamTypes.hasNext() && actualParams.hasNext()) {
                VariableTree p = actualParams.next();
                TypeMirror resolvedType = resolvedParamTypes.next();
                
                //XXX: should handle anonymous lambda parameters ('_')
                if (p.getType() == null || copy.getTreeUtilities().isSynthetic(new TreePath(ctx.getPath(), p.getType()))) {
                    methodParams.add(make.Variable(p.getModifiers(), p.getName(), make.Type(Utilities.resolveCapturedType(copy, resolvedType)), null));
                } else {
                    methodParams.add(p);
                }
            }
            
            BlockTree newMethodBody;
            switch (lambda.getBodyKind()) {
                case STATEMENT:
                    newMethodBody = (BlockTree) lambda.getBody();
                    break;
                case EXPRESSION:
                    StatementTree mainStatement;
                    if (descriptorType.getReturnType() == null || descriptorType.getReturnType().getKind() != TypeKind.VOID) {
                        mainStatement = make.Return((ExpressionTree) lambda.getBody());
                    } else {
                        mainStatement = make.ExpressionStatement((ExpressionTree) lambda.getBody());
                    }
                    newMethodBody = make.Block(Collections.singletonList(mainStatement), false);
                    break;
                default:
                    throw new IllegalStateException();
            }
            
            MethodTree newMethod = make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                                               abstractMethod.getSimpleName(),
                                               make.Type(descriptorType.getReturnType()),
                                               Collections.<TypeParameterTree>emptyList(), //XXX: type parameters
                                               methodParams,
                                               Collections.<ExpressionTree>emptyList(), //XXX: throws types
                                               newMethodBody,
                                               null);
            ClassTree innerClass = make.Class(make.Modifiers(EnumSet.noneOf(Modifier.class)),
                                              samTypeElement.getSimpleName(),
                                              Collections.<TypeParameterTree>emptyList(),
                                              null,
                                              Collections.<Tree>emptyList(),
                                              Collections.singletonList(newMethod));
            ExpressionTree targetTypeTree;
            
            if (((DeclaredType) samType).getTypeArguments().isEmpty()) {
                targetTypeTree = make.QualIdent(samTypeElement);
            } else {
                List<Tree> typeArguments = new ArrayList<>();
                for (TypeMirror ta : ((DeclaredType) samType).getTypeArguments()) {
                    typeArguments.add(make.Type(Utilities.resolveCapturedType(copy, ta)));
                }
                targetTypeTree = (ExpressionTree) make.ParameterizedType(make.QualIdent(samTypeElement), typeArguments);
            }
            
            NewClassTree newClass = make.NewClass(null, Collections.<ExpressionTree>emptyList(), targetTypeTree, Collections.<ExpressionTree>emptyList(), innerClass);
            
            copy.rewrite(ctx.getPath().getLeaf(), newClass);
            
            TreePath clazz = ctx.getPath();
            
            while (clazz != null && !TreeUtilities.CLASS_TREE_KINDS.contains(clazz.getLeaf().getKind())) {
                clazz = clazz.getParentPath();
            }
            
            if (clazz != null) {
                final Name outterClassName = ((ClassTree) clazz.getLeaf()).getSimpleName();
                
                new TreeScanner<Void, Void>() {
                    @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                        if (node.getName().contentEquals("this")) {
                            copy.rewrite(node, make.MemberSelect(make.Identifier(outterClassName), "this"));
                        }
                        return super.visitIdentifier(node, p);
                    }
                }.scan(lambda.getBody(), null);
            }
        }
        
    }
    
    private static final class MemberReference2Lambda extends JavaFix {

        public MemberReference2Lambda(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return Bundle.FIX_memberReference2Lambda();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            TreePath reference = ctx.getPath();
            Element refered = ctx.getWorkingCopy().getTrees().getElement(reference);

            if (refered == null || refered.getKind() != ElementKind.METHOD) {
                //TODO: log
                return ;
            }

            MemberReferenceTree mrt = (MemberReferenceTree) ctx.getPath().getLeaf();

            Element on = ctx.getWorkingCopy().getTrees().getElement(new TreePath(ctx.getPath(), mrt.getQualifierExpression()));
            ExpressionTree reciever = mrt.getQualifierExpression();
            List<VariableTree> formals = new ArrayList<>();
            List<IdentifierTree> actuals = new ArrayList<>();
            TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
            
            if (on != null && (on.getKind().isClass() || on.getKind().isInterface()) && !refered.getModifiers().contains(Modifier.STATIC)) {
                //static reference to instance method:
                formals.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "_this", null, null));
                reciever = make.Identifier("_this");
            }
            
            for (VariableElement param : ((ExecutableElement) refered).getParameters()) {
                formals.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), param.getSimpleName(), null, null));
                actuals.add(make.Identifier(param.getSimpleName()));
            }
            
            LambdaExpressionTree lambda = make.LambdaExpression(formals, make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(reciever, mrt.getName()), actuals));
            
            ctx.getWorkingCopy().rewrite(mrt, lambda);
        }
        
    }
    
    private static final class AddExplicitLambdaParameterTypes extends JavaFix {

        public AddExplicitLambdaParameterTypes(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return Bundle.FIX_addExplicitLambdaParameters();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            LambdaExpressionTree let = (LambdaExpressionTree) ctx.getPath().getLeaf();

            for (VariableTree var : let.getParameters()) {
                TreePath typePath = TreePath.getPath(ctx.getPath(), var.getType());
                if (ctx.getWorkingCopy().getTreeUtilities().isSynthetic(typePath)) {
                    Tree imported = ctx.getWorkingCopy().getTreeMaker().Type(ctx.getWorkingCopy().getTrees().getTypeMirror(typePath));
                    ctx.getWorkingCopy().rewrite(var.getType(), imported);
                }
            }
        }
        
    }
}

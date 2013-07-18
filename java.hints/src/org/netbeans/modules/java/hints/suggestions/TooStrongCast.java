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

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Type.TypeVar;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.hints.suggestions.Bundle.*;


/**
 * Detects a typecast, which is too strong.
 *
 * @author sdedic
 */
@NbBundle.Messages({
        "DN_TooStrongCast=Type cast is too strong",
        "# {0} - the new casted-to type; simple name",
        "FIX_ChangeCastTo=Cast to {0}",
        "# {0} - the type",
        "FIX_RemoveUnneededCast=Remove redundant cast to {0}",
        "# {0} - the type simple name",
        "TEXT_UnnecessaryCast=Unnecessary cast to {0}",
        "# {0} - the current type simple name",
        "# {1} - list of types that should be used ",
        "TEXT_TooStrongCast=Type cast to {0} is too strong. {1} should be used instead",
        "# {0} - the type name",
        "TEXT_TooStrongCastListFirst={0}",
        "# {0} - the preceding portion of list",
        "# {1} - the type name",
        "TEXT_TooStrongCastListMiddle={0}, {1}",
        "# {0} - the preceding portion of list",
        "# {1} - the type name",
        "TEXT_TooStrongCastListLast={0} or {1}"
})
public class TooStrongCast {
    @Hint(
        category = "abstraction",
        displayName = "#DN_TooStrongCast",
        description = "#DESC_TooStrongCast",
        enabled = false
    )
    @TriggerPattern("($type)$expression")
    public static ErrorDescription broadTypeCast(HintContext ctx) {
        List<? extends TypeMirror>   types;
        ExpectedTypeResolver exp = new ExpectedTypeResolver(ctx.getPath(), ctx.getInfo());
        types = exp.scan(ctx.getPath(), null);
        
        if (types == null) {
            return null;
        }
        
        // obtain the type of the casted expression. Some of the proposed types may be even type-compatible,
        // which means we could remove the cast at all.
        // just in case, non-castable types should be removed.
        CompilationInfo info = ctx.getInfo();
        TypeCastTree tct = (TypeCastTree)ctx.getPath().getLeaf();
        TypeMirror casteeType = info.getTrees().getTypeMirror(new TreePath(ctx.getPath(), tct.getExpression()));
        if (casteeType == null) {
            return null;
        }
        String lst = null;
        List<TypeMirror> filteredTypes = new ArrayList<TypeMirror>(types.size());
        TypeMirror castType = info.getTrees().getTypeMirror(new TreePath(ctx.getPath(), tct.getType()));
        TypeMirror castErasure = info.getTypes().erasure(castType);
        CharSequence currentTypeName = info.getTypeUtilities().getTypeName(castType);
        for (Iterator<? extends TypeMirror> it = types.iterator(); it.hasNext(); ) {
            TypeMirror tm = it.next();
            if (tm.getKind() == TypeKind.ERROR) {
                continue;
            }
            if (tm.getKind() == TypeKind.TYPEVAR) {
                TypeVar tvar = (TypeVar)tm;
                if (tvar.isExtendsBound()) {
                    tm = tvar.getLowerBound();
                } else if (tvar.isSuperBound()) {
                    tm = tvar.getUpperBound();
                }
            }
            TypeMirror tmErasure = info.getTypes().erasure(tm);
            if (info.getTypes().isAssignable(casteeType, tm)) {
                // note: it is possible that the if the cast is not there, a method call becomes ambiguous. So in the
                // case of method/constructor invocation, check if removing the cast will select exactly one method:
                TreePath parentExec = exp.getParentExecutable();
                if (parentExec != null && checkAmbiguous(info, parentExec, exp.getArgumentIndex(), casteeType)) {
                    continue;
                }
                // remove typecast, it is completely useless...
                return ErrorDescriptionFactory.forTree(ctx, tct.getType(), TEXT_UnnecessaryCast(
                        currentTypeName), new RemoveCast(info, ctx.getPath(), exp.getTheExpression(), currentTypeName).
                        toEditorFix());
            } else if (!info.getTypeUtilities().isCastable(casteeType, tm) || 
                       !info.getTypeUtilities().isCastable(castType, tm)) {
                continue;
            }
            if (info.getTypes().isAssignable(tmErasure, castErasure)) {
                return null;
            }
            filteredTypes.add(tm);
        }
        if (filteredTypes.isEmpty()) {
            return null;
        }
        int index = 0;
        Fix[] fixes = new Fix[filteredTypes.size()];
        for (TypeMirror tm : filteredTypes) {
            CharSequence tname = info.getTypeUtilities().getTypeName(tm);
            if (index == 0) {
                lst = TEXT_TooStrongCastListFirst(tname);
            } else if (index == types.size() - 1) {
                lst = TEXT_TooStrongCastListLast(lst, tname);
            } else {
                lst = TEXT_TooStrongCastListMiddle(lst, tname);
            }
            fixes[index] = new ReplaceTypeCast(info, ctx.getPath(), tm).toEditorFix();
            index++;
        }
        String msg = TEXT_TooStrongCast(currentTypeName, lst);
        
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), msg, fixes);
    }

    /**
     * Checks whether a method or constructor call would become ambiguous if the parameter type changes.
     * 
     * @param info compilation context
     * @param parentExec path to the constructor or method invocation
     * @param argIndex
     * @param casteeType
     * @return 
     */
    private static boolean checkAmbiguous(CompilationInfo info, TreePath parentExec, int argIndex, TypeMirror casteeType) {
        List<? extends ExpressionTree> actualArgs;
        ExecutableElement el = (ExecutableElement)info.getTrees().getElement(parentExec);
        boolean method = el.getKind() == ElementKind.METHOD;
        
        DeclaredType translateTo = null;
        // must not check el.getKind(), as this(...) invocation points to a CONSTRUCTOR, but is actually a MethodInvocationTree.
        if (parentExec.getLeaf() instanceof MethodInvocationTree) {
            MethodInvocationTree mi = (MethodInvocationTree)parentExec.getLeaf();
            TreePath selectPath = new TreePath(parentExec, mi.getMethodSelect());
            switch (mi.getMethodSelect().getKind()) {
                case MEMBER_SELECT: {
                    MemberSelectTree msel = (MemberSelectTree)mi.getMethodSelect();
                    TreePath expPath = new TreePath(selectPath, msel.getExpression());
                    TypeMirror tm = info.getTrees().getTypeMirror(expPath);
                    if (tm.getKind() == TypeKind.DECLARED) {
                        translateTo = (DeclaredType)tm;
                    }
                    break;
                }
                case IDENTIFIER:
                    translateTo = (DeclaredType)el.getEnclosingElement().asType();
                    break;
            }
            actualArgs = new ArrayList<ExpressionTree>(((MethodInvocationTree)parentExec.getLeaf()).getArguments());
        } else {
            actualArgs = new ArrayList<ExpressionTree>(((NewClassTree)parentExec.getLeaf()).getArguments());
            TypeMirror tm = info.getTrees().getTypeMirror(parentExec);
            if (tm.getKind() == TypeKind.DECLARED) {
                translateTo = (DeclaredType)tm;
            }
        }
        List<TypeMirror> mtypes = new ArrayList<TypeMirror>(actualArgs.size());
        for (int i = 0; i < actualArgs.size(); i++) {
            if (i == argIndex) {
                mtypes.add(info.getTypes().erasure(casteeType));
            } else {
                mtypes.add(info.getTypes().erasure(
                        info.getTrees().getTypeMirror(new TreePath(parentExec, actualArgs.get(i)))
                ));
            }
        }
        Name n = el.getSimpleName();
        TypeElement typeEl = (TypeElement)el.getEnclosingElement();
        int foundCnt = 0;
        TypeMirror mostSpecificType = null;
        for (ExecutableElement ex : method ?
                ElementFilter.methodsIn(info.getElements().getAllMembers(typeEl)) : 
                ElementFilter.constructorsIn(info.getElements().getAllMembers(typeEl))) {
            if (!ex.getSimpleName().contentEquals(n)) {
                continue;
            }
            if (ex.getParameters().size() != el.getParameters().size()) {
                // TODO: varargs
                continue;
            }
            ExecutableType exType = (ExecutableType)(translateTo != null ? info.getTypes().asMemberOf(translateTo, ex) : ex.asType());
            boolean matches = true;
            TypeMirror theArgType = null;
            for (int j = 0; j < mtypes.size(); j++) {
                TypeMirror parEr = info.getTypes().erasure(exType.getParameterTypes().get(j));
                if (j == argIndex) {
                    theArgType = parEr;
                }
                if (!info.getTypes().isAssignable(mtypes.get(j), parEr)) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                if (mostSpecificType == null) {
                    mostSpecificType = theArgType;
                } else if (info.getTypes().isAssignable(mostSpecificType, theArgType)) {
                    continue;
                } else if (info.getTypes().isAssignable(theArgType, mostSpecificType)) {
                    mostSpecificType = theArgType;
                    continue;
                }
                foundCnt++;
            }
        }
        return foundCnt > 1;
    }

    /**
     * Removes a redundant typecast
     */
    private static class RemoveCast extends JavaFix {
        /**
         * Remove all Trees upto this instance. The removed trees typically consist of type-casts and
         * parenthesis, which are all redundant if the expression itself has the correct type
         */
        private final TreePathHandle upto;
        
        private final CharSequence typeName;
        
        public RemoveCast(CompilationInfo info, TreePath tp, TreePath upTo, CharSequence typeName) {
            super(info, tp);
            this.upto = TreePathHandle.create(upTo, info);
            this.typeName = typeName;
        }

        @Override
        protected String getText() {
            return FIX_RemoveUnneededCast(typeName);
        }

        @Override
        protected void performRewrite(JavaFix.TransformationContext ctx) throws Exception {
            TreePath castPath = ctx.getPath();
            if (castPath.getLeaf().getKind() != Tree.Kind.TYPE_CAST) {
                return;
            }
            TypeCastTree tct = (TypeCastTree)castPath.getLeaf();
            Tree inside = tct.getExpression();
            Tree outside;
            TreePath upper = upto.resolve(ctx.getWorkingCopy());
            if (upper != null) {
                outside = upper.getLeaf();
            } else {
                outside = tct;
            }
            ctx.getWorkingCopy().rewrite(outside, inside);
        }
    }

    private static class ReplaceTypeCast extends JavaFix {
        private TypeMirrorHandle<TypeMirror> handle;
        private String typeName;
        
        public ReplaceTypeCast(CompilationInfo info, TreePath tp, TypeMirror castToType) {
            super(info, tp);
            this.handle = TypeMirrorHandle.create(castToType);
            this.typeName = info.getTypeUtilities().getTypeName(castToType).toString();
        }

        @Override
        protected String getText() {
            return FIX_ChangeCastTo(typeName);
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            TreePath castPath = ctx.getPath();
            if (castPath.getLeaf().getKind() != Tree.Kind.TYPE_CAST) {
                return;
            }
            TypeCastTree tct = (TypeCastTree)castPath.getLeaf();
            
            TypeMirror targetType = handle.resolve(ctx.getWorkingCopy());
            Tree tt = ctx.getWorkingCopy().getTreeMaker().Type(targetType);
            ctx.getWorkingCopy().rewrite(tct.getType(), tt);
        }
    }
    
}

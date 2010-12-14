/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPatterns;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(category="rules15", suppressWarnings="UseSpecificCatch")
public class UseSpecificCatch {

    @TriggerPatterns({
        @TriggerPattern("try {$tryBlock$;} catch (java.lang.Throwable $t) {$catch$;}"),
        @TriggerPattern("try {$tryBlock$;} catch (java.lang.Throwable $t) {$catch$;} finally {$fin$;}"),
        @TriggerPattern("try {$tryBlock$;} catch (final java.lang.Throwable $t) {$catch$;}"),
        @TriggerPattern("try {$tryBlock$;} catch (final java.lang.Throwable $t) {$catch$;} finally {$fin$;}")
    })
    public static ErrorDescription hint1(HintContext ctx) {
        return impl(ctx, "java.lang.Throwable");
    }

    @TriggerPatterns({
        @TriggerPattern("try {$tryBlock$;} catch (java.lang.Exception $t) {$catch$;}"),
        @TriggerPattern("try {$tryBlock$;} catch (java.lang.Exception $t) {$catch$;} finally {$fin$;}"),
        @TriggerPattern("try {$tryBlock$;} catch (final java.lang.Exception $t) {$catch$;}"),
        @TriggerPattern("try {$tryBlock$;} catch (final java.lang.Exception $t) {$catch$;} finally {$fin$;}")
    })
    public static ErrorDescription hint2(HintContext ctx) {
        return impl(ctx, "java.lang.Exception");
    }

    private static ErrorDescription impl(HintContext ctx, String th) {
        if (ctx.getInfo().getSourceVersion().compareTo(SourceVersion.RELEASE_7) < 0) return null;
        TryTree tt = (TryTree) ctx.getPath().getLeaf();
        Set<TypeMirror> exceptions = ctx.getInfo().getTreeUtilities().getUncaughtExceptions(new TreePath(ctx.getPath(), tt.getBlock()));

        if (exceptions.size() <= 1) return null; //was catching the generic exception intentional?

        TypeElement throwable = ctx.getInfo().getElements().getTypeElement(th);

        if (throwable == null) return null;

        if (exceptions.contains(throwable.asType())) return null;

        if (assignsTo(ctx, ctx.getVariables().get("$t"), ctx.getMultiVariables().get("$catch$"))) return null;
        
        String displayName = NbBundle.getMessage(UseSpecificCatch.class, "ERR_UseSpecificCatch");

        Set<TypeMirrorHandle<TypeMirror>> exceptionHandles = new LinkedHashSet<TypeMirrorHandle<TypeMirror>>();

        for (TypeMirror tm : exceptions) {
            exceptionHandles.add(TypeMirrorHandle.create(tm));
        }

        return ErrorDescriptionFactory.forName(ctx, tt.getCatches().get(0).getParameter().getType(), displayName, JavaFix.toEditorFix(new FixImpl(ctx.getInfo(), new TreePath(ctx.getPath(), tt.getCatches().get(0)), exceptionHandles)));
    }

    static boolean assignsTo(final HintContext ctx, TreePath variable, Iterable<? extends TreePath> statements) {
        final Element tEl = ctx.getInfo().getTrees().getElement(variable);

        if (tEl == null || tEl.getKind() != ElementKind.EXCEPTION_PARAMETER) return true;
        final boolean[] result = new boolean[1];

        for (TreePath tp : statements) {
            new TreePathScanner<Void, Void>() {
                @Override
                public Void visitAssignment(AssignmentTree node, Void p) {
                    if (tEl.equals(ctx.getInfo().getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable())))) {
                        result[0] = true;
                    }
                    return super.visitAssignment(node, p);
                }
            }.scan(tp, null);
        }

        return result[0];
    }
    private static final class FixImpl extends JavaFix {

        private final Set<TypeMirrorHandle<TypeMirror>> exceptionHandles;
        
        public FixImpl(CompilationInfo info, TreePath tryStatement, Set<TypeMirrorHandle<TypeMirror>> exceptionHandles) {
            super(info, tryStatement);
            this.exceptionHandles = exceptionHandles;
        }

        @Override
        protected String getText() {
            return NbBundle.getMessage(UseSpecificCatch.class, "FIX_UseSpecificCatch");
        }

        @Override
        protected void performRewrite(WorkingCopy wc, TreePath tp, UpgradeUICallback callback) {
            List<Tree> exceptions = new LinkedList<Tree>();

            for (TypeMirrorHandle<TypeMirror> h : exceptionHandles) {
                TypeMirror tm = h.resolve(wc);

                if (tm == null) return ; //XXX: log

                exceptions.add(wc.getTreeMaker().Type(tm));
            }

            VariableTree excVar = ((CatchTree) tp.getLeaf()).getParameter();

            wc.rewrite(excVar.getType(), wc.getTreeMaker().DisjunctiveType(exceptions));
        }

    }
}

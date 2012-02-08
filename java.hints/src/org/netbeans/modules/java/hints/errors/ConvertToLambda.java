/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPatterns;
import org.netbeans.modules.java.hints.jackpot.impl.pm.PatternCompiler;
import org.netbeans.modules.java.hints.jackpot.impl.tm.Matcher;
import org.netbeans.modules.java.hints.jackpot.impl.tm.Matcher.OccurrenceDescription;
import org.netbeans.modules.java.hints.jackpot.impl.tm.Pattern;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(id=ConvertToLambda.ID, category="rules15")
public class ConvertToLambda {

    public static final String ID = "Javac_canUseLambda";
    public static final Set<String> CODES = new HashSet<String>(Arrays.asList("compiler.note.potential.lambda.found"));

    @TriggerPatterns({
        @TriggerPattern("new $clazz($params$) { $method; }")
    })
    public static ErrorDescription compute(HintContext ctx) {
        ClassTree clazz = ((NewClassTree) ctx.getPath().getLeaf()).getClassBody();
        long start = ctx.getInfo().getTrees().getSourcePositions().getStartPosition(ctx.getInfo().getCompilationUnit(), clazz);

        OUTER: for (Diagnostic<?> d : ctx.getInfo().getDiagnostics()) {
            if (start != d.getStartPosition()) continue;
            if (!CODES.contains(d.getCode())) continue;

            List<Fix> fixes = Arrays.asList(JavaFix.toEditorFix(new FixImpl(ctx.getInfo(), ctx.getPath())));

            return ErrorDescriptionFactory.createErrorDescription(ctx.getSeverity().toEditorSeverity(), d.getMessage(null), fixes, ctx.getInfo().getFileObject(), (int) d.getStartPosition(), (int) d.getEndPosition());
        }

        return null;
    }

    private static final class FixImpl extends JavaFix {

        public FixImpl(CompilationInfo info, TreePath path) {
            super(info, path);
        }

        public String getText() {
            return NbBundle.getMessage(ConvertToLambda.class, "FIX_ConvertToLambda");
        }

        @Override
        protected void performRewrite(WorkingCopy copy, TreePath tp, boolean canShowUI) {
            if (tp.getLeaf().getKind() != Kind.NEW_CLASS) {
                //XXX: warning
                return ;
            }

            TreeMaker make = copy.getTreeMaker();
            TreePath clazz = new TreePath(tp, ((NewClassTree) tp.getLeaf()).getClassBody());
            MethodTree method = (MethodTree) ((ClassTree) clazz.getLeaf()).getMembers().get(1);
            Pattern p = PatternCompiler.compile(copy, "{ return $expression; }", Collections.<String, TypeMirror>emptyMap(), Collections.<String>emptyList());
            Collection<? extends OccurrenceDescription> found = Matcher.create(copy, new AtomicBoolean()).setSearchRoot(new TreePath(new TreePath(clazz, method), method.getBody())).setTreeTopSearch().match(p);
            Tree lambdaBody;

            if (found.isEmpty()) {
                lambdaBody = method.getBody();
            } else {
                lambdaBody = found.iterator().next().getVariables().get("$expression").getLeaf();
            }

            LambdaExpressionTree nue = make.LambdaExpression(method.getParameters(), lambdaBody);

            copy.rewrite(tp.getLeaf(), nue);
        }
        
    }

}

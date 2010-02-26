/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.java.hints;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.jackpot.code.spi.Constraint;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(id="StringBuilderAppend", category="performance")
public class StringBuilderAppend {

    @TriggerPattern(value="$build.append($app)",
                    constraints={
                        @Constraint(variable="$build", type="java.lang.StringBuilder"),
                        @Constraint(variable="$app", type="java.lang.String")
                    })
    public static ErrorDescription builder(HintContext ctx) {
        return hint(ctx, "StringBuilder");
    }

    @TriggerPattern(value="$build.append($app)",
                    constraints={
                        @Constraint(variable="$build", type="java.lang.StringBuffer"),
                        @Constraint(variable="$app", type="java.lang.String")
                    })
    public static ErrorDescription buffer(HintContext ctx) {
        return hint(ctx, "StringBuffer");
    }

    private static ErrorDescription hint(HintContext ctx, String clazzName) {
        CompilationInfo info = ctx.getInfo();
        MethodInvocationTree mit = (MethodInvocationTree) ctx.getPath().getLeaf();
        ExpressionTree param = mit.getArguments().get(0);
        List<List<TreePath>> sorted = Utilities.splitStringConcatenationToElements(info, new TreePath(ctx.getPath(), param));

        if (sorted.size() > 1) {
            String error = NbBundle.getMessage(StringBuilderAppend.class, "ERR_StringBuilderAppend", clazzName);
            return ErrorDescriptionFactory.forTree(ctx, param, error, new FixImpl(info.getSnapshot().getSource(), TreePathHandle.create(ctx.getPath(), info)));
        }

        return null;
    }

    private static final class FixImpl implements Fix {

        private final Source source;
        private final TreePathHandle tph;

        public FixImpl(Source source, TreePathHandle tph) {
            this.source = source;
            this.tph = tph;
        }

        public String getText() {
            return NbBundle.getMessage(StringBuilderAppend.class, "FIX_StringBuilderAppend");
        }

        public ChangeInfo implement() throws Exception {
            ModificationResult.runModificationTask(Collections.singletonList(source), new UserTask() {
                public void run (ResultIterator it) throws Exception {
                    WorkingCopy copy = WorkingCopy.get(it.getParserResult());//XXX: resolve in a correct position

                    if (copy == null) {
                        //XXX: log
                        return ;
                    }
                    
                    copy.toPhase(Phase.RESOLVED);

                    TreePath tp = tph.resolve(copy);

                    if (tp == null) {
                        return ;
                    }

                    MethodInvocationTree mit = (MethodInvocationTree) tp.getLeaf();
                    ExpressionTree param = mit.getArguments().get(0);
                    List<List<TreePath>> sorted = Utilities.splitStringConcatenationToElements(copy, new TreePath(tp, param));
                    ExpressionTree site = ((MemberSelectTree) mit.getMethodSelect()).getExpression();
                    TreeMaker make = copy.getTreeMaker();

                    for (List<TreePath> cluster : sorted) {
                        ExpressionTree arg = (ExpressionTree) cluster.remove(0).getLeaf();

                        while (!cluster.isEmpty()) {
                            arg = make.Binary(Kind.PLUS, arg, (ExpressionTree) cluster.remove(0).getLeaf());
                        }

                        while (arg.getKind() == Kind.PARENTHESIZED) {
                            arg = ((ParenthesizedTree) arg).getExpression();
                        }
                        
                        site = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(site, "append"), Collections.singletonList(arg));
                    }

                    copy.rewrite(mit, site);
                }
            }).commit();

            return null;
        }
        
    }

}

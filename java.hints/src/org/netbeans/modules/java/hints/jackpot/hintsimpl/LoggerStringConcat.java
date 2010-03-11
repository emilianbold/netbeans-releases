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

package org.netbeans.modules.java.hints.jackpot.hintsimpl;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.jackpot.code.spi.Constraint;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPatterns;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(id="org.netbeans.modules.java.hints.jackpot.hintsimpl.LoggerStringConcat", category="logging")
public class LoggerStringConcat {

    @TriggerPattern(value = "$logger.log($level, $message)",
                    constraints = {
                        @Constraint(variable="$logger", type="java.util.logging.Logger"),
                        @Constraint(variable="$level", type="java.util.logging.Level"),
                        @Constraint(variable="$message", type="java.lang.String")
                    })
    public static ErrorDescription hint1(HintContext ctx) {
        return compute(ctx, null);
    }

//    @Hint("org.netbeans.modules.java.hints.jackpot.hintsimpl.LoggerStringConcat")
//    @TriggerPattern(value = "$logger.fine($message)",
//                    constraints = {
//                        @Constraint(variable="$logger", type="java.util.logging.Logger"),
//                        @Constraint(variable="$message", type="java.lang.String")
//                    })
//    public static ErrorDescription hint2(HintContext ctx) {
//        String methodName = ctx.getVariableNames().get("$method");
//
//        if (findConstant(ctx.getInfo(), methodName) == null) {
//            return null;
//        }
//
//        return compute(ctx, methodName);
//    }

    @TriggerPatterns({
        @TriggerPattern(value = "$logger.severe($message)",
                        constraints = {
                            @Constraint(variable="$logger", type="java.util.logging.Logger"),
                            @Constraint(variable="$message", type="java.lang.String")
                        }),
        @TriggerPattern(value = "$logger.warning($message)",
                        constraints = {
                            @Constraint(variable="$logger", type="java.util.logging.Logger"),
                            @Constraint(variable="$message", type="java.lang.String")
                        }),
        @TriggerPattern(value = "$logger.info($message)",
                        constraints = {
                            @Constraint(variable="$logger", type="java.util.logging.Logger"),
                            @Constraint(variable="$message", type="java.lang.String")
                        }),
        @TriggerPattern(value = "$logger.config($message)",
                        constraints = {
                            @Constraint(variable="$logger", type="java.util.logging.Logger"),
                            @Constraint(variable="$message", type="java.lang.String")
                        }),
        @TriggerPattern(value = "$logger.fine($message)",
                        constraints = {
                            @Constraint(variable="$logger", type="java.util.logging.Logger"),
                            @Constraint(variable="$message", type="java.lang.String")
                        }),
        @TriggerPattern(value = "$logger.finer($message)",
                        constraints = {
                            @Constraint(variable="$logger", type="java.util.logging.Logger"),
                            @Constraint(variable="$message", type="java.lang.String")
                        }),
        @TriggerPattern(value = "$logger.finest($message)",
                        constraints = {
                            @Constraint(variable="$logger", type="java.util.logging.Logger"),
                            @Constraint(variable="$message", type="java.lang.String")
                        })
    })
    public static ErrorDescription hint2(HintContext ctx) {
        TreePath inv = ctx.getPath();
        MethodInvocationTree mit = (MethodInvocationTree) inv.getLeaf();
        MemberSelectTree sel = (MemberSelectTree) mit.getMethodSelect();
        String methodName = sel.getIdentifier().toString();

        assert findConstant(ctx.getInfo(), methodName) != null;

        return compute(ctx, methodName);
    }

    private static ErrorDescription compute(HintContext ctx, String methodName) {
        TreePath message = ctx.getVariables().get("$message");
        List<List<TreePath>> sorted = Utilities.splitStringConcatenationToElements(ctx.getInfo(), message);

        if (sorted.size() <= 1) {
            return null;
        }

        FixImpl fix = new FixImpl(NbBundle.getMessage(LoggerStringConcat.class, "MSG_LoggerStringConcat_fix"), methodName, TreePathHandle.create(ctx.getPath(), ctx.getInfo()), TreePathHandle.create(message, ctx.getInfo()));

        return ErrorDescriptionFactory.forTree(ctx, message, NbBundle.getMessage(LoggerStringConcat.class, "MSG_LoggerStringConcat"), fix);
    }

    private static void rewrite(WorkingCopy wc, ExpressionTree level, MethodInvocationTree invocation, TreePath message) {
        List<List<TreePath>> sorted = Utilities.splitStringConcatenationToElements(wc, message);
        StringBuilder workingLiteral = new StringBuilder();
        List<Tree> newMessage = new LinkedList<Tree>();
        List<ExpressionTree> newParams = new LinkedList<ExpressionTree>();
        int variablesCount = 0;
        TreeMaker make = wc.getTreeMaker();

        for (List<TreePath> element : sorted) {
            if (element.size() == 1 && element.get(0).getLeaf().getKind() == Kind.STRING_LITERAL) {
                String literalValue = (String) ((LiteralTree) element.get(0).getLeaf()).getValue();

                literalValue = literalValue.replaceAll("'", "''");
                literalValue = literalValue.replaceAll(Pattern.quote("{"), Matcher.quoteReplacement("'{'"));
                literalValue = literalValue.replaceAll(Pattern.quote("}"), Matcher.quoteReplacement("'}'"));
                workingLiteral.append(literalValue);
            } else {
                if (element.size() == 1 && !Utilities.isConstantString(wc, element.get(0))) {
                    workingLiteral.append("{");
                    workingLiteral.append(Integer.toString(variablesCount++));
                    workingLiteral.append("}");
                    newParams.add((ExpressionTree) element.get(0).getLeaf());
                } else {
                    if (workingLiteral.length() > 0) {
                        newMessage.add(make.Literal(workingLiteral.toString()));
                        workingLiteral.delete(0, workingLiteral.length());
                    }

                    for (TreePath tp : element) {
                        String literalValue = (String) ((LiteralTree) tp.getLeaf()).getValue();

                        if (literalValue.contains("'")) {
                            newMessage.add(make.Literal(literalValue.replaceAll("'", "''")));
                        } else {
                            newMessage.add(tp.getLeaf());
                        }
                    }
                }
            }
        }

        if (workingLiteral.length() > 0) {
            newMessage.add(make.Literal(workingLiteral.toString()));
        }

        ExpressionTree messageFinal = (ExpressionTree) newMessage.remove(0);

        while (!newMessage.isEmpty()) {
            messageFinal = make.Binary(Kind.PLUS, messageFinal, (ExpressionTree) newMessage.remove(0));
        }

        List<ExpressionTree> nueParams = new LinkedList<ExpressionTree>();

        nueParams.add(level);
        nueParams.add(messageFinal);

        if (newParams.size() > 1) {
            nueParams.add(make.NewArray(make.QualIdent(wc.getElements().getTypeElement("java.lang.Object")), Collections.<ExpressionTree>emptyList(), newParams));
        } else {
            nueParams.addAll(newParams);
        }

        MemberSelectTree sel = (MemberSelectTree) invocation.getMethodSelect();
        MemberSelectTree nueSel = make.MemberSelect(sel.getExpression(), "log");
        MethodInvocationTree nue = make.MethodInvocation((List<? extends ExpressionTree>) invocation.getTypeArguments(), nueSel, nueParams);

        wc.rewrite(invocation, nue);
    }

    private static VariableElement findConstant(CompilationInfo info, String logMethodName) {
        logMethodName = logMethodName.toUpperCase();
        
        TypeElement julLevel = info.getElements().getTypeElement("java.util.logging.Level");

        if (julLevel == null) {
            return null;
        }
        
        for (VariableElement el : ElementFilter.fieldsIn(julLevel.getEnclosedElements())) {
            if (el.getSimpleName().contentEquals(logMethodName)) {
                return el;
            }
        }

        return null;
    }

    private static final class FixImpl implements Fix {

        private final String displayName;
        private final String logMethodName; //only if != log
        private final TreePathHandle invocation;
        private final TreePathHandle message;

        public FixImpl(String displayName, String logMethodName, TreePathHandle invocation, TreePathHandle message) {
            this.displayName = displayName;
            this.logMethodName = logMethodName;
            this.invocation = invocation;
            this.message = message;
        }

        public String getText() {
            return displayName;
        }

        public ChangeInfo implement() throws Exception {
            JavaSource.forFileObject(invocation.getFileObject()).runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);
                    TreePath invocation = FixImpl.this.invocation.resolve(parameter);
                    TreePath message    = FixImpl.this.message.resolve(parameter);
                    MethodInvocationTree mit = (MethodInvocationTree) invocation.getLeaf();
                    ExpressionTree level = null;

                    if (logMethodName != null) {
                        String logMethodNameUpper = logMethodName.toUpperCase();
                        VariableElement c = findConstant(parameter, logMethodNameUpper);

                        level = parameter.getTreeMaker().QualIdent(c);
                    } else {
                        level = mit.getArguments().get(0);
                    }

                    rewrite(parameter, level, mit, message);
                }
            }).commit();

            return null;
        }

    }

}

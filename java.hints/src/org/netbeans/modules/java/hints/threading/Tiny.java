/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.threading;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.jackpot.code.spi.Constraint;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPatterns;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class Tiny {

    @Hint(category="thread")
    @TriggerPatterns({
        @TriggerPattern(value="$cond.notify()",
                        constraints=@Constraint(variable="$cond", type="java.util.concurrent.locks.Condition")),
        @TriggerPattern(value="$cond.notifyAll()",
                        constraints=@Constraint(variable="$cond", type="java.util.concurrent.locks.Condition"))
    })
    public static ErrorDescription notifyOnCondition(HintContext ctx) {
        String method = methodName((MethodInvocationTree) ctx.getPath().getLeaf());
        String toName = method.endsWith("All") ? "signalAll" : "signal";

        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_NotifyOnConditionFix", toName);
        Fix f = JavaFix.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$cond." + toName + "()");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_NotifyOnCondition", method);

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, f);
    }

    @Hint(category="thread")
    @TriggerPatterns({
        @TriggerPattern(value="$cond.wait()",
                        constraints=@Constraint(variable="$cond", type="java.util.concurrent.locks.Condition")),
        @TriggerPattern(value="$cond.wait($timeout)",
                        constraints={
                             @Constraint(variable="$cond", type="java.util.concurrent.locks.Condition"),
                             @Constraint(variable="$timeout", type="long")
                        }),
        @TriggerPattern(value="$cond.wait($timeout, $nanos)",
                        constraints={
                             @Constraint(variable="$cond", type="java.util.concurrent.locks.Condition"),
                             @Constraint(variable="$timeout", type="long"),
                             @Constraint(variable="$nanos", type="int")
                        })
    })
    public static ErrorDescription waitOnCondition(HintContext ctx) {
        //TODO: =>await?
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_WaitOnCondition");
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(category="thread")
    @TriggerPattern(value="$thread.run()",
                    constraints=@Constraint(variable="$thread", type="java.lang.Thread"))
    public static ErrorDescription threadRun(HintContext ctx) {
        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_ThreadRun");
        Fix f = JavaFix.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$thread.start()");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_ThreadRun");

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, f);
    }

    @Hint(category="thread")
    @TriggerPattern(value="$thread.start()",
                    constraints=@Constraint(variable="$thread", type="java.lang.Thread"))
    public static ErrorDescription threadStartInConstructor(HintContext ctx) {
        //TODO: instance initializers?
        if (!Utilities.isInConstructor(ctx)) {
            return null;
        }

        String displayName = NbBundle.getMessage(Tiny.class, "ERR_ThreadStartInConstructor");
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(category="thread")
    @TriggerPattern(value="$thread.yield()",
                    constraints=@Constraint(variable="$thread", type="java.lang.Thread"))
    public static ErrorDescription threadYield(HintContext ctx) {
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_ThreadYield");
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(category="thread")
    @TriggerPatterns({
        @TriggerPattern(value="$thread.stop()",
                        constraints=@Constraint(variable="$thread", type="java.lang.Thread")),
        @TriggerPattern(value="$thread.suspend()",
                        constraints=@Constraint(variable="$thread", type="java.lang.Thread")),
        @TriggerPattern(value="$thread.resume()",
                        constraints=@Constraint(variable="$thread", type="java.lang.Thread"))
    })
    public static ErrorDescription threadSuspend(HintContext ctx) {
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_ThreadSuspend", methodName((MethodInvocationTree) ctx.getPath().getLeaf()));
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(category="thread")
    @TriggerPattern(value="synchronized ($lock) $block",
                    constraints=@Constraint(variable="$lock", type="java.lang.Object"))
    public static ErrorDescription nestedSynchronized(HintContext ctx) {
        class Found extends Error {
            @Override public synchronized Throwable fillInStackTrace() {
                return this;
            }
        }

        TreePath up = ctx.getPath().getParentPath();

        while (up != null && up.getLeaf().getKind() != Kind.METHOD && up.getLeaf().getKind() != Kind.CLASS) {
            if (up.getLeaf().getKind() == Kind.SYNCHRONIZED) {
                return null;
            }

            up = up.getParentPath();
        }

        boolean report = false;

        if (up != null && up.getLeaf().getKind() == Kind.METHOD) {
            MethodTree mt = (MethodTree) up.getLeaf();

            report = mt.getModifiers().getFlags().contains(Modifier.SYNCHRONIZED);
        }

        if (!report) {
            try {
                new TreeScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree node, Void p) {
                        return null;
                    }
                    @Override
                    public Void visitSynchronized(SynchronizedTree node, Void p) {
                        throw new Found();
                    }
                }.scan(ctx.getVariables().get("$block").getLeaf(), null);
                return null;
            } catch (Found f) {
                //OK:
            }
        }
        
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_NestedSynchronized");
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(category="thread")
    @TriggerPattern(value="synchronized ($lock) {}",
                    constraints=@Constraint(variable="$lock", type="java.lang.Object"))
    public static ErrorDescription emptySynchronized(HintContext ctx) {
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_EmptySynchronized");
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(category="thread")
    @TriggerPattern(value="synchronized ($lock) {$statements$;}",
                    constraints=@Constraint(variable="$lock", type="java.util.concurrent.locks.Lock"))
    public static ErrorDescription synchronizedOnLock(HintContext ctx) {
        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_SynchronizedOnLock");
        Fix f = JavaFix.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$lock.lock(); try {$statements$;} finally {$lock.unlock();}");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_SynchronizedOnLock");

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, f);
    }

    @Hint(category="thread")
    @TriggerPatterns({
//        @TriggerPattern(value="volatile $mods$ $type[] $name;"),
//        @TriggerPattern(value="volatile $mods$ $type[] $name = $init;")
        @TriggerPattern(value="$mods$ $type[] $name;"),
        @TriggerPattern(value="$mods$ $type[] $name = $init;")
    })
    public static ErrorDescription volatileArray(HintContext ctx) {
        Element el = ctx.getInfo().getTrees().getElement(ctx.getPath());

        if (el == null || el.getKind() != ElementKind.FIELD || !el.getModifiers().contains(Modifier.VOLATILE)) {
            return null;
        }

        String displayName = NbBundle.getMessage(Tiny.class, "ERR_VolatileArrayField");
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    private static String methodName(MethodInvocationTree mit) {
        ExpressionTree select = mit.getMethodSelect();

        switch (select.getKind()) {
            case IDENTIFIER: return ((IdentifierTree) select).getName().toString();
            case MEMBER_SELECT: return ((MemberSelectTree) select).getIdentifier().toString();
            default: throw new UnsupportedOperationException(select.getKind().toString());
        }
    }
}

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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
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

    @Hint(category="thread", suppressWarnings="NotifyCalledOnCondition")
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

    @Hint(category="thread", suppressWarnings="WaitCalledOnCondition")
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

    @Hint(category="thread", suppressWarnings="CallToThreadRun")
    @TriggerPattern(value="$thread.run()",
                    constraints=@Constraint(variable="$thread", type="java.lang.Thread"))
    public static ErrorDescription threadRun(HintContext ctx) {
        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_ThreadRun");
        Fix f = JavaFix.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$thread.start()");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_ThreadRun");

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, f);
    }

    @Hint(category="thread", suppressWarnings="CallToThreadStartDuringObjectConstruction")
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

    @Hint(category="thread", suppressWarnings="CallToThreadYield")
    @TriggerPattern(value="java.lang.Thread.yield()")
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

    @Hint(category="thread", suppressWarnings="NestedSynchronizedStatement")
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

    @Hint(category="thread", suppressWarnings="EmptySynchronizedStatement")
    @TriggerPattern(value="synchronized ($lock) {}",
                    constraints=@Constraint(variable="$lock", type="java.lang.Object"))
    public static ErrorDescription emptySynchronized(HintContext ctx) {
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_EmptySynchronized");
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(category="thread", suppressWarnings="SynchroniziationOnLockObject")
    @TriggerPattern(value="synchronized ($lock) {$statements$;}",
                    constraints=@Constraint(variable="$lock", type="java.util.concurrent.locks.Lock"))
    public static ErrorDescription synchronizedOnLock(HintContext ctx) {
        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_SynchronizedOnLock");
        Fix f = JavaFix.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$lock.lock(); try {$statements$;} finally {$lock.unlock();}");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_SynchronizedOnLock");

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, f);
    }

    @Hint(category="thread", suppressWarnings="VolatileArrayField")
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

    @Hint(category="thread", suppressWarnings="LockAcquiredButNotSafelyReleased")
    @TriggerPattern(value="$lock.lock(); $statements$; $lock.unlock();",
                    constraints=@Constraint(variable="$lock", type="java.util.concurrent.locks.Lock"))
    public static ErrorDescription unlockOutsideTryFinally(HintContext ctx) {
        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_UnlockOutsideTryFinally");
        Fix f = JavaFix.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$lock.lock(); try {$statements$;} finally {$lock.unlock();}");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_UnlockOutsideTryFinally");

        //XXX:
        Tree mark;
        Tree matched = ctx.getPath().getLeaf();

        if (matched.getKind() == Kind.BLOCK) {
            List<? extends StatementTree> s = ((BlockTree) matched).getStatements();
            int count = ctx.getMultiVariables().get("$$1$").size();

            mark = s.get(count);
        } else {
            mark = matched;
        }

        return ErrorDescriptionFactory.forName(ctx, mark, displayName, f);
    }

    @Hint(category="thread", suppressWarnings="WaitWhileNotSynced")
    @TriggerPatterns({
        @TriggerPattern(value="$site.wait()",
                        constraints=@Constraint(variable="$site", type="java.lang.Object")),
        @TriggerPattern(value="$site.wait($timeout)",
                        constraints={
                             @Constraint(variable="$site", type="java.lang.Object"),
                             @Constraint(variable="$timeout", type="long")
                        }),
        @TriggerPattern(value="$site.wait($timeout, $nanos)",
                        constraints={
                             @Constraint(variable="$site", type="java.lang.Object"),
                             @Constraint(variable="$timeout", type="long"),
                             @Constraint(variable="$nanos", type="int")
                        })
    })
    public static ErrorDescription unsyncWait(HintContext ctx) {
        return unsyncHint(ctx, "ERR_UnsyncedWait");
    }
    
    @Hint(category="thread", suppressWarnings="NotifyWhileNotSynced")
    @TriggerPatterns({
        @TriggerPattern(value="$site.notify()",
                        constraints=@Constraint(variable="$site", type="java.lang.Object")),
        @TriggerPattern(value="$site.notifyAll()",
                        constraints=@Constraint(variable="$site", type="java.lang.Object"))
    })
    public static ErrorDescription unsyncNotify(HintContext ctx) {
        return unsyncHint(ctx, "ERR_UnsyncedNotify");
    }

    private static final Set<ElementKind> VARIABLES = EnumSet.of(ElementKind.ENUM_CONSTANT, ElementKind.EXCEPTION_PARAMETER, ElementKind.FIELD, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);

    private static ErrorDescription unsyncHint(HintContext ctx, String key) {
        VariableElement syncedOn;
        TreePath site = ctx.getVariables().get("$site");

        if (site != null) {
            Element siteEl = ctx.getInfo().getTrees().getElement(site);

            if (siteEl == null || !VARIABLES.contains(siteEl.getKind())) {
                return null;
            }

            syncedOn = (VariableElement) siteEl;
        } else {
            syncedOn = attributeThis(ctx.getInfo(), ctx.getPath());
        }

        TreePath inspect = ctx.getPath();

        while (inspect != null && inspect.getLeaf().getKind() != Kind.CLASS) {
            if (inspect.getLeaf().getKind() == Kind.SYNCHRONIZED) {
                Element current = ctx.getInfo().getTrees().getElement(new TreePath(inspect, ((SynchronizedTree) inspect.getLeaf()).getExpression()));

                if (syncedOn.equals(current)) {
                    return null;
                }
            }

            if (inspect.getLeaf().getKind() == Kind.METHOD) {
                if (((MethodTree) inspect.getLeaf()).getModifiers().getFlags().contains(Modifier.SYNCHRONIZED)) {
                    if (syncedOn.equals(attributeThis(ctx.getInfo(), inspect))) {
                        return null;
                    }
                }

                break;
            }

            inspect = inspect.getParentPath();
        }

        String displayName = NbBundle.getMessage(Tiny.class, key);

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }
    
    @Hint(category="thread", suppressWarnings="")
    @TriggerPatterns({
        @TriggerPattern(value="java.lang.Thread.sleep($to)",
                        constraints=@Constraint(variable="$to", type="long")),
        @TriggerPattern(value="java.lang.Thread.sleep($to, $nanos)",
                        constraints=@Constraint(variable="$to", type="long"),
                        constraints=@Constraint(variable="$nanos", type="int"))
    })
    public static ErrorDescription sleepInSync(HintContext ctx) {
        if (!isSynced(ctx, ctx.getPath())) {
            return null;
        }

        String displayName = NbBundle.getMessage(Tiny.class, "ERR_SleepInSync");

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(category="thread", suppressWarnings="")
    @TriggerPatterns({
        @TriggerPattern(value="java.lang.Thread.sleep($to)",
                        constraints=@Constraint(variable="$to", type="long")),
        @TriggerPattern(value="java.lang.Thread.sleep($to, $nanos)",
                        constraints=@Constraint(variable="$to", type="long"),
                        constraints=@Constraint(variable="$nanos", type="int"))
    })
    public static ErrorDescription sleepInLoop(HintContext ctx) {
        if (findLoop(ctx.getPath()) == null) {
            return null;
        }

        String displayName = NbBundle.getMessage(Tiny.class, "ERR_SleepInLoop");

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

    private static VariableElement attributeThis(CompilationInfo info, TreePath tp) {
        //XXX:
        Scope scope = info.getTrees().getScope(tp);
        ExpressionTree thisTree = info.getTreeUtilities().parseExpression("this", new SourcePositions[1]);

        info.getTreeUtilities().attributeTree(thisTree, scope);

        return (VariableElement) info.getTrees().getElement(new TreePath(tp, thisTree));
    }

    private static boolean isSynced(HintContext ctx, TreePath inspect) {
        while (inspect != null && inspect.getLeaf().getKind() != Kind.CLASS) {
            if (inspect.getLeaf().getKind() == Kind.SYNCHRONIZED) {
                return true;
            }

            if (inspect.getLeaf().getKind() == Kind.METHOD) {
                if (((MethodTree) inspect.getLeaf()).getModifiers().getFlags().contains(Modifier.SYNCHRONIZED)) {
                    return true;
                }

                break;
            }

            inspect = inspect.getParentPath();
        }

        return false;
    }

    private static final Set<Kind> LOOP_KINDS = EnumSet.of(Kind.DO_WHILE_LOOP, Kind.ENHANCED_FOR_LOOP, Kind.FOR_LOOP, Kind.WHILE_LOOP);

    private static TreePath findLoop(TreePath inspect) {
        while (inspect != null && inspect.getLeaf().getKind() != Kind.CLASS && !LOOP_KINDS.contains(inspect.getLeaf().getKind())) {
            inspect = inspect.getParentPath();
        }

        return LOOP_KINDS.contains(inspect.getLeaf().getKind()) ? inspect : null;
    }
}

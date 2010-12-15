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

import com.sun.source.tree.CatchTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.introduce.CopyFinder;
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
@Hint(category="rules15")
public class JoinCatches {

    @TriggerPatterns({
        @TriggerPattern(
                "try ($resources$) { $trystmts$; } catch ($var1) { $catchstmts1$; } catch $inter$ catch ($var2) { $catchstmts2$; } catch $more$"
        ),
        @TriggerPattern(
                "try ($resources$) { $trystmts$; } catch (final $var1) { $catchstmts1$; } catch $inter$ catch (final $var2) { $catchstmts2$; } catch $more$"
        ),
        @TriggerPattern(
                "try ($resources$) { $trystmts$; } catch ($var1) { $catchstmts1$; } catch $inter$ catch ($var2) { $catchstmts2$; } catch $more$ finally {$finstmts$;}"
        ),
        @TriggerPattern(
                "try ($resources$) { $trystmts$; } catch (final $var1) { $catchstmts1$; } catch $inter$ catch (final $var2) { $catchstmts2$; } catch $more$ finally {$finstmts$;}"
        )
    })
    public static ErrorDescription hint(HintContext ctx) {
        if (ctx.getInfo().getSourceVersion().compareTo(SourceVersion.RELEASE_7) < 0) return null;
        
        TryTree tt = (TryTree) ctx.getPath().getLeaf();

        List<? extends CatchTree> catches = new ArrayList<CatchTree>(tt.getCatches());

        if (catches.size() <= 1) return null;

        for (int i = 0; i < catches.size(); i++) {
            CatchTree toTest = catches.get(0);
            TreePath toTestPath = new TreePath(ctx.getPath(), toTest);
            VariableElement excVar = (VariableElement) ctx.getInfo().getTrees().getElement(new TreePath(toTestPath, toTest.getParameter()));
            List<Integer> duplicates = new LinkedList<Integer>();

            for (int j = i + 1; j < catches.size(); j++) {
                if (CopyFinder.isDuplicate(ctx.getInfo(), new TreePath(toTestPath, toTest.getBlock()), new TreePath(new TreePath(ctx.getPath(), catches.get(j)), ((CatchTree)catches.get(j)).getBlock()), true, ctx, false, Collections.singleton(excVar), new AtomicBoolean())) {
                    TreePath catchPath = new TreePath(ctx.getPath(), catches.get(j));
                    TreePath var = new TreePath(catchPath, ((CatchTree)catches.get(j)).getParameter());
                    Collection<TreePath> statements = new ArrayList<TreePath>();
                    TreePath blockPath = new TreePath(catchPath, ((CatchTree)catches.get(j)).getBlock());

                    for (StatementTree t : ((CatchTree)catches.get(j)).getBlock().getStatements()) {
                        statements.add(new TreePath(blockPath, t));
                    }

                    if (!UseSpecificCatch.assignsTo(ctx, var, statements)) {
                        duplicates.add(j);
                    }
                }
            }

            if (!duplicates.isEmpty()) {
                String displayName = NbBundle.getMessage(JoinCatches.class, "ERR_JoinCatches");

                return ErrorDescriptionFactory.forName(ctx, toTest.getParameter().getType(), displayName, JavaFix.toEditorFix(new FixImpl(ctx.getInfo(), ctx.getPath(), i, duplicates)));
            }
        }

        return null;
    }

    private static final class FixImpl extends JavaFix {

        private final int first;
        private final List<Integer> duplicates;
        
        public FixImpl(CompilationInfo info, TreePath tryStatement, int first, List<Integer> duplicates) {
            super(info, tryStatement);
            this.first = first;
            this.duplicates = duplicates;
        }

        @Override
        protected String getText() {
            return NbBundle.getMessage(JoinCatches.class, "FIX_JoinCatches");
        }

        @Override
        protected void performRewrite(WorkingCopy wc, TreePath tp, UpgradeUICallback callback) {
            List<Tree> disjointTypes = new LinkedList<Tree>();
            TryTree tt = (TryTree) tp.getLeaf();

            disjointTypes.add(tt.getCatches().get(first).getParameter().getType());

            for (Integer d : duplicates) {
                disjointTypes.add(tt.getCatches().get((int) d).getParameter().getType());
            }

            List<CatchTree> newCatches = new LinkedList<CatchTree>();
            int c = 0;

            for (CatchTree ct : tt.getCatches()) {
                if (c == first) {
                    wc.rewrite(ct.getParameter().getType(), wc.getTreeMaker().DisjunctiveType(disjointTypes));
                }
                
                if (duplicates.contains(c++)) continue;

                newCatches.add(ct);
            }

            TryTree nue = wc.getTreeMaker().Try(tt.getResources(), tt.getBlock(), newCatches, tt.getFinallyBlock());

            wc.rewrite(tt, nue);
        }

    }
}

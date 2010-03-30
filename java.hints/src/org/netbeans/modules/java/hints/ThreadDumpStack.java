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

package org.netbeans.modules.java.hints;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Jancura
 */
@Hint(category="code_maturity", suppressWarnings="CallToPrintStackTrace")
public class ThreadDumpStack {

    @TriggerPattern (value="Thread.dumpStack ()")
    public static ErrorDescription checkThreadDumpStack (HintContext ctx) {
        TreePath treePath = ctx.getPath ();
        CompilationInfo compilationInfo = ctx.getInfo ();
        return ErrorDescriptionFactory.forName (
            ctx,
            treePath,
            NbBundle.getMessage (ThreadDumpStack.class, "MSG_ThreadDumpStack"),
            new FixImpl (
                NbBundle.getMessage (
                    LoggerNotStaticFinal.class,
                    "MSG_ThreadDumpStack_fix"
                ),
                TreePathHandle.create (treePath, compilationInfo)
            )
        );
    }

    private static final class FixImpl implements Fix {

        private final String    text;
        private final TreePathHandle
                                treePathHandle;

        public FixImpl (
            String              text,
            TreePathHandle      loggerFieldHandle
        ) {
            this.text = text;
            this.treePathHandle = loggerFieldHandle;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public ChangeInfo implement () throws Exception {
            JavaSource.forFileObject (treePathHandle.getFileObject ()).runModificationTask (new Task<WorkingCopy> () {

                @Override
                public void run (WorkingCopy wc) throws Exception {
                    wc.toPhase (Phase.RESOLVED);
                    TreePath tp = treePathHandle.resolve (wc);
                    if (tp == null) return;
                    Tree expressionStatementTree = tp.getParentPath ().getLeaf ();
                    Tree parent2 = tp.getParentPath ().getParentPath ().getLeaf ();
                    if (!(parent2 instanceof BlockTree)) return;
                    BlockTree blockTree = (BlockTree) parent2;
                    List<? extends StatementTree> statements = blockTree.getStatements ();
                    List<StatementTree> newStatements = new ArrayList<StatementTree> ();
                    for (StatementTree statement : statements)
                        if (statement != expressionStatementTree)
                            newStatements.add (statement);
                    BlockTree newBlockTree = wc.getTreeMaker ().Block (newStatements, true);
                    wc.rewrite (blockTree, newBlockTree);
                }
            }).commit ();
            return null;
        }
    } // End of FixImpl class
}

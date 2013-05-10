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

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@Messages({
    "DN_ExtraCatch=Extra catch clauses",
    "FIX_RemoveCatch=Remove catch clause"
})
public class ExtraCatch implements ErrorRule<Void> {
    
    private static final Set<String> CODES = new HashSet<>(Arrays.asList(
        "compiler.err.except.already.caught",
        "compiler.err.except.never.thrown.in.try"
    ));

    @Override
    public Set<String> getCodes() {
        return CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        return Arrays.asList(new RemoveCatch(compilationInfo, treePath).toEditorFix());
    }

    @Override
    public String getId() {
        return ExtraCatch.class.getName();
    }

    @Override
    public String getDisplayName() {
        return Bundle.DN_ExtraCatch();
    }

    @Override
    public void cancel() {
    }
    
    private static final class RemoveCatch extends JavaFix {

        public RemoveCatch(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return Bundle.FIX_RemoveCatch();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            CatchTree toRemove = (CatchTree) ctx.getPath().getLeaf();
            TryTree parent = (TryTree) ctx.getPath().getParentPath().getLeaf();
            TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
            
            if (parent.getResources().isEmpty() && parent.getCatches().size() == 1) {
                Tree parentParent = ctx.getPath().getParentPath().getParentPath().getLeaf();
                
                switch (parentParent.getKind()) {
                    case BLOCK: {
                        BlockTree parentBlock = (BlockTree) parentParent;
                        List<StatementTree> statements = new ArrayList<>(parentBlock.getStatements());
                        statements.addAll(statements.indexOf(parent), parent.getBlock().getStatements());
                        statements.remove(parent);
                        ctx.getWorkingCopy().rewrite(parentParent, make.Block(statements, parentBlock.isStatic()));
                        return ;
                    }
                    case CASE: {
                        CaseTree parentCase = (CaseTree) parentParent;
                        List<StatementTree> statements = new ArrayList<>(parentCase.getStatements());
                        statements.addAll(statements.indexOf(parent), parent.getBlock().getStatements());
                        statements.remove(parent);
                        ctx.getWorkingCopy().rewrite(parentParent, make.Case(parentCase.getExpression(), statements));
                        return ;
                    }
                }
            }
            
            ctx.getWorkingCopy().rewrite(parent, make.removeTryCatch(parent, toRemove));
        }
        
    }
    
}

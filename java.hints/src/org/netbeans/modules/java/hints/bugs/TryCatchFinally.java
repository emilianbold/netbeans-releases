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
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.hints.bugs.Bundle.*;

/**
 * Contains hints for try-catch-finally blocks.
 * @author
 * sdedic
 */
@NbBundle.Messages({
    "# {0} - the rogue statement",
    "TEXT_returnBreakContinueInFinallyBlock=The ''{0}'' statement in the ''finally'' block discards unhandled exceptions",
    "TEXT_throwsInFinallyBlock=The 'throw' statement in 'finally' block may hide the original exception"
})
public class TryCatchFinally {
    private static final Logger LOG = Logger.getLogger(TryCatchFinally.class.getName());
    
    @Hint(category = "bugs",
          displayName = "#DN_TryCatchFinally_finallyThrowsException", // NOI18N
          description = "#DESC_TryCatchFinally_finallyThrowsException", // NOI18N
          suppressWarnings={"ThrowFromFinallyBlock"}, 
          options= Hint.Options.QUERY
    )
    @TriggerPattern("try { $smts$; } catch $catches$ finally { $handler$; }") // NOI18N
    public static List<ErrorDescription> finallyThrowsException(HintContext ctx) {
        List<Tree>  trees = new ArrayList<Tree>(3);
        ExitsFromBranches efab = new ExitsFromBranches(ctx.getInfo(), true);
        Collection<? extends TreePath> paths = ctx.getMultiVariables().get("$handler$"); // NOI18N
        
        for (TreePath tp : paths) {
            efab.scan(tp, trees);
        }
        if (trees.isEmpty()) {
            return null;
        }
        List<ErrorDescription> errs = new ArrayList<ErrorDescription>(trees.size());
        for (Tree stmt : trees) {
            errs.add(ErrorDescriptionFactory.forTree(ctx, stmt, TEXT_throwsInFinallyBlock()));
        }
        return errs;
    }
    
    @Hint(category = "bugs",
          displayName = "#DN_TryCatchFinally_finallyDiscardsException", // NOI18N
          description = "#DESC_TryCatchFinally_finallyDiscardsException", // NOI18N
          suppressWarnings={"FinallyDiscardsException", "", "ReturnFromFinallyBlock", "ContinueOrBreakFromFinallyBlock"}, 
          options= Hint.Options.QUERY
    )
    @TriggerPattern("try { $smts$; } catch $catches$ finally { $handler$; }") // NOI18N
    public static List<ErrorDescription> finallyDiscardsException(HintContext ctx) {
        List<Tree>  trees = new ArrayList<Tree>(3);
        ExitsFromBranches efab = new ExitsFromBranches(ctx.getInfo());
        Collection<? extends TreePath> paths = ctx.getMultiVariables().get("$handler$"); // NOI18N
        
        for (TreePath tp : paths) {
            efab.scan(tp, trees);
        }
        if (trees.isEmpty()) {
            return null;
        }
        List<ErrorDescription> errs = new ArrayList<ErrorDescription>(trees.size());
        for (Tree stmt : trees) {
            final String stmtName;
            switch (stmt.getKind()) {
                case CONTINUE:
                    stmtName = "continue"; // NOI18N
                    break;
                case BREAK:
                    stmtName = "break"; // NOI18N
                    break;
                case RETURN:
                    stmtName = "return"; // NOI18N
                    break;
                default:
                    LOG.log(Level.WARNING, "Unexpected statement kind: {0}", stmt.getKind()); // NOI18N
                    continue;
            }
            
            errs.add(ErrorDescriptionFactory.forTree(ctx, stmt, TEXT_returnBreakContinueInFinallyBlock(stmtName)));
        }
        return errs;
    }
    
    private static final class ExitsFromBranches extends TreePathScanner<Void, Collection<Tree>> {
        private final  boolean analyzeThrows;
        private final CompilationInfo info;
        private final Set<Tree> seenTrees = new HashSet<Tree>();
        private final Stack<Set<TypeMirror>> caughtExceptions = new Stack<Set<TypeMirror>>();

        public ExitsFromBranches(CompilationInfo info, boolean analyzeThrows) {
            this.info = info;
            this.analyzeThrows = analyzeThrows;
        }
        
        public ExitsFromBranches(CompilationInfo info) {
            this.info = info;
            this.analyzeThrows = false;
        }

        @Override
        public Void scan(Tree tree, Collection<Tree> trees) {
            seenTrees.add(tree);
            return super.scan(tree, trees);
        }

        /**
         * Note: if the labeled statement is 1st, in efab.scan(), the visit method is called without
         * prior scan(tree, param). This LabeledStatement is actually a target of break+continue, so
         * it must be also added to seenTrees.s
         */
        @Override
        public Void visitLabeledStatement(LabeledStatementTree node, Collection<Tree> p) {
            seenTrees.add(node);
            return super.visitLabeledStatement(node, p);
        }
        
        @Override
        public Void visitIf(IfTree node, Collection<Tree> trees) {
            scan(node.getThenStatement(), trees);
            scan(node.getElseStatement(), trees);
            return null;
        }

        @Override
        public Void visitReturn(ReturnTree node, Collection<Tree> trees) {
            if (!analyzeThrows) {
                trees.add(node);
            }
            return null;
        }

        @Override
        public Void visitBreak(BreakTree node, Collection<Tree> trees) {
            if (!analyzeThrows && !seenTrees.contains(info.getTreeUtilities().getBreakContinueTarget(getCurrentPath()))) {
                trees.add(node);
            }
            return null;
        }

        @Override
        public Void visitContinue(ContinueTree node, Collection<Tree> trees) {
            if (!analyzeThrows && !seenTrees.contains(info.getTreeUtilities().getBreakContinueTarget(getCurrentPath()))) {
                trees.add(node);
            }
            return null;
        }

        @Override
        public Void visitTry(TryTree node, Collection<Tree> trees) {
            Set<TypeMirror> caught = new HashSet<TypeMirror>();

            for (CatchTree ct : node.getCatches()) {
                TypeMirror t = info.getTrees().getTypeMirror(new TreePath(new TreePath(getCurrentPath(), ct), ct.getParameter()));

                if (t != null) {
                    caught.add(t);
                }
            }

            caughtExceptions.push(caught);
            
            try {
                scan(node.getBlock(), trees);
                scan(node.getFinallyBlock(), trees);
            } finally {
                caughtExceptions.pop();
            }
            return null;
        }

        @Override
        public Void visitThrow(ThrowTree node, Collection<Tree> trees) {
            if (!analyzeThrows) {
                return null;
            }
            TypeMirror type = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getExpression()));
            boolean isCaught = false;

            OUTER: for (Set<TypeMirror> caught : caughtExceptions) {
                for (TypeMirror c : caught) {
                    if (info.getTypes().isSubtype(type, c)) {
                        isCaught = true;
                        break OUTER;
                    }
                }
            }

            super.visitThrow(node, trees);
            if (!isCaught) {
                trees.add(node);
            }
            return null;
        }

    }
}

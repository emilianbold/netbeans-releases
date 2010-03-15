/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.swing.JComponent;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav tulach
 */
public class DoubleCheck extends AbstractHint {
    private transient volatile boolean stop;
    
    /** Creates a new instance of AddOverrideAnnotation */
    public DoubleCheck() {
        super( true, true, AbstractHint.HintSeverity.WARNING);
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.SYNCHRONIZED);
    }
    
    public List<ErrorDescription> run(CompilationInfo compilationInfo,
                                      TreePath treePath) {
        stop = false;
        Tree e = treePath.getLeaf();
        if (e == null || e.getKind() != Kind.SYNCHRONIZED) {
            return null;
        }

        SynchronizedTree synch = (SynchronizedTree)e;
        TreePath outer = findOuterIf(compilationInfo, treePath);
        if (outer == null) {
            return null;
        }

        IfTree same = null;
        TreePath block = new TreePath(treePath, synch.getBlock());
        for (StatementTree statement : synch.getBlock().getStatements()) {
            if (sameIfAndValidate(compilationInfo, new TreePath(block, statement), outer)) {
                same = (IfTree)statement;
                break;
            }
            if (stop) {
                return null;
            }
        }
        if (same == null) {
            return null;
        }

        List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(
            TreePathHandle.create(treePath, compilationInfo),
            TreePathHandle.create(outer, compilationInfo),
            compilationInfo.getFileObject()
        ));

        int span = (int)compilationInfo.getTrees().getSourcePositions().getStartPosition(
            compilationInfo.getCompilationUnit(),
            synch
        );

        ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(
            getSeverity().toEditorSeverity(),
            NbBundle.getMessage(DoubleCheck.class, "ERR_DoubleCheck"), // NOI18N
            fixes,
            compilationInfo.getFileObject(),
            span,
            span + "synchronized".length() // NOI18N
        );

        return Collections.singletonList(ed);
    }

    public String getId() {
        return getClass().getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(DoubleCheck.class, "MSG_DoubleCheck"); // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(DoubleCheck.class, "HINT_DoubleCheck"); // NOI18N
    }

    public void cancel() {
        stop = true;
    }
    
    public Preferences getPreferences() {
        return null;
    }
    
    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }    

    private TreePath findOuterIf(CompilationInfo compilationInfo, TreePath treePath) {
        while (!stop) {
            treePath = treePath.getParentPath();
            if (treePath == null) {
                break;
            }
            Tree leaf = treePath.getLeaf();
            
            if (leaf.getKind() == Kind.IF) {
                return treePath;
            }
            
            if (leaf.getKind() == Kind.BLOCK) {
                BlockTree b = (BlockTree)leaf;
                if (b.getStatements().size() == 1) {
                    // ok, empty blocks can be around synchronized(this) 
                    // statements
                    continue;
                }
            }
            
            return null;
        }
        return null;
    }

    private boolean sameIfAndValidate(CompilationInfo info, TreePath statementTP, TreePath secondTP) {
        StatementTree statement = (StatementTree) statementTP.getLeaf();
        
        if (statement.getKind() != Kind.IF) {
            return false;
        }
        
        IfTree first = (IfTree)statement;
        IfTree second = (IfTree) secondTP.getLeaf();
        
        if (first.getElseStatement() != null) {
            return false;
        }
        if (second.getElseStatement() != null) {
            return false;
        }
        
        TreePath varFirst = equalToNull(new TreePath(statementTP, first.getCondition()));
        TreePath varSecond = equalToNull(new TreePath(secondTP, second.getCondition()));
        
        if (varFirst == null || varSecond == null) {
            return false;
        }

        Element firstVariable = info.getTrees().getElement(varFirst);
        Element secondVariable = info.getTrees().getElement(varSecond);
        
        if (firstVariable != null && firstVariable.equals(secondVariable)) {
            return    getSourceLevel(info).compareTo(SOURCE_LEVEL_1_5) < 0
                   || !firstVariable.getModifiers().contains(Modifier.VOLATILE);
        }
        
        return false;
    }
    
    private TreePath equalToNull(TreePath tp) {
        ExpressionTree t = (ExpressionTree) tp.getLeaf();
        if (t.getKind() == Kind.PARENTHESIZED) {
            ParenthesizedTree p = (ParenthesizedTree)t;
            t = p.getExpression();
            tp = new TreePath(tp, t);
        }
        
        if (t.getKind() != Kind.EQUAL_TO) {
            return null;
        }
        BinaryTree bt = (BinaryTree)t;
        if (bt.getLeftOperand().getKind() == Kind.NULL_LITERAL && bt.getRightOperand().getKind() != Kind.NULL_LITERAL) {
            return new TreePath(tp, bt.getRightOperand());
        }
        if (bt.getLeftOperand().getKind() != Kind.NULL_LITERAL && bt.getRightOperand().getKind() == Kind.NULL_LITERAL) {
            return new TreePath(tp, bt.getLeftOperand());
        }
        return null;
    }

    private static SpecificationVersion getSourceLevel(CompilationInfo info) {
        String sl = SourceLevelQuery.getSourceLevel(info.getFileObject());

        if (sl == null) {
            return SOURCE_LEVEL_1_5;
        }
        
        return new SpecificationVersion(sl);
    }

    private static final SpecificationVersion SOURCE_LEVEL_1_5 = new SpecificationVersion("1.5");
    
    private static final class FixImpl implements Fix, Task<WorkingCopy> {
        private TreePathHandle synchHandle;
        private TreePathHandle ifHandle;
        private FileObject file;

        public FixImpl(TreePathHandle synchHandle, TreePathHandle ifHandle, FileObject file) {
            this.synchHandle = synchHandle;
            this.ifHandle = ifHandle;
            this.file = file;
        }
        
        
        public String getText() {
            return NbBundle.getMessage(DoubleCheck.class, "FIX_DoubleCheck"); // NOI18N
        }
        
        public ChangeInfo implement() throws IOException {
            ModificationResult result = JavaSource.forFileObject(file).runModificationTask(this);
            result.commit();
            return null;
        }
        
        @Override public String toString() {
            return "FixDoubleCheck"; // NOI18N
        }


        public void run(WorkingCopy wc) throws Exception {
            wc.toPhase(JavaSource.Phase.RESOLVED);
            Tree syncTree = synchHandle.resolve(wc).getLeaf();
            Tree ifTree = ifHandle.resolve(wc).getLeaf();
            wc.rewrite(ifTree, syncTree);
        }
    }
    
}

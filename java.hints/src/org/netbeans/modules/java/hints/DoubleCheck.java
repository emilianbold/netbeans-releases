/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.semantic.Utilities;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav tulach
 */
public class DoubleCheck extends AbstractHint {
    
    /** Creates a new instance of AddOverrideAnnotation */
    public DoubleCheck() {
        super( true, true, AbstractHint.HintSeverity.WARNING);
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.SYNCHRONIZED);
    }
    
    public List<ErrorDescription> run(CompilationInfo compilationInfo,
                                      TreePath treePath) {
        try {
            Document doc = compilationInfo.getDocument();
            
            if (doc == null) {
                return null;
            }
            
            Tree e = treePath.getLeaf();
            if (e == null || e.getKind() != Kind.SYNCHRONIZED) {
                return null;
            }
            
            SynchronizedTree synch = (SynchronizedTree)e;
            IfTree outer = findOuterIf(compilationInfo, treePath);
            if (outer == null) {
                return null;
            }
            
            IfTree same = null;
            for (StatementTree statement : synch.getBlock().getStatements()) {
                if (sameIf(statement, outer)) {
                    same = (IfTree)statement;
                    break;
                }
            }
            if (same == null) {
                return null;
            }
            
            TreePath outerPath = compilationInfo.getTrees().getPath(compilationInfo.getCompilationUnit(), outer);
            
            List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(
                TreePathHandle.create(treePath, compilationInfo),
                TreePathHandle.create(outerPath, compilationInfo),
                compilationInfo.getFileObject()
            ));

            int span = (int)compilationInfo.getTrees().getSourcePositions().getStartPosition(
                compilationInfo.getCompilationUnit(),
                synch
            );

            ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(
                getSeverity().toEditorSeverity(),
                NbBundle.getMessage(DoubleCheck.class, "MSG_FixDoubleCheck"), // NOI18N
                fixes,
                doc,
                doc.createPosition(span),
                doc.createPosition(span + "synchronized".length()) // NOI18N
            );

            return Collections.singletonList(ed);
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        return null;
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
        // XXX implement me 
    }
    
    public Preferences getPreferences() {
        return null;
    }
    
    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }    

    private IfTree findOuterIf(CompilationInfo compilationInfo, TreePath treePath) {
        for (;;) {
            treePath = treePath.getParentPath();
            if (treePath == null) {
                break;
            }
            Tree leaf = treePath.getLeaf();
            
            if (leaf.getKind() == Kind.IF) {
                return (IfTree)leaf;
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

    private boolean sameIf(StatementTree statement, IfTree second) {
        if (statement.getKind() != Kind.IF) {
            return false;
        }
        
        IfTree first = (IfTree)statement;
        
        if (first.getElseStatement() != null) {
            return false;
        }
        if (second.getElseStatement() != null) {
            return false;
        }
        
        ExpressionTree varFirst = equalToNull(first.getCondition());
        ExpressionTree varSecond = equalToNull(second.getCondition());
        
        if (varFirst == null || varSecond == null) {
            return false;
        }
        
        if (varFirst.getKind() == Kind.IDENTIFIER && varSecond.getKind() == Kind.IDENTIFIER) {
            IdentifierTree idFirst = (IdentifierTree)varFirst;
            IdentifierTree idSecond = (IdentifierTree)varSecond;
            
            return idFirst.getName().equals(idSecond.getName());
        }
        
        return false;
    }
    
    private ExpressionTree equalToNull(ExpressionTree t) {
        if (t.getKind() == Kind.PARENTHESIZED) {
            ParenthesizedTree p = (ParenthesizedTree)t;
            t = p.getExpression();
        }
        
        if (t.getKind() != Kind.EQUAL_TO) {
            return null;
        }
        BinaryTree bt = (BinaryTree)t;
        if (bt.getLeftOperand().getKind() == Kind.NULL_LITERAL && bt.getRightOperand().getKind() != Kind.NULL_LITERAL) {
            return bt.getRightOperand();
        }
        if (bt.getLeftOperand().getKind() != Kind.NULL_LITERAL && bt.getRightOperand().getKind() == Kind.NULL_LITERAL) {
            return bt.getLeftOperand();
        }
        return null;
    }
    
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
            return NbBundle.getMessage(DoubleCheck.class, "MSG_DoubleCheck"); // NOI18N
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

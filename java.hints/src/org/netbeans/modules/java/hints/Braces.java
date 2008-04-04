/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 */
public class Braces extends AbstractHint {

    static final EnumSet<JavaTokenId> nonRelevant = EnumSet.<JavaTokenId>of(
            JavaTokenId.LINE_COMMENT, 
            JavaTokenId.BLOCK_COMMENT,
            JavaTokenId.JAVADOC_COMMENT,
            JavaTokenId.WHITESPACE
    );
    
    private static final List<Fix> NO_FIXES = Collections.<Fix>emptyList();
    
    private String BRACES_ID = "Braces_"; // NOI18N
    
    private Tree.Kind treeKind;
    private Set<Tree.Kind> treeKinds;
    
    private Braces( Tree.Kind treeKind ) {
        super( false, true, HintSeverity.WARNING );
        this.treeKind = treeKind;
        if ( treeKind == Tree.Kind.FOR_LOOP ) {
            this.treeKinds = EnumSet.<Tree.Kind>of(treeKind, Tree.Kind.ENHANCED_FOR_LOOP);
        } 
        else {
            this.treeKinds = Collections.<Tree.Kind>singleton(treeKind);
        }
    }

    public static Braces createFor() {
        return new Braces( Tree.Kind.FOR_LOOP );
    }
    
    public static Braces createWhile() {
        return new Braces( Tree.Kind.WHILE_LOOP );
    }
    
    public static Braces createDoWhile() {
        return new Braces( Tree.Kind.DO_WHILE_LOOP );
    }
    
    public static Braces createIf() {
        return new Braces( Tree.Kind.IF );
    }
    
    public Set<Kind> getTreeKinds() {
        return treeKinds;
    }

    public List<ErrorDescription> run(CompilationInfo compilationInfo, TreePath treePath) {
        
        Tree tree = treePath.getLeaf();
        
        ErrorDescription ed = null;
                
        switch( tree.getKind() ) {
            case FOR_LOOP:
                ForLoopTree flt = (ForLoopTree) tree;
                ed = checkStatement(flt.getStatement(), treePath, compilationInfo);
                if ( ed != null ) {                    
                    return Collections.singletonList(ed);
                }
                break;
            case ENHANCED_FOR_LOOP:
                EnhancedForLoopTree eflt = (EnhancedForLoopTree) tree;
                ed = checkStatement( eflt.getStatement(), treePath, compilationInfo );
                if ( ed != null ) {                    
                    return Collections.singletonList(ed);
                }
                break;
            case WHILE_LOOP:
                WhileLoopTree wlt = (WhileLoopTree) tree;
                ed = checkStatement( wlt.getStatement(), treePath, compilationInfo);
                if ( ed != null ) {                    
                    return Collections.singletonList(ed);
                }
                break;
            case DO_WHILE_LOOP:
                DoWhileLoopTree dwlt = (DoWhileLoopTree) tree;
                ed = checkStatement( dwlt.getStatement(), treePath, compilationInfo);
                if ( ed != null ) {                    
                    return Collections.singletonList(ed);  
                }
                break;
            case IF:
                IfTree it = (IfTree)tree;
                List<ErrorDescription> eds = checkifStatements(it.getThenStatement(), it.getElseStatement(), treePath, compilationInfo );
                return eds;
        }
        
        return Collections.<ErrorDescription>emptyList();
    }
        
    public void cancel() {
        
    }

    public String getId() {
        return BRACES_ID + treeKind;
    }
    
    public String getDisplayName() {
        switch( treeKind ) {
            case FOR_LOOP:
                return NbBundle.getMessage(Braces.class, "LBL_Braces_For"); // NOI18N
            case WHILE_LOOP:
                return NbBundle.getMessage(Braces.class, "LBL_Braces_While"); // NOI18N
            case DO_WHILE_LOOP:
                return NbBundle.getMessage(Braces.class, "LBL_Braces_DoWhile"); // NOI18N
            case IF:
                return NbBundle.getMessage(Braces.class, "LBL_Braces_If"); // NOI18N
            default:
                return "No Name"; // NOI18N
        }        
    }

    public String getDescription() {
        switch( treeKind ) {
            case FOR_LOOP:
                return NbBundle.getMessage(Braces.class, "DSC_Braces_For"); // NOI18N
            case WHILE_LOOP:
                return NbBundle.getMessage(Braces.class, "DSC_Braces_While"); // NOI18N
            case DO_WHILE_LOOP:
                return NbBundle.getMessage(Braces.class, "DSC_Braces_DoWhile"); // NOI18N
            case IF:
                return NbBundle.getMessage(Braces.class, "DSC_Braces_If"); // NOI18N
            default:
                return "No Description"; // NOI18N
        }            
    }
    
    // Private methods ---------------------------------------------------------
    
    private ErrorDescription checkStatement( StatementTree statement, TreePath tp, CompilationInfo info )  {
                
        if ( statement != null && 
             statement.getKind() != Tree.Kind.EMPTY_STATEMENT && 
             statement.getKind() != Tree.Kind.BLOCK &&
             statement.getKind() != Tree.Kind.ERRONEOUS &&
             !isErroneousExpression( statement ) ) {
            return ErrorDescriptionFactory.createErrorDescription(
                        getSeverity().toEditorSeverity(), 
                        getDisplayName(), 
                        Collections.<Fix>singletonList(new BracesFix( info.getFileObject(), TreePathHandle.create(tp, info) ) ), 
                        info.getFileObject(),
                        (int)info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), statement ),
                        (int)info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), statement ) );
                    
        }        
        return null;
    }
    
    
    private List<ErrorDescription> checkifStatements( StatementTree thenSt, StatementTree elseSt, TreePath tp, CompilationInfo info )  {
        
        boolean fixThen = false;
        boolean fixElse = false;
        
        if ( thenSt != null && 
             thenSt.getKind() != Tree.Kind.EMPTY_STATEMENT && 
             thenSt.getKind() != Tree.Kind.BLOCK &&
             thenSt.getKind() != Tree.Kind.ERRONEOUS &&
             !isErroneousExpression( thenSt )) {
            fixThen = true;
        }
        
        if ( elseSt != null && 
             elseSt.getKind() != Tree.Kind.EMPTY_STATEMENT && 
             elseSt.getKind() != Tree.Kind.BLOCK &&
             elseSt.getKind() != Tree.Kind.ERRONEOUS &&
             elseSt.getKind() != Tree.Kind.IF &&
             !isErroneousExpression( elseSt )) {
            fixElse = true;
        }
        
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        
        if ( fixThen ) {
            BracesFix bf  = new BracesFix( info.getFileObject(), TreePathHandle.create(tp, info));
            bf.fixThen = fixThen;
            bf.fixElse = fixElse;
            result.add( ErrorDescriptionFactory.createErrorDescription(
                getSeverity().toEditorSeverity(), 
                getDisplayName(), 
                Collections.<Fix>singletonList( bf ), 
                info.getFileObject(),
                (int)info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), thenSt ),
                (int)info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), thenSt ) ) ); 
        }
        
        if ( fixElse ) {
            BracesFix bf  = new BracesFix( info.getFileObject(), TreePathHandle.create(tp, info));
            bf.fixThen = fixThen;
            bf.fixElse = fixElse;
            result.add( ErrorDescriptionFactory.createErrorDescription(
                getSeverity().toEditorSeverity(), 
                getDisplayName(), 
                Collections.<Fix>singletonList( bf ), 
                info.getFileObject(),
                (int)info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), elseSt ),
                (int)info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), elseSt ) ) ); 

        }
                
        return result;
    }
    
    private boolean isErroneousExpression(StatementTree statement) {
        if ( statement instanceof ExpressionStatementTree ) {
            if ( ((ExpressionStatementTree)statement).getExpression().getKind() == Kind.ERRONEOUS ) {
                return true;
            }
        }
        return false;
    }
    
    private static class BracesFix implements Fix, Task<WorkingCopy> {

        
        FileObject file;
        TreePathHandle tph;
        int kind;
        
        boolean fixThen;
        boolean fixElse;
        
        public BracesFix(FileObject file, TreePathHandle tph) {
            this.file = file;
            this.tph = tph;
        }
        
        public String getText() {
            return NbBundle.getMessage(Braces.class, "LBL_Braces_Fix"); // NOI18N
        }

        public ChangeInfo implement() throws IOException {
            JavaSource js = JavaSource.forFileObject(file);
            js.runModificationTask(this).commit();
            return null;
        }

        public void run(WorkingCopy copy) throws Exception {
            copy.toPhase(JavaSource.Phase.PARSED);
            TreePath path = tph.resolve(copy);
            
            if ( path != null ) {
                
                TreeMaker make = copy.getTreeMaker();
                Tree oldTree = path.getLeaf();                 
                
                switch( oldTree.getKind() ) {
                case FOR_LOOP:
                    ForLoopTree oldFor = (ForLoopTree)oldTree;
                    StatementTree oldBlock = oldFor.getStatement();
                    BlockTree newBlock = make.Block(Collections.<StatementTree>singletonList(oldBlock), false);
                    copy.rewrite(oldBlock, newBlock);
                    break;
                case ENHANCED_FOR_LOOP:
                    EnhancedForLoopTree oldEnhancedFor = (EnhancedForLoopTree)oldTree;
                    oldBlock = oldEnhancedFor.getStatement();
                    newBlock = make.Block(Collections.<StatementTree>singletonList(oldBlock), false);                    
                    copy.rewrite(oldBlock, newBlock);
                    break;
                case WHILE_LOOP:
                    WhileLoopTree oldWhile = (WhileLoopTree)oldTree;
                    oldBlock = oldWhile.getStatement();
                    newBlock = make.Block(Collections.<StatementTree>singletonList(oldBlock), false);                    
                    copy.rewrite(oldBlock, newBlock);
                    break;
                case DO_WHILE_LOOP:
                    DoWhileLoopTree oldDoWhile = (DoWhileLoopTree)oldTree;
                    oldBlock = oldDoWhile.getStatement();
                    newBlock = make.Block(Collections.<StatementTree>singletonList(oldBlock), false);                    
                    copy.rewrite(oldBlock, newBlock);
                    break;
                case IF:
                    IfTree oldIf = (IfTree)oldTree;
                    if ( fixThen ) {
                        oldBlock = oldIf.getThenStatement();
                        newBlock = make.Block(Collections.<StatementTree>singletonList(oldBlock), false);
                        copy.rewrite(oldBlock, newBlock);
                    }
                    if ( fixElse ) {
                        oldBlock = oldIf.getElseStatement();
                        newBlock = make.Block(Collections.<StatementTree>singletonList(oldBlock), false);
                        copy.rewrite(oldBlock, newBlock);
                    } 
                    
                }
            }
        }
                
    }

    
    
}

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

import com.sun.source.tree.IfTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
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
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
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
 * @author phrebejk
 */
public class EmptyStatements extends AbstractHint {

    static final EnumSet<JavaTokenId> nonRelevant = EnumSet.<JavaTokenId>of(
            JavaTokenId.LINE_COMMENT, 
            JavaTokenId.BLOCK_COMMENT,
            JavaTokenId.JAVADOC_COMMENT,
            JavaTokenId.WHITESPACE
    );
    
    private static final List<Fix> NO_FIXES = Collections.<Fix>emptyList();
    
    private String EMPTY_STATEMENTS_ID = "EmptyStatements_"; // NOI18N
    
    private Tree.Kind treeKind;
    private Set<Tree.Kind> treeKinds = EnumSet.<Tree.Kind>of(Tree.Kind.EMPTY_STATEMENT);
    private Set<Tree.Kind> NO_KINDS = EnumSet.noneOf(Tree.Kind.class);

    private static EmptyStatements delegate;
    private static EmptyStatements esFor;
    private static EmptyStatements esWhile;
    private static EmptyStatements esDoWhile;
    private static EmptyStatements esIf;
    private static EmptyStatements esBlock;
    
    private EmptyStatements( Tree.Kind treeKind ) {
        super( treeKind == Tree.Kind.IF ? false : true, true, HintSeverity.WARNING );
        this.treeKind = treeKind;                
    }

    public static EmptyStatements createDelegate() {
        return getDelegate();
    }
    
    public static EmptyStatements createFor() {
        EmptyStatements d = getDelegate();
        d.esFor = new EmptyStatements( Tree.Kind.FOR_LOOP );
        return d.esFor;
    }
    
    public static EmptyStatements createWhile() {
        EmptyStatements d = getDelegate();
        d.esWhile = new EmptyStatements( Tree.Kind.WHILE_LOOP );
        return d.esWhile;
    }
    
    public static EmptyStatements createDoWhile() {
        EmptyStatements d = getDelegate();
        d.esDoWhile = new EmptyStatements( Tree.Kind.DO_WHILE_LOOP );
        return d.esDoWhile;
    }
    
    public static EmptyStatements createIf() {
        EmptyStatements d = getDelegate();
        d.esIf = new EmptyStatements( Tree.Kind.IF );
        return d.esIf;
    }
    
    public static EmptyStatements createBlock() {
        EmptyStatements d = getDelegate();
        d.esBlock = new EmptyStatements( Tree.Kind.BLOCK );
        return d.esBlock;
    }
    
    public static synchronized EmptyStatements getDelegate() {
        if ( delegate == null ) {
            delegate = new EmptyStatements(null);
        }
        return delegate;
    }
    
    public Set<Kind> getTreeKinds() {
        return treeKind == null ? treeKinds : NO_KINDS;
    }

    public List<ErrorDescription> run(CompilationInfo compilationInfo, TreePath treePath) {
        
        Tree tree = treePath.getLeaf();
        
        if( tree.getKind() != Tree.Kind.EMPTY_STATEMENT ) {
            return null;
        }
        
        Tree parent = treePath.getParentPath().getLeaf();        
        
        if ( !isEnabled(parent.getKind()) ) {
            return null;
        }
        
        ErrorDescription ed = null;
                
        switch( parent.getKind() ) {
            case FOR_LOOP:
            case ENHANCED_FOR_LOOP:                    
            case WHILE_LOOP:
            case DO_WHILE_LOOP:        
                
                ed = createErrorDescription(treePath.getParentPath(), parent.getKind(), compilationInfo);
                if ( ed != null ) {                    
                    return Collections.singletonList(ed);
                }
                break;         
            case BLOCK:    
                ed = createErrorDescription(treePath, parent.getKind(), compilationInfo);
                if ( ed != null ) {                    
                    return Collections.singletonList(ed);
                }
                break;
            case IF:
                List<ErrorDescription> result = new ArrayList<ErrorDescription>(2);
                IfTree it = (IfTree)parent;
                if ( it.getThenStatement() != null && 
                     it.getThenStatement().getKind() == Tree.Kind.EMPTY_STATEMENT ) {
                    result.add( createErrorDescription(treePath.getParentPath(), parent.getKind(), compilationInfo) );
                }
                if ( it.getElseStatement() != null &&
                     it.getElseStatement().getKind() == Tree.Kind.EMPTY_STATEMENT ) {
                    result.add( createErrorDescription(treePath, parent.getKind(), compilationInfo) ); 
                }
                return result;
        }       
        
        return Collections.<ErrorDescription>emptyList();
    }
        
    public void cancel() {
        // Does nothing
    }

    public String getId() {
        return EMPTY_STATEMENTS_ID + treeKind;
    }
    
    public String getDisplayName() {
        if ( treeKind == null ) {
            return "Empty Statements Delegate"; // NOI18N
        }
        return NbBundle.getMessage(EmptyStatements.class, "LBL_Empty_" + treeKind.toString() ); // NOI18N                
    }

    public String getDescription() {
        if ( treeKind == null ) {
            return "Empty Statements Delegate"; // NOI18N
        }
        return NbBundle.getMessage(EmptyStatements.class, "DSC_Empty_" + treeKind.toString() ); // NOI18N
    }
    
    // Private methods ---------------------------------------------------------
    
    private ErrorDescription createErrorDescription( TreePath tp, Tree.Kind kind, CompilationInfo info )  {
                        
        return ErrorDescriptionFactory.createErrorDescription(
                    getSeverity().toEditorSeverity(), 
                    // getDisplayName(),
                    NbBundle.getMessage(EmptyStatements.class, "LBL_Empty_" + kind.toString()),
                    // Collections.<Fix>singletonList(new EmptyStatementFix( info.getFileObject(), TreePathHandle.create(tp, info) ) ), 
                    NO_FIXES,    
                    info.getFileObject(),
                    (int)info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tp.getLeaf()),
                    (int)info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tp.getLeaf()));
    }
    
    private boolean isEnabled( Tree.Kind kind ) {
        switch( kind ) {
            case FOR_LOOP:
            case ENHANCED_FOR_LOOP:      
                return esFor.isEnabled();
            case WHILE_LOOP:
                return esWhile.isEnabled();
            case DO_WHILE_LOOP:        
                return esDoWhile.isEnabled();
            case BLOCK:    
                return esBlock.isEnabled();
            case IF:
                return esIf.isEnabled();
        }
        return false;
    }
    
    /*
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
             !isErroneousExpression( elseSt )) {
            fixElse = true;
        }
        
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        
        if ( fixThen ) {
            EmptyStatementFix bf  = new EmptyStatementFix( info.getFileObject(), TreePathHandle.create(tp, info));
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
            EmptyStatementFix bf  = new EmptyStatementFix( info.getFileObject(), TreePathHandle.create(tp, info));
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
    */ 
    
    private static class EmptyStatementFix implements Fix, Task<WorkingCopy> {

        
        FileObject file;
        TreePathHandle tph;
        Tree.Kind kind;
        
        boolean fixThen;
        boolean fixElse;
        
        public EmptyStatementFix(FileObject file, TreePathHandle tph, Tree.Kind kind ) {
            this.file = file;
            this.tph = tph;
            this.kind = kind;
        }
        
        public String getText() {
            return NbBundle.getMessage(Braces.class, "LBL_Empty_Fix" + kind.toString()); // NOI18N
        }

        public ChangeInfo implement() {
            JavaSource js = JavaSource.forFileObject(file);
            try {
                js.runModificationTask(this).commit();
            }
            catch( IOException e ) {
                Exceptions.printStackTrace(e);
            }
            return null;
        }

        public void run(WorkingCopy copy) throws Exception {
            /*
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
             */ 
        }
        
                
    }

    
    
}

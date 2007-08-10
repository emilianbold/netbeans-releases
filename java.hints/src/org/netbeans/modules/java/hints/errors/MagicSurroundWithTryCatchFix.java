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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.ErrorManager;


/**
 *
 * @author Jan Lahoda
 */
final class MagicSurroundWithTryCatchFix implements Fix {
    
    private JavaSource js;
    private List<TypeMirrorHandle> thandles;
    private int offset;
    
    public MagicSurroundWithTryCatchFix(JavaSource js, List<TypeMirrorHandle> thandles, int offset) {
        this.js = js;
        this.thandles = thandles;
        this.offset = offset;
    }
    
    public String getText() {
        return "Surround with try-catch";
    }
    
    private static final String[] STREAM_ALIKE_CLASSES = new String[] {
        "java.io.InputStream",
        "java.io.OutputStream",
        "java.io.Reader",
        "java.io.Writer",
    };
    
    private boolean isStreamAlike(CompilationInfo info, TypeMirror type) {
        for (String fqn : STREAM_ALIKE_CLASSES) {
            Element inputStream = info.getElements().getTypeElement(fqn);
            
            if (info.getTypes().isAssignable(type, inputStream.asType()))
                return true;
        }
        
        return false;
    }

    public ChangeInfo implement() {
        try {
            js.runModificationTask(new Task<WorkingCopy>() {

                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(Phase.RESOLVED);
                    TreePath currentPath = wc.getTreeUtilities().pathFor(offset + 1);
                    
                    //find statement:
                    while (currentPath != null && !UncaughtException.STATEMENT_KINDS.contains(currentPath.getLeaf().getKind()))
                        currentPath = currentPath.getParentPath();
                    
                    //TODO: test for final??
                    TreePath statement = currentPath;
                    boolean streamAlike = false;
                    
                    if (statement.getLeaf().getKind() == Kind.VARIABLE) {
                        //special case variable declarations which intializers create streams or readers/writers:
                        Element curType = wc.getTrees().getElement(statement);
                        
                        streamAlike = isStreamAlike(wc, curType.asType());
                    }
                    
                    //find try block containing this statement, if exists:
                    TreePath catchTree = currentPath;
                    
                    while (catchTree != null
                            && catchTree.getLeaf().getKind() != Kind.TRY
                            && catchTree.getLeaf().getKind() != Kind.CLASS
                            && catchTree.getLeaf().getKind() != Kind.CATCH)
                        catchTree = catchTree.getParentPath();
                    
                    if (catchTree.getLeaf().getKind() == Kind.TRY) {
                        //only add catches for uncatched exceptions:
                        new TransformerImpl(wc, thandles, streamAlike, statement).scan(catchTree, null);
                    } else {
                        //find block containing this statement, if exists:
                        TreePath blockTree = currentPath;
                        
                        while (blockTree != null
                                && blockTree.getLeaf().getKind() != Kind.BLOCK)
                            blockTree = blockTree.getParentPath();
                        
                        new TransformerImpl(wc, thandles, streamAlike, statement).scan(blockTree, null);
                    }
                }
                }).commit();
        }  catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        return null;
    }
    
    private final class TransformerImpl extends TreePathScanner<Void, Void> {
        
        private WorkingCopy info;
        private List<TypeMirrorHandle> thandles;
        private boolean streamAlike;
        private TreePath statement;
        private TreeMaker make;
        
        public TransformerImpl(WorkingCopy info, List<TypeMirrorHandle> thandles, boolean streamAlike, TreePath statement) {
            this.info = info;
            this.thandles = thandles;
            this.streamAlike = streamAlike;
            this.statement = statement;
            this.make = info.getTreeMaker();
        }
        
        private StatementTree createExceptionsStatement() {
            TypeElement exceptions = info.getElements().getTypeElement("org.openide.util.Exceptions");
            
            if (exceptions == null) {
                return null;
            }
            
            return make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.QualIdent(exceptions), "printStackTrace"), Arrays.asList(make.Identifier("ex"))));
        }
        
        private StatementTree createLogStatement() {
            if (!GeneratorUtils.supportsOverride(info.getFileObject()))
                return null;
            
            TypeElement logger = info.getElements().getTypeElement("java.util.logging.Logger");
            TypeElement level  = info.getElements().getTypeElement("java.util.logging.Level");
            
            if (logger == null || level == null) {
                return null;
            }
            // find the containing top level class
            ClassTree containingTopLevel = null;
            for (Tree t : statement) {
                if (Kind.CLASS == t.getKind()) {
                    containingTopLevel = (ClassTree) t;
                }
            }
            // take it easy and make it as an identfier or literal
            ExpressionTree arg = containingTopLevel != null ? 
                make.Identifier(containingTopLevel.getSimpleName() + ".class.getName()") :
                make.Literal("global"); // global should never happen
            
            // check that there isn't any Logger class imported
            boolean useFQN = false;
            for (ImportTree dovoz : info.getCompilationUnit().getImports()) {
                MemberSelectTree id = (MemberSelectTree) dovoz.getQualifiedIdentifier();
                if ("Logger".equals(id.getIdentifier()) && !"java.util.logging.Logger".equals(id.toString())) {
                    useFQN = true;
                }
            }
            // finally, make the invocation
            ExpressionTree etExpression = make.MethodInvocation(
                    Collections.<ExpressionTree>emptyList(),
                    make.MemberSelect(
                        useFQN ? make.Identifier(logger.toString()) : make.QualIdent(logger),
                        "getLogger"
                    ),
                    Collections.<ExpressionTree>singletonList(arg)
            );
            ExpressionTree levelExpression = make.MemberSelect(make.QualIdent(level), "SEVERE");
            
            return make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(etExpression, "log"), Arrays.asList(levelExpression, make.Literal(null), make.Identifier("ex"))));
        }
        
        private StatementTree createPrintStackTraceStatement() {
            return make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier("ex"), "printStackTrace"), Collections.<ExpressionTree>emptyList()));
        }
        
        private CatchTree createCatch(TypeMirror type) {
            StatementTree logStatement = createExceptionsStatement();
            
            if (logStatement == null) {
                logStatement = createLogStatement();
            }
            
            if (logStatement == null) {
                logStatement = createPrintStackTraceStatement();
            }
            
            return make.Catch(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "ex", make.Type(type), null), make.Block(Collections.singletonList(logStatement), false));
        }
        
        private List<CatchTree> createCatches() {
            List<CatchTree> catches = new ArrayList<CatchTree>();
            
            for (TypeMirrorHandle th : thandles) {
                catches.add(createCatch(th.resolve(info)));
            }
            
            return catches;
        }
        
        public @Override Void visitTry(TryTree tt, Void p) {
            List<CatchTree> catches = createCatches();
            
            catches.addAll(tt.getCatches());
            
            if (!streamAlike) {
                info.rewrite(tt, make.Try(tt.getBlock(), catches, tt.getFinallyBlock()));
            } else {
                VariableTree originalDeclaration = (VariableTree) statement.getLeaf();
                VariableTree declaration = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), originalDeclaration.getName(), originalDeclaration.getType(), make.Literal(null));
                StatementTree assignment = make.ExpressionStatement(make.Assignment(make.Identifier(originalDeclaration.getName()), originalDeclaration.getInitializer()));
                List<StatementTree> finallyStatements = new ArrayList<StatementTree>(tt.getFinallyBlock() != null ? tt.getFinallyBlock().getStatements() : Collections.<StatementTree>emptyList());
                
                finallyStatements.add(createFinallyCloseBlockStatement(originalDeclaration.getName()));
                
                BlockTree finallyTree = make.Block(finallyStatements, false);
                
                info.rewrite(originalDeclaration, assignment);
                
                TryTree nueTry = make.Try(tt.getBlock(), catches, finallyTree);
                
                TreePath currentBlockCandidate = statement;
                
                while (currentBlockCandidate.getLeaf() != tt) {
                    currentBlockCandidate = currentBlockCandidate.getParentPath();
                }
                
                if (currentBlockCandidate.getLeaf().getKind() == Kind.BLOCK) {
                    BlockTree originalTree = (BlockTree) currentBlockCandidate.getLeaf();
                    List<StatementTree> statements = new ArrayList<StatementTree>(originalTree.getStatements());
                    int index = statements.indexOf(tt);
                    
                    statements.remove(index);
                    statements.add(index, nueTry);
                    statements.add(index, declaration);
                    info.rewrite(originalTree, make.Block(statements, false));
                } else {
                    BlockTree nueBlock = make.Block(Arrays.asList(declaration, nueTry), false);
                    
                    info.rewrite(tt, nueBlock);
                }
            }
            
            return null;
        }
        
        private StatementTree createFinallyCloseBlockStatement(CharSequence name) {
            StatementTree close = make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier(name), "close"), Collections.<ExpressionTree>emptyList()));
            StatementTree tryStatement = make.Try(make.Block(Collections.singletonList(close), false), Collections.singletonList(createCatch(info.getElements().getTypeElement("java.io.IOException").asType())), null);
            
            return tryStatement;
        }
        
        private BlockTree createBlock(StatementTree... trees) {
            List<StatementTree> statements = new LinkedList<StatementTree>();
            
            for (StatementTree t : trees) {
                if (t != null) {
                    statements.add(t);
                }
            }
            
            return make.Block(statements, false);
        }
        
        public @Override Void visitBlock(BlockTree bt, Void p) {
            List<CatchTree> catches = createCatches();
            
            //#89379: if inside a constructor, do not wrap the "super"/"this" call:
            //please note that the "super" or "this" call is supposed to be always
            //in the constructor body
            BlockTree toUse = bt;
            StatementTree toKeep = null;
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            
            if (parent.getKind() == Kind.METHOD && bt.getStatements().size() > 0) {
                MethodTree mt = (MethodTree) parent;
                
                if (mt.getReturnType() == null) {
                    toKeep = bt.getStatements().get(0);
                    toUse = make.Block(bt.getStatements().subList(1, bt.getStatements().size()), false);
                }
            }
            
            if (!streamAlike) {
                info.rewrite(bt, createBlock(toKeep, make.Try(toUse, catches, null)));
            } else {
                VariableTree originalDeclaration = (VariableTree) statement.getLeaf();
                VariableTree declaration = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), originalDeclaration.getName(), originalDeclaration.getType(), make.Identifier("null"));
                StatementTree assignment = make.ExpressionStatement(make.Assignment(make.Identifier(originalDeclaration.getName()), originalDeclaration.getInitializer()));
                BlockTree finallyTree = make.Block(Collections.singletonList(createFinallyCloseBlockStatement(originalDeclaration.getName())), false);
                
                info.rewrite(originalDeclaration, assignment);
                info.rewrite(bt, createBlock(toKeep, declaration, make.Try(toUse, catches, finallyTree)));
            }
            
            return null;
        }
        
    }
    
}

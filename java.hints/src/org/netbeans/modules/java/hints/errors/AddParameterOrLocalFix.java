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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Lahoda
 */
public class AddParameterOrLocalFix implements Fix {
    
    private FileObject file;
    private TypeMirrorHandle type;
    private String name;
    private boolean parameter;
    
    private int /*!!!Position*/ unresolvedVariable;
    
    public AddParameterOrLocalFix(CompilationInfo info,
                                  TypeMirror type, String name,
                                  boolean parameter,
                                  int /*!!!Position*/ unresolvedVariable) {
        this.file = info.getFileObject();
        if (type.getKind() == TypeKind.NULL) {
            type = info.getElements().getTypeElement("java.lang.Object").asType(); // NOI18N
        }
        this.type = TypeMirrorHandle.create(type);
        this.name = name;
        this.parameter = parameter;
        this.unresolvedVariable = unresolvedVariable;
    }

    public String getText() {
        return parameter ? 
            NbBundle.getMessage(AddParameterOrLocalFix.class, "LBL_FIX_Create_Parameter", name) : // NOI18N
            NbBundle.getMessage(AddParameterOrLocalFix.class, "LBL_FIX_Create_Local_Variable", name); // NOI18N
    }

    public ChangeInfo implement() {
        try {
            //use the original cp-info so it is "sure" that the proposedType can be resolved:
            JavaSource js = JavaSource.forFileObject(file);
            
            js.runModificationTask(new Task<WorkingCopy>() {
                
                public void run(final WorkingCopy working) throws IOException {
                    working.toPhase(Phase.RESOLVED);
                    
                    TypeMirror proposedType = type.resolve(working);
                    
                    if (proposedType == null) {
                        ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve proposed type."); // NOI18N
                        return;
                    }
                    
                    TreeMaker make = working.getTreeMaker();
                    TreePath tp = working.getTreeUtilities().pathFor(unresolvedVariable  + 1);
                    
                    assert tp.getLeaf().getKind() == Kind.IDENTIFIER;
                    
                    MethodTree targetTree = findMethod(tp);
                    
                    if (parameter) {
                        if (targetTree == null) {
                            Logger.getLogger("global").log(Level.WARNING, "Add parameter - cannot find the method."); // NOI18N
                        }
                        
                        MethodTree result = make.addMethodParameter(targetTree, make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.Type(proposedType), null));
                        
                        working.rewrite(targetTree, result);
                    } else {
                        resolveLocalVariable(working, tp, make, proposedType);
                    }
                }
            }).commit();
        } catch (IOException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        }
        
        return null;
    }
    
    private void resolveLocalVariable(final WorkingCopy wc, TreePath tp, TreeMaker make, TypeMirror proposedType) {
        final String name = ((IdentifierTree) tp.getLeaf()).getName().toString();
        
        final Element el = wc.getTrees().getElement(tp);
        
        //find first usage of this (undeclared) variable:
        TreePath method = tp;
        
        while (method.getLeaf().getKind() != Kind.COMPILATION_UNIT) {
            if (method.getLeaf().getKind() == Kind.METHOD) {
                break;
            }
            
            method = method.getParentPath();
        }
        
        if (method.getLeaf().getKind() != Kind.METHOD) {
            //TODO: probably initializer handle differently
            return;
        }
        
        class FirstUsage extends TreePathScanner<TreePath, Void> {
            private TreePath found;
            public @Override TreePath visitIdentifier(IdentifierTree tree, Void v) {
                if (tree.getName().contentEquals(el.getSimpleName())) {
                    if (found == null) {
                        found = getCurrentPath();
                    }
                    return findStatement(getCurrentPath());
                }
                return null;
            }
            public @Override TreePath visitBlock(BlockTree tree, Void v) {
                TreePath result = null;
                TreePath firstBranchStatementWithUsage = null;
                for (StatementTree t : tree.getStatements()) {
                    TreePath currentResult = scan(t, null);
                    
                    if (currentResult != null && result == null) {
                        result = currentResult;
                        firstBranchStatementWithUsage = new TreePath(getCurrentPath(), t);
                    }
                    
                    if (currentResult != t && result != null && result.getLeaf() != firstBranchStatementWithUsage.getLeaf()) {
                        //ie.: { x = 1; } ... { x = 1; }
                        result = firstBranchStatementWithUsage;
                    }
                }
                super.visitBlock(tree, v);
                return result;
            }
            public @Override TreePath reduce(TreePath tp1, TreePath tp2) {
                if (tp2 == null)
                    return tp1;
                
                return tp2;
                
            }
        }
        
        FirstUsage firstUsage  = new FirstUsage();
        TreePath firstUse = firstUsage.scan(method, null);
        
        if (firstUse == null || !isStatement(firstUse.getLeaf())) {
            Logger.getLogger("global").log(Level.WARNING, "Add local variable - cannot find a statement."); // NOI18N
            return;
        }
        
        StatementTree statement = (StatementTree) firstUse.getLeaf();
        
        if (statement.getKind() == Kind.EXPRESSION_STATEMENT) {
            ExpressionTree exp = ((ExpressionStatementTree) statement).getExpression();
            
            if (exp.getKind() == Kind.ASSIGNMENT) {
                //replace the expression statement with a variable declaration:
                AssignmentTree at = (AssignmentTree) exp;
                VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.Type(proposedType), at.getExpression());
                
                wc.rewrite(statement, vt);
                
                return;
            }
        }
        
        Tree statementParent = firstUse.getParentPath().getLeaf();
        VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.Type(proposedType), null);
        
        if (statementParent.getKind() == Kind.BLOCK) {
            BlockTree block = (BlockTree) statementParent;
            BlockTree nueBlock = make.insertBlockStatement(block, block.getStatements().indexOf(statement), vt);
            
            wc.rewrite(block, nueBlock);
        } else {
            BlockTree block = make.Block(Arrays.asList(vt, statement), false);
            
            wc.rewrite(statement, block);
        }
    }
    
    private TreePath findStatement(TreePath tp) {
        TreePath statement = tp;
        
        while (statement.getLeaf().getKind() != Kind.COMPILATION_UNIT) {
            if (isStatement(statement.getLeaf())) {
                return statement;
            }
            
            statement = statement.getParentPath();
        }
        
        return null;
    }
    
    private MethodTree findMethod(TreePath tp) {
        TreePath method = tp;
        
        while (method.getLeaf().getKind() != Kind.COMPILATION_UNIT) {
            if (method.getLeaf().getKind() == Kind.METHOD) {
                return (MethodTree) method.getLeaf();
            }
            
            method = method.getParentPath();
        }
        
        return null;
    }
    
    private boolean isStatement(Tree t) {
        Class intClass = t.getKind().asInterface();
        
        return StatementTree.class.isAssignableFrom(intClass);
    }
    
    String toDebugString(CompilationInfo info) {
        return "AddParameterOrLocalFix:" + name + ":" + type.resolve(info).toString() + ":" + parameter; // NOI18N
    }
}

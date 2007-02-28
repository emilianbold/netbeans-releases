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

package org.netbeans.modules.visualweb.insync.java;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author jdeva
 */
public class Method {
    private ElementHandle<ExecutableElement> execElementHandle;
    private JavaClass javaClass;    //Enclosing java class
    
    public Method(ExecutableElement element, JavaClass javaClass) {
        execElementHandle = ElementHandle.create(element);
        this.javaClass = javaClass;
    }
    
    /*
     *  Returns enclosing java class
     */ 
    public JavaClass getJavaClass() {
        return javaClass;
    }
    
    /*
     * Looks for a expression statement of the form a.b(arg1, ..); where a and b are the passed
     * in bName and mName respectively. Returns null if no such statement is found
     */     
    public Statement findStatement(final String beanName, final String methodName) {
        return (Statement)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                StatementTree stmtTree = findStatement(cinfo, beanName, methodName);
                return new Statement(TreePathHandle.create(TreeUtils.getTreePath(cinfo, stmtTree), cinfo),
                        Method.this, beanName, methodName);           
            }
        }, javaClass.getFileObject());    
    }
    
    /*
     * Looks for a expression statement of the form a.b(arg1, ..); where a and b are the passed
     * in bName and mName respectively. Returns null if no such statement is found
     */     
    StatementTree findStatement(CompilationInfo cinfo, String beanName, String methodName) {
        ExecutableElement execElement = execElementHandle.resolve(cinfo);
        BlockTree block = cinfo.getTrees().getTree(execElement).getBody();
        for(StatementTree statement : block.getStatements()) {
            if(statement.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                ExpressionStatementTree exprStatTree = (ExpressionStatementTree)statement;
                if(exprStatTree.getExpression().getKind() == Tree.Kind.METHOD_INVOCATION) {
                    MethodInvocationTree methInvkTree = (MethodInvocationTree)exprStatTree.getExpression();
                    if(methInvkTree.getMethodSelect().getKind() == Tree.Kind.MEMBER_SELECT) {
                        MemberSelectTree memSelTree = (MemberSelectTree)methInvkTree.getMethodSelect();
                        ExpressionTree exprTree = memSelTree.getExpression();
                        if(exprTree.getKind() == Tree.Kind.IDENTIFIER &&
                                memSelTree.getIdentifier().toString().equals(methodName) &&
                                ((IdentifierTree)exprTree).getName().toString().equals(beanName)) {
                            return statement;
                        }
                    }
                }
            }
        }
        return null;
    }

    /*
     * Adds a expression statement of the form a.b(arg1, ..);
     */ 
    private StatementTree addMethodInvocationStatement(WorkingCopy wc, MethodTree methodTree,
            String beanName, String methodName, List<ExpressionTree> args) {
         return addMethodInvocationStatement(wc, methodTree, 
                 TreeMakerUtils.createMethodInvocation(wc, beanName, methodName, args));
    }
    
    /*
     * Adds a expression statement of the form a.b();
     * 
     */ 
    public Statement addMethodInvocationStatement(final String beanName, 
            final String methodName, final String valueSource) {
        WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                TreeMaker make = wc.getTreeMaker();
                ExecutableElement elem = javaClass.getMethod(wc, "_init", new Class[]{});
                ArrayList<ExpressionTree> args = new ArrayList<ExpressionTree>();
                SourcePositions[] positions = new SourcePositions[1];
                args.add(wc.getTreeUtilities().parseExpression(valueSource, positions));
                addMethodInvocationStatement(wc, wc.getTrees().getTree(elem),
                        TreeMakerUtils.createMethodInvocation(wc, beanName, methodName, args));
                return null;
            }
        }, javaClass.getFileObject());
        return findStatement(beanName, methodName);
    }

    /*
     * Adds a expression statement of the form a.b(arg1, ..);
     */     
    public StatementTree addMethodInvocationStatement(WorkingCopy wc, MethodTree methodTree, 
            MethodInvocationTree exprTree) {
        ExpressionStatementTree exprStatTree = wc.getTreeMaker().ExpressionStatement(exprTree);
        addStatement(wc, methodTree.getBody(), exprStatTree);
        return exprStatTree;
    }   
    
    /*
     * Adds a return statement given a method and expression
     */     
    public StatementTree addReturnStatement(WorkingCopy wc, MethodTree methodTree, ExpressionTree exprTree) {
        ReturnTree returnTree = wc.getTreeMaker().Return(exprTree);
        addStatement(wc, methodTree.getBody(), returnTree);
        return returnTree;
    }
    
    /*
     * Adds a given statement to the block
     */       
    private BlockTree addStatement(WorkingCopy wc, BlockTree blockTree, StatementTree stmtTree) {
        BlockTree newBlockTree = wc.getTreeMaker().addBlockStatement(blockTree, stmtTree);
        wc.rewrite(blockTree, newBlockTree);
        return newBlockTree;
    }

    /*
     * Replaces method body with a given text
     */ 
    public MethodTree replaceMethodBody(WorkingCopy wc, MethodTree methodTree, String bodyText) {
        MethodTree newMethodTree = wc.getTreeMaker().Method(methodTree.getModifiers(), methodTree.getName(), 
                methodTree.getReturnType(), methodTree.getTypeParameters(), methodTree.getParameters(), 
                methodTree.getThrows(), bodyText, (ExpressionTree)methodTree.getDefaultValue());
        wc.rewrite(methodTree, newMethodTree);
        return newMethodTree;
    }

    /*
     * Removes a statement given a method and statement to be removed
     */     
    boolean removeStatement(WorkingCopy wc, StatementTree stmtTree) {
        ExecutableElement execElement = execElementHandle.resolve(wc);
        if(execElement != null) {
            BlockTree blockTree = wc.getTrees().getTree(execElement).getBody();
            BlockTree newBlockTree = wc.getTreeMaker().removeBlockStatement(blockTree, stmtTree);
            wc.rewrite(blockTree, newBlockTree);           
            return true;
        }
        return false;
    }
    
    /*
     * Removes the method from the enclosing class
     */ 
    public void remove() {
        javaClass.removeMethod(execElementHandle);
    }

    /*
     * Returns list of property set statements(i.e statements which looks like a.setFoo(arg1)
     * 
     */ 
    public List<Statement> getPropertySetStatements() {
        return (List<Statement>)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                List<Statement> stmts = new ArrayList<Statement>();
                ExecutableElement execElement = execElementHandle.resolve(cinfo);
                BlockTree block = cinfo.getTrees().getTree(execElement).getBody();
                for(StatementTree stmtTree : block.getStatements()){
                    if(Statement.IsPropertySetter(cinfo, stmtTree)) {
                        stmts.add(Statement.createStatementClass(cinfo, stmtTree, Method.this));
                    }
                }
                return stmts;
            }
        }, javaClass.getFileObject());           
    }
}

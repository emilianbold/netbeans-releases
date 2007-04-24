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
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.WorkingCopy;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
/**
 *
 * @author jdeva
 */
public class EventMethod extends Method{
    private String delegateName;
    
    /** Creates a new instance of DelegatorMethod */
    public EventMethod(ExecutableElement element, JavaClass javaClass) {
        super(element, javaClass);
    }
    
    /**
     * Update the last null return statement with a string return statement
     */
    public void updateLastReturnStatement(final String newStr) {
         WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                ExecutableElement execElement = execElementHandle.resolve(wc);
                BlockTree block = wc.getTrees().getTree(execElement).getBody();
                List<? extends StatementTree> stmts = block.getStatements();
                if(stmts.size() > 0) {
                    StatementTree stmt = stmts.get(stmts.size() - 1);
                    if(stmt.getKind() == Tree.Kind.RETURN) {
                        ReturnTree ret = (ReturnTree)stmt;
                        ExpressionTree expr = ret.getExpression();
                        if(expr.getKind() == Tree.Kind.NULL_LITERAL) {
                            Tree newExpr = wc.getTreeMaker().Literal(newStr);
                            wc.rewrite(expr, newExpr);
                        }
                    }
                }
                return null;
            }
        }, javaClass.getFileObject());
    }

    /**
     * Update all return statements returning the old string literal with new 
     * string literal. Uses visitor class to achieve
     */ 
    public void updateReturnStrings(final String oldStr, final String newStr) {
        WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                ExecutableElement execElement = execElementHandle.resolve(wc);
                BlockTree block = wc.getTrees().getTree(execElement).getBody();
                TreePath treePath = TreeUtils.getTreePath(wc, block);
                new Refactor.ReturnStatementLiteralRenamer(wc, oldStr, newStr).scan(treePath, null);
                return null;
            }
        }, javaClass.getFileObject());        
     }
    
    /**
     * @return the string returned by the last return statement
     */ 
    public String getMethodReturn() {
        return (String)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                ExecutableElement execElement = execElementHandle.resolve(cinfo);
                BlockTree block = cinfo.getTrees().getTree(execElement).getBody();
                List<? extends StatementTree> stmts = block.getStatements();
                if(stmts.size() > 0) {
                    StatementTree stmt = stmts.get(stmts.size() - 1);
                    if(stmt.getKind() == Tree.Kind.RETURN) {
                        ReturnTree ret = (ReturnTree)stmt;
                        ExpressionTree expr = ret.getExpression();
                        if(expr.getKind() == Tree.Kind.STRING_LITERAL) {
                            return ((LiteralTree)expr).getValue();
                        }
                    }
                }
                return null;
            }
        }, javaClass.getFileObject());
    }
}

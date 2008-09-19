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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.visualweb.insync.java;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
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
//                BlockTree block = cinfo.getTrees().getTree(execElement).getBody();
                MethodTree methodTree = cinfo.getTrees().getTree(execElement);
                if (methodTree == null) {
                    // #139727 NPE
                    return null;
                }
                BlockTree block = methodTree.getBody();
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

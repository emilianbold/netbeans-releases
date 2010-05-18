/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author jdeva
 */
public class DelegatorMethod extends Method{
    private String delegateName;
    
    /** Creates a new instance of DelegatorMethod */
    public DelegatorMethod(ExecutableElement element, JavaClass javaClass) {
        super(element, javaClass);
    }
    
    /*
     * Returns the name of the method to which this method delegates
     * Ex: foo() { fooImpl();}, returns 'fooImpl'
     */
    public String getDelegateName() {
        if(delegateName != null) {
            return delegateName;
        }
        return (String)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                MethodInvocationTree methInvk = getDelegate(cinfo);
                if(methInvk != null) {
                    delegateName = ((IdentifierTree)methInvk.getMethodSelect()).getName().toString();
                }
                return delegateName;
            }
        }, javaClass.getFileObject());
    }
    
    
    /*
     * Renames the name of the method to which this method delegates
     * Ex: foo() { fooImpl();} fooImpl is renamed
     */
    public void setDelegateName(final String name) {
        WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                MethodInvocationTree methInvk = getDelegate(wc);
                if(methInvk != null) {
                    IdentifierTree oldTree = (IdentifierTree)methInvk.getMethodSelect();
                    IdentifierTree newTree = wc.getTreeMaker().Identifier(name);
                    wc.rewrite(oldTree, newTree);
                }
                return null;
            }
        }, javaClass.getFileObject());
        delegateName = name;
    }
    
    
    /*
     * Adds a delegate statement to this method
     * 
     */ 
    public Statement addDelegateStatement(final String methodName, final String[] pNames, final boolean noreturn) {
        WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                TreeMaker make = wc.getTreeMaker();
                ExecutableElement elem = execElementHandle.resolve(wc);//javaClass.getMethod(wc, "_init", new Class[]{});
                MethodInvocationTree methInvkTree = TreeMakerUtils.createMethodInvocation(wc, methodName, pNames);
                if(noreturn) {
                    addMethodInvocationStatement(wc, wc.getTrees().getTree(elem), methInvkTree);
                }else {
                    addReturnStatement(wc, wc.getTrees().getTree(elem), methInvkTree);
                }
                return null;
            }
        }, javaClass.getFileObject());
        return null;
    }
    
    private MethodInvocationTree getDelegate(CompilationInfo cinfo) {
        ExecutableElement execElement = execElementHandle.resolve(cinfo);
        if(execElement != null) {
            MethodTree mTree = cinfo.getTrees().getTree(execElement);
            List<? extends StatementTree> stmts = mTree.getBody().getStatements();
            if(stmts.size() == 1) {
                StatementTree stmt = stmts.get(0);
                ExpressionTree expr = null;
                if(stmt.getKind() == Tree.Kind.RETURN) {
                    expr = ((ReturnTree)stmt).getExpression();
                }else if(stmt.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                    expr = ((ExpressionStatementTree)stmt).getExpression();
                }
                if(expr != null && expr.getKind() == Tree.Kind.METHOD_INVOCATION) {
                    MethodInvocationTree methInvk = (MethodInvocationTree)expr;
                    if(methInvk.getArguments().size() == mTree.getParameters().size() &&
                            methInvk.getMethodSelect().getKind() == Tree.Kind.IDENTIFIER) {
                        return methInvk;
                    }
                }
            }
        }
        return null;
    }
}

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

import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author jdeva
 */
public class Statement {
    private TreePathHandle stmtTreePathHandle;
    private Method method;          //Enclosing method
    private String beanName;        
    private String setterName;      
    
    /** Creates a new instance of StatementClass */
    public Statement(TreePathHandle stmtTreePathHandle, Method method, 
            String beanName, String setterName) {
        this.stmtTreePathHandle = stmtTreePathHandle;
        this.method = method;
        this.beanName = beanName;
        this.setterName = setterName;
    }
    
    /* 
     * Returns bean name for this property set statement
     */ 
    public String getBeanName() {
        return beanName;
    }
    
    /* 
     * Returns bean name for this property set statement
     */ 
    public String getPropertySetterName() {
        return setterName;
    }    
    
    /*
     *  Replaces the argument for property setter method invocation. Parses the passed in string
     *  to obtain the expression which is then replaces the old one
     */ 
    public void replaceArgument(final String valueSource) {
        WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                TreeMaker make = wc.getTreeMaker();
                ExpressionTree arg = getArgument(wc);
                SourcePositions[] positions = new SourcePositions[1];
                ExpressionTree newArg = wc.getTreeUtilities().parseExpression(valueSource, positions);
                wc.rewrite(arg, newArg);
                return null;
            }
        }, method.getJavaClass().getFileObject());
    }
    
    /*
     * Evaluate the argument of property setter method
     */ 
    public Object evaluateArgument() {
        return ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                return ExpressionUtils.getValue(cinfo, getArgument(cinfo));
            }
        }, method.getJavaClass().getFileObject());            
    }
    
    
    /*
     * Returns the argument for the property set statement
     */ 
    private ExpressionTree getArgument(CompilationInfo cinfo) {
        StatementTree stmtTree = (StatementTree) stmtTreePathHandle.resolve(cinfo).getLeaf();
        //StatementTree stmtTree = method.findPropertyStatement(cinfo, beanName, setterName);
        if (stmtTree != null) {
            // #137584 Fixing IndexOutOfBoundsException.
            List<? extends ExpressionTree> list = getMethodInvocationTree(cinfo, stmtTree).getArguments();
            return list.isEmpty() ? null : list.get(0);
        }
        return null;
    }
    
    /*
     * Returns the eventset adapter class
     */ 
    public JavaClass getAdapterClass() {
        return (JavaClass)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                ExpressionTree arg = getArgument(cinfo);
                if(arg.getKind() == Tree.Kind.NEW_CLASS) {
                    NewClassTree newClassTree = (NewClassTree)arg;
                    TypeElement typeElem = (TypeElement)TreeUtils.getElement(cinfo, newClassTree.getClassBody());
                    return new JavaClass(typeElem, Statement.this.method.getJavaClass().getFileObject());
                }
                return null;
            }
        }, method.getJavaClass().getFileObject());            
    }    
    
    /*
     * Returns the textual string of the argument of  property setter method
     */     
    public String getArgumentSource() {
        return (String)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                 return ExpressionUtils.getArgumentSource(cinfo, getArgument(cinfo));
            }
        }, method.getJavaClass().getFileObject());            
    }    
    
    /*
     * Returns true if the statement represents a property set statement
     * For example - a.foo(arg);
     */
    public static boolean IsPropertySetter(CompilationInfo cinfo, StatementTree stmtTree) {
        if(stmtTree.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
            ExpressionStatementTree exprStmtTree = (ExpressionStatementTree)stmtTree;
            if(exprStmtTree.getExpression().getKind() == Tree.Kind.METHOD_INVOCATION) {
                MethodInvocationTree methInvkTree = (MethodInvocationTree)exprStmtTree.getExpression();
                if(methInvkTree.getMethodSelect().getKind() == Tree.Kind.MEMBER_SELECT) {
                    MemberSelectTree memSelTree = (MemberSelectTree)methInvkTree.getMethodSelect();
                    if(methInvkTree.getArguments().size() == 1 && 
                            memSelTree.getExpression().getKind() == Tree.Kind.IDENTIFIER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /*
     * Creates a Statement class provided a statement tree reprsenting property set statement and its
     * enclosing Method
     */ 
    public static Statement createStatementClass(CompilationInfo cinfo, StatementTree stmtTree, Method method) {
        MethodInvocationTree methInvkTree = getMethodInvocationTree(cinfo, stmtTree);
        MemberSelectTree memSelTree = (MemberSelectTree)methInvkTree.getMethodSelect();
        String beanName = ((IdentifierTree)memSelTree.getExpression()).getName().toString();
        String setterName = memSelTree.getIdentifier().toString();
        return new Statement(TreePathHandle.create(
                TreeUtils.getTreePath(cinfo, stmtTree), cinfo), method, beanName, setterName);
    }
    /*
     * Returns a method invocation tree. 
     * It assumes the passed in argument represents a bean property set statement
     * 
     */    
    public static MethodInvocationTree getMethodInvocationTree(CompilationInfo cinfo, StatementTree stmtTree) {
        return (MethodInvocationTree)((ExpressionStatementTree)stmtTree).getExpression();
    }
    
    public boolean remove() {
        Boolean result = (Boolean)WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                TreeMaker make = wc.getTreeMaker();
                StatementTree stmtTree = (StatementTree) stmtTreePathHandle.resolve(wc).getLeaf();
                //StatementTree stmtTree = method.findPropertyStatement(wc, beanName, setterName);
                return method.removeStatement(wc, stmtTree);
            }
        }, method.getJavaClass().getFileObject());
        return result.booleanValue();
    }
    
    //This class holds on to bean name because of bug #96387
    //Until that bug is fixed, this is a workaround to fix #103122 
    public void setBeanName(String name) {
        this.beanName = name;
    }    
}

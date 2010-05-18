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

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.CompilationInfo;

/**
 *
 * @author jdeva
 */
public class ExpressionUtils {
    /*
     * Checks if the expression is a literal
     */ 
    private static boolean isLiteral(ExpressionTree exprTree) {
        Tree.Kind kind = exprTree.getKind();
        if(kind == kind.BOOLEAN_LITERAL || kind == kind.CHAR_LITERAL || kind == kind.DOUBLE_LITERAL ||
           kind == kind.FLOAT_LITERAL || kind == kind.INT_LITERAL || kind == kind.LONG_LITERAL ||
           kind == kind.NULL_LITERAL || kind == kind.STRING_LITERAL) {
            return true;
        }
        return false;
    }
    
    /*
     * Evaluates expression if it is one of the following
     * a) LiteralTree
     * b) MethodInvocationTree - only if the method is static
     * c) NewClassTree
     * d) NewArrayTree
     * 
     */
    public static Object getValue(CompilationInfo cinfo, ExpressionTree valueExpr) {
        Object value = null;
        try {
            if(isLiteral(valueExpr)){
                value = ((LiteralTree)valueExpr).getValue();
            }else if(valueExpr.getKind() == Tree.Kind.MEMBER_SELECT) {
                //Handles static field access in expression
                MemberSelectTree memSelTree = (MemberSelectTree)valueExpr;
                Object object = null;
                Class cls = null;
                if(memSelTree.getExpression().getKind() != Tree.Kind.MEMBER_SELECT) {
                    //Ex: new Color(0,0,0).
                    object = getValue(cinfo, memSelTree.getExpression());
                    if(object != null) {
                        cls = object.getClass();
                    }
                }else {
                    //Ex: Color.BLACK.
                    try {
                        cls = getClass(cinfo, memSelTree.getExpression());
                    }catch(ClassNotFoundException cnfe) {
                        object = getValue(cinfo, memSelTree.getExpression());
                        if(object != null) {
                            cls = object.getClass();
                        }
                    }
                }   
                if(cls != null) {
                    Field f = cls.getField(memSelTree.getIdentifier().toString());
                    if(f != null && (object != null || (f.getModifiers() & Modifier.STATIC) > 0)) {
                        value = f.get(object);
                    }
                }
                return value;
            }else if(valueExpr.getKind() == Tree.Kind.METHOD_INVOCATION) {
                MethodInvocationTree mInvoke = (MethodInvocationTree)valueExpr;
                ExpressionTree selectTree = mInvoke.getMethodSelect();
                if(selectTree.getKind() != Tree.Kind.MEMBER_SELECT) {
                    return null;
                }
                MemberSelectTree memSelTree = (MemberSelectTree)selectTree;
                String mname = memSelTree.getIdentifier().toString();

                List<? extends ExpressionTree> params = mInvoke.getArguments();
                int count = params.size();
                Object[] argv = new Object[count];
                Class[] argvt = new Class[count];
                Class cls = null;
                java.lang.reflect.Method m = null;
                String cname = null;
                
                Element elem = cinfo.getTrees().getElement(cinfo.getTrees().getPath(cinfo.getCompilationUnit(), memSelTree));
                Object object = null;
                if(elem instanceof ExecutableElement) {
                    //Check for static field access(ex: Color.BLACK.toString())
                    object = getValue(cinfo, memSelTree.getExpression());
                    if(object != null) {
                        cls = object.getClass();
                    }else {
                        cls = getClass(cinfo, memSelTree.getExpression());
                    }
                    if(cls != null) {
                        ExecutableElement execElem = (ExecutableElement)elem;
                        List<? extends VariableElement> actualParams = execElem.getParameters();
                        for (int i = 0; i < count; i++) {
                            ExpressionTree arg = (ExpressionTree) params.get(i);
                            argv[i] = getValue(cinfo, arg);
                            argvt[i] = ClassUtil.getClass(actualParams.get(i).asType().toString());
                        }
                        m = cls.getMethod(mname, argvt);
                    }
                }
                if(m != null && (object != null || (m.getModifiers() & Modifier.STATIC) > 0)) {
                    value = m.invoke(object, argv);
                }
            }else if(valueExpr instanceof NewClassTree) {
                NewClassTree newClassTree = (NewClassTree) valueExpr;
                Class cls = getClass(cinfo, newClassTree.getIdentifier());
                if(cls == null) {
                    return null;
                }

                List<? extends ExpressionTree> params = newClassTree.getArguments();
                int count = params.size();
                Object[] argv = new Object[count];
                Class[] argvt = new Class[count];
                java.lang.reflect.Constructor ctor = null;
                Element elem = cinfo.getTrees().getElement(cinfo.getTrees().getPath(cinfo.getCompilationUnit(), newClassTree));                
                if(elem instanceof ExecutableElement) {
                    ExecutableElement ctorElem = (ExecutableElement)elem;
                    List<? extends VariableElement> actualParams = ctorElem.getParameters();
                    for (int i = 0; i < count; i++) {
                        ExpressionTree arg = (ExpressionTree) params.get(i);
                        argv[i] = getValue(cinfo, arg);
                        argvt[i] = ClassUtil.getClass(actualParams.get(i).asType().toString());
                    }
                    ctor = cls.getConstructor(argvt);
                }
                if(ctor != null) {
                    value = ctor.newInstance(argv);
                }
            }else if(valueExpr instanceof NewArrayTree){
                NewArrayTree newArrayTree = (NewArrayTree)valueExpr;
                List<? extends ExpressionTree> inits = newArrayTree.getInitializers();
                int count = inits.size();
                Class elemClass = getClass(cinfo, newArrayTree.getType());
                Object array = Array.newInstance(elemClass, count);
                for (int i = 0; i < count; i++) {
                    ExpressionTree elem = (ExpressionTree) inits.get(i);
                    Array.set(array, i, getValue(cinfo, elem));
                }
                value = array;
                
            }else if(valueExpr instanceof TypeCastTree) {
                value = getValue(cinfo, ((TypeCastTree)valueExpr).getExpression());
            }else if(valueExpr instanceof AssignmentTree) {
                value = getValue(cinfo, ((AssignmentTree)valueExpr).getExpression());
            }
        }catch (Exception e) {
            System.out.println("ExpressinUtils.getValue() " + valueExpr);
            //Ignore exception and return null
        }

        return value;
    }
    
    /*
     * Internal utility method to obtain a class given the type tree
     */
    private static Class getClass(CompilationInfo cinfo, Tree tree) throws ClassNotFoundException{
        String typeName = TreeUtils.getFQN(cinfo, tree);
        return ClassUtil.getClass(typeName);
    }
    
    public static String getArgumentSource(CompilationInfo cinfo, ExpressionTree exprTree) {
        long end = cinfo.getTrees().getSourcePositions().getEndPosition(cinfo.getCompilationUnit(), exprTree);
        long start = cinfo.getTrees().getSourcePositions().getStartPosition(cinfo.getCompilationUnit(), exprTree);
        return cinfo.getText().substring((int)start, (int)end);
    }    
}

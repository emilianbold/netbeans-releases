/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import static com.sun.source.tree.Tree.Kind.*;

/**
 *
 * @author Jan Lahoda
 */
public final class AddCast implements ErrorRule<Void> {
    
    static void computeType(CompilationInfo info, int offset, TypeMirror[] tm, ExpressionTree[] expression, Tree[] leaf) {
        TreePath path = info.getTreeUtilities().pathFor(offset + 1);
        
        //TODO: this does not seem nice:
        while (path != null) {
            Tree scope = path.getLeaf();
            TypeMirror expected = null;
            TypeMirror resolved = null;
            ExpressionTree found = null;
            
            if (scope.getKind() == Kind.VARIABLE && ((VariableTree) scope).getInitializer() != null) {
                expected = info.getTrees().getTypeMirror(path);
                found = ((VariableTree) scope).getInitializer();
                resolved = info.getTrees().getTypeMirror(new TreePath(path, found));
            }
            
            if (scope.getKind() == Kind.ASSIGNMENT) {
                expected = info.getTrees().getTypeMirror(path);
                found = ((AssignmentTree) scope).getExpression();
                resolved = info.getTrees().getTypeMirror(new TreePath(path, found));
            }
            
            if (scope.getKind() == Kind.RETURN) {
                TreePath parents = path;
                
                while (parents != null && parents.getLeaf().getKind() != Kind.METHOD)
                    parents = parents.getParentPath();
                
                if (parents != null) {
                    Tree returnTypeTree = ((MethodTree) parents.getLeaf()).getReturnType();
                    if (returnTypeTree != null) {
                        expected = info.getTrees().getTypeMirror(new TreePath(parents, returnTypeTree));
                        found = ((ReturnTree) scope).getExpression();
                        resolved = info.getTrees().getTypeMirror(new TreePath(path, found));
                    }
                }
            }
            
            if (scope.getKind() == Kind.METHOD_INVOCATION || scope.getKind() == Kind.NEW_CLASS) {
                TypeMirror[] proposed = new TypeMirror[1];
                int[] index = new int[1];
                
                if (Utilities.fuzzyResolveMethodInvocation(info, path, proposed, index) != null) {
                    expected = proposed[0];
                    found = scope.getKind() == Kind.METHOD_INVOCATION ? ((MethodInvocationTree) scope).getArguments().get(index[0]) : ((NewClassTree) scope).getArguments().get(index[0]);
                    resolved = info.getTrees().getTypeMirror(new TreePath(path, found));
                }
            }
            
            if (expected != null && resolved != null) {
                TypeMirror foundTM = info.getTrees().getTypeMirror(new TreePath(path, found));
                
                if (foundTM.getKind() == TypeKind.EXECUTABLE) {
                    //XXX: ignoring executable, see AddCast9 for more information when this happens.
                } else {
                    if (   !info.getTypes().isAssignable(foundTM, expected)
                        && info.getTypeUtilities().isCastable(resolved, expected)
                           /*#85346: cast hint should not be proposed for error types:*/
                        && foundTM.getKind() != TypeKind.ERROR
                        && expected.getKind() != TypeKind.ERROR) {
                        tm[0] = expected;
                        expression[0] = found;
                        leaf[0] = scope;
                    } else { //clean up
                        tm[0] = null;
                    }
                }
            }
            
            path = path.getParentPath();
        }
        
        if (tm[0] != null) {
            tm[0] = org.netbeans.modules.java.hints.errors.Utilities.resolveCapturedType(info, tm[0]);
        }
    }
    
    public Set<String> getCodes() {
        return new HashSet<String>(Arrays.asList("compiler.err.prob.found.req", "compiler.err.cant.apply.symbol", "compiler.err.cant.resolve.location")); // NOI18N
    }
    
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        List<Fix> result = new ArrayList<Fix>();
        TypeMirror[] tm = new TypeMirror[1];
        ExpressionTree[] expression = new ExpressionTree[1];
        Tree[] leaf = new Tree[1];
        
        computeType(info, offset, tm, expression, leaf);
        
        if (tm[0] != null) {
            int position = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), expression[0]);
            Class interf = expression[0].getKind().asInterface();
            boolean wrapWithBrackets = interf == BinaryTree.class || interf == ConditionalExpressionTree.class;
            result.add(new AddCastFix(info.getJavaSource(), new HintDisplayNameVisitor(info).scan(expression[0], null), Utilities.getTypeName(tm[0], false).toString(), position, wrapWithBrackets));
        }
        
        return result;
    }
    
    public void cancel() {
        //XXX: not done yet
    }
    
    public String getId() {
        return AddCast.class.getName();
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(AddCast.class, "LBL_Add_Cast");
    }
    
    public String getDescription() {
        return NbBundle.getMessage(AddCast.class, "DSC_Add_Cast");
    }
    
    private static final Map<Kind, String> operator2DN;
    
    static {
        operator2DN = new HashMap<Kind, String>();
        
        operator2DN.put(AND, "&");
        operator2DN.put(XOR, "^");
        operator2DN.put(OR, "|");
        operator2DN.put(CONDITIONAL_AND, "&&");
        operator2DN.put(CONDITIONAL_OR, "||");
        operator2DN.put(MULTIPLY_ASSIGNMENT, "*=");
        operator2DN.put(DIVIDE_ASSIGNMENT, "/=");
        operator2DN.put(REMAINDER_ASSIGNMENT, "%=");
        operator2DN.put(PLUS_ASSIGNMENT, "+=");
        operator2DN.put(MINUS_ASSIGNMENT, "-=");
        operator2DN.put(LEFT_SHIFT_ASSIGNMENT, "<<=");
        operator2DN.put(RIGHT_SHIFT_ASSIGNMENT, ">>=");
        operator2DN.put(UNSIGNED_RIGHT_SHIFT_ASSIGNMENT, ">>>=");
        operator2DN.put(AND_ASSIGNMENT, "&=");
        operator2DN.put(XOR_ASSIGNMENT, "^=");
        operator2DN.put(OR_ASSIGNMENT, "|=");
        operator2DN.put(BITWISE_COMPLEMENT, "~");
        operator2DN.put(LOGICAL_COMPLEMENT, "!");
        operator2DN.put(MULTIPLY, "*");
        operator2DN.put(DIVIDE, "/");
        operator2DN.put(REMAINDER, "%");
        operator2DN.put(PLUS, "+");
        operator2DN.put(MINUS, "-");
        operator2DN.put(LEFT_SHIFT, "<<");
        operator2DN.put(RIGHT_SHIFT, ">>");
        operator2DN.put(UNSIGNED_RIGHT_SHIFT, ">>>");
        operator2DN.put(LESS_THAN, "<");
        operator2DN.put(GREATER_THAN, ">");
        operator2DN.put(LESS_THAN_EQUAL, "<=");
        operator2DN.put(GREATER_THAN_EQUAL, ">=");
        operator2DN.put(EQUAL_TO, "==");
        operator2DN.put(NOT_EQUAL_TO, "!=");
    }
    
    private static class HintDisplayNameVisitor extends TreeScanner<String, Void> {
        
        private CompilationInfo info;

        public HintDisplayNameVisitor(CompilationInfo info) {
            this.info = info;
        }
        
        public @Override String visitIdentifier(IdentifierTree tree, Void v) {
            return "..." + tree.getName().toString();
        }
        
        public @Override String visitMethodInvocation(MethodInvocationTree tree, Void v) {
            ExpressionTree methodSelect = tree.getMethodSelect();
            
            return "..." + simpleName(methodSelect) + "(...)"; // NOI18N
        }

        public @Override String visitArrayAccess(ArrayAccessTree node, Void p) {
            return "..." + simpleName(node.getExpression()) + "[]"; // NOI18N
        }
        
        public @Override String visitNewClass(NewClassTree nct, Void p) {
            return "...new " + simpleName(nct.getIdentifier()) + "(...)"; // NOI18N
        }

        @Override
        public String visitBinary(BinaryTree node, Void p) {
            String dn = operator2DN.get(node.getKind());
            
            return scan(node.getLeftOperand(), p) + dn + scan(node.getRightOperand(), p);
        }

        @Override
        public String visitLiteral(LiteralTree node, Void p) {
            if (node.getValue() instanceof String)
                return "...";
            
            int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), node);
            int end   = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), node);
            
            return info.getText().substring(start, end);
        }
        
        private String simpleName(Tree t) {
            if (t.getKind() == Kind.IDENTIFIER) {
                return ((IdentifierTree) t).getName().toString();
            }
            
            if (t.getKind() == Kind.MEMBER_SELECT) {
                return ((MemberSelectTree) t).getIdentifier().toString();
            }
            
            if (t.getKind() == Kind.METHOD_INVOCATION) {
                return scan(t, null);
            }
            
            if (t.getKind() == Kind.PARAMETERIZED_TYPE) {
                return simpleName(((ParameterizedTypeTree) t).getType()) + "<...>"; // NOI18N
            }
            
            if (t.getKind() == Kind.ARRAY_ACCESS) {
                return simpleName(((ArrayAccessTree) t).getExpression()) + "[]"; //NOI18N
            }

            if (t.getKind() == Kind.PARENTHESIZED) {
                return "(" + simpleName(((ParenthesizedTree)t).getExpression()) + ")"; //NOI18N
            }

            if (t.getKind() == Kind.TYPE_CAST) {
                return simpleName(((TypeCastTree)t).getType());
            }

            if (t.getKind() == Kind.ARRAY_TYPE) {
                return simpleName(((ArrayTypeTree)t).getType());
            }
            
            throw new IllegalStateException("Currently unsupported kind of tree: " + t.getKind()); // NOI18N
        }
    }

}

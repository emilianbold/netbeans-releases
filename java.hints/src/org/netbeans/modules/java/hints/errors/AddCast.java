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

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Lahoda
 */
public final class AddCast implements ErrorRule<Void> {
    
    static void computeType(CompilationInfo info, int offset, TypeMirror[] tm, ExpressionTree[] expression, Tree[] leaf) {
        TreePath path = info.getTreeUtilities().pathFor(offset);
        
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
                    expected = info.getTrees().getTypeMirror(new TreePath(parents, ((MethodTree) parents.getLeaf()).getReturnType()));
                    found = ((ReturnTree) scope).getExpression();
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
                    }
                }
            }
            
            path = path.getParentPath();
        }
    }
    
    public Set<String> getCodes() {
        return Collections.singleton("compiler.err.prob.found.req"); // NOI18N
    }
    
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        List<Fix> result = new ArrayList<Fix>();
        TypeMirror[] tm = new TypeMirror[1];
        ExpressionTree[] expression = new ExpressionTree[1];
        Tree[] leaf = new Tree[1];
        
        computeType(info, offset, tm, expression, leaf);
        
        if (tm[0] != null) {
            int position = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), expression[0]);
            
            result.add(new AddCastFix(info.getJavaSource(), new HintDisplayNameVisitor().scan(expression[0], null), Utilities.getTypeName(tm[0], false).toString(), position));
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
    
    private static class HintDisplayNameVisitor extends TreeScanner<String, Void> {
        
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
            
            throw new IllegalStateException("Currently unsupported kind of tree: " + t.getKind()); // NOI18N
        }
    }

}

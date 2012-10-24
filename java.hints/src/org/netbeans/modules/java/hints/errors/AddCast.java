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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
import javax.lang.model.type.ErrorType;
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

    private static final Set<String> ERROR_CODES = new HashSet<String>(Arrays.asList(
            "compiler.err.prob.found.req", // NOI18N
            "compiler.err.cant.apply.symbol", // NOI18N
            "compiler.err.cant.resolve.location.args")); // NOI18N
    
    static void computeType(CompilationInfo info, int offset, TypeMirror[] tm, Tree[] typeTree, ExpressionTree[] expression, Tree[] leaf) {
        TreePath path = info.getTreeUtilities().pathFor(offset + 1);
        int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), path.getLeaf());
        
        //TODO: this does not seem nice:
        while (path != null) {
            Tree scope = path.getLeaf();
            TypeMirror expected = null;
            Tree expectedTree = null;
            TypeMirror resolved = null;
            ExpressionTree found = null;
            
            if (scope.getKind() == Kind.VARIABLE && ((VariableTree) scope).getInitializer() != null) {
                expected = info.getTrees().getTypeMirror(path);
                expectedTree = ((VariableTree) scope).getType();
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
                List<TypeMirror> proposed = new ArrayList<TypeMirror>();
                int[] index = new int[1];
                
                if (!Utilities.fuzzyResolveMethodInvocation(info, path, proposed, index).isEmpty()) {
                    expected = proposed.get(0);
                    found = scope.getKind() == Kind.METHOD_INVOCATION ? ((MethodInvocationTree) scope).getArguments().get(index[0]) : ((NewClassTree) scope).getArguments().get(index[0]);
                    resolved = info.getTrees().getTypeMirror(new TreePath(path, found));
                }
            }
            
            if (expected != null && resolved != null) {
                TypeMirror foundTM = info.getTrees().getTypeMirror(new TreePath(path, found));

                if (foundTM.getKind() == TypeKind.ERROR) {
                    foundTM = info.getTrees().getOriginalType((ErrorType) foundTM);
                }

                if (resolved.getKind() == TypeKind.ERROR) {
                    resolved = info.getTrees().getOriginalType((ErrorType) resolved);
                }

                if (   foundTM.getKind() == TypeKind.EXECUTABLE
                    || foundTM.getKind() == TypeKind.PACKAGE
                    || foundTM.getKind() == TypeKind.NONE
                    || foundTM.getKind() == TypeKind.OTHER) {
                    //XXX: ignoring executable, see AddCast9 for more information when this happens.
                    //XXX: ignoring NONE, see test161450
                } else {
                    if (info.getTypeUtilities().isCastable(resolved, expected)) {
                        if (!info.getTypes().isAssignable(foundTM, expected)
                                /*#85346: cast hint should not be proposed for error types:*/
                                && foundTM.getKind() != TypeKind.ERROR
                                && expected.getKind() != TypeKind.ERROR) {
                            tm[0] = expected;
                            typeTree[0] = expectedTree;
                            expression[0] = found;
                            leaf[0] = scope;
                        }
                    } else {
                        tm[0] = null; //clean up, test136313
                    }
                }
            }
            
            if (info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), scope) < start && scope.getKind() != Kind.PARENTHESIZED) {
                break;
            }

            path = path.getParentPath();
        }
        
        if (tm[0] != null) {
            tm[0] = org.netbeans.modules.java.hints.errors.Utilities.resolveCapturedType(info, tm[0]);
        }
    }

    public Set<String> getCodes() {
        return ERROR_CODES;
    }
    
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        List<Fix> result = new ArrayList<Fix>();
        TypeMirror[] tm = new TypeMirror[1];
        Tree[] tmTree = new Tree[1];
        ExpressionTree[] expression = new ExpressionTree[1];
        Tree[] leaf = new Tree[1];
        
        computeType(info, offset, tm, tmTree, expression, leaf);
        
        if (tm[0] != null && tm[0].getKind() != TypeKind.NULL) {
            int position = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), expression[0]);
            Class interf = expression[0].getKind().asInterface();
            boolean wrapWithBrackets = interf == BinaryTree.class || interf == ConditionalExpressionTree.class;
            result.add(new AddCastFix(info.getJavaSource(), org.netbeans.modules.java.hints.errors.Utilities.shortDisplayName(info, expression[0]), Utilities.getTypeName(info, tm[0], false).toString(), position, wrapWithBrackets));
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
    
}

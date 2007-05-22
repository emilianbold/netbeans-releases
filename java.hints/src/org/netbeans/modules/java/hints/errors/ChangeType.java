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

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
 * This creates the hint to change the type of a variable to the type of 
 * initializer expression. In effect it works opposite of Add Cast hint.
 * 
 * @author Sandip Chitale
 */
public final class ChangeType implements ErrorRule<Void> {
    
    static void computeType(CompilationInfo info, int offset, TypeMirror[] tm, ExpressionTree[] expression, TypeMirror[] expressionType, Tree[] leaf) {
        TreePath path = info.getTreeUtilities().pathFor(offset);

        // Try to locate the right tree
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
            
            if (expected != null && resolved != null) {
                TypeMirror foundTM = info.getTrees().getTypeMirror(new TreePath(path, found));
                
                if (foundTM.getKind() == TypeKind.EXECUTABLE) {
                    //XXX: ignoring executable, see AddCast9 for more information when this happens.
                } else {
                    if (
                        /*#85346: cast hint should not be proposed for error types:*/
                        foundTM.getKind() != TypeKind.ERROR
                        && expected.getKind() != TypeKind.ERROR) {
                        tm[0] = expected;
                        expression[0] = found;
                        expressionType[0] = resolved;
                        leaf[0] = scope;
                        break;
                    }
                }
            }
            
            path = path.getParentPath();
        }
    }

    // Initialize the compiler error codes to which this hint responds.
    private static Set<String> codes = new HashSet<String>();
    static
    {
        codes = new HashSet<String>();
        codes.add("compiler.err.prob.found.req"); // NOI18N
        codes.add("compiler.err.incomparable.types"); // NOI18N
        codes = Collections.unmodifiableSet(codes);
    }
    
    public Set<String> getCodes() {
        return codes;
    }
    
    public List<Fix> run(CompilationInfo info,
            String diagnosticKey,
            int offset,
            TreePath treePath,
            Data<Void> data) {
        List<Fix> result = new ArrayList<Fix>();
        TypeMirror[] tm = new TypeMirror[1];
        ExpressionTree[] expression = new ExpressionTree[1];
        TypeMirror[] expressionType = new TypeMirror[1];
        Tree[] leaf = new Tree[1];
        
        computeType(info, offset, tm, expression, expressionType, leaf);
        
        if (leaf[0] instanceof VariableTree) {
            if (tm[0] != null) {
                int position = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), leaf[0]) + 1; // Need to add 1. Bug?
                result.add(new ChangeTypeFix(info.getJavaSource(),
                        ((VariableTree) leaf[0]).getName().toString(), 
                        Utilities.getTypeName(expressionType[0], false).toString(), position));
            }
        }
        
        return result;
    }
    
    public void cancel() {
        //XXX: not done yet
    }
    
    public String getId() {
        return ChangeType.class.getName();
    }
    
    public String getDisplayName() {
        return  NbBundle.getMessage(ChangeType.class, "MSG_ChangeVariablesTypeDisplayName"); // NOI18N
    }
    
    public String getDescription() {
        return NbBundle.getMessage(ChangeType.class, "MSG_ChangeVariablesTypeDescription"); // NOI18N
    }
}

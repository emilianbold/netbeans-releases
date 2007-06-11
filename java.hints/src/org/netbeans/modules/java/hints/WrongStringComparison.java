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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 */
public class WrongStringComparison extends AbstractHint {

    private static String STRING_TYPE = "java.lang.String";  // NOI18N
    
    private static final List<Fix> NO_FIXES = Collections.<Fix>emptyList();
    
    private static final Set<Tree.Kind> TREE_KINDS = 
            EnumSet.<Tree.Kind>of( Tree.Kind.EQUAL_TO, Tree.Kind.NOT_EQUAL_TO );
    
    public WrongStringComparison() {
        super( true, true, AbstractHint.HintSeverity.WARNING);
    }


    public Set<Kind> getTreeKinds() {
        return TREE_KINDS;
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
                
        Tree t = treePath.getLeaf();
        
        if ( t.getKind() != Tree.Kind.EQUAL_TO && 
             t.getKind() != Tree.Kind.NOT_EQUAL_TO ) {
            return null;
        }
        
        BinaryTree bt = (BinaryTree) t;
        
        System.out.println("BT " + bt);
        
        TreePath left = new TreePath(treePath, bt.getLeftOperand() );
        TreePath right = new TreePath(treePath, bt.getRightOperand() );
        
        Trees trees = info.getTrees(); 
        TypeMirror leftType = left == null ? null : trees.getTypeMirror(left);
        TypeMirror rightType = right == null ? null : trees.getTypeMirror(right);
                
        if ( leftType != null && rightType != null && 
             STRING_TYPE.equals(leftType.toString()) && 
             STRING_TYPE.equals(rightType.toString())) {
            return Collections.<ErrorDescription>singletonList(
                ErrorDescriptionFactory.createErrorDescription(
                    getSeverity().toEditorSeverity(), 
                    getDisplayName(), 
                    // Collections.<Fix>singletonList(new EmptyStatementFix( info.getFileObject(), TreePathHandle.create(tp, info) ) ), 
                    NO_FIXES,    
                    info.getFileObject(),
                    (int)info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t),
                    (int)info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), t)) );

        }
        
        return null;
    }

    public void cancel() {
        // Does nothing
    }
    
    public String getId() {
        return "Wrong_String_Comparison";
    }

    public String getDisplayName() {
        return NbBundle.getMessage(WrongStringComparison.class, "LBL_WrongStringComparison");
    }

    public String getDescription() {
        return NbBundle.getMessage(WrongStringComparison.class, "DSC_WrongStringComparison");
    }

}

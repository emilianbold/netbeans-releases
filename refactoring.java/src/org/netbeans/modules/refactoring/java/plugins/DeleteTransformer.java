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

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.*;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Jan Becicka
 */
public class DeleteTransformer extends SearchVisitor {

    public DeleteTransformer(WorkingCopy workingCopy) {
        super(workingCopy);
    }

    @Override
    public Tree visitMethod(MethodTree tree, Element p) {
        deleteDeclIfMatch(tree, p);
        return super.visitMethod(tree, p);
    }

    public Tree visitClass(ClassTree tree, Element p) {
        deleteDeclIfMatch(tree, p);
        return super.visitClass(tree, p);
    }

    public Tree visitVariable(VariableTree tree, Element p) {
        deleteDeclIfMatch(tree, p);
        return super.visitVariable(tree, p);
    }
    
    private void deleteDeclIfMatch(Tree tree, Element elementToFind) {
        if (workingCopy.getTreeUtilities().isSynthetic(getCurrentPath()))
            return ;
        
        Element el = workingCopy.getTrees().getElement(getCurrentPath());
        if (elementToFind.equals(el)) {
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            Tree newOne = null;
            if (parent.getKind() == Tree.Kind.CLASS) {
                newOne = make.removeClassMember((ClassTree) parent, tree);
            } else if (parent.getKind() == Tree.Kind.COMPILATION_UNIT) {
                newOne = make.removeCompUnitTypeDecl((CompilationUnitTree) parent, tree);
            }
            if (newOne!=null) {
                workingCopy.rewrite(parent,newOne);
            }
            addUsage(getCurrentPath());
        }
    }
}

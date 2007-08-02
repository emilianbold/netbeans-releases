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
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.List;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Jan Becicka
 */
public class FindSubtypesVisitor extends FindVisitor {

    private boolean recursive;
    public FindSubtypesVisitor(boolean recursive, WorkingCopy workingCopy) {
        super(workingCopy);
        this.recursive = recursive;
    }

    @Override
    public Tree visitClass(ClassTree node, Element elementToFind) {
        if (workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            return super.visitClass(node, elementToFind);
        }
        if (recursive) {
            if (isSubtype(getCurrentPath(), elementToFind)) {
                addUsage(getCurrentPath());
            }
        } else {
            TypeElement el = (TypeElement) workingCopy.getTrees().getElement(getCurrentPath());
            Types types = workingCopy.getTypes();
            if (el.getSuperclass()!=null && types.isSameType(types.erasure(el.getSuperclass()), types.erasure(elementToFind.asType())) || containsType(el.getInterfaces(), elementToFind.asType())) {
                addUsage(getCurrentPath());
            } 
        }
        return super.visitClass(node, elementToFind);
    }
    
    private boolean containsType(List<? extends TypeMirror> list, TypeMirror t) {
        Types types = workingCopy.getTypes();
        t = types.erasure(t);
        for (TypeMirror m:list) {
            if (types.isSameType(t, types.erasure(m))) {
                return true;
            };
        }
        return false;
    }
    
    protected boolean isSubtype(TreePath t1, Element t2) {
        Types types = workingCopy.getTypes();
        Trees trees = workingCopy.getTrees();
        return types.isSubtype(types.erasure(trees.getTypeMirror(t1)), types.erasure(t2.asType()));
    }

}

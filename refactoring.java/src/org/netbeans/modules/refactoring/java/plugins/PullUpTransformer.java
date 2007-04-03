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
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Jan Becicka
 */
public class PullUpTransformer extends SearchVisitor {

    Tree[] trees;
    public PullUpTransformer(WorkingCopy workingCopy, Tree trees[]) {
        super(workingCopy);
        this.trees = trees;
    }

    @Override
    public Tree visitClass(ClassTree tree, Element p) {
        Element el = workingCopy.getTrees().getElement(getCurrentPath());
        if (el.equals(p))
            return null;
        TypeMirror tm = el.asType();
        if (workingCopy.getTypes().isSubtype(tm, p.asType())) {
            ClassTree newOne = tree;
            for (int i = 0; i<trees.length; i++) {
                newOne = make.addClassMember(newOne, trees[i]);
            }
            workingCopy.rewrite(tree, newOne);
        }
        return super.visitClass(tree, p);
    }
    
}

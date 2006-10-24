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
import com.sun.source.util.TreePathScanner;
import java.util.ArrayList;
import java.util.Collection;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Jan Becicka
 */
public class FindSubtypesVisitor extends SearchVisitor {

    private boolean recursive;
    public FindSubtypesVisitor(boolean recursive, WorkingCopy workingCopy) {
        super(workingCopy);
        this.recursive = recursive;
    }

    @Override
    public Tree visitClass(ClassTree node, Element elementToFind) {
        if (recursive) {
            if (workingCopy.getTypes().isSubtype(workingCopy.getTrees().getTypeMirror(getCurrentPath()), elementToFind.asType())) {
                addUsage(node);
            }
        } else {
            TypeElement el = (TypeElement) workingCopy.getTrees().getElement(getCurrentPath());
            if (el.getSuperclass().equals(elementToFind.asType()) || el.getInterfaces().contains(elementToFind.asType())) {
                addUsage(node);
            } 
        }
        return super.visitClass(node, elementToFind);
    }

}

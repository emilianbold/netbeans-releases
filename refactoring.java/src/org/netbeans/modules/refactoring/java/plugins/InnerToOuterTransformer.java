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
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.api.MemberInfo;

/**
 *
 * @author Jan Becicka
 */
public class InnerToOuterTransformer extends SearchVisitor {

    public InnerToOuterTransformer(WorkingCopy workingCopy) {
        super(workingCopy);
    }
    
    public Tree visitNewClass(NewClassTree tree, Element p) {
        Element current = workingCopy.getTrees().getElement(getCurrentPath());
        if (current.equals(p)) {
        }
        return null;
    }

    
    @Override
    public Tree visitClass(ClassTree tree, Element p) {
//        // rename class if necessary (if the new name is different from the original name)
//        if (!refactoring.getClassName().equals(refactoring.getSourceType().getSimpleName())) {
//            refactoringElements.add(refactoring, new RenameClassElement());
//        }
//        
//        // add declaration of the field pointing to the original class (if user chooses to do so)
//        // and fix all references to the outer class elements
//        if (refactoring.getReferenceName() != null) {
//            refactoringElements.add(refactoring, new AddReferenceElement());
//            if (outerReferences!=null) {
//                refactoringElements.addAll(refactoring, outerReferences);
//            }
//            // find all explicit constructor invocations and fix them
//            for (Iterator it = refactoring.getSourceType().getSubClasses().iterator(); it.hasNext();) {
//                JavaClass cls = (JavaClass) it.next();
//                Object[] contents = cls.getContents().toArray();
//                boolean constructorVisited = false;
//                for (int i = 0; i < contents.length; i++) {
//                    if (contents[i] instanceof Constructor) {
//                        constructorVisited = true;
//                        Iterator stmts = ((Constructor) contents[i]).getBody().getStatements().iterator();
//                        Object firstStatement = stmts.hasNext() ? stmts.next() : null;
//                        if (firstStatement instanceof ConstructorInvocation) {
//                            ConstructorInvocation ci = (ConstructorInvocation) firstStatement;
//                            if (ci.isHasSuper()) {
//                                refactoringElements.add(refactoring, new AddInvocationArgumentElement(ci));
//                            }
//                        } else {
//                            refactoringElements.add(refactoring, new AddConstructorInvocationElement((Constructor) contents[i]));
//                        }
//                    }
//                }
//                if (!constructorVisited) {
//                    refactoringElements.add(refactoring, new AddConstructorElement(cls));
//                }
//            }
//        }
//        
//        JavaClass origOuter = (JavaClass) refactoring.getSourceType().refImmediateComposite();
//        // move the class to the outer level
//        Element parent = getFutureParent();
//        if (parent instanceof JavaClass) {
//            refactoringElements.add(refactoring, new MoveInnerToOuterElement((JavaClass) parent));
//        } else {
//            refactoringElements.add(refactoring, new MoveInnerToTopElement(refactoring.getSourceType(), refactoring.getClassName()));
//        }
//        
//        // fix all references to other inner classes of the outer class from the class being moved
//        if (multipartIds != null) {
//            refactoringElements.addAll(refactoring, multipartIds);
//        }
//
//        // fix all the references to the inner class (including imports and new class expressions)
//        ElementReference[] classReferences = (ElementReference[]) refactoring.getSourceType().getReferences().toArray(new ElementReference[0]);
//        for (int i = 0; i < classReferences.length; i++) {
//            refactoringElements.add(refactoring, new ChangeClassReferenceElement(classReferences[i], origOuter));
//        }
//        
        return null;
    }
    
}

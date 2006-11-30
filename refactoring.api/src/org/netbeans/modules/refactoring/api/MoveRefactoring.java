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

package org.netbeans.modules.refactoring.api;

/**
 * Move refactoring
 * This class is just holder for parameters of Move Refactgoring. 
 * Refactoring itself is implemented in plugins
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see AbstractRefactoring
 * @see RefactoringSession
 * @author Jan Becicka
 */
public final class MoveRefactoring extends AbstractRefactoring {

    private Object[] objectsToMove;
    private Object target;

    /**
     * @param objectsToMove Objects to be moved. E.g. FileObjects 
     * or TreePathHandles in case of Java Refactoring
     */
    public MoveRefactoring (Object ... objectsToMove) {
        this.objectsToMove = objectsToMove;
    }

    /** 
     * getter for moved objects
     */
    public Object[] getRefactoredObjects() {
        return objectsToMove;
    }

    /**
     * Target for moving
     * For instance Java Refactoring understands URL as target
     * @param target
     */
    public void setTarget(Object target) {
        this.target = target;
    }

    /**
     * Target for moving
     * For instance Java Refactoring understands URL as target
     * @return target
     */
    public Object getTarget() {
        return this.target;
    }
}

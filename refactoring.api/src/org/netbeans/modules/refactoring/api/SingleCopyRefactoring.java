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
 * Copy refactoring
 * This class is just holder for parameters of Single Copy Refactoring. 
 * Refactoring itself is implemented in plugins
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see AbstractRefactoring
 * @see RefactoringSession
 * @author Jan Becicka
 */
public class SingleCopyRefactoring extends AbstractRefactoring {

    private Object object;
    private Object target;
    private String newName;

    /**
     * @param objectToCopy Object to be copied.
     * E.g. FileObject in case of Java Refactoring
     */
    public SingleCopyRefactoring (Object objectToCopy) {
        this.object = objectToCopy;
    }

    /** 
     * getter for copied objects
     */
    public Object getRefactoredObject() {
        return object;
    }

    /**
     * Target where copy should be created
     * For instance Java Refactoring understands URL as target
     * @param target
     */
    public void setTarget(Object target) {
        this.target = target;
    }
    
    /**
     * Target where copy should be created
     * For instance Java Refactoring understands URL as target
     * @return target
     */
    public Object getTarget() {
        return target;
    }
    
    /**
     * getter for new name of copied file
     */
    public String getNewName() {
        return newName;
    }
    
    /**
     * setter for new name of copied file
     */
    public void setNewName(String newName) {
        this.newName = newName;
    }
}


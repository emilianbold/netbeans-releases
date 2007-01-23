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

package org.netbeans.modules.refactoring.spi.ui;

import java.io.IOException;

/**
 * @author Jan Becicka
 * This was historicaly intended to enhance RefactoringUI.
 * RefactoringUI must support "bypass" of refactoring for common 
 * operations Copy/Move/Rename for users to be able to do 
 * regular operation (Copy/Move/Rename) instead of refactoring operation
 * (Refactor | Copy, Refactor | Move, Refactor | Rename)
 * 
 * For instance UI for Java Rename Refactoring has checkbox 
 * [ ] Rename Without Refactoring
 *  
 * isRefactoringBypassRequired() should return true if and only if
 * this checkbox is checked.
 * doRefactoringBypass() implementation does only regular file rename
 * 
 */
public interface RefactoringUIBypass {
    
    /**
     * @return true if user want to bypass refactoring
     */
    boolean isRefactoringBypassRequired();
    
    /**
     * do regular operation, bypass refactoring
     */
    void doRefactoringBypass() throws IOException;
}

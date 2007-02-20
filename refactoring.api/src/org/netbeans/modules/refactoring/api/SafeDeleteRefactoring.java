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

import org.openide.util.Lookup;

/**
 * Refactoring to Safely Delete an element after checking its usages.
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see AbstractRefactoring
 * @see RefactoringSession
 * @author Bharath Ravikumar
 */
public final class SafeDeleteRefactoring extends AbstractRefactoring {
    private boolean checkInComments;

    /**
     * Creates a new instance of SafeDeleteRefactoring, passing the candidate
     * elements as parameter.
     * @param namedElements The elements to be safely deleted
     */
    public SafeDeleteRefactoring(Lookup namedElements) {
        super(namedElements);
    }
    
    /**
     * Indicates whether the usage of the elements will also be checked
     * in comments before deleting the elements
     * @return Returns the value of the field checkInComments
     */
    public boolean isCheckInComments() {
        return checkInComments;
    }
    
    /**
     * Sets whether or not the usage of the elements will be checked
     * in comments before deleting the elements
     * @param checkInComments Sets the checInComments field of this class
     */
    public void setCheckInComments(boolean checkInComments) {
        this.checkInComments = checkInComments;
    }
}

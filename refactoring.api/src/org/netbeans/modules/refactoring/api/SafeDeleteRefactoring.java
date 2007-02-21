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
 * @author Bharath Ravikumar, Jan Becicka
 */
public final class SafeDeleteRefactoring extends AbstractRefactoring {
    private boolean checkInComments;

    /**
     * Creates a new instance of SafeDeleteRefactoring, passing Lookup containing the candidate
     * elements as parameter.
     * 
     * Safe Delete Refactoring implementations currently understand following types:
     * <table border="1">
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Refactoring API (Default impl.)</td><td>FileObject</td><td>Does file delete</td></tr>
     *   <tr><td>Java Refactoring</td><td><ul><li>{@link org.openide.filesystems.FileObject}(s) with content type text/x-java (safly delete class)
     *                                <li>{@link org.netbeans.api.java.source.TreePathHandle} (class, field, method)</td>
     *                              <td>Check for usages and does refactoring inside .java files.</td></tr>
     * </table>
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

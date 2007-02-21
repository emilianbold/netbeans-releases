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
 * Refactoring used for renaming objects.
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see AbstractRefactoring
 * @see RefactoringSession
 * @author Jan Becicka, Martin Matula, Pavel Flaska, Daniel Prusa
 */
public final class RenameRefactoring extends AbstractRefactoring {
    private String newName = null;
    private boolean searchInComments;

    /**
     * Creates a new instance of RenameRefactoring.
     * Rename Refactoring implementations currently understand following types:
     * <table border="1">
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Refactoring API (Default impl.)</td><td>FileObject</td><td>Does file rename</td></tr>
     *   <tr><td>Java Refactoring</td><td><ul>
     *                                    <li>{@link org.openide.filesystems.FileObject}(s) with content type text/x-java (class rename)
     *                                    <li>{@link org.openide.filesystems.FileObject} (folder) folder rename 
     *                                    <li>{@link org.netbeans.api.java.source.TreePathHandle} (class, field, method rename)
     *                                    <li>{@link org.netbeans.api.fileinfo.NonRecursiveFolder} package rename</td>
     *                                    </ul>
     *                              <td>Does refactoring inside .java files. 
     *                               In case of FolderRename it also does corresponding file moves</td></tr>
     * </table>
     * @param item put object to rename into Lookup instance.
     */
    public RenameRefactoring(Lookup item) {
        super(item);
    }
    
    /**
     * Getter for property newName
     * @return Value of property newName
     */
    public String getNewName() {
        return newName;
    }
    
    /**
     * Setter for propety newName
     * @param newName New value of property newName
     */
    public void setNewName(String newName) {
        this.newName = newName;
    }
    
    /**
     * Getter for boolean property searchInComments
     * @return true if user selected search in comments
     */
    public boolean isSearchInComments() {
        return searchInComments;
    }

    /**
     * Setter for property searchInComments.
     * @param searchInComments New value of property searchInComments
     */
    public void setSearchInComments(boolean searchInComments) {
        this.searchInComments = searchInComments;
    }
}

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

import java.util.Collection;

/**
 * Refactoring used for renaming classes,  fields and methods.
 * It rename definition and references.
 * @author Jan Becicka, Martin Matula, Pavel Flaska, Daniel Prusa
 */
public final class RenameRefactoring extends AbstractRefactoring {
    private final Object item;
    private String newName = null;
    private boolean searchInComments;

    /**
     * Creates a new instance of RenameRefactoring
     * @param item Valid item is Method, JavaClass, Field, Parameter or LocalVariabe
     */
    public RenameRefactoring(Object item) {
        this.item = item;
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
     * Getter for property refactoredObject
     * @return refactoredObject (e.g. field, method, class)
     */
    public Object getRefactoredObject() {
        return item;
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

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
import org.netbeans.modules.refactoring.api.*;

/**
 * Where used query does not do any "real" refactring. I just perform Find Usages
 * @author Jan Becicka
 */
public final class WhereUsedQuery<T> extends AbstractRefactoring {
    private T  item;
    private boolean searchInComments;


    /**
     * Creates a new instance of WhereUsedQuery
     * @param item searched item
     */
    public WhereUsedQuery(T item) {
        this.item = item;
    }

    /**
     * Getter for searched item
     * @return searched item (typically JavaClass, Method or Field)
     */
    public final T getRefactoredObject() {
        return item;
    }    

    /**
     * Getter for boolean property searchInComments
     * @return Value of property searchInComments
     */
    public final boolean isSearchInComments() {
        return searchInComments;
    }

    /**
     * Getter for boolean property searchInComments
     * @return Value of property searchInComments
     */
    public final void setSearchInComments(boolean searchInComments) {
        this.searchInComments = searchInComments;
    }
    
}

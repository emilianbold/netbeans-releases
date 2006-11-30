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

import java.util.Hashtable;
import org.netbeans.modules.refactoring.api.*;

/**
 * Where used query does not do any "real" refactoring.
 * It just encapsulates parameters for Find Usages, which is implemented by
 * plugins.
 * Refactoring itself is implemented in plugins
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see AbstractRefactoring
 * @see RefactoringSession
 * @author Jan Becicka
 */
public final class WhereUsedQuery extends AbstractRefactoring {
    private Object item;
    public static final String SEARCH_IN_COMMENTS = "SEARCH_IN_COMMENTS";
    public static final String FIND_REFERENCES = "FIND_REFERENCES";
    
    /**
     * Creates a new instance of WhereUsedQuery
     * @param item searched item
     */
    public WhereUsedQuery(Object item) {
        this.item = item;
        putValue(FIND_REFERENCES, true);
    }

    /**
     * Getter for searched item
     * @return searched item. Java module understands TreePathHandle to be a parameter
     */
    public final Object getRefactoredObject() {
        return item;
    }    
    
    /**
     * Getter for searched item
     * @param item searched item. Java module understands TreePathHandle to be a parameter
     */
    public final void setRefactoredObject(Object item) {
        this.item = item;
    }
    
    private Hashtable hash = new Hashtable();
    
    /**
     * getter for various properties
     * @see WhereUsedQuery#SEARCH_IN_COMMENTS
     * @see WhereUsedQuery#FIND_REFERENCES
     */
    public final boolean getBooleanValue(Object key) {
        Object o = hash.get(key);
        if (o instanceof Boolean) 
            return (Boolean)o;
        return false;
    }
    
    /**
     * setter for various properties
     * @see WhereUsedQuery#SEARCH_IN_COMMENTS
     * @see WhereUsedQuery#FIND_REFERENCES
     */
    public final void putValue(Object key, Object value) {
        hash.put(key, (Boolean) value);
    }
}


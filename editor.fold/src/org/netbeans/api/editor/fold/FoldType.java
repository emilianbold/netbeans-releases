/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.editor.fold;

/**
 * Each {@link Fold} is identified by its fold type.
 * <br>
 * Each fold type presents a fold type acceptor as well
 * accepting just itself.
 *
 * <p>
 * As the <code>equals()</code> method is declared final
 * (delegating to <code>super</code>) the fold types
 * can directly be compared by using <code>==</code>
 * operator.
 *
 * <p>
 * Fold providers should export all the fold types
 * that they provide in their APIs so that the
 * clients can use these fold types in operations
 * with the fold hierarchy.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldType {
    
    private final String description;
    
    /**
     * Construct fold type with the given description.
     *
     * @param description textual description of the fold type.
     *  Two fold types with the same description are not equal.
     */
    public FoldType(String description) {
        this.description = description;
    }
    
    public boolean accepts(FoldType type) {
        return (type == this);
    }
    
    public String toString() {
        return description;
    }

}

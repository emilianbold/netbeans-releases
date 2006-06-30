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

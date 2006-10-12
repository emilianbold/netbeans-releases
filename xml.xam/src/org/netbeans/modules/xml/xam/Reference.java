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

package org.netbeans.modules.xml.xam;

/**
 * Represents reference to a component.  On writing, this indirection help serialize
 * the referenced component as an attribute string value.  On reading, the referenced
 * can be resolved on demand.
 * <p>
 * Note: Client code should always check for brokeness before access the referenced.
 *
 * @author Chris Webster
 * @author Rico Cruz
 * @author Nam Nguyen
 */

public interface Reference<T extends Referenceable> {
    /**
     * @return the referenced component. May return null if
     * #isBroken() returns true;
     */
    T get();
    
    /**
     * Returns type of the referenced.
     */
    Class<T> getType();

    /**
     * Returns true if the reference cannot be resolved in the current document
     */
    boolean isBroken();
    
    /**
     * Returns true if this reference refers to target.
     * <p>
     * Note: In some implementation, this method could be more efficient than 
     * invoking #get() for direct checking.
     */
    boolean references(T target);
    
    /**
     * @return string to use in persiting the reference as attribute string value
     * of the containing component
     */
    String getRefString();
}

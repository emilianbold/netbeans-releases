/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.validation.constraints;

import java.util.Collection;

/**
 * Constraint is an <code>Interface</code> needed to perform a Validation.
 * User can  define a new type of Validation by implementing this 
 * <code>Interface</code>.
 * Method <code>match</code> defines the Validation logic. This method
 * returns an empty collection if the value being validated is valid;
 * else it returns a <code>Collection</code> with a {@link ConstraintFailure}
 * object in it.
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public interface Constraint {
    /**
     * Validates the given value against this <code>Constraint</code>.
     * 
     * @param value the value to be validated
     * @param name the element name, value of which is being validated.
     *
     * @return <code>Collection</code> the Collection of failure Objects.
     * Collection is empty if there are no failures.
     */
    public Collection match(String value, String name);
}

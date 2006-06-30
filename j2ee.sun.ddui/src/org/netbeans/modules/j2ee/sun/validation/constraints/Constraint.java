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

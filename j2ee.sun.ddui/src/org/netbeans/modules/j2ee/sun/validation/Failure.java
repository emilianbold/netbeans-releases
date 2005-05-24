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

package org.netbeans.modules.j2ee.sun.validation;

/**
 * Failure is an <code>Interface</code> used to represent
 * validation failure.
 * <code>{@link ConstraintFailure}</code> object implements
 * this <code>Interface</code>.
 * 
 * @see ConstraintFailure
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public interface Failure {
    /**
    * Returns failure message for this failure.
    */
    public String failureMessage();
}

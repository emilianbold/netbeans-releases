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

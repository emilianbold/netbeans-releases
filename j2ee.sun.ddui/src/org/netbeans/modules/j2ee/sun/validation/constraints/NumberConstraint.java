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

import java.util.ArrayList;
import java.util.Collection;

import org.netbeans.modules.j2ee.sun.validation.constraints.ConstraintFailure;
import org.netbeans.modules.j2ee.sun.validation.util.BundleReader;

/**
 * NumberConstraint  is a {@link Constraint} to validate numbers.
 * It implements <code>Constraint</code> interface and extends
 * {@link ConstraintUtils} class.
 * <code>match</code> method of this object returns empty
 * <code>Collection</code> if the value being validated is a number; else it 
 * returns a <code>Collection</code> with a {@link ConstraintFailure} object in 
 * it. 
 * <code>ConstraintUtils</code> class provides utility methods for formating 
 * failure messages and a <code>print<method> method to print this object.
 *  
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class NumberConstraint extends ConstraintUtils
                implements Constraint {
    
    /** Creates a new instance of <code>NumberConstraint</code>. */
    public NumberConstraint() {
    }


    /**
     * Validates the given value against this <code>Constraint</code>.
     * 
     * @param value the value to be validated.
     * @param name the element name, value of which is being validated.
     * It is used only in case of <code>Constraint</code> failure, to construct
     * the failure message.
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty if 
     * there are no failures. This method will fail, if the given value
     * is non-numeric.
     */
    public Collection match(String value, String name) {
        ArrayList failed_constrained_list = new ArrayList();
        if(null != value){
            try {
                new Double(value);
                return failed_constrained_list;
            } catch(NumberFormatException e) {
                String failureMessage = formatFailureMessage(toString(), value,
                    name);

                failed_constrained_list.add(new ConstraintFailure(toString(),
                    value, name, failureMessage, BundleReader.getValue(
                        "MSG_NumberConstraint_Failure")));              //NOI18N
            }
        }
        return failed_constrained_list;
    }
}

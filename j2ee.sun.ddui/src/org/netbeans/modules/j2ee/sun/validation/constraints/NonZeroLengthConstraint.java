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

import java.util.ArrayList;
import java.util.Collection;

import org.netbeans.modules.j2ee.sun.validation.constraints.ConstraintFailure;
import org.netbeans.modules.j2ee.sun.validation.util.BundleReader;

/**
 * NonZeroLengthConstraint  is a {@link Constraint} to validate non zero
 * length value. It implements <code>Constraint</code> interface and extends 
 * {@link ConstraintUtils} class. 
 * <code>match</code> method of this object returns empty 
 * <code>Collection</code> if the value being validated is of non zero length;
 * else it returns a <code>Collection</code> with a {@link ConstraintFailure} 
 * object in it.
 * <code>ConstraintUtils</code> class provides utility methods for formating 
 * failure messages and a <code>print<method> method to print this object.
 *  
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class NonZeroLengthConstraint extends ConstraintUtils
                implements Constraint {
    
    /** Creates a new instance of 
     * <code>NonZeroLengthConstraintConstraint</code>.
     */
    public NonZeroLengthConstraint() {
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
     * there are no failures. Failure can occur if value being validated
     * is of zero length.
     */
    public Collection match(String value, String name) {
        Collection failed_constrained_list = new ArrayList();
        if (value != null){
            if(value.length() == 0){
                String failureMessage = formatFailureMessage(toString(), name);
                failed_constrained_list.add(new ConstraintFailure(toString(),
                    value, name, failureMessage, BundleReader.getValue(
                        "MSG_NonZeroLengthConstraint_Failure")));       //NOI18N
            }
        }
        return failed_constrained_list;
    }
}

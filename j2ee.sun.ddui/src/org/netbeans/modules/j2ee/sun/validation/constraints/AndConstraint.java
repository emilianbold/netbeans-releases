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

/**
 * AndConstraint  is a logical <code>And</code> {@link Constraint}. It validates
 * the given value by performing logical <code>And</code> of validations of the
 * value against its constituent <code>leftConstraint</code> and 
 * <code>rightConstraint</code>.
 * <p>
 * It implements <code>Constraint</code> interface and extends 
 * {@link ConstraintUtils} class. 
 * <code>match</code> method of this object returns empty collection if the
 * value being validated satisfies both the <code>leftConstraint</code> and the
 * <code>rightConstraint</code>; else it returns a <code>Collection</code>
 * with {@link ConstraintFailure} objects in it for the failed
 * <code>Constraints</code>.
 * <code>ConstraintUtils</code> class provides utility methods for formating 
 * failure messages and a <code>print<method> method to print this object.
 *  
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class AndConstraint extends ConstraintUtils implements Constraint {

    /**
     * The left <code>Constraint</code> of this <code>AndCosntraint</code>
     * This <code>AndConstraint</code> is logical <code>AND</code> of its left 
     * <code>Constraint</code> and right <code>Constraint</code>.
     */
    private Constraint leftConstraint = null;


    /**
     * The right <code>Constraint</code> of this <code>AndCosntraint</code>
     * This <code>AndConstraint</code> is logical <code>AND</code> of its left 
     * <code>Constraint</code> and right <code>Constraint</code>.
     */
    private Constraint rightConstraint = null;


    /** Creates a new instance of <code>AndConstraint</code> */
    public AndConstraint() {
        Constraint leftConstraint = null;
        Constraint rightConstraint = null;
    }


    /** Creates a new instance of <code>AndConstraint</code> */
    public AndConstraint(Constraint leftConstraint,
            Constraint rightConstraint){
        this.leftConstraint = leftConstraint;
        this.rightConstraint = rightConstraint;
    }


    /**
     * Validates the given value against this <code>Constraint</code>.
     * 
     * @param value the value to be validated
     * @param name the element name, value of which is being validated.
     * It is used only in case of <code>Constraint</code> failure, to construct
     * the failure message.
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty if 
     * there are no failures.
     */
    public Collection match(String value, String name) {
        Collection failed_constrained_list = new ArrayList();
        failed_constrained_list.addAll(leftConstraint.match(value, name));
        failed_constrained_list.addAll(rightConstraint.match(value, name));
        return failed_constrained_list;
    }


    /**
     * Sets the given <code>Constraint</code> as the
     * <code>leftConstraint</code> of this object.
     * 
     * @param constraint the <code>Constraint</code> to be
     * set as <code>leftConstraint</code> of this object
     */
    public void setLeftConstraint(Constraint constraint){
        leftConstraint = constraint;
    }


    /**
     * Sets the given <code>Constraint</code> as
     * the <code>rightConstraint</code> of this object.
     * 
     * @param constraint the <code>Constraint</code> to be
     * set as <code>rightConstraint</code> of this object
     */
    public void setRightConstraint(Constraint constraint){
        rightConstraint = constraint;
    }


    /**
     * Prints this <code>Constraint</code>.
     */
    public void print() {
        super.print();
        ((ConstraintUtils)leftConstraint).print();
        ((ConstraintUtils)rightConstraint).print();
    }
}

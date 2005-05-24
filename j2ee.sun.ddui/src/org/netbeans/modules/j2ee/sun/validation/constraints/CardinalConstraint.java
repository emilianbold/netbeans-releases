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
import java.text.MessageFormat;

import org.netbeans.modules.j2ee.sun.validation.Constants;
import org.netbeans.modules.j2ee.sun.validation.constraints.ConstraintFailure;
import org.netbeans.modules.j2ee.sun.validation.util.BundleReader;

/**
 * CardinalConstraint  is a {@link Constraint} to validate the structure.
 * It implements <code>Constraint</code> interface and extends 
 * {@link ConstraintUtils} class. 
 * <code>match</code> method of this object returns empty 
 * <code>Collection</code> if the value being validated conforms to the 
 * structure specified by this <code>Constraint</code>; else it returns a 
 * <code>Collection</code> with a {@link ConstraintFailure} object in it.
 * <code>ConstraintUtils</code> class provides utility methods for formating 
 * failure messages and a <code>print<method> method to print this object.
 *  
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class CardinalConstraint extends ConstraintUtils
                implements Constraint{
 
    /**
     * The structure represented by this <code>Constraint</code>.
     */
    private int cardinal =  Constants.MANDATORY_ELEMENT;


    /** Creates a new instance of CardinalConstraint */
    public CardinalConstraint() {
        cardinal = Constants.MANDATORY_ELEMENT;
    }


    /** Creates a new instance of <code>CardinalConstraint</code>. */
    public CardinalConstraint(int cardinal) {
        this.cardinal = cardinal;
    }


    /** Creates a new instance of <code>CardinalConstraint</code>. */
    public CardinalConstraint(String cardinal) {
        try {
           this.cardinal = Integer.parseInt(cardinal);
        } catch(NumberFormatException e) {
            String format = 
                BundleReader.getValue("Error_failed_to_create");        //NOI18N
            Object[] arguments = 
                new Object[]{"CardinalConstraint"};                     //NOI18N
            System.out.println(MessageFormat.format(format, arguments));
        }
    }


    /**
     * Validates the given value against this <code>Constraint</code>.
     * 
     * @param value the value to be validated.
     * This value must be of appropiate type, that is , either an Object or
     * an array of Objects, in consistence with the Cardinal represented
     * by this Object.
     * @param name the element name, value of which is being validated.
     * It is used only in case of <code>Constraint</code> failure, to
     * construct the failure message.
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty 
     * if there are no failures. This <code>Constraint</code> will
     * fail if the value does not conform to the structure represented
     * by this object.
     */
    public Collection match(String value, String name){
        return match((Object)value, name);
    }


    /**
     * Validates the given value against this <code>Constraint</code>.
     * 
     * @param value the value to be validated.
     * This value must be of appropriate type, that is , either an Object or
     * an array of Objects, in consistence with the Cardinal represented
     * by this Object.
     * @param name the element name, value of which is being validated.
     * It is used only in case of <code>Constraint</code> failure, to
     * construct the failure message.
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty 
     * if there are no failures. This method will fail if the value does
     * not conform to the structure represented by this object.
     */
    public Collection match(Object value, String name) {
        Collection failed_constrained_list = 
                new ArrayList();

        if(Constants.MANDATORY_ARRAY == cardinal) {
            Object[]  values = null;
            try {
                values =  (Object[])value;
            } catch(ClassCastException exception){
                System.out.println(BundleReader.getValue(
                   "Error_expects_argument_one_to_be_an_array_of_Objects"));//NOI18N
            }

            if((null == values) || (values.length  <  1)){
                String failureMessage = formatFailureMessage(toString(), name);
                failed_constrained_list.add(new ConstraintFailure(toString(),
                    null, name, failureMessage, BundleReader.getValue(
                        "MSG_CardinalConstraint_Failure")));            //NOI18N
            }
        } else {
            if(Constants.MANDATORY_ELEMENT == cardinal){
                if (null == value){
                    String failureMessage =
                        formatFailureMessage(toString(), name);
                    failed_constrained_list.add(
                        new ConstraintFailure(toString(), null, name,
                            failureMessage, BundleReader.getValue(
                                "MSG_CardinalConstraint_Failure")));    //NOI18N
                }
            }
        }
        return failed_constrained_list;
    }


    /**
     * Sets the given cardinal as the structure
     * represented by this object.
     * 
     * @param cardinal the cardinal to be set
     * as the structure represented by this object.
     */
    public void setCardinal(int cardinal){
        this.cardinal = cardinal;
    }


    /**
     * Sets the given cardinal as the structure
     * represented by this object.
     * 
     * @param cardinal the cardinal to be set
     * as the structure represented by this object.
     */
    public void setCardinal(String cardinal){
        try {
           this.cardinal = Integer.parseInt(cardinal);
        } catch(NumberFormatException e) {
            String format = 
                BundleReader.getValue("Error_failed_to_set");           //NOI18N
            Object[] arguments = 
                new Object[]{this.toString(), "Cardinal"};              //NOI18N

            System.out.println(MessageFormat.format(format, arguments));
        }
    }


    /**
     * Prints this <code>Constraint</code>.
     */
    public void print() {
        super.print();
        String format = BundleReader.getValue("Name_Value_Pair_Format");//NOI18N
        Object[] arguments = 
            new Object[]{"Cardinal", String.valueOf(cardinal)};         //NOI18N
        System.out.println(MessageFormat.format(format, arguments));
    }
}

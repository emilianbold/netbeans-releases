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
import java.util.Iterator;
import java.text.MessageFormat;

import org.netbeans.modules.j2ee.sun.validation.constraints.ConstraintFailure;
import org.netbeans.modules.j2ee.sun.validation.util.BundleReader;


/**
 * InConstraint  is a {@link Constraint} to validate an Enumeration value.
 * It implements <code>Constraint</code> interface and extends
 * {@link ConstraintUtils} class.
 * <code>match</code> method of this object returns empty 
 * <code>Collection</code> if the value being validated belongs to the 
 * enumeration represented by this object; else it returns a 
 * <code>Collection</code> with a {@link ConstraintFailure} object in it. 
 * <code>ConstraintUtils</code> class provides utility methods for formating 
 * failure messages and a <code>print<method> method to print this object.
 *  
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class InConstraint extends ConstraintUtils implements Constraint {

    /**
     * An enumeration represented by this <code>Constraint</code>.
     */
    private Collection enumeration = null;


    /** Creates a new instance of <code>InConstraint</code>. */
    public InConstraint() {
        enumeration = new ArrayList();
    }


    /** Creates a new instance of <code>InConstraint</code>. */
    public InConstraint(Collection enumeration) {
        this.enumeration = enumeration;
    }


    /** Creates a new instance of <code>InConstraint</code>. */
    public InConstraint(String[] enumeration) {
        this.enumeration = new ArrayList();
        int size = enumeration.length;
        for(int i=0; i<size; i++) {
            this.enumeration.add(enumeration[i]);
        }
    }


    /**
     * Validates the given value against this <code>Constraint</code>.
     * 
     * @param value the value to be validated
     * @param name the element name, value of which is being validated.
     * It is used only in case of <code>Constraint</code> failure, to 
     * construct the failure message.
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty 
     * if there are no failures. This method will fail, only for the values
     * that does not belong to an enumeration represented by this object.
     */
    public java.util.Collection match(String value, String name) {
        Collection failed_constrained_list = new ArrayList();
        if((value != null) && (value.length() != 0)) {
            if(!enumeration.contains(value)){
                String failureMessage = formatFailureMessage(toString(),
                    value, name);

                String format = BundleReader.getValue(
                    "MSG_InConstraint_Failure");                        //NOI18N
                String set = "";                                        //NOI18N
                Iterator iterator = enumeration.iterator();
                String member;
                while(iterator.hasNext()){
                    member = (String)iterator.next();
                    set = set + " " + member;                           //NOI18N
                }

                Object[] arguments = new Object[]{set};

                String genericFailureMessage = 
                    MessageFormat.format(format, arguments);

                failed_constrained_list.add(new ConstraintFailure(toString(),
                    value, name, failureMessage, genericFailureMessage));
            }
        }
        return failed_constrained_list;
    }


    /**
     * Sets the given <code>Collection</code> as the enumeration
     * represented by this object.
     * 
     * @param enumeration the <code>Collection</code> to be set
     * as the enumeration represented by this object.
     */
    public void setEnumerationValue(Collection enumeration){
        this.enumeration = enumeration;
    }


    /**
     * Adds the given <code>value</code> to the enumeration
     * represented by this object.
     * 
     * @param value the value to be added to the enumeration
     * represented by this object.
     */
    public void setEnumerationValue(String value){
        enumeration.add(value);
    }


    /**
     * Prints this <code>Constraint</code>.
     */
    void print() {
        super.print();
        String format = BundleReader.getValue("Name_Value_Pair_Format");//NOI18N
        Iterator iterator = enumeration.iterator();
        String values = "";
        while(iterator.hasNext()){
            values = values + "  " + (String)iterator.next();           //NOI18N
        }

        if(values != null){
            Object[] arguments = 
                new Object[]{"Enumeration Values", values};             //NOI18N
            System.out.println(MessageFormat.format(format, arguments));
        }
    }
}

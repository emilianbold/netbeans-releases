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
/*
 * ValidationSupport.java
 *
 * Created on November 11, 2003, 10:59 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.validation.constraints.ConstraintFailure;
import org.netbeans.modules.j2ee.sun.validation.ValidationManager;
import org.netbeans.modules.j2ee.sun.validation.ValidationManagerFactory;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class ValidationSupport {

    static final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle"); // NOI18N
    
    private ValidationManager validationManager;


    /** Creates a new instance of ValidationSupport */
    public ValidationSupport() {
        ValidationManagerFactory validationManagerFactory = 
            new ValidationManagerFactory();
        validationManager = validationManagerFactory.getValidationManager();
    }


    public Collection validate(String value, String xpath, String label){
        ArrayList errors = new ArrayList();

        Collection failures = 
            validationManager.validateIndividualProperty(value,xpath,label);

        if(failures != null){
            Iterator iterator = failures.iterator();
            ConstraintFailure failure;
            String error;

            while(iterator.hasNext()){
                Object object  = iterator.next();

                if(object instanceof ConstraintFailure){
                    failure = (ConstraintFailure)object;
                    error = failure.getName() + ": " +                  //NOI18N
                        failure.getGenericfailureMessage();             
                    errors.add(error);
                }
            }
       }
        return errors;
    }


    /**
     * Returns true if the given xpath represents mandatory field
     * 
     * @param xpath the given xpath.
     *
     * @return <code>boolean</code> <code>true</code> if the given xpath is 
     * of mandatory field; else returns <code>false</code>
     */
    public boolean iSRequiredProperty(String xpath){
        boolean isRequried = false;
        String property = ""; //NOI18N
        java.util.Collection errors = validate(property, xpath, null);
        if(!errors.isEmpty()){
            isRequried = true;
        }
        return isRequried;
    }


    /**
     * Returns marked-label for the given label. Marked labels are used in case
     * of madatory fields.
     * 
     * @param label the given label
     *
     * @return <code>String</code> the marked label. Marked label is formed by 
     * appending "*  " to the given field.
     */
    public String getMarkedLabel(String label){
        String format = bundle.getString("FMT_Required_Field_Label");   //NOI18N
        String requiedMark = bundle.getString("LBL_RequiredMark");      //NOI18N
        Object[] arguments = new Object[]{requiedMark, label};  
        return MessageFormat.format(format, arguments);
    }
}

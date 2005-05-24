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

import org.netbeans.modules.j2ee.sun.validation.util.ObjectFactory;
import org.netbeans.modules.j2ee.sun.validation.ValidationManager;


/**
 * ValidationManagerFactory is a factory to create {@link ValidationManager} 
 * objects. Creates <code>ValidationManager</code> based on the given Validation
 * File. 
 *
 * @see ValidationManager
 * 
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public class ValidationManagerFactory {
    /* A class implementation comment can go here. */

    /** Creates a new instance of <code>ValidationManagerFactory</code> */
    public ValidationManagerFactory(){
    }


    /**
     * Creates default Validation Manager.
     * Default Validation Manager is created using default Validation File
     *
     * @return <code>ValidationManager</code> the default Validation Manager.
     * 
     */
    public ValidationManager getValidationManager(){
        return (ValidationManager) ObjectFactory.newInstance(
            "org.netbeans.modules.j2ee.sun.validation." +               //NOI18N
                "ValidationManager");                                   //NOI18N
    }


    /**
     * Creates Validation Manager based on the given Validation File.
     *
     * @param validationFile the Validation File. Validation File specifies
     * Validation rules(which Constraints to apply to which elements).
     * 
     * @return <code>ValidationManager</code> the Validation Manager based on
     * the given Validation File.
     */
    public ValidationManager getValidationManager(String validationFile){
        return (ValidationManager) ObjectFactory.newInstance(
            "org.netbeans.modules.j2ee.sun.validation." +               //NOI18N
                "ValidationManager", validationFile);                   //NOI18N
    }
}

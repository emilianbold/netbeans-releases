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

package org.netbeans.modules.j2ee.sun.validation.util;

import java.lang.reflect.Constructor;

/**
 * This class is a generic Factory that employes Java reflection to
 * create Objects.
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public class ObjectFactory {
    /* A class implementation comment can go here. */

    /** Create an instance of the class with the specified name by calling the
      * no-argument constructor.
     */
    public static Object newInstance(String className){

        Utils utils = new Utils();
        return utils.createObject(className);
    }


    /** Create an instance of the class with the specified name by calling the
      * a constructor that takes an String.
     */
    public static Object newInstance(String className, String argument){
        Class classObject = null;
        Utils utils = new Utils();

        Class[] argumentTypes = new Class[] {String.class};
        Constructor constructor =
            utils.getConstructor(className, argumentTypes);

        Object[] argumentValues = new Object[] {argument};

        return utils.createObject(constructor, argumentValues);
    }


    /** Create an instance of the class with the specified name by calling the
      * a constructor that takes an String.
     */
    public static Object newInstance(String className, Object argument){
        Class classObject = null;
        Utils utils = new Utils();

        Class[] argumentTypes = new Class[] {Object.class};
        Constructor constructor =
            utils.getConstructor(className, argumentTypes);

        Object[] argumentValues = new Object[] {argument};

        return utils.createObject(constructor, argumentValues);
    }    
}

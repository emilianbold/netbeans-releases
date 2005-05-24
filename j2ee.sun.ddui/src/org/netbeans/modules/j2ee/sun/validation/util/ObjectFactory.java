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

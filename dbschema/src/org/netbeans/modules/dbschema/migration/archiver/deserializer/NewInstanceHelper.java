/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.migration.archiver.deserializer;

/**
 *
 * @author  Administrator
 * @version 
 */
public class NewInstanceHelper extends Object {

    /** Creates new NewInstanceHelper */
    public NewInstanceHelper() {
    }

    public static Object newInstance(String className, Object parentObject)
    {
        Object lReturnObj = null;
        // The following method is used to help init classes that do not have a
        // default constructor and hence using relaection to create them fails
        
        if ( className.equals("java.beans.PropertyChangeSupport") )
            lReturnObj = new java.beans.PropertyChangeSupport(parentObject);
        return lReturnObj;
    }
    
}

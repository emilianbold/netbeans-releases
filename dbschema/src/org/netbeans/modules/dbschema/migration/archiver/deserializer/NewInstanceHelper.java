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

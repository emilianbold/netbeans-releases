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

package org.netbeans.modules.db.explorer;

/**
 * Provides access to the api.DatabaseConnection constructor by the
 * {@link #createDatabaseConnection} method.
 * 
 * 
 * @author Andrei Badea
 */
public abstract class DatabaseConnectionAccessor {
    
    public static DatabaseConnectionAccessor DEFAULT;
    
    static {
        Class c = org.netbeans.api.db.explorer.DatabaseConnection.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException e) {
            assert false : e;
        }
    }
    
    public abstract org.netbeans.api.db.explorer.DatabaseConnection createDatabaseConnection(DatabaseConnection conn);
}

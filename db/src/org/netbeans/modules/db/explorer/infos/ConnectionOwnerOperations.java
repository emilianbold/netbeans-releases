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

package org.netbeans.modules.db.explorer.infos;

import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.lib.ddl.DBConnection;

/**
* Interface of driver-related nodes.
* @author Slavek Psenicka
*/
public interface ConnectionOwnerOperations
{
    /** Add connection operation
    * @param drv Driver to add (null allowed)
    */
    public void addConnection(DBConnection con)
    throws DatabaseException;
}

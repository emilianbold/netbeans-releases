/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.enterprise.modules.db.explorer.infos;

import com.netbeans.ddl.DBConnection;
import com.netbeans.enterprise.modules.db.*;
import com.netbeans.enterprise.modules.db.explorer.DatabaseDriver;
import com.netbeans.enterprise.modules.db.explorer.nodes.DatabaseNode;

/** 
* Interface of driver-related nodes.
* @author Slavek Psenicka
*/
public interface DeleteOperations
{
	public void delete()
	throws DatabaseException;
}

/*
* <<Log>>
*  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
* $
*/

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

import com.netbeans.enterprise.modules.db.*;
import com.netbeans.enterprise.modules.db.explorer.DatabaseDriver;

/** 
* Interface of driver-related nodes.
* @author Slavek Psenicka
*/
public interface TableOwnerOperations
{
	/** Add driver operation 
	* @param drv Driver to add
	*/
	public void createTable(DatabaseNodeInfo tinfo) 
	throws DatabaseException;

	/** Remove driver operation 
	* @param drv Driver to remove
	* @param node Owning node info
	*/
	public void dropTable(DatabaseNodeInfo tinfo) 
	throws DatabaseException;

	public void dropIndex(DatabaseNodeInfo tinfo) 
	throws DatabaseException;
}

/*
* <<Log>>
*  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
* $
*/

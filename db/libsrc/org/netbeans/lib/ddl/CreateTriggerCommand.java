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

package com.netbeans.ddl;

import java.sql.*;
import com.netbeans.ddl.*;

/** 
* Interface of create trigger action command. 
*
* @author Slavek Psenicka
*/
public interface CreateTriggerCommand extends DDLCommand 
{	
	/** Returns host table */
	public String getTableName();
	
	/** Sets the host table 
	* @param tablename Name of table.
	*/
	public void setTableName(String tablename);
}

/*
* <<Log>>
*  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/

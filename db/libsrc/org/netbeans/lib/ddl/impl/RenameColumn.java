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

package com.netbeans.ddl.impl;

import java.util.*;
import com.netbeans.ddl.*;
import com.netbeans.ddl.impl.*;

/** 
* Rename table command. Encapsulates name and new name of table.
*
* @author Slavek Psenicka
*/

public class RenameColumn extends ColumnCommand 
{
	/** New name */
	private String newname;

static final long serialVersionUID =7150074600789999024L;
	/** Remove simple column 
	* @param name Column name
	*/
	public TableColumn renameColumn(String name, String nname)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		newname = nname;
		return specifyColumn(TableColumn.COLUMN, name, Specification.RENAME_COLUMN);
	}
	
	/** Returns properties of command:
	* object.newname	New name of table
	*/
	public Map getCommandProperties()
	throws DDLException
	{
		Map args = super.getCommandProperties();
		args.put("column.newname", newname);			
		return args;	
	}
}

/*
* <<Log>>
*  2    Gandalf   1.1         8/17/99  Ian Formanek    Generated serial version 
*       UID
*  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
* $
*/

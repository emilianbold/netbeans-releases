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
* Remove column command.
*
* @author Slavek Psenicka
*/

public class RemoveColumn extends ColumnCommand 
{
	/** Remove simple column 
	* @param name Column name
	*/
	public TableColumn removeColumn(String name)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		return specifyColumn(TableColumn.COLUMN, name, Specification.REMOVE_COLUMN);
	}

	/** Remove unique column 
	* @param name Column name
	*/
	public TableColumn removeUniqueColumn(String name)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		TableColumn col = specifyColumn(TableColumn.UNIQUE, name, Specification.REMOVE_COLUMN);
		return col;
	}	

	/** Remove primary key 
	* @param name Column name
	*/
	public TableColumn removePrimaryKeyColumn(String name)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		TableColumn col = specifyColumn(TableColumn.PRIMARY_KEY, name, Specification.REMOVE_COLUMN);
		return col;
	}	

	/** Remove foreign key 
	* @param name Column name
	*/
	public TableColumn removeForeignKeyColumn(String name)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		TableColumn col = specifyColumn(TableColumn.FOREIGN_KEY, name, Specification.REMOVE_COLUMN);
		return col;
	}	

	/** Remove check 
	* @param name Check name
	*/
	public TableColumn removeCheckColumn(String name)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		TableColumn col = specifyColumn(TableColumn.CHECK, name, Specification.REMOVE_COLUMN);
		return col;
	}	
}

/*
* <<Log>>
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/

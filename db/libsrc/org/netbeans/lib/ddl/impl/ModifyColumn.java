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
* Interface of database action command. Instances should remember connection 
* information of DBSpec and use it in execute() method. This is a base interface
* used heavily for sub-interfacing (it is not subclassing :)
*
* @author Slavek Psenicka
*/

public class ModifyColumn extends ColumnCommand 
{
	public AbstractTableColumn specifyColumn(String type, String name)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		return specifyColumn(type, name, Specification.MODIFY_COLUMN);
	}

	public AbstractTableColumn specifyColumn(String name)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		return specifyColumn(TableColumn.COLUMN, name, Specification.MODIFY_COLUMN);
	}
}

/*
* <<Log>>
*  1    Gandalf   1.0         5/14/99  Slavek Psenicka 
* $
*/

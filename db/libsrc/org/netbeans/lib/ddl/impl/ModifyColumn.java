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
* information of DatabaseSpecification and use it in execute() method. This is a base interface
* used heavily for sub-interfacing (it is not subclassing :)
*
* @author Slavek Psenicka
*/

public class ModifyColumn extends ColumnCommand 
{
static final long serialVersionUID =2870433389508513811L;
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
*  3    Gandalf   1.2         9/10/99  Slavek Psenicka 
*  2    Gandalf   1.1         8/17/99  Ian Formanek    Generated serial version 
*       UID
*  1    Gandalf   1.0         5/14/99  Slavek Psenicka 
* $
*/

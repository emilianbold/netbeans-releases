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

public class AddColumn extends ColumnListCommand 
{
static final long serialVersionUID =8822373040877885121L;
	public AbstractTableColumn specifyColumn(String type, String name)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		return specifyColumn(type, name, Specification.ADD_COLUMN);
	}

	public AbstractTableColumn createColumn(String name)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		return specifyColumn(TableColumn.COLUMN, name, Specification.ADD_COLUMN);
	}

	public TableColumn createUniqueColumn(String name)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		TableColumn col = specifyColumn(TableColumn.UNIQUE, name, Specification.ADD_COLUMN);
		col.setObjectName(name+"_UQ");
		return col;
	}	

	public TableColumn createPrimaryKeyColumn(String name)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		TableColumn col = specifyColumn(TableColumn.PRIMARY_KEY, name, Specification.ADD_COLUMN);
		col.setObjectName(name+"_PK");
		return col;
	}	

	public TableColumn createCheckColumn(String name, String expression)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		TableColumn col = specifyColumn(TableColumn.CHECK, name, Specification.ADD_COLUMN);
		col.setObjectName(name+"_CH");
		col.setCheckCondition(expression);
		return col;
	}	

	public TableColumn createCheckConstraint(String name, String expression)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		TableColumn col = specifyColumn(TableColumn.CHECK_CONSTRAINT, name, Specification.ADD_COLUMN);
		col.setObjectName(name+"_CH");
		col.setCheckCondition(expression);
		return col;
	}	
}

/*
* <<Log>>
*  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         9/10/99  Slavek Psenicka 
*  4    Gandalf   1.3         8/17/99  Ian Formanek    Generated serial version 
*       UID
*  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/

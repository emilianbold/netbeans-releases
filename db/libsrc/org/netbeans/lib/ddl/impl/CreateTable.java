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

import java.text.ParseException;
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
public class CreateTable extends ColumnListCommand 
{
	public TableColumn createColumn(String name)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		return specifyColumn(TableColumn.COLUMN, name, Specification.CREATE_TABLE);
	}

	public TableColumn createColumn(String type, String name)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		return specifyColumn(type, name, Specification.CREATE_TABLE);
	}

	public TableColumn createUniqueColumn(String name)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		TableColumn col = specifyColumn(TableColumn.UNIQUE, name, Specification.CREATE_TABLE);
		col.setObjectName(name+"_UQ");
		return col;
	}	

	public TableColumn createPrimaryKeyColumn(String name)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		TableColumn col = specifyColumn(TableColumn.PRIMARY_KEY, name, Specification.CREATE_TABLE);
		col.setObjectName(name+"_PK");
		return col;
	}	

	public TableColumn createForeignKeyColumn(String name, String rtablename, String rcolumnname)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		TableColumn col = specifyColumn(TableColumn.FOREIGN_KEY, name, Specification.CREATE_TABLE);
		col.setObjectName(name+"_FK");
		col.setReferencedTableName(rtablename);
		col.setReferencedColumnName(rcolumnname);
		return col;
	}	

	public TableColumn createCheckColumn(String name, String expression)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		TableColumn col = specifyColumn(TableColumn.CHECK, name, Specification.CREATE_TABLE);
		col.setObjectName(name+"_CH");
		col.setCheckCondition(expression);
		return col;
	}	

	public TableColumn createUniqueConstraint(String columnname)	
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		TableColumn col = specifyColumn(TableColumn.UNIQUE_CONSTRAINT, columnname, Specification.CREATE_TABLE);
		col.setObjectName(columnname+"_UQ");
		return col;
	}

	public TableColumn createCheckConstraint(String name, String expression)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		TableColumn col = specifyColumn(TableColumn.CHECK_CONSTRAINT, name, Specification.CREATE_TABLE);
		col.setObjectName(name+"_CH");
		col.setCheckCondition(expression);
		return col;
	}	
		
	public TableColumn createPrimaryKeyConstraint(String columnname)	
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		TableColumn col = specifyColumn(TableColumn.PRIMARY_KEY_CONSTRAINT, columnname, Specification.CREATE_TABLE);
		col.setObjectName(columnname+"_PK");
		return col;
	}

	public TableColumn createForeignKeyConstraint(String columnname, String rtablename, String rcolumnname)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		TableColumn col = specifyColumn(TableColumn.FOREIGN_KEY_CONSTRAINT, columnname, Specification.CREATE_TABLE);
		col.setObjectName(columnname+"_FK");
		col.setReferencedTableName(rtablename);
		col.setReferencedColumnName(rcolumnname);
		return col;
	}
}

/*
* <<Log>>
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/

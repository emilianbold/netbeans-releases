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
import java.sql.*;
import java.text.ParseException;
import com.netbeans.ddl.*;
import com.netbeans.ddl.util.*;

/** 
* Default implementation of database column. It handles name, column name, it's 
* format and type. If used, column can handle referenced table and column.
* User can add custom properties into format.
*
* @author Slavek Psenicka
*/
public class AbstractTableColumn  
{	
	/** Name and column name. */
	private String name, cname, format;
	
	/** Type, usually column, primary or foreign key */
	private String otype;
	
	/** Additional properties */
	private Map addprops;

	/** Referenced table */
	String reftab;
	
	/** Referenced column */
	String refcol;
	
	/** Returns name of object */
	public String getObjectName()
	{
		return name;
	}
	
	/** Sets name of object */
	public void setObjectName(String oname)
	{
		name = oname;
	}

	/** Returns type of object */
	public String getObjectType()
	{
		return otype;
	}
	
	/** Sets name of column */
	public void setObjectType(String type)
	{
		otype = type;
	}

	/** Returns name of column */
	public String getColumnName()
	{
		return cname;
	}
	
	/** Sets name of column */
	public void setColumnName(String columnName)
	{
		cname = columnName;
	}
	
	/** Returns name of column */
	public String getFormat()
	{
		return format;
	}
	
	/** Sets name of column */
	public void setFormat(String fmt)
	{
		format = fmt;
	}

	/** Returns referenced table */
	public String getReferencedTableName()
	{
		return reftab;
	}
	
	/** Sets referenced table */
	public void setReferencedTableName(String table)
	{
		reftab = table;
	}

	/** Returns referenced column name */
	public String getReferencedColumnName()
	{
		return refcol;
	}
	
	/** Sets referenced column name */
	public void setReferencedColumnName(String col)
	{
		refcol = col;
	}

	/** Returns custom property identified by name */
	public Object getProperty(String pname)
	{
		return addprops.get(pname);
	}
	
	/** Sets property identified by name */
	public void setProperty(String pname, Object pval)
	{
		if (addprops == null) addprops = new HashMap();
		addprops.put(pname, pval);
	}

	/** Returns colum properties.
	* It first copies all custom properties, then sets:
	* object.name		Name of the object
	* column.name		Name of the column
	* These properties are required; an DDLException will throw if you
	* forgot to set it up.
	* fkobject.name		Referenced object name
	* fkcolumn.name		Referenced column name
	*/
	public Map getColumnProperties(AbstractCommand cmd)
	throws DDLException
	{
		HashMap args = new HashMap();
		String oname = getObjectName();
		String cname = getColumnName();
		
		if (addprops != null) args.putAll(addprops);
		if (oname != null) args.put("object.name", oname);
		else throw new DDLException("unknown object name");
		if (cname != null) args.put("column.name", cname);
		else throw new DDLException("unknown column name");
		
		if (reftab != null) args.put("fkobject.name", reftab);
		if (refcol != null) args.put("fkcolumn.name", refcol);
		return args;	
	}
	
	/** 
	* Returns full string representation of column. This string needs no 
	* additional formatting. Throws DDLException if format is not specified 
	* or CommandFormatter can't format it (it uses MapFormat to process entire 
	* lines and can solve [] enclosed expressions as optional.
	*/
	public String getCommand(AbstractCommand cmd)
	throws DDLException
	{
		Map cprops;
		if (format == null) throw new DDLException("no format specified");
		try {
			cprops = getColumnProperties(cmd);
			return CommandFormatter.format(format, cprops);
		} catch (Exception e) {
			throw new DDLException(e.getMessage());
		}
	}
}

/*
* <<Log>>
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/

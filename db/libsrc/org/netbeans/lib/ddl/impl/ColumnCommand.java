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

package org.netbeans.lib.ddl.impl;

import java.text.ParseException;
import java.util.*;
import org.netbeans.lib.ddl.*;

/** 
* Instances of this command operates with one column.
*
* @author Slavek Psenicka
*/

public class ColumnCommand extends AbstractCommand 
{
	/** Column */
	private TableColumn column;
		
static final long serialVersionUID =-4554975764392047624L;
	/** Creates specification of command
	* @param type Type of column
	* @param name Name of column
	* @param cmd Command
	*/	
	public TableColumn specifyColumn(String type, String name, String cmd)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		Map gprops = (Map)getSpecification().getProperties();
		Map props = (Map)getSpecification().getCommandProperties(cmd);
		Map bindmap = (Map)props.get("Binding");
		String tname = (String)bindmap.get(type);
		if (tname != null) {
			Map typemap = (Map)gprops.get(tname);
			if (typemap == null) throw new InstantiationException("unable to locate binded object "+tname);
			Class typeclass = Class.forName((String)typemap.get("Class"));
			String format = (String)typemap.get("Format");
			column = (TableColumn)typeclass.newInstance();
			column.setObjectName(name);
			column.setObjectType(type);
			column.setColumnName(name);
			column.setFormat(format);
		} else throw new InstantiationException("unable to locate type "+type+" in table: "+bindmap);

		return column;
	}
				
	public TableColumn getColumn()
	{
		return column;
	}			

	public void setColumn(TableColumn col)
	{
		column = col;
	}			
				
	/** 
	* Returns properties and it's values supported by this object.
	* column	Specification of the column 
	*/
	public Map getCommandProperties()
	throws DDLException
	{
		Map args = super.getCommandProperties();
		args.put("column", column.getCommand(this));
		return args;	
	}
}

/*
* <<Log>>
*  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         8/17/99  Ian Formanek    Generated serial version 
*       UID
*  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/

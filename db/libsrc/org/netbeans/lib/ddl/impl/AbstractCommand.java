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
* Basic implementation of DDLCommand. This class can be used for really simple
* commands with format and without arguments. Heavilly subclassed.
*
* @author Slavek Psenicka
*/
public class AbstractCommand implements DDLCommand 
{
	/** Command owner */
	private DBSpec spec;

	/** Command format */
	private String format;

	/** Object owner and name */
	private String owner, name;

	/** Additional properties */
	private Map addprops;
	
	/** Returns specification (DBSpec) for this command */
	public DBSpec getSpecification()
	{
		return spec;
	}
	
	/** 
	* Sets specification (DBSpec) for this command. This method is usually called 
	* in relevant createXXX method.
	* @param specification New specification object.
	*/
	public void setSpecification(DBSpec specification)
	{
		spec = specification;
	}
	
	/** 
	* Sets format for this command. This method is usually called in relevant createXXX
	* method.
	* @param fmt New format.
	*/
	public void setFormat(String fmt)
	{
		format = fmt;
	}

	/** Returns name of modified object */
	public String getObjectName()
	{
		return name;
	}
	
	/** Sets name to be used in command 
	* @param nam New name.
	*/
	public void setObjectName(String nam)
	{
		name = nam;	
	}

	/** Returns name of modified object */
	public String getObjectOwner()
	{
		return owner;
	}
	
	/** Sets name to be used in command
	* @param objectowner New owner.
	*/
	public void setObjectOwner(String objectowner)
	{
		owner = objectowner;	
	}

	/** Returns general property */
	public Object getProperty(String pname)
	{
		return addprops.get(pname);
	}
	
	/** Sets general property */
	public void setProperty(String pname, Object pval)
	{
		if (addprops == null) addprops = new HashMap();
		addprops.put(pname, pval);
	}

	/** 
	* Returns properties and it's values supported by this object.
	* object.name	Name of the object; use setObjectName() 
	* object.owner	Name of the object; use setObjectOwner() 
	* Throws DDLException if object name is not specified.
	*/
	public Map getCommandProperties()
	throws DDLException
	{
		HashMap args = new HashMap();
		if (addprops != null) args.putAll(addprops);
		String oname = getObjectName();
		if (oname != null) args.put("object.name", getObjectName());
		else throw new DDLException("unknown object name");
		args.put("object.owner", getObjectOwner());		
		return args;	
	}

	/** 
	* Executes command.
	* First it calls getCommand() to obtain command text. Then tries to open JDBC
	* connection and execute it. If connection is already open, uses it and leave
	* open; otherwise creates new one and closes after use. Throws DDLException if
	* something wrong occurs.
	*/
	public void execute() 
	throws DDLException
	{
		String fcmd;
		Connection fcon = null;
		boolean opened = false;
		
		try {
			fcmd = getCommand();
			System.out.println(fcmd);
		} catch (Exception e) {
			throw new DDLException("unable to format a command "+format+": "+e.getMessage());
		}
		
		try {
			fcon = spec.getJDBCConnection();
			if (fcon == null) {
				fcon = spec.openJDBCConnection();
				opened = true;
			}
			
			Statement stat = fcon.createStatement();
			stat.executeUpdate(fcmd);
		} catch (Exception e) {
			if (opened && fcon != null) spec.closeJDBCConnection();
			throw new DDLException("unable to execute a command "+fcmd+": "+e.getMessage());
		}
			
		if (opened) spec.closeJDBCConnection();
	}

	/** 
	* Returns full string representation of command. This string needs no 
	* formatting and could be used directly as argument of executeUpdate() 
	* command. Throws DDLException if format is not specified or CommandFormatter
	* can't format it (it uses MapFormat to process entire lines and can solve []
	* enclosed expressions as optional.
	*/
	public String getCommand()
	throws DDLException
	{
		if (format == null) throw new DDLException("no format specified");
		try {
			return CommandFormatter.format(format, getCommandProperties());
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

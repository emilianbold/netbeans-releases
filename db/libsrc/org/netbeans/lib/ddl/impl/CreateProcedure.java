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

public class CreateProcedure extends AbstractCommand implements ProcedureDescriptor 
{
	/** Catalog */
	private String cat;
	
	/** Body of the procedure */
	private String body;
	
	/** Arguments */
	private Vector args;
	
static final long serialVersionUID =1316633286943440734L;
	public CreateProcedure()
	{
		args = new Vector();
	}
	
	/** Returns catalog */
	public String getCatalog()
	{
		return cat;
	}
	
	/** Sets catalog */
	public void setCatalog(String cname)
	{
		cat = cname;
	}

	/** Returns text of procedure */
	public String getText()
	{
		return body;
	}
	
	/** Sets name of table */
	public void setText(String text)
	{
		body = text;
	}
	
	/** Returns arguments */
	public Vector getArguments()
	{
		return args;
	}
	
	public Argument getArgument(int index)
	{
		return (Argument)args.get(index);
	}
	
	/** Sets argument array */
	public void setArguments(Vector argarr)
	{
		args = argarr;	
	}

	public void setArgument(int index, Argument arg)
	{
		args.set(index, arg);
	}

	public Argument createArgument(String name, int type, int datatype)
	throws DDLException
	{
		try {
			Map gprops = (Map)getSpecification().getProperties();
			Map props = (Map)getSpecification().getCommandProperties(Specification.CREATE_PROCEDURE);
			Map bindmap = (Map)props.get("Binding");
			String tname = (String)bindmap.get("ARGUMENT");
			if (tname != null) {
				Map typemap = (Map)gprops.get(tname);
				if (typemap == null) throw new InstantiationException("unable to locate binded object "+tname);
				Class typeclass = Class.forName((String)typemap.get("Class"));
				String format = (String)typemap.get("Format");
				ProcedureArgument arg = (ProcedureArgument)typeclass.newInstance();
				arg.setName(name);
				arg.setType(type);
				arg.setDataType(datatype);
				arg.setFormat(format);
				return (Argument)arg;
			} else throw new InstantiationException("unable to locate type "+type+" in table: "+bindmap);
		} catch (Exception e) {
			throw new DDLException(e.getMessage());
		}
	}

	public void addArgument(String name, int type, int datatype)
	throws DDLException
	{
		Argument arg = createArgument(name, type, datatype);
		if (arg != null) args.add(arg);
	}

	public Map getCommandProperties()
	throws DDLException
	{
		Map props = (Map)getSpecification().getProperties();
		String cols = "", argdelim = (String)props.get("ArgumentListDelimiter");
		Map cmdprops = super.getCommandProperties();

		Enumeration col_e = args.elements();
		while (col_e.hasMoreElements()) {
			ProcedureArgument arg = (ProcedureArgument)col_e.nextElement();
			boolean inscomma = col_e.hasMoreElements();
			cols = cols + arg.getCommand(this)+(inscomma ? argdelim : "");
		}
		
		cmdprops.put("arguments", cols);
		cmdprops.put("body", body);
		return cmdprops;	
	}
}

/*
* <<Log>>
*  4    Gandalf   1.3         9/10/99  Slavek Psenicka 
*  3    Gandalf   1.2         8/17/99  Ian Formanek    Generated serial version 
*       UID
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/

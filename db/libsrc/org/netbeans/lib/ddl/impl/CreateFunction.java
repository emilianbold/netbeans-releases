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

public class CreateFunction extends CreateProcedure implements FunctionDescriptor 
{
	/** Catalog */
	private int rarg;
				
static final long serialVersionUID =-7554675717309349130L;
	/** Returns text of procedure */
	public int getReturnType()
	{
		return rarg;
	}
	
	/** Sets name of table */
	public void setReturnType(int aval)
	{
		rarg = aval;
	}

	public Map getCommandProperties()
	throws DDLException
	{
		Map cmdprops = super.getCommandProperties();
		cmdprops.put("return.type", getSpecification().getType(rarg));
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

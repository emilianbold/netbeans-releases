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

package com.netbeans.ddl;

import com.netbeans.ddl.DDLException;

/** 
* Interface of database action command. Instances should remember connection 
* information of DBSpec and use it in execute() method.
*
* @author Slavek Psenicka
*/
public interface DDLCommand
{
	/** Returns specification (DBSpec) for this command */
	public DBSpec getSpecification();
	
	/** Returns name of modified object */
	public String getObjectName();
	
	/** Sets name to be used in command 
	* @param name New name
	*/
	public void setObjectName(String name);

	/** Executes command */
	public void execute() throws DDLException;

	/** 
	* Returns full string representation of command. This string needs no 
	* formatting and could be used directly as argument of executeUpdate() 
	* command. Throws DDLException if format is not specified or CommandFormatter
	* can't format it (it uses MapFormat to process entire lines and can solve []
	* enclosed expressions as optional.
	*/
	public String getCommand()
	throws DDLException;
}

/*
* <<Log>>
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/

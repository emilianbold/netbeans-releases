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

/** 
* System is not able to locate appropriate resources to create DBSpec object
* (object describing the database). It means that database product is not 
* supported by system. You can use generic database system or write your 
* own description file. If you are sure that it is, please check location 
* of description files.
*
* @author Slavek Psenicka
*/
public class DatabaseProductNotFoundException extends Exception 
{
	/** Database product name */
	private String sname;

	/** Creates new exception
	* @param desc The text describing the exception
	*/
	public DatabaseProductNotFoundException (String spec) {
		super ();
		sname = spec;    
	}

	/** Creates new exception with text specified string.
	* @param spec Database product name
	* @param desc The text describing the exception
	*/
	public DatabaseProductNotFoundException (String spec, String desc) {
		super (desc);
		sname = spec;
	}
	
	/** Returns database product name.
	* This database is not supported by system. You can use generic database 
	* system or write your own description file.
	*/
	public String getDatabaseProductName()
	{
		return sname;
	}
}

/*
 * <<Log>>
 *  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
 * $
 */

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

/** Generic DDL operation-related exception.
* This is a generic DDL-operation related exception. 
*
* @author Slavek Psenicka
*/
public class DDLException extends Exception 
{
	/** Creates new exception 
	* @param message The text describing the exception
	*/
	public DDLException(String message) {
		super (message);
	}
}

/*
 * <<Log>>
 *  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
 * $
 */

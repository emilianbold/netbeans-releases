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

import java.util.Vector;

/** 
* Interface of create procedure action command. 
* @author Slavek Psenicka
*/
public interface CreateProcedureCommand extends DDLCommand 
{		
	/** Returns catalog */
	public String getCatalog();

	/** Sets catalog 
	*@param cname New catalog name
	*/
	public void setCatalog(String cname);

	/** Returns text of procedure */
	public String getText();

	/** Sets name of table
	*@param cname New code of procedure
	*/
	public void setText(String text);
	
	/** Returns arguments */
	public Vector getArguments();
	
	/** Sets argument array
	*@param cname New argument array
	*/
	public void setArguments(Vector args);
}

/*
* <<Log>>
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/

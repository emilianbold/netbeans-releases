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
* Interface of check constraint descriptor.
*
* @author Slavek Psenicka
*/
public interface CheckConstraintDescriptor extends TableConstraintDescriptor {

	/** Returns check condition for table */
	public String getCheckCondition();
	
	/** Sets check condition for table 
	* @param val Check condition
	*/
	public void setCheckCondition(String val);
}

/*
* <<Log>>
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/

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
* Interface for comment commands. Command CommentTable must implement it. If another
* command comments something, it should do it too.
*
* @author Slavek Psenicka
*/
public interface CommentOperationCommand extends DDLCommand {
	
	/** Returns object comment */
	public String getComment();
	
	/** Sets object comment 
	* @param comment New comment.
	*/
	public void setComment(String comment);
}

/*
* <<Log>>
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/

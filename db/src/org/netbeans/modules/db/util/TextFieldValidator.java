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

package com.netbeans.enterprise.modules.db.util;

import java.lang.Integer;

public abstract class TextFieldValidator
{
	public abstract boolean accepts(String value);
	
	public static final class integer extends TextFieldValidator
	{
		public boolean accepts(String value) {
			try {
				Integer intval = new Integer(value);
			} catch (NumberFormatException e) { return false; }
			return true;
		}
	}
}


/*
 * <<Log>>
 *  4    Gandalf-post-FCS1.2.1.0     4/10/00  Radko Najman    
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         5/14/99  Slavek Psenicka 
 * $
 */

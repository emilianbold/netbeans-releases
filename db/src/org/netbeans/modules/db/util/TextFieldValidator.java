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
	};
}


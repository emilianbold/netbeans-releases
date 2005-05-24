/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * TextMapping.java
 *
 * Created on October 27, 2003, 8:39 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

/** Class that associates two strings with each other, presumably one to be
 *  written in code contexts, such as XML, and the other for display purposes,
 *  allowing it to be localized independently of the code.  This was originally
 *  designed to make localized comboboxes and listboxes easier to program but
 *  there are probably other UI elements that could make use of it.
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class TextMapping {
	
	private String xmlText;
	private String displayText;

	public TextMapping(final String xml, final String display) {
		xmlText = xml;
		displayText = display;
	}

	public String toString() {
		return displayText;
	}

	public String getXMLString() {
		return xmlText;
	}
}

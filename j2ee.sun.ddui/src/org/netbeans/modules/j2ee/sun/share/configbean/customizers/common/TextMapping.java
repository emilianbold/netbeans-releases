/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
	
	private final String xmlText;
	private final String displayText;

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

/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.api.utils;

import java.util.StringTokenizer;

/** Miscellaneous utility classes useful for the C/C++/Fortran module */
public class CppUtils {

    public static String reformatWhitespaces(String string)  {
	return reformatWhitespaces(string, ""); // NOI18N
    }

    public static String reformatWhitespaces(String string, String prepend)  {
	return reformatWhitespaces(string, prepend, ""); // NOI18N
    }

    public static String reformatWhitespaces(String string, String prepend, String delimiter)  {
	if (string == null)
	    return null;
	String formattedString = ""; // NOI18N
	StringTokenizer st = new StringTokenizer(string);
	while (st.hasMoreTokens()) {
	    String token = st.nextToken();
	    String append = ""; // NOI18N
	    if (st.hasMoreTokens())
		append = delimiter + " "; // NOI18N
	    formattedString += prepend + token + append;
	}
	return formattedString;
    }
}


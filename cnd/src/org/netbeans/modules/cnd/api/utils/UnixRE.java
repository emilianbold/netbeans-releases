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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *  A Unix Regular Expression is one using the standard Unix shell syntax. Its
 *  less powerfull than the full Posix RE but the expected behavior for file
 *  filters.
 */
public class UnixRE {

    /** the UnixRE is stored as a regexp RE */
    Pattern re;

    /** Save a copy of the original pattern */
    boolean starPattern;

    boolean debugRE = false;

    public UnixRE(String pattern) throws PatternSyntaxException {
	StringBuffer unixText = new StringBuffer(256);
	char prev = 0;

	if (Boolean.getBoolean("ifdef.debug.unixre") &&	//NOI18N
			pattern.startsWith("[[[")) {	//NOI18N
	    // OLD re = new RE(pattern.substring(3));
	    re = Pattern.compile(pattern.substring(3));
	    return;
	}

	if (pattern.charAt(0) == '*') {
	    starPattern = true;
	} else {
	    starPattern = false;
	}

	// TODO: Escape all regexp magic chars that UnixRE doesn't want glob'ed
	unixText.append('^');
	for (int i = 0; i < pattern.length(); i++) {
	    char c = pattern.charAt(i);

	    if (c == '*' && prev != '\\') {
		unixText.append(".*");					//NOI18N
	    } else if (c == '?' && prev != '\\') {
		unixText.append(".{1}");				//NOI18N
	    } else if (c == '.' && prev != '\\') {
		unixText.append("\\.");					//NOI18N
	    } else {
		unixText.append(c);
	    }
	    prev = c;
	}
	unixText.append('$');

	// OLD re = new RE(unixText.toString());
	re = Pattern.compile(unixText.toString());
    }

    public boolean match(String s) {

	if (starPattern && s.charAt(0) == '.') {
	    return false;
	} else {
	    return re.matcher(s).find();
	}
    }

    /**
     *  Tells if the string is a Unix regular expression.
     */
    static public boolean isUnixRE(String s) {
	char	prev = 0;			// previous character
	char	c;				// current character

	for (int i = 0; i < s.length(); i++) {
	    c = s.charAt(i);
	    if ((c == '*' || c == '?' || c == '[' || c == ']')
				&& prev != '\\') {
		return true;
	    }
	    prev = c;
	}

	return false;
    }
}


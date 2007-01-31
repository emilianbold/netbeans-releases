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

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 *  Parse a Fortran file. Return complete Fortran statements, taking into consideration
 *  things like the source form, line length, continuation lines, and comments. This
 *  class works in conjunction with FortranParse to parse fortran files for mod and use
 *  statements.
 */
public class FortranParser {

    private FortranReader in;

    private boolean verbose = false;

    /** Contstructor for this semi-parsing class */
    public FortranParser(String file, String options,
			boolean verbose, boolean verboseReader) {

	this.verbose = verbose;

	try {
	    in = new FortranReader(file, options, verboseReader);
	} catch (FileNotFoundException ex) {
	    in = null;
	}
    }


    /** Contstructor for this semi-parsing class */
    public FortranParser(String file, String options) {
	this(file, options, false, false);
    }


    /**
     *  This is a semi-parser. Kind of. It gets complete Fortran statements (stripped of
     *  comments and labels) and checks for 2 specific keywords, MODULE and USE. It
     *  creates a list containing each MODULE or USE name preceded by an 'M' or 'U'.
     */
    public ArrayList parser() {
	ArrayList list = new ArrayList();
	String line;
	String arg;

	if (in == null) {
	    return null;
	}

	try {
	    while ((line = in.getStatement()) != null) {
		if (line.length() >= 15 && line.substring(0, 15).
				equalsIgnoreCase("moduleprocedure")) {	// NOI18N
		    // ignore this keyword
		} else if (line.length() >= 6 &&
				line.substring(0, 6).equalsIgnoreCase("module")) { // NOI18N
		    arg = getName(line.substring(6));
		    list.add("M" + arg);    // NOI18N
		} else if (line.length() >= 3 &&
				line.substring(0, 3).equalsIgnoreCase("use")) {	// NOI18N
		    arg = getName(line.substring(3));
		    list.add("U" + arg);    // NOI18N
		}
	    }
	} catch (UnexpectedEOFException ex) {
	    if (verbose) {
		System.err.println("Error: Unexpected EOF"); // NOI18N
		ex.printStackTrace();
	    }
	    return null;
	}

	return list;
    }


    /** Get the name following a MODULE or USE keyword */
    private String getName(String arg) {
	StringBuffer buf = new StringBuffer();
	String arg2 = arg.trim();
	
	for (int i = 0; i < arg2.length(); i++) {
	    char c = arg2.charAt(i);

	    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
					(c >= '0' && c <= '9') || c == '_') {
		buf.append(c);
	    } else {
		break;
	    }
	}
	return buf.toString();
    }



    /** This main method is used for testing the class */
    public static void main(String[] args) {
	FortranParser parser = null;
	boolean verbose = false;
	String file = null;

	for (int i = 0; i < args.length; i++) {
	    if (args[i].equals("-v")) {	// NOI18N
		verbose = true;
	    } else {
		file = args[i];
	    }
	}

	parser = new FortranParser(file, null);
	parser.verbose = verbose;

	ArrayList list = parser.parser();

	System.out.println("FortranParser: list has " + list.size() + " elements");// NOI18N
	for (int i = 0; i < list.size(); i++) {
	    String stmnt = list.get(i).toString();
	    if (stmnt.charAt(0) == 'M') {
		System.out.println("\tModule " + stmnt.substring(1));	// NOI18N
	    } else {
		System.out.println("\tUse " + stmnt.substring(1));  // NOI18N
	    }
	}
    }
}

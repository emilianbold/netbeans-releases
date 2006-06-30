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

package org.netbeans.modules.schema2beans;

import java.io.*;


/**
 *  schema2beans simple logger implementation
 */

public class TraceLogger extends Object {
    public static int MAXGROUP 		= 255;

    public static int DEBUG	   	= 1;

    public static int SVC_DD	   	= 1;

    public static PrintStream	output 	= System.out;

    static DDLogFlags flags = new DDLogFlags();

    static public void put(int type, int service, int group, int level,
			   int msg) {
	put(type, service, group, level, msg, null);
    }

    static public void put(int type, int service, int group, int level,
			   int msg, Object obj) {

	String strService = "DD ";	// NOI18N
	String strGroup = flags.dbgNames[group-1];
	String strMsg	= ((String[])(flags.actionSets[group-1]))[msg-1];
	
	if (obj != null) {
	    System.out.println( strService + " " + strGroup + " " +	// NOI18N
	    strMsg + "\t" + obj.toString());// NOI18N
	}
	else {
	    System.out.println( strService + " " + strGroup + " " +	strMsg);// NOI18N
	}
    }
    
    public static void error(String str) {
	output.println(str);
    }
    
    public static void error(Throwable e) {
	output.println("*** ERROR - got the following exception ---");	// NOI18N
	output.println(e.getMessage());
	e.printStackTrace(output);
	output.println("*** ERROR ---------------------------------");	// NOI18N
    }
}


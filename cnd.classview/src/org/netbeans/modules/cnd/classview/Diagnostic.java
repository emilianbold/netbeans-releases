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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.classview;

import java.io.*;
import org.netbeans.modules.cnd.modelutil.Tracer;

/**
 *
 * @author Vladimir Kvashin
 */
public class Diagnostic {
    
    public static final boolean DEBUG = Boolean.getBoolean("cnd.classview.trace"); // NOI18N
    public static final boolean DUMP_MODEL = Boolean.getBoolean("cnd.classview.dumpmodel"); // NOI18N
    
    private static Tracer tracer = new Tracer(System.err);
    
    public static void trace(Object arg) {
	if( DEBUG ) {
	    tracer.trace(arg);
	}
    }
    
    public static void indent() {
	tracer.indent();
    }
    
    public static void unindent() {
	tracer.unindent();
    }
    
    public static void traceStack(String message) {
	if( DEBUG ) {
	    trace(message);
	    StringWriter wr = new StringWriter();
	    new Exception(message).printStackTrace(new PrintWriter(wr));
	    //StringReader sr = new StringReader(wr.getBuffer().toString());
	    BufferedReader br = new  BufferedReader(new StringReader(wr.getBuffer().toString()));
	    try {
		br.readLine(); br.readLine();
		for( String s = br.readLine(); s != null; s = br.readLine() ) {
		    trace(s);
		}
	    } catch( IOException e ) {
		e.printStackTrace(System.err);
	    }
	}
    }
}

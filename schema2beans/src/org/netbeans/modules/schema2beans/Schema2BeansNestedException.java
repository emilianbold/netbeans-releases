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

/*
 * Schema2BeansNestedException
 */

package org.netbeans.modules.schema2beans;

import java.util.*;
import java.io.*;

public class Schema2BeansNestedException extends Schema2BeansException implements Serializable {
    protected Throwable childThrowable;
    protected String message;
    protected String stackTrace;

    public Schema2BeansNestedException(Throwable e) {
        super("");
        if (DDLogFlags.debug) {
            System.out.println("Created Schema2BeansNestedException1: e="+e);
            e.printStackTrace();
        }
        childThrowable = e;
        message = childThrowable.getMessage();
        genStackTrace();
    }
    
    public Schema2BeansNestedException(String mesg, Throwable e) {
        super(mesg);
        if (DDLogFlags.debug) {
            System.out.println("Created Schema2BeansNestedException2: e="+e+" mesg="+mesg);
            e.printStackTrace();
        }
        childThrowable = e;
        message = mesg+"\n"+childThrowable.getMessage();
        genStackTrace();
    }

    public Throwable getCause() {
        return childThrowable;
    }
    
    public String getMessage() {
        return message;
    }

    protected void genStackTrace() {
        StringWriter strWriter = new StringWriter();
        PrintWriter s = new PrintWriter(strWriter);
        if (childThrowable == null) {
            super.printStackTrace(s);
        } else {
            s.println(super.getMessage());
            childThrowable.printStackTrace(s);
        }
        stackTrace = strWriter.toString();
   }

    public void printStackTrace(PrintStream s) {
        s.println(stackTrace);
    }

    public void printStackTrace(PrintWriter s) {
        s.println(stackTrace);
    }

    public void printStackTrace() {
        System.err.println(stackTrace);
    }
}

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

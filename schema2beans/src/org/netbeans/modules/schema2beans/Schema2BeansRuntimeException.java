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
 * Schema2BeansRuntimeException
 * I'd rather this class didn't exist.  I wanted to change the API to throw
 * proper exceptions, but this is difficult to do with so many users of
 * this library.  This class exists to fill in where I found it
 * difficult to throw a normal Schema2BeansException, since anything
 * that inherits from RuntimeException doesn't have to be caught.
 *
 * With that being said, there are a few times when a RuntimeException
 * would be approbate enough, but I used Schema2BeansRuntimeException
 * as it has more features.
 */

package org.netbeans.modules.schema2beans;

import java.util.*;
import java.io.*;

public class Schema2BeansRuntimeException extends RuntimeException implements Serializable {
    protected Throwable childThrowable;
    protected String message;
    protected String stackTrace;

    public Schema2BeansRuntimeException(Throwable e) {
        super("");
        //System.out.println("Created Schema2BeansRuntimeException1: e="+e);
        //e.printStackTrace();
        childThrowable = e;
        message = childThrowable.getMessage();
        genStackTrace();
    }
    
    public Schema2BeansRuntimeException(String mesg) {
        super(mesg);
        //System.out.println("Created Schema2BeansRuntimeException3: mesg="+mesg);
        childThrowable = null;
        message = mesg;
        genStackTrace();
    }

    public Schema2BeansRuntimeException(String mesg, Throwable e) {
        super(mesg);
        //System.out.println("Created Schema2BeansRuntimeException2: e="+e+" mesg="+mesg);
        //e.printStackTrace();
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
        System.out.println(stackTrace);
    }
}

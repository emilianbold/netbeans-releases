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
 * Schema2beansException
 */

package org.netbeans.modules.schema2beans;

import java.io.Serializable;
import java.util.*;
import java.io.*;

public class Schema2BeansException extends Exception implements Serializable {
    protected String originalStackTrace;
    
    public Schema2BeansException(String msg) {
        super(msg);
    }

    public String getOriginalStackTrace() {
        //System.out.println("originalStackTrace="+originalStackTrace);
        if (originalStackTrace == null)
            return getMessage();
        return getMessage()+"\n"+originalStackTrace;
    }

    public void stashOriginalStackTrace() {
        StringWriter strWriter = new StringWriter();
        printStackTrace(new PrintWriter(strWriter));
        originalStackTrace = strWriter.toString();
        //System.out.println("stashOriginalStackTrace: originalStackTrace="+originalStackTrace);
    }
}

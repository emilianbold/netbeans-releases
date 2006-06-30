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

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


package org.netbeans.core.windows;


import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * Utility class for debugging support of window system.
 *
 * @author  Peter Zavadsky
 */
public abstract class Debug {
    /** Creates a new instance of Debug */
    private Debug() {
    }


    public static boolean isLoggable(Class clazz) {
        return Logger.getLogger(clazz.getName()).isLoggable(Level.FINE);
    }

    /** Logs debug message depending whether the logging is required based on class name.
     * @see org.openide.ErrorManager.getInstance(String) */
    public static void log(Class clazz, String message) {
        Logger.getLogger(clazz.getName()).fine(message);
    }
    
    
    public static void dumpStack(Class clazz) {
        // log(Class,String) only has an effect if INFORMATIONAL logging enabled on that prefix
        if(Logger.getLogger(clazz.getName()).isLoggable(Level.FINE)) {
            StringWriter sw = new StringWriter();
            new Throwable().printStackTrace(new PrintWriter(sw));
            log(clazz, sw.getBuffer().toString());
        }
    }
}

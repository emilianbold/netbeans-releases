/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows;


import java.io.PrintWriter;
import java.io.StringWriter;

import org.openide.ErrorManager;


/**
 * Utility class for debugging support of window system.
 *
 * @author  Peter Zavadsky
 */
public abstract class Debug {
    
    /** Creates a new instance of Debug */
    private Debug() {
    }
    
    /** Logs debug message depending whether the logging is required based on class name.
     * @see org.openide.ErrorManager.getInstance(String) */
    public static void log(Class clazz, String message) {
        ErrorManager.getDefault().getInstance(clazz.getName()).log(message);
    }
    
    
    public static void dumpStack(Class clazz) {
        // log(Class,String) only has an effect if INFORMATIONAL logging enabled on that prefix
        if(ErrorManager.getDefault().getInstance(clazz.getName())
                .isLoggable(ErrorManager.INFORMATIONAL)) {
            StringWriter sw = new StringWriter();
            new Throwable().printStackTrace(new PrintWriter(sw));
            log(clazz, sw.getBuffer().toString());
        }
    }
}

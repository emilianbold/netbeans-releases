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
package org.netbeans.tax;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;


/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public abstract class AbstractUtil {
    /** */
    private static final boolean DEBUG = Boolean.getBoolean ("netbeans.debug.tax") ;
    
    
    //
    // itself
    //
    
    /**
     * Kind of equals that treat null-ed object as equvivalent.
     * Suitable while testing for property equalence before firing.
     * return true if these are same
     */
    public static boolean equals (Object a, Object b) {
        if ( a != null ) {
            return (a.equals (b));
        } else {
            return (a == b);
        }
    }

    /**
     * Just for debugging purposes.
     */
    public static void debug (String message, Throwable ex) {
        if ( DEBUG ) {
            System.err.println (message);
            if ( ex != null ) {
                ex.printStackTrace (System.err);
            }
        }
    }
    
    
    /**
     * Just for debugging purposes.
     */
    public static void debug (String message) {
        debug (message, null);
    }
    
}

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer;

/**
 * Get values of system properties from external JDK. Put property names as input
 * parameters. Values of properties are sent to standard output one per line.
 */

public class Verify {

    public Verify() {
    }

    public static void main (String args[]) {
        for (int i = 0; i < args.length; i++) {
            System.out.println(System.getProperty(args[i]));
        }
    }

}

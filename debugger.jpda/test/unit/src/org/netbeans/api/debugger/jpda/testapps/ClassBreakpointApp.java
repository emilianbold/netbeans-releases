/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda.testapps;

/**
 * Sample class breakpoints application. DO NOT MODIFY - line numbers must not change in this source file.
 *
 * @author Maros Sandor
 */
public class ClassBreakpointApp {

    public static void main(String[] args) {
        ClassBreakpointApp sa = new ClassBreakpointApp();
        sa.loadClasses();
    }

    private void loadClasses() {
        try {
            Class.forName("org.netbeans.api.debugger.jpda.testapps.ClassBreakpointTest1");
            Class.forName("java.util.Map");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

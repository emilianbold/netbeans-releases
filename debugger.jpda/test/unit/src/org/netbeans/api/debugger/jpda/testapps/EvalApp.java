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
 * Sample application used in expression evaluator unit tests.
 * DO NOT MODIFY - line numbers must not change in this source file.
 *
 * @author Maros Sandor
 */
public class EvalApp {

    /* **************************************************************************************************
        The following code must stay where it is, on same line numbers, else all unit tests will fail.
    ************************************************************************************************** */
    public static void main(String[] args) {
        ix += 10;
        EvalApp app = new EvalApp();
        app.m1();
        app.m2();
    }

    public EvalApp() {
        m0();
    }

    private void m0() {
    }

    public int m2() {
        return 20;
    }

    private int m1() {
        return 5;
    }

    private float m3() {
        return 4.3f;
    }

    private static int      ix = 74;
    private static float    fx = 10.0f;
    private static double   dx = 10.0;
    private static boolean  bx = true;
    private static short    sx = 10;
    private static char     cix = 'a';
}

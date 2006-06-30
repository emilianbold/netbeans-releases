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

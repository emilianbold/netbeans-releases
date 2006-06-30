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
 * Sample call stack info application. DO NOT MODIFY - line numbers must not change in this source file.
 *
 * @author Maros Sandor
 */
public class CallStackApp {

    public static void main(String[] args) {
        CallStackApp sa = new CallStackApp();
        x += sa.m1();
        x += sa.m1();
        x += sa.m1();
        x += sa.m1();
        x += sa.m1();
    }

    public CallStackApp() {
    }

    private int m1() {
        int im1 = 10;
        m2();
        return im1;
    }

    private int m2() {
        int im2 = 20;
        m3();
        return im2;
    }

    private int m3() {
        int im3 = 30;
        return im3;
    }

    static int x = 20;
}

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda.testapps;

/**
 * Sample step application. DO NOT MODIFY - line numbers must not change in this source file.
 *
 * @author Maros Sandor
 */
public class AsynchStepApp {

    public static void main(String[] args) {
        AsynchStepApp sa = new AsynchStepApp();
        x += sa.m1();
        x += sa.m1();
        x += sa.m1();
        x += sa.m1();
        x += sa.m1();
    }

    public AsynchStepApp() {
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

    private int longMethod() {
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException iex) {}
        return 0;
    }

}

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
 * Sample field breakpoints application. DO NOT MODIFY - line numbers must not change in this source file.
 *
 * @author Maros Sandor
 */
public class FieldBreakpointApp {

    static int x = 1;

    static {
        x ++;
    }

    public static void main(String[] args) {
        FieldBreakpointApp sa = new FieldBreakpointApp();
        x ++;
        sa.m1();
        int isq = InnerStatic.getQ();
        InnerStatic is = new InnerStatic();
        int isw = is.getW();
    }

    private int y = 1;

    {
        y++;
    }

    public FieldBreakpointApp() {
        y++;
    }

    private int m1() {
        int im1 = 10;
        m2();
        Inner ic = new Inner();
        int iw = ic.getW();
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

    private static class InnerStatic {

        private static int q = 0;

        static {
            q ++;
        }

        private int w = 1;

        {
            w ++;
        }

        public InnerStatic() {
            w ++;
        }

        public static int getQ() {
            return q;
        }

        public int getW() {
            return w;
        }
    }

    private class Inner {

        private int w = 1;

        {
            w ++;
        }

        public Inner() {
            w ++;
        }

        public int getW() {
            return w;
        }
    }

}

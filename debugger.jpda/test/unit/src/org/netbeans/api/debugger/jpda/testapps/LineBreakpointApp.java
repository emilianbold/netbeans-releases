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
 * Sample line breakpoints application. DO NOT MODIFY - line numbers must not change in this source file.
 *
 * @author Maros Sandor
 */
public class LineBreakpointApp {

    static int x = 20;

    static {
        x += 30;
    }

    public static void main(String[] args) {
        LineBreakpointApp sa = new LineBreakpointApp();
        x += sa.m1();
        
        int isq = InnerStatic.getQ();
        InnerStatic is = new InnerStatic();
        int isw = is.getW();
    }

    private int y = 20;

    {
        y += 10;
    }

    public LineBreakpointApp() {
        y += 100;
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

        private static int q = 200;

        static {
            q += 40;
        }

        private int w = 70;

        {
            w += 10;
        }

        public InnerStatic() {
            w += 100;
        }

        public static int getQ() {
            return q;
        }

        public int getW() {
            return w;
        }
    }

    private class Inner {

        private int w = 70;

        {
            w += 10;
        }

        public Inner() {
            w += 100;
        }

        public int getW() {
            return w;
        }
    }

}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda.testapps;

/**
 * Sample step application. DO NOT MODIFY - line numbers must not change in this source file.
 *
 * @author Martin Entlicher
 */
public class ExpressionStepApp {

    public static void main(String[] args) {
        x += factorial(10);
        x += factorial(20) + factorial(30);
        x += factorial(40); x += factorial(50);
        ExpressionStepApp exs = new ExpressionStepApp();
        x = exs.m1(exs.m2((int) x));
        x = exs.m3(exs.m1(exs.m2((int) x)), exs.m1((int) x)).intValue();
        System.out.println(x);
    }

    public ExpressionStepApp() {
    }
    
    public static long factorial(int n) {
        long f = 1;
        for (int i = 2; i <= n; i++) {
            f *= i;
        }
        return f;
    }
    
    private int m1(int x) {
        int im1 = 10;
        return im1*x;
    }

    private int m2(int x) {
        int im2 = 20;
        return im2*x;
    }

    private Integer m3(int x, int y) {
        int im3 = 30;
        return new Integer(im3 + x + y);
    }

    static long x = 20L;
}

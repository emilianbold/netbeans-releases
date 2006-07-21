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
 * Sample method breakpoints application. DO NOT MODIFY - line numbers must not change in this source file.
 *
 * @author Maros Sandor
 */
public class MethodBreakpointApp {

    public static void main(String[] args) {
        MethodBreakpointApp sa = new MethodBreakpointApp();
        sa.a();
        sa.b();
        sa.c();
        new InnerStatic().getW();
        new ConcreteInner().compute(); new ConcreteInner().getString();
    }
    static {
        System.currentTimeMillis();
    }

    public MethodBreakpointApp() {
        System.currentTimeMillis();
    }

    private void a() {
        b();
        c();
    }

    private void b() {
        c();
    }

    private void c() {
        Inner i = new Inner(); i.getW(); i.getW();
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
    
    private static abstract class AbstractInner {
        
        public abstract double compute();
        
    }
    
    private static interface InterfaceInner {
        
        String getString();
        
    }
    
    private static class ConcreteInner extends AbstractInner implements InterfaceInner {
        
        public double compute() {
            double num = Math.PI/2;
            return Math.round(Math.sin(num)*1000)/1000.0;
        }
        
        public String getString() {
            char[] chars = new char[] { 'H', 'e', 'l', 'l', 'o' };
            return new String(chars);
        }
        
    }

}

/*
 * TestClass.java
 *
 * Created on August 23, 2006, 4:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.junit.pkgtestcreation.test;

import java.util.ArrayList;

/**
 *
 * @author ms159439
 */
public class TestClass {
    
    /**
     * Creates a new instance of TestClass
     */
    public TestClass() {
    }
    
    /**
     * public method
     */
    public int add(int a, int b) {
        return a + b;
    }
    
    /**
     * protected method
     */
    protected int arrayListCount(ArrayList a) {
        return a.size();
    }
    
    /**
     * private method
     * should not be included in any test
     */
    private int subs(int a, int b) {
        return a - b;
    }
    
    /**
     * friendly (pacakge private) method
     */ 
    double sqr(double a) {
        return Math.pow(a, 2);
    }
    
    /**
     * static friendly method
     */ 
    static double thirdPow(double a) {
        return Math.pow(a, 3);
    }
}

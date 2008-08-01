/*
 * TestClass2.java
 *
 * Created on September 8, 2006, 12:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.junit.pkgtestcreation.test;

import javax.naming.Name;

/**
 *
 * @author ms159439
 */
public class TestClass2 {
    
    /** Creates a new instance of TestClass2 */
    public TestClass2() {
    }
    
    /**
     * Hellos -- public
     * @param name subject's name
     * @return the hello statement
     */
    public String hello(String name) {
        return "Hello" + name;
    }
    
    String hello2(String name) {
        return "Hello" + name;
    }
    
}

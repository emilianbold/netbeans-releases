/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package myorg;

import org.netbeans.junit.*;

public class Hello2Test extends NbTestCase {
    
    public Hello2Test(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite(Hello2Test.class);
        
        return suite;
    }
    
    /** Test of hello method, of class myorg.Hello2. */
    public void testHello() {
        System.out.println("testHello");
        
        String greeting = testObject.hello("Joe");
        assertEquals(greeting, "Hello Joe!");
    }
    
    protected Hello2 testObject;
    
    protected void setUp() {
        testObject = new Hello2();
    }
}

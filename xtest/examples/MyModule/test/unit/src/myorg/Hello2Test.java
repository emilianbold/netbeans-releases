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

package myorg;

import junit.framework.*;
import org.netbeans.junit.*;

public class Hello2Test extends NbTestCase {

    public Hello2Test(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(Hello2Test.class);

        return suite;
    }

    /** Test of hello method, of class myorg.Hello2. */
    public void testHello() {
        System.out.println("testHello");
        
        String greeting = testObject.hello("Joe");
        assertEquals(greeting, "Hello Joe!");
    }
    
    /** even though this test fails, it is expected failue - feature of XTest */
    public void testExpectedFailure() {
        System.out.print("testExpectedFailure: I will fail, but only when running in IDE -- ");
        if (System.getProperty("netbeans.home") != null ) {
            System.out.println("I'm failing");            
            fail("I failed, since I'm running in IDE");
        }
        System.out.println("I'm ok");
    }
    
    
    protected Hello2 testObject;
    
    protected void setUp() {
        testObject = new Hello2();
    }
}

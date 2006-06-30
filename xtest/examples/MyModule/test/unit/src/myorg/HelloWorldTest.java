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

import java.io.*;
import java.net.URL;
import java.util.*;
import junit.framework.*;
import org.netbeans.junit.*;

public class HelloWorldTest extends NbTestCase {

    public HelloWorldTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(HelloWorldTest.class);

        return suite;
    }

    /** Test of greeting method, of class HelloWorld. */
    public void testGreeting1() {
        String greeting;
        
        System.out.println("testGreeting1");
        greeting = testObject.greeting();
        assertTrue(null != greeting);
    }

    

    public void testGreeting2() throws IOException {
        File test;
        File pass;
        FileWriter wr;
        String greeting;
        System.out.println("testGreeting2");
        greeting = testObject.greeting();
        test = new File(dataDir, "greeting.test");
        pass = new File(dataDir, "greeting.pass");
        wr = new FileWriter(test);
        wr.write(greeting);
        wr.close();
        
        assertFile("This failure is for demonstration purpose only.", test, pass, dataDir);
    }
    
    protected HelloWorld testObject;
    protected File dataDir;
    
    protected void setUp() {
        String packageName = "";
        if (null != getClass().getPackage()) {
            packageName = getClass().getPackage().getName();
        }
        dataDir = getDataDir();
        
        testObject = new HelloWorld();
    }
}

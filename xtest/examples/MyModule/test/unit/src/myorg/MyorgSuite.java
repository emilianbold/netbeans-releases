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

import junit.framework.*;
import org.netbeans.junit.*;

public class MyorgSuite extends NbTestCase {
    
    public MyorgSuite(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {       
        TestSuite suite = new NbTestSuite("MyorgSuite");
        suite.addTest(myorg.HelloWorldTest.suite());
        suite.addTest(myorg.Hello2Test.suite());       
        return suite;
    }
    
}

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

package org.netbeans.modules.junit;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.loaders.*;
import org.openide.src.*;
import org.openide.filesystems.*;
import org.openide.cookies.*;
import org.openide.execution.*;
import org.openide.debugger.*;
import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import junit.framework.*;

public class RunTestActionTest extends TestCase {
    
    public RunTestActionTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(RunTestActionTest.class);
        
        return suite;
    }
    
    /** Test of getName method, of class org.netbeans.modules.junit.RunTestAction. */
    public void testGetName() {
        System.out.println("testGetName");
        String name = TO.getName();
        assert(null != name);
    }
    
    /** Test of getHelpCtx method, of class org.netbeans.modules.junit.RunTestAction. */
    public void testGetHelpCtx() {
        System.out.println("testGetHelpCtx");
        HelpCtx hc = TO.getHelpCtx();
        assert(null != hc);
    }
    
    /** Test of cookieClasses method, of class org.netbeans.modules.junit.RunTestAction. */
    public void testCookieClasses() {
        System.out.println("testCookieClasses");
        Class[] c = TO.cookieClasses();
        assert(null != c);
    }
    
    /** Test of iconResource method, of class org.netbeans.modules.junit.RunTestAction. */
    public void testIconResource() {
        System.out.println("testIconResource");
        String icon = TO.iconResource();
        assert(null != icon);
    }
    
    /** Test of mode method, of class org.netbeans.modules.junit.RunTestAction. */
    public void testMode() {
        System.out.println("testMode");
        TO.mode();
    }
    
    /** Test of performAction method, of class org.netbeans.modules.junit.RunTestAction. */
    public void testPerformAction() {
        System.out.println("testPerformAction");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /* protected members */
    protected CreateTestAction TO = null;
    
    protected void setUp() {
        if (null == TO)
            TO = (CreateTestAction)CreateTestAction.findObject(CreateTestAction.class, true);
    }

    protected void tearDown() {
    }
}

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
        assertTrue(null != name);
    }
    
    /** Test of getHelpCtx method, of class org.netbeans.modules.junit.RunTestAction. */
    public void testGetHelpCtx() {
        System.out.println("testGetHelpCtx");
        HelpCtx hc = TO.getHelpCtx();
        assertTrue(null != hc);
    }
    
    /** Test of cookieClasses method, of class org.netbeans.modules.junit.RunTestAction. */
    public void testCookieClasses() {
        System.out.println("testCookieClasses");
        Class[] c = TO.cookieClasses();
        assertTrue(null != c);
    }
    
    /** Test of iconResource method, of class org.netbeans.modules.junit.RunTestAction. */
    public void testIconResource() {
        System.out.println("testIconResource");
        String icon = TO.iconResource();
        assertTrue(null != icon);
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

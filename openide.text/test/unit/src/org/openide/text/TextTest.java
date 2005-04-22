/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.openide.text;


import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.text.CloneableEditorSupport;


/**
 * Regression tests.
 *
 * How to run from IDE:
 *   1. Mount jar: junit.jar
 *   2. Mount dir: openide/src
 *   3. Mount dir: openide/test/regr/src
 *   4. Run class TextTest from dir openide/test/regr/src in internal execution 
 *   (inside IDE VM - set execution type in Properties window)
 *   It will open new window in Editor. When deadlock is there IDE hangs.
 * How to run from command line: 
 *   In directory: <NetBeans>/openide/test/
 *   Command: ant -Dxtest.attributes=regr
 *
 * @author  Marek Slama, Yarda Tulach
 */
public class TextTest extends NbTestCase {

    /** Creates new TextTest */
    public TextTest(String s) {
        super(s);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(TextTest.class));
    }


    /** Regression test to reproduce deadlock from bug #10449. */
    public void testDeadlock() throws Exception {
        System.out.println(System.currentTimeMillis() + " testDeadlock START");

        CloneableEditorSupport.Env env = new EmptyCESHidden.Env ();
        CloneableEditorSupport tst = new EmptyCESHidden(env);
        
        ((EmptyCESHidden.Env)env).setInstance(tst);

        Object doc = tst.openDocument();

        tst.open();

        System.out.println(System.currentTimeMillis() + " testDeadlock END");
    }
    
    protected void setUp() {
    }
    
    protected void tearDown() {
    }

}

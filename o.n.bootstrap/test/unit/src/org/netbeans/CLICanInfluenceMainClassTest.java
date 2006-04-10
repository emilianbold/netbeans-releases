/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans;

import org.fakepkg.FakeHandler;
import org.netbeans.junit.NbTestCase;

/** Tests that handler can set netbeans.mainclass property in its constructor.
 *
 * @author Jaroslav Tulach
 */
public class CLICanInfluenceMainClassTest extends NbTestCase {
    private static boolean called;
    
    public CLICanInfluenceMainClassTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    public static void main(String[] args) {
        called = true;
    }

    public void testCLIChangesMainClass() throws Exception {
        class R implements Runnable {
            public void run() {
                System.setProperty("netbeans.mainclass", CLICanInfluenceMainClassTest.class.getName());
            }
        }
        
        FakeHandler.toRun = new R();
        
        org.netbeans.MainImpl.main(new String[] { "--userdir", getWorkDirPath() });
        
        assertTrue("Our main method has been called", called);
    }
}

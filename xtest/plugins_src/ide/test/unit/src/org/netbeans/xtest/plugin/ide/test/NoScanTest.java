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
package org.netbeans.xtest.plugin.ide.test;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.progress.module.Controller;
import org.netbeans.progress.spi.InternalHandle;

/** Test netbeans.javacore.noscan=true is propagated to NetBeans IDE and
 * it works correctly. */
public class NoScanTest extends NbTestCase {
    
    /** Creates a new test.
     * @param name test name
     */
    public NoScanTest(String name) {
        super(name);
    }
    
    /** Create suite. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new NoScanTest("testNoScan"));
        return suite;
    }
    
    /** Set up. */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }

    private static boolean progressAppeared = false;
            
    /** Test netbeans.javacore.noscan=true is propagated to NetBeans IDE and
     * it works correctly. */
    public void testNoScan() throws Exception {
        Thread checkProgressThread = new Thread(new Runnable() {
            public void run() {
                while(!progressAppeared) {
                    InternalHandle[] handles = Controller.getDefault().getModel().getHandles();
                    if(handles.length > 0) {
                        progressAppeared = true;
                        return;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        });
        checkProgressThread.start();
        ProjectSupport.createProject(getWorkDir(), "SampleProject");
        checkProgressThread.interrupt();
        assertFalse("Progress bar should not appear because scanning is disabled by property netbeans.javacore.noscan=true", progressAppeared);
    }
}
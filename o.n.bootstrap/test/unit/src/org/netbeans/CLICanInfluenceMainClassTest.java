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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.gdb;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger.State;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.GdbBreakpoint;

/**
 *
 * @author gordonp
 */
public class GdbDebuggerTestCase extends GdbTestCase {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        startTest(getName());
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        doFinish();
    }

    public GdbDebuggerTestCase(String name) {
        super(name);
    }

    /** Test of startDebugger method, of class GdbDebugger */
    @Test
    public void testStartDebugger1() {
        String tfile = System.getProperty("java.io.tmpdir") + "test" + System.currentTimeMillis();
        File file = new File(tfile);
        if (file.exists()) {
            file.delete();
        }

        startDebugger("TmpTouch_1", "TmpTouch_1/tmptouch", tfile);
        debugger.resume();
        tlog("Tmp file is " + tfile);
        waitForState(State.EXITED);
        if (!file.exists()) {
            tlog("Failing because [" + tfile + "] doesn't exist!");
            fail("log " + tfile + " does not exist");
        }
        file.delete();
    }

    /** Test of startDebugger method, of class GdbDebugger */
    @Test
    public void testGetGdbVersion() {
        startDebugger("Args_1", "Args_1/args", "1111 2222 3333");
        double version = debugger.getGdbVersion();
        debugger.resume();
        tlog("gdbVersion is " + version);
        waitForState(State.EXITED);
    }

    /** Test of setting a line breakpoint */
    @Test
    public void testLineBreakpoint1() {
        startDebugger("BpTestProject", "BpTestProject/main", "");

        GdbBreakpoint b1 = setLineBreakpoint("bp.h", 31);
        GdbBreakpoint b2 = setLineBreakpoint("testf/bp.h", 31);

        debugger.resume();

        // should stop on the first breakpoint
        waitForBreakpoint(b1);

        debugger.resume();
        
        // should stop on the second breakpoint
        waitForBreakpoint(b2);

        debugger.resume();
        waitForState(State.EXITED);
    }

    /** Test of setting a function breakpoint */
    @Test
    public void testFunctionBreakpoint1() {
        startDebugger("BpTestProject", "BpTestProject/main", "");

        GdbBreakpoint b1 = setFunctionBreakpoint("foo1");
        GdbBreakpoint b2 = setFunctionBreakpoint("foo2");

        debugger.resume();

        // should stop on the first breakpoint
        waitForBreakpoint(b1);

        debugger.resume();

        // should stop on the second breakpoint
        waitForBreakpoint(b2);

        debugger.resume();
        waitForState(State.EXITED);
    }
}
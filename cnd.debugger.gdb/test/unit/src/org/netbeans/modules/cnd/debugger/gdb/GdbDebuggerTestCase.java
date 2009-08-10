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
import org.junit.Test;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger.State;
import org.netbeans.modules.cnd.debugger.common.breakpoints.CndBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.models.GdbWatchVariable;
import org.netbeans.modules.nativeexecution.test.Conditional;

/**
 *
 * @author gordonp
 */
public class GdbDebuggerTestCase extends GdbTestCase {

    public GdbDebuggerTestCase(String name) {
        super(name);
    }

    /** Test of startDebugger method, of class GdbDebugger */
    @Test
    @Conditional(section="gdb", key="functional.tests")
    public void testStartDebugger1() {
        String tfile = System.getProperty("java.io.tmpdir") + "test" + System.currentTimeMillis();
        File file = new File(tfile);
        if (file.exists()) {
            file.delete();
        }

        startDebugger("TmpTouch_1", "tmptouch", tfile);
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
    @Conditional(section="gdb", key="functional.tests")
    public void testGetGdbVersion() {
        startDebugger("Args_1", "args", "1111 2222 3333");
        double version = debugger.getGdbVersion();
        debugger.resume();
        tlog("gdbVersion is " + version);
        waitForState(State.EXITED);
    }

    /** Test start and normal exit */
    @Test
    @Conditional(section="gdb", key="functional.tests")
    public void testNormalExit() {
        startDebugger("BpTestProject", "main", "");

        debugger.resume();
        waitForState(State.EXITED);
    }

    /** Test of setting a line breakpoint */
    @Test
    @Conditional(section="gdb", key="functional.tests")
    public void testLineBreakpointSet() {
        startDebugger("BpTestProject", "main", "");

        CndBreakpoint b1 = setLineBreakpoint("bp.h", 31);
        CndBreakpoint b2 = setLineBreakpoint("testf/bp.h", 31);

        debugger.resume();

        // should stop on the first breakpoint
        waitForBreakpoint(b1);

        debugger.resume();
        
        // should stop on the second breakpoint
        waitForBreakpoint(b2);

        debugger.resume();
        waitForState(State.EXITED);
    }

    /** Test of setting and disabling a line breakpoint */
    @Test
    @Conditional(section="gdb", key="functional.tests")
    public void testLineBreakpointDisable() {
        startDebugger("BpTestProject", "main", "");

        CndBreakpoint b1 = setLineBreakpoint("bp.h", 31);
        b1.disable();
        
        CndBreakpoint b2 = setLineBreakpoint("testf/bp.h", 31);

        debugger.resume();
        // should stop on the second breakpoint only
        waitForBreakpoint(b2);

        debugger.resume();
        waitForState(State.EXITED);
    }

    /** Test of setting and removing a line breakpoint */
    @Test
    @Conditional(section="gdb", key="functional.tests")
    public void testLineBreakpointDelete() {
        startDebugger("BpTestProject", "main", "");

        CndBreakpoint b1 = setLineBreakpoint("bp.h", 31);
        dm.removeBreakpoint(b1);

        CndBreakpoint b2 = setLineBreakpoint("testf/bp.h", 31);

        debugger.resume();
        // should stop on the second breakpoint only
        waitForBreakpoint(b2);

        debugger.resume();
        waitForState(State.EXITED);
    }

    /** Test of setting a function breakpoint */
    @Test
    @Conditional(section="gdb", key="functional.tests")
    public void testFunctionBreakpointSet() {
        startDebugger("BpTestProject", "main", "");

        CndBreakpoint b1 = setFunctionBreakpoint("foo1");
        CndBreakpoint b2 = setFunctionBreakpoint("foo2");

        debugger.resume();

        // should stop on the first breakpoint
        waitForBreakpoint(b1);

        debugger.resume();

        // should stop on the second breakpoint
        waitForBreakpoint(b2);

        debugger.resume();
        waitForState(State.EXITED);
    }

    /** Test of setting a watch */
    @Test
    @Conditional(section="gdb", key="functional.tests")
    public void testWatchEvaluation() {
        startDebugger("BpTestProject", "main", "");

        // Set a breakpoint inside foo2()
        CndBreakpoint b2 = setLineBreakpoint("testf/bp.h", 31);

        // go there
        debugger.resume();
        waitForBreakpoint(b2);

        // Set a breakpoint inside foo1()
        CndBreakpoint b1 = setLineBreakpoint("bp.h", 31);

        // Create a watch on foo1
        Watch watch = dm.createWatch("foo1()");
        GdbWatchVariable var = new GdbWatchVariable(debugger, watch);

        var.getType();
        var.getValue();

        // it should not stop on the breakpoint during evaluation
        CallStackFrame csf = debugger.getCurrentCallStackFrame();
        assertEquals(b2.getPath(), csf.getFullname());
        assertEquals(b2.getLineNumber(), csf.getLineNumber());

        debugger.resume();
        waitForState(State.EXITED);
    }
}
/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda;

import org.netbeans.api.debugger.Breakpoint;

import java.net.URL;
import java.io.IOException;

import com.sun.jdi.connect.IllegalConnectorArgumentsException;

/**
 * Tests line breakpoints at various places.
 *
 * @author Maros Sandor
 */
public class LineBreakpointTest extends DebuggerJPDAApiTestBase {

    private JPDASupport     support;
    private JPDADebugger    debugger;
    private String          urlString;

    public LineBreakpointTest(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        super.setUp();
        ClassLoader cl = this.getClass().getClassLoader();
        URL url = cl.getResource("basic/LineBreakpointApp.class");
        urlString = url.toString();
    }

    public void testMultipleLineBreakpoints() throws Exception {
        try {
            LineBreakpoint lb1 = LineBreakpoint.create(urlString, 26);
            LineBreakpoint lb2 = LineBreakpoint.create(urlString, 31);
            LineBreakpoint lb3 = LineBreakpoint.create(urlString, 103);
            LineBreakpoint lb4 = LineBreakpoint.create(urlString, 86);
            LineBreakpoint lb5 = LineBreakpoint.create(urlString, 35);

            dm.addBreakpoint(lb1);
            dm.addBreakpoint(lb2);
            dm.addBreakpoint(lb3);
            dm.addBreakpoint(lb4);
            dm.addBreakpoint(lb5);

            TestBreakpointListener tb1 = new TestBreakpointListener(lb1);
            lb1.addJPDABreakpointListener(tb1);
            TestBreakpointListener tb2 = new TestBreakpointListener(lb2);
            lb2.addJPDABreakpointListener(tb2);
            TestBreakpointListener tb3 = new TestBreakpointListener(lb3);
            lb3.addJPDABreakpointListener(tb3);
            TestBreakpointListener tb4 = new TestBreakpointListener(lb4);
            lb4.addJPDABreakpointListener(tb4);
            TestBreakpointListener tb5 = new TestBreakpointListener(lb5);
            lb5.addJPDABreakpointListener(tb5);

            support = JPDASupport.listen("basic.LineBreakpointApp", false);
            debugger = support.getDebugger();

            support.waitState(JPDADebugger.STATE_STOPPED, 5000);  // 1st breakpoint hit
            assertEquals("Debugger stopped at wrong line", lb1.getLineNumber(), debugger.getCurrentCallStackFrame().getLineNumber(null));

            support.doContinue();
            support.waitState(JPDADebugger.STATE_STOPPED, 5000);  // 2nd breakpoint hit
            assertEquals("Debugger stopped at wrong line", lb2.getLineNumber(), debugger.getCurrentCallStackFrame().getLineNumber(null));

            support.doContinue();
            support.waitState(JPDADebugger.STATE_STOPPED, 5000);  // 3rd breakpoint hit
            assertEquals("Debugger stopped at wrong line", lb3.getLineNumber(), debugger.getCurrentCallStackFrame().getLineNumber(null));

            support.doContinue();
            support.waitState(JPDADebugger.STATE_STOPPED, 5000);  // 4th breakpoint hit
            assertEquals("Debugger stopped at wrong line", lb4.getLineNumber(), debugger.getCurrentCallStackFrame().getLineNumber(null));

            support.doContinue();
            support.waitState(JPDADebugger.STATE_STOPPED, 5000);  // 5th breakpoint hit
            assertEquals("Debugger stopped at wrong line", lb5.getLineNumber(), debugger.getCurrentCallStackFrame().getLineNumber(null));

            tb1.assertFailure();
            tb2.assertFailure();
            tb3.assertFailure();
            tb4.assertFailure();
            tb5.assertFailure();

            dm.removeBreakpoint(lb1);
            dm.removeBreakpoint(lb2);
            dm.removeBreakpoint(lb3);
            dm.removeBreakpoint(lb4);
            dm.removeBreakpoint(lb5);
            support.doContinue();
            support.waitDisconnected(5000);
        } finally {
            support.doFinish();
        }
    }

    public void testStaticBlockBreakpoint() throws Exception {
        doTestBreakpointComplete(23);
        doTestBreakpointComplete(26);
    }

    public void testStaticInnerClassBreakpoint() throws Exception {
        doTestBreakpointComplete(69);
        doTestBreakpointComplete(72);
        doTestBreakpointComplete(86);
    }

    public void testMainLineBreakpoint() throws Exception {
        doTestBreakpointComplete(30);
    }

    public void testConstructorLineBreakpoint() throws Exception {
        doTestBreakpointComplete(45);
    }

    public void testInnerLineBreakpoint() throws Exception {
        doTestBreakpointComplete(96);
        doTestBreakpointComplete(99);
        doTestBreakpointComplete(107);
    }

    private void doTestBreakpointComplete(int line) throws IOException, IllegalConnectorArgumentsException,
            DebuggerStartException {
        try {
            LineBreakpoint lb = doTestBreakpoint(line);
            support.doContinue();
            support.waitDisconnected(5000);
            dm.removeBreakpoint(lb);
        } finally {
            support.doFinish();
        }
    }

    private LineBreakpoint doTestBreakpoint(int line) throws IOException, IllegalConnectorArgumentsException,
            DebuggerStartException {
        LineBreakpoint lb = LineBreakpoint.create(urlString, line);
        TestBreakpointListener tbl = new TestBreakpointListener(lb);
        lb.addJPDABreakpointListener(tbl);
        dm.addBreakpoint(lb);

        support = JPDASupport.listen("basic.LineBreakpointApp", false);
        debugger = support.getDebugger();

        support.waitState(JPDADebugger.STATE_STOPPED, 5000);

        tbl.assertFailure();
        return lb;
    }

    private class TestBreakpointListener implements JPDABreakpointListener {

        private Breakpoint      bpt;

        private JPDABreakpointEvent event;
        private AssertionError      failure;

        public TestBreakpointListener(Breakpoint bpt) {
            this.bpt = bpt;
        }

        public void breakpointReached(JPDABreakpointEvent event) {
            try {
                checkEvent(event);
            } catch (AssertionError e) {
                failure = e;
            } catch (Throwable e) {
                failure = new AssertionError(e);
            }
        }

        private void checkEvent(JPDABreakpointEvent event) {
            this.event = event;
            assertEquals("Breakpoint event: Wrong source", bpt, event.getSource());
            assertEquals("Breakpoint event: Condition evaluation failed", JPDABreakpointEvent.CONDITION_NONE, event.getConditionResult());
            assertNotNull("Breakpoint event: Context thread is null", event.getThread());
//            assertNotNull("Breakpoint event: No reference type information", event.getReferenceType());
//            assertEquals("Breakpoint event: Wrong class name", "basic.LineBreakpointApp", event.getReferenceType().name());
        }

        public void assertFailure() {
            if (event == null) throw new AssertionError("Breakpoint was not hit (listener was not notified)");
            if (failure != null) throw failure;
        }
    }
}


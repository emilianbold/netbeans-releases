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

/**
 * Tests thread breakpoints.
 *
 * @author Maros Sandor
 */
public class ThreadBreakpointTest extends DebuggerJPDAApiTestBase {

    private JPDASupport     support;
    private JPDADebugger    debugger;

    private static final String CLASS_NAME = "org.netbeans.api.debugger.jpda.testapps.ThreadBreakpointApp";

    public ThreadBreakpointTest(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testMethodBreakpoints() throws Exception {
        try {
            ThreadBreakpoint tb1 = ThreadBreakpoint.create();
            tb1.setBreakpointType(ThreadBreakpoint.TYPE_THREAD_STARTED_OR_DEATH);
            TestBreakpointListener tbl = new TestBreakpointListener(10);
            tb1.addJPDABreakpointListener(tbl);
            dm.addBreakpoint(tb1);

            support = JPDASupport.listen(CLASS_NAME, false);
            debugger = support.getDebugger();

            for (;;) {
                support.waitStates(DebuggerConstants.STATE_STOPPED, DebuggerConstants.STATE_DISCONNECTED, 10000);
                if (debugger.getState() == DebuggerConstants.STATE_DISCONNECTED) break;
                support.doContinue();
            }
            tbl.assertFailure();

            dm.removeBreakpoint(tb1);
        } finally {
            support.doFinish();
        }
    }

    private class TestBreakpointListener implements JPDABreakpointListener {

        private int                 hitCount;
        private AssertionError      failure;
        private int                 expectedHitCount;

        public TestBreakpointListener(int expectedHitCount) {
            this.expectedHitCount = expectedHitCount;
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
//            ThreadBreakpoint tb = (ThreadBreakpoint) event.getSource();
            assertEquals("Breakpoint event: Condition evaluation failed", DebuggerConstants.CONDITION_NONE, event.getConditionResult());
            assertNotNull("Breakpoint event: Context thread is null", event.getThread());
            JPDAThread thread = event.getThread();
            if (thread.getName().startsWith("test-"))
            {
                JPDAThreadGroup group = thread.getParentThreadGroup();
                assertEquals("Wrong thread group", "testgroup", group.getName());
                assertEquals("Wrong parent thread group", "main", group.getParentThreadGroup().getName());
                assertEquals("Wrong number of child thread groups", 0, group.getThreadGroups().length);
                JPDAThread [] threads = group.getThreads();
                for (int i = 0; i < threads.length; i++) {
                    JPDAThread jpdaThread = threads[i];
                    if (!jpdaThread.getName().startsWith("test-")) throw new AssertionError("Thread group contains an alien thread");
                    assertSame("Child/parent mismatch", jpdaThread.getParentThreadGroup(), group);
                }
                hitCount++;
            }
        }

        public void assertFailure() {
            if (failure != null) throw failure;
            assertEquals("Breakpoint hit count mismatch", expectedHitCount, hitCount);
        }
    }
}

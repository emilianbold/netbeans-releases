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

import org.netbeans.spi.viewmodel.NoInformationException;

/**
 * Tests field breakpoints.
 *
 * @author Maros Sandor
 */
public class FieldBreakpointTest extends DebuggerJPDAApiTestBase {

    private JPDASupport     support;
    private JPDADebugger    debugger;

    private static final String CLASS_NAME = "org.netbeans.api.debugger.jpda.testapps.FieldBreakpointApp";

    public FieldBreakpointTest(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testFieldBreakpoints() throws Exception {
        try {
            FieldBreakpoint fb1 = FieldBreakpoint.create(CLASS_NAME, "x", DebuggerConstants.TYPE_MODIFICATION);
            // TODO: We have to set initial value of 2 because of bug #4984092, remove for Tiger
            TestBreakpointListener tbl = new TestBreakpointListener("x", 2, new int [] { 31 });
            fb1.addJPDABreakpointListener(tbl);
            dm.addBreakpoint(fb1);

            FieldBreakpoint fb2 = FieldBreakpoint.create(CLASS_NAME, "y", DebuggerConstants.TYPE_MODIFICATION);
            TestBreakpointListener tb2 = new TestBreakpointListener("y", 0, new int [] { 38, 41, 45 });
            fb2.addJPDABreakpointListener(tb2);
            dm.addBreakpoint(fb2);

            FieldBreakpoint fb3 = FieldBreakpoint.create(CLASS_NAME + "$InnerStatic", "q", DebuggerConstants.TYPE_MODIFICATION);
            TestBreakpointListener tb3 = new TestBreakpointListener("InnerStatic.q", 0, new int [] { });
            fb3.addJPDABreakpointListener(tb3);
            dm.addBreakpoint(fb3);

            FieldBreakpoint fb4 = FieldBreakpoint.create(CLASS_NAME + "$InnerStatic", "w", DebuggerConstants.TYPE_MODIFICATION);
            TestBreakpointListener tb4 = new TestBreakpointListener("InnerStatic.w", 0, new int [] { 75, 78, 82 });
            fb4.addJPDABreakpointListener(tb4);
            dm.addBreakpoint(fb4);

            FieldBreakpoint fb5 = FieldBreakpoint.create(CLASS_NAME + "$Inner", "w", DebuggerConstants.TYPE_MODIFICATION);
            TestBreakpointListener tb5 = new TestBreakpointListener("Inner.w", 0, new int [] { 96, 99, 103 });
            fb5.addJPDABreakpointListener(tb5);
            dm.addBreakpoint(fb5);

            support = JPDASupport.listen(CLASS_NAME, false);
            debugger = support.getDebugger();

            for (;;) {
                support.waitStates(DebuggerConstants.STATE_STOPPED, DebuggerConstants.STATE_DISCONNECTED, 10000);
                if (debugger.getState() == DebuggerConstants.STATE_DISCONNECTED) break;
                support.doContinue();
            }
            tbl.assertFailure();
            tb2.assertFailure();
            tb3.assertFailure();
            tb4.assertFailure();
            tb5.assertFailure();

            dm.removeBreakpoint(fb1);
            dm.removeBreakpoint(fb2);
            dm.removeBreakpoint(fb3);
            dm.removeBreakpoint(fb4);
            dm.removeBreakpoint(fb5);
        } finally {
            support.doFinish();
        }
    }

    private class TestBreakpointListener implements JPDABreakpointListener {

        private int                 hitCount;
        private int                 currentFieldValue;
        private AssertionError      failure;
        private String              variableName;
        private int[]               hitLines;

        public TestBreakpointListener(String variableName, int initialValue, int [] hitLines) {
            this.variableName = variableName;
            this.hitLines = hitLines;
            currentFieldValue = initialValue;
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

        private void checkEvent(JPDABreakpointEvent event) throws NoInformationException {
            FieldBreakpoint fb = (FieldBreakpoint) event.getSource();

            if (hitCount >= hitLines.length) throw new AssertionError("Breakpoint hit too many times for " + variableName + ": " + hitCount + " at " + getLine(event));
            int hitLine = hitLines[hitCount++];
            assertEquals("Breakpoint event: Condition evaluation failed", DebuggerConstants.CONDITION_NONE, event.getConditionResult());
            assertNotNull("Breakpoint event: Context thread is null", event.getThread());
            assertEquals("Breakpoint event: Hit at wrong place", hitLine, getLine(event));
            Variable var = event.getVariable();
            assertNotNull("Breakpoint event: No variable information", var);

            if (fb.getBreakpointType() == DebuggerConstants.TYPE_ACCESS) {
                assertEquals("Breakpoint event: Wrong field value", Integer.toString(currentFieldValue), var.getValue());
            } else {
                currentFieldValue ++;
                assertEquals("Breakpoint event: Wrong field value of " + fb.getFieldName() + " at " + getLine(event), Integer.toString(currentFieldValue), var.getValue());
            }
        }

        public void assertFailure() {
            if (failure != null) throw failure;
            assertEquals("Breakpoint hit count mismatch for: " + variableName, hitLines.length, hitCount);
        }
    }
}

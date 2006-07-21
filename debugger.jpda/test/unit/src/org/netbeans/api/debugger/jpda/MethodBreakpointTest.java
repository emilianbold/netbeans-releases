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

package org.netbeans.api.debugger.jpda;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;


/**
 * Tests method breakpoints.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class MethodBreakpointTest extends NbTestCase {

    private JPDASupport     support;
    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();

    private static final String CLASS_NAME =
        "org.netbeans.api.debugger.jpda.testapps.MethodBreakpointApp";

    public MethodBreakpointTest (String s) {
        super (s);
    }

    public void testMethodEntryBreakpoints() throws Exception {
        try {
            MethodBreakpoint mb1 = MethodBreakpoint.create (CLASS_NAME, "a");
            TestBreakpointListener tbl = new TestBreakpointListener 
                ("a", 46, 1);
            mb1.addJPDABreakpointListener (tbl);
            dm.addBreakpoint(mb1);

            MethodBreakpoint mb2 = MethodBreakpoint.create (CLASS_NAME, "b");
            TestBreakpointListener tb2 = new TestBreakpointListener 
                ("b", 51, 2);
            mb2.addJPDABreakpointListener (tb2);
            dm.addBreakpoint (mb2);

            MethodBreakpoint mb3 = MethodBreakpoint.create (CLASS_NAME, "c");
            TestBreakpointListener tb3 = new TestBreakpointListener
                ("c", 55, 4);
            mb3.addJPDABreakpointListener (tb3);
            dm.addBreakpoint (mb3);

            MethodBreakpoint mb4 = MethodBreakpoint.create 
                (CLASS_NAME, "<init>");
            TestBreakpointListener tb4 = new TestBreakpointListener
                ("<init>", 41, 1);
            mb4.addJPDABreakpointListener (tb4);
            dm.addBreakpoint (mb4);

            MethodBreakpoint mb5 = MethodBreakpoint.create
                (CLASS_NAME, "<clinit>");
            TestBreakpointListener tb5 = new TestBreakpointListener
                ("<clinit>", 38, 1);
            mb5.addJPDABreakpointListener (tb5);
            dm.addBreakpoint (mb5);

            MethodBreakpoint mb6 = MethodBreakpoint.create (
                CLASS_NAME + "$InnerStatic", 
                "<clinit>"
            );
            TestBreakpointListener tb6 = new TestBreakpointListener (
                "InnerStatic.<clinit>", 
                60, 
                1
            );
            mb6.addJPDABreakpointListener (tb6);
            dm.addBreakpoint (mb6);

            MethodBreakpoint mb7 = MethodBreakpoint.create (
                CLASS_NAME + "$InnerStatic", 
                "getW"
            );
            TestBreakpointListener tb7 = new TestBreakpointListener (
                "InnerStatic.getW", 
                81, 
                1
            );
            mb7.addJPDABreakpointListener (tb7);
            dm.addBreakpoint (mb7);

            MethodBreakpoint mb8 = MethodBreakpoint.create (
                CLASS_NAME + "$Inner", 
                "<init>"
            );
            TestBreakpointListener tb8 = new TestBreakpointListener (
                "Inner.<init>", 
                93, 
                4
            );
            mb8.addJPDABreakpointListener (tb8);
            dm.addBreakpoint (mb8);

            MethodBreakpoint mb9 = MethodBreakpoint.create (
                CLASS_NAME + "$Inner", 
                "getW"
            );
            TestBreakpointListener tb9 = new TestBreakpointListener (
                "Inner.getW", 
                98, 
                8
            );
            mb9.addJPDABreakpointListener (tb9);
            dm.addBreakpoint (mb9);

            support = JPDASupport.attach (CLASS_NAME);

            for (;;) {
                support.waitState (JPDADebugger.STATE_STOPPED);
                if (support.getDebugger ().getState () == 
                    JPDADebugger.STATE_DISCONNECTED
                ) break;
                support.doContinue ();
            }
            tbl.assertFailure ();
            tb2.assertFailure ();
            tb3.assertFailure ();
            tb4.assertFailure ();
            tb5.assertFailure ();
            tb6.assertFailure ();
            tb7.assertFailure ();
            tb8.assertFailure ();
            tb9.assertFailure ();

            dm.removeBreakpoint (mb1);
            dm.removeBreakpoint (mb2);
            dm.removeBreakpoint (mb3);
            dm.removeBreakpoint (mb4);
            dm.removeBreakpoint (mb5);
            dm.removeBreakpoint (mb6);
            dm.removeBreakpoint (mb7);
            dm.removeBreakpoint (mb8);
            dm.removeBreakpoint (mb9);
        } finally {
            support.doFinish();
        }
    }

    public void testMethodExitBreakpoints() throws Exception {
        try {
            MethodBreakpoint mb1 = MethodBreakpoint.create (
                CLASS_NAME + "$AbstractInner", "compute"
            );
            mb1.setBreakpointType(MethodBreakpoint.TYPE_METHOD_EXIT);
            TestBreakpointListener tbl = new TestBreakpointListener 
                ("compute", 118, 1, "1.0");
            mb1.addJPDABreakpointListener (tbl);
            dm.addBreakpoint(mb1);
            
            MethodBreakpoint mb2 = MethodBreakpoint.create (
                CLASS_NAME + "$InterfaceInner", "getString"
            );
            mb2.setBreakpointType(MethodBreakpoint.TYPE_METHOD_EXIT);
            TestBreakpointListener tb2 = new TestBreakpointListener 
                ("getString", 123, 1, "\"Hello\"");
            mb2.addJPDABreakpointListener (tb2);
            dm.addBreakpoint(mb2);
            
            support = JPDASupport.attach (CLASS_NAME);

            for (;;) {
                support.waitState (JPDADebugger.STATE_STOPPED);
                if (support.getDebugger ().getState () == 
                    JPDADebugger.STATE_DISCONNECTED
                ) break;
                support.doContinue ();
            }
            tbl.assertFailure ();
            tb2.assertFailure ();

            dm.removeBreakpoint (mb1);
            dm.removeBreakpoint (mb2);
        } finally {
            support.doFinish();
        }
    }

    private class TestBreakpointListener implements JPDABreakpointListener {

        private int                 hitCount;
        private AssertionError      failure;
        private String methodName;
        private int hitLine;
        private int expectedHitCount;
        private String returnValue;

        public TestBreakpointListener (
            String methodName, 
            int hitLine, 
            int expectedHitCount
        ) {
            this.methodName = methodName;
            this.hitLine = hitLine;
            this.expectedHitCount = expectedHitCount;
        }

        public TestBreakpointListener (
            String methodName, 
            int hitLine, 
            int expectedHitCount,
            String returnValue
        ) {
            this(methodName, hitLine, expectedHitCount);
            this.returnValue = returnValue;
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

        private void checkEvent (JPDABreakpointEvent event) {
            MethodBreakpoint mb = (MethodBreakpoint) event.getSource ();

            assertEquals (
                "Breakpoint event: Condition evaluation failed", 
                JPDABreakpointEvent.CONDITION_NONE, 
                event.getConditionResult ()
            );
            assertNotNull (
                "Breakpoint event: Context thread is null", 
                event.getThread ()
            );
            assertEquals (
                "Breakpoint event: Hit at wrong place", 
                hitLine, 
                event.getThread ().getLineNumber (null)
            );
            assertEquals (
                "Breakpoint event: Hit at wrong method", 
                mb.getMethodName (), 
                event.getThread ().getMethodName ()
            );
            
            if (returnValue != null && !System.getProperty("java.version").startsWith("1.5")) {
                Variable retVar = event.getVariable();
                assertNotNull(
                        "Breakpoint event: The return value must not be null!",
                        retVar);
                ReturnVariable returnVariable = (ReturnVariable) retVar;
                assertEquals(
                        "Breakpoint event: Wrong method name hit",
                        methodName, returnVariable.methodName());
                assertEquals(
                        "Breakpoint event: Wrong return value",
                        returnValue, returnVariable.getValue());
            }

            hitCount++;
        }

        public void assertFailure () {
            if (failure != null) 
                throw failure;
            assertEquals (
                "Breakpoint hit count mismatch for: " + methodName, 
                expectedHitCount, 
                hitCount
            );
        }
    }
}

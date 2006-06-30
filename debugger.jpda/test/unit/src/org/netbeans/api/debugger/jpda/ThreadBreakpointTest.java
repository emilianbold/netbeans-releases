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
 * Tests thread breakpoints.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class ThreadBreakpointTest extends NbTestCase {

    private JPDASupport     support;
    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();

    private static final String CLASS_NAME = 
        "org.netbeans.api.debugger.jpda.testapps.ThreadBreakpointApp";

    
    public ThreadBreakpointTest (String s) {
        super (s);
    }

    public void testMethodBreakpoints () throws Exception {
        try {
            ThreadBreakpoint tb1 = ThreadBreakpoint.create ();
            tb1.setBreakpointType (ThreadBreakpoint.TYPE_THREAD_STARTED_OR_DEATH);
            TestBreakpointListener tbl = new TestBreakpointListener (10);
            tb1.addJPDABreakpointListener (tbl);
            dm.addBreakpoint (tb1);

            support = JPDASupport.attach (CLASS_NAME);

            for (;;) {
                support.waitState (JPDADebugger.STATE_STOPPED);
                if (support.getDebugger ().getState () == 
                    JPDADebugger.STATE_DISCONNECTED
                ) break;
                support.doContinue ();
            }
            tbl.assertFailure ();

            dm.removeBreakpoint (tb1);
        } finally {
            support.doFinish();
        }
    }

    private class TestBreakpointListener implements JPDABreakpointListener {

        private int                 hitCount;
        private AssertionError      failure;
        private int                 expectedHitCount;

        public TestBreakpointListener (int expectedHitCount) {
            this.expectedHitCount = expectedHitCount;
        }

        public void breakpointReached (JPDABreakpointEvent event) {
            try {
                checkEvent (event);
            } catch (AssertionError e) {
                failure = e;
            } catch (Throwable e) {
                failure = new AssertionError (e);
            }
        }

        private void checkEvent (JPDABreakpointEvent event) {
//            ThreadBreakpoint tb = (ThreadBreakpoint) event.getSource();
            assertEquals (
                "Breakpoint event: Condition evaluation failed", 
                JPDABreakpointEvent.CONDITION_NONE, 
                event.getConditionResult ()
            );
            assertNotNull (
                "Breakpoint event: Context thread is null", 
                event.getThread ()
            );
            JPDAThread thread = event.getThread ();
            if (thread.getName ().startsWith ("test-")) {
                JPDAThreadGroup group = thread.getParentThreadGroup ();
                assertEquals (
                    "Wrong thread group", 
                    "testgroup", 
                    group.getName ()
                );
                assertEquals (
                    "Wrong parent thread group", 
                    "main", 
                    group.getParentThreadGroup ().getName ()
                );
                assertEquals (
                    "Wrong number of child thread groups", 
                    0, 
                    group.getThreadGroups ().length
                );
                JPDAThread [] threads = group.getThreads ();
                for (int i = 0; i < threads.length; i++) {
                    JPDAThread jpdaThread = threads [i];
                    if ( !jpdaThread.getName ().startsWith ("test-")) 
                        throw new AssertionError 
                            ("Thread group contains an alien thread");
                    assertSame (
                        "Child/parent mismatch", 
                        jpdaThread.getParentThreadGroup (), 
                        group
                    );
                }
                hitCount++;
            }
        }

        public void assertFailure () {
            if (failure != null) throw failure;
            assertEquals (
                "Breakpoint hit count mismatch", 
                expectedHitCount, 
                hitCount
            );
        }
    }
}

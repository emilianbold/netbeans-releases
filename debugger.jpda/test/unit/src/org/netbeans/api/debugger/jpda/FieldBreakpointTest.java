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

import com.sun.jdi.AbsentInformationException;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;
import org.openide.util.RequestProcessor;

/**
 * Tests field breakpoints.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class FieldBreakpointTest extends NbTestCase {

    private JPDASupport     support;
    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();

    private static final String CLASS_NAME = 
            "org.netbeans.api.debugger.jpda.testapps.FieldBreakpointApp";

    public FieldBreakpointTest (String s) {
        super (s);
    }

    public void testFieldBreakpoints() throws Exception {
        try {
//            Does not work on JDK1.4            
//            FieldBreakpoint fb1 = FieldBreakpoint.create (
//                CLASS_NAME, 
//                "x", 
//                FieldBreakpoint.TYPE_MODIFICATION
//            );
//            TestBreakpointListener tbl = new TestBreakpointListener (
//                "x", 
//                0, 
//                new int [] {23, 26, 31 }
//            );
//            fb1.addJPDABreakpointListener (tbl);
//            dm.addBreakpoint (fb1);

            FieldBreakpoint fb2 = FieldBreakpoint.create (
                CLASS_NAME, 
                "y", 
                FieldBreakpoint.TYPE_MODIFICATION
            );
            TestBreakpointListener tb2 = new TestBreakpointListener (
                "y", 
                0, 
                new int [] { 38, 41, 45 }
            );
            fb2.addJPDABreakpointListener (tb2);
            dm.addBreakpoint (fb2);

//            Does not work on JDK1.4            
//            FieldBreakpoint fb3 = FieldBreakpoint.create (
//                CLASS_NAME + "$InnerStatic", 
//                "q", 
//                FieldBreakpoint.TYPE_MODIFICATION
//            );
//            TestBreakpointListener tb3 = new TestBreakpointListener (
//                "InnerStatic.q", 
//                0, 
//                new int [] {69, 72 }
//            );
//            fb3.addJPDABreakpointListener (tb3);
//            dm.addBreakpoint (fb3);

            FieldBreakpoint fb4 = FieldBreakpoint.create (
                CLASS_NAME + "$InnerStatic", 
                "w", 
                FieldBreakpoint.TYPE_MODIFICATION
            );
            TestBreakpointListener tb4 = new TestBreakpointListener (
                "InnerStatic.w", 
                0, 
                new int [] { 75, 78, 82 }
            );
            fb4.addJPDABreakpointListener (tb4);
            dm.addBreakpoint (fb4);

            FieldBreakpoint fb5 = FieldBreakpoint.create (
                CLASS_NAME + "$Inner", 
                "w", 
                FieldBreakpoint.TYPE_MODIFICATION
            );
            TestBreakpointListener tb5 = new TestBreakpointListener (
                "Inner.w", 
                0, 
                new int [] { 96, 99, 103 }
            );
            fb5.addJPDABreakpointListener (tb5);
            dm.addBreakpoint (fb5);

            support = JPDASupport.attach (CLASS_NAME);
            for (;;) {
                support.waitState (JPDADebugger.STATE_STOPPED);
                if ( support.getDebugger ().getState () == 
                     JPDADebugger.STATE_DISCONNECTED
                ) 
                    break;
                support.doContinue ();
            }
            
//            tbl.assertFailure ();
            tb2.assertFailure ();
//            tb3.assertFailure ();
            tb4.assertFailure ();
            tb5.assertFailure ();

//            dm.removeBreakpoint (fb1);
            dm.removeBreakpoint (fb2);
//            dm.removeBreakpoint (fb3);
            dm.removeBreakpoint (fb4);
            dm.removeBreakpoint (fb5);
        } finally {
            support.doFinish ();
        }
    }

    private class TestBreakpointListener implements JPDABreakpointListener {

        private int                 hitCount;
        private int                 currentFieldValue;
        private AssertionError      failure;
        private String              variableName;
        private int[]               hitLines;

        public TestBreakpointListener (
                String variableName, 
                int initialValue, 
                int [] hitLines
                ) {
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

        private void checkEvent (JPDABreakpointEvent event) 
        throws AbsentInformationException {
            
            FieldBreakpoint fb = (FieldBreakpoint) event.getSource ();

            System.out.println (
                variableName + " : "  + 
                event.getThread ().getCallStack () [0].getLineNumber (null) + 
                " : " + event.getVariable ().getValue ());
            
            if (hitCount >= hitLines.length) 
                throw new AssertionError (
                    "Breakpoint hit too many times for " + variableName + 
                    ": " + hitCount + " at " + 
                    event.getThread ().getCallStack () [0].getLineNumber (null)
                );
            int hitLine = hitLines [hitCount++];
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
                event.getThread ().getCallStack () [0].getLineNumber (null)
            );
            Variable var = event.getVariable ();
            assertNotNull (
                "Breakpoint event: No variable information", 
                var
            );

            if (fb.getBreakpointType () == FieldBreakpoint.TYPE_ACCESS) {
                assertEquals (
                    "Breakpoint event: Wrong field value", 
                    Integer.toString (currentFieldValue), 
                    var.getValue ()
                );
            } else {
                currentFieldValue ++;
                assertEquals (
                    "Breakpoint event: Wrong field value of " + 
                    fb.getFieldName () + " at " + 
                    event.getThread ().getCallStack () [0].getLineNumber (null), 
                    Integer.toString (currentFieldValue), 
                    var.getValue ()
                );
            }
        }

        public void assertFailure () {
            if (failure != null) throw failure;
            assertEquals (
                "Breakpoint hit count mismatch for: " + variableName, 
                hitLines.length, 
                hitCount
            );
        }
    }
}

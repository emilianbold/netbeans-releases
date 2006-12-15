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

import java.net.URL;
import java.io.IOException;

import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;

/**
 * Tests line breakpoints at various places.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class LineBreakpointTest extends NbTestCase {

    private static final String TEST_APP = Utils.getURL(System.getProperty ("test.dir.src") + 
        "org/netbeans/api/debugger/jpda/testapps/LineBreakpointApp.java");
    
    
    private JPDASupport support;
    
    
    public LineBreakpointTest (String s) {
        super (s);
    }

    public void testConditionalBreakpoint() throws Exception {
        doTestBreakpointComplete (
            39, 
            "x==22", 
            JPDABreakpointEvent.CONDITION_FALSE
        );
        doTestBreakpointComplete (
            40, 
            "x==60", 
            JPDABreakpointEvent.CONDITION_TRUE
        );
    }

    public void testMultipleLineBreakpoints () throws Exception {
        try {
            LineBreakpoint lb1 = LineBreakpoint.create (TEST_APP, 32);
            LineBreakpoint lb2 = LineBreakpoint.create (TEST_APP, 37);
            LineBreakpoint lb3 = LineBreakpoint.create (TEST_APP, 109);
            lb3.setPreferredClassName("org.netbeans.api.debugger.jpda.testapps.LineBreakpoint$Inner");
            LineBreakpoint lb4 = LineBreakpoint.create (TEST_APP, 92);
            lb4.setPreferredClassName("org.netbeans.api.debugger.jpda.testapps.LineBreakpoint$InnerStatic");
            LineBreakpoint lb5 = LineBreakpoint.create (TEST_APP, 41);

            DebuggerManager dm = DebuggerManager.getDebuggerManager ();
            dm.addBreakpoint (lb1);
            dm.addBreakpoint (lb2);
            dm.addBreakpoint (lb3);
            dm.addBreakpoint (lb4);
            dm.addBreakpoint (lb5);

            TestBreakpointListener tb1 = new TestBreakpointListener (lb1);
            lb1.addJPDABreakpointListener (tb1);
            TestBreakpointListener tb2 = new TestBreakpointListener (lb2);
            lb2.addJPDABreakpointListener (tb2);
            TestBreakpointListener tb3 = new TestBreakpointListener (lb3);
            lb3.addJPDABreakpointListener (tb3);
            TestBreakpointListener tb4 = new TestBreakpointListener (lb4);
            lb4.addJPDABreakpointListener (tb4);
            TestBreakpointListener tb5 = new TestBreakpointListener (lb5);
            lb5.addJPDABreakpointListener (tb5);

            support = JPDASupport.attach (
                "org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp"
            );
            JPDADebugger debugger = support.getDebugger();

            support.waitState (JPDADebugger.STATE_STOPPED);  // 1st breakpoint hit
            assertEquals (
                "Debugger stopped at wrong line", 
                lb1.getLineNumber (), 
                debugger.getCurrentCallStackFrame ().getLineNumber (null)
            );

            support.doContinue();
            support.waitState (JPDADebugger.STATE_STOPPED);  // 2nd breakpoint hit
            assertEquals (
                "Debugger stopped at wrong line", 
                lb2.getLineNumber (), 
                debugger.getCurrentCallStackFrame ().getLineNumber (null)
            );

            support.doContinue ();
            support.waitState (JPDADebugger.STATE_STOPPED);  // 3rd breakpoint hit
            assertEquals (
                "Debugger stopped at wrong line", 
                lb3.getLineNumber (), 
                debugger.getCurrentCallStackFrame ().getLineNumber (null)
            );

            support.doContinue ();
            support.waitState (JPDADebugger.STATE_STOPPED);  // 4th breakpoint hit
            assertEquals (
                "Debugger stopped at wrong line", 
                lb4.getLineNumber (), 
                debugger.getCurrentCallStackFrame ().getLineNumber (null)
            );

            support.doContinue ();
            support.waitState (JPDADebugger.STATE_STOPPED);  // 5th breakpoint hit
            assertEquals (
                "Debugger stopped at wrong line", 
                lb5.getLineNumber (), 
                debugger.getCurrentCallStackFrame ().getLineNumber (null)
            );

            tb1.checkResult ();
            tb2.checkResult ();
            tb3.checkResult ();
            tb4.checkResult ();
            tb5.checkResult ();

            dm.removeBreakpoint (lb1);
            dm.removeBreakpoint (lb2);
            dm.removeBreakpoint (lb3);
            dm.removeBreakpoint (lb4);
            dm.removeBreakpoint (lb5);
            support.doContinue ();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            if (support != null) support.doFinish ();
        }
    }

    public void testStaticBlockBreakpoint() throws Exception {
        doTestBreakpointComplete(29);
        doTestBreakpointComplete(32);
    }

    public void testStaticInnerClassBreakpoint() throws Exception {
        doTestBreakpointComplete(75);
        doTestBreakpointComplete(78);
        doTestBreakpointComplete(92);
    }

    public void testMainLineBreakpoint() throws Exception {
        doTestBreakpointComplete(36);
    }

    public void testConstructorLineBreakpoint() throws Exception {
        doTestBreakpointComplete(51);
    }

    public void testInnerLineBreakpoint () throws Exception {
        doTestBreakpointComplete (102);
        doTestBreakpointComplete (105);
        doTestBreakpointComplete (113);
    }

    private void doTestBreakpointComplete (
        int line, 
        String condition, 
        int conditionResult
    ) throws IOException, IllegalConnectorArgumentsException,
    DebuggerStartException {
        try {
            LineBreakpoint lb = doTestBreakpoint (
                line, 
                condition, 
                conditionResult
            );
            if ( condition == null || 
                 conditionResult == JPDABreakpointEvent.CONDITION_TRUE
            ) {
                support.doContinue();
                support.waitState (JPDADebugger.STATE_DISCONNECTED);
            }
            DebuggerManager.getDebuggerManager ().removeBreakpoint (lb);
        } finally {
            if (support != null) support.doFinish();
        }
    }

    private void doTestBreakpointComplete (int line) throws IOException, 
    IllegalConnectorArgumentsException, DebuggerStartException {
        doTestBreakpointComplete (
            line, 
            null, 
            JPDABreakpointEvent.CONDITION_NONE
        );
    }

    private LineBreakpoint doTestBreakpoint (
        int         line, 
        String      condition, 
        int         conditionResult
    ) throws IOException, IllegalConnectorArgumentsException, 
    DebuggerStartException {
        JPDASupport.removeAllBreakpoints ();
        LineBreakpoint lb = LineBreakpoint.create (TEST_APP, line);
        if (73 <= line && line <= 98) {
            lb.setPreferredClassName("org.netbeans.api.debugger.jpda.testapps.LineBreakpoint$InnerStatic");
        } else if (100 <= line && line <= 115) {
            lb.setPreferredClassName("org.netbeans.api.debugger.jpda.testapps.LineBreakpoint$Inner");
        }
        lb.setCondition (condition);
        TestBreakpointListener tbl = new TestBreakpointListener 
            (lb, conditionResult);
        lb.addJPDABreakpointListener (tbl);
        DebuggerManager.getDebuggerManager ().addBreakpoint (lb);

        support = JPDASupport.attach (
            "org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp"
        );

        if ( condition == null || 
             conditionResult == JPDABreakpointEvent.CONDITION_TRUE
        ) {
            support.waitState (JPDADebugger.STATE_STOPPED);
        } else {
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        }

        tbl.checkResult ();
        return lb;
    }

    
    /**
     * Tests debugger's ability to make difference between different projects
     * with the same classes while getting the locations during class-loaded event.
     *
     * 1. The user creates 2 classes: ${test.dir.src}/.../LineBreakpointApp.java
     *    and ${test.dir.src_2}/.../LineBreakpointApp.java
     * 2. Then set a breakpoint in ${test.dir.src_2}/.../LineBreakpointApp.java.
     * 
     * Debugger should stop _only_ in the second project. If debugger stopped in
     * the first one, then assertion violation would arise because of source path
     * equality test.
     */
    public void testBreakpointUnambiguity1 () throws Exception {
        try {
            LineBreakpoint lb1 = LineBreakpoint.create (TEST_APP, 39);
//            lb1.setSourceRoot(System.getProperty ("test.dir.src"));
            DebuggerManager dm = DebuggerManager.getDebuggerManager ();
            dm.addBreakpoint (lb1);
            
            TestBreakpointListener tb1 = new TestBreakpointListener (lb1);
            lb1.addJPDABreakpointListener (tb1);
            
            support = JPDASupport.attach (
                "org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp"
            );
            JPDADebugger debugger = support.getDebugger();

            support.waitState (JPDADebugger.STATE_STOPPED);  // breakpoint hit, the source root is correct
            assertEquals (
                "Debugger stopped at wrong line", 
                lb1.getLineNumber (), 
                debugger.getCurrentCallStackFrame ().getLineNumber (null)
            );

            tb1.checkResult ();
            support.doContinue();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
            dm.removeBreakpoint (lb1);
            support.doFinish ();
            /*
            // Second run - BP should not be hit with a different source root - viz testBreakpointUnambiguity2()
            support = null;
            lb1 = LineBreakpoint.create (TEST_APP, 39);
            lb1.setSourceRoot(System.getProperty ("test.dir.src")+"_2");
            dm = DebuggerManager.getDebuggerManager ();
            dm.addBreakpoint (lb1);
            
            tb1 = new TestBreakpointListener (lb1);
            lb1.addJPDABreakpointListener (tb1);
            
            support = JPDASupport.attach (
                "org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp"
            );
            debugger = support.getDebugger();
            
            support.waitState (JPDADebugger.STATE_STOPPED); // Stopped or disconnected
            assertEquals(
                    "Debugger should not stop on BP with faked source root",
                    debugger.getState(),
                    JPDADebugger.STATE_DISCONNECTED
            );
            tb1.checkNotNotified();
            dm.removeBreakpoint (lb1);
             */
        } finally {
            if (support != null) support.doFinish ();
        }
    }

    /**
     * Tests debugger's ability to make difference between different projects
     * with the same classes while getting the locations during class-loaded event.
     *
     * 1. The user creates 2 classes: ${test.dir.src}/.../LineBreakpointApp.java
     *    and ${test.dir.src_2}/.../LineBreakpointApp.java
     * 2. Then set a breakpoint in ${test.dir.src_2}/.../LineBreakpointApp.java.
     * 
     * Debugger should stop _only_ in the second project. If debugger stopped in
     * the first one, then assertion violation would arise because of source path
     * equality test.
     */
    public void testBreakpointUnambiguity2 () throws Exception {
        try {
            LineBreakpoint lb1 = LineBreakpoint.create(
                    Utils.getURL(System.getProperty ("user.home") + // intentionally bad path
                    java.io.File.separator +
                    "org/netbeans/api/debugger/jpda/testapps/LineBreakpointApp.java"), 39);
            //lb1.setSourceRoot(System.getProperty ("test.dir.src") + "_2");
            DebuggerManager dm = DebuggerManager.getDebuggerManager ();
            dm.addBreakpoint (lb1);
            
            TestBreakpointListener tb1 = new TestBreakpointListener (lb1);
            lb1.addJPDABreakpointListener (tb1);
            
            support = JPDASupport.attach (
                "org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp"
            );
            JPDADebugger debugger = support.getDebugger();

            support.waitState (JPDADebugger.STATE_STOPPED); // Stopped or disconnected
            assertEquals(
                    "Debugger should not stop on BP with faked source root",
                    debugger.getState(),
                    JPDADebugger.STATE_DISCONNECTED
            );
            
            tb1.checkNotNotified();
            dm.removeBreakpoint (lb1);
        } finally {
            if (support != null) support.doFinish ();
        }
    }

    // innerclasses ............................................................
    
    private class TestBreakpointListener implements JPDABreakpointListener {

        private LineBreakpoint  lineBreakpoint;
        private int             conditionResult;

        private JPDABreakpointEvent event;
        private AssertionError      failure;

        public TestBreakpointListener (LineBreakpoint lineBreakpoint) {
            this (lineBreakpoint, JPDABreakpointEvent.CONDITION_NONE);
        }

        public TestBreakpointListener (
            LineBreakpoint lineBreakpoint, 
            int conditionResult
        ) {
            this.lineBreakpoint = lineBreakpoint;
            this.conditionResult = conditionResult;
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
            this.event = event;
            assertEquals (
                "Breakpoint event: Wrong source breakpoint", 
                lineBreakpoint, 
                event.getSource ()
            );
            assertNotNull (
                "Breakpoint event: Context thread is null", 
                event.getThread ()
            );

            int result = event.getConditionResult ();
            if ( result == JPDABreakpointEvent.CONDITION_FAILED && 
                 conditionResult != JPDABreakpointEvent.CONDITION_FAILED
            )
                failure = new AssertionError (event.getConditionException ());
            else 
            if (result != conditionResult)
                failure = new AssertionError (
                    "Unexpected breakpoint condition result: " + result
                );
        }

        public void checkResult () {
            if (event == null) {
                CallStackFrame f = support.getDebugger ().
                    getCurrentCallStackFrame ();
                int ln = -1;
                if (f != null) {
                    ln = f.getLineNumber (null);
                }
                throw new AssertionError (
                    "Breakpoint was not hit (listener was not notified) " + ln
                );
            }
            if (failure != null) throw failure;
        }
        
        public void checkNotNotified() {
            if (event != null) {
                JPDAThread t = event.getThread();
                throw new AssertionError (
                    "Breakpoint was hit (listener was notified) in thread " + t
                );
            }
            if (failure != null) throw failure;
        }
    }
}

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

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;


/**
 * Tests JPDA stepping actions: step in, step out and step over.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class StepTest extends NbTestCase {

    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();
    private String          sourceRoot = System.getProperty ("test.dir.src");
    private JPDASupport     support;

    public StepTest (String s) {
        super (s);
    }

    public void testStepOver () throws Exception {
        try {
            JPDASupport.removeAllBreakpoints ();
            LineBreakpoint lb = LineBreakpoint.create (
                sourceRoot + 
                    "org/netbeans/api/debugger/jpda/testapps/StepApp.java",
                30
            );
            dm.addBreakpoint (lb);
            support = JPDASupport.attach
                ("org.netbeans.api.debugger.jpda.testapps.StepApp");
            support.waitState (JPDADebugger.STATE_STOPPED);
            dm.removeBreakpoint (lb);
            assertEquals (
                "Execution stopped in wrong class", 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getClassName (), 
                "org.netbeans.api.debugger.jpda.testapps.StepApp"
            );
            assertEquals (
                "Execution stopped at wrong line", 
                30, 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getLineNumber (null)
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                31
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                32
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                33
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                34
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                35
            );
            support.doContinue ();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            support.doFinish ();
        }
    }

    public void testStepInto () throws Exception {
        try {
            JPDASupport.removeAllBreakpoints ();
            LineBreakpoint lb = LineBreakpoint.create (
                sourceRoot + 
                    "org/netbeans/api/debugger/jpda/testapps/StepApp.java",
                30
            );
            dm.addBreakpoint (lb);
            support = JPDASupport.attach
                ("org.netbeans.api.debugger.jpda.testapps.StepApp");
            support.waitState (JPDADebugger.STATE_STOPPED);
            dm.removeBreakpoint (lb);
            assertEquals (
                "Execution stopped in wrong class", 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getClassName (), 
                "org.netbeans.api.debugger.jpda.testapps.StepApp"
            );
            assertEquals (
                "Execution stopped at wrong line", 
                30, 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getLineNumber (null)
            );

            stepCheck (
                ActionsManager.ACTION_STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                38
            );
//            stepCheck (ActionsManager.ACTION_STEP_INTO, "java.lang.Object", -1);
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                39
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                30
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                31
            );
            stepCheck (
                ActionsManager.ACTION_STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                42
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                43
            );
            stepCheck (
                ActionsManager.ACTION_STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                48
            );

            support.doContinue ();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            support.doFinish ();
        }
    }

    public void testStepOut () throws Exception {
        try {
            JPDASupport.removeAllBreakpoints ();
            LineBreakpoint lb = LineBreakpoint.create (
                sourceRoot + 
                    "org/netbeans/api/debugger/jpda/testapps/StepApp.java",
                30
            );
            dm.addBreakpoint (lb);
            support = JPDASupport.attach
                ("org.netbeans.api.debugger.jpda.testapps.StepApp");
            support.waitState (JPDADebugger.STATE_STOPPED);
            dm.removeBreakpoint (lb);
            assertEquals (
                "Execution stopped in wrong class", 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getClassName (), 
                "org.netbeans.api.debugger.jpda.testapps.StepApp"
            );
            assertEquals (
                "Execution stopped at wrong line", 
                30, 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getLineNumber (null)
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                31
            );
            stepCheck (
                ActionsManager.ACTION_STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                42
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                43
            );
            stepCheck (
                ActionsManager.ACTION_STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                48
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OUT, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                43
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OUT, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                31
            );

            support.doContinue ();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            support.doFinish ();
        }
    }

    private void stepCheck (
        Object stepType, 
        String clsExpected, 
        int lineExpected
    ) {
        support.step (stepType);
        assertEquals(
            "Execution stopped in wrong class", 
            clsExpected, 
            support.getDebugger ().getCurrentCallStackFrame ().getClassName ()
        );
        assertEquals (
            "Execution stopped at wrong line", 
            lineExpected, 
            support.getDebugger ().getCurrentCallStackFrame ().
                getLineNumber (null)
        );
    }
}

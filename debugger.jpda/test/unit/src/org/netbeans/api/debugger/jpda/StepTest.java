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
 * Tests JPDA stepping actions: step in, step out and step over.
 *
 * @author Maros Sandor
 */
public class StepTest extends DebuggerJPDAApiTestBase {

    private static final int STEP_INTO = 0;
    private static final int STEP_OVER = 1;
    private static final int STEP_OUT  = 3;

    private JPDASupport support;

    public StepTest(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        support = JPDASupport.listen("org.netbeans.api.debugger.jpda.testapps.StepApp");
    }

    public void testStepOver() throws Exception {
        try {
            int line;

            line = support.getDebugger().getCurrentCallStackFrame().getLineNumber(null);
            String cls = support.getDebugger().getCurrentCallStackFrame().getClassName();
            assertEquals("Execution stopped in wrong class", cls, "org.netbeans.api.debugger.jpda.testapps.StepApp");
            assertEquals("Execution stopped at wrong line", 24, line);

            stepCheck(STEP_OVER, "org.netbeans.api.debugger.jpda.testapps.StepApp", 25);
            stepCheck(STEP_OVER, "org.netbeans.api.debugger.jpda.testapps.StepApp", 26);
            stepCheck(STEP_OVER, "org.netbeans.api.debugger.jpda.testapps.StepApp", 27);
            stepCheck(STEP_OVER, "org.netbeans.api.debugger.jpda.testapps.StepApp", 28);
            stepCheck(STEP_OVER, "org.netbeans.api.debugger.jpda.testapps.StepApp", 29);

            support.doContinue();
            support.waitDisconnected(5000);
        } finally {
            support.doFinish();
        }
    }

    public void testStepInto() throws Exception {
        try {
            support = JPDASupport.listen("org.netbeans.api.debugger.jpda.testapps.StepApp");

            int line = support.getDebugger().getCurrentCallStackFrame().getLineNumber(null);
            String cls = support.getDebugger().getCurrentCallStackFrame().getClassName();
            assertEquals("Execution stopped in wrong class", cls, "org.netbeans.api.debugger.jpda.testapps.StepApp");
            assertEquals("Execution stopped at wrong line", 24, line);

            stepCheck(STEP_INTO, "org.netbeans.api.debugger.jpda.testapps.StepApp", 32);
            stepCheck(STEP_INTO, "java.lang.Object", -1);
            stepCheck(STEP_INTO, "org.netbeans.api.debugger.jpda.testapps.StepApp", 33);
            stepCheck(STEP_INTO, "org.netbeans.api.debugger.jpda.testapps.StepApp", 24);
            stepCheck(STEP_INTO, "org.netbeans.api.debugger.jpda.testapps.StepApp", 25);
            stepCheck(STEP_INTO, "org.netbeans.api.debugger.jpda.testapps.StepApp", 36);
            stepCheck(STEP_INTO, "org.netbeans.api.debugger.jpda.testapps.StepApp", 37);
            stepCheck(STEP_INTO, "org.netbeans.api.debugger.jpda.testapps.StepApp", 42);

            support.doContinue();
            support.waitDisconnected(5000);
        } finally {
            support.doFinish();
        }
    }

    public void testStepOut() throws Exception {
        try {
            support = JPDASupport.listen("org.netbeans.api.debugger.jpda.testapps.StepApp");

            int line = support.getDebugger().getCurrentCallStackFrame().getLineNumber(null);
            String cls = support.getDebugger().getCurrentCallStackFrame().getClassName();
            assertEquals("Execution stopped in wrong class", cls, "org.netbeans.api.debugger.jpda.testapps.StepApp");
            assertEquals("Execution stopped at wrong line", 24, line);

            stepCheck(STEP_OVER, "org.netbeans.api.debugger.jpda.testapps.StepApp", 25);
            stepCheck(STEP_INTO, "org.netbeans.api.debugger.jpda.testapps.StepApp", 36);
            stepCheck(STEP_OVER, "org.netbeans.api.debugger.jpda.testapps.StepApp", 37);
            stepCheck(STEP_INTO, "org.netbeans.api.debugger.jpda.testapps.StepApp", 42);

            stepCheck(STEP_OUT, "org.netbeans.api.debugger.jpda.testapps.StepApp", 37);
            stepCheck(STEP_OUT, "org.netbeans.api.debugger.jpda.testapps.StepApp", 25);

            support.doContinue();
            support.waitDisconnected(5000);
        } finally {
            support.doFinish();
        }
    }

    private void stepCheck(int stepType, String clsExpected, int lineExpected) {
        switch (stepType) {
        case STEP_INTO:
            support.stepInto();
            break;
         case STEP_OVER:
            support.stepOver();
            break;
         case STEP_OUT:
            support.stepOut();
            break;
        }
        String cls = support.getDebugger().getCurrentCallStackFrame().getClassName();
        int line = support.getDebugger().getCurrentCallStackFrame().getLineNumber(null);
        assertEquals("Execution stopped in wrong class", clsExpected, cls);
        if (lineExpected != -1) assertEquals("Execution stopped at wrong line", lineExpected, line);
    }

}

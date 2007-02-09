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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda;

import java.util.List;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;


/**
 * Tests JPDA expression stepping action.
 *
 * @author Martin Entlicher, Jan Jancura
 */
public class ExpressionStepTest extends NbTestCase {

    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();
    private String          sourceRoot = System.getProperty ("test.dir.src");
    private JPDASupport     support;

    public ExpressionStepTest (String s) {
        super (s);
    }

    public void testExpressionStep() throws Exception {
        try {
            JPDASupport.removeAllBreakpoints ();
            LineBreakpoint lb = LineBreakpoint.create (
                Utils.getURL(sourceRoot + 
                    "org/netbeans/api/debugger/jpda/testapps/ExpressionStepApp.java"),
                30
            );
            dm.addBreakpoint (lb);
            support = JPDASupport.attach
                ("org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp");
            support.waitState (JPDADebugger.STATE_STOPPED);
            dm.removeBreakpoint (lb);
            assertEquals (
                "Execution stopped in wrong class", 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getClassName (), 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp"
            );
            assertEquals (
                "Execution stopped at wrong line", 
                30, 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getLineNumber (null)
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                30, 14,
                "factorial"
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                31, 14,
                "factorial",
                new Object[] {"3628800"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                31, 30,
                "factorial",
                new Object[] {"2432902008176640000"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                32, 14,
                "factorial",
                new Object[] {"2432902008176640000", "-8764578968847253504"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                32, 34,
                "factorial",
                new Object[] {"-70609262346240000"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                33, 37,
                "<init>", // "ExpressionStepApp",
                new Object[] {"-70609262346240000", "-3258495067890909184"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                34, 20,
                "m2"
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                34, 13,
                "m1",
                new Object[] {"-899453552"}
            );
            
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                35, 27,
                "m2",
                new Object[] {"-899453552", "-404600928"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                35, 20,
                "m1",
                new Object[] {"497916032"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                35, 27,
                "m1",
                new Object[] {"497916032", "684193024"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                35, 13,
                "m3",
                new Object[] {"497916032", "684193024", "248958016"}
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OPERATION, 
                "org.netbeans.api.debugger.jpda.testapps.ExpressionStepApp", 
                35, 13,
                "intValue",
                new Object[] {"497916032", "684193024", "248958016", "933151070"}
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
        int lineExpected,
        int column,
        String methodName
    ) {
        try {
            // We need to wait for all listeners to be notified and appropriate
            // actions to be enabled/disabled
            Thread.currentThread().sleep(10);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
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
        if (column > 0) {
            Operation op = support.getDebugger ().getCurrentCallStackFrame ().getCurrentOperation(null);
            assertNotNull(op);
            assertEquals("Execution stopped at a wrong column", column, op.getMethodStartPosition().getColumn());
        }
        if (methodName != null) {
            Operation op = support.getDebugger ().getCurrentCallStackFrame ().getCurrentOperation(null);
            assertNotNull(op);
            assertEquals("Execution stopped at a wrong method call", methodName, op.getMethodName());
        }
    }
    
    private void stepCheck (
        Object stepType, 
        String clsExpected, 
        int lineExpected,
        int column,
        String methodName,
        Object[] returnValues
    ) {
        stepCheck(stepType, clsExpected, lineExpected, column, methodName);
        List<Operation> ops = support.getDebugger ().getCurrentThread().getLastOperations();
        assertEquals("Different count of last operations and expected return values.", returnValues.length, ops.size());
        for (int i = 0; i < returnValues.length; i++) {
            Variable rv = ops.get(i).getReturnValue();
            if (rv != null) {
                assertEquals("Bad return value", returnValues[i], rv.getValue());
            }
        }
    }
    
}
